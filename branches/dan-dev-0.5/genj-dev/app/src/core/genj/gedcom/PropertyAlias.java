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


/**
 * Gedcom Property : ALIAs
 * Property for representing the fact that two individuals are the same person
 */
public class PropertyAlias extends PropertyXRef {
  
  /**
   * Empty Constructor
   */
  /*package*/ PropertyAlias() {
    super("ALIA");
  }

  /**
   * need tag-argument constructor for all properties
   */
  /*package*/ PropertyAlias(String tag) {
    super(tag);
    assertTag("ALIA");
  }

  /**
   * Links reference to entity (if not already done)
   */
  public void link() throws GedcomException {
    
    Indi indi = (Indi)getEntity();

     // Try to find entity
    Entity ent = getCandidate();
    
    // Connect back from alias (maybe using back reference)
    List<PropertyAlias> aliass = ent.getProperties(PropertyAlias.class);
    for (int i=0, j=aliass.size(); i<j; i++) {
      PropertyAlias alias = aliass.get(i);
      if (alias.isCandidate(indi)) {
        link(alias);
        return;
      }        
    }
    

    // Create Backlink using ALIAs
    PropertyAlias alias = new PropertyAlias();
    try {
      ent.addProperty(alias);
    } catch (Throwable t) {
    }

    // ... and point
    link(alias);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.INDI;
  }
  
} //PropertyAssociation
