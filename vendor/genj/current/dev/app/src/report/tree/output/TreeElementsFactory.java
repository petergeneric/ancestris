/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.report.options.ComponentContainer;

import java.util.ArrayList;
import java.util.List;


/**
 * Creates classes that generate individual and family boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class TreeElementsFactory implements ComponentContainer
{
    private TreeElements elements;
    private TreeElements rotateElements;
    private TreeElements flipElements;

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

    public List<Object> getComponents()
    {
        List<Object> components = new ArrayList<Object>();
        components.add(elements);
        components.add(rotateElements);
        components.add(flipElements);
        components.add(this);
        return components;
    }
}
