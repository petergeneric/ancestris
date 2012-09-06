/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package genj.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

/**
 *
 * @author daniel
 */
public interface AncestrisPreferences {

    /**
     * Returns a map of values
     */
    @SuppressWarnings(value = "unchecked")
    <K, V> Map<K, V> get(String key, Map<K, V> def);

    /**
     * Returns array of ints by key
     */
    int[] get(String key, int[] def);

    /**
     * Returns array of Boolean by key
     */
    Boolean[] get(String key, Boolean[] def);

    /**
     * Returns array of Rectangles by key
     */
    Rectangle[] get(String key, Rectangle[] def);

    /**
     * Returns array of strings by key
     */
    String[] get(String key, String[] def);

    /**
     * Returns float parameter to key
     */
    float get(String key, float def);

    /**
     * Returns integer parameter to key
     */
    int get(String key, int def);

    /**
     * Returns Enum parameter for Key
     * @param <T>
     * @param key
     * @param def
     * @return 
     */
    <T extends Enum<T>> T get (String key, T def);

    /**
     * Returns dimension parameter by key
     */
    Dimension get(String key, Dimension def);

    /**
     * Returns font parameter by key
     */
    Font get(String key, Font def);

    /**
     * Returns point parameter by key
     */
    Point get(String key, Point def);

    /**
     * Returns point parameter by key
     */
    Point2D get(String key, Point2D def);

    /**
     * Returns rectangle parameter by key
     */
    Rectangle get(String key, Rectangle def);

    /**
     * Returns a collection of strings by key
     */
    List<String> get(String key, List<String> def);

    /**
     * Returns boolean parameter by key
     */
    boolean get(String key, boolean def);

    /**
     * Returns color parameter by key
     */
    Color get(String key, Color def);

    /**
     * Returns String parameter to key
     */
    String get(String key, String def);

    /**
     * Returns a collection of strings by key
     */
    Collection<String> get(String key, Collection<String> def);

    JFrame get(String key, JFrame frame);

    void propertyChange(PropertyChangeEvent evt);

    /**
     * Remembers a String value
     */
    void put(String key, String value);

    /**
     * Remember an array of values
     */
    void put(String key, Map<String, ?> values);

    /**
     * Remembers an array of ints
     */
    void put(String key, int[] value);

    /**
     * Remembers an array of Rectangles
     */
    void put(String key, Rectangle[] value);

    /**
     * Remembers an array of Strings(Objects)
     */
    void put(String key, Object[] value);

    /**
     * Remembers an array of Strings
     */
    void put(String key, Object[] value, int length);

    /**
     * Remembers an float value
     */
    void put(String key, float value);

    /**
     * Remembers an int value
     */
    void put(String key, int value);

    /**
     * Remembers an enum value
     */
    void put (String key, Enum value);

    /**
     * Remembers a point value
     */
    void put(String key, Dimension value);

    /**
     * Remembers a font value
     */
    void put(String key, Font value);

    /**
     * Remembers a point value
     */
    void put(String key, Point value);

    /**
     * Remembers a point value
     */
    void put(String key, Point2D value);

    /**
     * Remembers a rectangle value
     */
    void put(String key, Rectangle value);

    /**
     * Remembers a collection of Strings
     */
    void put(String key, Collection<?> values);

    /**
     * Remembers a boolean value
     */
    void put(String key, Boolean value);

    /**
     * Remembers a boolean value
     */
    void put(String key, Color value);

    /**
     * store JFrame characteristics
     */
    void put(String key, JFrame frame);

}
