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

import genj.gedcom.PropertyFile;

/**
 *
 * @author daniel
 */
public class PropertyFileWrapper extends PropertyWrapper {

    PropertyFileWrapper(PropertyFile p) {
        super(p);
    }

    public String getAbsolutePath() {
        try {
            return ((PropertyFile) property).getInput().get().getLocation();
        } catch (Exception e) {
            return null;
        }
    }
    
}
