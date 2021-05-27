/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importlegacy_name;
import static ancestris.modules.imports.gedcom.Bundle.importlegacy_note;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
/**
 * The import function for ImportLegacy
 */
@NbBundle.Messages({
    "importlegacy.name=Legacy Family Tree",
    "importlegacy.note=This file has been modified by Ancestris Legacy Import module."
})
@ServiceProvider(service = Import.class)
public class ImportLegacy extends Import {

    // Place records
    protected static Pattern tag_plac_pattern = Pattern.compile("(PLAC|FORM|FONE|TYPE|ROMN|MAP|LATI|LONG|NOTE|CONC|CONT)");
    private static String PLACE_RECORD_TAG = "_PLAC_DEFN";
    private static HashMap<String, ImportPlace> hashPlaces;

    // Event records
    private static String EVENT_RECORD_TAG = "_EVENT_DEFN";
    private static HashMap<String, String> hashEventsToNoteID;
    private int    eventIndex = 0;
    
    // Todo records
    private static String TODO_RECORD_TAG = "_TODO";
    private int    todoIndex = 0;
    
    // Hashtag records
    private static String HASHTAG_TAG = "_HASHTAG";
    private static String HASHTAG_RECORD_TAG = "_HASHTAG_DEFN";
    private int    hashtagIndex = 0;
    
    // Story records
    private static String STORY_RECORD_TAG = "_STORY";
    
    
    /**
     * Constructor
     */
    public ImportLegacy() {
        super();
        hashPlaces = new HashMap<String, ImportPlace>();
        hashEventsToNoteID = new HashMap<String, String>();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importlegacy_name();
    }

    @Override
    protected String getImportComment() {
        return importlegacy_note();
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();

        invalidPaths.add("ADDR:MAP");
        invalidPaths.add("ADDR:MAP:LATI");
        invalidPaths.add("ADDR:MAP:LONG");
        invalidPaths.add("ADDR:OBJE");
        invalidPaths.add("ADDR:OBJE:FILE");
        invalidPaths.add("ADDR:OBJE:FORM");
        invalidPaths.add("FAM:_STAT:DATE");

        hashPlaces.clear();
        hashEventsToNoteID.clear();
        eventIndex =0;
        todoIndex = 0;
        
    }

    
    /**
     * **** 1 ***
     * - Run generic code
     * - Memorise all place records (ImportLegacy creates places as records like _PLAC_DEFN ; we need to re-include them later for each corresponding PLAC tags)
     * - Prepare links between event type and _EVENT_DEFN records
     */
    @Override
    protected void firstPass() {
        super.firstPass();
        

        // Memorise Place records content
        if (input.getLevel() == 0 && input.getTag().equals(PLACE_RECORD_TAG)) {
            String key = "";
            ImportPlace importPlace = null;
            String tag = "";

            try {
                input.getNextLine(false);  // stay on current line
                key = input.getValue();
                if (!hashPlaces.containsKey(key)) {
                    importPlace = new ImportPlace();
                    hashPlaces.put(key, importPlace);
                }
                importPlace = hashPlaces.get(key);
                importPlace.used = false;
                while (input.getLevel() != 0) {
                    input.getNextLine(true);   // read next line and move to next line
                    tag = input.getTag();
                    if (!tag.startsWith("_") && !tag_plac_pattern.matcher(tag).matches()) {
                        tag = "_" + tag;
                    }
                    importPlace.addLine(input.getLevel()-1, tag, input.getValue()); // memorise line
                    input.getNextLine(false);  // read next line and stay on current line
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "error.unknown") + e);
                return;
            }
            return;
        }
        
        
        // Prepare links between event type and _EVENT_DEFN records
        // if line is an event record, prepare note ID that will be used to replace event with a note
        if (input.getLevel() == 0 && input.getTag().equals(EVENT_RECORD_TAG)) {
            eventIndex++;
            hashEventsToNoteID.put(input.getValue(), "E" + eventIndex);
            return;
        }
    }

    /**
     * *** 2 ***
     * - Run generic code
     * 
     * - For each PLAC tag, write memorised lines of a _PLAC_DEFN record
     * 
     * - For each event type for which a record exist, add a note pointer underneath it
     * - For each _EVENT_DEFN record level 0, replace with new NOTEs @Exxx@
     * 
     * - For each _TODO record level 0, replace with new NOTEs @Txxx@
     * 
     * - For each date, check that it is valid
     * 
     * - Check invalid tags
     * 
     * - Quit at each fix if true
     */
    @Override
    protected boolean process() throws IOException {
        
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        
        //
        // Preempt changes to non existing paths before super.process() is performed
        //
        
        // Change tag that are at invalid tag locations and that we know how to rename them (the standard process would rename it)
        if ("FAM:BLES".equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with MARR
            // Remember input value before call to ProcessEventValues
            int level = input.getLevel();
            TagPath newTagPath = new TagPath(input.getPath().getParent().getShortName()+":MARR");
            String valueBefore = input.getValue();
            String xref = currentXref;
            // Call for fixing other potential issues
            if (processEventValues(newTagPath)) {
                return true;
            }
            // Line was ok except for this issue, so write it
            output.writeLine(level, newTagPath.getLast(), valueBefore);
            fixes.add(new ImportFix(xref, "invalidTagLocation.2", pathBefore, newTagPath.getShortName(), valueBefore, valueBefore));
            return true;
        }
        
        if ("SOUR:MEDI".equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with SOUR:NOTE
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "NOTE", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.2", pathBefore, path.getParent().getShortName()+":NOTE", valueBefore, valueBefore));
            return true;
        }
        
        if ("FAM:_MARRIED".equalsIgnoreCase(pathBefore)) {  // valid sub tag but there is better, replace with "EVEN value"
            String valueBefore = input.getValue();
            String valueAfter = "_MARRIED " + input.getValue();
            output.writeLine(input.getLevel(), "EVEN", valueAfter);
            fixes.add(new ImportFix(currentXref, "invalidTag.3", pathBefore, path.getParent().getShortName()+":EVEN", valueBefore, valueAfter));
            return true;
        }
        
        
        
        
        
        ////////////////////
        // STORY_RECORD_TAG:
        ////////////////////
        // Do before process otherwise subtags might be processed during process
        // For each _STORY tag, replace with NOTE
        if (STORY_RECORD_TAG.equals(input.getTag())) {  // this one is both the "0 @Y2@ _STORY" line and the "1 _STORY @Y2@" line
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), input.getXref(), "NOTE", valueBefore);
            String pathAfter = input.getLevel() == 0 ? "NOTE" : path.getParent().getShortName()+":NOTE";
            fixes.add(new ImportFix(currentXref, "invalidEntity.01", pathBefore, pathAfter, valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":TITL").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONC", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":DATE").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONT", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":PLAC").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONT", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONT", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT:CONC").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONC", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONC", valueBefore, valueBefore));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT:CONT").equalsIgnoreCase(pathBefore)) {  // invalid tag here, replace with "1 CONC"
            String valueBefore = input.getValue();
            output.writeLine(1, "CONT", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }
        
        // Process other non existing paths
        if (super.processInvalidPath(input.getPath())) {
            return true;
        }
        
        // For each date, check that it is valid and repeat code of Import on invalid dates.  (bypasses super.process())
        // (remove dots, translate some anomalies in Danish, etc.)
        if ("DATE".equals(input.getTag())) {
            String valueBefore = input.getValue();
            String date = valueBefore.toLowerCase().replace(".", "").replace("okt", "OCT").replace("maj", "MAY").replace("eft", "AFT").replace("omkr", "ABT").replace("omk", "ABT")
                    .replace("før", "BEF").replace("ansl", "EST").replace("ansl", "EST").replace("fra", "FROM").replace("til", "TO").replace("mel", "BET").replace("og", "AND").toUpperCase();
            if (!valueBefore.equals(date)) {
                // Includes routine from Import.java
                if (processInvalidDates(date, "invalidDate.2")) {
                    return true;
                }
            }
        }
        
        
        // Rest of the process
        if (super.process()) {
            return true;
        }
        
        ////////////////////
        // INVALID RECORDS:
        ////////////////////
        
        ////////////////////
        // PLACE_RECORD_TAG:
        ////////////////////
        // Attach PLAC records
        // For each PLAC tag in an event (level >= 2), write memorised lines of a _PLAC_DEFN record
        if ("PLAC".equals(input.getTag()) && input.getLevel() > 1) {
            int rootlevel = input.getLevel();
            String key = input.getValue();
            ImportPlace importPlace = hashPlaces.get(key);
            if (importPlace != null) {
                importPlace.used = true;
                for (int i = 0 ; i < importPlace.levels.size() ; i++) {
                    output.writeLine(rootlevel + importPlace.levels.get(i), importPlace.tags.get(i), importPlace.values.get(i));
                }
                fixes.add(new ImportFix(currentXref, "invalidEntity.06", pathBefore, pathBefore+":..", key, key));
            } else {
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            }
            return true;
        }
        
        
        // Remove all PLAC records. Non used PLACes will be transformed before the end to NOTE entities
        if (input.getLevel() == 0 && input.getTag().equals(PLACE_RECORD_TAG)) {
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() != 0) {
                input.getNextLine(true);   // consume line and do nothing
                input.getNextLine(false);  // read next line and stay on it
            }
            return true;
        }

        
        
        ////////////////////
        // EVENT_RECORD_TAG:
        ////////////////////

        // Replace _EVENT_DEFN records by NOTE records
        // For each _EVENT_DEFN record level 0, replace with new NOTEs @Exxx@
        if (input.getLevel() == 0 && input.getTag().equals(EVENT_RECORD_TAG)) {
            String valueBefore = input.getValue();
            String xref = hashEventsToNoteID.get(valueBefore);
            output.writeLine(input.getLevel(), xref, "NOTE", valueBefore);
            // Write rest of record changing tags to user defined tags.
            String tag = "";
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() != 0) {
                input.getNextLine(true);   // read next line and move to next line
                tag = input.getTag();
                if (!tag.startsWith("_")) {
                    tag = "_" + tag;
                }
                output.writeLine(input.getLevel(), tag, input.getValue());
                input.getNextLine(false);  // read same line and stay on current line
            }
            fixes.add(new ImportFix(xref, "invalidEntity.08", pathBefore, "NOTE:..", valueBefore, valueBefore));
            return true;
        }
        
        // Attach pointer to NOTE records which replaces the EVEN record
        // For each event type for which a record exist, add a note pointer at next line at same level as event
        if ("TYPE".equals(input.getTag()) && input.getLevel() == 2) {
            output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            String xref = hashEventsToNoteID.get(input.getValue());
            if (xref != null) {
                String valueAfter = "@" + xref + "@";
                output.writeLine(input.getLevel(), "NOTE", valueAfter);
                fixes.add(new ImportFix(currentXref, "invalidEntity.09", pathBefore, path.getParent().getShortName()+":NOTE", "", valueAfter));
            }
            return true;
        }
        

        ////////////////////
        // HASHTAG_RECORD_TAG:
        ////////////////////
        
        // For each HASHTAG record, replace with NOTE record
        if (input.getLevel() == 0 && input.getTag().equals(HASHTAG_RECORD_TAG)) {
            hashtagIndex++;
            String xref = "HT" + hashtagIndex;
            output.writeLine(input.getLevel(), xref, "NOTE", input.getValue());
            // Write rest of record changing tags to user defined tags.
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() != 0) {
                input.getNextLine(true);   // read next line and move to next line
                output.writeLine(input.getLevel(), "CONT", input.getValue());
                input.getNextLine(false);  // read same line and stay on current line
            }
            fixes.add(new ImportFix(xref, "invalidEntity.03", pathBefore, "NOTE:..", "", ""));
            return true;
        }

        
        // For each HASHTAG tag, replace with NOTE tag
        if (HASHTAG_TAG.equalsIgnoreCase(input.getTag())) {
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "NOTE", valueBefore);
            String pathAfter = input.getPath().getParent().getShortName() + ":NOTE";
            fixes.add(new ImportFix(currentXref, "invalidEntity.05", pathBefore, pathAfter, valueBefore, valueBefore));
            return true;
        }
        if (pathBefore.endsWith(HASHTAG_TAG+":NOTE")) {
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "CONT", valueBefore);
            String pathAfter = input.getPath().getParent().getParent().getShortName() + ":NOTE:CONT";
            fixes.add(new ImportFix(currentXref, "invalidEntity.05", pathBefore, pathAfter, valueBefore, valueBefore));
            return true;
        }
        
        
        
        ////////////////////
        // TODO_RECORD_TAG:
        ////////////////////

        // Replaces a TODO record by a NOTE record
        // For each _TODO record level 0 (general note), replace with new NOTEs @Txxx@
        if (input.getLevel() == 0 && input.getTag().equals(TODO_RECORD_TAG)) {
            todoIndex++;
            int level = input.getLevel();
            String value = input.getValue();
            if (value == null || value.isEmpty()) {
                input.getNextLine(false);  // read same line and stay on current line
                value = input.getValue();
            }
            String xref = "T" + todoIndex;
            output.writeLine(level, xref, "NOTE", value);
            // Write rest of record changing tags to user defined tags.
            String tag = "";
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() != 0) {
                input.getNextLine(true);   // read next line and move to next line
                tag = input.getTag();
                if ("DESC".equals(tag)) {  // replace DESC tag by TODO to keep track of the TODO tag.
                    tag = "TODO";
                }
                if (!tag.startsWith("_")) {
                    tag = "_" + tag;
                }
                output.writeLine(input.getLevel(), tag, input.getValue());
                input.getNextLine(false);  // read same line and stay on current line
            }
            fixes.add(new ImportFix(xref, "invalidEntity.10", pathBefore, "NOTE:..", "", ""));
            return true;
        }

        
        // Clean _TODO citation
        // For each _TODO line with level > 0 (enclosed note), replace DESC with _TODO and add underscore to subtags
        if (input.getLevel() > 0 && input.getTag().equals(TODO_RECORD_TAG)) {
            int rootLevel = input.getLevel();
            output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            String tag = "";
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() > rootLevel) {
                input.getNextLine(true);   // read next line and move to next line
                tag = input.getTag();
                if ("DESC".equals(tag)) {  // replace DESC tag by TODO to keep track of the TODO tag.
                    tag = TODO_RECORD_TAG;
                }
                if (!tag.startsWith("_")) {
                    tag = "_" + tag;
                }
                output.writeLine(input.getLevel(), tag, input.getValue());
                input.getNextLine(false);  // read same line and stay on current line
            }
            fixes.add(new ImportFix(currentXref, "invalidEntity.11", pathBefore, pathBefore+":..", "", ""));
            return true;
        }

        return false;
    }
    
    /**
     * *** 3 ***
     * - Run generic code
     * - Write unused places to NOTEs @Pxxx@, using only user-defined tags
     * 
     */
    @Override
    protected void finalise() throws IOException {
        super.finalise();
        
        ////////////////////
        // PLACE_RECORD_TAG:
        ////////////////////
        // Write unused places to NOTEs @Pxxx@
        int p = 0;
        String tag = "";
        for (String key : hashPlaces.keySet()) {
            ImportPlace importPlace = hashPlaces.get(key);
            String valueBefore = importPlace.values.get(0);
            if (!importPlace.used) {
                p++;
                String xref = "P" + p;
                output.writeLine(0, "P" + p, "NOTE", valueBefore);
                for (int i = 0; i < importPlace.levels.size(); i++) {
                    tag = importPlace.tags.get(i);
                    if (!tag.startsWith("_")) {
                        tag = "_" + tag;
                    }
                    output.writeLine(1 + importPlace.levels.get(i), tag, importPlace.values.get(i));
                }
                fixes.add(new ImportFix(xref, "invalidEntity.07", PLACE_RECORD_TAG, "NOTE:..", valueBefore, valueBefore));
            }
        }
    }
    
    
    /**
     * *** 4 ***
     * - Run generic code
     * - <run specific code>
     * - Quit *after* all have been run
     */
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        
        boolean ret = false;
        
        fixFXfields(gedcom); // before fixGedcom.

        ret |= super.fixGedcom(gedcom);
        incrementProgress();
        ret |= fixOther(gedcom);
        incrementProgress();
        
        return ret;
    }

    
    /**
     * *** 5 *** 
     * - Run generic code
     * - <run specific code>
     */
    @Override
    public void complete() {
        super.complete();
    }

    
    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    
    ////////////////////////////  END OF LOGIC /////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    
    
    
    /**
     * Specific code depending on import type after Gedcom is processed
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean fixed = false;
        
        Property[] props = null;

        /**
         * Fix invalid tags in INDIs
         * - BURI:CREM to be replaced by CREM
         * - Move ADDR, PHON, EMAIL, and WWW to a to-be-created RESI event ; at the same time, move the MAP tag underneath ADDR to a PLAC tag with a name made of CITY, STAE, CTRY
         * - ...ADDR:NOTE is invalid, move note one level up
         * 
         */
        Property crem = null;
        for (Entity entity : gedcom.getIndis()) {
            // Fix BURI
            for (Property buri : entity.getProperties("BURI", false)) {
                // If no CREM, continue
                crem = buri.getProperty("CREM", false);
                if (crem == null) {
                    continue;
                }
                // else remove crem
                buri.delProperty(crem);
                String valueBefore = buri.getValue();
                String pathBefore = buri.getPath(true).getShortName();
                // and create a new CREM property at the same position as BURI, and move to it all properties underneath BURI
                crem= entity.addProperty("CREM", valueBefore); //, entity.getPropertyPosition(buri));
                for (Property p : buri.getProperties()) {
                    movePropertiesRecursively(p, crem);
                }
                // delete buri
                entity.delProperty(buri);
                fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.3", pathBefore, crem.getPath(true).getShortName(), valueBefore, valueBefore));
                fixed = true;
            }
        }

        for (Entity entity : gedcom.getIndis()) {
            // Ex: fix INDI:DEAT:CAUS:SOUR to INDI:DEAT:SOUR
            for (Property cause : entity.getAllProperties("CAUS")) {
                props = cause.getProperties();
                for (Property source : props) {
                    String valueBefore = source.getValue();
                    String pathBefore = source.getPath(true).getShortName();
                    String sourceTag = source.getTag();
                    movePropertiesRecursively(source, cause.getParent());
                    Property p = cause.getParent().getProperty(sourceTag, false);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
            }

        }

        for (Entity entity : gedcom.getEntities()) {
            
            for (Property addr : entity.getAllProperties("ADDR")) {
                
                // Please note that, because ADDR might have been incorrectly located, they are not necessarily MultilineProperties because they were grammar invalid at reading time
                
                // Fix MAP in ADDR. move the MAP tag underneath ADDR to a PLAC tag with a name made of CITY, STAE, CTRY
                for (Property map : addr.getAllProperties("MAP")) {
                    String pathBefore = map.getPath(true).getShortName();
                    String city = addr.getPropertyValue("CITY");
                    String stae = addr.getPropertyValue("STAE");
                    String ctry = addr.getPropertyValue("CTRY");
                    String valueAfter = city + PropertyPlace.JURISDICTION_SEPARATOR + stae + PropertyPlace.JURISDICTION_SEPARATOR + ctry;
                    Property plac = addr.getParent().addProperty("PLAC", valueAfter);
                    movePropertiesRecursively(map, plac);
                    Property p = plac.getProperty("MAP", false);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.4", pathBefore, p.getPath(true).getShortName(), "", valueAfter));
                    fixed = true;
                }
                
                // Move notes back to addr parent
                for (Property note : addr.getAllProperties("NOTE")) {
                    String valueBefore = note.getValue();
                    String pathBefore = note.getPath(true).getShortName();
                    movePropertiesRecursively(note, addr.getParent());
                    Property p = addr.getParent().getProperty("NOTE", false);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
                
                // Remove redundant INDI name in addresses:
                // => Move ADR1 value to ADDR and get rid of CONTs and _NAME, 
                //1 ADDR blablabla    => redundant
                //2 CONT street    => redundant
                //2 CONT City CP land    => redundant
                //2 _NAME blablabla    => redundant (name of person)
                //2 ADR1 street
                //2 CITY City
                //2 POST CP
                //2 CTRY land
                Property name = addr.getProperty("_NAME", false);
                if (entity instanceof Indi && name != null) {
                    String lastname = ((Indi)entity).getLastName();
                    if (name.getValue().equals(addr.getValue())) {
                        String valueBefore = addr.getValue();
                        String pathBefore = addr.getPath(true).getShortName();
                        
                        // Get rid of _NAME
                        addr.delProperty(name);
                        
                        if (name.getValue().contains(lastname)) {
                            // move ADR1 value to ADDR
                            Property adr1 = addr.getProperty("ADR1", false);
                            if (adr1 != null) {
                                addr.setValue(adr1.getValue());
                                addr.delProperty(adr1);
                            } else {
                                addr.setValue("");
                            }

                            // get rid of ADR lines
                            for (Property cont : addr.getProperties("CONT", false)) {
                                addr.delProperty(cont);
                            }

                            // Get rid of addr and parent if all empty with no subordinates
                            if (addr.getValue().isEmpty() && addr.getNoOfProperties() == 0) {
                                Property parent = addr.getParent();
                                parent.delProperty(addr);
                                if (parent.getValue().isEmpty() && parent.getNoOfProperties() == 0) {
                                    parent.getParent().delProperty(parent);
                                }
                            }
                        }
                        
                        fixes.add(new ImportFix(entity.getId(), "invalidInformation.1", pathBefore, pathBefore, valueBefore, ""));
                        fixed = true;
                    }
                }
            }
        }

        
       for (Entity entity : gedcom.getEntities()) {
            
            // NOTE:DATE to be replaced with NOTE:_DATE as the date value appears to already be included in the event date tag (redundant)
            // NOTE:SOUR to be replaced with SOUR
            for (Property note : entity.getAllProperties("NOTE")) {
                Property dates[] = note.getProperties("DATE", false);
                for (Property date : dates) {
                    String valueBefore = date.getValue();
                    String pathBefore = date.getPath(true).getShortName();
                    Property p = note.addProperty("_DATE", valueBefore);
                    note.delProperty(date);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.1", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
                for (Property source : note.getAllProperties("SOUR")) {
                    String valueBefore = source.getValue();
                    String pathBefore = source.getPath(true).getShortName();
                    movePropertiesRecursively(source, note.getParent());
                    Property p = note.getParent().getProperty("SOUR", false);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
            }
            
            // Fix invalid TYPE <value>:DATE by replacing with entity:EVEN:TYPE <value> + entity:EVEN:DATE
            for (Property type : entity.getAllProperties("TYPE")) {
                Property date = type.getProperty("DATE");
                if (date != null) {
                    String valueBefore = date.getValue();
                    String pathBefore = date.getPath(true).getShortName();
                    Property even = entity.addProperty("EVEN", "");
                    even.addProperty("TYPE", type.getValue());
                    Property p = even.addProperty("DATE", valueBefore);
                    type.delProperty(date);
                    type.getParent().delProperty(type);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
            }

            // SOUR:DATE invalid when SOUR is an entity ; But in this case, no date exist underneath ; replace with _DATE
            if (entity.getTag().equals("SOUR")) {
                for (Property date : entity.getAllProperties("DATE")) {
                    String valueBefore = date.getValue();
                    String pathBefore = date.getPath(true).getShortName();
                    Property p = entity.addProperty("_DATE", valueBefore);
                    entity.delProperty(date);
                    fixes.add(new ImportFix(entity.getId(), "invalidTagLocation.1", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
            }
        }
        
        return fixed;
    }

    
    /**
     * Migrates NPFX and NSFX to OCCUs
     */
    private boolean fixFXfields(Gedcom gedcom) {
        
        boolean fixed = false;
        
        // 2021-05-20 FL:
        // It appears that Danish translation of Legacy for Name prefix and Name suffix says title prefix and title suffix.
        // Danish users are thus inputing occupations into name title fields. 
        // It seems OK in the Portuguese version.
        // So the hack will be to ask users (when locale is "da"), if they wish to migrate NPFX and NSFX fields to OCCU fields.
        
        // Step 1 : check if we have NPFX and NSFX fields
        int total = 0;
        int fx = 0;
        for (Entity indi : gedcom.getIndis()) {
            total++;
            for (Property name : indi.getProperties("NAME", false)) {
                if (name.getProperties("NPFX", false).length > 0 || name.getProperties("NSFX", false).length > 0) {
                    fx++;
                }
            }
        }
        if (fx == 0) {
            return false; // nothing to do
        }
        
        // Step 2 : check if we speak danish
        boolean danish = Locale.getDefault().getLanguage().equals(new Locale("da").getLanguage());
        
        
        // Only ask the question if Danish or more than 10% prefixes/suffixes in names
        boolean existsFXfields = fx * 100 / total > 10;
        if (!danish && !existsFXfields) {
            return false;
        }

        // Step 3 : Ask the user
        String software = NbBundle.getMessage(getClass(), "importlegacy.name");
        String message = NbBundle.getMessage(getClass(), "importlegacy.issue1", software);
        Object rc = DialogManager.create(NbBundle.getMessage(Import.class, "OpenIDE-Module-Name"), message)
                .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show();

        if (rc != DialogManager.YES_OPTION) {
            return false;
        }
        
        
        // Step 4 : fix it
        for (Entity indi : gedcom.getIndis()) {
            for (Property name : indi.getProperties("NAME", false)) {
                Property npfx = name.getProperty("NPFX", false);
                if (npfx != null) {
                    String valueBefore = npfx.getValue();
                    String pathBefore = npfx.getPath(true).getShortName();
                    Property p = indi.addProperty("OCCU", valueBefore);
                    name.delProperty(npfx);
                    fixes.add(new ImportFix(indi.getId(), "invalidName.9", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
                Property nsfx = name.getProperty("NSFX", false);
                if (nsfx != null) {
                    String valueBefore = nsfx.getValue();
                    String pathBefore = nsfx.getPath(true).getShortName();
                    Property p = indi.addProperty("OCCU", valueBefore);
                    name.delProperty(nsfx);
                    fixes.add(new ImportFix(indi.getId(), "invalidName.9", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                    fixed = true;
                }
            }
        }
        
        return fixed;
        
    }
    
    
    

    
    ////////////////////////////////////////////////////////////////////////////
    //                            FIXING TOOLS                                //
    ////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    private class ImportPlace {

        protected boolean used = false;
        protected List<Integer> levels = new LinkedList<Integer>();
        protected List<String> tags = new LinkedList<String>();
        protected List<String> values = new LinkedList<String>();

        private void addLine(int level, String tag, String value) {
            levels.add(level);
            tags.add(tag);
            values.add(value);
        }
        
    }

    

    

    
    
}
