/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.filter;

import ancestris.report.svgtree.IndiBox;
import ancestris.report.svgtree.output.TreeElements;

/**
 * Determines the width and height of individual boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class DetermineBoxSizes extends TreeFilterBase {

    private final TreeElements elements;

    public DetermineBoxSizes(TreeElements elements) {
        this.elements = elements;
    }

    @Override
    public void preFilter(IndiBox indibox) {
        elements.getIndiBoxSize(indibox);
        if (indibox.family != null) {
            elements.getFamBoxSize(indibox.family);
        }
    }
}
