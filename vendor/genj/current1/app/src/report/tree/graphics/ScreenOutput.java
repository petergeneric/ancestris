/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.report.Report;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * Displays the report output in a component on screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ScreenOutput implements GraphicsOutput {

    /**
     * Renders output to a Graphics2D object.
     */
    private GraphicsRenderer renderer = null;

    /**
     * The component containing the whole tree view.
     */
    private JComponent view;

    /**
     * Panel containing the main view.
     */
    private JScrollPane pane = new JScrollPane();

    private Point lastPoint;

    /**
     * Constructs the object.
     */
    public ScreenOutput() {
        view = new JComponent() {
            public void paint(Graphics g) {
            	Graphics2D g2 = (Graphics2D)g;
            	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            			RenderingHints.VALUE_ANTIALIAS_ON);
                renderer.render(g2);
            }
        };

        pane.setViewportView(view);
        pane.setPreferredSize(new Dimension(300, 200));

        pane.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		lastPoint = e.getPoint();
        		pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	}

        	public void mouseReleased(MouseEvent e) {
        		pane.setCursor(Cursor.getDefaultCursor());
        	}
        });

        pane.addMouseMotionListener(new MouseMotionAdapter() {

      		private JScrollBar hSb = pane.getHorizontalScrollBar();
    		private JScrollBar vSb = pane.getVerticalScrollBar();

        	public void mouseDragged(MouseEvent e) {
          		int dX = lastPoint.x - e.getX();
        		int dY = lastPoint.y - e.getY();

        		hSb.setValue(hSb.getValue()+dX);
        		vSb.setValue(vSb.getValue()+dY);
        		lastPoint = e.getPoint();
        	}

        });
    }

    /**
     * Prepares the component to be displayed.
     */
    public void output(GraphicsRenderer renderer) {
        this.renderer = renderer;
        view.setPreferredSize(new Dimension(renderer.getImageWidth(),
                renderer.getImageHeight()));


        /* This was supposed to center both scrollbars but it doesn't work
        JScrollBar sb = getHorizontalScrollBar();
        sb.setValue(1); // Without this the next instruction doesn't work correctly
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
        sb = getVerticalScrollBar();
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
        */
    }

    /**
     * Displays the component.
     */
    public void display(Report report) {
        report.showComponentToUser(pane);
    }

    /**
     * Return null because no file is produced.
     */
	public String getFileExtension()
	{
		return null;
	}
}
