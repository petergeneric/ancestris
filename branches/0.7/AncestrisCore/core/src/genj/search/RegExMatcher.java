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

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A matcher based on regular expressions
 */
public class RegExMatcher extends Matcher {
  
  /** the compiled regular expression */
  private Pattern compiled;
  
  /**
   * If someone wants to instantiate this they
   * better use JDK1.4 and higher
   */
  /*package*/ RegExMatcher() {
  }
  
  /**
   * @see genj.search.Matcher#init(java.lang.String)
   */
  public void init(String pattern) {
    try {
      compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.DOTALL);
    } catch (PatternSyntaxException pe) {
      throw new IllegalArgumentException("There's a problem with the regular expression '"+pattern+"': "+pe.getDescription());
    }
  }
  
  /**
   * @see genj.search.Matcher#match(java.lang.String, java.util.List)
   */
  protected void match(String input, List<Match> result) {
    // try to match anything
    java.util.regex.Matcher m = compiled.matcher(input);
    while (true) {
      if (!m.find()) return;
      result.add(new Match(m.start(), m.end()-m.start()));
    }
    // done
  }

} //RegExMatcher