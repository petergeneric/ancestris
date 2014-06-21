package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
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
id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateNoteAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/Note_add.png",
displayName = "#CTL_GenealogyEditorCreateNoteAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 400)
})
@Messages("CTL_GenealogyEditorCreateNoteAction=Create a new Note")
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
            int undoNb = gedcom.getUndoNb();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mNote = (Note) gedcom.createEntity(Gedcom.NOTE);
                    }
                }); // end of doUnitOfWork
                NoteEditor noteEditorPanel = new NoteEditor();
                noteEditorPanel.set(mNote);
                DialogManager.ADialog noteEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(NoteEditor.class, "NoteEditor.create.title"),
                        noteEditorPanel);
                noteEditorDialog.setDialogId(NoteEditor.class.getName());
                if (noteEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    noteEditorPanel.commit();
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
