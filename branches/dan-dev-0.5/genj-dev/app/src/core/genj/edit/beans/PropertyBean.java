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
package genj.edit.beans;

import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyQuality;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.util.ChangeSupport;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ContextProvider;
import genj.view.ViewContext;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/**
 * Beans allow the user to edit gedcom properties
 */
public abstract class PropertyBean extends JPanel implements ContextProvider {
  
  private final static int CACHE_PRELOAD = 10;
  protected final static Resources RESOURCES = Resources.get(PropertyBean.class); 
  protected final static Logger LOG = Logger.getLogger("genj.edit.beans");
  protected final static Registry REGISTRY = Registry.get(PropertyBean.class); 
  private final static Class<?>[] PROPERTY2BEANTYPE = { // TODO beans could be resolved dynamically to allow plugin overrides
    Entity.class                , EntityBean.class,
    PropertyQuality.class       , QualityBean.class,
    PropertyPlace.class         , PlaceBean.class, // before choice!
    PropertyAge.class           , AgeBean.class,
    PropertyChoiceValue.class   , ChoiceBean.class,
    PropertyDate.class          , DateBean.class,
    PropertyEvent.class         , EventBean.class,
    PropertyFile.class          , FileBean.class,
    PropertyBlob.class          , FileBean.class,
    PropertyMultilineValue.class, MLEBean.class,
    PropertyName.class          , NameBean.class,
    PropertySex.class           , SexBean.class,
    PropertyXRef.class          , XRefBean.class,
    Property.class              , SimpleValueBean.class  // last!
  };
  private final static boolean isCache = "true".equals(EnvironmentChecker.getProperty("genj.edit.beans.cache", "true", "checking if bean cache is enabled or not"));
  private final static Map<Class<? extends PropertyBean>,List<PropertyBean>> BEANCACHE = createBeanCache();
  
  /** the context to edit */
  protected Property root;
  protected TagPath path;
  protected Property property;
  protected List<? extends PropertyBean> session;
  
  /** the default focus */
  protected JComponent defaultFocus = null;
  
  /** change support */
  protected ChangeSupport changeSupport = new ChangeSupport(this);


  @SuppressWarnings("unchecked")
  private static Map<Class<? extends PropertyBean>,List<PropertyBean>> createBeanCache() {
    LOG.fine("Initializing bean cache");
    
    Map<Class<? extends PropertyBean>,List<PropertyBean>> result = new HashMap<Class<? extends PropertyBean>,List<PropertyBean>>();
    
    if (isCache) for (int i=0;i<PROPERTY2BEANTYPE.length;i+=2) {
      try {
        List<PropertyBean> cache = new ArrayList<PropertyBean>(CACHE_PRELOAD);
        for (int j=0;j<CACHE_PRELOAD;j++)
          cache.add((PropertyBean)PROPERTY2BEANTYPE[i+1].newInstance());
        result.put((Class<? extends PropertyBean>)PROPERTY2BEANTYPE[i+1], cache);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "can't instantiate bean "+PROPERTY2BEANTYPE[i+1], t);
      }
    }
    return result;
  }

  /**
   * Lookup
   */
  @SuppressWarnings("unchecked")
  public static PropertyBean getBean(Class<? extends Property> property) {
    
    for (int i=0;i<PROPERTY2BEANTYPE.length;i+=2) {
      if (PROPERTY2BEANTYPE[i]!=null&&PROPERTY2BEANTYPE[i].isAssignableFrom(property))
        return getBeanImpl((Class<? extends PropertyBean>)PROPERTY2BEANTYPE[i+1]);
    }

    LOG.warning("Can't find declared bean for property type "+property.getName()+")");
    return getBeanImpl(SimpleValueBean.class);
  }
  
  @SuppressWarnings("unchecked")
  public static PropertyBean getBean(String bean) {
    try {
      return getBeanImpl((Class<? extends PropertyBean>)Class.forName(bean));
    } catch (ClassNotFoundException e) {
      LOG.log(Level.FINE, "Can't find desired bean "+bean, e);
      return getBeanImpl(SimpleValueBean.class);
    }
  }
  
  private static PropertyBean getBeanImpl(Class<? extends PropertyBean> clazz) {
    try {
      // grab from cache if we can
      List<PropertyBean> cache = BEANCACHE.get(clazz);
      if (cache!=null&&!cache.isEmpty()) {
        PropertyBean bean = cache.remove(cache.size()-1);
        if (bean.getParent()==null)
          return bean;
        LOG.log(Level.FINE, "Bean has parent coming out of cache "+bean);
      }
      return ((PropertyBean)clazz.newInstance());
    } catch (Throwable t) {
      LOG.log(Level.FINE, "Problem with bean lookup "+clazz.getName(), t);
      return new SimpleValueBean();
    }
  }
  
  /**
   * recycle an unused bean
   */
  public static void recycle(PropertyBean bean) {
    
    // safety check - still in use?
    if (bean.getParent()!=null)
      throw new IllegalArgumentException("bean still has parent");
    
    // clear state (gc)
    bean.root = null;
    bean.path = null;
    bean.property = null;
    bean.session = null;

    // ignore cache?
    if (!isCache)
      return;

    // cache it
    List<PropertyBean> cache = BEANCACHE.get(bean.getClass());
    if (cache==null) {
      cache = new ArrayList<PropertyBean>();
      BEANCACHE.put(bean.getClass(), cache);
    }
    if (cache.size()<CACHE_PRELOAD)
      cache.add(bean);
  }

  /**
   * Available beans
   */
  public static Set<Class<? extends PropertyBean>> getAvailableBeans() {
    return Collections.unmodifiableSet(BEANCACHE.keySet());
  }

  /** constructor */
  protected PropertyBean() {
    setOpaque(false);  
  }

  /**
   * tell bean to prefer the horizontal instead of the vertical
   */
  public void setPreferHorizontal(boolean set) {
    // bean dependent
  }
  
  /**
   * set property to look at
   */
  public final PropertyBean setContext(Property property) {
    return setContext(property, new TagPath("."), property, new ArrayList<PropertyBean>());
  }
  
  /**
   * set property to look at
   */
  public final PropertyBean setContext(Property root, TagPath path, Property property, List<PropertyBean> session) {
    
    if (root==null||path==null)
      throw new IllegalArgumentException("root and path cannot be null");
    
    this.root = root;
    this.path = path;
    this.property = property;
    this.session = session;

    setPropertyImpl(property);
    
    changeSupport.setChanged(false);
    
    return this;
  }

  protected abstract void setPropertyImpl(Property prop);
  
  /**
   * ContextProvider callback 
   */
  public ViewContext getContext() {
    // ok, this is tricky since some beans might not
    // want to expose a property (is null) and the one
    // we're looking at might actually not be part of 
    // an entity yet - no context in those cases
    // (otherwise other code that relies on properties being
    // part of an entity might break)
    return property==null||property.getEntity()==null ? null : new ViewContext(property);
  }
  
  /**
   * Current Root
   */
  public final Property getRoot() {
    return root;
  }
  
  /**
   * Current Path
   */
  public final TagPath getPath() {
    return path;
  }
  
  /**
   * Current Property
   */
  public final Property getProperty() {
    return property;
  }
  
  /**
   * Whether the bean is valid and can be committed as of current state
   */
  public boolean isCommittable() {
    return true;
  }
  
  /**
   * Whether the bean has changed since first listener was attached
   */
  public boolean hasChanged() {
    return changeSupport.hasChanged();
  }
  
  /**
   * Listener 
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }
  
  /**
   * Listener 
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  /**
   * Commit any changes made by the user
   */
  public final void commit() throws GedcomException {
    // still need target?
    if (property==null)
      property = root.setValue(path, "");
    // let impl do its thing
    commitImpl(property);
    // clear changed
    changeSupport.setChanged(false);
    // nothing more
  }
  
  protected abstract void commitImpl(Property property) throws GedcomException;
  
  /**
   * Editable? default is yes
   */
  public boolean isEditable() {
    return true;
  }
  
  /** 
   * overridden requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    // delegate to default focus
    if (defaultFocus!=null)
      return defaultFocus.requestFocusInWindow();
    return false;
  }

  /** 
   * overridden requestFocus()
   */
  public void requestFocus() {
    // delegate to default focus
    if (defaultFocus!=null)
      defaultFocus.requestFocus();
    else 
      super.requestFocus();
  }
  
  /**
   * Provide available actions
   */
  public List<? extends Action> getActions() {
    return new ArrayList<Action>();
  }
  
  /**
   * A preview component using EntityRenderer for an entity
   */
  public class Preview extends JComponent {
    /** entity */
    private Entity entity;
    /** the blueprint renderer we're using */
    private BlueprintRenderer renderer;
    /**
     * Constructor
     */
    protected Preview() {
      setBorder(new EmptyBorder(4,4,4,4));
    }
    /**
     * @see genj.edit.ProxyXRef.Content#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      Insets insets = getInsets();
      Rectangle box = new Rectangle(insets.left,insets.top,getWidth()-insets.left-insets.right,getHeight()-insets.top-insets.bottom);     
      // clear background
      g.setColor(Color.WHITE); 
      g.fillRect(box.x, box.y, box.width, box.height);
      // render entity
      if (renderer!=null&&entity!=null) 
        renderer.render(g, entity, box);
      // done
    }
    protected void setEntity(Entity ent) {
      entity = ent;
      if (entity!=null)
        renderer = new BlueprintRenderer(BlueprintManager.getInstance().getBlueprint(entity.getTag(), "Edit"));
      repaint();
    }
  } //Preview
  
} //Proxy
