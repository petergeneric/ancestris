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

import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class GedcomIndi implements Serializable {
        public String gedcomName = "";
        public String entityID = "";
        
        public String indiSex = "";
        public String indiLastName = "";
        public String indiFirstName = "";
        public String indiBirthDate = "";
        public String indiBirthPlace = "";
        public String indiDeathDate = "";
        public String indiDeathPlace = "";
}
