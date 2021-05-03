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
package genj.gedcom;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Class for encapsulating a path of tags that describe the way through
 * a tree of properties. An example for a path is TagPath("INDI:BIRT:DATE")
 * which denotes the <em>date</em> of property <em>birth</em> of an
 * individual.
 * @author  Nils Meier
 * @version 0.1 04/21/98
 * @version 2004/08/25 made immutable
 */
//XXX: Add desciption for advanced usage
public class TagPath implements Comparable{
  
  /** a logical name */
  private String name = null;

  /** the list of tags that describe the path */
  private String tags[];
  private int qualifiers[];
  
  /** the length (= number of elements) in tags (length<=tags.length) */
  private int len;
  
  /** the hash of this path (immutable) */
  private int hash = 0;
  
  /** our marker */
  public final static char SEPARATOR = ':';
  public final static String SEPARATOR_STRING = String.valueOf(SEPARATOR);
  public final static char SELECTOR = '?';

  public final static String STAY_TAG = ".";
  public final static String MOVEUP_TAG = "..";
  public final static String FOLLOW_TAG = "*";
  
  private static Map<String,TagPath> tagCache = new HashMap<String,TagPath>(5);

  /**
   * Constructor for TagPath
   * @param path path as colon separated string value a:b:c
   * @exception IllegalArgumentException in case format isn't o.k.
   */
  public TagPath(String path) throws IllegalArgumentException {
    this(path, null);
  }
  
  public TagPath(String path, boolean calque) throws IllegalArgumentException {
    this(path.split(SEPARATOR_STRING), null, calque);
  }
  
  public TagPath(String[] path, String name) throws IllegalArgumentException {
      this(path, name, false);
  }
  
  public TagPath(String[] path, String name, boolean calque) throws IllegalArgumentException {
    // keep name
    this.name = name;

    // Parse path
    len = path.length;
    if (len==0)
      throw new IllegalArgumentException("No valid path '"+path+"'");

    // ... setup data
    tags = new String[len];
    qualifiers = new int[len];
    for (int i=0;i<len;i++) {
      
      // next token
      String tag = path[i];
      if (tag.length()==0) 
        throw new IllegalArgumentException("Empty tag in '"+Arrays.toString(path)+"' is not valid");

      // remember
      set(i, tag, calque);
      
    }
    
    // Done
  }
  
  /**
   * Constructor for TagPath
   * @param path path as colon separated string value a:b:c
   * @exception IllegalArgumentException in case format isn't o.k.
   */
  public TagPath(String path, String name) throws IllegalArgumentException {
    this(path.split(SEPARATOR_STRING), name, false);
  }
  
  /**
   * Returns a TagPath instance representing the specified String value. 
   * If a new TagPath instance is not required, this method should generally be used 
   * in preference to the constructor TagPath.TagPath(String),
   * as this method is likely to yield significantly better space and time performance by caching frequently requested values.
   * 
   * @param path a path value
   * @return a TagPath instance representing path
   */
  public static TagPath valueOf(String path){
      TagPath result = tagCache.get(path);
      if (result == null){
          result = new TagPath(path);
          tagCache.put(path, result);
      }
      return result;
  }

  protected void set(int pos, String tag, boolean calque) {
    
    // check qualifier
    int qualifier = -1;
    int separator = tag.indexOf(SELECTOR);
    if (separator>0) {
      try {
        qualifier = Integer.parseInt(tag.substring(separator+1));
        if (calque) {
            qualifier = qualifier -1;
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Illegal tag qualifier in '"+tag+"'");
      }
      tag = tag.substring(0, separator);
    }

    // remember 
    tags[pos] = tag;
    qualifiers[pos] = qualifier;
    hash += tag.hashCode();

  }
  
  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other) {
    this(other, other.len);
  }

  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other, int length) {
    // copyup to len and rehash
    len = length;
    tags = other.tags;
    qualifiers = other.qualifiers;
    for (int i=0; i<len; i++)
      hash += tags[i].hashCode();
    // done
  }

  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other, String tag) {
    
    // setup len
    len = other.len+1;
  
    // copy and append   
    tags = new String[len];
    qualifiers = new int[len];
    
    System.arraycopy(other.tags, 0, tags, 0, other.len);
    System.arraycopy(other.qualifiers, 0, qualifiers, 0, other.len);
    
    tags[len-1] = tag;
    qualifiers[len-1] = -1;
    
    // prepare our hash
    hash = other.hash + tag.hashCode();
  }
  
  /**
   * Constructor for TagPath
   * @param path path as colon separated string value c:b:a
   * @exception IllegalArgumentException in case format isn't o.k.
   * @return the path [a:b:c]
   */
  /*package*/ TagPath(Stack<String> path) throws IllegalArgumentException {
    // grab stack elements
    len = path.size();
    tags = new String[len];
    qualifiers = new int[len];
    for (int i=0;i<len;i++) 
      set(i, path.pop().toString(), false);
    // done
  }
  
  /**
   * Wether this path starts with prefix
   */
  public boolean startsWith(TagPath prefix) {
    // not if longer
    if (prefix.len>len) 
      return false;
    // check
    for (int i=0;i<prefix.len;i++) {
      if (!tags[i].equals(prefix.tags[i]) || qualifiers[i]!=prefix.qualifiers[i]) 
        return false;
    }
    // yes
    return true;
  }

  /**
   * Returns comparison between two TagPaths
   */
  public boolean equals(Object obj) {

    // Me ?
    if (obj==this) 
      return true;

    // TagPath ?
    if (!(obj instanceof TagPath))
      return false;

    // Size ?
    TagPath other = (TagPath)obj;
    if (other.len!=len) 
      return false;

    // Elements ?
    for (int i=0;i<len;i++) {
      if (!tags[i].equals(other.tags[i]) || qualifiers[i]!=other.qualifiers[i]) 
        return false;
    }

    // Equal
    return true;
  }
  
  /**
   * Returns the n-th tag of this path.
   * @param which 0-based number. If negative starts from the end. 
   * get(-1) returns the las element (equivalent to getLast)
   * @return tag as <code>String</code>
   */
  public String get(int which) {
      if (which<0)
          which += len;
      
      if (which<0)
          return null;
    return tags[which];
  }

  /**
   * Returns the first tag of this path
   * @return first tag as <code>String</code>
   */
  public String getFirst() {
    return get(0);
  }

  /**
   * Returns the last tag of this path
   * @return last tag as <code>String</code>
   */
  public String getLast() {
    return get(len-1);
  }

  /**
   * Returns the length of this path
   * @return length of this path
   */
  public int length() {
    return len;
  }
    
  /**
   * Returns the path as a string
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i=0;i<len;i++) {
      if (i>0) result.append(':');
      result.append(tags[i]);
      if (qualifiers[i]>=0) {
        result.append(SELECTOR);
        result.append(qualifiers[i]);
      }
    }
    return result.toString();
  }
  
  /**
   * Returns the path as an array of string string
   */
  public String[] toArray() {
    return tags;
  }
  
  public int[] getQualifiers() {
      return qualifiers;
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return hash;
  }
  
  /**
   * Accessor - name
   */
  public String getName() {
    if (name==null) {
      // try to find a reasonable tag to display as text (that's not '.' or '*')
      int i = length()-1;
      String tag = get(i);
      while (i>1&&!Character.isJavaIdentifierPart(tag.charAt(0))) 
        tag = get(--i);
      
      // as text
      name = Gedcom.getName(tag);
      
      // qualify 2nd level path element (e.g. date or place) for events if possible
      //  BIRT:DATE > Date - Birth
      //  IMMI:PLAC > Date - Immigration
      //  NAME:NICK > Nickname (not "Nickname - Name")
      if (i>1 && Character.isLetter(get(i-1).charAt(0))) {
        String up = Gedcom.getName(get(i-1));
        if (!Pattern.compile(".*"+up+".*", Pattern.CASE_INSENSITIVE).matcher(name).find())
          name = name + " - " + up;
      }
    }
    return name;
  }

  /**
   * Resolve a path from given property
   */
  public static TagPath get(Property prop) {
    
    String p = prop.getTag();
    while (!(prop instanceof Entity)) {
      prop = prop.getParent();
      p = prop.getTag() + SEPARATOR_STRING + p;
    }
    
    // done
    return new TagPath(p);
  }

  /**
   * Get an array out of collection
   */
  public static TagPath[] toArray(Collection<TagPath> c) {
    return c.toArray(new TagPath[c.size()]);
  }
  
  /**
   * Get an array of tag Paths out of an array of strings
   */
  public static TagPath[] toArray(String[] paths) {
    TagPath[] result = new TagPath[paths.length];
    for (int i=0; i<result.length; i++) {
      result[i] = new TagPath(paths[i]);
    }
    return result;
  }

  /**
   * Iterate a properties nodes corresponding to this path
   */
  public void iterate(Property root, PropertyVisitor visitor) {
    iterate(root, visitor, true);
  }
  public void iterate(Property root, PropertyVisitor visitor, boolean backtrack) {
    
    // first tag has to match
    String tag = get(0);
    char c = tag.charAt(0);
    if (c=='.'||c=='*')
      iterate(0, root, visitor, backtrack);
    else if (tag.equals(root.getTag()))
      iterate(1, root, visitor, backtrack);
  }
  
  protected boolean iterate(int pos, Property prop, PropertyVisitor visitor, boolean backtrack) {
    
    String tag;
    
    // follow as far as we can without recursing into children
    for (;;pos++) {
      
      // walked the path?
      if (pos==length()) 
        return visitor.leaf(prop);
      
      // check next tag
      tag = get(pos);
      
       // up?
      if (tag.equals(MOVEUP_TAG)) {
        if (prop.getParent()!=null)
          prop = prop.getParent();
        continue;
      }
      // stay?
      if (tag.equals(STAY_TAG)) {
        continue;
      }
      // follow?
      if (tag.equals(FOLLOW_TAG)) {
        // check out target
        if (!(prop instanceof PropertyXRef)||((PropertyXRef)prop).getTarget()==null)
          return false;
        prop = ((PropertyXRef)prop).getTarget();
        continue;
      }
      
      // looks like we have a child at hand
      break;
    }
    
    return recurse(visitor, prop, tag, pos, backtrack);
  }

    protected boolean recurse(PropertyVisitor visitor, Property prop, String tag, int pos, boolean backtrack) {
        // let visitor know that we're recursing now
        if (!visitor.recursion(prop, tag))
            return false;
        
        // recurse into children
        int qualifier = qualifiers[pos];
        for (int i=0, c=0;i<prop.getNoOfProperties();i++) {
            Property child = prop.getProperty(i);
            if (!backtrack && prop.getProperty(child.getTag())!=child)
                continue;
            if (tag.equals(child.getTag())) {
                if (qualifier<0||qualifier==c++) {
                    if (!iterate(pos+1, child, visitor, backtrack))
                        return false;
                }
            }
        }

        // backtrack
        return true;
    }

  /**
   * tag in path check
   */
  public boolean contains(String tag) {
    for (int i=0;i<len;i++) 
      if (tags[i].equals(tag))
        return true;
    return false;
  }

    @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }
  
} //TagPath
