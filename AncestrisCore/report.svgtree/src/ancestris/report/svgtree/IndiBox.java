/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree;

import genj.gedcom.Fam;
import genj.gedcom.Indi;

/**
 * Class representing a single individual box and its links to adjecent boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class IndiBox {

    /**
     * Direction of the previous connected IndiBox.
     */
    public static enum Direction {
        NONE, SPOUSE, PARENT, CHILD, NEXTMARRIAGE
    };

    public static int totalBoxes = 0;
    public static int netTotalBoxes = 0;

    /**
     * The individual connected with this box.
     */
    public Indi individual;

    /**
     * Previous box in the tree.
     */
    public IndiBox prev = null;

    /**
     * Spouse's box.
     */
    public IndiBox spouse = null;

    /**
     * Family where spouse.
     */
    public FamBox family;

    /**
     * Parent's box.
     */
    public IndiBox parent = null;

    /**
     * Children's boxes.
     */
    public IndiBox[] children = null;

    /**
     * Box of an individual in the next marriage of one of this individual or his/her spouse.
     */
    public IndiBox nextMarriage = null;

    /**
     * X coordinate relative to the position of the previous IndiBox in pixels.
     */
    public int x = 0;

    /**
     * Y coordinate relative to the position of the previous IndiBox in generation lines.
     */
    public int y = 0;

    /**
     * Width of the box in pixels.
     */
    public int width = 10;

    /**
     * Height of the box in pixels.
     */
    public int height = 10;

    // Space taken by all child-nodes of this IndiBox.
    public int wPlus = 0;

    public int wMinus = 0;

    public int hPlus = 0;

    public int hMinus = 0;

    /**
     * Constructs the object.
     *
     * @param individual individual connected with this box
     */
    public IndiBox(Indi individual) {
        this.individual = individual;
        totalBoxes++;
    }

    /**
     * Constructs the object
     *
     * @param individual individual connected with this box
     * @param prev
     */
    public IndiBox(Indi individual, IndiBox prev) {
        this.individual = individual;
        this.prev = prev;
        totalBoxes++;
    }

    /**
     * Returns the direction of the previous connected IndiBox.
     */
    public Direction getDir() {
        if (prev == null) {
            return Direction.NONE;
        }
        if (this == prev.spouse) {
            return Direction.SPOUSE;
        }
        if (this == prev.parent) {
            return Direction.PARENT;
        }
        if (this == prev.nextMarriage) {
            return Direction.NEXTMARRIAGE;
        }
        return Direction.CHILD;
    }

    /**
     * @return true if this IndiBox has child boxes connected.
     */
    public boolean hasChildren() {
        return (children != null && children.length > 0);
    }

    public Fam getFamily() {
        if (family == null) {
            return null;
        }
        return family.family;
    }

    public static void setTotalBoxes(int set) {
        totalBoxes = set;
    }

    public static int getTotalBoxes() {
        return totalBoxes;
    }
    
    public static int getNetTotalBoxes() {
        return netTotalBoxes;
    }
}
