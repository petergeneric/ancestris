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
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Exceptions;
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

        // Get matching type from preferences
        String matchType = NbPreferences.forModule(TreeSharingOptionsPanel.class).get("MatchingType", TreeSharingOptionsPanel.MATCHING_TYPES[0]);
        
        // Loop on all members
        for (AncestrisMember member : ancestrisMembers) {
            
            // Skip if member not allowed
            if (!member.isAllowed()) {
                continue;
            }
            
            // Connect to member, otherwise skip
            if (!commHandler.connectToAncestrisFriend(member)) {
                continue;
            }
            
            // Get all shared entities from member for all its shared gedcoms at the same time
            List<FriendGedcomEntity> memberEntities = getSharedEntitiesFromMember(member);
            
            // Loop on each of *my* shared gedcoms
            for (SharedGedcom sharedGedcom : sharedGedcoms) {
                
                // Get all shared (public) entities from my sharedGedcom
                List<Entity> myEntities = sharedGedcom.getAllPublicEntities();
                
                // Loop on all my shared entities
                for (Entity myEntity : myEntities) {
                    
                    // Loop all member shared entities
                    for (FriendGedcomEntity memberEntity : memberEntities) {
                        if ((myEntity instanceof Indi) && (memberEntity.isIndi())) {
                            Indi myIndi = (Indi) myEntity;
                            Indi memberIndi = memberEntity.getIndi();
                            // same individual
                            if (isSameIndividual(myIndi, memberIndi, matchType)) { // we have a match
                                owner.createMatch(sharedGedcom, myEntity, memberEntity, Gedcom.INDI);
                            }
                            continue;
                        }

                        if ((myEntity instanceof Fam) && (memberEntity.isFam())) {
                            Fam myFam = (Fam) myEntity;
                            Fam memberFam = memberEntity.getFam();
                            // same husband and same wife ?
                            if (isSameIndividual(myFam.getHusband(), memberFam.getHusband(), matchType) && (isSameIndividual(myFam.getWife(), memberFam.getWife(), matchType))) { // we have a match
                                owner.createMatch(sharedGedcom, myEntity, memberEntity, Gedcom.FAM);
                            }
                        }
                    } // endfor memberEntities
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
        
        
        // dummy code for now, until communication gets done
        List<FriendGedcomEntity> allShared = new LinkedList<FriendGedcomEntity>();
        
        Gedcom gedcom = new Gedcom();
        gedcom.setName("other.ged");
        try {
            Indi indi1 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi2 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi3 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi4 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi5 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi6 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi7 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi8 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi9 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi10 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi11 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi12 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi13 = (Indi) gedcom.createEntity(Gedcom.INDI);
            Indi indi14 = (Indi) gedcom.createEntity(Gedcom.INDI);

            indi1.setName("Frédéric", "LAPEYRE");
            indi2.setName("Anne Marie Sophie", "LAPEYRE");
            indi3.setName("Marie Anne Rosalie", "TROUILLET");
            indi4.setName("Jean Georges", "RAUCH");
            indi5.setName("François Henri", "RIOU");
            indi6.setName("Elisabeth Victoire", "ROBERT");
            indi7.setName("Jacques Léon Paulin", "ROUQUETTE");
            indi8.setName("Georges", "SCHLUCK");
            indi9.setName("marie", "KAYE");
            indi10.setName("Alexis", "GUILLOT");
            indi11.setName("Victor Jean Marie Joseph", "de LEUZE");
            indi12.setName("Valentine", "RENAUD");
            indi13.setName("Marie Magdeleine Clothilde", "CHAVINIER");
            indi14.setName("Napoléon", "BONAPARTE");

            allShared.add(new FriendGedcomEntity(new AncestrisFriend("François", "xxxx"), gedcom, indi1));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Daniel", "xxxx"), gedcom, indi2));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Daniel", "xxxx"), gedcom, indi3));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Daniel", "xxxx"), gedcom, indi4));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Yannick", "xxxx"), gedcom, indi5));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Yannick", "xxxx"), gedcom, indi6));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Dominique", "xxxx"), gedcom, indi7));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Valérie", "xxxx"), gedcom, indi8));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Daniel", "xxxx"), gedcom, indi9));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Jeannot", "xxxx"), gedcom, indi10));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Frederic", "xxxx"), gedcom, indi11));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Daniel", "xxxx"), gedcom, indi12));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Jeannot", "xxxx"), gedcom, indi13));
            allShared.add(new FriendGedcomEntity(new AncestrisFriend("Raymond", "xxxx"), gedcom, indi14));

        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Dummy code to test : Extract entities related to member only (as if I had found them through the communication to that member)
        List<FriendGedcomEntity> ret = new LinkedList<FriendGedcomEntity>();
        for (FriendGedcomEntity element : allShared) {
            if (element.getName().equals(member.getName())) {
                ret.add(element);
            }
        }
        
        return ret;
    }

    
    
    /**
     * Test if two individuals match (TODO: exactMatch false to be implemented !)
     * 
     * @param myIndi
     * @param collectedIndi
     * @param exactMatch
     * @return 
     */
    private boolean isSameIndividual(Indi myIndi, Indi collectedIndi, String matchType) {
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.getLastName().equals(collectedIndi.getLastName())) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && !myIndi.getFirstName().equals(collectedIndi.getFirstName())) {
            return false;
        }
        if (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0])) {
            return true;
        }
        return false;
        //return (matchType.equals(TreeSharingOptionsPanel.MATCHING_TYPES[0]) && myIndi.getBirthDate().compareTo(collectedIndi.getBirthDate()) == 0);
    }

    
    
    
    
}
