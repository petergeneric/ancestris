/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GeoPlugin extends AncestrisPlugin implements GedcomFileListener {

    public void commitRequested(Context context) {
    }

    public void gedcomClosed(Gedcom gedcom) {
        GeoPlacesList.remove(gedcom);
    }

    public void gedcomOpened(Gedcom gedcom) {
    }

    @Override
    public boolean launchModule(Object o) {
        if (o instanceof Gedcom) {
            Gedcom gedcom = (Gedcom) o;
            GeoListTopComponent tc = new GeoListTopComponent();
            tc.init(new Context(gedcom));
            tc.open();
            tc.requestActive();
        }

        return true;
    }

    
}
