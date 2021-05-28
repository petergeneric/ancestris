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
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importrootsmagic_name;
import static ancestris.modules.imports.gedcom.Bundle.importrootsmagic_note;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    "importrootsmagic.name=RootsMagic",
    "importrootsmagic.note=This file has been modified by Ancestris RootsMagic Import module."
})
@ServiceProvider(service = Import.class)
public class ImportRootsMagic extends Import {
    
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    protected static final String SPECIAL_EVENTS = "(WAC|Degree|Military|Mission|Stillborn|"
            + "Illness|Living|Elected|Excomm|Namesake|Separation|AltName|_DNA|Misc|Race|Civi|Arrival|"
            + "Departure|CongressionalDistrict|Locati|ARI|PLC_Orgin)";
    protected static final Pattern SPECIAL_EVENTS_PATTERN = Pattern.compile(SPECIAL_EVENTS);     
    
    private int folderIndex = 0;
    
    /**
     * Constructor
     */
    public ImportRootsMagic() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importrootsmagic_name();
    }

    @Override
    protected String getImportComment() {
        return importrootsmagic_note();
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

        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        
        if (input.getLevel() == 0 && ("_TASK".equals(input.getTag()) || "_FOLDER".equals(input.getTag()))) {
            String valueBefore = input.getValue();
            String pathAfter = "NOTE";
            String xref = currentXref;
            if ("_FOLDER".equals(input.getTag()) && xref.isEmpty()) {
                folderIndex++;
                xref = "FOLDER" + folderIndex;
            }
            output.writeLine(input.getLevel(), xref, "NOTE", valueBefore);
            fixes.add(new ImportFix(xref, "invalidEntity.01", pathBefore, pathAfter, valueBefore, valueBefore));
            return true;
        }
        if (("_FOLDER:NOTE").equalsIgnoreCase(pathBefore)) { 
            String valueBefore = input.getValue();
            output.writeLine(1, "CONT", valueBefore);
            String xref = "FOLDER" + folderIndex;
            fixes.add(new ImportFix(xref, "invalidEntity.02", pathBefore, "NOTE:CONT", valueBefore, valueBefore));
            return true;
        }

        if (("_FOLDER:NOTE:CONC").equalsIgnoreCase(pathBefore) || ("_FOLDER:NOTE:CONT").equalsIgnoreCase(pathBefore)) { 
            String valueBefore = input.getValue();
            output.writeLine(1, input.getTag(), valueBefore);
            String xref = "FOLDER" + folderIndex;
            fixes.add(new ImportFix(xref, "invalidEntity.02", pathBefore, "NOTE:"+input.getTag(), valueBefore, valueBefore));
            return true;
        }

        // Remove all _PLAC records. 
        if (input.getLevel() == 0 && (input.getTag().equals("_PLAC") || input.getTag().equals("_EVDEF"))) {
            input.getNextLine(false);  // read next line and stay on current line
            while (input.getLevel() != 0) {
                input.getNextLine(true);   // consume line and do nothing
                input.getNextLine(false);  // read next line and stay on it
            }
            return true;
        }
        
        if (input.getLevel() == 1 && SPECIAL_EVENTS_PATTERN.matcher(input.getTag()).matches()) {
            String valueBefore = input.getValue();
            String valueAfter = input.getTag() + (valueBefore.isEmpty() ? "" : " - " + valueBefore);
            output.writeLine(1, "EVEN", valueAfter);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName() + ":EVEN", valueBefore, valueAfter));
            return true;
        }

        /**
         * TODO:
         * - Source citation TMPLP ???
         * - PRIM Y ??
         * - Sources data ???
         * - Individual data ???
         * 
         * 
         */


        
        if (super.process()) {
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
        return false;
    }
}
