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
import ancestris.modules.treesharing.communication.GedcomNumbers;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanel;
import ancestris.modules.treesharing.panels.AncestrisFriend;
import ancestris.modules.treesharing.panels.FriendGedcomEntity;
import ancestris.modules.treesharing.panels.SharedGedcom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        owner.displaySearchedMember("");
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

//        if (true) {
//            Set<MatchData> list = new HashSet<MatchData>();
//            
//            Entity indi = sharedGedcoms.get(0).getGedcom().getFirstEntity(Gedcom.INDI);
//            FriendGedcomEntity fge = new FriendGedcomEntity("Ami", "francois.ged", "I0001");
//            MatchData md = new MatchData(indi, fge, 1);
//            list.add(md);
//            
//            indi = sharedGedcoms.get(0).getGedcom().getEntity("I00002");
//            fge = new FriendGedcomEntity("Ami", "francois.ged", "I0002");
//            md = new MatchData(indi, fge, 2);
//            list.add(md);
//            
//            indi = sharedGedcoms.get(0).getGedcom().getEntity("I00003");
//            fge = new FriendGedcomEntity("Ami", "francois.ged", "I0002");
//            md = new MatchData(indi, fge, 3);
//            list.add(md);
//            
//            DialogManager.create("titre", new ListEntitiesPanel("allgedcoms", "nom", list)).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
//            stopGracefully();
//            return;
//        }
        
        
        // Initialize variables
        AncestrisFriend friend = null;
        boolean profileExchanged = false;
        int matchType = TreeSharingOptionsPanel.getMatchType();
        List<AncestrisMember> copyOfAncestrisMembers = (List) ((ArrayList) ancestrisMembers).clone(); // Copy ancestris members to avoid concurrent access to the list while using it
        Set<String> myIndiLastnames = owner.getCommHandler().getMySharedIndiLastnames(sharedGedcoms);
        Set<String> myFamLastnames = owner.getCommHandler().getMySharedFamLastnames(sharedGedcoms);  
        
        // Loop on all members
        for (AncestrisMember member : copyOfAncestrisMembers) {
            
            // Skip if member not allowed or if it is myself
            if (!member.isAllowed() || member.getMemberName().equals(owner.getPreferredPseudo())) {
                continue;
            }

            // Indicate which member is being analysed
            owner.displaySearchedMember(member.getMemberName());
            
            
            // Ask for stats first (nb of indis and nb of families)
            owner.getCommHandler().setCommunicationInProgress(false);
            GedcomNumbers gedcomNumbers = owner.getCommHandler().getNbOfEntities(member);
            if (gedcomNumbers == null) {
                continue;
            }
            owner.getCommHandler().setCommunicationInProgress(true);

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
                if (myGedcomIndis != null && !myGedcomIndis.isEmpty()) {
                    friend = addCommonIndis(sharedGedcoms, myGedcomIndis, memberGedcomIndis, matchType, member);    // create/update matches and friends if common are found
                    if (friend != null) {
                        friend.setTotals(gedcomNumbers.nbIndis, gedcomNumbers.nbFams);      // set numbers
                        if (!profileExchanged) {
                            friend.setProfile(owner.getCommHandler().getProfileMember(member), owner.getMyProfile());  // get member profile and set it for friend
                            owner.getCommHandler().thankMember(member);   // thank and give my profile
                            profileExchanged = true;
                        }
                        friend = null;
                    }
                }
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
                if (myGedcomFams != null && !myGedcomFams.isEmpty()) {
                    friend = addCommonFams(sharedGedcoms, myGedcomFams, memberGedcomFams, matchType, member);    // create/update matches and friends
                    if (friend != null) {
                        friend.setTotals(gedcomNumbers.nbIndis, gedcomNumbers.nbFams);      // set numbers
                        if (!profileExchanged) {
                            friend.setProfile(owner.getCommHandler().getProfileMember(member), owner.getMyProfile());  // get member profile and set it for friend
                            owner.getCommHandler().thankMember(member);   // thank and give my profile
                            profileExchanged = true;
                        }
                        friend = null;
                    }
                }
                
            }

            // Thank you, exchange profiles and update friend
            if (friend != null) {
                friend.setTotals(gedcomNumbers.nbIndis, gedcomNumbers.nbFams);      // set numbers
                friend.setProfile(owner.getCommHandler().getProfileMember(member), owner.getMyProfile());  // get member profile and set it for friend
                friend = null;
                owner.getCommHandler().thankMember(member);   // thank and give my profile
            }
            
            
        } // endfor members

        owner.getCommHandler().setCommunicationInProgress(false);
        stopGracefully();
    }
    
    
    
    
    
    
    
    
    
    
    private Set<String> getCommonItems(Set<String> myIndiLastnames, Set<String> memberIndiLastnames) {
        memberIndiLastnames.retainAll(myIndiLastnames); 
        return new HashSet<String>(memberIndiLastnames);
    }

    private AncestrisFriend addCommonIndis(List<SharedGedcom> sharedGedcoms, Set<GedcomIndi> myGedcomIndis, Set<GedcomIndi> memberGedcomIndis, int matchType, AncestrisMember member) {
        
        AncestrisFriend friend = null;
        int retMatch;

        // Loop on each of *my* shared gedcoms
        for (SharedGedcom sharedGedcom : sharedGedcoms) {

            // Loop on all *my* shared entities
            for (GedcomIndi myGedcomIndi : myGedcomIndis) {

                // Exclude all entities not belonging to sharedGedcom
                if (!myGedcomIndi.gedcomName.equals(sharedGedcom.getGedcom().getName())) {
                    continue;
                }
                
                // Loop all *member* entities
                for (GedcomIndi memberGedcomIndi : memberGedcomIndis) {
                    retMatch = isSameIndividual(myGedcomIndi, memberGedcomIndi, matchType);
                    if (retMatch != TreeSharingOptionsPanel.NO_MATCH) { // we have a match
                        friend = owner.createMatch(sharedGedcom, 
                                sharedGedcom.getGedcom().getEntity(myGedcomIndi.entityID), 
                                new FriendGedcomEntity(member.getMemberName(), memberGedcomIndi.gedcomName, memberGedcomIndi.entityID), 
                                member, retMatch);
                    }
                    continue;
                } // endfor memberEntities
            } // endfor myEntities
        } // endfor myGedcoms
        
        return friend;
    }

    
    
    
    private AncestrisFriend addCommonFams(List<SharedGedcom> sharedGedcoms, Set<GedcomFam> myGedcomFams, Set<GedcomFam> memberGedcomFams, int matchType, AncestrisMember member) {
        
        AncestrisFriend friend = null;
        int retMatch;

        // Loop on each of *my* shared gedcoms
        for (SharedGedcom sharedGedcom : sharedGedcoms) {

            // Loop on all *my* shared entities
            for (GedcomFam myGedcomFam : myGedcomFams) {

                // Exclude all entities not belonging to sharedGedcom
                if (!myGedcomFam.gedcomName.equals(sharedGedcom.getGedcom().getName())) {
                    continue;
                }
                
                // Loop all *member* entities
                for (GedcomFam memberGedcomFam : memberGedcomFams) {
                    retMatch = isSameFamily(myGedcomFam, memberGedcomFam, matchType);
                    if (retMatch != TreeSharingOptionsPanel.NO_MATCH) { // we have a match
                        friend = owner.createMatch(sharedGedcom, 
                                sharedGedcom.getGedcom().getEntity(myGedcomFam.entityID), 
                                new FriendGedcomEntity(member.getMemberName(), memberGedcomFam.gedcomName, memberGedcomFam.entityID), 
                                member, retMatch);
                    }
                    continue;
                } // endfor memberEntities
            } // endfor myEntities
        } // endfor myGedcoms
        
        return friend;
    }

    
    


    
    
    /**
     * Match functions for indi and fam:
     * - Purpose is not to garantee a match but to suggest to the user a possible match
     * - Compared data have to rely on elements most commonly found in any genealogy : name, place, dates
     * - Exact match will be as exact as:
     *      - individuals : exact lastname, exact firstname, exact birth city/country, exact birth year, and if available, same for death
     *      - families : same for both husband and wife
     * - Flash match will be like in a flash report ("2 people have a chance to have been at the same place at the same time"):
     *      - individuals : exact lastname, any first name (not compared), across all events : one city/country in common and overlapping time period (min/max do cross)
     *      - families : same for both husband and wife
     */
    private int isSameIndividual(GedcomIndi myIndi, GedcomIndi friendIndi, int matchType) {

        // make it easier for the formulas for myIndi (A)
        String Aln = myIndi.indiLastName;
        String Afn = myIndi.indiFirstName;
        String Apl1 = myIndi.indiBirthPlace;
        String Apl2 = myIndi.indiDeathPlace;
        int Ayr1 = Integer.valueOf(myIndi.indiBirthDate);
        int Ayr2 = Integer.valueOf(myIndi.indiDeathDate);

        // make it easier for the formulas for friendIndi (B)
        String Bln = friendIndi.indiLastName;
        String Bfn = friendIndi.indiFirstName;
        String Bpl1 = friendIndi.indiBirthPlace;
        String Bpl2 = friendIndi.indiDeathPlace;
        int Byr1 = Integer.valueOf(friendIndi.indiBirthDate);
        int Byr2 = Integer.valueOf(friendIndi.indiDeathDate);

        // Formulas : Detect exact match first
        if (Aln.equals(Bln) && Afn.equals(Bfn) && Apl1.equals(Bpl1) && Ayr1 == Byr1 && Apl2.equals(Bpl2) && Ayr2 == Byr2) {
            return TreeSharingOptionsPanel.EXACT_MATCH;
        }
        
        // Formulas : Detect flash match
        if (matchType >= TreeSharingOptionsPanel.FLASH_MATCH) {
            if (Aln.equals(Bln) // if same lastname, 
                    && (Apl1.length() > 1 || Apl2.length() > 1 || Bpl1.length() > 1 || Bpl2.length() > 1) // ...at least one place indicated across both, 
                    && (Ayr1 > 0 || Ayr2 > 0) && (Byr1 > 0 || Byr2 > 0)) { // ...and at least one year with non zero for each (year can always be estimated and therefore filled in by user, places cannot)
                if (Apl1.equals(Bpl1) || Apl1.equals(Bpl2) || Apl2.equals(Bpl1) || Apl2.equals(Bpl2)) {    // a place in common, either birth or death
                    if ((Ayr1 <= Byr1 && Byr1 <= Ayr2) || (Byr1 <= Ayr1 && Ayr1 <= Byr2)) {  // dates overlap
                        return TreeSharingOptionsPanel.FLASH_MATCH;
                    }
                }
            }
        }
        
        // Formulas : Detect loose match (just lastname and first name)
        if (matchType >= TreeSharingOptionsPanel.LOOSE_MATCH) {
            if (Aln.equals(Bln) && Afn.equals(Bfn)) {
                return TreeSharingOptionsPanel.LOOSE_MATCH;
            }
        }

        // no match
        return TreeSharingOptionsPanel.NO_MATCH;
        
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && !myIndi.indiLastName.equals(friendIndi.indiLastName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && !myIndi.indiFirstName.equals(friendIndi.indiFirstName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0])) {
//            return TreeSharingOptionsPanel.EXACT_MATCH;
//        }
//        return TreeSharingOptionsPanel.NO_MATCH;
    }

    
    
    private int isSameFamily(GedcomFam myFamily, GedcomFam friendFam, int matchType) {

        // make it easier for the formulas for myFamily
        String Ahln = myFamily.husbLastName;
        String Ahfn = myFamily.husbFirstName;
        String Apl1 = myFamily.husbBirthPlace;
        String Apl2 = myFamily.husbDeathPlace;
        int Ayr1 = Integer.valueOf(myFamily.husbBirthDate);
        int Ayr2 = Integer.valueOf(myFamily.husbDeathDate);
        String Awln = myFamily.wifeLastName;
        String Awfn = myFamily.wifeFirstName;
        String Apl3 = myFamily.wifeBirthPlace;
        String Apl4 = myFamily.wifeDeathPlace;
        int Ayr3 = Integer.valueOf(myFamily.wifeBirthDate);
        int Ayr4 = Integer.valueOf(myFamily.wifeDeathDate);
        String Apl5 = myFamily.famMarrPlace;
        int Ayr5 = Integer.valueOf(myFamily.famMarrDate);

        // make it easier for the formulas for friendFam
        String Bhln = friendFam.husbLastName;
        String Bhfn = friendFam.husbFirstName;
        String Bpl1 = friendFam.husbBirthPlace;
        String Bpl2 = friendFam.husbDeathPlace;
        int Byr1 = Integer.valueOf(friendFam.husbBirthDate);
        int Byr2 = Integer.valueOf(friendFam.husbDeathDate);
        String Bwln = friendFam.wifeLastName;
        String Bwfn = friendFam.wifeFirstName;
        String Bpl3 = friendFam.wifeBirthPlace;
        String Bpl4 = friendFam.wifeDeathPlace;
        int Byr3 = Integer.valueOf(friendFam.wifeBirthDate);
        int Byr4 = Integer.valueOf(friendFam.wifeDeathDate);
        String Bpl5 = friendFam.famMarrPlace;
        int Byr5 = Integer.valueOf(friendFam.famMarrDate);

        // Formulas : Detect exact match first
        if (Ahln.equals(Bhln) && Ahfn.equals(Bhfn) && Apl1.equals(Bpl1) && Ayr1 == Byr1 && Apl2.equals(Bpl2) && Ayr2 == Byr2
                && Awln.equals(Bwln) && Awfn.equals(Bwfn) && Apl3.equals(Bpl3) && Ayr3 == Byr3 && Apl4.equals(Bpl4) && Ayr4 == Byr4
                && Apl5.equals(Bpl5) && Ayr5 == Byr5) {
            return TreeSharingOptionsPanel.EXACT_MATCH;
        }

        // Formulas : Detect flash match
        if (matchType >= TreeSharingOptionsPanel.FLASH_MATCH) {  // [0] is flash match
            
            // Prepare variables
            int AyrMin = getNonZeroMin(Ayr1, Ayr2, Ayr3, Ayr4, Ayr5);
            int AyrMax = getNonZeroMax(Ayr1, Ayr2, Ayr3, Ayr4, Ayr5);
            int ByrMin = getNonZeroMin(Byr1, Byr2, Byr3, Byr4, Byr5);
            int ByrMax = getNonZeroMax(Byr1, Byr2, Byr3, Byr4, Byr5);
            Set<String> Apls = getNonZeroPlaces(Apl1, Apl2, Apl3, Apl4, Apl5);
            Set<String> Bpls = getNonZeroPlaces(Bpl1, Bpl2, Bpl3, Bpl4, Bpl5);
            
            // Now do the test
            if (Ahln.equals(Bhln) && Awln.equals(Bwln) // if same lastname for each spouse, 
            && Apls.size() > 0 && Bpls.size() > 0 && isPlaceCommon(Apls, Bpls)) { // ...if at least one place indicated on each side and one in common (places overlap !)
                if ((AyrMin <= ByrMin && ByrMin <= AyrMax) || (ByrMin <= AyrMin && AyrMin <= ByrMax)) {  // if dates overlap
                    return TreeSharingOptionsPanel.FLASH_MATCH;
                }
            }
        }

        // Formulas : Detect loose match (just lastname and first name)
        if (matchType >= TreeSharingOptionsPanel.LOOSE_MATCH) {
            if (Ahln.equals(Bhln) && Ahfn.equals(Bhfn) && Awln.equals(Bwln) && Awfn.equals(Bwfn)) {
                return TreeSharingOptionsPanel.LOOSE_MATCH;
            }
        }

        // No match
        return TreeSharingOptionsPanel.NO_MATCH;

        
        
        
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && myFamily.husbLastName != null && !myFamily.husbLastName.equals(friendFam.husbLastName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && myFamily.husbFirstName != null && !myFamily.husbFirstName.equals(friendFam.husbFirstName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && myFamily.wifeLastName != null && !myFamily.wifeLastName.equals(friendFam.wifeLastName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0]) && myFamily.wifeFirstName != null && !myFamily.wifeFirstName.equals(friendFam.wifeFirstName)) {
//            return TreeSharingOptionsPanel.NO_MATCH;
//        }
//        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_MENU[0])) {
//            return TreeSharingOptionsPanel.EXACT_MATCH;
//        }
//        return TreeSharingOptionsPanel.NO_MATCH;
    }
    
    
    
    

    private int getNonZeroMin(int Ayr1, int Ayr2, int Ayr3, int Ayr4, int Ayr5) {

        int ret = Integer.MAX_VALUE;
        
        if (Ayr1 != 0) {
           ret = Math.min(ret, Ayr1);
        }
        if (Ayr2 != 0) {
           ret = Math.min(ret, Ayr2);
        }
        if (Ayr3 != 0) {
           ret = Math.min(ret, Ayr3);
        }
        if (Ayr4 != 0) {
           ret = Math.min(ret, Ayr4);
        }
        if (Ayr5 != 0) {
           ret = Math.min(ret, Ayr5);
        }
        return ret;
    }

    private int getNonZeroMax(int Ayr1, int Ayr2, int Ayr3, int Ayr4, int Ayr5) {

        int ret = Integer.MIN_VALUE;
        
        if (Ayr1 != 0) {
           ret = Math.max(ret, Ayr1);
        }
        if (Ayr2 != 0) {
           ret = Math.max(ret, Ayr2);
        }
        if (Ayr3 != 0) {
           ret = Math.max(ret, Ayr3);
        }
        if (Ayr4 != 0) {
           ret = Math.max(ret, Ayr4);
        }
        if (Ayr5 != 0) {
           ret = Math.max(ret, Ayr5);
        }
        return ret;
    }

    private Set<String> getNonZeroPlaces(String Apl1, String Apl2, String Apl3, String Apl4, String Apl5) {
        
        Set<String> ret = new HashSet<String>();
        if (Apl1.length()>1) {
            ret.add(Apl1);
        }
        if (Apl2.length()>1) {
            ret.add(Apl1);
        }
        if (Apl3.length()>1) {
            ret.add(Apl1);
        }
        if (Apl4.length()>1) {
            ret.add(Apl1);
        }
        if (Apl5.length()>1) {
            ret.add(Apl1);
        }
        return ret;
    }

    private boolean isPlaceCommon(Set<String> Apls, Set<String> Bpls) {
        
        for (String Aplace : Apls) {
            if (Bpls.contains(Aplace)) {
                return true;
            }
        }
        return false;
    }
    
    
}

