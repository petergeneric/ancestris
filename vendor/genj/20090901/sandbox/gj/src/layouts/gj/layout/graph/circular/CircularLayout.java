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
package gj.layout.graph.circular;

import gj.geom.Geometry;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Layout that arranges nodes in a circle with the
 * least amount of line intersections
 */
public class CircularLayout implements GraphLayout {
  
  /** constant */
  private final static double TWOPI = 2*Math.PI;
  
  /** padding */
  private double padNodes = 12.0D;
  
  /** whether we're generating a single circle or not */
  private boolean isSingleCircle = true;
  
  /**
   * Getter - is single circle
   */
  public boolean isSingleCircle() {
    return isSingleCircle;
  }
  
  /**
   * Setter - is single circle
   */
  public void setSingleCircle(boolean set) {
    isSingleCircle=set;
  }
  
  /**
   * Getter - padding
   */
  public double getPadding() {
    return padNodes;
  }
  
  /**
   * Setter - padding
   */
  public void setPadding(double set) {
    padNodes=set;
  }
  
  /**
   * @see gj.layout.GraphLayout#apply(Graph2D, LayoutContext)
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {
    
    // no purpose in empty|1-ary graph
    if (graph2d.getVertices().size() < 2) 
      return LayoutHelper.getBounds(graph2d);
    
    // create a CircularGraph
    CircularGraph cgraph = new CircularGraph(graph2d, isSingleCircle);
    
    // analyze the circle(s)
    Iterator<CircularGraph.Circle> it = cgraph.getCircles().iterator();
    double x=0,y=0;
    while (it.hasNext()) {
      
      // look at a circle
      CircularGraph.Circle circle = (CircularGraph.Circle)it.next();
      layout(graph2d, circle, x, y);
      
      // next
      x+=160;
    }
    
    // FIXME circular doesn't work
    
    // update the arcs
    LayoutHelper.setRoutings(graph2d);
    
    // done
    return LayoutHelper.getBounds(graph2d);
  } 
  
  /**
   * layout a circle
   */
  private void layout(Graph2D graph2d, CircularGraph.Circle circle, double cx, double cy) {
    
    // nodes
    List<Vertex> nodes = new ArrayList<Vertex>(circle.getNodes());
    
    // one node only?
    if (nodes.size()==1) {
      Vertex one = nodes.get(0);
      graph2d.setShape(one, ShapeHelper.createShape(graph2d.getShape(one), new Point2D.Double(cx,cy)));
      return;
    }
    
    // nodes' degrees and global circumference
    double[] sizes = new double[nodes.size()];
    double circumference = 0;
    
    // analyze nodes in circle
    for (int n=0;n<nodes.size();n++) {
        
      // .. its size - the length of vector (x,y)
      Rectangle2D bounds = graph2d.getShape(nodes.get(n)).getBounds2D();
      double size = Geometry.getLength(bounds.getWidth()+padNodes, bounds.getHeight()+padNodes);
        
      // .. keep what we need
      sizes[n] = size;
        
      // .. increase circ
      circumference += size;
    }
      
    // calculate radius (c=2PIr => r=c/2/PI)
    double radius = circumference/TWOPI;
      
    // put 'em in a circle
    double radian = 0;
    for (int n=0;n<nodes.size();n++) {
      double x = (int)(cx + Math.sin(radian)*radius);
      double y = (int)(cy + Math.cos(radian)*radius);
      Vertex node = nodes.get(n);
      Point2D pos = new Point2D.Double(x,y);
      graph2d.setShape(node, ShapeHelper.createShape(graph2d.getShape(node), pos));

      radian += TWOPI*sizes[n]/circumference;
    }
    
    radian=0;
    
  }

} //CircularLayout
