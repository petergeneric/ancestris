/**
 * Ancestris 
 *
 * Copyright (C) 1997 - 2020 Frederic Lapeyre <frederics@ancestris.org>
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
 * Gedcom Property : NOTE 
 * A property that links an existing note
 */
public class PropertyNick extends PropertyChoiceValue {

  public static final String TAG = "NICK";
  
  /**
   * need tag-argument constructor for all properties
   */
  /*package*/PropertyNick(String tag) {
    super(tag);
    assertTag(TAG);
  }

  /*package*/ PropertyNick() {
    super(TAG);
  }

  
} //PropertyNote

