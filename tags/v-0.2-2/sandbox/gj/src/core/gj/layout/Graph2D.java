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
package gj.layout;


import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.awt.Shape;

/**
 * A graph with 2 dimensional layout information
 */
public interface Graph2D extends Graph {

  /**
   * Edge's shape
   */
  public Routing getRouting(Edge edge);

  /**
   * Edge's shape
   */
  public void setRouting(Edge edge, Routing routing);

  /**
   * Vertex's shape
   */
  public Shape getShape(Vertex vertex);

  /**
   * Vertex's shape
   */
  public void setShape(Vertex vertex, Shape shape);

  /**
   * Edge's port control
   */
  public Port getPort(Edge edge, Vertex at);
  
} //Layout2D
