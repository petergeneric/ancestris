/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook.creator;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.PropertyDate;
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
    public Indi indiDeCujus = null;
    public WebHelper wh = null;
    //
    // outputs
    public int nbGen = 0;
    public int nbAncestors = 0;
    public Indi indiOlder = null;
    public Indi longIndiG = null;
    public int nbGenG = 0;
    public Indi longIndiA = null;
    public int nbAncestorsA = 0;
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

    public boolean update(Gedcom gedcom, boolean dispLonguest) {

        // number of generations and ancestors
        nbGenTemp = 0;
        nbAncestorsTemp = 0;
        calcGenAncestors(indiDeCujus, true); // calculates nbGenTemp and nbAncestorsTemp and indiOlder
        nbGen = nbGenTemp;
        nbAncestors = nbAncestorsTemp;
        nbGen--;
        nbAncestors--;

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

    private void calcGenAncestors(Indi indiStart, boolean calcOlder) {
        
        List<Ancestor> ancestorsList = wh.getAncestorsList(indiStart);
        nbGenTemp = 1;
        nbAncestorsTemp = 0;
        indiOlder = indiStart;
        PropertyDate propDateMin = new PropertyDate(9999);
        Set<Indi> listDifferent = new HashSet<Indi>();
        for (Ancestor ancestor : ancestorsList) {
            if (ancestor.gen > nbGenTemp) {
                nbGenTemp = ancestor.gen;
            }
            listDifferent.add(ancestor.indi);
            if (calcOlder) {
                PropertyDate propDate = ancestor.indi.getBirthDate();
                if (propDate != null && propDate.compareTo(propDateMin) < 0) {
                    propDateMin = propDate;
                    indiOlder = ancestor.indi;
                }
            }
        }
        nbAncestorsTemp = listDifferent.size();
    }

    public void calcLonguestLine(Indi indiRef) {
        int nbG = 0;
        int nbA = 0;

        // get all individuals from Gedcom
        List<Indi> indis = wh.getIndividuals(indiRef.getGedcom(), null);

        // loop to look for longuest line
        for (Iterator<Indi> it = indis.iterator(); it.hasNext();) {
            Indi indi = it.next();
            nbGenTemp = 0;
            nbAncestorsTemp = 0;
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
