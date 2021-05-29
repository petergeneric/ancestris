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
package genj.print;

import genj.renderer.DPI;

/**
 * Page description
 */
public class Page {
  
  private int x,y;
  private double widthInches;
  private double heightInches;
  private DPI dpi;
  
  /**
   * width in inches
   */
  public double width() {
    return widthInches;
  }

  /**
   * height in inches
   */
  public double height() {
    return heightInches;
  }

  public DPI dpi() {
    return dpi;
  }
  
  public int x() {
    if (x==Integer.MAX_VALUE)
      throw new IllegalArgumentException("no index information");
    return x;
  }

  public int y() {
    if (y==Integer.MAX_VALUE)
      throw new IllegalArgumentException("no index information");
    return y;
  }

  /**
   * Constructor - page for querying
   */
  public Page(double widthInches, double heightInches, DPI dpi) {
    this.x = Integer.MAX_VALUE;
    this.y = Integer.MAX_VALUE;
    this.widthInches = widthInches;
    this.heightInches = heightInches;
    this.dpi = dpi;
  }
  
  /**
   * Constructor - page for printing
   */
  public Page(int x, int y, double widthInches, double heightInches, DPI dpi) {
    if (x<0||x==Integer.MAX_VALUE)
      throw new IllegalArgumentException("invalid x");
    if (y<0||y==Integer.MAX_VALUE)
      throw new IllegalArgumentException("invalid y");
    this.x = x;
    this.y = y;
    this.widthInches = widthInches;
    this.heightInches = heightInches;
    this.dpi = dpi;
  }
}
