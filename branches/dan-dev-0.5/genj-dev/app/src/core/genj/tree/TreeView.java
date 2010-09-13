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
package genj.tree;

import genj.common.SelectEntityWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.print.PrintAction;
import genj.print.PrintRenderer;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.ChooseBlueprintAction;
import genj.renderer.DPI;
import genj.renderer.Options;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.util.swing.ViewPortOverview;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.ContextProvider;
import genj.view.ScreenshotAction;
import genj.view.SelectionSink;
import genj.view.SettingsAction;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewContext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TreeView
 */
public class TreeView extends View implements ContextProvider, ActionProvider {
  
  protected final static ImageIcon BOOKMARK_ICON = new ImageIcon(TreeView.class, "images/Bookmark");      
  protected final static Registry REGISTRY = Registry.get(TreeView.class);
  protected final static Resources RESOURCES = Resources.get(TreeView.class);
  protected final static String TITLE = RESOURCES.getString("title");
  
  /** the units we use */
  private final Point2D DPMM;
  
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our overview */
  private Overview overview;
  
  /** our content renderer */
  private ContentRenderer contentRenderer;
  
  /** our current zoom */
  private double zoom = 1.0D;

  /** our current zoom */  
  private SliderWidget sliderZoom;  
  
  /** whether we use antialising */
  private boolean isAntialiasing = false;
  
  /** our colors */
  private Map<String,Color> colors = new HashMap<String, Color>();
  
  /** our blueprints */
  private Map<String,String> tag2blueprint = new HashMap<String,String>();
  
  /** our renderers */
  private Map<String,BlueprintRenderer> tag2renderer = new HashMap<String, BlueprintRenderer>();
  
  /** our content's font */
  private Font contentFont = new Font("SansSerif", 0, 10);
  
  /** current centered position */
  private Point2D.Double center = new Point2D.Double(0,0);
  
  /** current context */
  private Context context = new Context();
  
  private boolean ignoreContextChange = false;
  
  /**
   * Constructor
   */
  public TreeView() {
    
    // remember
    DPI dpi = Options.getInstance().getDPI();
    DPMM = new Point2D.Float(
      dpi.horizontal() / 2.54F / 10,
      dpi.vertical() / 2.54F / 10
    );
    
    // grab colors
    colors.put("background", Color.WHITE);
    colors.put("indis"     , Color.BLACK);
    colors.put("fams"      , Color.DARK_GRAY);
    colors.put("arcs"      , Color.BLUE);
    colors.put("selects"   , Color.RED);
    colors = REGISTRY.get("color", colors);
    
    // grab font
    contentFont = REGISTRY.get("font", contentFont);
    
    // grab blueprints
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      String tag = Gedcom.ENTITIES[t];
      tag2blueprint.put(tag, REGISTRY.get("blueprint."+tag, ""));
    }
    
    // setup model
    model = new Model();
    model.setVertical(REGISTRY.get("vertical",true));
    model.setFamilies(REGISTRY.get("families",true));
    model.setBendArcs(REGISTRY.get("bend"    ,true));
    model.setMarrSymbols(REGISTRY.get("marrs",true));
    TreeMetrics defm = model.getMetrics();
    model.setMetrics(new TreeMetrics(
      REGISTRY.get("windis",defm.wIndis),
      REGISTRY.get("hindis",defm.hIndis),
      REGISTRY.get("wfams" ,defm.wFams ),
      REGISTRY.get("hfams" ,defm.hFams ),
      REGISTRY.get("pad"   ,defm.pad   )
    ));
    isAntialiasing = REGISTRY.get("antial", false);
    model.setHideAncestorsIDs(REGISTRY.get("hide.ancestors", new ArrayList<String>()));
    model.setHideDescendantsIDs(REGISTRY.get("hide.descendants", new ArrayList<String>()));
 
    // setup child components
    contentRenderer = new ContentRenderer();
    content = new Content();
    JScrollPane scroll = new ScrollPaneWidget(new ViewPortAdapter(content));
    overview = new Overview(scroll);
    overview.setVisible(REGISTRY.get("overview", true));
    overview.setSize(REGISTRY.get("overview", new Dimension(64,64)));
    zoom = Math.max(0.1, Math.min(1.0, REGISTRY.get("zoom", 1.0F)));
    
    // setup layout
    add(overview);
    add(scroll);
    
//    // scroll to current
//    javax.swing.SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//        scrollToCurrent();
//      }
//    });
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // settings
    REGISTRY.put("overview", overview.isVisible());
    REGISTRY.put("overview", overview.getSize());
    REGISTRY.put("zoom", (float)zoom);
    REGISTRY.put("vertical", model.isVertical());
    REGISTRY.put("families", model.isFamilies());
    REGISTRY.put("bend"    , model.isBendArcs());
    REGISTRY.put("marrs"   , model.isMarrSymbols());
    TreeMetrics m = model.getMetrics();
    REGISTRY.put("windis"  , m.wIndis);
    REGISTRY.put("hindis"  , m.hIndis);
    REGISTRY.put("wfams"   , m.wFams );
    REGISTRY.put("hfams"   , m.hFams );
    REGISTRY.put("pad"     , m.pad   );
    REGISTRY.put("antial"  , isAntialiasing );
    REGISTRY.put("font"    , contentFont);
    REGISTRY.put("color", colors);
    // blueprints
    for (String tag : tag2blueprint.keySet()) {
      REGISTRY.put("blueprint."+tag, getBlueprint(tag).getName()); 
    }
    
    // root    
    if (model.getRoot()!=null) 
      REGISTRY.put("root", model.getRoot().getId());
    
    // stoppers
    REGISTRY.put("hide.ancestors"  , model.getHideAncestorsIDs());
    REGISTRY.put("hide.descendants", model.getHideDescendantsIDs());
    
    // done
    super.removeNotify();
  }
  
  /**
   * ContextProvider callback
   */
  public ViewContext getContext() {
    return content.getContext();
  }
  
  /**
   * @see java.awt.Container#doLayout()
   */
  public void doLayout() {
    // layout components
    int 
      w = getWidth(),
      h = getHeight();
    Component[] cs = getComponents();
    for (int c=0; c<cs.length; c++) {
      if (cs[c]==overview) continue;
      cs[c].setBounds(0,0,w,h);
    }
    // done
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,480);
  }

  /**
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
  public boolean isOptimizedDrawingEnabled() {
    return !overview.isVisible();
  }

  /**
   * Accessor - isAntialising.
   */
  public boolean isAntialising() {
    return isAntialiasing;
  }

  /**
   * Accessor - isAntialising.
   */
  public void setAntialiasing(boolean set) {
    if (isAntialiasing==set) return;
    isAntialiasing = set;
    repaint();
  }
  
  /**
   * Access - contentFont
   */
  public Font getContentFont() {
    return contentFont;
  }

  /**
   * Access - contentFont
   */
  public void setContentFont(Font set) {
    // change?
    if (set==null||contentFont.equals(set)) 
      return;
    // remember
    contentFont = set;
    // show
    repaint();
  }
  
  public void setColors(Map<String,Color> set) {
    for (String key : colors.keySet()) {
      Color c = set.get(key);
      if (c!=null)
        colors.put(key, c);
    }
    repaint();
  }
  
  public Map<String,Color> getColors() {
    return Collections.unmodifiableMap(colors);
  }

  /**
   * Access - Mode
   */
  public Model getModel() {
    return model;
  }

  /**
   * view callback
   */
  @Override
  public void setContext(Context newContext, boolean isActionPerformed) {
    
    // ignored?
    if (ignoreContextChange)
      return;
    
    // remember
    context = new Context(newContext.getGedcom(), newContext.getEntities());
    
    // must root change?
    if (isActionPerformed || context.getGedcom()==null) {
      setRoot(context.getEntity());
      return;
    }
    
    // nothing we can show?
    if (context.getEntity()==null)
      return;

    // try to show - otherwise force
    if (!show(context.getEntity()))
      setRoot(context.getEntity());
    
    // done
  }
  
  /**
   * Set current entity
   */
  /*package*/ boolean show(Entity entity) {
    
    // allowed?
    if (!(entity instanceof Indi||entity instanceof Fam)) 
      return true;
    
    // Node for it?
    TreeNode node = model.getNode(entity);
    if (node==null) 
      return false;
    
    // scroll
    scrollTo(node.pos);
    
    // make sure it's reflected
    content.repaint();
    overview.repaint();
    
    // done
    return true;
  }
  
  /**
   * Scroll to given position
   */
  private void scrollTo(Point p) {
    // remember
    center.setLocation(p);
    // scroll
    Rectangle2D b = model.getBounds();
    Dimension   d = getSize();
    content.scrollRectToVisible(new Rectangle(
      (int)( (p.getX()-b.getMinX()) * (DPMM.getX()*zoom) ) - d.width /2,
      (int)( (p.getY()-b.getMinY()) * (DPMM.getY()*zoom) ) - d.height/2,
      d.width ,
      d.height
    ));
    // done
  }
  
  /**
   * Scroll to current entity
   */
  private void scrollToCurrent() {
    
    Entity current = context.getEntity();
    if (current==null)
      return;
    
    // Node for it?
    TreeNode node = model.getNode(current);
    if (node==null) 
      return;
    
    // scroll
    scrollTo(node.pos);

    // done    
  }
  
  
	private void setZoom(double d) {
		zoom = Math.max(0.1D, Math.min(1.0, d));
		content.invalidate();
		TreeView.this.validate();
		scrollToCurrent();
		repaint();
	}
  
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(ToolBar toolbar) {

    // zooming!    
    sliderZoom = new SliderWidget(1, 100, (int)(zoom*100));
    sliderZoom.addChangeListener(new ZoomGlue());
    sliderZoom.setAlignmentX(0F);
    sliderZoom.setOpaque(false);
    sliderZoom.setFocusable(false);
    toolbar.add(sliderZoom);
    
    // overview
    ButtonHelper bh = new ButtonHelper();
    toolbar.add(bh.create(new ActionOverview(), null, overview.isVisible()));
    
    // gap
    toolbar.addSeparator();
    
    // vertical/horizontal
    toolbar.add(bh.create(new ActionOrientation(), Images.imgVert, model.isVertical()));
    
    // families?
    toolbar.add(bh.create(new ActionFamsAndSpouses(), Images.imgDoFams, model.isFamilies()));
      
    // toggless?
    toolbar.add(bh.create(new ActionFoldSymbols(), null, model.isFoldSymbols()));
      
    // gap
    toolbar.addSeparator();
        
    // bookmarks
    PopupWidget pb = new PopupWidget("",BOOKMARK_ICON) {
      @Override
      public void showPopup() {
        removeItems();
        for (Bookmark bookmark : TreeView.this.model.getBookmarks())
          addItem(new ActionGoto(bookmark));
        // add items now
        super.showPopup();
      }
    };
    pb.setToolTipText(RESOURCES.getString("bookmark.tip"));
    pb.setOpaque(false);
    toolbar.add(pb);
    
    // settings
    toolbar.add(new Settings());
    toolbar.add(new ScreenshotAction(content));
    toolbar.add(new Print());
    
    // done
  }
  
  /**
   * create actions for a context
   */
  public void createActions(Context context, Purpose purpose, Group result) {
    
    // not for own
    if (context instanceof TreeContext)
      return;

    // for context menu of one record
    if (purpose==Purpose.CONTEXT&&context.getEntities().size()==1) {
      // fam or indi?
      Entity entity = context.getEntity();
      if (entity instanceof Indi||entity instanceof Fam) { 
        // create an action for our tree
        result.add(new ActionRoot(entity, true));
        result.add(new ActionBookmark(entity, false));
      }
    }
    
    // done
  }

  /**
   * Sets the root of this view
   */
  public void setRoot(Entity root) {
    
    // save bookmarks
    Entity old = model.getRoot();
    if (old!=null) {
      Gedcom gedcom = old.getGedcom();
      REGISTRY.put(gedcom.getName()+".bookmarks", model.getBookmarks());
    }
    
    // switch root
    if (root==null || root instanceof Indi ||root instanceof Fam) 
      model.setRoot(root);
    
    // load bookmarks
    if (root!=null) {
      Gedcom gedcom = root.getGedcom();
      List<Bookmark> bookmarks = new ArrayList<Bookmark>();
      for (String b : REGISTRY.get(gedcom.getName()+".bookmarks", new String[0])) {
        try {
          bookmarks.add(new Bookmark(gedcom, b));
        } catch (Throwable t) {
        }
      }
      model.setBookmarks(bookmarks);
    }

  }

  /**
   * Translate a view position into a model position
   */
  private Point view2model(Point pos) {
    Rectangle bounds = model.getBounds();
    return new Point(
      (int)Math.rint(pos.x / (DPMM.getX()*zoom) + bounds.getMinX()), 
      (int)Math.rint(pos.y / (DPMM.getY()*zoom) + bounds.getMinY())
    );
  }

  /**
   * Resolve a blueprint
   */
  /*package*/ Blueprint getBlueprint(String tag) {
    return BlueprintManager.getInstance().getBlueprint(tag, tag2blueprint.get(tag));
  }
  
  /**
   * Resolve a renderer
   */
  private BlueprintRenderer getEntityRenderer(String tag) {
    BlueprintRenderer result = tag2renderer.get(tag);
    if (result==null) { 
      result = new BlueprintRenderer(getBlueprint(tag));
      tag2renderer.put(tag,result);
    }
    return result;
  }
  
  /**
   * Overview
   */
  private class Overview extends ViewPortOverview implements ModelListener {
    /**
     * Constructor
     */
    private Overview(JScrollPane scroll) {
      super(scroll.getViewport());
      super.setSize(new Dimension(TreeView.this.getWidth()/4,TreeView.this.getHeight()/4));
    }
    
    public void addNotify() {
      // cont
      super.addNotify();
      // listen to model events
      model.addListener(this);
    }
    
    public void removeNotify() {
      model.removeListener(this);
      // cont
      super.removeNotify();
    }
    
    /**
     * @see java.awt.Component#setSize(int, int)
     */
    public void setSize(int width, int height) {
      width = Math.max(32,width);
      height = Math.max(32,height);
      super.setSize(width, height);
    }
    /**
     * @see genj.util.swing.ViewPortOverview#paintContent(java.awt.Graphics, double, double)
     */
    protected void renderContent(Graphics g, double zoomx, double zoomy) {

      // fill backgound
      g.setColor(Color.WHITE);
      Rectangle r = g.getClipBounds();
      g.fillRect(r.x,r.y,r.width,r.height);

      // go 2d
      UnitGraphics gw = new UnitGraphics(g,DPMM.getX()*zoomx*zoom, DPMM.getY()*zoomy*zoom);
      
      // init renderer
      contentRenderer.cIndiShape     = Color.BLACK;
      contentRenderer.cFamShape      = Color.BLACK;
      contentRenderer.cArcs          = Color.LIGHT_GRAY;
      contentRenderer.cSelectedShape = Color.RED;
      contentRenderer.selected       = context.getEntities();
      contentRenderer.indiRenderer   = null;
      contentRenderer.famRenderer    = null;
      
      // let the renderer do its work
      contentRenderer.render(gw, model);
      
      // restore
      gw.popTransformation();

      // done  
    }
    /**
     * @see genj.tree.ModelListener#nodesChanged(genj.tree.Model, java.util.List)
     */
    public void nodesChanged(Model arg0, Collection<TreeNode> arg1) {
      repaint();
    }
    /**
     * @see genj.tree.ModelListener#structureChanged(genj.tree.Model)
     */
    public void structureChanged(Model arg0) {
      repaint();
    }
  } //Overview
  
  /**
   * The content we use for drawing
   */
  private class Content extends JComponent implements ModelListener, MouseWheelListener, MouseListener, ContextProvider  {

    /**
     * Constructor
     */
    private Content() {
      // listen to mouse events
      addMouseListener(this);
      addMouseWheelListener(this);
//      setFocusable(true);
//      setRequestFocusEnabled(true);

      new Up().install(this, "U", JComponent.WHEN_FOCUSED);
    }

    private class Up extends Action2 {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("up");
      }
    }
    

    public void addNotify() {
      // cont
      super.addNotify();
      // listen to model events
      model.addListener(this);
    }
    
    public void removeNotify() {
      model.removeListener(this);
      // cont
      super.removeNotify();
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
      
      // zoom
      if (e.isControlDown()) {
        sliderZoom.setValue(sliderZoom.getValue() - e.getWheelRotation()*10);
        return;
      }
      
      // scroll
      JViewport viewport = (JViewport)getParent().getParent();
      Rectangle r = viewport.getVisibleRect();
      if (e.isShiftDown()) 
        r.x += e.getWheelRotation()*16;
      else
        r.y += e.getWheelRotation()*16;
      viewport.scrollRectToVisible(r);
      
    }
    
    /**
     * ContextProvider - callback
     */
    public ViewContext getContext() {
      TreeContext result = new TreeContext(context);
      Entity entity = context.getEntity();
      if (entity instanceof Indi) {
        result.addAction(new ActionBookmark((Indi)context.getEntity(), true));
        result.addAction(new ActionRoot((Indi)context.getEntity(), false));
      }
      if (entity!=null) {
        result.addAction(new ChooseBlueprintAction(entity, getBlueprint(entity.getTag())) {
          @Override
          protected void commit(Entity recipient, Blueprint blueprint) {
            tag2blueprint.put(recipient.getTag(), blueprint.getName());
            tag2renderer.remove(recipient.getTag());
            repaint();
          }
        });
      }
      result.addAction(new ActionChooseRoot());
      return result;
    }
    
    /**
     * @see genj.tree.ModelListener#structureChanged(Model)
     */
    public void structureChanged(Model model) {
      // 20030403 dropped revalidate() here because it works
      // lazily - for scrolling to work the invalidate()/validate()
      // has to run synchronously and the component has to 
      // be layouted correctly. Then no intermediates are
      // painted and the scroll calculation is correct
      invalidate();
      TreeView.this.validate(); // note: call on parent
      // still shuffle a repaint
      repaint();
      // scrolling should work now
      scrollToCurrent();
    }
    
    /**
     * @see genj.tree.ModelListener#nodesChanged(Model, List)
     */
    public void nodesChanged(Model model, Collection<TreeNode> nodes) {
      repaint();
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      Rectangle2D bounds = model.getBounds();
      double 
        w = bounds.getWidth () * (DPMM.getX()*zoom),
        h = bounds.getHeight() * (DPMM.getY()*zoom);
      return new Dimension((int)w,(int)h);
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paint(Graphics g) {
      // fill backgound
      g.setColor((Color)colors.get("background"));
      Rectangle r = g.getClipBounds();
      g.fillRect(r.x,r.y,r.width,r.height);
      // resolve our Graphics
      UnitGraphics gw = new UnitGraphics(g,DPMM.getX()*zoom, DPMM.getY()*zoom);
      gw.setAntialiasing(isAntialiasing);
      
      // render selection?
      Boolean selection = (Boolean) ((Graphics2D)g).getRenderingHint(RenderSelectionHintKey.KEY);
      if (selection==null)
        selection = true;
      
      // init renderer
      contentRenderer.font           = contentFont;
      contentRenderer.cIndiShape     = (Color)colors.get("indis");
      contentRenderer.cFamShape      = (Color)colors.get("fams");
      contentRenderer.cArcs          = (Color)colors.get("arcs");
      contentRenderer.cSelectedShape = (Color)colors.get("selects");
      contentRenderer.selected       = selection ? context.getEntities() : new ArrayList<Entity>() ;
      contentRenderer.indiRenderer   = getEntityRenderer(Gedcom.INDI);
      contentRenderer.famRenderer    = getEntityRenderer(Gedcom.FAM );
      // let the renderer do its work
      contentRenderer.render(gw, model);
      // done
    }
    
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      requestFocusInWindow();
      // check node
      Point p = view2model(e.getPoint());
      Object content = model.getContentAt(p.x, p.y);
      // nothing?
      if (content==null) {
        repaint();
        overview.repaint();
        return;
      }
      // entity?
      if (content instanceof Entity) {
        Entity entity = (Entity)content;
        // change current!
        if ((e.getModifiers()&MouseEvent.CTRL_DOWN_MASK)!=0) {
          List<Entity> entities = new ArrayList<Entity>(context.getEntities());
          if (entities.contains(entity))
            entities.remove(entity);
          else
            entities.add(entity);
        } else {
          context = new Context(entity);
        }
        repaint();
        overview.repaint();
        // propagate to others
        try {
          ignoreContextChange = true;
          SelectionSink.Dispatcher.fireSelection(e, context);
        } finally {
          ignoreContextChange = false;
        }
        return;
      }
      // runnable?
      if (content instanceof Runnable) {
        ((Runnable)content).run();
        return;
      }
      // done
    }
    
    /**
     * @see java.awt.event.MouseAdapter#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent evt) {
    }
  } //Content

  /**
   * Glue for zooming
   */
  private class ZoomGlue implements ChangeListener {
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
    	setZoom(sliderZoom.getValue()*0.01D);
    }

  } //ZoomGlue
  
  /**
   * Action for opening overview
   */
  private class ActionOverview extends Action2 {
    /**
     * Constructor
     */
    private ActionOverview() {
      setImage(Images.imgOverview);
      setTip(RESOURCES, "overview.tip");
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      overview.setVisible(!overview.isVisible());
    }
  } //ActionOverview    

  /**
   * ActionTree
   */
  private class ActionRoot extends Action2 {
    /** entity */
    private Entity root;
    /**
     * Constructor
     */
    private ActionRoot(Entity entity, boolean in) {
      root = entity;
      setText(RESOURCES.getString(in ? "root.in" : "root",TITLE));
      setImage(Images.imgView);
    }
    
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      setRoot(root);
    }
  } //ActionTree

  /**
   * Action Orientation change
   */
  private class ActionOrientation extends Action2 {
    /**
     * Constructor
     */
    private ActionOrientation() {
      super.setImage(Images.imgHori);
      super.setTip(RESOURCES, "orientation.tip");
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      model.setVertical(!model.isVertical());
      scrollToCurrent();
    }
  } //ActionOrientation
  
  /**
   * Action Families n Spouses
   */
  private class ActionFamsAndSpouses extends Action2 {
    /**
     * Constructor
     */
    private ActionFamsAndSpouses() {
      super.setImage(Images.imgDontFams);
      super.setTip(RESOURCES, "families.tip");
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      model.setFamilies(!model.isFamilies());
      scrollToCurrent();
    }
  } //ActionFamsAndSpouses

  /**
   * Action FoldSymbols on/off
   */
  private class ActionFoldSymbols extends Action2 {
    /**
     * Constructor
     */
    private ActionFoldSymbols() {
      super.setImage(Images.imgFoldSymbols);
      super.setTip(RESOURCES, "foldsymbols.tip");
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      model.setFoldSymbols(!model.isFoldSymbols());
      scrollToCurrent();
    }
  } //ActionFolding
  
  /**
   * Action - choose a root through dialog
   */
  private class ActionChooseRoot extends Action2 {

    /** constructor */
    private ActionChooseRoot() {
      setText(RESOURCES, "select.root");
      setImage(Images.imgView);
    }

    /** do the choosin' */
    public void actionPerformed(ActionEvent event) {
      
      // let the user choose an individual
      SelectEntityWidget select = new SelectEntityWidget(context.getGedcom(), Gedcom.INDI, null);
      int rc = DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, select, Action2.okCancel(), TreeView.this);
      if (rc==0) 
        setRoot(select.getSelection());
      
      // done
    }
    
  } //ActionChooseRoot

  private class ActionGoto extends Action2 {
    private Bookmark bookmark;
    private ActionGoto(Bookmark bookmark) {
      this.bookmark = bookmark;
      // setup text
      setText(bookmark.getName());
      setImage(Gedcom.getEntityImage(bookmark.getEntity().getTag()));
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      // let everyone know
      SelectionSink.Dispatcher.fireSelection(TreeView.this, new Context(bookmark.getEntity()), false);
    }

  }
  
  /**
   * Action - bookmark something
   */
  private class ActionBookmark extends Action2 {
    /** the entity */
    private Entity entity;
    /** 
     * Constructor 
     */
    private ActionBookmark(Entity e, boolean local) {
      entity = e;
      setImage(BOOKMARK_ICON);
      if (local) {
        setText(RESOURCES, "bookmark.add");
      } else {
        setText(RESOURCES.getString("bookmark.in",TITLE));
      }
    } 
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      
      // calculate a name
      String name = "";
      if (entity instanceof Indi) {
        name = ((Indi)entity).getName();
      }
      if (entity instanceof Fam) {
        Indi husb = ((Fam)entity).getHusband();
        Indi wife = ((Fam)entity).getWife();
        if (husb!=null&&wife!=null) name = husb.getName() + " & " + wife.getName();
      }
      
      // Ask for name of bookmark
      name = DialogHelper.openDialog(
        TITLE, DialogHelper.QUESTION_MESSAGE, RESOURCES.getString("bookmark.name"), name, TreeView.this
      );
      
      if (name==null) return;
      
      // create it
      model.addBookmark(new Bookmark(name, entity));
      
      // done
    }
  
  } //ActionBookmark
  
  private class Settings extends SettingsAction {

    @Override
    protected TreeViewSettings getEditor() {
      return new TreeViewSettings(TreeView.this);
    }
    
  }
  
  private class TreeContext extends ViewContext {
    public TreeContext(Context context) {
      super(context);
    } 
  }
  
  private class Print extends PrintAction {
    
    public boolean yes;
    
    protected Print() {
      super(TITLE);
    }
    @Override
    protected PrintRenderer getRenderer() {
      return new TreeViewPrinter(TreeView.this);
    }
  }
  
} //TreeView
