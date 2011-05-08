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
public class MultimediaLinkBean implements Serializable {

    private Property obje;
    public static final String PROP_OBJE = "OBJE";
    private Property form;
    public static final String PROP_FORM = "FORM";
    private Property titl;
    public static final String PROP_TITL = "TITL";
    private Property file;
    public static final String PROP_FILE = "FILE";
    private Property[] note;
    public static final String PROP_NOTE = "NOTE";



    private PropertyChangeSupport propertySupport;

    public MultimediaLinkBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of obje
     *
     * @return the value of obje
     */
    public Property getObje() {
        return obje;
    }

    /**
     * Set the value of obje
     *
     * @param obje new value of obje
     */
    public void setObje(Property obje) {
        Property oldObje = this.obje;
        this.obje = obje;
        propertySupport.firePropertyChange(PROP_OBJE, oldObje, obje);
    }

    /**
     * Get the value of form
     *
     * @return the value of form
     */
    public Property getForm() {
        return form;
    }

    /**
     * Set the value of form
     *
     * @param form new value of form
     */
    public void setForm(Property form) {
        Property oldForm = this.form;
        this.form = form;
        propertySupport.firePropertyChange(PROP_FORM, oldForm, form);
    }

    /**
     * Get the value of title
     *
     * @return the value of title
     */
    public Property getTitl() {
        return titl;
    }

    /**
     * Set the value of title
     *
     * @param title new value of title
     */
    public void setTitl(Property titl) {
        Property oldTitl = this.titl;
        this.titl = titl;
        propertySupport.firePropertyChange(PROP_TITL, oldTitl, titl);
    }

    /**
     * Get the value of file
     *
     * @return the value of file
     */
    public Property getFile() {
        return file;
    }

    /**
     * Set the value of file
     *
     * @param file new value of file
     */
    public void setFile(Property file) {
        Property oldFile = this.file;
        this.file = file;
        propertySupport.firePropertyChange(PROP_FILE, oldFile, file);
    }

    /**
     * Get the value of note
     *
     * @return the value of note
     */
    public Property[] getNote() {
        return note;
    }

    /**
     * Set the value of note
     *
     * @param note new value of note
     */
    public void setNote(Property[] note) {
        Property[] oldNote = this.note;
        this.note = note;
        propertySupport.firePropertyChange(PROP_NOTE, oldNote, note);
    }

    /**
     * Get the value of note at specified index
     *
     * @param index
     * @return the value of note at specified index
     */
    public Property getNote(int index) {
        return this.note[index];
    }

    /**
     * Set the value of note at specified index.
     *
     * @param index
     * @param newNote new value of note at specified index
     */
    public void setNote(int index, Property newNote) {
        Property oldNote = this.note[index];
        this.note[index] = newNote;
        propertySupport.fireIndexedPropertyChange(PROP_NOTE, index, oldNote, newNote);
    }



    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
