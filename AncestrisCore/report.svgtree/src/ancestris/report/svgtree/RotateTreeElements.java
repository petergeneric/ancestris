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
import java.awt.geom.AffineTransform;

/**
 * Rotates information boxes, preserving their position.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RotateTreeElements extends FilterTreeElements {

    public static final int ROTATE_0 = 0; // No transformation
    public static final int ROTATE_270 = 1;
    public static final int ROTATE_180 = 2;
    public static final int ROTATE_90 = 3;

    /**
     * Tree rotation.
     */
    public int boxrotation = 0;

    public String[] boxrotations = { "none", "270", "180" , "90" };

    private final Translator translator;
    
    /**
     * Constructs the object.
     */
    public RotateTreeElements(Translator translator, Graphics2D graphics, TreeElements elements) {
        super(graphics, elements);
        this.translator = translator;
        for (int i = 0; i < boxrotations.length; i++) {
            boxrotations[i] = translator.translate("rotation." + boxrotations[i]);
        }
    }

    public RotateTreeElements(Translator translator, TreeElements elements) {
        this(translator, null, elements);
    }

    /**
     * Outputs a rotated image of an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    @Override
    public void drawIndiBox(IndiBox indibox, int x, int y, int gen) {
        AffineTransform transform = graphics.getTransform();
        transform(x, y, indibox.width, indibox.height);
        transpose(indibox);
        elements.drawIndiBox(indibox, 0, 0, gen);
        transpose(indibox);
        graphics.setTransform(transform);
    }

    /**
     * Outputs a rotated image of a family box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    @Override
    public void drawFamBox(FamBox fambox, int x, int y, int gen) {
        AffineTransform transform = graphics.getTransform();
        transform(x, y, fambox.width, fambox.height);
        transpose(fambox);
        elements.drawFamBox(fambox, 0, 0, gen);
        transpose(fambox);
        graphics.setTransform(transform);
    }

	/**
     * Applies the rotation transformation.
	 */
    private void transform(int x, int y, int w, int h) {
        switch (boxrotation) {
            case ROTATE_0:
                graphics.translate(x, y);
                break;
            case ROTATE_90:
                graphics.translate(x + w, y);
                graphics.rotate(Math.PI/2);
                break;
            case ROTATE_180:
                graphics.translate(x + w, y + h);
                graphics.rotate(Math.PI);
                break;
            case ROTATE_270:
                graphics.translate(x, y + h);
                graphics.rotate(-Math.PI/2);
                break;
        }
    }

    private void transpose(IndiBox indibox) {
        if (boxrotation == ROTATE_0 || boxrotation == ROTATE_180)
            return;
        int tmp = indibox.width;
        indibox.width = indibox.height;
        indibox.height = tmp;
    }

    private void transpose(FamBox fambox) {
        if (boxrotation == ROTATE_0 || boxrotation == ROTATE_180)
            return;
        int tmp = fambox.width;
        fambox.width = fambox.height;
        fambox.height = tmp;
    }

    @Override
    public void getIndiBoxSize(IndiBox indibox)
    {
        elements.getIndiBoxSize(indibox);
        transpose(indibox);
    }

    @Override
    public void getFamBoxSize(FamBox fambox)
    {
        elements.getFamBoxSize(fambox);
        transpose(fambox);
    }
}
