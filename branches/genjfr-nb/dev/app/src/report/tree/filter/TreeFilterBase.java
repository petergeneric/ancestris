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
 * Base class for tree filters. Defines two abstract methods: preFilter() and postFilter().
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class TreeFilterBase implements TreeFilter {

    /**
     * Runs preFilter(), then filter on all child nodes and postFilter() in the end.
     */
    public final void filter(IndiBox indibox) {
        if (indibox == null)
            return;

        preFilter(indibox);

        filter(indibox.parent);
        filter(indibox.spouse);
        filter(indibox.nextMarriage);
        if (indibox.hasChildren())
            for (int i = 0; i < indibox.children.length; i++)
                filter(indibox.children[i]);

        postFilter(indibox);
    }

    /**
     * Method run before child nodes are filtered.
     * By default it does nothing.
     */
    protected void preFilter(IndiBox indibox) {
    }

    /**
     * Method run after child nodes are filtered.
     * By default it does nothing.
     */
    protected void postFilter(IndiBox indibox) {
    }
}
