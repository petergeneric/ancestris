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
import java.util.StringTokenizer;


/**
 * A matcher based on simple text containment
 */
public class SimpleMatcher extends Matcher {
  
  /** the words we're looking for */
  private String[] words;
  
  /**
   * @see genj.search.Matcher#init(java.lang.String)
   */
  public void init(String pattern) {
    StringTokenizer tokens = new StringTokenizer(pattern.toLowerCase());
    words = new String[tokens.countTokens()];
    for (int i=0;i<words.length;i++)
      words[i] = tokens.nextToken();
  }
  
  /**
   * @see genj.search.Matcher#match(java.lang.String, java.util.List)
   */
  protected void match(String input, List<Match> result) {
    
    input = input.toLowerCase();
    
    ArrayList<Match> matches = new ArrayList<Match>(words.length);
    
    // search for matches
    for (int i=0;i<words.length;i++) {

      int start = input.indexOf(words[i]);
      if (start<0) 
        return;
      
      while (start>=0) {
        int end = start + words[i].length();
        matches.add(new Match(start, end-start));
        start = input.indexOf(words[i], start+1);
      }
    }
    
    // all found
    result.addAll(matches);
  }

} //RegExMatcher