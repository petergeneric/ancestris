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
package genj.io;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * The immutable filter
 */
public interface Filter {

  public String getName();
  
  public boolean veto(Property property);

  public boolean veto(Entity entity);
  
  public static class Union implements Filter {

    private Set<Property> vetoed;
    
    public Union(Gedcom gedcom, Collection<Filter> filters) {

      // go through all entities/properties and check supplied filters
      vetoed = new HashSet<Property>();    
      for (Entity e : gedcom.getEntities()) 
        scan(e, filters);

      // check transitive vetoes
      Deque<Property> transitive = new ArrayDeque<Property>(vetoed);
      while (!transitive.isEmpty()) {
        Property property = transitive.removeLast();
        for (PropertyXRef xref : property.getProperties(PropertyXRef.class)) {
          if (!xref.isValid()) continue;
          PropertyXRef target = xref.getTarget();
          if (!vetoed.add(target)) continue;
          transitive.add(target);
        }
      }
      
    }
    
    private void scan(Entity entity, Collection<Filter> filters) {
      if (isVetoed(entity, filters))
        vetoed.add(entity);
      else for (Property p : entity.getProperties())
        scan(p, filters);
    }
    
    private void scan(Property property, Collection<Filter> filters) {
      if (isVetoed(property, filters))
        vetoed.add(property);
      else for (Property p : property.getProperties())
        scan(p, filters);
    }
    
    private boolean isVetoed(Entity entity, Collection<Filter> filters) {
      for (Filter filter : filters)
        if (filter.veto(entity))
          return true;
      return false;
    }

    private boolean isVetoed(Property property, Collection<Filter> filters) {
      for (Filter filter : filters)
        if (filter.veto(property))
          return true;
      return false;
    }

    public String getName() {
      return "Union";
    }

    public boolean veto(Property property) {
      return vetoed.contains(property);
    }

    public boolean veto(Entity entity) {
      return vetoed.contains(entity);
    }

  }
  
} //Filter
  
