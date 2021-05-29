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
import static ancestris.modules.imports.gedcom.Bundle.importgeneatique_name;
import static ancestris.modules.imports.gedcom.Bundle.importgeneatique_note;
import genj.gedcom.Context;
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
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
        invalidPaths.add("INDI:QUAY");
        invalidPaths.add("INDI:PLAC");
    }

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
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        boolean v55 = GEDCOM_VERSION.equals("5.5");

        if ("DECO".equalsIgnoreCase(tag)) {  // invalid tag here but useful information, replace with FACT
            String newTag = v55 ? "EVEN" : "FACT";
            output.writeLine(input.getLevel(), newTag, valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+":"+newTag, valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() == 1 && ("_IMA".equalsIgnoreCase(tag) || "_IMG".equalsIgnoreCase(tag))) {  // valid tag here but more relevant as EVEN
            String newTag = "EVEN";
            output.writeLine(input.getLevel(), newTag, valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.3", pathBefore, path.getParent().getShortName()+":"+newTag, valueBefore, valueBefore));
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

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
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
        return false;
    }

    
}
