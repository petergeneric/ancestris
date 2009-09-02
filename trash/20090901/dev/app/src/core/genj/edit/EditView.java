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
package genj.edit;

import genj.edit.actions.Redo;
import genj.edit.actions.Undo;
import genj.edit.beans.BeanFactory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.PopupWidget;
import genj.view.CommitRequestedEvent;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import spin.Spin;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements ToolBarSupport, WindowBroadcastListener, ContextProvider  {
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.edit");
  
  /** instances */
  private static List instances = new LinkedList();

  /** the gedcom we're looking at */
  private Gedcom  gedcom;
  
  /** the registry we use */
  private Registry registry;
  
  /** bean factory */
  private BeanFactory beanFactory;

  /** the view manager */
  private ViewManager manager;
  
  /** the resources we use */
  static final Resources resources = Resources.get(EditView.class);

  /** actions we offer */
  private Sticky   sticky = new Sticky();
  private Back     back = new Back();
  private Forward forward = new Forward();
  private Mode     mode;
  private ContextMenu contextMenu = new ContextMenu();
  private Callback callback = new Callback();
  private Undo undo;
  private Redo redo;
  
  /** whether we're sticky */
  private  boolean isSticky = false;

  /** current editor */
  private Editor editor;
  
  /**
   * Constructor
   */
  public EditView(String setTitle, Gedcom setGedcom, Registry setRegistry, ViewManager setManager) {
    
    super(new BorderLayout());
    
    // remember
    gedcom   = setGedcom;
    registry = setRegistry;
    manager  = setManager;
    beanFactory = new BeanFactory(manager, registry);

    // prepare action
    mode = new Mode();
    undo = new Undo(gedcom);
    redo = new Redo(gedcom);
    
    // run mode switch if applicable
    if (registry.get("advanced", false))
      mode.trigger();
    
    // add keybindings
    InputMap imap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    ActionMap amap = getActionMap();
    imap.put(KeyStroke.getKeyStroke("alt LEFT"), back);
    amap.put(back, back);
    imap.put(KeyStroke.getKeyStroke("alt RIGHT"), forward);
    amap.put(forward, forward);

    // Done
  }
  
  

  
  /**
   * Set editor to use
   */
  private void setEditor(Editor set) {

    // preserve old context and reset current editor to force commit changes
    ViewContext old = null;
    if (editor!=null) {
      old = editor.getContext();
      editor.setContext(new ViewContext(gedcom));
    }
    
    // remove old editor 
    removeAll();
      
    // keep new
    editor = set;
    editor.init(gedcom, this, registry);

    // add to layout
    add(editor, BorderLayout.CENTER);

    // restore old context
    if (old!=null)
      editor.setContext(old);
      
    // show
    revalidate();
    repaint();
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    
    // let super do its thing first
    super.addNotify();    
    
    // remember
    instances.add(this);
    
    // Check if we were sticky
    Entity entity = gedcom.getEntity(registry.get("entity", (String)null));
    if (registry.get("sticky", false) && entity!=null) {
      isSticky = true;
    } else {
      // fallback
      ViewContext context = ContextSelectionEvent.getLastBroadcastedSelection();
      if (context!=null&&context.getGedcom()==gedcom&&gedcom.contains(context.getEntity()))
        entity = context.getEntity();
      // fallback more (only if needed)
      if (entity==null)
        entity = gedcom.getFirstEntity(Gedcom.INDI);
    }
    
    if (entity!=null)
      setContext(new ViewContext(entity));

    // listen to gedcom
    callback.enable();
    gedcom.addGedcomListener((GedcomListener)Spin.over(undo));
    gedcom.addGedcomListener((GedcomListener)Spin.over(redo));
    
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    
    // remember context
    registry.put("sticky", isSticky);
    Entity entity = editor.getContext().getEntity();
    if (entity!=null)
      registry.put("entity", entity.getId());

    // remember mode
    registry.put("advanced", mode.advanced);

    // forget this instance
    instances.remove(this);
    
    // don't listen to gedcom
    callback.disable();
    gedcom.removeGedcomListener((GedcomListener)Spin.over(undo));
    gedcom.removeGedcomListener((GedcomListener)Spin.over(redo));
    
    // Continue
    super.removeNotify();

    // Done
  }
  
  /**
   * BeanFactory
   */
  /*package*/ BeanFactory getBeanFactory() {
    return beanFactory;
  }
  
  /**
   * Ask the user whether he wants to commit changes 
   */
  /*package*/ boolean isCommitChanges() {
    
    // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume 
    // that the containing window was closed and we're not going to throw a dialog out there or do a change
    // behind the covers - we really would need a about-to-close hook for contained components here :(
    if (!getTopLevelAncestor().isVisible())
      return false;
      
    // check for auto commit
    if (Options.getInstance().isAutoCommit)
      return true;
    
    JCheckBox auto = new JCheckBox(resources.getString("confirm.autocomit"));
    auto.setFocusable(false);
    
    int rc = WindowManager.getInstance(this).openDialog(null, 
        resources.getString("confirm.keep.changes"), WindowManager.QUESTION_MESSAGE, 
        new JComponent[] {
          new JLabel(resources.getString("confirm.keep.changes")),
          auto
        },
        Action2.yesNo(), 
        this
    );
    
    if (rc!=0)
      return false;
    
    Options.getInstance().isAutoCommit = auto.isSelected();
    
    return true;
    
  }
  
  /**
   * Return all open instances for given gedcom
   */
  /*package*/ static EditView[] getInstances(Gedcom gedcom) {
    List result = new ArrayList();
    Iterator it = instances.iterator();
    while (it.hasNext()) {
      EditView edit = (EditView)it.next();
      if (edit.gedcom==gedcom)
        result.add(edit);
    }
    return (EditView[])result.toArray(new EditView[result.size()]);
  }
  
  /**
   * ContextProvider callback
   */
  public ViewContext getContext() {
    return editor.getContext();
  }

  /**
   * Context listener callback
   */
  public boolean handleBroadcastEvent(genj.window.WindowBroadcastEvent event) {
    
    // check for commit request
    if (event instanceof CommitRequestedEvent && ((CommitRequestedEvent)event).getGedcom()==gedcom) {
      editor.commit();
      return true;
    }
    
    
    // check for context selection
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, gedcom);
    if (cse==null) 
      return true;
    
    ViewContext context = cse.getContext();
    
    // ignore if no entity info in it
    if (context.getEntity()==null)
      return true;
    
    // an inbound message ?
    if (cse.isInbound()) {
      // set context unless sticky
      if (!isSticky) setContext(context); 
      // don't continue inbound
      return false;
    }
      
    // an outbound message coming from a contained component - we listen for double clicks ourselves
    if (cse.isActionPerformed()) {
      
      if (context.getProperty() instanceof PropertyXRef) {
        
        PropertyXRef xref = (PropertyXRef)context.getProperty();
        xref = xref.getTarget();
        if (xref!=null)
          context = new ViewContext(xref);
      }
      
      // follow
      setContext(context);
      
    }
      
    // let it bubble up outbound
    return true;
  }
  
  public void setContext(ViewContext context) {
    
    // keep track of current editor's context
    ViewContext current = editor.getContext();
    if (current.getEntity()!=context.getEntity())
      back.push(current);

    // tell to editors
    setContextImpl(context);
    
    // done
  }
  
  private void setContextImpl(ViewContext context) {
    
    editor.setContext(context);

    // update title
    context = editor.getContext();
    manager.setTitle(this, context!=null&&context.getEntity()!=null?context.getEntity().toString():"");
    
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {

    // buttons for property manipulation    
    ButtonHelper bh = new ButtonHelper()
      .setInsets(0)
      .setContainer(bar);

    // return in history
    bh.create(back);
    bh.create(forward);
    
    // toggle sticky
    bh.create(sticky, Images.imgStickOn, isSticky);
    
    // add undo/redo
    bh.create(undo);
    bh.create(redo);
    
    // add actions
    bar.add(contextMenu);
    
    // add basic/advanced
    bar.addSeparator();
    bh.create(mode, Images.imgAdvanced, mode.advanced).setFocusable(false);
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,480);
  }
  
  /**
   * whether we're sticky
   */
  public boolean isSticky() {
    return isSticky;
  }
  
  /**
   * Current entity
   */
  public Entity getEntity() {
    return editor.getContext().getEntity();
  }
  
  /**
   * ContextMenu
   */
  private class ContextMenu extends PopupWidget {
    
    /** constructor */
    private ContextMenu() {
      setIcon(Gedcom.getImage());
      setToolTipText(resources.getString( "action.context.tip" ));
    }
    
    /** override - popup creation */
    protected JPopupMenu createPopup() {
      // force editor to commit
      editor.setContext(editor.getContext());
      // create popup
      return manager.getContextMenu(editor.getContext(), this);
    }
     
  } //ContextMenu
  
  /**
   * Action - toggle
   */
  private class Sticky extends Action2 {
    /** constructor */
    protected Sticky() {
      super.setImage(Images.imgStickOff);
      super.setTip(resources, "action.stick.tip");
    }
    /** run */
    protected void execute() {
      isSticky = !isSticky;
    }
  } //Sticky
  
  /**
   * Action - advanced or basic
   */
  private class Mode extends Action2 {
    private boolean advanced = false;
    private Mode() {
      setImage(Images.imgView);
      setEditor(new BasicEditor());
      setTip(resources, "action.mode");
    }
    protected void execute() {
      advanced = !advanced;
      setEditor(advanced ? (Editor)new AdvancedEditor() : new BasicEditor());
    }
  } //Advanced

  /**
   * Forward to a previous context
   */  
  private class Forward extends Back {
    
    /**
     * Constructor
     */
    public Forward() {
      
      // patch looks
      setImage(Images.imgForward);
      setTip(Resources.get(this).getString("action.forward.tip"));
      
    }
    
    /**
     * go forward
     */
    protected void execute() {
      
      if (stack.size()==0)
        return;
      
      // push current on back
      Context old = editor.getContext();
      if (old.getEntities().length>0) {
        back.stack.push(editor.getContext());
        back.setEnabled(true);
      }
      
      // go forward
      ViewContext context = new ViewContext((Context)stack.pop());
      
      // let others know (we'll ignore the outgoing never receiving the incoming)
      WindowManager.broadcast(new ContextSelectionEvent(context, EditView.this));
      setContextImpl(context);
      
      // reflect state
      setEnabled(stack.size()>0);
    }
    
  } //Forward
  
  /**
   * Return to a previous context
   */  
  private class Back extends Action2 {
    
    /** stack of where to go back to  */
    protected Stack stack = new Stack();
    
    /**
     * Constructor
     */
    public Back() {
      
      // setup looks
      setImage(Images.imgBack);
      setTip(Resources.get(this).getString("action.return.tip"));
      setEnabled(false);
      
    }

    /**
     * go back
     */
    protected void execute() {
      if (stack.size()==0)
        return;
      
      // push current on forward
      Context old = editor.getContext();
      if (old.getEntities().length>0) {
        forward.stack.push(editor.getContext());
        forward.setEnabled(true);
      }
      
      // return to last
      ViewContext context = new ViewContext((Context)stack.pop());
      
      // let others know (we'll ignore the outgoing never receiving the incoming)
      WindowManager.broadcast(new ContextSelectionEvent(context, EditView.this));
      setContextImpl(context);
      
      // reflect state
      setEnabled(stack.size()>0);
    }
    
    /** 
     * push another on stack 
     */
    public void push(Context context) {
      // clear forward
      forward.clear();
      // keep it
      stack.push(new Context(context));
      // trim stack - arbitrarily chosen size
      while (stack.size()>32)
        stack.remove(0);
      // we're good
      setEnabled(true);
    }
    
    void clear() {
      stack.clear();
      setEnabled(false);
    }
    
    void remove(Entity entity) {
      // parse stack
      for (Iterator it = stack.listIterator(); it.hasNext(); ) {
        Context ctx = (Context)it.next();
        Entity[] ents = ctx.getEntities();
        for (int i = 0; i < ents.length; i++) {
          if (ents[i]==entity) {
            it.remove();
            break;
          }
        }
      }
      // update status
      setEnabled(!stack.isEmpty());
    }
    
    void remove(Property prop) {
      List list = Collections.singletonList(prop);
      // parse stack
      for (Iterator it = stack.listIterator(); it.hasNext(); ) {
        Context ctx = (Context)it.next();
        ctx.removeProperties(list);
      }
      
    }
  } //Back

  /**
   * Gedcom callback
   */  
  private class Callback extends GedcomListenerAdapter {
    
    void enable() {
      gedcom.addGedcomListener((GedcomListener)Spin.over(this));
      back.clear();
      forward.clear();
    }
    
    void disable() {
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
      back.clear();
      forward.clear();
    }
    
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      back.remove(entity);
      forward.remove(entity);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
      back.remove(removed);
      forward.remove(removed);
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
      // check if we should go back to one
      if (editor.getContext().getEntities().length==0) {
        if (back.isEnabled()) back.execute();
      }
    }
  } //Back
  
} //EditView
