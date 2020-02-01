/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import static ancestris.modules.imports.gedcom.Bundle.importgramps_name;
import static ancestris.modules.imports.gedcom.Bundle.importgramps_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@NbBundle.Messages({
    "importgramps.name=Gramps",
    "importgramps.note=This file has been modified by the Ancestris Gramps Import module."
    
})
@ServiceProvider(service = Import.class)
public class ImportGramps extends Import {

    /**
     * Constructor
     */
    public ImportGramps() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importgramps_name();
    }

    @Override
    protected String getImportComment() {
        return importgramps_note();
    }

    
    @Override
    protected void firstPass() {
        super.firstPass();
//        if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
//            if (!hashrepo.containsKey(input.getValue())) {
//                clerepo++;
//                hashrepo.put(input.getValue(), clerepo);
//                sb.append("0 @" + typerepo).append(clerepo).append("@ REPO").append(EOL);
//                sb.append("1 NAME ").append(input.getValue()).append(EOL);
//            }
//        }
    }

    @Override
    protected boolean process() throws IOException {
        if (super.process()) {
            return true;
        }

        if ((input.getLevel() == 1) && (input.getTag().equals("GEDC"))) {
            output.writeLine(1, "DEST", "ANY");
            output.writeLine(1, "GEDC", "");
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixDestination"));
            return true;
        }
        
        String tag = input.getTag();
        TagPath path = input.getPath();
        
        // usually followed by CONC, not convenient for very long strings, replace with NOTE
        if ("INDI:BIRT:TYPE".equalsIgnoreCase(path.toString())) {  
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("FAM:MARR:TYPE".equalsIgnoreCase(path.toString())) {  
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("INDI:DEAT:TYPE".equalsIgnoreCase(path.toString())) { 
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("INDI:WILL:TYPE".equalsIgnoreCase(path.toString())) { 
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        
        // invalid tag here, replace with _TIME
        if (path.toString().contains("DATE:TIME") && !path.toString().contains("CHAN")) {  
            String result = output.writeLine(input.getLevel(), "_TIME", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }

        
        return false;
    }
    
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= processEntities(gedcom);
        incrementProgress();
        ret |= processCONC(gedcom);
        incrementProgress();
        ret |= super.convertAssociations(gedcom);    // check
        return ret;
    }

    
    
    
    
    
    
    
    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
        Property[] props = null;
        Property prop = null;
        Property host = null;
        
        console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMedia"));

        // Put FORM underneath FILE on all FILEs
        List<Property> propsList = (List<Property>) gedcom.getPropertiesByClass(PropertyFile.class);
        for (Property file : propsList) {
            prop = file.getParent().getProperty("FORM");
            if (prop != null) {
                file.addProperty("FORM", prop.getValue());
                prop.getParent().delProperty(prop);
                hasErrors = true;
            }
            prop = file.getProperty("FORM");
            if (prop == null) {
                file.addProperty("FORM", getExtension(file.getValue()));
                hasErrors = true;
            }
        }
        
        console.println("=============================");
        
        return hasErrors;

    }

    
    public boolean processCONC(Gedcom gedcom) {

        boolean hasErrors = false;
        
        console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixCONC"));

        List<Property> list = new ArrayList<Property>();
        for (Entity entity : gedcom.getEntities()) {
            getPropertiesRecursively(list, "CONC", entity);
        }
        for (Property prop : list) {
            Property parent = prop.getParent();
            int nb = parent.getAllProperties("CONC").size();
            if (!parent.getTag().equals("NOTE") && (nb == 1)) {
                console.println("CONC : " + prop.getValue() + " ==> " + parent.getTag());
                parent.setValue(parent.getValue() + prop.getValue());
                parent.delProperty(prop);
            }
        }
        
        console.println("=============================");
        
        return hasErrors;


    }
    
}
