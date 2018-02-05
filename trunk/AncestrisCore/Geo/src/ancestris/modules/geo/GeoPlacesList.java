/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyName;
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
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
class GeoPlacesList implements GedcomMetaListener {

    private static final String FORCE_REFRESH_DATE = "03-05-2015";
    
    public static String TYPEOFCHANGE_GEDCOM = "gedcom";
    public static String TYPEOFCHANGE_COORDINATES = "coord";
    public static String TYPEOFCHANGE_NAME = "name";
    private static SortedMap<Gedcom, GeoPlacesList> instances;

    private final Gedcom gedcom;
    private GeoNodeObject[] geoNodes;
    private final List<GeoPlacesListener> listeners = new ArrayList<GeoPlacesListener>(10);
    private boolean stopListening = false;
    private boolean updateRequired = false;
    private PropertyPlace copiedPlace = null;
    private GeoPosition copiedPosition = null;


    public GeoPlacesList(Gedcom gedcom) {
        this.gedcom = gedcom;
        this.geoNodes = null;
        AncestrisPlugin.register(new GeoPlugin());
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

    public static void remove(Gedcom gedcom) {
        if (gedcom == null || instances == null) {
            return;
        }
        GeoPlacesList gpl = instances.get(gedcom);
        if (gpl == null) {
            return;
        }
        gedcom.removeGedcomListener(gpl);
        instances.remove(gedcom);
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
    public synchronized void launchPlacesSearch(boolean force) {
        List<PropertyPlace> placesProps = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);

        if (force) {
            // Checks if format of saved locations is up to date, otherwise cleans the locations to force research again from the Internet
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date versionDate;
            Date fromValidDate;
            try {
                versionDate = (Date) formatter.parse(NbPreferences.forModule(GeoPlacesList.class).get("##Version Date##", "01-01-1900"));
                fromValidDate = (Date) formatter.parse(FORCE_REFRESH_DATE);
                if (versionDate.before(fromValidDate)) {
                    if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesQuestion"),
                            NbBundle.getMessage(GeoPlacesList.class, "TXT_eraseLocalPlacesTitle"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        NbPreferences.forModule(GeoPlacesList.class).clear();
                        NbPreferences.forModule(GeoPlacesList.class).put("##Version Date##", FORCE_REFRESH_DATE);
                    } else {
                        return;
                    }
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // search the geo objects locally and else on internet
        new GeoInternetSearch(this, placesProps).executeSearch(gedcom, force);
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
        if (updateRequired) {
            return;
        }
        checkReloadPlaces(entity);
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (updateRequired) {
            return;
        }
        checkReloadPlaces(entity);
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (updateRequired) {
            return;
        }
        checkReloadPlaces(property);
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        if (updateRequired) {
            return;
        }
        checkReloadPlaces(property);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        if (updateRequired) {
            return;
        }
        checkReloadPlaces(property);
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
        reloadPlaces();
    }
    
    private void checkReloadPlaces(Property property) {
        List<PropertyPlace> tmpList = property.getProperties(PropertyPlace.class);
        if (property instanceof PropertyPlace || !tmpList.isEmpty()) {  // updating place list is required if we are modifying a place
            updateRequired = true;
        } else if (property instanceof PropertyName) { // updating place list is required if we are modifying a name of an entity containing a place
            if (!property.getEntity().getProperties(PropertyPlace.class).isEmpty()) {
                updateRequired = true;
            }
        }
    }
    
    public void reloadPlaces() {
        if (!stopListening && updateRequired) {
            stopListening();
            launchPlacesSearch(false);
            updateRequired = false;
        }
    }

    public void stopListening() {
        stopListening = true;
    }

    public void startListening() {
        stopListening = false;
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

    public boolean setPlaceDisplayFormat(PropertyPlace place) {
        boolean changed = false;

        String displayFormat = getGedcom().getPlaceDisplayFormat();
        PlaceDisplayFormatPanel fdfPanel = new PlaceDisplayFormatPanel(place);
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_SetPlaceDisplayFormat"), fdfPanel).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).show();
        if (o == DialogManager.OK_OPTION) {
            String newPlaceDisplayFormat = fdfPanel.getDisplayFormat();
            if (!newPlaceDisplayFormat.equals(displayFormat)) {
                gedcom.setPlaceDisplayFormat(newPlaceDisplayFormat);
                changed = true;
            }
        }
        
        return changed;
    }
    
    public String getPlaceKey(PropertyPlace place) {
        return getPlaceDisplayFormat(place) + "[" + getMapString(place) + "]";
    }
    
    public String getPlaceDisplayFormat(PropertyPlace place) {
        String str = "";
        if (place == null || (str = place.format(null)).isEmpty()) {
            return NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty");
        }
        //TODO: Should we  move this code to PropertyPlace.format?
        str = str.replaceAll("\\(\\)", ""); // aestethic cleanning of empty jurisdictions in case they are between ()
        str = str.replaceAll("\\[\\]", ""); // aestethic cleanning of empty jurisdictions in case they are between []
        str = str.replaceAll("\\{\\}", ""); // aestethic cleanning of empty jurisdictions in case they are between {}
        str = str.replaceAll("<html>", ""); // remove start and end tags
        str = str.replaceAll("</html>", ""); // remove start and end tags
        return str;
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
            ret += prop.getDisplayValue();
        }
        return ret;
    }
    
    public void setMapCoord(PropertyPlace placeSource, List<PropertyPlace> propPlaces) {
        PropertyLatitude latitudeSource = placeSource.getLatitude(false);
        PropertyLongitude longitudeSource = placeSource.getLongitude(false);
        String latitudeSourceStr = latitudeSource == null ? "" : latitudeSource.getValue();
        String longitudeSourceStr = longitudeSource == null ? "" : longitudeSource.getValue();
        
        for (PropertyPlace pp : propPlaces) {
            pp.setValue(placeSource.getValue());
            pp.setCoordinates(latitudeSourceStr ,longitudeSourceStr);
        }
    }
    
    public void setCopiedPlace(PropertyPlace place, GeoPosition geoPoint) {
        this.copiedPlace = place;
        this.copiedPosition = geoPoint;
    }

    public PropertyPlace getCopiedPlace() {
        return this.copiedPlace;
    }

    public GeoPosition getCopiedPosition() {
        return this.copiedPosition;
    }

}
