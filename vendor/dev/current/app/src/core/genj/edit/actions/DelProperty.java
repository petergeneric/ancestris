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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.view.ViewManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * PDelete - delete a property
 */  
public class DelProperty extends AbstractChange {
  
  /** the candidates to delete */
  private Set candidates = new HashSet();
  
  /**
   * Constructor
   */
  public DelProperty(Property property, ViewManager manager) {
    super(property.getGedcom(), Images.imgDelEntity, resources.getString("delete"), manager);
    candidates.add(property);
  }

  /**
   * Constructor
   */
  public DelProperty(Property[] properties, ViewManager manager) {
    super(properties[0].getGedcom(), Images.imgDelEntity, resources.getString("delete"), manager);
    candidates.addAll(Arrays.asList(properties));
  }

  /**
   * Perform the delete
   */
  public void perform(Gedcom gedcom) throws GedcomException {
    for (Iterator candidate = candidates.iterator(); candidate.hasNext();) {
      Property prop  = (Property) candidate.next();
      prop.getParent().delProperty(prop);
    }
  }
  
} //DelProperty

