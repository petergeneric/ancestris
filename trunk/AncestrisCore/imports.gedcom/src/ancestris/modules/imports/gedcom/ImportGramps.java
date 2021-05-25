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
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importgramps_name;
import static ancestris.modules.imports.gedcom.Bundle.importgramps_note;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.TagPath;
import java.io.IOException;
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
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    @Override
    protected boolean process() throws IOException {
        
        if (super.process()) {
            return true;
        }

        String tag = input.getTag();
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        
        // invalid tag here, replace with ..:_TIME
        if (path.toString().contains("DATE:TIME") && !path.toString().contains("CHAN") && !currentXref.equals("HEAD")) {  
            output.writeLine(input.getLevel()-1, "_TIME", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getParent().getShortName()+":_TIME", valueBefore, valueBefore));
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        return ret;
    }
    
    
    
}
