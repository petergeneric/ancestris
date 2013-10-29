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

import genj.gedcom.Entity;
import genj.gedcom.PropertyXRef;

/**
 *
 * @author daniel
 */
public class PropertyXRefWrapper extends PropertyWrapper {

    PropertyXRefWrapper(PropertyXRef p) {
        super(p);
    }

    public EntityWrapper getTarget() {
        Entity e = ((PropertyXRef) property).getTargetEntity();
        return (EntityWrapper) create(e);
    }
    
}
