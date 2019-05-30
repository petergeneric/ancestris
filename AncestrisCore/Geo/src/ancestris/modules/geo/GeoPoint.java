/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.openide.util.WeakListeners;

/**
 *
 * @author frederic
 */
public class GeoPoint extends DefaultWaypoint implements PropertyChangeListener {

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
