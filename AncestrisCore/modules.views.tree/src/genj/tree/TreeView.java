/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2007 Nils Meier <nils@meiers.net>
 * Copyright (C) 2008 - 2018 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.tree;
//XXX: genj.tree is publically exported as plugin set a dependancy on TreeView
// We must remove this like (redesign DnD logic or write some Interface API)

import ancestris.awt.FilteredMouseAdapter;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.CommonActions;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.ActionSaveViewAsGedcom;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import ancestris.modules.views.tree.style.Style;
import ancestris.modules.views.tree.style.TreeStyleManager;
import ancestris.swing.ToolBar;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectIndiOrFamPanel;
import ancestris.view.ExplorerHelper;
import ancestris.view.PropertyProvider;
import ancestris.view.SelectionActionEvent;
import ancestris.view.SelectionDispatcher;
import ancestris.view.TemplateToolTip;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintRenderer;
import genj.renderer.ChooseBlueprintAction;
import genj.renderer.DPI;
import genj.renderer.RenderOptions;
import genj.renderer.RenderSelectionHintKey;
import genj.tree.Model.NextFamily;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.util.swing.ViewPortOverview;
import genj.view.ScreenshotAction;
import genj.view.SettingsAction;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.DropDownButtonFactory;
import static org.openide.awt.DropDownButtonFactory.createDropDownButton;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * TreeView
 */
// FIXME: used to find proper TreeView component for RootAction
//@ServiceProvider(service=TreeView.class)
public class TreeView extends View implements Filter, AncestrisActionProvider, PropertyProvider {

    private static final Logger LOG = Logger.getLogger("ancestris.tree");

    protected final static ImageIcon BACKWARD_ICON = new ImageIcon(TreeView.class, "images/Back");
    protected final static ImageIcon BOOKMARK_ICON = new ImageIcon(TreeView.class, "images/Bookmark");
    protected final static ImageIcon FORWARD_ICON = new ImageIcon(TreeView.class, "images/Forward");
    protected final static Registry REGISTRY = Registry.get(TreeView.class);
    protected final static Resources RESOURCES = Resources.get(TreeView.class);
    protected final static String TITLE = RESOURCES.getString("title");
    protected final static String TIP = RESOURCES.getString("tooltip");
    /** the units we use */
    private final Point2D DPMM;
    /** our model */
    private final Model model;
    /** our content */
    private final Content content;
    private final ScrollPaneWidget scroll;
    /** our overview */
    private final Overview overview;
    /** our content renderer */
    private final ContentRenderer contentRenderer;
    /** our current zoom */
    private static float MINZOOM = 0.1F;
    private static float MAXZOOM = 1.0F;
    private static float DEFZOOM = 0.5F;
    private double zoom = DEFZOOM;
    private SliderWidget sliderZoom;
    /** folded state */
    private boolean isFolded = true;
    
    /** some buttons that will need to be updated */
    private ActionFamsAndSpouses famAndSpouseAction;
    private ActionGotoContext gotoContext;
    private ActionGotoRoot gotoRoot;
    private PopupWidget previousRootButton, nextRootButton;

    /** our styles */
    private TreeStyleManager styleManager;
    private Style style;

    /** current centered position */
    private final Point2D.Double center = new Point2D.Double(0, 0);
    /** current context */
    private Context context = new Context();
    private boolean ignoreContextChange = false;
    private final Sticky sticky = new Sticky();
    // Lookup listener for action callback
    private org.openide.util.Lookup.Result<SelectionActionEvent> result;

    private final TemplateToolTip tt = new TemplateToolTip();
    private JLabel rootTitle;
    
    // set root menu
    private JButton rootMenu;
    private RootList<Bookmark> rootList;
    // set goto menu
    private JButton gotoMenu;
    private boolean forceCenterCurrentAtOpening = true;

    /**
     * Constructor
     */
    public TreeView() {

        // remember
        DPI dpi = RenderOptions.getInstance().getDPI();
        DPMM = new Point2D.Float(
                dpi.horizontal() / 2.54F / 10,
                dpi.vertical() / 2.54F / 10);

        // grab styles
        styleManager = TreeStyleManager.getInstance(REGISTRY);
        style = styleManager.getStyle(REGISTRY.get("style", "default"));
        zoom = Math.max(MINZOOM, Math.min(MAXZOOM, REGISTRY.get("zoom", DEFZOOM)));  // zoom can be distinct from style.zoom

        // setup model
        model = new Model(this, style);
        model.setVertical(REGISTRY.get("vertical", true));
        model.setFamilies(REGISTRY.get("families", true));
        model.setHideAncestorsIDs(REGISTRY.get("hide.ancestors", new ArrayList<>()));
        model.setHideDescendantsIDs(REGISTRY.get("hide.descendants", new ArrayList<>()));
        model.setMaxGenerations(REGISTRY.get("maxgenerations", 20));

        // setup child components
        contentRenderer = new ContentRenderer();
        content = new Content();
        setExplorerHelper(new ExplorerHelper(content));
        scroll = new ScrollPaneWidget(new ViewPortAdapter(content));
        scroll.setView(this);
        overview = new Overview(scroll);
        overview.setVisible(REGISTRY.get("overview", true));
        overview.setSize(REGISTRY.get("overview", new Dimension(160, 80)));
        

        // setup layout
        add(overview);
        add(scroll);
        
        setCenteringPolicy();
        
        // Init rootList
        if (rootList == null) {
            rootList = new RootList<>();
        }
        
        // done
    }

    public Gedcom getGedcom() {
        if (context != null && context.getGedcom() != null) {
            return context.getGedcom();
        }
        return model.getRoot() == null ? null : model.getRoot().getGedcom();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Used only for Filter interface
        AncestrisPlugin.register(this);
        forceCenterCurrentAtOpening = true;
    }

    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {
        forceCenterCurrentAtOpening = true;
        AncestrisPlugin.unregister(this);
        // done
        super.removeNotify();
    }
    
    public void writeProperties(){
        // settings
        REGISTRY.put("overview", overview.isVisible());
        REGISTRY.put("overview", overview.getSize());
        REGISTRY.put("zoom", (float) zoom);
        REGISTRY.put("vertical", model.isVertical());
        REGISTRY.put("families", model.isFamilies());
        REGISTRY.put("maxgenerations", model.getMaxGenerations());
        REGISTRY.put("style", style.key);
        if (style.key.equals(TreeStyleManager.PERSOSTYLE)) { // only save perso settings if current style is perso (otherwise, it would overwrite perso style)
            saveStyle();
        }
        
        // root    
        if (model.getRoot() != null) {
            Registry r = getGedcom().getRegistry();
            r.put("tree.root", model.getRoot().getId());
            // Center position
            r.put("tree.center", getCenter());
        }

        // stoppers
        REGISTRY.put("hide.ancestors", model.getHideAncestorsIDs());
        REGISTRY.put("hide.descendants", model.getHideDescendantsIDs());
        
    }

    public void saveStyle(){
        styleManager.putStyle(REGISTRY);
    }

    
    public boolean confirmStyleOverwrite() {
        Style persoStyle = getStyleManager().getPersoStyle();
        if (getStyle() != persoStyle) {
            String title = NbBundle.getMessage(Style.class, "TITL_WarnStyleChange");
            String msg = NbBundle.getMessage(Style.class, "MSG_WarnStyleChange");
            Object o = DialogManager.create(title, msg).setOptionType(DialogManager.YES_NO_OPTION).setDialogId("style.change").show();
            return o.equals(DialogManager.OK_OPTION);
        }
        return true;
    }
            
    // TreeView Preferences
    public static boolean isAutoScroll() {
        return REGISTRY.get("auto.scroll", false);
    }

    public static void setAutoScroll(boolean autoScroll) {
        REGISTRY.put("auto.scroll", autoScroll);
    }

    public static boolean showPopup() {
        return REGISTRY.get("show.popup", false);
    }

    public static void setShowPopup(boolean showPopup) {
        REGISTRY.put("show.popup", showPopup);
    }
    
  /**
   * option - behaviour on action event (double click)
     * @return 
   */
    public static TreeViewSettings.OnAction getOnAction(){
        return REGISTRY.get("on.action",TreeViewSettings.OnAction.SETROOT);
    }
  
    public static void setOnAction(TreeViewSettings.OnAction action){
        REGISTRY.put("on.action",action);
    }

    /**
     * @see java.awt.Container#doLayout()
     */
    @Override
    public void doLayout() {
        // layout components
        int w = getWidth(),
                h = getHeight();
        Component[] cs = getComponents();
        for (Component c : cs) {
            if (c == overview) {
                continue;
            }
            c.setBounds(0, 0, w, h);
        }
        // done
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(480, 480);
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
        return style.antialiasing;
    }

    /**
     * Accessor - isAntialising.
     */
    public void setAntialiasing(boolean set) {
        if (style.antialiasing == set) {
            return;
        }
        style.antialiasing = set;
        repaint();
    }

    /**
     * Access - contentFont
     */
    public Font getContentFont() {
        return style.font;
    }

    /**
     * Access - contentFont
     */
    public void setContentFont(Font set) {
        // change?
        if (set == null || style.font.equals(set)) {
            return;
        }
        // remember
        style.font = set;
        // show
        repaint();
    }

    public void setColors(Map<String, Color> set) {
        for (String key : style.colors.keySet()) {
            Color c = set.get(key);
            if (c != null) {
                style.colors.put(key, c);
            }
        }
        repaint();
    }

    public Map<String, Color> getColors() {
        return Collections.unmodifiableMap(style.colors);
    }

    /**
     * Access - Model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Access - Style
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Access - Style
     */
    public void setStyle(Style set) {
        // change?
        if (set == null || style.equals(set)) {
            return;
        }
        // remember position of centered entity
        model.getCenteredEntities();

        // remember
        style = set;
        // reset zoom to new style zoom
        zoom = style.zoom;
        sliderZoom.setValue((int) (zoom * 100));
        // show
        getModel().setStyle(set);
        //repaint();
    }

    /**
     * Access - Styles
     */
    public TreeStyleManager getStyleManager() {
        return styleManager;
    }

    //XXX: we could probable install listners in gedcomdirectory
    private org.openide.util.Lookup.Result<SelectionActionEvent> addLookupListener(Context context) {
        org.openide.util.Lookup.Result<SelectionActionEvent> r;
        try {
            // Install action listener
            r = GedcomDirectory.getDefault().getDataObject(context).getLookup().lookupResult(SelectionActionEvent.class);
        } catch (ContextNotFoundException ex) {
            r = null;
        }
        final org.openide.util.Lookup.Result<SelectionActionEvent> returnValue = r;
        if (returnValue != null) {
            returnValue.addLookupListener((LookupEvent ev) -> {
                // notify
                //XXX: we must put selected nodes in global selection lookup (in fact use Explorer API)
                for (SelectionActionEvent e : returnValue.allInstances()) {
                    if (e != null) {
                        if (e.isAction()) {
                            fireAction(e.getSource(), e.getContext());
                        } else if (isEventInMe(e.getSource())) {
                            setContext(e.getContext());
                        }
                    }
                }
            });
        }
        return returnValue;
    }

    private boolean isEventInMe(Object comp) {
        if (comp == null) {
            return false;
        }
        Component source = null;
        if (comp instanceof Component) {
            source = SwingUtilities.getAncestorOfClass(TreeView.class, (Component) comp);
        }
        return (source != null && source == this);
    }

    public void fireAction(Object comp, Context context) {
        if (context == null) {
            return;
        }
        // ignored?
        if (ignoreContextChange) {
            return;
        }
        if (sticky.isSelected()) {
            return;
        }

        Component source = null;
        if (comp != null && comp instanceof Component) {
            source = SwingUtilities.getAncestorOfClass(TreeView.class, (Component) comp);
        }
        if (source != null && source == this) {
            setRoot(context.getEntity());
        } else {
            switch(TreeView.getOnAction()){
                case NONE:
                    break;
                case SETROOT:
                    setRoot(context.getEntity());
                    break;
                case CENTER:
                    show(context.getEntity(), true);
                    break;
            }
        }
    }

    /**
     * view callback
     */
    @Override
    public void setContext(Context newContext) {
        if (newContext == null) {
            return;
        }
        // install action listener
        if (result == null) {
            result = addLookupListener(newContext);
        }
        if (context.getGedcom() != null && !newContext.getGedcom().equals(context.getGedcom())) {
            return;
        }
        // ignored?
        if (ignoreContextChange) {
            return;
        }
        if (sticky.isSelected()) {
            return;
        }

        setContextImpl(newContext);
    }

    private void setContextImpl(Context newContext) {
        context = new Context(newContext.getGedcom(), newContext.getEntities());

        // nothing we can show?
        if (context.getEntity() == null) {
            return;
        }

        show(context.getEntity());
        // done
        
    }

    /**
     * Set current entity
     */
    /* package */ public boolean show(Entity entity) {
        return show(entity, false);
    }

    /* package */ private boolean show(Entity entity, boolean forceCenter) {
        // allowed?
        if (!(entity instanceof Indi || entity instanceof Fam)) // FIXME: ne devrait-on pas plutot renvoyer false dans ce cas?
        {
            return true;
        }

        // Node for it?
        TreeNode node = model.getNode(entity);
        if (node == null) {
            return false;
        }

        // scroll
        scrollTo(node.pos, forceCenter);

        // make sure it's reflected
        content.repaint();
        overview.repaint();

        // done
        return true;
    }
    
    /**
     * Scroll to given position
     */
    private void scrollTo(Point2D p, boolean forceCenter) {
        if (forceCenter || isAutoScroll()) {
            // remember
            center.setLocation(p);
            // scroll
            Rectangle2D b = model.getBounds();
            Dimension d = getSize();
            Rectangle r = new Rectangle(
                    (int) ((p.getX() - b.getMinX()) * (DPMM.getX() * zoom)) - d.width / 2,
                    (int) ((p.getY() - b.getMinY()) * (DPMM.getY() * zoom)) - d.height / 2,
                    d.width,
                    d.height);
            content.scrollRectToVisible(r);
            // done
        }
    }

    public Point getCenter(){
        if (scroll == null) return null;
        JViewport v = scroll.getViewport();
        return view2model(new Point(
                v.getViewPosition().x+v.getExtentSize().width/2,
                v.getViewPosition().y+v.getExtentSize().height/2));
    }

    /**
     * Scroll to current or default entity
     * 1/ if autoscroll is ON, center on current entty and only fallback to default if not in the model
     * 2/ if autoscroll is OFF, center on default entity
     */
    private void scrollToCurrent(boolean forceCentering, boolean forceCurrent) {

        if (forceCurrent || isAutoScroll()) {
            Entity current = context.getEntity();
            if (current != null) {
                // Node for it? 
                TreeNode node = model.getNode(current);
                if (node != null) {
                    scrollTo(node.pos, forceCurrent);
                    return;
                }
            }
        }
        
        // Null or not in model, so scroll to default
        TreeNode node = null;
        for (Entity entity : model.getDefaultEntities()) {
            node = model.getNode(entity);
            if (node != null) {
                model.clearDefaultEntities(); // should work only once
                break;
            }
        }
        // if null, exit
        if (node == null) {
            return; // node default, give up
        }
        
        scrollTo(node.pos, forceCentering);
    }

    private void setZoom(double d) {
        Point centr = getCenter();
        zoom = Math.max(MINZOOM, Math.min(MAXZOOM, d));
        if (getStyleManager().getPersoStyle() == style) { // only change style zoom value if style is perso
            style.zoom = zoom;
        }
        content.invalidate();
        if (isAutoScroll()){
            scrollToCurrent(false, false);
        } else {
            scrollTo(centr, true);
        }
        TreeView.this.validate();
        repaint();
    }

    /**
     * @see genj.view.ToolBarSupport#populate(JToolBar)
     */
    @Override
    public void populate(ToolBar toolbar) {

        // zooming!    
        sliderZoom = new SliderWidget(1, 100, (int) (zoom * 100));
        sliderZoom.addChangeListener(new ZoomGlue());
        sliderZoom.setAlignmentX(0F);
        sliderZoom.setOpaque(false);
        sliderZoom.setFocusable(false);
        sliderZoom.setPreferredSize(new Dimension(60, sliderZoom.getPreferredSize().height));
        sliderZoom.setMinimumSize(new Dimension(30, sliderZoom.getPreferredSize().height));
        toolbar.add(sliderZoom);

        // overview
        ButtonHelper bh = new ButtonHelper();
        toolbar.add(bh.create(new ActionOverview(), null, overview.isVisible()));

        // gap
        toolbar.addSeparator();

        // vertical/horizontal
        toolbar.add(bh.create(new ActionOrientation(), Images.imgVert, model.isVertical()));

        // families?
        famAndSpouseAction = new ActionFamsAndSpouses();
        toolbar.add(bh.create(famAndSpouseAction, null, model.isFamilies()));

        // toggless?
        toolbar.add(bh.create(new ActionFoldSymbols(), null, model.isFoldSymbols()));
        toolbar.add(bh.create(new ActionFoldUnfoldAll()));

        // gap
        toolbar.addSeparator();
        toolbar.add(new ScreenshotAction(content));

        // View
        gotoMenu = createDropDownButton(Images.imgGotoRoot, null);
        gotoContext = new ActionGotoContext(gotoMenu);
        gotoRoot = new ActionGotoRoot(gotoMenu);
        gotoMenu.putClientProperty(
                DropDownButtonFactory.PROP_DROP_DOWN_MENU,
                Utilities.actionsToPopup(new Action[]{gotoContext,gotoRoot}, org.openide.util.Lookup.EMPTY));
        gotoMenu.setAction(gotoContext);
        toolbar.add(gotoMenu);

        // ROOT MANAGEMENT
        toolbar.addSeparator();
        toolbar.add(new JToggleButton(sticky));

        // rebuild root
        rootMenu = createDropDownButton(Images.imgView,null); 
        Action def1 = new ActionRootContext(rootMenu);
        Action def2 = new ActionChooseRoot(rootMenu);
        rootMenu.putClientProperty(
                DropDownButtonFactory.PROP_DROP_DOWN_MENU,
                Utilities.actionsToPopup(new Action[]{def1, def2}, org.openide.util.Lookup.EMPTY));
        rootMenu.setAction(def1);
        toolbar.add(rootMenu);

        // root bookmarks
        PopupWidget bookmarkbutton = new PopupWidget("", BOOKMARK_ICON) {
            @Override
            public void showPopup() {
                removeItems();
                for (Bookmark bookmark : TreeView.this.model.getBookmarks()) {
                    addItem(new ActionGoto(bookmark, false));
                }
                super.showPopup();
            }
        };
        bookmarkbutton.setToolTipText(RESOURCES.getString("bookmark.tip"));
        bookmarkbutton.setOpaque(false);
        toolbar.add(bookmarkbutton);
        
        // root navigation backward
        previousRootButton = new PopupWidget("", BACKWARD_ICON) {
            @Override
            public void showPopup() {
                removeItems();
                for (Bookmark bookmark : rootList.getList(false)) {
                    addItem(new ActionGoto(bookmark, true));
                }
                super.showPopup();
            }
        };
        previousRootButton.setToolTipText(RESOURCES.getString("navigation.backward.tip"));
        previousRootButton.setOpaque(false);
        toolbar.add(previousRootButton);
        
        // root title
        rootTitle = new JLabel();
        rootTitle.setHorizontalAlignment(SwingConstants.CENTER);
        toolbar.add(rootTitle, "growx, pushx, center");
        setRootTitle("");

        // root navigation forward
        nextRootButton = new PopupWidget("", FORWARD_ICON) {
            @Override
            public void showPopup() {
                removeItems();
                for (Bookmark bookmark : rootList.getList(true)) {
                    addItem(new ActionGoto(bookmark, true));
                }
                super.showPopup();
            }
        };
        nextRootButton.setToolTipText(RESOURCES.getString("navigation.forward.tip"));
        nextRootButton.setOpaque(false);
        toolbar.add(nextRootButton);

        // settings
        toolbar.addSeparator();
        
        // Export tree
        toolbar.add(new ActionSaveViewAsGedcom(getGedcom(), this));

        // Blueprint
        toolbar.add(new ActionBluePrint());
        // Settings
        toolbar.add(new Settings());
        toolbar.setFloatable(false);

        // done
    }

    private void setRootTitle(String title) {
        title = title.replaceAll("\\) \\+", ")<br>+"); // line break for family entity after husband name to make sure it fits on 2 lines
        rootTitle.setText("<html><center>" + RESOURCES.getString("root.name") + " " + title + "</center></html");

    }

    /**
     * // XXX: we will have to check this API when we will deal with global drag and
     * drop in all other componants
     *
     * Retrieve entity at given coodinates
     *
     * @param entityPos Point in TreeView coordinates (when treeViewCoordinates is true)
     *        entityPos Point in Content coordinates (when treeViewCoordinates is false)
     * @param treeViewCoordinates
     *
     * @return entity over mouse pointer or null if there is no entity
     */
    @Override
    public Entity getEntityAt(Point entityPos, boolean treeViewCoordinates) {
        if (model == null) {
            return null;
        }

        Point entityContentPos = new Point();
        
        if (treeViewCoordinates) {
            // je recupere la position de Content / Treeview
            ViewPortAdapter va = (ViewPortAdapter) content.getParent();
            JViewport vp = (JViewport) va.getParent();
            Point viewPosition = vp.getViewPosition();
            // je recupere la position décalée de "content" due au centrage
            // qui n'est pas nul quand "content" est plus petit que viewport
            Point contentShift = content.getLocation();

            // je change de repere TreeView => Content
            entityContentPos.x = entityPos.x + viewPosition.x - contentShift.x;
            entityContentPos.y = entityPos.y + viewPosition.y - contentShift.y;
        } else {
            entityContentPos.x = entityPos.x;
            entityContentPos.y = entityPos.y;
        }
        // je change de repere Content => model
        Point modelPos = view2model(entityContentPos);
        // je recherche l'entité a cette position dans le modele
        return model.getEntityAt(modelPos.x, modelPos.y);
    }

    public Entity getRoot() {
        if (model == null) {
            return null;
        }
        return model.getRoot();
    }

    /**
     * Sets the root of this view
     */
    public void setRoot(Entity root) {

        // save bookmarks
        Entity old = model.getRoot();
        if (root == null || old == root) {
            return;
        }
        if (old != null) {
            Gedcom gedcom = old.getGedcom();
            if (gedcom != null) {
                REGISTRY.put(gedcom.getName() + ".bookmarks", model.getBookmarks());
            }
        }

        // switch root
        if (root instanceof Indi || root instanceof Fam) {
            model.setRoot(root);
            rootList.add(new Bookmark(root.getDisplayTitle(), root));
            previousRootButton.setEnabled(!rootList.isStart());
            nextRootButton.setEnabled(!rootList.isEnd());
            show(root, true);
            setRootTitle(root.getDisplayTitle());
            famAndSpouseAction.updateButton();
        }

        // load bookmarks
        if (root != null) {
            Gedcom gedcom = root.getGedcom();
            List<Bookmark> bookmarks = new ArrayList<>();
            String[] booklist = REGISTRY.get(gedcom.getName() + ".bookmarks", new String[0]);
            for (String b : booklist) {
                try {
                    bookmarks.add(new Bookmark(gedcom, b));
                } catch (IllegalArgumentException t) { // Not a Bookmark, note it as separator
                    bookmarks.add(new BookmarkSeparator(b));
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
                (int) Math.rint(pos.x / (DPMM.getX() * zoom) + bounds.getMinX()),
                (int) Math.rint(pos.y / (DPMM.getY() * zoom) + bounds.getMinY()));
    }

    /**
     * Resolve a renderer
     */
    private BlueprintRenderer getEntityRenderer(String tag) {
        if (tag.equals("INDI")) {
            return new BlueprintRenderer(style.blueprintIndi);
        } else {
            return new BlueprintRenderer(style.blueprintFam);
        }
    }

    @Override
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus) {
            return new ArrayList<>();
        }
        List<Action> actions = new ArrayList<>();
        if (nodes.length == 1) {
            actions.add(new ActionBluePrint());
        }
        return actions;
    }

    // Centers on selected entity at opening (does not seem to always have an effect.
    // It's because of Window resize : if a TopComponent opens after this one, this one should recenter
    private void setCenteringPolicy() {

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scrollToCurrent(false, forceCenterCurrentAtOpening);
            }
        });
        Timer timer = new Timer("once");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                forceCenterCurrentAtOpening = false;
            }
        };
        timer.schedule(task, 3000L);
    }

    public void forceFamilies(boolean b) {
        famAndSpouseAction.setImage(Images.imgDontFams);
        famAndSpouseAction.setTip("<html>" + RESOURCES.getString("familiesnot.tip") + "</html>");
    }

    @Override
    public Property provideVisibleProperty(Point point) {
        return getEntityAt(point, false);
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
            super.setSize(new Dimension(TreeView.this.getWidth() / 4, TreeView.this.getHeight() / 4));
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
            width = Math.max(32, width);
            height = Math.max(32, height);
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
            g.fillRect(r.x, r.y, r.width, r.height);

            // go 2d
            UnitGraphics gw = new UnitGraphics(g, DPMM.getX() * zoomx * zoom, DPMM.getY() * zoomy * zoom);

            // init renderer
            contentRenderer.overview = true;
            contentRenderer.cBackground = style.colors.get("background");
            contentRenderer.cMaleIndiShape = Color.BLACK;
            contentRenderer.cFemaleIndiShape = Color.BLACK;
            contentRenderer.cUnknownIndiShape = Color.BLACK;
            contentRenderer.cFamShape = Color.BLACK;
            contentRenderer.cArcs = Color.LIGHT_GRAY;
            contentRenderer.cSelectedShape = style.colors.get("selects");
            contentRenderer.cRootShape = style.colors.get("roots");
            contentRenderer.indisThick = 1;
            contentRenderer.famsThick = 1;
            contentRenderer.selected = context.getEntities();
            contentRenderer.root = getRoot();
            contentRenderer.indiRenderer = null;
            contentRenderer.famRenderer = null;

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
    private class Content extends JComponent implements ModelListener {

        private transient FilteredMouseAdapter mouseAdapter;

        /**
         * Constructor
         */
        private Content() {
            putClientProperty("print.printable", Boolean.TRUE); // NOI18N
            // listen to mouse events
            mouseAdapter = new FilteredMouseAdapter() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    Content.this.mouseWheelMoved(e);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    Content.this.mousePressed(e);
                }

                @Override
                public void mouseClickedFiltered(MouseEvent me) {
                    Content.this.mouseClicked(me);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    ///forceCenterAtOpening = false;
                }
            };
            addMouseListener(mouseAdapter);
            addMouseWheelListener(mouseAdapter);
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

        @Override
        public void addNotify() {
            // cont
            super.addNotify();
            // listen to model events
            model.addListener(this);
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        @Override
        public void removeNotify() {
            model.removeListener(this);
            // cont
            super.removeNotify();
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }

        @Override
        public JToolTip createToolTip() {
            tt.setComponent(this);
            return tt;
        }

        /*
         * fake set tt text to let tooltip manager hide or show tt
         * the get ttlocation must return null if no entity can be found. if not tt show a blank component
         */
        private Entity oldTTEntity = null;

        @Override
        public String getToolTipText(MouseEvent event) {
            if (!showPopup()) {
                oldTTEntity = null;
                return null;
            }
            Entity entity = getEntityForEvent(event);
            if (entity != oldTTEntity) {
                ttPosition = null;
                oldTTEntity = entity;
            }
            tt.setEntity(oldTTEntity);
            if (oldTTEntity == null) {
                return null;
            } else {
                return oldTTEntity.getId();
            }
        }

        /**
         * Helper to find entity for a MouseEvent position in Content coordinate
         *
         * @param event
         *
         * @return Entity
         */
        private Entity getEntityForEvent(MouseEvent event) {
            // check node
            Entity entity = null;
            Point p = view2model(event.getPoint());
            Object content = model.getContentAt(p.x, p.y);
            // nothing?
            if (content != null && content instanceof Entity) {
                entity = (Entity) content;
            }
            if (content != null && content instanceof NextFamily) {
                entity = ((NextFamily) content).getSpouse();
            }
            return entity;
        }

        private Point ttPosition = null;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
            if (!showPopup() || oldTTEntity == null) {
                return null;
            }

            if (ttPosition == null) {
                ttPosition = new Point(event.getX() - 5, event.getY() + 2);
            }
            return ttPosition;
        }

        /**
         * @param e
         */
        public void mouseWheelMoved(MouseWheelEvent e) {

            // zoom
            if (e.isControlDown()) {
                sliderZoom.setValue(sliderZoom.getValue() - e.getWheelRotation() * 10);
                return;
            }

            // scroll
            JViewport viewport = (JViewport) getParent().getParent();
            Rectangle r = viewport.getVisibleRect();
            if (e.isShiftDown()) {
                r.x += e.getWheelRotation() * 32;
            } else {
                r.y += e.getWheelRotation() * 32;
            }
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
            scrollToCurrent(true, false);
            // update button
            famAndSpouseAction.updateButton();
            gotoContext.updateButton();

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
            double w = bounds.getWidth() * (DPMM.getX() * zoom),
                    h = bounds.getHeight() * (DPMM.getY() * zoom);
            return new Dimension((int) w, (int) h);
        }

        /**
         * @see javax.swing.JComponent#paintComponent(Graphics)
         */
        @Override
        public void paint(Graphics g) {
            // fill backgound
            g.setColor(style.colors.get("background"));
            Rectangle r = g.getClipBounds();
            g.fillRect(r.x, r.y, r.width, r.height);
            // resolve our Graphics
            UnitGraphics gw = new UnitGraphics(g, DPMM.getX() * zoom, DPMM.getY() * zoom);
            gw.setAntialiasing(style.antialiasing);

            // render selection?
            Boolean selection = (Boolean) ((Graphics2D) g).getRenderingHint(RenderSelectionHintKey.KEY);
            if (selection == null) {
                selection = true;
            }

            // init renderer
            contentRenderer.overview = false;
            contentRenderer.font = style.font;
            contentRenderer.cBackground = style.colors.get("background");
            contentRenderer.cMaleIndiShape = style.colors.get("maleindis");
            contentRenderer.cFemaleIndiShape = style.colors.get("femaleindis");
            contentRenderer.cUnknownIndiShape = style.colors.get("unknownindis");
            contentRenderer.cFamShape = style.colors.get("fams");
            contentRenderer.cArcs = style.colors.get("arcs");
            contentRenderer.cSelectedShape = style.colors.get("selects");
            contentRenderer.cRootShape = style.colors.get("roots");
            contentRenderer.indisThick = model.getMetrics().indisThick;
            contentRenderer.famsThick = model.getMetrics().famsThick;
            contentRenderer.selected = selection ? context.getEntities() : new ArrayList<>();
            contentRenderer.root = getRoot();
            contentRenderer.indiRenderer = getEntityRenderer(Gedcom.INDI);
            contentRenderer.famRenderer = getEntityRenderer(Gedcom.FAM);
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
            if (content == null) {
                repaint();
                overview.repaint();
            }
            if (content instanceof Entity) {
                Entity entity = (Entity) content;
                // change current!
                if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
                    List<Entity> entities = new ArrayList<>(context.getEntities());
                    if (entities.contains(entity)) {
                        entities.remove(entity);
                    } else {
                        entities.add(entity);
                    }
                } else {
                    context = new Context(entity);
                }
//        repaint();
//        overview.repaint();
                // propagate to others
                try {
//          ignoreContextChange = true;
                    SelectionDispatcher.fireSelection(e, context);
                } finally {
//          ignoreContextChange = false;
                }
            }
            // done
        }

        /**
         * @see java.awt.event.MouseAdapter#mouseClicked(MouseEvent)
         */
        public void mouseClicked(MouseEvent e) {
            requestFocusInWindow();
            // check node
            Point p = view2model(e.getPoint());
            Object content = model.getContentAt(p.x, p.y);
            // runnable?
            if (content instanceof Runnable) {
                ((Runnable) content).run();
            }
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
            setZoom(sliderZoom.getValue() * 0.01D);
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

    public Action getRootAction(Entity e, boolean b) {
        if (e instanceof Indi || e instanceof Fam) {
            return new ActionRoot(e, false);
        }
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
            setText(RESOURCES.getString("root"));
            setTip(RESOURCES.getString("root.tip"));
            setImage(Images.imgView);
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
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
            scrollToCurrent(false, false);
        }
    } //ActionOrientation

    /**
     * Action Families n Spouses
     */
    private class ActionFamsAndSpouses extends AbstractAncestrisAction {

        public void updateButton() {
            boolean forceFamily = (getRoot() instanceof Fam);
            super.setEnabled(!forceFamily);
            String addition = "";
            if (forceFamily) {
                model.setFamilies(forceFamily);
                addition = RESOURCES.getString("familiesforced.tip");
            }
            super.setImage(model.isFamilies() ? Images.imgDontFams : Images.imgDoFams);
            super.setTip("<html>" + RESOURCES.getString(model.isFamilies() ? "familiesnot.tip" : "families.tip") + "<br>" + addition + "</html>");
        }
        
        /**
         * Constructor
         */
        private ActionFamsAndSpouses() {
            updateButton();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            model.setFamilies(!model.isFamilies());
            updateButton();
            scrollToCurrent(true, false);
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
            scrollToCurrent(true, false);
        }
    } //ActionFolding

    /**
     * Action Fold All / Unfold All
     */
    private class ActionFoldUnfoldAll extends AbstractAncestrisAction {

        /**
         * Constructor
         */
        private ActionFoldUnfoldAll() {
            super.setImage(isFolded ? Images.imgUnfoldAll : Images.imgFoldAll);
            super.setTip(RESOURCES.getString(isFolded ? "unfoldall.tip" : "foldall.tip", model.getMaxGenerations()));
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (isFolded) {
                model.unfoldAll();
            } else {
                model.foldAll();
            }
            isFolded = !isFolded;
            super.setImage(isFolded ? Images.imgUnfoldAll : Images.imgFoldAll);
            super.setTip(RESOURCES.getString(isFolded ? "unfoldall.tip" : "foldall.tip", model.getMaxGenerations()));
            scrollToCurrent(true, false);
        }
    } //ActionFolding

    /**
     * Action - choose a root through dialog
     * set parent menu button action on this action 
     */
    // FIXME: should we implement this in AbstractAncestrisAction?
    private class ActionChooseRoot extends AbstractAncestrisAction {
        private JButton bMenu = null;

        /** constructor */
        private ActionChooseRoot() {
            this(null);
        }
        private ActionChooseRoot(JButton b) {
            setText(RESOURCES.getString("select.root"));
            setTip(RESOURCES.getString("select.root"));
            setImage(Images.imgView);
            bMenu = b;
        }

        /** do the choosin' */
        @Override
        public void actionPerformed(ActionEvent event) {

            // let the user choose an individual
            SelectIndiOrFamPanel select = new SelectIndiOrFamPanel(context.getGedcom(), model.getRoot().getTag(), NbBundle.getMessage(this.getClass(), "treeview.askentity"), null);
            if (DialogManager.OK_OPTION != DialogManager.create(getText(), select)
                    .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("select.root").show()) {
                return;
            }
            setRoot(select.getSelection());
            if (bMenu.getAction() != this) {
                bMenu.setAction(this);
            }
            // done
        }
    } //ActionChooseRoot

    /**
     * Action to set root to current context 
     */
    private class ActionRootContext extends AbstractAncestrisContextAction {

        private Entity entity;
        private JButton bMenu = null;

        public ActionRootContext() {
            this(null);
        }
        public ActionRootContext(JButton b) {
            super();
            bMenu = b;
            Entity e = null;
            Context c = TreeView.this.context;
            if (c != null)
                e = c.getEntity();
            if (e == null)
                e = TreeView.this.getRoot();
            setTip(RESOURCES.getString("root.context",e==null?"":e.getDisplayTitle()));
            setText(RESOURCES.getString("root.context",e==null?"":e.getDisplayTitle()));
            setImage(Images.imgView);
        }

        @Override
        protected void contextChanged() {
            if (!contextProperties.isEmpty()) {
                Property prop = contextProperties.get(0);
                if (prop.getEntity() instanceof Indi || prop.getEntity() instanceof Fam) {
                    entity = prop.getEntity();
                } else if (entity == null) {
                    entity = prop.getGedcom().getFirstEntity(Gedcom.INDI);
                }
            }
            if (entity != null){
                setTip(RESOURCES.getString("root.context",entity==null?"":entity.getDisplayTitle()));
                setText(RESOURCES.getString("root.context",entity==null?"":entity.getDisplayTitle()));
            }
            super.contextChanged();
        }

        @Override
        protected void actionPerformedImpl(final ActionEvent event) {
            setRoot(entity);
            if (bMenu.getAction() != this)
                bMenu.setAction( this );
        }
    }

    /**
     * Action - recenter tree to root
     */
    private class ActionGotoRoot extends AbstractAncestrisAction {
        private JButton bMenu = null;
        
        /** constructor */
        private ActionGotoRoot() {
            this(null);
        }
        private ActionGotoRoot(JButton b) {
            bMenu = b;
            setTip(RESOURCES.getString("goto.root.tip"));
            setText(RESOURCES.getString("goto.root.tip"));
            setImage(Images.imgGotoRoot);
        }

        /** do the choosin' */
        @Override
        public void actionPerformed(ActionEvent event) {
            show(getRoot(), true);
            if (bMenu.getAction() != this)
                bMenu.setAction( this );
            // done
        }
    } //ActionChooseRoot

    private class ActionGoto extends AbstractAncestrisAction {

        private Bookmark bookmark;
        private boolean freezeRootList = false;

        private ActionGoto(Bookmark bookmark, boolean freezeRootList) {
            this.bookmark = bookmark;
            this.freezeRootList = freezeRootList;
            // setup text
            setText(bookmark.getName());
            if (bookmark.getEntity() != null) {
                setImage(Gedcom.getEntityImage(bookmark.getEntity().getTag()));
            }
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            // let everyone know
            if (bookmark.getEntity() == null) {
                return;
            }
            Context newContext = new Context(bookmark.getEntity());

            try {
                ignoreContextChange = true;
                SelectionDispatcher.fireSelection(newContext);
            } finally {
                ignoreContextChange = false;
            }
            if (freezeRootList) {
                rootList.freeze();
                rootList.setIndex(bookmark);
            }
            setRoot(bookmark.getEntity());
            if (freezeRootList) {
                rootList.unfreeze();
            }
            setContextImpl(newContext);
        }

    }

    public Action getBookmarkAction(Entity e, boolean local) {
        if (e instanceof Indi || e instanceof Fam) {
            return new ActionBookmark(e, local);
        }
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
                setTip(RESOURCES.getString("bookmark.add.tip"));
            } else {
                setText(RESOURCES.getString("bookmark.in"));
                setTip(RESOURCES.getString("bookmark.in.tip"));
            }
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // calculate a name
            String name = entity.getDisplayTitle();

            // Ask for name of bookmark
            String text = RESOURCES.getString("bookmark.name");
            // FL : 10/2019. 
            // We need imput field to be long. DialogID does not work for InputLine
            // => trick : make string longer that 81 characteres to force dialog to display 2 lines
            text += "                                                                                 ".substring(text.length()); 
            final String value = DialogManager.create(TITLE, text, name).show();
            if ( value == null) {
                return;
            } 

            
            // create it
            model.addBookmark(new Bookmark(value, entity));

            // save bookmarks
            {
                Entity root = model.getRoot();
                if (root != null) {
                    REGISTRY.put(root.getGedcom().getName() + ".bookmarks", model.getBookmarks());
                }
            }

            // done
        }
    } //ActionBookmark

    
    private class RootList<E> extends ArrayList<E> {

        // Index indicating the current root in the list
        private int index = -1;
        private boolean isFrozen = false;
        
        public RootList() {
            super();
        }
        
        @Override
        // adding a root always deletes the end of the list from index, and replaces it with the new element
        public boolean add(E e) {
            if (isFrozen) {
                return true;
            }
            for (int i = this.size()-1; i>index; i--) {
                this.remove(i);
            }
            boolean ret = super.add(e);
            index++;
            return ret;
        }

        private void freeze() {
            isFrozen = true;
        }

        private void unfreeze() {
            isFrozen = false;
        }

        private boolean isStart() {
            return index == 0;
        }

        private boolean isEnd() {
            return index == this.size()-1;
        }
        
        private List<E> getList(boolean direction) {

            List<E> ret = new ArrayList<>();
            if (direction) { // forward
                for (int i = index+1; i<this.size(); i++) {
                    ret.add(this.get(i));
                }
            } else { // backward
                for (int i = index-1; i>=0; i--) {
                    ret.add(this.get(i));
                }
            }
            return ret;
        }

        private void setIndex(Bookmark bookmark) {
            for (int i = 0; i<this.size(); i++) {
                if (this.get(i) == bookmark) {
                    index = i;
                    break;
                }
            }
        }

    }
    

    /**
     * Action - settings
     */
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

    private class ActionBluePrint extends AbstractAncestrisContextAction {

        private final ImageIcon IMAGE = new ImageIcon(ChooseBlueprintAction.class, "Blueprint.png");
        private Entity entity;

        public ActionBluePrint() {
            super();
            setImage(IMAGE);
            /*
             * Reset and set image and text to be sure that propertyCHanged event is
             * fired. just after init, image and text are changed and if no change is done
             * on them, the display can be out of sync. PropertertyChangeListeners can only be
             * called after object construction so in our case we must update ui after all 
             * initialisations occurred
             */
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    Icon icon = getImage();
                    String text = getText();
                    String tt = getTip();
                    setImage(IMAGE).setImage(icon);
                    setText("").setText(text);
                    setTip("").setTip(tt);
                }
            });
        }

        @Override
        protected void contextChanged() {
            if (!contextProperties.isEmpty()) {
                Property prop = contextProperties.get(0);
                if (prop.getEntity() instanceof Indi || prop.getEntity() instanceof Fam) {
                    entity = prop.getEntity();
                } else if (entity == null) {
                    entity = prop.getGedcom().getFirstEntity(Gedcom.INDI);
                }
            }
            if (entity == null) {
                entity = new Indi();
            }

            setImageText(IMAGE.getOverLayed(entity.getImage(false)),
                    NbBundle.getMessage(ChooseBlueprintAction.class, "blueprint.select.for", Gedcom.getName(entity.getTag(), true)));
            super.contextChanged();
        }

        @Override
        protected void actionPerformedImpl(final ActionEvent event) {
            if (entity != null) {

                Blueprint styleBlueprint;
                if (entity instanceof Indi) {
                    styleBlueprint = style.blueprintIndi;
                } else {
                    styleBlueprint = style.blueprintFam;
                }
                new ChooseBlueprintAction(entity, styleBlueprint) {

                    @Override
                    protected void commit(Entity recipient, Blueprint blueprint) {
                        
                        // First warn user if current style is not perso style and changes could overwrite it. Ask for confirmation.
                        if (!confirmStyleOverwrite()) {
                            return;
                        }
                        Style persoStyle = getStyleManager().getPersoStyle();
                        setStyle(persoStyle);
                        if (recipient instanceof Indi) {
                            style.blueprintIndi = blueprint;
                        } else {
                            style.blueprintFam = blueprint;
                        }
                        saveStyle();
                        repaint();
                    }
                }.actionPerformed(null);
            }
        }
    }

    private class ActionGotoContext extends AbstractAncestrisContextAction {

        private Entity entity;
        private JButton bMenu = null;
        private boolean isContextPresent = false;

        public ActionGotoContext() {
            this(null);
        }
        public ActionGotoContext(JButton b) {
            super();
            bMenu = b;
            setImage(Images.imgGotoContext);
        }

        @Override
        protected void contextChanged() {
            if (!contextProperties.isEmpty()) {
                Property prop = contextProperties.get(0);
                if (prop.getEntity() instanceof Indi || prop.getEntity() instanceof Fam) {
                    entity = prop.getEntity();
                } else if (entity == null) {
                    entity = prop.getGedcom().getFirstEntity(Gedcom.INDI);
                }
            }
            updateButton();
            super.contextChanged();
        }

        @Override
        protected void actionPerformedImpl(final ActionEvent event) {
            if (!isContextPresent) {
                return;
            }
            TreeView.this.scrollToCurrent(true, true);
            if (bMenu.getAction() != this) {
                bMenu.setAction( this );
            }
        }
        
        // Goto entity must not be null and it must be present in the tree
        public void updateButton() {
            String label = "";
            if (entity != null && model.getNode(entity) != null) {
                label = NbBundle.getMessage(ActionGotoContext.class, "goto.context.tip",entity.getDisplayTitle());
                setImage(Images.imgGotoContext);
                isContextPresent = true;
                setText(label);
                setTip(label);   // this does not seem to update tip once it has been set to contextnotintree
                if (bMenu.getAction() != this) {
                    bMenu.putClientProperty(DropDownButtonFactory.PROP_DROP_DOWN_MENU,
                        Utilities.actionsToPopup(new Action[]{gotoContext,gotoRoot}, org.openide.util.Lookup.EMPTY));
                    bMenu.setAction(this);
                }
            } else {
                label = NbBundle.getMessage(ActionGotoContext.class, "goto.contextnotintree.tip", entity != null ? entity.getDisplayTitle() : "");
                setImage(ImageUtilities.createDisabledIcon((Icon) Images.imgGotoContext));
                isContextPresent = false;
                setText(label);
                setTip(label);
                if (bMenu.getAction() == this) {
                    bMenu.putClientProperty(DropDownButtonFactory.PROP_DROP_DOWN_MENU,
                        Utilities.actionsToPopup(new Action[]{gotoContext,gotoRoot}, org.openide.util.Lookup.EMPTY));
                    bMenu.setAction(gotoRoot);
                }
            }

        }
    }

    private class TreeContext extends ViewContext {

        public TreeContext(Context context) {
            super(context);
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
        if (ent instanceof Indi) {
            return !ents.contains(ent);
        }
        // fam?
        if (ent instanceof Fam) {
            boolean b = ents.contains(ent);
            if (model.isFamilies() || b) {
                return !b;
            }
            Fam fam = (Fam) ent;
            boolean father = ents.contains(fam.getHusband()),
                    mother = ents.contains(fam.getWife()),
                    child = false;
            Indi[] children = fam.getChildren();
            for (int i = 0; child == false && i < children.length; i++) {
                if (ents.contains(children[i])) {
                    child = true;
                }
            }
            // father and mother or parent and child
            return !((father && mother) || (father && child) || (mother && child));
        }
        // let submitter through if it's THE one
        if (model.getRoot().getGedcom().getSubmitter() == ent) {
            return false;
        }
        // maybe a referenced other type?
        Entity[] refs = PropertyXRef.getReferences(ent);
        for (Entity ref : refs) {
            if (ents.contains(ref)) {
                return false;
            }
            Entity[] refs2 = PropertyXRef.getReferences(ref);
            for (Entity ref2 : refs2) {
                if (ents.contains(ref2)) {
                    return false;
                }

            }
        }
        // not
        return true;
    }

    /**
     * A string representation of this view as a filter
     */
    @Override
    public String getFilterName() {
        return NbBundle.getMessage(TreeView.class, "TTL_Filter", getIndividualsCount(), TITLE);
    }

    @Override
    public int getIndividualsCount() {
        int sum = 0;
        for (Entity ent : (Set<Entity>)model.getEntities()) {
            if (ent instanceof Indi) {
                sum++;
            }
        }
        return sum;
    }
    
    @Override
    public boolean canApplyTo(Gedcom gedcom) {
        return (gedcom != null && gedcom.equals(getGedcom()));
    }
} //TreeView
