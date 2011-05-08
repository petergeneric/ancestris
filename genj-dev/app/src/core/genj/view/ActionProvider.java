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
package genj.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;

import java.util.List;

/**
 * Support for a actions of property/entity/gedcom
 */
public interface ActionProvider {
  
  /**
   * Callback for actions on a list of entities
   */
  public List createActions(Property[] properties, ViewManager manager);
  
  /**
   * Callback for actions on a property
   */
  public List createActions(Property property, ViewManager manager);

  /**
   * Callback for actions on an entity
   */
  public List createActions(Entity entity, ViewManager manager);

  /**
   * Callback for actions on a gedcom
   */
  public List createActions(Gedcom gedcom, ViewManager manager);

} //ContextMenuSupport
