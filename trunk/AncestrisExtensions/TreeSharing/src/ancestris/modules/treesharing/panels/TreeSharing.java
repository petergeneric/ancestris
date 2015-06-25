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

import ancestris.gedcom.GedcomFileListener;
import ancestris.modules.treesharing.communication.Comm;
import ancestris.modules.treesharing.communication.FriendGedcomEntity;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Purpose of the whole sharing tree game : 
 * ---------------------------------------
 * Get subtrees of other Ancestris friends which share common entities (INDI, FAM) with my own shared gedcom trees
 * 
 * Overall process, which might be split into visual steps from users:
 * - Identify the list of currently sharing friends from the ancestris server (crypted communication)
 * - Launch sequentially a 1-to-1 communications with each sharing Ancestris friend (crypted communication)
 * - Ask each member ancestris running program for the list of shared [gedcom x entities(INDI, FAM)] 
 *      / limited to owner's criteria (duration, selected members, privacy) 
 *      / one gedcom at a time, crypted and zipped
 *      / until all data collected on my ancestris
 * - Once data collected within my ancestris, without me knowing, compares all entities to all of mines,
 *      / on "Lastname" + "one firstname" + "birth or death date" for individuals and families 
 * - Flags all my entities which are found as matching across all shared gedcoms, and which ancestris member/gedcom/entityID it is matched to (pseudos and the matching entities list)
 * - Continue with each member
 * - Store permanently matching elements (crypted) and skip members and entities already matched (from crypted storage) (useful for performance reason)
 * - Notifies me of the existence of matches, but without revealing any data
 * - Notifies the identified members that I have identified common data with them
 * <pause>
 * - Requests authorisation to the identified friends 
 * - Once mutual agreement confirmed, asynchronously, provides each member (me and my matching mate) the matching entities list, the total number of entities
 * <pause>
 * - Upon subsequent agreement, subtrees and related sources/media/repos/note could be shared among users (qualify size and direction (ancestors, descendants, siblings) before transmitting
 * <pause>
 * - Upon agreement, communicate members one human contact detail (eg: email or tel)
 * 
 * Principle of security :
 *      1/ No data can be obtain without sharing one's own 
 *      2/ Data can only be obtained from matching entities in my trees => users can only get as much as they share !
 *      3/ Data remains crypted across the Internet
 *      4/ No gedcom data is stored on the ancestris centralised server : server only has members "access information" and public crypting key
 *      5/ Members do see connected members' pseudos (otherwise would not know when to run their search and would not be human!)
 *      5/ Ancestris friends do not get somebody else data without prior owner's authorisation
 *      6/ Only ancestris applications know who's who and manipulate the data until explicit authorisation from owners
 *      7/ Shared gedcom files have to be opened in Ancestris
 *             
 * Principle of usage :
 *      1/ Make interaction as simple as possible : do not explicit all steps and unecessary ones, make it as automated as possible
 *      2/ At every new entering member or tree shared, previous requests run again on added data (do no implement this yet)
 * 
 * 
 * Note : depending on number of shared gedcoms across the Ancestris community, performance might justify to optimise/change the concept and the architecture
 *
 * @author frederic
 */
public class TreeSharing {
    
    private final boolean EXACT_MATCH = true;
    private final boolean APPROXIMATE_MATCH = false;
    
    private Timer timer;
    private List<Gedcom> sharedGedcoms;     // List of shared gedcoms
    private boolean respectPrivacy;         // Same privacy level for all gedcoms (simplicity)
    private String[] allowedFriends;        // Same friends for all gedcoms (simplicity)
    private String myName = "Frederic";
    private String myAccess = "URLxyz";
    
    private Comm commHandler;
    
    
    
    
    /**
     * 
     * @param sharedGedcom
     * @return 
     */
    private boolean startSharingGedcom(Gedcom sharedGedcom) {
        this.sharedGedcoms.add(sharedGedcom);
        return true;
    }
    
      /**
     * 
     * @param gedcom
     * @return 
     */
    private boolean stopSharingGedcom(Gedcom gedcom) {
        
        // If gedcom is not shared, return
        if (!sharedGedcoms.contains(gedcom)) {
           return true; 
        }
        
        // If only one gedcom is shared, stop sharing all gedcoms
        if (sharedGedcoms.size() == 1) {
//            return stopSharingGedcoms();
        }
        
        // Otherwise just remove this gedcom from shared list
        sharedGedcoms.remove(gedcom);
        
        return true;
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * 
     * @return 
     */
    private List<Entity> getSharedEntitiesFromSharedGedcoms() {
        List<Entity> mySharedEntities = new LinkedList<Entity>();
        
        // Loop my shared gedcom list
        
        // For each of them, add list of entities non vetoed by sharing filter
        // TODO : only share Indi and Fam entities which respect privacy indicator
        
        // Return list
        
        return mySharedEntities;
    }

    
    
    
    
    

    
    
    
    /**
     * Ask each ancestris friend running program for the list of shared [gedcom x entities(INDI, FAM)] 
     * 
     * @param mySharedGedcoms : the gedcom files I am sharing
     * @param friends : list of sharing friends
     * @return 
     */
    private Map<Entity, FriendGedcomEntity> getAllMatchingEntities(Gedcom[] sharedGedcoms, List<AncestrisFriend> friends) {

        List<Entity> mySharedEntities = getSharedEntitiesFromSharedGedcoms();
        
        Map<Entity, FriendGedcomEntity> foundMatchingEntities = new HashMap<Entity, FriendGedcomEntity>();
        boolean matchCriteria = EXACT_MATCH;
                
        for (AncestrisFriend friend : friends) {
            if (commHandler.connectToAncestrisFriend(friend)) {
                foundMatchingEntities.putAll(getMatchingEntities(mySharedEntities, friend, matchCriteria));
            }
        }
        
        
        return foundMatchingEntities;
    }

    
    
    
    
    /**
     * Get matching entities for a given friend
     * 
     * @param myEntities
     * @param friend
     * @return 
     */
    private Map<Entity, FriendGedcomEntity> getMatchingEntities(List<Entity> mySharedEntities, AncestrisFriend friend, boolean exactMatch) {
        
        Map<Entity, FriendGedcomEntity> foundMatchingEntities = new HashMap<Entity, FriendGedcomEntity>();
        
        List<FriendGedcomEntity> collectedEntities = getSharedEntitiesFromFriend(friend);
        for (Entity myEntity : mySharedEntities) {
            for (FriendGedcomEntity collectedEntity : collectedEntities) {
                if ((myEntity instanceof Indi) && (collectedEntity.isIndi())) {
                    Indi myIndi = (Indi) myEntity;
                    Indi collectedIndi = collectedEntity.getIndi();
                    // same name ?
                    if (isSameIndividual(myIndi, collectedIndi, exactMatch)) {
                        // we have a match
                        foundMatchingEntities.put(myEntity, collectedEntity);
                    }
                }
                
                if ((myEntity instanceof Fam) && (collectedEntity.isFam())) {
                    Fam myFam = (Fam) myEntity;
                    Fam collectedFam = collectedEntity.getFam();
                    // same husband ?
                    if (isSameIndividual(myFam.getHusband(), collectedFam.getHusband(), exactMatch) && (isSameIndividual(myFam.getWife(), collectedFam.getWife(), exactMatch))) {
                        // we have a match
                        foundMatchingEntities.put(myEntity, collectedEntity);
                    }
                }
            }
        }
        return foundMatchingEntities;
    }
    
    
    
    
    
    /**
     * Collect all shared entities for a given friend, regardless of whether they are matching or not. Will not be known to user yet.
     * Ask the ancestris friend running program for the list of shared [gedcom x entities(INDI, FAM)] 
     *      / limited to owner's criteria (duration, selected members, privacy) 
     *      / one gedcom at a time, crypted and zipped
     *      / until all data collected on my ancestris
     * These criteria should be unknown from requesting member
     * 
     * @param friend
     * @return 
     */
    private List<FriendGedcomEntity> getSharedEntitiesFromFriend(AncestrisFriend friend) {
        return null;
    }

    
    
    /**
     * 
     * @return 
     */
    public List<FriendGedcomEntity> provideSharedEntitiesToFriend() {
        
        // Check if friend is in allowed list
        
        // Build return list
        List<FriendGedcomEntity> providedEntities = new LinkedList<FriendGedcomEntity>();
        List<Entity> sharedEntities = getSharedEntitiesFromSharedGedcoms();
        for (Entity entity : sharedEntities) {
            providedEntities.add(new FriendGedcomEntity(new AncestrisFriend(myName, myAccess), entity.getGedcom(), entity));
        }
        
        return providedEntities;
    }


    
    
    /**
     * Test if two individuals match (TODO: exactMatch false to be implemented !)
     * 
     * @param myIndi
     * @param collectedIndi
     * @param exactMatch
     * @return 
     */
    private boolean isSameIndividual(Indi myIndi, Indi collectedIndi, boolean exactMatch) {
        if (!myIndi.getLastName().equals(collectedIndi.getLastName()) && exactMatch) {
            return false;
        }
        if (!myIndi.getFirstName().equals(collectedIndi.getFirstName()) && exactMatch) {
            return false;
        }
        return (myIndi.getBirthDate().compareTo(collectedIndi.getBirthDate()) == 0 && exactMatch);
    }

    
    
    
      
  

}
