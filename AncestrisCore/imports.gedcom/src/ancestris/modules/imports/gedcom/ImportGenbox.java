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
import static ancestris.modules.imports.gedcom.Bundle.importgenbox_name;
import static ancestris.modules.imports.gedcom.Bundle.importgenbox_note;
import org.openide.util.lookup.ServiceProvider;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
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
    protected boolean process() throws IOException{
        if (processAGE()) {
            return true;
        }
        if (super.process()) {
            return true;
        }
        return false;
    }

    /**
     * Convert most of exotic age format to gedcom format.
     * @return
     * @throws IOException 
     */
    private boolean processAGE() throws IOException {
        if (input.getTag().equals("AGE")) {
            String old = input.getValue();
            String newValue = old;
            if (old.matches("\\d+")){
                newValue = old+"y";
            }
            newValue = newValue.replace(" day", "d");
            newValue = newValue.replace("ds", "d");

            newValue = newValue.replace(" month", "m");
            newValue = newValue.replace(" mth", "m");
            newValue = newValue.replace("ms", "m");

            newValue = newValue.replace(" yr", "y");
            newValue = newValue.replace("ys", "y");

            // Allaws replace
            if (newValue != null) {
                output.writeLine(input.getLevel(), input.getTag(), newValue);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        fixFAMC(gedcom);
        incrementProgress();
        fixCONC(gedcom);
        incrementProgress();
        return super.fixGedcom(gedcom);

    }
    
    /**
     * Fix CONC tag where thez are inappropriate.
     * ATM remove TITLE:CONC and PAGE:CONC only if there 
     * is no CONT tag where user assistance must be done 
     * using Vidate Gedcom expension
     * @param gedcom
     * @return true
     */
    private boolean fixCONC(Gedcom gedcom){
        // Loop through entities
        for (Entity e : gedcom.getEntities()) {
            fixCONCrecurse(e);
        }
        return true;
    }
    /**
     * Test a property (recursively)
     */
    private void fixCONCrecurse(Property prop) {
        if (prop.getTag().equals("TITL") || prop.getTag().equals("PAGE")){
            Property propConc [] = prop.getProperties ("CONC");
            if (propConc.length==0){
                return;
            }
            if (prop.getProperty("CONT")!= null){
                return;
            }
            StringBuilder sb = new StringBuilder(prop.getValue());
            for (Property p:propConc){
                sb.append(p.getValue());
            }
            prop.delProperties("CONC");
            prop.setValue(sb.toString());
            return;
        }
        for (Property child:prop.getProperties()){
            fixCONCrecurse(child);
        }
        // done
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
//                console.println("famc removed");
                if (famc.getFamily().equals(fc.getFamily())) {
                    fc.getParent().delProperty(fc);
                }
            }
        }
        // Allways succeed
        return true;
    }
}
