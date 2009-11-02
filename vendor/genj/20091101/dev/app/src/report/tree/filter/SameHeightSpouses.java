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
 * Ensures spouses' boxes are the same height.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class SameHeightSpouses extends TreeFilterBase {
    protected void preFilter(IndiBox indibox) {
        if (indibox.spouse != null) {
            if (indibox.spouse.height > indibox.height)
                indibox.height = indibox.spouse.height;
            else
                indibox.spouse.height = indibox.height;
        }
    }
}