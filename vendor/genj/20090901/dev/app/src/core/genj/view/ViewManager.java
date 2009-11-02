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
package genj.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.MnemonicAndText;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.MenuHelper;
import genj.window.WindowManager;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import sun.misc.Service;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.view");

  /** resources */
  /*package*/ static Resources RESOURCES = Resources.get(ViewManager.class);
  
  /** global accelerators */
  /*package*/ Map keyStrokes2factories = new HashMap();
  
  /** our context hook */
  private ContextHook contextHook = new ContextHook();
  
  /** factory instances of views */
  private ViewFactory[] factories = null;
  
  /** open views */
  private Map gedcom2factory2handles = new HashMap();
  private LinkedList allHandles = new LinkedList();
  
  /** a window manager */
  private WindowManager windowManager = null;
  
  /**
   * Constructor
   */
  public ViewManager(WindowManager windowManager) {

    // lookup all factories dynamically
    List factories = new ArrayList();
    Iterator it = Service.providers(ViewFactory.class);
    while (it.hasNext()) 
      factories.add(it.next());

    // continue with init
    init(windowManager, factories);
    
    // listen
    GedcomDirectory.getInstance().addListener(new GedcomDirectory.Listener() {
      public void gedcomRegistered(int num, Gedcom gedcom) {
      }
      public void gedcomUnregistered(int num, Gedcom gedcom) {
        closeViews(gedcom);
      }
    });
  }
  
  /**
   * Constructor
   */
  public ViewManager(WindowManager windowManager, String[] factoryTypes) {
    
    // instantiate factories
    List factories = new ArrayList();
    for (int f=0;f<factoryTypes.length;f++) {    
      try {
        factories.add( (ViewFactory)Class.forName(factoryTypes[f]).newInstance() );
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Factory of type "+factoryTypes[f]+" cannot be instantiated", t);
      }
    }
    
    // continue with init
    init(windowManager, factories);
  }
  
  /**
   * get all views for given gedcom 
   */
  public ViewHandle[] getViews(Gedcom gedcom) {
    
    // look for views looking at gedcom    
    List result = new ArrayList();
    for (Iterator handles = allHandles.iterator(); handles.hasNext() ; ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getGedcom()==gedcom)  
        result.add(handle);
    }
    
    // done
    return (ViewHandle[])result.toArray(new ViewHandle[result.size()]);
  }
  
  /**
   * Initialization
   */
  private void init(WindowManager setWindowManager, List setFactories) {
    
    // remember
    windowManager = setWindowManager;
    
    // keep factories
    factories = (ViewFactory[])setFactories.toArray(new ViewFactory[setFactories.size()]);
    
    // loop over factories, grab keyboard shortcuts and sign up context listeners
    for (int f=0;f<factories.length;f++) {    
      ViewFactory factory = factories[f];
      // check shortcut
      String keystroke = "ctrl "+new MnemonicAndText(factory.getTitle(false)).getMnemonic();
      if (!keyStrokes2factories.containsKey(keystroke)) {
        keyStrokes2factories.put(keystroke, factory);
      }
    }
    
    // done
  }
  
  /**
   * Installs global key accelerators for given component
   */
   
  
  /**
   * Returns all known view factories
   */
  public ViewFactory[] getFactories() {
    return factories;
  }
  
  /**
   * Opens settings for given view settings component
   */
  /*package*/ void openSettings(ViewHandle handle) {
    
    // Frame already open?
    SettingsWidget settings = (SettingsWidget)windowManager.getContent("settings");
    if (settings==null) {
      settings = new SettingsWidget(this);
      settings.setView(handle);
      windowManager.openWindow(
        "settings", 
        RESOURCES.getString("view.edit.title"),
        Images.imgSettings,
        settings,
        null, null
      );
    } else {
      settings.setView(handle);
    }
    // done
  }
  
  /**
   * Helper that returns registry for gedcom
   */
  public static Registry getRegistry(Gedcom gedcom) {
    Origin origin = gedcom.getOrigin();
    String name = origin.getFileName();
    return Registry.lookup(name, origin);
  }
  
  /**
   * Get the package name of a Factory
   */
  /*package*/ String getPackage(ViewFactory factory) {
    
    Matcher m = Pattern.compile(".*\\.(.*)\\..*").matcher(factory.getClass().getName());
    if (!m.find())
      throw new IllegalArgumentException("can't resolve package for "+factory);
    return m.group(1);
    
  }

  /**
   * Next in the number of views for given factory
   */
  private int getNextInSequence(Gedcom gedcom, ViewFactory factory) {
    
    // check handles for factory
    Map factories2handles = (Map)gedcom2factory2handles.get(gedcom);
    if (factories2handles==null)
      return 1;
    List handles = (List)factories2handles.get(factory.getClass());
    if (handles==null)
      return 1;
    
    // find first empty spot
    int result = 1;
    for (Iterator it = handles.iterator(); it.hasNext(); ) {
      ViewHandle handle = (ViewHandle)it.next();
      if (handle==null) break;
      result++;
    }
    
    return result;
  }
  
  /**
   * Closes a view
   */
  protected void closeView(ViewHandle handle) {
    // close property editor if open and showing settings
    windowManager.close("settings");
    // now close view
    windowManager.close(handle.getKey());
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    // forget about it
    Map factory2handles = (Map)gedcom2factory2handles.get(handle.getGedcom());
    List handles = (List)factory2handles.get(handle.getFactory().getClass());
    handles.set(handle.getSequence()-1, null);
    allHandles.remove(handle);
    // done
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public ViewHandle openView(Class factory, Gedcom gedcom) {
    for (int f=0; f<factories.length; f++) {
      if (factories[f].getClass().equals(factory)) 
        return openView(gedcom, factories[f]);   	
    }
    throw new IllegalArgumentException("Unknown factory "+factory.getName());
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  public ViewHandle openView(Gedcom gedcom, ViewFactory factory) {
    return openView(gedcom, factory, -1);
  }
  
  /**
   * Opens a view on a gedcom file
   * @return the view component
   */
  protected ViewHandle openView(final Gedcom gedcom, ViewFactory factory, int sequence) {
    
    // figure out what sequence # this view will get
    if (sequence<0)
      sequence = getNextInSequence(gedcom, factory);
    Map factory2handles = (Map)gedcom2factory2handles.get(gedcom);
    if (factory2handles==null) {
      factory2handles = new HashMap();
      gedcom2factory2handles.put(gedcom, factory2handles);
    }
    Vector handles = (Vector)factory2handles.get(factory.getClass());
    if (handles==null) {
      handles = new Vector(10);
      factory2handles.put(factory.getClass(), handles);
    }
    handles.setSize(Math.max(handles.size(), sequence));
    
    // already open?
    if (handles.get(sequence-1)!=null) {
      ViewHandle old = (ViewHandle)handles.get(sequence-1);
      windowManager.show(old.getKey());
      return old;
    }
    
    // get a registry 
    Registry registry = new Registry( getRegistry(gedcom), getPackage(factory)+"."+sequence) ;

    // title 
    String title = gedcom.getName()+" - "+factory.getTitle(false)+" ("+registry.getViewSuffix()+")";

    // create the view
    JComponent view = factory.createView(title, gedcom, registry, this);
    
    // create a handle for it
    final ViewHandle handle = new ViewHandle(this, gedcom, title, registry, factory, view, sequence);
    
    // wrap it into a container
    ViewContainer container = new ViewContainer(handle);

    // add context hook for keyboard shortcuts
    InputMap inputs = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputs.put(KeyStroke.getKeyStroke("shift F10"), contextHook);
    inputs.put(KeyStroke.getKeyStroke("CONTEXT_MENU"), contextHook); // this only works in Tiger 1.5 on Windows
    view.getActionMap().put(contextHook, contextHook);

    // remember
    handles.set(handle.getSequence()-1, handle);
    allHandles.add(handle);

    // prepare to forget
    Action2 close = new Action2() {
      protected void execute() {
        // let us handle close
        closeView(handle);
      }
    };
    
    // open frame
    windowManager.openWindow(handle.getKey(), title, factory.getImage(), container, null,  close);
        
    // done
    return handle;
  }
  
  /**
   * Closes all views on given Gedcom
   */
  public void closeViews(Gedcom gedcom) {
    
    // look for views looking at gedcom    
    ViewHandle[] handles = (ViewHandle[])allHandles.toArray(new ViewHandle[allHandles.size()]);
    for (int i=0;i<handles.length;i++) {
      if (handles[i].getGedcom()==gedcom) 
        closeView(handles[i]);
    }
    
    // done
  }
  
  /** 
   * Show a view (bring it to front)
   */
  public void showView(JComponent view) {

    // loop through views
    for (Iterator handles = allHandles.iterator(); handles.hasNext(); ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getView()==view) {
        windowManager.show(handle.getKey());
        break;
      }
    }
    
    // not found    
  }
  
  /**
   * Set a view's title
   */
  public void setTitle(JComponent view, String title) {
    
    // loop through views
    for (Iterator handles = allHandles.iterator(); handles.hasNext(); ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getView()==view) {
        windowManager.setTitle(handle.getKey(), handle.getTitle() + (title.length()>0 ? " - " + title : ""));
        break;
      }
    }
    
    // not found    
  }

  /**
   * Returns views and factories with given support 
   */
  public Object[] getViews(Class of, Gedcom gedcom) {
    
    List result = new ArrayList(16);
    
    // loop through factories
    for (int f=0; f<factories.length; f++) {
      if (of.isAssignableFrom(factories[f].getClass())) 
        result.add(factories[f]);
    }
    // loop through views
    for (Iterator handles = allHandles.iterator(); handles.hasNext(); ) {
      ViewHandle handle = (ViewHandle)handles.next();
      if (handle.getGedcom()==gedcom && of.isAssignableFrom(handle.getView().getClass()))
        result.add(handle.getView());
    }
    
    // done
    return result.toArray((Object[])Array.newInstance(of, result.size()));
  }
  
  /**
   * Get a context menu
   */
  public JPopupMenu getContextMenu(ViewContext context, Component target) {
    
    // make sure context is valid
    if (context==null)
      return null;
    
    Property[] properties = context.getProperties();
    Entity[] entities = context.getEntities();
    Gedcom gedcom = context.getGedcom();

    // make sure any existing popup is cleared
    MenuSelectionManager.defaultManager().clearSelectedPath();
    
    // hook up context menu to toplevel component - child components are more likely to have been 
    // removed already by the time any of the associated actions are run
    while (target.getParent()!=null) target = target.getParent();

    // create a popup
    MenuHelper mh = new MenuHelper().setTarget(target);
    JPopupMenu popup = mh.createPopup();

    // popup local actions?
    mh.createItems(context.getActions());
    mh.createSeparator(); // it's lazy
  
    // find ActionSupport implementors
    ActionProvider[] as = (ActionProvider[])getViews(ActionProvider.class, context.getGedcom());
    
    // items for set or single property?
    if (properties.length>1) {
      mh.createMenu("'"+Property.getPropertyNames(properties, 5)+"' ("+properties.length+")");
      for (int i = 0; i < as.length; i++) try {
        mh.createSeparator();
        mh.createItems(as[i].createActions(properties, this));
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider threw "+t.getClass()+" on createActions(Property[])", t);
      }
      mh.popMenu();
    }
    if (properties.length==1) {
      Property property = properties[0];
      while (property!=null&&!(property instanceof Entity)&&!property.isTransient()) {
        // a sub-menu with appropriate actions
        mh.createMenu(Property.LABEL+" '"+TagPath.get(property).getName() + '\'' , property.getImage(false));
        for (int i = 0; i < as.length; i++) try {
          mh.createItems(as[i].createActions(property, this));
        } catch (Throwable t) {
          LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Property)", t);
        }
        mh.popMenu();
        // recursively for parents
        property = property.getParent();
      }
    }
        
    // items for set or single entity
    if (entities.length>1) {
      mh.createMenu("'"+Property.getPropertyNames(entities,5)+"' ("+entities.length+")");
      for (int i = 0; i < as.length; i++) try {
        mh.createSeparator();
        mh.createItems(as[i].createActions(entities, this));
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider threw "+t.getClass()+" on createActions(Entity[])", t);
      }
      mh.popMenu();
    }
    if (entities.length==1) {
      Entity entity = entities[0];
      String title = Gedcom.getName(entity.getTag(),false)+" '"+entity.getId()+'\'';
      mh.createMenu(title, entity.getImage(false));
      for (int i = 0; i < as.length; i++) try {
        mh.createItems(as[i].createActions(entity, this));
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Entity)", t);
      }
      mh.popMenu();
    }
        
    // items for gedcom
    String title = "Gedcom '"+gedcom.getName()+'\'';
    mh.createMenu(title, Gedcom.getImage());
    for (int i = 0; i < as.length; i++) try {
      mh.createItems(as[i].createActions(gedcom, this));
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "Action Provider "+as[i].getClass().getName()+" threw "+t.getClass()+" on createActions(Gedcom", t);
    }
    mh.popMenu();

    // done
    return popup;
  }
  
  /**
   * Our hook into keyboard and mouse operated context changes / menues
   */
  private class ContextHook extends Action2 implements AWTEventListener {
    
    /** constructor */
    private ContextHook() {
      try {
        AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            Toolkit.getDefaultToolkit().addAWTEventListener(ContextHook.this, AWTEvent.MOUSE_EVENT_MASK);
            return null;
          }
        });
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Cannot install ContextHook ("+t.getMessage()+")");
      }
    }
    
    /**
     * Resolve context for given component
     */
    private ViewContext getContext(Component component) {
      ViewContext context;
      // find context provider in component hierarchy
      while (component!=null) {
        // component can provide context?
        if (component instanceof ContextProvider) {
          ContextProvider provider = (ContextProvider)component;
          context = provider.getContext();
          if (context!=null)
            return context;
        }
        // try parent
        component = component.getParent();
      }
      // not found
      return null;
    }
    
    /**
     * A Key press initiation of the context menu
     */
    protected void execute() {
      // only for jcomponents with focus
      Component focus = FocusManager.getCurrentManager().getFocusOwner();
      if (!(focus instanceof JComponent))
        return;
      // look for ContextProvider and show menu if appropriate
      ViewContext context = getContext(focus);
      if (context!=null) {
        JPopupMenu popup = getContextMenu(context, focus);
        if (popup!=null)
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
      if (!(me.isPopupTrigger()||me.getID()==MouseEvent.MOUSE_CLICKED))
        return;

      // NM 20080130 do the component/context calculation in another event to allow everyone to catch up
      // Peter reported that the context menu is the wrong one as PropertyTreeWidget
      // changes the selection on mouse clicks (following right-clicks).
      // It might be that eventDispatched() is called before the mouse click is propagated to the
      // component thus calculates the menu before the selection changes.
      // So I'm trying now to show the popup this in a later event to make sure everyone caught up to the event
      
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // find deepest component (since components without attached listeners
          // won't be the source for this event)
          Component component  = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());
          if (!(component instanceof JComponent))
            return;
          Point point = SwingUtilities.convertPoint(me.getComponent(), me.getX(), me.getY(), component );
          
          // try to identify context
          ViewContext context = getContext(component);
          if (context==null) 
            return;
    
          // a double-click on provider?
          if (me.getButton()==MouseEvent.BUTTON1&&me.getID()==MouseEvent.MOUSE_CLICKED&&me.getClickCount()==2) {
            WindowManager.broadcast(new ContextSelectionEvent(context, component, true));
            return;
          }
  
          // a popup?
          if(me.isPopupTrigger())  {
            
            // cancel any menu
            MenuSelectionManager.defaultManager().clearSelectedPath();
            
            // show context menu
            JPopupMenu popup = getContextMenu(context, (JComponent)component);
            if (popup!=null)
              popup.show((JComponent)component, point.x, point.y);
            
          }
        }
      });
        
      // done
    }
    
  } //ContextMenuHook
  

} //ViewManager
