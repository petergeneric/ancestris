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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author frederic
 */
public class STEvent implements Comparable {

    private static String SEP = " - ";

    public String type;
    public String city;
    public double lat;
    public double lon;
    public String[] lastnames;
    public int year;
    
    public String entity;
    public String propertyName;
    public String propertyDate;
    public String propertyPlace;
    public String propertyString;
    
    private Property property;

    public STEvent() {
        property = null;
    }
    
    public STEvent(Property property, Double lat, Double lon) {
        
        this.type = buildType(property.getTag());
        this.city = buildCity(property.getProperty("PLAC"));
        this.lat = lat;
        this.lon = lon;
        this.lastnames = buildLastnames(property.getEntity());
        this.year = buildDate((PropertyDate) property.getProperty("DATE"));

        this.property = property;
        this.entity = getDisplayEntity();
        this.propertyName = getDisplayName();
        this.propertyDate = getDisplayDate();
        this.propertyPlace = getDisplayPlace();
        this.propertyString = getDisplayString();
    }
    
    @Override
    public int compareTo(Object o) {
        return getKey().compareTo(((STEvent) o).getKey());
    }

    public boolean isSameType(STEvent e) {
        return this.type.equals(e.type);
    }

    public boolean isSameCity(STEvent e) {
        if (this.city.equals(e.city)) {
            return true;
        }
        for (String city1 : city.split(" ")) {
            for (String city2 : e.city.split(" ")) {
                if (city1.contains(city2) || city2.contains(city1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSameYear(STEvent e) {
        return this.year == e.year;
    }

    public boolean isSameName(STEvent e) {
        for (String name1 : lastnames) {
            for (String name2 : e.lastnames) {
                if (name1.contains(name2) || name2.contains(name1)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isSame(STEvent e) {
        return isSameType(e) && isSameCity(e) && isSameName(e) && isSameYear(e);
    }

    public String getDisplayEvent() {
        return propertyName + SEP + propertyDate + SEP + propertyPlace;
    }

    public Object getProperty() {
        return property == null ? propertyString : property;
    }
    

    
    
    private String getDisplayEntity() {
        Entity ent = (Entity) property.getEntity();
        return ent.toString(true);
    }

    private String getDisplayName() {
        return property.getPropertyName();
    }

    private String getDisplayDate() {
        PropertyDate pDate = (PropertyDate) property.getProperty("DATE");
        return pDate == null ? "" : pDate.getDisplayValue();
    }

    private String getDisplayPlace() {
        PropertyPlace pPlace = (PropertyPlace) property.getProperty("PLAC");
        return pPlace == null ? "" : pPlace.getDisplayValue();
    }

    private String getDisplayString() {
        return property.toString();
    }

    private String getKey() {
        StringBuilder ret = new StringBuilder();
        ret.append(type).append(SEP);
        ret.append(city).append(SEP);
        for (String name : lastnames) {
            ret.append(name).append(SEP);
        }
        return ret.toString();
    }
    
    
    private static String buildType(String tag) {
        // Types should be sorted alphabetically like this "B", "M", "S", "Z" 
        String type = "";
        switch (tag) {
            case "BIRT":
            case "CHR":
            case "BAPM":
                type = "B";
                break;
            case "MARR":
            case "MARC":
            case "ENGA":
                type = "M";
                break;
            case "DEAT":
            case "BURI":
            case "CREM":
                type = "S";
                break;
            default:
                type = "Z";
                break;
        }
        return type;
    }

    private static String buildCity(Property pPlace) {
        return ((PropertyPlace) pPlace).getCity().toUpperCase();
    }

    private static int buildDate(PropertyDate pDate) {
        PointInTime pitS = pDate.getStart();
        if (pDate.isRange()) {
            PointInTime pitE = pDate.getEnd();
            return (pitS.getYear() + pitE.getYear()) / 2;
        } else {
            return pitS.getYear();
        }
    }

    private static String[] buildLastnames(Entity entity) {
        if (entity instanceof Indi) {
            Indi indi = (Indi) entity;
            return upperCase(indi.getLastNames());
        }
        if (entity instanceof Fam) {
            List<String> names = new ArrayList<>();
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            if (husb != null) {
                names.addAll(Arrays.asList(upperCase(husb.getLastNames())));
            }
            Indi wife = fam.getWife();
            if (wife != null) {
                names.addAll(Arrays.asList(upperCase(wife.getLastNames())));
            }
            return names.toArray(new String[names.size()]);
        }
        return null;
    }

    private static String[] upperCase(String[] names) {
        for (int i=0; i<names.length; i++) {
            names[i] = names[i].toUpperCase();
        }
        return names;
    }

    
}