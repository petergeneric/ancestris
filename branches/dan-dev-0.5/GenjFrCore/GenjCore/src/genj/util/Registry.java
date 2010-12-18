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
 * 
 * $Revision: 1.41 $ $Author: nmeier $ $Date: 2010-01-14 00:20:46 $
 */
package genj.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JFrame;

/**
 * Registry - betterfied java.util.Properties
 */
public class Registry implements PropertyChangeListener {
  

  private String prefix;
  
  private static Map<File, Registry> file2registry = new HashMap<File, Registry>();
  private static Logger LOG =Logger.getLogger("genj");

  private IRegistryStorage storage = null;

  /**
   * Constructor 
   */
  public Registry(Registry registry, String view) {
    if (registry.prefix.length()>0)
      view = registry.prefix + "." + view;
    this.prefix = view;
    LOG.info("set storage to registry storage");
    this.storage = registry.storage;
  }


    protected Registry(IRegistryStorage preference) {
    LOG.info("set storage to preference storage");
        this.storage = preference;
        this.prefix = "";
    }

  /**
   * Constructor for registry loaded from InputStream
   * that can't be looked up and won't be saved
   * @param InputStream to load registry from
   */
  public Registry(InputStream in) {
    // Load settings
    prefix = "";
    LOG.info("set storage to input streams storage");
    storage = RegistryStorageFactory.getFactory().get(in);
  }
  
  private Registry(File file) {
    // Load settings
    prefix = "";
    LOG.info("set storage to file storage");
    storage = RegistryStorageFactory.getFactory().get(file);
  }

  /**
   * Registry representation of a file - changes are committed
   */
  public static Registry get(File file) {
    synchronized (Registry.class) {
      Registry r = file2registry.get(file);
      if (r==null) {
        r = new Registry(file);
        file2registry.put(file, r);
      }
      return r;
    }
  }

  /**
   * Accessor 
   */
  public static Registry get(Object source) {
    return get(source.getClass());
  }
  
  /**
   * Accessor 
   */
  public static Registry get(Class<?> source) {
          return new Registry(RegistryStorageFactory.getFactory().get(source));
  }
  
  /**
   * Accessor 
   */
  public static Registry get(String pckg) {
      return new Registry(RegistryStorageFactory.getFactory().get(pckg));
  }

//  void addPreferenceChangeListener(PreferenceChangeListener pcl){
//      storage.addPreferenceChangeListener(pcl);
//  }
  /**
   * Remove keys
   */
  public void remove(String prefix) {
      if (storage != null){
          storage.remove(prefix);
          return;
      }
  }
  
  /**
   * Returns a map of values
   */
  @SuppressWarnings("unchecked")
  public <K,V> Map<K,V> get(String key, Map<K,V> def) {
    Map<K,V> result = new HashMap<K,V>();
    // loop over keys in map
    for (K subkey : def.keySet()) {
      // grab from default
      V value = def.get(subkey);
      // try to get a better value
      try {
        value = (V)getClass().getMethod("get", new Class[]{ String.class, value.getClass() })
          .invoke(this, new Object[]{ key+"."+subkey, value });
      } catch (Throwable t) {
      }
      // overwrite it
      result.put(subkey, value);
    }
    // done
    return result;
  }

  /**
   * Returns array of ints by key
   */
  public int[] get(String key, int[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size<0)
      return def;

    // Gather array
    int result[] = new int[size];
    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+(i+1),-1);
    }

    // Done
    return result;
  }

  /**
   * Returns array of Rectangles by key
   */
  public Rectangle[] get(String key, Rectangle[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    Rectangle[] result = new Rectangle[size];
    Rectangle empty = new Rectangle(-1,-1,-1,-1);

    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+(i+1),empty);
    }

    // Done
    return result;
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
      result[i] = get(key+"."+(i+1),"");
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
   * Returns dimension parameter by key
   */
  public Dimension get(String key, Dimension def) {

    // Get box dimension
    int w = get(key+".w", -1);
    int h = get(key+".h", -1);

    // Missing ?
    if ( (w==-1) || (h==-1) )
      return def;

    // Done
    return new Dimension(w,h);
  }

  /**
   * Returns font parameter by key
   */
  public Font get(String key, Font def) {

    String face = get(key+".name" ,(String)null);
    int style   = get(key+".style",-1);
    int size    = get(key+".size" ,-1);

    if ( (face==null)||(style==-1)||(size==-1) )
      return def;

    return new Font(face,style,size);
  }

  /**
   * Returns point parameter by key
   */
  public Point get(String key, Point def) {

    // Get box dimension
    int x = get(key+".x", Integer.MAX_VALUE);
    int y = get(key+".y", Integer.MAX_VALUE);

    // Missing ?
    if ( x==Integer.MAX_VALUE || y==Integer.MAX_VALUE )
      return def;

    // Done
    return new Point(x,y);
  }

  /**
   * Returns point parameter by key
   */
  public Point2D get(String key, Point2D def) {

    // Get box dimension
    float x = get(key+".x", Float.NaN);
    float y = get(key+".y", Float.NaN);

    // Missing ?
    if ( Float.isNaN(x) || Float.isNaN(y) )
      return def;

    // Done
    return new Point2D.Double(x,y);
  }

  /**
   * Returns rectangle parameter by key
   */
  public Rectangle get(String key, Rectangle def) {

    // Get box dimension
    int x = get(key+".x", Integer.MAX_VALUE);
    int y = get(key+".y", Integer.MAX_VALUE);
    int w = get(key+".w", Integer.MAX_VALUE);
    int h = get(key+".h", Integer.MAX_VALUE);

    // Missing ?
    if ( (x==Integer.MAX_VALUE) || (y==Integer.MAX_VALUE) || (w==Integer.MAX_VALUE) || (h==Integer.MAX_VALUE) )
      return def;

    // Done
    return new Rectangle(x,y,w,h);
  }

  /**
   * Returns a collection of strings by key
   */
  public List<String> get(String key, List<String> def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Create result
    List<String> result = new ArrayList<String>();
    for (int i=0;i<size;i++) 
      result.add(get(key+"."+(i+1),""));

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
    if (result.equals("1") || result.equals("true"))
      return true;
    if (result.equals("0") || result.equals("false"))
      return false;

    // Done
    return def;
  }

  /**
   * Returns color parameter by key
   */
  public Color get(String key, Color def) {

    // Get property by key
    int result = get(key,Integer.MAX_VALUE);

    // .. existing ?
    if (result==Integer.MAX_VALUE)
      return def;

    // done
    return new Color(result);
  }

  /**
   * Returns String parameter to key
   */
  public String get(String key, String def) {
      LOG.log(Level.FINER, "get registry key {0}", key);
      if (storage != null){
          return storage.get(key,def);
      } else {
          Logger.getLogger("genj").severe("No storage set");
      }
      return def;
  }

  /**
   * Remembers a String value
   */
  public void put(String key, String value) {
      LOG.log(Level.FINER, "put registry key {0}={1}", new String[]{key,value});
      if (storage != null){
          storage.put(key,value);
      } else {
          Logger.getLogger("genj").severe("No storage set");
      }
  }

  /**
   * Remember an array of values
   */
  public void put(String key, Map<String,?> values) {
    
    // loop over keys in map
    for (String subkey : values.keySet()) {
      // grab value
      Object value = values.get(subkey);
      // try to store
      try {
        value = getClass().getMethod("put", new Class[]{ String.class, value.getClass() })
          .invoke(this, new Object[]{ key+"."+subkey, value });
      } catch (Throwable t) {        
      }
    }
    // done
  }
  
  /**
   * Remembers an array of ints
   */
  public void put(String key, int[] value) {

    // Remember
    int l = value.length;
    put(key,l);

    for (int i=0;i<l;i++)
      put(key+"."+(i+1),""+value[i]);

    // Done
  }

  /**
   * Remembers an array of Rectangles
   */
  public void put(String key, Rectangle[] value) {

    // Remember
    int l = value.length;

    put(key,""+l);

    for (int i=0;i<l;i++)
      put(key+"."+(i+1),value[i]);

    // Done
  }

  /**
   * Remembers an array of Strings(Objects)
   */
  public void put(String key, Object value[]) {
    put(key,value,value.length);
  }

  /**
   * Remembers an array of Strings
   */
  public void put(String key, Object value[], int length) {

    // Remember
    int l = Math.min(value.length,length);

    put(key,""+l);

    for (int i=0;i<l;i++) {
      put(key+"."+(i+1),value[i].toString());
    }

    // Done
  }

  /**
   * Remembers an float value
   */
  public void put(String key, float value) {
    put(key,""+value);
  }

  /**
   * Remembers an int value
   */
  public void put(String key, int value) {
    put(key,""+value);
  }

  /**
   * Remembers a point value
   */
  public void put(String key, Dimension value) {

    // Remember box dimension
    put(key+".w",value.width);
    put(key+".h",value.height);

    // Done
  }

  /**
   * Remembers a font value
   */
  public void put(String key, Font value) {

    // Remember box dimension
    put(key+".name" ,value.getName() );
    put(key+".style",value.getStyle());
    put(key+".size" ,value.getSize() );

    // Done
  }

  /**
   * Remembers a point value
   */
  public void put(String key, Point value) {

    // Remember box dimension
    put(key+".x",value.x);
    put(key+".y",value.y);

    // Done
  }


  /**
   * Remembers a point value
   */
  public void put(String key, Point2D value) {

    // Remember box dimension
    put(key+".x",(float)value.getX());
    put(key+".y",(float)value.getY());

    // Done
  }

  /**
   * Remembers a rectangle value
   */
  public void put(String key, Rectangle value) {

    // Remember box dimension
    put(key+".x",value.x);
    put(key+".y",value.y);
    put(key+".w",value.width);
    put(key+".h",value.height);

    // Done
  }

  /**
   * Remembers a collection of Strings
   */
  public void put(String key, Collection<?> values) {

    // Remember
    int l = values.size();
    put(key,l);
    
    Iterator<?> elements = values.iterator();
    for (int i=0;elements.hasNext();i++) {
      put(key+"."+(i+1),elements.next().toString());
    }

    // Done
  }

  /**
   * Remembers a boolean value
   */
  public void put(String key, Boolean value) {

    // Remember
    put(key,(value?"1":"0"));

    // Done
  }
  
  /**
   * Remembers a boolean value
   */
  public void put(String key, Color value) {

    // Remember
    put(key,value.getRGB());

    // Done
  }
    
  /**
   * Save registries
   */
  public static void persist() {
    
    // Go through list of registries that have a file
    for (File file : file2registry.keySet()) {
      Registry registry = file2registry.get(file);
      registry.storage.persist();
    }

    // Done
  }

  /**
   * store JFrame characteristics
   */
  public void put(String key, JFrame frame) {
    Rectangle bounds = frame.getBounds();
    boolean maximized = frame.getExtendedState()==JFrame.MAXIMIZED_BOTH;
    if (bounds!=null&&!maximized)
      put(key, bounds);
    put(key+".maximized", maximized);
  }
  
  public JFrame get(String key, JFrame frame) {
    
    frame.setBounds(get(key, new Rectangle(0,0,640,480)));
    if (get(key+".maximized", true))
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    
    return frame;
  }
 
  public void propertyChange(PropertyChangeEvent evt) {
    String key = evt.getPropertyName();
    Object val = evt.getNewValue();
    if (val==null) {
      remove(key);
      return;
    }
    
    try {
      getClass().getMethod("put", String.class, val.getClass())
      .invoke(this, key, val);
    } catch (Throwable t) {
      put(key, val.toString());
    }
  }

} //Registry
