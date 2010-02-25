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

import gj.layout.Graph2D;
import gj.layout.Port;
import gj.layout.Routing;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple default implementation of a layout
 */
public class DefaultGraph implements Graph2D {

  private Graph graph;
  private Shape defaultShape;
  private Map<Vertex, Shape> vertex2shape = new HashMap<Vertex, Shape>();
  private Map<Edge, Routing> edge2path = new HashMap<Edge, Routing>();
  
  /*package*/ DefaultGraph() {
    this(null);
  }
  
  public DefaultGraph(Graph graph) {
    this(graph, new Rectangle());
  }
  
  public DefaultGraph(Graph graph, Shape defaultShape) {
    this.graph = graph;
    this.defaultShape = defaultShape;
  }
  
  public Collection<? extends Edge> getEdges() {
    return graph==null ? new ArrayList<Edge>() : graph.getEdges();
  }
  
  public Collection<? extends Vertex> getVertices() {
    return graph==null ? new ArrayList<Vertex>() : graph.getVertices();
  }
  
  protected Shape getDefaultShape(Vertex vertex) {
    return defaultShape;
  }
  
  public Routing getRouting(Edge edge) {
    
    Routing result = edge2path.get(edge);
    if (result==null) {
      result = LayoutHelper.getRouting(edge, this);
      edge2path.put(edge, result);
    }
    return result;
  }

  public void setRouting(Edge edge, Routing path) {
    edge2path.put(edge, path);
  }

  public Shape getShape(Vertex vertex) {
    Shape result = vertex2shape.get(vertex);
    if (result==null)
      result = getDefaultShape(vertex);
    return result;
  }
  
  public void setShape(Vertex vertex, Shape shape) {
    vertex2shape.put(vertex, shape); 
  }

  /**
   * Edge's port control
   */
  public Port getPort(Edge edge, Vertex at) {
    return Port.None;
  }
  
} //DefaultGraph
