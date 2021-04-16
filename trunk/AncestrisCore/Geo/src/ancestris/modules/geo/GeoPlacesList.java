/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author frederic
 */
public class GeoPlacesList implements GedcomMetaListener {

    private static final String NO_DATE = "01-01-1900";
    private static final String FORCE_REFRESH_DATE = "03-05-2015";  // date of last format change

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

    public GeoNodeObject[] getNodes() {
        return geoNodes;
    }

    public Map<Place, Set<Property>> getPlaces(boolean withInfo, Callable okWhenDone, Callable cancelWhenDone) {

        boolean pleaseSearch = false;
        if (geoNodes == null) {
            pleaseSearch = true;
        } else if (withInfo) {
            int count = 0;
            for (GeoNodeObject node : geoNodes) {
                if (!node.isEvent) {
                    if (node.areCoordinatesUnknown() || node.isMissingLocalInformation()) {
                        count++;
                    }
                }
            }
            if (count > geoNodes.length / 2) {
                pleaseSearch = true;
            }
        }

        if (pleaseSearch) {
            Callable okCallback = okWhenDone;
            if (okCallback == null) {
                okCallback = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        DialogManager.create(NbBundle.getMessage(GeoInternetSearch.class, "TITL_SearchCompleted"),
                                NbBundle.getMessage(GeoInternetSearch.class, "TEXT_SearchCompleted")).setOptionType(DialogManager.OK_ONLY_OPTION).show();
                        return null;
                    }
                };
            }
            launchPlacesSearch(GeoNodeObject.GEO_SEARCH_LOCAL_ONLY, true, true, okCallback, cancelWhenDone);
            return null;
        } else {
            Map<Place, Set<Property>> map = new HashMap<>();
            Place place = null;
            for (GeoNodeObject node : geoNodes) {
                if (!node.isEvent && !node.areCoordinatesUnknown()) {
                    place = node.getPlace();
                    if (place == null) {
                        continue; // should not occur
                    }
                    Set<Property> events = map.get(place);
                    if (events == null) {
                        events = new HashSet<>();
                        map.put(place, events);
                    }
                    events.addAll(node.getEventsProperties());
                }
            }
            return map;
        }
    }

    /**
     * Launch places search locally or over the net for the list of cities of Gedcom file internetSearchType : force search over the net, otherwise only in the local file
     *
     * @param internetSearchType
     * @param checkCoordinates
     * @param checkLocalMissing
     * @param runWhenDone
     */
    public synchronized void launchPlacesSearch(final int internetSearchType, boolean checkCoordinates, boolean checkLocalMissing, Callable okWhenDone, Callable cancelWhenDone) {

        // Get gedcom cities and check it is not empty
        // If empty, popup explaining that the Geo module only works if places are provided
        final List<PropertyPlace> placesProps = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);
        if (placesProps.isEmpty()) {
            DialogManager.create(NbBundle.getMessage(GeoInternetSearch.class, "ANOMALY_Title"), NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesNone"))
                    .setDialogId("geo.refresh.noplace").setOptionType(DialogManager.OK_ONLY_OPTION).show();
            return;
        }

        // Checks if format of saved locations is up to date, otherwise cleans the locations to force research again from the Internet
        // If 1/1/1900, this is a first time use or with no saved locations file, then skip warning
        // If another date, erase local file
        String paramDate = NbPreferences.forModule(PlaceFactory.class).get("##Version Date##", NO_DATE);
        if (!paramDate.equals(NO_DATE)) {
            try {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                Date versionDate = (Date) formatter.parse(paramDate);
                Date fromValidDate = (Date) formatter.parse(FORCE_REFRESH_DATE);
                if (versionDate.before(fromValidDate)) {
                    NbPreferences.forModule(PlaceFactory.class).clear();
                    NbPreferences.forModule(PlaceFactory.class).put("##Version Date##", FORCE_REFRESH_DATE);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Let ancestris do the search (separate thread) through the geo objects locally and else on internet if web seaarch is on 
        final GeoPlacesList gpl = this;
        GeoInternetSearch gis = new GeoInternetSearch(this, placesProps) {
            @Override
            public void callback() {
                // We are back from the search (local or internet):
                // In case search was local only, the purpose here is to check if a search on the internet is however necessary.
                // Recommend an internet search when : 
                //    - If geocoordinates are not found or not enough populated (less than 50%) (node coordinates are unknown)
                //    - If not found in local file 
                if (internetSearchType == GeoNodeObject.GEO_SEARCH_LOCAL_ONLY && (checkCoordinates || checkLocalMissing)) {
                    boolean reforce = false;
                    String msg = "";

                    // determine proper message depending on the situation
                    if (getNodes() == null) {
                        msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesAborted");
                    } else if (getNodes().length > 0) {
                        int countCoordinatesUnknown = 0;
                        int countLocalPlaceMissing = 0;
                        for (GeoNodeObject node : getNodes()) {
                            if (node.areCoordinatesUnknown()) {
                                countCoordinatesUnknown++;
                            }
                            if (node.isMissingLocalInformation()) {
                                countLocalPlaceMissing++;
                            }
                        }
                        if (checkCoordinates) {
                            if (countCoordinatesUnknown == getNodes().length) {
                                msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesNoCoord");
                            } else if (countCoordinatesUnknown * 100 / getNodes().length > 50) { // more than 50% of places with unknown coordinates
                                msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesMissingCoord"); 
                            }
                        }
                        if (checkLocalMissing) {
                            if (countLocalPlaceMissing == getNodes().length) {
                                msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesNoLocal", gedcom.getDisplayName());
                            } else if (countLocalPlaceMissing * 100 / getNodes().length > 50) { // more than 50% of places are not locally documented
                                msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlacesMissingLocal", gedcom.getDisplayName()); 
                            }
                        }
                    }

                    // Display message
                    if (!msg.isEmpty()) {
                        Object o = DialogManager.create(NbBundle.getMessage(GeoInternetSearch.class, "ANOMALY_Title"), msg).setDialogId("geo.refresh.coord").setOptionType(DialogManager.YES_NO_OPTION).show();
                        if (o.equals(DialogManager.OK_OPTION)) {
                            reforce = true;
                        } else {
                            if (cancelWhenDone != null) {
                                try {
                                    cancelWhenDone.call();
                                    return;
                                } catch (Exception ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }

                    // Re-Run search on the internet 
                    if (reforce) {
                        GeoInternetSearch gis2 = new GeoInternetSearch(gpl, placesProps) {
                            @Override
                           public void callback() {
                                if (okWhenDone != null) {
                                    try {
                                        okWhenDone.call();
                                    } catch (Exception ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        };
                        gis2.executeSearch(gedcom, GeoNodeObject.GEO_SEARCH_WEB_ONLY);
                    } else {
                        if (okWhenDone != null) {
                            try {
                                okWhenDone.call();
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        };
        gis.executeSearch(gedcom, internetSearchType);

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
            launchPlacesSearch(GeoNodeObject.GEO_SEARCH_LOCAL_THEN_WEB, false, false, null, null);
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
            gedcom.setPlaceDisplayFormat(newPlaceDisplayFormat);
            changed = true;
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
        if (str.equals(",[]")) {
            return "";
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
            pp.setCoordinates(latitudeSourceStr, longitudeSourceStr);
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
