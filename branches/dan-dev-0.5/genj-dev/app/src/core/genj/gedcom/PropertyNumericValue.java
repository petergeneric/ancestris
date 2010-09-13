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
 * Gedcom Property for simple values
 */
public class PropertyNumericValue extends Property {
  
  private Class<?> box = Integer.class; 
  
  /** the numeric value of boxed type */
  private Object value = "";
  
  /**
   * need tag-argument constructor for all properties
   */
  public PropertyNumericValue(String tag) {
    super(tag);
  }

  /**
   * Returns the value of this property
   */
  public String getValue() {
    return value.toString();
  }

  /**
   * Sets the value of this property
   */
  @SuppressWarnings("unchecked")
  public void setValue(String set) {
    
    // grab old
    String old = getValue();
    
    // box value
    try {
      value = (Comparable)box.getConstructor(new Class[]{String.class}).newInstance(new Object[]{set});
    } catch (Throwable t) {
      value = set;
    }
    
    // propagate change
    propagatePropertyChanged(this, old);
  }
  
  /**
   * Compare two numeric values
   */
  @SuppressWarnings("unchecked")
  public int compareTo(Property other) {
    PropertyNumericValue that = (PropertyNumericValue)other;
    // boxes don't match?
    if (that.value.getClass()!=this.value.getClass())
      return super.compareTo(other);
    // let boxes compare
    return ((Comparable<Object>)(this.value)).compareTo((Comparable<Object>)(that.value));
  }
  
} //PropertyNumericValue
