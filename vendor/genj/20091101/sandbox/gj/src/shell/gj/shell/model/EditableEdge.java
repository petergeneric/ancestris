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

import gj.geom.Geometry;
import gj.geom.ShapeHelper;
import gj.layout.Routing;
import gj.model.Edge;
import gj.util.LayoutHelper;

import java.awt.geom.Point2D;

/**
 * A default implementation for an Edge
 */
public class EditableEdge implements Edge {
  
  /** starting vertex */
  private EditableVertex start;

  /** ending vertex */
  private EditableVertex end;
  
  private Routing path;
  
  private long hash = -1;
  
  /**
   * Constructor
   */
  EditableEdge(EditableVertex start, EditableVertex end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * Check if a point lies at vertex
   */
  public boolean contains(Point2D point) {
    return 8>Geometry.getMinimumDistance(point, getPath().getPathIterator(null));
  }
  
  /**
   * overriden - create a default edge shape if necessary
   */
  public Routing getPath() {
    
    if (!updateHash()) 
      setPath(makeShape());
    
    return path;
  }
  
  private Routing makeShape() {
    return LayoutHelper.getRouting(
        start.getShape(), ShapeHelper.getCenter(start.getShape()),
        end.getShape(), ShapeHelper.getCenter(end.getShape())
    );   
  }
  
  public void setPath(Routing set) {
    if (set==null)
      set = makeShape();
    path = set;
    updateHash();
  }
  
  boolean updateHash() {
    long oldHash = hash;
    hash = (int)(start.getShape().hashCode()+end.getShape().hashCode());
    return oldHash==hash;
  }

  /**
   * String represenation
   */
  @Override
  public String toString() {
    return start.toString() + ">" + end.toString();
  }
  
  /**
   * the start of the edge
   */
  public EditableVertex getStart() {
    return start;
  }

  /**
   * the end of the edge
   */
  public EditableVertex getEnd() {
    return end;
  }

  @Override
  public int hashCode() {
    return start.hashCode() + end.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Edge))
      return false;
    Edge that = (Edge)obj;
    return (this.start.equals(that.getStart()) && this.end.equals(that.getEnd()));
  }
  
}
