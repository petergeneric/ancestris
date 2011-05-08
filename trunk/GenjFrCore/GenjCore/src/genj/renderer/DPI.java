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
package genj.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Dots per inch
 */
public class DPI {

  public final static Key KEY = new DPIHintKey();

  public final static double INCH = 2.54D;

  private int horizontal, vertical;
  
  public int horizontal() {
    return horizontal;
  }
  
  public int vertical() {
    return vertical;
  }
  
  public DPI(int horizontal, int vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }
  
  /**
   * resolve DPI From graphics
   */
  public static DPI get(Graphics graphics) {
    DPI dpi = (DPI)((Graphics2D)graphics).getRenderingHint(KEY);
    if (dpi==null)
      dpi = Options.getInstance().getDPI();
    return dpi;
  }
  
  public Dimension2D toPixel(Dimension2D inches) {
    return new gj.awt.geom.Dimension2D.Double(
        inches.getWidth() * horizontal,
        inches.getHeight() * vertical
    );
  }
  
  public Rectangle2D toPixel(Rectangle2D inches) {
    return new Rectangle2D.Double(
      inches.getX() * horizontal,
      inches.getY() * vertical,
      inches.getWidth() * horizontal,
      inches.getHeight() * vertical
    );
  }
  
  public Line2D toPixel(Line2D inches) {
    return new Line2D.Double(toPixel(inches.getP1()), toPixel(inches.getP2()));
  }
  
  public Point2D toPixel(Point2D inches) {
    return new Point2D.Double(inches.getX() * horizontal, inches.getY() * vertical);
  }
  
  @Override
  public String toString() {
    return horizontal+" by "+vertical+" dpi";
  }
 
  /**
   * a rendering hint for hinting at dpi
   */
  private static class DPIHintKey extends Key {
    
    private DPIHintKey() {
      super(0);
    }

    @Override
    public boolean isCompatibleValue(Object val) {
      return val instanceof DPI;
    }
  }  
}
