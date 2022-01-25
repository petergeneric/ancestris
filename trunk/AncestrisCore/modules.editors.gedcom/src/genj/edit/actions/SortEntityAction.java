/*
 * Ancestris - https://www.ancestris.org
 *
 * Copyright 2022 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.edit.actions;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.CommonActions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.TagPath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Sort entity's properties by date.
 *
 * @author Zurga
 */
@ActionID(category = "Tree", id = "genj.tree.actions.sortProperties")
@ActionRegistration(
        displayName = "SortProperties",
        iconInMenu = true,
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 534)})
public class SortEntityAction extends AbstractAncestrisAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger("ancestris.app", null);

    private Context context = null;

    public SortEntityAction() {
        super();
        setImage("genj/edit/images/Sort.png");
        setText(NbBundle.getMessage(SortEntityAction.class, "action.sort"));
        setTip(NbBundle.getMessage(SortEntityAction.class, "action.sort.tip"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        LOG.log(Level.FINEST, "Entering action Sort properties");
        if (context == null) {
            LOG.log(Level.FINEST, "Exiting action Sort properties, no context");
            return;
        }

        Entity e = context.getEntity();
        // Only two possibles entities allowed.
        if (e instanceof Indi) {
            sortPropertyIndi((Indi) e);
        } else {
            sortPropertyFam((Fam) e);
        }
        LOG.log(Level.FINEST, "Exiting action Sort properties");
    }

    private void sortPropertyIndi(Indi indi) {
        List<SortingProperty> sps = new ArrayList<>();
        List<Property> sortableProperties = new ArrayList<>();
        // Find sortables properties (5.5, 5.5.1, 7.0)
        Collections.addAll(sortableProperties, indi.getProperties("BIRT"));
        Collections.addAll(sortableProperties, indi.getProperties("CHR"));
        Collections.addAll(sortableProperties, indi.getProperties("BAPM"));
        Collections.addAll(sortableProperties, indi.getProperties("CAST"));
        Collections.addAll(sortableProperties, indi.getProperties("DSCR"));
        Collections.addAll(sortableProperties, indi.getProperties("EDUC"));
        Collections.addAll(sortableProperties, indi.getProperties("IDNO"));
        Collections.addAll(sortableProperties, indi.getProperties("NATI"));
        Collections.addAll(sortableProperties, indi.getProperties("NATI"));
        Collections.addAll(sortableProperties, indi.getProperties("NCHI"));
        Collections.addAll(sortableProperties, indi.getProperties("NMR"));
        Collections.addAll(sortableProperties, indi.getProperties("OCCU"));
        Collections.addAll(sortableProperties, indi.getProperties("PROP"));
        Collections.addAll(sortableProperties, indi.getProperties("RELI"));
        Collections.addAll(sortableProperties, indi.getProperties("RESI"));
        Collections.addAll(sortableProperties, indi.getProperties("SSN"));
        Collections.addAll(sortableProperties, indi.getProperties("TITL"));
        Collections.addAll(sortableProperties, indi.getProperties("FACT"));
        Collections.addAll(sortableProperties, indi.getProperties("BARM"));
        Collections.addAll(sortableProperties, indi.getProperties("BASM"));
        Collections.addAll(sortableProperties, indi.getProperties("BLES"));
        Collections.addAll(sortableProperties, indi.getProperties("CENS"));
        Collections.addAll(sortableProperties, indi.getProperties("CHRA"));
        Collections.addAll(sortableProperties, indi.getProperties("CONF"));
        Collections.addAll(sortableProperties, indi.getProperties("EMIG"));
        Collections.addAll(sortableProperties, indi.getProperties("FCOM"));
        Collections.addAll(sortableProperties, indi.getProperties("GRAD"));
        Collections.addAll(sortableProperties, indi.getProperties("IMMI"));
        Collections.addAll(sortableProperties, indi.getProperties("NATU"));
        Collections.addAll(sortableProperties, indi.getProperties("ORDN"));
        Collections.addAll(sortableProperties, indi.getProperties("PROB"));
        Collections.addAll(sortableProperties, indi.getProperties("RETI"));
        Collections.addAll(sortableProperties, indi.getProperties("ADOP"));
        Collections.addAll(sortableProperties, indi.getProperties("EVEN"));
        Collections.addAll(sortableProperties, indi.getProperties("BAPL"));
        Collections.addAll(sortableProperties, indi.getProperties("CONL"));
        Collections.addAll(sortableProperties, indi.getProperties("ENDL"));
        Collections.addAll(sortableProperties, indi.getProperties("INIL"));
        Collections.addAll(sortableProperties, indi.getProperties("SLGC"));
        Collections.addAll(sortableProperties, indi.getProperties("FAMS"));
        Collections.addAll(sortableProperties, indi.getProperties("WILL"));
        Collections.addAll(sortableProperties, indi.getProperties("DEAT"));
        Collections.addAll(sortableProperties, indi.getProperties("CREM"));
        Collections.addAll(sortableProperties, indi.getProperties("BURI"));

        for (Property p : sortableProperties) {
            List<PropertyDate> dates = p.getProperties(PropertyDate.class);
            if (!dates.isEmpty()) {
                Collections.sort(dates);
                sps.add(new SortingProperty(dates.get(0), p));
            }
            if (p instanceof PropertyFamilySpouse) { // Child try marriage date
                PropertyFamilySpouse pc = (PropertyFamilySpouse) p;
                PropertyDate pd = pc.getFamily().getMarriageDate();
                if (pd != null) {
                    sps.add(new SortingProperty(pd, p));
                }
            }
        }
        Collections.sort(sps);
        sortableProperties.clear();
        // Put Name, sex and numbering at the beginning
        Collections.addAll(sortableProperties, indi.getProperties("NAME"));
        Collections.addAll(sortableProperties, indi.getProperties("SEX"));
        Collections.addAll(sortableProperties, indi.getProperties("_SOSA"));
        Collections.addAll(sortableProperties, indi.getProperties("_SOSADABOVILLE"));
        Collections.addAll(sortableProperties, indi.getProperties("_DABOVILLE"));
        Collections.addAll(sortableProperties, indi.getProperties("FAMC"));
        // Put sorted ones
        for (SortingProperty sp : sps) {
            sortableProperties.add(sp.getContexte());
        }
        // Move all
        try {
            context.getGedcom().doUnitOfWork((Gedcom gedcom) -> {
                indi.moveProperties(sortableProperties, 0);
            });
        } catch (GedcomException e) {
            DialogManager.createError(null, e.getMessage()).show();
        }

    }

    private void sortPropertyFam(Fam fam) {
        List<SortingProperty> sps = new ArrayList<>();
        List<Property> sortableProperties = new ArrayList<>();
        // Find sortables properties (5.5, 5.5.1, 7.0)
        Collections.addAll(sortableProperties, fam.getProperties("NCHI"));
        Collections.addAll(sortableProperties, fam.getProperties("RESI"));
        Collections.addAll(sortableProperties, fam.getProperties("ENGA"));
        Collections.addAll(sortableProperties, fam.getProperties("MARB"));
        Collections.addAll(sortableProperties, fam.getProperties("MARC"));
        Collections.addAll(sortableProperties, fam.getProperties("MARL"));
        Collections.addAll(sortableProperties, fam.getProperties("MARR"));
        Collections.addAll(sortableProperties, fam.getProperties("ANUL"));
        Collections.addAll(sortableProperties, fam.getProperties("MARS"));
        Collections.addAll(sortableProperties, fam.getProperties("CHIL"));
        Collections.addAll(sortableProperties, fam.getProperties("DIVF"));
        Collections.addAll(sortableProperties, fam.getProperties("DIV"));
        Collections.addAll(sortableProperties, fam.getProperties("CENS"));
        Collections.addAll(sortableProperties, fam.getProperties("EVEN"));
        Collections.addAll(sortableProperties, fam.getProperties("SLGS"));

        for (Property p : sortableProperties) {
            List<PropertyDate> dates = p.getProperties(PropertyDate.class);
            if (!dates.isEmpty()) {
                Collections.sort(dates);
                sps.add(new SortingProperty(dates.get(0), p));
            }
            if (p instanceof PropertyChild) { // Child try birth date
                PropertyChild pc = (PropertyChild) p;
                PropertyDate pd = pc.getChild().getBirthDate();
                if (pd != null) {
                    sps.add(new SortingProperty(pd, p));
                } else { // No birthdate try Baptism
                    pd = (PropertyDate) pc.getChild().getProperty(new TagPath("INDI:CHR:DATE"));
                    if (pd != null) {
                        sps.add(new SortingProperty(pd, p));
                    }
                }
            }
        }
        Collections.sort(sps);
        sortableProperties.clear();
        // Put HUSB and WIFE at the beginning
        Collections.addAll(sortableProperties, fam.getProperties("HUSB"));
        Collections.addAll(sortableProperties, fam.getProperties("WIFE"));
        // Put sorted ones
        for (SortingProperty sp : sps) {
            sortableProperties.add(sp.getContexte());
        }
        // Move all
        try {
            context.getGedcom().doUnitOfWork((Gedcom gedcom) -> {
                fam.moveProperties(sortableProperties, 0);
            });
        } catch (GedcomException e) {
            DialogManager.createError(null, e.getMessage()).show();
        }
        
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Entity e = context.lookup(Entity.class);
        if (e == null || (!(e instanceof Indi) && !(e instanceof Fam))) {
            return CommonActions.NOOP;
        } else {
            this.context = new Context(e);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            return this;
        }
    }

    private class SortingProperty implements Comparable<SortingProperty> {

        private final PropertyDate date;
        private final Property contexte;

        SortingProperty(PropertyDate theDate, Property theProp) {
            date = theDate;
            contexte = theProp;
        }

        @Override
        public int compareTo(SortingProperty o) {
            return date.compareTo(o.getDate());
        }

        public PropertyDate getDate() {
            return date;
        }

        public Property getContexte() {
            return contexte;
        }

    }

}
