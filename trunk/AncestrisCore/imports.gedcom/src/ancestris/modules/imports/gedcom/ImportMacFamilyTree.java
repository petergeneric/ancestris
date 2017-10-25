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
import static ancestris.modules.imports.gedcom.Bundle.importmacfamilytree_name;
import static ancestris.modules.imports.gedcom.Bundle.importmacfamilytree_note;
import org.openide.util.lookup.ServiceProvider;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

    private static int counter = 0;
    private static final HashMap<Integer, String> hashFixes = new HashMap<Integer, String>();
    String previousLine = "";
    
    /**
     * Constructor
     */
    public ImportMacFamilyTree() {
        super();
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
    protected void firstPass() {
        super.firstPass();
        detectBadLines();
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
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= processEntities(gedcom);
        return ret;
    }

    
    
    
    
    
    
    private void detectBadLines() {
        //console.println(input.getLine());
        if (input.getLine().startsWith("/")) {
            hashFixes.put(counter, previousLine);
            //console.println("Storing into counter = " + counter + " value = " + previousLine);
            counter++;
        }
        previousLine = input.getLine();

    }

    
    
    private boolean beforeProcess() throws IOException {

        String tag = input.getTag();
        TagPath path = input.getPath();
        
//console.println(input.getLine());
        if (hashFixes.containsValue(input.getLine())) {
            try {
                String str = input.getValue()+"/";
                output.writeLine(input.getLevel(), input.getXref(), input.getTag(), str);
                console.println("Fixed line " + str);
                return true;
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        
        // Replace OBJE:URL with OBJE:FILE
        if ("OBJE:URL".equalsIgnoreCase(path.toString())) {  
            String result = output.writeLine(input.getLevel(), "FILE", input.getValue());
            result += "\n" + output.writeLine(input.getLevel(), "FORM", "url");
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        
        // ASSO does not accept DATE
        if ("INDI:ASSO:DATE".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with "_XXX"
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        
        
        
        return false;
    }

    private boolean afterProcess() {
        return false;
    }

    
    
    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
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
                host = ((PropertyChild) child).getTarget();
                host.addProperty("PEDI", prop.getValue());
                prop.getParent().delProperty(prop);
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixPedi", host.getEntity().toString(true)));
                hasErrors = true;
            }
        }
        
        
        // Remove empty CHAN if another CHAN is present
        for (Entity entity : gedcom.getEntities()) {
            List<Property> changes = entity.getAllProperties("CHAN");
            for (Property chan : changes) {
                if (chan.getValue().trim().isEmpty()) {
                    chan.getParent().delProperty(chan);
                    console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixEmptyChan", entity.toString(true)));
                    hasErrors = true;
                }
            }
            
            // For individual with several BIRTs, BAPM, DEAT, etc. change the other ones into EVEN
            if (entity.getTag().equals("INDI")) {
                reduceEvents(entity, "BIRT");
                reduceEvents(entity, "BAPM");
                reduceEvents(entity, "DEAT");
                reduceEvents(entity, "BURI");
                reduceEvents(entity, "CREM");
            }

            
        }
        
        
        // Move LONG et LATI sous PLAC:MAP
        List<Property> placeList = (List<Property>) gedcom.getPropertiesByClass(PropertyPlace.class);
        for (Property place : placeList) {
            prop = place.getParent().getProperty("LONG");
            prop2 = place.getParent().getProperty("LATI");
            if (prop != null) {
                Property map = place.addProperty("MAP", "");
                map.addProperty("LONG", prop.getValue());
                prop.getParent().delProperty(prop);
                if (prop2 != null) {
                    map.addProperty("LATI", prop2.getValue());
                    prop2.getParent().delProperty(prop2);
                }
                hasErrors = true;
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixPlaceCoord", place.getValue()));
            }
        }
        
        
        // Move OBJE:TITL sous OBJE:FILE
        for (Media media : gedcom.getMedias()) {
            host = media.getProperty("FILE");
            prop = media.getProperty("TITL");
            if (host != null && prop != null) {
                host.addProperty("TITL", prop.getValue());
                media.delProperty(prop);
                hasErrors = true;
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaTitle", media.toString()));
            }
        }
        
        // Add OBJE:FILE:FORM sous OBJE:FILE
        List<Property> fileList = (List<Property>) gedcom.getPropertiesByClass(PropertyFile.class);
        for (Property file : fileList) {
            prop = file.getParent().getProperty("FORM");
            if (prop != null) {
                file.addProperty("FORM", prop.getValue());
                prop.getParent().delProperty(prop);
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaForm", file.toString()));
                hasErrors = true;
            }
            prop = file.getProperty("FORM");
            if (prop == null) {
                file.addProperty("FORM", getExtension(file.getValue()));
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaForm", file.toString()));
                hasErrors = true;
            }
        }
        
        
        // Fix PLAC, DATE, AGNC under SOUR records
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
                    data.addProperty("AGNC", agnc.getValue());
                    sour.delProperty(agnc);
                    hasErrors = true;
                }

                Property even = data.getProperty("EVEN");
                if (even == null) {
                    even = data.addProperty("EVEN", "");
                }
                if (date != null) {
                    even.addProperty("DATE", date.getValue());
                    sour.delProperty(date);
                    hasErrors = true;
                }
                if (plac != null) {
                    even.addProperty("PLAC", plac.getValue());
                    sour.delProperty(plac);
                    hasErrors = true;
                }
                
            }
            
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixSource", sour.toString()));
        }
        
        
        
        console.println("=============================");
        
        return hasErrors;

    }

    
    
}
