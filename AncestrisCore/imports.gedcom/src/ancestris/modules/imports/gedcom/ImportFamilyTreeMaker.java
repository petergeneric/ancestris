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
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importfamilytreemaker_name;
import static ancestris.modules.imports.gedcom.Bundle.importfamilytreemaker_note;
import genj.gedcom.Context;
import genj.gedcom.TagPath;
import java.io.IOException;
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
    "importfamilytreemaker.name=Family Tree Maker (FTM)",
    "importfamilytreemaker.note=This file has been modified by Ancestris Family Tree Maker Import module."
})
@ServiceProvider(service = Import.class)
public class ImportFamilyTreeMaker extends Import {

    /**
     * Constructor
     */
    public ImportFamilyTreeMaker() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importfamilytreemaker_name();
    }

    @Override
    protected String getImportComment() {
        return importfamilytreemaker_note();
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    /**
     * *** 1 ***
     * - Run generic code
     * - <run specific code>
     */
    @Override
    protected void firstPass() {
        super.firstPass();
        GEDCOM_VERSION = "5.5.1"; // Overwrite version - FTM seems to consider norm 5.5.1 eventhough it says 5.5
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
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        

        // Overwrite version
        if ("HEAD:GEDC:VERS".equalsIgnoreCase(pathBefore)) {  
            String valueAfter = GEDCOM_VERSION;
            if (!valueBefore.equals(valueAfter)) {
                output.writeLine(2, "VERS", GEDCOM_VERSION);
                fixes.add(new ImportFix(currentXref, "header.Version", pathBefore, pathBefore, valueBefore, valueAfter));
                return true;
            }
            return false;
        }

        // Invalid use of ALI
        if ((input.getLevel() == 1) && (input.getTag().equals("ALIA"))) {
            output.writeLine(1, "NAME", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTag.3", path.getShortName(), path.getParent().getShortName()+":NAME", valueBefore, valueBefore));
            return true;
        }
        
        
        // Fix OBJE:FILE:TEXT and OBJE:FILE:DATE
        // Change TEXT to NOTE
        // Change DATE to NOTE
        if ("OBJE:FILE:TEXT".equalsIgnoreCase(path.toString()) || "OBJE:FILE:DATE".equalsIgnoreCase(path.toString())) {
            output.writeLine(input.getLevel()-1, "NOTE", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.2", path.getShortName(), "OBJE:NOTE", valueBefore, valueBefore));
            return true;
        }
        if ("OBJE:FILE:TEXT:CONC".equalsIgnoreCase(path.toString()) || "OBJE:FILE:TEXT:CONT".equalsIgnoreCase(path.toString())) {
            output.writeLine(input.getLevel()-1, tag, input.getValue());
            // same issue as above, do not add one more.
            return true;
        }


        if (super.process()) {
            return true;
        }
        
        return false;
    }
    
    
}
