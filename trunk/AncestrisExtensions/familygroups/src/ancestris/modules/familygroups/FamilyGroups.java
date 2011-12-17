package ancestris.modules.familygroups;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class FamilyGroups {

    private Context context;
    private int minGroupSize = 0;  // Don't print groups with size less than this
    private int maxGroupSize = 0;
    private String CurrentId = null;
    private List<FamilyGroupFilter> filters = null;

    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet<Indi> implements Comparable<Tree> {

        private Indi oldestIndividual;
        private HashSet<Entity> entities = new HashSet<Entity>();

        @Override
        public int compareTo(Tree that) {
            return (that).size() - (this).size();
        }

        public Indi getOldestIndividual() {
            return (oldestIndividual);
        }

        @Override
        public String toString() {
            return oldestIndividual.getId() + " " + getTitle();
        }

        public String getTitle() {
            return oldestIndividual.getName()
                    + " (" + oldestIndividual.getBirthAsString() + "-"
                    + oldestIndividual.getDeathAsString() + ")";
        }

        @Override
        public boolean add(Indi indi) {
            // check if oldest
            if (isOldest(indi)) {
                oldestIndividual = indi;
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

            doc = new Document(NbBundle.getMessage(this.getClass(), "CTL_OpenFamilyGroups"));
            doc.startSection(NbBundle.getMessage(this.getClass(), "CTL_OpenFamilyGroups"));

            // Sort in descending order by count
            Collections.sort(trees);

            filters = new ArrayList<FamilyGroupFilter>(10);

            // Print sorted list of groups
            doc.startTable("width=100%, border=1");
            doc.addTableColumn("column-width=10%");
            doc.addTableColumn("column-width=10%");
            doc.addTableColumn("column-width=80%");
            doc.nextTableRow("font-weight=bold");
            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.count"));
            doc.nextTableCell();
            doc.addText("ID");
            doc.nextTableCell();
            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.indi_name"));

            int grandtotal = 0;
            int loners = 0;
            for (int i = 0; i < trees.size(); i++) {

                Tree tree = trees.get(i);

                // sort group entities by birth date
                grandtotal += tree.size();
                if (tree.size() < getMinGroupSize()) {
                    loners += tree.size();
                } else {
                    if (tree.size() < getMaxGroupSize()) {

                        String prefix = "" + tree.size();
                        Iterator it = tree.iterator();
                        while (it.hasNext()) {
                            Indi indi = (Indi) it.next();

                            doc.nextTableRow();
                            doc.addText(prefix);
                            doc.nextTableCell();
                            doc.addLink(indi.getId(), indi);
                            doc.nextTableCell();
                            doc.addText(indi.getName() + " (" + indi.getBirthAsString() + " - " + indi.getDeathAsString() + ")");

                            prefix = " ";
                        }
                    } else {
                        Indi oldestIndividual = tree.getOldestIndividual();
                        doc.nextTableRow();
                        doc.addText(String.format("%d", tree.size()));
                        doc.nextTableCell();
                        doc.addLink(oldestIndividual.getId(), oldestIndividual);
                        doc.nextTableCell();
                        doc.addText(oldestIndividual.getName() + " (" + oldestIndividual.getBirthAsString() + " - " + oldestIndividual.getDeathAsString() + ")");
                    }
                    FamilyGroupFilter filter = new FamilyGroupFilter(tree);
                    AncestrisPlugin.register(filter);
                    filters.add(filter);
                }
            }
            doc.endTable();
            doc.nextParagraph();

            doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.grandtotal", grandtotal));

            if (loners > 0) {

                doc.nextParagraph();

                doc.addText(NbBundle.getMessage(this.getClass(), "FamilyGroupsTopComponent.loners", loners, getMinGroupSize()));
            }
        }

        return doc;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Set unvisited) {
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
