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

/**
 * An interface for properties with multiple lines
 * CONCatenated or CONTinued
 */
public interface MultiLineProperty {
  
  /**
   * Set value as multiline encoded string (containing '\n's)
   */
  public void setValue(String value);
  
  /**
   * Return an iterator for writing lines
   */
  public Collector getLineCollector();

  /**
   * Return an iterator for reading lines
   */
  public Iterator getLineIterator();
  
  /**
   * Collecting multiple lines into a value for a MultiLineProperty 
   */
  public interface Collector {
    
    /**
     * append
     * @return true for consumed, false otherwise
     */
    public boolean append(int indent, String tag, String value);
    
    /**
     * get the resulting value
     */
    public String getValue();
    
  }
  
  /**
   * An iterator for reading multiple lines
   */
  public interface Iterator {
    
    /** change value to iterate over */
    public void setValue(String value);

    /** relative level of indent (normaly 0) */
    public int getIndent();
    
    /** tag for line */
    public String getTag();
    
    /** value for line */
    public String getValue();
    
    /** set to next line */ 
    public boolean next();
    
  } //LineIterator

} //MultiLineSupport
