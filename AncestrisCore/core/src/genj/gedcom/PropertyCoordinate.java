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
public abstract class PropertyCoordinate extends Property{
    
    private Double coordinate = Double.NaN;
    private char direction = '\0';
    private String value = null;
    

    @Override
    public boolean isValid() {
        return coordinate != Double.NaN && (direction == '\0' || direction == getDirection(coordinate));
    }    
    
    /**
     * Returns string representation of direction (NS,EW) for this coordinate.
     * 
     * @param coordinate
     * @return 
     */
    abstract char getDirection(double coordinate);
    
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
                result = Double.parseDouble(matcher.group("deg"));
                result = ((direction == 'S') || (direction == 'W')) ? -result : result;
            } catch (NumberFormatException e){}
        }
        return result;
    }

    @Override
    public String getValue() {
        if (isValid()){
            String res = getDirection(coordinate)+String.valueOf(Math.abs(coordinate));
            return res.length()>8?res.substring(0, 8):res;
        }
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
        coordinate = parseCoordinate(value);
//        if (coordinate != Double.NaN){
//            this.value = null;
//        }
    }
    
    public Double getDoubleValue(){
        return coordinate;
    }
}
