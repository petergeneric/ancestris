/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.renderer;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.util.Dimension2d;
import genj.util.EnvironmentChecker;
import genj.util.swing.ImageIcon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * A renderer for entities - blueprint necessary
 */
public class BlueprintRenderer {

  private final static ImageIcon BROKEN = new ImageIcon(BlueprintEditor.class, "Broken.png");
  
  private final static Logger LOG = Logger.getLogger("genj.renderer");
  
  public static final String HINT_KEY_TXT = "txt";
  public static final String HINT_KEY_IMG = "img";
  public static final String HINT_KEY_SHORT = "short";

  public static final String HINT_VALUE_TRUE = "yes";
  public static final String HINT_VALUE_FALSE = "no";
  
  private final static String STARS = "*****";
  
  private final static int IMAGE_GAP = 4;
  
  private final static Stroke DEBUG_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{ 1,2 }, 0.0f);
  
  // this will initialize and load the html32 dtd
  // "/javax/swing/text/html/parser/html32.bdtd"
  static {
    new ParserDelegator();
  }
  
  /** the property image width */
  private static final int 
    PROP_IMAGE_WIDTH  = Indi.IMG_MALE.getIconWidth()+4,
    PROP_IMAGE_HEIGHT = Indi.IMG_MALE.getIconHeight();
  
  /** a no value char array */
  private static final Segment EMPTY_SEGMENT = new Segment(); 
  
  /** the root of our rendering view */
  private RootView root;
  
  /** the document we're looking at */
  private HTMLDocument doc = new MyHTMLDocument();
  
  /** the factory we're using to create views */
  private MyHTMLFactory factory = new MyHTMLFactory();
  
  /** a cached dtd for html */
  private static DTD dtd = null;
  
  /** the entity we're looking at */
  private Entity entity;
  
  /** all views that need to be invalidated per repaint */
  private List<MyView> volatileViews = new ArrayList<MyView>(64);
  
  /** all TableViews we know */
  private List<View> tableViews = new ArrayList<View>(4);
  
  /** whether we have a debug mode */
  private boolean isDebug = false;
  
  /** current graphics context */
  private Graphics2D graphics;
  
  private Font plain,bold,italic;
  
  /**
   * Constructor
   */  
  public BlueprintRenderer(Blueprint bp) {
    
    // we wrap the html in html/body
    StringBuffer html = new StringBuffer();
    html.append("<html<body>");
    html.append(bp.getHTML());
    html.append("</body></html>");

    // read and parse the html
    try {
      
      // I started out to use a HTMLEditorkit for reading the html into the document
      //  new HTMLEditorKit().read(new StringReader(html), doc, 0);
      // but this won't allow me to fix a problem with HTMLDocument.HTMLReader 
      // so I'm using ParserDelegator directly allowing me to use MyHTMLReader
      //  new ParserDelegator().parse(new StringReader(html), new HTMLReader(doc), false);
      // but I also want to override the DocumentParser which is instantiated
      // inside ParserDelegator.
      //  new DocumentParser(dtd).parse(r, cb, ignoreCharSet);
      // so I'm reading the dtd myself and creat my own DocumentParser
      
      // .. we need out own html reader (javax.swing.text.html.HTMLDocument.HTMLReader)
      MyHTMLReader reader = new MyHTMLReader(doc);
      
      // .. trigger parsing (we want our own parser here so there's a little bit more magic at this point)
      new MyDocumentParser(DTD.getDTD("html32")).parse(new StringReader(html.toString()), reader, false);
      
      // .. flush reader
      reader.flush();      
      
    } catch (Throwable t) {
      Logger.getLogger("genj.renderer").log(Level.WARNING, "can't parse blueprint "+bp, t);
    }

    // create the root view
    root = new RootView(factory.create(doc.getDefaultRootElement()));

    // done    
  }
  
  /**
   * Render the entity on given context
   */
  public void render(Graphics g, Entity e, Rectangle r) {

    // keep the entity and graphics
    entity = e;
    graphics = (Graphics2D)g;
    
    // see http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html
    // While Java assumes 72 dpi screen resolution Windows uses 96 dpi or 120 dpi depending on your font size setting in the display properties. 
    Font font = g.getFont();
    if (!EnvironmentChecker.isMac()) {
      float factor = DPI.get(graphics).vertical()/72F; 
      font = font.deriveFont(factor*font.getSize2D());
    }
    this.plain = font;
    this.bold = font.deriveFont(Font.BOLD);
    this.italic = font.deriveFont(Font.ITALIC);

    try {
      
      // invalidate views 
      for (MyView pv : volatileViews) 
        pv.invalidate();
      
      // and make sure TableView's update their grid
      for (View tv : tableViews) {
        // this will cause invalidateGrid on a javax.swing.text.html.TableView
        tv.replace(0,0,null);
      }
      
      // set the size of root - this triggers a layout of the views
      root.setSize((float)r.getWidth(),(float)r.getHeight());
      
      // clip and paint it
      Rectangle oc = g.getClipBounds();
      g.clipRect(r.x,r.y,r.width,r.height);
      try {
        root.paint(g, r);
      } finally {
        g.setClip(oc.x,oc.y,oc.width,oc.height);
      }

    } catch (Throwable t) {
      LOG.log(Level.WARNING, "can't render", t);
    }
    // done
  }
  
  /**
   * Sets debug mode 
   */
  public void setDebug(boolean set) {
    isDebug = set;
  }
  
  /**
   * Default implementation to lookup property from entity
   */
  protected Property getProperty(Entity entity, TagPath path) {
    return entity.getProperty(path);
  }
  
  /**
   * Our own HTMLDocument
   */  
  private class MyHTMLDocument extends HTMLDocument {
    
    /**
     * @see javax.swing.text.DefaultStyledDocument#getFont(javax.swing.text.AttributeSet)
     */
    public Font getFont(AttributeSet attr) {
      
      Font result = plain;
      if (StyleConstants.isBold(attr)) 
        result = bold;
      else if (StyleConstants.isItalic(attr))
        result = italic;
      return result;
    }
  } //MyHTMLDocument
  
  /**
   * My own parser that overrides the original's property 
   * <pre>strict=true</pre> - this will make the underlying parser 
   * not skip spaces after close-tags
   */
  private class MyDocumentParser extends DocumentParser {
    /**
     * Constructor
     */
    private MyDocumentParser(DTD dtd) {
      super(dtd);
      // patch strictness
      strict = true;
      // done      
    }
  } //MyDocumentParser

  /**
   * I've created my own subclass of HTMLDocument.HTMLReader a ParserCallback
   * to achieve some special behaviour overriding protected method:
   * @see HTMLDocument.HTMLReader.blockClose(Tag t)
   */
  private static class MyHTMLReader extends HTMLDocument.HTMLReader {
    /** whether we ignore content */
    private boolean skipContent = false;
    /**
     * Constructor
     */
    protected MyHTMLReader(HTMLDocument doc) {
      doc.super(0);
    }
    /**
     * In the original HTMLReader this will add a newline to the end
     * of a block (if there was no newline already). For tables this
     * means that there will be a InlineView (\n) that might flow into
     * a separate *empty* line 
     * <pre>
     *  if(!lastWasNewline) {
     *   addContent(NEWLINE, 0, 1, true);
     *   lastWasNewline = true;
     *  }
     * </pre>
     * 
     * @see javax.swing.text.html.HTMLDocument.HTMLReader#blockClose(javax.swing.text.html.HTML.Tag)
     */
    protected void blockClose(Tag t) {
      // mark that we skip anything that might be added to content 
      // in super.blockClose(). The super class' implementation
      // adds trailing \n's before a block-close. They tend to
      // flow into the next line resulting in empty full-height
      // lines when horizontal space is restricted :(
      skipContent = true;
      // delegate to super
      super.blockClose(t);
      // back to accepting content
      skipContent = false;
    }
    /**
     * @see javax.swing.text.html.HTMLDocument.HTMLReader#addContent(char, int, int, boolean)
     */
    protected void addContent(char[] data, int offs, int length, boolean generateImpliedPIfNecessary) {
      if (!skipContent) super.addContent(data, offs, length, generateImpliedPIfNecessary);
    }
    
  } //MyHTMLReader
  
  /**
   * My own HTMLFactory that extends the default one to support
   * tags <i>prop</i> and <i>i18n</i>
   */
  private class MyHTMLFactory extends HTMLFactory {
    
    /**
     * @see javax.swing.text.ViewFactory#create(Element)
     */
    public View create(Element elem) {
      
      String name = elem.getName();

      // check if the element is "prop"
      if ("prop".equals(name)) {
        PropertyView result = new PropertyView(elem);
        volatileViews.add(result);
        return result;
        
      }
      
      // maybe its "name" or "i18n"
      if ("name".equals(name)||"i18n".equals(name)) {
        return new I18NView(elem);
      }
      
      // a media item?
      if ("media".equals(name)) {
        MediaView result = new MediaView(elem);
        volatileViews.add(result);
        return result;
      }
        
      // default to super
      View result = super.create(elem);

      // .. keep track of TableViews for later dynamic invalidation
      if ("table".equals(elem.getName())) {
        tableViews.add(result);
      }
      return result;
    }
  
  } //ModifiedHTMLFactory
  
  /**
   * 
   */
  private abstract class MyView extends View {
  
    /** the cached font we're using */
    private Font font = null;
    
    /** the cached foreground we're using */
    private Color foreground = null;
    
    /** the cached preferred span */
    private Dimension2D preferredSpan = null;

    /** max span percent 0-100 */
    private int max = 0;
    
    /**
     * Constructor
     */
    MyView(Element elem) {
      super(elem);
      
      // minimum?
      try {
        max = Integer.parseInt((String)elem.getAttributes().getAttribute("max"));
      } catch (Throwable t) {
      }
    }

    /**
     * @see javax.swing.text.View#viewToModel(float, float, Shape, Bias[])
     */
    public int viewToModel(float arg0, float arg1, Shape arg2, Bias[] arg3) {
      throw new RuntimeException("viewToModel() is not supported");
    }
    /**
     * @see javax.swing.text.View#modelToView(int, Shape, Bias)
     */
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
      throw new RuntimeException("modelToView() is not supported");
    }
 
    /**
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    public int getBreakWeight(int axis, float pos, float len) {
      // not on vertical
      if (axis==Y_AXIS) return BadBreakWeight;
      // horizontal might work after our content
      if (len > getPreferredSpan(X_AXIS)) {
        return GoodBreakWeight;
      }
      return BadBreakWeight;
    }  
    /**
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    public View breakView(int axis, int offset, float pos, float len) {
      return this;
    }
    
    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis) {
      // check cached preferred Span
      if (preferredSpan==null) {
        preferredSpan = getPreferredSpan();
        
        if (max>0) {
          double maxWidth = root.width*max/100;
          if (preferredSpan.getWidth()>maxWidth)
            preferredSpan = new Dimension2d(maxWidth, preferredSpan.getHeight() * maxWidth/preferredSpan.getWidth());
        }        
      }
      return (float)(axis==X_AXIS ? preferredSpan.getWidth() : preferredSpan.getHeight());
    }
    
    @Override
    public float getMinimumSpan(int axis) {
      return getPreferredSpan(axis);
    }
    
    /**
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis) {
      return getPreferredSpan(axis);
    }

    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
      // horizontal unchanged
      if (X_AXIS==axis) 
        return super.getAlignment(axis);
      // height we prefer
      float height = (float)getPreferredSpan().getHeight();
      // where's first line's baseline
      FontMetrics fm = getGraphics().getFontMetrics();
      float h = fm.getHeight();
      float d = fm.getDescent();
      return (h-d)/height;
    }
    
    @Override
    public Graphics getGraphics() {
      graphics.setFont(getFont());
      return graphics;
    }
    
    /** 
     * Returns the current fg color
     */
    protected Color getForeground() {
  
      // we cached the color so that it's retrieved only once
      // instead of using this view's attributes we get the
      // document's stylesheet and ask it for this view's
      // attributes
      if (foreground==null) 
        //foreground = doc.getForeground(getAttributes());
        foreground = doc.getForeground(doc.getStyleSheet().getViewAttributes(this));
      
      return foreground;
    }
    
    /** 
     * Returns the current font
     */
    protected Font getFont() {
      // we cached the font so that it's retrieved only once
      // instead of using this view's attributes we get the
      // document's stylesheet and ask it for this view's
      // attributes
      if (font==null) {
        font = doc.getFont(doc.getStyleSheet().getViewAttributes(this));
      }
      return font;
    }
    
    /**
     * Get the preferred span
     */
    protected abstract Dimension2D getPreferredSpan();

    /**
     * Invalidates this views current state
     */
    protected void invalidate() {
      // invalidate state
      preferredSpan = null;
      font = null;
      // signal preference change through super
      super.preferenceChanged(this,true,true);
    }
    
    /**
     * we use our kit's view factory
     */
    public ViewFactory getViewFactory() {
      return factory;
    }

    protected void render(String txt, Graphics2D g, Rectangle r) {
      
      // check for empty string
      if (txt.length()==0)
        return;
      
      // prepare layout
      TextLayout layout = new TextLayout(txt, g.getFont(), g.getFontRenderContext());
      
      // draw it
      layout.draw(g, (float)r.getX(), (float)r.getY()+layout.getAscent());
    }    
    
  } //MyView

  /**
   * RootView onto a HTML Document
   */
  private class RootView extends View {
  
    /** the root of the html's view hierarchy */
    private View view;
    
    /** the size of the root view */
    private float width, height;
  
    /**
     * Constructor
     */
    RootView(View view) {
      
      // block super
      super(null);
  
      // keep view
      this.view = view;
      view.setParent(this);
      
      // done
    }
    
    @Override
    public float getPreferredSpan(int axis) {
      throw new RuntimeException("getPreferredSpan() is not supported");
    }
    
    /**
     * @see javax.swing.text.View#viewToModel(float, float, Shape, Bias[])
     */
    public int viewToModel(float arg0, float arg1, Shape arg2, Bias[] arg3) {
      throw new RuntimeException("viewToModel() is not supported");
    }
    /**
     * @see javax.swing.text.View#modelToView(int, Shape, Bias)
     */
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
      throw new RuntimeException("modelToView() is not supported");
    }
    
    /**
     * we don't have any attributes
     */
    public AttributeSet getAttributes() {
      return null;
    }
  
    /**
     * we let the wrapped view do the painting
     */
    public void paint(Graphics g, Shape allocation) {
      view.paint(g, allocation);
    }
  
    /** 
     * our document is the parsed html'
     */
    public Document getDocument() {
      return doc;
    }
    
    @Override
    public Graphics getGraphics() {
      return graphics;
    }
    
    /**
     * the wrapped view needs to be sized
     */    
    public void setSize(float wIdth, float heIght) {
      // remember
      width = wIdth;
      height = heIght;
      // delegate
      try {
        view.setSize(width, height);
      } catch (Throwable t) {
        LOG.log(Level.FINE, "unexpected", t);
      }
      // done
    }

    /**
     * we use our kit's view factory
     */
    public ViewFactory getViewFactory() {
      return factory;
    }
  }

  /**
   * A view for translating text
   */
  private class I18NView extends MyView {
    
    /** the text to paint */
    private String txt = "?";
    
    /**
     * Constructor
     */
    private I18NView(Element elem) {
      super(elem);
      // resolve and localize text .. tag|entity
      Object o = elem.getAttributes().getAttribute("tag");
      if (o!=null) txt = Gedcom.getName(o.toString());
      else {
        o = elem.getAttributes().getAttribute("entity");
        if (o!=null) txt = Gedcom.getName(o.toString());
      }
      // done
    }
    /**
     * @see javax.swing.text.View#paint(java.awt.Graphics, java.awt.Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      Rectangle r = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      g.setFont(getFont());
      g.setColor(getForeground());
      render(txt,(Graphics2D)g,r);
    }
    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension2D getPreferredSpan() {
      FontMetrics fm = graphics.getFontMetrics(getFont());
      return new Dimension(
        fm.stringWidth(txt),
        fm.getAscent() + fm.getDescent()
      );
    }
  } //LocalizeView

  /**
   * A view that renders available media
   */
  private class MediaView extends MyView {
    
    private TagPath path2root = null;

    /** 
     * Constructor
     */
    MediaView(Element elem) {
      super(elem);

      Object p = elem.getAttributes().getAttribute("path");
      if (p!=null) try {
        path2root = new TagPath((String)p);
      } catch (IllegalArgumentException e) {
        if (LOG.isLoggable(Level.FINER))
          LOG.log(Level.FINER, "got wrong path "+p);
      }
    }
    
    private Property getRoot() {
      Property result = null;
      if (path2root!=null)
        result = entity.getProperty(path2root);
      return result !=null ? result : entity;
    }
    
    @Override
    protected Dimension2D getPreferredSpan() {
      Dimension2D size = MediaRenderer.getSize(getRoot(), graphics);
      if (isDebug && size.getWidth()==0&&size.getHeight()==0)
        return BROKEN.getSizeInPoints(DPI.get(graphics));
      return size;
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
      
      Rectangle r = allocation.getBounds();

      if (isDebug) {
        Dimension2D size = MediaRenderer.getSize(getRoot(), graphics);
        if (size.getWidth()==0&&size.getHeight()==0) {
          BROKEN.paintIcon(g, r.x, r.y);
          return;
        }
      }
      MediaRenderer.render(g, r, getRoot());
    }
    
  }
  
  /**
   * A view that wraps a property and its value
   */
  private class PropertyView extends MyView {
    
    /** configuration */
    private Map<String,String> attributes;
    private TagPath path = null;
    
    /** cached information */
    private Property cachedProperty = null;
    private Dimension2D cachedSize = null;
    
    /** 
     * Constructor
     */
    PropertyView(Element elem) {
      super(elem);

      // prepare attributes
      attributes = new HashMap<String, String>();
      
      for (Enumeration<?> as = elem.getAttributes().getAttributeNames(); as.hasMoreElements(); ) {
        Object key = as.nextElement();
        if (key instanceof String)
          attributes.put((String)key, (String)elem.getAttributes().getAttribute(key));
      }
      
      // grab path
      Object p = elem.getAttributes().getAttribute("path");
      if (p!=null) try {
        path = new TagPath((String)p);
      } catch (IllegalArgumentException e) {
        if (LOG.isLoggable(Level.FINER))
          LOG.log(Level.FINER, "got wrong path "+p);
      }
      
      // done
    }
    
    /**
     * Gets an int value from attributes
     */
    private int getAttribute(String key, int min, int max, int def) {
      // grab a value and try to parse
      String val = attributes.get(key);
      if (val!=null) try {
        return Math.max(min, Math.min(max, Integer.parseInt(val)));
      } catch (NumberFormatException e) {
      }
      // not found
      return def;
    }
    
    /**
     * Get Property
     */
    private Property getProperty() {
      // still looking for property?
      if (cachedProperty!=null)
        return cachedProperty;
      if (entity==null||path==null)
        return null;
      cachedProperty = BlueprintRenderer.this.getProperty(entity, path);
      return cachedProperty;
    }
    
    /**
     * @see javax.swing.text.View#paint(Graphics, Shape)
     */
    public void paint(Graphics g, Shape allocation) {
      
      Property prop = getProperty();
      if (prop==null)
        return;
      
      Graphics2D graphics = (Graphics2D)g;
      
      // setup painting attributes and bounds
      Rectangle r = (allocation instanceof Rectangle) ? (Rectangle)allocation : allocation.getBounds();
      Color fg = super.getForeground();
      
      // debug?
      if (isDebug) {
        Stroke stroke = graphics.getStroke();
        graphics.setStroke(DEBUG_STROKE);
        g.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 32));
        graphics.draw(r);
        graphics.setStroke(stroke);
      }
      
      // clip and render
      Shape old = graphics.getClip();
      graphics.clip(r);
      g.setColor(fg);
      g.setFont(super.getFont());
      render(prop, graphics, r);
      g.setClip(old);
      
      // done
    }
    
    private void render(Property prop, Graphics2D g, Rectangle r) {
      
      // entities (specifically NOTE) can be a multi-line - let fall through
      if (!(prop instanceof Entity) && prop instanceof MultiLineProperty) {
        render((MultiLineProperty)prop, g, r);
        return;
      }
      if (prop instanceof PropertyFile||prop instanceof PropertyBlob) {
        MediaRenderer.render(g, r, prop);
        return;
      }
      // image?
      if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) 
        render(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false), g, r);
      // text?
      if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT))) 
        render(getText(prop), g, r);
    }
    
    private void render(MultiLineProperty mle, Graphics2D g, Rectangle r) {
      // get lines
      MultiLineProperty.Iterator line = mle.getLineIterator();
      
      // paint
      Graphics2D graphics = (Graphics2D)g;
      Font font = g.getFont();
      FontRenderContext context = graphics.getFontRenderContext();

      float 
        x = (float)r.getX(),
        y = (float)r.getY();
      
      do {

        // analyze line
        String txt = line.getValue();
        LineMetrics lm = font.getLineMetrics(txt, context);
        y += lm.getHeight();
        
        // draw line
        graphics.drawString(txt, x, y - lm.getDescent());
        
        // .. break if line doesn't fit anymore
        if (y>r.getMaxY()) 
          break;
        
      } while (line.next());
      
      // done
    }
    
    private void render(ImageIcon img, Graphics2D g, Rectangle bounds) {
      
      // no space?
      if (bounds.getHeight()==0||bounds.getWidth()==0)
        return;
      
      // draw image with maximum height of a character
      int 
        w = img.getIconWidth(),
        max = g.getFontMetrics().getHeight();
      
      AffineTransform at = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
      if (max<img.getIconHeight()) {
        float scale = max/(float)img.getIconHeight();
        at.scale(scale, scale);
        w = (int)Math.ceil(w*scale);
      }
      g.drawImage(img.getImage(), at, null);
      
      // patch bounds for skip
      bounds.x += w+IMAGE_GAP;
      bounds.width -= w+IMAGE_GAP;
    }
        
    
    private String getText(Property prop) {
      if (prop instanceof Entity)
        return getText((Entity)prop);
      if (prop.isPrivate())
        return STARS;
      if (prop instanceof PropertyPlace)
        return getText((PropertyPlace)prop);
      if (prop instanceof PropertySex)
        return getText((PropertySex)prop);
      
      return prop.getDisplayValue();
    }
    
    private String getText(Entity entity) {
      return entity.getId();
    }
    
    private String getText(PropertySex sex) {
      
      if (!attributes.containsKey(HINT_KEY_TXT))
        attributes.put(HINT_KEY_TXT, HINT_VALUE_FALSE);
      if (!attributes.containsKey(HINT_KEY_IMG))
        attributes.put(HINT_KEY_IMG, HINT_VALUE_TRUE);
        
      String result = sex.getDisplayValue();
      if (result.length()>0 && HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_SHORT)))
        result = result.substring(0,1);
      return result;
    }
    
    private String getText(PropertyPlace place) {
      return place.format(attributes.get("format"));
    }
    
    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    protected Dimension2D getPreferredSpan() {
      // cached?
      if (cachedSize!=null)
        return cachedSize;
      // check property
      cachedSize = getSize();
      return cachedSize;
    }
    
    private Dimension2D getSize() {
      Property prop = getProperty();
      if (prop==null)
        return new Dimension();
      if (!(prop instanceof Entity)&&prop instanceof MultiLineProperty)
        return getSize((MultiLineProperty)prop);
      if (prop instanceof PropertyFile || prop instanceof PropertyBlob)
        return MediaRenderer.getSize(prop, getGraphics());
      return getSize(prop);
    }
    
    private Dimension2D getSize(Property prop) {
      
      String txt = getText(prop);
      
      double 
        w = 0,
        h = 0;
      
      // calculate text size (the default size we use)
      graphics.setFont(super.getFont());
      FontMetrics fm = graphics.getFontMetrics();
      if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT))&&txt.length()>0) {
        w += fm.stringWidth(txt);
        h = Math.max(h, fm.getAscent() + fm.getDescent());
      }
      // add image size
      if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) {
        ImageIcon img = prop.getImage(false);
        float max = fm.getHeight();
        float scale = 1F;
        if (max<img.getIconHeight()) 
          scale = max/img.getIconHeight();
        w += (int)Math.ceil(img.getIconWidth()*scale) + IMAGE_GAP;
        h = Math.max(h, fm.getHeight());
      }
      
      // done
      return new Dimension2d(w,h);
    }
    
    private Dimension2D getSize(MultiLineProperty mle) {
      
      // count 'em
      graphics.setFont(super.getFont());
      FontMetrics fm = graphics.getFontMetrics();
      int lines = 0;
      double width = 0;
      double height = 0;
      MultiLineProperty.Iterator line = mle.getLineIterator();
      do {
        lines++;
        width = Math.max(width, fm.stringWidth(line.getValue()));
        height += fm.getHeight();
      } while (line.next());
      
      // done
      return new Dimension2d(width, height);
    }
    
    /**
     * Invalidates this views current state
     */
    protected void invalidate() {
      // invalidate cached information that's depending
      // on the current entity's properties
      cachedProperty = null;
      cachedSize = null;
      super.invalidate();
    }
    
  } //PropertyView

} //EntityRenderer
