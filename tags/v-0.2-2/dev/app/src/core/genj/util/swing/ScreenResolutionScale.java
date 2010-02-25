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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.swing.JComponent;

/**
 * A component that lets the user adjust the resolution
 * by dragging a ruler */
public class ScreenResolutionScale extends JComponent {
  
  /** current dpi */
  private Point dpi = new Point( 
    Toolkit.getDefaultToolkit().getScreenResolution(),
    Toolkit.getDefaultToolkit().getScreenResolution()
  );
  
  /** dpi2cm */
  private final static float DPI2CM = 1F/2.54F;

  /**
   * Constructor   */
  public ScreenResolutionScale(Point dpi) {
    setDPI(dpi);
    addMouseMotionListener(new MouseGlue());
  }
  
  /**
   * Accessor - dpi
   */
  public Point getDPI() {
    return new Point(dpi);
  }

  /**
   * Accessor - dpi
   */
  public void setDPI(Point set) {
    dpi.setLocation(set);
  }

  /**
   * Accessor - dpc
   */
  public Point2D getDPC() {
    return new Point2D.Float(
      DPI2CM * dpi.x,
      DPI2CM * dpi.y
    );
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics graphcs) {
    
    // clear background
    graphcs.setColor(Color.white);
    graphcs.fillRect(0,0,getWidth(),getHeight());
    graphcs.setColor(Color.black);
    graphcs.drawRect(0,0,getWidth()-1,getHeight()-1);

    // draw label
    paintLabel(graphcs);
    
    // draw scale
    paintScale(graphcs);
    
    // done
  }
  
  /**
   * draw the scale   */
  private void paintScale(Graphics graphcs) {

    // wrap it
    UnitGraphics gw = new UnitGraphics(graphcs, DPI2CM * dpi.x, DPI2CM * dpi.y);
    gw.setAntialiasing(true);

    // set font
    gw.setFont(new Font("Arial", Font.PLAIN, 10));
    
    // draw ticks   
    Rectangle2D clip = gw.getClip();

    int X=1;
    do {
      // segment
      gw.setColor(Color.gray);
      for (double x=0.1; x<0.9; x+=0.1)
        gw.draw(X-x,0,X-x,0.1);
      gw.setColor(Color.black);
      gw.draw(X,0,X,0.4);
      gw.draw(""+X, X, 1, 0.0D, 0.0D);
      // next
    } while (X++<clip.getMaxX());

    int Y=1;
    do {
      // segment
      gw.setColor(Color.gray);
      for (double y=0.1; y<0.9; y+=0.1)
        gw.draw(0,Y-y,0.1,Y-y);
      gw.setColor(Color.black);
      gw.draw(0,Y,0.4,Y);
      gw.draw(""+Y, 1, Y, 0.0, 0.0);
      // next
    } while (Y++<clip.getMaxY());
    // done
  }
  
  /**
   * draw the label in the middle
   */
  private void paintLabel(Graphics graphcs) {
    graphcs.setColor(Color.black);
    FontMetrics fm = graphcs.getFontMetrics(); 
    int
      fh = fm.getHeight(),
      fd = fh - fm.getDescent();
    
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
    String[] txt = new String[]{
      ""+nf.format(dpi.x),
      "by",
      ""+nf.format(dpi.y),
      "DPI"
    };
    for (int i = 0; i < txt.length; i++) {
      graphcs.drawString(
        txt[i], 
        getWidth()/2 - fm.stringWidth(txt[i])/2, 
        getHeight()/2 - txt.length*fh/2 + i*fh + fh
      );
    }
    
    graphcs.drawString("cm", 16, 16+fm.getAscent());
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(3*dpi.x, 3*dpi.y);
  }
  
  /**
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return new Dimension(64,64);
  }


  /**
   * Glue for mouse events   */
  private class MouseGlue extends MouseAdapter implements MouseMotionListener {
    
    /** the axis that is modified */
    private boolean axis;
    
    /** the start position of a drag */
    private Point startPos = new Point();
    
    /** the start dotsPcms of a drag */
    private Point startDPI = new Point();
    
    /**
     * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
      
      // remember current position
      startPos.x = e.getPoint().x;
      startPos.y = e.getPoint().y;
      startDPI.x = dpi.x;
      startDPI.y = dpi.y;

      // check mode n-s/w-e      
      axis = startPos.x>startPos.y;
      
      // update cursor
      setCursor(Cursor.getPredefinedCursor(axis?Cursor.E_RESIZE_CURSOR:Cursor.S_RESIZE_CURSOR));
      
      // done
    }

    /**
     * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      // update axis resolution
      float 
        x = e.getPoint().x,
        y = e.getPoint().y;
      if (axis) 
        dpi.x = (int)Math.max(10, startDPI.x * (x/startPos.x) );
      else     
        dpi.y = (int)Math.max(10, startDPI.y * (y/startPos.y) );
        
      // show it
      repaint();
    }
    
  } //MouseGlue
} //ResolutionRuler
