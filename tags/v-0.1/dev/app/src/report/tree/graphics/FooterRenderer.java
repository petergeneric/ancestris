/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.gedcom.PropertyChange;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import tree.IndiBox;
import tree.Translator;
import tree.filter.TreeFilterBase;
import tree.output.GraphicsTreeElements;

/**
 * Displays a footer below the rendered image.
 * The footer can containt the last change time of the displayed set of information,
 * the last change time of the whole gedcom or the current time.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class FooterRenderer implements GraphicsRenderer
{
    private static final String FOOTER_PREFIX = "footer.";

    /**
     * Margin between the footer and other elements.
     */
    public static final int MARGIN = 5;

    /**
     * Footer font size.
     */
    private static int FONT_SIZE = 10;

    /**
     * Footer mode. Changes the contents of the footer.
     */
    public int footer_mode = 1;

    public String[] footer_modes = { "none", "local", "global", "current" };

    /**
     * The date to be displayed.
     */
    private String displayDate;

    /**
     * The renderer that renders the actual image.
     */
    private GraphicsRenderer renderer;

    /**
     * Translates strings using the report.
     */
    private Translator translator;

    /**
     * Creates the object.
     * @param renderer  image renderer
     */
    public FooterRenderer(GraphicsRenderer renderer, Translator translator)
    {
        this.renderer = renderer;
        this.translator = translator;
    }

    /**
     * Initializes the date to be displayed from the given tree data.
     * @param firstIndi  first individual in the tree
     */
    public void setFirstIndi(IndiBox firstIndi)
    {
        displayDate = null;

        switch (footer_mode)
        {
        case 1:
            FindDateOfModification filter = new FindDateOfModification();
            filter.filter(firstIndi);
            displayDate = filter.mostRecent;
            if (displayDate != null)
                break;
        case 2:
            PropertyChange lastChange = firstIndi.individual.getGedcom().getLastChange();
            if (lastChange != null)
                displayDate = lastChange.toString();
            break;
        case 3:
            PropertyChange currentTime = new PropertyChange();
            currentTime.setTime(System.currentTimeMillis());
            displayDate = currentTime.toString();
            break;
        }
        if (displayDate == null)
            displayDate = translator.translate("not_available");
    }

    public int getImageHeight()
    {
        if (footer_mode == 0)
            return renderer.getImageHeight();
        return renderer.getImageHeight() + FONT_SIZE + MARGIN;
    }

    public int getImageWidth()
    {
        return renderer.getImageWidth();
    }

    /**
     * Renders the footer and calls the enclosed renderer to render the image.
     */
    public void render(Graphics2D graphics)
    {
        if (footer_mode != 0)
        {
            graphics.setBackground(Color.WHITE);
            graphics.clearRect(0, 0, getImageWidth(), getImageHeight());

            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("verdana", Font.PLAIN, FONT_SIZE));
            String caption = translator.translate(FOOTER_PREFIX + footer_modes[footer_mode]) + ": " + displayDate;
            GraphicsTreeElements.alignRightString(graphics, caption, getImageWidth() - MARGIN, getImageHeight() - FONT_SIZE / 4 - MARGIN);
        }
        renderer.render(graphics);
    }

    /**
     * Finds the most recent chage date among all individuals and families in the tree.
     */
    private static class FindDateOfModification extends TreeFilterBase
    {
        long mostRecentTime = 0;
        String mostRecent = null;

        @Override
        protected void preFilter(IndiBox indibox)
        {
            checkDate(indibox.individual.getLastChange());
            if (indibox.family != null)
                checkDate(indibox.family.family.getLastChange());
        }

        private void checkDate(PropertyChange lastChange)
        {
            if (lastChange == null)
                return;

            long time = lastChange.getTime();
            if (time > mostRecentTime)
            {
                mostRecentTime = time;
                mostRecent = lastChange.toString();
            }
        }
    }
}
