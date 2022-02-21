/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree;

import ancestris.report.svgtree.build.TreeBuilder;
import ancestris.report.svgtree.filter.DetermineBoxSizes;
import ancestris.report.svgtree.filter.TreeFilter;
import ancestris.report.svgtree.graphics.GraphicsOutput;
import ancestris.report.svgtree.graphics.GraphicsRenderer;
import ancestris.report.svgtree.output.RendererFactory;
import ancestris.report.svgtree.output.TreeElements;
import ancestris.report.svgtree.output.TreeElementsFactory;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Indi;
import genj.report.Report;
import java.io.IOException;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * ReportGraphicalTree.
 * The report works in 3 phases:
 * <ol>
 * <li> Choose people to display and build the target tree structure</li>
 * <li> Arrange the individual boxes - assign (x, y) coordinates to all boxes</li>
 * <li> Output the final tree to a file or to the screen and display the result</li>
 * </ol>
 * Each of these steps can be separately customized.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * Improvements Frederic Lapeyre <frederic@ancestris.org>
 * @version 1.0
 */
@ServiceProvider(service = Report.class)
public class ReportGraphicalTree extends Report
{
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);
    /**
     * Object used for translating strings.
     */
    private final Translator translator = new Translator(this);

    /**
     * Places boxes on the plane.
     */
    public LayoutFactory layouts = new LayoutFactory(translator);

    /**
     * Draws the tree to an output.
     */
    public RendererFactory renderers = new RendererFactory(translator);

    /**
     * Builds the tree structure.
     */
    public TreeBuilder builder = new BasicTreeBuilder(translator);

    /**
     * Provides implementations for drawing elements of the tree.
     */
    public TreeElementsFactory treeElements = new TreeElementsFactory(translator);

    /**
     * Generates file or screen output.
     */
    public GraphicsOutputFactory outputs = new GraphicsOutputFactory(translator);
    
    /**
     * Defines colors
     */
    public ColorManager colorManager = new ColorManager();

    /**
     * The report's entry point
     */
    public Object start(Indi indi) {
        
        // Update common parameters (defined in one module, used in another, so not synchronized).
        // Done on purpose : parameters are grouped logically for the user, so they have to be synchronized before starting the report
        ((FlipTreeElements) treeElements.flipElements).setFlip(layouts.flip);
        ((BasicTreeBuilder)builder).setHusbandFirst(layouts.husband_first);
        ((RotateRenderer)renderers.rotateRenderer).setRotation(layouts.rotation);
        treeElements.setColors(colorManager);
        indi.getGedcom().getPlaceDisplayFormat();

        // Build the tree
        LOG.info("WIP: Enter report");
        IndiBox.setTotalBoxes(0);
        LOG.info("WIP: Enter build report");
        IndiBox indibox = builder.build(indi);
        LOG.info("WIP: Exit build report");
        int totalBoxes = IndiBox.getNetTotalBoxes();
        LOG.info("WIP: Resulting in " + totalBoxes + " indi boxes net of " + IndiBox.getTotalBoxes());

        if (totalBoxes > 1000) {
            if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(this.getClass(), "TITL_SizeWarning"), 
                    NbBundle.getMessage(this.getClass(), "MSG_SizeWarning", totalBoxes))
                    .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("report.ReportGraphicalTree").show()) {
                return null;
            }
        }
        
        if (outputs.output_types[outputs.output_type].toUpperCase().contains("PDF") && treeElements.elements.high_quality_images) {
            if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(this.getClass(), "TITL_PDFSizeWarning"), 
                    NbBundle.getMessage(this.getClass(), "MSG_PDFSizeWarning", totalBoxes))
                    .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("report.ReportGraphicalTree").show()) {
                return null;
            }
        }
        
        LOG.info("WIP: Enter create layout");
        TreeElements elements = treeElements.createElements();

        new DetermineBoxSizes(elements).filter(indibox);
        LOG.info("WIP: Exit create layout");

        // Arrange the tree boxes
        TreeFilter arranger = layouts.createLayout();
        arranger.filter(indibox);

        LOG.info("WIP: Enter render");
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
            println("  * Increase the memory limit for Ancestris");
            println("  * Build a smaller tree");
            println("  * Choose SVG output (requires the least memory)");
        } catch (IOException e) {
            println("Error generating output: " + e.getMessage());
        }
        
        LOG.info("WIP: Exit render");

        return output.result(this);
    }

}
