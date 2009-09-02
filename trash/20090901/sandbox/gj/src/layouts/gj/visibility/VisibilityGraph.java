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
package gj.visibility;

import gj.geom.FlattenedPathConsumer;
import gj.geom.Geometry;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.layout.Port;
import gj.layout.Routing;
import gj.model.Edge;
import gj.model.Vertex;
import gj.model.WeightedGraph;
import gj.util.LayoutHelper;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a visibility graph implementation
 * TODO visibility graph can be done faster
 *  http://www.geometrylab.de/VisGraph/index.html.en
 *  http://bengimizrahi.googlepages.com/visibilitygraphconstructionforasetofpolygons(in2d)
 */
public class VisibilityGraph implements Graph2D, WeightedGraph {
  
  // using integer points for the visibility graph to avoid rounding errors 
  private Map<Point, PointLocation> point2location;
  private List<VisibleConnection> connections;
    
  /**
   * Constructor
   * @param graph2d 2d graph to build visibility graph from
   */
  public VisibilityGraph(Graph2D graph2d) {
    
    // init
    this.point2location = new HashMap<Point, PointLocation>(graph2d.getVertices().size()*4);
    this.connections = new ArrayList<VisibleConnection>();
     
    // each vertex is a hole
    final List<Hole> holes = new ArrayList<Hole>();
    for (Vertex v : graph2d.getVertices())
      holes.add(new Hole(graph2d.getShape(v)));
    
    // loop over holes and check visibility to others
    for (int i=0; i<holes.size(); i++) {
      scan(holes.get(i), holes.subList(i+1, holes.size()), holes);
    }
    
    // done
  }
  
  private void scan(Hole source, List<Hole> destinations, List<Hole> holes) {
    
    for (Hole dest : destinations) {
      
      for (int i=0;i<source.points.size();i++) {
        Point sourcePoint = source.points.get(i);
        for (int j=0;j<dest.points.size();j++) {
          Point destPoint = dest.points.get(j);
          if (!obstructed(sourcePoint, destPoint, holes))
            vertex(sourcePoint).sees(vertex(destPoint), connections);
        }
      }
      
    }
    // done
  }

  /** check whether a line [from,to] is obstructed by given holes */
  private boolean obstructed(Point2D from, Point2D to, List<Hole> holes) {
    for (int i=0;i<holes.size();i++) {
      if (holes.get(i).obstructs(from, to))
        return true;
    }
    return false;
  }

  
  /**
   * lookup a vertex
   */
  private PointLocation vertex(Point pos) {
    PointLocation v = point2location.get(pos);
    if (v==null) {
      v = new PointLocation(pos);
      point2location.put(pos, v);
    }
    return v;
  }
  
  /** interface implementation */
  public double getWeight(Edge edge) {
    return ((VisibleConnection)edge).weight;
  }
  
  /** interface implementation */
  public Collection<? extends Vertex> getVertices() {
    return point2location.values();
  }

  /** interface implementation */
  public Collection<? extends Edge> getEdges() {
    return Collections.unmodifiableCollection(connections);
  }
  
  /** vertex for position */
  public Vertex getVertex(Point2D point) throws IllegalArgumentException {
    PointLocation result = point2location.get(round(point));
    if (result==null)
      throw new IllegalArgumentException("Point "+point+" is not a valid point location");
    return result;
  }

  /** helper for rounding */
  Point round(Point2D p) {
    return new Point((int)p.getX(), (int)p.getY());
  }

  /** debug shape */
  public Shape getDebugShape() {
    
    GeneralPath result = new GeneralPath();
    for (Edge edge : getEdges()) {
      result.append(getRouting(edge), false);
    }
    return result;
  }

  public Routing getRouting(Edge edge) {
    return LayoutHelper.getRouting(edge, this);
  }

  public Point2D getPosition(Vertex vertex) {
    PointLocation loc = (PointLocation)vertex;
    return new Point2D.Double(loc.x, loc.y);
  }

  public Shape getShape(Vertex vertex) {
    return new Rectangle( ((PointLocation)vertex).x, ((PointLocation)vertex).y, 0, 0);
  }

  public void setRouting(Edge edge, Routing shape) {
    throw new IllegalArgumentException("n/a");
  }

  public void setPosition(Vertex vertex, Point2D pos) {
    throw new IllegalArgumentException("n/a");
  }

  public void setShape(Vertex vertex, Shape shape) {
    throw new IllegalArgumentException("n/a");
  }
  
  public Port getPort(Edge edge, Vertex at) {
    return Port.None;
  }
  
  /**
   * a hole in the original graph
   */
  private class Hole {
    
    private List<Point> points = new ArrayList<Point>(4);
    
    Hole(Shape shape) {
      
      ShapeHelper.iterateShape(Geometry.getConvexHull(shape), new FlattenedPathConsumer() {
        public boolean consumeLine(Point2D start, Point2D end) {
          Point s = round(start);
          if (points.isEmpty() || !points.get(points.size()-1).equals(s))
            points.add(s);
          Point e = round(end);
          if (!s.equals(e))
            points.add(e);
          return true;
        }
      });
      
      for (int i=0;i<points.size();i++) {
        vertex(points.get(i)).sees(vertex(points.get( (i+1)%points.size() )), connections);
      }
    }
    
    boolean obstructs(Point2D lineStart, Point2D lineEnd) {
      
      int n = 0;
      for (int i=0;i<points.size();i++) {
        
        Point2D p = Geometry.getIntersection(lineStart, lineEnd, points.get(i), points.get((i+1)%points.size()));
        if (p!=null) {
          // truly intersecting a segment? (no start/end)
          if (!(p.equals(lineStart)||p.equals(lineEnd)))
            return true;
          // segment going through inside? (more than one corner)
          if (++n>2)
            return true;
        }
      }
      
      return false;
    }
  } //Hole
  
  /**
   * An edge in the visibility graph
   */
  private static class VisibleConnection implements Edge {
    
    PointLocation start,end;
    double weight;
    
    VisibleConnection(PointLocation start, PointLocation end) {
      this.start = start;
      this.end = end;
      
      weight = end.distance(start);
    }
    public Vertex getStart() {
      return start;
    }
    public Vertex getEnd() {
      return end;
    }
  } //E
  
  /**
   * A vertex in the the visibility graph
   */
  private static class PointLocation extends Point implements Vertex {
    
    List<VisibleConnection> es = new ArrayList<VisibleConnection>(4);
    
    PointLocation(Point pos) {
      super(pos.x, pos.y);
    }
    
    void sees(PointLocation that, List<VisibleConnection> connections) {
      
      for (int i=0;i<es.size();i++) {
        VisibleConnection e = es.get(i); 
        if (e.end.equals(that)||e.start.equals(that))
          return;
      }
      
      VisibleConnection e = new VisibleConnection(this, that);
      connections.add(e);
      this.es.add(e);
      that.es.add(e);
      
    }
    
    public Collection<? extends Edge> getEdges() {
      return Collections.unmodifiableCollection(es);
    }
  } //PointLocation
  
} //DefaultVisibilityGraph
