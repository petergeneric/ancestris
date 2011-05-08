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

import gj.layout.GraphLayout;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.layout.Routing;
import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;

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
  private Movement[] moves;
  
  /** the shapes of edges */
  private List<Object> edgesAndShapes;
  
  /** animation status */
  private long 
    totalTime = 1000    ,
    frameTime = 1000/60 ,
    startFrame = 0,
    lastFrame = 0;
    
  /**
   * Constructor (before)
   */
  public Animation(EditableGraph graph, GraphLayout layout, LayoutContext context) throws LayoutException, GraphNotSupportedException {
    
    // something to animate?
    if (graph.getNumVertices() == 0) 
      return;

    // keep some data
    this.graph = graph;

    // create movements for vertices ...
    moves = new Movement[graph.getNumVertices()];
    Iterator<EditableVertex> vertices = graph.getVertices().iterator();
    for (int m=0;vertices.hasNext();m++) 
      moves[m] = new Movement(vertices.next());
   
    // run layout
    context.getLogger().info("Started pre-animation graph layout "+layout.getClass().getSimpleName());
    layout.apply(graph, context);
    context.getLogger().info("Finished pre-animation graph layout "+layout.getClass().getSimpleName());
    
    // take a snapshot of what's there right now
    for (int m=0;m<moves.length;m++) 
      moves[m].snapshot();
    
    edgesAndShapes = new ArrayList<Object>(graph.getNumEdges());
    for (Iterator<EditableEdge> edges = graph.getEdges().iterator(); edges.hasNext(); ) {
      EditableEdge edge = edges.next();
      edgesAndShapes.add(edge);
      edgesAndShapes.add(edge.getPath());
    }
    
    // move back to start
    animate(0);
    
    // setup start
    startFrame = System.currentTimeMillis(); 

    
    // done for now
  }
  
  /**
   * Runs one frame of the animation
   * @return true if animation is done
   */
  public boolean animate() throws InterruptedException {
    
    // done?
    if (moves==null)
      return true;

    // check what we're doing now
    long now = System.currentTimeMillis();
      
    // has total passed already?
    if (startFrame+totalTime<now) {
      stop();
      return true;
    }

    // sleep some?
    long sleep = (lastFrame+frameTime)-now;
    if (sleep>0) 
      Thread.sleep(sleep); 
        
    // do the move
    double time = Math.min(1, ((double)now-startFrame)/totalTime);
    if (animate(time)) 
      stop();
        
    // remember
    lastFrame = now;
    
    // done for now
    return false;
  }
    
  /**
   * Stops the animation by setting it to the last frame
   */
  public boolean stop() {
    // perform step to final frame
    if (moves!=null) 
      animate(1D);
    
    // restore edges
    // TODO currently edges are layed out without bends in animation
    Iterator<?> it = edgesAndShapes.iterator();
    while (it.hasNext()) {
      ((EditableEdge)it.next()).setPath((Routing)it.next());
    }
    // stop all moves
    moves=null;
    // done
    return false;
  }

  /**
   * Performing one step in the animation
   */
  private boolean animate( double time) {
    
    boolean done = true;

    synchronized (graph) {
      // loop moves
      for (int m=0;m<moves.length;m++) {
        Movement move = moves[m];
        // do the move
        done &= move.animate(time);
      }
    }
        
    // done
    return done;
  }

  /**
   * A movement of a vertex in the animation
   */
  private class Movement {
    private EditableVertex vertex;
    Movement(EditableVertex set) { 
      vertex = set; 
    }
    void snapshot() { 
      // FIXME need animation snapshot
    } 
    boolean animate(double time) {
      // done?
      if (time==1) {
        // FIXME need animation destination
        return true;
      }
      // FIXME need animation step
      // done      
      return false;
    }
  }
  
}
