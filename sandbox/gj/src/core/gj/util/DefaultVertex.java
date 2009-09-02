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

import gj.model.Vertex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A vertex implementation - suitable for on-the-fly/lazy generation
 */
public class DefaultVertex<T> implements Vertex {
  
  private T content;
  private Set<DefaultEdge<T>> edges = new HashSet<DefaultEdge<T>>();
  
  public DefaultVertex(T content) {
    this.content = content;
  }
  
  public T getContent() {
    return content;
  }
  
  /*package*/ void addEdge(DefaultEdge<T> edge) {
    edges.add(edge);
  }
  
  public Collection<DefaultEdge<T>> getEdges() {
    return Collections.unmodifiableCollection(edges);
  }
  
  @Override
  public int hashCode() {
    return content.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DefaultVertex))
      return false;
    DefaultVertex<?> that = (DefaultVertex<?>)obj;
    return this.content.equals(that.content);
  }
  
  @Override
  public String toString() {
    return content.toString();
  }

  public static Set<Vertex> wrap(Object[] vertices) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Object vertex : vertices)
      result.add(new DefaultVertex<Object>(vertex));
    return result;
  }
  
  public static Set<Vertex> wrap(Collection<Object> vertices) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Object vertex : vertices)
      result.add(new DefaultVertex<Object>(vertex));
    return result;
  }
  
}
