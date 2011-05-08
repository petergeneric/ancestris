/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A layout that arranges components in nested blocks of rows and columns
 * <pre>
 * <!ELEMENT row (col*|*)>
 * <!ELEMENT col (row*|*)>
 * <!ELEMENT *>
 * <!ATTLIST * wx CDATA>
 * <!ATTLIST * wy CDATA>
 * <!ATTLIST * gx CDATA>
 * <!ATTLIST * gy CDATA>
 * </pre>
 * wx,wy are weight arguments - gx,gy are grow arguments
 */
public class NestedBlockLayout implements LayoutManager2, Cloneable {
  
  private final static SAXException DONE = new SAXException("");
  
  private final static Logger LOG = Logger.getLogger("genj.util");

  /** whether we've been invalidated recently */
  private boolean invalidated = true;
  
  /** one root row is holds all the columns */
  private Block root;
  
  /** padding */
  private int padding = 1;
  
  /**
   * Constructor
   */
  private NestedBlockLayout(Block root) {
    this.root = root;
  }

  /**
   * Constructor
   */
  public NestedBlockLayout(String descriptor) {
    try {
      init(new StringReader(descriptor));
    } catch (IOException e) {
      // can't happen
    }
  }

  /**
   * Constructor
   */
  public NestedBlockLayout(Reader descriptor) throws IOException {
    init(descriptor);
  }
  
  /**
   * Constructor
   */
  public NestedBlockLayout(InputStream descriptor) throws IOException {
    init(new InputStreamReader(descriptor));
  }
  
  /**
   * Accessor to cell definitions
   */
  public Collection getCells() {
    return root.getCells(new ArrayList(10));
  }
  
  /**
   * Post Constructor Initializer
   */
  private void init(Reader descriptor) throws IOException {
    
    // parse descriptor
    try {
	    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
	    parser.parse(new InputSource(descriptor), new DescriptorHandler());
    } catch (SAXException sax) {
      if (DONE==sax) {
        return;
      }
      throw new RuntimeException(sax);
    } catch (IOException ioe) {
      throw (IOException)ioe;
    } catch (Exception e) {
      throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
    }
    
    // done
  }
  
  /**
   * Our descriptor parser
   */
  private class DescriptorHandler extends DefaultHandler {
    
    private Stack stack = new Stack();
    
    public InputSource resolveEntity(String publicId, String systemId) {
      // 20060601 let's not try to resolve any external entities - in case of GenJ running as an applet and a 
      // webserver returning a custom 404 spmeone might read a layout string from getResourceAsStream()
      // which doesn't return null but returns a custom page that we can't parse
      throw new IllegalArgumentException("Request for resolveEntity "+publicId+"/"+systemId+" not allowed in layout descriptor");
    }
    
    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {
      // new block!
      Block block = getBlock(qName, attributes);
      // make root or add to parent
      if (stack.isEmpty()) {
        root = block;
      } else {
        Block parent = (Block)stack.peek();
	      parent.add(block);
      }
      // throw on stack
      stack.add(block);
      // done
    }
    
    private Block getBlock(String element, Attributes attrs) {
      // row?
      if ("row".equals(element)) 
        return new Row(attrs);
      // column?
      if ("col".equals(element))
        return new Column(attrs);
      // a cell!
      return new Cell(element, attrs, padding);
    }
    
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      
      // check
      if (stack==null||stack.size()==0)
        throw new SAXException("unexpected /element");

      // pop last
      Block block = (Block)stack.pop();
      
      // are we done?
      if (stack.isEmpty())
        throw DONE;
    }
    
  };

  /**
   * an block in the layout
   */
  private static abstract class Block implements Cloneable {
    
    /** preferred size of column */
    Dimension preferred;

    /** weight of column */
    Point2D.Double weight;
    
    /** subs */
    ArrayList subs = new ArrayList(16);
    
    /** constructor */
    Block(Attributes attributes) {
    }

    /** copy */
    protected Object clone() {
      try {
        Block clone = (Block)super.clone();
        
        clone.subs = new ArrayList(subs.size());
        for (int i=0;i<subs.size();i++)
          clone.subs.add( ((Block)subs.get(i)).clone() );
        return clone;
      } catch (CloneNotSupportedException cnse) {
        throw new Error();
      }
    }
    
    /** remove */
    boolean remove(Component component) {

      // look for it
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        if (sub.remove(component)) {
          invalidate(false);
          return true;
        }
      }
      
      // not found
      return false;
      
    }
    
    /** add sub */
    Block add(Block block) {
      subs.add(block);
      invalidate(false);
      return block;
    }
    
    /** invalidate state */
    void invalidate(boolean recurse) {
      
      // clear state
      preferred = null;
      weight = null;

      // recurse
      if (recurse) for (int i=0;i<subs.size();i++) {
        ((Block)subs.get(i)).invalidate(true);
      }
    }
    
    /** weight */
    abstract Point2D weight();
    
    /** preferred size */
    abstract Dimension preferred();
      
    /** layout */
    abstract void layout(Rectangle in);
    
    /** all cells */
    Collection getCells(Collection collect) {
      for (int i=0;i<subs.size();i++) 
        ((Block)subs.get(i)).getCells(collect);
      return collect;
    }
    
    /** cell by element name */
    Cell getCell(String element) {
      // look for it in our subs
      Cell result = null;
      for (int i=0;result==null&&i<subs.size();i++) {

        // a sub at a time
        Block sub = (Block)subs.get(i);
        result = sub.getCell(element);
        
        // next
      }
      return result;
    }

  } //Block
  
  /**
   * a row
   */
  private static class Row extends Block {

    /** constructor */
    Row(Attributes attributes) {
      super(attributes);
    }

    /** add a sub */
    Block add(Block sub) {
      if (sub instanceof Row)
        throw new IllegalArgumentException("row can't contain row");
      super.add(sub);
      return sub;
    }
    
    /** preferred size */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
    
      // calculate
      preferred = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        preferred.width += sub.width;
        preferred.height = Math.max(preferred.height, sub.height);
      }
    
      // done
      return preferred;
    }
    
    /** weight */
    Point2D weight() {
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point2D.Double();
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        weight.x += sub.weight().getX();
        weight.y = Math.max(weight.y, sub.weight().getY());
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      // compute spare space horizontally
      double weight = 0;
      int spare = in.width;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().width;
        weight += sub.weight().getX();
      }
      double spareOverWeight = weight>0 ? spare/weight : 0;
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = sub.preferred().width + (int)(sub.weight().getX() * spareOverWeight);
        avail.height = in.height;

        sub.layout(avail);
  
        avail.x += avail.width;
      }
      
    }
    
  } //Row
  
  /**
   * a column
   */
  private static class Column extends Block {
    
    /** constructor */
    Column(Attributes attributes) {
      super(attributes);
    }

    /** add a sub */
    Block add(Block sub) {
      if (sub instanceof Column)
        throw new IllegalArgumentException("column can't contain column");
      super.add(sub);
      return sub;
    }

    /** preferred size */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
    
      // calculate
      preferred = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        preferred.width = Math.max(preferred.width, sub.width);
        preferred.height += sub.height;
      }
    
      // done
      return preferred;
    }
    
    /** weight */
    Point2D weight() {
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = new Point2D.Double();
      for (int i=0;i<subs.size();i++) {
        Point2D sub = ((Block)subs.get(i)).weight();
        weight.x = Math.max(weight.x, sub.getX());
        weight.y += sub.getY();
      }      
      
      // done
      return weight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      // compute spare space vertically
      double weight = 0;
      int spare = in.height;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().height;
        weight += sub.weight().getY();
      }
      double spareOverWeight = weight>0 ? spare/weight : 0;
      
      // loop over subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = in.width;
        avail.height = sub.preferred().height + (int)(sub.weight().getY() * spareOverWeight);
        
        sub.layout(avail);
  
        avail.y += avail.height;
      }
      
    }
    
  } //Column
  
  /**
   * Component
   */
  public static class Cell extends Block {
    
    /** a unique element id */
    private String element;
    
    /** attributes */
    private Map attrs = new HashMap();
    
    /** wrapped component */
    private Component component;
    
    /** grow constraints */
    private Point grow = new Point();
    
    /** padding */
    private int padding;
    
    /** cached weight */
    private Point2D.Double staticWeight = new Point2D.Double();
    
    /** constructor */
    private Cell(String element, Attributes attributes, int padding) {
      
      super(attributes);
      
      // keep key
      this.element = element;
      this.padding = padding;
      
      for (int i=0,j=attributes.getLength();i<j;i++) 
        attrs.put(attributes.getQName(i), attributes.getValue(i));
      
      // look for weight info
      String wx = getAttribute("wx");
      if (wx!=null)
        staticWeight.x = Float.parseFloat(wx);
      String wy = getAttribute("wy");
      if (wy!=null)
        staticWeight.y = Float.parseFloat(wy);
      
      // look for grow info
      String gx = getAttribute("gx");
      if (gx!=null)
        grow.x = 1;
      String gy = getAttribute("gy");
      if (gy!=null)
        grow.y = 1;

      // done
    }
    
    /** cloning */
    protected Object clone()  {
      Cell clone = (Cell)super.clone();
      clone.component = null;
      return clone;
    }
    
    /** set contained content */
    void setContent(Component component) {
      this.component = component;
    }
    
    /** returns nested block layout */
    public Collection getNestedLayouts() {
      ArrayList result = new ArrayList(subs.size());
      for (int i = 0; i < subs.size(); i++) {
        result.add(new NestedBlockLayout((Block)subs.get(i)));
      }
      return result;
    }
    
    /** element */
    public String getElement() {
      return element;
    }
    
    /** Access a cell descriptor attribute */
    public boolean isAttribute(String attr) {
      return attrs.containsKey(attr);
    }
    
    /** attribute */
    public String getAttribute(String attr) {
      return (String)attrs.get(attr);
    }
    
    /** remove */
    boolean remove(Component component) {
      if (this.component==component) {
        this.component = null;
        invalidate(false);
        return true;
      }
      return false;
    }
    
    /** preferred */
    Dimension preferred() {
      // known?
      if (preferred!=null)
        return preferred;
      // calc
      if (component==null||!component.isVisible())
        preferred = new Dimension();
      else {
	      preferred = new Dimension(component.getPreferredSize());
	      preferred.width += padding*2;
	      preferred.height += padding*2;
      }
	    return preferred;
    }
    
    /** weight */
    Point2D weight() {
      return component==null ? new Point2D.Double() : staticWeight;
    }
    
    /** layout */
    void layout(Rectangle in) {
      
      if (component==null)
        return;
      
      // calculate what's available
      Rectangle avail = new Rectangle(in.x+padding, in.y+padding, in.width-padding*2, in.height-padding*2);
      
      // make sure it's not more than maximum
      Dimension pref = preferred();
      Dimension max = component.getMaximumSize();
      if (grow.x!=0) 
        max.width = avail.width;
      else if (staticWeight.x==0) 
        max.width = pref.width;
        
      if (grow.y!=0) 
        max.height = avail.height;
      else if (staticWeight.y==0)
        max.height = pref.height;
      
      // share space
      int extraX = avail.width-max.width;
      if (extraX>0) {
        avail.x += extraX/2;
        avail.width = max.width;
      }
      
      int extraY = avail.height-max.height;
      if (extraY>0) {
        avail.y += extraY/2;
        avail.height = max.height;
      }

      // set it
      component.setBounds(avail);
    }
    
    /** cell by element name */
    Cell getCell(String elem) {
      return ( (elem==null&&component==null) || element.equals(elem)) ? this : null;
    }
    
    /** all cells */
    Collection getCells(Collection collect) {
      collect.add(this);
      return collect;
    }

  } //Cell
  
  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(Component comp, Object key) {

    // a cell?
    if (key instanceof Cell) {
      ((Cell)key).setContent(comp);
      return;
    }
    
    // lookup cell
    Cell cell = root.getCell(key!=null ? key.toString() : null);
    if (cell!=null) {
      cell.setContent(comp);
      return;
    }
  
    // no match
    if (key==null)
      throw new IllegalArgumentException("no available descriptor element - element qualifier required");
    throw new IllegalArgumentException("element qualifier doesn't match any descriptor element");

    // done
  }

  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(String element, Component comp) {
    addLayoutComponent(comp, element);
  }

  /**
   * Component/Layout lifecycle callback
   * @param comp the removed component
   */
  public void removeLayoutComponent(Component comp) {
    root.remove(comp);
  }

  /**
   * Our maximum size isn't limited
   */
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * minimum equals preferred layout size
   */
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  /**
   * Our layout alignment
   */
  public float getLayoutAlignmentX(Container target) {
    return 0;
  }

  /**
   * Our layout alignment
   */
  public float getLayoutAlignmentY(Container target) {
    return 0;
  }

  /**
   * Component/Layout lifecycle callback
   */
  public void invalidateLayout(Container target) {
    if (!invalidated) {
      root.invalidate(true);
      invalidated = true;
    }
  }

  /**
   * our preferred layout size
   */
  public Dimension preferredLayoutSize(Container parent) {
    invalidated = false;
    return root.preferred();
  }
  
  /**
   * Component/Layout lifecycle callback
   */
  public void layoutContainer(Container parent) {
    
    // prepare insets
    Insets insets = parent.getInsets();
    Rectangle in = new Rectangle(
      insets.left,
      insets.top,
      parent.getWidth()-insets.left-insets.right,
      parent.getHeight()-insets.top-insets.bottom
    );
    // layout
    root.layout(in);
    // remember
    invalidated = false;
  }
  
  /**
   * Create a private copy
   */
  public NestedBlockLayout copy() {
    try {
      NestedBlockLayout clone = (NestedBlockLayout)super.clone();
      clone.root = (Block)clone.root.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

} //ColumnLayout