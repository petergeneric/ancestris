package ancestris.modules.editors.placeeditor.actions;

import ancestris.modules.editors.placeeditor.gedcom.GedcomPlacesEditorPanel;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
        category = "Tools",
        id = "ancestris.gedcom.place.GedcomPlaceAction")
@ActionRegistration(
        iconBase = "ancestris/modules/editors/placeeditor/Place.png",
        displayName = "#CTL_GedcomPlaceEditorAction")
@ActionReference(path = "Menu/Tools", position = 700)
@Messages("CTL_GedcomPlaceEditorAction=Places editor")
public final class GedcomPlaceEditorAction implements ActionListener {

    Context context;

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = context.getGedcom();
            GedcomPlacesEditorPanel gedcomPlacesPanel = new GedcomPlacesEditorPanel(gedcom);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    gedcomPlacesPanel, // instance of your panel
                    org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_GedcomPlaceEditorAction") + " - " + gedcom.getName(), // title of the dialog
                    false,
                    new Object[]{},
                    (Object)null,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
        }
    }
}
