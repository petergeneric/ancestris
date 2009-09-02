/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

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
    public static class Direction {
        public static final int NONE = 0;
        public static final int SPOUSE = 1;
        public static final int PARENT = 2;
        public static final int CHILD = 3;
        public static final int NEXTMARRIAGE = 4;
    }
    /* This will wait for Java 5.0 compatibility
    static enum Direction {
        NONE, SPOUSE, PARENT, CHILD, NEXTMARRIAGE
    };
    */

    /**
     * Previous box in the tree.
     */
    public IndiBox prev = null;

    /**
     * Spouse's box.
     */
    public IndiBox spouse = null;

    /**
     * Parent's box.
     */
    public IndiBox parent = null;

    /**
     * Children's boxes.
     */
    public IndiBox[] children = null;

    /**
     * Box of an individual in the next marriage of one of this individual or
     * his/her spouse.
     */
    public IndiBox nextMarriage = null;

    /**
     * X coordinate relative to the position of the previous IndiBox in pixels.
     */
    public int x = 0;

    /**
     * Y coordinate relative to the position of the previous IndiBox in
     * generation lines.
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
     * The individual connected with this box.
     */
    public Indi individual;

    /**
     * Family where spouse.
     */
    public FamBox family;

    /**
     * Constructs the object.
     * @param individual  individual connected with this box
     */
    public IndiBox(Indi individual) {
        this.individual = individual;
    }

    /**
     * Constructs the object
     * @param individual  individual connected with this box
     * @param prev
     */
    public IndiBox(Indi individual, IndiBox prev) {
        this.individual = individual;
        this.prev = prev;
    }

    /**
     * Returns the direction of the previous connected IndiBox.
     */
    public int getDir() {
        if (prev == null)
            return Direction.NONE;
        if (this == prev.spouse)
            return Direction.SPOUSE;
        if (this == prev.parent)
            return Direction.PARENT;
        if (this == prev.nextMarriage)
            return Direction.NEXTMARRIAGE;
        return Direction.CHILD;
    }

    /**
     * @return true if this IndiBox has child boxes connected.
     */
    public boolean hasChildren() {
        return (children != null && children.length > 0);
    }

    public Fam getFamily() {
        if (family == null)
            return null;
        return family.family;
    }
}