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

import java.text.MessageFormat;

import genj.util.Resources;


/**
 * Abstract base type for all Entities - don't make abstract since we actually
 * instantiate this for entities we don't know 
 */
public class Entity extends Property {
  
  /** the containing gedcom */
  private Gedcom gedcom;
  
  /** the id */
  private String id;
  
  /** the tag */
  private String tag;
  
  /** just in case someone's using a value */
  private String value;
  
  /**
   * Lifecycle - callback after being added to Gedcom
   */
  /*package*/ void addNotify(Gedcom ged) {
    // remember
    gedcom = ged;
    // propagate change (see Property.addNotify() for motivation why propagate is here)
    ged.propagateEntityAdded(this);
    // done    
  }
  
  /**
   * Lifecycle - callback before being removed from Gedcom
   */
  /*package*/ void beforeDelNotify() {
    
    // delete children
    delProperties();

    // propagate change (see addNotify() for motivation why propagate is here)
    gedcom.propagateEntityDeleted(this);
    
    // forget gedcom
    gedcom = null;
    
    // done    
  }
  
  /**
   * Return the last change of this entity (might be null)
   */
  public PropertyChange getLastChange() {
    return (PropertyChange)getProperty("CHAN");
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * @see genj.gedcom.Property#getEntity()
   */
  public Entity getEntity() {
    return this;
  }

  /**
   * Changes an entity's ID
   */
  public void setId(String set) throws GedcomException {
    
    // change it
    String old = id;
    id = set;
    
    // tell Gedcom about it
    if (gedcom!=null) try {
      gedcom.propagateEntityIDChanged(this, old);
    } catch (Throwable t) {
      id = old;
    }

    // done
  }
  
  /**
   * Returns entity's id
   * @return id
   */
  public String getId() {
    return id;
  }
  
  /**
   * Initialize entity
   */
  /*package*/ void init(String setTag, String setId) {
    tag = setTag;
    id = setId;
  }
  
  /**
   * @see genj.gedcom.Property#toString()
   */
  public final String toString() {
    return toString(true, false);
  }
  
  public final String toString(boolean showIds, boolean showAsLink) {
    
    StringBuffer buf = new StringBuffer();
    buf.append(getToStringPrefix(showIds, showAsLink));
    if (buf.length()==0)
      buf.append(getTag());
    if (showIds) {
      buf.append(" (");
      String linkToId = getIdLinkFormat();
   if (showAsLink && ! linkToId.equals("")) {
  MessageFormat fmt = Resources.getMessageFormat(linkToId);
  buf.append(fmt.format(new String[]{""+getId()}));
   }else {
        buf.append(getId());
   }
      buf.append(')');
    }
    return buf.toString();
  }

  protected String getIdLinkFormat() {
 return genj.report.Options.getInstance().getLinkToId();
  }

  protected String getToStringPrefix(boolean showIds, boolean showAsLink) {
    return getTag();
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return tag;
  }
  
  /**
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return value!=null?value : "";
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String set) {
    value = set;
  }

  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    try {
      Entity other = (Entity)o;
      return getID() - other.getID(); 
    } catch (Throwable t) {
    }
    return super.compareTo(o);
  }

  /**
   * Returns a comparable id
   */
  private int getID() throws NumberFormatException {
    
    int 
      start = 0,
      end   = id.length()-1;
      
    while (start<=end&&!Character.isDigit(id.charAt(start))) start++;
    while (end>=start&&!Character.isDigit(id.charAt(end))) end--;

    if (end<start) throw new NumberFormatException();
         
    return Integer.parseInt(id.substring(start, end+1));
  }

  /**
   * Format a sub-property of this entity
   * @see Property#format(String)
   */
  public String format(String propertyTag, String format) {
    Property p = getProperty(propertyTag);
    return p!=null ? p.format(format) : "";
  }

  /**
   * Propagate changed property
   */
  void propagateXRefLinked(PropertyXRef property1, PropertyXRef property2) {
    if (gedcom!=null)
      gedcom.propagateXRefLinked(property1, property2);
  }

  void propagateXRefUnlinked(PropertyXRef property1, PropertyXRef property2) {
    if (gedcom!=null)
      gedcom.propagateXRefUnlinked(property1, property2);
  }

  void propagatePropertyAdded(Property container, int pos, Property added) {
    if (gedcom!=null)
      gedcom.propagatePropertyAdded(this, container, pos, added);
  }

  void propagatePropertyDeleted(Property container, int pos, Property deleted) {
    if (gedcom!=null)
      gedcom.propagatePropertyDeleted(this, container, pos, deleted);
  }

  void propagatePropertyChanged(Property property, String oldValue) {
    if (gedcom!=null)
      gedcom.propagatePropertyChanged(this, property, oldValue);
  }

  void propagatePropertyMoved(Property property, Property moved, int from, int to) {
    if (gedcom!=null)
      gedcom.propagatePropertyMoved(property, moved, from, to);
  }
} //Entity
