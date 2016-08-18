/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.timeline;

import ancestris.core.TextOptions;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model implements GedcomListener {

    /**
     * the context and gedcom we're looking at
     */
    private Context context;
    private Gedcom gedcom;

    /**
     * limits
     */
    /*package*/
    double max = Double.NaN, min = Double.NaN;

    /**
     * a filter for events that we're interested in
     */
    private Set<TagPath> paths = new HashSet<TagPath>();
    private Set<String> tags = new HashSet<String>();

    /**
     * default filter
     */
    private final static String[] DEFAULT_PATHS = new String[]{
        "INDI:BIRT", "INDI:BAPM", "FAM:MARR", "FAM:DIV", "INDI:DEAT"
    };

    /**
     * our levels
     */
    /*package*/
    public List<List<Event>> eventLayers;
    public List<List<EventSerie>> indiLayers;
    public Map<Indi, EventSerie> indiSeries;
    private Set<Indi> tmpRecursedIndi;

    /**
     * time per event
     */
    /*package*/
    double timeBeforeEvent = 0.5D, timeAfterEvent = 2.0D;
    static int EST_SPAN = 9;  // number of years to estimate life span when dates are not indicated
    static int EST_LIVING = 100;  // number of years to estimate a living person 
    boolean isPackIndi = false;   // true means pack layers for indi 

    /**
     * listeners
     */
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    /**
     * Constructor
     */
    /*package*/
    public Model() {
    }

    /**
     * Sets the filter - set of Tags we consider
     */
    public void setPaths(Collection<TagPath> set) {

        if (set == null) {
            set = Arrays.asList(TagPath.toArray(DEFAULT_PATHS));
        }

        paths.clear();
        tags.clear();

        for (TagPath path : set) {
            paths.add(path);
            tags.add(path.getLast());
        }

        // re-generate events
        createLayers();

        // done
    }

    /**
     * gedcom we're looking at
     */
    /*package*/ Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * context and gedcom to look at
     */
    /*package*/ void setGedcom(Context context) {

        // noop
        if (context == null) {
            return;
        }
        this.context = context;
        
        Gedcom newGedcom = context.getGedcom();
        if (gedcom == newGedcom) {
            return;
        }

        // old?
        if (gedcom != null) {
            gedcom.removeGedcomListener(this);
        }

        // keep
        gedcom = newGedcom;

        // new?
        if (gedcom != null) {
            gedcom.addGedcomListener(this);
        }

        // create events
        createLayers();

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
        if (timeBeforeEvent == before && timeAfterEvent == after) {
            return;
        }
        // remember
        timeBeforeEvent = before;
        timeAfterEvent = after;
        // layout the events we've got
        if (eventLayers != null) {
            layoutEvents();
        }
        // done
    }

    /**
     * Convert a point in time into a gregorian year (double)
     */
    /*package*/ static double toDouble(PointInTime pit, boolean roundUp) throws GedcomException {

        // all Gregorian for now
        Calendar calendar = PointInTime.GREGORIAN;

        if (pit.getCalendar() != calendar) {
            pit = pit.getPointInTime(calendar);
        }

        // year
        int year = pit.getYear();
        double result = year;

        // month
        int month = pit.getMonth();
        if (month == PointInTime.UNKNOWN) {
            return roundUp ? result + 1 : result;
        }

        double months = calendar.getMonths();
        result += month / months;

        // day
        int day = pit.getDay();
        if (day == PointInTime.UNKNOWN) {
            return roundUp ? result + 1 / months : result;
        }

        double days = calendar.getDays(month, year);
        result += day / months / days;

        // done
        return result;
    }

    /**
     * Returns a point in time for a year (double)
     */
    /*package*/ static PointInTime toPointInTime(double year) {

        Calendar calendar = PointInTime.GREGORIAN;

        int months = calendar.getMonths();

        int y = (int) Math.floor(year);

        year = year % 1;
        if (year < 0) {
            year = 1 + year;
        }

        int m = (int) Math.floor(year * months);

        int days = calendar.getDays(m, y);

        int d = (int) Math.floor((year * months) % 1 * days);

        return new PointInTime(d, m, y);
    }

    /**
     * Returns an event by year/layer
     */
    protected Event getEvent(double year, int layer) {
        if (layer >= eventLayers.size()) {
            return null;
        }
        // look for events in appropriate layer
        Iterator events = ((List) eventLayers.get(layer)).iterator();
        while (events.hasNext()) {
            Event event = (Event) events.next();
            if (event.from - timeBeforeEvent < year && year < event.to + timeAfterEvent) {
                return event;
            }
        }
        // done
        return null;
    }

    /**
     * Returns an event by year/layer
     */
    protected EventSerie getEventSerie(double year, int layer) {
        if (layer >= indiLayers.size()) {
            return null;
        }
        // look for indis in appropriate layer
        Iterator eventSeries = ((List) indiLayers.get(layer)).iterator();
        while (eventSeries.hasNext()) {
            EventSerie eventSerie = (EventSerie) eventSeries.next();
            if (eventSerie.from - timeBeforeEvent < year && year < eventSerie.to + timeAfterEvent) {
                return eventSerie;
            }
        }
        // done
        return null;
    }

    public int getLayerFromEvent(Event event) {
        if (event == null) {
            return 0;
        }
        // look for events in appropriate layer
        int layer = 0;
        for (List<Event> layers : eventLayers) {
            for (Event e : layers) {
                if (e == event) {
                    return layer;
                }
            }
            layer++;
        }
        return 0;
    }

    public int getLayerFromEventSerie(EventSerie eventSerie) {
        if (eventSerie == null) {
            return 0;
        }
        // look for events in appropriate layer
        int layer = 0;
        for (List<EventSerie> layers : indiLayers) {
            for (EventSerie es : layers) {
                if (es == eventSerie) {
                    return layer;
                }
            }
            layer++;
        }
        return 0;
    }

    /**
     * Returns the events that cover the given context
     */
    protected Set<Event> getEvents(Context context) {

        Set<Event> propertyHits = new HashSet<Event>();
        Set<Event> entityHits = new HashSet<Event>();

        List<? extends Property> props = context.getProperties();
        List<Entity> ents = getAllContextEntities(context);

        for (List<Event> eventLayer : eventLayers) {
            Iterator events = ((List) eventLayer).iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                for (int j = 0; j < ents.size(); j++) {
                    if (ents.get(j) == event.getEntity()) {
                        entityHits.add(event);
                    }
                }
                for (int i = 0; i < props.size(); i++) {
                    if (event.getProperty() == props.get(i) || event.getProperty().contains(props.get(i))) {
                        propertyHits.add(event);
                    }
                }
            }
        }

        return propertyHits.isEmpty() ? entityHits : propertyHits;
    }

    /**
     * Returns the events that cover the given context
     */
    protected Set<EventSerie> getIndis(Context context) {

        Set<EventSerie> propertyHits = new HashSet<EventSerie>();
        Set<EventSerie> entityHits = new HashSet<EventSerie>();

        List<? extends Property> props = context.getProperties();
        List<? extends Entity> ents = getAllContextEntities(context);

        for (List<EventSerie> indiLayer : indiLayers) {
            Iterator indis = ((List) indiLayer).iterator();
            while (indis.hasNext()) {
                EventSerie eventSerie = (EventSerie) indis.next();
                for (int j = 0; j < ents.size(); j++) {
                    if (ents.get(j) == eventSerie.getEntity()) {
                        entityHits.add(eventSerie);
                    }
                }
                for (int i = 0; i < props.size(); i++) {
                    if (eventSerie.getProperty() == props.get(i) || eventSerie.contains(props.get(i))) {
                        propertyHits.add(eventSerie);
                    }
                }
            }
        }

        return propertyHits.isEmpty() ? entityHits : propertyHits;
    }

    /**
     * Returns all the entities related to the contex
     * Add families to context in case of indis, 
     * and add spouses and kids in case of families
     */
    public List<Entity> getAllContextEntities(Context context) {
        List<Entity> ents = new ArrayList<Entity>();
        for (Entity ent : context.getEntities()) {
            ents.add(ent);
            if (ent instanceof Indi) {
                Indi indi = (Indi) ent;
                Fam[] fams = indi.getFamiliesWhereSpouse();
                ents.addAll(Arrays.asList(fams));
            } 
            if (ent instanceof Fam) {
                Fam fam = (Fam) ent;
                Indi husb = fam.getHusband();
                if (husb != null) {
                    ents.add(husb);
                }
                Indi wife = fam.getWife();
                if (wife != null) {
                    ents.add(wife);
                }
                ents.addAll(Arrays.asList(fam.getChildren()));
            }
            Indi indi = getIndiFromEntity(ent);
            if (indi != null) {
                ents.add(indi);
            }
        }
        return ents;
    }

    
    /**
     * Returns the filter - set of Tags we consider
     */
    public Set<TagPath> getPaths() {
        return Collections.unmodifiableSet(paths);
    }

    /**
     * Trigger callback - our structure has changed
     */
    private void fireStructureChanged() {
        for (int l = listeners.size() - 1; l >= 0; l--) {
            (listeners.get(l)).structureChanged();
        }
    }

    /**
     * Trigger callback - our data has changed
     */
    private void fireDataChanged() {
        for (int l = listeners.size() - 1; l >= 0; l--) {
            (listeners.get(l)).dataChanged();
        }
    }

    /**
     * Retags events for given entity
     */
    private void contentEvents(Entity entity) {
        // loop through eventLayers
        for (List<Event> eventLayer : eventLayers) {
            List layer = (List) eventLayer;
            Iterator events = layer.iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                if (event.pe.getEntity() == entity) {
                    event.content();
                }
            }
        }
        // loop through indiLayers
        for (List<EventSerie> indiLayer : indiLayers) {
            List layer = (List) indiLayer;
            Iterator eventSeries = layer.iterator();
            while (eventSeries.hasNext()) {
                EventSerie eventSerie = (EventSerie) eventSeries.next();
                if (eventSerie.getEntity() == entity) {
                    eventSerie.content();
                }
            }
        }
        // done
    }

    /**
     * Gather Events
     */
    private void createLayers() {
        // reset
        min = Double.MAX_VALUE;
        max = -Double.MAX_VALUE;

        // prepare some space
        eventLayers = new ArrayList<List<Event>>(10);
        indiLayers = new ArrayList<List<EventSerie>>(10);
        indiSeries = new HashMap<Indi, EventSerie>();

        // look for events in INDIs and FAMs
        if (gedcom != null) {
            createLayersFrom(gedcom.getEntities(Gedcom.INDI).iterator());
            createLayersFrom(gedcom.getEntities(Gedcom.FAM).iterator());
            // Create indi layers now that they are built
            createIndiLayers();
        }

        // Extend time by before/after
        max += timeAfterEvent;
        min -= timeBeforeEvent;

        // Trigger
        fireStructureChanged();

        // done
    }

    /**
     * Gather Events for given entities
     *
     * @param es list of entities to find events in
     */
    private void createLayersFrom(Iterator es) {
        // loop through entities
        while (es.hasNext()) {
            Entity ent = (Entity) es.next();
            List ps = ent.getProperties(PropertyEvent.class);
            for (Object p : ps) {
                PropertyEvent pe = (PropertyEvent) p;
                if (tags.contains(pe.getTag())) {
                    createEventFrom(ent, pe);
                }
            }
        }
        // done
    }

    /**
     * Gather Event for given PropertyEvent
     *
     * @param pe property to use
     */
    private void createEventFrom(Entity ent, PropertyEvent pe) {
        // we need a valid date for new event
        PropertyDate pd = pe.getDate();
        if (pd == null || !pd.isValid() || !pd.isComparable()) {
            return;
        }

        // Event is valid. So insert it in event layers and insert it in indiLayers. 
        try {
            // Create event
            Event e = new Event(pe, pd);

            // Insert Event in layers of events
            insertEvent(e);

            // Get corresponding indis along the way
            if (ent instanceof Indi) {
                updateIndi((Indi) ent, e);
            } else if (ent instanceof Fam) {
                Fam fam = (Fam) ent;
                Indi husb = fam.getHusband();
                if (husb != null) {
                    updateIndi(husb, e);
                }
                Indi wife = fam.getWife();
                if (wife != null) {
                    updateIndi(wife, e);
                }
                // done
            }
        } catch (GedcomException e) {
        }
        // done
    }

    /**
     * Create or update EventSeries for individuals
     */
    private void updateIndi(Indi indi, Event e) {

        // Get EventSerie for that indi, or else create it
        EventSerie es = indiSeries.get(indi);
        if (es == null) {
            es = new EventSerie(indi);
            indiSeries.put(indi, es);
        }

        // Add event to the serie
        es.addEvent(e);

        // done
    }

    /**
     * Insert the Event into one of our eventLayers
     */
    private void insertEvent(Event e) {

        // remember min and max
        min = Math.min(Math.floor(e.from), min);
        max = Math.max(Math.ceil(e.to), max);

        // find a level that suits us
        for (List<Event> layer : eventLayers) {
            if (insertEvent(e, layer)) {
                return;
            }
            // continue
        }

        // create a new layer
        List<Event> layer = new LinkedList<Event>();
        eventLayers.add(layer);
        layer.add(e);

        // done
    }

    /**
     * Create our indiLayers traversing the trees from the ancestors
     */
    public void createIndiLayers() {
        
        if (context == null) {
            return; 
        }

        // Reset everything
        tmpRecursedIndi = new HashSet<Indi>();
        indiLayers.clear();
        for (EventSerie es : indiSeries.values()) {
            es.layered = false;
        }

        // Insert empty layer at the top
        List<EventSerie> layer = new LinkedList<EventSerie>();
        indiLayers.add(layer);
        layer.add(new EventSerie(null));

        // (Re)build layers using scan traverse of the tree from root indi, getting ancestor-most individual along the way and descending kids
        Entity entity = context.getEntity();
        Indi rootIndi = getIndiFromEntity(entity);
        if (rootIndi != null) {
            recurseTree(getOldestAgnaticAncestor(rootIndi));
        }

        // Do the same for the remaining individuals, starting with the longuest trees, but in other layers, separating each "tree" by a blank line
        List<EventSerie> values = new LinkedList(indiSeries.values());
        Collections.sort(values, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                double d1 = ((EventSerie) (o1)).from;
                double d2 = ((EventSerie) (o2)).from;
                if (d1 == d2) {
                    return 0;
                }
                if (d1 < d2) {
                    return -1;
                }
                if (d1 > d2) {
                    return +1;
                }
                return 0;
            }
        });
        for (EventSerie es : values) {
            if (es.layered) {
                continue;
            }
            // add empty layer and reset base layer
            if (indiLayers.size() > 1 && !isPackIndi) {
                layer = new LinkedList<EventSerie>();
                indiLayers.add(layer);
                layer.add(new EventSerie(null));
            }
            recurseTree(getOldestAgnaticAncestor(es.indi));
        }

        // Insert empty layer at bottom
        layer = new LinkedList<EventSerie>();
        indiLayers.add(layer);
        layer.add(new EventSerie(null));
    }
        
    private Indi getIndiFromEntity(Entity entity) {
        if (entity == null) {
            return null; 
        }
        if (entity instanceof Indi) {
            return (Indi) entity;
        }
        if (entity instanceof Fam) {
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            Indi wife = fam.getWife();
            if (husb == null && wife == null) {
                Indi[] children = fam.getChildren(true);
                if (children.length ==0) {
                    return null;
                }
                return children[0];
            }
            if (husb != null && wife == null) {
                return husb;
            } 
            if (wife != null && husb == null) {
                return wife;
            }
            Indi ancestorHusb = getOldestAgnaticAncestor(husb);
            Indi ancestorWife = getOldestAgnaticAncestor(wife);
            PropertyDate ahbd = ancestorHusb.getBirthDate();
            PropertyDate awbd = ancestorWife.getBirthDate();
            if (ahbd.isValid() && awbd.isValid()) {
                if (awbd.compareTo(ahbd) > 0) {
                    return husb;
                } else {
                    return wife;
                }
            }
            if (!ahbd.isValid() && awbd.isValid()) {
                return wife;
            }
            if (ahbd.isValid() && !awbd.isValid()) {
                return husb;
            }
            return husb;
        }
        Entity target = null;
        // return any indi linked to that entity
        for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
            target = xref.getTargetEntity();
            if (target instanceof Indi) {
                return (Indi) target;
            }
        }
        // else return any family
        for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
            target = xref.getTargetEntity();
            if (target instanceof Fam) {
                return getIndiFromEntity(target);
            }
        } // else give up
        return null;
    }
    
    
    private Indi getOldestAgnaticAncestor(Indi indi) {
        Fam fam = indi.getFamilyWhereBiologicalChild();
        if (fam != null) {
            Indi father = fam.getHusband();
            if (father != null) {
                return getOldestAgnaticAncestor(father);
            }
        }
        return indi;
    }

    public void recurseTree(Indi indi) {
        if (indi == null || tmpRecursedIndi.contains(indi)) {
            return;
        }
        tmpRecursedIndi.add(indi);
        insertEventSerie(indiSeries.get(indi));
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            Indi spouse = fam.getOtherSpouse(indi);
            if (spouse != null && !tmpRecursedIndi.contains(spouse)) {
                Indi ancestor = getOldestAgnaticAncestor(spouse);
                if (ancestor != null) {
                    recurseTree(ancestor);
                }
            }
            Indi[] children = fam.getChildren(true);
            for (Indi child : children) {
                recurseTree(child);
            }
        }
        
    }

    
    /**
     * Insert the EventSerie into one of our indiLayers
     */
    private void insertEventSerie(EventSerie es) {
        // return if already layered
        if (es == null || es.layered) {
            return;
        }
        es.layered = true;

        if (isPackIndi) {
            // Either find a level that suits us (leave first layer empty)
            for (int iLayer = 1; iLayer < indiLayers.size(); iLayer++) {
                if (insertEventSerie(es, indiLayers.get(iLayer))) {
                    return;
                }
                // continue
            }
        }

        // Or else create a new layer
        List<EventSerie> layer = new LinkedList<EventSerie>();
        indiLayers.add(layer);
        layer.add(es);
 
        // done
    }

    /**
     * Insert the Event into a layer
     *
     * @return whether that was successfull
     */
    private boolean insertEvent(Event candidate, List<Event> layer) {
        // loop through layer
        ListIterator<Event> events = layer.listIterator();
        do {
            Event event = events.next();
            // before?
            if (candidate.to + timeAfterEvent < event.from - timeBeforeEvent) {
                events.previous();
                events.add(candidate);
                return true;
            }
            // overlap?
            if (candidate.from - timeBeforeEvent < event.to + timeAfterEvent) {
                return false;
            }
            // after?
        } while (events.hasNext());
        // after!
        events.add(candidate);
        return true;
    }

    /**
     * Insert the EventSerie into a layer
     *
     * @return whether that was successfull
     */
    private boolean insertEventSerie(EventSerie candidate, List<EventSerie> layer) {
        // loop through layer
        ListIterator<EventSerie> eventSeries = layer.listIterator();
        do {
            EventSerie eventSerie = eventSeries.next();
            // before?
            if (candidate.to + timeAfterEvent < eventSerie.from - timeBeforeEvent) {
                eventSeries.previous();
                eventSeries.add(candidate);
                return true;
            }
            // overlap?
            if (candidate.from - timeBeforeEvent < eventSerie.to + timeAfterEvent) {
                return false;
            }
            // after?
        } while (eventSeries.hasNext());
        // after!
        eventSeries.add(candidate);
        return true;
    }

    /**
     * Layout events by using the existing set of events and re-stacking them in
     * eventLayers
     */
    private void layoutEvents() {
        // reset
        min = Double.MAX_VALUE;
        max = -Double.MAX_VALUE;
        // keep old and create some new space
        List<List<Event>> old = eventLayers;
        eventLayers = new ArrayList<List<Event>>(10);
        // loop through old
        for (List<Event> layer : old) {
            for (Event event : layer) {
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

    public int getMaxLayersNumber() {
        return Math.max(indiLayers.size(), eventLayers.size());
    }
    
    public int getLayersNumber(int mode) {
        if (mode == TimelineView.INDI_MODE) {
            return indiLayers.size();
        } else {
            return eventLayers.size();
        }
    }

    void setPackIndi(boolean set) {
        isPackIndi = set;
    }

    /**
     * An event in our model
     */
    /*package*/ class Event implements Comparable {

        /**
         * state
         */
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
            from = toDouble(propDate.getStart(), propDate.getFormat() == PropertyDate.AFTER);
            to = propDate.isRange() ? toDouble(propDate.getEnd(), false) : from;
            // from<to?
            if (from > to) {
                throw new GedcomException("");
            }
            // calculate content
            content();
            // done
        }

        /**
         * calculate a content
         */
        private void content() {
            Entity e = pe.getEntity();
            content = e.toString();
        }

        /**
         * String representation
         */
        @Override
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
         * The sex of the individual
         */
        /*package*/ int getSex() {
            Entity entity = pe.getEntity();
            if (entity instanceof Indi) {
                return ((Indi) entity).getSex();
            } else {
                return PropertySex.UNKNOWN;
            }
        }

        /**
         * The property for that event
         */
        /*package*/ PropertyEvent getProperty() {
            return pe;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Event) {
                double other = ((Event) o).from;
                return from > other ? +1 : from < other ? -1 : 0;
            } else {
                return -1;
            }
        }
    } //Event

    /**
     * An event serie in our model (events of an individual)
     */
    /*package*/ class EventSerie {

        /**
         * state
         */
        /*package*/ Indi indi;
        /*package*/ double from, to;
        /*package*/ SortedSet<Event> events;
        /*package*/ String content;
        /*package*/ boolean layered;

        /**
         * Constructor
         */
        EventSerie(Indi indi) {
            // remember
            this.indi = indi;
            events = new TreeSet<Event>();
            // setup time
            from = Double.MAX_VALUE;
            to = Double.MIN_VALUE;
            // calculate content
            content();
            // init layered (not yet in a layer)
            layered = false;
            // done
        }

        /**
         * calculate a content
         */
        private void content() {
            content = indi == null ? "" : indi.toString();
        }

        /**
         * String representation
         */
        @Override
        public String toString() {
            return content;
        }

        /**
         * The entity for that event
         */
        /*package*/ Entity getEntity() {
            return indi;
        }

        /**
         * The sex of the individual
         */
        /*package*/ int getSex() {
            return indi.getSex();
        }

        /**
         * The property for that event
         */
        /*package*/ PropertyEvent getProperty() {
            if (events.isEmpty()) {
                return null;
            } else {
                return events.first().getProperty();
            }
        }

        /**
         * Add event to the serie
         */
        public void addEvent(Event e) {
            events.add(e);
            from = Math.min(getFrom(), e.from);
            to = Math.max(getTo(), e.to);
        }

        /**
         * Get estimated From date - calculted one if birth is one of the events
         * - estimated to 25 years before the From date otherwise
         */
        public double getFrom() {
            double minDate = getFirstEvent().from;
            if (contains("BIRT")) {
                return minDate;
            } else {
                return minDate - EST_SPAN;
            }
        }

        /**
         * Get estimated To date - calculted one if death is one of the events -
         * estimated to last + 25 if (last+25) older than 100 years from today,
         * else today
         */
        public double getTo() {
            double maxDate = getLastEvent().to;
            if (contains("DEAT")) {
                return maxDate;
            } else {
                int now = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (maxDate < (now - EST_LIVING)) {
                    return maxDate + EST_SPAN;
                } else {
                    return now;
                }
            }
        }

        /**
         * Get list of event dates
         */
        public double[] getDates() {
            double[] ret = new double[events.size()];
            int i = 0;
            for (Event e : events) {
                ret[i] = e.from;
                i++;
            }
            return ret;
        }

        /**
         * Get image
         */
        public ImageIcon getImage() {
            return indi.getImage();
        }

        /**
         * Get tag
         */
        public String getTag() {
            return indi.getTag();
        }

        /**
         * Get display dates
         */
        public String getDisplayDates() {
            String birth = indi.getBirthAsString();
            String death = indi.getDeathAsString();
            return TextOptions.getInstance().getBirthSymbol() + (birth.isEmpty() ? "-" : birth) + " " + TextOptions.getInstance().getDeathSymbol() + (death.isEmpty() ? "-" : death);
        }

        /**
         * Get First event
         */
        public Event getFirstEvent() {
            return events.first();
        }

        /**
         * Get Last event
         */
        public Event getLastEvent() {
            return events.last();
        }

        /**
         * Test if EVEN is among the events
         */
        private boolean contains(String tag) {
            for (Event e : events) {
                if (e.pe.getTag().equals(tag)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Test if property is among the events
         */
        private boolean contains(Property prop) {
            for (Event e : events) {
                if (e.pe.equals(prop)) {
                    return true;
                }
            }
            return false;
        }

    } //EventSerie

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

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        createLayers();
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        createLayers();
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        gedcomPropertyDeleted(gedcom, added, -1, added);
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        gedcomPropertyDeleted(gedcom, property, -1, property);
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        if (deleted instanceof PropertyDate) {
            createLayers();
        } else if (deleted instanceof PropertyName) {
            contentEvents(property.getEntity());
            fireDataChanged();
        }
    }

} //TimelineModel 
