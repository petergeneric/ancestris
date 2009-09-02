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
package gj.util;

import static gj.geom.Geometry.getClosest;
import static gj.geom.Geometry.getIntersections;
import static gj.geom.Geometry.getMaximumDistance;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.layout.GraphNotSupportedException;
import gj.layout.Port;
import gj.layout.Routing;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper for analyzing model.*
 */
public class LayoutHelper {

  /**
   * Resolve the port for given shape
   * @param shape the shape to calculate a port for
   * @param index zero based index of count ports on side
   * @param count number ports on side
   * @param side the side of the port
   */
  public static Point2D getPort(Shape shape, int index, int count, Port side) {
    
    if (index<0||index>=count)
      throw new IllegalArgumentException("!(0<="+index+"<"+count+")");
    count++;
    index ++;
    
    Rectangle2D bounds = shape.getBounds2D();
    switch (side) {
    case None:
      return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()); 
    case North:
      return new Point2D.Double(bounds.getMinX() + (bounds.getWidth()/count)*index, bounds.getMinY()); 
    case South:
      return new Point2D.Double(bounds.getMinX() + (bounds.getWidth()/count)*index, bounds.getMaxY()); 
    case West:
      return new Point2D.Double(bounds.getMinX(), bounds.getMinY() + (bounds.getHeight()/count)*index); 
    case East:
      return new Point2D.Double(bounds.getMaxX(), bounds.getMinY() + (bounds.getHeight()/count)*index); 
    default:
      throw new IllegalArgumentException("n/a");
    }
  }
  
  /**
   * Translates a node's position
   */
  public static void translate(Graph2D graph2d, Vertex vertex, Point2D delta) {
    graph2d.setShape(vertex, ShapeHelper.createShape(graph2d.getShape(vertex), AffineTransform.getTranslateInstance(delta.getX(), delta.getY())));
  }

  /**
   * Checks whether given Node n is neighbour of any of the given nodes. 
   * That is  E node(i), E arc(i,j) where node = node(j)
   */
  public static boolean isNeighbour(Graph graph, Vertex vertex, Collection<? extends Vertex> vertices) {
    for (Edge edge : vertex.getEdges()) {
      if (vertices.contains(edge.getStart()) || vertices.contains(edge.getEnd()) )
        return true;
    }
    return false;
  }

  /**
   * Calculates the dimension of set of nodes
   */
  public static Rectangle2D getBounds(Graph2D graph2d) {
    // no content?
    if (graph2d==null||graph2d.getVertices().size()==0) 
      return new Rectangle2D.Double(0,0,0,0);
    // loop through nodes and calculate
    double x1=Double.MAX_VALUE,y1=Double.MAX_VALUE,x2=-Double.MAX_VALUE,y2=-Double.MAX_VALUE;
    for (Vertex vertex : graph2d.getVertices()) {
      Rectangle2D box = graph2d.getShape(vertex).getBounds2D();
      x1 = Math.min(x1,box.getMinX());
      y1 = Math.min(y1,box.getMinY());
      x2 = Math.max(x2,box.getMaxX());
      y2 = Math.max(y2,box.getMaxY());
    }
    return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
  }
 
  /**
   * Check graph for spanning tree
   */
  public static void assertSpanningTree(Graph graph) throws GraphNotSupportedException {
    
    if (graph.getVertices().size()==0)
      return;
    
    // look for cycles
    Set<Vertex> visited = new HashSet<Vertex>();
    if (containsCycle(graph, null, graph.getVertices().iterator().next(), visited))
      throw new GraphNotSupportedException("graph is not acyclic");
    
    // check spanning
    if (visited.size() != graph.getVertices().size())
      throw new GraphNotSupportedException("graph is not a spanning tree");
    
  }

  /**
   * Find cycles
   */
  private static boolean containsCycle(Graph graph, Vertex backtrack, Vertex root, Set<Vertex> visited) {
    
    // to shouldn't have been visited before
    if (visited.contains(root)) 
      return true;
  
    // remember it
    visited.add(root);
    
    // Recurse into neighbours
    for (Vertex neighbour : getNeighbours(root)) {
      if (neighbour.equals(backtrack)) 
        continue;
      if (containsCycle(graph, root, neighbour, visited))
        return true;
    }
    
    // done
    return false;
  }
  
  /**
   * Get other vertex in an edge
   */
  public static Vertex getOther(Edge edge, Vertex vertex) {
    if (edge.getStart().equals(vertex))
      return edge.getEnd();
    if (edge.getEnd().equals(vertex))
      return edge.getStart();
    throw new IllegalArgumentException("vertex "+vertex+" not in "+edge);
  }
  
  /**
   * get normalized edges. That's all edges from a given vertex without loops and dupes
   */
  public static List<Edge> getNormalizedEdges(Vertex vertex) {
    
    Set<Vertex> children = new HashSet<Vertex>();
    List<Edge> edges = new ArrayList<Edge>(vertex.getEdges().size());
    for (Edge edge : vertex.getEdges()) {
      Vertex child = LayoutHelper.getOther(edge, vertex);
      if (children.contains(child)||child.equals(vertex))
        continue;
      children.add(child);
      edges.add(edge);
    }

    return edges;
  }
  
  /**
   * Check vertex being sink
   */
  public static boolean isSink(Vertex vertex) {
    for (Edge edge : vertex.getEdges()) {
      if (edge.getStart().equals(vertex))
        return false;
    }
    return true;
  }
  
  /**
   * Get neighbouring vertices. That's all 
   * <pre>
   *   A e E n : e(vertex,n ) || e(n,vertex) e graph && !n==vertex
   * </pre> 
   */
  public static Set<Vertex> getNeighbours(Vertex vertex) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Edge edge : vertex.getEdges()) {
      Vertex start = edge.getStart();
      Vertex end = edge.getEnd();
      if (start.equals(vertex)&&!end.equals(vertex)) 
        result.add(end);
      else if (!start.equals(vertex))
        result.add(start);
    }
    return result;
  }
  
  /**
   * Returns the edge between vertex1 and vertex2
   */
  public static Edge getEdge(Vertex vertex1, Vertex vertex2) {
    for (Edge edge : vertex1.getEdges()) {
      if (contains(edge, vertex1) && contains(edge, vertex2))
        return edge;
    }
    throw new IllegalArgumentException("no edge between ["+vertex1+"] and ["+vertex2+"]");
  }
  
  /**
   * Get children of a given vertex. That's all 
   * <pre>
   *   A e E n : e(vertex,n ) e graph && !n==vertex
   * </pre> 
   */
  public static Set<Vertex> getChildren(Vertex vertex) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Edge edge : vertex.getEdges()) {
      Vertex start = edge.getStart();
      Vertex end = edge.getEnd();
      if (start.equals(vertex)&&!end.equals(vertex)) 
        result.add(end);
    }
    return result;
  }
  
  public static boolean contains(Edge edge, Vertex vertex) {
    return edge.getStart().equals(vertex) || edge.getEnd().equals(vertex);
  }
  
  public static double getDiameter(Vertex vertex, Graph2D layout) {
    Shape shape = layout.getShape(vertex);
    return getMaximumDistance(ShapeHelper.getCenter(shape), shape) * 2;
  }

  /**
   * Calculate shape of all arcs in graph
   */
  public static void setRoutings(Graph2D graph2d) {
    
    for (Edge edge : graph2d.getEdges()) { 
      setRouting(edge, graph2d);
    }
    
  }

  /**
   * Calculate a shape for an arc
   */
  public static void setRouting(Edge edge, Graph2D graph2d) {
    graph2d.setRouting(edge, getRouting(edge, graph2d));
  }
  
  /**
   * path for given points
   */
  public static Routing getRouting(Point2D from, Point2D to) {
    return getRouting(
        new Rectangle2D.Double(from.getX(),from.getY(),0,0), new Point2D.Double(),
        new Rectangle2D.Double(to.getX(),to.getY(),0,0), new Point2D.Double()
    );
  }

  /**
   * path for given edge
   */
  public static Routing getRouting(Edge edge, Graph2D graph2d) {
    return getRouting(graph2d.getShape(edge.getStart()), graph2d.getShape(edge.getEnd()));
  }

  /**
   * Calculates a path between two ports from source to destination shape
   * @param fromShape the source shape 
   * @param toShape the destination shape 
   */
  public static Routing getRouting(Shape fromShape, Shape toShape) {
    return getRouting(fromShape, ShapeHelper.getCenter(fromShape), toShape, ShapeHelper.getCenter(toShape), false);
  }
  
  /**
   * Calculates a path between two ports from source to destination shape
   * @param fromShape the source shape 
   * @param fromPort the port for fromShape
   * @param toShape the destination shape 
   * @param toPort the port for toShape
   */
  public static Routing getRouting(Shape fromShape, Point2D fromPort, Shape toShape, Point2D toPort) {
    return getRouting(fromShape, fromPort, toShape, toPort, false);
  }
  
  public static Routing getRouting(Shape fromShape, Point2D fromPort, Shape toShape, Point2D toPort, boolean reversed) {
    ArrayList<Point2D> points = new ArrayList<Point2D>(4);
    points.add(fromPort);
    points.add(toPort);
    return getRouting(points, fromShape, toShape, reversed);
  }
  
  /**
   * path with a line going through points between two shapes
   * @param points a sequence of points describing the path (from source port via transitions to destination port)
   * @param fromShape originating shape
   * @param toShape destination shape
   */  
  public static Routing getRouting(Point2D[] points, Shape fromShape, Shape toShape, boolean reversed) {
    return getRouting(new ArrayList<Point2D>(Arrays.asList(points)),fromShape,toShape,reversed);
  }
  
  /**
   * path with a line going through points between two shapes
   * @param points a sequence of points describing the path (from source port via transitions to destination port)
   * @param fromShape originating shape
   * @param toShape destination shape
   */  
  public static Routing getRouting(List<Point2D> points, Shape fromShape, Shape toShape, boolean reversed) {
    
    if (points.size()<2)
      throw new IllegalArgumentException("list of points cannot be smaller than two");
    
    // A simple line through points
    DefaultRouting result = new DefaultRouting();
    
    // intersect the first segment with fromShape
    Collection<Point2D> is = getIntersections(points.get(0), points.get(1), true, fromShape);
    if (!is.isEmpty())
      points.set(0, getClosest(points.get(1), is));
    else {
      Point2D fromCenter = ShapeHelper.getCenter(fromShape);
      if (!points.get(0).equals(fromCenter)) {
        is = getIntersections(fromCenter, points.get(0), true, fromShape);
        if (is.isEmpty())
          points.add(0, fromCenter);
        else
          points.add(0, getClosest(points.get(0), is));
      }
    }
    
    // intersect the last segment with toShape
    int n = points.size();
    is = getIntersections(points.get(n-2), points.get(n-1), true, toShape);
    if (!is.isEmpty())
      points.set(n-1, getClosest(points.get(n-2), is));
    else {
      Point2D toCenter = ShapeHelper.getCenter(toShape);
      if (!points.get(n-1).equals(toCenter)) {
        is = getIntersections(points.get(n-1), toCenter, true, toShape);
        if (is.isEmpty())
          points.add(toCenter);
        else
          points.add(getClosest(points.get(n-1), is));
      }
    }
    
    // add the points to this path relative to start
    if (!reversed) {
      for (int i=0;i<points.size();i++) {
        Point2D p = points.get(i);
        if (i==0)
          result.start(new Point2D.Double( p.getX() , p.getY() ));
        else
          result.lineTo(new Point2D.Double( p.getX() , p.getY() ));
      }
      
    } else {
      for (int i=n-1;i>=0;i--) {
        Point2D p = points.get(i);
        if (i==n-1)
          result.start(new Point2D.Double( p.getX() , p.getY() ));
        else
          result.lineTo(new Point2D.Double( p.getX() , p.getY() ));
      }
    }
    
    // done
    return result;
  }

  /**
   * Calculate in-degree of a vertex
   */
  public static int getInDegree(Vertex v) {
    int result = 0;
    for (Edge e : v.getEdges()) {
      if (e.getEnd().equals(v))
        result ++;
    }
    return result;
  }

  /**
   * Calculate out-degree of a vertex
   */
  public static int getOutDegree(Vertex v) {
    int result = 0;
    for (Edge e : v.getEdges()) {
      if (e.getStart().equals(v))
        result ++;
    }
    return result;
  }
} //ModelHelper
