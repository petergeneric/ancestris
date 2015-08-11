/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.modules.editors.geoplace.MapPlaceEditor;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyPlace;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
class GeoPlacesList implements GedcomListener {

    private static final String FORCE_REFRESH_DATE = "03-05-2015";
    
    public static String TYPEOFCHANGE_GEDCOM = "gedcom";
    public static String TYPEOFCHANGE_COORDINATES = "coord";
    public static String TYPEOFCHANGE_NAME = "name";
    private static SortedMap<Gedcom, GeoPlacesList> instances;
    private final Gedcom gedcom;
    private GeoNodeObject[] geoNodes;
    private final List<GeoPlacesListener> listeners = new ArrayList<GeoPlacesListener>(10);
    private boolean stopListening = false;
    private PropertyPlace copiedPlace = null;


    public GeoPlacesList(Gedcom gedcom) {
        this.gedcom = gedcom;
        this.geoNodes = null;
    }

    public static synchronized GeoPlacesList getInstance(Gedcom gedcom) {
        if (instances == null) {
            instances = new TreeMap<Gedcom, GeoPlacesList>();
        }
        if (gedcom == null) {
            return null;
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

    public GeoNodeObject[] getPlaces() {
        return geoNodes;
    }

    /**
     * Launch places search over the net for the list of cities of Gedcom file
     */
    @SuppressWarnings("unchecked")
    public synchronized void launchPlacesSearch() {
        List<PropertyPlace> placesProps = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);

        // Check that display format of places is set
        initPlaceDisplayFormat(false);
        
        // Checks if format of saved locations is up to date, otherwise cleans the locations to force research again from the Internet
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
        Date versionDate;
        Date fromValidDate;
        try {
            versionDate = (Date)formatter.parse(NbPreferences.forModule(GeoPlacesList.class).get("##Version Date##", "01-01-1900"));
            fromValidDate = (Date)formatter.parse(FORCE_REFRESH_DATE);
            if (versionDate.before(fromValidDate) && JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), 
                    NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesQuestion"), 
                    NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesTitle"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                NbPreferences.forModule(GeoPlacesList.class).clear();
                NbPreferences.forModule(GeoPlacesList.class).put("##Version Date##", FORCE_REFRESH_DATE);
            }
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        // search the geo objects locally and else on internet
        new GeoInternetSearch(this, placesProps).executeSearch(gedcom);
    }

    public void setPlaces(GeoNodeObject[] result) {
        geoNodes = result;
        notifyListeners(TYPEOFCHANGE_GEDCOM);
        startListening();
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
        reloadPlaces();
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        reloadPlaces();
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        reloadPlaces();
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        reloadPlaces();
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        reloadPlaces();
    }

    @SuppressWarnings("unchecked")
    public void notifyListeners(String change) {
        GeoPlacesListener[] gpls = listeners.toArray(new GeoPlacesListener[listeners.size()]);
        for (GeoPlacesListener gpl : gpls) {
            try {
                gpl.geoPlacesChanged(this, change);
            } catch (Throwable t) {
                System.out.println("exception in geoplaceslist listener " + gpl + t);
            }
        }
    }

    public void refreshPlaceCoord() {
        notifyListeners(TYPEOFCHANGE_COORDINATES);
    }

    public void refreshPlaceName() {
        notifyListeners(TYPEOFCHANGE_NAME);
    }

    public void reloadPlaces() {
        if (!stopListening) {
            stopListening();
            launchPlacesSearch();
        }
    }

    public void stopListening() {
        stopListening = true;
    }

    public void startListening() {
        stopListening = false;
    }
    
    /**
     * Initiate place display format.
     * 
     * 1/ Use User preferences first for this gedcom (TODO)
     * 2/ Default to "city,hamlet,geo_code,county/dept,state/region,country" otherwise
     *    For that, I need PlaceFormatEditorOptionsPanel to collect placesortorder preferences for this gedcom
     * 
     */
    public boolean initPlaceDisplayFormat(boolean forceEdit) {
        // TODO: use Lookup to find place editor
        return MapPlaceEditor.updatePlaceFormat(gedcom,forceEdit);
    }

    // TODO: add a setting for Geo module to modify place format
    public String getPlaceDisplayFormat(PropertyPlace place) {
        String str = "";
        if (place == null || (str = place.format(null)).isEmpty()) {
            return NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty");
        }
        
        //TODO: Should we  move this code to PropertyPlace.format?
        str = str.replaceAll("\\(\\)", ""); // aestethic cleanning of empty jurisdictions in case they are between ()
        str = str.replaceAll("\\[\\]", ""); // aestethic cleanning of empty jurisdictions in case they are between []
        str = str.replaceAll("\\{\\}", ""); // aestethic cleanning of empty jurisdictions in case they are between {}
        return str;
    }
    
    public String getPlaceKey(PropertyPlace place) {
        return getPlaceDisplayFormat(place) + "[" + getMapString(place);
    }
    
    // Need to distinguish locations where geocoordinates are in gedcom from those where they are not
    public String getMapString(PropertyPlace place) {
        String ret = "";
        Property prop = place.getLatitude(true);
        if (prop != null) {
            ret += prop.getDisplayValue() + " ";
        }
        prop = place.getLongitude(true);
        if (prop != null) {
            ret += prop.getDisplayValue() + "]";
        }
        return ret;
    }
    
    public void setMapCoord(PropertyPlace placeSource, List<PropertyPlace> propPlaces) {
        PropertyLatitude latitudeSource = placeSource.getLatitude(false);
        PropertyLongitude longitudeSource = placeSource.getLongitude(false);
        if (latitudeSource == null || longitudeSource == null) {
            return;
        }
        String latitudeSourceStr = latitudeSource.getValue();
        String longitudeSourceStr = longitudeSource.getValue();
        
        for (PropertyPlace pp : propPlaces) {
            pp.setValue(placeSource.getValue());
            pp.setCoordinates(latitudeSourceStr ,longitudeSourceStr);
        }
    }
    
    void setCopiedPlace(PropertyPlace place) {
        this.copiedPlace = place;
    }

    PropertyPlace getCopiedPlace() {
        return this.copiedPlace;
    }
    
}
