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

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.geo.GeoListAction", category = "Window")
@ActionRegistration(iconBase = "ancestris/modules/geo/list.png", displayName = "#CTL_GeoListAction", iconInMenu = true)
@ActionReference(path = "Menu/View", name = "GeoListAction", position = -450)
public final class GeoListAction implements ActionListener {

    Context context = null;

    public GeoListAction(Property property) {
        context = new Context(property);
    }

    public void actionPerformed(ActionEvent e) {
        GeoListTopComponent tc = new GeoListTopComponent();
        if (context == null) {
            return;
        }
        tc.init(context);
        tc.open();
        tc.requestActive();
    }
}
