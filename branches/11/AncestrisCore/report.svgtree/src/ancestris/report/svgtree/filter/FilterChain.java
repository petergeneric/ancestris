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
 * A filter chain executes a list of filters one after another.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FilterChain implements TreeFilter {

    private final TreeFilter[] filters;

    public FilterChain(TreeFilter[] filters) {
        this.filters = filters;
    }

    @Override
    public void filter(IndiBox indibox) {
        for (TreeFilter filter : filters) {
            filter.filter(indibox);
        }
    }
}
