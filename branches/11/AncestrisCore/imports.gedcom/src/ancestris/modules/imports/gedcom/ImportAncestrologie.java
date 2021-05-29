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
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_name;
import static ancestris.modules.imports.gedcom.Bundle.importancestrologie_note;
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
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }


    @Override
    protected void firstPass() {
        super.firstPass();
    }

    @Override
    protected boolean process() throws IOException {

        String tag = input.getTag();
        TagPath path = input.getPath();
        
        
        // Replace SOUR:EVEN by SOUR:DATA:EVEN
        if (path.toString().equals("SOUR:EVEN")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "DATA", "");
            output.writeLine(input.getLevel()+1, "EVEN", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.3", path.getShortName(), path.getParent().getShortName()+"DATA:EVEN", valueBefore, valueBefore));
            return true;
        }
        
        // invalid tag here, replace with _TIME
        if (tag.equals("TIME") && !path.toString().contains("CHAN") && !currentXref.equals("HEAD")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "_TIME", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", path.getShortName(), path.getParent().getShortName()+":_TIME", valueBefore, valueBefore));
            return true;
        }
        
        

        // Super process  (yes tags, invalid tags, etc)
        if (super.process()) {
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
