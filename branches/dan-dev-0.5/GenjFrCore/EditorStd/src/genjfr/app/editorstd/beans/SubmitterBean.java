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
public class SubmitterBean implements Serializable {

    private PropertySimpleValue name;
    public static final String PROP_NAME = "NAME";
    private PropertySimpleValue lang;
    public static final String PROP_LANG = "LANG";
    private PropertySimpleValue rfn;
    public static final String PROP_RFN = "RFN";
    private PropertySimpleValue rin;
    public static final String PROP_RIN = "RIN";




    private PropertyChangeSupport propertySupport;

    public SubmitterBean() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public PropertySimpleValue getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(PropertySimpleValue name) {
        PropertySimpleValue oldName = this.name;
        this.name = name;
        propertySupport.firePropertyChange(PROP_NAME, oldName, name);
    }

    /**
     * Get the value of lang
     *
     * @return the value of lang
     */
    public PropertySimpleValue getLang() {
        return lang;
    }

    /**
     * Set the value of lang
     *
     * @param lang new value of lang
     */
    public void setLang(PropertySimpleValue lang) {
        PropertySimpleValue oldLang = this.lang;
        this.lang = lang;
        propertySupport.firePropertyChange(PROP_LANG, oldLang, lang);
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
