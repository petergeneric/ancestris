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

package ancestris.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author frederic
 */
public class EventUsage {
    
    static public String[] otherEventsList = new String[] {
        "BAPM", "BLES", "ADOP", "FCOM", "BARM", "BASM", "CONF", "NATI", "RELI", 
        "CAST", "CHRA", "GRAD", "ORDN", "ENGA", "MARB", "MARL", "MARS", "MARC", 
        "DIVF", "DIV", "ANUL", "EMIG", "IMMI", "NATU", "TITL", "SSN", "IDNO", 
        "DSCR", "EDUC", "CENS", "PROP", "EVEN", "FACT", "PROB", "WILL", "CREM"
    };
    
    public static void init(Map<String, EventUsage> eventUsages) {
        eventUsages.put("INDI", new EventUsage( 0, "INDI"));    // The general event for an individual (/!\ this is not a gedcom event)

        eventUsages.put("BIRT", new EventUsage( 1, "INDI"));    // The event of entering into life.

        eventUsages.put("CHR",  new EventUsage( 2, "INDI"));    // The religious event (not LDS) of baptizing and/or naming a child.
        eventUsages.put("BAPM", new EventUsage( 3, "INDI"));    // The event of baptism (not LDS), performed in infancy or later
        eventUsages.put("BLES", new EventUsage( 4, "INDI"));    // A religious event of bestowing divine care or intercession. Sometimes given in connection with a naming ceremony
        
        eventUsages.put("ADOP", new EventUsage( 5, "INDI"));    // Pertaining to creation of a legally approved child-parent relationship that does not exist biologically

        eventUsages.put("FCOM", new EventUsage( 6, "INDI"));    // First communion A religious rite, the first act of sharing in the Lord's supper as part of church worship. age 9-10

        eventUsages.put("BARM", new EventUsage( 7, "INDI"));    // bar mitzvah (boy) age = 13
        eventUsages.put("BASM", new EventUsage( 8, "INDI"));    // bas mitzsvah (girl) age = 13
        eventUsages.put("CONF", new EventUsage( 9, "INDI"));    // The religious event (not LDS) of conferring the gift of the Holy Ghost and, among protestants, full church membership. age = 13

        eventUsages.put("NATI", new EventUsage(10, "INDI"));    // (nationality) The national heritage of an individual
        eventUsages.put("RELI", new EventUsage(11, "INDI"));    // (religion) A religious denomination to which a person is affiliated or for which a record applies
        eventUsages.put("CAST", new EventUsage(12, "INDI"));    // (caste) The name of an individual's rank or status in society which is sometimes based on racial or religious differences, or differences in wealth, inherited rank, profession, occupation, etc.
        
        
        eventUsages.put("CHRA", new EventUsage(13, "INDI"));    // The religious event (not LDS) of baptizing and/or naming an adult person.

        
        eventUsages.put("GRAD", new EventUsage(15, "INDI"));    // An event of awarding educational diplomas or degrees to individuals. (20-25)
        eventUsages.put("ORDN", new EventUsage(16, "INDI"));    // (ordination) A religious event of receiving authority to act in religious matters. Age 25-30

        eventUsages.put("ENGA", new EventUsage(20, "FAM"));     // An event of recording or announcing an agreement between two people to become married
        eventUsages.put("MARB", new EventUsage(21, "FAM"));     // An event of an official public notice given that two people intend to marry.
        eventUsages.put("MARL", new EventUsage(22, "FAM"));     // An event of obtaining a legal license to marry
        eventUsages.put("MARS", new EventUsage(23, "FAM"));     // An event of creating an agreement between two people contemplating marriage, at which time they agree to release or modify property rights that would otherwise arise from the marriage
        eventUsages.put("MARC", new EventUsage(24, "FAM"));     // An event of recording a formal agreement of marriage, including the prenuptial agreement in which marriage partners reach agreement about the property rights of one or both, securing property to their children.
        eventUsages.put("MARR", new EventUsage(25, "FAM"));     // A legal, common-law, or customary event of creating a family unit of a man and a woman as husband and wife.

        eventUsages.put("OCCU", new EventUsage(30, "INDI"));    // The type of work or profession of an individual. (30-40)
        eventUsages.put("RESI", new EventUsage(31, "ALL"));     // An address or place of residence that a family or individual resided

        eventUsages.put("DIVF", new EventUsage(40, "FAM"));     // An event of filing for a divorce by a spouse.
        eventUsages.put("DIV",  new EventUsage(41, "FAM"));     // An event of dissolving a marriage through civil action.
        eventUsages.put("ANUL", new EventUsage(42, "FAM"));     // Declaring a marriage void from the beginning (never existed).

        // any age
        eventUsages.put("EMIG", new EventUsage(50, "INDI"));    // An event of leaving one's homeland with the intent of residing elsewhere
        eventUsages.put("IMMI", new EventUsage(51, "INDI"));    // An event of entering into a new locality with the intent of residing there.
        eventUsages.put("NATU", new EventUsage(52, "INDI"));    // The event of obtaining citizenship.
        eventUsages.put("TITL", new EventUsage(53, "INDI"));    // A formal designation used by an individual in connection with positions of royalty or other social status
        eventUsages.put("SSN",  new EventUsage(54, "INDI"));    // A number assigned by the United States Social Security Administration. Used for tax identification purposes.
        eventUsages.put("IDNO", new EventUsage(55, "INDI"));    // A number assigned to identify a person within some significant external system
        eventUsages.put("DSCR", new EventUsage(56, "INDI"));    // The physical characteristics of a person, place, or thing
        eventUsages.put("EDUC", new EventUsage(57, "INDI"));    // Indicator of a level of education attained.
        eventUsages.put("CENS", new EventUsage(58, "ALL"));     // The event of the periodic count of the population for a designated locality, such as a national or state Census.
        eventUsages.put("PROP", new EventUsage(59, "INDI"));    // Pertaining to possessions such as real estate or other property of interest.
        eventUsages.put("EVEN", new EventUsage(60, "ALL"));     // Event - Pertaining to a noteworthy happening related to an individual, a group, or an organization
        eventUsages.put("FACT", new EventUsage(61, "INDI"));    // Fact concerning an individual, a group, or an organization
        eventUsages.put("NCHI", new EventUsage(62, "INDI"));    // The number of children that this person is known to be the parent of (all marriages) when subordinate to an individual, or that belong to this family when subordinate to a FAM_RECORD
        eventUsages.put("NMR",  new EventUsage(63, "INDI"));    // The number of times this person has participated in a family as a spouse or parent.
        // end of any age

        eventUsages.put("RETI", new EventUsage(70, "INDI"));    // An event of exiting an occupational relationship with an employer after a qualifying time period. (60-67)

        eventUsages.put("PROB", new EventUsage(80, "INDI"));    // (probate) An event of judicial determination of the validity of a will. May indicate several related court activities over several dates.
        eventUsages.put("WILL", new EventUsage(81, "INDI"));    // A legal document treated as an event, by which a person disposes of his or her estate, to take effect after death. The event date is the date the will was signed while the person was alive
        
        eventUsages.put("DEAT", new EventUsage(97, "INDI"));    // The event when mortal life terminates
        eventUsages.put("BURI", new EventUsage(98, "INDI"));    // The event of the proper disposing of the mortal remains of a deceased person.
        eventUsages.put("CREM", new EventUsage(99, "INDI"));    // Disposal of the remains of a person's body by fire
    }

    public static String[] getTags(Map<String, EventUsage> eventUsages, String type) {
        
        List<String> ret = new ArrayList<String>();
        Set<String> keys = eventUsages.keySet();
        for (String key : keys) {
            EventUsage eu = eventUsages.get(key);
            if (eu.getType().equals(type) || eu.getType().equals("ALL")) {
                ret.add(key);
            }
        }
        return ret.toArray(new String[ret.size()]);
        
    }
    
    public static boolean isEventTag(String tag) {
        Map<String, EventUsage> eventUsages = new HashMap<String, EventUsage>();
        EventUsage.init(eventUsages);
        Set<String> keys = eventUsages.keySet();
        return keys.contains(tag);
    }
    
    
    
    private int order = 0;
    private String type = "";

    private EventUsage(int order, String type) {
        this.order = order;
        this.type = type;
    }
    
    public int getOrder() {
        return order;
    }

    public String getType() {
        return type;
    }
    
    
}
