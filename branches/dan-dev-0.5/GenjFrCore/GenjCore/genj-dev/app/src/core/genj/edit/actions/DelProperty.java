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

import genj.edit.Images;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PDelete - delete a property
 */  
public class DelProperty extends AbstractChange {
  
  /** the candidates to delete */
  private Set<Property> candidates = new HashSet<Property>();
  
  /**
   * Constructor
   */
  public DelProperty(Property property) {
    super(property.getGedcom(), Images.imgDel, resources.getString("delete"));
    candidates.add(property);
  }

  /**
   * Constructor
   */
  public DelProperty(List<? extends Property> properties) {
    super(properties.get(0).getGedcom(), Images.imgDel, resources.getString("delete"));
    candidates.addAll(properties);
  }
  
  @Override
  protected String getConfirmMessage() {
    StringBuffer txt = new StringBuffer();
    txt.append(resources.getString("confirm.del.props", candidates.size()));
    txt.append("\n");
    int i=0; for (Property prop : candidates)  {
      if (i++>16) {
        txt.append("...");
        break;
      }
      txt.append(prop.toString());
      txt.append("\n");
    }
    return txt.toString();
  }
  
  /**
   * Perform the delete
   */
  protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
    
    // leaving an orphan?
    Set<Entity> orphans = new HashSet<Entity>();

    for (Property prop : candidates) {
      
      if (prop instanceof PropertyXRef && prop.isValid()) {
        orphans.add( ((PropertyXRef)prop).getTargetEntity() );
        orphans.add( prop.getEntity() );
      }
      
      prop.getParent().delProperty(prop);
    }
    
    // check for and delete orphans
    for (Entity orphan : orphans) {
      if (!(orphan instanceof Indi || orphan.isConnected()))
        gedcom.deleteEntity(orphan);
    }

    // nothing to go to
    return null;
  }
  
} //DelProperty

