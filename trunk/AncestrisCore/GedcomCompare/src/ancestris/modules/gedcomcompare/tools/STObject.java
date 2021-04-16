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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frederic
 */
public class STObject {

    public String spaceTime = "";

    public int nbB = 0;
    public int nbM = 0;
    public int nbS = 0;
    public int nbZ = 0;

    public Set<String> cities = new HashSet<>();
    public Set<String> names = new HashSet<>();
    public Set<String> years = new HashSet<>();

    public int nbCities = 0;
    public int nbNames = 0;
    public int nbYears = 0;

    public List<STEvent> eventsCityNames1 = new ArrayList<>(); // only used for intersection
    public List<STEvent> eventsCityNames2 = new ArrayList<>(); // only used for intersection
    public int nbCityName = 0;

    public List<STEvent> events = new ArrayList<>();
    public int nbEvents = 0;

    public Double lat = 0d;
    public Double lon = 0d;

    

    public STObject() {
    }

    public STObject(String spaceTime, List<STEvent> events) {
        this.spaceTime = spaceTime;
        
        for (STEvent event : events) {
            switch (event.type) {
                case "B": nbB++; break;
                case "M": nbM++; break;
                case "S": nbS++; break;
                case "Z": nbZ++; break;
            }
            cities.add(event.city);
            names.addAll(Arrays.asList(event.lastnames));
            years.add(String.valueOf(event.year));
            lat += event.lat;
            lon += event.lon;
        }
        
        nbCities = cities.size();
        nbNames = names.size();
        nbYears = years.size();

        this.events = events;
        nbEvents = events.size();
        
        if (nbEvents != 0) {
            lat = lat / nbEvents;
            lon = lon / nbEvents;
        }
    }
    
    public int getTime() {
        String[] bits = spaceTime.split("-");
        return Integer.parseInt(bits[1]);
    }
    
    public boolean isEligible(int level) {
        if (level == 1) {
            return true;
        }
        if (level == 2) {
            return (nbCities > 0 && nbNames > 0);
        }
        if (level == 3) {
            return nbEvents > 0 || nbCityName > 0 || (nbCities > 0 && nbNames > 0);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return spaceTime;
    }
    
    public void setNbEvents(int set) {
        nbEvents = set;
    }

}
