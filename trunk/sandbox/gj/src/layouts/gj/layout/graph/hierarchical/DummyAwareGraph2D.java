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
package gj.layout.graph.hierarchical;

import gj.layout.Graph2D;
import gj.layout.Port;
import gj.layout.Routing;
import gj.model.Edge;
import gj.model.Vertex;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A layout that knows how to handle layer dummy vertices
 */
public class DummyAwareGraph2D implements Graph2D {
  
  private Map<Vertex, Shape> dummy2shape = new HashMap<Vertex, Shape>();
  private Graph2D wrapped;
  
  public DummyAwareGraph2D(Graph2D wrapped) {
    this.wrapped = wrapped;
  }
  
  public Collection<? extends Vertex> getVertices() {
    return wrapped.getVertices();
  }
  
  public Collection<? extends Edge> getEdges() {
    return wrapped.getEdges();
  }
  
  public Shape getShape(Vertex vertex) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      return wrapped.getShape(vertex);
    Shape result = dummy2shape.get(vertex);
    return result!=null ? result : new Rectangle2D.Double();
  }
  
  public Routing getRouting(Edge edge) {
    return wrapped.getRouting(edge);
  }

  public void setRouting(Edge edge, Routing shape) {
    wrapped.setRouting(edge, shape);
  }

  public void setShape(Vertex vertex, Shape shape) {
    if (!(vertex instanceof LayerAssignment.DummyVertex))
      wrapped.setShape(vertex, shape);
    else
      dummy2shape.put(vertex, shape);
  }
  
  public Port getPort(Edge edge, Vertex at) {
    if (  (edge.getStart() instanceof LayerAssignment.DummyVertex)
        ||(edge.getEnd  () instanceof LayerAssignment.DummyVertex))
      return Port.None;
    return wrapped.getPort(edge, at);
  }

} //DummyAwareGraph2D
