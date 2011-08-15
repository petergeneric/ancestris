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

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Printing Renderer
 */
public interface PageRenderer {
  
  /**
   * Calculate the number of pages required for printing
   * @param pageSize size of page in inches
   * @param dpi resolution
   * @result dimension expressing number of pages horizontally&vertically
   */
  public Dimension getPages(Page page);
  
  /**
   * Render page content 
   * @param g graphics context to render on - (0,0) in graphics space is the top-left
   * @param page page to render (x,y)
   */  
  public void renderPage(Graphics2D g, Page page);
  
} //PrintRenderer
