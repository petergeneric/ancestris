/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import java.awt.Dimension;
import java.awt.Graphics2D;

import tree.IndiBox;
import tree.graphics.GraphicsRenderer;

/**
 * Common code for family tree rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class TreeRendererBase implements GraphicsRenderer {

    /**
     * Size of image margin.
     */
    protected static final int PAGE_MARGIN = 10;

    protected IndiBox firstIndi;

    /**
     * Draws tree elements (boxes, lines, ...).
     */
    protected TreeElements elements;

    public void setFirstIndi(IndiBox firstIndi)
    {
        this.firstIndi = firstIndi;
    }

    public void setElements(TreeElements elements)
    {
        this.elements = elements;
    }

    /**
     * Renders the family tree to the given Graphics2D object.
     */
	public void render(Graphics2D graphics)
	{
        elements.setGraphics(graphics);
        render();
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     */
	public void render() {
		elements.header(getImageWidth(), getImageHeight());
        drawTree(firstIndi, firstIndi.wMinus + PAGE_MARGIN, firstIndi.hMinus + PAGE_MARGIN, 0);
        elements.footer();
	}

    /**
     * Returns the image width (in pixels, including margins)
     */
    public int getImageWidth() {
        return firstIndi.wMinus + firstIndi.wPlus + 2 * PAGE_MARGIN;
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    public int getImageHeight() {
        return firstIndi.hMinus + firstIndi.hPlus + 2 * PAGE_MARGIN;
    }

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     * @param baseX  x coordinate
     * @param baseY  y coordinate
     * @param gen  generation number
     */
    protected abstract void drawLines(IndiBox indibox, int baseX, int baseY);

    /**
     * Returns the position of the family box relative to the individual box.
     */
    protected abstract Dimension getFamboxCoords(IndiBox indibox);

    /**
     * Renders the family tree.
     * @param indibox  first individual
     * @param baseX    x coordinate of individual
     * @param baseY    y coordinate of individual
     * @param gen      generation number of individual
     */
    private void drawTree(IndiBox indibox, int baseX, int baseY, int gen)
    {
        baseX += indibox.x;
        baseY += indibox.y;

        // Draw lines attached to this individual
        drawLines(indibox, baseX, baseY);

        // The individual
        elements.drawIndiBox(indibox, baseX, baseY, gen);

        // Family box
        if (indibox.family != null) {
            Dimension coords = getFamboxCoords(indibox);
            elements.drawFamBox(indibox.family, baseX + coords.width, baseY + coords.height, gen);
        }

        // Spouse
        if (indibox.spouse != null)
            drawTree(indibox.spouse, baseX, baseY, gen);

        // Parent
        if (indibox.parent != null)
            drawTree(indibox.parent, baseX, baseY, gen - 1);

        // Children
        if (indibox.hasChildren())
            for (int i = 0; i < indibox.children.length; i++)
                drawTree(indibox.children[i], baseX, baseY, gen + 1);

        // Next marriage
        if (indibox.nextMarriage != null)
            drawTree(indibox.nextMarriage, baseX, baseY, gen);
    }
}
