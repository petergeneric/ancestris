package ancestris.modules.editors.genealogyeditor.editors;

import ancestris.api.editor.Editor;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public abstract class EntityEditor extends Editor {

    private final boolean isNew;

    public EntityEditor (boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public boolean showPanel() {

        Gedcom gedcom = getContext().getGedcom();
        int undoNb = gedcom.getUndoNb();

        DialogManager dialogManager = new DialogManager.ADialog(getTitle(), this);
        dialogManager.setDialogId(this.getClass().getName());
        if (dialogManager.show().equals(NotifyDescriptor.OK_OPTION)) {
            if (changes.hasChanged()) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            commit();
                        }
                    });
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
                changes.fireChangeEvent();
            }
            return true;
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
            return false;
        }
    }

    /**
     * @return the isNew
     */
    public boolean isNew() {
        return isNew;
    }
}
