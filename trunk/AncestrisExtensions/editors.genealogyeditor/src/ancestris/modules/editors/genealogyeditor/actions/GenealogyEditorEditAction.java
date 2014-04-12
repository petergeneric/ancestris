package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.panels.*;
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
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.GenealogyEditorAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/edit.png",
        displayName = "#CTL_IndividualEditorAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 100)
})
@Messages("CTL_IndividualEditorAction=Edit current individual")
public final class GenealogyEditorEditAction implements ActionListener {

    private final DataObject context;

    public GenealogyEditorEditAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;
        DialogManager.ADialog editorDialog;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Entity entity = context.getEntity();
            if (entity instanceof Indi) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();
                individualEditorPanel.set((Indi) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.edit.title", entity),
                        individualEditorPanel);
                editorDialog.setDialogId(IndividualEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    individualEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Fam) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
                familyEditorPanel.set((Fam) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.edit.title", entity),
                        familyEditorPanel);
                editorDialog.setDialogId(FamilyEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    familyEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Note) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                NoteEditorPanel noteEditorPanel = new NoteEditorPanel();
                noteEditorPanel.set(gedcom, null, (Note) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(NoteEditorPanel.class, "NoteEditorPanel.edit.title", entity),
                        noteEditorPanel);
                editorDialog.setDialogId(NoteEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    noteEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Media) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                MultiMediaObjectEditorPanel multiMediaObjectEditorPanel = new MultiMediaObjectEditorPanel();
                multiMediaObjectEditorPanel.set((Media) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(MultiMediaObjectEditorPanel.class, "MultiMediaObjectEditorPanel.edit.title", entity),
                        multiMediaObjectEditorPanel);
                editorDialog.setDialogId(SourceEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    multiMediaObjectEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Source) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
                sourceEditorPanel.setSource((Source) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.edit.title", entity),
                        sourceEditorPanel);
                editorDialog.setDialogId(SourceEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    sourceEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Repository) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                RepositoryEditorPanel repositoryEditorPanel = new RepositoryEditorPanel();
                repositoryEditorPanel.setRepository((Repository) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(RepositoryEditorPanel.class, "RepositoryEditorPanel.edit.title", entity),
                        repositoryEditorPanel);
                editorDialog.setDialogId(RepositoryEditorPanel.class.getName());

                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    repositoryEditorPanel.commit();
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            }
        }
    }
}
