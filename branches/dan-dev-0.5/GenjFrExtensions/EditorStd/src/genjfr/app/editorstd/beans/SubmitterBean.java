/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.beans;

import genj.gedcom.Property;
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
    public final int langSize = 3;
    private Property[] lang = new Property[langSize];
    public static final String PROP_LANG = "LANG";
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
    public Property[] getLang() {
        return lang;
    }

    /**
     * Set the value of lang
     *
     * @param lang new value of lang
     */
    public void setLang(Property[] lang) {
        if (lang == null) {
            return;
        }
        Property[] oldLang = this.lang;
        for (int i = 0; i < langSize; i++) {
            if (lang.length > i) {
                this.lang[i] = lang[i];
            } else {
                this.lang[i] = null;
            }
        }
        propertySupport.firePropertyChange(PROP_LANG, oldLang, lang);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
