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
package genj.timeline;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyName;
import genj.gedcom.TagPath;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model implements GedcomListener {

  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** limits */
  /*package*/ double 
    max = Double.NaN,
    min = Double.NaN;

  /** a filter for events that we're interested in */
  private Set<TagPath> paths = new HashSet<TagPath>();
  private Set<String> tags = new HashSet<String>();
  
  /** default filter */
  private final static String[] DEFAULT_PATHS = new String[]{ 
    "INDI:BIRT", "FAM:MARR", "INDI:EMIG" 
  };
    
  /** our levels */
  /*package*/List<List<Event>> layers;
  
  /** time per event */
  /*package*/ double 
    timeBeforeEvent = 0.5D,
    timeAfterEvent  = 2.0D;
  
  /** listeners */
  private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
  
  /**
   * Constructor
   */
  /*package*/ Model() {
    setPaths(Arrays.asList(TagPath.toArray(DEFAULT_PATHS)));
  }
  
  /**
   * gedcom we're looking at
   */
  /*package*/ Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * gedcom to look at
   */
  /*package*/ void setGedcom(Gedcom newGedcom) {
    
    // noop
    if (gedcom==newGedcom)
      return;
    
    // old?
    if (gedcom!=null)
      gedcom.removeGedcomListener(this);
    
    // keep
    gedcom = newGedcom;
    
    // new?
    if (gedcom!=null)
      gedcom.addGedcomListener(this);
    
    // create events
    createEvents();
    
  }
  
  /**
   * Add a listener
   */
  /*package*/ void addListener(Listener listener) {
    listeners.add(listener);
  }
  
  /**
   * Removes a listener
   */
  /*package*/ void removeListener(Listener listener) {
    listeners.remove(listener);
  }
  
  /**
   * change time per event
   */
  /*package*/ void setTimePerEvent(double before, double after) {
    // already there?
    if (timeBeforeEvent==before&&timeAfterEvent==after) 
      return;
    // remember
    timeBeforeEvent = before;
    timeAfterEvent = after;
    // layout the events we've got
    if (layers!=null) 
      layoutEvents();
    // done
  }
  
  /** 
   * Convert a point in time into a gregorian year (double)
   */
  /*package*/ static double toDouble(PointInTime pit, boolean roundUp) throws GedcomException {
    
    // all Gregorian for now
    Calendar calendar = PointInTime.GREGORIAN;
    
    if (pit.getCalendar()!=calendar) { 
      pit = pit.getPointInTime(calendar);
    }
    
    // year
    int year = pit.getYear();
    double result = year; 

    // month
    int month = pit.getMonth();
    if (month==PointInTime.UNKNOWN)
      return roundUp ? result+1 : result;

    double months = calendar.getMonths(); 
    result += month / months;

    // day
    int day = pit.getDay();
    if (day==PointInTime.UNKNOWN) 
      return roundUp ? result+1/months : result;

    double days = calendar.getDays(month, year);
    result += day/months/days;

    // done
    return result;
  }
  
  /**
   * Returns a point in time for a year (double)
   */
  /*package*/ static PointInTime toPointInTime(double year) {

    Calendar calendar = PointInTime.GREGORIAN;
    
    int months = calendar.getMonths();
    
    int y = (int)Math.floor(year);
    
    year = year%1;
    if (year<0) year = 1+year;
    
    int m = (int)Math.floor(year * months);

    int days = calendar.getDays(m, y);
    
    int d = (int)Math.floor((year*months)%1 * days);
    
    return new PointInTime(d, m, y);
  }
  
  /**
   * Returns an event by year/layer
   */
  protected Event getEvent(double year, int layer) {
    // look for events in appropriate layer
    Iterator events = ((List)layers.get(layer)).iterator();
    while (events.hasNext()) {
      Event event = (Event)events.next();
      if (event.from-timeBeforeEvent<year&&year<event.to+timeAfterEvent)
        return event;
    }
    // done
    return null;
  }
  
  /**
   * Returns the events that cover the given context
   */
  protected Set<Event> getEvents(Context context) {
    
    Set propertyHits = new HashSet();
    Set entityHits = new HashSet();
    
    List<? extends Property> props = context.getProperties();
    List<? extends Entity> ents = context.getEntities();
    
    for (int l=0; l<layers.size(); l++) {
      Iterator events = ((List)layers.get(l)).iterator();
      while (events.hasNext()) {
        Event event = (Event)events.next();
        for (int j = 0; j < ents.size(); j++) {
          if (ents.get(j)==event.getEntity())
            entityHits.add(event);
        }
        for (int i = 0; i < props.size(); i++) {
          if (event.getProperty()==props.get(i)||event.getProperty().contains(props.get(i)))
            propertyHits.add(event);
        }
      }
    }

    return propertyHits.isEmpty() ? entityHits : propertyHits;
  } 
  
  /**
   * Returns the filter - set of Tags we consider
   */
  public Set<TagPath> getPaths() {
    return Collections.unmodifiableSet(paths);
  }
  
  /**
   * Sets the filter - set of Tags we consider
   */
  public void setPaths(Collection<TagPath> set) {
    
    paths.clear();
    tags.clear();
    
    for (TagPath path : set) {
      paths.add(path);
      tags.add(path.getLast());
    }
    
    // re-generate events
    createEvents();
    
    // done
    
  }
  
  /**
   * Trigger callback - our structure has changed
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((Listener)listeners.get(l)).structureChanged();
    }
  }

  /**
   * Trigger callback - our data has changed
   */
  private void fireDataChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((Listener)listeners.get(l)).dataChanged();
    }
  }
  
  /**
   * Retags events for given entity
   */
  private final void contentEvents(Entity entity) {
    // loop through layers
    for (int l=0; l<layers.size(); l++) {
      List layer = (List)layers.get(l);
      Iterator events = layer.iterator();
      while (events.hasNext()) {
        Event event = (Event)events.next();
        if (event.pe.getEntity()==entity) event.content();
      }
    }
    // done
  }

  /**
   * Layout events by using the existing set of events
   * and re-stacking them in layers
   */
  private final void layoutEvents() {
    // reset
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    // keep old and create some new space
    List old = layers;
    layers = new ArrayList(10);
    // loop through old
    for (int l=0; l<old.size(); l++) {
      List layer = (List)old.get(l);
      Iterator events = layer.iterator();
      while (events.hasNext()) {
        Event event = (Event)events.next();
        insertEvent(event);
      }
    }
    // extend time by before/after
    max += timeAfterEvent;
    min -= timeBeforeEvent;
    // trigger
    fireStructureChanged();
    // done
  }
  
  /**
   * Gather Events
   */
  private final void createEvents() {
    // reset
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    // prepare some space
    layers = new ArrayList<List<Event>>(10);
    // look for events in INDIs and FAMs
    if (gedcom!=null) {
      createEventsFrom(gedcom.getEntities(Gedcom.INDI).iterator());
      createEventsFrom(gedcom.getEntities(Gedcom.FAM ).iterator());
    }
    // extend time by before/after
    max += timeAfterEvent;
    min -= timeBeforeEvent;
    // trigger
    fireStructureChanged();
    // done
  }
  
  /** 
   * Gather Events for given entities
   * @param es list of entities to find events in
   */
  private final void createEventsFrom(Iterator es) {
    // loop through entities
    while (es.hasNext()) {
      Entity e = (Entity)es.next();
      List ps = e.getProperties(PropertyEvent.class);
      for (int j=0; j<ps.size(); j++) {
        PropertyEvent pe = (PropertyEvent)ps.get(j);
        if (tags.contains(pe.getTag())) createEventFrom(pe);
      }
    }
    // done
  }
  
  /** 
   * Gather Event for given PropertyEvent
   * @param pe property to use
   */
  private final void createEventFrom(PropertyEvent pe) {
    // we need a valid date for new event
    PropertyDate pd = pe.getDate();
    if (pd==null||!pd.isValid()||!pd.isComparable())
      return;
    
    // get it 
    try { 
      insertEvent(new Event(pe, pd));
    } catch (GedcomException e) {
    }
    // done
  }
  
  /**
   * Insert the Event into one of our layers
   */
  private final void insertEvent(Event e) {
    
    // remember min and max
    min = Math.min(Math.floor(e.from), min);
    max = Math.max(Math.ceil (e.to  ), max);
    
    // find a level that suits us
    for (int l=0;l<layers.size();l++) {
      // try to insert in level
      List layer = (List)layers.get(l);
      if (insertEvent(e, layer)) return;
      // continue
    }
    
    // create a new layer
    List layer = new LinkedList();
    layers.add(layer);
    layer.add(e);
    
    // done
  }
  
  /**
   * Insert the Event into a layer
   * @return whether that was successfull
   */
  private final boolean insertEvent(Event candidate, List layer) {
    // loop through layer
    ListIterator events = layer.listIterator();
    do {
      Event event = (Event)events.next();
      // before?
      if (candidate.to+timeAfterEvent<event.from-timeBeforeEvent) {
        events.previous();
        events.add(candidate);
        return true;
      }
      // overlap?
      if (candidate.from-timeBeforeEvent<event.to+timeAfterEvent) 
        return false;
      // after?
    } while (events.hasNext());
    // after!
    events.add(candidate);
    return true;
  }
  
  /**
   * An event in our model
   */
  /*package*/ class Event {
    /** state */
    /*package*/ double from, to;
    /*package*/ PropertyEvent pe;
    /*package*/ PropertyDate pd;
    /*package*/ String content;
    /** 
     * Constructor
     */
    Event(PropertyEvent propEvent, PropertyDate propDate) throws GedcomException {
      // remember
      pe = propEvent;
      pd = propDate;
      // setup time
      from = toDouble(propDate.getStart(), propDate.getFormat()==PropertyDate.AFTER);
      to  = propDate.isRange() ? toDouble(propDate.getEnd(), false) : from;
      // from<to?
      if (from>to)
        throw new GedcomException("");
      // calculate content
      content();
      // done
    }
    
    /** 
     * calculate a content
     */
    private final void content() {
      Entity e = pe.getEntity();
      content = e.toString();
    }
    /**
     * String representation
     */
    public String toString() {
      return content;
    }
    /**
     * The entity for that event
     */
    /*package*/ Entity getEntity() {
      return pe.getEntity();
    }
    /**
     * The property for that event
     */
    /*package*/ PropertyEvent getProperty() {
      return pe;
    }
  } //Event
  
  /**
   * Interface for listeners
   */
  /*package*/ interface Listener {
    /**
     * callback for data changes
     */
    public void dataChanged();
    /**
     * callback for structure (and data) changes
     */
    public void structureChanged();
  } //ModelListener

  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    createEvents();
  }

  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    createEvents();
  }

  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    gedcomPropertyDeleted(gedcom, added, -1, added);
  }

  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    gedcomPropertyDeleted(gedcom, property, -1, property);
  }

  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
    if (deleted instanceof PropertyDate) {
      createEvents();
    } else if (deleted instanceof PropertyName) {
      contentEvents(property.getEntity());
      fireDataChanged();
    }
  }
  
} //TimelineModel 
