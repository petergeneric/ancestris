/* * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Workbench;
import genj.app.WorkbenchListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.util.swing.Action2;
import genj.util.swing.MenuHelper;
import genj.view.ActionProvider;
import genj.view.ActionProvider.Purpose;
import genj.view.ContextProvider;
import genj.view.SelectionSink;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;

/**
 * Top component which displays something.
 */
// TODO: regarder en detail cette faq: http://wiki.netbeans.org/DevFaqNonSingletonTopComponents
//TODO: delete@ConvertAsProperties(
//    dtd="-//genjfr.app//ControlCenter//EN",
//    autostore=false
//)
/*
 * on sauvegarde les modes
 * voir http://netbeans.org/bugzilla/show_bug.cgi?id=179526
 * Pour que les modes (TOUS et meme ceux crees par l'utilisateur et donc anonymous)
 * il faut mettre la persistence a ALWAYS
 * or cela conduit a un nombre de fichiers assez important dans le userdir.
 * XXX: il faut donc trouver un moyen pour que la persistence des modes fonctionne.
 * Pour le moment on laisse a ALWAYS
 * Pour qu'un mode soit persistent il doit:
 * - etre non vide
 * - etre marque comme permanent mais ce n'est possible que via le fichier de descrition du mode (donc pas pour les nouveaux modes crees)
 * - Note: il n'existe pas de possibilite de mettre un mode permanent via l'api (voire DefaultModeModel)
 *
 * Les possibilites:
 * - mettre un 'dummy' TC dans les modes pour les rendre permanent
 *   => en attendant une modif des sources
 * - modifier les sources de NB
 *   => trop lourd!
 * - voir si on peut faire comme pour le lifecycle
 *   => non car le DefautModeModel n'est pas instancie via lookup
 * - autre ???
 */
//@RetainLocation("genjfr-editor")
@ServiceProvider(service=WorkbenchListener.class)
//@ServiceProvider(service=GenjViewInterface.class,WorkbenchListener.class)
public class GenjViewTopComponent extends TopComponent implements GenjViewInterface,WorkbenchListener {

//    static GenjViewTopComponent factory;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "GenjViewTopComponent";
    private static javax.swing.JPanel panel;
    private View view=null;
    AToolBar bar = null;
    private boolean isRestored = false;
  private final static Logger LOG = Logger.getLogger("genj.app");
  private final static ContextHook HOOK = new ContextHook();
    private Context context;


    String getDefaultFactoryMode() {return "genjfr-editor";}

    String getDefaultMode(){
        return NbPreferences.forModule(this.getClass()).get(preferredID()+".dockMode",getDefaultFactoryMode());
    }

    public void setDefaultMode(String mode) {
        NbPreferences.forModule(this.getClass()).put(preferredID()+".dockMode", mode);
    }

    public void setDefaultMode(Mode mode) {
        setDefaultMode(mode.getName());
    }

    @Override
    public void open() {
        if (context == null)
            return;
        if (!isRestored) {
            String modeName = App.getRegistry(getGedcom()).get(preferredID()+".dockMode", getDefaultMode()) ;
            
             Mode m = WindowManager.getDefault().findMode (modeName);
             if (m != null) {
                m.dockInto(this);
             }
        }
        super.open();
    }

    public Gedcom getGedcom() {
        return context.getGedcom();
    }
/**
 *
 * @param gedcom
 * @deprecated
 */
    public void setGedcom(Gedcom gedcom) {
        LOG.warning("setGedcom obsolete, try workaround...");
        this.context = new Context(gedcom);
    }

    public View getView() {
        return view;
    }

    /**
     * @deprecated : use GenjFrPlugin.register(this)
     */
    public void addLookup() {
        GenjFrPlugin.register(this);
    }


    public GenjViewTopComponent() {
        super();
        // toutes les fenetres peuvent aller dans tous les modes
            putClientProperty("TopComponentAllowDockAnywhere", Boolean.TRUE); 
    }

    public void setPanel(JPanel jpanel) {

        panel = jpanel;
        if (panel == null) {
            return;
        }

        // setup layout
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }


    void setPanel(Context context, ViewFactory factory) {
        this.context = context;
        if (context == null || context.getGedcom() == null) {
            return;
        }

        // get a registry
//        genj.util.Registry registry = new genj.util.Registry(ViewManager.getRegistry(gedcom), getPackage(factory)/*+"."+sequence*/);
        genj.util.Registry registry = genj.util.Registry.get(getGedcom().getOrigin().getFile(getGedcom().getOrigin().getFileName()+".properties"));

        // title
//        String title = gedcom.getName() + " - " + factory.getTitle() + " (" + registry.getViewSuffix() + ")";
        String title = getGedcom().getName() + " - " + factory.getTitle() ;

        // create the view
        view = factory.createView();
        view.setContext(context, true);
        setPanel(view);
        setToolBar(view);
        GenjFrPlugin.register(this);
    }

    /**
     * Get the package name of a Factory
     */
    /*package*/ String getPackage(ViewFactory factory) {

        Matcher m = Pattern.compile(".*\\.(.*)\\..*").matcher(factory.getClass().getName());
        if (!m.find()) {
            throw new IllegalArgumentException("can't resolve package for " + factory);
        }
        return m.group(1);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Obtain the panel for this view
     */
//    static public JPanel getPanel() {
//        return panel;
//    }

    @Override
    public int getPersistenceType() {
//        return TopComponent.PERSISTENCE_ONLY_OPENED;
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        Mode mode = getMode();
        if (mode == null)
            return;
        for (TopComponent tc: mode.getTopComponents()) {
            if (tc instanceof ModePersisterTopComponent)
                return;
        }
        mode.dockInto(new ModePersisterTopComponent());
    }

    // code pour forcer la persistence des mode (place ici aussi car ne fonctionne pas tjs dans close
    @Override
    public boolean canClose(){
        Mode mode = getMode();
        if (mode == null)
            return true;
        for (TopComponent tc: mode.getTopComponents()) {
            if (tc instanceof ModePersisterTopComponent)
                return true;
        }
        mode.dockInto(new ModePersisterTopComponent());
        return true;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("gedcom",getGedcom().getOrigin().toString());
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        readPropertiesImpl(p);
        return this;
    }

    void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        final String gedName = p.getProperty("gedcom");
//        if (gedName==null) return;
        if (gedName==null)
            close();
        isRestored = true;
        waitStartup(gedName);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public GenjViewTopComponent create() {
        try {
            return this.getClass().newInstance();
            //return Constructor.newInstance(this);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
        //return Constructor.newInstance(this);
    }

    /**
     * Gets an action to display a GenjTopComponent. Used in layer.xml
     * @param component
     * @param displayName
     * @param iconBase
     * @param noIconInMenu
     * @return the action
     */
    static public Action openAction(TopComponent component, String displayName, String iconBase, boolean noIconInMenu) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        map.put("component", component); // NOI18N

        return openAction(map);
    }

    static public Action openAction(Map map) {
//        map.put("iconBase", Images((new EditViewFactory()).getImage().toString())); // NOI18N
//        map.put("noIconInMenu", false); // NOI18N
return                 new OpenGenjViewAction((GenjViewTopComponent) map.get("component"), map);
//
//
//        return Actions.alwaysEnabled(
//                new OpenGenjViewAction((GenjViewTopComponent) map.get("component"), map),
//                (String) map.get("displayName"), // NOI18N
//                (String) map.get("iconBase"), // NOI18N
//                Boolean.TRUE.equals(map.get("noIconInMenu")) // NOI18N
//                );
    }

    ViewFactory getViewFactory() {
        return null;
    }

    void init() {
        init(App.center.getSelectedContext(true));
    }

    public void init(Context context) {
        setName();
        setToolTipText();
        if (getViewFactory() == null)
            return;
        setPanel(context,getViewFactory());
        String gedcomName;
        if ((getGedcom() != null) && ((gedcomName = getGedcom().getName())!=null)){
            setName(gedcomName);
            setToolTipText(getToolTipText() + ": " + gedcomName);
        }
        setIcon(getViewFactory().getImage().getImage());
        // Modification du titre de la fenetre si undockee
        // voir ici: http://old.nabble.com/Look-and-feel-issues-td21583766.html
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent evt) {
                Window w = SwingUtilities.getWindowAncestor(GenjViewTopComponent.this);
                if(w!=null && w instanceof JFrame && ! (w.equals(WindowManager.getDefault().getMainWindow()))){
//                if(w!=null && w instanceof JFrame){
                    ((JFrame)w).setTitle(getName());
                    ((JFrame)w).setIconImage(getIcon());
                }
            }

        });

    }

    //FIXME: revoir la synchro avec le CC
    void waitStartup(String name){
        final String gedName = name;
                new Thread(new Runnable() {
            public void run() {
                while (!App.center.isReady(0))
                        ;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    if (App.center.getOpenedGedcom(gedName) == null)
                        close();
                    else {
                        init(App.center.getOpenedContext(gedName));
                        open();
                    }
                }
            });
            }
        }).start();
    }


    void setName() {
        setName(getViewFactory().getTitle());
    }
    void setToolTipText(){
        setToolTipText(getViewFactory().getTitle());
    }

    public Mode getMode() {
        return WindowManager.getDefault().findMode(this);
    }

    // ToolBar support
    private void setToolBar(View view) {

        if (bar != null) {
            return;
        }
        bar = new AToolBar();

        bar.beginUpdate();
        view.populate(bar);
        bar.endUpdate();
//        if (EnvironmentChecker.getProperty(this, "genj.view.toolbarnoproblem", null, "checking for switch to not use glue in toolbar")!=null)
//          bar.add(Box.createGlue());
//        else
//            bar.addSeparator();

        //    add(bar, viewHandle.getRegistry().get("toolbar", BorderLayout.WEST));
        if ((bar != null) && (bar.getToolBar() != null)){
            add(bar.getToolBar(), genj.util.Registry.get(view).get("toolbar",BorderLayout.WEST));;
        }
    // done
    }

  /**
   * When adding components we fix a Toolbar's sub-component's orientation
   */
    @Override
  protected void addImpl(Component comp, Object constraints, int index) {
    // restore toolbar orientation?
    if ((bar!=null) && (comp==bar.getToolBar())) {
      // remember
      genj.util.Registry.get(view).put("toolbar", constraints.toString());
      // find orientation
      int orientation = SwingConstants.HORIZONTAL;
      if (BorderLayout.WEST.equals(constraints)||BorderLayout.EAST.equals(constraints))
        orientation = SwingConstants.VERTICAL;
      // fix orientation for toolbar
      bar.setOrientation(orientation);
      // toolbar o.k.
    }
    // go ahead with super
    super.addImpl(comp, constraints, index);
    // done
  }

    private class AToolBar implements ToolBar{
        AtomicBoolean notEmpty = new AtomicBoolean(false);
        JToolBar bar = new JToolBar();

     public void add(Action action) {
        bar.add(action);
        bar.setVisible(true);
        notEmpty.set(true);
     }

        public void add(JComponent component) {
        bar.add(component);
        bar.setVisible(true);
        component.setFocusable(false);
        notEmpty.set(true);
      }


      public void addSeparator() {
        bar.addSeparator();
        bar.setVisible(true);
        notEmpty.set(true);
      }

      public JToolBar getToolBar() {
          return (notEmpty.get())?bar:null;
      }

        private void setOrientation(int orientation) {
            bar.setOrientation(orientation);
        }

    public void beginUpdate() {
      notEmpty.set(false);
      bar.removeAll();
      bar.setVisible(false);
//      bar.validate();
    }
    public void endUpdate() {
    }

    }

    // Context menu support

  /**
   * Our hook into keyboard and mouse operated context changes / menu
   */
  private static class ContextHook extends Action2 implements AWTEventListener {

    /** constructor */
    private ContextHook() {
      try {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
          public Void run() {
            Toolkit.getDefaultToolkit().addAWTEventListener(ContextHook.this, AWTEvent.MOUSE_EVENT_MASK);
            return null;
          }
        });
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Cannot install ContextHook", t);
      }
    }

    /**
     * Find workbench for given component
     * @return workbench or null
     */

    /**
     * A Key press initiation of the context menu
     */
    public void actionPerformed(ActionEvent event) {
      // only for jcomponents with focus
      Component focus = FocusManager.getCurrentManager().getFocusOwner();
      if (!(focus instanceof JComponent))
        return;
      // look for ContextProvider and show menu if appropriate
      ViewContext context = new ContextProvider.Lookup(focus).getContext();
      if (context != null) {
        JPopupMenu popup = getContextMenu(context);
        if (popup != null)
          popup.show(focus, 0, 0);
      }
      // done
    }

    /**
     * A mouse click initiation of the context menu
     */
    public void eventDispatched(AWTEvent event) {

      // a mouse popup/click event?
      if (!(event instanceof MouseEvent))
        return;
      final MouseEvent me = (MouseEvent) event;
      if (!(me.isPopupTrigger() || me.getID() == MouseEvent.MOUSE_CLICKED))
        return;

      // NM 20080130 do the component/context calculation in another event to
      // allow everyone to catch up
      // Peter reported that the context menu is the wrong one as
      // PropertyTreeWidget
      // changes the selection on mouse clicks (following right-clicks).
      // It might be that eventDispatched() is called before the mouse click is
      // propagated to the
      // component thus calculates the menu before the selection changes.
      // So I'm trying now to show the popup this in a later event to make sure
      // everyone caught up to the event

      // find workbench now (popup menu might go away after this method call)
//      final Workbench workbench = getWorkbench((Component)me.getSource());
//      if (workbench==null)
//        return;

      // find context at point
      final Component source = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());
      final ContextProvider.Lookup lookup = new ContextProvider.Lookup(source);
      if (lookup.getContext()==null)
        return;

      final Point point = SwingUtilities.convertPoint(me.getComponent(), me.getX(), me.getY(), me.getComponent());

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {

          // a double-click on provider?
          if (lookup.getProvider() == source
              && me.getButton() == MouseEvent.BUTTON1
              && me.getID() == MouseEvent.MOUSE_CLICKED
              && me.getClickCount() == 2) {
            SelectionSink.Dispatcher.fireSelection(me.getComponent(), lookup.getContext(), true);
            return;
          }

          // a popup?
          if (me.isPopupTrigger()) {

            // cancel any menu
            MenuSelectionManager.defaultManager().clearSelectedPath();

            // show context menu
            JPopupMenu popup = getContextMenu(lookup.getContext());
            if (popup != null)
              popup.show(me.getComponent(), point.x, point.y);

          }
        }
      });

      // done
    }

    /**
     * Create a popup menu for given context
     */
    private JPopupMenu getContextMenu(ViewContext context) {

      // make sure context is valid
      if (context==null)
        return null;

      // make sure any existing popup is cleared
      MenuSelectionManager.defaultManager().clearSelectedPath();

      // create a popup
      MenuHelper mh = new MenuHelper();
      JPopupMenu popup = mh.createPopup();

      // popup local actions?
      mh.createItems(context.getActions());

      // get and merge all actions
      List<Action2> groups = new ArrayList<Action2>(8);
      List<Action2> singles = new ArrayList<Action2>(8);
      Map<Action2.Group,Action2.Group> lookup = new HashMap<Action2.Group,Action2.Group>();

      for (Action2 action : getProvidedActions(context)) {
        if (action instanceof Action2.Group) {
          Action2.Group group = lookup.get(action);
          if (group!=null) {
            group.add(new ActionProvider.SeparatorAction());
            group.addAll((Action2.Group)action);
          } else {
            lookup.put((Action2.Group)action, (Action2.Group)action);
            groups.add((Action2.Group)action);
          }
        } else {
          singles.add(action);
        }
      }

      // add to menu
      mh.createItems(groups);
      mh.createItems(singles);

      // done
      return popup;
    }

      private Action2.Group getProvidedActions(Context context) {
      Action2.Group group = new Action2.Group("");
      // ask the action providers
        for (ActionProvider provider : (List<ActionProvider>) GenjFrPlugin.lookupAll(ActionProvider.class) )
        provider.createActions(context, Purpose.CONTEXT, group);
      // done
      return group;
    }


  } //ContextHook

    // Workbench listener support

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    // appropriate?
    if (context.getGedcom()!= this.context.getGedcom()) {
      LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
      return;
    }

    // already known?
    if (!isActionPerformed && this.context.equals(context))
      return;

    LOG.finer("fireSelection("+context+","+isActionPerformed+")");

    // remember
    this.context = context;

    if (context.getGedcom()!=null)
      App.getRegistry(context.getGedcom()).put(context.getGedcom().getName()+".context", context.toString());

        if (view != null)
          view.setContext(context, isActionPerformed);
  }

    public void processStarted(Workbench workbench, Trackable process) {
    }

    public void processStopped(Workbench workbench, Trackable process) {
    }

    public void commitRequested(Workbench workbench, Context context) {
        if (context.getGedcom()!= this.context.getGedcom()) {
          LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
          return;
        }
        if (view != null)
            view.commit();
    }

    public void workbenchClosing(Workbench workbench) {
        if (view != null)
            view.closing();
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    }

    public void viewRestored(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public void viewClosed(Workbench workbench, View view) {
    }

}
