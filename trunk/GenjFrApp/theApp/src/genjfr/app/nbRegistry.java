/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 * @author daniel
 */
public class nbRegistry {
      /**
   * Remembers a collection of Strings
   */
  public static void put(Properties p,String key, Collection values) {

    // Remember
    int l = values.size();
    p.put(key,""+l);

    Iterator elements = values.iterator();
    for (int i=0;elements.hasNext();i++) {
      p.put(key+"."+(i+1),elements.next().toString());
    }

    // Done
  }

  /**
   * Returns String parameter to key
   */
  public static String get(Properties p,String key, String def) {

    // Get property by key
    String result;
      result = p.getProperty(key);

    if (result==null)
      return def;

    // Done
    return result;
  }

  /**
   * Returns integer parameter to key
   */
  public static int get(Properties p,String key, int def) {

    // Get property by key
    String result = get(p,key,(String)null);

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
   * Returns a collection of strings by key
   */
  public static Collection get(Properties p, String key, Collection def) {

    // Get size of array
    int size = get(p,key,-1);
    if (size==-1)
      return def;

    // Create result
    Collection result;
    if (def == null)
         def = new HashSet();

    try {
      result = (Collection)def.getClass().newInstance();
    } catch (Throwable t) {
      return def;
    }

    // Collection content
    for (int i=0;i<size;i++) {
      result.add(get(p,key+"."+(i+1),""));
    }

    // Done
    return result;
  }



}
