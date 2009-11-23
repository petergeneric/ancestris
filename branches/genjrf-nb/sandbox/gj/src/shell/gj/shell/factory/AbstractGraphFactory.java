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

import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Base for all Graph/Tree/.. creation
 */
public abstract class AbstractGraphFactory {
  
  /** shape for nodes */
  protected Shape nodeShape;
  
  /**
   * Set default node shape
   */
  public void setNodeShape(Shape set) {
    nodeShape = set;
  }
  
  /**
   * Create a graph
   */
  public abstract EditableGraph create(Rectangle2D bounds);

  /**
   * Helper that returns Point2D for given args
   */
  protected Point2D getPoint(double x, double y) {
    return new Point2D.Double(x, y);
  }

  /**
   * Helper that returns the node with minimum degree from a list
   */
  protected EditableVertex getMinDegNode(EditableGraph graph, List<EditableVertex> list, boolean remove) {
    
    int pos = getRandomIndex(list.size());

    EditableVertex result = list.get(pos);
    int min = graph.getNumAdjacentVertices(result);
    
    for (int i=1;i<list.size();i++) {
      EditableVertex other = list.get( (pos+i)%list.size() );
      if (graph.getNumAdjacentVertices(other) < min) 
        result=other;
    }
    if (remove) list.remove(result);
    
    return result;
  }
  
  /**
   * Helper that returns a random index for a list
   */
  protected int getRandomIndex(int ceiling) {
    double rnd = 1;
    while (rnd==1) rnd=Math.random();
    return (int)(rnd*ceiling);
  }

  /**
   * Helper that returns a random DefaultNode from a list
   */
  protected EditableVertex getRandomNode(List<EditableVertex> list, boolean remove) {
    int i = getRandomIndex(list.size());
    return remove ? list.remove(i) : list.get(i);
  }
  
  /**
   * Helper that returns a random DefaultArc from a list
   */
  protected Object getRandomArc(List<EditableEdge> list, boolean remove) {
    int i = getRandomIndex(list.size());
    return remove ? list.remove(i) : list.get(i);
  }
  

  /**
   * Helper to create a random position in given canvas
   * @param canvas the canvas to respect
   * @param shape the shape that will be place at the resulting position
   */
  protected Point2D getRandomPosition(Rectangle2D canvas, Shape shape) {
    
    Rectangle2D nodeCanvas = shape.getBounds2D();

    double 
      x = canvas.getMinX() - nodeCanvas.getMinX(),
      y = canvas.getMinY() - nodeCanvas.getMinY(),
      w = canvas.getWidth() - nodeCanvas.getWidth(),
      h = canvas.getHeight() - nodeCanvas.getHeight();

    return new Point2D.Double(x + Math.random()*w, y + Math.random()*h);
    
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = getClass().getName();
    return s.substring(s.lastIndexOf('.')+1);
  }
  
} //AbstractFactory
