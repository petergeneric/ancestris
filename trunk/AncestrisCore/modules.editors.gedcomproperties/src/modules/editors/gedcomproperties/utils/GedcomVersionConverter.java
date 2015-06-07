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
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<Property> invalidPropsInvalidTags;           // Will store properties with invalid tags, and not starting with "_"
    private List<Property> invalidPropsMultipleTags;          // Will store properties which should be singleton and are not, and not starting with "_"
    private Map<Property, String[]> invalidPropsMissingTags;  // Will store properties where a child tag is missing. Map is list of missing tags for that parent property

    public GedcomVersionConverter(Gedcom gedcom, String fromGrammar, String toGrammar) {
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
    private final Set<String> MOVED_TAGS = new HashSet<String>(Arrays.asList("FORM"));
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
                // if entity OBJE, create OBJE:FILE and move from OBJE:FORM to OBJE:FILE:FORM
                Property p;
                if (entityTag.equals(Gedcom.OBJE)) {
                    p = parent.addProperty("FILE", "");
                } else { // if entity OBJE link, OBJE:FILE exists, just move from OBJE:FORM to OBJE:FILE:FORM
                    p = parent.getProperty("FILE");
                    if (p == null) {
                        p = parent.addProperty("FILE", "");
                    }
                }
                Property newProp = p.addProperty("FORM", prop.getValue());
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
                // if entity OBJE, move from OBJE:FILE:FORM to OBJE:FORM and delete OBJE:FILE:FORM
                Property p = parent.getParent();
                Property newProp = p.addProperty("FORM", prop.getValue());
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
        return isConvertible;
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
                list.sort(null);            // somethimes users get java.lang.NoSuchMethodError: java.util.List.sort(Ljava/util/Comparator;)V
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
        list.sort(null);
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
        list.sort(null);
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
            for (Tab tabElement : subtree) {
                isLeaf = tabElement.fromProp.getNoOfProperties() == 0;
                if (isLeaf) {
                    tabElement.fromProp.getParent().delProperty(tabElement.fromProp);
                    invalidPropsInvalidTags.remove(tabElement.fromProp);
                    invalidPropsInvalidTags.add(tabElement.toProp);
                    subtree.remove(tabElement);
                }
            }
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
