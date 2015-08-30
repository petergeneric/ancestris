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

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class GedcomFam implements Serializable {

    
        public String gedcomName = "";
        public String entityID = "";
        public String famMarrDate = "";
        public String famMarrPlace = "";
        public String husbLastName = "";
        public String husbFirstName = "";
        public String husbBirthDate = "";
        public String husbBirthPlace = "";
        public String husbDeathDate = "";
        public String husbDeathPlace = "";
        public String wifeLastName = "";
        public String wifeFirstName = "";
        public String wifeBirthDate = "";
        public String wifeBirthPlace = "";
        public String wifeDeathDate = "";
        public String wifeDeathPlace = "";
        
        
        public GedcomFam(Gedcom gedcom, Fam fam) {
        this.gedcomName = gedcom.getName();
        this.entityID = fam.getId();

        PropertyDate propDate = fam.getMarriageDate();
        this.famMarrDate = propDate != null ? propDate.getValue() : "";
        Property prop = fam.getProperty(new TagPath("FAM:MARR:PLAC"));
        this.famMarrPlace = prop != null ? prop.getValue() : "";
        Indi husband = fam.getHusband();
        if (husband != null) {
            this.husbLastName = husband.getLastName();
            this.husbFirstName = husband.getFirstName();
            this.husbBirthDate = husband.getBirthAsString();
            this.husbBirthPlace = getBirthPlace(husband);
            this.husbDeathDate = husband.getDeathAsString();
            this.husbDeathPlace = getDeathPlace(husband);
        }
        Indi wife = fam.getWife();
        if (wife != null) {
            this.wifeLastName = wife.getLastName();
            this.wifeFirstName = wife.getFirstName();
            this.wifeBirthDate = wife.getBirthAsString();
            this.wifeBirthPlace = getBirthPlace(wife);
            this.wifeDeathDate = wife.getDeathAsString();
            this.wifeDeathPlace = getDeathPlace(wife);
        }
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
