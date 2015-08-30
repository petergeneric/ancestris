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
import ancestris.modules.treesharing.communication.GedcomFam;
import ancestris.modules.treesharing.communication.GedcomIndi;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanel;
import ancestris.modules.treesharing.panels.AncestrisFriend;
import ancestris.modules.treesharing.panels.FriendGedcomEntity;
import ancestris.modules.treesharing.panels.SharedGedcom;
import genj.gedcom.Gedcom;
import java.util.ArrayList;
import java.util.HashSet;
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
            getAllMatchingEntities(owner.getSharedGedcoms(), owner.getAncestrisMembers());
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
    private void getAllMatchingEntities(List<SharedGedcom> sharedGedcoms, List<AncestrisMember> ancestrisMembers) {

        // Initialize variables
        String matchType = NbPreferences.forModule(TreeSharingOptionsPanel.class).get("MatchingType", TreeSharingOptionsPanel.MATCHING_TYPES[0]);
        List<AncestrisMember> copyOfAncestrisMembers = (List) ((ArrayList) ancestrisMembers).clone(); // Copy ancestris members to avoid concurrent access to the list while using it
        Set<String> myIndiLastnames = owner.getCommHandler().getMySharedIndiLastnames(sharedGedcoms);
        Set<String> myFamLastnames = owner.getCommHandler().getMySharedFamLastnames(sharedGedcoms);  
        
        
        // Loop on all members
        for (AncestrisMember member : copyOfAncestrisMembers) {
            
            // Skip if member not allowed or if it is myself
            if (!member.isAllowed() || member.getMemberName().equals(owner.getPreferredPseudo())) {
                continue;
            }

            
            // A. Individuals
            // Phase 1 - Get all lastnames from member for all its shared gedcoms at the same time and identify commons ones with mine
            Set<String> memberIndiLastnames = owner.getCommHandler().getSharedIndiLastnamesFromMember(member);
            if (memberIndiLastnames == null || memberIndiLastnames.isEmpty()) {
                continue;
            }
            Set<String> commonIndiLastnames = getCommonItems(myIndiLastnames, new HashSet<String>(memberIndiLastnames));
            if (commonIndiLastnames == null || commonIndiLastnames.isEmpty()) {
                continue;
            }
            
            // Phase 2 - Get individual details for common lastnames from member and identify matching ones according to matching type
            Set<GedcomIndi> memberGedcomIndis = owner.getCommHandler().getGedcomIndisFromMember(member, commonIndiLastnames);
            if (memberGedcomIndis != null && !memberGedcomIndis.isEmpty()) {
                // Phase 3 - Identify and create/update matches
                Set<GedcomIndi> myGedcomIndis = owner.getCommHandler().getMySharedGedcomIndis(sharedGedcoms, commonIndiLastnames);
                addCommonIndis(sharedGedcoms, myGedcomIndis, memberGedcomIndis, matchType, member);    // create/update matches and friends
            }

            
            // B. Families
            // Phase 1 - Get all fams lastnames from member for all its shared gedcoms at the same time and identify commons ones with mine
            Set<String> memberFamLastnames = owner.getCommHandler().getSharedFamLastnamesFromMember(member);
            if (memberFamLastnames == null || memberFamLastnames.isEmpty()) {
                continue;
            }
            Set<String> commonFamLastnames = getCommonItems(myFamLastnames, new HashSet<String>(memberFamLastnames));
            if (commonFamLastnames == null || commonFamLastnames.isEmpty()) {
                continue;
            }
            
            // Phase 2 - Get individual details for common fam lastnames from member and identify matching ones according to matching type
            Set<GedcomFam> memberGedcomFams = owner.getCommHandler().getGedcomFamsFromMember(member, commonFamLastnames);
            if (memberGedcomFams != null && !memberGedcomFams.isEmpty()) {
                // Phase 3 - Identify and create/update matches
                Set<GedcomFam> myGedcomFams = owner.getCommHandler().getMySharedGedcomFams(sharedGedcoms, commonFamLastnames);
                addCommonFams(sharedGedcoms, myGedcomFams, memberGedcomFams, matchType, member);    // create/update matches and friends
            }
            
        } // endfor members

        
        stopGracefully();
    }
    
    
    
    
    
    
    
    
    
    
    private Set<String> getCommonItems(Set<String> myIndiLastnames, Set<String> memberIndiLastnames) {
        memberIndiLastnames.retainAll(myIndiLastnames); 
        return new HashSet<String>(memberIndiLastnames);
    }

    private void addCommonIndis(List<SharedGedcom> sharedGedcoms, Set<GedcomIndi> myGedcomIndis, Set<GedcomIndi> memberGedcomIndis, String matchType, AncestrisMember member) {
        
        // Counters
        AncestrisFriend friend = null;
        int iIndis = 0;

        // Loop on each of *my* shared gedcoms
        for (SharedGedcom sharedGedcom : sharedGedcoms) {

            // Loop on all *my* shared entities
            for (GedcomIndi myGedcomIndi : myGedcomIndis) {

                // Loop all *member* entities
                friend = null;
                iIndis = 0;
                for (GedcomIndi memberGedcomIndi : memberGedcomIndis) {
                    iIndis++;
                    if (isSameIndividual(myGedcomIndi, memberGedcomIndi, matchType)) { // we have a match
                        friend = owner.createMatch(sharedGedcom, 
                                sharedGedcom.getGedcom().getEntity(myGedcomIndi.entityID), 
                                new FriendGedcomEntity(member.getMemberName(), memberGedcomIndi.gedcomName, memberGedcomIndi.entityID), 
                                member);
                    }
                    continue;
                } // endfor memberEntities
                if (friend != null) {
                    friend.updateTotals(iIndis, 0);
                }
            } // endfor myEntities
        } // endfor myGedcoms
        
    }

    private void addCommonFams(List<SharedGedcom> sharedGedcoms, Set<GedcomFam> myGedcomFams, Set<GedcomFam> memberGedcomFams, String matchType, AncestrisMember member) {
        
        // Counters
        AncestrisFriend friend = null;
        int iIndis = 0;

        // Loop on each of *my* shared gedcoms
        for (SharedGedcom sharedGedcom : sharedGedcoms) {

            // Loop on all *my* shared entities
            for (GedcomFam myGedcomFam : myGedcomFams) {

                // Loop all *member* entities
                friend = null;
                iIndis = 0;
                for (GedcomFam memberGedcomFam : memberGedcomFams) {
                    iIndis++;
                    if (isSameFamily(myGedcomFam, memberGedcomFam, matchType)) { // we have a match
                        friend = owner.createMatch(sharedGedcom, 
                                sharedGedcom.getGedcom().getEntity(myGedcomFam.entityID), 
                                new FriendGedcomEntity(member.getMemberName(), memberGedcomFam.gedcomName, memberGedcomFam.entityID), 
                                member);
                    }
                    continue;
                } // endfor memberEntities
                if (friend != null) {
                    friend.updateTotals(iIndis, 0);
                }
            } // endfor myEntities
        } // endfor myGedcoms
        
    }

    
    


    
    
    /**
     * Match functions for indi and fam:
     *  TODO: enrich match function
     *  Reminder : purpose is not to garantee a match but to suggest to the user a possible match
     *  Later introduce colors : green for exact match on all criteria, orange on some only
     */
    private boolean isSameIndividual(GedcomIndi myIndi, GedcomIndi friendIndi, String matchType) {
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.indiLastName.equals(friendIndi.indiLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.indiLastName.equals(friendIndi.indiFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0])) {
            return true;
        }
        return false;
    }

    private boolean isSameFamily(GedcomFam myFamily, GedcomFam friendFam, String matchType) {
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && myFamily.husbLastName != null && !myFamily.husbLastName.equals(friendFam.husbLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && myFamily.husbFirstName != null && !myFamily.husbFirstName.equals(friendFam.husbFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && myFamily.wifeLastName != null && !myFamily.wifeLastName.equals(friendFam.wifeLastName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && myFamily.wifeFirstName != null && !myFamily.wifeFirstName.equals(friendFam.wifeFirstName)) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0])) {
            return true;
        }
        return false;
    }
    
    
}

