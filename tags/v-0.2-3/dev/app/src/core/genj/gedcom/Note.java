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

import java.util.List;
import java.util.regex.Pattern;




/**
 * Class for encapsulating a note
 */
public class Note extends Entity implements MultiLineProperty {

  /** a delegate for keep the text data crammed in here by Gedcom grammar */
  private PropertyMultilineValue delegate;
  
  /**
   * Notification to entity that it has been added to a Gedcom
   */
  /*package*/ void addNotify(Gedcom ged) {
    
    // continue
    super.addNotify(ged);

    // create a delegate we're using for storing the 
    // note's multiline value
    if (delegate==null) {
      delegate = (PropertyMultilineValue)addProperty("NOTE", "");
      delegate.isTransient = true;
    }
    
    // done
  }

  /**
   * Returns the delegate multiline value property that actually
   * contains this note's content
   */
  public PropertyMultilineValue getDelegate() {
    return delegate;
  }
  
  /**
   * Note ...
   */
  protected String getToStringPrefix(boolean hideIds, boolean showAsLink) {
    return delegate.getDisplayValue();
  }

  /**
   * @see genj.gedcom.Entity#setValue(java.lang.String)
   */
  public void setValue(String newValue) {
    // keep it in delegate
    delegate.setValue(newValue);
  }
  
  /**
   * @see genj.gedcom.Property#delProperty(genj.gedcom.Property)
   */
  public void delProperty(Property which) {
    // ignore request unless not delegate
    if (which!=delegate) 
      super.delProperty(which);
  }

    
  /**
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return delegate.getValue();
  }
  
  public List findProperties(Pattern tag, Pattern value) {
    // let super do its thing
    List result = super.findProperties(tag, value);
    // don't let 'this' in there
    result.remove(this);
    // done
    return result;
  }
  
  /**
   * @see genj.gedcom.MultiLineProperty#getLineIterator()
   */
  public Iterator getLineIterator() {
    return delegate.getLineIterator();
  }

  /**
   * @see genj.gedcom.MultiLineProperty#getLineCollector()
   */
  public Collector getLineCollector() {
    return delegate.getLineCollector();
  }
  
  /**
   * @see genj.gedcom.Property#isPrivate()
   */
  public boolean isPrivate() {
    return delegate.isPrivate();
  }

  /**
   * @see genj.gedcom.Property#setPrivate(boolean, boolean)
   */
  public void setPrivate(boolean set, boolean recursively) {
    delegate.setPrivate(set, recursively);
  }

} //Note
