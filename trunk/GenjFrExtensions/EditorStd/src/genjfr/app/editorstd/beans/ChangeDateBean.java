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
public class ChangeDateBean implements Serializable {

    private Property date;
    public static final String PROP_DATE = "DATE";
    private Property time;
    public static final String PROP_TIME = "TIME";


    private PropertyChangeSupport propertySupport;

    public ChangeDateBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of date
     *
     * @return the value of date
     */
    public Property getDate() {
        return date;
    }

    /**
     * Set the value of date
     *
     * @param date new value of date
     */
    public void setDate(Property date) {
        Property oldDate = this.date;
        this.date = date;
        propertySupport.firePropertyChange(PROP_DATE, oldDate, date);
    }

    /**
     * Get the value of time
     *
     * @return the value of time
     */
    public Property getTime() {
        return time;
    }

    /**
     * Set the value of time
     *
     * @param time new value of time
     */
    public void setTime(Property time) {
        Property oldTime = this.time;
        this.time = time;
        propertySupport.firePropertyChange(PROP_TIME, oldTime, time);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
