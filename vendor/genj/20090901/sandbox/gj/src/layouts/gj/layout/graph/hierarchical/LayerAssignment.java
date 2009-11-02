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
import gj.layout.GraphNotSupportedException;
import gj.model.Edge;
import gj.model.Vertex;

import java.util.Collection;
import java.util.Comparator;

/**
 * A interface to a layering of vertexes
 */
public interface LayerAssignment {

  /**
   * Process given graph and layout and produce a proper layer assignment
   * @param graph the graph to analyze
   * @throws GraphNotSupportedException
   */
  public void assignLayers(Graph2D graph, Comparator<Vertex> orderOfVerticesInLayer) throws GraphNotSupportedException;
  
  /**
   * number of dummy vertices
   */
  public int getNumDummyVertices();

  /**
   * Number of layers
   * @return height
   */
  public int getHeight();

  /**
   * Maximum of number of Vertices each layer
   * @return width
   */
  public int getWidth();
  
  /**
   * Number of Vertices for given layer 
   * @param layer the layer to prompt
   * @return width
   */
  public int getWidth(int layer);
  
  /**
   * Vertex in a layer
   * @param layer the layer
   * @param u the position in the layer
   * @return selected vertex in graph or DUMMY
   * @see DummyVertex
   */
  public Vertex getVertex(int layer, int u);

  /**
   * Swap two vertices in a layer
   * @param layer the layer
   * @param u first position
   * @param v second position
   */
  public void swapVertices(int layer, int u, int v);

  /**
   * Routing of a given edge
   * @param edge the edge
   * @return routing of edge
   */
  public Routing getRouting(Edge edge);

  /**
   * Adjacent positions for layer and positions
   * @param layer the layer
   * @param u the position
   * @return list of indices in layer-1
   */
  public int[] getIncomingIndices(int layer, int u);
  
  /**
   * Adjacent positions for layer and positions
   * @param layer the layer
   * @param u the position
   * @return list of indices in layer+1
   */
  public int[] getOutgoingIndices(int layer, int u);

  /**
   * An edge routing
   */
  public class Routing {
    public int outIndex;
    public int outDegree;
    public int len;
    public int[] layers;
    public int[] positions;
    public int inIndex;
    public int inDegree;
  }

  /**
   * Dummy vertex 
   * @see LayerAssignment#getVertex(int, int)
   */
  public class DummyVertex implements Vertex {
    @Override
    public String toString() {
      return "Dummy";
    }
    public Collection<? extends Edge> getEdges() {
      throw new IllegalArgumentException("n/a");
    }
  };
}
