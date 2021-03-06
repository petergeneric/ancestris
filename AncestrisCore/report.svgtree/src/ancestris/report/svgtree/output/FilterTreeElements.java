/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.output;

import ancestris.report.svgtree.FamBox;
import ancestris.report.svgtree.IndiBox;
import java.awt.Graphics2D;

/**
 * Base class for classes modifying/filtering another TreeElements class.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FilterTreeElements implements TreeElements {

    /**
     * The graphics object to paint on.
     */
    protected Graphics2D graphics = null;

    protected TreeElements elements;

    /**
     * Constructs the object.
     */
    public FilterTreeElements(Graphics2D graphics, TreeElements elements) {
        this.graphics = graphics;
        this.elements = elements;
    }

    public FilterTreeElements(TreeElements elements) {
        this(null, elements);
    }

    /**
     * Sets the Graphics2D object to draw on.
     */
    @Override
    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
        elements.setGraphics(graphics);
    }

    /**
     * Outputs an individual box.
     *
     * @param i individual
     * @param x x coordinate
     * @param y y coordinate
     * @param gen generation number
     */
    @Override
    public void drawIndiBox(IndiBox indibox, int x, int y, int gen) {
        elements.drawIndiBox(indibox, x, y, gen);
    }

    /**
     * Outputs a family box.
     *
     * @param i individual
     * @param x x coordinate
     * @param y y coordinate
     * @param gen generation number
     */
    @Override
    public void drawFamBox(FamBox fambox, int x, int y, int gen) {
        elements.drawFamBox(fambox, x, y, gen);
    }

    /**
     * Outputs a line.
     *
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        elements.drawLine(x1, y1, x2, y2);
    }

    /**
     * Outputs a dashed line.
     *
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    @Override
    public void drawDashedLine(int x1, int y1, int x2, int y2) {
        elements.drawDashedLine(x1, y1, x2, y2);
    }

    /**
     * Initializes the graphics.
     */
    @Override
    public void header(int width, int height) {
        elements.header(width, height);
    }

    /**
     * Footer.
     */
    @Override
    public void footer() {
        elements.footer();
    }

    @Override
    public void getIndiBoxSize(IndiBox indibox) {
        elements.getIndiBoxSize(indibox);
    }

    @Override
    public void getFamBoxSize(FamBox fambox) {
        elements.getFamBoxSize(fambox);
    }
}
