/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.filter;

import tree.IndiBox;


/**
 * Interface for classes used to change an existing family tree.
 * A filter can remove or add individuals/families to the tree
 * or arrange/rearrange individual boxes in the family tree.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface TreeFilter {

    /**
     * Changes the family tree starting from the selected individual.
     * @param indibox  root individual
     */
	public void filter(IndiBox indibox);
}
