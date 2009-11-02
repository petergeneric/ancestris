/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A Helper to access values via reflection
 */
public class ReflectHelper {
  
  /**
   * Returns the name of a class without package information
   */
  public static String getName(Class<?> clazz) {
    String result = clazz.getName();
    int dot = result.lastIndexOf('.');
    return dot<0 ? result : result.substring(dot+1); 
  }

  /**
   * Returns setters of given instance with given argumentType
   * @name regex for matching method-name
   */
  public static List<Method> getMethods(Object instance, String name, Class<?>[] arguments, boolean isPrefix) {
    
    // loop over methods
    Method[] methods = instance.getClass().getMethods();
    ArrayList<Method> collect = new ArrayList<Method>(methods.length);
    compliance: for (int m=0; m<methods.length; m++) {
      // here's a method
      Method method = methods[m];
      // check prefix
      if (!method.getName().matches(name)) continue;
      // check public
      if (!Modifier.isPublic(method.getModifiers())) continue;
      // check static
      if (Modifier.isStatic(method.getModifiers())) continue;
      // check parameter types
      Class<?>[] ptypes = method.getParameterTypes();
      for (int a=0; a<ptypes.length; a++) {
        if (a>=arguments.length) {
          if (!isPrefix) continue compliance;
          else break;
        }
        if (arguments[a]!=null&&!ptypes[a].isAssignableFrom(arguments[a])) continue compliance;
      }
      collect.add(method);
    }
    
    // done
    return collect;
  }

  /**
   * Returns the properties of given instance (which
   * are all its public attributes)
   * @return a map with field-name/value pairs
   */
  public static List<Property> getProperties(Object instance, boolean primitiveOnly) {
    
    // prepare a result
    List<Property> list = new ArrayList<Property>();
    
    // loop over *public* methods, *no* prefix, *no* argument
    List<Method> methods = getMethods(instance, ".*", new Class[0], false);
    for (Method getter : methods) {
      // check *primitive* result
      if (primitiveOnly&&!(getter.getReturnType().isPrimitive()||getter.getReturnType().isEnum())) 
        continue;
      // check *is* or *get* naming convention
      String name = null;
      if (getter.getName().startsWith("is" )) name = getter.getName().substring(2);
      if (getter.getName().startsWith("get")) name = getter.getName().substring(3);
      if (name==null) continue;
      // check *setter*
      Method setter;
      try {
        String t = "set"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        setter = instance.getClass().getMethod(t, new Class[]{getter.getReturnType()});
      } catch (NoSuchMethodException e) {
        continue;
      }
      // keep 'em
      list.add(new Property(instance, name, getter, setter));
    }
    
    // done
    return list;
  }

  /**
   * Sets a property value
   */
  public static boolean setValue(Property prop, Object value) {
    
    try {
      prop.setValue(value);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }
  
  /**
   * Gets a property value
   */
  public static Object getValue(Property prop) {
    
    try {
      return prop.getValue();
    } catch (Throwable t) {
      return null;
    }
  }
  
  /*
  public static Property[] getProperties(Object instance, boolean declared) {
    
    // prepare a result
    List list = new ArrayList();
    
    // loop over public fields
    Field[] fields = declared ? instance.getClass().getDeclaredFields() : instance.getClass().getFields();
      
    for (int f=0; f<fields.length; f++) {
      Field field = fields[f];
      if (!Modifier.isPublic(field.getModifiers())) continue;
      if (Modifier.isStatic(field.getModifiers())) continue;
      try {
        list.add(new Property(field.getName(), field.get(instance)));
      } catch (IllegalAccessException iae) {
        // ignored
      }
    }
    
    // done
    Property[] result = new Property[list.size()];
    list.toArray(result);
    return result;
  }
  
  public static void setProperties(Object instance, Property[] properties) {
    
    // loop through all values
    for (int p=0; p<properties.length; p++) {
      
      // and set it 
      try {
        Field field = instance.getClass().getField(properties[p].name);
        field.set(instance, wrap(properties[p].value,field.getType()));
      } catch (Throwable t) {
        // ignored
      }
      
      // next
    }
    
    // done
  }
  */
  
  /**
   * Wraps a given value into an single-argument constructor 
   * of given type
   */
  public static Object wrap(Object instance, Class<?> target) {
    // check for primitive wrappers
    if (Boolean.TYPE.equals(target)) {
      target = Boolean.class;
    }
    if (Integer.TYPE.equals(target)) {
      target = Integer.class;
    }
    if (Short.TYPE.equals(target)) {
      target = Short.class;
    }
    if (Character.TYPE.equals(target)) {
      target = Character.class;
    }
    if (Long.TYPE.equals(target)) {
      target = Long.class;
    }
    if (Double.TYPE.equals(target)) {
      target = Double.class;
    }
    // already o.k.?
    if (target.isAssignableFrom(instance.getClass()))
      return instance;
    // try to use the one-argument constructor
    try {
      Constructor<?> constructor = target.getConstructor(new Class[]{ instance.getClass() } );
      return constructor.newInstance(new Object[]{instance});
    } catch (Throwable t) {
      throw new IllegalArgumentException("Couldn't wrap "+instance.getClass()+" in an instance of "+target);
    }
    // done
  }
  
  /**
   * Returns an instance of a named type or null
   */
  public static Object getInstance(String type, Class<?> target) {
    try {
      Class<?> c = Class.forName(type);
      if (!target.isAssignableFrom(c)) return null;
      return c.newInstance();
    } catch (Throwable t) {
      return null;
    }
  }
  
  /**
   * A wrapper for a Property
   */
  public static class Property implements Comparable<Property> {
    /** instance */
    private Object instance;
    /** the name of the property */
    private String name;
    /** the getter method */
    private Method getter;
    /** the setter method */
    private Method setter;
    /** constructor */
    protected Property(Object i, String n, Method g, Method s) {
      instance = i;
      name   = n;
      getter = g;
      setter = s;
    }
    /** name */
    public String getName() {
      return name;
    }
    /** type */
    public Class<?> getType() {
      return getter.getReturnType();
    }
    /** instance */
    public Object getInstance() {
      return instance;
    }
    /** get */
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
      return getter.invoke(instance, new Object[0]);
    }
    /** set */
    public void setValue(Object value) throws IllegalAccessException, InvocationTargetException {
      setter.invoke(instance, new Object[]{ wrap(value, setter.getParameterTypes()[0]) });
    }
    /** string representation */
    @Override
    public String toString() {
      Object value;
      try {
        value = getValue();
      } catch (Throwable t) {
        value = t.toString();
      }
      return name+'='+value;
    }
    /** hierarchy = # superclasses of declared class */
    protected int getHierarchy() {
      int result = 0;
      Class<?> type = getter.getDeclaringClass();
      for (;type!=null;result++) type=type.getSuperclass();
      return result;
    }
    /** @see java.lang.Comparable#compareTo(Object) */
    public int compareTo(Property other) {
      
      int i = other.getHierarchy()-getHierarchy();
      if (i==0) i = name.compareTo(other.name);
      return i;
    }

  } //Property
    
}
