/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
import ancestris.modules.place.geonames.GeonamesResearcher;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.geonames.InsufficientStyleException;
import org.geonames.Toponym;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GeoNodeObject {

    // To refer to originator and common elements of all nodes
    private final GeoPlacesList gplOwner;  

    // 2 types of geoNodeObjects : places or events
    public boolean isEvent;
    
    // For places, coordinates are either taken from gedcom and therefore considered confirmed,
    // or from search, therefore considered as a proposition to be validated,
    // or unfound and therefore to be searched and confirmed by user
    private final static int GEO_CONFIRMED = 2; 
    private final static int GEO_PROPOSED = 1; 
    private final static int GEO_UNKNOWN = 0; 
    private final static String COLOR_CONFIRMED = "color='!textText'"; // default
    private final static String COLOR_PROPOSED = "color='#0066ff'"; // blue
    private final static String COLOR_UNKNOWN = "color='#ff2300'"; // red
    private final static String EMPTY_PLACE = NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty");
    

    // variables for places
    private GeonamesResearcher geonamesResearcher = null;
    public Place defaultPlace = null;
    private int geo_type = GEO_UNKNOWN;
    private final PropertyPlace place;
    private Double latitude = null;
    private Double longitude = null;
    private Place toponym = null;                   // Local or internet match
    public  boolean isInError = false;              // In case error while searching
    private String placeDisplayFormat = "";         // Store display format
    private String placeKey = "";                   // Store place key (ex: for sorting)
    
    // Variables for events, parent and list of events
    private final Property property;
    private List<GeoNodeObject> events = new ArrayList<>();

    // Technical listener
    private final List<PropertyChangeListener> listeners;
    
    public GeoNodeObject(GeonamesResearcher geonamesResearcher, GeoPlacesList gplOwner, PropertyPlace place, boolean avoidInternetSearch) {
        // Initialize
        this.geonamesResearcher = geonamesResearcher;
        this.defaultPlace = geonamesResearcher.defaultPlace();
        listeners = Collections.synchronizedList(new LinkedList<>());
        this.gplOwner = gplOwner;
        this.place = place;
        this.placeDisplayFormat = gplOwner.getPlaceDisplayFormat(place);
        this.placeKey = gplOwner.getPlaceKey(place);
        this.property = place.getParent();
        this.isEvent = false;
        
        // Run search of place
        this.toponym = (placeDisplayFormat.equals(EMPTY_PLACE) ? defaultPlace : getToponymFromPlace(place, avoidInternetSearch));  // search place;
        
        // Set coordinates
        setGedcomCoordinates();
        
        // Add events
        events.add(new GeoNodeObject(gplOwner, place.getParent(), place));
    }

    public GeoNodeObject(GeoPlacesList gplOwner, Property event, PropertyPlace pp) {
        listeners = Collections.synchronizedList(new LinkedList<>());
        this.isEvent = true;
        events = null;
        property = event;
        place = pp;
        this.gplOwner = gplOwner;
    }
    
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.remove(pcl);
    }

    
    /**
     * Search Toponym for given place, using name only, first locally if data
     * exists, else on the internet if flag permits
     *
     * Local toponym only has coordinates Local format :
     * "place.getDisplayValue().replaceAll(",", "").replaceAll(" +", "")=[lat with 5 decimal places];[lon with 5 decimal places]
     *
     * @param place : property place to be searched
     * @param avoidInternetSearch : true  : look on the Internet only if not found locally
     *                              false : always look on the Internet, regardless of whether it is found locally or not
     * @return
     */
    public final Place getToponymFromPlace(PropertyPlace place, boolean avoidInternetSearch) {

        Place retPlace = null;
        boolean foundLocally = false;

        String placePieces = place.getPlaceToLocalFormat();
        if (placePieces.isEmpty()) {
            return defaultPlace;
        }
        
        String city = place.getCity();
        if (city.isEmpty()) {
            return defaultPlace;
        }

        // Search locally first (trimming spaces)
        if (avoidInternetSearch) {
            retPlace = PlaceFactory.findPlace(placePieces); 
            foundLocally = retPlace != null;
        }
        
        // Search on the internet for first instance, if not found locally
        if (!foundLocally) {
            retPlace = geonamesResearcher.searchMassPlace(placePieces, city, defaultPlace);
            isInError = retPlace == null;
        }

        // Remember for next time if found on the Internet and not locally
        if (!foundLocally && retPlace != null) {
            PlaceFactory.rememberPlace(placePieces, retPlace);
        }

        return retPlace;
    }


    /**
     * Set geocoordinates to those stored in gedcom if they are found, regardless of toponym found or not, 
     * default to toponym coordinates otherwise.
     *
     * @param topo
     */
    private void setGedcomCoordinates() {

        this.latitude = null;
        this.longitude = null;
        
        // Set to coordinates found in Gedcom if they exist
        PropertyLatitude lat = this.place.getLatitude(true);
        if (lat != null) latitude = lat.getDoubleValue();
        PropertyLongitude lon = this.place.getLongitude(true);
        if (lon != null) longitude = lon.getDoubleValue();
        
        // Set to toponym coordinates otherwise, and default if null
        if (this.latitude == null || this.longitude == null) {
            
            if (this.toponym == null) {
                this.toponym = geonamesResearcher.defaultPlace();
            }
            if (toponym.getLatitude().compareTo(defaultPlace.getLatitude()) == 0 && toponym.getLongitude().compareTo(defaultPlace.getLongitude()) == 0) {
                geo_type = GEO_UNKNOWN;
            } else {
                geo_type = GEO_PROPOSED;
            }
            this.latitude = this.toponym.getLatitude();
            this.longitude = this.toponym.getLongitude();
        } else {
            geo_type = GEO_CONFIRMED;
        }

    }
    
    public boolean isUnknown() {
        return geo_type == GEO_UNKNOWN;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public GeoPosition getGeoPosition() {
        return new GeoPosition(this.latitude, this.longitude);
    }

    /**
     * Returns double geocoordinates as formatted string coordinates
     * 
     * @return 
     */
    public String getTextCoordinates() {
        Double lat = getLatitude();
        Double lon = getLongitude();
        char we = 'E', ns = 'N';
        if (lat < 0) {
            lat = -lat;
            ns = 'S';
        }
        if (lon < 0) {
            lon = -lon;
            we = 'W';
        }
        DecimalFormat format = new DecimalFormat("0.0");
        return ns + format.format(lat) + " " + we + format.format(lon);
    }

    @SuppressWarnings("deprecation")
    public Image getIcon() {
        if (isEvent) {
            return property.getImage(true).getImage();
        }
        if (geo_type == GEO_UNKNOWN) {
            return ImageUtilities.loadImage("ancestris/modules/geo/geo_red.png");
        }
        return ImageUtilities.loadImage("ancestris/modules/geo/geo.png");
    }

    public String getColor() {
        String color = COLOR_UNKNOWN;
        switch (geo_type) {
            case GEO_CONFIRMED:
                color = COLOR_CONFIRMED;
                break;
            case GEO_PROPOSED:
                color = COLOR_PROPOSED;
                break;
            case GEO_UNKNOWN:
                color = COLOR_UNKNOWN;
                break;
        }
        return color;            
    }
    
    public String getNbOfEvents() {
        return "" + events.size();
    }

    /**
     * Used to sort.
     * @return 
     */
    @Override
    public String toString() {
        if (isEvent) {
            return toDisplayString();
        } else {
            return placeKey;
        }
    }

    /**
     * Used to display.
     * @return 
     */
    public String toDisplayString() {
        if (isEvent) {
            return property.getPropertyName() + " - " + property.getEntity().toString();
        } else {
            return placeDisplayFormat;
        }
    }

    public Gedcom getGedcom() {
        return place != null ? place.getGedcom() : property != null ? property.getGedcom() : null;
    }

    public Property getProperty() {
        return property;
    }

    public PropertyPlace getPlace() {
        return place;
    }

    public String getCity() {
        return (place == null || place.toString().trim().isEmpty()) ? NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty") : place.getCity();
    }

    public String getPopulation() {
        return getPopulation(toponym);
    }

    public String getPopulation(Place topo) {
        if (topo == null) {
            return "0";
        }
        final Long pop = topo.getPopulation();
        DecimalFormat format = new DecimalFormat("#,##0");
        return pop != null ? format.format(pop) : "0";
    }

    @SuppressWarnings("unchecked")
    public GeoNodeObject[] getAllEvents() {
        if (events != null) {
            Collections.sort(events, GeoListTopComponent.sortEvents);
            return events.toArray(new GeoNodeObject[events.size()]);
        }
        return null;
    }

    public GeoNodeObject[] getFilteredEvents(GeoFilter filter) {
        List<GeoNodeObject> list = new ArrayList<>();
        if (events != null) {
            events.stream().filter((event) -> (filter.compliesEvent(event))).forEachOrdered((event) -> {
                list.add(event);
            });
            Collections.sort(list, GeoListTopComponent.sortEvents);
            return list.toArray(new GeoNodeObject[list.size()]);
        }
        return null;
    }

    public List<PropertyPlace> getEventsPlaces() {
        if (events != null) {
            List<PropertyPlace> propPlaces = new ArrayList<>();
            events.forEach((geoNodeObject) -> {
                propPlaces.add(geoNodeObject.getPlace());
            });
            return propPlaces;
        }
        return null;
    }

    public void updateAllEventsPlaces(PropertyPlace place) {
        if (place != null) {
            gplOwner.setMapCoord(place, getEventsPlaces());
            gplOwner.launchPlacesSearch(false); // we always need to refresh the list, even for on event, as it could then match an existing one
        }
    }

    public List<Indi> getIndis() {
        List<Indi> indis = new ArrayList<>();

        Entity ent = property.getEntity();
        if (ent instanceof Indi) {
            indis.add((Indi) ent);
        } else if (ent instanceof Fam) {
            Indi indi = ((Fam) ent).getHusband();
            if (indi != null) {
                indis.add(indi);
            }
            indi = ((Fam) ent).getWife();
            if (indi != null) {
                indis.add(indi);
            }
        }
        return indis;
    }


    public int getEventsMaxDate() {
        int date = -99999;
        
        Property dateProp = property.getProperty("DATE");
        if (dateProp != null && dateProp instanceof PropertyDate) {
            int year = 0;
            PropertyDate p = (PropertyDate) dateProp;
            try {
                if (p.isRange()) {
                    year = p.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear();
                } else {
                    year = p.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                }
            } catch (GedcomException ex) {
            }
            if (date < year) {
                date = year;
            }
        }
        return date;
    }

    public int getEventsMinDate() {
        int date = +99999;

        Property dateProp = property.getProperty("DATE");
        if (dateProp != null && dateProp instanceof PropertyDate) {
            int year = 0;
            PropertyDate p = (PropertyDate) dateProp;
            try {
                year = p.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
            } catch (GedcomException ex) {
            }
            if (date > year) {
                date = year;
            }
        }
        return date;
    }

    public String getEventTag() {
        return property.getTag();
    }

    
    
    public String[] getEventsInfo(GeoFilter filter) {

        if (filter == null) {
            return null;
        }
        GeoNodeObject[] eventsOfNode = getFilteredEvents(filter); 
        if (eventsOfNode == null) {
            return null;
        }
        
        String[] str = {"nb individus", "patronyme le plus fréquent", "nb events", "births", "marriages", "deaths", "other events", "date min", "date max"};
        HashSet<String> indiv = new HashSet<>();
        SortedMap<String, Integer> pat = new TreeMap<>();
        int dateMin = +99999;
        int dateMax = -99999;
        int eBirths = 0, eMarriages = 0, eDeaths = 0, eOther = 0;

        // loop on all events
        for (GeoNodeObject eventOfNode : eventsOfNode) {
            Property prop = eventOfNode.property;
            Entity ent = prop.getEntity();
            // increments events
            String tag = prop.getTag();
            if (filter.isBirth(tag)) {
                eBirths++;
            } else if (filter.isMarriage(tag)) {
                eMarriages++;
            } else if (filter.isDeath(tag)) {
                eDeaths++;
            } else {
                eOther++;
            }
            // counts patronyms, for individuals or families
            String patronym;
            if (ent instanceof Indi) {
                patronym = ((Indi) ent).getLastName();
                Integer nb = pat.get(patronym);
                if (nb == null) {
                    nb = 0;
                }
                nb++;
                pat.put(patronym, nb);
                indiv.add(ent.toString());
            } else if (ent instanceof Fam) {
                Indi indi = ((Fam) ent).getHusband();
                if (indi != null && filter.compliesIndi(indi)) {
                    patronym = indi.getLastName();
                    Integer nb = pat.get(patronym);
                    if (nb == null) {
                        nb = 0;
                    }
                    nb++;
                    pat.put(patronym, nb);
                    indiv.add(indi.toString());
                }
                indi = ((Fam) ent).getWife();
                if (indi != null && filter.compliesIndi(indi)) {
                    patronym = indi.getLastName();
                    Integer nb = pat.get(patronym);
                    if (nb == null) {
                        nb = 0;
                    }
                    nb++;
                    pat.put(patronym, nb);
                    indiv.add(indi.toString());
                }
            }
            // Gets min and max dates
            Property dateProp = prop.getProperty("DATE");
            if (dateProp != null && dateProp instanceof PropertyDate) {
                int year1 = 0, year2 = 0;
                PropertyDate p = (PropertyDate) dateProp;
                try {
                    if (p.isRange()) {
                        year1 = p.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                        year2 = p.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear();
                    } else {
                        year1 = p.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                        year2 = year1;
                    }
                } catch (GedcomException ex) {
                }
                if (dateMin > year1) {
                    dateMin = year1;
                }
                if (dateMax < year2) {
                    dateMax = year2;
                }
            }
        }

        // Calculates frequency
        Iterator<String> it = pat.keySet().iterator();
        Integer max = 0;
        String patMax = "";
        while (it.hasNext()) {
            String key = it.next();
            Integer nb = pat.get(key);
            if (nb > max) {
                max = nb;
                patMax = key;
            }
        }

        // Build info
        str[0] = "" + indiv.size();
        str[1] = "" + patMax + " (" + max + ")";
        str[2] = "" + eventsOfNode.length;
        str[3] = "" + eBirths;
        str[4] = "" + eMarriages;
        str[5] = "" + eDeaths;
        str[6] = "" + eOther;
        str[7] = "" + (dateMin == +99999 ? "-" : dateMin);
        str[8] = "" + (dateMax == -99999 ? "-" : dateMax);
        return str;
    }

    void addEvent(Property parent, PropertyPlace pp) {
        events.add(new GeoNodeObject(gplOwner, parent, pp));
    }

    public String displayToponym(Place place) {
        Toponym topo = place.getToponym();
        if (topo == null) {
            return "";
        }
        String spa = " ";
        String sep = "   \n";
        String name = topo.getName() == null ? NbBundle.getMessage(GeoInternetSearch.class, "TXT_UNKNOWN") : topo.getName();
        StringBuilder str = new StringBuilder();
        try {
            String timezone = dispName("");
            if (topo.getTimezone() != null) {
                timezone = topo.getTimezone().getTimezoneId() + " (" + topo.getTimezone().getGmtOffset() + ")";
            }
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Name")).append(spa).append(name).append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Coord")).append(spa).append(getTextCoordinates()).append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Time")).append(spa).append(timezone).append(sep);
            str.append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_CdInsee")).append(spa).append(dispName(topo.getAdminCode4())).append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Distri")).append(spa).append(dispName(topo.getAdminName3())).append(" (").append(dispName(topo.getAdminCode3())).append(")").append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Dept")).append(spa).append(dispName(topo.getAdminName2())).append(" (").append(dispName(topo.getAdminCode2())).append(")").append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Region")).append(spa).append(dispName(topo.getAdminName1())).append(" (").append(dispName(topo.getAdminCode1())).append(")").append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Cntry")).append(spa).append(dispName(topo.getCountryName())).append(sep);
            str.append(sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Pop")).append(spa).append(place.getPopulation());
            str.append(sep);
            str.append(" ");
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return str.toString();
    }

    private String dispName(String str) {
        return str == null || str.isEmpty() ? "-" : str;
    }



}
