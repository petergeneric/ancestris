/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree;

import ancestris.report.svgtree.graphics.GraphicsRenderer;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Rotates the whole image.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RotateRenderer implements GraphicsRenderer
{
    public static final int ROTATE_0 = 0; // No transformation
    public static final int ROTATE_270 = 1;
    public static final int ROTATE_180 = 2;
    public static final int ROTATE_90 = 3;

    private final GraphicsRenderer renderer;

    /**
     * Image rotation.
     */
    public int rotation = ROTATE_0;
    public String[] rotations = { "none", "270", "180" , "90" };

    public RotateRenderer(GraphicsRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Override
    public int getImageHeight()
    {
        if (rotation == ROTATE_0 || rotation == ROTATE_180)
            return renderer.getImageHeight();
        return renderer.getImageWidth();
    }

    @Override
    public int getImageWidth()
    {
        if (rotation == ROTATE_0 || rotation == ROTATE_180)
            return renderer.getImageWidth();
        return renderer.getImageHeight();
    }

    @Override
    public void render(Graphics2D graphics)
    {
        AffineTransform transform = graphics.getTransform();
        switch (rotation) {
            case ROTATE_90:
                graphics.translate(renderer.getImageHeight(), 0);
                graphics.rotate(Math.PI/2);
                break;
            case ROTATE_180:
                graphics.translate(renderer.getImageWidth(), renderer.getImageHeight());
                graphics.rotate(Math.PI);
                break;
            case ROTATE_270:
                graphics.translate(0, renderer.getImageWidth());
                graphics.rotate(-Math.PI/2);
                break;
        }
        renderer.render(graphics);
        graphics.setTransform(transform);
    }
}
