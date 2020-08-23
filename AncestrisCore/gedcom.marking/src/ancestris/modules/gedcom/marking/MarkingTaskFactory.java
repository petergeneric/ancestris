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
import ancestris.modules.commonAncestor.CommonAncestorTopComponent;
import ancestris.modules.document.view.HyperLinkTextDocumentView;
import ancestris.modules.document.view.WidgetDocumentView;
import ancestris.util.TimingUtility;
import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class MarkingTaskFactory {

    public static MarkingTask create(Context context, Indi indiDeCujus, MarkingPanel.Settings settings) {
        return new Impl(context, indiDeCujus, settings);
    }

    private static class Impl implements MarkingTask {

        private final static Logger log = Logger.getLogger(MarkingTaskFactory.class.getName());
        private HyperLinkTextDocumentView summary = null;
        private final String LINESTR = "====================================================================================================";
        private Gedcom gedcom = null;
        private Context context = null;
        private Indi indi = null;
        private MarkingPanel.Settings settings = null;

        private int progress = 0;
        private int counter = 0, maxCounter = 0;
        private boolean cancel = false;
        private String taskName = "";
        private String state = "";

        private Color notSosaColor = new Color(153, 0, 255);

        private Impl(Context context, Indi indiDeCujus, MarkingPanel.Settings settings) {
            this.context = context;
            this.gedcom = context.getGedcom();
            this.indi = indiDeCujus;
            this.settings = settings;
            this.taskName = NbBundle.getMessage(MarkingAction.class, "MarkingAction.AskParams");

            maxCounter = (settings.isTreeTop ? 3 : 0)
                    + (settings.isTreeBottom ? 3 : 0)
                    + (settings.isImplex || settings.isMulti ? 4 : 0)
                    + (settings.isSearch ? 3 : 0);

        }

        @Override
        public void run() {
            mark(context, indi, settings);
        }

        @Override
        public void cancelTrackable() {
            cancel = true;
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public String getTaskName() {
            return taskName;
        }

        @Override
        public String getState() {
            return state;
        }

        /**
         * Update progress bar.
         *
         * @param progress current progress
         * @return true if progressbar upgraded, false if cancelled or finished.
         */
        public boolean setProgress(String name, int p) {
            state = name != null ? name : getTaskName();
            progress = p * 100 / maxCounter;
            log.log(Level.FINE, TimingUtility.getInstance().getTime() + " - step name = "+name);
            return true;
        }

        // Tree top individuals are relative to a deCujus individual, otherwise all isolated people would be marked which is probably not what we want => need an individual
        // Tree bottom individuals are relative to a root individual, in a descending tree logic => need an individual
        // Implex people are individuals with descendants who have kids together, regardless of a root individual. => no need for an intial individual
        private void mark(Context context, Indi indiDeCujus, final MarkingPanel.Settings settings) {

            counter = 1;
            TimingUtility.getInstance().reset(); 

            List<ViewContext> treetops = new ArrayList<>();
            List<ViewContext> treebottoms = new ArrayList<>();
            List<ViewContext> implexes = new ArrayList<>(); // = married cousins
            List<ViewContext> multipleancestors = new ArrayList<>();
            List<ViewContext> searchIndividuals = new ArrayList<>();

            // Mark tree top individuals
            if (settings.isTreeTop) {
                markTreeTop(gedcom, indiDeCujus, settings, treetops, NbBundle.getMessage(this.getClass(), "MarkingPanel.jCheckBoxTreeTop.text"));
            }
            if (cancel) {
                return;
            }

            // Mark tree bottom individuals
            if (settings.isTreeBottom) {
                markTreeBottom(gedcom, indiDeCujus, settings, treebottoms, NbBundle.getMessage(this.getClass(), "MarkingPanel.jCheckBoxTreeBottom.text"));
            }
            if (cancel) {
                return;
            }

            // Mark implex people.
            if (settings.isImplex || settings.isMulti) {
                markImplex(gedcom, settings, implexes, multipleancestors, NbBundle.getMessage(this.getClass(), "MarkingPanel.jCheckBoxImplex.text"));
            }
            if (cancel) {
                return;
            }

            // Mark selected individuals
            if (settings.isSearch) {
                markSearch(gedcom, settings, searchIndividuals, NbBundle.getMessage(this.getClass(), "MarkingPanel.jCheckBoxSearch.text"));
            }
            if (cancel) {
                return;
            }

            //================================================================================================================================
            // At completion : Display lists or display message
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    boolean shown = false;
            
                    if (settings.toBeDisplayed) {
                        summary = new HyperLinkTextDocumentView(new Context(gedcom), taskName, taskName);
                        summary.add(taskName + "\n");
                        summary.add(LINESTR.substring(0, taskName.length())+"\n");
                    }
                    
                    if (settings.isTreeTop && treetops.size() > 0 && settings.toBeDisplayed) {
                        showDocument(gedcom, treetops, "MarkingPanel.jCheckBoxTreeTop.text", treetops.size(), context.toString());
                        shown = true;
                    }

                    if (settings.isTreeBottom && treebottoms.size() > 0 && settings.toBeDisplayed) {
                        showDocument(gedcom, treebottoms, "MarkingPanel.jCheckBoxTreeBottom.text", treebottoms.size(), context.toString());
                        shown = true;
                    }

                    if (settings.isImplex && implexes.size() > 0 && settings.toBeDisplayed) {
                        // Activate common ancestor tool
                        CommonAncestorTopComponent.createInstance(context);
                        // Show list
                        showDocument(gedcom, implexes, "MarkingPanel.jCheckBoxImplex.text", implexes.size(), context.getGedcom().getDisplayName());
                        shown = true;
                    }

                    if (settings.isMulti && multipleancestors.size() > 0 && settings.toBeDisplayed) {
                        showDocument(gedcom, multipleancestors, "MarkingPanel.jCheckBoxMulti.text", multipleancestors.size(), context.getGedcom().getDisplayName());
                        shown = true;
                    }

                    if (settings.isSearch && searchIndividuals.size() > 0 && settings.toBeDisplayed) {
                        showDocument(gedcom, searchIndividuals, "MarkingPanel.jCheckBoxSearch.text", searchIndividuals.size(), context.getGedcom().getDisplayName() + " | " + NbBundle.getMessage(MarkingAction.class, settings.searchOption));
                        shown = true;
                    }

                    if (!settings.toBeDisplayed && settings.toBeMarked) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(MarkingAction.class, "MarkingAction.Done"), NotifyDescriptor.INFORMATION_MESSAGE));
                    }

                    if (settings.toBeDisplayed && !shown) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(MarkingAction.class, "MarkingAction.Nothing"), NotifyDescriptor.INFORMATION_MESSAGE));
                    }

                    if (settings.toBeDisplayed) {
                        String optionsStr = "\n" + NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.options.text");
                        summary.add(optionsStr+"\n");
                        summary.add(LINESTR.substring(0, optionsStr.length())+"\n");
                        summary.add(settings.displaySettings());
                        summary.add("\n\n");
                    }
                    
                }
            });
            
        }

        private void showDocument(Gedcom gedcom, Object list, String text, int size, String context) {
            Object object = new ContextListWidget((List<Context>) list);
            String title = NbBundle.getMessage(MarkingAction.class, text);
            String message1 = NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.title", size, title);
            String message2 = NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.tip", title, context);
            summary.add("\n* "+ NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.tab.text") 
                    + " : " + message1 + "\n   - " + message2 + "\n     " + size 
                    + " " + NbBundle.getMessage(MarkingAction.class, "MarkingPanel.Marked.found.text") + ".\n\n");
            new WidgetDocumentView(new Context(gedcom), message1, message2, ((JComponent) object));
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

        private int maxGen = 0;

        private void markTreeTop(Gedcom gedcom, Indi indiDeCujus, MarkingPanel.Settings settings, List<ViewContext> treetops, String taskName) {

            setProgress(taskName, counter);

            // Clean gedcom file for all tags (use specific delete method to make it more efficient than GedcomUtilities.deleteTags)
            if (settings.toBeErased) {
                deleteTags(gedcom, Gedcom.INDI, settings.treeTopTag);
            }
            setProgress(taskName + " 1/3", counter++);

            maxGen = 0;
            Set<Indi> viewedIndis = new HashSet<>();
            iterateUp(indiDeCujus, 1, BigInteger.ONE, viewedIndis, settings, treetops);
            setProgress(taskName + " 2/3", counter++);

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
            setProgress(taskName + " 3/3", counter++);
        }

        private void iterateUp(Indi indi, int gen, BigInteger sosa, Set<Indi> viewedIndis, MarkingPanel.Settings settings, List<ViewContext> treetops) {

            // Get father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                Indi parent = famc.getHusband();
                if (parent != null) {
                    iterateUp(parent, gen + 1, sosa.shiftLeft(1), viewedIndis, settings, treetops);
                }
                parent = famc.getWife();
                if (parent != null) {
                    iterateUp(parent, gen + 1, sosa.shiftLeft(1).add(BigInteger.ONE), viewedIndis, settings, treetops);
                }
            } else {
                if (!viewedIndis.contains(indi)) {  // we only need each treetop individual once
                    viewedIndis.add(indi);
                    if (settings.toBeMarked) {
                        indi.addProperty(settings.treeTopTag, settings.treeTopValue);
                    }
                    if (settings.toBeDisplayed) {
                        treetops.add(new ViewContext(indi).setText(sosa.bitLength() + "%" + sosa + "%" + indi.getDisplayTitle(true)));
                        maxGen = Math.max(maxGen, sosa.bitLength());
                    }
                }
            }
        }

        private void markTreeBottom(Gedcom gedcom, Indi indiDeCujus, MarkingPanel.Settings settings, List<ViewContext> treebottoms, String taskName) {

            setProgress(taskName, counter);

            // Clean gedcom file for all tags
            if (settings.toBeErased) {
                deleteTags(gedcom, Gedcom.INDI, settings.treeBottomTag);
            }
            setProgress(taskName + " 1/3", counter++);

            Set<Indi> viewedIndis = new HashSet<>();
            Set<Fam> viewedFams = new HashSet<>();
            iterateDown(indiDeCujus, 1, "1", viewedIndis, viewedFams, settings, treebottoms);
            setProgress(taskName + " 2/3", counter++);

            // Format numbers and sort
            if (settings.toBeDisplayed) {
                sortMarkers(treebottoms);
            }
            setProgress(taskName + " 3/3", counter++);
        }

        private void iterateDown(Indi indi, int gen, String num, Set<Indi> viewedIndis, Set<Fam> viewedFams, MarkingPanel.Settings settings, List<ViewContext> treebottoms) {

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
                    iterateDown(children[c], gen + 1, num + (several ? suffix.toString() : "") + (c + 1), viewedIndis, viewedFams, settings, treebottoms);
                }
                suffix++;
            }
        }

        private void markImplex(Gedcom gedcom, MarkingPanel.Settings settings, List<ViewContext> implexes, List<ViewContext> multipleancestors, String taskName) {

            setProgress(taskName, counter);

            // Clean gedcom file for all tags
            if (settings.toBeErased) {
                if (settings.isImplex) {
                    deleteTags(gedcom, Gedcom.FAM, settings.implexTag);
                }
                if (settings.isMulti) {
                    deleteTags(gedcom, Gedcom.INDI, settings.multiTag);
                }
            }
            setProgress(taskName + " 1/4", counter++);

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
            setProgress(taskName + " 2/4", counter++);

            if (implexes.isEmpty()) {
                implexes.add(new ViewContext(gedcom).setText(NbBundle.getMessage(MarkingAction.class, "MarkingPanel.NoImplexFound")));
            }
            int size = 0;
            String str = "", strMax = "";
            int ctr = 0;
            for (Indi indi : ancestors.keySet()) {
                size = ancestors.get(indi).size();
                ViewContext vc = new ViewContext(indi).setText(indi.getDisplayTitle(true) + " (" + size + ")").setCode(String.valueOf(size));
                Property prop1 = indi.getProperty(Indi.TAG_SOSA);
                Property prop2 = indi.getProperty(Indi.TAG_SOSADABOVILLE);
                if (prop1 == null & prop2 == null) {
                    vc.setColor(notSosaColor);
                }
                multipleancestors.add(vc);
                if (settings.isMulti && settings.toBeMarked) { // remember first 20 fams only
                    str = settings.multiValue + " ";
                    strMax = "";
                    ctr = 0;
                    ArrayList<String> ids = new ArrayList<>();
                    for (Fam fam : ancestors.get(indi)) {
                        ids.add(fam.getId());
                        if (ctr++ > 20) {
                            strMax = "...";
                            break;
                        }
                    }
                    Collections.sort(ids);
                    for (String s : ids) {
                        str += "(" + s + ")";
                    }
                    str += strMax;
                    indi.addProperty(settings.multiTag, str);
                }
            }
            setProgress(taskName + " 3/4", counter++);

            if (multipleancestors.isEmpty()) {
                implexes.add(new ViewContext(gedcom).setText(NbBundle.getMessage(MarkingAction.class, "MarkingPanel.NoMultipleAncestorsFound")));
            }

            sortMarkers2(implexes);
            sortMarkers2(multipleancestors);
            setProgress(taskName + " 4/4", counter++);

        }

        private boolean addImplex(Fam fam, List<ViewContext> implexes, int NbOfCommonAncestors) {
            if (fam != null) {
                implexes.add(new ViewContext(fam)
                        .setText(fam.getDisplayFullNames(true) + " (" + NbOfCommonAncestors + ")")
                        .setAction(new MarkedAction(fam))
                        .setCode(String.valueOf(NbOfCommonAncestors)));
            }
            return false;
        }

        // Rather than reusing existing algos for isAncestorOf and CommonAncestors, we develop one here
        // because we have to be more efficient and store information along the way
        // in order to avoid recalculating common ancestors for all couples in the genealogy
        private int getNbOfCommonAncestor(Fam fam, Indi indi1, Indi indi2, HashMap<Indi, Set<Fam>> ancestors) {

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

        // recursive
        private void getAllDescendants(Indi indi, Set<Indi> descendants) {

            if (indi == null || descendants.contains(indi)) {
                return;
            }
            descendants.add(indi);
            Fam[] fams = indi.getFamiliesWhereSpouse();
            for (Fam fam : fams) {
                for (Indi child : fam.getChildren()) {
                    getAllDescendants(child, descendants);
                }
            }
        }

        // recursive
        private void getYDNALine(Indi indi, Set<Indi> YIndis) {

            if (indi == null || YIndis.contains(indi)) {
                return;
            }
            YIndis.add(indi);

            // Go to father
            Fam fam = indi.getFamilyWhereBiologicalChild();
            if (fam != null) {
                Indi husb = fam.getHusband();
                if (husb != null) {
                    getYDNALine(husb, YIndis);
                }
            }
            
            // Go to each male child
            Indi[] children = indi.getChildren();
            for (Indi child : children) {
                if (child.getSex() == PropertySex.MALE) {
                    getYDNALine(child, YIndis);
                }
            }

        }

        // recursive
        private void getmtDNALine(Indi indi, Set<Indi> mtIndis) {

            if (indi == null || mtIndis.contains(indi)) {
                return;
            }
            mtIndis.add(indi);

            // Go to mother
            Fam fam = indi.getFamilyWhereBiologicalChild();
            if (fam != null) {
                Indi wife = fam.getWife();
                if (wife != null) {
                    getmtDNALine(wife, mtIndis);
                }
            }
            
            // Go to each female child and iterate, but do not iterate on males, just ad them
            Indi[] children = indi.getChildren();
            for (Indi child : children) {
                if (child.getSex() == PropertySex.FEMALE) {
                    getmtDNALine(child, mtIndis);
                } else {
                    mtIndis.add(child);
                }
            }

        }

        private void markSearch(Gedcom gedcom, MarkingPanel.Settings settings, List<ViewContext> searchIndividuals, String taskName) {

            setProgress(taskName, counter);

            // Clean gedcom file for all tags
            if (settings.toBeErased) {
                deleteTags(gedcom, Gedcom.INDI, settings.searchTag);
            }
            setProgress(taskName + " 1/3", counter++);

            Set<Indi> indis = new HashSet<>();
            Set<Fam> famSet = new HashSet<>();
            Fam[] famArr = null;

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
                            if (famSet.contains(fam)) {
                                continue;
                            }
                            famSet.add(fam);
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
                            famArr = indi.getFamiliesWhereSpouse();
                            for (Fam fam : famArr) {
                                if (famSet.contains(fam)) {
                                    continue;
                                }
                                famSet.add(fam);
                                Indi spouse = fam.getOtherSpouse(indi);
                                if (spouse != null && !indis.contains(spouse)) {
                                    indis.add(spouse);
                                }
                            }
                        }
                    }
                    break;

                case MarkingPanel.SEARCH_ANCESTOR_OF:
                    for (Entity entity : entities) {
                        if (indis.contains(entity)) {
                            continue;
                        }
                        for (Indi indi : getIndis(entity)) {
                            getAllAncestors((Indi) entity, indis);
                        }
                    }
                    break;

                case MarkingPanel.SEARCH_DESCENDANT_OF:
                    for (Entity entity : entities) {
                        if (indis.contains(entity)) {
                            continue;
                        }
                        for (Indi indi : getIndis(entity)) {
                            getAllDescendants((Indi) entity, indis);
                        }
                    }
                    break;

                case MarkingPanel.SEARCH_PATRILINE_OF:
                    for (Entity entity : entities) {
                        if (indis.contains(entity)) {
                            continue;
                        }
                        for (Indi indi : getIndis(entity)) {
                            getYDNALine((Indi) entity, indis);
                        }
                    }
                    break;

                case MarkingPanel.SEARCH_MATRILINE_OF:
                    for (Entity entity : entities) {
                        if (indis.contains(entity)) {
                            continue;
                        }
                        for (Indi indi : getIndis(entity)) {
                            getmtDNALine((Indi) entity, indis);
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
            setProgress(taskName + " 2/3", counter++);

            // Mark individuals
            for (Indi indi : indis) {
                ViewContext vc = new ViewContext(indi).setText(indi.toString(true));
                searchIndividuals.add(vc);
                if (settings.isSearch && settings.toBeMarked) {
                    indi.addProperty(settings.searchTag, settings.searchValue);
                }
            }

            sortMarkers(searchIndividuals);
            setProgress(taskName + " 3/3", counter++);
        }

        private Collection<? extends Indi> getIndis(Entity entity) {
            Collection<Indi> ret = new HashSet<>();
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
            Collection<Fam> ret = new HashSet<>();
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

        private void deleteTags(Gedcom gedcom, String entityTag, String propertyTag) {
            for (Entity entity : gedcom.getEntities(entityTag)) {
                for (Property prop : entity.getProperties(propertyTag)) {
                    entity.delProperty(prop);
                }
            }
        }

    }

    // This action only provides the ability to open up the Common Ancestor tool
    // because when selecting a family, the tool already displays what we want.
    private static class MarkedAction implements Action {

        private CommonAncestorTopComponent commonAncestorComponent = null;
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
