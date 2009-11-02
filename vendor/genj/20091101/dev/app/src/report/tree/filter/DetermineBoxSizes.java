/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.filter;

import tree.IndiBox;
import tree.output.TreeElements;

/**
 * Determines the width and height of individual boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class DetermineBoxSizes extends TreeFilterBase {

    private TreeElements elements;

    public DetermineBoxSizes(TreeElements elements) {
        this.elements = elements;
    }

    public void preFilter(IndiBox indibox) {
        elements.getIndiBoxSize(indibox);
        if (indibox.family != null)
            elements.getFamBoxSize(indibox.family);
    }
}
