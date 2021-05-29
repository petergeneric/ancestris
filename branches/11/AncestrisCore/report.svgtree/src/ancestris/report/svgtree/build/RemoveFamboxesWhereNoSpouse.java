/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree.build;

import ancestris.report.svgtree.IndiBox;
import ancestris.report.svgtree.filter.TreeFilterBase;

/**
 * Removes all family boxes from the tree where there aren't both parents.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RemoveFamboxesWhereNoSpouse extends TreeFilterBase {
    @Override
    protected void preFilter(IndiBox indibox) {
        if (indibox.spouse == null)
            indibox.family = null;
    }
}
