/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.build;

import genj.gedcom.Indi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tree.IndiBox;
import tree.filter.TreeFilterBase;

/**
 * Filters out all spouses (except ancestors).
 * When a spouse is removed, all children from all marriages are connected
 * to the parent that is left.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class NoSpouseFilter extends TreeFilterBase {

    /**
     * Runs the filter on the given individual.
     */
    protected void preFilter(IndiBox indibox) {

        if (indibox.getDir() != IndiBox.Direction.PARENT &&
            indibox.getDir() != IndiBox.Direction.SPOUSE)
        {
            indibox.children = getChildren(indibox, indibox.individual);
            if (indibox.children != null)
            	for (IndiBox c : indibox.children)
            		c.prev = indibox;
            indibox.spouse = null;
        }
    }

    /**
     * Returns all children of an individual (from all marriages)
     * @param indibox  start looking from this individual
     * @param parent  look only for parent's children
     */
    private IndiBox[] getChildren(IndiBox indibox, Indi parent) {
        IndiBox[] children = null;
        if (indibox.individual == parent || (indibox.spouse != null && indibox.spouse.individual == parent))
        	children = indibox.children;
        
        if (indibox.nextMarriage != null)
            children = merge(children, getChildren(indibox.nextMarriage, parent));
        if (indibox.spouse != null && indibox.spouse.nextMarriage != null)
            children = merge(children, getChildren(indibox.spouse.nextMarriage, parent));
        return children;
    }

    /**
     * Merges two arrays into one.
     */
    private IndiBox[] merge(IndiBox[] a, IndiBox[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        List<IndiBox> list = new ArrayList<IndiBox>(Arrays.asList(a));
        list.addAll(Arrays.asList(b));
        return list.toArray(new IndiBox[0]);
    }
}
