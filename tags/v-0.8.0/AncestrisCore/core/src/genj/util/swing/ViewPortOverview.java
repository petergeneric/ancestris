/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A component showing an overview for a viewport
 */
public class ViewPortOverview extends JPanel {
  
  /** the square dimension used for resizing */
  private final static int DIM_RESIZE = 6;

  /** keep the viewport */
  private JViewport viewport;
  
  /** last indicator painted */
  private Rectangle last;
  
  /**
   * Constructor
   */
  public ViewPortOverview(JViewport viewpOrt) {
    viewport = viewpOrt;
    EventGlue glue = new EventGlue();    
    viewport.addChangeListener(glue);
    addMouseListener(glue); 
    addMouseMotionListener(glue);
    viewport.addComponentListener(glue);
  }
  
  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  public void paint(Graphics g) {
    
    // clear it
    Dimension dim = getSize();
    g.setColor(Color.white);
    g.fillRect(0,0,dim.width,dim.height);

    // calculate zoom
    Point2D zoom = getZoom();

    // let subclass handle content rendering
    renderContent(g, zoom.getX(), zoom.getY());

    // frame it
    g.setColor(new Color(0, 128, 0));
    g.drawRect(0,0,dim.width-1,dim.height-1);

    // do we have the zoom
    if (zoom==null) zoom = getZoom();
    
    // build rect
    Rectangle shown = viewport.getViewRect();
    last= new Rectangle(
      (int)(shown.x     * zoom.getX()),
      (int)(shown.y     * zoom.getY()),
      (int)(shown.width * zoom.getX()),
      (int)(shown.height* zoom.getY())
    );
    
    // indicate content bounds
    Graphics2D g2d = (Graphics2D)g;
    g.drawRect(last.x, last.y, last.width, last.height);
    g2d.setColor(new Color(0, 255, 0, 64));
    g.fillRect(last.x, last.y, last.width, last.height);
    
  }
  
  /**
   * Override for specific rendering
   */
  protected void renderContent(Graphics g, double zoomx, double zoomy) {
  }
  
  /**
   * Helper that calculates the correct zoom for the viewport's content 'view'
   * to fit in this overview
   */
  private Point2D getZoom() {

    // need available space (ours) and used space (view's)  
    Dimension avail = getSize();
    
    Component c = viewport.getView();
    if (c instanceof ViewPortAdapter) c = ((ViewPortAdapter)c).getComponent();
    Dimension view = c.getSize();
    
    // done
    return new Point2D.Double(
      ((double)avail.width )/view.width ,
      ((double)avail.height)/view.height
    );
  }
  
  /**
   * Event glue
   */
  private class EventGlue
    extends ComponentAdapter
  	implements ChangeListener, MouseListener, MouseMotionListener
  {
    /** tracking dragging offset*/
    private Point dragOffset = null;  
    /** resizing */
    private boolean isResize = false;
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
      if (!isVisible()) return;
      repaint();
    }
    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // center in position
      
      Rectangle shown = viewport.getViewRect();
      Point2D zoom = getZoom();
      int 
        x = (int)(e.getPoint().x/zoom.getX()),
        y = (int)(e.getPoint().y/zoom.getY());

      // scroll
      viewport.setViewPosition(new Point(x-shown.width/2,y-shown.height/2));
      
    }
    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      Point p = e.getPoint();
      // check for resize
      isResize = isResize(p);
      // check for a drag offset
      dragOffset = last.contains(p) ? new Point(last.x-p.x, last.y-p.y) : null;
      // done
    }
    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
      // cleanup dragging
      dragOffset = null;
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      Point p = e.getPoint();
      // resizing
      if (isResize) {
        setSize(new Dimension(p.x, p.y));
        return;
      } 
      // no drag start?
      if (dragOffset==null) return;
      // calculate position
      Point2D zoom = getZoom();
      Rectangle shown = viewport.getViewRect();
      int 
        x = (int)(((double)(p.x + dragOffset.x))/zoom.getX()),
        y = (int)(((double)(p.y + dragOffset.y))/zoom.getY());
      // scroll
      viewport.scrollRectToVisible(new Rectangle(
        x-shown.x,y-shown.y,shown.width,shown.height
      ));
      // done
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
      // the location
      Point p = e.getPoint();
      // assuming default
      int cursor = Cursor.DEFAULT_CURSOR;
      // maybe scroll view?
      if (last!=null&&last.contains(p))
        cursor = Cursor.MOVE_CURSOR;
      // check if resize
      if (isResize(p))
        cursor = Cursor.SE_RESIZE_CURSOR;
      // set it
      setCursor(Cursor.getPredefinedCursor(cursor));
    }
    
    /**
     * Helper that decides whether we're resizing 
     */
    private boolean isResize(Point p) {
      Dimension dim = getSize();
      return p.x>dim.width-DIM_RESIZE&&p.y>dim.height-DIM_RESIZE;
    }
    
    /**
     * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
    	repaint();
    }

  } //EventGlue

} //ViewPortOverview
