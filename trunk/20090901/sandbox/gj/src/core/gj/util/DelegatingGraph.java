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
import gj.model.Vertex;

import java.awt.Shape;
import java.util.Collection;

/**
 * A delegating graph2d
 */
public class DelegatingGraph implements Graph2D {
  
  private Graph2D delegated;
  
  /** constructor */
  public DelegatingGraph(Graph2D delegated) {
    this.delegated = delegated;
  }

  /** delegating call */
  public Routing getRouting(Edge edge) {
    return delegated.getRouting(edge);
  }

  /** delegating call */
  public Shape getShape(Vertex vertex) {
    return delegated.getShape(vertex);
  }

  /** delegating call */
  public void setRouting(Edge edge, Routing shape) {
    delegated.setRouting(edge, shape);
  }

  /** delegating call */
  public void setShape(Vertex vertex, Shape shape) {
    delegated.setShape(vertex, shape);
  }

  /** delegating call */
  public Collection<? extends Edge> getEdges() {
    return delegated.getEdges();
  }

  /** delegating call */
  public Collection<? extends Vertex> getVertices() {
    return delegated.getVertices();
  }
  
  /** delegating call */
  public Port getPort(Edge edge, Vertex at) {
    return delegated.getPort(edge, at);
  }
  
} //DelegatingGraph2D
