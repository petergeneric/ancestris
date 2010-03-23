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
import genj.gedcom.time.PointInTime;

import java.text.Collator;

import javax.swing.ImageIcon;

/**
 * Gedcom Property : AGE
 */
public class PropertyAge extends Property {

  public final static String TAG = "AGE";
  public final static ImageIcon IMG = Grammar.V55.getMeta(new TagPath("INDI:BIRT:AGE")).getImage();
  
  /** the age */
  private Delta age = new Delta(0, 0, 0);
  private int younger_exactly_older = 0;

  /** as string */
  private String ageAsString;
  
  public static String[] PHRASES = {
    "CHILD", "INFANT", "STILLBORN"
  };

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    
    Collator c = getGedcom().getCollator();
    
    if (ageAsString == null)
      return true;
    for (int i = 0; i < PHRASES.length; i++) 
      if (c.compare(PHRASES[i], ageAsString)==0)
        return true;
    
    return false;
  }

  /**
   * @see genj.gedcom.Property#addNotify(genj.gedcom.Property)
   */
  /*package*/ void afterAddNotify() {
    // continue
    super.afterAddNotify();
    // try to update age
    updateAge();
    // done
  }

  /**
   * Accessor Tag
   */
  public String getTag() {
    return TAG;
  }
  
  /**
   * Label for Age
   */
  public static String getLabelForAge() {
    return Gedcom.getName(TAG);
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  Property init(MetaProperty meta, String value) throws GedcomException {
    meta.assertTag(TAG);
    return super.init(meta, value);
  }

  /**
   * Accessor Value
   */
  public String getValue() {

    if (ageAsString != null)
      return ageAsString;

    // since we're expected to return a Gedcom compliant value
    // here we're not localizing the return value (e.g. 1y 2m 3d)       
    if (younger_exactly_older>0)
      return ">"+age.getValue();
    if (younger_exactly_older<0)
      return "<"+age.getValue();
    return age.getValue();
  }

  /**
   * Accessor Value
   */
  public void setValue(String newValue) {
    String old = getValue();
    
    // try to parse
    if (newValue.startsWith(">")) {
      newValue = newValue.substring(1);
      younger_exactly_older = 1;
    } else if (newValue.startsWith("<")) {
      newValue = newValue.substring(1);
      younger_exactly_older = -1;
    }

    if (age.setValue(newValue))
      ageAsString = null;
    else
      ageAsString = newValue;
    
    // notify
    propagatePropertyChanged(this, old);
    
    // Done
  }

  /**
   * Update the age
   */
  public boolean updateAge() {
    
    String old  = getValue();

    // calc delta
    Delta delta = Delta.get(getEarlier(), getLater());
    if (delta == null)
      return false;
      
    age = delta;
    younger_exactly_older = 0;
    ageAsString = null;

    // notify
    propagatePropertyChanged(this, old);
    
    // done
    return true;
  }

  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    PropertyAge other = (PropertyAge)o;
    if (!isValid()||!other.isValid())
      return super.compareTo(o);
    return age.compareTo(other.age);
  }

  /**
   * Calculates earlier point in time (the birth)
   *
   *  INDI:EVENT:AGE -> INDI:BIRT:DATE
   *
   *  FAM:MARR:HUSB:AGE -> FAM:HUSB -> INDI:BIRT:DATE
   *
   *  FAM:MARR:WIFE:AGE -> FAM:WIFE -> INDI:BIRT:DATE
   */
  public PointInTime getEarlier() {
    Entity e = getEntity();
    // might FAM:MARR:WIFE|HUSB:AGE
    if (e instanceof Fam) {
      Property parent = getParent();
      if (parent.getTag().equals(PropertyHusband.TAG))
        e = ((Fam) e).getHusband();
      if (parent.getTag().equals(PropertyWife.TAG))
        e = ((Fam) e).getWife();
    }
    // check individual?
    if (!(e instanceof Indi))
      return null;
    // date
    PropertyDate birth = ((Indi) e).getBirthDate();
    return birth != null ? birth.getStart() : null;
  }

  /**
   * Calculates later point in time (the event)
   *
   * INDI:EVENT:AGE -> INDI:EVENT:DATE
   *
   * FAM:EVENT:HUSB:AGE -> FAM:EVENT:DATE
   *
   * FAM:EVENT:WIFE:AGE -> FAM:EVENT:DATE
   *
   */
  public PointInTime getLater() {
    Property parent = getParent();
    // might FAM:MARR:WIFE|HUSB:AGE
    if (parent.getTag().equals(PropertyHusband.TAG) || parent.getTag().equals(PropertyWife.TAG)) {
      // one more up
      parent = parent.getParent();
    }
    // check event
    if (!(parent instanceof PropertyEvent))
      return null;
    PropertyDate date = ((PropertyEvent) parent).getDate();
    // start of date
    return date != null ? date.getStart() : null;
  }

} //PropertyAge
