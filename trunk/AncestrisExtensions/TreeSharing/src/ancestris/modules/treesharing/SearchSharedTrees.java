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
package ancestris.modules.treesharing;

import ancestris.modules.treesharing.communication.AncestrisMember;
import ancestris.modules.treesharing.communication.Comm;
import ancestris.modules.treesharing.communication.FriendGedcomEntity;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanel;
import ancestris.modules.treesharing.panels.AncestrisFriend;
import ancestris.modules.treesharing.panels.SharedGedcom;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openide.util.NbPreferences;

/**
 *
 * @author frederic
 */
public class SearchSharedTrees extends Thread {
    
    private final TreeSharingTopComponent owner;
    private volatile boolean stopRun;

    public SearchSharedTrees(TreeSharingTopComponent tstc) {
        this.owner = tstc;
    }
        
        
    @Override
    public void run() {
        stopRun = false;
        owner.setRotatingIcon(true);
        while (!stopRun) {
            owner.updateMembersList();
            getAllMatchingEntities(owner.getCommHandler(), owner.getSharedGedcoms(), owner.getAncestrisMembers());
        }
    }

    public void stopGracefully() {
        stopRun = true;
        owner.setRotatingIcon(false);
    }

    /**
     * Main search function
     * 
     * Scans all members shared entities for each gedcom shared entities
     * Each time a match is found, an update is sent back to owner to display update
     * 
     * @param sharedGedcoms
     * @param ancestrisMembers 
     */
    private void getAllMatchingEntities(Comm commHandler, List<SharedGedcom> sharedGedcoms, List<AncestrisMember> ancestrisMembers) {

        Set<String> lastnames;
        List<String> allnames;
        List<FriendGedcomEntity> entities;
                
        byte[] pack;
        
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            System.out.print("gedcom=" + sharedGedcom.getGedcom().getName() + "\n");
            lastnames = sharedGedcom.getPublicLastnames();
            pack = owner.getCommHandler().objectWrapper(lastnames);
            System.out.print("nb lastnames=" + lastnames.size() + "\n");
            System.out.print("size lastnames object to send=" + pack.length + "\n");
            System.out.print(" \n");
            allnames = sharedGedcom.getPublicNames();
            pack = owner.getCommHandler().objectWrapper(allnames);
            System.out.print("nb names=" + allnames.size() + "\n");
            System.out.print("size allnames object to send=" + pack.length + "\n");
            System.out.print(" \n");
            entities = sharedGedcom.getAllSharedEntities(owner.getRegisteredPseudo());
            pack = owner.getCommHandler().objectWrapper(entities);
            System.out.print("nb all shared entities=" + entities.size() + "\n");
            System.out.print("size all shared entities object to send=" + pack.length + "\n");
            System.out.print(" \n");
            System.out.print(" \n");
            System.out.print(" \n");
        }
        
        
        stopGracefully();
        
        
        if (true) return;
        
        // Counters
        AncestrisFriend friend = null;
        int iIndis = 0;
        int iFams = 0;
        
        // Get matching type from preferences
        String matchType = NbPreferences.forModule(TreeSharingOptionsPanel.class).get("MatchingType", TreeSharingOptionsPanel.MATCHING_TYPES[0]);
        
        // Copy ancestris members to avoid concurrent access to the list while using it
        List<AncestrisMember> copyOfAncestrisMembers = (List) ((ArrayList) ancestrisMembers).clone();
        
        // Loop on all members
        for (AncestrisMember member : copyOfAncestrisMembers) {
            
            // Skip if member not allowed or if it is myself
            if (!member.isAllowed() || member.getMemberName().equals(owner.getPreferredPseudo())) {
                continue;
            }
            
            // Get all shared entities from member for all its shared gedcoms at the same time
            List<FriendGedcomEntity> memberEntities = getSharedEntitiesFromMember(member);
            if (memberEntities == null || memberEntities.isEmpty()) {
                continue;
            }
            
            // Loop on each of *my* shared gedcoms
            for (SharedGedcom sharedGedcom : sharedGedcoms) {
                
                // Get all shared (public) entities from my sharedGedcom
                List<Entity> myEntities = sharedGedcom.getAllPublicEntities();
                
                // Loop on all my shared entities
                for (Entity myEntity : myEntities) {
                    
                    // Loop all member shared entities
                    friend = null;
                    iIndis = 0;
                    iFams = 0;
                    for (FriendGedcomEntity memberEntity : memberEntities) {
                        if ((myEntity instanceof Indi) && (memberEntity.isIndi())) {
                            Indi myIndi = (Indi) myEntity;
                            iIndis++;
                            // same individual
                            if (isSameIndividual(myIndi, memberEntity, matchType)) { // we have a match
                                friend = owner.createMatch(sharedGedcom, myEntity, memberEntity, member.getIPAddress(), member.getPortAddress(), Gedcom.INDI);
                            }
                            continue;
                        }

                        if ((myEntity instanceof Fam) && (memberEntity.isFam())) {
                            Fam myFam = (Fam) myEntity;
                            iFams++;
                            // same husband and same wife ?
                            if (isSameFamily(myFam, memberEntity, matchType)) { // we have a match
                                friend = owner.createMatch(sharedGedcom, myEntity, memberEntity, member.getIPAddress(), member.getPortAddress(), Gedcom.FAM);
                            }
                        }
                    } // endfor memberEntities
                    if (friend != null) {
                        friend.updateTotals(iIndis, iFams);
                    }
                } // endfor myEntities
            } // endfor myGedcoms
        } // endfor members
        stopGracefully();
        // end of search
    }

    
    /**
     * Collect all shared entities for a given friend, regardless of whether they are matching or not. Will not be known to user yet.
     * Ask the ancestris member running program for the list of shared [gedcom x entities(INDI, FAM)] 
     *      / limited to owner's criteria (duration, selected members, privacy) 
     *      / and collecting all shared gedcoms of member
     * These criteria should be unknown from requesting member
     * 
     * @param friend
     * @return 
     */
    private List<FriendGedcomEntity> getSharedEntitiesFromMember(AncestrisMember member) {
        
        return owner.getCommHandler().call(member);

    }

    
    
    /**
     * Test if two individuals match (TODO: exactMatch false to be implemented !)
     * 
     * @param myIndi
     * @param collectedIndi
     * @param exactMatch
     * @return 
     */
    private boolean isSameIndividual(Indi myIndi, FriendGedcomEntity friendIndi, String matchType) {
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.getLastName().equals(friendIndi.indiLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.getFirstName().equals(friendIndi.indiFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0])) {
            return true;
        }
        return false;
    }

    private boolean isSameFamily(Fam myFamily, FriendGedcomEntity friendFam, String matchType) {
        Indi husband = myFamily.getHusband();
        Indi wife = myFamily.getWife();
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && husband != null && !husband.getLastName().equals(friendFam.husbLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && husband != null && !husband.getFirstName().equals(friendFam.husbFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && wife != null && !wife.getLastName().equals(friendFam.wifeLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && wife != null && !wife.getFirstName().equals(friendFam.wifeFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0])) {
            return true;
        }
        return false;
    }

    
    
    
    
}
