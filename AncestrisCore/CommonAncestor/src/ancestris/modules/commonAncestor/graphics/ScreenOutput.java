/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.commonAncestor.graphics;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Displays the report output in a component on screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ScreenOutput implements IGraphicsOutput {

    /**
     * Renders output to a Graphics2D object.
     */
    private IGraphicsRenderer renderer = null;
    /**
     * The component containing the whole tree view.
     */
    private JComponent view;

    /**
     * Constructs the object.
     */
    public ScreenOutput() {

        view = new JComponent() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                renderer.render(g2);
            }
        };
    }

    /**
     * Prepares the component to be displayed.
     */
    public void output(IGraphicsRenderer renderer) {
        this.renderer = renderer;
        view.setPreferredSize(new Dimension(renderer.getImageWidth(),
                renderer.getImageHeight()));
    }

    /**
     * Return the display view.
     */
    public Object result() {
        return view;
    }

    /**
     * Return null because no file is produced.
     */
    public String getFileExtension() {
        return null;
    }
}
