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
package genj.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Matching 
 */
public abstract class Matcher {
  
  /** 
   * init
   */
  public abstract void init(String pattern);
  
  /**
   * match
   */
  public final Match[] match(String value) {
    List result = new ArrayList(100);
    match(value, result);
    return (Match[])result.toArray(new Match[result.size()]);
  }

  /**
   * match (impl)
   */
  protected abstract void match(String value, List result);
  
  /**
   * A match
   */
  public static class Match {
    /** section */
    public int pos, len;
    /** constructor */
    protected Match(int p, int l) { pos=p; len=l; }
  } //Match

} //Matcher