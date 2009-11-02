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
package gj.shell;

import gj.geom.Geometry;
import gj.layout.Routing;
import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An animation
 */
/*package*/ class Animation {

  /** the graph */
  private EditableGraph graph;
  
  /** the moves */
  private Motion[] motions;
  
  /** the shapes of edges */
  private List<Object> edgesAndShapes;
  
  /** animation status */
  private long 
    totalTime, startFrame;
  
  private double START = 0D, END = 1D;
  
  /**
   * Constructor
   */
  public Animation(EditableGraph graph) {
    this(graph, 1000);
  }
  public Animation(EditableGraph graph, long totalTime) {
    this.graph = graph;
    this.totalTime = totalTime;
  }
    
  /**
   * marker - before layout
   */
  public void beforeLayout() {
    
    // create movements for vertices ...
    motions = new Motion[graph.getNumVertices()];
    Iterator<EditableVertex> vertices = graph.getVertices().iterator();
    for (int m=0;vertices.hasNext();m++) 
      motions[m] = new Motion(vertices.next());
   
  }
  
  public void afterLayout() {
    
    // take a snapshot of what's there right now
    for (int m=0;m<motions.length;m++) 
      motions[m].afterLayout();
    
    edgesAndShapes = new ArrayList<Object>(graph.getNumEdges());
    for (Iterator<EditableEdge> edges = graph.getEdges().iterator(); edges.hasNext(); ) {
      EditableEdge edge = edges.next();
      edgesAndShapes.add(edge);
      edgesAndShapes.add(edge.getPath());
    }
    
    // done for now
  }
  
  /**
   * Runs one frame of the animation
   * @return true if animation is done
   */
  public boolean animate() {
    
    // check what we're doing now
    long now = System.currentTimeMillis();
    
    // first frame?
    if (startFrame==0)
      startFrame = now;
      
    // has total passed already?
    if (startFrame+totalTime<now) {
      stop();
      return true;
    }

    // do the move
    if (animate(Math.min(END, ((double)now-startFrame)/totalTime))) {
      stop();
      return true;
    }
    
    // done for now
    return false;
  }
    
  /**
   * Stops the animation by setting it to the last frame
   */
  public void stop() {
    
    // perform step to final frame
    animate(END);
    
    // restore edges
    // TODO currently edges are layed out without bends in animation
    Iterator<?> it = edgesAndShapes.iterator();
    while (it.hasNext()) {
      ((EditableEdge)it.next()).setPath((Routing)it.next());
    }
    // stop all moves
    motions=null;
    
    // done
  }

  /**
   * Performing one step in the animation
   */
  private boolean animate(double index) {
    
    boolean done = true;
    synchronized (graph) {
      // loop moves
      for (int m=0;m<motions.length;m++) {
        Motion motion = motions[m];
        // do the move
        done &= motion.animate(index);
      }
    }
        
    // done
    return done;
  }

  /**
   * A motion in the animation
   */
  private class Motion {
    
    private EditableVertex vertex;
    private GeneralPath start, end;
    
    Motion(EditableVertex set) { 
      vertex = set; 
      beforeLayout();
    }
    void beforeLayout() { 
      start = new GeneralPath(vertex.getShape());
    } 
    void afterLayout() { 
      end = new GeneralPath(vertex.getShape());
    } 
    boolean animate(double index) {
      vertex.setShape(shape(index));
      return false;
    }

    private Shape shape(double index) {

      // start or end
      if (index==START)
        return start;
      if (index==END)
        return end;

      // blend shapes
      return Geometry.getInterpolation(index, start, end);
    }
  }
  
}
