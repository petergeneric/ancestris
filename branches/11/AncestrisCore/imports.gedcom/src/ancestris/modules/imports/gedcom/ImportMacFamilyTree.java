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
import static ancestris.modules.imports.gedcom.Bundle.importmacfamilytree_name;
import static ancestris.modules.imports.gedcom.Bundle.importmacfamilytree_note;
import org.openide.util.lookup.ServiceProvider;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyPlace;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author frederic
 */
/**
 * The import function for MacFamilyTree
 */
@NbBundle.Messages({
    "importmacfamilytree.name=MacFamilyTree",
    "importmacfamilytree.note=This file has been modified by the Ancestris MacFamilyTree Import module."
})
@ServiceProvider(service = Import.class)
public class ImportMacFamilyTree extends Import {

    private static HashMap<String, ImportPlace> hashPlaces;
    private static String PLACE_RECORD_TAG = "_PLAC";
    protected static Pattern tag_plac_pattern = Pattern.compile("(PLAC|FORM|FONE|TYPE|ROMN|MAP|LATI|LONG|NOTE|CONC|CONT)");
    
    /**
     * Constructor
     */
    public ImportMacFamilyTree() {
        super();
        hashPlaces = new HashMap<String, ImportPlace>();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importmacfamilytree_name();
    }

    @Override
    protected String getImportComment() {
        return importmacfamilytree_note();
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }

    
    @Override
    protected void init() {
        super.init();

        hashPlaces.clear();
        
        invalidPaths.add("_CRE:DATE");
        invalidPaths.add("_CRE:DATE:TIME");
        invalidPaths.add("INDI:ASSO:DATE");
    }

    
    
    @Override
    protected void firstPass() {
        super.firstPass();
        
        
        // Memorise Place records content
        if (input.getLevel() == 0 && input.getTag().equals(PLACE_RECORD_TAG)) {
            String key = "";
            ImportPlace importPlace = null;
            String tag = "";

            try {
                key = input.getValue();
                if (!hashPlaces.containsKey(key)) {
                    importPlace = new ImportPlace();
                    hashPlaces.put(key, importPlace);
                }
                importPlace = hashPlaces.get(key);
                importPlace.used = false;
                input.getNextLine(false);  // stay on current line
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
        
        
    }

    @Override
    protected boolean process() throws IOException {
        // before super process
        if (beforeProcess()) {
            return true;
        }
        // Super process  (yes tags, invalid tags, etc)
        if (super.process()) {
            return true;
        }
        
        //After super process
        if (afterProcess()) {
            return true;
        }
        return false;
    }
    

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
            if (!importPlace.used) {
                String valueBefore = importPlace.values.get(0);
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

    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        incrementProgress();
        ret |= processEntities(gedcom);
        return ret;
    }

    
    
    
    
    private boolean beforeProcess() throws IOException {

        String tag = input.getTag();
        TagPath path = input.getPath();
        
        // Replace all CHAN:DATE:TIME of level > 1 to _CHAN:_DATE:_TIME
        if (input.getLevel() > 1 && "CHAN".equals(input.getTag())) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "_CHAN", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getShortName()+":_CHAN", valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() > 2 && path.toString().endsWith("CHAN:DATE")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "_DATE", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getShortName()+":_DATE", valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() > 3 && path.toString().endsWith("CHAN:DATE:TIME")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "_TIME", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getShortName()+":_TIME", valueBefore, valueBefore));
            return true;
        }
        
        
        // Replace all LABL to NOTE
        if ("LABL".equals(input.getTag())) {  
            String valueBefore = input.getValue();
            String newTag = "NOTE";
            if (input.getLevel() == 0) {
                output.writeLine(input.getLevel(), currentXref, newTag, valueBefore);
            } else {
                output.writeLine(input.getLevel(), newTag, valueBefore);
                newTag = ":" + newTag;
            }
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+newTag, valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() == 1 && path.toString().endsWith("LABL:TITL")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "CONT", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+":CONT", valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() == 1 && path.toString().endsWith("LABL:COLR")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "_COLR", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+":_COLR", valueBefore, valueBefore));
            return true;
        }
        
        return false;
    }

    private boolean afterProcess() throws IOException {

        // For each PLAC tag in an event (level >= 2), write memorised lines of a _PLAC_DEFN record
        if ("PLAC".equals(input.getTag()) && input.getLevel() > 1) {
            ImportPlace importPlace = hashPlaces.get(input.getValue());
            if (importPlace != null) {
                importPlace.used = true;
            }
            return false;
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


        return false;
    }

    
    
    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;

        String pathBefore = "";
        String pathAfter = "";
        String valueBefore = "";
        
        Property[] props = null;
        Property prop = null;
        Property prop2 = null;
        Property host = null;
        
        // Move FAM:CHIL:PEDI to CHIL_Target:PEDI (=CHIL => INDI => FAMC:PEDI)
        for (Fam fam : gedcom.getFamilies()) {
            Property[] children = fam.getProperties("CHIL");
            for (Property child : children) {
                prop = child.getProperty("PEDI");
                if (prop == null) {
                    continue;
                }
                pathBefore = prop.getPath(true).getShortName();
                valueBefore = prop.getValue();
                host = ((PropertyChild) child).getTarget();
                prop2 = host.addProperty("PEDI", valueBefore);
                pathAfter = prop2.getPath(true).getShortName();
                prop.getParent().delProperty(prop);
                fixes.add(new ImportFix(prop2.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                hasErrors = true;
            }
        }
        
        
        // Move LONG et LATI sous PLAC:MAP
        List<Property> placeList = (List<Property>) gedcom.getPropertiesByClass(PropertyPlace.class);
        
        for (Property place : placeList) {
            
            Property pmap = place.getParent().getProperty("MAP");
            Property plong = place.getParent().getProperty("LONG");
            Property plati = place.getParent().getProperty("LATI");
            
            // create a map if either a long or lati exists
            if ((plong != null || plati != null) && pmap == null) {
                pmap = place.addProperty("MAP", "");
            }
            
            if (plong != null) {
                pathBefore = plong.getPath(true).getShortName();
                valueBefore = plong.getValue();
                prop2 = pmap.addProperty("LONG", valueBefore);
                pathAfter = prop2.getPath(true).getShortName();
                plong.getParent().delProperty(plong);
                fixes.add(new ImportFix(place.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                hasErrors = true;
            }
            
            if (plati != null) {
                pathBefore = plati.getPath(true).getShortName();
                valueBefore = plati.getValue();
                prop2 = pmap.addProperty("LATI", valueBefore);
                pathAfter = prop2.getPath(true).getShortName();
                plati.getParent().delProperty(plati);
                fixes.add(new ImportFix(place.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                hasErrors = true;
            }
            
            
        }
        
        // Fix PLAC, DATE, AGNC under SOUR records
        // SOUR:AGNC => SOUR:DATA:AGNC
        // SOUR:PLAC => SOUR:DATA:EVEN:PLAC
        // SOUR:DATE => SOUR:DATA:EVEN:DATE
        for (Source sour : gedcom.getSources()) {
            Property plac = sour.getProperty("PLAC");
            Property date = sour.getProperty("DATE");
            Property agnc = sour.getProperty("AGNC");
            
            if (plac != null || date != null || agnc != null) {
                
                Property data = sour.getProperty("DATA");
                if (data == null) {
                    data = sour.addProperty("DATA", "");
                }
                
                if (agnc != null) {
                    pathBefore = agnc.getPath(true).getShortName();
                    valueBefore = agnc.getValue();
                    Property p = data.addProperty("AGNC", valueBefore);
                    pathAfter = p.getPath(true).getShortName();
                    sour.delProperty(agnc);
                    fixes.add(new ImportFix(sour.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                }

                Property even = data.getProperty("EVEN");
                if (even == null) {
                    even = data.addProperty("EVEN", "");
                }
                if (date != null) {
                    pathBefore = date.getPath(true).getShortName();
                    valueBefore = date.getValue();
                    Property p = even.addProperty("DATE", valueBefore);
                    pathAfter = p.getPath(true).getShortName();
                    sour.delProperty(date);
                    fixes.add(new ImportFix(sour.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                }
                if (plac != null) {
                    pathBefore = plac.getPath(true).getShortName();
                    valueBefore = plac.getValue();
                    Property p = even.addProperty("PLAC", valueBefore);
                    pathAfter = p.getPath(true).getShortName();
                    sour.delProperty(plac);
                    fixes.add(new ImportFix(sour.getEntity().getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                }
                
            }
        }
        
        return hasErrors;

    }

    
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
