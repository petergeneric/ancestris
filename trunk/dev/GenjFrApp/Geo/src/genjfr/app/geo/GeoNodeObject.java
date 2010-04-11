/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import genjfr.app.App;
import java.awt.Image;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.geonames.InsufficientStyleException;
import org.geonames.Style;
import org.geonames.Timezone;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
class GeoNodeObject {

    private PropertyPlace place;
    private List<GeoNodeObject> events = new ArrayList<GeoNodeObject>();
    private Property property;
    public boolean isUnknown;
    public boolean isEvent;
    public boolean toBeDisplayed;
    private Toponym toponym = defaultToponym();
    public boolean isInError = false;

    public GeoNodeObject(PropertyPlace place, boolean localOnly) {
        this.place = place;
        this.isEvent = false;
        this.isUnknown = false;
        this.toBeDisplayed = false;
        this.toponym = getToponymFromPlace(place, localOnly);
        events.add(new GeoNodeObject(place.getParent(), place));
    }

    public GeoNodeObject(Property event, PropertyPlace pp) {
        this.isEvent = true;
        events = null;
        property = event;
        place = pp;
    }

    @SuppressWarnings("deprecation")
    public Image getIcon() {
        if (isEvent) {
            return property.getImage(true).getImage();
        }
        return Utilities.loadImage("genjfr/app/geo/geo.png");
    }

    @Override
    public String toString() {
        String str = "";
        if (isEvent) {
            str = property.getPropertyName() + " - " + property.getEntity().toString();
        } else {
            str = (place == null || place.toString().isEmpty()) ? NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty") : place.toString();
        }
        return str;
    }

    public Gedcom getGedcom() {
        return place != null ? place.getGedcom() : property != null ? property.getGedcom() : null;
    }

    public PropertyPlace getPlace() {
        return place;
    }

    public Property getProperty() {
        return property;
    }

    @SuppressWarnings("unchecked")
    public GeoNodeObject[] getEvents() {
        if (events != null) {
            Collections.sort(events, sortEvents);
            return events.toArray(new GeoNodeObject[events.size()]);
        }
        return null;
    }

    public String getEventsAsText() {
        String str = "";
        GeoNodeObject[] gno = getEvents();
        for (int i = 0; i < gno.length; i++) {
            GeoNodeObject event = gno[i];
            str += "- " + event.toString() + "\n";
        }
        return str;
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

    public List<Indi> getIndis() {
        List<Indi> indis = new ArrayList<Indi>();
        // loop on events
        GeoNodeObject[] gno = getEvents();
        for (int i = 0; i < gno.length; i++) {
            Property prop = gno[i].property;
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
        String[] str = {"nb individus", "patronyme le plus fréquent", "nb events", "date min", "date max"};
        HashSet<String> indiv = new HashSet<String>();
        SortedMap<String, Integer> pat = new TreeMap<String, Integer>();
        int dateMin = +99999;
        int dateMax = -99999;
        GeoNodeObject[] gno = getEvents();
        if (gno == null) {
            return null;
        }
        for (int i = 0; i < gno.length; i++) {
            Property prop = gno[i].property;
            Entity ent = prop.getEntity();
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
        str[3] = "" + (dateMin == +99999 ? "-" : dateMin);
        str[4] = "" + (dateMax == -99999 ? "-" : dateMax);
        return str;
    }

    void addEvent(Property parent, PropertyPlace pp) {
        events.add(new GeoNodeObject(parent, pp));
    }

    public Toponym getToponym() {
        return toponym;
    }

    public void setToponym(Toponym topo) {
        this.toponym = topo;
    }

    public String getPlaceAsString() {
        return place != null ? place.toString() : "";
    }

    public String getCity() {
        return (place == null || place.toString().isEmpty()) ? NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty") : place.getCity().toString();
    }

    public String getCoordinates(Toponym topo) {
        Double lat = topo != null ? topo.getLatitude() : defaultToponym().getLatitude();
        Double lon = topo != null ? topo.getLongitude() : defaultToponym().getLongitude();
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

    public String getCoordinates() {
        return getCoordinates(toponym);
    }

    public void setCoordinates(Toponym topo) {
        toponym.setLatitude(topo.getLatitude());
        toponym.setLongitude(topo.getLongitude());
        return;
    }

    public String getNbOfEvents() {
        return "" + events.size();
    }

    public GeoPosition getGeoPosition() {
        return toponym != null ? new GeoPosition(toponym.getLatitude(), toponym.getLongitude()) : new GeoPosition(defaultToponym().getLatitude(), defaultToponym().getLongitude());
    }

    public Double getLongitude() {
        return toponym != null ? toponym.getLongitude() : defaultToponym().getLongitude();
    }

    public Double getLatitude() {
        return toponym != null ? toponym.getLatitude() : defaultToponym().getLatitude();
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

    public Toponym getToponymFromPlace(PropertyPlace place, boolean localOnly) {

        Toponym topo = null;

        // go back if place is null
        if (place == null || place.toString().trim().isEmpty()) {
            return defaultToponym();
        }

        // search locally first
        topo = Code2Toponym(NbPreferences.forModule(GeoPlacesList.class).get(place.getValueStartingWithCity(), null));
        boolean foundLocally = topo != null;

        // search on the internet
        if (topo == null && !localOnly) {
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setMaxRows(1);
            searchCriteria.setLanguage(NbPreferences.forModule(App.class).get("language", "").equals("2") ? "fr" : "en");
            searchCriteria.setStyle(Style.FULL);
            ToponymSearchResult searchResult;
            //
            try {
                searchCriteria.setQ(place.toString());
                searchResult = WebService.search(searchCriteria);
                for (Toponym iTopo : searchResult.getToponyms()) {
                    topo = iTopo; // take the first one
                    break;
                }
            } catch (Exception e) {
                isInError = true;
                return null;
            }
        }

        // remember for next time
        if (!foundLocally && topo != null && !topo.getName().isEmpty()) {
            NbPreferences.forModule(GeoPlacesList.class).put(place.getValueStartingWithCity(), Toponym2Code(topo));
        }

        // return first found
        return topo;
    }

    public List<Toponym> getToponymsFromPlace(String place, int max) {
        if (place == null) {
            return null;
        }
        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setMaxRows(max);
        searchCriteria.setLanguage(NbPreferences.forModule(App.class).get("language", "").equals("2") ? "fr" : "en");
        searchCriteria.setStyle(Style.FULL);
        ToponymSearchResult searchResult;
        List<Toponym> topo = new ArrayList<Toponym>();
        //
        try {
            searchCriteria.setQ(place);
            searchResult = WebService.search(searchCriteria);
            for (Toponym iTopo : searchResult.getToponyms()) {
                topo.add(iTopo);
            }
        } catch (Exception ex) {
            return null;
        }
        return topo;
    }

    private Toponym defaultToponym() {
        Toponym topo = new Toponym();
        topo.setLatitude(45);
        topo.setLongitude(-4);
        topo.setPopulation(Long.getLong("0"));
        return topo;

    }

    public HashSet<String> getEventTypes() {
        HashSet<String> tags = new HashSet<String>();
        GeoNodeObject[] gno = getEvents();
        for (int i = 0; i < gno.length; i++) {
            Property prop = gno[i].property;
            tags.add(prop.getTag());
        }
        return tags;
    }

    public int getEventsMinDate() {
        int date = +99999;
        GeoNodeObject[] gno = getEvents();
        for (int i = 0; i < gno.length; i++) {
            Property prop = gno[i].property;
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
        for (int i = 0; i < gno.length; i++) {
            Property prop = gno[i].property;
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
        String sep = "\n";
        StringBuilder str = new StringBuilder();
        try {
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Name") + spa + topo.getName() + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Coord") + spa + getCoordinates(topo) + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Time") + spa + topo.getTimezone().getTimezoneId() + " (" + topo.getTimezone().getGmtOffset() + ")" + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Cntry") + spa + dispName(topo.getCountryName()) + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Region") + spa + dispName(topo.getAdminName1()) + " (" + dispName(topo.getAdminCode1()) + ")" + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Dept") + spa + dispName(topo.getAdminName2()) + " (" + dispName(topo.getAdminCode2()) + ")" + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Distri") + spa + dispName(topo.getAdminName3()) + " (" + dispName(topo.getAdminCode3()) + ")" + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_CdInsee") + spa + dispName(topo.getAdminCode4()) + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Post") + spa + dispName(topo.getPostcode()) + sep);
            str.append(NbBundle.getMessage(GeoNodeObject.class, "TXT_Pop") + spa + getPopulation(topo));
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return str.toString();
    }

    public String Toponym2Code(Toponym topo) {
        if (topo == null) {
            return "";
        }
        String sep = ";";
        StringBuilder str = new StringBuilder();
        try {
            str.append(topo.getName() + sep);
            str.append(topo.getLatitude() + sep);
            str.append(topo.getLongitude() + sep);
            if (topo.getTimezone() != null) {
                str.append(topo.getTimezone().getTimezoneId() + sep);
                str.append(topo.getTimezone().getGmtOffset() + sep);
            } else {
                str.append("-" + sep);
                str.append("-" + sep);
            }
            str.append(dispName(topo.getCountryName()) + sep);
            str.append(dispName(topo.getAdminName1()) + sep);
            str.append(dispName(topo.getAdminCode1()) + sep);
            str.append(dispName(topo.getAdminName2()) + sep);
            str.append(dispName(topo.getAdminCode2()) + sep);
            str.append(dispName(topo.getAdminName3()) + sep);
            str.append(dispName(topo.getAdminCode3()) + sep);
            str.append(dispName(topo.getAdminCode4()) + sep);
            str.append(dispName(topo.getPostcode()) + sep);
            str.append(topo.getPopulation());
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return str.toString();
    }

    /**
     * Celle;52.6166667;10.0833333;Europe/Berlin;1.0;Allemagne;Basse-Saxe;06;;;-;-;-;-;71010
     * @param code
     * @return
     */
    public Toponym Code2Toponym(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        String sep = ";";
        Toponym topo = new Toponym();
        try {
            StringTokenizer tokens = new StringTokenizer(code, sep);
            if (tokens.hasMoreTokens()) {
                topo.setName(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setLatitude(Double.parseDouble(tokens.nextToken()));
            }
            if (tokens.hasMoreTokens()) {
                topo.setLongitude(Double.parseDouble(tokens.nextToken()));
            }
            if (tokens.hasMoreTokens()) {
                Timezone tz = new Timezone();
                tz.setTimezoneId(tokens.nextToken());
                if (tokens.hasMoreTokens()) {
                    tz.setGmtOffset(Double.parseDouble(tokens.nextToken()));
                }
                topo.setTimezone(tz);
            }
            if (tokens.hasMoreTokens()) {
                topo.setCountryName(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminName1(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminCode1(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminName2(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminCode2(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminName3(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminCode3(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setAdminCode4(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setPostcode(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                topo.setPopulation(Long.parseLong(tokens.nextToken()));
            }
        } catch (Throwable t) {
        }
        return topo;
    }

    private String dispName(String str) {
        return str == null || str.isEmpty() ? "-" : str;
    }
    /**
     * Comparator to sort events
     */
    public Comparator sortEvents = new Comparator() {

        public int compare(Object o1, Object o2) {
            GeoNodeObject obj1 = (GeoNodeObject) o1;
            GeoNodeObject obj2 = (GeoNodeObject) o2;
            if (obj1 == null) {
                return +1;
            }
            if (obj2 == null) {
                return -1;
            }
            return obj1.toString().compareTo(obj2.toString());
        }
    };
}
