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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.renderer.EntityRenderer;
import genj.util.swing.UnitGraphics;
import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Node;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Iterator;

/**
 * The renderer knowing how to render the content of tree's model
 */
public class ContentRenderer {

  /** shape color for indis */
  /*package*/ Color cIndiShape = null;
  
  /** shape color for fams */
  /*package*/ Color cFamShape = null;
  
  /** shape color for arcs */
  /*package*/ Color cArcs = null;

  /** selected color */
  /*package*/ Color cSelectedShape = null;

  /** an entity that we consider selected */
  /*package*/ Entity selection = null;
  
  /** the entity renderer we're using */
  /*package*/ EntityRenderer indiRenderer, famRenderer;
  
  /**
   * Render the content
   */
  public void render(UnitGraphics g, Model model) {  
    // translate to center
    Rectangle bounds = model.getBounds();
    g.translate(-bounds.getX(), -bounds.getY());
    // render the arcs
    renderArcs(g, model);
    // render the nodes
    renderNodes(g, model);
    // done
  }  
  
  /**
   * Render the nodes
   */
  private void renderNodes(UnitGraphics g, Model model) {
    // clip is the range we'll be looking in range
    Rectangle clip = g.getClip().getBounds();
    // loop
    Iterator it = model.getNodesIn(clip).iterator();
    while (it.hasNext()) {
      // grab node and its shape
      Node node = (Node)it.next();
      Shape shape = node.getShape();
      Point2D pos = node.getPosition();
      // no shape -> no rendering
      if (shape==null) continue;
      // bounds not intersecting clip -> no rendering
      Rectangle r = shape.getBounds();
      if (!clip.intersects(
        pos.getX()+r.getMinX(), 
        pos.getY()+r.getMinY(),
        r.getWidth(),
        r.getHeight() 
      )) continue;
      // render it
      renderNode(g, pos, shape, node.getContent());
      // next
    }
    // done
  }
  
  /**
   * Render a node
   */
  private void renderNode(UnitGraphics g, Point2D pos, Shape shape, Object content) {
    double 
      x = pos.getX(),
      y = pos.getY();
    // draw its shape
    g.setColor(getColor(content));
    g.draw(shape, x, y);
    // draw its content
    renderContent(g, x, y, shape, content);
    // done
  }
  
  /**
   * Calc color for given node   */
  private Color getColor(Object content) {
    // selected?
    if (cSelectedShape!=null&&content!=null&&content==selection) {
      return cSelectedShape;
    }
    // fam?
    if (content instanceof Fam)
      return cFamShape;
    // indi?
    return cIndiShape;
  }
  
  /**
   * Render the content of a node
   */
  private void renderContent(UnitGraphics g, double x, double y, Shape shape, Object content) {
    
    // safety check
    EntityRenderer renderer = null;
    if (content instanceof Indi) renderer = indiRenderer;
    if (content instanceof Fam ) renderer = famRenderer;
    if (renderer==null) return;
    // preserve clip&transformation
    Rectangle r2d = shape.getBounds();
    g.pushClip(x, y, r2d);
    g.pushTransformation();
    // draw it
    g.translate(x, y);
    Rectangle r = g.getRectangle(r2d);
    r.x+=2;r.y+=2;r.width-=4;r.height-=4;
    g.setColor(Color.black);
    renderer.render(g.getGraphics(), (Entity)content, r);
    // restore clip&transformation
    g.popTransformation();    
    g.popClip();
    // done
  }
  
  /**
   * Render the arcs
   */
  private void renderArcs(UnitGraphics g, Model model) {
    // clip is the range we'll be looking in range
    Rectangle clip = g.getClip().getBounds();
    // prepare color
    g.setColor(cArcs);
    // loop
    Iterator it = model.getArcsIn(clip).iterator();
    while (it.hasNext()) {
      // grab arc
      Arc arc = (Arc)it.next();
      // its path
      Path path = arc.getPath();
      if (path!=null) g.draw(path, 0, 0);
      // next
    }
    // done
  }
  
} //ContentRenderer
