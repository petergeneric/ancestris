package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
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
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorAddFamilyAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/family_add.png",
        displayName = "#CTL_GenealogyEditorAddFamilyAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 300)
})
@Messages("CTL_GenealogyEditorAddFamilyAction=Create a Family")
public final class GenealogyEditorCreateFamilyAction implements ActionListener {

    private final DataObject context;
    Entity entity;

    public GenealogyEditorCreateFamilyAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Context gedcomContext;
        DialogManager.ADialog editorDialog;

        if ((gedcomContext = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = gedcomContext.getGedcom();
            int undoNb = gedcom.getUndoNb();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        entity = gedcom.createEntity(Gedcom.FAM);
                    }
                }); // end of doUnitOfWork
                FamilyEditor familyEditorPanel = new FamilyEditor();
                familyEditorPanel.set((Fam) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(FamilyEditor.class, "FamilyEditor.create.title"),
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
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
