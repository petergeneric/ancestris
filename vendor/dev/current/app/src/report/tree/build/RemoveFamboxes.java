/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.build;

import tree.IndiBox;
import tree.filter.TreeFilterBase;

/**
 * Removes all family boxes from the tree.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RemoveFamboxes extends TreeFilterBase {
    protected void preFilter(IndiBox indibox) {
        indibox.family = null;
    }
}
