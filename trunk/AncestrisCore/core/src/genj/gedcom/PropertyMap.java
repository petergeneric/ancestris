/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.gedcom;

/**
 *
 * @author daniel
 */
public class PropertyMap extends PropertySimpleReadOnly {

    private static String TAG55 = "_MAP";
    private static String TAG551 = "MAP";

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyMap(String tag) {
        super(tag);
    }

    /**
     * Creates a new PropertyMap object according to gedcom version. see
     * {@link #getTag(boolean)}
     *
     * @param isGedcom55
     */
    public PropertyMap(boolean isGedcom55) {
        super(getTag(isGedcom55));
    }

    /**
     * Return tag according to gedcom version
     *
     * @param isGedcom55 true if gedcom version 5.5, false for version 5.5.1
     * @return
     */
    public static String getTag(boolean isGedcom55) {
        return isGedcom55 ? TAG55 : TAG551;
    }

    /**
     * Constructor with tag & value
     */
    public PropertyMap(String tag, String value) {
        super(tag, value);
    }

    /**
     * Return PropertyLatitude for this Map. Resolve against gedcom version
     *
     * @return
     */
    public PropertyLatitude getLatitude() {
        return (PropertyLatitude) getProperty(PropertyLatitude.getTag(isVersion55()));
    }

    /**
     * Return PropertyLongitude for this Map. Resolve aginst gedcom version
     *
     * @return
     */
    public PropertyLongitude getLongitude() {
        return (PropertyLongitude) getProperty(PropertyLongitude.getTag(isVersion55()));
    }
}
