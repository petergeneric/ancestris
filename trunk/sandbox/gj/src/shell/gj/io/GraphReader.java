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
import gj.geom.ShapeHelper;
import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;
import gj.util.DefaultRouting;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read a graph
 */
public class GraphReader {
  
  /** statics - default */
  private final static Shape DEFAULT_SHAPE = new Rectangle2D.Double(-16,-16,32,32);
  
  /** the parser we're using */
  private SAXParser parser;
  
  /** the input we're reading from */
  private InputStream in;
  
  /** the graph we're creating */
  private EditableGraph result;

  /** identity support */
  private Map<String,EditableVertex> id2vertex = new HashMap<String,EditableVertex>();
  private Map<String,EditableEdge>  id2edge = new HashMap<String,EditableEdge>();
  private Map<String,Shape> id2shape = new HashMap<String,Shape>();

  /**
   * Constructor
   */
  public GraphReader(InputStream in) throws IOException {
    
    // Try to get a parser
    try {
      parser = SAXParserFactory.newInstance().newSAXParser();
    } catch (Throwable t) {
      throw new IOException("Couldn't initialize SAXParser for reading ("+t.getMessage()+")");
    }
    
    // remember in
    this.in = in;
    
    // done
  }
  
  /**
   * Read - Graph
   */
  public EditableGraph read() throws IOException {
    
    // create a graph
    result = new EditableGraph();
    
    try {
      
      // parse
      XMLHandler handler = new XMLHandler(new GraphHandler(result));
      parser.parse(in, handler);
      
    } catch (SAXException e) {
      throw new IOException("Couldn't read successfully because of SAXException ("+e.getMessage()+")");
    }

    // done
    return result;
  }
  
  /**
   * Handle an error during read
   */
  protected void error(String message) {
    throw new RuntimeException(message);
  }
  
  /**
   * Handling structure elements
   */
  private abstract class ElementHandler {
    protected ElementHandler start(String name, Attributes atts) { return this; }
    protected void end(String name) {} 
  } //ElementHandler
  
  /**
   * Handling structure elements - Graph
   */
  private class GraphHandler extends ElementHandler {
    private EditableGraph graph;
    protected GraphHandler(EditableGraph grAph) {
      graph = grAph;
    }
    @Override
    protected ElementHandler start(String name, Attributes atts) {
      if ("node".equals(name)||"vertex".equals(name)) return new VertexHandler(graph, atts);
      if ("arc".equals(name)||"edge".equals(name)) return new EdgeHandler(graph, atts);
      if ("shape".equals(name)) return new ShapeHandler("shape", atts);
      return this;
    }
  } //GraphHandler
  
  /**
   * Handling structure elements - Vertex
   */
  private class VertexHandler extends ElementHandler {
    private ShapeHandler shapeHandler;
    private ShapeHandler originalShapeHandler;
    private EditableGraph graph;
    private String id = null;
    private Shape shape = null;
    private Point2D pos = null;
    private Object content = null;
    protected VertexHandler(EditableGraph grAph, Attributes atts) {
      graph = grAph;
      // the id
      id = atts.getValue("id");
      if (id==null)
        error("expected id=");
      // find a shape
      shape = id2shape.get(atts.getValue("sid"));
      if (shape==null) shape = DEFAULT_SHAPE;
      // relative position?
      if (atts.getValue("x")!=null)
        pos = new Point2D.Double(Double.parseDouble(atts.getValue("x")), Double.parseDouble(atts.getValue("y")));
      // its content
      content = atts.getValue("c");
      // done
    }
    @Override
    protected ElementHandler start(String name, Attributes atts) {
      if ("shape".equals(name)) {
        shapeHandler = new ShapeHandler("shape", atts);
        return shapeHandler;
      }
      if ("original".equals(name)) {
        originalShapeHandler = new ShapeHandler("original", atts);
        return originalShapeHandler;
      }
      return this;
    }
    @Override
    protected void end(String name) {
      // check for shape in vertex
      if (shapeHandler!=null) 
        shape = shapeHandler.getResult();
      // handle old style relative shape position
      if (pos!=null) 
        shape = ShapeHelper.createShape(shape, pos);
      // calculate original shape
      Shape original = originalShapeHandler!=null ? originalShapeHandler.getResult() : shape;
      // create the vertex
      EditableVertex v = graph.addVertex(original, content);
      v.setShape(shape);
      // keep it
      id2vertex.put(id, v);
    }    
  } //VertexHandler
  
  /**
   * Handling structure elements - Edge
   */
  private class EdgeHandler extends ElementHandler {
    private ShapeHandler shapeHandler;
    private EditableEdge edge;
    private boolean invert;
    protected EdgeHandler(EditableGraph graph, Attributes atts) {
      EditableVertex
        s = id2vertex.get(atts.getValue("s")),
        e = id2vertex.get(atts.getValue("e"));
      edge = graph.addEdge(s, e);
      id2edge.put(atts.getValue("id"),edge);
      invert = "-1".equals(atts.getValue("d"));
    }
    @Override
    protected ElementHandler start(String name, Attributes atts) {
      if ("path".equals(name)) {
        shapeHandler = new ShapeHandler("path", atts);
        return shapeHandler;
      }
      return this;
    }
    @Override
    protected void end(String name) {
      if (shapeHandler!=null) {
        DefaultRouting path = new DefaultRouting(shapeHandler.getResult());
        if (invert)
          path.setInverted();
        edge.setPath(path);
      }
    }    
  } //EdgeHandler

  /**
   * Handling structure elements - Shape
   */
  private class ShapeHandler extends ElementHandler {
    private String end;
    private double[] values = new double[100];
    private int size=0;
    private String id;
    private Shape result;
    protected ShapeHandler(String end, Attributes atts) {
      id = atts.getValue("id");
      this.end = end;
    }
    @Override
    protected ElementHandler start(String name, Attributes atts) {
      
      for (int i=0;i<SEG_NAMES.length;i++) {
        
        if (SEG_NAMES[i].equals(name)) {
          values[size++]=i;
          
          if (SEG_SIZES[i]>0) {
            
            for (int j=0;j<SEG_SIZES[i]/2; j++) {
              values[size++] = Double.parseDouble(atts.getValue("x"+j));
              values[size++] = Double.parseDouble(atts.getValue("y"+j));
            }
          }
          break;
        }
      }
      
      return this;
    }
    @Override
    protected void end(String name) {
      if (!end.equals(name)) return;
      values[size++]=-1;
      result = ShapeHelper.createShape(0,0,1,1,values);
      if (id!=null) id2shape.put(id, result);
    }
    protected Shape getResult() {
      return result;
    }
  } //ShapeHandler
  
  
  /**
   * A SAX 'Default' Handler that knows how to read xml 
   * stacking element handlers
   */
  private final class XMLHandler extends DefaultHandler {
    
    private Stack<ElementHandler> stack = new Stack<ElementHandler>();
    
    /**
     * Constructor
     */
    public XMLHandler(ElementHandler root) {
      stack.push(root);
    }    

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String namespaceURI,String localName,String qName,Attributes atts)throws SAXException {
      ElementHandler current = (ElementHandler)stack.peek();
      ElementHandler next = current.start(qName,atts);
      stack.push(next);
    }
    

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
      ElementHandler current = (ElementHandler)stack.pop();
      current.end(qName);
    }

  } //XMLHandler

} //GraphReader
