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
package genj.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A proxy for calling interfaces without exceptions coming through
 */
public class SafeProxy {
  
  @SuppressWarnings("unchecked")
  public static <T> T unwrap(T object) {
    try {
      return ((SafeHandler<T>)Proxy.getInvocationHandler(object)).impl;
    } catch (Throwable t) {
      throw new IllegalArgumentException("not wrapped");
    }
  }

  /**
   * harden an implementation of interfaces against exceptions 
   * @param implementation implementation to harden
   * @return
   */
  public static<T> T harden(final T implementation) {
    return harden(implementation, Logger.getAnonymousLogger());
  }
  
  public static<T> List<T> harden(final List<T> ts, Logger logger) {
    for (ListIterator<T> li = ts.listIterator(); li.hasNext(); ) {
     li.set(harden(li.next(), logger));
    }
    return ts;
  }
  
  /**
   * harden an implementation of interfaces against exceptions 
   * @param implementation implementation to harden
   * @return
   */
  @SuppressWarnings("unchecked")
  public static<T> T harden(final T implementation, Logger logger) {
    
    // checks
    if (logger==null||implementation==null)
      throw new IllegalArgumentException("implementation|logger==null");

    // interfaces 
    List<Class<?>> interfaces = new ArrayList<Class<?>>();
    Class c = implementation.getClass();
    while (c!=null) {
      for (Class<?> i : c.getInterfaces()) 
        if (Modifier.isPublic(i.getModifiers())&&!interfaces.contains(i)) 
          interfaces.add(i);
      c = c.getSuperclass();
    }

    // create
    return (T)Proxy.newProxyInstance(implementation.getClass().getClassLoader(), interfaces.toArray(new Class<?>[interfaces.size()]), new SafeHandler<T>(implementation, logger));
  }
  
  /** the proxy handler */
  private static class SafeHandler<T> implements InvocationHandler {
    
    private T impl;
    private Logger logger;
    
    private SafeHandler(T impl, Logger logger) {
      this.impl = impl;
      this.logger = logger;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      
      // equals check?
      if ("equals".equals(method.getName()) && args.length==1) try {
        return impl.equals( ((SafeHandler<T>)Proxy.getInvocationHandler(args[0])).impl );
      } catch (IllegalArgumentException e) {
        return false;
      }
      
      Throwable t;
      try {
        return method.invoke(impl, args);
      } catch (InvocationTargetException ite) {
        t = ite.getCause();
      } catch (Throwable tt) {
        t = tt;
      }
      logger.log(Level.WARNING, "Implementation "+impl.getClass().getName() + "." + method.getName()+" threw exception "+t.getClass().getName()+"("+t.getMessage()+")", t);
      return null;
    }
  }

}
