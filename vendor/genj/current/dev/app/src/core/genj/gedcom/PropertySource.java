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
 * Gedcom Property : SOURCE 
 * A property that links to an existing SOURCE entity
 */
public class PropertySource extends PropertyXRef {

  /**
   * This will be called once when instantiation has
   * happend - it's our chance to substitute this with
   * a multilinevalue if no reference applicable
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // expecting SOUR
    meta.assertTag("SOUR");
    // ONLY for @..@!!!
    if (value.startsWith("@")&&value.endsWith("@"))
      return super.init(meta,value);
    // switch to multiline value
    return new PropertyMultilineValue().init(meta, value);
  }


  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "SOUR";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // Look for Source
    Source source = (Source)getCandidate();

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef();
    source.addProperty(fxref);

    // ... and point
    link(fxref);

    // done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.SOUR;
  }

  /**
   * @see genj.gedcom.PropertyXRef#overlay(genj.util.swing.ImageIcon)
   */
  protected ImageIcon overlay(ImageIcon img) {
    // used as a reference? go ahead and overlay!
    if (super.getTargetEntity()!=null)
      return super.overlay(img);
    // used inline! no overlay!
    return img;
  }
} //PropertySource

