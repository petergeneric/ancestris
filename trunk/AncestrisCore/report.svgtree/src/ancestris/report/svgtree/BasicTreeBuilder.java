/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree;

import ancestris.report.svgtree.IndiBox.Direction;
import ancestris.report.svgtree.build.NoSpouseFilter;
import ancestris.report.svgtree.build.RemoveFamboxes;
import ancestris.report.svgtree.build.RemoveFamboxesWhereNoSpouse;
import ancestris.report.svgtree.build.TreeBuilder;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Builds the family tree based on gedcom data.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class BasicTreeBuilder implements TreeBuilder {

    /**
     * Number of generations of ancestors.
     */
    public int gen_ancestors = 3;

    public String[] gen_ancestorss = {"nolimit", "0", "1", "2",
        "3", "4", "5", "6", "7", "8", "9", "10"};

    /**
     * Number of generations of descentants of ancestors.
     */
    public int gen_ancestor_descendants = 3;

    public String[] gen_ancestor_descendantss = {"nolimit", "0",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    /**
     * Number of generations of descentants.
     */
    public int gen_descendants = 3;

    public String[] gen_descendantss = {"nolimit", "0", "1", "2",
        "3", "4", "5", "6", "7", "8", "9", "10"};

    /**
     * Whether to display spouses (excluding ancestors).
     */
    public boolean show_spouses = true;

    /**
     * Whether to display other marriages of ancestors.
     */
    public boolean other_marriages = true;

    /**
     * Whether to display the family box.
     */
    public boolean display_fambox = true;

    
    // Private params
    
    
    /**
     * Whether build the tree from husband or wife
     */
    private boolean husband_first = true;

    /**
     * List of individuals whose parent trees have already been built
     */
    private Set<Indi> parentTreeIndis = null;

    
    private final Translator translator;
    
    /**
     * Constructor
     * @param translator 
     */
    BasicTreeBuilder(Translator translator) {
        this.translator = translator;
        gen_ancestorss[0] = translator.translate(gen_ancestorss[0]);
        gen_ancestor_descendantss[0] = translator.translate(gen_ancestor_descendantss[0]);
        gen_descendantss[0] = translator.translate(gen_descendantss[0]);
    }

    public void setHusbandFirst(boolean set) {
        this.husband_first = set;
    }
    
    /**
     * Builds the family tree starting with given individual.
     */
    @Override
    public IndiBox build(Indi indi) {
        parentTreeIndis = new HashSet<>();
        IndiBox indibox = new IndiBox(indi);
        buildTree(indibox, Direction.NONE, 0, 0);

        if (!show_spouses) {
            new NoSpouseFilter().filter(indibox);
        }
        if (!display_fambox) {
            new RemoveFamboxes().filter(indibox);
        } else {
            new RemoveFamboxesWhereNoSpouse().filter(indibox);
        }

        return indibox;
    }

    private void buildTree(IndiBox indibox, Direction dir, int genUp, int genDown) {
        // get all families where spouse
        List<Fam> families = new ArrayList<>(Arrays.asList(indibox.individual.getFamiliesWhereSpouse()));

        if (!families.isEmpty()) {
            
            Fam indiboxFamily = families.get(0);
            if (!other_marriages && dir != Direction.CHILD) {
                indiboxFamily = indibox.individual.getPreferredFamily();
            }
            Indi spouse;
            if (dir == Direction.PARENT) {
                indiboxFamily = indibox.prev.individual.getFamiliesWhereChild()[0];
                spouse = indiboxFamily.getOtherSpouse(indibox.individual);
                if (spouse != null) {
                    families.addAll(Arrays.asList(spouse.getFamiliesWhereSpouse()));
                }
                while (families.remove(indiboxFamily));
                families.add(0, indiboxFamily);
            } else {
                spouse = indiboxFamily.getOtherSpouse(indibox.individual);
            }

            indibox.family = new FamBox(indiboxFamily);

            if (spouse != null) {
                indibox.spouse = new IndiBox(spouse, indibox);
            }

            // build indiboxes for these marriages
            if (other_marriages || genDown != 0) {
                IndiBox last = indibox.spouse;
                if (last == null) {
                    last = indibox;
                }

                Iterator<Fam> i = families.iterator();
                i.next();
                while (i.hasNext()) {
                    Fam f = i.next();
                    Indi indi = indibox.individual;
                    if (indibox.individual != f.getHusband() && indibox.individual != f.getWife()) {
                        indi = spouse;
                    }
                    IndiBox box = new IndiBox(indi, last);
                    box.family = new FamBox(f);
                    if (f.getOtherSpouse(indi) != null) {
                        box.spouse = new IndiBox(f.getOtherSpouse(indi), box);
                    }
                    last.nextMarriage = box;
                    last = box.spouse;
                    if (last == null) {
                        last = box;
                    }
                }
            }

            // for each of these families:
            IndiBox last = indibox;
            while (last != null) {
                // check whether to add parents
                if ((dir == Direction.PARENT || dir == Direction.NONE) && (gen_ancestors == 0 || -genUp < gen_ancestors - 1)) {
                    Indi parent = getParent(last.individual);
                    if (parent != null && !parentTreeIndis.contains(last.individual) && (!other_marriages || !show_spouses || last.family == null || last.family.family == last.individual.getPreferredFamily())) {
                        last.parent = new IndiBox(parent, last);
                        parentTreeIndis.add(last.individual);
                        buildTree(last.parent, Direction.PARENT, genUp - 1, genDown);
                    }
                    if (last.spouse != null && !parentTreeIndis.contains(last.spouse.individual) && (!other_marriages || !show_spouses || last.family == null || last.family.family == last.spouse.individual.getPreferredFamily())) {
                        parent = getParent(last.spouse.individual);
                        if (parent != null) {
                            last.spouse.parent = new IndiBox(parent, last.spouse);
                            parentTreeIndis.add(last.spouse.individual);
                            buildTree(last.spouse.parent, Direction.PARENT, genUp - 1, genDown);
                        }
                    }
                }

                // check whether to add children
                if ((genUp == 0 && (gen_descendants == 0 || genDown < gen_descendants - 1))
                        || (genUp < 0 && (gen_ancestor_descendants == 0 || genDown < gen_ancestor_descendants - 1))) {
                    // if (dir == DIR_PARENT)
                    //   for (all children)-prev buildTree(child, DIR_CHILD)
                    // else
                    //   for all children buildTree(child, DIR_CHILD)
                    List<Indi> children = new ArrayList<>(Arrays.asList(last.getFamily().getChildren()));
                    if (last == indibox && dir == Direction.PARENT) {
                        children.remove(indibox.prev.individual);
                    }
                    last.children = new IndiBox[children.size()];
                    for (int j = 0; j < children.size(); j++) {
                        last.children[j] = new IndiBox(children.get(j), last);
                        buildTree(last.children[j], Direction.CHILD, genUp, genDown + 1);
                    }
                }

                if (!other_marriages && genDown == 0) {
                    last = null;
                } else if (last.spouse != null) {
                    last = last.spouse.nextMarriage;
                } else {
                    last = last.nextMarriage;
                }
            }

        }

        if ((dir == Direction.PARENT || dir == Direction.NONE) && (gen_ancestors == 0 || -genUp < gen_ancestors - 1)) {
            Indi parent = getParent(indibox.individual);
            if (parent != null && !parentTreeIndis.contains(indibox.individual) && (!other_marriages || !show_spouses || indibox.family == null || indibox.family.family == indibox.individual.getPreferredFamily())) {
                indibox.parent = new IndiBox(parent, indibox);
                parentTreeIndis.add(indibox.individual);
                buildTree(indibox.parent, Direction.PARENT, genUp - 1, genDown);
            }
            if (indibox.spouse != null && !parentTreeIndis.contains(indibox.spouse.individual) && (!other_marriages || !show_spouses || indibox.family == null || indibox.family.family == indibox.spouse.individual.getPreferredFamily())) {
                parent = getParent(indibox.spouse.individual);
                if (parent != null) {
                    indibox.spouse.parent = new IndiBox(parent, indibox.spouse);
                    parentTreeIndis.add(indibox.spouse.individual);
                    buildTree(indibox.spouse.parent, Direction.PARENT, genUp - 1, genDown);
                }
            }
        }
    }

    /**
     * Returns the first parent of the given individual or null if one can not be found.
     */
    private Indi getParent(Indi i) {
        Fam[] fs = i.getFamiliesWhereChild();
        if (fs.length == 0) {
            return null;
        }
        Fam f = fs[0];
        if (husband_first) {
            if (f.getHusband() != null) {
                return f.getHusband();
            }
            return f.getWife();
        }
        if (f.getWife() != null) {
            return f.getWife();
        }
        return f.getHusband();
    }

}
