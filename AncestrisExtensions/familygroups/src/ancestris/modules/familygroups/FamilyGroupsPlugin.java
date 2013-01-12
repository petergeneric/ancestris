package ancestris.modules.familygroups;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.fo.Document;
import genj.gedcom.*;
import genj.io.Filter;
import java.util.*;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class FamilyGroupsPlugin extends AncestrisPlugin {

    private int minGroupSize = 0;  // Don't print groups with size less than this
    private int maxGroupSize = 0;
    private List<FamilyGroupFilter> filters = null;

    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet<Indi> implements Comparable<Tree> {

        private Indi oldestIndividual;
        private Indi youngestIndividual;
        private HashSet<Entity> entities = new HashSet<Entity>();

        @Override
        public int compareTo(Tree that) {
            return (that).size() - (this).size();
        }

        public Indi getOldestIndividual() {
            return (oldestIndividual);
        }

        public Indi getYoungestIndividual() {
            return (youngestIndividual);
        }

        public HashSet<PropertyPlace> getPlaces() {
            HashSet<PropertyPlace> propertyPlaces = new HashSet<PropertyPlace>();

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
                        propertyPlaces.add((PropertyPlace) findProperty);
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
            return NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.treeTitle",
                    new Object[]{oldestIndividual.getName(),
                        oldestIndividual.getBirthAsString().length() > 0?oldestIndividual.getBirthAsString():"-",
                        oldestIndividual.getDeathAsString().length() > 0?oldestIndividual.getDeathAsString():"-",
                        youngestIndividual.getName(),
                        youngestIndividual.getBirthAsString().length() > 0?youngestIndividual.getBirthAsString():"-",
                        youngestIndividual.getDeathAsString().length() > 0?youngestIndividual.getDeathAsString():"-"});
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

        public void addEntity(Entity entity) {
            entities.add(entity);
        }

        public boolean hasEntity(Entity e) {
            return entities.contains(e);
        }

        private boolean isOldest(Indi indi) {
            long jd;
            try {
                jd = oldestIndividual.getBirthDate().getStart().getJulianDay();
            } catch (Throwable t) {
                return true;
            }
            try {
                return indi.getBirthDate().getStart().getJulianDay() < jd;
            } catch (Throwable t) {
                return false;
            }

        }

        private boolean isYoungest(Indi indi) {
            long jd;
            try {
                jd = youngestIndividual.getBirthDate().getStart().getJulianDay();
            } catch (Throwable t) {
                return true;
            }
            try {
                return indi.getBirthDate().getStart().getJulianDay() > jd;
            } catch (Throwable t) {
                return false;
            }

        }
    } //Tree

    private static class FamilyGroupFilter implements Filter {

        private Tree tree;

        public FamilyGroupFilter(Tree tree) {
            this.tree = tree;
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }

        @Override
        public String getFilterName() {
            return NbBundle.getMessage(FamilyGroupFilter.class, "TTL_Filter",
                    tree.size(), tree.getTitle());
        }

        @Override
        public boolean veto(Property property) {
            return false;
        }

        @Override
        public boolean veto(Entity entity) {
            if (entity instanceof Indi || entity instanceof Fam) {
                return !tree.hasEntity(entity);
            }
            for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
                if (xref.isValid() && tree.hasEntity(xref.getTargetEntity())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            if (tree == null) {
                return false;
            }
            return tree.oldestIndividual.getGedcom().equals(gedcom);
        }
    }

    public Document start(Gedcom myGedcom) {
        List<Tree> trees = new ArrayList<Tree>();
        Document doc = null;
        Entity[] _indis = myGedcom.getEntities(Gedcom.INDI, "INDI:NAME");

        setMinGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("minGroupSize", "2")));
        setMaxGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("maxGroupSize", "20")));

        // FIXME: must be redesigned!!
        List<Indi> indiList = new ArrayList<Indi>(_indis.length);

        for (Entity indi : _indis) {
            if (indi instanceof Indi) {
                indiList.add((Indi) indi);
            }
        }

        HashSet<Indi> unvisited = new HashSet<Indi>(indiList);
        HashSet<Indi> allIndis = new HashSet<Indi>(indiList);

//          println(String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.fileheader"), myGedcom.getName()));
        while (!unvisited.isEmpty()) {
            Indi indi = unvisited.iterator().next();

            // start a new sub-tree
            Tree tree = new Tree();

            // indi has been visited now
            unvisited.remove(indi);

            // collect all relatives
            iterate(indi, tree, allIndis);

            // remember
            trees.add(tree);
        }

        // Report about groups
        if (!trees.isEmpty()) {
            int grandtotal = 0;
            int loners = 0;
            doc = new Document(NbBundle.getMessage(this.getClass(), "CTL_OpenFamilyGroups"));

            filters = new ArrayList<FamilyGroupFilter>(10);


            doc.startSection(NbBundle.getMessage(this.getClass(), "CTL_OpenFamilyGroups"));
            doc.startTable("width=100%, border=1");

            // Sort in descending order by count
            Collections.sort(trees);

            for (int i = 0; i < trees.size(); i++) {

                Tree tree = trees.get(i);

                // sort group entities by birth date
                grandtotal += tree.size();
                if (tree.size() < getMinGroupSize()) {
                    loners += tree.size();
                } else {
                    doc.nextTableRow("font-size=1.125em, font-weight=bold, line-height=200%");
                    doc.nextTableCell("colspan=6, width=100%");
                    doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.groupCount", new Object[]{i, tree.size()}));

                    doc.nextTableRow();
                    doc.nextTableCell("colspan=6, width=100%");
                    doc.addText(tree.getTitle());
                    if (tree.size() < getMaxGroupSize()) {

                        for (Iterator<PropertyPlace> it = tree.getPlaces().iterator(); it.hasNext();) {
                            PropertyPlace PropertyPlace = it.next();
                            if (PropertyPlace.format(null).length() > 0) {
                                doc.nextTableRow();
                                doc.nextTableCell("colspan=6, width=100%");
                                doc.addText(PropertyPlace.format(null));
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
                        Iterator<Indi> it = tree.iterator();
                        while (it.hasNext()) {
                            Indi indi = it.next();
                            Fam[] familiesWhereChild = indi.getFamiliesWhereChild();
                            Fam[] familiesWhereSpouse = indi.getFamiliesWhereSpouse();
                            int maxRows = Math.max(familiesWhereChild.length, familiesWhereSpouse.length);

                            for (int index = 0; index < maxRows; index++) {
                                doc.nextTableRow();

                                if (index == 0) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(indi.getId(), indi.getAnchor());
                                    doc.nextTableCell("width=30%");
                                    doc.addText(indi.getLastName(), "font-weight=bold");
                                    doc.addText(" " + indi.getFirstName());
                                    doc.addText(" (" + indi.getBirthAsString() + " - " + indi.getDeathAsString() + ")");
                                } else {
                                    doc.nextTableCell("width=4%");
                                    doc.nextTableCell("width=30%");
                                }

                                if (index < familiesWhereSpouse.length) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(familiesWhereSpouse[index].getId(), familiesWhereSpouse[index].getAnchor());
                                    doc.nextTableCell("width=29%");
                                    doc.addText(familiesWhereSpouse[index].getHusband() + " - " + familiesWhereSpouse[index].getWife());
                                } else {
                                    doc.nextTableCell("width=4%");
                                    doc.nextTableCell("width=29%");
                                }

                                if (index < familiesWhereChild.length) {
                                    doc.nextTableCell("width=4%");
                                    doc.addLink(familiesWhereChild[index].getId(), familiesWhereChild[index].getAnchor());
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

            doc.nextParagraph("font-size=1.25em,line-height=200%");
            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.grandtotal", grandtotal));

            if (loners > 0) {

                doc.nextParagraph("font-size=1.25em, ,line-height=200%");
                doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.loners", loners, getMinGroupSize()));
            }
        }

        return doc;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Set<Indi> unvisited) {
        // individuals we need to check
        Stack<Indi> todos = new Stack<Indi>();

        if (unvisited.remove(indi)) {
            todos.add(indi);
        }

        // loop
        while (!todos.isEmpty()) {

            Indi todo = todos.pop();

            // belongs to group
            tree.add(todo);

            // check the ancestors
            Fam famc = todo.getFamilyWhereBiologicalChild();

            if (famc != null) {
                tree.addEntity(famc);
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

            for (int f = 0; f < fams.length; f++) {

                // Get the family & process the spouse
                Fam fam = fams[f];
                tree.addEntity(fam);
                Indi spouse = fam.getOtherSpouse(todo);

                if (spouse != null && unvisited.remove(spouse)) {
                    todos.push(spouse);
                } // .. and all the kids
                Indi[] children = fam.getChildren();

                for (int c = 0; c < children.length; c++) {
                    if (unvisited.remove(children[c])) {
                        todos.push(children[c]);
                    }
                }

                // next family
            }

            // continue with to-dos
        }

        // done
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
     * @param maxGroupSize the maxGroupSize to set
     */
    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    private void resetFilters() {
        // Clears filters
        if (filters != null) {
            for (FamilyGroupFilter filter : filters) {
                filter.setTree(null);
                AncestrisPlugin.unregister(filter);
            }
        }
        filters = null;
    }
}
