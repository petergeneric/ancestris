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
package genj.edit.actions;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.view.ViewManager;

/**
 * Create an association between a property (the existing target) and an individual (a source to be chosen or created)
 */
public class CreateAssociation extends CreateRelationship {
  
  private Property target;
  
  /** constructor */
  public CreateAssociation(Property target, ViewManager mgr) {
    super(resources.getString("create.association"), target.getGedcom(), Gedcom.INDI, mgr);
    this.target = target;
    
  }

  /** description of what this does */
  public String getDescription() {
    return resources.getString("create.association.with", new String[]{ Gedcom.getName(target.getTag()), target.getEntity().toString() });
  }

  /** perform the change */
  protected Property change(Entity source, boolean targetIsNew) throws GedcomException {
    
    Indi indi = (Indi)source;

    // create ASSO in entity
    PropertyXRef asso = (PropertyXRef)indi.addProperty("ASSO", '@'+target.getEntity().getId()+'@');
    
    // setup anchor through RELA if applicable
    TagPath anchor = target.getPath(true);
    Property rela = asso.addProperty("RELA", anchor==null ? "" : '@'+anchor.toString() );

    // link it
    try {
      asso.link();
    } catch (GedcomException e) {
      indi.delProperty(asso);
      throw e;
    }
    
    // done - continue with relationship
    return rela;
  }

}
