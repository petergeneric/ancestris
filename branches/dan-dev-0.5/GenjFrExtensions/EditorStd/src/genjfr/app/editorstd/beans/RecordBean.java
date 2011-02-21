/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.editorstd.beans;

import genj.gedcom.PropertySimpleValue;
import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class RecordBean implements Serializable {

    private PropertySimpleValue rin;
    public static final String PROP_RIN = "RIN";

    private PropertyChangeSupport propertySupport;

    public RecordBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of rin
     *
     * @return the value of rin
     */
    public PropertySimpleValue getRin() {
        return rin;
    }

    /**
     * Set the value of rin
     *
     * @param rin new value of rin
     */
    public void setRin(PropertySimpleValue rin) {
        PropertySimpleValue oldRin = this.rin;
        this.rin = rin;
        propertySupport.firePropertyChange(PROP_RIN, oldRin, rin);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
