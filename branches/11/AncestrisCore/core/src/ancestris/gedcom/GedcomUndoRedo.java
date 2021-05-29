/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.gedcom;

import genj.gedcom.Gedcom;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;

/**
 *
 * @author daniel
 *
 * voir aussi UndoRedo.Manager. Pour le moment on code notre propre classe
 */
public class GedcomUndoRedo implements UndoRedo {

    private Gedcom gedcom;
    private final ChangeSupport cs = new ChangeSupport(this);

    public GedcomUndoRedo(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public boolean canUndo() {
        return (gedcom != null && gedcom.canUndo());
    }

    public boolean canRedo() {
        return (gedcom != null && gedcom.canRedo());
    }

    public void undo() throws CannotUndoException {
        if (gedcom != null && gedcom.canUndo()) {
            gedcom.undoUnitOfWork();
        }
    }

    public void redo() throws CannotRedoException {
        gedcom.redoUnitOfWork();
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /* Removes the listener
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public void gedcomUpdated(Gedcom gedcom) {
        if (this.gedcom.equals(gedcom)) {
            cs.fireChange();
        }
    }

    public String getUndoPresentationName() {
        // TODO: mettre ici le libelle de l'action a annuler
        return "";
    }

    public String getRedoPresentationName() {
        // TODO: mettre ici le libelle de l'action a retablir
        return "";
    }
}
