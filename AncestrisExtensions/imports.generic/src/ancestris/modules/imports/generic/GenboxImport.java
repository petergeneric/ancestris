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
package ancestris.modules.imports.generic;

import ancestris.api.imports.Import;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import static ancestris.modules.imports.generic.Bundle.*;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyFamilyChild;
import java.util.List;

/**
 *
 * @author daniel
 */
@NbBundle.Messages({
    "genboximport.note=This file has been modified by the Ancestris Genbox Import plugin."
})
@ServiceProvider(service = Import.class)
public class GenboxImport extends Import {

    @Override
    public String toString() {
        return "Genbox";
    }

    @Override
    protected String getImportComment() {
        return genboximport_note();
    }

    @Override
    public boolean fixGedcom(Gedcom gedcom) {
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
        return super.fixGedcom(gedcom); //To change body of generated methods, choose Tools | Templates.
    }
}
