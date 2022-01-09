/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree;

import ancestris.report.svgtree.arrange.AlignLeftArranger;
import ancestris.report.svgtree.arrange.CenteredArranger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ancestris.report.svgtree.filter.FilterChain;
import ancestris.report.svgtree.filter.SameHeightSpouses;
import ancestris.report.svgtree.filter.TreeFilter;
import ancestris.report.svgtree.output.HorizontalLines;

/**
 * Creates classes that do the tree layout..
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class LayoutFactory {
    /**
     * Minimal gap between boxes and lines.
     */
    public static final int SPACING = 10;

    /**
     * Type of arrangement.
     */
    public int arrangement = 0;

    public String[] arrangements;

    private final Map<String, TreeFilter> layouts = new LinkedHashMap<>();
    private final List<TreeFilter> layoutList = new ArrayList<>();

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

}
