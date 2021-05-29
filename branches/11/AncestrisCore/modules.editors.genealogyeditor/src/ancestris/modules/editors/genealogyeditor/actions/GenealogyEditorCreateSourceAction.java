package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
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
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateSourceAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/source_add.png",
        displayName = "#CTL_GenealogyEditorCreateSourceAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 600)
})
@Messages("CTL_GenealogyEditorCreateSourceAction=Create a new source")
public final class GenealogyEditorCreateSourceAction implements ActionListener {

    private final DataObject context;
    private Source mSource;

    public GenealogyEditorCreateSourceAction(DataObject context) {
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
                        mSource = (Source) gedcom.createEntity(Gedcom.SOUR);
                    }
                }); // end of doUnitOfWork

                SourceEditor sourceEditor = new SourceEditor(true);
                sourceEditor.setContext(new Context(mSource));
                if (!sourceEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
