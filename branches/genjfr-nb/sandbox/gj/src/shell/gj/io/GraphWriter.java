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
package gj.io;

import static gj.geom.PathIteratorKnowHow.SEG_NAMES;
import static gj.geom.PathIteratorKnowHow.SEG_SIZES;
import gj.layout.Routing;
import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;
import gj.util.DefaultRouting;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Write a graph
 */
public class GraphWriter {

  /** an empty string */
  private final static String EMPTY = "                                                         ";
  
  /** the out */
  private PrintWriter out;
  
  /** stack */
  private Stack<String> stack = new Stack<String>();
  
  /** elements mapped to ids */
  private Map<Object,Integer> element2id  = new HashMap<Object,Integer>();
  
  /**
   * Constructor
   */  
  public GraphWriter(OutputStream out) throws IOException {
    this.out = new PrintWriter(out);
  }

  /**
   * Write - Graph
   */
  public void write(EditableGraph g) throws IOException {
    // open the graph
    push("graph",null,false);
    // vertices
    writeVertices(g);
    // edges
    writeEdges(g);
    // done
    pop();
    out.flush();
  }

  /**
   * Write - Edges
   */
  private void writeEdges(EditableGraph g) throws IOException {
    
    push("edges",null,false);
    
    // write 'em
    for (EditableEdge edge : g.getEdges()) 
      writeEdge(g, edge);
    
    pop();
  }

  /**
   * Write - Edge
   */
  private void writeEdge(EditableGraph g, EditableEdge edge) throws IOException {

    Routing path = edge.getPath();
    
    ElementInfo info = new ElementInfo();
    info.put("id", getId(edge));
    info.put("s", getId(edge.getStart()));
    info.put("e", getId(edge.getEnd()));
    if (path instanceof DefaultRouting && ((DefaultRouting)path).isInverted())
      info.put("d", "-1" );
    push("edge",info,false);
      writeShape("path", path);
    pop();
  }

  /**
   * Write - Shape
   */
  private void writeShape(String element, Shape shape) throws IOException {

    ElementInfo info = new ElementInfo();
    push(element,info,false);
    PathIterator it = shape.getPathIterator(null);
    double[] segment = new double[6];
    while (!it.isDone()) {
      int type = it.currentSegment(segment);
      writeSegment(type,segment);
      it.next();
    };
    pop();
  }
  
  /**
   * Write - Segment
   */
  private void writeSegment(int type, double[] segment) throws IOException {
    ElementInfo info = new ElementInfo();
    for (int i=0;i<SEG_SIZES[type]/2;i++) {
      info.put("x"+i, segment[i*2+0]);
      info.put("y"+i, segment[i*2+1]);
    }
    push(SEG_NAMES[type],info,true);
  }
  
  
  /**
   * Write - Vertices
   */
  private void writeVertices(EditableGraph g) throws IOException {
    push("vertices",null,false);
    for (EditableVertex vertex : g.getVertices()) 
      writeVertex(vertex);
    pop();
  }
  
  /**
   * Write - Vertex
   */
  private void writeVertex(EditableVertex v) throws IOException {
    
    // gather element information
    ElementInfo info = new ElementInfo();
    info.put("id", getId(v));

    Object content = v.getContent();
    if (content!=null)
      info.put("c", content.toString());

    push("vertex",info,false);

    // store shape
    Shape shape = v.getShape();
    writeShape("shape", shape);
    
    Shape original = v.getOriginalShape();
    if (!shape.equals(original))
      writeShape("original", original);
    
    pop();

    // done
  }
  
  /**
   * Get id for element (vertex or edge)
   */
  private int getId(Object element) {
    // lookup
    Integer result = element2id.get(element);
    if (result==null) {
      result = element2id.size()+1;
      element2id.put(element, result);
    }
    // done
    return result;
  }

  /**
   * Push element
   */
  private void push(String tag, ElementInfo info, boolean close) {
    StringBuffer b = new StringBuffer();
    b.append('<').append(tag);
    if (info!=null) info.append(b);
    if (close) {
      write(b.append("/>").toString());
    } else {
      write(b.append('>').toString());
      stack.push(tag);
    }
  }
  
  /**
   * Pop element
   */
  private void pop() {
    write("</"+stack.pop()+">");
  }
  
  /**
   * Write txt
   */
  private void write(String txt) {
    out.print(EMPTY.substring(0,stack.size()));
    out.println(txt);
  }

  /**
   * Element information
   */
  public static class ElementInfo {
    private ArrayList<String> list = new ArrayList<String>(6);
    public void put(String key, double val) {
      list.add(key);
      list.add(Double.toString(val));
    }    
    public void put(String key, int val) {
      list.add(key);
      list.add(Integer.toString(val));
    }    
    public void put(String key, String val) {
      if (val==null)
        return;
      list.add(key);
      list.add(val);
    }    
    public void append(StringBuffer b) {
      Iterator<String> it = list.iterator();
      while (it.hasNext()) {
        String key = it.next();
        String val = it.next();
        b.append(' ').append(key).append("=\"").append(val).append("\"");
      }
    }
  } //ElementInfo

} //GraphWriter

