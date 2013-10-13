package ancestris.modules.editors.placeeditor.actions;

import ancestris.modules.editors.placeeditor.panels.GedcomPlaceFormatEditorPanel;
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
        id = "ancestris.modules.editors.placeeditor.actions.PlaceFormatEditorAction")
@ActionRegistration(
        iconBase = "ancestris/modules/editors/placeeditor/Place.png",
        displayName = "#CTL_PlaceFormatEditorAction")
@ActionReference(path = "Menu/Tools/Gedcom", position = 3333)
@Messages("CTL_PlaceFormatEditorAction=Edit place format")
public final class PlaceFormatEditorAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = context.getGedcom();
            GedcomPlaceFormatEditorPanel gedcomPlaceFormatEditorPanel = new GedcomPlaceFormatEditorPanel(gedcom);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    gedcomPlaceFormatEditorPanel, // instance of your panel
                    org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_PlaceFormatEditorAction"), // title of the dialog
                    true,
                    new Object[]{},
                    (Object) null,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
        }
    }
}
