/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.graphics;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.svg.PDFDocumentGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;

/**
 * Outputs generated graphics to a PDF file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class PdfWriter extends GraphicsFileOutput {

    /**
     * Writes the drawing to the output stream.
     *
     * @param out destination output stream
     * @param renderer this object renders the drawing
     */
    @Override
    public void write(OutputStream out, GraphicsRenderer renderer) throws IOException {

        // Limit to 14400 x 14400 (see 
        int width = renderer.getImageWidth();
        int height = renderer.getImageHeight();
        double scale = 1;

        if (width > 14400) {
            scale = 14400.0 / width;
            width = 14400;
            height *= scale;
        }
        if (height > 14400) {
            scale = 14400.0 / height;
            height = 14400;
            width *= scale;
        }

        PDFDocumentGraphics2D pdfGraphics = new PDFDocumentGraphics2D(true, out, width, height);
        pdfGraphics.setGraphicContext(new GraphicContext());
        pdfGraphics.fill(new Rectangle(0, 0, 1, 1)); // initialize graphics

        if (scale != 1) {
            AffineTransform transform = pdfGraphics.getTransform();
            transform.scale(scale, scale);
            pdfGraphics.setTransform(transform);
        }

        renderer.render(pdfGraphics);
        pdfGraphics.finish();
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }
}
