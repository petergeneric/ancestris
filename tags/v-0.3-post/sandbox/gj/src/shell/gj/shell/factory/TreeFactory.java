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
package gj.shell.factory;

import gj.geom.ShapeHelper;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphFactory - a Tree
 */
public class TreeFactory extends AbstractGraphFactory {
  
  /** the maximum depth */
  private int maxDepth = 5;

  /** the maximum number of children */
  private int maxChildren = 4;

  /** the number of nodes */
  private int numNodes = 4;

  /** 
   * Getter - the maximum depth 
   */
  public int getMaxDepth() {
    return maxDepth;
  }

  /** 
   * Setter - the maximum depth 
   */
  public void setMaxDepth(int set) {
    maxDepth=set;
  }

  /** 
   * Getter - the maximum number of children 
   */
  public int getMaxChildren() {
    return maxChildren;
  }

  /** 
   * Setter - the maximum number of children 
   */
  public void setMaxChildren(int set) {
    maxChildren=set;
  }

  /** 
   * Getter - the number of nodes 
   */
  public int getNumNodes() {
    return numNodes;
  }

  /** 
   * Setter - the number of nodes 
   */
  public void setNumNodes(int set) {
    numNodes=set;
  }

  /** a sample */
  private static final String[][] sample = {
      { "1.1", "1.2" },
      { "1.2", "1.3", "1.2.1.1", "1.2.2.1", "1.2.3.1" },
      { "1.3", "1.4" },
      { "1.4", "1.5", "1.4.1.1" },
      { "1.5", "1.6" },
      { "1.6" },
      
      { "1.2.1.1", "1.2.1.2" },
      { "1.2.1.2" },
      
      { "1.2.2.1", "1.2.2.2" },
      { "1.2.2.2", "1.2.2.3" },
      { "1.2.2.3", "1.2.2.4" },
      { "1.2.2.4" },

      { "1.2.3.1", "1.2.3.2" },
      { "1.2.3.2" },
      
      { "1.4.1.1", "1.4.1.2", "1.4.1.2.1.1"},
      { "1.4.1.2" },
      { "1.4.1.2.1.1",  "1.4.1.2.1.2"},
      { "1.4.1.2.1.2" }
    };
    
  /**
   * @see gj.shell.factory.AbstractGraphFactory#create(Rectangle2D)
   */
  @Override
  public EditableGraph create(Rectangle2D bounds) {
    
    // create the graph
    EditableGraph graph = new EditableGraph();
    
    // We loop through the sample data
    Map<String,EditableVertex> nodes = new HashMap<String,EditableVertex>(sample.length);
    for (int s = 0; s < sample.length; s++) {

      String key = sample[s][0];
      Point2D pos = getRandomPosition(bounds, nodeShape);
      Shape shape = ShapeHelper.createShape(nodeShape, pos);
      EditableVertex vertex = graph.addVertex(shape, key);
      nodes.put(key, vertex);
    }
     
    for (int s = 0; s < sample.length; s++) {
      EditableVertex from = nodes.get(sample[s][0]);
      for (int c = 1; c < sample[s].length; c++) {
        String key = sample[s][c];        
        EditableVertex to = nodes.get(key);
        graph.addEdge(from, to);
      }
    }
    
    // Done    
    return graph;
  }

} //TreeFactory
