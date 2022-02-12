/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree;

import static ancestris.report.svgtree.LayoutFactory.ROTATE_0;
import static ancestris.report.svgtree.LayoutFactory.ROTATE_180;
import static ancestris.report.svgtree.LayoutFactory.ROTATE_270;
import static ancestris.report.svgtree.LayoutFactory.ROTATE_90;
import ancestris.report.svgtree.graphics.GraphicsRenderer;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Rotates the whole image.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RotateRenderer implements GraphicsRenderer {

    private final GraphicsRenderer renderer;

    /**
     * Image rotation.
     */
    private int rotation = ROTATE_0;

    public RotateRenderer(GraphicsRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRotation(int set) {
        this.rotation = set;
    }
    
    @Override
    public int getImageHeight() {
        if (rotation == ROTATE_0 || rotation == ROTATE_180) {
            return renderer.getImageHeight();
        }
        return renderer.getImageWidth();
    }

    @Override
    public int getImageWidth() {
        if (rotation == ROTATE_0 || rotation == ROTATE_180) {
            return renderer.getImageWidth();
        }
        return renderer.getImageHeight();
    }

    @Override
    public void render(Graphics2D graphics) {
        AffineTransform transform = graphics.getTransform();
        switch (rotation) {
            case ROTATE_90:
                graphics.translate(renderer.getImageHeight(), 0);
                graphics.rotate(Math.PI / 2);
                break;
            case ROTATE_180:
                graphics.translate(renderer.getImageWidth(), renderer.getImageHeight());
                graphics.rotate(Math.PI);
                break;
            case ROTATE_270:
                graphics.translate(0, renderer.getImageWidth());
                graphics.rotate(-Math.PI / 2);
                break;
        }
        renderer.render(graphics);
        graphics.setTransform(transform);
    }
}
