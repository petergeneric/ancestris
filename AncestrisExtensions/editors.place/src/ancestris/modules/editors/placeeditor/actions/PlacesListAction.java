package ancestris.modules.editors.placeeditor.actions;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.editors.placeeditor.topcomponents.PlacesListTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(id = "ancestris.modules.editors.placeeditor.actions.PlaceListAction", category = "Window")
@ActionRegistration(
        displayName = "#CTL_PlaceListAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/View", name = "PlaceListAction", position = -410)
public final class PlacesListAction extends AbstractAncestrisContextAction {

    public PlacesListAction() {
        super();
        setImage("ancestris/modules/editors/placeeditor/actions/Place.png");
        setText(NbBundle.getMessage(PlacesListAction.class, "CTL_PlaceListAction"));
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
            PlacesListTopComponent tc = new PlacesListTopComponent(contextToOpen.getGedcom());
            tc.init(contextToOpen);
            tc.open();
            tc.requestActive();
        }
    }
    
    private TopComponent findTopComponent(Gedcom gedcom) {
        Set<TopComponent> openTopComponents = WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : openTopComponents) {
            if (tc.getLookup().lookup(Gedcom.class) == gedcom) {
                return tc;
            }
        }

        return null;
    }
}
