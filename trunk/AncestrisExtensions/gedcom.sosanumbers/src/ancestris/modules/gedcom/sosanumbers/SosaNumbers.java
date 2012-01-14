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
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.modules.gedcom.utlilities.GedcomUtilities;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dominique
 */
public class SosaNumbers {

    private class Pair {

        String ID = "";
        int sosa = 0;

        public Pair(String ID, int sosa) {
            this.ID = ID;
            this.sosa = sosa;
        }
    }
    private final static Logger LOG = Logger.getLogger(SosaNumbers.class.getName(), null);
    private Gedcom gedcom = null;
    final private String SOSA_TAG = "_SOSA";

    SosaNumbers(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public void generateSosaNbs(final Indi indiDeCujus) {
        final List<Pair> sosaList = new ArrayList<Pair>();   // list only used to store ids of sosas
        // Perform unit of work
        final DecimalFormat formatNbrs = new DecimalFormat("0");

        // Clean gedcom file for all tags
        new GedcomUtilities(gedcom).deleteTags(SOSA_TAG, GedcomUtilities.ENT_INDI);
        sosaList.add(new Pair(indiDeCujus.getId(), 1));

        // Perform unit of work
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    Pair pair;
                    Indi indi, indiOther;
                    String indiID = "";
                    int sosaCounter = 0;
                    Fam famc;

                    // Put de-cujus first in list and update its sosa tag
                    indiDeCujus.addProperty(SOSA_TAG, formatNbrs.format(1), setPropertyPosition(indiDeCujus));

                    // Iterate on the list to go up the tree.
                    // Store both parents in list
                    for (ListIterator<Pair> listIter = sosaList.listIterator(); listIter.hasNext();) {
                        pair = listIter.next();
                        indiID = pair.ID;
                        sosaCounter = pair.sosa;
                        indi = (Indi) gedcom.getEntity(indiID);
                        // Get father and mother
                        famc = indi.getFamilyWhereBiologicalChild();
                        if (famc != null) {
                            indiOther = famc.getWife();
                            if (indiOther != null) {
                                indiOther.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter + 1), setPropertyPosition(indiOther));
                                LOG.log(Level.INFO, "{0} -> {1}", new Object[]{indiOther.toString(), formatNbrs.format(2 * sosaCounter + 1)});
                                listIter.add(new Pair(indiOther.getId(), 2 * sosaCounter + 1));
                                listIter.previous();
                            }
                            indiOther = famc.getHusband();
                            if (indiOther != null) {
                                indiOther.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter), setPropertyPosition(indiOther));
                                LOG.log(Level.INFO, "{0} -> {1}", new Object[]{indiOther.toString(), formatNbrs.format(2 * sosaCounter)});
                                listIter.add(new Pair(indiOther.getId(), 2 * sosaCounter));
                                listIter.previous();
                            }
                        }
                    }
                }
            }); // end of doUnitOfWork
        } catch (GedcomException e) {
            LOG.severe(e.getMessage());
        }
    }

    /**
     * Set Property position
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
}
