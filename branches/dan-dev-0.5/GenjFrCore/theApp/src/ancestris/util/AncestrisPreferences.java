/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author daniel
 */
public class AncestrisPreferences {
    private Preferences nbPref = null;
    private AncestrisPreferences(Class cls){
        nbPref = NbPreferences.forModule(cls);
    }

    public static AncestrisPreferences get(Class cls){
        return new AncestrisPreferences(cls);
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
   * Returns integer parameter to key
   */
  public int get(String key, int def) {
    return nbPref.getInt(key, def);
  }

  /**
   * Returns String parameter to key
   */
  public String get(String key, String def) {
      return nbPref.get(key, def);
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
   * Remembers an int value
   */
  public void put(String key, int value) {
    nbPref.putInt(key,value);
  }

  /**
   * Remembers a String value
   */
  public void put(String key, String value) {
    nbPref.put(key, value);
  }



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
