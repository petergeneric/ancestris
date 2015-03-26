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
package ancestris.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses longitude or latitude [Ex: "75°36'12.20\"W"] to java double.
 *
 * @author Sholay
 */
/**
 * modified to handle values in float number presentation.
 * @author daniel
 */
public class LatLonParser {

    private final static String EXPRESSION = "^(?<deg>[-+0-9]+)[^0-9]+(?<min>[0-9]+)[^0-9]+(?<sec>[0-9.,]+)[^0-9.,ENSW]+(?<pos>[ENSW]*)$";

    /**
     * Parses the latitude and longitude to double
     *
     * @param value
     * @return
     */
    public static double ParseLatLonValue(String value) {
        double result = Double.NaN;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 2).replace("\"\"", "\"");
        }
        Pattern pattern = Pattern.compile(EXPRESSION);
        Matcher matcher = pattern.matcher(value);
            double deg = Double.NaN;
            double min = Double.NaN;
            double sec = Double.NaN;
            char pos = '\0'; 
            if (matcher.matches()) {
            deg = Double.parseDouble(matcher.group("deg"));
            min = Double.parseDouble(matcher.group("min"));
            sec = Double.parseDouble(matcher.group("sec"));
            String p = matcher.group("pos");
            if (!p.isEmpty()){
                pos = matcher.group("pos").charAt(0);
            }
            result = deg + (min / 60) + (sec / 3600);
            result = ((pos == 'S') || (pos == 'W')) ? -result : result;
        } else {
            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException e){}
        }
        return result;
    }
}
