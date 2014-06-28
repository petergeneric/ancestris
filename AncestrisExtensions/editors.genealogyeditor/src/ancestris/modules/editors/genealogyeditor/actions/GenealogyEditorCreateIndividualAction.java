package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
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
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateIndividualAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/indi_add.png",
        displayName = "#CTL_GenealogyEditorCreateIndividualAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 200)
})
@Messages("CTL_GenealogyEditorCreateIndividualAction=Create new Individual")
public final class GenealogyEditorCreateIndividualAction implements ActionListener {

    private final DataObject context;
    Entity entity;

    public GenealogyEditorCreateIndividualAction(DataObject context) {
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
                        entity = gedcom.createEntity(Gedcom.INDI);
                    }
                }); // end of doUnitOfWork

                IndividualEditor individualEditor = new IndividualEditor();

                individualEditor.set((Indi) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.create.title"),
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
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
