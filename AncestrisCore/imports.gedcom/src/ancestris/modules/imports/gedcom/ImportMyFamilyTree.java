/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import static ancestris.modules.imports.gedcom.Bundle.importmyfamilytree_name;
import static ancestris.modules.imports.gedcom.Bundle.importmyfamilytree_note;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@NbBundle.Messages({
    "importmyfamilytree.name=MyFamilyTree", 
    "importmyfamilytree.note=This file has been modified by the Ancestris MyFamilyTree Import module."
})
@ServiceProvider(service = Import.class)
public class ImportMyFamilyTree extends Import {

    /**
     * Constructor
     */
    public ImportMyFamilyTree() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importmyfamilytree_name();
    }

    @Override
    protected String getImportComment() {
        return importmyfamilytree_note();
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    
    
    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
       /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
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
        return super.fixGedcom(gedcom);
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
    
}
