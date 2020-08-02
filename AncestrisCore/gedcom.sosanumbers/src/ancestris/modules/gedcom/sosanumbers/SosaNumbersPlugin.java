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
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class SosaNumbersPlugin extends AncestrisPlugin implements Constants, GedcomFileListener {

    @Override
    public void commitRequested(Context context) {
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        String selectedEntityID = gedcom.getRegistry().get(DECUJUSID, "");
        Indi indiDeCujus = null;
        if (!selectedEntityID.isEmpty()) {
            indiDeCujus = (Indi) gedcom.getEntity(Gedcom.INDI, selectedEntityID);
        }
      //Desactivate numering on flight
      //  Boolean saved = gedcom.getRegistry().get(SAVE, true);
      //  if (indiDeCujus != null && !saved) {
      //      sosaNumbers = new SosaNumbersGenerator();
      //      sosaNumbers.run(gedcom, indiDeCujus, null);
      //  }
    }
}
