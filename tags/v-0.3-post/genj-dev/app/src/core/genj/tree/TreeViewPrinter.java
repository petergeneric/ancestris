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
package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.Printer;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * A print renderer for tree */
public class TreeViewPrinter implements Printer {
  
  /** the tree view */
  private TreeView tree;
  
  /**
   * Sets the view to print   */
  public void setView(JComponent view) {
    tree = (TreeView)view;
  }


  /**
   * @see genj.print.Printer#calcSize(Dimension2D, Point)
   */
  public Dimension calcSize(Dimension2D pageSizeInInches, Point dpi) {
    Rectangle mmbounds = tree.getModel().getBounds();
    return new Dimension(
      (int)Math.ceil(mmbounds.width*0.1F/2.54F / pageSizeInInches.getWidth()), 
      (int)Math.ceil(mmbounds.height*0.1F/2.54F  / pageSizeInInches.getHeight())
    );
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Dimension2D pageSizeInInches, Point dpi, boolean preview) {

    // translate to correct page and give a hint of renderable space in gray
    UnitGraphics ug = new UnitGraphics(g, dpi.x, dpi.y);
    ug.setColor(Color.LIGHT_GRAY);
    ug.draw(new Rectangle2D.Double(0,0,pageSizeInInches.getWidth(),pageSizeInInches.getHeight()),0,0);
    ug.translate(
      -page.x*pageSizeInInches.getWidth(), 
      -page.y*pageSizeInInches.getHeight()
    );

    // prepare rendering on mm/10 space
    UnitGraphics graphics = new UnitGraphics(g, dpi.x/2.54F*0.1D, dpi.y/2.54F*0.1D);
    
    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.selection      = null;

    if (!preview) {    
      renderer.indiRenderer   = tree.createEntityRenderer(Gedcom.INDI).setResolution(dpi);
      renderer.famRenderer    =  tree.createEntityRenderer(Gedcom.FAM).setResolution(dpi);
    }
    
    renderer.render(graphics, tree.getModel());

  }

} //TreePrintRenderer
