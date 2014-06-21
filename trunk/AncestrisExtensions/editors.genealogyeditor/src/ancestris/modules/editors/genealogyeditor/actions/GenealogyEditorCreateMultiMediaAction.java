package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Media;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;


@ActionID(
        category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateMultiMediaAction"
)
@ActionRegistration(
        iconBase = "ancestris/modules/editors/genealogyeditor/resources/Media_add.png",
        displayName = "#CTL_GenealogyEditorCreateMultiMediaAction"
)
@ActionReference(path = "Toolbars/GenealogyEditor", position = 500)
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
            int undoNb = gedcom.getUndoNb();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mMedia = (Media) gedcom.createEntity(Gedcom.OBJE);
                    }
                }); // end of doUnitOfWork

                MultiMediaObjectEditor multiMediaObjectEditorPanel = new MultiMediaObjectEditor();
                multiMediaObjectEditorPanel.set((Media) mMedia);

                DialogManager.ADialog multiMediaObjectEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditorPanel.create.title"),
                        multiMediaObjectEditorPanel);
                multiMediaObjectEditorDialog.setDialogId(MultiMediaObjectEditor.class.getName());

                if (multiMediaObjectEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    multiMediaObjectEditorPanel.commit();
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
