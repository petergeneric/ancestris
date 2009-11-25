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
package gj.shell.model;

import gj.geom.ShapeHelper;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A default implementation for a vertex
 */
public class EditableVertex implements Vertex {
  
  /** the content of this vertex */
  private Object content;
  
  /** all edges of this vertex */
  private Collection<EditableEdge> edges = new LinkedHashSet<EditableEdge>(3);
  
  /** the original shape of this node */
  private Shape originalShape, editedShape;
  
  /**
   * interface implementation
   */
  public Shape getShape() {
    return editedShape;
  }
  
  /**
   * special request for original shape
   */
  public Shape getOriginalShape() {
    return originalShape;
  }
  
  /**
   * special request for original shape
   */
  public void setOriginalShape(Shape shape) {
    originalShape = shape;
    setShape(shape);
  }
  
  /**
   * interface implementation
   */
  public void setShape(Shape set) {
    
    editedShape = set;
    
    Point2D to = ShapeHelper.getCenter(set);
    //Point2D from = ShapeHelper.getCenter(originalShape);
    originalShape = ShapeHelper.createShape(originalShape, to);
  }
  
  /**
   * Constructor
   */  
  EditableVertex(Shape shape, Object content) {
    
    if (shape==null) shape = new Rectangle();
    
    this.content = content;
    this.originalShape = shape;
    setShape(shape);
  }
  
  /**
   * Number of neighbours
   */
  public int getNumNeighbours() {
    return getNeighbours().size();
  }
  
  /**
   * Returns neighbours
   */
  public Set<Vertex> getNeighbours() {
    return LayoutHelper.getNeighbours(this);
  }

  /**
   * Check for neighbour
   */
  public boolean isNeighbour(EditableVertex v) {
    return getNeighbours().contains(v);
  }
  
  /**
   * Adds an edge to given vertex
   */
  /*package*/ EditableEdge addEdge(EditableVertex that) {

    // don't allow duplicates
    if (getNeighbours().contains(that))
      throw new IllegalArgumentException("already exists edge between "+this+" and "+that);
    if (this.equals(that))
      throw new IllegalArgumentException("can't have edge between self ("+this+")");

    // setup self
    EditableEdge edge = new EditableEdge(this, that);
    this.edges.add(edge);
    if (that!=this) 
      that.edges.add(edge);
    
    // done
    return edge;
  }
  
  /**
   * Retrieves one edge
   */
  public EditableEdge getEdge(EditableVertex to) {
    for (EditableEdge edge : edges) {
      if (edge.getStart()==this&&edge.getEnd()==to||edge.getStart()==to&&edge.getEnd()==this)
        return edge;
    }
    throw new IllegalArgumentException("no edge between "+this+" and "+to);
  }
  
  /**
   * Retrieves all edges
   */
  public Collection<EditableEdge> getEdges() {
    return edges;
  }
  
  /**
   * Removes edge from this vertex
   */
  /*package*/ void removeEdge(EditableEdge edge) {
    edges.remove(edge);
  }
  
  /**
   * Check if a point lies within vertex
   */
  public boolean contains(Point2D point) {
    return getShape().contains(point.getX(),point.getY());   
  }
  
  /**
   * interface implementation
   */
  public Object getContent() {
    return content;
  }

  /**
   * accessor - content
   */
  public void setContent(Object set) {
    content = set;
  }

  /**
   * String representation
   */
  @Override
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }

} //Vertex
