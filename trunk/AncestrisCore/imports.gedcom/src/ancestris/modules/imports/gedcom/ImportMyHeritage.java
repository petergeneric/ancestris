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
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_name;
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_note;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import java.io.IOException;

/**
 *
 * @author frederic
 */
/**
 * The import function for ImportMyHeritage
 */
@NbBundle.Messages({
    "importmyheritage.name=MyHeritage",
    "importmyheritage.note=This file has been modified by the Ancestris MyHeritage Import module."
})
@ServiceProvider(service = Import.class)
public class ImportMyHeritage extends Import {

    /**
     * Constructor
     */
    public ImportMyHeritage() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importmyheritage_name();
    }

    @Override
    protected String getImportComment() {
        return importmyheritage_note();
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

        return hasErrors;
    }

    
    
    
}
