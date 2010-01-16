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
package gj.layout.graph.tree;

import static gj.util.LayoutHelper.getInDegree;
import gj.layout.Graph2D;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.model.Vertex;
import gj.util.AbstractGraphLayout;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Vertex layout for Trees
 */
public class TreeLayout extends AbstractGraphLayout<Vertex> {

  /** distance of nodes in generation */
  private int distanceInGeneration = 20;

  /** distance of nodes between generations */
  private int distanceBetweenGenerations = 20;

  /** the alignment of parent over its children */
  private Alignment alignmentOfParents = Alignment.Center;

  /** whether children should be balanced or simply stacked */
  private boolean isBalanceChildren = false;

  /** what we do with edges */
  private EdgeLayout edgeLayout = EdgeLayout.Polyline;

  /** orientation in degrees 0-359 */
  private double orientation = 180;

  /** whether to order by position instead of natural sequence */
  private boolean isOrderSiblingsByPosition = true;
  
  /** whether we allow acyclic graphs */
  private boolean isSingleSourceDAG = true;

  /**
   * Getter - distance of nodes in generation
   */
  public int getDistanceInGeneration() {
    return distanceInGeneration;
  }

  /**
   * Setter - distance of nodes in generation
   */
  public void setDistanceInGeneration(int set) {
    distanceInGeneration = set;
  }

  /**
   * Getter - distance of nodes between generations
   */
  public int getDistanceBetweenGenerations() {
    return distanceBetweenGenerations;
  }

  /**
   * Setter - distance of nodes between generations
   */
  public void setDistanceBetweenGenerations(int set) {
    distanceBetweenGenerations=set;
  }

  /**
   * Getter - the alignment of parent over its children
   */
  public Alignment getAlignmentOfParents() {
    return alignmentOfParents;
  }

  /**
   * Setter - the alignment of parent over its children
   */
  public void setAlignmentOfParents(Alignment set) {
    alignmentOfParents = set;
  }

  /**
   * Getter - whether children are balanced optimally 
   * (spacing them apart where necessary) instead of
   * simply stacking them. Example
   * <pre>
   *      A                A
   *    +-+---+         +--+--+
   *    B C   D   -->   B  C  D
   *  +-+-+ +-+-+     +-+-+ +-+-+
   *  E F G H I J     E F G H I J
   * </pre>
   */
  public boolean getBalanceChildren() {
    return isBalanceChildren;
  }

  /**
   * Setter 
   */
  public void setBalanceChildren(boolean set) {
    isBalanceChildren=set;
  }

  /**
   * Getter - what we do with edges
   */
  public EdgeLayout getEdgeLayout() {
    return edgeLayout;
  }

  /**
   * Setter - whether arcs are direct or bended
   */
  public void setEdgeLayout(EdgeLayout set) {
    edgeLayout = set;
  }
  
  /**
   * Setter - which orientation to use
   * @param orientation value between 0 and 360 degree
   */
  public void setOrientation(double orientation) {
    this.orientation = orientation;
  }
  
  /**
   * Getter - which orientation to use
   * @return value between 0 and 360 degree
   */
  public double getOrientation() {
    return orientation;
  }
  
  /**
   * Getter - root node
   */
  public Vertex getRoot(Graph2D graph2d) {
    // check remembered
    Vertex result = getAttribute(graph2d);
    if (result==null) {
      // find root with zero in-degree
      for (Vertex v : graph2d.getVertices()) {
        if (getInDegree(v)==0) {
          result = v;
          setRoot(graph2d, result);
          break;
        }
      }
    }
    // done
    return result;
  }

  /**
   * Getter - root node
   */
  public void setRoot(Graph2D graph2d, Vertex root) {
    setAttribute(graph2d, root);
  }

  /**
   * Setter - whether to order siblings by their current position
   */
  public void setOrderSiblingsByPosition(boolean isOrderSiblingsByPosition) {
    this.isOrderSiblingsByPosition = isOrderSiblingsByPosition;
  }

  /**
   * Getter - whether to order siblings by their current position
   */
  public boolean isOrderSiblingsByPosition() {
    return isOrderSiblingsByPosition;
  }

  /**
   * Whether layout should assume a single source directed acyclic graph or not.
   * In the former case this means that direction of edges is considered and graphs without
   * directed cycles but with shared sub-trees are handled by routing edges between 
   * non-siblings via a euclidean shortest path layout
   */
  public boolean isSingleSourceDAG() {
    return isSingleSourceDAG;
  }

  /**
   * Whether layout should assume a single source directed acyclic graph or not.
   * In the former case this means that direction of edges is considered and graphs without
   * directed cycles but with shared sub-trees are handled by routing edges between 
   * non-siblings via a euclidean shortest path layout
   */
  public void setSingleSourceDAG(boolean setSingleSourceDAG) {
    this.isSingleSourceDAG = setSingleSourceDAG;
  }

  /**
   * Layout a layout capable graph
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {
    
    // ignore an empty tree
    Collection<? extends Vertex> vertices = graph2d.getVertices(); 
    if (vertices.isEmpty())
      return new Rectangle2D.Double();
    
    // check root
    Vertex root = getRoot(graph2d);
    if (root==null)
      throw new GraphNotSupportedException("Graph is not a tree (no vertex with in-degree of zero)");
    
    context.getLogger().fine("root is ["+root+"]");
    
    // recurse into it
    Set<Vertex> visited = new HashSet<Vertex>();
    Branch branch = new Branch(null, root, graph2d, new ArrayDeque<Vertex>(), visited, this, context);
    
    if (context.isDebug())
      context.addDebugShape(branch.getShape());
    
    // check spanning tree in case we assumed DAG with single source
    if (isSingleSourceDAG&&visited.size()!=vertices.size()) {
      context.getLogger().fine("not a spanning tree (#visited="+visited.size()+" #vertices="+vertices.size());
      throw new GraphNotSupportedException("Graph is not a spanning tree ("+vertices.size()+"!="+visited.size()+")");
    }
    
    // layout edges
    edgeLayout.apply(graph2d, branch, this, context);
    
    // done
    return branch.getShape();
  }
  
} //TreeLayout
