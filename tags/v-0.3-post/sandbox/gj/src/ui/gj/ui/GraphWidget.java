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

import gj.layout.Graph2D;
import gj.model.Vertex;
import gj.util.EmptyGraph;
import gj.util.LayoutHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

/**
 * A lightweight graph widget for rendering a graph
 */
public class GraphWidget extends JComponent {

  /** the graph we're displaying */
  private Graph2D graph2d;
  
  /** the size of the graph */
  private Rectangle graphBounds;
  
  /** whether antialiasing is on */
  private boolean isAntialiasing = true;
  
  /** a renderer */
  private GraphRenderer renderer;
  
  /**
   * Constructor
   */
  public GraphWidget() {
    this(new EmptyGraph(), new DefaultGraphRenderer());
  }
  
  /**
   * Constructor
   */
  public GraphWidget(Graph2D graphLayout) {
    this(graphLayout, new DefaultGraphRenderer());
  }
  
  /**
   * Constructor
   */
  public GraphWidget(Graph2D graph2d, GraphRenderer renderer) {
    this.renderer = renderer;
    this.graph2d = graph2d;
    graphBounds = LayoutHelper.getBounds(graph2d).getBounds();
  }
  
  /**
   * Accessor - Renderer
   */
  public void setRenderer(GraphRenderer renderer) {
    
    this.renderer = renderer;
    repaint();
    
  }
  
  /**
   * Accessor - Graph
   */
  public void setGraph2D(Graph2D graph2d) {
    setGraph2D(graph2d, null);
  }
    
  public void setGraph2D(Graph2D graph2d, Rectangle bounds) {
    // cleanup data
    this.graph2d = graph2d;
    graphBounds = bounds != null ? bounds : LayoutHelper.getBounds(graph2d).getBounds();
    
    // make sure that's reflected
    revalidate();
    repaint();
    
    // done
  }
  
  /**
   * Accessor - Graph
   */
  public Graph2D getGraph2D() {
    return graph2d;
  }
  
  /**
   * Accessor - Antialiasing enabled or not
   */
  public boolean isAntialiasing() {
    return isAntialiasing;
  }

  /**
   * Accessor - Antialiasing enabled or not
   */
  public void setAntialiasing(boolean set) {
    isAntialiasing=set;
    repaint();
  }
  
  /**
   * Calculate x offset for centered graph
   */
  private int getXOffset() {
    if (graph2d==null) return 0;
    return -graphBounds.x+(getWidth()-graphBounds.width)/2;
  }
  
  /**
   * Calculate y offset for centered graph
   */
  private int getYOffset() {
    if (graph2d==null) return 0;
    return -graphBounds.y+(getHeight()-graphBounds.height)/2;
  }
  
  /**
   * @see java.awt.Component#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    if (graph2d==null) return new Dimension();
    return graphBounds.getSize();
  }

  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {
    
    // clear background
    g.setColor(Color.white);
    g.fillRect(0,0,getWidth(),getHeight());

    // graph there?
    if (graph2d==null) 
      return;
    
    // cast to 2d
    Graphics2D graphics = (Graphics2D)g;
    
    // switch on antialiasing?
    graphics.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
    );
    
    // synchronize on graph and go?
    synchronized (graph2d) {
      // create our working graphics
      // paint at 0,0
      graphics.translate(getXOffset(),getYOffset());
      // let the renderer do its work
      renderer.render(graph2d, graphics);
    }

    // done
  }
  
  /**
   * Convert screen position to model
   */
  protected Point getPoint(Point p) {
    return new Point(
      p.x - getXOffset(),
      p.y - getYOffset()
    );
  }
  
  protected Point model2screen(Point2D point) {
    return new Point(
        ((int)point.getX()) + getXOffset(),
        ((int)point.getY()) + getYOffset()
    );
  }
  
  /**
   * return vertex by point
   */
  public Vertex getVertexAt(Point point) {
    if (graph2d==null)
      return null;
    // adjust point for graph (graph space doesn't start at 0,0)
    point = getPoint(point);
    for (Vertex v : graph2d.getVertices()) {
      if (graph2d.getShape(v).contains(point.x, point.y))
        return v;
    }
    return null;
  }
  
}
