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

import genj.gedcom.PropertyName;

/**
 *
 * @author daniel
 */
public class PropertyNameWrapper extends PropertyWrapper {

    PropertyNameWrapper(PropertyName p) {
        super(p);
    }

    public String getLastName() {
        return ((PropertyName) property).getLastName();
    }

    public String getFirstName() {
        return ((PropertyName) property).getFirstName();
    }
    
}
