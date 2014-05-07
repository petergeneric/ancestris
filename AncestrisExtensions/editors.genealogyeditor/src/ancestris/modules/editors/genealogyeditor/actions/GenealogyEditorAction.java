package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.modules.editors.genealogyeditor.panels.FamilyEditorPanel;
import ancestris.modules.editors.genealogyeditor.panels.IndividualEditorPanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import javax.swing.Action;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = AncestrisEditor.class,position = 200)
public class GenealogyEditorAction extends AncestrisEditor {

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi || property instanceof Fam);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean edit(Property property, boolean isNew) {
        if (property instanceof Fam) {
            return editEntity((Fam) property, isNew);
        }
        if (property instanceof Indi) {
            return editEntity((Indi) property, isNew);
        }
        return false;
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

    public static boolean editEntity(Fam fam, boolean isNew) {
        DialogManager.ADialog editorDialog;
        FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
        Gedcom gedcom = fam.getGedcom();
        int undoNb = gedcom.getUndoNb();
        familyEditorPanel.set(fam);
        if (isNew) {
            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.create.title"),
                    familyEditorPanel);
        } else {
            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.edit.title", fam),
                    familyEditorPanel);
        }
        editorDialog.setDialogId(FamilyEditorPanel.class.getName());
        if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
            familyEditorPanel.commit();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
        return true;
    }

    public static boolean editEntity(Indi indi, boolean isNew) {
        DialogManager.ADialog editorDialog;
        Gedcom gedcom = indi.getGedcom();
        int undoNb = gedcom.getUndoNb();
        IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();
        individualEditorPanel.set(indi);
        if (isNew) {
            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.create.title"),
                    individualEditorPanel);
        } else {
            editorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.edit.title", indi),
                    individualEditorPanel);
        }
        editorDialog.setDialogId(IndividualEditorPanel.class.getName());
        if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
            individualEditorPanel.commit();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
        return true;
    }
}
