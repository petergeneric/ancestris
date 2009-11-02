/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Fam;

/**
 * Class representing a single family box.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FamBox {

    /**
     * Width of the box in pixels.
     */
    public int width = 10;

    /**
     * Height of the box in pixels.
     */
    public int height = 10;

    /**
     * The family.
     */
    public Fam family;

    /**
     * Constructs the object.
     * @param individual  individual connected with this box
     */
    public FamBox(Fam family) {
        this.family = family;
    }
}
