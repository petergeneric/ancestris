/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.modules.commonAncestor.graphics;


import java.io.IOException;


/**
 * Interface for classes writing report output.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface IGraphicsOutput {

  /**
   * Writes the family tree to the output.
   */
  public void output(IGraphicsRenderer renderer) throws IOException;

  /**
   * Displays the generated content.
   */
  public Object result();

  /**
   * Returns the file extension for this file type.
   * @return File extension without leading dot
   */
  public String getFileExtension();
}
