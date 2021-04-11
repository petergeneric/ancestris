/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.api.place;

import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyPlace;
import java.text.DecimalFormat;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Factory to manage the local place register (get, put)
 * 
 * Place instance created from a PropertyPlace or other elements (excluding Toponyms)

 * @author frederic
 */
public class PlaceFactory implements Place {

    public final static double DEFAULT_LAT = 45; // in the middle of the sea
    public final static double DEFAULT_LON = -4; // in the middle of the sea
    private static String SEP = ";";

    private PropertyPlace propertyPlace = null;
    private Double latitude, longitude = 0d;
    private Long population = 0L;
    private String name = "";
    private String countryCode = "";
    private String countryName = "";
    private String[] adminCodes = new String[] { "", "", "", "", "" };
    private String[] adminNames = new String[] { "", "", "", "", "" };
    private String timezoneId = "";
    private String timezoneGmtOffset = "";


    /* public constructors */   
    public PlaceFactory(PropertyPlace pPlace) {
        if (pPlace == null) {
            return;
        }
        this.propertyPlace = pPlace;
        
        // Default values if they exist on file
        setValues(PlaceFactory.getValues(pPlace.getPlaceToLocalFormat()));
        
        // Overwrite with provided values
        PropertyLatitude pLat = propertyPlace.getLatitude(true);
        PropertyLongitude pLon = propertyPlace.getLongitude(true);

        if (pLat != null) {
            this.latitude = pLat.getDoubleValue();
        } else {
            this.latitude = DEFAULT_LAT;
        }
        if (pLon != null) {
            this.longitude = pLon.getDoubleValue();
        } else {
            this.longitude = DEFAULT_LON;
        }
        
        // Set name if none exists
       if (this.name.isEmpty()) {
            this.name = pPlace.getCity();
        }
    }

    public PlaceFactory(PropertyPlace pPlace, GeoPosition geoPoint) {
        this.propertyPlace = pPlace;

        // Default valus if they exist on file
        setValues(PlaceFactory.getValues(pPlace.getPlaceToLocalFormat()));

        // Overwrite with provided values
        this.latitude = geoPoint.getLatitude();
        this.longitude = geoPoint.getLongitude();
    }

   /* private constructors */
    
    private PlaceFactory(PropertyPlace pPlace, String value) {
        this.propertyPlace = pPlace;
        setValues(value);
    }
    
    /* Factories */
    
   /* Generate a complete place from property place key and local file */
    
    public static Place getLocalPlace(PropertyPlace pPlace) {
        if (pPlace == null) {
            return null;
        }
        String value = PlaceFactory.getValues(pPlace.getPlaceToLocalFormat());
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new PlaceFactory(pPlace, value);
    }

    /* Write a complete place on local file */
    
    public static void putLocalPlace(PropertyPlace pPlace, Place place) {
        if (place == null) {
            throw new UnsupportedOperationException("PlaceFactory - null place.");
        }
        String key = pPlace.getPlaceToLocalFormat();
        
        StringBuilder sb = new StringBuilder();
        sb.append(place.getLatitude()).append(SEP);
        sb.append(place.getLongitude()).append(SEP);
        sb.append(place.getPopulation()).append(SEP);
        sb.append(place.getCountryCode()).append(SEP);
        sb.append(place.getAdminCode(1)).append(SEP);
        sb.append(place.getAdminCode(2)).append(SEP);
        sb.append(place.getAdminCode(3)).append(SEP);
        sb.append(place.getAdminCode(4)).append(SEP);
        sb.append(place.getAdminCode(5)).append(SEP);
        sb.append(place.getName()).append(SEP);
        sb.append(place.getAdminName(1)).append(SEP);
        sb.append(place.getAdminName(2)).append(SEP);
        sb.append(place.getAdminName(3)).append(SEP);
        sb.append(place.getAdminName(4)).append(SEP);
        sb.append(place.getAdminName(5)).append(SEP);
        sb.append(place.getCountryName()).append(SEP);
        sb.append(place.getTimeZoneId()).append(SEP); 
        sb.append(place.getTimeZoneGmtOffset()); 
        String value = sb.toString();
        
        PlaceFactory.putValues(key, value);
    }

    private void setValues(String value) {
        if (value == null) {
            return;
        }
        try {
            String[] bits = value.split(SEP);
            int max = bits.length;
            int i = 0;
            if (i<max) {
                this.latitude = Double.parseDouble(bits[i++]);
            }
            if (i<max) {
                this.longitude = Double.parseDouble(bits[i++]);
            }
            if (i<max) {
                this.population = Long.parseLong(bits[i++]);
            }
            if (i<max) {
                this.countryCode = bits[i++];
            }
            if (i<max) {
                this.adminCodes[0] = bits[i++];
            }
            if (i<max) {
                this.adminCodes[1] = bits[i++];
            }
            if (i<max) {
                this.adminCodes[2] = bits[i++];
            }
            if (i<max) {
                this.adminCodes[3] = bits[i++];
            }
            if (i<max) {
                this.adminCodes[4] = bits[i++];
            }
            if (i<max) {
                this.name = bits[i++];
            }
            if (i<max) {
                this.adminNames[0] = bits[i++];
            }
            if (i<max) {
                this.adminNames[1] = bits[i++];
            }
            if (i<max) {
                this.adminNames[2] = bits[i++];
            }
            if (i<max) {
                this.adminNames[3] = bits[i++];
            }
            if (i<max) {
                this.adminNames[4] = bits[i++];
            }
            if (i<max) {
                this.countryName = bits[i++];
            }
            if (i<max) {
                this.timezoneId = bits[i++];
            }
            if (i<max) {
                this.timezoneGmtOffset = bits[i++];
            }
        } catch (NumberFormatException t) {
        }
    }
    
    /* methods */
    private static String getValues(String key) {
        return NbPreferences.forModule(PlaceFactory.class).get(key, null);
    }
    
    private static void putValues(String key, String value) {
        NbPreferences.forModule(PlaceFactory.class).put(key, value);
    }
    
    public static String buildInfo(Place place) {
        String spa = " ";
        String sep = "   \n";
        String name = place.getName() == null ? NbBundle.getMessage(PlaceFactory.class, "TXT_UNKNOWN") : place.getName();
        StringBuilder str = new StringBuilder();
        String timezone = place.getTimeZoneId() + " (" + place.getTimeZoneGmtOffset() + ")";
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Name")).append(spa).append(name).append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Coord")).append(spa).append(getTextCoordinates(place)).append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Time")).append(spa).append(timezone).append(sep);
        str.append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_CdInsee")).append(spa).append(dispName(place.getAdminCode(4))).append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Distri")).append(spa).append(dispName(place.getAdminName(4))).append(" (").append(dispName(place.getAdminCode(3))).append(")").append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Dept")).append(spa).append(dispName(place.getAdminName(2))).append(" (").append(dispName(place.getAdminCode(2))).append(")").append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Region")).append(spa).append(dispName(place.getAdminName(1))).append(" (").append(dispName(place.getAdminCode(1))).append(")").append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Cntry")).append(spa).append(dispName(place.getCountryName())).append(sep);
        str.append(sep);
        str.append(NbBundle.getMessage(PlaceFactory.class, "TXT_Pop")).append(spa).append(place.getPopulation());
        str.append(sep);
        str.append(" ");
        return str.toString();
    }

    private static String dispName(String str) {
        return str == null || str.isEmpty() ? "-" : str;
    }

    private static String getTextCoordinates(Place place) {
        Double lat = place.getLatitude();
        Double lon = place.getLongitude();
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

    
    @Override
    public String toString() {
        return "PropPlace:"+propertyPlace.getDisplayValue()+ "; " 
                + "Name: "+getName() + " ;" 
                + "CC: "+getCountryCode() + "; " 
                + "AC1: "+getAdminCode(1) + "; " 
                + "AC2: "+getAdminCode(2) + "; " 
                + "AC3: "+getAdminCode(3) + "; " 
                + "AC4: "+getAdminCode(4) + "; " 
                + "AC5: "+getAdminCode(5) + "; ";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInfo() {
        return PlaceFactory.buildInfo(this);
    }

    @Override
    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public String getCountryName() {
        return countryName;
    }

    @Override
    public String getAdminCode(int level) {
        return adminCodes[level-1];
    }

   @Override
    public String getAdminName(int level) {
        return adminNames[level-1];
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public String getTimeZoneId() {
        return timezoneId;
    }

    @Override
    public String getTimeZoneGmtOffset() {
        return timezoneGmtOffset;
    }

    @Override
    public Long getPopulation() {
        return population;
    }

    @Override
    public String getCity() {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getCity();
    }

    @Override
    public String getFirstAvailableJurisdiction() {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getFirstAvailableJurisdiction();
    }

    @Override
    public String[] getFormat() {
        if (propertyPlace == null) {
            return null;
        }
        return propertyPlace.getFormat();
    }

    @Override
    public String getFormatAsString() {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getFormatAsString();
    }

    @Override
    public String getJurisdiction(int hierarchyLevel) {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getJurisdiction(hierarchyLevel);
    }

    @Override
    public String[] getJurisdictions() {
        if (propertyPlace == null) {
            return null;
        }
        return propertyPlace.getJurisdictions();
    }

    @Override
    public String getValueStartingWithCity() {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getValueStartingWithCity();
    }

    @Override
    public void setFormatAsString(boolean global, String format) {
        if (propertyPlace == null) {
            return;
        }
        propertyPlace.setFormatAsString(global, format);
    }

    @Override
    public String getPlaceToLocalFormat() {
        if (propertyPlace == null) {
            return "";
        }
        return propertyPlace.getPlaceToLocalFormat();
    }

    @Override
    public int compareTo(Place that) {
        if (propertyPlace == null) {
            return -1;
         }
        return that.getValueStartingWithCity().compareToIgnoreCase(propertyPlace.getValueStartingWithCity());
     }

}
