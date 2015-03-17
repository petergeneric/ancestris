/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer.velocity;

import genj.gedcom.PropertyAge;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
public class PropertyAgeWrapper extends PropertyWrapper {

    PropertyAgeWrapper(PropertyAge p) {
        super(p);
    }

    public String get(String what) {
        Matcher temp = Pattern.compile("(([0-9]+)y *)?(([0-9]+)m *)?(([0-9]+)d)?").matcher(getValue());
        if (temp.matches()) {
            if (what.equals("Years")) {
                return temp.group(2);
            }
            if (what.equals("Months")) {
                return temp.group(4);
            }
            if (what.equals("Days")) {
                return temp.group(6);
            }
            return null;
        }
        return null;
    }

    public String toString() {
        return get("Years");
    }
    
}
