/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree;

import ancestris.report.svgtree.build.NoSpouseFilter;
import ancestris.report.svgtree.build.RemoveDuplicates;
import ancestris.report.svgtree.build.RemoveDuplicatesPrepare;
import ancestris.report.svgtree.build.RemoveFamboxes;
import ancestris.report.svgtree.build.RemoveFamboxesWhereNoSpouse;
import ancestris.report.svgtree.build.TreeBuilder;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * Whether to show duplicate branches
     */
    public boolean show_duplicates = false;

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
     * We need to ensure that a visited family that would appear more than once is complete with all branches for one, and without any branches for the others, in order not to duplicate branches.
     * In the "secondary" families, a mark on them would mean that the "full" family exists somewhere else in the tree.
     */
    private VisitedFamilies visitedFamilies = null;

    
    private final Translator translator;
    
    /**
     * Constructor
     * @param translator 
     */
    public BasicTreeBuilder(Translator translator) {
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
        visitedFamilies = new VisitedFamilies();
        IndiBox indiBox = new IndiBox(indi);
        buildTree(indiBox, 
                gen_ancestors == 0 ? 999 : gen_ancestors - 1, 
                gen_descendants == 0 ? 999 : gen_descendants - 1, 
                gen_ancestor_descendants == 0 ? 999 : gen_ancestor_descendants - 1, 
                0, 0, other_marriages);

        if (!show_spouses) {
            new NoSpouseFilter().filter(indiBox);
        }
        
        if (!display_fambox) {
            new RemoveFamboxes().filter(indiBox);
        } else {
            new RemoveFamboxesWhereNoSpouse().filter(indiBox);
        }

        // Mark or remove duplicates
        RemoveDuplicatesPrepare rdp = new RemoveDuplicatesPrepare();
        rdp.filter(indiBox);
        sortSosa(rdp.getMap());
        new RemoveDuplicates(show_duplicates).filter(indiBox);
        
        return indiBox;
    }

    /**
     * Builds the tree boxes traversing it by sosa order from the initial indi (so non recursive traversal), and descending at each sosa while traversing
     * The purpose is to build a balanced tree in case of implexe and multiple mariages, when we do not want to display duplicate branches
     * (we want to trim top duplicates rather than bottom ones)
     * E.g. : 1 DESC, 2 DESC, 3 DESC, etc.
     */
    private void buildTree(IndiBox deCujusBox, int maxAncGen, int maxDesGen, int maxAncDesGen, int currentAncGen, int currentDesGen, boolean mainSosaLine) {
        
        // Build sosa sequence of Indi boxes up to the maximum generation, excluding decujus
        BigInteger sosaStart = BigInteger.ONE.add(BigInteger.ONE); // we start at a couple with first indi being 2
        Map<BigInteger, IndiBox> sosaIndiBoxesMap = new HashMap<>();
        sosaIndiBoxesMap.put(sosaStart, deCujusBox);
        if (currentAncGen < maxAncGen) {
            buildSosaTree(sosaIndiBoxesMap, deCujusBox, maxAncGen, currentAncGen + 1, sosaStart, mainSosaLine);
        }
        
        // Traverse Sosa sequence and build descendants of ancestors down to max ancestor/descendants generation
        for (BigInteger sosa : sosaIndiBoxesMap.keySet()) {
            buildDescendantsTree(sosaIndiBoxesMap.get(sosa), sosa.equals(BigInteger.ONE) ? maxDesGen : maxAncDesGen, currentDesGen + 1, mainSosaLine);
        }
        
        // Traverse Sosa sequence and build other marriages ascendants and descendants
        if (mainSosaLine) {
            for (BigInteger sosa : sosaIndiBoxesMap.keySet()) {
                IndiBox last = sosaIndiBoxesMap.get(sosa);
                if (last.spouse != null && last.spouse.nextMarriage != null) {
                    buildTree(last.spouse.nextMarriage, maxAncGen, maxAncDesGen, maxAncDesGen, sosa.bitLength()-2, currentDesGen, false);
                }
                if (last.nextMarriage != null) {
                    buildTree(last.nextMarriage, maxAncGen, maxAncDesGen, maxAncDesGen, sosa.bitLength()-2, currentDesGen, false);
                }
            }
        }
        
    }
    
    private void buildSosaTree(Map<BigInteger, IndiBox> map, IndiBox indiBox, int maxGen, int currentGen, BigInteger sosa, boolean mainSosaLine) {

        // Set same generation links
        setLinks(indiBox, sosa, mainSosaLine);
        
        // Set indiBox links with parents if we need to go up
        if (currentGen <= maxGen && visitedFamilies.canAddAscendants(indiBox.getFamily(), indiBox)) {
            Indi parent = getParent(indiBox.individual);
            if (parent != null) {
                IndiBox parentBox = new IndiBox(parent, indiBox);
                indiBox.parent = parentBox;
                BigInteger bi = sosa.shiftLeft(1);
                map.put(bi, parentBox);
                visitedFamilies.addAscendants(indiBox.getFamily(), indiBox, indiBox.spouse, indiBox.family, true);
                buildSosaTree(map, parentBox, maxGen, currentGen + 1, bi, mainSosaLine);
            }

            if (indiBox.spouse != null && visitedFamilies.canAddAscendants(indiBox.getFamily(), indiBox.spouse)) {
                parent = getParent(indiBox.spouse.individual);
                if (parent != null) {
                    IndiBox parentSpouseBox = new IndiBox(parent, indiBox.spouse);
                    indiBox.spouse.parent = parentSpouseBox;
                    BigInteger bi = sosa.add(BigInteger.ONE).shiftLeft(1);
                    map.put(bi, parentSpouseBox);
                    visitedFamilies.addAscendants(indiBox.getFamily(), indiBox.spouse, indiBox, indiBox.family, true);
                    buildSosaTree(map, parentSpouseBox, maxGen, currentGen + 1, bi, mainSosaLine);
                }
            }
        }
    }
    
    private void buildDescendantsTree(IndiBox indiBox, int maxGen, int currentGen, boolean mainSosaLine) {

        if (currentGen > maxGen || indiBox.getFamily() == null || !visitedFamilies.canAddDescendants(indiBox)) {
            return;
        }
        // Set link indiBox.children
        visitedFamilies.addDescendants(indiBox);
        List<Indi> children = new ArrayList<>(Arrays.asList(indiBox.getFamily().getChildren(true)));
        if (indiBox.prev != null) {
            children.remove(indiBox.prev.individual);  // indiBox.prev is the child that has generated the parent whose children we are looking at, it is already looked at, so remove it from here
        }
        indiBox.children = new IndiBox[children.size()];
        for (int i = 0; i < children.size(); i++) {
            IndiBox childBox = new IndiBox(children.get(i), indiBox);
            setLinks(childBox, indiBox.family.sosa, mainSosaLine);
            indiBox.children[i] = childBox;
            buildDescendantsTree(childBox, maxGen, currentGen + 1, mainSosaLine);
        }
    }
    
    /**
     * Set same generation links (links other than parents and children)
     * @param indiBox 
     */
    private void setLinks(IndiBox indiBox, BigInteger sosa, boolean mainSosaLine) {
        
        if (indiBox.family != null || indiBox.spouse != null) {
            return;
        }
        
        List<Fam> families = new ArrayList<>(Arrays.asList(indiBox.individual.getFamiliesWhereSpouse(true)));
        if (!families.isEmpty()) {

            // indiBox.family;
            Fam indiboxFamily = families.get(0);
            if (!other_marriages) {
                indiboxFamily = indiBox.individual.getPreferredFamily();
            }
            indiBox.family = new FamBox(indiboxFamily);
            if (mainSosaLine) {
                indiBox.family.sosa = sosa;
            }

            // indiBox.spouse;
            Indi spouse = indiboxFamily.getOtherSpouse(indiBox.individual);
            if (spouse != null) {
                indiBox.spouse = new IndiBox(spouse, indiBox);
            }

            // indiBox.nextMarriage;
            if (other_marriages) {
                IndiBox last = indiBox.spouse;
                if (last == null) {
                    last = indiBox;
                }

                if (spouse != null) {
                    families.addAll(Arrays.asList(spouse.getFamiliesWhereSpouse()));
                }
                // make sure the previous family is first in the list to iterate below from the second one
                while (families.remove(indiboxFamily)); 
                families.add(0, indiboxFamily);
                Iterator<Fam> i = families.iterator();
                i.next(); // go the second family
                while (i.hasNext()) {
                    Fam f = i.next();
                    Indi indi = indiBox.individual;
                    if (indiBox.individual != f.getHusband() && indiBox.individual != f.getWife()) {
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

    /**
     * Calculate the FamBoxes involved in removing both parents and children:
     * (we keep one family with all its branches, the other same families will have no branches)
     * 
     * - For each family with several FamBoxs
     *    - sort each list of FamBoxes ascending on sosa number (set sosa numbers while building the tree)
     *    - those with no SOSA number (remarriages) should be at the end of the list, by increasing date of marriage
     *    - mark the FamBoxes of this list with an increasing index that will be displayed in FamBoxes, starting from 1
     * 
     * @param map 
     */
    private void sortSosa(Map<Fam, List<FamBox>> map) {
        for (Fam fam : map.keySet()) {
            List<FamBox> list = map.get(fam);
            if (list.size() == 1) {
                continue;
            }
            Collections.sort(list, (Object o1, Object o2) -> {
                BigInteger bi1 = ((FamBox)o1).sosa;
                BigInteger bi2 = ((FamBox)o2).sosa;
                if (bi1 == null && bi2 == null) {
                    return 0;
                }
                if (bi1 == null && bi2 != null) {
                    return 1;
                }
                if (bi1 != null && bi2 == null) {
                    return -1;
                }
                return ((FamBox)o1).sosa.compareTo(((FamBox)o2).sosa);
            });
            int i = 1;
            for (FamBox fb : list) {
                fb.index = i;
                fb.indexTotal = list.size();
                i++;
            }
        }
    }

    
    
    
    // Convert this non duplicate logic to filters after spouse, because duplicates change after applying spouse filters.
    
    
    /**
     * Visited family stores for each family (fam), the 3 boxes of the tree that have the branch attached to them
     * The first time a family is visited, we allocate to it the 3 current boxes.
     * The next time it is encountered, it has to be the same 3 boxes, otherwise no branches are added.
     * In other words, if a family box or an indi box appears more than once, only one family (fam) has got all 3 branches to it. The others have none.
     */
    private class VisitedFamilies {
        
        private Map<Fam, VisitedFamilyBoxes> visitedFamilies;
        
        public VisitedFamilies() {
            visitedFamilies = new HashMap<>();
        }

        private void addDescendants(IndiBox indiBox) {
            addAscendants(indiBox.getFamily(), indiBox, indiBox.spouse, indiBox.family, false);
        }

        private void addAscendants(Fam family, IndiBox indiBoxMain, IndiBox indiBoxSpouse, FamBox famBox, boolean isAscending) {
            VisitedFamilyBoxes vf = visitedFamilies.get(family);
            if (vf == null) {
                vf = new VisitedFamilyBoxes();
                visitedFamilies.put(family, vf);
            }
            if (family.getHusband() == indiBoxMain.individual) {
                vf.setBox(indiBoxMain, indiBoxSpouse, famBox);
                if (isAscending) {
                    vf.setHusbandDone();
                }
            } else {
                vf.setBox(indiBoxSpouse, indiBoxMain, famBox);
                if (isAscending) {
                    vf.setWifeDone();
                }
            }
        }

        // It is ok to add parents to this indiBox if indiBox.individual does not have a box somewhere else with parents,
        // or if we are on the same family and box and we have note done both parents already
        private boolean canAddAscendants(Fam family, IndiBox indiBox) {
            if (show_duplicates || true) {
                return true;
            }
            
            // If we are on the same family, we have to be on the same box
            VisitedFamilyBoxes vf = visitedFamilies.get(family);
            if (vf != null) {
                if (family.getHusband() == indiBox.individual) {
                    return vf.husbandBoxWithAscendants == indiBox && !vf.isHusbandDone();
                } else {
                    return vf.wifeBoxWithAscendants == indiBox && !vf.isWifeDone();
                }
            }
            // If we are NOT on the same family, we have to check if indiBox.individual has not already a box associated to it
            for (Fam fam : visitedFamilies.keySet()) {
                if (fam == family || (fam.getHusband() != indiBox.individual && fam.getWife() != indiBox.individual)) {
                    continue;
                }
                // indiBox.individual has necessarily another indiBox associated to it because it is found in another visited family
                // and when we visit a family, we allocate 3 boxes
                return false;
            }
            return true;
        }

        private boolean canAddDescendants(IndiBox indiBox) {
            if (show_duplicates || true) {
                return true;
            }
            VisitedFamilyBoxes vf = visitedFamilies.get(indiBox.getFamily());
            if (vf == null) {
                return true;
            }
            return vf == null || vf.familyBoxWithDescendant == indiBox.family;
        }

        
    }
    
    
    
    private class VisitedFamilyBoxes {
        
        private IndiBox husbandBoxWithAscendants = null;
        private IndiBox wifeBoxWithAscendants = null;
        private FamBox familyBoxWithDescendant = null;
        private boolean husbandDone = false;
        private boolean wifeDone = false;

        private VisitedFamilyBoxes() {
        }

        private void setBox(IndiBox husbBox, IndiBox wifeBox, FamBox famBox) {
            if (this.husbandBoxWithAscendants == null) {
                this.husbandBoxWithAscendants = husbBox;
            }
            
            if (this.wifeBoxWithAscendants == null) {
                this.wifeBoxWithAscendants = wifeBox;
            }
            
            if (this.familyBoxWithDescendant == null) {
                this.familyBoxWithDescendant = famBox;
            }
        }

        private void setHusbandDone() {
            husbandDone = true;
        }

        private void setWifeDone() {
            wifeDone = true;
        }

        private boolean isHusbandDone() {
            return husbandDone;
        }

        private boolean isWifeDone() {
            return wifeDone;
        }
    }
    

// 2022-02-20 - FL - Once fully test, this can be removed.
// ========================================================
//    private void old_buildTree(IndiBox indiBox, Direction dir, int genUp, int genDown) {
//        // get all families where spouse
//        List<Fam> families = new ArrayList<>(Arrays.asList(indiBox.individual.getFamiliesWhereSpouse()));
//
//        if (!families.isEmpty()) {
//            
//            Fam indiboxFamily = families.get(0);
//            if (!other_marriages && dir != Direction.CHILD) {
//                indiboxFamily = indiBox.individual.getPreferredFamily();
//            }
//            Indi spouse;
//            if (dir == Direction.PARENT) {
//                indiboxFamily = indiBox.prev.individual.getFamiliesWhereChild()[0];
//                spouse = indiboxFamily.getOtherSpouse(indiBox.individual);
//                if (spouse != null) {
//                    families.addAll(Arrays.asList(spouse.getFamiliesWhereSpouse()));
//                }
//                while (families.remove(indiboxFamily));
//                families.add(0, indiboxFamily);
//            } else {
//                spouse = indiboxFamily.getOtherSpouse(indiBox.individual);
//            }
//
//            indiBox.family = new FamBox(indiboxFamily);
//
//            if (spouse != null) {
//                indiBox.spouse = new IndiBox(spouse, indiBox);
//            }
//
//            // build indiboxes for these marriages
//            if (other_marriages || genDown != 0) {
//                IndiBox last = indiBox.spouse;
//                if (last == null) {
//                    last = indiBox;
//                }
//
//                Iterator<Fam> i = families.iterator();
//                i.next();
//                while (i.hasNext()) {
//                    Fam f = i.next();
//                    Indi indi = indiBox.individual;
//                    if (indiBox.individual != f.getHusband() && indiBox.individual != f.getWife()) {
//                        indi = spouse;
//                    }
//                    IndiBox box = new IndiBox(indi, last);
//                    box.family = new FamBox(f);
//                    if (f.getOtherSpouse(indi) != null) {
//                        box.spouse = new IndiBox(f.getOtherSpouse(indi), box);
//                    }
//                    last.nextMarriage = box;
//                    last = box.spouse;
//                    if (last == null) {
//                        last = box;
//                    }
//                }
//            }
//
//            // for each of these families:
//            IndiBox last = indiBox;
//            while (last != null) {
//                // check whether to add children
//                if ((genUp == 0 && (gen_descendants == 0 || genDown < gen_descendants - 1))
//                    || (genUp < 0  && (gen_ancestor_descendants == 0 || genDown < gen_ancestor_descendants - 1))) {
//                    if (visitedFamilies.canAddDescendants(last)) {
//                        visitedFamilies.addDescendants(last);
//                        List<Indi> children = new ArrayList<>(Arrays.asList(last.getFamily().getChildren()));
//                        if (last == indiBox && dir == Direction.PARENT) {
//                            children.remove(indiBox.prev.individual);
//                        }
//                        last.children = new IndiBox[children.size()];
//                        for (int j = 0; j < children.size(); j++) {
//                            last.children[j] = new IndiBox(children.get(j), last);
//                            old_buildTree(last.children[j], Direction.CHILD, genUp, genDown + 1);
//                        }
//                    }
//                }
//
//                // check whether to add parents
//                if ((dir == Direction.PARENT || dir == Direction.NONE) && (gen_ancestors == 0 || -genUp < gen_ancestors - 1)) {
//                    Indi parent = getParent(last.individual);
//                    if (parent != null && (!other_marriages || !show_spouses || last.family == null || last.family.family == last.individual.getPreferredFamily()) && visitedFamilies.canAddAscendants(last.getFamily(), last)) {
//                        last.parent = new IndiBox(parent, last);
//                        visitedFamilies.addAscendants(last.getFamily(), last, last.spouse, last.family, true);
//                        old_buildTree(last.parent, Direction.PARENT, genUp - 1, genDown);
//                    }
//                    if (last.spouse != null && (!other_marriages || !show_spouses || last.family == null || last.family.family == last.spouse.individual.getPreferredFamily()) && visitedFamilies.canAddAscendants(last.getFamily(), last.spouse)) {
//                        parent = getParent(last.spouse.individual);
//                        if (parent != null) {
//                            last.spouse.parent = new IndiBox(parent, last.spouse);
//                            visitedFamilies.addAscendants(last.getFamily(), last.spouse, last, last.family, true);
//                            old_buildTree(last.spouse.parent, Direction.PARENT, genUp - 1, genDown);
//                        }
//                    }
//                }
//
//                if (!other_marriages && genDown == 0) {
//                    last = null;
//                } else if (last.spouse != null) {
//                    last = last.spouse.nextMarriage;
//                } else {
//                    last = last.nextMarriage;
//                }
//            }
//
//        }
//
//        if ((dir == Direction.PARENT || dir == Direction.NONE) && (gen_ancestors == 0 || -genUp < gen_ancestors - 1)) {
//            Indi parent = getParent(indiBox.individual);
//            if (parent != null && (!other_marriages || !show_spouses || indiBox.family == null || indiBox.family.family == indiBox.individual.getPreferredFamily()) && visitedFamilies.canAddAscendants(indiBox.getFamily(), indiBox)) {
//                visitedFamilies.addAscendants(indiBox.getFamily(), indiBox, indiBox.spouse, indiBox.family, true);
//                indiBox.parent = new IndiBox(parent, indiBox);
//                old_buildTree(indiBox.parent, Direction.PARENT, genUp - 1, genDown);
//            }
//            if (indiBox.spouse != null && (!other_marriages || !show_spouses || indiBox.family == null || indiBox.family.family == indiBox.spouse.individual.getPreferredFamily()) && visitedFamilies.canAddAscendants(indiBox.getFamily(), indiBox.spouse)) {
//                parent = getParent(indiBox.spouse.individual);
//                if (parent != null) {
//                    visitedFamilies.addAscendants(indiBox.getFamily(), indiBox.spouse, indiBox, indiBox.family, true);
//                    indiBox.spouse.parent = new IndiBox(parent, indiBox.spouse);
//                    old_buildTree(indiBox.spouse.parent, Direction.PARENT, genUp - 1, genDown);
//                }
//            }
//        }
//    }
//
    
}
