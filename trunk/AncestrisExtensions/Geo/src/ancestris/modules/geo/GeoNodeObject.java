/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.libs.geonames.GeonamesOptions;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.geonames.InsufficientStyleException;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

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
    
    // Unknown places will be pointed to the sea
    private final static int DEFAULT_LAT = 45; // in the middle of the sea
    private final static int DEFAULT_LON = -4; // in the middle of the sea

    // Location elements used from and to gedcom
    private PropertyPlace place;
    private Double latitude = null;
    private Double longitude = null;
    
    // Technical location elements
    private int geo_type = GEO_UNKNOWN;
    private String EMPTY_PLACE = NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty");
    private Toponym toponym = defaultToponym();     // Local or internet match
    public  boolean isInError = false;              // In case error while searching
    private String placeDisplayFormat = "";         // Store display format
    private String placeKey = "";                   // Store place key (ex: for sorting)
    
    // For events, parent and list of events
    private Property property;
    private List<GeoNodeObject> events = new ArrayList<GeoNodeObject>();

    // Technical listener
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());
    
    public GeoNodeObject(GeoPlacesList gplOwner, PropertyPlace place, boolean avoidInternetSearch) {
        this.gplOwner = gplOwner;
        this.place = place;
        this.placeDisplayFormat = gplOwner.getPlaceDisplayFormat(place);
        this.placeKey = gplOwner.getPlaceKey(place);
        this.property = place.getParent();
        this.isEvent = false;
        this.toponym = getToponymFromPlace(place, avoidInternetSearch);
        setGedcomCoordinates();
        events.add(new GeoNodeObject(gplOwner, place.getParent(), place));
    }

    public GeoNodeObject(GeoPlacesList gplOwner, Property event, PropertyPlace pp) {
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

    private void fire(String propertyName, Object old, Object nue) {
        //Passing 0 below on purpose, so you only synchronize for one atomic call:
        PropertyChangeListener[] pcls = listeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
        }
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
    public Toponym getToponymFromPlace(PropertyPlace place, boolean avoidInternetSearch) {

        Toponym topo = defaultToponym();
        boolean foundLocally = false;
        String searchedPlace = place.getValueStartingWithCity();

        // Return default if place is null or empty (= nothing to search)
        if (avoidInternetSearch && placeDisplayFormat.equals(EMPTY_PLACE)) {
            return topo;
        }
        
        // Search locally first (trimming spaces)
        if (avoidInternetSearch) {
            topo = Code2Toponym(NbPreferences.forModule(GeoPlacesList.class).get(searchedPlace.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, "").replaceAll(" +", ""), null));
            foundLocally = (topo != null);
        }

        // Search on the internet for first instance, if not found locally
        if (!foundLocally) {
            topo = null;
            WebService.setUserName(GeonamesOptions.getInstance().getUserName());
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setNameStartsWith(place.getCity());
            searchCriteria.setMaxRows(1);
            searchCriteria.setLanguage(Locale.getDefault().toString());
            searchCriteria.setStyle(Style.FULL);
            ToponymSearchResult searchResult;
            //
            try {
                // try search with all elements of place name to be more precise, separating the words
                searchCriteria.setQ(searchedPlace.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, " ").replaceAll(" +", " "));
                searchResult = WebService.search(searchCriteria);
                for (Toponym iTopo : searchResult.getToponyms()) {
                    topo = iTopo; // take the first one
                    break;
                }
                if (topo == null) { // try with numbers only (i.e. Martinique is not in France according to 'geonames' so country fails the search)
                    searchCriteria.setQ(place.getNumericalJurisdictions().replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, " "));
                    searchResult = WebService.search(searchCriteria);
                    for (Toponym iTopo : searchResult.getToponyms()) {
                        topo = iTopo; // take the first one
                        break;
                    }
                }
                if (topo == null) { // try without "q" so only with namestartswith
                    searchCriteria.setQ(null);
                    searchResult = WebService.search(searchCriteria);
                    for (Toponym iTopo : searchResult.getToponyms()) {
                        topo = iTopo; // take the first one
                        break;
                    }
                }
                if (topo == null) { // if still not found, default topo
                    topo = defaultToponym();
                }
            } catch (Exception e) {
                isInError = true;
                return null;
            }
        }

        // Remember for next time if found on the Internet and not locally
        if (!foundLocally && topo != null) {
            NbPreferences.forModule(GeoPlacesList.class).put(searchedPlace.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, "").replaceAll(" +", ""), Toponym2Code(topo));
        }

        return topo;
    }

    /**
     * Defines a default toponym pointing in the middle of the sea
     *
     * @return
     */
    private Toponym defaultToponym() {
        Toponym topo = new Toponym();
        topo.setLatitude(DEFAULT_LAT);
        topo.setLongitude(DEFAULT_LON);
        topo.setPopulation(Long.getLong("0"));
        return topo;
    }

    /**
     * Determines if toponym points to the default geocoordinates, regardless of
     * its name and other elements
     *
     * @param toponym
     * @return
     */
    private boolean calcUnknown(Toponym toponym) {
        if (toponym == null) {
            return false;
        }
        Toponym topo = defaultToponym();
        return (toponym.getLatitude() == topo.getLatitude() && toponym.getLongitude() == topo.getLongitude());
    }

    /**
     * Converts geocoordinates strings to toponym (elements other than
     * geocoordinates are not used in that case)
     *
     * @param code
     * @return
     */
    public Toponym Code2Toponym(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        Toponym topo = new Toponym();
        try {
            StringTokenizer tokens = new StringTokenizer(code, ";");
            if (tokens.hasMoreTokens()) {
                topo.setLatitude(Double.parseDouble(tokens.nextToken()));
            }
            if (tokens.hasMoreTokens()) {
                topo.setLongitude(Double.parseDouble(tokens.nextToken()));
            }
            if (tokens.hasMoreTokens()) {
                topo.setPopulation(Long.parseLong(tokens.nextToken()));
            }
        } catch (Throwable t) {
        }
        return topo;
    }

    /**
     * Converts toponym coordinates into coordinates string
     *
     * @param topo
     * @return
     */
    public String Toponym2Code(Toponym topo) {
        if (topo == null) {
            return "";
        }
        try {
            return topo.getLatitude() + ";" + topo.getLongitude() + ";" + topo.getPopulation();
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
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
                this.toponym = defaultToponym();
            }
            if (calcUnknown(this.toponym)) {
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
        String str;
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

    public String getPopulation(Toponym topo) {
        Long pop = Long.getLong("0");
        try {
            pop = topo != null ? topo.getPopulation() : defaultToponym().getPopulation();
        } catch (InsufficientStyleException ex) {
        }
        DecimalFormat format = new DecimalFormat("#,##0");
        return pop != null ? format.format(pop) : "0";
    }

    @SuppressWarnings("unchecked")
    public GeoNodeObject[] getEvents() {
        if (events != null) {
            Collections.sort(events, sortEvents);
            return events.toArray(new GeoNodeObject[events.size()]);
        }
        return null;
    }

    public List<PropertyPlace> getEventsPlaces() {
        if (events != null) {
            List<PropertyPlace> propPlaces = new ArrayList<PropertyPlace>();
            for (GeoNodeObject geoNodeObject : events) {
                propPlaces.add(geoNodeObject.getPlace());
            }
            return propPlaces;
        }
        return null;
    }

    public void updateAllEventsPlaces(PropertyPlace place) {
        if (place != null) {
            gplOwner.setMapCoord(place, getEventsPlaces());
            gplOwner.launchPlacesSearch(); // we always need to refresh the list, even for on event, as it could then match an existing one
        }
    }

    public List<Indi> getIndis() {
        List<Indi> indis = new ArrayList<Indi>();
        // loop on events
        GeoNodeObject[] gno = getEvents();
        for (GeoNodeObject gno1 : gno) {
            Property prop = gno1.property;
            Entity ent = prop.getEntity();
            // counts patronyms, for individuals or families
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
        }
        return indis;
    }

    public String[] getEventsInfo() {

        String[] str = {"nb individus", "patronyme le plus fréquent", "nb events", "births", "marriages", "deaths", "other events", "date min", "date max"};
        HashSet<String> indiv = new HashSet<String>();
        SortedMap<String, Integer> pat = new TreeMap<String, Integer>();
        int dateMin = +99999;
        int dateMax = -99999;
        GeoNodeObject[] gno = getEvents();
        if (gno == null) {
            return null;
        }
        int eBirths = 0, eMarriages = 0, eDeaths = 0, eOther = 0;

        // loop on all events
        for (GeoNodeObject gno1 : gno) {
            Property prop = gno1.property;
            Entity ent = prop.getEntity();
            // increments events
            String tag = prop.getTag();
            if (tag.equals("BIRT") || tag.equals("CHR")) {
                eBirths++;
            } else if (tag.equals("MARR") || tag.equals("ENGA") || tag.equals("MARB") || tag.equals("MARC")) {
                eMarriages++;
            } else if (tag.equals("DEAT") || tag.equals("BURI") || tag.equals("CREM")) {
                eDeaths++;
            } else {
                eOther++;
            }
            // counts patronyms, for individuals or families
            String patronym = "";
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
                if (indi != null) {
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
                if (indi != null) {
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
        str[2] = "" + events.size();
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

    public HashSet<String> getEventTypes() {
        HashSet<String> tags = new HashSet<String>();
        GeoNodeObject[] gno = getEvents();
        for (GeoNodeObject gno1 : gno) {
            Property prop = gno1.property;
            tags.add(prop.getTag());
        }
        return tags;
    }

    public int getEventsMinDate() {
        int date = +99999;
        GeoNodeObject[] gno = getEvents();
        for (GeoNodeObject gno1 : gno) {
            Property prop = gno1.property;
            Property dateProp = prop.getProperty("DATE");
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
        }
        return date;
    }

    public int getEventsMaxDate() {
        int date = -99999;
        GeoNodeObject[] gno = getEvents();
        for (GeoNodeObject gno1 : gno) {
            Property prop = gno1.property;
            Property dateProp = prop.getProperty("DATE");
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
        }
        return date;
    }

    public String displayToponym(Toponym topo) {
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
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Pop")).append(spa).append(getPopulation(topo));
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
    /**
     * Comparator to sort events
     */
    public Comparator<GeoNodeObject> sortEvents = new Comparator<GeoNodeObject>() {

        public int compare(GeoNodeObject o1, GeoNodeObject o2) {
            if (o1 == null) {
                return +1;
            }
            if (o2 == null) {
                return -1;
            }
            return o1.toString().compareTo(o2.toString());
        }
    };

}
