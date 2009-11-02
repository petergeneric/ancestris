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

import genj.edit.beans.BeanFactory;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyVisitor;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.LinkWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.view.ContextProvider;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import spin.Spin;

/**
 * The basic version of an editor for a entity. Tries to hide Gedcom complexity from the user while being flexible in what it offers to edit information pertaining to an entity.
 */
/* package */class BasicEditor extends Editor implements ContextProvider {

  /** keep a cache of descriptors */
  private static Map META2DESCRIPTOR = new HashMap();
  
  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity currentEntity = null;

  /** registry */
  private Registry registry;

  /** edit */
  private EditView view;
  
  /** actions */
  private Action2 ok = new OK(), cancel = new Cancel();
  
  /** current panels */
  private BeanPanel beanPanel;
  private JPanel buttonPanel;
  
  private GedcomListener callback = new Callback();

  /**
   * Callback - init for edit
   */
  public void init(Gedcom gedcom, EditView edit, Registry registry) {

    // remember
    this.gedcom = gedcom;
    this.view = edit;
    this.registry = registry;
    
    // make user focus root
    setFocusTraversalPolicy(new FocusPolicy());
    setFocusCycleRoot(true);

    // create panel for actions
    buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttonPanel);
    bh.create(ok).setFocusable(false);    
    bh.create(cancel).setFocusable(false);
    
    // done
  }
  
  /**
   * Intercepted add notification
   */
  public void addNotify() {
    // let super continue
    super.addNotify();
    // listen to gedcom events
    gedcom.addGedcomListener((GedcomListener)Spin.over(callback));
    // done
  }

  /**
   * Intercepted remove notification
   */
  public void removeNotify() {
    // clean up state
    setEntity(null, null);
    // stop listening to gedcom events
    gedcom.removeGedcomListener((GedcomListener)Spin.over(callback));
    // let super continue
    super.removeNotify();
  }

  /**
   * Callback - our current context
   */
  public ViewContext getContext() {
    // try to find a bean with focus
    PropertyBean bean = getFocus();
    if (bean!=null&&bean.getContext()!=null) 
      return bean.getContext();
    // currently edited?
    if (currentEntity!=null)
      return new ViewContext(currentEntity);
    // gedcom at least
    return new ViewContext(gedcom);
  }

  /**
   * Callback - set current context
   */
  public void setContext(ViewContext context) {
    
    // a different entity to look at?
    if (currentEntity != context.getEntity()) {
      
      // change entity being edited
      setEntity(context.getEntity(), context.getProperty());
      
    } else {

      // simply change focus if possible
      if (beanPanel!=null)
        beanPanel.select(context.getProperty());
      
    }

    // done
  }
  
  @Override
  public void commit() {
    if (ok.isEnabled())
      ok.trigger();
  }
  
  /**
   * Set current entity
   */
  public void setEntity(Entity set, Property focus) {
    
    // commit what needs to be committed
    if (!gedcom.isWriteLocked()&&currentEntity!=null&&ok.isEnabled()&&view.isCommitChanges()) 
      ok.trigger();

    // remember
    currentEntity = set;
    
    // try to find focus receiver if need be
    if (focus==null) {
      // last bean's property would be most appropriate
      PropertyBean bean = getFocus();
      if (bean!=null&&bean.getProperty()!=null&&bean.getProperty().getEntity()==currentEntity) focus  = bean.getProperty();
      // fallback to entity itself
      if (focus==null) focus = currentEntity;
    }
    
    // remove all we've setup to this point
    if (beanPanel!=null) {
      removeAll();
      beanPanel=null;
    }

    // set it up anew
    if (currentEntity!=null) {
      
      try {
        beanPanel = new BeanPanel();
        
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(beanPanel));
        add(BorderLayout.SOUTH, buttonPanel);

      } catch (Throwable t) {
        EditView.LOG.log(Level.SEVERE, "problem changing entity", t);
      }

      // start without ok and cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);

    }
    
    // show
    revalidate();
    repaint();
    
    // set focus
    if (beanPanel!=null)
      beanPanel.select(focus);

    // done
  }
  
  /**
   * Find currently focussed PropertyBean
   */
  private PropertyBean getFocus() {
    
    Component focus = FocusManager.getCurrentManager().getFocusOwner();
    while (focus!=null&&!(focus instanceof PropertyBean))
      focus = focus.getParent();
    
    if (!(focus instanceof PropertyBean))
      return null;
    
    return SwingUtilities.isDescendingFrom(focus, this) ? (PropertyBean)focus : null;

  }

  /**
   * Find a descriptor for given property
   * @return private copy descriptor or null if n/a
   */
  private static NestedBlockLayout getSharedDescriptor(MetaProperty meta) {
    
    // got a cached one already?
    NestedBlockLayout descriptor  = (NestedBlockLayout)META2DESCRIPTOR.get(meta);
    if (descriptor!=null) 
      return descriptor;

    // hmm, already determined we don't have one?
    if (META2DESCRIPTOR.containsKey(meta))
      return null;
    
    // try to read a descriptor (looking up the inheritance chain)
    for (MetaProperty cursor = meta; descriptor==null && cursor!=null ; cursor = cursor.getSuper() ) {
      
      String file  = "descriptors/" + (cursor.isEntity() ? "entities" : "properties") + "/" + cursor.getTag()+".xml";
      
      try {
        InputStream in = BasicEditor.class.getResourceAsStream(file);
        if (in==null) continue;
        descriptor = new NestedBlockLayout(in);
        in.close();
      } catch (IOException e) {
        EditView.LOG.log(Level.WARNING, "problem reading descriptor "+file+" ("+e.getMessage()+")");
      } catch (Throwable t) {
        // 20060601 don't let iae go through - a custom server 404 might return an invalid in
        EditView.LOG.log(Level.WARNING, "problem parsing descriptor "+file+" ("+t.getMessage()+")");
      }
    }
      
      
    // cache it
    META2DESCRIPTOR.put(meta, descriptor);

    // done
    return descriptor;
  }
  
  /**
   * A proxy for a property - it can be used as a container
   * for temporary sub-properties that are not committed to
   * the proxied context 
   */
  private static class PropertyProxy extends Property {
    private Property proxied;
    /** constructor */
    private PropertyProxy(Property prop) {
      this.proxied = prop;
    }
    public Property getProxied() {
      return proxied;
    }
    public boolean isContained(Property in) {
      return proxied==in ? true : proxied.isContained(in);
    }
    public Gedcom getGedcom() { return proxied.getGedcom(); }
    public String getValue() { throw new IllegalArgumentException(); };
    public void setValue(String val) { throw new IllegalArgumentException(); };
    public String getTag() { return proxied.getTag(); }
    public TagPath getPath() { return proxied.getPath(); }
    public MetaProperty getMetaProperty() { return proxied.getMetaProperty(); }
  }
    
  /**
   * A 'bean' we use for groups
   */
  private class PopupBean extends PopupWidget {
    
    private PropertyBean wrapped;
    
    /**
     * constructor
     */
    private PopupBean(PropertyBean wrapped) {
      
      // remember wrapped
      this.wrapped = wrapped;
      wrapped.setAlignmentX(0);
      
      // prepare image
      Property prop = wrapped.getProperty();
      ImageIcon img = prop.getImage(false);
      if (prop.getValue().length()==0)
        img = img.getDisabled(50);
      setIcon(img);
      setToolTipText(prop.getPropertyName());
      
      // fix looks
      setFocusable(false);
      setBorder(null);
      
      // prepare 'actions'
      List actions = new ArrayList();
      actions.add(new JLabel(prop.getPropertyName()));
      actions.add(wrapped);
      setActions(actions);

      // done
    }
    
    /**
     * intercept popup
     */
    public void showPopup() {
      // let super do its thing
      super.showPopup();
      // request focus
      SwingUtilities.getWindowAncestor(wrapped).setFocusableWindowState(true);
      wrapped.requestFocus();
      // update image
      setIcon(wrapped.getProperty().getImage(false));
    }
    
      
  } //Label
  
  /**
   * A ok action
   */
  private class OK extends Action2 {

    /** constructor */
    private OK() {
      setText(Action2.TXT_OK);
    }

    /** cancel current proxy */
    protected void execute() {
      
      // bean panel?
      if (beanPanel==null)
        return;
      
      // commit changes (without listing to the change itself)
      try {
        gedcom.removeGedcomListener((GedcomListener)Spin.over(callback));
        gedcom.doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) {
            beanPanel.commit();
          }
        });
      } finally {
        gedcom.addGedcomListener((GedcomListener)Spin.over(callback));
      }

      // lookup current focus now (any temporary props are committed now)
      PropertyBean focussedBean = getFocus();
      Property focus = focussedBean !=null ? focussedBean.getProperty() : null;
      
      // set selection
      beanPanel.select(focus);

      // done
    }

  } //OK

  /**
   * A cancel action
   */
  private class Cancel extends Action2 {

    /** constructor */
    private Cancel() {
      setText(Action2.TXT_CANCEL);
    }

    /** cancel current proxy */
    protected void execute() {
      // disable ok&cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);

      // re-set for cancel
      setEntity(currentEntity, null);
    }

  } //Cancel

  /**
   * The default container FocusTravelPolicy works based on
   * x/y coordinates which doesn't work well with the column
   * layout used.
   * ContainerOrderFocusTraversalPolicy would do fine but Sun
   * (namely David Mendenhall) in its eternal wisdom has decided
   * to put the working accept()-check into a protected method
   * of LayoutFocusTraversalPolicy basically rendering the
   * former layout useless.
   * I'm doing a hack to get the ContainerOrderFTP with
   * LayoutFTP's accept :(
   */
  private class FocusPolicy extends ContainerOrderFocusTraversalPolicy {
    private Hack hack = new Hack();
    protected boolean accept(Component c) {
      return hack.accept(c);
    }
    private class Hack extends LayoutFocusTraversalPolicy {
      protected boolean accept(Component c) {
        return super.accept(c);
      }
    }
  } //FocusPolicy
  
  /**
   * A panel containing all the beans for editing
   */
  private class BeanPanel extends JPanel implements ChangeListener {

    /** top level tags */
    private Set topLevelTags = new HashSet();
    
    /** beans */
    private List beans = new ArrayList(32);
    
    /** tabs */
    private JTabbedPane tabsPane;
    
    /** constructor */
    BeanPanel() {
      
      // parse entity descriptor
      parse(this, currentEntity, getSharedDescriptor(currentEntity.getMetaProperty()).copy() );
      
      // done
    }
    
    /**
     * destructor - call when panel isn't needed anymore 
     */
    public void removeNotify() {
      
      // get rid of all beans
      removeAll();
      
      // recycle beans
      BeanFactory factory = view.getBeanFactory();
      for (Iterator it=beans.iterator(); it.hasNext(); ) {
        PropertyBean bean = (PropertyBean)it.next();
        bean.removeChangeListener(this);
        bean.setProperty(null);
        try {
          factory.recycle(bean);
        } catch (Throwable t) {
          EditView.LOG.log(Level.WARNING, "Problem cleaning up bean "+bean, t);
        }
      }
      beans.clear();
      
      // done
      super.removeNotify();
      
    }
    
    /**
     * commit beans - transaction has to be running already
     */
    void commit() {
      
      // loop over beans 
      try{
        for (Iterator it = beans.iterator(); it.hasNext();) {
          // check next
          PropertyBean bean = (PropertyBean)it.next();
          if (bean.hasChanged()&&bean.getProperty()!=null) {
            Property prop = bean.getProperty();
            // proxied?
            PropertyProxy proxy = (PropertyProxy)prop.getContaining(PropertyProxy.class);
            if (proxy!=null) 
              prop = proxy.getProxied().setValue(prop.getPathToContaining(proxy), "");
            // commit its changes
            bean.commit(prop);
            // next
          }
        }
      } finally {
        ok.setEnabled(false);
      }
      // done
    }
    
    /**
     * Select a property's bean
     */
    void select(Property prop) {
      if (prop==null||beans.isEmpty())
        return;
      // look for appropriate bean showing prop
      for (Iterator it=beans.iterator(); it.hasNext(); ) {
        PropertyBean bean = (PropertyBean)it.next();
        if (bean.getProperty()==prop) {
          bean.requestFocusInWindow();
          return;
        }
      }
      
      // check if one of the beans' properties is contained in prop
      for (Iterator it=beans.iterator(); it.hasNext(); ) {
        PropertyBean bean = (PropertyBean)it.next();
        if (bean.isDisplayable() && bean.getProperty()!=null && bean.getProperty().isContained(prop)) {
          bean.requestFocusInWindow();
          return;
        }
      }
      
      // check tabs specifically (there might be no properties yet)
      if (tabsPane!=null) {
        Component[] cs  = tabsPane.getComponents();
        for (int i = 0; i < cs.length; i++) {
          JComponent c = (JComponent)cs[i];
          if (c.getClientProperty(Property.class)==prop) {
            c.requestFocusInWindow();
            return;
          }
        }
      }
      
      // otherwise use first bean
      PropertyBean first = (PropertyBean)beans.get(0);
      first.requestFocusInWindow();
      
      // done
    }

    /**
     * ChangeListener callback - a bean tells us about a change made by the user
     */
    public void stateChanged(ChangeEvent e) {
      ok.setEnabled(true);
      cancel.setEnabled(true);
    }
    
    /**
     * Parse descriptor
     */
    private void parse(JPanel panel, Property root, NestedBlockLayout descriptor)  {

      panel.setLayout(descriptor);
      
      // fill cells with beans
      for (Iterator cells = descriptor.getCells().iterator(); cells.hasNext(); ) {
        NestedBlockLayout.Cell cell = (NestedBlockLayout.Cell)cells.next();
        JComponent comp = createComponent(root, cell);
        if (comp!=null) 
          panel.add(comp, cell);
      }
      
      // done
    }
    
    /**
     * Create a component for given cell
     */
    private JComponent createComponent(Property root, NestedBlockLayout.Cell cell) {
      
      String element = cell.getElement();
      
      // right gedcom version?
      String version = cell.getAttribute("gedcom");
      if (version!=null & !root.getGedcom().getGrammar().getVersion().equals(version))
        return null;
      
      // tabs?
      if ("tabs".equals(element)) {
        tabsPane = new ContextTabbedPane();
        
        for (Iterator tabs=cell.getNestedLayouts().iterator(); tabs.hasNext();) {
          NestedBlockLayout tabLayout = (NestedBlockLayout)tabs.next();
          JPanel tab = new JPanel();
          parse(tab, root, tabLayout);
          tabsPane.addTab("", root.getImage(false), tab);
        }
        
        createTabs(tabsPane);
        
        return tabsPane;
      }
      
      // prepare some info and state
      TagPath path = new TagPath(cell.getAttribute("path"));
      MetaProperty meta = root.getMetaProperty().getNestedRecursively(path, false);
      
      // conditional?
      String iff = cell.getAttribute("if"); 
      if (iff!=null&&root.getProperty(new TagPath(iff))==null)
          return null;
      String ifnot = cell.getAttribute("ifnot"); 
      if (ifnot!=null&&root.getProperty(new TagPath(ifnot))!=null)
          return null;
      
      // a label?
      if ("label".equals(element)) {

        JLabel label;
        if (path.length()==1&&path.getLast().equals(currentEntity.getTag()))
          label = new JLabel(meta.getName() + ' ' + currentEntity.getId(), currentEntity.getImage(false), SwingConstants.LEFT);
        else
          label = new JLabel(meta.getName(cell.isAttribute("plural")), meta.getImage(), SwingConstants.LEFT);

        return label;
      }
      
      // a bean?
      if ("bean".equals(element)) {
        // create bean
        PropertyBean bean = createBean(root, path, meta, cell.getAttribute("type"));
        if (bean==null)
          return null;
        // track it
        if (root==currentEntity&&path.length()>1)
          topLevelTags.add(path.get(1));
        // finally wrap in popup if requested?
        return cell.getAttribute("popup")==null ? bean : (JComponent)new PopupBean(bean);
      }

      // bug in the descriptor
      throw new IllegalArgumentException("Template element "+cell.getElement()+" is unkown");
    }
    
    /**
     * create a bean
     * @param root we need the bean for
     * @param path path to property we need bean for
     * @param explicit bean type
     */
    private PropertyBean createBean(Property root, TagPath path, MetaProperty meta, String beanOverride) {

      // try to resolve existing prop - this has to be a property along
      // the first possible path to avoid that in this case:
      //  INDI
      //   BIRT
      //    DATE sometime
      //   BIRT
      //    PLAC somewhere
      // the result of INDI:BIRT:DATE/INDI:BIRT:PLAC is
      //   somtime/somewhere
      // => !backtrack
      Property prop = root.getProperty(path, false);
      
      // addressed property doesn't exist yet? create a proxy that mirrors
      // the root and add create a temporary holder (enjoys the necessary
      // context - namely gedcom)
      if (prop==null) 
        prop = new PropertyProxy(root).setValue(path, "");

      // create bean for property
      BeanFactory factory = view.getBeanFactory();
      PropertyBean bean = beanOverride==null ? factory.get(prop) : factory.get(beanOverride, prop);
      bean.addChangeListener(this);
      beans.add(bean);
      
      // done
      return bean;
    }
    
    /**
     * Create tabs from introspection
     */
    private void createTabs(JTabbedPane tabs) {
      
      // create all tabs
      Set skippedTags = new HashSet();
      for (int i=0, j=currentEntity.getNoOfProperties(); i<j; i++) {
        Property prop = currentEntity.getProperty(i);
        // check tag - skipped or covered already?
        String tag = prop.getTag();
        if (skippedTags.add(tag)&&topLevelTags.contains(tag)) 
          continue;
        topLevelTags.add(tag);
        // create a tab for it
        createTab(prop, tabs);
        // next
      }
      
      // 'create' a tab for creating new properties
      JPanel newTab = new JPanel(new FlowLayout(FlowLayout.LEFT));
      newTab.setPreferredSize(new Dimension(64,64));
      tabs.addTab("", Images.imgNew, newTab);
      
      // add buttons for creating sub-properties 
      MetaProperty[] nested = currentEntity.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN);
      Arrays.sort(nested);
      for (int i=0;i<nested.length;i++) {
        MetaProperty meta = nested[i];
        // if there's a descriptor for it
        NestedBlockLayout descriptor = getSharedDescriptor(meta);
        if (descriptor==null||descriptor.getCells().isEmpty())
          continue;
        // .. and if there's no other already with isSingleton
        if (topLevelTags.contains(meta.getTag())&&meta.isSingleton())
          continue;
        // create a button for it
        newTab.add(new LinkWidget(new AddTab(meta)));
      }
    
      // done
    }
    
    /**
    * Create a tab
    */
   private void createTab(Property prop, JTabbedPane tabs) {
     
     // show simple xref bean for PropertyXRef
     if (prop instanceof PropertyXRef) {
       // don't create tabs for individuals and families
       try {
         String tt = ((PropertyXRef)prop).getTargetType();
         if (tt.equals(Gedcom.INDI)||tt.equals(Gedcom.FAM))
           return;
       } catch (IllegalArgumentException e) {
         // huh? non target type? (like in case of a foreign xref) ... ignore this prop
         return;
       }
       // add a tab for anything else
       tabs.insertTab(prop.getPropertyName(), prop.getImage(false), view.getBeanFactory().get(prop), prop.getPropertyInfo(), 0);
       return;
     }
     
     // got a descriptor for it?
     MetaProperty meta = prop.getMetaProperty();
     NestedBlockLayout descriptor = getSharedDescriptor(meta);
     if (descriptor==null) 
       return;
     
     // create the panel
     JPanel tab = new JPanel();
     tab.putClientProperty(Property.class, prop);

     parse(tab, prop, descriptor.copy());
     tabs.insertTab(meta.getName() + prop.format("{ $y}"), prop.getImage(false), tab, meta.getInfo(), 0);

     // done
   }
   
   private class ContextTabbedPane extends JTabbedPane implements ContextProvider {
     private ContextTabbedPane() {
       super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
     }
     public ViewContext getContext() {
       // check if tab for property
       Component selection = tabsPane.getSelectedComponent();
       Property prop = (Property)((JComponent)selection).getClientProperty(Property.class);
       if (prop==null)
         return null;
       // provide a context with delete
       return new ViewContext(prop).addAction(new DelTab(prop));
     }
   } //ContextTabbedPane
    
  } //BeanPanel
  
  /** An action for adding 'new tabs' */
  private class AddTab extends Action2 {
    
    private MetaProperty meta;
    private Property property;
    
    /** constructor */
    private AddTab(MetaProperty meta) {
      // remember
      this.meta = meta;
      // looks
      setText(meta.getName());
      setImage(meta.getImage());
      setTip(meta.getInfo());
    }
  
    /** callback initiate create */
    protected void execute() {
      
      // safety check
      if (currentEntity==null)
        return;
      
      gedcom.doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) {
          
          // commit bean changes
          if (ok.isEnabled()&&view.isCommitChanges()) 
            beanPanel.commit();
          
          // add property for tab
          property = currentEntity.addProperty(meta.getTag(), "");
        }
      });
      
      // select panel for our new property
      if (beanPanel!=null) SwingUtilities.invokeLater(new Runnable() {
        // not deferring this won't make the focus switch happen :(
        public void run() {
          beanPanel.select(property);
        }
      });
      
      // done
    }
    
  } //AddTab
  
  /**
   * A remove tab action
   */
  private class DelTab extends Action2 {
    private Property prop;
    private DelTab(Property prop) {
      setText(EditView.resources.getString("action.del", prop.getPropertyName()));
      setImage(Images.imgCut);
      this.prop = prop;
    }
   protected void execute() {
     
     // safety check
     if (currentEntity==null)
       return;
     
     gedcom.doMuteUnitOfWork(new UnitOfWork() {
       public void perform(Gedcom gedcom) {
         
         // commit bean changes
         if (ok.isEnabled()&&view.isCommitChanges()) 
           beanPanel.commit();
         
         // delete property
         prop.getParent().delProperty(prop);
         
       }
     });
     

     // done
   }
 }

  /**
   * our gedcom callback for others changing the gedcom information
   */
  private class Callback extends GedcomListenerAdapter {
    
    private Property setFocus;
    
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      setFocus = null;
    }
    
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      setEntity(currentEntity, setFocus);
    }
    
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      if (currentEntity==entity)
        currentEntity = null;
    }
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      if (setFocus==null && property.getEntity()==currentEntity) {
        setFocus = added;
      }
    }
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      if (setFocus==null && property.getEntity()==currentEntity)
        setFocus = property;
    }
  };
  
} //BasicEditor
