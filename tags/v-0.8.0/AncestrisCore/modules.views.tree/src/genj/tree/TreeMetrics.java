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
package genj.tree;

/**
 * Metrics a Tree's model is based on (based on millimeters)
 */  
public class TreeMetrics {
    
  /*package*/ int
    wIndis, hIndis,
    wFams, hFams,
    pad;
    
  /**
   * Constructor
   */
  public TreeMetrics(int windis, int hindis, int wfams, int hfams, int padng) {
    // remember
    wIndis = windis;
    hIndis = hindis;
    wFams  = wfams;
    hFams  = hfams;
    pad    = padng;
    // done      
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    // check
    if (!(o instanceof TreeMetrics)) 
      return false;
    // compare
    TreeMetrics other = (TreeMetrics)o;
    return 
      wIndis == other.wIndis&&
      hIndis == other.hIndis&&
      wFams  == other.wFams &&
      hFams  == other.hFams &&
      pad    == other.pad   ;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public int hashCode() {
    return wIndis+hIndis+wFams+hFams+pad;
  }

  /**
   * Calculates the maximum value
   */
  /*package*/ int calcMax() {
    int max = Integer.MIN_VALUE;
    if (wIndis>max) max=wIndis;
    if (hIndis>max) max=hIndis;
    if (wFams >max) max=wFams ;
    if (hFams >max) max=hFams ;
    if (pad   >max) max=pad   ;
    return max;
  }

} //TreeMetrics

