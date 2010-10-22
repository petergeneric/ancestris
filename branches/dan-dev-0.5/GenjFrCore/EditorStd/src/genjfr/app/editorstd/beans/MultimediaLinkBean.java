/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.editorstd.beans;

import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class MultimediaLinkBean implements Serializable {

    private PropertyChangeSupport propertySupport;

    public MultimediaLinkBean() {
        propertySupport = new PropertyChangeSupport(this);
    }




    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
