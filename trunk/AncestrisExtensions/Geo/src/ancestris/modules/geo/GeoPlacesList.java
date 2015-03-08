/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.modules.editors.genealogyeditor.panels.PlaceFormatEditorOptionsPanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
class GeoPlacesList implements GedcomListener {

    private String MAPTAG;
    private String LATITAG;
    private String LONGTAG;

    public static String TYPEOFCHANGE_GEDCOM = "gedcom";
    public static String TYPEOFCHANGE_COORDINATES = "coord";
    public static String TYPEOFCHANGE_NAME = "name";
    private static SortedMap<Gedcom, GeoPlacesList> instances;
    private final Gedcom gedcom;
    private GeoNodeObject[] geoNodes;
    private List<GeoPlacesListener> listeners = new ArrayList<GeoPlacesListener>(10);
    private boolean stopListening = false;
    private int[] placeSortOrder;
    private String placeDisplayFormat;
    private PropertyPlace copiedPlace = null;


    public GeoPlacesList(Gedcom gedcom) {
        this.gedcom = gedcom;
        this.geoNodes = null;
        initMapTags();
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
        Collection<Entity> entities = gedcom.getEntities();
        List<PropertyPlace> placesProps = new ArrayList<PropertyPlace>();
        for (Entity ent : entities) {
            getPropertiesRecursively(ent, placesProps, PropertyPlace.class);
        }

        // Check that display format of places is set
        initPlaceDisplayFormat(false);
        
        // Checks if format of saved locations is up to date, otherwise cleans the locations to force research again from the Internet
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
        Date versionDate = null;
        Date fromValidDate = null;
        try {
            versionDate = (Date)formatter.parse(NbPreferences.forModule(GeoPlacesList.class).get("##Version Date##", "01-01-1900"));
            fromValidDate = (Date)formatter.parse("01-03-2015");
            if (versionDate.before(fromValidDate) && JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), 
                    NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesQuestion"), 
                    NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesTitle"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                NbPreferences.forModule(GeoPlacesList.class).clear();
                NbPreferences.forModule(GeoPlacesList.class).put("##Version Date##", "01-03-2015");
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

    @SuppressWarnings("unchecked")
    public void getPropertiesRecursively(Property parent, List props, Class<? extends Property> clazz) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
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
        boolean ret = false;
        PlaceFormatEditorOptionsPanel pfeop = new PlaceFormatEditorOptionsPanel(gedcom);
        placeSortOrder = pfeop.getPlaceSortOrder();

        if (!pfeop.isRegisteredPlaceSortOrder() || forceEdit) {
            DialogManager.ADialog gedcomPlaceFormatEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(PlaceFormatEditorOptionsPanel.class, "PlaceFormatEditorOptionsPanel.title"),
                    pfeop);
            gedcomPlaceFormatEditorDialog.setDialogId(PlaceFormatEditorOptionsPanel.class.getName());
            if (gedcomPlaceFormatEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                placeSortOrder = pfeop.getPlaceSortOrder();
                pfeop.registerPlaceSortOrder();
                ret = true;
            }
        } 
        placeDisplayFormat = pfeop.getPlaceDisplayFormat();
        return ret;
    }

    public String getPlaceDisplayFormat(PropertyPlace place) {
        
        if (place == null || place.toString().trim().isEmpty() || place.getFirstAvailableJurisdiction().trim().isEmpty()) {
            return NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty");
        }
        
        String str = placeDisplayFormat;
        String[] jurisdictions = place.getJurisdictions();
        
        
        for (int i = 0; i < Math.max(jurisdictions.length, PropertyPlace.getFormat(gedcom).length); i++) {
            String juri = "";
            if (i < jurisdictions.length) {
                juri = (jurisdictions[i].trim().length() == 0) ? "" : jurisdictions[i];
            }
            str = str.replaceAll("\\b" + i + "{1}\\b", juri);    // replace only digit not surrounded by other digits (ex: zip code is not to be replaced)
        }
        str = str.replaceAll("\\(\\)", ""); // aestethic cleanning of empty jurisdictions in case there are between ()
        str = str.replaceAll("\\[\\]", ""); // aestethic cleanning of empty jurisdictions in case there are between []
        str = str.replaceAll("\\{\\}", ""); // aestethic cleanning of empty jurisdictions in case there are between {}
        return str;
    }
    
    public String getPlaceKey(PropertyPlace place) {
        return getPlaceDisplayFormat(place) + "[" + getMapString(place);
    }
    
    // Need to distinguish locations where geocoordinates are in gedcom from those where they are not
    public String getMapString(PropertyPlace place) {
        String ret = "";
        Property map = place.getProperty(MAPTAG);
        if (map != null) {
            Property prop = map.getProperty(LATITAG);
            if (prop != null) {
                ret += prop.getDisplayValue() + " ";
            }
            prop = map.getProperty(LONGTAG);
            if (prop != null) {
                ret += prop.getDisplayValue() + "]";
            }
        }
        return ret;
    }
    
    public void setMapCoord(PropertyPlace placeSource, List<PropertyPlace> propPlaces) {
        Property mapSource = placeSource.getProperty(MAPTAG);
        if (mapSource == null) {
            return;
        }
        Property latitudeSource = mapSource.getProperty(LATITAG);
        Property longitudeSource = mapSource.getProperty(LONGTAG);
        if (latitudeSource == null || longitudeSource == null) {
            return;
        }
        String latitudeSourceStr = latitudeSource.getDisplayValue();
        String longitudeSourceStr = longitudeSource.getDisplayValue();
        
        Property map;
        Property latitude;
        Property longitude;
        for (PropertyPlace pp : propPlaces) {
            pp.setValue(placeSource.getDisplayValue());
            map = pp.getProperty(MAPTAG);
            if (map != null) {
                latitude = map.getProperty(LATITAG);
                if (latitude == null) {
                    map.addProperty(LATITAG, latitudeSourceStr);
                } else {
                    latitude.setValue(latitudeSourceStr);
                }
                longitude = map.getProperty(LONGTAG);
                if (longitude == null) {
                    map.addProperty(LONGTAG, longitudeSourceStr);
                } else {
                    longitude.setValue(longitudeSourceStr);
                }
            } else {
                map = pp.addProperty(MAPTAG, "");
                map.addProperty(LATITAG, latitudeSourceStr);
                map.addProperty(LONGTAG, longitudeSourceStr);
            }
        }
    }
    
    private void initMapTags() {
        if (getGedcom().getGrammar().getVersion().equals("5.5.1")) {
            MAPTAG = "MAP";
            LATITAG = "LATI";
            LONGTAG = "LONG";
        } else {
            MAPTAG = "_MAP";
            LATITAG = "_LATI";
            LONGTAG = "_LONG";
        }
    }

    void setCopiedPlace(PropertyPlace place) {
        this.copiedPlace = place;
    }

    PropertyPlace getCopiedPlace() {
        return this.copiedPlace;
    }
    
}
