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

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.modules.gedcom.utilities.SelectEntityDialog;
import genj.app.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class SosaNumbers {

    private class GedcomEventHandler implements GedcomFileListener, GedcomListener {

        private final Preferences modulePreferences = NbPreferences.forModule(SosaNumbers.class);

        GedcomEventHandler() {
            AncestrisPlugin.register(this);
        }

        @Override
        public void commitRequested(Context context) {
        }

        @Override
        public void gedcomClosed(Gedcom gedcom) {
            gedcom.removeGedcomListener(this);
        }

        @Override
        public void gedcomOpened(Gedcom gedcom) {
            String selectedEntityID = modulePreferences.get("SelectEntityDialog." + gedcom.getName(), "");
            Indi indiDeCujus = null;
            if (selectedEntityID.isEmpty()) {
                SelectEntityDialog selectEntityDialog = new SelectEntityDialog(NbBundle.getMessage(this.getClass(), "AskDeCujus"), gedcom, Gedcom.INDI);
                if ((indiDeCujus = (Indi) selectEntityDialog.getEntity()) != null) {
                    modulePreferences.put("SelectEntityDialog." + gedcom.getName(), indiDeCujus.getId());
                } else {
                    modulePreferences.put("SelectEntityDialog." + gedcom.getName(), "No SOSA");
                }
            } else if (!selectedEntityID.equals("No SOSA")) {
                indiDeCujus = (Indi) gedcom.getEntity(Gedcom.INDI, selectedEntityID);
            }
            if (indiDeCujus != null) {
                gedcom.addGedcomListener(this);
                generateSosaNbs(gedcom, indiDeCujus);
            }
        }

        @Override
        public void gedcomEntityAdded(final Gedcom gedcom, final Entity entity) {
            // Perform unit of work
            try {
                DecimalFormat formatNbrs = new DecimalFormat("0");
                // generate nested sosa Tags
                if (entity instanceof Indi) {
                    Indi indi = (Indi) entity;
                    // Get father and mother
                    Fam famc = indi.getFamilyWhereBiologicalChild();
                    if (famc != null) {
                        Indi husband = famc.getHusband();
                        if (husband != null) {
                            Property sosaPropertyValue = husband.getProperty(SOSA_TAG);
                            if (sosaAboNumbering) {
                                int sosaNumber = Integer.getInteger(sosaPropertyValue.getValue());

                                // re generate all sosaAboNumbering tags
                                Fam[] families = indi.getFamiliesWhereSpouse();
                                for (Fam family : families) {
                                    // Order Children
                                    int ChildOrder = 1;

                                    for (Indi child : family.getChildren(true)) {
                                        Property sosaAboNumberingPropertyValue = child.getProperty(DABOVILLE_TAG);
                                        if (sosaAboNumberingPropertyValue != null) {
                                            sosaAboNumberingPropertyValue.setValue(formatNbrs.format(sosaNumber) + "." + formatNbrs.format(ChildOrder));
                                        } else {
                                            Property addedProperty = child.addProperty(DABOVILLE_TAG, formatNbrs.format(sosaNumber) + "." + formatNbrs.format(ChildOrder), setPropertyPosition(child));
                                            addedProperty.setGuessed(true);
                                        }
                                        ChildOrder += 1;
                                    }
                                }
                            }
                        }
                    } else {
                        Fam[] families = indi.getFamiliesWhereSpouse();

                        for (Fam family : families) {
                            for (Indi child : family.getChildren(true)) {
                                Property sosaPropertyValue = child.getProperty(SOSA_TAG);
                                if (sosaPropertyValue != null) {
                                    int sosaNumber = Integer.getInteger(sosaPropertyValue.getValue());
                                    if (indi.getSex() == PropertySex.MALE) {
                                        Property addedProperty = indi.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaNumber), setPropertyPosition(indi));
                                        addedProperty.setGuessed(true);
                                    } else if (indi.getSex() == PropertySex.FEMALE) {
                                        Property addedProperty = indi.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaNumber + 1), setPropertyPosition(indi));
                                        addedProperty.setGuessed(true);
                                    }
                                    if (sosaAboNumbering && indi.getSex() == PropertySex.MALE) {
                                        for (Fam family2 : families) {
                                            // Order Children
                                            int ChildOrder = 1;
                                            for (Indi child2 : family2.getChildren(true)) {
                                                Property addedProperty = child.addProperty(DABOVILLE_TAG, formatNbrs.format(2 * sosaNumber) + "." + formatNbrs.format(ChildOrder), setPropertyPosition(child2));
                                                addedProperty.setGuessed(true);
                                                ChildOrder += 1;
                                            }
                                        }
                                    }
                                    break; // Only one sosa possible
                                }
                            }
                        }
                    }
                }
            } catch (GedcomException e) {
                LOG.severe(e.getMessage());
            }
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
    }
    private final static Logger LOG = Logger.getLogger(SosaNumbers.class.getName(), null);
    private final GedcomEventHandler gedcomEventHandler = new GedcomEventHandler();
    final private String SOSA_TAG = "_SOSA";
    final private String DABOVILLE_TAG = "_DABOVILLE";
    final private boolean sosaAboNumbering = true;

    public void generateSosaNbs(final Gedcom gedcom, final Indi indiDeCujus) {
        final List<Indi> sosaList = new ArrayList<Indi>();   // list only used to store ids of sosas
        // Perform unit of work
        final DecimalFormat formatNbrs = new DecimalFormat("0");

        // Clean gedcom file for all SOSA and SOSA_ABBO tags
        new GedcomUtilities(gedcom).deleteTags(SOSA_TAG, GedcomUtilities.ENT_INDI);
        new GedcomUtilities(gedcom).deleteTags(DABOVILLE_TAG, GedcomUtilities.ENT_INDI);

        try {
            Indi indi;
            Indi wife;
            Indi husband;
            int sosaCounter = 0;
            Fam famc;
            // Put de-cujus first in list and update its sosa tag
            Property addedProperty = indiDeCujus.addProperty(SOSA_TAG, formatNbrs.format(1), setPropertyPosition(indiDeCujus));
            addedProperty.setGuessed(true);

            sosaList.add(indiDeCujus);
            // Iterate on the list to go up the tree.
            // Store both parents in list
            ListIterator<Indi> listIter = sosaList.listIterator();
            while (listIter.hasNext()) {
                indi = listIter.next();
                sosaCounter = Integer.parseInt(indi.getPropertyValue(SOSA_TAG));
                // Sosa d'Aboville generation
                if (sosaAboNumbering && indi.getSex() == PropertySex.MALE) {
                    dabovilleNumbering(indi);
                }
                // Get father and mother
                famc = indi.getFamilyWhereBiologicalChild();
                if (famc != null) {
                    wife = famc.getWife();
                    if (wife != null) {
                        addedProperty = wife.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter + 1), setPropertyPosition(wife));
                        addedProperty.setGuessed(true);
                        LOG.log(Level.INFO, "{0} -> {1}", new Object[]{wife.toString(), formatNbrs.format(2 * sosaCounter + 1)});
                        listIter.add(wife);
                        listIter.previous();
                    }
                    husband = famc.getHusband();
                    if (husband != null) {
                        addedProperty = husband.addProperty(SOSA_TAG, formatNbrs.format(2 * sosaCounter), setPropertyPosition(husband));
                        addedProperty.setGuessed(true);
                        LOG.log(Level.INFO, "{0} -> {1}", new Object[]{husband.toString(), formatNbrs.format(2 * sosaCounter)});
                        listIter.add(husband);
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

    private void dabovilleNumbering(Indi indiDeCujus) {
        Indi indi;
        List<Indi> dabovilleList = new ArrayList<Indi>();
        Property addedProperty;
        String daboCounter;
        // Iterate on the list to go down the tree.
        dabovilleList.add(indiDeCujus);
        ListIterator<Indi> listIter = dabovilleList.listIterator();
        while (listIter.hasNext()) {
            indi = listIter.next();
            Character suffix = 'a';
            if (indi.getProperty(SOSA_TAG) == null) {
                daboCounter = indi.getPropertyValue(DABOVILLE_TAG);
            } else {
                daboCounter = indi.getPropertyValue(SOSA_TAG);
            }

            Fam[] families = indi.getFamiliesWhereSpouse();
            for (Fam family : families) {
                int ChildOrder = 1;
                for (Indi child : family.getChildren(true)) {
                    try {
                        String counter = daboCounter + (families.length > 1 ? suffix.toString() + "." : ".") + ChildOrder;
                        addedProperty = child.addProperty(DABOVILLE_TAG, counter, setPropertyPosition(child));
                        addedProperty.setGuessed(true);
                        // if in sosa tree children are already numbers
                        if (child.getProperty(SOSA_TAG) == null) {
                            listIter.add(child);
                            listIter.previous();
                        }
                        ChildOrder++;
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                suffix++;
            }
        }
    }
}
