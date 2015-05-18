/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

public class GraphicsHelper {

  /**
   * render text
   */
  public static void render(Graphics2D graphics, String str, Rectangle2D box, double xalign, double yalign) {
    
    FontMetrics fm = graphics.getFontMetrics();
    LineMetrics lm = fm.getLineMetrics(str, graphics);

    float h = 0;
    String[] lines = str.split("\\\n");
    float[] ws = new float[lines.length];
    for (int i=0;i<lines.length;i++) {
      Rectangle2D r = fm.getStringBounds(lines[i], graphics);
      ws[i] = (float)r.getWidth();
      h = Math.max(h,(float)r.getHeight());
    }

    Shape clip = graphics.getClip();
    graphics.clip(box);
    
    for (int i=0;i<lines.length;i++) {
      double x = Math.max(box.getX(), box.getCenterX() - ws[i]*xalign);
      double y = Math.max(box.getY(), box.getY() + (box.getHeight()-lines.length*h)*yalign + i*h + h - lm.getDescent()); 
      
      graphics.drawString(lines[i], (float)x, (float)y);
    }
    
    graphics.setClip(clip);
  }
  
  /**
   * render text
   */
  public static Rectangle render(Graphics2D graphics, String str, double x, double y, double xalign, double yalign) {
    
    FontMetrics fm = graphics.getFontMetrics();
    Rectangle2D r = fm.getStringBounds(str, graphics);
    LineMetrics lm = fm.getLineMetrics(str, graphics);
    
    float h = (float)r.getHeight();
    float w = (float)r.getWidth();
    
    x = x- w*xalign;
    y = y - h*yalign; 
      
    graphics.drawString(str, (float)x, (float)y + h - lm.getDescent());
    
    return new Rectangle((int)x,(int)y,(int)w,(int)h);
  }
  
  public static Icon getIcon(Shape shape, Color color) {
    Dimension size = shape.getBounds().getSize();
    return new ShapeAsIcon(Math.max(size.width, size.height), shape, color);
  }
  
  public static Icon getIcon(int size, Shape shape, Color color) {
    return new ShapeAsIcon(size, shape, color);
  }
  
  public static Icon getIcon(int size, Shape shape) {
    return new ShapeAsIcon(size, shape, null);
  }
  
  public static Icon getIcon(double... shape) {
    return getIcon(null, shape);
  }
  public static Icon getIcon(Color color, double... shape) {
    GeneralPath path = new GeneralPath();
    path.moveTo(shape[0],shape[1]);
    for (int i=2;i<shape.length;i+=2) 
      path.lineTo(shape[i+0], shape[i+1]);
    path.closePath();
    return new ShapeAsIcon(path, color);
  }
  
  /**
   * A shape as icon
   */
  private static class ShapeAsIcon implements Icon {

    private Dimension size;
    private Shape shape;
    private Color color;

    private ShapeAsIcon(Shape shape, Color color) {
      this.color = color;
      this.size = shape.getBounds().getSize();
      this.shape = shape;
    }
    private ShapeAsIcon(int size, Shape shape, Color color) {
      this.color = color;
      this.size = new Dimension(size, size);
      this.shape = shape;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (color!=null)
        g.setColor(color);
      g.translate(x, y);
      ((Graphics2D) g).fill(shape);
      g.translate(-x, -y);
    }

    public int getIconWidth() {
      return size.width;
    }

    public int getIconHeight() {
      return size.height;
    }

  }
}
