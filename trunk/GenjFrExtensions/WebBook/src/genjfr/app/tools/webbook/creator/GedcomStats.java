/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genjfr.app.tools.webbook.WebBookParams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
    public String olderBirthDate = "";
    //
    // private variables
    private int nbGenTemp = 0;
    private int nbAncestorsTemp = 0;
    private Map gens = null;

    // constructor
    public GedcomStats(WebBookParams wp, WebHelper wh) {
        indiDeCujus = wh.getIndiDeCujus(wp.param_decujus);
        this.wh = wh;
        update(wh.gedcom, wp.param_dispStatAncestor.equals("1"));
    }

    public boolean update(Gedcom gedcom, boolean dispLonguest) {

        // number of generations and ancestors
        nbGenTemp = 0;
        nbAncestorsTemp = 0;
        gens = new TreeMap();          // will map generation to indis
        calcGenAncestors(indiDeCujus); // calculates nbGenTemp and nbAncestorsTemp
        nbGen = nbGenTemp;
        nbAncestors = nbAncestorsTemp;

        // Older ancestor and his/her birthdate
        indiOlder = indiDeCujus;
        boolean foundDate = false;
        PropertyDate propDateMin = indiOlder.getBirthDate();
        if (propDateMin == null) {
            propDateMin = new PropertyDate(2100);
        }
        for (Iterator it = gens.keySet().iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            Integer calcGens = (Integer) gens.get(indi);
            if (calcGens == nbGen) {
                PropertyDate propDate = indi.getBirthDate();
                if (propDate == null) {
                    indiOlder = indi;
                }
                if (propDate != null && propDate.compareTo(propDateMin) < 0) {
                    propDateMin = propDate;
                    foundDate = true;
                    indiOlder = indi;
                }
            }
        }

        olderBirthDate = (foundDate) ? propDateMin.getDisplayValue() : null;

        nbGen--;
        nbAncestors--;

        // number of individuals
        nbIndis = wh.getNbIndis();
        nbAscendants = wh.getAncestors(indiDeCujus).size();
        nbCousins = wh.getCousins(indiDeCujus).size();
        nbOthers = nbIndis - nbAscendants - nbCousins;
        nbAscendants = Math.max(nbAscendants - 1, 0);


        //  number of places, main locations
        Collection entities = gedcom.getEntities();
        List placesProps = new ArrayList();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            wh.getPropertiesRecursively((Property) ent, placesProps, "PLAC");
        }
        nbPlaces = wh.getTotalCitiesCount();

        int max = 0;
        String placeMax = "";
        Integer val = 0;
        String juridic = "";
        Map placeTop = new TreeMap();
        for (Iterator it = placesProps.iterator(); it.hasNext();) {
            Property prop = (Property) it.next();
            if (prop instanceof PropertyPlace) {
                juridic = wh.getPlace((PropertyPlace) prop, 1);
            } else {
                juridic = prop.getValue();
            }
            if (juridic != null && juridic.length() > 0) {
                val = 1;
                if (placeTop.get(juridic) != null) {
                    val = (Integer) placeTop.get(juridic) + 1;
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
        Collection families = gedcom.getEntities(Gedcom.FAM);
        nbFams = families.size();
        int cptKids = 0, cptFams = 0;
        for (Iterator it = families.iterator(); it.hasNext();) {
            Fam family = (Fam) it.next();
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

    private void calcGenAncestors(Indi indiStart) {
        Integer calcGens = 1;
        nbGenTemp = 1;
        Fam famc;
        Indi indi = null, indiOther = null;
        List sosaList = new ArrayList();
        Set hs = new HashSet();
        sosaList.add(indiStart);
        gens.clear();
        gens.put(indiStart, calcGens);
        for (ListIterator listIter = sosaList.listIterator(); listIter.hasNext();) {
            indi = (Indi) listIter.next();
            if (!hs.contains(indi)) {
                hs.add(indi);
            }
            calcGens = (Integer) gens.get(indi);
            if (calcGens == null) {
                calcGens = 1;
            }
            famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                calcGens++;
                if (calcGens > nbGenTemp) {
                    nbGenTemp = calcGens;
                }
                indiOther = famc.getWife();
                if (indiOther != null && !hs.contains(indiOther)) {
                    listIter.add(indiOther);
                    gens.put(indiOther, calcGens);
                    listIter.previous();
                }
                indiOther = famc.getHusband();
                if (indiOther != null && !hs.contains(indiOther)) {
                    listIter.add(indiOther);
                    gens.put(indiOther, calcGens);
                    listIter.previous();
                }
            }
        }
        nbAncestorsTemp = sosaList.size();
    }

    public void calcLonguestLine(Indi indiRef) {
        int nbG1 = 0, nbG2 = 0;
        int nbA1 = 0, nbA2 = 0;

        // get all individuals from Gedcom
        List indis = wh.getIndividuals(indiRef.getGedcom(), null);

        // loop to look for longuest line
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            nbGenTemp = 0;
            nbAncestorsTemp = 0;
            gens = new TreeMap();
            calcGenAncestors(indi);
            if (nbGenTemp > nbG1) {
                nbG1 = nbGenTemp;
                nbG2 = nbAncestorsTemp;
                longIndiG = indi;
            }
            if ((nbGenTemp == nbG1) && (indi == indiRef)) {
                nbG1 = nbGenTemp;
                nbG2 = nbAncestorsTemp;
                longIndiG = indi;
            }
            if (nbAncestorsTemp > nbA2) {
                nbA1 = nbGenTemp;
                nbA2 = nbAncestorsTemp;
                longIndiA = indi;
            }
            if ((nbAncestorsTemp == nbA2) && (indi == indiRef)) {
                nbA1 = nbGenTemp;
                nbA2 = nbAncestorsTemp;
                longIndiA = indi;
            }
        }
        nbGenG = nbG1 - 1;
        nbAncestorsA = nbA2 - 1;

        return;
    }
}
