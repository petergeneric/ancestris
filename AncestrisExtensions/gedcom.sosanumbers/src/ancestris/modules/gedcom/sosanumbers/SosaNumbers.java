/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012 Ancestris
 *
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 * Extract from GenJ - Report - ReportToolBox
 *
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.modules.gedcom.utilities.GedcomUtilities;
import genj.gedcom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class SosaNumbers implements GedcomListener {

    private final static Logger LOG = Logger.getLogger(SosaNumbers.class.getName(), null);
    final private String SOSA_TAG = "_SOSA";
    final private String DABOVILLE_TAG = "_SOSA_DABOVILLE";
    final private boolean sosaAboNumbering = true;

    public void generateSosaNbs(final Gedcom gedcom, final Indi indiDeCujus) {
        final List<Pair> sosaList = new ArrayList<Pair>();   // list only used to store ids of sosas
        // Perform unit of work
        final DecimalFormat formatNbrs = new DecimalFormat("0");

        // Clean gedcom file for all SOSA and SOSA_ABBO tags
        GedcomUtilities.deleteTags(gedcom, SOSA_TAG, GedcomUtilities.ENT_INDI);
        GedcomUtilities.deleteTags(gedcom, DABOVILLE_TAG, GedcomUtilities.ENT_INDI);

        try {
            Indi wife;
            Indi husband;
            Property sosaProperty = null;
            int sosaCounter = 1;
            Fam famc;

            // Put de-cujus first in list and update its sosa tag
            sosaProperty = indiDeCujus.addProperty(SOSA_TAG, formatNbrs.format(sosaCounter), setPropertyPosition(indiDeCujus));
            sosaProperty.setGuessed(true);

            sosaList.add(new Pair(indiDeCujus, formatNbrs.format(sosaCounter)));
            // Iterate on the list to go up the tree.
            // Store both parents in list
            ListIterator<Pair> listIter = sosaList.listIterator();
            while (listIter.hasNext()) {
                Pair pair = listIter.next();
                sosaCounter = Integer.parseInt(pair.value);
                // Sosa d'Aboville generation
                if (pair.indi.equals(indiDeCujus) || (sosaAboNumbering && pair.indi.getSex() == PropertySex.MALE)) {
                    dabovilleNumbering(pair.indi, pair.value);
                }
                // Get father and mother
                famc = pair.indi.getFamilyWhereBiologicalChild();
                if (famc != null) {
                    wife = famc.getWife();
                    if (wife != null) {
                        if ((sosaProperty = wife.getProperty(SOSA_TAG)) == null) {
                            sosaProperty = wife.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter + 1) + " " + computeGene(2 * sosaCounter + 1), setPropertyPosition(wife));
                            sosaProperty.setGuessed(true);
                        } else {
                            sosaProperty.setValue(sosaProperty.getValue() + ";" + formatNbrs.format(2 * sosaCounter + 1) + " " + computeGene(2 * sosaCounter + 1));
                        }
                        LOG.log(Level.INFO, "{0} -> {1}", new Object[]{wife.toString(), formatNbrs.format(2 * sosaCounter + 1)});
                        listIter.add(new Pair(wife, formatNbrs.format(2 * sosaCounter + 1)));
                        listIter.previous();
                    }
                    husband = famc.getHusband();
                    if (husband != null) {
                        if ((sosaProperty = husband.getProperty(SOSA_TAG)) == null) {
                            sosaProperty = husband.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter) + " " + computeGene(2 * sosaCounter + 1), setPropertyPosition(husband));
                            sosaProperty.setGuessed(true);
                        } else {
                            sosaProperty.setValue(sosaProperty.getValue() + ";" + formatNbrs.format(2 * sosaCounter) + " " + computeGene(2 * sosaCounter + 1));
                        }
                        LOG.log(Level.INFO, "{0} -> {1}", new Object[]{husband.toString(), formatNbrs.format(2 * sosaCounter)});
                        listIter.add(new Pair(husband, formatNbrs.format(2 * sosaCounter)));
                        listIter.previous();
                    }
                }
            }
        } catch (GedcomException e) {
            LOG.severe(e.getMessage());
        }
    }

    /**
     * Set Property position
     *
     * @param prop
     * @return
     */
    private int setPropertyPosition(Property prop) {
        if (prop == null) {
            return 1;
        }
        Property pName = prop.getProperty("NAME");
        if (pName == null) {
            return 1;
        }
        return prop.getPropertyPosition(pName) + 1;
    }

    /**
     *
     * @param sosa
     * @return String
     */
    String computeGene(Integer sosa) {
        Integer generation = 0;
        while ((sosa = sosa >> 1) != 0) {
            generation++;
        }
        // Sosa number = 1 generation 1; Sosa number = 2 or 3 generation 2 and so on
        generation++;
        return "(Gen " + generation.toString() + ")";
    }

    /**
     *
     * @param indiDeCujus
     * @param sosaValue
     */
    private void dabovilleNumbering(Indi indiDeCujus, String sosaValue) {
        Pair pair;
        List<Pair> dabovilleList = new ArrayList<Pair>();
        Property dabovilleProperty;
        String daboCounter;

        // Iterate on the list to go down the tree.
        dabovilleList.add(new Pair(indiDeCujus, sosaValue));
        ListIterator<Pair> listIter = dabovilleList.listIterator();
        while (listIter.hasNext()) {
            pair = listIter.next();
            Character suffix = 'a';
            daboCounter = pair.value;

            Fam[] families = pair.indi.getFamiliesWhereSpouse();
            for (Fam family : families) {
                int ChildOrder = 1;
                for (Indi child : family.getChildren(true)) {
                    try {
                        String counter = daboCounter + (families.length > 1 ? suffix.toString() + "." : ".") + ChildOrder;
                        // Skip if indi has already a sosa_aboville tag
                        if (child.getProperty(DABOVILLE_TAG) == null) {
                            dabovilleProperty = child.addProperty(DABOVILLE_TAG, counter, setPropertyPosition(child));
                            dabovilleProperty.setGuessed(true);

                            // if in sosa tree children are already numbers
                            if (child.getProperty(SOSA_TAG) == null) {
                                listIter.add(new Pair(child, counter));
                                listIter.previous();
                            }
                            ChildOrder++;
                        }
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                suffix++;
            }
        }
    }

    @Override
    public void gedcomEntityAdded(final Gedcom gedcom, final Entity entity) {
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        // Check if in Sosa if so delete all referenced tags
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property,
            int pos, Property added) {
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property,
            int pos, Property deleted) {
    }

    private class Pair {

        Indi indi;
        String value;

        public Pair(Indi indi, String value) {
            this.indi = indi;
            this.value = value;
        }
    }
}
