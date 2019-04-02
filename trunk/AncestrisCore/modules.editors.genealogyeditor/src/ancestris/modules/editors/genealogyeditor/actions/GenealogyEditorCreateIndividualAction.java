package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
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

        if ((gedcomContext = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = gedcomContext.getGedcom();
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            if (atc == null) {

                AncestrisTopComponent win = new AriesTopComponent().create(gedcomContext);
                win.open();
                win.requestActive();
            }

            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        entity = gedcom.createEntity(Gedcom.INDI);
                        entity.addProperty("NAME", "");
                    }
                }); // end of doUnitOfWork

                IndividualEditor individualEditor = new IndividualEditor(true);
                individualEditor.setContext(new Context(entity));
                if (!individualEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
