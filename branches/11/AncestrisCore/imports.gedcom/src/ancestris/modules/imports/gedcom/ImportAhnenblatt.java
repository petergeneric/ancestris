/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2020 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import ancestris.api.imports.ImportFix;
import genj.gedcom.Context;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Specific import for Ahnenblatt.
 *
 * @author Zurga
 */
@NbBundle.Messages({
    "importahnenblatt.name=Ahnenblatt",
    "importahnenblatt.note=This file has been modified by Ancestris Ahnenblatt Import module."
})
@ServiceProvider(service = Import.class)
public class ImportAhnenblatt extends Import {

    /**
     * Constructor
     */
    public ImportAhnenblatt() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(ImportAhnenblatt.class, "importahnenblatt.name");
    }

    @Override
    protected String getImportComment() {
        return NbBundle.getMessage(ImportAhnenblatt.class, "importahnenblatt.note");
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }

    
    @Override
    protected boolean process() throws IOException {

        // Replace all CSTA entity to NOTE entity
        if (input.getLevel() == 0 && "CSTA".equals(input.getTag())) {  
            String valueBefore = input.getValue();
            String newTag = "NOTE";
            output.writeLine(input.getLevel(), currentXref, newTag, valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, input.getPath().getParent().getShortName()+newTag, valueBefore, valueBefore));
            return true;
        }
        
        if (input.getLevel() == 1 && input.getPath().toString().endsWith("CSTA:NAME")) {  
            String valueBefore = input.getValue();
            output.writeLine(input.getLevel(), "CONT", valueBefore);
            String pathBefore = input.getPath().getShortName();
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, input.getPath().getParent().getShortName()+":CONT", valueBefore, valueBefore));
            return true;
        }
        
        
        
        return super.process();
    }

}
