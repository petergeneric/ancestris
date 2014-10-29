/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

import genj.gedcom.Context;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Utilities;

@ActionID(id = "ancestris.modules.geo.GeoMapAction", category = "Window")
@ActionRegistration(iconBase = "ancestris/modules/geo/geo.png", displayName = "#CTL_GeoMapAction", iconInMenu = true)
@ActionReference(path = "Menu/View", name = "GeoMapAction", position = -650)
public final class GeoMapAction implements ActionListener {

    private Context context = null;

    public GeoMapAction(Property property) {
        context = new Context(property);
    }
    
    public void actionPerformed(ActionEvent e) {
        GeoMapTopComponent tc = new GeoMapTopComponent();
//        Context c = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null){
            return;
        }
        tc.init(context);
        tc.open();
        tc.requestActive();
    }
}
