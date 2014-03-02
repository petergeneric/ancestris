package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.panels.RepositoryEditorPanel;
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
id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateRepositoryAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/Repository.png",
displayName = "#CTL_GenealogyEditorCreateRepositoryAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 600)
})
@Messages("CTL_GenealogyEditorCreateRepositoryAction=Create a new repository")
public final class GenealogyEditorCreateRepositoryAction implements ActionListener {

    private final DataObject context;
    private Repository mRepository;

    public GenealogyEditorCreateRepositoryAction(DataObject context) {
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
                        mRepository = (Repository) gedcom.createEntity(Gedcom.REPO);
                    }
                }); // end of doUnitOfWork
                RepositoryEditorPanel repositoryEditorPanel = new RepositoryEditorPanel();
                repositoryEditorPanel.setRepository(mRepository);

                DialogManager.ADialog editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(RepositoryEditorPanel.class, "RepositoryEditorPanel.create.title"),
                        repositoryEditorPanel);
                editorDialog.setDialogId(RepositoryEditorPanel.class.getName());

                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    repositoryEditorPanel.commit();
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
