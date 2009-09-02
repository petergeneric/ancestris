/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.build;

import genj.gedcom.Fam;
import genj.gedcom.Indi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import tree.FamBox;
import tree.IndiBox;
import tree.IndiBox.Direction;

/**
 * Builds the family tree based on gedcom data.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class BasicTreeBuilder implements TreeBuilder {

    /**
     * Number of generations of ancestors.
     */
    public int gen_ancestors = 0;

    public String[] gen_ancestorss = { "nolimit", "0", "1", "2",
            "3", "4", "5", "6", "7", "8", "9", "10" };

    /**
     * Number of generations of descentants of ancestors.
     */
    public int gen_ancestor_descendants = 0;

    public String[] gen_ancestor_descendantss = { "nolimit", "0",
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

    /**
     * Number of generations of descentants.
     */
    public int gen_descendants = 0;

    public String[] gen_descendantss = { "nolimit", "0", "1", "2",
            "3", "4", "5", "6", "7", "8", "9", "10" };

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


    /**
     * Builds the family tree starting with given individual.
     */
	public IndiBox build(Indi indi) {
		IndiBox indibox = new IndiBox(indi);
		buildTree(indibox, Direction.NONE, 0, 0);

        if (!show_spouses)
            new NoSpouseFilter().filter(indibox);
        if (!display_fambox)
            new RemoveFamboxes().filter(indibox);
        else
            new RemoveFamboxesWhereNoSpouse().filter(indibox);

		return indibox;
	}

	private void buildTree(IndiBox indibox, int dir, int genUp, int genDown) {
		// get all families where spouse
		List families = new ArrayList(Arrays.asList(indibox.individual.getFamiliesWhereSpouse()));

        Fam indiboxFamily = null;

		if (!families.isEmpty()) {
			// if (dir == DIR_PARENT) get all families where spouse is spouse
			indiboxFamily = (Fam)families.get(0);
			Indi spouse = null;
			if (dir == Direction.PARENT) {
				indiboxFamily = indibox.prev.individual.getFamiliesWhereChild()[0];
				spouse = indiboxFamily.getOtherSpouse(indibox.individual);
				if (spouse != null)
					families.addAll(Arrays.asList(spouse.getFamiliesWhereSpouse()));
				while(families.remove(indiboxFamily));
				families.add(0, indiboxFamily);
			} else
				spouse = indiboxFamily.getOtherSpouse(indibox.individual);

            indibox.family = new FamBox(indiboxFamily);

			if (spouse != null)
				indibox.spouse = new IndiBox(spouse, indibox);

            // build indiboxes for these marriages
            if (other_marriages || genDown != 0) {
    			IndiBox last = indibox.spouse;
                if (last == null)
                    last = indibox;

    			Iterator i = families.iterator();
    			i.next();
    			while (i.hasNext()) {
    				Fam f = (Fam)i.next();
    				Indi indi = indibox.individual;
    				if (indibox.individual != f.getHusband() && indibox.individual != f.getWife())
    					indi = spouse;
    				IndiBox box = new IndiBox(indi, last);
    				box.family = new FamBox(f);
                    if (f.getOtherSpouse(indi) != null)
                        box.spouse = new IndiBox(f.getOtherSpouse(indi), box);
    				last.nextMarriage = box;
    				last = box.spouse;
                    if (last == null)
                        last = box;
    			}
            }
			// for each of these families:
			IndiBox last = indibox;
			while (last != null) {
                // check whether to add children
                if ((genUp == 0 && (gen_descendants == 0 || genDown < gen_descendants - 1)) ||
                    (genUp < 0 && (gen_ancestor_descendants == 0 || genDown < gen_ancestor_descendants - 1)))
                {
    			    // if (dir == DIR_PARENT)
    			    //   for (all children)-prev buildTree(child, DIR_CHILD)
    			    // else
    			    //   for all children buildTree(child, DIR_CHILD)
    				List children = new ArrayList(Arrays.asList(last.getFamily().getChildren()));
    				if (last == indibox && dir == Direction.PARENT)
    					children.remove(indibox.prev.individual);
    				last.children = new IndiBox[children.size()];
    				for (int j = 0; j < children.size(); j++) {
    					last.children[j] = new IndiBox((Indi)children.get(j), last);
    					buildTree(last.children[j], Direction.CHILD, genUp, genDown + 1);
    				}
                }

                if (!other_marriages && genDown == 0)
                    last = null;
                else if (last.spouse != null)
                    last = last.spouse.nextMarriage;
                else
                    last = last.nextMarriage;
			}

		}

		// if (dir == DIR_PARENT || dir == DIR_NONE)
		//   buildTree(parent, DIR_PARENT)
		//   buildTree(spouse's parent, DIR_PARENT)
		if ((dir == Direction.PARENT || dir == Direction.NONE) && (gen_ancestors == 0 || -genUp < gen_ancestors - 1)) {
			Indi parent = getParent(indibox.individual);
			if (parent != null) {
				indibox.parent = new IndiBox(parent, indibox);
				buildTree(indibox.parent, Direction.PARENT, genUp - 1, genDown);
			}
			if (indibox.spouse != null) {
				parent = getParent(indibox.spouse.individual);
				if (parent != null) {
					indibox.spouse.parent = new IndiBox(parent, indibox.spouse);
					buildTree(indibox.spouse.parent, Direction.PARENT, genUp - 1, genDown);
				}
			}
		}
	}

    /**
     * Returns the first parent of the given individual or null if one can
     * not be found.
     */
	private Indi getParent(Indi i) {
		Fam[] fs = i.getFamiliesWhereChild();
		if (fs.length == 0)
			return null;
		Fam f = fs[0];
		if (f.getHusband() != null)
			return f.getHusband();
		return f.getWife();
	}
}
