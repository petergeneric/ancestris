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
     * Rotation angles
     */
    public static final int ROTATE_0 = 0; // No transformation
    public static final int ROTATE_270 = 1;
    public static final int ROTATE_180 = 2;
    public static final int ROTATE_90 = 3;

    /**
     * Type of arrangement.
     */
    public int arrangement = 0;
    public String[] arrangements;

    /**
     * Type of horizontal flip .
     */
    public boolean flip = false;

    /**
     * Whether build the tree from husband or wife.
     */
    public boolean husband_first = true;

    /**
     * Type of rotation .
     */
    public int rotation = ROTATE_0;
    public String[] rotations = { "none", "270", "180" , "90" };

    
    
    // Private params
    
    private final Map<String, TreeFilter> layouts = new LinkedHashMap<>();
    private final List<TreeFilter> layoutList = new ArrayList<>();
    private final Translator translator;


    
    
    /**
     * Creates the object
     */
    public LayoutFactory(Translator translator)
    {
        this.translator = translator;
        add(translator.translate("arrangement.center"), getLayout(new CenteredArranger(SPACING)));
        add(translator.translate("arrangement.left"), getLayout(new AlignLeftArranger(SPACING)));
        for (int i = 0; i < rotations.length; i++) {
            rotations[i] = translator.translate("rotation." + rotations[i]);
        }
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
