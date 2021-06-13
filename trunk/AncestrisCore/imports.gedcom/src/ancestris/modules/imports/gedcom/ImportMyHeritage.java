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
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_name;
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_note;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

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
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
        invalidPaths.add("FAM:NCHI:NOTE");
        invalidPaths.add("ALBUM:TITL");
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

        
        if ("_ALBUM".equalsIgnoreCase(tag)) {  // invalid tag here, replace with NOTE
            output.writeLine(input.getLevel(), "NOTE", valueBefore);
            String newTag = ":NOTE";
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+newTag, valueBefore, valueBefore));
            return true;
        }
        
        if ("ALBUM".equalsIgnoreCase(tag) || "_PUBLISH".equalsIgnoreCase(tag)) {  // invalid entities here, replace with NOTE
            String xref = currentXref;
            output.writeLine(input.getLevel(), xref, "NOTE", valueBefore);
            String newTag = "NOTE";
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+newTag, valueBefore, valueBefore));
            return true;
        }
        
        if (super.process()) {
            return true;
        }

        return processOther();
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
    private boolean processOther() throws IOException {
        String tag = input.getTag();
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        if ("RIN".equals(tag) && (path.length() != 2)) {  // invalid tag here
            output.writeLine(input.getLevel(), "_RIN", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getShortName()+":_RIN", valueBefore, valueBefore));
            return true;
        }
        return false;
    }

    /**
     * Specific code depending from import type after Gedcom is processed
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean hasErrors = false;
    
        // Only one PAGE tag in a SOUR link
        List<PropertySource> sourceLinks = (List<PropertySource>) gedcom.getPropertiesByClass(PropertySource.class);
        for (PropertySource ps : sourceLinks) {
            Property[] props = ps.getProperties("PAGE");
            if (props.length > 1) {
                int cnt = 0;
                for (Property page : props) {
                    cnt++;
                    if (cnt > 1) {
                        String pathBefore = page.getPath(true).getShortName();
                        String valueBefore = page.getValue();
                        Property p = ps.addProperty("_PAGE", valueBefore);
                        ps.delProperty(page);
                        fixes.add(new ImportFix(ps.getEntity().getId(), "eventsCardinality.2", pathBefore, p.getPath(true).getParent().getShortName()+":_PAGE", valueBefore, valueBefore));
                        hasErrors = true;
                    }
                }
            }
        }
            
        return hasErrors;
    }

    
    
    
}
