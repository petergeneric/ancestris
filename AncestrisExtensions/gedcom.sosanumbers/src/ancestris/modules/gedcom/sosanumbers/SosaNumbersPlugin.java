/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class SosaNumbersPlugin extends AncestrisPlugin implements GedcomFileListener {

    SosaNumbers sosaNumbers = null;

    @Override
    public void commitRequested(Context context) {
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
        if (sosaNumbers != null) {
//        gedcom.removeGedcomListener(sosaNumbers);
        }
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        Preferences modulePreferences = NbPreferences.forModule(SosaNumbers.class);
        String selectedEntityID = modulePreferences.get("SelectEntityDialog." + gedcom.getName(), "");
        Indi indiDeCujus = null;
        if (!selectedEntityID.isEmpty()) {
            indiDeCujus = (Indi) gedcom.getEntity(Gedcom.INDI, selectedEntityID);
        }
        if (indiDeCujus != null) {
            sosaNumbers = new SosaNumbers();
            sosaNumbers.generateSosaNbs(gedcom, indiDeCujus);
//          gedcom.addGedcomListener(sosaNumbers);
        }
    }
}
