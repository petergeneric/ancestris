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

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.geo.GeoListAction", category = "Window")
@ActionRegistration(
        displayName = "#CTL_GeoListAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/View", name = "GeoListAction", position = -420)
public final class GeoListAction extends AbstractAncestrisContextAction {

    public GeoListAction() {
        super();
        setImage("ancestris/modules/geo/list.png");
        setText(NbBundle.getMessage(GeoListAction.class, "CTL_GeoListAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            GeoListTopComponent tc = new GeoListTopComponent();
            tc.init(contextToOpen);
            tc.open();
            tc.requestActive();
        }
    }
}
