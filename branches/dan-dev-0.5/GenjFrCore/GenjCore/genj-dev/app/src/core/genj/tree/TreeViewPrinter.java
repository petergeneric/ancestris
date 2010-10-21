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
package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.renderer.DPI;
import genj.renderer.BlueprintRenderer;
import genj.util.swing.UnitGraphics;
import gj.awt.geom.Dimension2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A print renderer for tree */
public class TreeViewPrinter implements PrintRenderer {
  
  private TreeView view;
  
  /**
   * Constructor
   */
  public TreeViewPrinter(TreeView view) {
    this.view = view;
  }
  
  /**
   * size of print in inches
   */
  public Dimension2D getSize() {
    Rectangle mmbounds = view.getModel().getBounds();
    return new Dimension2D.Double(mmbounds.width*0.1F/2.54F, mmbounds.height*0.1F/2.54F);
  }

  /**
   * render on canvas
   */
  public void render(Graphics2D g) {
    
    // prepare rendering on mm/10 space
    DPI dpi = DPI.get(g);
    UnitGraphics graphics = new UnitGraphics(g, dpi.horizontal()/DPI.INCH*0.1D, dpi.vertical()/DPI.INCH*0.1D);
    
    ContentRenderer renderer = new ContentRenderer();
    renderer.font           = view.getContentFont();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.indiRenderer   = new BlueprintRenderer(view.getBlueprint(Gedcom.INDI));
    renderer.famRenderer    = new BlueprintRenderer(view.getBlueprint(Gedcom.FAM));
    
    renderer.render(graphics, view.getModel());

  }

} //TreePrintRenderer
