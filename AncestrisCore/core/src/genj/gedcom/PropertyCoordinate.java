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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
public abstract class PropertyCoordinate extends Property {
    
    private Double coordinate = Double.NaN;
    private char direction = '\0';
    private String value = "";
    

    private boolean isTrueCoordinate() {
        return !coordinate.isNaN() && (direction == '\0' || direction == getDirection(coordinate)) && isValidCoordinateRange(coordinate);
    }    
    
    @Override
    public boolean isValid() {
        return (getValue().isEmpty() && super.isValid()) || isTrueCoordinate();
    }    
    
    /**
     * Returns string representation of direction (NS,EW) for this coordinate.
     * 
     * @param coordinate
     * @return 
     */
    abstract char getDirection(double coordinate);

    abstract boolean isValidCoordinateRange(double coordinate);
    
    private final static Pattern COORD_PATTERN = Pattern.compile("^(?<deg>[-+0-9]+)[^0-9]+(?<min>[0-9]+)[^0-9]+(?<sec>[0-9.,]+)[^0-9.,ENSW]+(?<pos>[ENSW]*)$");
    private final static Pattern GDC_COORD_PATTERN = Pattern.compile("^(?<pos>[ENSW]?)(?<deg>.*)$");

  /**
   * Constructor with tag
   */
  public PropertyCoordinate(String tag) {
    super(tag);
  }
    /**
     * Parses the latitude and longitude to double.
     * 
     * @param value coordinate, can be of any form:
     * <ul><li/>String representation of a Double value
     * <li/>NSEWnn.fff
     * <li/>[+-]dd°mm'ss"[NSWE]
     * </ul>
     * @return Coordination in as a Double value
     */
    private double parseCoordinate(String value) {
        double result = Double.NaN;
        // Clear direction flag
        direction = '\0';
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 2).replace("\"\"", "\"");
        }
        // convert to uppercase
        value = value.toUpperCase();
        Matcher matcher = COORD_PATTERN.matcher(value);
        if (matcher.matches()) {
            double deg = Double.parseDouble(matcher.group("deg"));
            double min = Double.parseDouble(matcher.group("min"));
            double sec = Double.parseDouble(matcher.group("sec"));
            String p = matcher.group("pos");
            if (!p.isEmpty()){
                direction = p.charAt(0);
            }
            result = deg + (min / 60) + (sec / 3600);
            result = ((direction == 'S') || (direction == 'W')) ? -result : result;
            return result;
        }
        matcher = GDC_COORD_PATTERN.matcher(value);
        if (matcher.matches()) {
            String p = matcher.group("pos");
            if (!p.isEmpty()){
                direction = p.charAt(0);
            }
            try {
                result = Double.parseDouble(matcher.group("deg").replace(",", "."));
                result = ((direction == 'S') || (direction == 'W')) ? -result : result;
            } catch (NumberFormatException e){}
        }
        return result;
    }

    @Override
    public String getValue() {
        if (isTrueCoordinate()){
            return getDirection(coordinate)+String.valueOf(Math.abs(coordinate));
        }
        if (value == null) {
            value = "";
        }
        return value;
    }

    @Override
    public void setValue(String value) {
        String old = getValue();
        value = trim(value);
        coordinate = parseCoordinate(value);
        this.value = value;
        propagatePropertyChanged(this, old);
    }
    
    protected String trim(String value) {
        return (value != null) ? value.replaceAll("\\r\\n|\\r|\\n", " ") : "";
    }
    
    public Double getDoubleValue(){
        return coordinate;
    }
}
