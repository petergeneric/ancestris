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

import genj.gedcom.time.Delta;
import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends Property {
  
  public static ImageIcon IMG = Grammar.V55.getMeta(new TagPath("INDI:EVEN")).getImage();
  
  /** whether the event is known to have happened */
  private boolean knownToHaveHappened;

  /**
   * need tag-argument constructor for all properties
   */
  public PropertyEvent(String tag) {
    super(tag);
  }
  
  /**
   * Returns the date of the event
   */
  public PropertyDate getDate() {
    return getDate(true);
  }

  /**
   * Returns the date of the event
   * @param valid specifies wether data has to be valid to be found
   */
  public PropertyDate getDate(boolean valid) {

    // Try to get date-property which is valid
    Property prop = getProperty("DATE",valid);
    if (prop==null) 
      return null;

    // Return as Date
    return (PropertyDate)prop;
  }

  /**
   * Calculate event's date
   */
  public String getDateAsString() {
    Property date = getProperty("DATE");
    return date!=null ? date.getValue() : "";
  }

  /**
   * Returns the value of this property
   */
  public String getValue() {
    return knownToHaveHappened ? "Y" : "";
  }

  /**
   * Sets the value of this property
   */
  public void setValue(String value) {
    setKnownToHaveHappened(value.toLowerCase().equals("y"));
  }
  
  @Override
  void propagatePropertyChanged(Property property, String oldValue) {
    super.propagatePropertyChanged(property, oldValue);
    
    // sniff for changes in date
    if (property instanceof PropertyDate && getProperty("DATE")==property && getParent() instanceof Indi) {
      // propagate birth changes to co-located other events
      if (getParent().getProperty("BIRT") == this) {
        for (PropertyEvent event : getParent().getProperties(PropertyEvent.class)) {
          if (event!=this)
            event.updateAge((PropertyDate)property);
        }
      } else if (!"BIRT".equals(getTag())){
        updateAge();
      }
    }

    // done
  }


  /**
   * Update age information for this event
   */
  public void updateAge() {
    
    if (!(getParent() instanceof Indi ))
      return;
    
    updateAge( ((Indi)getParent()).getBirthDate() );
    
  }
  
  /*package*/ void updateAge(PropertyDate birt) {
    
    // got a date?
    PropertyDate date = getDate(true);
    if (date==birt)
      return;
    
    // got age?
    PropertyAge age = (PropertyAge) getProperty("AGE");
    if (age==null) {
      if (date==null || !Options.getInstance().isAddAge)
        return;
      age = (PropertyAge)addProperty("AGE", "");
    }
    
    // no age computable?
    if (date==null||birt==null||!birt.isValid()) {
      // leave it alone
      return;
    }
    
    // compute
    if (birt.getStart().compareTo(date.getStart())>=0)
      age.setValue("");
    else
      age.setValue(Delta.get(birt.getStart(), date.getStart()));
    
    // done
  }

  /**
   * Returns the list of paths which identify PropertyEvents
   */
  public static TagPath[] getTagPaths(Gedcom gedcom) {
    return gedcom.getGrammar().getAllPaths(null, PropertyEvent.class);  
  }
  
  /**
   * Access - whether this event is known to have happened
   * @return null if this attribute is not supported, true or false otherwise
   */
  public Boolean isKnownToHaveHappened() {
    // patch - no known EVEN
    if (getTag().equals("EVEN"))
      return null;
    return new Boolean(knownToHaveHappened);
  }

  /**
   * Access - whether this event is known to have happened
   */
  public void setKnownToHaveHappened(boolean set) {
    String old = getValue();
    knownToHaveHappened = set;
    propagatePropertyChanged(this, old);
  }

// Could do an automatic 'y' here but that would pollute
// the gedcom data unnecessary, no?
//  
//  /**
//   * @see genj.gedcom.Property#changeNotify(genj.gedcom.Property, int)
//   */
//  void changeNotify(Property prop, int status) {
//    // continue upwards 
//    super.changeNotify(prop, status);
//    // update known state
//    if (status!=Change.PDEL && prop instanceof PropertyDate) {
//      if (((PropertyDate)prop).isValid()) setKnownToHaveHappened(true);
//    }
//    // done
//  }


} //PropertyEvent
