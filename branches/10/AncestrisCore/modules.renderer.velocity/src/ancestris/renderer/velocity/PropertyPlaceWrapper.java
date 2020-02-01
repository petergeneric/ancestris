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

import genj.gedcom.PropertyPlace;

/**
 *
 * @author daniel
 */
public class PropertyPlaceWrapper extends PropertyWrapper {

    PropertyPlaceWrapper(PropertyPlace p) {
        super(p);
    }

    public String[] getHierarchy() {
        String hierarchy = ((PropertyPlace) property).getFormatAsString();
        return hierarchy.split(",");
    }

    public String getJuridiction(int level) {
        return ((PropertyPlace) property).getJurisdiction(level);
    }

    public String getFirstAvailableJurisdiction() {
        return ((PropertyPlace) property).getFirstAvailableJurisdiction();
    }

    public String getCity() {
        return ((PropertyPlace) property).getCity();
    }

    public String getJuridiction(String f) {
        String[] values = ((PropertyPlace) property).getValue().split(",");
        String[] hierarchy = getHierarchy();
        for (int i = 0; i < hierarchy.length; i++) {
            if (hierarchy[i].trim().equalsIgnoreCase(f.trim())) {
                return values[i];
            }
        }
        return null;
    }
    
}
