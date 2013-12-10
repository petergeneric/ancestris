package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.panels.IndividualEditorPanel;
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
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        entity = gedcom.createEntity(Gedcom.INDI);
                    }
                }); // end of doUnitOfWork

                IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();

                individualEditorPanel.set((Indi) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.title"),
                        individualEditorPanel);

                editorDialog.setDialogId(IndividualEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    individualEditorPanel.commit();
                } else {
                    while (gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
