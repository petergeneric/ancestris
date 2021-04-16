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
package ancestris.modules.gedcomcompare.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author frederic
 */
public class STMap {

    private static final int SIZE = 5;

    private String name = "";

    private Map<String, STObject> stObjectMap;

    private int overlap = 0;
    private int lastCityNb = 0;
    private int eventNb = 0;

    private boolean isRemote = false;
    private boolean isComplete = true;
    
    public STMap() {
        stObjectMap = new HashMap<>();
    }

    public STMap(STMapCapsule mapCapsule) {
        isRemote = true;
        isComplete = false;
        name = mapCapsule.name;
        stObjectMap = new HashMap<>();
        for (String key : mapCapsule.map.keySet()) {
            List<STEvent> events = new ArrayList<>();
            STObject sto = new STObject(key, events);
            sto.setNbEvents(mapCapsule.map.get(key));
            stObjectMap.put(key, sto);
        }
        
    }

    public boolean isRemote() {
        return isRemote;
    }

    public boolean isComplete() {
        return isComplete;
    }

    void setComplete(boolean b) {
        isComplete = b;
    }

    public void put(String key, STObject stObject) {
        this.stObjectMap.put(key, stObject);
    }

    public STObject get(String key) {
        return stObjectMap.get(key);
    }

    public Set<String> keySet() {
        return stObjectMap.keySet();
    }

    public void setName(String set) {
        name = set;
    }

    public String getName() {
        return name;
    }

    public void setOverlap(int set) {
        overlap = set;
    }

    public int getOverlap() {
        return overlap;
    }

    public void incrementLastCityNb(int increment) {
        lastCityNb += increment;
    }

    public int getLastCityNb() {
        return lastCityNb;
    }

    public void incrementEventNb(int increment) {
        eventNb += increment;
    }

    public int getEventNb() {
        return eventNb;
    }

    public Set<STPoint> getPoints(int type) {
        Set<STPoint> ret = new HashSet<>();
        for (STObject o : stObjectMap.values()) {
            if (o.lat == 0 && o.lon == 0) {
                continue;
            }
            STPoint point = new STPoint(type, o.lat, o.lon, o.getTime());
            ret.add(point);
        }

        return ret;
    }

    public Object[][] getData() {

        List<Object[]> lines = new ArrayList<>();
        Object[] line = null;
        int l1;

        for (String key : stObjectMap.keySet()) {

            STObject o = stObjectMap.get(key);
            l1 = o.eventsCityNames1.size();
            if (l1 == 0) {
                continue;
            }

            // add ST block header
            lines.add(getBlankLine());
            lines.add(getLineFromKey(key));

            for (int l = 0; l < l1; l++) {
                // add ST city
                line = new Object[SIZE];
                line[0] = "2";
                line[1] = o.eventsCityNames1.get(l).city;
                line[2] = o.eventsCityNames2.get(l).city;
                line[3] = "";
                line[4] = "";
                lines.add(line);

                // add ST names
                line = new Object[SIZE];
                line[0] = "3";
                line[1] = getNamesFromArray(o.eventsCityNames1.get(l).lastnames);
                line[2] = getNamesFromArray(o.eventsCityNames2.get(l).lastnames);
                line[3] = "";
                line[4] = "";
                lines.add(line);

                // add ST indi
                line = new Object[SIZE];
                line[0] = "4";
                line[1] = o.eventsCityNames1.get(l).entity;
                line[2] = o.eventsCityNames2.get(l).entity;
                line[3] = "";
                line[4] = "";
                lines.add(line);

                // add ST events
                line = new Object[SIZE];
                line[0] = "5";
                if (o.eventsCityNames1.get(l).isSame(o.eventsCityNames2.get(l))) {
                    line[0] = "6";  // match
                }
                line[1] = o.eventsCityNames1.get(l).getDisplayEvent();
                line[2] = o.eventsCityNames2.get(l).getDisplayEvent();
                line[3] = o.eventsCityNames1.get(l).getProperty();
                line[4] = o.eventsCityNames2.get(l).getProperty();
                lines.add(line);

            }
        }

        return lines.toArray(new Object[lines.size()][5]);
    }

    private Object[] getBlankLine() {
        Object[] line = new Object[SIZE];
        line[0] = "0";
        line[1] = "";
        line[2] = "";
        line[3] = "";
        line[4] = "";
        return line;
    }

    private Object[] getLineFromKey(String key) {
        Object[] line = new Object[SIZE];
        line[0] = "1"; // line type
        line[1] = STFactory.getSTDisplayValue(key);
        line[2] = "";
        line[3] = "";
        line[4] = "";
        return line;
    }

    private String getNamesFromArray(String[] names) {
        String ret = "";
        for (String str : names) {
            ret += str + " ";
        }
        return ret;
    }

    public String[] getTopSpaceKeys(int length) {

        // Rebuild map with space keys only
        Map<String, Integer> map = new HashMap<>();
        for (String key : stObjectMap.keySet()) {
            STObject sto = stObjectMap.get(key);
            String space = key.split("-")[0];
            Integer value = map.get(space);
            if (value == null) {
                value = 0;
            }
            value += sto.nbEvents;
            map.put(space, value);
        }
        
        // Sort by decreasing order of nb of events and keep only top length
        Map<String, Integer> topMap = map.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(length)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        // Enrich the keys with the occurrences
        List<String> list = new ArrayList<>();
        topMap.keySet().forEach((key) -> {
            list.add(key+"_("+ topMap.get(key) + ")");
        });

        return list.toArray(new String[list.size()]);
    }


    public List<String> getAreaCityNames() {
        
        List<String> ret = new ArrayList<>();
        int l1;
        for (String key : stObjectMap.keySet()) {

            STObject o = stObjectMap.get(key);
            l1 = o.eventsCityNames1.size();
            if (l1 == 0) {
                continue;
            }
            
            for (int l = 0; l < l1; l++) {
                ret.add(STFactory.getSTDisplayValue(key) + " - " + o.eventsCityNames1.get(l).city + ": " + getNamesFromArray(o.eventsCityNames1.get(l).lastnames));
            }
            
        }

        return ret;
    }

}
