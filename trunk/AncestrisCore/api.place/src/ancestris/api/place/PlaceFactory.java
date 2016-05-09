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
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author frederic
 */
public class PlaceFactory implements Place {

    private PropertyPlace propertyPlace = null;
    private Double latitude, longitude = 0d;

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
    

}
