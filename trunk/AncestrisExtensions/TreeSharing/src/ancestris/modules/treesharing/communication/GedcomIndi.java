/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.treesharing.communication;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class GedcomIndi implements Serializable {

    
        public String gedcomName = "";
        public String entityID = "";
        public String indiLastName = "";
        public String indiFirstName = "";
        public String indiBirthDate = "";
        public String indiBirthPlace = "";
        public String indiDeathDate = "";
        public String indiDeathPlace = "";
        
        public GedcomIndi(Gedcom gedcom, Indi indi) {
            this.gedcomName = gedcom.getName();
            this.entityID = indi.getId();
            this.indiLastName = indi.getLastName();
            this.indiFirstName = indi.getFirstName();
            this.indiBirthDate = indi.getBirthAsString();
            this.indiBirthPlace = getBirthPlace(indi);
            this.indiDeathDate = indi.getDeathAsString();
            this.indiDeathPlace = getDeathPlace(indi);
        }
        
        private String getBirthPlace(Indi indi) {
            Property prop = indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
            return prop != null ? prop.getValue() : "";
        }
        
        private String getDeathPlace(Indi indi) {
            Property prop = indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
            return prop != null ? prop.getValue() : "";
        }

}
