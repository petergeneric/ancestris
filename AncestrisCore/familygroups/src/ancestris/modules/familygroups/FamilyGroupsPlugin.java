package ancestris.modules.familygroups;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.ActionSaveViewAsGedcom;
import static ancestris.modules.familygroups.Bundle.title;
import ancestris.util.TimingUtility;
import ancestris.util.Utilities;
import genj.fo.Document;
import genj.gedcom.*;
import genj.io.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class FamilyGroupsPlugin extends AncestrisPlugin implements FamilyGroupsRunner {

    private final static Logger log = Logger.getLogger(FamilyGroupsPlugin.class.getName());
    private static final Level logLevel = Level.FINE;
    private Gedcom gedcom;
    private Document document;
    private int minGroupSize = 0;  // Don't print groups with size less than this
    private int maxGroupSize = 0;
    private boolean separateAssos = false;
    private List<FamilyGroupFilter> filters = null;
    
    // Trackable elements
    private int counter = 0, maxCounter = 0;
    private boolean cancel = false;
    private String taskName = "";
    private String state = "";


    // Mandatory constructor (unused)
    public FamilyGroupsPlugin() {
    }
    
    // Used constructor
    public FamilyGroupsPlugin(Gedcom gedcom) {
        this.gedcom = gedcom;
        taskName = NbBundle.getMessage(FamilyGroupsPlugin.class, "CTL_FamilyGroupsTopComponent");
    }
    
    public FamilyGroupsPlugin getFgp() {
        return this;
    }
    

    public Document getDocument() {
        return document;
    }
    
    @Override
    public void run() {
        if (gedcom != null) {
            this.document = start(gedcom);
        }
    }

    @Override
    public void cancelTrackable() {
        cancel = true;
    }

    @Override
    public int getProgress() {
        int progress = 100 * counter / maxCounter;
        if (progress % 5 == 0) {
            log.log(logLevel, "Time=" + TimingUtility.getInstance().getTime() + " - state = " + getState() + " - progress = " + progress + " (" + counter + ")");
        }
        return progress;
    }

    @Override
    public String getState() {
        return NbBundle.getMessage(this.getClass(), state, counter, maxCounter);
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    
    
    
    private Document start(Gedcom gedcom) {
        
        List<Tree> trees = new ArrayList<>();
        Document doc = null;
        Tree treeAssos = new Tree();
        treeAssos.setAsso(true);

        setMinGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("minGroupSize", "2")));
        setMaxGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("maxGroupSize", "20")));
        setAssoSeparation(NbPreferences.forModule(OpenFamilyGroupsAction.class).getBoolean("separateAssos", false));

        String title = title(gedcom.getDisplayName());
        Collection<Indi> indiList = gedcom.getIndis();
        HashSet<Indi> unvisited = new HashSet<>(indiList);

        TimingUtility.getInstance().reset();
        maxCounter = indiList.size() * 20;  // first step represents 5% of total approx. (progress bar cannot be reset)
        counter = 0;
        state = "State.Iteration";
        log.log(logLevel, "Time=" + TimingUtility.getInstance().getTime() + " - Start state = " + getState());
        while (!unvisited.isEmpty()) {
            
            if (cancel) {
                return doc;
            }
            Indi indi = unvisited.iterator().next();

            // start a new sub-tree
            Tree tree = new Tree();

            // collect all relatives
            iterate(indi, tree, treeAssos, unvisited);

            // remember
            if (!tree.isEmpty()) {
                trees.add(tree);
            }
        }
        
        // Sort in descending order by count
        Collections.sort(trees);

        // Add assos at the end if not empty
        if (!treeAssos.isEmpty()) {
            trees.add(treeAssos);
        }

        // Report about groups
        if (!trees.isEmpty()) {
            int grandtotal = 0;
            int loners = 0;
            doc = new Document(title);

            filters = new ArrayList<>(10);

            doc.startSection(title);
            doc.startTable("width=100%, border=1");

            // calculate maxCounter which is the number of indis to display
            counter = 0;
            for (int i = 0; i < trees.size(); i++) {
                Tree tree = trees.get(i);
                if (tree.size() >= getMinGroupSize() && tree.size() <= getMaxGroupSize()) {
                    counter += tree.size();
                }
            }
            maxCounter = counter;
            counter = 0;
            state = "State.Editing";
            log.log(logLevel, "Time=" + TimingUtility.getInstance().getTime() + " - Start state = " + getState());

            // calculate maxCounter
            for (int i = 0; i < trees.size(); i++) {

                Tree tree = trees.get(i);

                // sort group filteredIndis by birth date
                grandtotal += tree.size();
                if (tree.size() < getMinGroupSize()) {
                    loners += tree.size();
                } else {
                    tree.setNb(i+1);
                    doc.nextTableRow("font-size=1.125em, font-weight=bold, line-height=200%");
                    doc.nextTableCell("colspan=6, width=100%");
                    String word = NbBundle.getMessage(this.getClass(), tree.size()>1 ? "FamilyGroupsTopComponent.individual_plural" : "FamilyGroupsTopComponent.individual_singular");
                    doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.groupCount", new Object[]{i+1, tree.size(), word}));

                    doc.nextTableRow();
                    doc.nextTableCell("colspan=6, width=100%");
                    doc.addText(tree.getTitle());
                    if (tree.size() <= getMaxGroupSize()) {

                        for (PropertyPlace PropertyPlace : tree.getPlaces()) {
                            if (PropertyPlace.format(null).length() > 0) {
                                doc.nextTableRow();
                                doc.nextTableCell("colspan=6, width=100%");
                                String str = PropertyPlace.format(null).replaceAll("\\<.*?>", "").replaceAll(",", " ").trim().replaceAll(" ", ", ");
                                doc.addText(str);
                            }
                        }

                        doc.nextTableRow("font-weight=bold");
                        doc.nextTableCell("colspan=2, width=34%");
                        doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.indi_name"));
                        doc.nextTableCell("colspan=2, width=33%");
                        doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.familySpouse"));
                        doc.nextTableCell("colspan=2, width=33%");
                        doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.familyChild"));

                        // Print sorted list of groups
                        for (Indi indi : tree) {
                            counter++;
                            if (cancel) {
                                return doc;
                            }
                            Fam[] familiesWhereChild = indi.getFamiliesWhereChild();
                            Fam[] familiesWhereSpouse = indi.getFamiliesWhereSpouse();
                            int maxRows = Math.max(Math.max(familiesWhereChild.length, familiesWhereSpouse.length),1); // Display people alone if groupsize is 1.

                            for (int index = 0; index < maxRows; index++) {
                                doc.nextTableRow();

                                if (index == 0) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(indi.getId(), indi.getLinkAnchor());
                                    doc.nextTableCell("width=30%");
                                    doc.addText(indi.getLastName(), "font-weight=bold, color=blue");
                                    doc.addText(" " + indi.getFirstName());
                                    doc.addText(" (" + indi.getBirthAsString() + " - " + indi.getDeathAsString() + ")");
                                } else {
                                    doc.nextTableCell("width=4%");
                                    doc.nextTableCell("width=30%");
                                }

                                if (index < familiesWhereSpouse.length) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(familiesWhereSpouse[index].getId(), familiesWhereSpouse[index].getLinkAnchor());
                                    doc.nextTableCell("width=29%");
                                    doc.addText(familiesWhereSpouse[index].getHusband() + " - " + familiesWhereSpouse[index].getWife());
                                } else {
                                    doc.nextTableCell("width=4%");
                                    doc.nextTableCell("width=29%");
                                }

                                if (index < familiesWhereChild.length) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(familiesWhereChild[index].getId(), familiesWhereChild[index].getLinkAnchor());
                                    doc.nextTableCell("width=29%");
                                    doc.addText(familiesWhereChild[index].getHusband() + " - " + familiesWhereChild[index].getWife());
                                } else {
                                    doc.nextTableCell("width=4%");
                                    doc.nextTableCell("width=29%");
                                }
                            }
                        }
                    }

                    FamilyGroupFilter filter = new FamilyGroupFilter(tree);
                    AncestrisPlugin.register(filter);
                    filters.add(filter);
                }
            }
            doc.endTable();

            doc.nextParagraph(" ");
            doc.addText("   ");
            doc.nextParagraph("font-size=1.1em,font-weight=bold,line-height=200%");
            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.grandtotal", grandtotal));
            if (loners > 0) {
                doc.nextParagraph("font-size=1.1em,font-weight=bold,line-height=200%");
                doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.loners", loners, getMinGroupSize()));
            }
            doc.nextParagraph(" ");
            doc.addText("   ");
            doc.nextParagraph("font-size=1em,line-height=200%");
            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.saveas"));
        }

        log.log(logLevel, "Time=" + TimingUtility.getInstance().getTime() + " - End state   = " + getState());
        return doc;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Tree treeAssos, Set<Indi> unvisited) {
        // individuals we need to check
        Stack<Indi> todos = new Stack<>();

        if (unvisited.remove(indi)) {
            todos.add(indi);
        }

        // loop
        while (!todos.isEmpty()) {

            counter++;
        
            Indi todo = todos.pop();
            
            // check the ancestors
            Fam famc = todo.getFamilyWhereBiologicalChild();

            if (famc != null) {
                Indi mother = famc.getWife();
                if (mother != null && unvisited.remove(mother)) {
                    todos.push(mother);
                }

                Indi father = famc.getHusband();

                if (father != null && unvisited.remove(father)) {
                    todos.push(father);
                }
            }

            // check descendants
            Fam[] fams = todo.getFamiliesWhereSpouse();

            for (Fam fam : fams) {
                // Get the family & process the spouse
                Indi spouse = fam.getOtherSpouse(todo);
                if (spouse != null && unvisited.remove(spouse)) {
                    todos.push(spouse);
                }
                // .. and all the kids
                Indi[] children = fam.getChildren();
                for (Indi children1 : children) {
                    if (unvisited.remove(children1)) {
                        todos.push(children1);
                    }
                }
                // next family
            } // continue with to-dos

            
            // add to group
            if (famc == null && (fams == null || fams.length == 0) && separateAssos && isAsso(indi))  {
                treeAssos.add(todo);
            } else {
                tree.add(todo);
            }
        }

        // done
    }
    
    private boolean isAsso(Indi indi) {
        Property[] assoProps = indi.getProperties("ASSO");
        return assoProps.length > 0;
    }


    /**
     * @return the minGroupSize
     */
    public int getMinGroupSize() {
        return minGroupSize;
    }

    /**
     * @param minGroupSize the minGroupSize to set
     */
    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    /**
     * @return the maxGroupSize
     */
    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    /**
     * @return the filters extract actions
     */
    public AbstractAncestrisAction getExtractAction(Gedcom gedcom) {
        return new ActionSaveViewAsGedcom(gedcom, filters);
    }

    /**
     * @return the mark action
     */
    public AbstractAncestrisAction getMarkAction(Gedcom gedcom) {
        return new ActionMark(gedcom, filters);
    }

    /**
     * @param maxGroupSize the maxGroupSize to set
     */
    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    /**
     * @param set : true if we want to group isolated associations into one group
     */
    public void setAssoSeparation(boolean set) {
        this.separateAssos = set;
    }

    public void stop() {
        // Clears filters
        if (filters != null) {
            for (FamilyGroupFilter filter : filters) {
                filter.setTree(null);
                AncestrisPlugin.unregister(filter);
            }
        }
        filters = null;
    }






    
    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet<Indi> implements Comparable<Tree> {

        private int number;
        private Indi oldestIndividual;
        private Indi youngestIndividual;
        private final Set<Indi> filteredIndis = new HashSet<>();
        public Set<Entity> connectedEntities = new HashSet<>();
        private boolean isAsso = false;

        @Override
        public int compareTo(Tree that) {
            return (that).size() - (this).size();
        }

        public void setNb(int i) {
            number = i;
        }

        public int getNb() {
            return number;
        }

        public void setAsso(boolean set) {
            isAsso = set;
        }

        public boolean isAsso() {
            return isAsso;
        }

        public Indi getOldestIndividual() {
            return (oldestIndividual);
        }

        public Indi getYoungestIndividual() {
            return (youngestIndividual);
        }

        public HashSet<PropertyPlace> getPlaces() {
            HashSet<PropertyPlace> propertyPlaces = new HashSet<>();

            Iterator<Indi> entityIterator = this.iterator();
            while (entityIterator.hasNext()) {
                Indi indi = entityIterator.next();
                List<Property> findProperties = indi.findProperties(Pattern.compile("PLAC"), null);
                Iterator<Property> indiPlacesIt = findProperties.iterator();
                while (indiPlacesIt.hasNext()) {
                    boolean found = false;
                    Property findProperty = indiPlacesIt.next();
                    Iterator<PropertyPlace> propertyPlacesIt = propertyPlaces.iterator();
                    while (propertyPlacesIt.hasNext()) {
                        PropertyPlace propertyPlace = propertyPlacesIt.next();
                        if (propertyPlace.compareTo(findProperty) == 0) {
                            found = true;
                        }
                    }
                    if (found == false) {
                        // ClassCastException if tag PLAC is used at a forbidden GEDCOM place => SimplePropertyValue and not a PropertyPlace
                        if (findProperty instanceof PropertyPlace) {
                            propertyPlaces.add((PropertyPlace) findProperty);
                        }
                    }
                }
            }
            return propertyPlaces;
        }

        @Override
        public String toString() {
            return oldestIndividual.getId() + " " + getTitle();
        }

        public String getTitle() {
            if (!isAsso) {
                return NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.treeTitle",
                    new Object[]{oldestIndividual.getName(),
                        oldestIndividual.getBirthAsString().length() > 0 ? oldestIndividual.getBirthAsString() : "-",
                        oldestIndividual.getDeathAsString().length() > 0 ? oldestIndividual.getDeathAsString() : "-",
                        youngestIndividual.getName(),
                        youngestIndividual.getBirthAsString().length() > 0 ? youngestIndividual.getBirthAsString() : "-",
                        youngestIndividual.getDeathAsString().length() > 0 ? youngestIndividual.getDeathAsString() : "-"});
            } else {
                return NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.assoTreeTitle");
            }
        }

        @Override
        public boolean add(Indi indi) {
            // check if oldest
            if (isOldest(indi)) {
                oldestIndividual = indi;
            }

            // check if youngest
            if (isYoungest(indi)) {
                youngestIndividual = indi;
            }

            // continue
            addEntity(indi);
            return super.add(indi);
        }

        private void addEntity(Indi indi) {
            filteredIndis.add(indi);
        }

        public boolean hasEntity(Entity e) {
            return filteredIndis.contains(e);
        }

        private boolean isOldest(Indi indi) {
            long jd;
            try {
                jd = oldestIndividual.getBirthDate().getStart().getJulianDay();
            } catch (GedcomException | NullPointerException t) {
                return true;
            }
            try {
                return indi.getBirthDate().getStart().getJulianDay() < jd;
            } catch (GedcomException | NullPointerException t) {
                return false;
            }

        }

        private boolean isYoungest(Indi indi) {
            long jd;
            try {
                jd = youngestIndividual.getBirthDate().getStart().getJulianDay();
            } catch (GedcomException | NullPointerException t) {
                return true;
            }
            try {
                return indi.getBirthDate().getStart().getJulianDay() > jd;
            } catch (GedcomException | NullPointerException t) {
                return false;
            }

        }

    } //Tree

    private static class FamilyGroupFilter implements Filter {

        private Tree tree;
        private final static int SIZEMAX = 90;

        public FamilyGroupFilter(Tree tree) {
            this.tree = tree;
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }

        @Override
        public String getFilterName() {
            String text = NbBundle.getMessage(FamilyGroupFilter.class, "TTL_Filter", tree.size(), tree.getNb(), tree.getOldestIndividual());
            if (text.length() > SIZEMAX) {
                int index = text.substring(0, SIZEMAX).lastIndexOf(" ");
                text = text.substring(0, index) + "...";
            }
            return text;
        }

        /**
         * Include all filteredIndis which depend on at least one Indi which is in the tree (uses utility)
         *
         * @param entity
         * @return
         */
        @Override
        public boolean veto(Entity entity) {
            // let submitter through if it's THE one
            if (entity == entity.getGedcom().getSubmitter()) {
                return false;
            }
            // Check if belongs to connected entities
            calculateIndis();
            return !tree.connectedEntities.contains(entity);
        }

        /**
         * Exclude properties that reference individuals which are not part of
         * the tree
         *
         * @param property
         * @return
         */
        @Override
        public boolean veto(Property property) {
            if (property instanceof PropertyXRef) {
                PropertyXRef xref = (PropertyXRef) property;
                if (xref.isValid() && !tree.connectedEntities.contains(xref.getTargetEntity())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            if (tree == null) {
                return false;
            }
            return tree.oldestIndividual.getGedcom().equals(gedcom);
        }

        private void calculateIndis() {
            if (tree.connectedEntities.isEmpty()) {
                for (Indi indi : tree.filteredIndis) {
                    tree.connectedEntities.addAll(Utilities.getDependingEntitiesRecursively(indi, tree.filteredIndis));
                }
            }
        }

        
        @Override
        public int getIndividualsCount() {
            if (tree.connectedEntities.isEmpty()) {
                calculateIndis();
            }
            int sum = 0;
            for (Entity ent : tree.connectedEntities) {
                if (ent instanceof Indi) {
                    sum++;
                }
            }
            return sum;
        }
    }



}
