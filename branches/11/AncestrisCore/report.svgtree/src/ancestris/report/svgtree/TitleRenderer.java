/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree;

import ancestris.report.svgtree.graphics.GraphicsRenderer;
import ancestris.report.svgtree.graphics.GraphicsUtil;
import genj.gedcom.Indi;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;

/**
 * Displays a title above the rendered image.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class TitleRenderer implements GraphicsRenderer
{
    public static final int VERTICAL_MARGIN = 10;
    /**
     * The renderer that renders the actual image.
     */
    private GraphicsRenderer renderer;

    /**
     * Image title.
     */
    public String title = "$n ($i)";
    private String formattedTitle;
    
    public int fontNameTitle = 0;
    public String fontNameTitles[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    /**
     * Title font height. If set to 0, the height is determined automatically.
     */
    public int title_height = 0;

    /**
     * Creates the object.
     * @param renderer  image renderer
     */
    public TitleRenderer(GraphicsRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Override
    public int getImageHeight()
    {
        if (title.equals(""))
            return renderer.getImageHeight();
        return renderer.getImageHeight() + getTitleHeight() + VERTICAL_MARGIN;
    }

    private int getTitleHeight()
    {
        if (title_height > 0)
            return title_height;
        return (renderer.getImageHeight() + renderer.getImageWidth()) / 40; // auto-size
    }

    @Override
    public int getImageWidth()
    {
        return renderer.getImageWidth();
    }

    /**
     * Renders the title and calls the enclosed renderer to render the image.
     */
    @Override
    public void render(Graphics2D graphics)
    {
        if (!title.equals(""))
        {
            graphics.setBackground(Color.WHITE);
            graphics.clearRect(0, 0, getImageWidth(), getImageHeight());

            int height = getTitleHeight();
            graphics.setColor(Color.BLACK);
            checkFont();
            graphics.setFont(new Font(fontNameTitles[fontNameTitle], Font.BOLD, height));
            GraphicsTreeElements.centerString(graphics, formattedTitle, getImageWidth() / 2, height * 3/4 + VERTICAL_MARGIN);

            graphics.translate(0, height + VERTICAL_MARGIN); // Move rendered image below the title
        }
        renderer.render(graphics);
    }
    
    private void checkFont(){
        if (!GraphicsUtil.checkFont(fontNameTitles[fontNameTitle])) {
            fontNameTitles[fontNameTitle] = "verdana";
        }        
    }
    
    /**
     * @param indi
     */
    private String format(String value, Indi indi)
    {
      value = value.replaceAll("\\$i", indi.getId());
      value = value.replaceAll("\\$n", indi.getName());
      value = value.replaceAll("\\$f", indi.getFirstName());
      value = value.replaceAll("\\$l", indi.getLastName());
      return value;
    }

    public void setIndi(IndiBox firstIndi) {
      formattedTitle = format(title, firstIndi.individual);
    }

}
