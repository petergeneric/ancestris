/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.arrange;

import genj.report.options.ComponentContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tree.filter.FilterChain;
import tree.filter.SameHeightSpouses;
import tree.filter.TreeFilter;
import tree.output.HorizontalLines;

/**
 * Creates classes that do the tree layout..
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class LayoutFactory implements ComponentContainer
{
    /**
     * Minimal gap between boxes and lines.
     */
    public static final int SPACING = 10;

    /**
     * Type of arrangement.
     */
    public int arrangement = 0;

    public String[] arrangements;

    private Map<String, TreeFilter> layouts = new LinkedHashMap<String, TreeFilter>();
    private List<TreeFilter> layoutList = new ArrayList<TreeFilter>();

    /**
     * Creates the object
     */
    public LayoutFactory()
    {
        add("center", getLayout(new CenteredArranger(SPACING)));
        add("left", getLayout(new AlignLeftArranger(SPACING)));
    }

    public TreeFilter createLayout()
    {
        return layoutList.get(arrangement);
    }

    private void add(String name, TreeFilter layout)
    {
        layouts.put(name, layout);
        layoutList.add(layout);
        arrangements = layouts.keySet().toArray(new String[0]);
    }

    private TreeFilter getLayout(TreeFilter layout)
    {
        return new FilterChain(new TreeFilter[] {
                layout,
                new SameHeightSpouses(),
                new HorizontalLines(SPACING)
            });
    }

    public List<Object> getComponents()
    {
        return Arrays.asList(new Object[] { this });
    }
}
