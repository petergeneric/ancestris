/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;


/**
 * Outputs generated graphics to a PNG file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class PngWriter extends GraphicsFileOutput
{
	/**
     * Writes the drawing to the output stream.
     * @param out  destination output stream
     * @param renderer this object renders the drawing
     */
	public void write(OutputStream out, GraphicsRenderer renderer) throws IOException
    {
        BufferedImage image = new BufferedImage(renderer.getImageWidth(), renderer.getImageHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        renderer.render(graphics);

        //write image to file
        ImageIO.write(image, "png", out);
	}

	public String getFileExtension() {
		return "png";
	}
}
