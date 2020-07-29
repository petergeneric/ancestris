/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.marking;

import ancestris.api.search.SearchCommunicator;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.commonAncestor.CommonAncestorTopComponent;
import ancestris.modules.document.view.WidgetDocumentView;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectEntityPanel;
import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import genj.view.ViewContext;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.gedcom.marking.MarkingAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_MarkingAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "MarkingAction", position = 2500)
public final class MarkingAction extends AbstractAncestrisContextAction {

    private CommonAncestorTopComponent commonAncestorComponent = null;
    private Color notSosaColor = new Color(153,0,255);

    
    public MarkingAction() {
        super();
        setImage("ancestris/modules/gedcom/marking/MarkingIcon.png");
        setText(NbBundle.getMessage(MarkingAction.class, "CTL_MarkingAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // Ask user to choose numbering preferences
        Context contextToOpen = getContext();
        if (contextToOpen == null) {
            return;
        }

        MarkingPanel markingPanel = new MarkingPanel(getContext());
        Object choice = DialogManager.create(NbBundle.getMessage(MarkingAction.class, "MarkingAction.AskParams"), markingPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("markingPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            markingPanel.savePreferences();
            marking(contextToOpen, markingPanel.getSettings());
        }

    }

    // Tree top individuals are relative to a deCujus individual, otherwise all isolated people would be marked which is probably not what we want => need an individual
    // Tree bottom individuals are relative to a root individual, in a descending tree logic => need an individual
    // Implex people are individuals with descendants who have kids together, regardless of a root individual. => no need for an intial individual
    private boolean marking(Context contextToOpen, final MarkingPanel.Settings settings) {

        final Gedcom gedcom = contextToOpen.getGedcom();
        Indi indiDeCujus = null;

        List<ViewContext> treetops = new ArrayList<>();
        List<ViewContext> treebottoms = new ArrayList<>();
        List<ViewContext> implexes = new ArrayList<>(); // = married cousins
        List<ViewContext> multipleancestors = new ArrayList<>();
        List<ViewContext> searchIndividuals = new ArrayList<>();

        // Get a first individual
        if (settings.isTreeTop || settings.isTreeBottom) {
            Entity entity = contextToOpen.getEntity();
            if (entity instanceof Indi) {
                indiDeCujus = (Indi) entity;
            } else {
                // Selection box
                SelectEntityPanel select = new SelectEntityPanel(gedcom, Gedcom.INDI, NbBundle.getMessage(this.getClass(), "MarkingAction.AskIndividual"),
                        contextToOpen.getEntity());
                if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(this.getClass(), "CTL_MarkingAction"), select)
                        .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("markingPanelIndi").show()) {
                    return false;
                }
                indiDeCujus = (Indi) select.getSelection();
            }
            if (indiDeCujus == null) {
                return false;
            }
            // we have our root indi

        }

        // Mark tree top individuals
        if (settings.isTreeTop) {
            final Indi finalIndi = indiDeCujus;
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        markTreeTop(gedcom, finalIndi, settings, treetops);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }

        // Mark tree bottom individuals
        if (settings.isTreeBottom) {
            final Indi finalIndi = indiDeCujus;
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        markTreeBottom(gedcom, finalIndi, settings, treebottoms);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }

        // Mark implex people.
        if (settings.isImplex || settings.isMulti) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        markImplex(gedcom, settings, implexes, multipleancestors);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }

        // Mark selected individuals
        if (settings.isSearch) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        markSearch(gedcom, settings, searchIndividuals);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }
        
        
        //================================================================================================================================
        
        // Display list of people
        boolean shown = false;
        
        if (settings.isTreeTop && treetops.size() > 0 && settings.toBeDisplayed) {
            showDocument(gedcom, treetops, "MarkingPanel.jCheckBoxTreeTop.text", treetops.size(), contextToOpen.toString());
            shown = true;
        }

        if (settings.isTreeBottom && treebottoms.size() > 0 && settings.toBeDisplayed) {
            showDocument(gedcom, treebottoms, "MarkingPanel.jCheckBoxTreeBottom.text", treebottoms.size(), contextToOpen.toString());
            shown = true;
        }

        if (settings.isImplex && implexes.size() > 0 && settings.toBeDisplayed) {
            // Activate common ancestor tool
            commonAncestorComponent = CommonAncestorTopComponent.createInstance(contextToOpen);
            // Show list
            showDocument(gedcom, implexes, "MarkingPanel.jCheckBoxImplex.text", implexes.size(), contextToOpen.getGedcom().getDisplayName());
            shown = true;
        }

        if (settings.isMulti && multipleancestors.size() > 0 && settings.toBeDisplayed) {
            showDocument(gedcom, multipleancestors, "MarkingPanel.jCheckBoxMulti.text", multipleancestors.size(), contextToOpen.getGedcom().getDisplayName());
            shown = true;
        }
        
        if (settings.isSearch && searchIndividuals.size() > 0 && settings.toBeDisplayed) {
            showDocument(gedcom, searchIndividuals, "MarkingPanel.jCheckBoxSearch.text", searchIndividuals.size(), contextToOpen.getGedcom().getDisplayName() + " | " + NbBundle.getMessage(MarkingAction.class, settings.searchOption));
            shown = true;
        }
        
        if (!settings.toBeDisplayed && settings.toBeMarked) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(MarkingAction.class, "MarkingAction.Done"), NotifyDescriptor.INFORMATION_MESSAGE));
        }
        
        if (settings.toBeDisplayed && !shown) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(MarkingAction.class, "MarkingAction.Nothing"), NotifyDescriptor.INFORMATION_MESSAGE));
        }
        

        return true;
    }

    private void showDocument(Gedcom gedcom, Object list, String text, int size, String context) {
        Object object = new ContextListWidget((List<Context>) list);
        String title = NbBundle.getMessage(MarkingAction.class, text);
        new WidgetDocumentView(new Context(gedcom),
                NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.title", size, title),
                NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.tip", title, context),
                ((JComponent) object));
    }
    
    private void sortMarkers(List<ViewContext> list) {
        list.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((ViewContext) o1).compareTo((ViewContext) o2);
            }
        });
    }

    private void sortMarkers2(List<ViewContext> list) {
        list.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                int i1 = Integer.valueOf(((ViewContext) o1).getCode());
                int i2 = Integer.valueOf(((ViewContext) o2).getCode());
                if (i1 != i2) {
                    return Integer.compare(i2, i1);
                }
                return ((ViewContext) o1).compareTo((ViewContext) o2);
            }
        });
    }

    private void markTreeTop(Gedcom gedcom, Indi indiDeCujus, MarkingPanel.Settings settings, List<ViewContext> treetops) {

        // Clean gedcom file for all tags
        if (settings.toBeMarked) {
            GedcomUtilities.deleteTags(gedcom, settings.treeTopTag, Gedcom.INDI, false);
        }

        Set<Indi> viewedIndis = new HashSet<>();
        List<Pair> sosaList = new ArrayList<>();   // list only used to store ids of sosas
        Pair pair = new Pair("", BigInteger.ZERO);
        String indiID = "";
        Indi indi, indiOther;
        BigInteger sosaCounter;
        Fam famc;
        int maxGen = 0;

        // Put de-cujus first in list and update its sosa tag
        sosaList.add(new Pair(indiDeCujus.getId(), BigInteger.ONE));

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
                    listIter.add(new Pair(indiOther.getId(), sosaCounter.shiftLeft(1).add(BigInteger.ONE)));
                    listIter.previous();
                }
                indiOther = famc.getHusband();
                if (indiOther != null) {
                    listIter.add(new Pair(indiOther.getId(), sosaCounter.shiftLeft(1)));
                    listIter.previous();
                }
            } else {
                if (!viewedIndis.contains(indi)) {  // we only need each treetop individual once
                    viewedIndis.add(indi);
                    if (settings.toBeMarked) {
                        indi.addProperty(settings.treeTopTag, settings.treeTopValue);
                    }
                    if (settings.toBeDisplayed) {
                        treetops.add(new ViewContext(indi).setText(sosaCounter.bitLength() + "%" + sosaCounter + "%" + indi.getDisplayTitle(true)));
                        maxGen = Math.max(maxGen, sosaCounter.bitLength());
                    }
                }
            }
        }

        // Format numbers and sort
        if (settings.toBeDisplayed) {
            int nbDigit = (int) (Math.log10(2) * maxGen) + 1;
            DecimalFormat formatGen = new DecimalFormat("00");
            DecimalFormat formatSosa = new DecimalFormat("0000000000000000000000000000000".substring(0, nbDigit));
            for (ViewContext vc : treetops) {
                String[] str = vc.getText().split("%");
                vc.setText("G" + formatGen.format(Integer.valueOf(str[0])) + "-" + formatSosa.format(new BigInteger(str[1])) + " - " + str[2]);
            }

            sortMarkers(treetops);
        }
    }

    private void markTreeBottom(Gedcom gedcom, Indi indiDeCujus, MarkingPanel.Settings settings, List<ViewContext> treebottoms) {

        // Clean gedcom file for all tags
        if (settings.toBeMarked) {
            GedcomUtilities.deleteTags(gedcom, settings.treeBottomTag, Gedcom.INDI, false);
        }

        Set<Indi> viewedIndis = new HashSet<>();
        Set<Fam> viewedFams = new HashSet<>();
        iterate(indiDeCujus, 1, "1", viewedIndis, viewedFams, settings, treebottoms);

        // Format numbers and sort
        if (settings.toBeDisplayed) {
            sortMarkers(treebottoms);
        }
    }

    private void iterate(Indi indi, int gen, String num, Set<Indi> viewedIndis, Set<Fam> viewedFams, MarkingPanel.Settings settings, List<ViewContext> treebottoms) {

        Fam[] fams = indi.getFamiliesWhereSpouse();
        boolean several = fams.length > 1;
        Character suffix = 'a';
        DecimalFormat formatGen = new DecimalFormat("00");

        if (fams.length == 0) {
            if (viewedIndis.contains(indi)) {
                return;
            }
            viewedIndis.add(indi);
            if (settings.toBeMarked) {
                indi.addProperty(settings.treeBottomTag, settings.treeBottomValue);
            }
            if (settings.toBeDisplayed) {
                treebottoms.add(new ViewContext(indi).setText("G" + formatGen.format(gen) + "-" + num + " - " + indi.getDisplayTitle(true)));
            }
            return;
        }

        for (Fam fam : fams) {
            if (viewedFams.contains(fam)) {
                continue;
            }
            viewedFams.add(fam);
            Indi[] children = fam.getChildren();

            if (children.length == 0) {
                Indi husb = fam.getHusband();
                Indi wife = fam.getWife();

                if (husb != null && !viewedIndis.contains(husb)) {
                    viewedIndis.add(husb);
                    if (settings.toBeMarked) {
                        husb.addProperty(settings.treeBottomTag, settings.treeBottomValue + " " + fam.getId());
                    }
                    if (settings.toBeDisplayed) {
                        treebottoms.add(new ViewContext(husb).setText("G" + formatGen.format(gen) + "-" + num + " - " + husb.getDisplayTitle(true)));
                    }
                }
                if (wife != null && !viewedIndis.contains(wife)) {
                    viewedIndis.add(wife);
                    if (settings.toBeMarked) {
                        wife.addProperty(settings.treeBottomTag, settings.treeBottomValue + " " + fam.getId());
                    }
                    if (settings.toBeDisplayed) {
                        treebottoms.add(new ViewContext(wife).setText("G" + formatGen.format(gen) + "-" + num + suffix.toString() + " - " + wife.getDisplayTitle(true)));
                    }
                }
                return;
            }

            for (int c = 0; c < children.length; c++) {
                iterate(children[c], gen + 1, num + (several ? suffix.toString() : "") + (c + 1), viewedIndis, viewedFams, settings, treebottoms);
            }
            suffix++;
        }
    }

    private void markImplex(Gedcom gedcom, MarkingPanel.Settings settings, List<ViewContext> implexes, List<ViewContext> multipleancestors) {

        // Clean gedcom file for all tags
        if (settings.toBeMarked) {
            if (settings.isImplex) {
                GedcomUtilities.deleteTags(gedcom, settings.implexTag, Gedcom.INDI, false);
                GedcomUtilities.deleteTags(gedcom, settings.implexTag, Gedcom.FAM, false);
            }
            if (settings.isMulti) {
                GedcomUtilities.deleteTags(gedcom, settings.multiTag, Gedcom.INDI, false);
            }
        }

        HashMap<Indi, Set<Fam>> ancestors = new HashMap<>(); // will store all multiple ancestors map to the famillies from which their are multiple

        gedcom.getFamilies().forEach((fam) -> {
            Indi husb = fam.getHusband();
            Indi wife = fam.getWife();
            int nbCA = getNbOfCommonAncestor(fam, husb, wife, ancestors);
            if (nbCA > 0) {
                addImplex(fam, implexes, nbCA);
                if (settings.isImplex && settings.toBeMarked) {
                    String str = settings.implexValue + " (" + String.valueOf(nbCA) + ")";
                    fam.addProperty(settings.implexTag, str);
                }
            }
        });
        if (implexes.isEmpty()) {
            implexes.add(new ViewContext(gedcom).setText(NbBundle.getMessage(MarkingAction.class, "MarkingPanel.NoImplexFound")));
        }
        int size = 0;
        String str = "";
        for (Indi indi : ancestors.keySet()) {
            size = ancestors.get(indi).size();
            ViewContext vc = new ViewContext(indi).setText(indi.getDisplayTitle(true) + " (" + size + ")").setCode(String.valueOf(size));
            Property prop1 = indi.getProperty(Indi.TAG_SOSA);
            Property prop2 = indi.getProperty(Indi.TAG_SOSADABOVILLE);
            if (prop1 == null & prop2 == null) {
                vc.setColor(notSosaColor);
            }
            multipleancestors.add(vc);
            if (settings.isMulti && settings.toBeMarked) {
                str = " ";
                List<Fam> sortedList = new ArrayList<>(ancestors.get(indi));
                Collections.sort(sortedList, ((Entity)indi).getComparator());  // use id entity sorter
                for (Fam fam : sortedList) {
                    str += "(" + fam.getId() + ")";
                }
                indi.addProperty(settings.multiTag, settings.multiValue + str);
            }
        }
        if (multipleancestors.isEmpty()) {
            implexes.add(new ViewContext(gedcom).setText(NbBundle.getMessage(MarkingAction.class, "MarkingPanel.NoMultipleAncestorsFound")));
        }

        sortMarkers2(implexes);
        sortMarkers2(multipleancestors);

    }

    private boolean addImplex(Fam fam, List<ViewContext> implexes, int NbOfCommonAncestors) {
        if (fam != null) {
            implexes.add(new ViewContext(fam)
                    .setText(fam.getDisplayFullNames(true) + " ("+NbOfCommonAncestors+")")
                    .setAction(new MarkedAction(fam))
                    .setCode(String.valueOf(NbOfCommonAncestors)));
        }
        return false;
    }

    // Rather than reusing existing algos for isAncestorOf and CommonAncestors, we develop one here
    // because we have to be more efficient and store information along the way
    // in order to avoid recalculating common ancestors for all couples in the genealogy
    private int getNbOfCommonAncestor(Fam fam, Indi indi1, Indi indi2, HashMap<Indi, Set<Fam>>  ancestors) {

        int ret = 0;
        
        if (indi1 == null || indi2 == null) {
            return 0;
        }

        // Get all ancestors once of indi1 and indi2
        Set<Indi> ancestorsOf1 = new HashSet<>();
        getAllAncestors(indi1, ancestorsOf1);
        Set<Indi> ancestorsOf2 = new HashSet<>();
        getAllAncestors(indi2, ancestorsOf2);

        // Scan one set and check presence in the other set
        for (Indi indi : ancestorsOf1) {
            if (ancestorsOf2.contains(indi)) {
                Set<Fam> setOfFam = ancestors.get(indi);
                if (setOfFam == null) {
                    setOfFam = new HashSet<Fam>();
                    ancestors.put(indi, setOfFam);
                }
                setOfFam.add(fam);
                ret++;
            }
        }

        return ret;
    }

    // recursive
    private void getAllAncestors(Indi indi, Set<Indi> ancestors) {

        if (indi == null || ancestors.contains(indi)) {
            return;
        }
        ancestors.add(indi);
        Fam fam = indi.getFamilyWhereBiologicalChild();
        if (fam == null) {
            return;
        }
        getAllAncestors(fam.getHusband(), ancestors);
        getAllAncestors(fam.getWife(), ancestors);

    }

    
    private void markSearch(Gedcom gedcom, MarkingPanel.Settings settings, List<ViewContext> searchIndividuals) {

        Set<Indi> indis = new HashSet<>();
        
        // Get selected individuals and loop through them
        List<Entity> entities = SearchCommunicator.getResultEntities(gedcom);
        switch (settings.searchOption) {
            
            case MarkingPanel.SEARCH_PARENT_OF:
                for (Entity entity : entities) {
                    Set<Indi> subIndis = (Set<Indi>) getIndis(entity);
                    for (Indi indi : subIndis) {
                        Fam fam = indi.getFamilyWhereBiologicalChild();
                        if (fam != null) {
                            Indi husb = fam.getHusband();
                            if (husb != null) {
                                indis.add(husb);
                            }
                            Indi wife = fam.getWife();
                            if (wife != null) {
                                indis.add(wife);
                            }
                        }
                    }
                }
                break;
                
            case MarkingPanel.SEARCH_CHILD_OF:
                for (Entity entity : entities) {
                    Set<Fam> subFams = (Set<Fam>) getFams(entity);
                    for (Fam fam : subFams) {
                        for (Indi child : fam.getChildren()) {
                            indis.add(child);
                        }
                    }
                }
                break;
                
            case MarkingPanel.SEARCH_SPOUSE_OF:
                for (Entity entity : entities) {
                    if (entity instanceof Indi) {
                        Indi indi = (Indi) entity;
                        Fam[] fams = indi.getFamiliesWhereSpouse();
                        for (Fam fam : fams) {
                            Indi spouse = fam.getOtherSpouse(indi);
                            if (spouse != null) {
                                indis.add(spouse);
                            }
                        }
                    }
                }
                break;
                
            case MarkingPanel.SEARCH_ANCESTOR_OF:
                for (Entity entity : entities) {
                    for (Indi ancestor : gedcom.getIndis()) {
                        if (entity instanceof Indi) {
                            if (ancestor.isAncestorOf((Indi) entity)) {
                                indis.add(ancestor);
                            }
                        } else if (entity instanceof Fam) {
                            if (ancestor.isAncestorOf((Fam) entity)) {
                                indis.add(ancestor);
                            }
                        } 
                    }
                }
                break;
                
            case MarkingPanel.SEARCH_DESCENDANT_OF:
                for (Entity entity : entities) {
                    for (Indi ancestor : gedcom.getIndis()) {
                        if (entity instanceof Indi) {
                            if (ancestor.isDescendantOf((Indi) entity)) {
                                indis.add(ancestor);
                            }
                        } else if (entity instanceof Fam) {
                            if (ancestor.isDescendantOf((Fam) entity)) {
                                indis.add(ancestor);
                            }
                        } 
                    }
                }
                break;
                
            case MarkingPanel.SEARCH_INDI:
            default:
                for (Entity entity : entities) {
                    indis.addAll(getIndis(entity));
                }
                break;
        }
        
        // Mark individuals
        for (Indi indi : indis) {
            ViewContext vc = new ViewContext(indi).setText(indi.toString(true));
            searchIndividuals.add(vc);
            if (settings.isSearch && settings.toBeMarked) {
                indi.addProperty(settings.searchTag, settings.searchValue);
            }
        }

        sortMarkers(searchIndividuals);
    }

    private Collection<? extends Indi> getIndis(Entity entity) {
        Collection<Indi> ret = new HashSet<Indi>(); 
        if (entity instanceof Indi) {
            ret.add((Indi) entity);
        } else if (entity instanceof Fam) {
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            if (husb != null) {
                ret.add(husb);
            }
            Indi wife = fam.getWife();
            if (wife != null) {
                ret.add(wife);
            }
        }
        return ret;
    }
    
    private Collection<? extends Fam> getFams(Entity entity) {
        Collection<Fam> ret = new HashSet<Fam>(); 
        if (entity instanceof Fam) {
            ret.add((Fam) entity);
        } else if (entity instanceof Indi) {
            Indi indi = (Indi) entity;
            Fam[] fams = indi.getFamiliesWhereSpouse();
            for (Fam fam : fams) {
                if (fam != null) {
                    ret.add(fam);
                }
            }
        }
        return ret;
    }
    
    
    /**
     * ******************************************************************************
     * Class used by to go up a tree
     */
    private class Pair {

        String ID = "";
        BigInteger sosa = BigInteger.ZERO;

        public Pair(String ID, BigInteger sosa) {
            this.ID = ID;
            this.sosa = sosa;
        }
    }

    // This action only provides the ability to open up the Common Ancestor tool
    // because when selecting a family, the tool already displays what we want.
    private class MarkedAction implements Action {

        private Fam fam = null;
        
        private MarkedAction(Fam fam) {
            this.fam = fam;
        }

        @Override
        public Object getValue(String key) {
            return null;
        }

        @Override
        public void putValue(String key, Object value) {
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        // Place couple people in the common ancestor search fields, and display preview.
        public void actionPerformed(ActionEvent e) {
            commonAncestorComponent = CommonAncestorTopComponent.createInstance(new Context(fam)); // get existing instance if open, create it otherwise
            commonAncestorComponent.setContext(new Context(fam));
        }
        

    }


}
