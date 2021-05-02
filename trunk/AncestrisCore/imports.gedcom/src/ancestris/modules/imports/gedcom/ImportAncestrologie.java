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
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_name;
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_note;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
/**
 * The import function for MacFamilyTree
 */
@NbBundle.Messages({
    "importancestrologie.name=Ancestrologie",
    "importancestrologie.note=This file has been modified by Ancestris Ancestrologie Import module."
})
@ServiceProvider(service = Import.class)
public class ImportAncestrologie extends Import {

    String previousLine = "";
    private boolean header = false;
    
    /**
     * Constructor
     */
    public ImportAncestrologie() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importancestrologie_name();
    }

    @Override
    protected String getImportComment() {
        return importancestrologie_note();
    }

    @Override
    protected void firstPass() {
        super.firstPass();
    }

    @Override
    protected boolean process() throws IOException {

        String tag = input.getTag();
        TagPath path = input.getPath();
        
        
        // Replace SOUR:EVEN by SOUR:NOTE
        if (path.toString().equals("SOUR:EVEN")) {  
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        
        

        // Super process  (yes tags, invalid tags, etc)
        if (super.process()) {
            return true;
        }
        
        // invalid tag here, replace with _TIME
        if (path.toString().contains("TIME") && !path.toString().contains("CHAN")) {  
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
        ret |= super.convertAssociations(gedcom);
        return ret;
    }

    
    
    
    
    
    
    
    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
        Property[] props = null;
        Property host = null;
        Property date = null;
        

        // Move INDI:ADDR under a new RESI tag
        for (Indi indi : gedcom.getIndis()) {
            props = indi.getProperties("ADDR");
            for (Property prop : props) {
                // Create RESI
                host = indi.addProperty("RESI", "");
                
                // In case a adte appears under ADDR, move it to RESI level
                date = prop.getProperty("DATE");
                if (date != null) {
                    host.addProperty("DATE", date.getValue());
                    prop.delProperty(date);
                }
                
                // Move rest of ADDR
                try {
                    GedcomUtilities.movePropertyRecursively(prop, host);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                nbChanges++;
            }
            hasErrors = true;
        }

        
        
        
        return hasErrors;

    }

    
    
}
