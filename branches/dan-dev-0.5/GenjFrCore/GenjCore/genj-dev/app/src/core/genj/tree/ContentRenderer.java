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
import genj.renderer.EmptyHintKey;
import genj.renderer.BlueprintRenderer;
import genj.renderer.RenderPreviewHintKey;
import genj.util.swing.UnitGraphics;
import gj.model.Node;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The renderer knowing how to render the content of tree's model
 */
public class ContentRenderer {
  
  /*package*/ Font font = null;

  /** shape color for indis */
  /*package*/ Color cIndiShape = null;
  
  /** shape color for fams */
  /*package*/ Color cFamShape = null;
  
  /** shape color for arcs */
  /*package*/ Color cArcs = null;

  /** selected color */
  /*package*/ Color cSelectedShape = null;

  /** an entity that we consider selected */
  /*package*/ Collection<? extends Entity> selected = new ArrayList<Entity>(0);
  
  /** the entity renderer we're using */
  /*package*/ BlueprintRenderer indiRenderer, famRenderer;
  
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
    int count = 0;
    for (Node node : model.getNodesIn(clip)) {
      // grab node and its shape
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
      count++;
      renderNode(g, pos, shape, node.getContent());
      // next
    }
    if (count>0)
      g.getGraphics().setRenderingHint(EmptyHintKey.KEY, false);
    
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
    // draw its content if not meant for speed
    if (!Boolean.TRUE.equals(g.getGraphics().getRenderingHint(RenderPreviewHintKey.KEY)))
      renderContent(g, x, y, shape, content);
    // done
  }
  
  /**
   * Calc color for given node   */
  private Color getColor(Object content) {
    // selected?
    if (cSelectedShape!=null&&selected.contains(content)) {
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
    BlueprintRenderer renderer = null;
    if (content instanceof Indi) renderer = indiRenderer;
    if (content instanceof Fam ) renderer = famRenderer;
    if (renderer==null) 
      return;
    // preserve clip&transformation
    Rectangle r2d = shape.getBounds();
    g.pushClip(x, y, r2d);
    g.pushTransformation();
    // draw it
    g.translate(x, y);
    Rectangle r = g.getRectangle(r2d);
    r.x+=2;r.y+=2;r.width-=4;r.height-=4;
    g.setColor(Color.black);
    g.setFont(font);
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
    Collection<TreeArc> arcs = model.getArcsIn(clip);
    for (TreeArc arc : arcs) 
      g.draw(arc.getPath(), 0, 0);
    if (!arcs.isEmpty())
      g.getGraphics().setRenderingHint(EmptyHintKey.KEY, false);

    // done
  }
  
} //ContentRenderer
