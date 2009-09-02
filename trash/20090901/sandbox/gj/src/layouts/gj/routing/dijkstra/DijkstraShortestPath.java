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
package gj.routing.dijkstra;

import static gj.util.LayoutHelper.getNormalizedEdges;
import static gj.util.LayoutHelper.getOther;
import gj.layout.GraphNotSupportedException;
import gj.model.Edge;
import gj.model.Vertex;
import gj.model.WeightedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the shortes path algorithm by Dijkstra
 * @see <a href="http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra on Wikepedia</a>
 */
public class DijkstraShortestPath {
  
  /**
   * calculate the shortes path from given source to destination in graph
   * @param graph
   * @param source
   * @param sink
   */
  public List<Vertex> getShortestPath(WeightedGraph graph, Vertex source, Vertex sink) throws GraphNotSupportedException {
    
    // preload unvisited and start with source
    final Map<Vertex,Integer> vertex2distance = new HashMap<Vertex, Integer>();
    
    List<Vertex> considered = new ArrayList<Vertex>();
    
    Vertex cursor = source;
    setDistance(vertex2distance, source, 0);
    
    // loop from cursor through all unvisited vertices until we hit dest
    while (!cursor.equals(sink)) {
      
      // consider all neighbours
      int dist2here = vertex2distance.get(cursor);

      for (Edge edge : getNormalizedEdges(cursor)) {
        Vertex neighbour = getOther(edge, cursor);
        int dist2there = dist2here + getWeight(graph, edge);
        if (dist2there < getDistance(vertex2distance, neighbour)) {
          setDistance(vertex2distance, neighbour, dist2there);

          int pos=0;
          while (pos<considered.size() && dist2there<getDistance(vertex2distance, considered.get(pos))) pos++;
          considered.add(pos, neighbour);
        }
      }
      
      // pick next considered with min distance
      cursor = considered.remove(considered.size()-1);
      if (cursor==null)
        throw new GraphNotSupportedException("Graph is not spanning");
      
    }

    // walk distance backwards to collect the resulting path
    LinkedList<Vertex> result = new LinkedList<Vertex>();
    int distance = vertex2distance.get(sink);
    result.addFirst(sink);
    while (!cursor.equals(source)) {
      
      // look through neighbours
      for (Edge edge : getNormalizedEdges(cursor)) {
        Vertex neighbour = getOther(edge, cursor);
        int dist2there = distance - getWeight(graph, edge);
        if (getDistance(vertex2distance, neighbour) == dist2there) {
          distance = dist2there;
          cursor = neighbour;
          result.addFirst(cursor);
          break;
        }
      }
      
      // backtrack
    }
    
    // done
    return result;
  }
  
  /**
   * helper - weight of edge
   */
  private int getWeight(WeightedGraph graph, Edge edge) {
    return (int)Math.ceil(graph.getWeight(edge));
  }
  
  /**
   * helper - set distance value of a vertex
   */
  private void setDistance(Map<Vertex,Integer> vertex2distance, Vertex vertex, double distance) {
    vertex2distance.put(vertex, new Integer((int)Math.ceil(distance))); 
  }
  
  /**
   * helper - get distance value of a vertex
   */
  private int getDistance(Map<Vertex,Integer> vertex2distance, Vertex vertex) {
    Integer i = vertex2distance.get(vertex);
    return i==null ? Integer.MAX_VALUE : i.intValue();
  }
  

} //DijkstraShortestPath
