package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
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
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateSubmitterAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/submitter_add.png",
        displayName = "#CTL_GenealogyEditorCreateSubmitterAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 800)
})
@Messages("CTL_GenealogyEditorCreateSubmitterAction=Create a new submitter")
public final class GenealogyEditorCreateSubmitterAction implements ActionListener {

    private final DataObject context;
    private Submitter mSubmitter;

    public GenealogyEditorCreateSubmitterAction(DataObject context) {
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
                        mSubmitter = (Submitter) gedcom.createEntity(Gedcom.SUBM);
                    }
                }); // end of doUnitOfWork

                SubmitterEditor submitterEditor = new SubmitterEditor(true);
                submitterEditor.setContext(new Context(mSubmitter));
                if (!submitterEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
