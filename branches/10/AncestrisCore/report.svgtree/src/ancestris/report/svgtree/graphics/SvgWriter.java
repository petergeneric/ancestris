/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.graphics;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Outputs generated graphics to a SVG file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class SvgWriter extends GraphicsFileOutput {

    /**
     * Writes the drawing to the output stream.
     *
     * @param out destination output stream
     * @param renderer this object renders the drawing
     */
    @Override
    public void write(OutputStream out, GraphicsRenderer renderer) throws IOException {
        DOMImplementation domImpl
                = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGraphics = new SVGGraphics2D(document);
        svgGraphics.setSVGCanvasSize(new Dimension(renderer.getImageWidth(),
                renderer.getImageHeight()));
        svgGraphics.getGeneratorContext().setComment("Generated by GenealogyJ with Batik SVG Generator");
        renderer.render(svgGraphics);
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        svgGraphics.stream(writer, false);
    }

    @Override
    public String getFileExtension() {
        return "svg";
    }
}
