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
 * RELA property as sub-property for ASSOciation - it contains
 * a textual RELAtionship description for that ASSOciation. We
 * also use it to store the path to the ASSOciation's target 
 * (the anchor) because ASSOciations are represented with
 * back-pointing properties in Gedcom. The anchor contains the
 * path to the ASSOciation's ForeignXRef's parent, e.g.
 * INDI:BIRT or FAM:MARR. On load we can provide the ASSOciation
 * with that anchor for reconstruction of the old appropriate
 * link.
 */
public class PropertyRelationship extends PropertyChoiceValue {

  /** an anchor is appended to the default gedcom RELA value e.g. Witness@INDI:BIRT */
  private TagPath anchor = null;

  /**
   * need tag-argument constructor for all properties
   */
  public PropertyRelationship(String tag) {
    super(tag);
    assertTag("RELA");
  }
  
  /**
   * Compute Gedcom compliant value which includes our anchor information
   */
  public String getValue() {
    String value = super.getValue();
    TagPath anchor = getAnchor();
    if (anchor!=null&&anchor.length()>0)
      value += '@' + anchor.toString();
    return value;
  }
  
  public String getDisplayValue() {
    return super.getValue();
  }
  
  /**
   * Parse value
   */
  public void setValue(String value) {

    // parse anchor if one is still needed
    int i = value.lastIndexOf('@');
    if (i>=0) {
      try {
        anchor = new TagPath(value.substring(i+1));
        // relink association if anchor is still different (means, we're linked)
        if (!getAnchor().equals(anchor)) {
          PropertyAssociation asso = (PropertyAssociation)getParent();
          Property target = asso.getTarget();
          asso.unlink();
          target.getParent().delProperty(target);
          asso.link();
        }
      } catch (Throwable t) {
      }
      value = value.substring(0,i);
    }
    // continue
    super.setValue(value);
  }
  
  /**
   * Compute target of associated Association
   */
  /*package*/ Property getTarget() {
    // look for it through ASSO
    Property parent = getParent();
    if (parent instanceof PropertyAssociation)
      return ((PropertyAssociation)parent).getTarget();
    return null;
  }
  
  /**
   * Compute anchor
   * @return might be null
   */
  /*package*/ TagPath getAnchor() {

    // try to find accurate target base on target's parent
    Property target = getTarget();
    if (target!=null) {
      Property panchor = target.getParent();
      if (!(panchor instanceof Entity)&&panchor!=null) {
        // try non-unique path first - this is the simplest case e.g. INDI:BIRT:DATE
        TagPath result = panchor.getPath(false);
        // .. fallback to unique path if necessary INDI:BIRT#2:DATE
        return panchor.getEntity().getProperty(result) == panchor ? result : panchor.getPath(true); 
      }
    }
    
    // fallback to current cached anchor
    return anchor;

  }

} //PropertyRelationship
