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

import gj.model.Graph;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A graph that is broken down in circles
 */
/*package*/ class CircularGraph {

  /** the circles in a graph */
  private Set<Circle> circles;
  
  /** the mapping between vertex and its circle */
  private Map<Vertex, Circle> vertex2circle;
  
  /**
   * Constructor
   */
  /*package*/ CircularGraph(Graph graph, boolean isSingleCircle) {
    
    // anything to do?
    if (graph.getVertices().isEmpty()) 
      return;
    
    // prepare our nodes and their initial circles
    circles = new HashSet<Circle>();
    vertex2circle = new HashMap<Vertex, Circle>(graph.getVertices().size());
    
    // simple for isSingleCircle=true
    if (isSingleCircle) {
      new Circle(graph);
      return;
    }
    
    // find circles for all
    Set<? extends Vertex> unvisited = new HashSet<Vertex>(graph.getVertices());
    while (!unvisited.isEmpty()) 
      findCircles(graph, unvisited.iterator().next(), null, new Stack<Vertex>(), unvisited);

    // done    
  }
  
  /**
   * Find circles starting at given vertex
   */
  private void findCircles(Graph graph, Vertex vertex, Vertex parent, Stack<Vertex> path, Set<? extends Vertex> unvisited) {
    
    // have we been here before?
    if (path.contains(vertex)) {
      Circle circle = getCircle(vertex);
      circle.fold(path, vertex);
      return;
    }
    
    // now its visited
    unvisited.remove(vertex);
    
    // create a circle for it
    new Circle(Collections.singleton(vertex));

    // add current vertex to stack
    path.push(vertex);
    
    // recurse into neighbours traversing via arcs
    for (Vertex neighbour : LayoutHelper.getNeighbours(vertex)) {
      // don't go back
      if (neighbour==vertex||neighbour==parent)
        continue;
      // recurse into child
      findCircles(graph, neighbour, vertex, path, unvisited);
    }
    
    // take current vertex of stack again
    path.pop();
    
    // done
  }
  
  /**
   * Accessor - the circles
   */
  /*package*/ Collection<Circle> getCircles() {
    return circles;
  }
  
  /**
   * Accessor - a circle
   */
  /*package*/ Circle getCircle(Vertex vertex) {
    Circle result = vertex2circle.get(vertex);
    if (result==null)
      result = new Circle(Collections.singleton(vertex));
    return result;
  }
  
  /**
   * The circle in a graph
   */
  /*package*/ class Circle extends HashSet<Vertex> {

    /**
     * Creates a new circle
     */
    Circle(Graph graph) {
      for (Vertex vertex : graph.getVertices())
        add(vertex);
      circles.add(this);
    }
    
    /**
     * Creates a new circle
     */
    Circle(Collection<Vertex> nodes) {
      addAll(nodes);
      circles.add(this);
    }
    
    /**
     * Add a vertex
     */
    @Override
    public boolean add(Vertex vertex) {
      // let super do its thing
      boolean rc = super.add(vertex);
      // remember vertex->this
      if (rc)
        vertex2circle.put(vertex, this);
      // done
      return rc;
    }
    
    /**
     * Folds all elements in path down to stop into this
     * circle. Folded nodes' circles are merged.
     */
    void fold(Stack<Vertex> path, Vertex stop) {
      
      // Loop through stack elements
      for (int i=path.size()-1;;i--) {
        // get next (=previous) the vertex in the stack
        Vertex vertex = path.get(i);
        // back at the stop?
        if (vertex==stop) 
          break;
        // grab its circle
        Circle other = getCircle(vertex);
        addAll(other);
        circles.remove(other);
        // next stack element
      }
      
      // done
    }
    
    /**
     * Accessor - the nodes
     */
    /*package*/ Set<Vertex> getNodes() {
      return this;
    }
    
  } //Circle  
  
} //CircularGraph
