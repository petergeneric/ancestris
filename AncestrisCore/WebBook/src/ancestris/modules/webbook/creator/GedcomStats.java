/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author frederic
 */
class GedcomStats {

    // inputs
    public WebHelper wh = null;
    public Indi indiDeCujus = null;         // The deCujus individual
    //
    // outputs
    public int nbAncestors = 0;             // The number of different ancestors of the DeCujus
    public int nbGen = 0;                   // The level number of generations where these ancestors have been found
    public Indi indiOlder = null;           // The oldest ancestor of the DeCujus
    
    public Indi longIndiG = null;           // Individual with the longuest ancestors line 
    public int nbGenG = 0;                  // Nb of generations of the longuest line
    
    public Indi longIndiA = null;           // Individual with the most ancestors
    public int nbAncestorsA = 0;            // Nb of ancestors of the largest line
    
    public int nbIndis = 0;
    public int nbFams = 0;
    public int nbNames = 0;
    public int nbPlaces = 0;
    public int nbAscendants = 0;
    public int nbCousins = 0;
    public int nbOthers = 0;
    public int nbFamsWithKids = 0;
    public double avgKids = 0;
    public String place = "";
    //
    // private variables
    private int nbGenTemp = 0;
    private int nbAncestorsTemp = 0;

    // constructor
    public GedcomStats(WebBookParams wp, WebHelper wh) {
        this.wh = wh;
        indiDeCujus = wh.getIndiDeCujus(wp.param_decujus);
        update(wh.gedcom, wp.param_dispStatAncestor.equals("1"));
    }

    @SuppressWarnings("unchecked")
    public boolean update(Gedcom gedcom, boolean dispLonguest) {

        // number of generations and ancestors
        calcGenAncestors(indiDeCujus, true); // calculates nbGenTemp and nbAncestorsTemp and indiOlder
        nbGen = nbGenTemp - 1;
        nbAncestors = nbAncestorsTemp - 1;

        // number of individuals
        nbIndis = wh.getNbIndis();
        nbAscendants = wh.getAncestors(indiDeCujus).size();
        nbCousins = wh.getCousins(indiDeCujus).size();
        nbOthers = nbIndis - nbAscendants - nbCousins;
        nbAscendants = Math.max(nbAscendants - 1, 0);


        //  number of places, main locations
        Collection<Entity> entities = gedcom.getEntities();
        List<Property> placesProps = new ArrayList<Property>();
        for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
            Entity ent = it.next();
            wh.getPropertiesRecursively((Property) ent, placesProps, PropertyPlace.class);
        }
        nbPlaces = wh.getTotalCitiesCount();

        int max = 0;
        String placeMax = "";
        Integer val = 0;
        String juridic = "";
        Map<String, Integer> placeTop = new TreeMap<String, Integer>();
        for (Iterator<Property> it = placesProps.iterator(); it.hasNext();) {
            Property prop = it.next();
            if (prop instanceof PropertyPlace) {
                juridic = ((PropertyPlace)prop).getCity().trim();
            } 
            if (juridic != null && juridic.length() > 0) {
                val = 1;
                if (placeTop.get(juridic) != null) {
                    val = placeTop.get(juridic) + 1;
                }
                placeTop.put(juridic, val);
                if (val > max) {
                    max = val;
                    placeMax = juridic;
                }
            }
        }
        place = placeMax;


        // number of families and those with kids and average number of kids per marriage
        Collection<Fam> families = (Collection<Fam>) gedcom.getEntities(Gedcom.FAM);
        nbFams = families.size();
        int cptKids = 0, cptFams = 0;
        for (Iterator<Fam> it = families.iterator(); it.hasNext();) {
            Fam family = it.next();
            int nb = family.getNoOfChildren();
            if (nb > 0) {
                cptFams++;
                cptKids += nb;
            }
        }
        nbFamsWithKids = cptFams;
        avgKids = (nbFamsWithKids > 0) ? (cptKids * 100 / nbFamsWithKids) : 0;
        avgKids /= 100;

        // number of names
        nbNames = wh.getTotalNamesCount();

        return true;
    }

    /**
     * Calculates the number of different ancestors of a given individual, and the number of maximum generations up to the "oldest" ancestor
     * 
     * @param indiStart : starting individual
     * @param calcOlder : true means calculate oldest ancestor
     * Returns:
     *    - nbAncestorsTemp : nb of ancestors
     *    - nbGenTemp : nb of generations
     */
    private void calcGenAncestors(Indi indiStart, boolean calcOlder) {
        
        // Get all ancestors
        List<Ancestor> ancestorsList = wh.getAncestorsList(indiStart);
        
        // Init variables
        nbGenTemp = 1;
        nbAncestorsTemp = 0;
        indiOlder = indiStart;
        PropertyDate propDateMin = new PropertyDate(9999);
        Set<Indi> listDifferent = new HashSet<Indi>();     // list with no duplicates
        
        // Loop over ancestors
        for (Ancestor ancestor : ancestorsList) {
            if (ancestor.gen > nbGenTemp) {
                nbGenTemp = ancestor.gen;
            }
            listDifferent.add(ancestor.indi);
            if (calcOlder) {
                PropertyDate propDate = ancestor.indi.getBirthDate();
                if (propDate != null && propDate.isValid() && propDate.compareTo(propDateMin) < 0) {
                    propDateMin = propDate;
                    indiOlder = ancestor.indi;
                }
            }
        }
        nbAncestorsTemp = listDifferent.size();
    }

    /**
     * Calculates the longuest and largest ancestors lines
     * @param indiRef 
     */
    public void calcLonguestLine(Indi indiRef) {

        int nbG = 0;
        int nbA = 0;

        // get all individuals from Gedcom
        List<Indi> indis = wh.getIndividuals(indiRef.getGedcom(), null);

        // loop to look for longuest line (G) / largest line (A)
        for (Indi indi : indis) {
            calcGenAncestors(indi, false);
            if (nbGenTemp > nbG) {
                nbG = nbGenTemp;
                longIndiG = indi;
            }
            if ((nbGenTemp == nbG) && (indi == indiRef)) {
                longIndiG = indi;
            }
            if (nbAncestorsTemp > nbA) {
                nbA = nbAncestorsTemp;
                longIndiA = indi;
            }
            if ((nbAncestorsTemp == nbA) && (indi == indiRef)) {
                longIndiA = indi;
            }
        }
        nbGenG = nbG - 1;
        nbAncestorsA = nbA - 1;

        return;
    }
}
