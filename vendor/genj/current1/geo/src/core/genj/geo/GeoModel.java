/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.util.swing.Action2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spin.Spin;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Geographic model wrapper for gedcom
 */
/*package*/ class GeoModel implements GedcomListener {
  
  public final static int 
    ALL_MATCHED = 0,
    SOME_MATCHED = 1,
    ERROR = 2;
  
  /** state */
  private List listeners = new ArrayList();
  private Gedcom gedcom;
  private Map locations = new HashMap();
  private LinkedList resolvers = new LinkedList();
  
  /**
   * Constructor
   */
  public GeoModel() {
  }
  
  /**
   * Accessor - gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - gedcom
   */
  public void setGedcom(Gedcom set) {
    
    // had one before?
    if (gedcom!=null) {
      // clear our list of locations
      Collection removed = new ArrayList(locations.keySet());
      for (Iterator it = removed.iterator(); it.hasNext();)  {
        GeoLocation loc = (GeoLocation) it.next();
        locations.remove(loc);
        fireLocationRemoved(loc);
      }
      // detach
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
    }
    
    // remember
    gedcom = set;

    // new one?
    if (gedcom!=null) {
      // grab everything again
      for (Iterator it = GeoLocation.parseEntities(gedcom.getEntities()).iterator(); it.hasNext();) {
        GeoLocation loc = (GeoLocation) it.next();
        locations.put(loc, loc);
        fireLocationAdded(loc);
      }
      // start a resolver
      resolve(locations.keySet(), false);
      // attach
      gedcom.addGedcomListener((GedcomListener)Spin.over(this));
    }
    
    // done
  }
  
  /**
   * Set a location's coordinates
   */
  public void setCoordinates(GeoLocation loc, Coordinate coord) {
    loc = (GeoLocation)locations.get(loc);
    if (loc!=null) {
      loc.setCoordinate(coord);
      GeoService.getInstance().remember(gedcom, loc);
      fireLocationUpdated(loc);
    }
  }
  
  /**
   * Accessor - locations
   */
  public synchronized int getNumLocations() {
    return locations.size();
  }
  
  /**
   * Accessor - locations
   */
  public synchronized Collection getLocations() {
    return locations.keySet();
  }
  
  /**
   * Tell listeners about a found location
   */
  private void fireLocationAdded(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationAdded(location);
    }
  }
  
  /**
   * Tell listeners about an updated location
   */
  private void fireLocationUpdated(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationUpdated(location);
    }
  }

  /**
   * Tell listeners about a removed location
   */
  private void fireLocationRemoved(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationRemoved(location);
    }
  }
  
  /**
   * Tell listeners about async resolving going on
   */
  private void fireAsyncResolveStart() {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].asyncResolveStart();
    }
  }
  
  /**
   * Tell listeners about async resolving ended
   */
  private void fireAsyncResolveEnd(int status, String msg) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].asyncResolveEnd(status, msg);
    }
  }
  
  /**
   * Resolve all locations (again)
   */
  public void resolveAll() {
    resolve(locations.keySet(), true);
  }
  
  /**
   * Start a resolver
   */
  private void resolve(Collection todo, boolean matchAll) {
    synchronized (resolvers) {
      Resolver resolver = new Resolver(todo, matchAll);
      if (resolvers.isEmpty())
        resolver.trigger();
      else
        resolvers.add(resolver);
    }
  }

  /**
   * A resolver for our locations
   */
  private class Resolver extends Action2 {
    
    private ArrayList todo;
    private boolean matchAll;
    private Throwable err = null;

    /** constructor */
    private Resolver(Collection todo, boolean matchAll) {
      setAsync(Action2.ASYNC_SAME_INSTANCE);
      getThread().setDaemon(true);
      // make a private copy of todo
      this.todo = new ArrayList(todo);
      this.matchAll = matchAll;
    }
    
    /** just signal that we're busy */
    protected boolean preExecute() {
      fireAsyncResolveStart();
      return true;
    }

    /** async exec */
    protected void execute() {
      try { 
        GeoService.getInstance().match(gedcom, todo, matchAll);
      } catch (Throwable t) {
        err = t;
      }
    }

    /** sync post-exec */
    protected void postExecute(boolean preExecuteResult) {
      // update all locations - some might have changed even in case of err
      int misses = 0;
      for (Iterator it=todo.iterator(); it.hasNext(); ) {
        GeoLocation loc = (GeoLocation)it.next();
        if (!loc.isValid()) misses++;
        GeoLocation old = (GeoLocation)locations.get(loc);
        if (old!=null) fireLocationUpdated(old);
      }
      // signal we're done
      if (err!=null) 
        fireAsyncResolveEnd( ERROR, GeoView.RESOURCES.getString("resolve.error", err.getMessage() ));
      else
        fireAsyncResolveEnd( misses>0 ? SOME_MATCHED : ALL_MATCHED, GeoView.RESOURCES.getString("resolve.matches", new Integer[]{ new Integer(todo.size()-misses), new Integer(todo.size())}));
      // start pending?
      synchronized (resolvers) {
        if (!resolvers.isEmpty())
          ((Resolver)resolvers.removeFirst()).trigger();
      }
    }
    
  } //Resolver
  
  /**
   * add a listener
   */
  public void addGeoModelListener(GeoModelListener l) {
    // remember
    listeners.add(l);
    // done
  }
  
  /**
   * remove a listener
   */
  public void removeGeoModelListener(GeoModelListener l) {
    // bbye
    listeners.remove(l);
  }

  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    
    // reparse entities changed
    Set added = GeoLocation.parseEntities(Collections.singletonList(entity));
    
    for (Iterator locs = added.iterator(); locs.hasNext(); ) {
      GeoLocation loc = (GeoLocation)locs.next();
      GeoLocation old = (GeoLocation)locations.get(loc);
      if (old!=null) {
        old.add(loc);
        fireLocationUpdated(old);
      } else {
        locations.put(loc, loc);
        fireLocationAdded(loc);
      }
    }
    
    // resolve
    resolve(added, true);
  }

  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    
    List current = new ArrayList(locations.keySet());
    for (Iterator locs = current.iterator(); locs.hasNext(); ) {
      GeoLocation loc = (GeoLocation)locs.next();
      loc.removeEntity(entity);
      if (loc.getNumProperties()==0) {
        locations.remove(loc);
        fireLocationRemoved(loc);
      } else {
        fireLocationUpdated(loc);
      }
    }
    
  }

  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    gedcomPropertyChanged(gedcom, property);
  }

  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    Entity entity = property.getEntity();
    gedcomEntityDeleted(gedcom, entity);
    gedcomEntityAdded(gedcom, entity);
  }

  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
    gedcomPropertyChanged(gedcom, property);
  }
  
}
