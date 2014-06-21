package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
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
                IndividualEditor individualEditorPanel = new IndividualEditor();
                individualEditorPanel.set((Indi) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditor.class, "IndividualEditorPanel.edit.title", entity),
                        individualEditorPanel);
                editorDialog.setDialogId(IndividualEditor.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        individualEditorPanel.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Fam) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                FamilyEditor familyEditorPanel = new FamilyEditor();
                familyEditorPanel.set((Fam) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(FamilyEditor.class, "FamilyEditor.edit.title", entity),
                        familyEditorPanel);
                editorDialog.setDialogId(FamilyEditor.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        familyEditorPanel.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Note) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                NoteEditor noteEditorPanel = new NoteEditor();
                noteEditorPanel.set((Note) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(NoteEditor.class, "NoteEditorPanel.edit.title", entity),
                        noteEditorPanel);
                editorDialog.setDialogId(NoteEditor.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        noteEditorPanel.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Media) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                multiMediaObjectEditor.set((Media) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.edit.title", entity),
                        multiMediaObjectEditor);
                editorDialog.setDialogId(MultiMediaObjectEditor.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        multiMediaObjectEditor.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Source) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                SourceEditor sourceEditor = new SourceEditor();
                sourceEditor.set((Source) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(SourceEditor.class, "SourceEditor.edit.title", entity),
                        sourceEditor);
                editorDialog.setDialogId(SourceEditor.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        sourceEditor.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (entity instanceof Repository) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();
                RepositoryEditor repositoryEditorPanel = new RepositoryEditor();
                repositoryEditorPanel.set((Repository) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(RepositoryEditor.class, "RepositoryEditor.edit.title", entity),
                        repositoryEditorPanel);
                editorDialog.setDialogId(RepositoryEditor.class.getName());

                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        repositoryEditorPanel.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            }
        }
    }
}
