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
import ancestris.util.GedcomUtilities;
import static ancestris.modules.imports.gedcom.Bundle.importlegacy_name;
import static ancestris.modules.imports.gedcom.Bundle.importlegacy_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import genj.io.InputSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private static String HASHTAG_RECORD_TAG = "_HASHTAG_DEFN";
    
    // Story records
    private static String STORY_RECORD_TAG = "_STORY";
    
    // Flags
    private boolean fixindi = false;
    private boolean fixaddr = false;
    
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
        hashPlaces.clear();
        hashEventsToNoteID.clear();
        eventIndex =0;
        todoIndex = 0;
        fixindi = false;
        fixaddr = false;
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

        // Spot buricrem issues, indi addr issues, etc.
        if ("INDI:BURI:CREM".equalsIgnoreCase(input.getPath().toString())) {  // invalid tag here, will have to be replaced with CREM
            fixindi = true;
        }
        if ("INDI:ADDR".equalsIgnoreCase(input.getPath().toString())) {  // invalid tag here, will have to be fixed
            fixindi = true;
        }
        if (input.getPath().toString().endsWith("ADDR:MAP")) {  // invalid tag here, will have to be fixed
            fixaddr = true;
        }
        if (input.getPath().toString().endsWith("ADDR:NOTE")) {  // invalid tag here, will have to be fixed
            fixaddr = true;
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
        String path = input.getPath().toString();
        
        // Check invalid events (bypasses super.process())
        if ("FAM:BLES".equalsIgnoreCase(path)) {  // invalid tag here, replace with MARR
            // Fix tag
            String tag = "MARR";
            
            // Run default fix YesTag
            String result = null;
            int level = input.getLevel();
            String line = input.getLine();
            if (input.getValue().length() != 0) {
                if (input.getValue().equalsIgnoreCase("y")){
                    output.writeLine(level, tag, input.getValue());
                } else {
                    result = output.writeLine(level, tag, null);
                    result += "\n"+output.writeLine(level+1, "NOTE", input.getValue());
                }
            } else {
                String temp = input.getNextLine(false);
                if ((temp != null) && (input.getLevel() == level + 1)) {
                    output.writeLine(level, tag, null);
                } else {
                    result = output.writeLine(level, tag, "Y");
                }
            }
            
            if (result != null){
                nbChanges++;
                console.println(NbBundle.getMessage(ImportLegacy.class, "Import.fixTagNotAllowed", line + " ==> " + "MARR"));
            }
            return true;
        }

        // For each REPO:ADDR, MAP, LATI, LONG tag, replace with _XXX
        if (("REPO:ADDR:MAP").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_MAP", input.getValue());
            nbChanges++;
            return true;
        }
        if (("REPO:ADDR:MAP:LATI").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_LATI", input.getValue());
            nbChanges++;
            return true;
        }
        if (("REPO:ADDR:MAP:LONG").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_LONG", input.getValue());
            nbChanges++;
            return true;
        }
        if (("REPO:ADDR:OBJE").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_OBJE", input.getValue());
            nbChanges++;
            return true;
        }
        if (("REPO:ADDR:OBJE:FILE").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_FILE", input.getValue());
            nbChanges++;
            return true;
        }
        if (("REPO:ADDR:OBJE:FORM").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_FORM", input.getValue());
            nbChanges++;
            return true;
        }
        
        
        // For each _STORY tag, replace with NOTE
        if (input.getTag().equals(STORY_RECORD_TAG)) {
            output.writeLine(input.getLevel(), input.getXref(), "NOTE", input.getValue());
            nbChanges++;
            return true;
        }
        if ((STORY_RECORD_TAG+":TITL").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONC", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ((STORY_RECORD_TAG+":DATE").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONT", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ((STORY_RECORD_TAG+":PLAC").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONT", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONT", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT:CONC").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONC", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ((STORY_RECORD_TAG+":TEXT:CONT").equalsIgnoreCase(path)) {  // invalid tag here, replace with "1 CONC"
            String result = output.writeLine(1, "CONT", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        
        // For each _HASHTAG:NOTE tag, replace with _NOTE
        if ((HASHTAG_RECORD_TAG+":NOTE").equalsIgnoreCase(path)) {
            output.writeLine(input.getLevel(), "_NOTE", input.getValue());
            nbChanges++;
            return true;
        }
        

        // For each date, check that it is valid and repeat code of Import on invalid dates.  (bypasses super.process())
        // (remove dots, translate some anomalies in Danish, etc.)
        if ("DATE".equals(input.getTag())) {
            String date = input.getValue().toLowerCase().replace(".", "").replace("okt", "Oct").replace("maj", "May").replace("eft", "AFT").replace("omkr", "ABT").replace("omk", "ABT")
                    .replace("før", "BEF").replace("ansl", "EST").replace("ansl", "EST").replace("fra", "FROM").replace("til", "TO").replace("mel", "BET").replace("og", "AND");
            // Includes routine from Import.java
            if (date.contains("/")) {
                date = convertDate(date);
            }
            output.writeLine(input.getLevel(), "DATE", date);
            nbChanges++;
            return true;
        }
        
        // Run default fixes
        if (super.process()) {
            return true;
        }
        
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
            } else {
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            }
            nbChanges++;
            return true;
        }
        
        // For each event type for which a record exist, add a note pointer at next line at same level as event
        if ("TYPE".equals(input.getTag()) && input.getLevel() == 2) {
            output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            String xref = hashEventsToNoteID.get(input.getValue());
            if (xref != null) {
                output.writeLine(input.getLevel(), "NOTE", "@" + xref + "@");
            }
            nbChanges++;
            return true;
        }

        // For each _EVENT_DEFN record level 0, replace with new NOTEs @Exxx@
        if (input.getLevel() == 0 && input.getTag().equals(EVENT_RECORD_TAG)) {
            String xref = hashEventsToNoteID.get(input.getValue());
            output.writeLine(input.getLevel(), xref, "NOTE", input.getValue());
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
            nbChanges++;
            return true;
        }
        
        // For each _TODO record level 0 (general note), replace with new NOTEs @Txxx@
        if (input.getLevel() == 0 && input.getTag().equals(TODO_RECORD_TAG)) {
            todoIndex++;
            int level = input.getLevel();
            String value = input.getValue();
            if (value == null || value.isEmpty()) {
                input.getNextLine(false);  // read same line and stay on current line
                value = input.getValue();
            }
            output.writeLine(level, "T" + todoIndex, "NOTE", value);
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
            nbChanges++;
            return true;
        }

        // For each _TODO record level > 0 (enclosed note), replace DESC with _TODO and add underscore to subtags
        if (input.getLevel() > 0 && input.getTag().equals(TODO_RECORD_TAG)) {
            int rootLevel = input.getLevel();
            output.writeLine(input.getLevel(), input.getTag(), input.getValue());
            String tag = "";
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() > rootLevel) {
                input.getNextLine(true);   // read next line and move to next line
                tag = input.getTag();
                if ("DESC".equals(tag)) {  // replace DESC tag by TODO to keep track of the TODO tag.
                    tag = "_TODO";
                }
                if (!tag.startsWith("_")) {
                    tag = "_" + tag;
                }
                output.writeLine(input.getLevel(), tag, input.getValue());
                input.getNextLine(false);  // read same line and stay on current line
            }
            nbChanges++;
            return true;
        }

        
        // Check invalid tags
        if ("SOUR:MEDI".equalsIgnoreCase(path)) {  // invalid tag here, replace with SOUR:NOTE
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportLegacy.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            nbChanges++;
            return true;
        }
        
        
        
        if (processOther()) {
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
        
        // Write unused places to NOTEs @Pxxx@
        int p = 0;
        String tag = "";
        for (String key : hashPlaces.keySet()) {
            ImportPlace importPlace = hashPlaces.get(key);
            if (!importPlace.used) {
                p++;
                output.writeLine(0, "P" + p, "NOTE", importPlace.values.get(0));
                for (int i = 0; i < importPlace.levels.size(); i++) {
                    tag = importPlace.tags.get(i);
                    if (!tag.startsWith("_")) {
                        tag = "_" + tag;
                    }
                    output.writeLine(1 + importPlace.levels.get(i), tag, importPlace.values.get(i));
                }
            }
            nbChanges++;
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
        boolean ret = fixNames(gedcom);
        ret |= fixOther(gedcom);
        incrementProgress();
        ret |= fixPlaces(gedcom);
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

    ////////////////////////////  END OF LOGIC /////////////////////////////////


    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Specific code depending from import type
     * @return 
     */
    private boolean processOther() {
        return false;
    }

    /**
     * Specific code depending from import type after Gedcom is processed
     * - Fix Media
     * - Fix invalid tags
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean hasErrors = false;
        
        Property[] props = null;
        Property prop = null;
        Property host = null;

        /**
         * Fix invalid tags in INDIs
         * - BURI:CREM to be replaced by CREM
         * - Move ADDR, PHON, EMAIL, and WWW to a to-be-created RESI event ; at the same time, move the MAP tag underneath ADDR to a PLAC tag with a name made of CITY, STAE, CTRY
         * - ...ADDR:NOTE is invalid, move note one level up
         * 
         */
        if (fixindi) {
            Property crem = null;
            for (Entity entity : gedcom.getIndis()) {
                // Fix BURI
                for (Property buri : entity.getProperties("BURI")) {
                    // If no CREM, continue
                    crem = buri.getProperty("CREM");
                    if (crem == null) {
                        continue;
                    }
                    // else remove crem
                    buri.delProperty(crem);
                    // and create a new CREM property at the same position, and move to it all properties underneath BURI
                    try {
                        crem= entity.addProperty("CREM", buri.getValue()); //, entity.getPropertyPosition(buri));
                        for (Property p : buri.getProperties()) {
                            GedcomUtilities.movePropertyRecursively(p, crem);
                        }
                    } catch (GedcomException ex) {
                        continue;
                    }
                    // delete buri
                    entity.delProperty(buri);
                    nbChanges++;
                }
                
                // Fix ADDR
                for (Property addr : entity.getProperties("ADDR")) {
                    prop = entity.getProperty("RESI");
                    if (prop == null) {
                        prop = entity.addProperty("RESI", "");
                    }
                    try {
                        GedcomUtilities.movePropertyRecursively(addr, prop);
                        nbChanges++;
                    } catch (GedcomException ex) {
                        continue;
                    }
                }
                for (Property phon : entity.getProperties("PHON")) {
                    prop = entity.getProperty("RESI");
                    if (prop == null) {
                        prop = entity.addProperty("RESI", "");
                    }
                    prop.addProperty(phon.getTag(), phon.getValue());
                    entity.delProperty(phon);
                    nbChanges++;
                }
                for (Property email : entity.getProperties("EMAIL")) {
                    prop = entity.getProperty("RESI");
                    if (prop == null) {
                        prop = entity.addProperty("RESI", "");
                    }
                    prop.addProperty(email.getTag(), email.getValue());
                    entity.delProperty(email);
                    nbChanges++;
                }
                for (Property www : entity.getProperties("WWW")) {
                    prop = entity.getProperty("RESI");
                    if (prop == null) {
                        prop = entity.addProperty("RESI", "");
                    }
                    prop.addProperty(www.getTag(), www.getValue());
                    entity.delProperty(www);
                    nbChanges++;
                }
                
                // Fix INDI:DEAT:CAUS:SOUR to INDI:DEAT:SOUR
                for (Property cause : entity.getAllProperties("CAUS")) {
                    props = cause.getProperties();
                    for (Property source : props) {
                        try {
                            GedcomUtilities.movePropertyRecursively(source, cause.getParent());
                            nbChanges++;
                        } catch (GedcomException ex) {
                            continue;
                        }
                    }
                }
                
            }
        }

        if (fixaddr) {
            for (Entity entity : gedcom.getEntities()) {
                // Fix MAP in ADDR. move the MAP tag underneath ADDR to a PLAC tag with a name made of CITY, STAE, CTRY
                for (Property addr : entity.getAllProperties("ADDR")) {
                    for (Property map : addr.getAllProperties("MAP")) {
                        String city = addr.getPropertyValue("CITY");
                        String stae = addr.getPropertyValue("STAE");
                        String ctry = addr.getPropertyValue("CTRY");
                        Property plac = addr.getParent().addProperty("PLAC", city + PropertyPlace.JURISDICTION_SEPARATOR + stae + PropertyPlace.JURISDICTION_SEPARATOR + ctry);
                        try {
                            GedcomUtilities.movePropertyRecursively(map, plac);
                            nbChanges++;
                        } catch (GedcomException ex) {
                            continue;
                        }
                    }
                    for (Property note : addr.getAllProperties("NOTE")) {
                        try {
                            GedcomUtilities.movePropertyRecursively(note, addr.getParent());
                            nbChanges++;
                        } catch (GedcomException ex) {
                            continue;
                        }
                    }
                }
            }
        }
        
        
        
        
        /**
         * Fix of structure in all entities
         * - OBJE Form not properly placed
         * - ...:SOUR:DATE not allowed, move it to DATA, create DATA if does not exist (we assume it is a source date)
         * - ...:SOUR:PAGE:CONC/CONT not allowed, replaced with SOUR:PAGE longer string
         * - ...:SOUR:REFN invalid in citation, move it to source record
         * - NOTE:DATE to be replaced with NOTE:_DATE as the date value appears to already be included in the event date tag (redundant)
         * - ....:TYPE:DATE : Fix invalid TYPE <value>:DATE by replacing with ..:EVEN <value>:DATE
         * - SOUR:DATE invalid when SOUR is an entity ; But in this case, no date exist underneath ; replace with _DATE
         */
        
        for (Entity entity : gedcom.getEntities()) {

            // Fix OBJE
            for (Property obje : entity.getAllProperties("OBJE")) {
                prop = obje.getProperty("FORM");
                if (prop != null) { // error : there should not be a FORM under an OBJE => move it under FILE
                    host = obje.getProperty("FILE");
                    if (host == null) {  // there is *NO* FILE = create one
                        host = obje.addProperty("FILE", "");
                    }
                    host.addProperty("FORM", prop.getValue());
                    prop.getParent().delProperty(prop);
                    hasErrors = true;
                    nbChanges++;
                }
                host = obje.getProperty("FILE");
                if (host != null) {
                    prop = host.getProperty("FORM");
                    if (prop == null) {
                        host.addProperty("FORM", getExtension(host.getValue()));
                        hasErrors = true;
                        nbChanges++;
                    }
                    prop = obje.getProperty("TITL");
                    if (prop == null && host instanceof PropertyFile) {
                        PropertyFile filep = (PropertyFile) host;
                        InputSource is = filep.getInput().orElse(null);
                        String title = is != null ? is.getName() : "";
                        int i = title.indexOf(".");
                        obje.addProperty("TITL", i == -1 ? title : title.substring(0, i));
                        hasErrors = true;
                        nbChanges++;
                    }
                }

            }
            
            // Fix SOUR
            for (Property source : entity.getAllProperties("SOUR")) {
                // SOUR:DATE not allowed, move it to DATA, create DATA if does not exist
                Property dates[] = source.getProperties("DATE");
                if (dates != null && dates.length > 0) {
                    Property data = source.getProperty("DATA");
                    if (data == null) {
                        data = source.addProperty("DATA", "");
                    }
                    for (Property date : dates) {
                        data.addProperty("DATE", date.getValue());
                        source.delProperty(date);
                    }
                    nbChanges++;
                }
                
                // SOUR:PAGE:CONC/CONT not allowed, replaced with SOUR:PAGE longer string
                for (Property page : source.getProperties("PAGE")) {
                    String value = page.getValue();
                    Property concs[] = page.getProperties("CONC");
                    for (Property conc : concs) {
                        value += conc.getValue();
                        page.delProperty(conc);
                    }
                    Property conts[] = page.getProperties("CONT");
                    for (Property cont : conts) {
                        value += cont.getValue();
                        page.delProperty(cont);
                    }
                    page.setValue(value);
                    nbChanges++;
                }
                
                // SOUR:REFN invalid in citation, move it to source record
                for (Property refn : source.getProperties("REFN")) {
                    if (source instanceof PropertySource) {
                        PropertySource pSource = (PropertySource) source;
                        Source sourceEntity = (Source) pSource.getTargetEntity();
                        sourceEntity.addProperty("REFN", refn.getValue());
                    } else {
                        source.addProperty("_REFN", refn.getValue());
                    }
                    source.delProperty(refn);
                    nbChanges++;
                }
                
                
            }
            
            // NOTE:DATE to be replaced with NOTE:_DATE as the date value appears to already be included in the vevent date tag (redundant)
            for (Property note : entity.getAllProperties("NOTE")) {
                Property tags[] = note.getProperties("DATE");
                for (Property tag : tags) {
                    note.addProperty("_DATE", tag.getValue());
                    note.delProperty(tag);
                    nbChanges++;
                }
            }
            
            // Fix invalid TYPE <value>:DATE by replacing with entity:EVEN:TYPE <value> + entity:EVEN:DATE
            for (Property type : entity.getAllProperties("TYPE")) {
                Property date = type.getProperty("DATE");
                if (date != null) {
                    Property even = entity.addProperty("EVEN", "");
                    even.addProperty("TYPE", type.getValue());
                    even.addProperty("DATE", date.getValue());
                    type.delProperty(date);
                    type.getParent().delProperty(type);
                    nbChanges++;
                }
            }

            // SOUR:DATE invalid when SOUR is an entity ; But in this case, no date exist underneath ; replace with _DATE
            if (entity.getTag().equals("SOUR")) {
                for (Property date : entity.getAllProperties("DATE")) {
                    entity.addProperty("_DATE", date.getValue());
                    entity.delProperty(date);
                    nbChanges++;
                }
                
                
            }
            
            
        }
        

        
        return hasErrors;
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
