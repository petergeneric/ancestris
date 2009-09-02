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

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.swing.Action2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class ViewContext extends Context {
  
  private ViewManager manager;
  private List actions = new ArrayList();
  
  /**
   * Constructor
   */
  public ViewContext(Context context) {
    super(context);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Gedcom ged) {
    super(ged);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Property prop) {
    super(prop);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Entity entity) {
    super(entity);
  }
  
  /**
   * Add an action
   */
  public ViewContext addAction(Action2 action) {
    actions.add(action);
    return this;
  }
  
  /**
   * Add actions
   */
  public ViewContext addActions(Action2.Group group) {
    actions.add(group);
    return this;
  }
  
  /**
   * Access to actions
   */
  public List getActions() {
    return Collections.unmodifiableList(actions);
  }
  
  /**
   * Connect to manager
   */
  /*package*/ void setManager(ViewManager set) {
    manager = set;
  }
  
  /**
   * Accessor
   */
  public ViewManager getManager() {
    return manager;
  }
  
} //Context
