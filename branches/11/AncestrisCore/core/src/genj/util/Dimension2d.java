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
package genj.util;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

/**
 * Impl for Dimension2D
 */
public class Dimension2d extends Dimension2D {
  
  private float width  = 0;
  private float height = 0;

  public Dimension2d(Rectangle2D rectangle) {
    width = (float)rectangle.getWidth();
    height= (float)rectangle.getHeight();
  }
  
  public Dimension2d() {
  }
  
  public Dimension2d(double width, double height) {
    setSize(width, height);
  }
  
  public double getHeight() {
    return height;
  }

  public double getWidth() {
    return width;
  }

  public void setSize(double width, double height) {
    this.width = (float)width;
    this.height= (float)height;
  }
  
  public String toString() {
    return width + " x " + height;
  }
  
  public Dimension toDimension() {
    return new Dimension(
      (int)Math.ceil(width),
      (int)Math.ceil(height)
    );
  }

  public static Dimension getDimension(Dimension2D dim) {
    return new Dimension(
      (int)Math.ceil(dim.getWidth()),
      (int)Math.ceil(dim.getHeight())
    );
  }
  
} //Dimension2d

