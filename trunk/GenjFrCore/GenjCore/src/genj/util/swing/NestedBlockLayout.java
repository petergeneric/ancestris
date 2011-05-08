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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A layout that arranges components in nested blocks of rows and columns
 * <pre>
 * <!ELEMENT row (col*|T*)>
 * <!ELEMENT col (row*|T*)>
 * <!ELEMENT table (row*|T*)>
 * <!ELEMENT T>
 * <!ATTLIST col drawer CDATA>
 * <!ATTLIST T wx CDATA>
 * <!ATTLIST T wy CDATA>
 * <!ATTLIST T gx CDATA>
 * <!ATTLIST T gy CDATA>
 * <!ATTLIST T ax CDATA>
 * <!ATTLIST T ay CDATA>
 * </pre>
 * wx,wy are weight arguments - gx,gy are grow arguments
 * 
 * examples:
 * <pre> [col][row][A][B][C][/row][row][D][E][/row]F[/col]
 *  +----------+
 *  | A B C    |
 *  | DDDDDD E |
 *  | FFFFFFFF |
 *  +----------+
 * </pre> 
 *  
 * <pre> [row][A][col][row]BC[/row]EF[/col][/row]
 *  +----------+
 *  | A B C    |
 *  | A EEEE   |
 *  | A FFFFFF |
 *  +----------+
 * </pre> 
 *  
 * <pre> [table][row][A][B][C][/row][row][D][E][/row][row][F][G][H][/row][/table]
 *  +----------+
 *  | A B    C |
 *  | D EEEE   |
 *  | F GG   H |
 *  +----------+
 * </pre> 
 * 
 * Note: table doesn't support the notion of span yet
 */
public class NestedBlockLayout implements LayoutManager2, Cloneable {
  
  private final static SAXException DONE = new SAXException("");
  private final static SAXParser PARSER = getSaxParser();
  
  private final static SAXParser getSaxParser() {
    try {
      return SAXParserFactory.newInstance().newSAXParser();
    } catch (Throwable t) {
      Logger.getLogger("genj.util.swing").log(Level.SEVERE, "Can't initialize SAX parser", t);
      throw new Error("Can't initialize SAX parser", t);
    }
  }
  
  private final static Logger LOG = Logger.getLogger("genj.util");

  /** whether we've been invalidated recently */
  private boolean invalidated = true;
  
  /** one root row is holds all the columns */
  private Block root;
  private Set<Component> components = new HashSet<Component>();
  
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
  public Collection<Cell> getCells() {
    return root.getCells(new ArrayList<Cell>(10));
  }
  
  /**
   * Post Constructor Initializer
   */
  private void init(Reader descriptor) throws IOException {
    
    // parse descriptor
    try {
	    PARSER.parse(new InputSource(descriptor), new DescriptorHandler());
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
    
    private Stack<Block> stack = new Stack<Block>();
    
    public InputSource resolveEntity(String publicId, String systemId) {
      // 20060601 let's not try to resolve any external entities - in case of GenJ running as an applet and a 
      // webserver returning a custom 404 spmeone might read a layout string from getResourceAsStream()
      // which doesn't return null but returns a custom page that we can't parse
      throw new IllegalArgumentException("Request for resolveEntity "+publicId+"/"+systemId+" not allowed in layout descriptor");
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      
      boolean startsWithSpace = Character.isWhitespace(ch[start]);
      boolean endsWithSpace = Character.isWhitespace(ch[start+length-1]);
      
      // trim
      while (length>0 && Character.isWhitespace(ch[start])) { start++; length--; }
      while (length>0 && Character.isWhitespace(ch[start+length-1])) length--;
      if (length==0)
        return;
      
      // add
      if (startsWithSpace) { start--; length++; }
      if (endsWithSpace) { length++; }
      String s = new String(ch,start,length);
            
      Block parent = (Block)stack.peek();
      parent.add(new Cell(s));
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
      // table?
      if ("table".equals(element))
        return new Table(attrs);
      // a cell!
      return new Cell(element, attrs);
    }
    
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      
      // check
      if (stack==null||stack.size()==0)
        throw new SAXException("unexpected /element");

      // pop last
      stack.pop();
      
      // are we done?
      if (stack.isEmpty())
        throw DONE;
    }
    
  };

  /**
   * a block in the layout
   */
  private static abstract class Block implements Cloneable {
    
    Insets padding = this instanceof Cell ? new Insets(1,1,1,1) : new Insets(0,0,0,0);
    
    /** preferred size of column */
    private Dimension preferred;

    /** weight/growth */
    Point weight;
    Point grow;
    int cols = 1;
    
    Block(Attributes attributes) {
      
      grow = new Point();
      
      // additional info?
      if (attributes==null)
        return;
      
      // look for cols info
      String c = attributes.getValue("cols");
      if (c!=null) {
        cols = Integer.parseInt(c);
        if (cols<=0)
          throw new IllegalArgumentException("cols<=0");
      }      
      
      // look for grow info
      String gx = attributes.getValue("gx");
      if (gx!=null)
        grow.x = Integer.parseInt(gx)>0 ? 1 : 0;
      String gy = attributes.getValue("gy");
      if (gy!=null)
        grow.y = Integer.parseInt(gy)>0 ? 1 : 0;
      
      // look for padding
      String pad = attributes.getValue("pad");
      if (pad!=null) {
        String[] pads = pad.split(",");
        switch (pads.length) {
        case 0: break;
        case 1: padding.set(Integer.parseInt(pads[0]), Integer.parseInt(pads[0]), Integer.parseInt(pads[0]), Integer.parseInt(pads[0])); break;
        case 2: padding.set(Integer.parseInt(pads[0]), Integer.parseInt(pads[1]), Integer.parseInt(pads[0]), Integer.parseInt(pads[1])); break;
        case 3: padding.set(Integer.parseInt(pads[0]), Integer.parseInt(pads[1]), Integer.parseInt(pads[2]), Integer.parseInt(pads[1])); break;
        case 4: padding.set(Integer.parseInt(pads[0]), Integer.parseInt(pads[1]), Integer.parseInt(pads[2]), Integer.parseInt(pads[3])); break;
        default: 
          throw new IllegalArgumentException("invalid padding "+pad+" given ("+attributes+")");
        }
      }
    }

    /** copy */
    protected Block clone() {
      try {
        return (Block)super.clone();
      } catch (CloneNotSupportedException cnse) {
        throw new Error();
      }
    }
    
    /** remove */
    abstract boolean removeContent(Component component);

    /** add sub */
    abstract Block add(Block block);
    
    /** invalidate state */
    void invalidate(boolean recurse) {
      // clear state
      preferred = null;
      weight = null;
    }
    
    /** weight */
    abstract Point weight();
    
    /** grow within parent */
    Point grow() {
      return grow;
    }
    
    /** preferred size */
    final Dimension preferred() {
      if (preferred==null) {
        preferred = preferredImpl();
        if (preferred.width>0&&preferred.height>0) {
          preferred.width += padding.left + padding.right;
          preferred.height += padding.top+ padding.bottom;
        }        
      }
      return preferred;
    }
    
    abstract Dimension preferredImpl();
      
    /** layout */
    final void layout(Rectangle in) {
      Rectangle avail = new Rectangle(in);
      avail.x += padding.left;
      avail.width -= padding.left+padding.right;
      avail.y += padding.top;
      avail.height -= padding.top+padding.bottom;
      layoutImpl(avail);
    }
    
    abstract void layoutImpl(Rectangle in);
    
    /** all cells */
    abstract Collection<Cell> getCells(Collection<Cell> collect);
    
    /** set cell content */
    abstract List<Block> setContent(Object key, Component component, List<Block> path);    
    
    @Override
    public String toString() {
      StringBuffer result = new StringBuffer();
      toString(result);
      return result.toString();
    }
    
    protected abstract void toString(StringBuffer result);
    
  } //Block
  
  /**
   * a row
   */
  private static class Row extends Folder {

    Row(Attributes attr) {
      super(attr);
    }
    
    /** preferred size */
    Dimension preferredFolder() {
      Dimension result = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        result.width += sub.width;
        result.height = Math.max(result.height, sub.height);
      }
      return result;
    }
    
    /** weight */
    @Override
    Point weightFolder() {
      Point result = new Point();
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        result.x += sub.weight().x;
        result.y = Math.max(result.y, sub.weight().y);
      }      
      return result;
    }
    
    /** layout */
    @Override
    void layoutFolder(Rectangle in) {
      
      // compute spare space horizontally
      double weight = 0;
      int grow = 0;
      int spare = in.width;
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        spare -= sub.preferred().width;
        weight += sub.weight().getX();
        grow += sub.grow().x;
      }
      double weightFactor = weight>0 ? spare/weight : 0;
      int growFactor = weightFactor==0 && grow>0 ? spare/grow : 0;
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        
        avail.width = sub.preferred().width + (int)(sub.weight().getX() * weightFactor) + (sub.grow().x*growFactor);
        avail.height = in.height;

        sub.layout(avail);
  
        avail.x += avail.width;
      }
      
    }

  } //Row
  
  /**
   * a container
   */
  private static abstract class Folder extends Block {
    
    protected transient Expander expander = null;
    
    /** subs */
    ArrayList<Block> subs = new ArrayList<Block>(16);
    
    /** constructor */
    protected Folder(Attributes attr) {
      super(attr);
    }
    
    /** add sub */
    Block add(Block block) {
      subs.add(block);
      invalidate(false);
      return block;
    }
    
    protected void toString(StringBuffer result) {
      result.append("<"+getClass().getSimpleName()+">");
      for (int i=0;i<subs.size();i++)
        subs.get(i).toString(result);
      result.append("</"+getClass().getSimpleName()+">");
    }
    
    /** copy */
    protected Folder clone() {
      Folder clone = (Folder)super.clone();
      clone.subs = new ArrayList<Block>(subs.size());
      for (int i=0;i<subs.size();i++)
        clone.subs.add( (Block)subs.get(i).clone() );
      return clone;
    }
    
    /** remove */
    boolean removeContent(Component component) {

      if (expander==component)
        expander=null;
      
      // look for it
      for (int i=0;i<subs.size();i++) {
        Block sub = (Block)subs.get(i);
        if (sub.removeContent(component)) {
          invalidate(false);
          return true;
        }
      }
      
      // not found
      return false;
      
    }
    
    /** invalidate state */
    void invalidate(boolean recurse) {
      super.invalidate(recurse);
      // recurse
      if (recurse) for (int i=0;i<subs.size();i++) {
        ((Block)subs.get(i)).invalidate(true);
      }
    }
    
    /** all cells */
    Collection<Cell> getCells(Collection<Cell> collect) {
      for (int i=0;i<subs.size();i++) 
        ((Block)subs.get(i)).getCells(collect);
      return collect;
    }
    
    /** set cell content */
    List<Block> setContent(Object key, Component component, List<Block> path) {
      
      int lastKeyMatch = -1;
      
      // look for it in our subs
      for (int i=0; i<subs.size(); i++) {
        
        Block sub = subs.get(i);

        if (sub instanceof Cell && key instanceof String && key.equals(((Cell)sub).element)) 
          lastKeyMatch = i;

        // try to set
        if (!sub.setContent(key, component, path).isEmpty()) {
          path.add(this);
          if (component instanceof Expander) {
            int indent = ((Expander)component).getIndent();
            if (indent<path.size() && path.get(indent)==this)
              expander = (Expander)component;
          }
          return path;
        }
        
      }

      // last chance fallback case - clone cell?
      if (lastKeyMatch>=0) {
        Block clone = subs.get(lastKeyMatch).clone();
        subs.add(lastKeyMatch+1, clone);
        clone.setContent(key, component, path);
        path.add(this);
        return path;
      }

      // not found
      return path;
    }

    /** preferred size */
    final Dimension preferredImpl() {
      if (expander!=null&&expander.isCollapsed)
        return expander.getPreferredSize();
      // known?
      return preferredFolder();
    }
    
    abstract Dimension preferredFolder();
      
    final Point grow() {
      return expander!=null&&expander.isCollapsed ? new Point() : grow;
    }
    
    /** weight */
    final Point weight() {
      
      if (expander!=null&&expander.isCollapsed)
        return new Point();
      
      // known?
      if (weight!=null)
        return weight;
      
      // calculate
      weight = weightFolder();
      
      // done
      return weight;
    }
    
    abstract Point weightFolder();
    
    /** layout */
    final void layoutImpl(Rectangle in) {

      // closed?
      if (expander!=null&&expander.isCollapsed) {
        for (Block sub : subs) 
          sub.layout(new Rectangle(0,0));
        Dimension d = expander.getPreferredSize();
        expander.setBounds(in.x,in.y,d.width,d.height);
        return;
      }

      layoutFolder(in);
    }
    
    abstract void layoutFolder(Rectangle in);
  }
  
  /**
   * a column
   */
  private static class Column extends Folder {
    
    Column(Attributes attr) {
      super(attr);
    }
    
    /** preferred size */
    @Override
    Dimension preferredFolder() {
      Dimension result = new Dimension();
      for (int i=0;i<subs.size();i++) {
        Dimension sub = ((Block)subs.get(i)).preferred();
        result.width = Math.max(result.width, sub.width);
        result.height += sub.height;
      }
      return result;
    }
    
    /** weight */
    @Override
    Point weightFolder() {
      Point result = new Point();
      for (int i=0;i<subs.size();i++) {
        Point sub = ((Block)subs.get(i)).weight();
        result.x = Math.max(result.x, sub.x);
        result.y += sub.y;
      }      
      return result;
    }
    
    /** layout */
    @Override
    void layoutFolder(Rectangle in) {

      // compute spare space vertically
      double weight = 0;
      int spare = in.height;
      int grow = 0;
      for (Block sub : subs) {
        spare -= sub.preferred().height;
        weight += sub.weight().getY();
        grow += sub.grow().y;
      }
      double weightFactor = weight>0 ? spare/weight : 0;
      int growFactor = weightFactor==0 && grow>0 ? spare/grow : 0;
      
      // loop over subs
      Rectangle avail = new Rectangle(in.x, in.y, 0, 0);
      for (int i=0;i<subs.size();i++) {
        
        Block sub = (Block)subs.get(i);
        avail.x = in.x;
        avail.width = in.width;
        avail.height = sub.preferred().height + (int)(sub.weight().getY() * weightFactor) + (sub.grow().y*growFactor);
        
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
    private Map<String,String> attrs = new HashMap<String, String>();
    
    /** wrapped component */
    private Component component;
    
    /** cached weight */
    private Point cellWeight = new Point();
    
    /** cached alignment */
    private Point2D.Double cellAlign = new Point2D.Double(0,0.5);

    /** constructor */
    private Cell(String text) {
      super(null);
      this.element = "text";
      attrs.put("value", text);
    }
    
    /** constructor */
    private Cell(String element, Attributes attributes) {
      
      super(attributes);
      
      // keep key
      this.element = element;
      
      for (int i=0,j=attributes.getLength();i<j;i++) 
        attrs.put(attributes.getQName(i), attributes.getValue(i));
      
      // look for weight info
      String wx = getAttribute("wx");
      if (wx!=null) {
        cellWeight.x = Integer.parseInt(wx);
        if (attributes.getValue("gx")==null)
          grow.x = 1;
      }
      String wy = getAttribute("wy");
      if (wy!=null) {
        cellWeight.y = Integer.parseInt(wy);
        if (attributes.getValue("gy")==null)
          grow.y = 1;
      }
      
      // look for alignment info
      String ax = getAttribute("ax");
      if (ax!=null)
        cellAlign.x = Float.parseFloat(ax);
      String ay = getAttribute("ay");
      if (ay!=null)
        cellAlign.y = Float.parseFloat(ay);

      // done
    }
    
    /** cloning */
    protected Block clone()  {
      Cell clone = (Cell)super.clone();
      clone.component = null;
      return clone;
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
    boolean removeContent(Component component) {
      if (this.component==component) {
        this.component = null;
        invalidate(false);
        return true;
      }
      return false;
    }
    
    /** preferred */
    Dimension preferredImpl() {
      // calc
      if (component==null||!component.isVisible())
        return new Dimension();
      Dimension result = new Dimension(component.getPreferredSize());
      Dimension max = component.getMaximumSize();
      result.width = Math.min(max.width, result.width);
      result.height = Math.min(max.height, result.height);
      return result;
    }
    
    @Override
    protected void toString(StringBuffer result) {
      result.append("<cell ");
      result.append(attrs);
      result.append("/>");
    }
    
    /** weight */
    @Override
    Point weight() {
      return component==null ? new Point() : cellWeight;
    }
    
    /** layout */
    void layoutImpl(Rectangle in) {
      
      if (component==null)
        return;
      
      // calculate what's available
      Rectangle avail = new Rectangle(in.x, in.y, in.width, in.height);
      
      // make sure it's not more than maximum
      Dimension max = component.getMaximumSize();
      if (avail.width>max.width) {
        int extraX = avail.width-max.width;
        avail.x += extraX * cellAlign.x;
        avail.width = max.width;
      }
      if (avail.height>max.height) {
        int extraY = avail.height-max.height;
        avail.y += extraY * cellAlign.y;
        avail.height = max.height;
      }

      // set it
      component.setBounds(avail);
    }
    
    /** set cell content*/
    @Override
    List<Block> setContent(Object key, Component component, List<Block> path) {
      
      if (  (key instanceof Cell&&key!=this)
         || (key instanceof String&&(!element.equals(key)||this.component!=null))
         || (key==null&&this.component!=null))
        return path;
      
      if (this.component!=null)
        throw new IllegalArgumentException("can't set component twice");
      
      this.component = component;
      path.add(this);
      
      return path;
    }
    
    /** all cells */
    Collection<Cell> getCells(Collection<Cell> collect) {
      collect.add(this);
      return collect;
    }

    @Override
    Block add(Block block) {
      throw new IllegalArgumentException("cell.add() not supported");
    }

  } //Cell
  
  /**
   * Component/Layout lifecycle callback
   */
  public void addLayoutComponent(Component comp, Object key) {
    
    if (components.contains(comp))
      throw new IllegalArgumentException("already added");

    List<Block> path = root.setContent(key, comp, new ArrayList<Block>());
    if (!path.isEmpty()) {
      components.add(comp);
      return;
    }
    
    // no match
    if (key==null)
      throw new IllegalArgumentException("no available descriptor element - element qualifier required");
    throw new IllegalArgumentException("element qualifier doesn't match any descriptor element");
    
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
    root.removeContent(comp);
    components.remove(comp);
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
      
      // pickup any leftover components from a layout switch
      for (Component c : target.getComponents()) 
        if (!components.contains(c))
          addLayoutComponent(c, null);
    }
  }

  /**
   * our preferred layout size
   */
  public Dimension preferredLayoutSize(Container parent) {
    Dimension result = root.preferred();
    Insets insets = parent.getInsets();
    result.width += insets.left + insets.right;
    result.height += insets.top + insets.bottom;
    return result;
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
      clone.components = new HashSet<Component>();
      clone.invalidated = false;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
  
  /**
   * a table
   */
  private static class Table extends Folder {

    private ArrayList<Integer> rowHeights;
    private ArrayList<Integer> colWidths;
    private ArrayList<Integer> rowWeights;
    private ArrayList<Integer> colWeights;
    
    Table(Attributes attrs) {
      super(attrs);
    }
    
    private void calcGrid() {
      
      if (rowHeights!=null)
        return;
      
      rowHeights = new ArrayList<Integer>();
      colWidths = new ArrayList<Integer>();
      rowWeights = new ArrayList<Integer>();
      colWeights = new ArrayList<Integer>();
      
      // do one run for single col cells
      for (int r=0;r<subs.size();r++) {
        Block row = subs.get(r);
        
        List<Block> cells;
        
        if (row instanceof Row) 
          cells = ((Row)row).subs;
        else
          cells = Collections.singletonList(row);
        
        Block cell;
        for (int c=0;c<cells.size();c += cell.cols) {
          cell = cells.get(c);
          Dimension d = cell.preferred();
          if (cell.cols==1) 
            grow(colWidths, c, d.width);
          grow(rowHeights, r, d.height);
          Point w = cell.weight();
          if (cell.cols==1)
            grow(colWeights, c, w.x);
          grow(rowWeights, r, w.y);
        }
      }
      
      // do another run for multi col cells
      for (int r=0;r<subs.size();r++) {
        Block row = subs.get(r);
        List<Block> cells;
        if (row instanceof Row) 
          cells = ((Row)row).subs;
        else
          cells = Collections.singletonList(row);
          
        Block cell;
        for (int c=0;c<cells.size();c += cell.cols) {
          cell = cells.get(c);
          Dimension d = cell.preferred();
          if (cell.cols==1)
            continue;
          
          int spannedWeight = 0;
          int spannedWidth = 0;
          if (c+cell.cols>colWidths.size())
            throw new IllegalArgumentException("cols out of bounds for "+cell);
          for (int j=0;j<cell.cols;j++) {
            spannedWidth += colWidths.get(c+j);
            spannedWeight += colWeights.get(c+j);
          }
          
          // increase spanned cells equally (plus fudge factor on first column)
          if (spannedWidth<d.width) {
            int missing = d.width-spannedWidth;
            for (int j=0;j<cell.cols;j++) {
              int share = spannedWeight>0 ? missing*colWeights.get(c+j)/spannedWeight : missing/cell.cols;
              grow( colWidths, c+j, colWidths.get(c+j) + share );
            }
          }
          
        }
      }
      
      // done
    }
    
    @Override
    void layoutFolder(Rectangle in) {

      // calculate preferred grid & size
      Dimension preferred = preferred();
      calcGrid();
      
      // calculate extras
      float xWeightMultiplier = 0;
      if (in.width>preferred.width) {
        int w = 0;
        for (int i=0;i<colWeights.size();i++)
          w += colWeights.get(i);
        xWeightMultiplier = (in.width-preferred.width)/(float)w;
      }
      float yWeightMultiplier = 0;
      if (in.height>preferred.height) {
        int h = 0;
        for (int i=0;i<rowWeights.size();i++)
          h += rowWeights.get(i);
        yWeightMultiplier = (in.height-preferred.height)/(float)h;
      }
      
      // layout subs
      Rectangle avail = new Rectangle(in.x, in.y, in.width, in.height);
      for (int r=0;r<subs.size();r++) {
        
        Block row = subs.get(r);
        
        List<Block> cells;
        
        if (row instanceof Row) 
          cells = ((Row)row).subs;
        else
          cells = Collections.singletonList(row);
        
        int x = avail.x;
        int h = rowHeights.get(r) + (int)(rowWeights.get(r)*yWeightMultiplier);
        Block sub;
        for (int c=0;c<cells.size();) {
          sub = cells.get(c);
          int w = 0;
          for (int i=0;i<sub.cols;i++,c++) 
            w += colWidths.get(c) + (int)(colWeights.get(c)*xWeightMultiplier);
          w = Math.min( avail.x+avail.width-x, w);
          sub.layout(new Rectangle(x, avail.y, w, h));
          x += w;
        }
        
        // next row
        avail.y += h;
      }

      // done
    }
    
    private void grow(ArrayList<Integer> values, int i, Integer value) {
      while (values.size()<i+1)
        values.add(0);
      values.set(i, Math.max(values.get(i), value));
    }
    
    @Override
    Dimension preferredFolder() {
      
      // calculate preferred grid
      calcGrid();
      
      // add it up
      Dimension result = new Dimension(0,0);
      for (int c=0;c<colWidths.size();c++)
        result.width += colWidths.get(c);
      for (int r=0;r<rowHeights.size();r++)
        result.height += rowHeights.get(r);
      
      // done
      return result;
    }

    @Override
    Point weightFolder() {
      
      // calculate preferred grid
      calcGrid();
      
      // add it up
      Point result = new Point(0,0);
      for (int c=0;c<colWeights.size();c++)
        result.x += colWeights.get(c);
      for (int r=0;r<rowWeights.size();r++)
        result.y += rowWeights.get(r);
      
      // done
      return result;
    }
    
    @Override
    void invalidate(boolean recurse) {
      super.invalidate(recurse);
      
      rowHeights = null;
      colWidths = null;
      rowWeights = null;
      colWeights = null;
    }
  }
  
  /**
   * A widget that expands and collapse containing blocks
   */
  public static class Expander extends JLabel {
    
    private String expandedLabel, collapsedLabel;
    private final static Icon FOLDED = GraphicsHelper.getIcon(8, collapsed(8));
    private final static Icon UNFOLDED = GraphicsHelper.getIcon(8, open(8));
    private boolean isCollapsed = false;
    private int indent = 1;
    
    private static Shape collapsed(int size) {
      GeneralPath shape = new GeneralPath();
      shape.moveTo(size/4, 0);
      shape.lineTo(size/4, size+1);
      shape.lineTo(size*3/4, size/2);
      shape.closePath();
      return shape;
    }
    
    private static Shape open(int size) {
      GeneralPath shape = new GeneralPath();
      shape.moveTo(0, size/4);
      shape.lineTo(size, size/4);
      shape.lineTo(size/2, size*3/4);
      shape.closePath();
      return shape;
    }
    
    /**
     * Constructor
     */
    public Expander(String expandedLabel, String collapsedLabel) {
      this(expandedLabel, collapsedLabel, 1);
    }
    
    /**
     * Constructor
     */
    public Expander(String label) {
      this(label, 1);
    }
    
    /**
     * Constructor
     */
    public Expander(String label, int indent) {
      this(label, label, indent);
      
    }
    /**
     * Constructor
     */
    public Expander(String expandedLabel, String collapsedLabel, int indent) {
      super(expandedLabel);
      this.collapsedLabel = collapsedLabel;
      this.expandedLabel = expandedLabel;
      this.indent = Math.max(1, indent);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new Mouser());
    }
    
    public int getIndent() {
      return indent;
    }
    
    public void setCollapsed(boolean set) {
      isCollapsed = set;
      setText(isCollapsed ? collapsedLabel : expandedLabel);
    }
    
    public boolean isCollapsed() {
      return isCollapsed;
    }

    public Icon getIcon() {
      return isCollapsed ? FOLDED : UNFOLDED;
    }
    
    private class Mouser extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
        
        setCollapsed(!isCollapsed);
        
        firePropertyChange("folded", !isCollapsed, isCollapsed);

        Component parent = getParent();
        if (parent instanceof JComponent)
          ((JComponent)parent).revalidate();
        else {
          parent.invalidate();
          parent.validate();
        }
        
        // done
      }
    }
    
  }
  
} //ColumnLayout