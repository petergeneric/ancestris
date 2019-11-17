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
import static ancestris.modules.imports.gedcom.Bundle.importgeneatique_name;
import static ancestris.modules.imports.gedcom.Bundle.importgeneatique_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author frederic
 */
/**
 * The import function for ImportGeneatique
 */
@NbBundle.Messages({
    "importgeneatique.name=Geneatique",
    "importgeneatique.note=This file has been modified by the Ancestris Geneatique Import module."
})
@ServiceProvider(service = Import.class)
public class ImportGeneatique extends Import {

    /**
     * Constructor
     */
    public ImportGeneatique() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importgeneatique_name();
    }

    @Override
    protected String getImportComment() {
        return importgeneatique_note();
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
        
        // Replace ASSO:TYPE by ASSO:RELA
        if (path.toString().endsWith("ASSO:TYPE")) { 
            String result = output.writeLine(input.getLevel(), "RELA", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixAssoRela", input.getLine() + " ==> " + result));
            return true;
        }
        
        
        
        if (super.process()) {
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
        ret |= super.convertAssociations(gedcom);
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
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean hasErrors = false;
        Property[] props = null;
        Property prop = null;
        Property host = null;
        
        
        
        for (Indi indi : gedcom.getIndis()) {


            // Turn "1 NICK" into "1 NAME/NICK" (Put any NICK of level 1 to a NICK in the NAME tag
            props = indi.getProperties("NICK");
            for (Property nick : props) {
                Property name = indi.getProperty("NAME");
                if (name == null) {
                    name = indi.addProperty("NAME", "");
                }
                name.addProperty("NICK", nick.getValue());
                indi.delProperty(nick);
                hasErrors = true;
            }
            
            // Turn "_IMG/OBJE/FILE" into "OBJE/FILE" (form will be taken care of below)
            props = indi.getProperties("_IMG");
            for (Property img : props) {
                int pos = indi.getPropertyPosition(img);
                Property obje = img.getProperty("OBJE");
                if (obje != null) {
                    Property file = obje.getProperty("FILE");
                    try {
                        prop = indi.addProperty("OBJE", obje.getValue(), pos);
                    } catch (GedcomException ex) {
                        prop = indi.addProperty("OBJE", obje.getValue());
                    }
                    prop.addProperty("FILE", file.getValue());
                }
                indi.delProperty(img);
                hasErrors = true;
            }
            
            
        }

        
        
        // Add OBJE:FORM next to OBJE:FILE in 5,5 grammar if no FORM exists
        if (gedcom.getGrammar().equals(Grammar.V55)) {
            List<Property> fileList = (List<Property>) gedcom.getPropertiesByClass(PropertyFile.class);
            for (Property file : fileList) {
                host = file.getParent();
                prop = host.getProperty("FORM");
                if (prop == null) {
                    String ext = getExtension(file.getValue());
                    if (ext == null) {
                        ext = "none";
                    }
                    host.addProperty("FORM", ext);
                    console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaForm", file.toString()));
                    hasErrors = true;
                }
            }
        }
        
        
        
        return hasErrors;
    }

    
    
    
}
