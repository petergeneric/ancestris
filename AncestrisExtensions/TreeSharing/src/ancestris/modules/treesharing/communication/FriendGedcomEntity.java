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

import ancestris.modules.treesharing.panels.AncestrisFriend;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;

/**
 *
 * @author frederic
 */
public class FriendGedcomEntity {

    
        private AncestrisFriend friend = null;
        private Gedcom gedcom = null;
        private Entity entity = null;
        
        
        public FriendGedcomEntity(AncestrisFriend friend, Gedcom gedcom, Entity entity) {
            this.friend = friend;
            this.gedcom = gedcom;
            this.entity = entity;
        }
        
        public boolean isIndi() {
            return entity instanceof Indi;
        }

        public boolean isFam() {
            return entity instanceof Fam;
        }
    
        public Indi getIndi() {
            return (Indi) entity;
        }

        public Fam getFam() {
            return (Fam) entity;
        }

        public String getName() {
            if (friend == null) {
                return "";
            }
            return friend.getFriendName();
        }

    
}
