/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjreports.tree.output;

import java.util.ArrayList;
import java.util.List;


/**
 * Creates classes that generate individual and family boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class TreeElementsFactory {
  
    public TreeElements elements;
    public TreeElements rotateElements;
    public TreeElements flipElements;

    public TreeElementsFactory()
    {
        elements = new GraphicsTreeElements();
        rotateElements = new RotateTreeElements(elements);
        flipElements = new FlipTreeElements(rotateElements);
    }

    public TreeElements createElements()
    {
        return flipElements;
    }

}
