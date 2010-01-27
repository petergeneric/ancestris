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
import java.util.List;

/**
 * Gedcom Property : MEDIA
 * Property wrapping a reference to a multiMedia object - this object
 * can contain BLOBs with in-line information. We discourage the use
 * of this entity in GenJ and encourage in-line OBJE properties instead.
 */
public class PropertyMedia extends PropertyXRef implements IconValueAvailable {

  /**
   * This will be called once when instantiation has
   * happend - it's our chance to substitute this with
   * a read-only value if no reference applicable
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // expecting NOTE
    meta.assertTag("OBJE");
    // ONLY for @..@!!!
    if (value.startsWith("@")&&value.endsWith("@"))
      return super.init(meta,value);
    // switch to ro value
    return new PropertySimpleReadOnly().init(meta, value);
  }


  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "OBJE";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // Look for media
    Media media = (Media)getCandidate();

    // Create a back-reference
    PropertyForeignXRef fxref = new PropertyForeignXRef();
    media.addProperty(fxref);

    // .. and point to it
    link(fxref);

    // Done

  }
  
  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.OBJE;
  }

  /**
   * Returns an ImgIcon if existing in one of the sub-properties
   */
  public ImageIcon getValueAsIcon() {
    List ps = super.getProperties(IconValueAvailable.class);
    return ps.isEmpty() ? null : ((IconValueAvailable)ps.get(0)).getValueAsIcon();
  }
  
} //PropertyMedia
