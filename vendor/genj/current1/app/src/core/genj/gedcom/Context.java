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

import genj.util.swing.ImageIcon;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class Context implements Comparable {
  
  private Gedcom gedcom;
  private List entities = new ArrayList();
  private List properties = new ArrayList();
  private Class entityType = null;
  private Class propertyType = null;
  private ImageIcon  img = null;
  private String txt = null;
  
  /**
   * Constructor
   */
  public Context(Context context) {
    this.gedcom = context.gedcom;
    this.entities.addAll(context.entities);
    this.properties.addAll(context.properties);
    this.entityType = context.entityType;
    this.propertyType = context.propertyType;
    this.img = context.img;
    this.txt = context.txt;
  }
  
  /**
   * Constructor
   */
  public Context(Gedcom ged) {
    if (ged==null)
      throw new IllegalArgumentException("gedcom for context can't be null");
    gedcom = ged;
  }
  
  /**
   * Constructor
   */
  public Context(Property prop) {
    this(prop.getGedcom());
    addProperty(prop);
  }
  
  /**
   * Constructor
   */
  public Context(Entity entity) {
    this(entity.getGedcom());
    addEntity(entity);
  }
  
  /**
   * Add an entity
   */
  public void addEntity(Entity e) {
    // check gedcom
    if (e.getGedcom()!=gedcom)
      throw new IllegalArgumentException("entity's gedcom can't be different");
    // keep track of entity/types we contain
    entities.remove(e);
    if (entityType!=null&&entityType!=e.getClass())
      entityType = Entity.class;
    else 
      entityType = e.getClass();
    entities.add(e);
  }
  
  /**
   * Add entities
   */
  public void addEntities(Entity[] es) {
    for (int i = 0; i < es.length; i++) 
      addEntity(es[i]);
  }
  
  /**
   * Remove entities
   */
  public void removeEntities(Collection rem) {
    
    // easy for entities
    entities.removeAll(rem);
    
    // do properties to
    for (ListIterator iterator = properties.listIterator(); iterator.hasNext();) {
      Property prop = (Property) iterator.next();
      if (rem.contains(prop.getEntity()))
        iterator.remove();
    }
  }
  
  /**
   * Add a property
   */
  public void addProperty(Property p) {
    // keep entity
    addEntity(p.getEntity());
    if (p instanceof Entity)
      return;
    // check gedcom
    if (p.getGedcom()!=gedcom)
      throw new IllegalArgumentException("property's gedcom can't be different");
    // keep track of property types we contain
    properties.remove(p);
    if (propertyType!=null&&propertyType!=p.getClass())
      propertyType = Property.class;
    else 
      propertyType = p.getClass();
    // keep it
    properties.add(p);
  }
  
  /**
   * Add properties
   */
  public void addProperties(Property[] ps) {
    for (int i = 0; i < ps.length; i++) 
      addProperty(ps[i]);
  }
  
  /**
   * Remove properties
   */
  public void removeProperties(Collection rem) {
    properties.removeAll(rem);
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
  public Entity[] getEntities() {
    if (entityType==null)
      return new Entity[0];
    return (Entity[])entities.toArray((Entity[])Array.newInstance(entityType, entities.size()));
  }

  /**
   * Accessor - properties
   */
  public Property[] getProperties() {
    if (propertyType==null)
      return new Property[0];
    return (Property[])properties.toArray((Property[])Array.newInstance(propertyType, properties.size()));
  }

  /** 
   * Accessor 
   */
  public String getText() {
    
    if (txt!=null)
      return txt;
    
    if (properties.size()==1) {
      Property prop = (Property)properties.get(0);
      txt = Gedcom.getName(prop.getTag()) + "/" + prop.getEntity();
    } else if (!properties.isEmpty())
      txt = Property.getPropertyNames(Property.toArray(properties), 5);
    else  if (entities.size()==1) 
      txt = entities.get(0).toString();
    else if (!entities.isEmpty())
      txt = Entity.getPropertyNames(Property.toArray(entities), 5);
    else txt = gedcom.getName();
    
    return txt;
  }
  
  /** 
   * Accessor
   */
  public Context setText(String text) {
    txt = text;
    return this;
  }
  
  /** 
   * Accessor
   */
  public ImageIcon getImage() {
    // an override?
    if (img!=null)
      return img;
    // check prop/entity/gedcom
    if (properties.size()==1)
      img = ((Property)properties.get(0)).getImage(false);
    else if (entities.size()==1)
      img = ((Entity)entities.get(0)).getImage(false);
    else img = Gedcom.getImage();
    return img;
  }
  
  /** 
   * Accessor
   */
  public Context setImage(ImageIcon set) {
    img = set;
    return this;
  }

  /**
   * Add given context to this context
   */
  public void addContext(Context context) {
    if (context.getGedcom()!=getGedcom())
      throw new IllegalArgumentException();
    addProperties(context.getProperties());
    addEntities(context.getEntities());
  }
  
  /** comparison  */
  public int compareTo(Object o) {
    Context that = (Context)o;
    if (this.txt==null)
      return -1;
    if (that.txt==null)
      return 1;
    return this.txt.compareTo(that.txt);
  }
  
} //Context
