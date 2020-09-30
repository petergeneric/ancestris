/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree;

import ancestris.report.svgtree.output.FilterTreeElements;
import ancestris.report.svgtree.output.TreeElements;
import java.awt.Graphics2D;

/**
 * Produces a mirror image of the tree, preserving information boxes in readable form.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FlipTreeElements extends FilterTreeElements {

    public boolean flip = false;

    /**
     * Constructs the object.
     */
    public FlipTreeElements(Graphics2D graphics, TreeElements elements) {
        super(graphics, elements);
    }

    public FlipTreeElements(TreeElements elements)
    {
        super(elements);
    }

    /**
     * Outputs an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    @Override
    public void drawIndiBox(IndiBox indibox, int x, int y, int gen) {
        if (!flip)
        {
            elements.drawIndiBox(indibox, x, y, gen);
            return;
        }
        graphics.translate(x + indibox.width/2, y);
        graphics.scale(-1, 1);
        elements.drawIndiBox(indibox, -indibox.width/2, 0, gen);
        graphics.scale(-1, 1);
        graphics.translate(-x - indibox.width/2, -y);
    }

    /**
     * Outputs a family box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    @Override
    public void drawFamBox(FamBox fambox, int x, int y, int gen) {
        if (!flip)
        {
            elements.drawFamBox(fambox, x, y, gen);
            return;
        }
        graphics.translate(x + fambox.width/2, y);
        graphics.scale(-1, 1);
        elements.drawFamBox(fambox, -fambox.width/2, 0, gen);
        graphics.scale(-1, 1);
        graphics.translate(-x - fambox.width/2, -y);
    }

    /**
     * Initializes the graphics.
     */
    @Override
    public void header(int width, int height) {
        if (flip)
        {
            graphics.translate(width/2, 0);
            graphics.scale(-1, 1);
            graphics.translate(-width/2, 0);
        }
        elements.header(width, height);
    }

    /**
     * Footer.
     */
    @Override
    public void footer() {
        elements.footer();
    }
}
