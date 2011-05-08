/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.ui;

import static gj.geom.PathIteratorKnowHow.SEG_LINETO;
import static gj.geom.PathIteratorKnowHow.SEG_MOVETO;
import static gj.geom.PathIteratorKnowHow.SEG_CLOSE;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.layout.Routing;
import gj.model.Edge;
import gj.model.Vertex;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

/**
 * A default implementation for rendering a graph
 */
public class DefaultGraphRenderer implements GraphRenderer {

  /** an arrow-head pointing upwards */
  private final static Shape ARROW_HEAD = ShapeHelper.createShape(0,0,1,1,new double[]{
      SEG_MOVETO, 0, 0, 
      SEG_LINETO, -3, -7, 
      SEG_LINETO,  3, -7, 
      SEG_CLOSE
  });

  /**
   * The rendering functionality
   */
  public void render(Graph2D graph2d, Graphics2D graphics) {
  
    // the arcs
    renderEdges(graph2d, graphics);    
    
    // the nodes
    renderVertices(graph2d, graphics);
  
    // done
  }

  /**
   * Renders all Nodes
   */
  protected void renderVertices(Graph2D graph2d, Graphics2D graphics) {
    
    // Loop through the graph's nodes
    for (Vertex vertex : graph2d.getVertices()) {
      renderVertex(graph2d, vertex, graphics);
    }
    
    // Done
  }

  protected void renderVertex(Graph2D graph2d, Vertex vertex, Graphics2D graphics) {
    
    // figure out its color
    Color color = getColor(vertex);
    Stroke stroke = getStroke(vertex);
  
    // draw its shape
    graphics.setColor(color);
    graphics.setStroke(stroke);
    Shape shape = graph2d.getShape(vertex);
    draw(shape, false, graphics);

    // and content    
    String text = getText(vertex);
    Icon icon = getIcon(vertex);

    Shape oldcp = graphics.getClip();
    graphics.clip(shape);
    draw(text, icon, shape.getBounds2D(), 0.5, 0.5, graphics);
    graphics.setClip(oldcp);

    // done
  }
  
  protected String getText(Vertex vertex) {
    return vertex==null ? "" : vertex.toString();
  }
  
  protected Icon getIcon(Vertex vertex) {
    return null; // javax.swing.UIManager.getIcon( "OptionPane.errorIcon" );
  }
  
  /**
   * Color resolve
   */
  protected Color getColor(Vertex vertex) {
    return Color.BLACK;    
  }

  /**
   * Color resolve
   */
  protected Color getColor(Edge edge) {
    return Color.BLACK;    
  }

  /**
   * Stroke resolve
   */
  protected Stroke getStroke(Vertex vertex) {
    return new BasicStroke();    
  }

  /**
   * Renders all Arcs
   */
  protected void renderEdges(Graph2D graph2d, Graphics2D graphics) {
    
    for (Edge edge : graph2d.getEdges())
      renderEdge(graph2d, edge, graphics);
  
    // Done
  }

  /**
   * Renders an Arc
   */
  protected void renderEdge(Graph2D graph2d, Edge edge, Graphics2D graphics) {
    
    AffineTransform old = graphics.getTransform();
    
    // arbitrary color
    graphics.setColor(getColor(edge));
    
    // draw path from start
    Routing path = graph2d.getRouting(edge);
    graphics.draw(graph2d.getRouting(edge));
    
    // draw arrow
    Point2D pos = path.getLastPoint();
    graphics.setBackground(getColor(edge));
    graphics.translate(pos.getX(), pos.getY());
    graphics.rotate(path.getLastAngle());
    graphics.fill(ARROW_HEAD);
    graphics.draw(ARROW_HEAD);
    
    // done      
    graphics.setTransform(old);
  }

  /**
   * Helper that renders a shape at given position with given rotation
   */
  protected void draw(Shape shape, boolean fill, Graphics2D graphics) {
    if (fill) graphics.fill(shape);
    else graphics.draw(shape);
  }

  /**
   * Helper that renders a string at given position
   */
  protected void draw(String text, Icon icon, Rectangle2D at, double horizontalAlign, double verticalAlign,Graphics2D graphics) {

    // calculate width/height
    FontMetrics fm = graphics.getFontMetrics();
    double height = 0;
    double width = 0;
    for (int cursor=0;cursor<text.length();) {
      int newline = text.indexOf('\n', cursor);
      if (newline<0) newline = text.length();
      String line = text.substring(cursor, newline);
      Rectangle2D r = fm.getStringBounds(line, graphics);
      LineMetrics lm = fm.getLineMetrics(line, graphics);
      width = Math.max(width, r.getWidth());
      height += r.getHeight();
      cursor = newline+1;
    }
    
    // draw icon
    if (icon!=null) { 
      int iwidth = icon.getIconWidth();
      int cwidth = fm.charWidth(' '); 
      icon.paintIcon(null, graphics, (int)(at.getX() + (at.getWidth()-width-iwidth-cwidth)*horizontalAlign), (int)(at.getY() + (at.getHeight()-icon.getIconHeight())*verticalAlign));
      at.setRect(at.getX()+iwidth+cwidth, at.getY(), at.getWidth()-iwidth-cwidth, at.getHeight());
    }
    
    // draw lines
    double x = at.getX() + (at.getWidth()-width)*horizontalAlign;
    double y = at.getY() + (at.getHeight()-height)*verticalAlign;
    for (int cursor=0;cursor<text.length();) {
      int newline = text.indexOf('\n', cursor);
      if (newline<0) newline = text.length();
      String line = text.substring(cursor, newline);
      Rectangle2D r = fm.getStringBounds(line, graphics);
      LineMetrics lm = fm.getLineMetrics(line, graphics);
      graphics.drawString(line, (float)x, (float)y + lm.getHeight() - lm.getDescent());
      cursor = newline+1;
      y += r.getHeight();
    }

    // done
  }
  
}
