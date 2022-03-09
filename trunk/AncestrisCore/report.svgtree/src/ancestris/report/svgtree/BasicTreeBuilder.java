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
        IndiBox.netTotalBoxes = 0;
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
        if (currentAncGen <= maxAncGen) {
            buildSosaTree(sosaIndiBoxesMap, deCujusBox, maxAncGen, currentAncGen + 1, sosaStart, mainSosaLine);
        }
        
        // Traverse Sosa sequence and build descendants of ancestors down to max ancestor/descendants generation
        for (BigInteger sosa : sosaIndiBoxesMap.keySet()) {
            buildDescendantsTree(sosaIndiBoxesMap.get(sosa), sosa.equals(BigInteger.ONE.shiftLeft(1)) ? maxDesGen : maxAncDesGen, currentDesGen + 1, mainSosaLine);
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
        if (currentGen <= maxGen) {
            Indi parent = getParent(indiBox.individual);
            if (parent != null) {
                IndiBox parentBox = new IndiBox(parent, indiBox);
                indiBox.parent = parentBox;
                BigInteger bi = sosa.shiftLeft(1);
                map.put(bi, parentBox);
                buildSosaTree(map, parentBox, maxGen, currentGen + 1, bi, mainSosaLine);
            }

            if (indiBox.spouse != null) {
                parent = getParent(indiBox.spouse.individual);
                if (parent != null) {
                    IndiBox parentSpouseBox = new IndiBox(parent, indiBox.spouse);
                    indiBox.spouse.parent = parentSpouseBox;
                    BigInteger bi = sosa.add(BigInteger.ONE).shiftLeft(1);
                    map.put(bi, parentSpouseBox);
                    buildSosaTree(map, parentSpouseBox, maxGen, currentGen + 1, bi, mainSosaLine);
                }
            }
        }
    }
    
    private void buildDescendantsTree(IndiBox indiBox, int maxGen, int currentGen, boolean mainSosaLine) {

        if (currentGen > maxGen || indiBox.getFamily() == null) {
            return;
        }
        // Set link indiBox.children
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

    

}
