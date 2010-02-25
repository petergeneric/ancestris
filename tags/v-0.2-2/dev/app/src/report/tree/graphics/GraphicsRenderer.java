/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import java.awt.Graphics2D;

/**
 * Interface for classes drawing images using a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface GraphicsRenderer {

    /**
     * Renders the image on the given Graphics2D object.
     */
    public void render(Graphics2D graphics);

    /**
     * Returns the image width in pixels.
     */
    public int getImageWidth();

    /**
     * Returns the image height in pixels.
     */
    public int getImageHeight();
}
