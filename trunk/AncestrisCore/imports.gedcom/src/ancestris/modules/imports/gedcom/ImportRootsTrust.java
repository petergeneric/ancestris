/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
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
import static ancestris.modules.imports.gedcom.Bundle.importrootstrust_name;
import static ancestris.modules.imports.gedcom.Bundle.importrootstrust_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.io.InputSource;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
/**
 * The import function for ImportFamilyTreeMaker
 */
@NbBundle.Messages({
    "importrootstrust.name=RootsTrust",
    "importrootstrust.note=This file has been modified by Ancestris RootsTrust Import module."
})
@ServiceProvider(service = Import.class)
public class ImportRootsTrust extends Import {
     private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    /**
     * Constructor
     */
    public ImportRootsTrust() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importrootstrust_name();
    }

    @Override
    protected String getImportComment() {
        return importrootstrust_note();
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    
    /**
     * *** 1 ***
     * - Run generic code
     * - <run specific code>
     */
    @Override
    protected void firstPass() {
        super.firstPass();
    }

    /**
     * *** 2 ***
     * - Run generic code
     * - <run specific code>
     * - Quit at each fix if true
     */
    @Override
    protected boolean process() throws IOException {
        
        String tag = input.getTag();
        TagPath path = input.getPath();

        // Overwrite version - FTM seems to consider norm 5.5.1 eventhough it says 5.5
        if ((input.getLevel() == 2) && (input.getTag().equals("VERS") && (input.getValue().startsWith("5.5")))) {
            GEDCOM_VERSION = "5.5.1"; 
            output.writeLine(2, "VERS", GEDCOM_VERSION);
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixVersion", GEDCOM_VERSION));
            nbChanges++;
            return true;
        }
        
        // Fix OBJE:FILE:TEXT and OBJE:FILE:DATE
        // Change TEXT to _TEXT
        // Change DATE to _DATE
        if ("OBJE:FILE:TEXT".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with "_XXX"
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("OBJE:FILE:DATE".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with "_XXX"
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }

        // Fix ALIA /alias name/ with NAME /alias name/ + TYPE aka
        if ("INDI:ALIA".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with "NAME"
            String result = output.writeLine(input.getLevel(), "NAME", input.getValue());
            result += "\n" + output.writeLine(input.getLevel()+1, "TYPE", "aka");
            console.println(NbBundle.getMessage(Import.class, "Import.fixNames", input.getLine() + " ==> " + result));
            return true;
        }

        // Fix INDI:NAME:OBJE to INDI:OBJE
        if ("INDI:NAME:OBJE".equalsIgnoreCase(path.toString())) {  // invalid tag here, move one level up
            output.writeLine(input.getLevel()-1, input.getXref(), tag, input.getValue());
            return true;
        }
        
        return super.process();
    }
    
    /**
     * *** 3 ***
     * - Run generic code
     * - <run specific code>
     * 
     */
    @Override
    protected void finalise() throws IOException {
        super.finalise();
    }
    
    
    /**
     * *** 4 ***
     * - Run generic code
     * - <run specific code>
     * - Quit *after* all have been run
     */
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
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

    ////////////////////////////  END OF LOGIC /////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Specific code depending from import type after Gedcom is processed
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean hasErrors = false;
        Stack<Property> propToDelete = new Stack<>();
        
        for (Entity entity : gedcom.getEntities()) {
            
            // Fix SOUR:PAGE:CONC invalid, add CONC value to PAGE value 
            for (Property page : entity.getAllProperties("PAGE")) {
                String value = page.getValue();
                int n = page.getNoOfProperties();
                for (int i = 0; i < n ; i++) {
                    final Property prop = page.getProperty(i);
                    final String tag = prop.getTag();
                    if ("CONC".equals(tag)) {
                        value += prop.getValue();
                        propToDelete.add(prop);
                    } else if ("CONT".equals(tag)) {
                        value += " " + prop.getValue();
                        propToDelete.add(prop);
                    }
                }
                if (n > 0) {
                    page.setValue(value);
                    while (!propToDelete.empty()) {
                        page.delProperty(propToDelete.pop());
                    }
                    nbChanges++;
                    hasErrors = true;
                }
            }

            // Move NAME:SOUR and NAME:NOTE one level up
            if (entity.getTag().equals("INDI")) {
                final Property host = entity.getProperty("NAME");
                if (host != null) {
                    Property[] props = host.getProperties("SOUR");
                    for (Property sour : props) {
                        try {
                            GedcomUtilities.movePropertyRecursively(sour, entity);
                            nbChanges++;
                        } catch (GedcomException ex) {
                            LOG.log(Level.INFO, "Error with SOUR", ex);
                        }
                    }
                    props = host.getProperties("NOTE");
                    for (Property note : props) {
                        try {
                            GedcomUtilities.movePropertyRecursively(note, entity);
                            nbChanges++;
                        } catch (GedcomException ex) {
                            LOG.log(Level.INFO, "Error with NOTE", ex);
                        }
                    }
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
            
            // Add FORM to FILE
            if (entity.getTag().equals("OBJE")) {
                Property prop = entity.getProperty("FORM");
                if (prop != null) {
                    final Property host = entity.getProperty("FILE");
                    if (host != null) {
                        host.addProperty("FORM", prop.getValue());
                        prop.getParent().delProperty(prop);
                        hasErrors = true;
                    }
                }
                final Property host = entity.getProperty("FILE");
                if (host != null) {
                    prop = host.getProperty("FORM");
                    if (prop == null) {
                        String ext = getExtension(host.getValue());
                        if (ext == null) {
                            ext = "none";
                        }
                        host.addProperty("FORM", ext);
                        hasErrors = true;
                    }
                    prop = host.getProperty("TITL");
                    if (prop == null) {
                        PropertyFile filep = (PropertyFile) host;
                        InputSource file = filep.getInput().orElse(null);
                        String title = file != null ? file.getName() : "";
                        int i = title.indexOf(".");
                        host.addProperty("TITL", i == -1 ? title : title.substring(0, i));
                        hasErrors = true;
                    }
                } else {
                    entity.addDefaultProperties();
                }
            }   
        }
        return hasErrors;
    }
}
