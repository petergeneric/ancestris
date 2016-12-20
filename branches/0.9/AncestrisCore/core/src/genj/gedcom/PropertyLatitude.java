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
/**
 * Gedcom Property for Latitude representation
 */
public class PropertyLatitude extends PropertyCoordinate {

    private static String TAG55 = "_LATI";
    private static String TAG551 = "LATI";

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyLatitude(String tag) {
        super(tag);
    }

    /**
     * Creates a new PropertyLatitude object according to gedcom version. see {@link #getTag(boolean)
     * }
     *
     * @param isGedcom55
     */
    public PropertyLatitude(boolean isGedcom55) {
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

    @Override
    char getDirection(double coordinate) {
        return coordinate < 0 ? 'S' : 'N';
    }

}
