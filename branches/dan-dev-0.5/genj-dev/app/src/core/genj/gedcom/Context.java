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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */
public class Context {

  private Gedcom gedcom;
  private List<Entity> entities = new ArrayList<Entity>();
  private List<Property> properties = new ArrayList<Property>();

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Context))
      return false;
    Context that = (Context)obj;
    return this.gedcom==that.gedcom && this.entities.equals(that.entities) 
      && this.properties.equals(that.properties);
  }
  
  /**
   * Constructor
   */
  public Context() {
  }
  
  /**
   * Constructor
   */
  public Context(Context context) {
    this.gedcom = context.gedcom;
    this.entities.addAll(context.entities);
    this.properties.addAll(context.properties);
  }

  /**
   * Constructor
   */
  public Context(Gedcom gedcom, Collection<? extends Entity> entities) {
    this(gedcom, entities, null);
  }
  
  /**
   * Constructor
   */
  public Context(Gedcom gedcom, Collection<? extends Entity> entities, Collection<? extends Property> properties) {
    
    this.gedcom = gedcom;

    // grab ents
    if (entities!=null)
      for (Entity e : entities) {
        if (e.getGedcom()!=gedcom)
          throw new IllegalArgumentException("gedcom must be same");
        if (!this.entities.contains(e))
          this.entities.add(e);
      }

    // grab props
    if (properties!=null)
      for (Property p : properties) {
        // we don't want entities to leak through as properties
        if (p instanceof Entity) {
          if (!this.entities.contains(p))
            this.entities.add((Entity)p);
        } else if (!this.properties.contains(p)) {
          Entity e = p.getEntity();
          if (e.getGedcom()!=gedcom)
            throw new IllegalArgumentException("gedcom must be same");
          this.properties.add(p);
          if (!this.entities.contains(e))
            this.entities.add(e);
        }
      }

    // done
  }

  /**
   * Constructor
   */
  public Context(Gedcom ged) {
    gedcom = ged;
  }

  /**
   * Constructor
   */
  public Context(Property prop) {
    this(prop.getGedcom());
    properties.add(prop);
    Entity entity = prop.getEntity();
    if (!entities.contains(entity))
      entities.add(entity);
  }

  /**
   * Constructor
   */
  public Context(Entity entity) {
    this(entity.getGedcom());
    entities.add(entity);
  }
  
  /**
   * A context minus the given entity
   */
  public Context remove(Entity entity) {
    List<Entity> ents = new ArrayList<Entity>(entities);
    ents.remove(entity);
    List<Property> props = new ArrayList<Property>(properties.size());
    for (Property prop : properties) {
      Entity ent = prop.getEntity();
      if (ent!=entity&&ent!=null)
        props.add(prop);
    }
    return new Context(gedcom, ents, props);
  }

  /**
   * A context minus the given property
   */
  public Context remove(Property property) {
    List<Entity> ents = new ArrayList<Entity>(entities);
    List<Property> props = new ArrayList<Property>(properties);
    props.remove(property);
    return new Context(gedcom, ents, props);
  }

  /**
   * Accessor
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Accessor - last entity selected
   */
  public Entity getEntity() {
    return entities.isEmpty() ? null : (Entity)entities.get(0);
  }

  /**
   * Accessor - last property selected
   */
  public Property getProperty() {
    return properties.isEmpty() ? null : (Property)properties.get(0);
  }

  /**
   * Accessor - all entities
   */
  public List<? extends Entity> getEntities() {
    return entities;
  }

  /**
   * Accessor - properties
   */
  public List<? extends Property> getProperties() {
    return properties;
  }

  /** storage */
  public String toString() {
    
    if (gedcom==null)
      return "";
    
    StringBuffer result = new StringBuffer();
    result.append(gedcom.getName());
    for (Entity entity : entities) {
      result.append(";");
      result.append(entity.getId());
      
      for (Property prop : properties) {
        if (prop.getEntity()==entity) {
          result.append(",");
          result.append(prop.getPath());
        }
      }
      
    }
    return result.toString();
  }
  
  public static Context fromString(Gedcom gedcom, String toString) throws GedcomException {

    List<Entity> entities = new ArrayList<Entity>();
    List<Property> properties = new ArrayList<Property>();

    String[] es = toString.split(";");
    
    // first is gedcom name
    if (!es[0].equals(gedcom.getName()))
      throw new GedcomException(es[0]+" doesn't match "+gedcom.getName());
    
    // loop over entities
    for (int e=1; e<es.length; e++) {
      
      String[] ps = es[e].split(",");

      // first is entity id
      Entity entity = gedcom.getEntity(ps[0]);
      if (entity==null)
        throw new GedcomException(ps[0]+" not in "+gedcom);
      entities.add(entity);

      // then props
      for (int p=1; p<ps.length; p++) {
        try {
          Property property = entity.getPropertyByPath(ps[p]);
          if (property==null)
            throw new GedcomException(ps[p]+" not in "+ps[0]+" in "+gedcom);
          properties.add(property);
        } catch (IllegalArgumentException iae) {
          throw new GedcomException(ps[p]+" not valid for "+es[e]);
        }
        
      }
    }
    return new Context(gedcom, entities, properties);
    
  }
  

} //Context
