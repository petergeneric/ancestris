/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjreports.tree;

import genj.gedcom.Indi;
import genj.report.Report;

import java.io.IOException;

import genjreports.tree.arrange.LayoutFactory;
import genjreports.tree.build.BasicTreeBuilder;
import genjreports.tree.build.TreeBuilder;
import genjreports.tree.filter.DetermineBoxSizes;
import genjreports.tree.filter.TreeFilter;
import genjreports.tree.graphics.GraphicsOutput;
import genjreports.tree.graphics.GraphicsOutputFactory;
import genjreports.tree.graphics.GraphicsRenderer;
import genjreports.tree.output.RendererFactory;
import genjreports.tree.output.TreeElements;
import genjreports.tree.output.TreeElementsFactory;

/**
 * GenJ - ReportGraphicalTree.
 * The report works in 3 phases:
 * <ol>
 * <li> Choose people to display and build the target tree structure</li>
 * <li> Arrange the individual boxes - assign (x, y) coordinates to all boxes</li>
 * <li> Output the final tree to a file or to the screen and display the result</li>
 * </ol>
 * Each of these steps can be separately customized.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version 0.24
 */
public class ReportGraphicalTree extends Report
{
    /**
     * Object used for translating strings.
     */
    private Translator translator = new Translator(this);

    /**
     * Builds the tree structure.
     */
    public TreeBuilder builder = new BasicTreeBuilder();

    /**
     * Provides implementations for drawing elements of the tree.
     */
    public TreeElementsFactory treeElements = new TreeElementsFactory();

    /**
     * Places boxes on the plane.
     */
    public LayoutFactory layouts = new LayoutFactory();

    /**
     * Draws the tree to an output.
     */
    public RendererFactory renderers = new RendererFactory(translator);

    /**
     * Generates file or screen output.
     */
    public GraphicsOutputFactory outputs = new GraphicsOutputFactory();

    public ReportGraphicalTree()
    {
//        // Add options from all components
//        addOptions(builder, BUILDER_CATEGORY);
//        addOptions(layouts, LAYOUT_CATEGORY);
//        addOptions(treeElements, ELEMENTS_CATEGORY);
//        addOptions(renderers, RENDERER_CATEGORY);
//        addOptions(outputs, OUTPUT_CATEGORY);
//
//        // Override categories for these options
//        setCategory("flip", LAYOUT_CATEGORY);
//        setCategory("rotation", LAYOUT_CATEGORY);
    }

    /**
     * The report's entry point
     */
    public Object start(Indi indi) {

        // Build the tree
        IndiBox indibox = builder.build(indi);

        TreeElements elements = treeElements.createElements();

        new DetermineBoxSizes(elements).filter(indibox);

        // Arrange the tree boxes
        TreeFilter arranger = layouts.createLayout();;
        arranger.filter(indibox);

        // Create renderer
        GraphicsRenderer renderer = renderers.createRenderer(indibox, elements);

        // Render and display the tree
        GraphicsOutput output = outputs.createOutput(this);
        if (output == null)  // Report cancelled
        {
            return null;
        }

        try {
            output.output(renderer);
        } catch (OutOfMemoryError e) {
            println("ERROR! The report ran out of memory.\n");
            println("You can try to do the following things:");
            println("  * Increase the memory limit for GenJ");
            println("  * Build a smaller tree");
            println("  * Choose SVG output (requires the least memory)");
        } catch (IOException e) {
            println("Error generating output: " + e.getMessage());
        }

        return output.result(this);
    }

}
