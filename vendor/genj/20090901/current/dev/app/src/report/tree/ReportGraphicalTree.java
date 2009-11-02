/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Indi;
import genj.option.PropertyOption;
import genj.report.options.ComponentReport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tree.arrange.LayoutFactory;
import tree.build.BasicTreeBuilder;
import tree.build.TreeBuilder;
import tree.filter.DetermineBoxSizes;
import tree.filter.TreeFilter;
import tree.graphics.GraphicsOutput;
import tree.graphics.GraphicsOutputFactory;
import tree.graphics.GraphicsRenderer;
import tree.output.RendererFactory;
import tree.output.TreeElements;
import tree.output.TreeElementsFactory;

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
public class ReportGraphicalTree extends ComponentReport
{
    private static final String OUTPUT_CATEGORY = "output";
    private static final String ELEMENTS_CATEGORY = "elements";
    private static final String RENDERER_CATEGORY = "renderer";
    private static final String LAYOUT_CATEGORY = "layout";
    private static final String BUILDER_CATEGORY = "builder";

    /**
     * Object used for translating strings.
     */
    private Translator translator = new Translator(this);;

    /**
     * Builds the tree structure.
     */
    private TreeBuilder builder = new BasicTreeBuilder();

    /**
     * Provides implementations for drawing elements of the tree.
     */
    private TreeElementsFactory treeElements = new TreeElementsFactory();

    /**
     * Places boxes on the plane.
     */
    private LayoutFactory layouts = new LayoutFactory();

    /**
     * Draws the tree to an output.
     */
    private RendererFactory renderers = new RendererFactory(translator);

    /**
     * Generates file or screen output.
     */
    private GraphicsOutputFactory outputs = new GraphicsOutputFactory();

    /**
     * Original option values for options that use variable replacing.
     */
    private Map<PropertyOption, Object> originalValues;

    public ReportGraphicalTree()
    {
        // Add options from all components
        addOptions(builder, BUILDER_CATEGORY);
        addOptions(layouts, LAYOUT_CATEGORY);
        addOptions(treeElements, ELEMENTS_CATEGORY);
        addOptions(renderers, RENDERER_CATEGORY);
        addOptions(outputs, OUTPUT_CATEGORY);

        // Override categories for these options
        setCategory("flip", LAYOUT_CATEGORY);
        setCategory("rotation", LAYOUT_CATEGORY);
    }

    /**
     * The result is stored in files
     */
    public boolean usesStandardOut() {
        return false;
    }

    /**
     * The report's entry point
     */
    public void start(Indi indi) {

        // Replace variables
        replaceVariables(indi);

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
            restoreOptionValues();
            return;
        }

        try {
            output.output(renderer);
            output.display(this);
        } catch (OutOfMemoryError e) {
            println("ERROR! The report ran out of memory.\n");
            println("You can try to do the following things:");
            println("  * Increase the memory limit for GenJ");
            println("  * Build a smaller tree");
            println("  * Choose SVG output (requires the least memory)");
        } catch (IOException e) {
            println("Error generating output: " + e.getMessage());
        }

        // Restore option values (those with replaced variables)
        restoreOptionValues();
    }

    /**
     * @param indi
     */
    private void replaceVariables(Indi indi)
    {
        originalValues = new HashMap<PropertyOption, Object>();
        for (PropertyOption option : getOptions())
        {
            if (option.getValue().getClass().equals(String.class))
            {
                String value = (String)option.getValue();
                originalValues.put(option, value);

                value = value.replaceAll("\\$i", indi.getId());
                value = value.replaceAll("\\$n", indi.getName());
                value = value.replaceAll("\\$f", indi.getFirstName());
                value = value.replaceAll("\\$l", indi.getLastName());

                option.setValue(value);
            }
        }
    }

    private void restoreOptionValues()
    {
        for (Map.Entry<PropertyOption, Object> entry : originalValues.entrySet())
            entry.getKey().setValue(entry.getValue());
    }
}
