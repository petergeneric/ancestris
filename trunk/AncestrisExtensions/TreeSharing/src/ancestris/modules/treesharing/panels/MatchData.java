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

import genj.gedcom.Entity;

/**
 *
 * @author frederic
 */
public class MatchData {
    
    public Entity myEntity;
    public FriendGedcomEntity friendGedcomEntity;
    public String matchType;
    
    public MatchData(Entity entity, FriendGedcomEntity otherEntity, String type) {
        this.myEntity = entity;
        this.friendGedcomEntity = otherEntity;
        this.matchType = type;
    }

}
