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

import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An adapter for tree to graph
 */
public class TreeGraphAdapter<V> implements Graph {
  
  private Tree<V> tree;
  
  public TreeGraphAdapter(Tree<V> tree) {
    this.tree = tree;
  }
  
  public int getNumVertices() {
    return _getNumVertices(tree.getRoot());
  }
  
  private int _getNumVertices(V parent) {
    int result = 1;
    for (V child : tree.getChildren(parent)) 
      result += _getNumVertices(child);
    return result;
  }
  
  @SuppressWarnings({ "unchecked" })
  public V getContent(Vertex v) {
    return ((DefaultVertex<V>)v).getContent();
  }

  public Collection<Vertex> getVertices() {
    return _getVertices(new DefaultVertex<V>(tree.getRoot()), new ArrayList<Vertex>());
  }
    
  private Collection<Vertex> _getVertices(DefaultVertex<V> vparent, List<Vertex> result) {
    result.add(vparent);
    for (V child : tree.getChildren(vparent.getContent())) {
      DefaultVertex<V> vchild = new DefaultVertex<V>(child);
      new DefaultEdge<V>(vparent, vchild);
      _getVertices(vchild, result);
    }
    return result;
  }

  public int getNumEdges() {
    return _getNumEdges(tree.getRoot());
  }
  
  private int _getNumEdges(V parent) {
    int result = 0;
    for (V child : tree.getChildren(parent)) {
      result ++;
      result += _getNumEdges(child);
    }
    return result;
  }
  
  public Collection<Edge> getEdges() {
    return _getEdges(tree.getRoot(), new ArrayList<Edge>());
  }
  
  private Collection<Edge> _getEdges(V parent, List<Edge> result) {
    for (V child : tree.getChildren(parent)) {
      result.add(new DefaultEdge<V>(new DefaultVertex<V>(parent), new DefaultVertex<V>(child)));
      _getEdges(child, result);
    }
    return result;
  }
  
  /**
   * Interface for a tree
   */
  public interface Tree<V> {

    public abstract V getRoot();
    
    public abstract List<V> getChildren(V parent);
    
    public abstract V getParent(V child);

  }
  
}
