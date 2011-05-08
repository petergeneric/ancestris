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
public class RegisteredBean implements Serializable {

    private PropertySimpleValue rfn;
    public static final String PROP_RFN = "RFN";

    private PropertyChangeSupport propertySupport;

    public RegisteredBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of rfn
     *
     * @return the value of rfn
     */
    public PropertySimpleValue getRfn() {
        return rfn;
    }

    /**
     * Set the value of rfn
     *
     * @param rfn new value of rfn
     */
    public void setRfn(PropertySimpleValue rfn) {
        PropertySimpleValue oldRfn = this.rfn;
        this.rfn = rfn;
        propertySupport.firePropertyChange(PROP_RFN, oldRfn, rfn);
    }



    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
