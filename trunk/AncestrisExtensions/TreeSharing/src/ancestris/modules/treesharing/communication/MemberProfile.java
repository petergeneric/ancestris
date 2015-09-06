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

import java.awt.Image;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class MemberProfile implements Serializable {

        // nom, prénom, ville, pays, email, photo, h/f
        public String lastname = "";
        public String firstname = "";
        public String city = "";
        public String country = "";
        public String email = "";
        public Image photo = null;
        
        public MemberProfile() {
        }
        

}
