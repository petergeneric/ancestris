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
    public int matchResult;
    
    public MatchData(Entity entity, FriendGedcomEntity otherEntity, int matchResult) {
        this.myEntity = entity;
        this.friendGedcomEntity = otherEntity;
        this.matchResult = matchResult;
    }
    
    @Override
    public boolean equals(Object obj) {
        MatchData md = (MatchData) obj;
        return md.myEntity.getGedcom().getName().equals(this.myEntity.getGedcom().getName())
            && md.myEntity.getId().equals(this.myEntity.getId())
            && md.friendGedcomEntity.entityID.equals(this.friendGedcomEntity.entityID)
            && md.friendGedcomEntity.gedcomName.equals(this.friendGedcomEntity.gedcomName)
            && md.friendGedcomEntity.friend.equals(this.friendGedcomEntity.friend);
    }

    @Override
    public int hashCode() {
        return myEntity.getGedcom().getName().hashCode() 
             + myEntity.getId().hashCode() 
             + friendGedcomEntity.entityID.hashCode() 
             + friendGedcomEntity.gedcomName.hashCode() 
             + friendGedcomEntity.friend.hashCode();
    }
}
