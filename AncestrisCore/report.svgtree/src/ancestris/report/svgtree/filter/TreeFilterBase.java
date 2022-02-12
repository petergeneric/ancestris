/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.filter;

import ancestris.report.svgtree.IndiBox;

/**
 * Base class for tree filters. Defines two abstract methods: preFilter() and postFilter().
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class TreeFilterBase implements TreeFilter {

    /**
     * Runs preFilter(), then filter on all child nodes and postFilter() in the end.
     */
    @Override
    public final void filter(IndiBox indibox) {
        if (indibox == null) {
            return;
        }

        preFilter(indibox);

        filter(indibox.parent);
        filter(indibox.spouse);
        filter(indibox.nextMarriage);
        if (indibox.hasChildren()) {
            for (IndiBox children : indibox.children) {
                filter(children);
            }
        }

        postFilter(indibox);
    }

    /**
     * Method run before child nodes are filtered. By default it does nothing.
     */
    protected void preFilter(IndiBox indibox) {
    }

    /**
     * Method run after child nodes are filtered. By default it does nothing.
     */
    protected void postFilter(IndiBox indibox) {
    }
}
