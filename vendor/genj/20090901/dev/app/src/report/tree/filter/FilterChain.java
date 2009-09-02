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
 * A filter chain executes a list of filters one after another.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FilterChain implements TreeFilter {

    private TreeFilter[] filters;

    public FilterChain(TreeFilter[] filters)
    {
        this.filters = filters;
    }

	public void filter(IndiBox indibox)
    {
        for (int i = 0; i < filters.length; i++)
            filters[i].filter(indibox);
    }
}
