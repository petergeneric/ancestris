/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.geo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.openide.util.WeakListeners;

/**
 *
 * @author frederic
 */
public class GeoPoint extends Waypoint implements PropertyChangeListener {

    GeoNodeObject gno = null;

    public GeoPoint(GeoNodeObject obj) {
        super(obj.getLatitude(), obj.getLongitude());
        this.gno = obj;
        if (obj != null) {
            obj.addPropertyChangeListener(WeakListeners.propertyChange(this, obj));
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if ("topo".equals(pce.getPropertyName())) {
            setPosition(gno.getGeoPosition());
        }
    }

    public GeoNodeObject getGeoNodeObject() {
        return gno;
    }
}
