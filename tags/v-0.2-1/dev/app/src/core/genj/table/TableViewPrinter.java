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
package genj.table;

import genj.gedcom.Property;
import genj.gedcom.PropertySimpleValue;
import genj.print.Printer;
import genj.renderer.PropertyRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

/**
 * A print renderer for table */
public class TableViewPrinter implements Printer {
  
  private int pad = 2;
  
  /** print state */
  private int[] 
    rowHeights,
    colWidths,
    colsOnPage,
    rowsOnPage;
  private int 
  	headerHeight,
    pageWidth,
    pageHeight;

  /** the table view */
  private TableView table;
  
  /** font */
  private Font font = new Font("SansSerif", Font.PLAIN, 8);
  private FontRenderContext context = new FontRenderContext(null, false, true);

  private Property header = new PropertySimpleValue();
  
  private int maxColumnWidth = 25;
  
  /**
   * property - max column size
   */
  public int getMaxColumnWidth() {
    return maxColumnWidth;
  }
  
  /**
   * property - max column size
   */
  public void setMaxColumnWidth(int set) {
    maxColumnWidth = Math.max(1, Math.min(100, set));
  }
  
  /**
   * Sets the view to print   */
  public void setView(JComponent view) {
    table = (TableView)view;
  }

  /**
   * @see genj.print.Printer#calcSize(Dimension2D, Point)
   */
  public Dimension calcSize(Dimension2D pageSizeInInches, Point dpi) {

    TableModel model = table.getModel();
    
    // prepare data
    pageWidth = (int)Math.ceil(pageSizeInInches.getWidth()*dpi.x);
    pageHeight = (int)Math.ceil(pageSizeInInches.getHeight()*dpi.y);
    headerHeight = 0;
    
    
    rowHeights = new int[model.getRowCount()];
    colWidths = new int[model.getColumnCount()];
    
    // calculate header parameters
    for (int col=0;col<colWidths.length;col++) {
      header.setValue(model.getColumnName(col));
      calcSize(-1, col, header, dpi);
    }
    
    // analyze all rows
    for (int row=0, height=0;row<rowHeights.length;row++) {
      // analyze all columns
      for (int col=0;col<colWidths.length;col++) {
        // add cell
        calcSize(row, col, (Property)model.getValueAt(row,col), dpi);
      }
      // next row
    }

    // Prepare result
    int pagesx = 1;
    int pagesy = 1;
    colsOnPage = new int[colWidths.length];
    rowsOnPage = new int[rowHeights.length];
    
    // calculate pages horizontally
    for (int col=0, width=0;col<colWidths.length;col++) {
      // too much for current page? 
      if (width+colWidths[col]>pageWidth) {
        width = 0;
        pagesx++;
      }
      // increase columns on current page
      colsOnPage[pagesx-1]++;
      // increase width
      width += colWidths[col] + pad;
    }
    
    // calculate pages vertically
    for (int row=0, height=headerHeight+pad;row<rowHeights.length;row++) {
      // too much for current page?
      if (height+rowHeights[row]>pageHeight) {
        height = headerHeight+pad;
        pagesy ++;
      }
      // increase rows on current page
      rowsOnPage[pagesy-1]++;
      // increase height
      height += rowHeights[row] + pad;
    }
    
    // done
    return new Dimension(pagesx,pagesy);
  }

  /**
   * Registers a cell with given dimensions into the colwidths/rowheights state
   */
  private void calcSize(int row, int col, Property prop, Point dpi) {
    // need property
    if (prop==null)
      return;
    // grab size
    Dimension2D dim = PropertyRenderer.get(prop).getSize(font, context, prop, new HashMap(), dpi);
    // keep height
    if (row<0)
      headerHeight    = max(dim.getHeight(), headerHeight, pageHeight - headerHeight - pad);
    else
      rowHeights[row] = max(dim.getHeight(), rowHeights[row], pageHeight - headerHeight - pad);
    // keep width
    colWidths[col] = max(dim.getWidth(), colWidths[col], pageWidth*maxColumnWidth/100);
    // done
  }
  
  /**
   * Helper - maximum of two values smaller than limit
   */
  private int max(double one, int two, int limit) {
    return Math.min(limit, (int)Math.max(Math.ceil(one), two));
  }
  
  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Dimension2D pageSizeInInches, Point dpi, boolean preview) {
    
    // no columns no content
    if (colsOnPage[page.x]==0)
      return;
    
    // scale to 1/72 inch space
    g.scale(dpi.x/72F, dpi.y/72F);

    // prepare rendering characteristics
    g.setColor(Color.BLACK);
    g.setFont(font);

    // grab model
    TableModel model = table.getModel();
    
    // identify column/row for this page
    int scol=0, cols=0;
    for (int c=0;c<page.x;c++)
      scol += colsOnPage[c];
    cols = colsOnPage[page.x];
    
    // draw header
    for (int col=0,x=0;col<cols;col++) {
      // render in given space
      Rectangle r = new Rectangle(x, 0, colWidths[scol+col], headerHeight); 
      header.setValue(model.getColumnName(scol+col));
      render(g, r, header, dpi);
      // increase current horizontal position
      x += r.getWidth() + pad;
      // draw line between cols
      if (col<cols-1)
        g.drawLine(x - pad/2, 0, x - pad/2, pageHeight);
    }
    g.drawLine(0, headerHeight + pad/2, pageWidth, headerHeight + pad/2);
    
    // draw rows - there might be no rows on current page - only header!
    if (rowsOnPage.length>0) {
      int rows = rowsOnPage[page.y];
      int srow=0;
      for (int r=0;r<page.y;r++)
        srow += rowsOnPage[r];
      
      for (int row=0,y=headerHeight+pad;row<rows;row++) {
        // draw cols
        for (int col=0,x=0;col<cols;col++) {
          // render in given space
          Rectangle r = new Rectangle(x, y, colWidths[scol+col], rowHeights[srow+row]);
          render(g, r, (Property)model.getValueAt(srow+row, scol+col), dpi);
          // increase current horizontal position
          x += colWidths[scol+col] + pad;
        }
        // increase current vertical position
        y += rowHeights[srow+row] + pad;
        // draw line between rows
        if (row<rows-1)
          g.drawLine(0, y - pad/2, pageWidth, y - pad/2);
        // next row
      }
    }
    
    // done
  }

  /**
   * Render a property
   */
  private void render(Graphics2D g, Rectangle r, Property prop, Point dpi) {
    // need property
    if (prop==null)
      return;
    // set clip
    Shape clip = g.getClip();
    g.clip(r);
    // grab renderer and render
    PropertyRenderer.get(prop).render(g, r, prop, new HashMap(), dpi);
    // restore clip
    g.setClip(clip);
    // done
  }
  
} //TreePrintRenderer

