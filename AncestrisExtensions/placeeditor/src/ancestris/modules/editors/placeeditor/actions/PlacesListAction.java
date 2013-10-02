package ancestris.modules.editors.placeeditor.actions;

import ancestris.modules.editors.placeeditor.topcomponents.PlacesListTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Tools",
id = "ancestris.modules.editors.placeeditor.actions.PlaceListAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/placeeditor/actions/Place.png",
displayName = "#CTL_PlaceListAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 1450)
})
@Messages("CTL_PlaceListAction=Places list")
public final class PlacesListAction implements ActionListener {

    Context context;

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = context.getGedcom();

            TopComponent tc = findTopComponent(gedcom);
            if (tc == null) {
                tc = new PlacesListTopComponent(gedcom);
                tc.open();
            }
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
