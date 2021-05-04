/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.geo.GeoMapAction", category = "Window")
@ActionRegistration(
        displayName = "#CTL_GeoMapAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/View", name = "GeoMapAction", position = -650)
public final class GeoMapAction extends AbstractAncestrisContextAction {

    public GeoMapAction() {
        super();
        setImage("ancestris/modules/geo/geo16.png");
        setText(NbBundle.getMessage(GeoMapAction.class, "CTL_GeoMapAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        GeoMapTopComponent tc = new GeoMapTopComponent();
        tc.init(contextToOpen);
        tc.open();
        tc.requestActive();
    }
}
