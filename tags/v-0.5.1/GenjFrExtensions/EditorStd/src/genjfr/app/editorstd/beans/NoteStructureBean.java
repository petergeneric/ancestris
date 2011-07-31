/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.editorstd.beans;

import genj.gedcom.Property;
import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class NoteStructureBean implements Serializable {

    private Property note;
    public static final String PROP_NOTE = "NOTE";
    private Property[] sour;
    public static final String PROP_SOUR = "SOUR";

    private PropertyChangeSupport propertySupport;

    public NoteStructureBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of note
     *
     * @return the value of note
     */
    public Property getNote() {
        return note;
    }

    /**
     * Set the value of note
     *
     * @param note new value of note
     */
    public void setNote(Property note) {
        Property oldNote = this.note;
        this.note = note;
        propertySupport.firePropertyChange(PROP_NOTE, oldNote, note);
    }

    /**
     * Get the value of sour
     *
     * @return the value of sour
     */
    public Property[] getSour() {
        return sour;
    }

    /**
     * Set the value of sour
     *
     * @param sour new value of sour
     */
    public void setSour(Property[] sour) {
        Property[] oldSour = this.sour;
        this.sour = sour;
        propertySupport.firePropertyChange(PROP_SOUR, oldSour, sour);
    }

    /**
     * Get the value of sour at specified index
     *
     * @param index
     * @return the value of sour at specified index
     */
    public Property getSour(int index) {
        return this.sour[index];
    }

    /**
     * Set the value of sour at specified index.
     *
     * @param index
     * @param newSour new value of sour at specified index
     */
    public void setSour(int index, Property newSour) {
        Property oldSour = this.sour[index];
        this.sour[index] = newSour;
        propertySupport.fireIndexedPropertyChange(PROP_SOUR, index, oldSour, newSour);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
