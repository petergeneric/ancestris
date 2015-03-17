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

import genj.gedcom.Gedcom;

/**
 *
 * @author daniel
 */
public class GedcomWrapper extends Object {
    Gedcom theGedcom;

    GedcomWrapper(Gedcom g) {
        theGedcom = g;
    }

    public EntityWrapper getSubmitter() {
        return new EntityWrapper(theGedcom.getSubmitter());
    }
    
}
