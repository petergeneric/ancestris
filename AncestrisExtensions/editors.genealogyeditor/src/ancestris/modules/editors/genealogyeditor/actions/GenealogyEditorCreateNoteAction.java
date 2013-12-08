package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.panels.IndividualEditorPanel;
import ancestris.modules.editors.genealogyeditor.panels.NoteEditorPanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(category = "Edit",
id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateNoteAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/Note_add.png",
displayName = "#CTL_GenealogyEditorCreateNoteAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 400)
})
@Messages("CTL_GenealogyEditorCreateNoteAction=Create new note")
public final class GenealogyEditorCreateNoteAction implements ActionListener {

    private final DataObject context;
    private Note mNote;

    public GenealogyEditorCreateNoteAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Context gedcomContext;

        if ((gedcomContext = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = gedcomContext.getGedcom();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mNote = (Note) gedcom.createEntity(Gedcom.NOTE);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                NoteEditorPanel noteEditorPanel = new NoteEditorPanel();
                noteEditorPanel.set(mNote);
                DialogManager.ADialog noteEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(NoteEditorPanel.class, "NoteEditorPanel.title"),
                        noteEditorPanel);
                noteEditorDialog.setDialogId(NoteEditorPanel.class.getName());
                if (noteEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    noteEditorPanel.commit();
                } else {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }
}
