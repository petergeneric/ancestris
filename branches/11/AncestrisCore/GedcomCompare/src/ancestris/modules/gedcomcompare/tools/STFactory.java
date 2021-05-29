package ancestris.modules.gedcomcompare.tools;

import ancestris.api.place.Place;
import ancestris.gedcom.privacy.standard.PrivacyPolicyImpl;
import ancestris.modules.console.Console;
import ancestris.modules.gedcomcompare.options.GedcomCompareOptionsPanel;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 *
 * @author frederic
 */
public class STFactory {

    private static String NOK = "*";
    private static int timeStart = 2119;
    private static int timeIncrement = 10;

    private static final Logger LOG = Logger.getLogger("ancestris.gedcomcompare");

    // Build map of STObjects from places map
    public static STMap buildSTMap(Map<Place, Set<Property>> map, Console console) {

        boolean updatedConsole = false;
        String lineStr = "--------------------------------------------------------------------------------------------------------";
        String sectionTitle = NbBundle.getMessage(STFactory.class, "Cons_UnfoundPlaceTitle"); 
        
        Map<String, List<STEvent>> tmpSTEvents = new HashMap<>();

        for (Place place : map.keySet()) {
            String spaceKey = getSpaceKey(place);
            if (spaceKey.contains(NOK)) {
                String msg = NbBundle.getMessage(STFactory.class, "Cons_UnfoundPlace", place.getPlaceToLocalFormat(), spaceKey); 
                LOG.info(msg);
                if (!updatedConsole) {
                    updatedConsole = true;
                    console.reset();
                    console.println(lineStr.substring(19), true);
                    console.println(sectionTitle);
                    console.println(lineStr + "\n");
                }
                console.println(msg);
                continue;
            }
            for (Property pEvent : map.get(place)) {
                Property date = pEvent.getProperty("DATE");
                if (date != null && !date.getValue().isEmpty()) {
                    String tk = getTimeKey((PropertyDate) date);
                    if (tk == null) {
                        continue;
                    }
                    String spaceTimeKey = spaceKey + "-" + tk;
                    List<STEvent> events = tmpSTEvents.get(spaceTimeKey);
                    if (events == null) {
                        events = new ArrayList<>();
                        tmpSTEvents.put(spaceTimeKey, events);
                    }
                    events.add(new STEvent(pEvent, place.getLatitude(), place.getLongitude()));
                }
            }
        }

        STMap stMap = new STMap();
        for (String spaceTimeKey : tmpSTEvents.keySet()) {
            STObject sto = new STObject(spaceTimeKey, tmpSTEvents.get(spaceTimeKey));
            stMap.put(spaceTimeKey, sto);
            stMap.incrementEventNb(sto.nbEvents);
            stMap.incrementLastCityNb(sto.nbCityName);
        }

        // get gedcom
        String gedName = "";
        Set<Property> set = map.get(map.keySet().iterator().next());
        if (!set.isEmpty()) {
            Property prop = set.iterator().next();
            if (prop != null) {
                gedName = prop.getGedcom().getDisplayName();
            }
        }

        stMap.setName(gedName);
        
        if (updatedConsole) {
            console.println("\n" + lineStr + "\n");
            if (NbPreferences.forModule(GedcomCompareOptionsPanel.class).getBoolean("ShowOutput", false)) {
                console.show();
            }
        }
        
        return stMap;
    }

    public static STMap intersectSTMaps(STMap stMap1, STMap stMap2) {

        STMap stIntersection = new STMap();

        stMap1.keySet().forEach((key) -> {
            STObject stObject2 = stMap2.get(key);
            if (stObject2 != null) {
                STObject stObject1 = stMap1.get(key);
                STObject stInterObject = intersectSTObjects(stObject1, stObject2);
                stIntersection.incrementLastCityNb(stInterObject.nbCityName);
                stIntersection.incrementEventNb(stInterObject.nbEvents);
                if (stInterObject.isEligible(1)) {
                    stIntersection.put(key, stInterObject);
                }
            }
        });

        stIntersection.setOverlap(stIntersection.keySet().size() * 200 / (stMap1.keySet().size() + stMap2.keySet().size()));
        if (stIntersection.getOverlap() == 0 && stIntersection.keySet().size() > 0) {
            stIntersection.setOverlap(1);
        }

        return stIntersection;
    }

    /*
    *   Intersection object is an articial list of all common bits between the two objects
    *   - common key
    *   - nb of event of each type in common (min of both)
    *   - nb of common cities and list of them
    *   - nb of common names and list of them
    *   - nb of common years and list of them
    *   - list of common events
    *
     */
    public static STObject intersectSTObjects(STObject stObject1, STObject stObject2) {

        STObject stIntersection = new STObject();

        stIntersection.spaceTime = stObject1.spaceTime;
        if (stObject2.lat != 0 && stObject2.lon != 0) {
            stIntersection.lat = (stObject1.lat + stObject2.lat) / 2;
            stIntersection.lon = (stObject1.lon + stObject2.lon) / 2;
        }
        stIntersection.nbB = Math.min(stObject1.nbB, stObject2.nbB);
        stIntersection.nbM = Math.min(stObject1.nbM, stObject2.nbM);
        stIntersection.nbS = Math.min(stObject1.nbS, stObject2.nbS);
        stIntersection.nbZ = Math.min(stObject1.nbZ, stObject2.nbZ);

        for (String city : stObject1.cities) {
            if (stObject2.cities.contains(city)) {
                stIntersection.cities.add(city);
            }
        }
        stIntersection.nbCities = stIntersection.cities.size();

        for (String name : stObject1.names) {
            if (stObject2.names.contains(name)) {
                stIntersection.names.add(name);
            }
        }
        stIntersection.nbNames = stIntersection.names.size();

        for (String year : stObject1.years) {
            if (stObject2.years.contains(year)) {
                stIntersection.years.add(year);
            }
        }
        stIntersection.nbYears = stIntersection.years.size();

        for (STEvent ev1 : stObject1.events) {
            for (STEvent ev2 : stObject2.events) {
                if (ev1.isSameName(ev2) && ev1.isSameCity(ev2)) {
                    stIntersection.eventsCityNames1.add(ev1);
                    stIntersection.eventsCityNames2.add(ev2);
                }
                if (ev1.isSame(ev2)) {
                    stIntersection.events.add(ev1);
                }
            }
        }
        stIntersection.nbCityName = stIntersection.eventsCityNames1.size();
        stIntersection.nbEvents = stIntersection.events.size();

        return stIntersection;
    }

    /**
     * TOOLS ===========================================================================
     */
    private static String getTimeKey(PropertyDate date) {
        PointInTime pitS = null;
        PointInTime pitE = null;
        try {
            pitS = date.getStart().getPointInTime(PointInTime.GREGORIAN);
            pitE = date.getEnd().getPointInTime(PointInTime.GREGORIAN);
        } catch (GedcomException ex) {
            LOG.finest("Date exception." + ex.getLocalizedMessage());
            return null;
        }
        if (pitS == null || pitE == null) {
            return null;
        }
        if (date.isRange()) {
            return String.format("%02d", (timeStart - (pitS.getYear() + pitE.getYear()) / 2) / timeIncrement);
        } else {
            return String.format("%02d", (timeStart - pitS.getYear()) / timeIncrement);
        }
    }

    private static String getSpaceKey(Place place) {
        String countryCode = place.getCountryCode();
        String state = place.getAdminCode(1);
        String dept = place.getAdminCode(2);
        if (countryCode.isEmpty()) {
            countryCode = NOK;
        }
        if (state.isEmpty()) {
            state = NOK;
        }
        if (dept.isEmpty()) {
            dept = NOK;
        }
        switch (countryCode) {
            case "FR":
                if (dept.equals("75")) {
                    dept = place.getAdminCode(5);
                }
                if (dept.equals("69")) {
                    dept = place.getAdminCode(4);
                }
                if (dept.equals("13")) {
                    dept = place.getAdminCode(3);
                }
                break;
            default:
        }
        String ret = countryCode + "." + state + "." + dept;

        return ret;
    }

    public static String getSTDisplayValue(String key) {
        String[] bits = key.split("-");
        String space = bits[0];
        int time = timeStart - timeIncrement + 1 - Integer.parseInt(bits[1]) * timeIncrement;
        String timeStr = "[" + String.valueOf(time) + "-" + String.valueOf(time + timeIncrement - 1) + "]";
        return space + " " + timeStr;
    }

    private static String getStringFrom(Set<String> set) {
        StringBuffer sb = new StringBuffer();
        for (String str : set) {
            sb.append(str).append(" ");
        }
        return sb.toString();
    }

    /**
     * Serializables ===========================================================================
     */
    public static STMapCapsule getSerializedSTMap(STMap stMap) {

        Map<String, Integer> map = new HashMap<>();

        for (String key : stMap.keySet()) {
            STObject sto = stMap.get(key);
            map.put(key, sto.nbEvents);
        }
        STMapCapsule capsule = new STMapCapsule(map);
        capsule.name = stMap.getName();

        return capsule;
    }

    public static STMap getUnserializedSTMap(STMapCapsule mapCapsule) {
        return mapCapsule != null ? new STMap(mapCapsule) : null;
    }

    public static STMapEventsCapsule getSerializedSTMapEvents(STMap stMap, Set<String> keys, boolean privacy) {

        STMapEventsCapsule capsule = new STMapEventsCapsule();

        capsule.name = stMap.getName();

        for (String key : keys) {
            STObject sto = stMap.get(key);
            capsule.map.put(key, convertObjectToCapsule(sto, privacy));
        }

        return capsule;
    }

    private static STObjectCapsule convertObjectToCapsule(STObject sto, boolean privacy) {

        STObjectCapsule oc = new STObjectCapsule();

        oc.spaceTime = sto.spaceTime;

        oc.nbB = sto.nbB;
        oc.nbM = sto.nbM;
        oc.nbS = sto.nbS;
        oc.nbZ = sto.nbZ;
        oc.nbCities = sto.nbCities;
        oc.nbNames = sto.nbNames;
        oc.nbYears = sto.nbYears;
        oc.nbCityName = sto.nbCityName;
        oc.nbEvents = sto.nbEvents;
        oc.lat = sto.lat;
        oc.lon = sto.lon;

        oc.cities.addAll(sto.cities);
        oc.names.addAll(sto.names);
        oc.years.addAll(sto.years);

        if (privacy) {
            PrivacyPolicyImpl ppi = new PrivacyPolicyImpl();
            ppi.clear();
            for (STEvent event : sto.events) {
                Property prop = (Property) event.getProperty();
                if (!ppi.isPrivate(prop)) {
                    oc.events.add(convertEventToCapsule(event));
                } else {
                    oc.nbEvents--;
                }
            }
        } else {
            for (STEvent event : sto.events) {
                oc.events.add(convertEventToCapsule(event));
            }
        }

        return oc;

    }

    private static STEventCapsule convertEventToCapsule(STEvent event) {

        STEventCapsule ec = new STEventCapsule();

        ec.type = event.type;
        ec.city = event.city;
        ec.lat = event.lat;
        ec.lon = event.lon;
        ec.lastnames = new String[event.lastnames.length];
        for (int i = 0; i < event.lastnames.length; i++) {
            ec.lastnames[i] = event.lastnames[i];
        }
        ec.year = event.year;

        ec.entity = event.entity;

        ec.propertyName = event.propertyName;
        ec.propertyDate = event.propertyDate;
        ec.propertyPlace = event.propertyPlace;
        ec.propertyString = event.propertyString;

        return ec;
    }

    public static void updateMap(STMap stMap, STMapEventsCapsule capsule) {

        if (stMap == null || capsule == null) {
            return;
        }

        for (String key : capsule.map.keySet()) {
            STObjectCapsule stoc = capsule.map.get(key);
            STObject sto = stMap.get(key);
            if (sto == null) {
                continue;
            }
            updateObjectWithCapsule(sto, stoc);
            stMap.incrementEventNb(sto.nbEvents);
            stMap.incrementLastCityNb(sto.nbCityName);
        }

    }

    private static void updateObjectWithCapsule(STObject sto, STObjectCapsule stoc) {

        if (sto == null) {
            sto = new STObject();
        }

        sto.spaceTime = stoc.spaceTime;

        sto.nbB = stoc.nbB;
        sto.nbM = stoc.nbM;
        sto.nbS = stoc.nbS;
        sto.nbZ = stoc.nbZ;
        sto.nbCities = stoc.nbCities;
        sto.nbNames = stoc.nbNames;
        sto.nbYears = stoc.nbYears;
        sto.nbCityName = stoc.nbCityName;
        sto.nbEvents = stoc.nbEvents;
        sto.lat = stoc.lat;
        sto.lon = stoc.lon;

        sto.cities.addAll(stoc.cities);
        sto.names.addAll(stoc.names);
        sto.years.addAll(stoc.years);

        if (sto.events == null) {
            sto.events = new ArrayList<>();
        } else {
            sto.events.clear();
        }
        for (STEventCapsule ec : stoc.events) {
            STEvent event = new STEvent();
            updateEventWithCapsule(event, ec);
            sto.events.add(event);
        }

    }

    private static void updateEventWithCapsule(STEvent event, STEventCapsule ec) {

        event.type = ec.type;
        event.city = ec.city;
        event.lat = ec.lat;
        event.lon = ec.lon;
        event.lastnames = new String[ec.lastnames.length];
        for (int i = 0; i < ec.lastnames.length; i++) {
            event.lastnames[i] = ec.lastnames[i];
        }
        event.year = ec.year;

        event.entity = ec.entity;

        event.propertyName = ec.propertyName;
        event.propertyDate = ec.propertyDate;
        event.propertyPlace = ec.propertyPlace;
        event.propertyString = ec.propertyString;

    }

    /**
     * Printers ===========================================================================
     */
    public static void printFullSTMap(STMap stMap) {

        if (stMap == null) {
            return;
        }
        
        for (String key : stMap.keySet()) {
            STObject sto = stMap.get(key);
            printSTObject(sto);
        }
    }

    public static void printMiniSTMap(STMap stMap) {
        if (stMap == null) {
            return;
        }
        
        for (String key : stMap.keySet()) {
            STObject sto = stMap.get(key);
            LOG.fine("  ST_OBJECT: " + sto.spaceTime + ";lat= " + sto.lat + ";lon= " + sto.lon);
            if (sto.lat > 44 && sto.lat < 45.6 && sto.lon < -2 && sto.lon > -4) {
                printSTObject(sto);
            }
        }
    }


    private static void printSTObject(STObject stObject) {
        LOG.fine("      ");
        LOG.fine("  NEW ST_OBJECT ********************************************");
        LOG.fine("      Key= " + stObject.spaceTime);
        LOG.fine("      nbB= " + stObject.nbB);
        LOG.fine("      nbM= " + stObject.nbB);
        LOG.fine("      nbS= " + stObject.nbB);
        LOG.fine("      nbZ= " + stObject.nbB);
        LOG.fine("      nbCities= " + stObject.nbCities);
        LOG.fine("      nbNames= " + stObject.nbNames);
        LOG.fine("      nbYears= " + stObject.nbYears);
        LOG.fine("      NbCityName= " + stObject.nbCityName);
        LOG.fine("      NbEvents= " + stObject.nbEvents);
        LOG.fine("      Cities = " + getStringFrom(stObject.cities));
        LOG.fine("      Names = " + getStringFrom(stObject.names));
        LOG.fine("      Years = " + getStringFrom(stObject.years));
        LOG.fine("      lat= " + stObject.lat);
        LOG.fine("      lon= " + stObject.lon);
        LOG.fine("      Events:");
        for (STEvent event : stObject.events) {
            LOG.fine("      - type= " + event.type);
            LOG.fine("      - city= " + event.city);
            LOG.fine("      - lat= " + event.lat);
            LOG.fine("      - lon= " + event.lon);
            LOG.fine("      - year= " + event.year);
            LOG.fine("      - property= " + event.propertyString);
            int i = 0;
            for (String last : event.lastnames) {
                LOG.fine("      - lastnames[" + i + "]=" + last);
                i++;
            }

        }
    }

    // CLASSES ===========================================================================
    public static class STObjectComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(((STObject) o1).nbEvents, ((STObject) o2).nbEvents);
        }
    }

}
