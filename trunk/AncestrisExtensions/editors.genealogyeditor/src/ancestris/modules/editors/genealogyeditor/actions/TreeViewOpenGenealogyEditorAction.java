package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.core.actions.AbstractAncestrisAction;
import static ancestris.modules.editors.genealogyeditor.actions.Bundle.*;
import ancestris.modules.editors.genealogyeditor.panels.FamilyEditorPanel;
import ancestris.modules.editors.genealogyeditor.panels.IndividualEditorPanel;
import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
//@ActionID(category = "Edit", id = "ancestris.modules.editors.genealogyeditor")
//@ActionRegistration(displayName = "#OpenInEditor.title")
//@ActionReferences({
//    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
@NbBundle.Messages("OpenInEditor.title=Edit using the Genealogy Editor")
public class TreeViewOpenGenealogyEditorAction extends AbstractAction implements ContextAwareAction {

    private Gedcom gedcom = null;
    private Entity entity = null;

    @Override
    public void actionPerformed(ActionEvent ae) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(org.openide.util.Lookup context) {
        return new OpenEditor(context.lookup(Entity.class));
    }

    private static final class OpenEditor extends AbstractAncestrisAction {

        Entity entity;

        public OpenEditor(Entity context) {
            this.entity = context;
            setText(OpenInEditor_title());  // NOI18N
            setImage(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.muteSelection(true);
            DialogManager.ADialog editorDialog;
            if (entity != null) {

                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();

                if (entity instanceof Indi) {
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
                }

                SelectionDispatcher.muteSelection(false);
            }
        }
    }
}
