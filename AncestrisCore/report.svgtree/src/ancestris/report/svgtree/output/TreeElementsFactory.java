/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree.output;

import ancestris.report.svgtree.ColorManager;
import ancestris.report.svgtree.FlipTreeElements;
import ancestris.report.svgtree.RotateTreeElements;
import ancestris.report.svgtree.GraphicsTreeElements;
import ancestris.report.svgtree.Translator;

/**
 * Creates classes that generate individual and family boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class TreeElementsFactory {
  
    public GraphicsTreeElements elements;
    public TreeElements rotateElements;
    public TreeElements flipElements;

    public TreeElementsFactory(Translator translator)
    {
        elements = new GraphicsTreeElements(translator);
        rotateElements = new RotateTreeElements(translator, elements);
        flipElements = new FlipTreeElements(rotateElements);
    }

    public TreeElements createElements()
    {
        return flipElements;
    }
    
    public void setColors(ColorManager colorManager) {
        elements.setColorManager(colorManager);
    }

}
