package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Media;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
        category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateMultiMediaAction"
)
@ActionRegistration(
        iconBase = "ancestris/modules/editors/genealogyeditor/resources/media_add.png",
        displayName = "#CTL_GenealogyEditorCreateMultiMediaAction"
)
@ActionReference(path = "Toolbars/GenealogyEditor", position = 400)
@Messages("CTL_GenealogyEditorCreateMultiMediaAction=Create new multimedia object")
public final class GenealogyEditorCreateMultiMediaAction implements ActionListener {

    private final DataObject context;
    private Media mMedia;

    public GenealogyEditorCreateMultiMediaAction(DataObject context) {
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

            int undoNb = gedcom.getUndoNb();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mMedia = (Media) gedcom.createEntity(Gedcom.OBJE);
                    }
                }); // end of doUnitOfWork

                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor(true);
                multiMediaObjectEditor.setContext(new Context(mMedia));
                if (!multiMediaObjectEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
