/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.tools;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author frederic
 */
public class STMapCapsule implements Serializable {

    public String name = "";

    public Map<String, Integer> map;
    
    STMapCapsule(Map<String, Integer> map) {
        this.map = map;
    }
    

}
