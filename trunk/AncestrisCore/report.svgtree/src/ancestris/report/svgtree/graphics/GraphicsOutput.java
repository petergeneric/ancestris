/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.graphics;

import genj.report.Report;

import java.io.IOException;

/**
 * Interface for classes writing report output.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface GraphicsOutput {

    /**
     * Writes the family tree to the output.
     */
    public void output(GraphicsRenderer renderer) throws IOException;

    /**
     * Displays the generated content.
     */
    public Object result(Report report);
}
