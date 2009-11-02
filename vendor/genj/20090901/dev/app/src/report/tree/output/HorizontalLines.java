/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import java.util.HashMap;
import java.util.Map;

import tree.IndiBox;
import tree.filter.TreeFilter;
import tree.filter.TreeFilterBase;

/**
 * Converts line numbers to coordinates.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class HorizontalLines implements TreeFilter {

    /**
     * Smallest level number.
     */
    private int levelMin = 0;

    /**
     * Largest level number.
     */
    private int levelMax = 0;

    /**
     * Heights of levels.
     */
    private Map levelHeight = new HashMap();

    /**
     * Y coordinates of levels.
     */
    private Map levelCoord = new HashMap();

    private int spacing;

    public HorizontalLines(int spacing)
    {
        this.spacing = spacing;
    }

    public void filter(IndiBox indibox)
    {
        // Reset variables
        levelMin = 0;
        levelMax = 0;
        levelHeight.clear();
        levelCoord.clear();

        // Determine height of levels
        new DetermineLevelHeight().filter(indibox);

        // Assign coordinates to levels
        int yCoord = 0;
        for (int i = levelMin; i <= levelMax; i++) {
            levelCoord.put(new Integer(i), new Integer(yCoord));
            yCoord += ((Integer)levelHeight.get(new Integer(i))).intValue();
        }
        levelCoord.put(new Integer(levelMax + 1), new Integer(yCoord));

        // Assign coordinates to boxes
        new AssignCoordinates().filter(indibox);
	}

    /**
     * Converts the generation level number to image Y coordinate.
     */
    private int getYCoord(int level) {
        return ((Integer)levelCoord.get(new Integer(level))).intValue();
    }

    /**
     * Determines line heights. The height of a line is the maximum height of a box
     * in this line.
     */
    private class DetermineLevelHeight extends TreeFilterBase {

        /**
         * Current level.
         */
        private int level = 0;

        protected void preFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level += indibox.y;

            if (level > levelMax)
                levelMax = level;
            if (level < levelMin)
                levelMin = level;

            Integer lev = new Integer(level);
            Integer height = (Integer)levelHeight.get(lev);
            int heightInt = 0;
            if (height != null)
                heightInt = height.intValue();
            int newHeight = indibox.height + spacing * 2;
            if (indibox.family != null)
                newHeight += indibox.family.height;
            if (newHeight > heightInt)
                levelHeight.put(lev, new Integer(newHeight));
        }

        protected void postFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level -= indibox.y;
        }
    }

    /**
     * Assigns coordinates to boxes based on coordinates of levels.
     */
    private class AssignCoordinates extends TreeFilterBase {

        /**
         * Current level.
         */
        private int level = 0;

        protected void preFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level += indibox.y;
        }

        protected void postFilter(IndiBox indibox) {
            int thisLevel = level;
            if (indibox.prev != null) {
                level -= indibox.y;
                indibox.y = getYCoord(thisLevel) - getYCoord(thisLevel - indibox.y);
            }
            indibox.hPlus = getYCoord(thisLevel + indibox.hPlus) - getYCoord(thisLevel);
            indibox.hMinus = getYCoord(thisLevel) - getYCoord(thisLevel - indibox.hMinus);
        }
    }
}
