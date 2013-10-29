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

/**
 *
 * @author daniel
 */
public class EntityWrapper extends PropertyWrapper {

    EntityWrapper(Entity e) {
        super(e);
    }

    public String getId() {
        return ((Entity) property).getId();
    }
    
}
