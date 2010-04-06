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

import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : - (internal)
 * This XRef is for pointing back to XRefs in case
 * Gedcom does only support uni-direction
 */
/*package*/ class PropertyForeignXRef extends PropertyXRef {

  /**
   * Empty Constructor
   */
  protected PropertyForeignXRef() {
  }
  
  /**
   * getTag method comment.
   */
  public String getTag() {
    return "XREF";
  }

  /**
   * There's no gedcom equivalent to a foreign (back) reference - returning ID of foreign entity
   */
  public String getValue() {
    Entity entity = getTargetEntity();
    return entity==null ? "" : '@'+getTargetEntity().getId()+'@';
  }
  
  
  /**
   * A human readable text representation 
   */
  public String getDisplayValue() {
    // 20070212 target can be null (on unlink() for example)
    PropertyXRef target = getTarget();
    return target != null ? target.getForeignDisplayValue() : "";
  }

  /**
   * link method comment.
   */
  public void link() {
    throw new RuntimeException("link is not support by ForeignXRefs");
  }

  /**
   * setValue method comment.
   */
  public void setValue(String newValue) {
    // ignored
  }

  /**
   * @see genj.gedcom.PropertyXRef#getImage(boolean)
   */
  public ImageIcon getImage(boolean checkValid) {
    // 20070212 target can be null (on unlink() for example)
    PropertyXRef target = getTarget();
    return target != null ? overlay(target.getEntity().getImage(false)) : MetaProperty.IMG_ERROR;
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    throw new IllegalArgumentException("getTargetType is not support by ForeignXRefs");
  }

  /**
   * @see genj.gedcom.PropertyForeignXRef#isValid()
   */
  public boolean isValid() {
    return false;
  }

  /**
   * @see genj.gedcom.PropertyForeignXRef#isTransient()
   */
  public boolean isTransient() {
    return true; //YES!
  }

} //PropertyForeignXRef
