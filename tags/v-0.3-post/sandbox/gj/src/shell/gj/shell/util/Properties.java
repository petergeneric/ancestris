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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Properties - better java.util.Properties
 */
public class Properties {

  /** the contained original java.util.Properties */
  private java.util.Properties content;

  /**
   * Constructor - simple
   */
  public Properties() {
    content = new java.util.Properties();
  }

  /**
   * Constructor - read from file
   */
  public Properties(File file) {

    // Load settings
    try {
      FileInputStream in = new FileInputStream(file);
      content.load(in);
      in.close();
    } catch (Exception ex) {
    }

    // Done
  }

  /**
   * Constructor - read from InputStream
   */
  public Properties(InputStream in) {
    this();
    
    // Load settings
    try {
      content.load(in);
    } catch (Exception ex) {
    }

    // Done
  }
  
  /**
   * Constructor - read from class-local resource
   */
  public Properties(Class<?> type, String name) {
    this();
    
    // Load settings
    try {
      content.load(type.getResourceAsStream(name));
    } catch (Exception ex) {
    }

    // Done    
  }

  /**
   * Returns array of ints by key
   */
  public int[] get(String key, int[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    int result[] = new int[size];
    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+i,-1);
    }

    // Done
    return result;
  }

  /**
   * Returns array of Rectangles by key
   */
  public Rectangle2D[] get(String key, Rectangle2D[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    Rectangle2D[] result = new Rectangle2D[size];
    Rectangle2D empty = new Rectangle2D.Double(-1,-1,-1,-1);

    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+i,empty);
    }

    // Done
    return result;
  }

  /**
   * Returns array of instance of given type
   */
  public Object[] get(String key, Object[] def) {
    
    // Get the data
    String[] types = get(key,new String[0]);
    if (types.length==0)
      return def;
    
    // The target type
    Class<?> target = def.getClass().getComponentType();  
    
    // Loop
    List<Object> result = new ArrayList<Object>();
    
    try {
	    for (int i = 0; i<types.length; i++) {
	      Object instance = ReflectHelper.getInstance(types[i], target);
	      if (instance!=null) 
	        result.add(instance);
	    }
    } catch (Throwable t) {
      
    }
    
    // Done
    return result.toArray((Object[])Array.newInstance(target,result.size()));
  }

  /**
   * Returns array of strings by key
   */
  public String[] get(String key, String[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    String result[] = new String[size];
    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+i,"");
    }

    // Done
    return result;
  }

  /**
   * Returns float parameter to key
   */
  public float get(String key, float def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // .. number ?
    try {
      return Float.valueOf(result.trim()).floatValue();
    } catch (NumberFormatException ex) {
    }

    return def;
  }

  /**
   * Returns integer parameter to key
   */
  public int get(String key, int def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // .. number ?
    try {
      return Integer.parseInt(result.trim());
    } catch (NumberFormatException ex) {
    }

    return def;
  }

  /**
   * Returns rectangle parameter by key
   */
  public Rectangle2D get(String key, Rectangle2D def) {

    // Get box dimension
    int x = get(key+".x", Integer.MAX_VALUE);
    int y = get(key+".y", Integer.MAX_VALUE);
    int w = get(key+".w", Integer.MAX_VALUE);
    int h = get(key+".h", Integer.MAX_VALUE);

    // Missing ?
    if ( (x==Integer.MAX_VALUE) || (y==Integer.MAX_VALUE) || (w==Integer.MAX_VALUE) || (h==Integer.MAX_VALUE) )
      return def;

    // Done
    return new Rectangle2D.Double(x,y,w,h);
  }

  /**
   * Returns String parameter by key
   */
  public String get(String key, String def) {

    // Get property by key
    String result = content.getProperty(key);

    // .. existing ?
    if (result==null)
      return def;

    // .. information ?
    result = result.trim();
    if (result.length()==0)
      return def;

    // Done
    return result;
  }

  /**
   * Returns boolean parameter by key
   */
  public boolean get(String key, boolean def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // boolean value
    if (result.equals("1"))
      return true;
    if (result.equals("0"))
      return false;

    // Done
    return def;
  }

  /**
   * String representation
   */
  @Override
  public String toString() {
    return content.toString();
  }
}
