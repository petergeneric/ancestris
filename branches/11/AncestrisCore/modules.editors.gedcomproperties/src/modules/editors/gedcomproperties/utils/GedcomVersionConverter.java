/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties.utils;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Media;
import genj.gedcom.MetaProperty;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GedcomVersionConverter {

    private final Gedcom gedcom;
    private Exception error = null;
    Grammar fromGrammar = null, toGrammar = null;
    boolean isConvertible = false;
    boolean upgrade = false;
    boolean isMediaConvert = false;

    private List<Property> invalidPropsInvalidTags;           // Will store properties with invalid tags, and not starting with "_"
    private List<Property> invalidPropsMultipleTags;          // Will store properties which should be singleton and are not, and not starting with "_"
    private Map<Property, String[]> invalidPropsMissingTags;  // Will store properties where a child tag is missing. Map is list of missing tags for that parent property

    public GedcomVersionConverter(Gedcom gedcom, String fromGrammar, String toGrammar, String mediaConvert) {
        this.gedcom = gedcom;
        isConvertible = false;
        if (fromGrammar.equals(Grammar.GRAMMAR55) && toGrammar.equals(Grammar.GRAMMAR551)) {
            this.fromGrammar = Grammar.V55;
            this.toGrammar = Grammar.V551;
            upgrade = true;
            isConvertible = true;
        } else if (fromGrammar.equals(Grammar.GRAMMAR551) && toGrammar.equals(Grammar.GRAMMAR55)) {
            this.fromGrammar = Grammar.V551;
            this.toGrammar = Grammar.V55;
            upgrade = false;
            isConvertible = true;
        }
        this.isMediaConvert = mediaConvert.equals("1");

    }

    /**
     * Conversion of Grammar
     *
     * Principle : All invalid elements in current Gedcom file according to new
     * Grammar (future Grammar mistakes) should be changed - If they can, change
     * them according to grammar changes and ensure they are valid in the new
     * grammar after the change (ex : _MAP invalid and known to be changed to
     * MAP) - If they can't, leave them as is and notify user
     *
     * Grammar mistakes looked for: - invalid paths : - isValid(TagPath) :
     * tagPath should exist (error is : Invalid tagpath structure) - invalid
     * cardinality : - isSingleton() : property should not have siblings at same
     * level (error is : Tag should be unique for TagPath of parent) -
     * isRequired() : parent should have this tag as a child (error is : missing
     * Tag for TagPath of parent)
     *
     * For each mistakes that cannot be fixed in new grammar: - if Tag starts
     * with "_" it is allowed (Warning : proprietary tag) - Otherwise, error
     * remains
     *
     *
     * @return
     */
    public boolean convert() {

        if (isConvertible) {
            // Let's define list of properties which will be invalid according to grammar
            invalidPropsInvalidTags = new LinkedList<Property>();
            invalidPropsMultipleTags = new LinkedList<Property>();
            invalidPropsMissingTags = new LinkedHashMap<Property, String[]>();

            // Collect anomalies
            getAnomalies(false);

            // Process anomalies to try to fix them according to new norm
            if (upgrade) {
                upgradeGedcom();
            } else {
                downgradeGedcom();
            }

            // Refresh anomalies
            getAnomalies(true);

            // Return
            if (!invalidPropsInvalidTags.isEmpty() || !invalidPropsMultipleTags.isEmpty() || !invalidPropsMissingTags.isEmpty()) {
                error = new Exception(NbBundle.getMessage(GedcomVersionConverter.class, "ERR_VersionErrors"));
                return false;
            }
        }
        
        if (isMediaConvert) {
            transformMedia();
        }

        return true;
    }

    private void getAnomalies(boolean allowUndescore) {
        // Let's define the list which will store all properties of the gedcom file
        List<Property> listOfProperties = new ArrayList<Property>();                  // Will store all properties except those of header entity

        // Loop on all entities
        List<Entity> allEntities = gedcom.getEntities();
        for (Entity entity : allEntities) {
            if (entity.getTag().equals("HEAD")) {
                continue;
            }
            getPropertiesRecursively(entity, listOfProperties);
        }

        // Loop on all properties to collect anomalies
        invalidPropsInvalidTags.clear();
        invalidPropsMultipleTags.clear();
        invalidPropsMissingTags.clear();

        for (Property property : listOfProperties) {
            // detect invalid tagpath
            if (isInvalidTagPath(toGrammar, property, allowUndescore)) {
                invalidPropsInvalidTags.add(property);
            }
            // detect multiple singleton
            if (isMultipleSingleton(toGrammar, property)) {
                invalidPropsMultipleTags.add(property);
            }
            // detect missing kid tags for this property
            String[] missingTags = getMissingKidTags(toGrammar, property);
            if (missingTags.length != 0) {
                invalidPropsMissingTags.put(property, missingTags);
            }

        }
    }

    /**
     * Upgrade from gedcom 5.5 to Gedcom 5.5.1
     *
     * Tag to be replaced: ------------------- 
     * convert _EMAIL, _FAX, _WWW to EMAIL, FAX, WWW if parent has an ADDR child (ADDR is mandatory) 
     * Convert _TYPE, _FONE, _ROMN to TYPE, FONE, ROMN, FONE:TYPE, ROMN:TYPE if parent is INDI:NAME 
     * Convert _STAT to STAT if parent is FAMC 
     * Convert _MAP, _LATI, _LONG, _FONE, _ROMN to MAP, LATI, LONG, FONE, ROMN if parent is PLAC 
     * Convert _FACT to FACT if parent is INDI 
     * Convert _RESN, _RELI to RESN, RELI if parent is an event 
     * Tag to be moved: ---------------- 
     * Convert OBJE:FORM & OBJE:FILE(1) to OBJE:FILE(n):FORM for OBJE link and for OBJE record 
     * Tag to be deleted: ------------------ 
     * Convert BLOB to _BLOB and subtags to _subtags
     *
     * Note: to change the tag of a property, it needs to be created and deleted
     * (moved) When moving properties around, all descendant properties need to
     * be moved as well otherwise will become orphans
     *
     */
    private final Set<String> REPLACED_TAGS_ADDR = new HashSet<String>(Arrays.asList("_EMAIL", "_FAX", "_WWW"));
    private final Set<String> REPLACED_TAGS_NAME = new HashSet<String>(Arrays.asList("_TYPE", "_FONE", "_ROMN"));
    private final Set<String> REPLACED_TAGS_FAMC = new HashSet<String>(Arrays.asList("_STAT"));
    private final Set<String> REPLACED_TAGS_PLAC = new HashSet<String>(Arrays.asList("_MAP", "_FONE", "_ROMN"));
    private final Set<String> REPLACED_TAGS_MAP = new HashSet<String>(Arrays.asList("_LATI", "_LONG"));
    private final Set<String> REPLACED_TAGS_INDI = new HashSet<String>(Arrays.asList("_FACT"));
    private final Set<String> REPLACED_TAGS_EVEN = new HashSet<String>(Arrays.asList("_RESN", "_RELI"));
    private final Set<String> MOVED_TAGS = new HashSet<String>(Arrays.asList("FORM", "TITL"));
    private final Set<String> REMOVED_TAGS = new HashSet<String>(Arrays.asList("BLOB"));

    private final Set<String> EVENTS = new HashSet<String>(Arrays.asList(
            "ANUL", "ADOP", "BAPM", "BARM", "BASM", "BIRT", "BLES", "BURI", "CAST", "CENS", "CENS", "CHR", "CHRA", "CONF", "CREM",
            "DEAT", "DIV", "DIVF", "DSCR", "EDUC", "EMIG", "ENGA", "EVEN", "FCOM", "GRAD", "IDNO", "IMMI", "MARB", "MARC", "MARL",
            "MARR", "MARS", "NATI", "NATU", "NCHI", "NMR", "OCCU", "ORDN", "PROB", "PROP", "RELI", "RESI", "RETI", "SSN", "TITL", "WILL"
    ));

    private void upgradeGedcom() {

        // Go through invalid tags
        while (!invalidPropsInvalidTags.isEmpty()) {
            Property prop = (Property) ((LinkedList) invalidPropsInvalidTags).removeFirst();
            String entityTag = prop.getEntity().getTag();
            String tag = prop.getTag();
            String newTag = tag.substring(1, tag.length());
            Property parent = prop.getParent();
            String parentTag = parent != null ? parent.getTag() : "";

            if (REPLACED_TAGS_ADDR.contains(tag) && parent.getProperty("ADDR") != null) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_NAME.contains(tag) && parentTag.equals("NAME") && entityTag.equals(Gedcom.INDI)) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_FAMC.contains(tag) && parentTag.equals("FAMC")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_PLAC.contains(tag) && parentTag.equals("PLAC")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_MAP.contains(tag) && (parentTag.equals("MAP") || parentTag.equals("_MAP"))) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_INDI.contains(tag) && parentTag.equals("INDI")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_EVEN.contains(tag) && EVENTS.contains(parentTag)) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (MOVED_TAGS.contains(tag) && parentTag.equals("OBJE")) {
                // Get or create OBJE:FILE if does not already exists, and move prop from OBJE:tag to OBJE:FILE:tag if OBJE is an entity, only for FORM otherwise 
                if (!entityTag.equals(Gedcom.OBJE) && (tag.equals("TITL"))) { // case of OBJE link (not entity) for a TITL
                    continue;
                }
                Property p = parent.getProperty("FILE");
                if (p == null) {
                    p = parent.addProperty("FILE", "");
                }
                Property newProp = p.addProperty(tag, prop.getValue());
                moveSubTree(prop, newProp);
                parent.delProperty(prop);
                continue;
            }
            if (REMOVED_TAGS.contains(tag)) {
                replaceProperty(prop, parent, "_" + tag);
            }
        } // endfor

        // Go through incorrect multiple tags 
        // ==> NOTHING to be done in terms of Grammar changes between 5.5 and 5.5.1) so just comment out codes (futur use maybe!)
        //while (!invalidPropsMultipleTags.isEmpty()) {
        //}
        
        // Go through incorrect missing tags
        // ==> NOTHING to be done in terms of Grammar changes between 5.5 and 5.5.1) 
        // but create missing tags anyway, as empty fields, to make sure file will be compatible with grammar
        List listProp = new LinkedList();
        listProp.addAll(invalidPropsMissingTags.keySet());
        while (!listProp.isEmpty()) {
            Property property = (Property) ((LinkedList) listProp).removeFirst();
            String[] tags = ((String[]) invalidPropsMissingTags.get(property));
            for (int i = 0; i < tags.length; i++) {
                if (property.getProperty(tags[i]) == null) {    // if tag is still missing after previous anomalies correction, correct it
                    property.addProperty(tags[i], "");
                }
            }
        }

    }

    /**
     * Downgrade from gedcom 5.5.1 to Gedcom 5.5
     *
     * Reverse upgrade operations. Leave all remaining tags as "_xxx".
     */
    private void downgradeGedcom() {
        // Go through invalid tags
        while (!invalidPropsInvalidTags.isEmpty()) {
            Property prop = (Property) ((LinkedList) invalidPropsInvalidTags).removeFirst();
            String entityTag = prop.getEntity().getTag();
            String tag = prop.getTag();
            String newTag = "_" + tag;
            Property parent = prop.getParent();
            String parentTag = parent != null ? parent.getTag() : "";

            if (REPLACED_TAGS_ADDR.contains(newTag) && parent.getProperty("ADDR") != null) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_NAME.contains(newTag) && parentTag.equals("NAME") && entityTag.equals(Gedcom.INDI)) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_FAMC.contains(newTag) && parentTag.equals("FAMC")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_PLAC.contains(newTag) && parentTag.equals("PLAC")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_MAP.contains(newTag) && (parentTag.equals("MAP") || parentTag.equals("_MAP"))) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_INDI.contains(newTag) && parentTag.equals("INDI")) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (REPLACED_TAGS_EVEN.contains(newTag) && EVENTS.contains(parentTag)) {
                replaceProperty(prop, parent, newTag);
                continue;
            }
            if (MOVED_TAGS.contains(tag) && parentTag.equals("FILE")) {
                // Move from OBJE:FILE:tag to OBJE:tag and delete OBJE:FILE:tag
                Property obje = parent.getParent();
                Property newProp = obje.addProperty(tag, prop.getValue());
                moveSubTree(prop, newProp);
                parent.delProperty(prop);
                continue;
            }
            if (REMOVED_TAGS.contains(newTag)) {
                replaceProperty(prop, parent, tag);
            }
        } // endfor

        // Go through incorrect multiple tags 
        // ==> NOTHING to be done in terms of Grammar changes between 5.5 and 5.5.1) so just comment out codes (futur use maybe!)
        //while (!invalidPropsMultipleTags.isEmpty()) {
        //}
        // Go through incorrect missing tags
        // ==> NOTHING to be done in terms of Grammar changes between 5.5 and 5.5.1) 
        // but create missing tags anyway, as empty fields, to make sure file will be compatible with grammar
        List listProp = new LinkedList();
        listProp.addAll(invalidPropsMissingTags.keySet());
        while (!listProp.isEmpty()) {
            Property property = (Property) ((LinkedList) listProp).removeFirst();
            String[] tags = ((String[]) invalidPropsMissingTags.get(property));
            for (int i = 0; i < tags.length; i++) {
                if (property.getProperty(tags[i]) == null) {    // if tag is still missing after previous anomalies correction, correct it
                    property.addProperty(tags[i], "");
                }
            }
        }

    }

    public boolean isConvertible() {
        return isConvertible || isMediaConvert;
    }

    public boolean isWithError() {
        return error != null;
    }

    public String[] getInvalidPropsInvalidTags() {
        if (invalidPropsInvalidTags == null) {
            return null;
        }
        List<String> list = new ArrayList();
        for (Property prop : invalidPropsInvalidTags) {
            list.add(prop.getPath().toString() + " (" + prop.getEntity().getId() + ") - " + prop.getDisplayValue());
        }
        if (!list.isEmpty()) {
            try {
                Collections.sort(list,null);            // somethimes users get java.lang.NoSuchMethodError: java.util.List.sort(Ljava/util/Comparator;)V
            } catch (NoSuchMethodError e) { // so I disconsider the exceptions here.
            }                     
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getInvalidPropsMultipleTags() {
        if (invalidPropsMultipleTags == null) {
            return null;
        }
        List<String> list = new ArrayList();
        for (Property prop : invalidPropsMultipleTags) {
            list.add(prop.getPath().toString() + " (" + prop.getEntity().getId() + ") - " + prop.getDisplayValue());
        }
        Collections.sort(list,null);
        return list.toArray(new String[list.size()]);
    }

    public String[] getInvalidPropsMissingTags() {
        if (invalidPropsMissingTags == null) {
            return null;
        }
        List<String> list = new ArrayList();
        for (Property prop : invalidPropsMissingTags.keySet()) {
            String[] arr = ((String[]) invalidPropsMissingTags.get(prop));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                String s = arr[i];
                builder.append(s);
                if (i != arr.length - 1) {
                    builder.append(", ");
                }
            }
            String value = prop.getDisplayValue().trim();
            if (value.isEmpty()) {
                value = NbBundle.getMessage(GedcomVersionConverter.class, "VAL_EmptyValue");
            }
            list.add(prop.getPath().toString() + " (" + prop.getEntity().getId() + ") - " + value + " : " + builder.toString());
        }
        Collections.sort(list,null);
        return list.toArray(new String[list.size()]);
    }

    private boolean isInvalidTagPath(Grammar toGrammar, Property property, boolean allowUndescore) {
        if (property instanceof PropertyXRef) {
            return false;
        }
        if (allowUndescore && property.getTag().startsWith("_")) {
            return false;
        }
            return !toGrammar.isValid(property.getPath());
    }

    
    private boolean isMultipleSingleton(Grammar toGrammar, Property property) {
        return (toGrammar.getMeta(property.getPath()).isSingleton() && property.getParent().getProperties(property.getTag()).length > 1);
    }

    private String[] getMissingKidTags(Grammar toGrammar, Property parent) {
        List<String> listOfTags = new ArrayList<String>();
        String tag;
        MetaProperty meta = toGrammar.getMeta(parent.getPath());
        MetaProperty[] metas = meta.getNestedChildren();
        for (MetaProperty metaChild : metas) {
            tag = metaChild.getTag();
            if (metaChild.isRequired() && parent.getProperty(tag) == null) {
                listOfTags.add(tag);
            }
        }
        return listOfTags.toArray(new String[listOfTags.size()]);
    }

    public void getPropertiesRecursively(Property parent, List props) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            props.add(child);
            getPropertiesRecursively(child, props);
        }
    }

    private void replaceProperty(Property prop, Property parent, String newTag) {
        Property newProp = parent.addProperty(newTag, prop.getValue());
        moveSubTree(prop, newProp);
        parent.delProperty(prop);
        invalidPropsInvalidTags.remove(prop);
    }

    private void moveSubTree(Property fromProp, Property toProp) {
        // Create and populate temporary table holding linear tree elements
        List<Tab> subtree = new LinkedList<Tab>();
        List<Tab> removedTabs = new LinkedList<Tab>();
        subtree.add(new Tab(fromProp, null, toProp));
        storePropertiesRecursively(fromProp, subtree);

        // Use it to create the duplicate tree while storing duplicated elements in front of their original ones
        Property fromParent;
        for (Tab tabElement : subtree) {
            if (tabElement.parentIndex == null) {
                continue;
            }
            fromParent = subtree.get(tabElement.parentIndex).toProp;
            tabElement.setToProp(fromParent.addProperty(tabElement.fromProp.getTag(), tabElement.fromProp.getValue()));
        }

        // Delete original tree first from leaves 
        subtree.remove(0);
        boolean isLeaf;
        while (!subtree.isEmpty()) {
            removedTabs.clear();
            for (Tab tabElement : subtree) {
                isLeaf = tabElement.fromProp.getNoOfProperties() == 0;
                if (isLeaf) {
                    tabElement.fromProp.getParent().delProperty(tabElement.fromProp);
                    invalidPropsInvalidTags.remove(tabElement.fromProp);
                    invalidPropsInvalidTags.add(tabElement.toProp);
                    removedTabs.add(tabElement);
                }
            }
            subtree.removeAll(removedTabs);
        }

    }

    private void storePropertiesRecursively(Property parent, List<Tab> tree) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            tree.add(new Tab(child, getParentIndexFromChild(tree, child), null));
            storePropertiesRecursively(child, tree);
        }
    }

    private Integer getParentIndexFromChild(List<Tab> tree, Property child) {
        for (Tab tabElement : tree) {
            if (tabElement.fromProp == child.getParent()) {
                return tree.indexOf(tabElement);
            }
        }
        return null;
    }

    
    /**
     * Transform media, notes and sources (for 5.5.1 only)
     * 
     * =======================================================================
     * NOTES FROM :
     * ============
     * NOTE_STRUCTURE:=
     * [
     * n NOTE @<XREF:NOTE>@ {1:1}
     * |
     * n NOTE [<SUBMITTER_TEXT> | <NULL>] {1:1}
     *    +1 [CONC|CONT] <SUBMITTER_TEXT> {0:M}
     * ]
     * 
     * TO:
     * ===
     * NOTE_RECORD:=
     * n @<XREF:NOTE>@ NOTE <SUBMITTER_TEXT> {1:1}
     *    +1 [CONC|CONT] <SUBMITTER_TEXT> {0:M}
     *    +1 REFN <USER_REFERENCE_NUMBER> {0:M}
     *       +2 TYPE <USER_REFERENCE_TYPE> {0:1}
     *    +1 RIN <AUTOMATED_RECORD_ID> {0:1}
     *    +1 <<SOURCE_CITATION>> {0:M}
     *    +1 <<CHANGE_DATE>> {0:1}
     * 
     * ==>  1 Find all notes properties recursively across all entities 
     *      2 If it is not a XREF entity, continue
     *      3    create NOTE record
     *      4    attach it as link in PropertyNote
     *      5    move value to NOTE:value
     * 
     * 
     * =======================================================================
     * MEDIA FROM :
     * ==========
     * MULTIMEDIA_LINK:=
     * n OBJE @<XREF:OBJE>@ {1:1} (FL: no need to convert from here)
     * |
     * n OBJE (FL : to be converted)
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
     *    +2 FORM <MULTIMEDIA_FORMAT> {1:1}
     *       +3 MEDI <SOURCE_MEDIA_TYPE> {0:1}
     * +1 TITL <DESCRIPTIVE_TITLE>
     * 
     * TO:
     * ===
     * MULTIMEDIA_RECORD:=
     * n @XREF:OBJE@ OBJE {1:1}
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
     *    +2 FORM <MULTIMEDIA_FORMAT> {1:1}
     *       +3 TYPE <SOURCE_MEDIA_TYPE> {0:1}
     *    +2 TITL <DESCRIPTIVE_TITLE> {0:1}
     * +1 REFN <USER_REFERENCE_NUMBER> {0:M}
     *    +2 TYPE <USER_REFERENCE_TYPE> {0:1}
     * +1 RIN <AUTOMATED_RECORD_ID> {0:1}
     * +1 <<NOTE_STRUCTURE>> {0:M}
     * +1 <<SOURCE_CITATION>> {0:M}
     * +1 <<CHANGE_DATE>> {0:1}
     * 
     * ==>  1 Find all PropertyFile of gedcom 
     *      2 If it does not belong to a OBJE entity, continue
     *      3    create OBJE record
     *      4    attach it as link PropertyMedia
     *      5    move FILE, FORM, MEDI to OBJE:FILE, OBJE:FILE:FORM, OBJE:FILE:FORM:TYPE
     *      6    move TITL to OBJE:FILE:TITL
     * 
     * 
     * =======================================================================
     * SOURCES FROM :
     * ============
     * SOURCE_CITATION:=
     * [
     * pointer to source record (preferred) (FL: not to be converted)
     * n SOUR @<XREF:SOUR>@ {1:1}
     *    +1 PAGE <WHERE_WITHIN_SOURCE> {0:1}
     *    +1 EVEN <EVENT_TYPE_CITED_FROM> {0:1}
     *       +2 ROLE <ROLE_IN_EVENT> {0:1}
     *    +1 DATA {0:1}
     *       +2 DATE <ENTRY_RECORDING_DATE> {0:1}
     *       +2 TEXT <TEXT_FROM_SOURCE> {0:M}
     *          +3 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
     *    +1 <<MULTIMEDIA_LINK>> {0:M}
     *    +1 <<NOTE_STRUCTURE>> {0:M}
     *    +1 QUAY <CERTAINTY_ASSESSMENT> {0:1}
     * |
     *  Systems not using source records (FL : to be converted)
     * n SOUR <SOURCE_DESCRIPTION> {1:1}
     *    +1 [CONC|CONT] <SOURCE_DESCRIPTION> {0:M}
     *    +1 TEXT <TEXT_FROM_SOURCE> {0:M}
     *       +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
     *    +1 <<MULTIMEDIA_LINK>> {0:M}
     *    +1 <<NOTE_STRUCTURE>> {0:M}
     *    +1 QUAY <CERTAINTY_ASSESSMENT> {0:1}
     * ]
     * 
     * TO:
     * ===
     * SOURCE_RECORD:=
     * n @<XREF:SOUR>@ SOUR {1:1}
     *    +1 DATA {0:1}
     *       +2 EVEN <EVENTS_RECORDED> {0:M}
     *          +3 DATE <DATE_PERIOD> {0:1}
     *          +3 PLAC <SOURCE_JURISDICTION_PLACE> {0:1}
     *       +2 AGNC <RESPONSIBLE_AGENCY> {0:1}
     *       +2 <<NOTE_STRUCTURE>> {0:M}
     *    +1 AUTH <SOURCE_ORIGINATOR> {0:1}
     *       +2 [CONC|CONT] <SOURCE_ORIGINATOR> {0:M}
     *    +1 TITL <SOURCE_DESCRIPTIVE_TITLE> {0:1}
     *       +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE> {0:M}
     *    +1 ABBR <SOURCE_FILED_BY_ENTRY> {0:1}
     *    +1 PUBL <SOURCE_PUBLICATION_FACTS> {0:1}
     *       +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS> {0:M}
     *    +1 TEXT <TEXT_FROM_SOURCE> {0:1}
     *       +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
     *    +1 <<SOURCE_REPOSITORY_CITATION>> {0:M}
     *    +1 REFN <USER_REFERENCE_NUMBER> {0:M}
     *       +2 TYPE <USER_REFERENCE_TYPE> {0:1}
     *    +1 RIN <AUTOMATED_RECORD_ID> {0:1}
     *    +1 <<CHANGE_DATE>> {0:1}
     *    +1 <<NOTE_STRUCTURE>> {0:M}
     *    +1 <<MULTIMEDIA_LINK>> {0:M}
     *  
     * 
     * ==>  1 Find all sources properties recursively across all entities (indi, fam, note, obje)
     *      2 If it is not a XREF entity, continue
     *      3    create SOUR record
     *      4    attach it as link PropertySource
     *      5    move source_description value to SOUR:TITL
     *      6    move source_text to SOUR:TEXT
     *      7    move OBJE link to OBJE link in PropertySource
     *      8    move NOTE link to NOTE link in PropertySource
     *      9    move QUAY to QUAY in PropertySource
     * 
     * =======================================================================
     * 
     */
    

    
    
    private void transformMedia() {
        
        // Get all properties
        List<Property> allProps = new ArrayList<Property>();
        List<Entity> allEntities = gedcom.getEntities();
        for (Entity entity : allEntities) {
            if (entity.getTag().equals("HEAD")) {
                continue; 
            }
            getPropertiesRecursively(entity, allProps);
        }

        // Get all NOTE & OBJE & SOUR to transform
        List<Property> listOfNotes = new ArrayList<Property>();
        List<Property> listOfMedia = new ArrayList<Property>();
        List<Property> listOfSources = new ArrayList<Property>();
        
        for (Property property : allProps) {
            if (property.getTag().equals("NOTE") && !(property instanceof PropertyNote) && (!property.getEntity().getTag().equals(Gedcom.NOTE))) {
                listOfNotes.add(property);
            }
            if (property.getTag().equals("OBJE") && !(property instanceof PropertyMedia) && (!property.getEntity().getTag().equals(Gedcom.OBJE))) {
                listOfMedia.add(property);
            }
            if (property.getTag().equals("SOUR") && !(property instanceof PropertySource) && (!property.getEntity().getTag().equals(Gedcom.SOUR))) {
                listOfSources.add(property);
            }
        }
        
        
        // Transform NOTE
        Map<String, Note> notesMap = new HashMap<String, Note>();
        Note noteRecord = null;
        try {
            for (Property property : listOfNotes) {
                Property parent = property.getParent();
                String itemNote = property.getValue();
                if (!itemNote.isEmpty()) {
                    noteRecord = notesMap.get(itemNote);
                    if (noteRecord == null) {
                        noteRecord = (Note) gedcom.createEntity(Gedcom.NOTE);
                        noteRecord.setValue(property.getValue());
                        notesMap.put(itemNote, noteRecord);
                    }
                    parent.addNote(noteRecord);
                }
                parent.delProperty(property);
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Transform OBJE
        Media mediaRecord = null;
        try {
            for (Property property : listOfMedia) {
                Property parent = property.getParent();
                mediaRecord = (Media) gedcom.createEntity(Gedcom.OBJE);
                Property propTitle = property.getProperty("TITL");
                List<PropertyFile> fileProps = property.getProperties(PropertyFile.class);
                for (PropertyFile fileProp : fileProps) {
                    Property toProp = mediaRecord.addProperty("FILE", fileProp.getValue());
                    if (propTitle != null) {
                        toProp.addProperty("TITL", propTitle.getValue());
                    }
                    Property fromProp = fileProp.getProperty("FORM");  
                    if (fromProp != null) {
                        toProp = toProp.addProperty("FORM", fromProp.getValue());
                        fromProp = fromProp.getProperty("MEDI");
                        if (fromProp != null) {
                            toProp.addProperty("TYPE", fromProp.getValue());
                        }
                    }
                }
                parent.addMedia(mediaRecord);
                parent.delProperty(property);
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }


        
        
        // Transform SOUR
        Source sourceRecord = null;
        try {
            for (Property property : listOfSources) {
                Property parent = property.getParent();
                sourceRecord = (Source) gedcom.createEntity(Gedcom.SOUR);
                Property fromProp = property.getProperty("TEXT");
                if (fromProp != null) {
                    sourceRecord.addProperty("TEXT", fromProp.getValue());
                }
                fromProp = property.getProperty("NOTE");
                if (fromProp != null) {
                    String value = fromProp.getValue();
                    if (value.startsWith("@")) {
                        Property xref = sourceRecord.addProperty("NOTE", value);
                        try {
                            ((PropertyNote) xref).link();
                        } catch (GedcomException e) {
                            sourceRecord.delProperty(xref);
                        }
                    } else {
                        sourceRecord.addProperty("NOTE", value);
                    }
                }
                Property xref = parent.addProperty("SOUR", '@' + sourceRecord.getId() + '@');
                try {
                    ((PropertySource) xref).link();
                } catch (GedcomException e) {
                    parent.delProperty(xref);
                }
                parent.delProperty(property);
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        
        
        
        
        

        
        
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private class Tab {

        public Property fromProp;
        public Integer parentIndex;
        public Property toProp;

        private Tab(Property fromProp, Integer parentIndex, Property toProp) {
            this.fromProp = fromProp;
            this.parentIndex = parentIndex;
            this.toProp = toProp;
        }

        public void setToProp(Property prop) {
            this.toProp = prop;
        }
    }

}
