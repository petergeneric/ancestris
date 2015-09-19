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

import ancestris.modules.treesharing.communication.GedcomFam;
import ancestris.modules.treesharing.communication.GedcomIndi;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertySex;

/**
 *
 * @author frederic
 */
public class FriendGedcomEntity {

    public String type = "";
    public AncestrisFriend afriend = null;
    public String friend = "";
    public String gedcomName = "";
    public String entityID = "";
    
    public String indiID = "";
    public int    indiSex = PropertySex.UNKNOWN;
    public String indiLastName = "";
    public String indiFirstName = "";
    public String indiBirthDate = "";
    public String indiBirthPlace = "";
    public String indiDeathDate = "";
    public String indiDeathPlace = "";

    public String spouID = "";
    public int    spouSex = PropertySex.UNKNOWN;
    public String spouLastName = "";
    public String spouFirstName = "";
    public String spouBirthDate = "";
    public String spouBirthPlace = "";
    public String spouDeathDate = "";
    public String spouDeathPlace = "";
    
    public String famMarrDate = "";
    public String famMarrPlace = "";
    

    public FriendGedcomEntity(String name, GedcomIndi indi) {
        this.type= Gedcom.INDI;
        this.friend = name;
        this.gedcomName = indi.gedcomName;
        this.entityID = indi.entityID;
        this.indiID = indi.entityID;
        this.indiSex = Integer.valueOf(indi.indiSex);
        this.indiLastName = indi.indiLastName;
        this.indiFirstName = indi.indiFirstName;
        this.indiBirthDate = indi.indiBirthDate;
        this.indiBirthPlace = indi.indiBirthPlace;
        this.indiDeathDate = indi.indiDeathDate;
        this.indiDeathPlace = indi.indiDeathPlace;
    }

    public FriendGedcomEntity(String name, GedcomFam fam) {
        this.type= Gedcom.FAM;
        this.friend = name;
        this.gedcomName = fam.gedcomName;
        this.entityID = fam.entityID;
        this.indiID = fam.husbID;
        this.indiSex = Integer.valueOf(fam.husbSex);
        this.indiLastName = fam.husbLastName;
        this.indiFirstName = fam.husbFirstName;
        this.indiBirthDate = fam.husbBirthDate;
        this.indiBirthPlace = fam.husbBirthPlace;
        this.indiDeathDate = fam.husbDeathDate;
        this.indiDeathPlace = fam.husbDeathPlace;
        this.spouID = fam.wifeID;
        this.spouSex = Integer.valueOf(fam.wifeSex);
        this.spouLastName = fam.wifeLastName;
        this.spouFirstName = fam.wifeFirstName;
        this.spouBirthDate = fam.wifeBirthDate;
        this.spouBirthPlace = fam.wifeBirthPlace;
        this.spouDeathDate = fam.wifeDeathDate;
        this.spouDeathPlace = fam.wifeDeathPlace;
        this.famMarrDate = fam.famMarrDate;
        this.famMarrPlace = fam.famMarrPlace;
    }
        
    public void setFriend(AncestrisFriend afriend) {
        this.afriend = afriend;
    }
    
    
}
