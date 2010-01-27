/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.fop.svg.PDFDocumentGraphics2D;


/**
 * Outputs generated graphics to a PDF file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class PdfWriter extends GraphicsFileOutput
{
	/**
     * Writes the drawing to the output stream.
     * @param out  destination output stream
     * @param renderer this object renders the drawing
     */
	public void write(OutputStream out, GraphicsRenderer renderer) throws IOException {
        PDFDocumentGraphics2D pdfGraphics = new PDFDocumentGraphics2D(true, out,
            renderer.getImageWidth(), renderer.getImageHeight());
        pdfGraphics.setGraphicContext(new GraphicContext());
        pdfGraphics.fill(new Rectangle(0, 0, 1, 1)); // initialize graphics
        renderer.render(pdfGraphics);
        pdfGraphics.finish();
	}

	public String getFileExtension() {
		return "pdf";
	}
}
