/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.Preferences;

/**
 *
 * @author daniel
 */
public class MyPreferences {
      /**
   * Remembers a collection of Strings
   */
  public static void put(Preferences p,String key, Collection values) {

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
   * Returns a collection of strings by key
   */
  public static Collection get(Preferences p, String key, Collection def) {

    // Get size of array
    int size = p.getInt(key,-1);
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
      result.add(p.get(key+"."+(i+1),""));
    }

    // Done
    return result;
  }



}
