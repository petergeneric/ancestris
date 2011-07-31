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
 * Gedcom Property : MEDIA
 * Property wrapping a reference to a multiMedia object - in Gedcom 5.5 this 
 * record contains BLOBs with in-line information. We discourage the use
 * of this entity in GenJ and encourage either to switch to 5.5.1 which allows
 * in-line FILE or use inline OBJE properties instead.
 */
public class PropertyMedia extends PropertyXRef {
  
  /**
   * need tag-argument constructor for all properties
   */
  /*package*/ PropertyMedia(String tag) {
    super(tag);
    assertTag("OBJE");
  }

  /*package*/ PropertyMedia() {
    super("OBJE");
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

} //PropertyMedia
