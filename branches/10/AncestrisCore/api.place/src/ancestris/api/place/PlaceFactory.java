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
import java.util.StringTokenizer;
import org.geonames.Toponym;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.NbPreferences;

/**
 *
 * @author frederic
 */
public class PlaceFactory implements Place {

    private PropertyPlace propertyPlace = null;
    private Double latitude, longitude = 0d;
    private Long population = 0L;

    public PlaceFactory(PropertyPlace place) {
        this.propertyPlace = place;
        if (propertyPlace != null) {
            PropertyLongitude pLon = propertyPlace.getLongitude(true);
            this.longitude = pLon != null ? pLon.getDoubleValue() : null;
            PropertyLatitude pLat = propertyPlace.getLatitude(true);
            this.latitude = pLat != null ? pLat.getDoubleValue() : null;
        }
    }

    public PlaceFactory(PropertyPlace place, GeoPosition geoPoint) {
        this.propertyPlace = place;
        this.latitude = geoPoint.getLatitude();
        this.longitude = geoPoint.getLongitude();
    }

    private PlaceFactory(Double lat, Double lon, Long pop) {
        this.propertyPlace = null;
        this.latitude = lat;
        this.longitude = lon;
        this.population = pop;
    }
    
    
    @Override
    public int compareTo(Place that) {
        if (propertyPlace == null) {
            return -1;
        }
        return that.getValueStartingWithCity().compareToIgnoreCase(propertyPlace.getValueStartingWithCity());
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
    public Long getPopulation() {
        return population;
    }

    @Override
    public Toponym getToponym() {
        return null;
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

    
    public static Place findPlace(String searchedPlaceKey) {
        String code = NbPreferences.forModule(PlaceFactory.class).get(searchedPlaceKey, null);
        return Code2Place(code);
    }

    public static void rememberPlace(String searchedPlaceKey, Place place) {
        NbPreferences.forModule(PlaceFactory.class).put(searchedPlaceKey, Place2Code(place));
    }

    
    
    /**
     * Converts geocoordinates strings to place (elements other than
     * geocoordinates are not used in that case)
     *
     * @param code
     * @return
     */
    private static Place Code2Place(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        Double lat = 0d;
        Double lon = 0d;
        Long pop = 0L;
        try {
            StringTokenizer tokens = new StringTokenizer(code, ";");
            if (tokens.hasMoreTokens()) {
                lat= Double.parseDouble(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                lon= Double.parseDouble(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                pop = Long.parseLong(tokens.nextToken());
            }
        } catch (Throwable t) {
        }
        return new PlaceFactory(lat, lon, pop);

    }

    /**
     * Converts toponym coordinates into coordinates string
     *
     * @param topo
     * @return
     */
    private static String Place2Code(Place place) {
        if (place == null) {
            return "";
        }
        return place.getLatitude() + ";" + place.getLongitude() + ";" + place.getPopulation();
    }

    

}
