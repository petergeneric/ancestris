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

import genj.gedcom.Property;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for cached PropertyBeans
 */
public class BeanFactory {
  
  private Logger LOG = Logger.getLogger("genj.edit"); 
  
  private final static Class[] beanTypes = {
    EntityBean.class,
    PlaceBean.class, // before choice!
    AgeBean.class,
    ChoiceBean.class,
    DateBean.class,
    EventBean.class,
    FileBean.class,
    MLEBean.class,
    NameBean.class,
    SexBean.class,
    XRefBean.class,
    SimpleValueBean.class // last!
  };
  
  private boolean isRecycling = true;
  
  /** registry used for all beans */
  private Registry registry;
  
  /** cached instances */
  private final static Map property2cached= new HashMap();
  
  /** map a 'proxy' to a resolved type */
  private static Map proxy2type = new HashMap();
  
  /**
   * Constructor
   */
  public BeanFactory(ViewManager viewManager, Registry registry) {
    this.registry = registry;

    if ("false".equals(EnvironmentChecker.getProperty(this, "genj.bean.recycle", "true", "checking whether to recycle beans"))) {
      isRecycling = false;
      Logger.getLogger("genj.edit.beans").log(Level.INFO, "Not recycling beansas genj.bean.recycle=false");
    }
      
  }

  /**
   * Returns a cached property bean of given type name
   */
  public PropertyBean get(String type, Property prop) {
    
    // grab a bean
    PropertyBean bean = getBeanOfType(type);
    
    // set its value
    bean.setProperty(prop);
    
    // done
    return bean;
  }
  
  /**
   * Returns a cached property bean suitable to let the user edit given property
   */
  public PropertyBean get(Property prop) {

    // grab a bean
    PropertyBean bean = getBeanFor(prop);
    
    // set its value
    bean.setProperty(prop);
    
    // done
    return bean;
  }
  
  /**
   * Try to lookup a recycled bean
   */
  private synchronized PropertyBean getBeanOfType(String type) {
    
    try {
      // create new instance
      PropertyBean bean = (PropertyBean)Class.forName(type).newInstance();
      bean.initialize(registry);
      // done
      return bean;
      
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "can't instantiate bean of type "+type, t);
    }
    
    // fallback with new instance
    PropertyBean bean = (PropertyBean)new SimpleValueBean();
    bean.initialize(registry);
    return bean;
  }
  
  /**
   * Try to lookup a recycled bean
   */
  private synchronized PropertyBean getBeanFor(Property prop) {
    // look into cache
    List cached = (List)property2cached.get(prop.getClass());
    if (cached!=null&&!cached.isEmpty()) {
      PropertyBean result = (PropertyBean)cached.remove(cached.size()-1);
      return result;
    }
    // create new instances
    try {
      for (int i=0;i<beanTypes.length;i++) {
        PropertyBean bean = (PropertyBean)beanTypes[i].newInstance();
        if (bean.accepts(prop)) {
          bean.initialize(registry);
          return bean;
        }
      }
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "can't instantiate/init bean for "+prop.getClass().getName(), t);
    }
    return new SimpleValueBean();
  }
  
  /**
   * Recycle a bean
   */
  public synchronized void recycle(PropertyBean bean) {
    
    // no recycling?
    if (!isRecycling) 
      return;
    
    Property property = bean.getProperty();
    if (property==null)
      return;
    // look into cache
    List cached = (List)property2cached.get(property.getClass());
    if (cached==null) {
      cached = new ArrayList();
      property2cached.put(property.getClass(), cached);
    }
    cached.add(bean);
    // done
  }
  
}
