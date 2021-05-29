/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importgenbox_name;
import static ancestris.modules.imports.gedcom.Bundle.importgenbox_note;
import genj.gedcom.Context;
import org.openide.util.lookup.ServiceProvider;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyFamilyChild;
import java.io.IOException;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
@NbBundle.Messages({
    "importgenbox.name=Genbox",
    "importgenbox.note=This file has been modified by the Ancestris Genbox Import module."
})
@ServiceProvider(service = Import.class)
public class ImportGenbox extends Import {

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importgenbox_name();
    }

    @Override
    protected String getImportComment() {
        return importgenbox_note();
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    
    @Override
    protected boolean process() throws IOException{

        if (input.getTag().equals("AGE")) {
            String pathBefore = input.getPath().getShortName();
            String valueBefore = input.getValue();
            String valueAfter = valueBefore;
            if (valueBefore.matches("\\d+")){
                valueAfter = valueBefore+"y";
            }
            valueAfter = valueBefore.replace(" day", "d").replace("ds", "d").replace(" month", "m").replace(" mth", "m").replace("ms", "m").replace(" yr", "y").replace("ys", "y");
            output.writeLine(input.getLevel(), input.getTag(), valueAfter);
            fixes.add(new ImportFix(currentXref, "section.invalidAge.1", pathBefore, pathBefore, valueBefore, valueAfter));
            return true;
        }
        
        if (super.process()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        fixFAMC(gedcom);
        incrementProgress();
        return super.fixGedcom(gedcom);

    }
    
    /**
     * Fix Famc duplicates. 
     * Loop over all INDIs and if a INDI:FAMC is the same family than INDI:BIRT:FAMC, then removes
     * the later
     * @param gedcom
     * @return always true
     */
    private boolean fixFAMC(Gedcom gedcom){
        
        for (Indi indi : gedcom.getIndis()) {
            // loop over all famc
            List<PropertyFamilyChild> famcs = indi.getProperties(PropertyFamilyChild.class);
            if (famcs.size() <= 1) {
                continue;
            }
            PropertyFamilyChild famc = (PropertyFamilyChild) indi.getProperty("FAMC");
            // can't fix if no FAMC under INDI
            if (famc == null) {
                continue;
            }
            for (PropertyFamilyChild fc : famcs) {
                if (famc.equals(fc)) {
                    continue;
                }
                if (famc.getFamily().equals(fc.getFamily())) {
                    String pathBefore = fc.getPath(true).getShortName();
                    String valueBefore = fc.getValue();
                    fc.getParent().delProperty(fc);
                    fixes.add(new ImportFix(indi.getId(), "duplicateAssociations.1", pathBefore, "", valueBefore, ""));
                }
            }
        }
        // Allways succeed
        return true;
    }
}
