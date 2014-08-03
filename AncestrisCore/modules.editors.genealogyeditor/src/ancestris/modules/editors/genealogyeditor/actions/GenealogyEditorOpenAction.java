package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.GenealogyEditorTopComponent;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/*
@ActionID(
        category = "View",
        id = "ancestris.modules.editors.genealogyeditor.GenealogyEditorOpenAction"
)
@ActionRegistration(
        displayName = "#CTL_GenealogyEditorOpenAction"
)
@ActionReference(path = "Menu/View", position = 125)
@Messages("CTL_GenealogyEditorOpenAction=Genealogy Editor")
*/
public final class GenealogyEditorOpenAction implements ActionListener {

    private final DataObject context;

    public GenealogyEditorOpenAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Context contextToOpen = Utilities.actionsGlobalContext().lookup(Context.class);
        if (contextToOpen != null) {
            AncestrisTopComponent win = new GenealogyEditorTopComponent().create(contextToOpen);
//            win.init(contextToOpen);
            win.open();
            win.requestActive();
            // TODO use context
        }
    }
}
