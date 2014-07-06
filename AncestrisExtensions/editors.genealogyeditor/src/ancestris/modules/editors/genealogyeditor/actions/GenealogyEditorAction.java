package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import javax.swing.Action;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = AncestrisEditor.class, position = 200)
public class GenealogyEditorAction extends AncestrisEditor {

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi
                || property instanceof Fam
                || property instanceof Note
                || property instanceof Media
                || property instanceof Source
                || property instanceof Submitter
                || property instanceof Repository);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean edit(Property property, boolean isNew) {
        DialogManager.ADialog editorDialog;
        Gedcom gedcom = property.getGedcom();
        int undoNb = gedcom.getUndoNb();

        if (property instanceof Indi) {
            IndividualEditor individualEditor = new IndividualEditor();
            individualEditor.set((Indi) property);
            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(IndividualEditor.class, isNew ? "IndividualEditor.create.title" : "IndividualEditor.edit.title", property),
                    individualEditor);
            editorDialog.setDialogId(IndividualEditor.class.getName());
            if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    individualEditor.commit();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            return true;
        } else if (property instanceof Fam) {
            FamilyEditor familyEditorPanel = new FamilyEditor();
            familyEditorPanel.set((Fam) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(FamilyEditor.class, isNew ? "FamilyEditor.create.title" : "FamilyEditor.edit.title", property),
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
            return true;
        } else if (property instanceof Note) {
            NoteEditor noteEditor = new NoteEditor();
            noteEditor.set((Note) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(NoteEditor.class, isNew ? "NoteEditor.create.title" : "NoteEditor.edit.title", property),
                    noteEditor);
            editorDialog.setDialogId(NoteEditor.class.getName());
            if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    noteEditor.commit();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            return true;
        } else if (property instanceof Media) {
            MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
            multiMediaObjectEditor.set((Media) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(MultiMediaObjectEditor.class, isNew ? "MultiMediaObjectEditor.create.title" : "MultiMediaObjectEditor.edit.title", property),
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
            return true;
        } else if (property instanceof Source) {
            SourceEditor sourceEditor = new SourceEditor();
            sourceEditor.set((Source) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(SourceEditor.class, isNew ? "SourceEditor.create.title" : "SourceEditor.edit.title", property),
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
            return true;
        } else if (property instanceof Repository) {
            RepositoryEditor repositoryEditor = new RepositoryEditor();
            repositoryEditor.set((Repository) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RepositoryEditor.class, isNew ? "RepositoryEditor.create.title" : "RepositoryEditor.edit.title", property),
                    repositoryEditor);
            editorDialog.setDialogId(RepositoryEditor.class.getName());

            if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    repositoryEditor.commit();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            return true;
        } else if (property instanceof Submitter) {
            SubmitterEditor submitterEditor = new SubmitterEditor();
            submitterEditor.set((Submitter) property);

            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(SubmitterEditor.class, isNew ? "SubmitterEditor.create.title" : "SubmitterEditor.edit.title", property),
                    submitterEditor);
            editorDialog.setDialogId(SubmitterEditor.class.getName());

            if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    submitterEditor.commit();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Action getCreateParentAction(Indi child, int sex) {
        return AActions.alwaysEnabled(
                new ACreateParent(child, sex, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.createparent.title"),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateChildAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateChild(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.createchild.title", indi),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateSpouse(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.addspouse.title"),
                "ancestris/modules/editors/standard/images/add-spouse.png", // NOI18N
                true);
    }
}
