package genj.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A hashmap that keeps track of keys and their references
 */
public class ReferenceSet<KEY,REF> {

  /** the map we use for key->reference */
  private Map<KEY,Set<REF>> key2references = new HashMap<KEY,Set<REF>>();
  
  /** total number of references we know about */
  private int size = 0;
  
  /**
   * Constructor - uses a TreeMap that keeps
   * keys sorted by their natural order
   */
  public ReferenceSet() {
  }
  
  /**
   * Returns the references for a given key
   */
  public Set<REF> getReferences(KEY key) {
    // null is ignored
    if (key==null) 
      return new HashSet<REF>();
    // lookup
    Set<REF> references = key2references.get(key);
    if (references==null) 
      return new HashSet<REF>();
    // return references
    return references;
  }
  
  /**
   * Returns the total number of references 
   */
  public int getSize() {
    return size;
  }
  
  /**
   * Returns the number of reference for given key
   */
  public int getSize(KEY key) {
    // null is ignored
    if (key==null) 
      return 0;
    // lookup
    Set<REF> references = key2references.get(key);
    if (references==null) 
      return 0;
    // done
    return references.size();
  }

  /**
   * Add a key
   */
  public boolean add(KEY key) {
    return add(key, null);
  }

  /**
   * Add a key and its reference
   * @return whether the reference was actually added (could have been known already) 
   */
  public boolean add(KEY key, REF reference) {
    // null is ignored
    if (key==null) 
      return false;
    // lookup
    Set<REF> references = key2references.get(key);
    if (references==null) {
      references = new HashSet<REF>();
      key2references.put(key, references);
    }
    // safety check for reference==null - might be
    // and still was necessary to keep key    
    if (reference==null)
      return false;
    // add
    if (!references.add(reference)) 
      return false;
    // increase total
    size++;      
    // done
    return true;
  }
  
  /**
   * Remove a reference for given key
   */
  public boolean remove(KEY key, REF reference) {
    // null is ignored
    if (key==null) 
      return false;
    // lookup
    Set<REF> references = key2references.get(key);
    if (references==null) 
      return false;
    // remove
    if (!references.remove(reference))
      return false;
    // decrease total
    size--;
    // remove value
    if (references.isEmpty())
      key2references.remove(key);
    // done
    return true; 
  }
  
  /**
   * Return all keys (in arbitrary order)
   */
  public List<KEY> getKeys() {
    return getKeys(null);
  }
  
  /**
   * Return all keys
   * @param comparator a comparator for sorting the keys or to sort by reference count
   */
  public List<KEY> getKeys(Comparator<Object> comparator) {
    ArrayList<KEY> result = new ArrayList<KEY>(key2references.keySet()); 
    if (comparator!=null) 
      Collections.sort(result, comparator);
    else 
      Collections.sort(result, new Comparator<KEY>() {
        public int compare(KEY o1, KEY o2) {
          return getSize(o1) - getSize(o2);
        }
      });
      
    return result;
  }

} //ReferenceSet
