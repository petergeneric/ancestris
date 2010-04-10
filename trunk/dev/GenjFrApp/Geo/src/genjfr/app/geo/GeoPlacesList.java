/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author frederic
 */
class GeoPlacesList implements GedcomListener {

    private static SortedMap<Gedcom, GeoPlacesList> instances;
    private final Gedcom gedcom;
    private GeoNodeObject[] geoNodes;
    private List listeners = new ArrayList(10);

    public GeoPlacesList(Gedcom gedcom) {
        this.gedcom = gedcom;
        this.geoNodes = getPlaces();
    }

    public static synchronized GeoPlacesList getInstance(Gedcom gedcom) {
        if (instances == null) {
            instances = new TreeMap<Gedcom, GeoPlacesList>();
        }
        GeoPlacesList gpl = instances.get(gedcom);
        if (gpl == null) {
            gpl = new GeoPlacesList(gedcom);
            instances.put(gedcom, gpl);
            gedcom.addGedcomListener(gpl);
        }
        return gpl;
    }

    public Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * Return list of cities of Gedcom file
     */
    public GeoNodeObject[] getPlaces() {
        return getPlaces(false);
    }

    @SuppressWarnings("unchecked")
    public GeoNodeObject[] getPlaces(boolean force) {
        // Return already calculated nodes
        if (!force && geoNodes != null) {
            return geoNodes;
        }

        // Else recalculate : find all property places
        Collection entities = gedcom.getEntities();
        List<PropertyPlace> placesProps = new ArrayList<PropertyPlace>();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            getPropertiesRecursively((Property) ent, placesProps, PropertyPlace.class);
        }

        // search the geo objects locally and else on internet
        new GeoInternetSearch(this, placesProps).executeSearch();
        return geoNodes;
    }

    public void setPlaces(GeoNodeObject[] result) {
        geoNodes = result;
    }

    @SuppressWarnings("unchecked")
    public void getPropertiesRecursively(Property parent, List props, Class clazz) {
        Property[] children = parent.getProperties();
        for (int c = 0; c < children.length; c++) {
            Property child = children[c];
            props.addAll(child.getProperties(clazz));
            getPropertiesRecursively(child, props, clazz);
        }
    }

    /**
     * Adds a Listener which will be notified when data changes
     */
    @SuppressWarnings("unchecked")
    public void addGeoPlacesListener(GeoPlacesListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        synchronized (listeners) {
            if (!listeners.add(listener)) {
                throw new IllegalArgumentException("can't add gedcom listener " + listener + "twice");
            }
        }
    }

    /**
     * Removes a Listener from receiving notifications
     */
    public void removeGeoPlacesListener(GeoPlacesListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        geoNodes = getPlaces(true);
        notifyListeners();
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        geoNodes = getPlaces(true);
        notifyListeners();
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        geoNodes = getPlaces(true);
        notifyListeners();
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        geoNodes = getPlaces(true);
        notifyListeners();
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        geoNodes = getPlaces(true);
        notifyListeners();
    }

    @SuppressWarnings("unchecked")
    public void notifyListeners() {
        GeoPlacesListener[] gpls = (GeoPlacesListener[]) listeners.toArray(new GeoPlacesListener[listeners.size()]);
        for (int l = 0; l < gpls.length; l++) {
            try {
                gpls[l].geoPlacesChanged(this);
            } catch (Throwable t) {
                System.out.println("exception in geoplaceslist listener " + gpls[l] + t);
            }
        }

    }

}
