/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author frederic
 */
class GeoChildrenNodes extends Children.Keys {

    static private GeoPlacesList geoPlacesList = null;
    private boolean isPlace = false;
    private boolean isEvent = false;
    private GeoNodeObject parent = null;

    public GeoChildrenNodes(GeoPlacesList gpl) {
        GeoChildrenNodes.geoPlacesList = gpl;
        isPlace = true;
    }

    public GeoChildrenNodes(GeoNodeObject parent) {
        this.parent = parent;
        isEvent = true;
    }

    @Override
    protected Node[] createNodes(Object key) {
        GeoNodeObject obj = (GeoNodeObject) key;
        return new Node[]{new GeoNode(obj)};
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addNotify() {
        GeoNodeObject[] objs;
        if (isPlace) {
            objs = geoPlacesList.getPlaces();
        } else if ((isEvent) && (parent != null)) {
            objs = parent.getEvents();
        } else {
            return;
        }
        if (objs != null) {
            setKeys(objs);
        }
    }
}
