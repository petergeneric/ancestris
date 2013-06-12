package ancestris.modules.editors.placeeditor.actions;

import ancestris.modules.editors.placeeditor.FindPlacePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "ancestris.modules.editors.placeeditor.PlaceEditorAction")
@ActionRegistration(
        iconBase = "ancestris/modules/editors/placeeditor/Place.png",
        displayName = "#CTL_SearchPlaceAction")
@ActionReference(path = "Menu/Tools", position = 600)
@Messages("CTL_SearchPlaceAction=Search Place")
public final class SearchPlaceAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FindPlacePanel placeEditorPanel = new FindPlacePanel();
        // Create a custom NotifyDescriptor, specify the panel instance as a parameter + other params
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                placeEditorPanel, // instance of your panel
                org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_SearchPlaceAction"), // title of the dialog
                false,
                DialogDescriptor.DEFAULT_OPTION,
                DialogDescriptor.CLOSED_OPTION,
                null);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
    }
}
