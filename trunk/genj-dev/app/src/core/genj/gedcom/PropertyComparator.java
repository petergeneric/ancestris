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

import java.util.Comparator;

/**
 * A path based comparator of properties where prop[i]<prop[i+1] (unless reversed)
 */
public class PropertyComparator implements Comparator {
  
  /** the path we're sorting by */
  private TagPath path;
  
  /** whether comparison is reversed */
  private int reversed = 1;

  /**
   * Constructor
   */
  public PropertyComparator(String path) {
    this(new TagPath(path));
  }
  
  /**
   * Constructor
   */
  public PropertyComparator(String path, boolean reversed) {
    this(path);
    this.reversed = reversed ? -1 : 1;
  }
  
  /**
   * Constructor
   */
  public PropertyComparator(TagPath path) {
    this.path = path;
  }
  
  /**
   * Accessor - path
   */
  public TagPath getPath() {
    return path;
  }
  
  /**
   * @see java.util.Comparator#compare(Object, Object)
   */
  public int compare(Object o1, Object o2) {
    
    Property 
      p1 = ((Property)o1).getProperty(path),
      p2 = ((Property)o2).getProperty(path);
    
    // null?
    if (p1==p2  ) return  0;
    if (p1==null) return -1 * reversed;
    if (p2==null) return  1 * reversed;
    
    // let p's compare themselves
    return p1.compareTo(p2) * reversed;
    
  }

} //PropertyComparator