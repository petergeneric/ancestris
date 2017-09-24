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
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_name;
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_note;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.TagPath;
import java.io.IOException;

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
        // before super process
        if (beforeProcess()) {
            return true;
        }
        // Super process  (yes tags, invalid tags, etc)
        if (super.process()) {
            return true;
        }
        
        String tag = input.getTag();
        TagPath path = input.getPath();
        
        // invalid tag here, replace with _TIME
        if (path.toString().contains("TIME") && !path.toString().contains("CHAN")) {  
            String result = output.writeLine(input.getLevel(), "_TIME", input.getValue());
            console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
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

    
    
    
    
    
    
    private boolean beforeProcess() {
        if ((input.getLevel() == 0) && (!header)) {
            try {
                header = true;
                output.writeLine(1, null, "DEST", "ANY");
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        
        
        return false;
    }


    private boolean afterProcess() {
        return false;
    }

    
    
    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
        
        
        
        return hasErrors;

    }

    
    
}
