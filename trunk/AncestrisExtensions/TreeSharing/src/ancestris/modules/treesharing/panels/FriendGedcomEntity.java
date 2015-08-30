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

package ancestris.modules.treesharing.panels;

/**
 *
 * @author frederic
 */
public class FriendGedcomEntity {

    public String friend = "";
    public String gedcomName = "";
    public String entityID = "";

    public FriendGedcomEntity(String friend, String gedcomName, String entityID) {
        this.friend = friend;
        this.gedcomName = gedcomName;
        this.entityID = entityID;
    }
        
    
}
