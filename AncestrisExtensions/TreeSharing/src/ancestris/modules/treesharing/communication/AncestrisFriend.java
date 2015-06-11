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

import java.net.URL;

/**
 *
 * @author frederic
 */
public class AncestrisFriend {

        String name;
        URL access;
        
        public AncestrisFriend(String name, URL access) {
            this.name = name;
            this.access = access;
        }
    
}
