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
//XXX: genj.tree is publically exported as plugin set a dependancy on TreeView
// We must remove this like (redesign DnD logic or write some Interface API)

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.CommonActions;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.PropertyNode;
import ancestris.view.ExplorerHelper;
import ancestris.view.SelectionSink;
import genj.common.SelectEntityWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import genj.print.PrintAction;
import genj.print.PrintRenderer;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.ChooseBlueprintAction;
import genj.renderer.DPI;
import genj.renderer.RenderOptions;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import genj.util.Resources;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.util.swing.ViewPortOverview;
import genj.view.ScreenshotAction;
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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;

/**
 * TreeView
 */
// FIXME: used to find proper TreeView component for RootAction
//XXX: not used @ServiceProvider(service=TreeView.class)
public class TreeView extends View implements Filter {
  
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

  private Sticky sticky = new Sticky();

  /**
   * Constructor
   */
  public TreeView() {

    // remember
    DPI dpi = RenderOptions.getInstance().getDPI();
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
    setExplorerHelper(new ExplorerHelper(content));
    JScrollPane scroll = new ScrollPaneWidget(new ViewPortAdapter(content));
    overview = new Overview(scroll);
    overview.setVisible(REGISTRY.get("overview", true));
    overview.setSize(REGISTRY.get("overview", new Dimension(160,80)));
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

  public Gedcom getGedcom(){
      return (context==null?null:context.getGedcom());
  }
    @Override
    public void addNotify() {
        super.addNotify();
//XXX: removed in favour of GenjViewProxy    AncestrisPlugin.register (this);
        // FIXME: should be removed? use @serviceprovider?
    AncestrisPlugin.register (this);
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
    @Override
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
        model.getRoot().getGedcom().getRegistry().put("tree.root", model.getRoot().getId());
    
    // stoppers
    REGISTRY.put("hide.ancestors"  , model.getHideAncestorsIDs());
    REGISTRY.put("hide.descendants", model.getHideDescendantsIDs());
    
    AncestrisPlugin.unregister(this);
//XXX: removed in favour of GenjViewProxy    AncestrisPlugin.unregister(this);
    // done
    super.removeNotify();
  }

  // TreeViw Preferences
    public static boolean isFollowSelection() {
        return REGISTRY.get("selection.follow",false);
    }

    public static void setFollowSelection(boolean followSelection) {
        REGISTRY.put("selection.follow", followSelection);
    }

  
  /**
   * @see java.awt.Container#doLayout()
   */
    @Override
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
    @Override
  public Dimension getPreferredSize() {
    return new Dimension(480,480);
  }

  /**
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
    @Override
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

  //XXX: removed as selection will be done with lookups
//    @Override
//    public void setMyContext(Context context, boolean isActionPerformed) {
//        if (isActionPerformed) {
//            if (context == null) {
//                return;
//            }
//            setRoot(context.getEntity());
//        } else {
//            setContext(context, isActionPerformed);
//        }
//    }
//
  /**
   * view callback
   */
  @Override
  public void setContext(Context newContext, boolean isActionPerformed) {

      if (newContext == null)
          return;
      if (context.getGedcom() != null && ! newContext.getGedcom().equals(context.getGedcom()))
          return;
    // ignored?
    if (ignoreContextChange)
      return;
    if (sticky.isSelected()){
        return;
    }

    if (isFollowSelection()){
        if (isActionPerformed){
            setRoot(newContext.getEntity());
        }
    }else {
        if (!isActionPerformed){
            return;
        }
    }

   context = new Context(newContext.getGedcom(), newContext.getEntities());

    // nothing we can show?
    if (context.getEntity()==null)
      return;

    show(context.getEntity());

    // done
  }

  /**
   * Set current entity
   */
  /*package*/ public boolean show(Entity entity) {
    
    // allowed?
    if (!(entity instanceof Indi||entity instanceof Fam))
        // FIXME: ne devrait-on pas plutot renvoyer false dans ce cas?
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
   * Show current entity, show root if failed
   */
  /*package*/ public boolean show(Entity entity, boolean fallbackRoot) {
        // try to show
        if (show(context.getEntity()))
          return true;
        // otherwise try root
        if (fallbackRoot)
          return show(getRoot());
        return false;
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
    @Override
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

    // sticky
    toolbar.add(new JToggleButton(sticky));
    
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
    toolbar.add(new ActionGotoRoot());
    toolbar.add(new ActionChooseRoot());
    toolbar.addSeparator();
    
    // settings
    toolbar.add(new ScreenshotAction(content));
    toolbar.add(new Print());
    // Le addglue ne fonctionne pas acause du slider dont la taille n'est pas prise en compte correctement
//    toolbar.addGlue();
    toolbar.addSeparator();
    toolbar.add(new Settings());
    toolbar.add(new ActionBluePrint());
    
    // done
  }
  
  /**
   * // XXX: we will have to check this API when we will deal wil global drag and
   * drop in all other componants
   *
   * Retreive entity at given cooodinates
   * @param entityPos  Point in TreeView coordinates
   * @return entity over mouse pointer or null if there is no entity
   */
  public Entity getEntityAt(Point entityPos){
      if (model == null) return null;

      // je recupere la position de Content / Treeview
      ViewPortAdapter va = (ViewPortAdapter) content.getParent();
      JViewport vp = (JViewport) va.getParent();
      Point viewPosition = vp.getViewPosition();
      // je recupere la position décalée de "content" due au centrage
      // qui n'est pas nul quand "content" est plus petit que viewport
      Point contentShift = content.getLocation();

      // je change de repere TreeView => Content
      Point entityContentPos = new Point();
      entityContentPos.x = entityPos.x + viewPosition.x - contentShift.x;
      entityContentPos.y = entityPos.y + viewPosition.y - contentShift.y;
      // je change de repere Content => model
      Point modelPos = view2model(entityContentPos);
      // je recherche l'entité a cette position dans le modele
      return model.getEntityAt(modelPos.x, modelPos.y);
  }


  public Entity getRoot(){
      if (model == null) return null;
      return model.getRoot();
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
    
        @Override
    public void addNotify() {
      // cont
      super.addNotify();
      // listen to model events
      model.addListener(this);
    }
    
        @Override
    public void removeNotify() {
      model.removeListener(this);
      // cont
      super.removeNotify();
    }
    
    /**
     * @see java.awt.Component#setSize(int, int)
     */
        @Override
    public void setSize(int width, int height) {
      width = Math.max(32,width);
      height = Math.max(32,height);
      super.setSize(width, height);
    }
    /**
     * @see genj.util.swing.ViewPortOverview#paintContent(java.awt.Graphics, double, double)
     */
        @Override
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
      contentRenderer.cRootShape     = Color.GREEN;
      contentRenderer.selected       = context.getEntities();
      contentRenderer.root           = getRoot();
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
        @Override
    public void nodesChanged(Model arg0, Collection<TreeNode> arg1) {
      repaint();
    }
    /**
     * @see genj.tree.ModelListener#structureChanged(genj.tree.Model)
     */
        @Override
    public void structureChanged(Model arg0) {
      repaint();
    }
  } //Overview
  
  /**
   * The content we use for drawing
   */
  private class Content extends JComponent implements ModelListener, MouseWheelListener, MouseListener{

    /**
     * Constructor
     */
    private Content() {
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
      // listen to mouse events
      addMouseListener(this);
      addMouseWheelListener(this);
//      setFocusable(true);
//      setRequestFocusEnabled(true);

      //XXX: actions in layer (registration)
//      new Up().install(this, "U", JComponent.WHEN_FOCUSED);
    }

    private class Up extends AbstractAncestrisAction {
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
    
        @Override
    public void removeNotify() {
      model.removeListener(this);
      // cont
      super.removeNotify();
    }
    
        @Override
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
     * @see genj.tree.ModelListener#structureChanged(Model)
     */
        @Override
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
        @Override
    public void nodesChanged(Model model, Collection<TreeNode> nodes) {
      repaint();
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
        @Override
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
        @Override
    public void paint(Graphics g) {
      // fill backgound
      g.setColor(colors.get("background"));
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
      contentRenderer.cIndiShape     = colors.get("indis");
      contentRenderer.cFamShape      = colors.get("fams");
      contentRenderer.cArcs          = colors.get("arcs");
      contentRenderer.cSelectedShape = colors.get("selects");
      contentRenderer.cRootShape     = Color.GREEN;
      contentRenderer.selected       = selection ? context.getEntities() : new ArrayList<Entity>() ;
      contentRenderer.root           = getRoot();
      contentRenderer.indiRenderer   = getEntityRenderer(Gedcom.INDI);
      contentRenderer.famRenderer    = getEntityRenderer(Gedcom.FAM );
      // let the renderer do its work
      contentRenderer.render(gw, model);
      // done
    }
    
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
        @Override
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
        @Override
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
        @Override
    public void stateChanged(ChangeEvent e) {
    	setZoom(sliderZoom.getValue()*0.01D);
    }

  } //ZoomGlue
  
  /**
   * Action for opening overview
   */
  private class ActionOverview extends AbstractAncestrisAction {
    /**
     * Constructor
     */
    private ActionOverview() {
      setImage(Images.imgOverview);
      setTip(RESOURCES.getString("overview.tip"));
    }
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      overview.setVisible(!overview.isVisible());
    }
  } //ActionOverview    

  public Action getRootAction(Entity e, boolean b){
        if (e instanceof Indi || e instanceof Fam)
            return new ActionRoot(e,false);
      return CommonActions.NOOP;
  }
/**
 * ActionRoot
 */
  private class ActionRoot extends AbstractAncestrisAction {
    /** entity */
    private Entity root;
    /**
     * Constructor
     */
    private ActionRoot(Entity entity, boolean in) {
      putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
      root = entity;
      setText(RESOURCES.getString(in ? "root.in" : "root",TITLE));
      setImage(Images.imgView);
    }
    
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
    public void actionPerformed(ActionEvent event) {
      setRoot(root);
    }
  } //ActionRoot

  /**
   * Action Orientation change
   */
  private class ActionOrientation extends AbstractAncestrisAction {
    /**
     * Constructor
     */
    private ActionOrientation() {
      super.setImage(Images.imgHori);
      super.setTip(RESOURCES.getString("orientation.tip"));
    }
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      model.setVertical(!model.isVertical());
      scrollToCurrent();
    }
  } //ActionOrientation
  
  /**
   * Action Families n Spouses
   */
  private class ActionFamsAndSpouses extends AbstractAncestrisAction {
    /**
     * Constructor
     */
    private ActionFamsAndSpouses() {
      super.setImage(Images.imgDontFams);
      super.setTip(RESOURCES.getString("families.tip"));
    }
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      model.setFamilies(!model.isFamilies());
      scrollToCurrent();
    }
  } //ActionFamsAndSpouses

  /**
   * Action FoldSymbols on/off
   */
  private class ActionFoldSymbols extends AbstractAncestrisAction {
    /**
     * Constructor
     */
    private ActionFoldSymbols() {
      super.setImage(Images.imgFoldSymbols);
      super.setTip(RESOURCES.getString("foldsymbols.tip"));
    }
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      model.setFoldSymbols(!model.isFoldSymbols());
      scrollToCurrent();
    }
  } //ActionFolding
  
  /**
   * Action - choose a root through dialog
   */
  private class ActionChooseRoot extends AbstractAncestrisAction {

    /** constructor */
    private ActionChooseRoot() {
      setText(RESOURCES.getString("select.root"));
      setTip(RESOURCES.getString("select.root"));
      setImage(Images.imgView);
    }

    /** do the choosin' */
        @Override
    public void actionPerformed(ActionEvent event) {
      
      // let the user choose an individual
      SelectEntityWidget select = new SelectEntityWidget(context.getGedcom(), Gedcom.INDI, null);
      int rc = DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, select, AbstractAncestrisAction.okCancel(), TreeView.this);
      if (rc==0) 
        setRoot(select.getSelection());
      
      // done
    }
    
  } //ActionChooseRoot

  /**
   * Action - recenter tree to root
   */
  private class ActionGotoRoot extends AbstractAncestrisAction {

    /** constructor */
    private ActionGotoRoot() {
      setTip(RESOURCES.getString("goto.root.tip"));
      setImage(Images.imgGotoRoot);
    }

    /** do the choosin' */
        @Override
    public void actionPerformed(ActionEvent event) {

       show(getRoot());

      // done
    }

  } //ActionChooseRoot

  private class ActionGoto extends AbstractAncestrisAction {
    private Bookmark bookmark;
    private ActionGoto(Bookmark bookmark) {
      this.bookmark = bookmark;
      // setup text
      setText(bookmark.getName());
      setImage(Gedcom.getEntityImage(bookmark.getEntity().getTag()));
    }
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      // let everyone know
      Context newContext = new Context(bookmark.getEntity());

      try {
        ignoreContextChange = true;
        SelectionSink.Dispatcher.fireSelection(TreeView.this, newContext, false);
      } finally {
        ignoreContextChange = false;
      }
        setRoot(bookmark.getEntity());
      setContext(newContext, true);
    }

  }
  public Action getBookmarkAction(Entity e, boolean local){
        if (e instanceof Indi || e instanceof Fam)
            return new ActionBookmark(e,local);
      return CommonActions.NOOP;
  }
  /**
   * Action - bookmark something
   */
  private class ActionBookmark extends AbstractAncestrisAction {
    /** the entity */
    private Entity entity;
    /** 
     * Constructor 
     */
    private ActionBookmark(Entity e, boolean local) {
      putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
      entity = e;
      setImage(BOOKMARK_ICON);
      if (local) {
        setText(RESOURCES.getString("bookmark.add"));
      } else {
        setText(RESOURCES.getString("bookmark.in",TITLE));
      }
    } 
    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      
      // calculate a name
      String name = "";
      if (entity instanceof Indi) {
        name = ((Indi)entity).getName();
      }
      if (entity instanceof Fam) {
        Indi husb = ((Fam)entity).getHusband();
        Indi wife = ((Fam)entity).getWife();
        if (husb==null&&wife==null)
            name = entity.getId();
        else
            name = (husb==null? "":husb.getName()) + " & " + (wife == null? "" : wife.getName());
      }
      
      // Ask for name of bookmark
      name = DialogHelper.openDialog(
        TITLE, DialogHelper.QUESTION_MESSAGE, RESOURCES.getString("bookmark.name"), name, TreeView.this
      );
      
      if (name==null) return;
      
      // create it
      model.addBookmark(new Bookmark(name, entity));

      // save bookmarks
        {
          Entity root = model.getRoot();
          if (root != null){
            REGISTRY.put(root.getGedcom().getName()+".bookmarks", model.getBookmarks());
          }
        }
      
      // done
    }
  
  } //ActionBookmark
  
  private class Settings extends SettingsAction {

    @Override
    protected TreeViewSettings getEditor() {
      return new TreeViewSettings(TreeView.this);
    }
    
  }

    /**
   * Action - toggle sticky mode
   */
  private class Sticky extends AbstractAncestrisAction {
    /** constructor */
    protected Sticky() {
      super.setImage(ancestris.core.resources.Images.imgStickOff);
      super.setTip(RESOURCES.getString("action.stick.tip"));
      super.setSelected(false);
    }
    /** run */
        @Override
    public void actionPerformed(ActionEvent event) {
      setSelected(isSelected());
    }
    @Override
    public boolean setSelected(boolean selected) {
      super.setImage(selected ? ancestris.core.resources.Images.imgStickOn : ancestris.core.resources.Images.imgStickOff);
      return super.setSelected(selected);
    }
  } //Sticky
  
  private class ActionBluePrint extends AbstractAncestrisContextAction{

        public ActionBluePrint() {
            super();
            setImage( new ImageIcon(ChooseBlueprintAction.class, "Blueprint.png"));
        }

        @Override
        protected void actionPerformedImpl() {
            if (!contextProperties.isEmpty() && contextProperties.get(0) instanceof Entity){
            Entity entity = (Entity)(contextProperties.get(0));
            
            new ChooseBlueprintAction(entity, getBlueprint(entity.getTag())) {

                    @Override
                    protected void commit(Entity recipient, Blueprint blueprint) {
                        tag2blueprint.put(recipient.getTag(), blueprint.getName());
                        tag2renderer.remove(recipient.getTag());
                        repaint();
                    }
                }.actionPerformed(null);
        }
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
// Filter interface
    @Override
  public boolean veto(Property prop) {
    // all non-entities are fine
      return false;
    }
    @Override
  public boolean veto(Entity ent) {
    Set ents = model.getEntities();
    // indi?
    if (ent instanceof Indi)
      return !ents.contains(ent);
    // fam?
    if (ent instanceof Fam) {
      boolean b = ents.contains(ent);
      if (model.isFamilies()||b) return !b;
      Fam fam = (Fam)ent;
      boolean
        father = ents.contains(fam.getHusband()),
        mother = ents.contains(fam.getWife()),
        child = false;
      Indi[] children = fam.getChildren();
      for (int i = 0; child==false && i<children.length; i++) {
        if (ents.contains(children[i])) child = true;
      }
      // father and mother or parent and child
      return !((father&&mother) || (father&&child) || (mother&&child));
    }
    // let submitter through if it's THE one
    if (model.getRoot().getGedcom().getSubmitter()==ent)
      return false;
    // maybe a referenced other type?
    Entity[] refs = PropertyXRef.getReferences(ent);
    for (int r=0; r<refs.length; r++) {
      if (ents.contains(refs[r])) return false;
    }
    // not
    return true;
  }

  /**
   * A string representation of this view as a filter
   */
    @Override
  public String getFilterName() {
    return NbBundle.getMessage(TreeView.class, "TTL_Filter",
            model.getEntities().size(),TITLE);
  }

    @Override
    public boolean canApplyTo(Gedcom gedcom) {
        return (gedcom != null && gedcom.equals(context.getGedcom()));
    }


} //TreeView
