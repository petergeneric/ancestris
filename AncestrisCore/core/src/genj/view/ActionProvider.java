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

import ancestris.core.actions.SubMenuAction;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.Action2.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Support for a actions of property/entity/gedcom
 */
public interface ActionProvider {

  public enum Purpose {
    TOOLBAR,
    CONTEXT,
    MENU
  }

  /**
   * Callback for actions on a list of entities
   */
  public void createActions(Context context, Purpose purpose, Group into);

  /**
   * an action group for a property
   */
  public final class PropertyActionGroup extends SubMenuAction {
    private Property p;
    public PropertyActionGroup(Property property) {
      super(Property.LABEL+" '"+TagPath.get(property).getName() + '\'', property.getImage(false));
      p = property;
    }
    @Override
    public boolean equals(Object that) {
      return that instanceof PropertyActionGroup && ((PropertyActionGroup)that).p.equals(this.p);
    }
    @Override
    public int hashCode() {
      return p.hashCode();
    }
  }

  /**
   * an action group for an entity
   */
  public class EntityActionGroup extends SubMenuAction {
    private Entity e;
    public EntityActionGroup(Entity entity) {
      super(Gedcom.getName(entity.getTag(),false)+" '"+entity.getId()+'\'', entity.getImage(false));
      e = entity;
    }
    @Override
    public boolean equals(Object that) {
      return that instanceof EntityActionGroup && ((EntityActionGroup)that).e.equals(this.e);
    }
    @Override
    public int hashCode() {
      return e.hashCode();
    }
  }

  /**
   * an action group for a list of properties
   */
  public class PropertiesActionGroup extends SubMenuAction {
    private List<Property> ps;
    public PropertiesActionGroup(List<? extends Property> properties) {
      super("'"+Property.getPropertyNames(properties, 5)+"' ("+properties.size()+")");
      ps = new ArrayList<Property>(properties);
    }
    @Override
    public boolean equals(Object that) {
      return that instanceof PropertiesActionGroup && ((PropertiesActionGroup)that).ps.equals(this.ps);
    }
    @Override
    public int hashCode() {
      return ps.hashCode();
    }
  }
  
  /**
   * an action group for a list of entities
   */
  public class EntitiesActionGroup extends SubMenuAction {
    private List<Entity> es;
    public EntitiesActionGroup(List<? extends Entity> entities) {
      super("'"+Property.getPropertyNames(entities,5)+"' ("+entities.size()+")");
      es = new ArrayList<Entity>(entities);
    }
    @Override
    public boolean equals(Object that) {
      return that instanceof EntitiesActionGroup && ((EntitiesActionGroup)that).es.equals(this.es);
    }
    @Override
    public int hashCode() {
      return es.hashCode();
    }
  }

  /**
   * an action group for gedcom
   */
  public class GedcomActionGroup extends SubMenuAction {
    private Gedcom gedcom;
    public GedcomActionGroup(Gedcom gedcom) {
      super("Gedcom '"+gedcom.getName()+'\'', Gedcom.getImage());
      this.gedcom = gedcom;
    }
    @Override
    public boolean equals(Object that) {
      return that instanceof GedcomActionGroup && ((GedcomActionGroup)that).gedcom.equals(this.gedcom);
    }
    @Override
    public int hashCode() {
      return gedcom.hashCode();
    }
  }
  
} 
