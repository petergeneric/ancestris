package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
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
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorCreateNoteAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/note_add.png",
        displayName = "#CTL_GenealogyEditorCreateNoteAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 500)
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
                        mNote = (Note) gedcom.createEntity(Gedcom.NOTE);
                    }
                }); // end of doUnitOfWork
                NoteEditor noteEditor = new NoteEditor(true);
                noteEditor.setContext(new Context(mNote));
                if (!noteEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
