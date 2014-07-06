package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(category = "Edit",
id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateSubmitterAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/submitter_add.png",
displayName = "#CTL_GenealogyEditorCreateSubmitterAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 600)
})
@Messages("CTL_GenealogyEditorCreateSubmitterAction=Create a new submitter")
public final class GenealogyEditorCreateSubmitterAction implements ActionListener {

    private final DataObject context;
    private Submitter mSubmitter;

    public GenealogyEditorCreateSubmitterAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Context gedcomContext;

        if ((gedcomContext = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = gedcomContext.getGedcom();
            int undoNb = gedcom.getUndoNb();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mSubmitter = (Submitter) gedcom.createEntity(Gedcom.SUBM);
                    }
                }); // end of doUnitOfWork

                SubmitterEditor submitterEditor = new SubmitterEditor();
                submitterEditor.set(mSubmitter);

                DialogManager.ADialog sourceEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.create.title"),
                        submitterEditor);
                sourceEditorDialog.setDialogId(SubmitterEditor.class.getName());

                if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    submitterEditor.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
