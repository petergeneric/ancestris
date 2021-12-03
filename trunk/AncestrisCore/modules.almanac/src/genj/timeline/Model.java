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
import ancestris.util.TimingUtility;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.windows.WindowManager;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
class Model {

    private static final Logger LOG = Logger.getLogger("ancestris.chronology");

    /**
     * the context and gedcom we're looking at
     */
    private Context context;
    private Gedcom gedcom;

    /**
     * limits
     */
    public double max = Double.NaN, min = Double.NaN;
    public double now = today();

    /**
     * a filter for events that we're interested in
     */
    private final Set<TagPath> paths = new HashSet<>();
    private final Set<String> tags = new HashSet<>();

    /**
     * default filter
     */
    private final static String[] DEFAULT_PATHS = new String[]{
        "INDI:BIRT", "INDI:BAPM", "FAM:MARR", "FAM:DIV", "INDI:DEAT"
    };

    /**
     * our levels
     */
    // Data maps for sorted elements
    private final Map<Double, Event> eventMap = new TreeMap<>();
    private final Map<Indi, EventSerie> indiSeries = new HashMap<>();
    private static final Double INCREMENT_D = 0.00000001;

    // Layers
    public List<List<Event>> eventLayers = new ArrayList<>();
    public List<List<EventSerie>> indiLayers = new ArrayList<>();

    /**
     * time per event
     */
    double timeBeforeEvent = 0.5D, timeAfterEvent = 2.0D, cmPerYear = 0D;
    static int EST_SPAN = 9;  // number of years to estimate life span when dates are not indicated
    static int EST_LIVING = 100;  // number of years to estimate a living person 
    boolean isPackIndi = false;   // true means pack layers for indi 

    /**
     * listeners
     */
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Callback callback = new Callback();

    /**
     * multi-threading
     */
    private RequestProcessor.Task layoutAllLayersThread, layoutEventLayersThread, layoutIndiLayersThread;
    private final Object lock = new Object();
    private boolean isRebuilding = false;
    private boolean isRedrawing = false;
    private static RequestProcessor RP = null;
    private int progressCounter = 0;
    private boolean isGedcomChanging = false;

    /**
     * interaction with view
     */
    private final TimelineView view;

    /**
     * Constructor
     */
    /*package*/
    public Model(TimelineView view) {
        this.view = view;
    }

    /**
     * Sets the filter - set of Tags we consider
     */
    public void setPaths(Collection<TagPath> set, boolean rebuild) {

        if (set == null) {
            set = Arrays.asList(TagPath.toArray(DEFAULT_PATHS));
        }

        paths.clear();
        tags.clear();

        for (TagPath path : set) {
            paths.add(path);
            tags.add(path.getLast());
        }

        if (rebuild) {
            createAndLayoutAllLayers();
        }

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
    public void setGedcom(Context context) {

        if (context == null) {
            return;
        }

        Gedcom newGedcom = context.getGedcom();
        if (gedcom == newGedcom) {
            if (this.context != context) {
                this.context = context;
                view.update();
            }
            return;
        }

        this.context = context;

        // old?
        if (gedcom != null) {
            gedcom.removeGedcomListener(callback);
        }

        // keep
        gedcom = newGedcom;

        // new?
        if (gedcom != null) {
            gedcom.addGedcomListener(callback);
        }

        // create events because gedcom is different
        createAndLayoutAllLayers();
    }

    private void updateView() {
        view.update();
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
        if (gedcom != null && callback != null) {
            gedcom.removeGedcomListener(callback);
        }
    }

    /**
     * change time per event
     */
    /*package*/ void setTimePerEvent(double before, double after, double cm, boolean redraw) {
        // already there?
        if (timeBeforeEvent == before && timeAfterEvent == after) {
            return;
        }

        if (eventMap == null || indiSeries == null) {
            return;
        }

        // reset min, max and zoom
        timeBeforeEvent = before;
        timeAfterEvent = after;
        cmPerYear = cm;

        if (redraw) {
            layoutLayers(false);
        }

        // done
    }

    public void setPackIndi(boolean set, boolean redraw) {
        isPackIndi = set;
        if (redraw) {
            layoutLayers(true);
        }
    }

    public void layoutLayers(boolean indiOnly) {
        setMinMax();
        if (isRebuilding || isRedrawing) {
            return;
        }
        if (!indiOnly) {
            layoutEventLayers();
        }
        layoutIndiLayers();
    }

    private void setMinMax() {
        if (!eventMap.isEmpty()) {
            List<Double> tmpList = new ArrayList(eventMap.keySet());
            min = tmpList.get(0) - 2 * timeBeforeEvent;
            max = Math.max(tmpList.get(eventMap.size() - 1), now + 1) + 2 * timeAfterEvent;
        }
    }

    public boolean isReady() {
        return !isRebuilding && !isRedrawing;
    }

    public double getCmPerYear() {
        return cmPerYear;
    }

    /**
     * Convert a point in time into a gregorian year (double)
     */
    private static double today() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        PointInTime pit = new PointInTime(cal);
        try {
            return toDouble(pit, false);
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
        }
        return 0d;
    }

    /**
     * Convert a point in time into a gregorian year (double)
     */
    public static double toDouble(PointInTime pit, boolean roundUp) throws GedcomException {

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
     * Returns all the entities related to the contex Add families to context in
     * case of indis, and add spouses and kids in case of families
     */
    public List<Entity> getAllContextEntities(Context context) {
        List<Entity> ents = new ArrayList<>();
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

    private Indi getIndiFromEntity(Entity entity) {
        if (entity == null) {
            return null;
        }

        // Case of indi
        if (entity instanceof Indi) {
            return (Indi) entity;
        }

        // Case of fam
        if (entity instanceof Fam) {
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            Indi wife = fam.getWife();
            if (husb == null && wife == null) {
                Indi[] children = fam.getChildren(true);
                if (children.length == 0) {
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
            Indi ancestorHusb = getOldestAgnaticAncestor(husb, new HashSet<>());
            Indi ancestorWife = getOldestAgnaticAncestor(wife, new HashSet<>());
            PropertyDate ahbd = ancestorHusb.getBirthDate();
            PropertyDate awbd = ancestorWife.getBirthDate();
            if (ahbd != null && awbd != null && ahbd.isValid() && awbd.isValid()) {
                if (awbd.compareTo(ahbd) > 0) {
                    return husb;
                } else {
                    return wife;
                }
            }
            if ((ahbd == null || !ahbd.isValid()) && (awbd != null && awbd.isValid())) {
                return wife;
            }
            if ((ahbd != null && ahbd.isValid()) && (awbd == null || !awbd.isValid())) {
                return husb;
            }
            return husb;
        }

        // Case of other entity, find a linkned indi
        Entity target;
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
        } // else find first indi of genealogy
        return (Indi) entity.getGedcom().getFirstEntity("INDI");
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

    public int getMaxLayersNumber() {
        return Math.max(indiLayers == null ? 0 : indiLayers.size(), eventLayers == null ? 0 : eventLayers.size());
    }

    public int getLayersNumber(int mode) {
        if (mode == TimelineView.INDI_MODE) {
            return indiLayers == null ? 0 : indiLayers.size();
        } else {
            return eventLayers == null ? 0 : eventLayers.size();
        }
    }

    /**
     * Rebuild all layers from scratch
     */
    private void createAndLayoutAllLayers() {

        if (gedcom == null || isRebuilding) {
            return;
        }

        final TimingUtility tu = new TimingUtility();
        LOG.log(Level.FINER, tu.getTime() + " - Launch tasks to create all events, individuals and then lay out the layers");

        // the progress bar
        final ProgressHandle ph = ProgressHandle.createHandle("", () -> {
            if (null == layoutAllLayersThread) {
                return false;
            }
            return layoutAllLayersThread.cancel();
        });

        // Define task to be launched
        Runnable runnable = () -> {
            synchronized (lock) {
                while (isRebuilding || isRedrawing) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                isRebuilding = true;
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to create all events, individuals and then lay out the layers...Start.");
                // reset sizes and maps
                min = Double.MAX_VALUE;
                max = -Double.MAX_VALUE;
                eventMap.clear();
                indiSeries.clear();
                
                try {
                    // Calculate events
                    ph.setDisplayName(NbBundle.getMessage(getClass(), "TXT_CreateLayers_Msg1"));
                    ph.start();
                    ph.switchToDeterminate(gedcom.getIndis().size() + gedcom.getFamilies().size());
                    progressCounter = 0;
                    createEventsFromEntities(gedcom.getEntities(Gedcom.INDI).iterator(), ph);
                    createEventsFromEntities(gedcom.getEntities(Gedcom.FAM).iterator(), ph);
                    isRebuilding = false;
                    
                    // Create layers in two separate tasks
                    setMinMax();
                    layoutEventLayers();
                    layoutIndiLayers();
                    lock.notifyAll();
                    
                } catch (MissingResourceException t) {
                    LOG.log(Level.SEVERE, "Error in calculating events", t);
                }
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to create all events, individuals and then lay out the layers...End.");
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("Chrono Model View", 1, true);
        }
        layoutAllLayersThread = RP.create(runnable); //the task is not started yet

        layoutAllLayersThread.addTaskListener((Task task) -> {
            ph.finish();
            isRebuilding = false;
        });

        layoutAllLayersThread.schedule(0); //start the task

        // done
    }

    /**
     * Layout events by using the existing set of events and re-stacking them in
     * eventLayers
     */
    private void layoutEventLayers() {

        if (gedcom == null) {
            return;
        }

        final TimingUtility tu = new TimingUtility();
        LOG.log(Level.FINER, tu.getTime() + " - Launch task to lay out EVENT layers");

        // the progress bar
        final ProgressHandle ph = ProgressHandle.createHandle(NbBundle.getMessage(getClass(), "TXT_CreateLayers_Msg2"), () -> {
            if (null == layoutEventLayersThread) {
                return false;
            }
            return layoutEventLayersThread.cancel();
        });

        // Define task to be launched
        Runnable runnable = () -> {
            synchronized (lock) {
                while (isRebuilding || isRedrawing) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                isRedrawing = true;
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to lay out EVENT layers...Start.");
                try {
                    ph.start();
                    ph.switchToDeterminate(eventMap.size());
                    progressCounter = 0;
                    createEventLayers(ph);
                    lock.notifyAll();
                    
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "Error in calculating events", t);
                }
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to lay out EVENT layers...End.");
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("Chrono Model View", 1, true);
        }
        layoutEventLayersThread = RP.create(runnable); //the task is not started yet

        layoutEventLayersThread.addTaskListener((Task task) -> {
            ph.finish();
            
            // Trigger change
            fireStructureChanged();
            
            isRedrawing = false;
        });

        layoutEventLayersThread.schedule(0); //start the task

        // done
    }

    /**
     * Layout indi layers
     */
    private void layoutIndiLayers() {

        if (gedcom == null) {
            return;
        }

        final TimingUtility tu = new TimingUtility();
        LOG.log(Level.FINER, tu.getTime() + " - Launch task to lay out INDI layers");

        // the progress bar
        final ProgressHandle ph = ProgressHandle.createHandle(NbBundle.getMessage(getClass(), "TXT_CreateLayers_Msg2"), () -> {
            if (null == layoutIndiLayersThread) {
                return false;
            }
            return layoutIndiLayersThread.cancel();
        });

        // Define task to be launched
        Runnable runnable = () -> {
            synchronized (lock) {
                while (isRebuilding || isRedrawing) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                isRedrawing = true;
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to lay out INDI layers...Start.");
                try {
                    ph.start();
                    ph.switchToDeterminate(gedcom.getIndis().size());
                    progressCounter = 0;
                    createIndiLayers(ph);
                    lock.notifyAll();
                    
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "Error in calculating events", t);
                }
                LOG.log(Level.FINER, tu.getTime() + " - Executing tasks to lay out INDI layers...End.");
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("Chrono Model View", 1, true);
        }
        layoutIndiLayersThread = RP.create(runnable); //the task is not started yet

        layoutIndiLayersThread.addTaskListener((Task task) -> {
            ph.finish();
            
            // Trigger change
            fireStructureChanged();
            
            isRedrawing = false;
            updateView();
        });

        layoutIndiLayersThread.schedule(0); //start the task

        // done
    }

    /**
     * Returns the events that cover the given context
     */
    protected LinkedList<Event> getEvents() {

        final LinkedList<Event> propertyHits = new LinkedList<>();
        final LinkedList<Event> entityHits = new LinkedList<>();

        if (eventLayers == null || eventLayers.isEmpty()) {
            return entityHits;
        }

        List<? extends Property> props = context.getProperties();
        List<Entity> ents = getAllContextEntities(context);

        synchronized (lock) {
            while (isRebuilding || isRedrawing) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            for (List<Event> eventLayer : eventLayers) {
                Iterator events = ((List) eventLayer).iterator();
                while (events.hasNext()) {
                    Event event = (Event) events.next();
                    for (Entity ent : ents) {
                        if (ent == event.getEntity()) {
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
            lock.notifyAll();
        }

        return propertyHits.isEmpty() ? entityHits : propertyHits;
    }

    /**
     * Returns the events that cover the given context
     */
    protected List<EventSerie> getIndis() {

        final LinkedList<EventSerie> propertyHits = new LinkedList<>();
        final LinkedList<EventSerie> entityHits = new LinkedList<>();

        if (indiLayers == null || indiLayers.isEmpty()) {
            return entityHits;
        }

        synchronized (lock) {
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
            lock.notifyAll();
        }

        return propertyHits.isEmpty() ? entityHits : propertyHits;
    }

    public List<Indi> getIndisFromLayers() {
        List<Indi> ret = new ArrayList<>();

        if (indiLayers == null || indiLayers.isEmpty()) {
            return ret;
        }

        synchronized (lock) {
            for (List<EventSerie> indiLayer : indiLayers) {
                Iterator it = ((List) indiLayer).iterator();
                while (it.hasNext()) {
                    EventSerie eventSerie = (EventSerie) it.next();
                    ret.add(eventSerie.indi);
                }
            }
            lock.notifyAll();
        }

        return ret;
    }

    /**
     * Free up memory
     */
    public void eraseAll() {

        Runnable runnable = () -> {
            // List<List<Event>> eventLayers
            for (Iterator<List<Event>> it = eventLayers.iterator(); it.hasNext();) {
                for (Iterator<Event> it2 = it.next().iterator(); it2.hasNext();) {
                    it2.next();
                    it2.remove();
                }
                it.remove();
            }
            
            // List<List<EventSerie>> indiLayers
            for (Iterator<List<EventSerie>> it = indiLayers.iterator(); it.hasNext();) {
                for (Iterator<EventSerie> it2 = it.next().iterator(); it2.hasNext();) {
                    it2.next();
                    it2.remove();
                }
                it.remove();
            }
            
            // Map<Double, Event> eventMap
            for (Iterator<Map.Entry<Double, Event>> it = eventMap.entrySet().iterator(); it.hasNext();) {
                it.next();
                it.remove();
            }
            
            // Map<Indi, EventSerie> indiSeries
            for (Iterator<Map.Entry<Indi, EventSerie>> it = indiSeries.entrySet().iterator(); it.hasNext();) {
                it.next();
                it.remove();
            }
        };

        new RequestProcessor("interruptible tasks", 1, true).create(runnable).schedule(0);
    }

    /**
     * Gather Events for given entities
     *
     * @param es list of entities to find events in
     */
    private void createEventsFromEntities(Iterator es, ProgressHandle ph) {
        // loop through entities
        while (es.hasNext()) {
            ph.progress(progressCounter++);
            Entity ent = (Entity) es.next();
            Property[] props = ent.getProperties();
            for (Property p : props) {
                if (tags.contains(p.getTag())) {
                    try {
                        createEventFromEntityEvent(ent, (PropertyEvent) p);
                    } catch (ClassCastException e) {
                        LOG.log(Level.INFO, "Unable to convert property : " + p.toString() + " entity :" + ent.getId(), e);
                    }
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
        // done
    }

    /**
     * Gather Event for given PropertyEvent
     *
     * @param pe property to use
     */
    private void createEventFromEntityEvent(Entity ent, PropertyEvent pe) {
        // we need a valid date for new event
        PropertyDate pd = pe.getDate();
        if (pd == null || !pd.isValid() || !pd.isComparable()) {
            return;
        }

        // Event is valid. Update events and indis series. 
        try {
            // Create event
            Event e = new Event(pe, pd);

            // Store event
            Double key = e.from;
            while (eventMap.containsKey(key)) {
                key += INCREMENT_D;
            }
            eventMap.put(key, e);

            // Store indis
            if (ent instanceof Indi) {
                updateEventSeriesWithIndi((Indi) ent, e);
            } else if (ent instanceof Fam) {
                Fam fam = (Fam) ent;
                Indi husb = fam.getHusband();
                if (husb != null) {
                    updateEventSeriesWithIndi(husb, e);
                }
                Indi wife = fam.getWife();
                if (wife != null) {
                    updateEventSeriesWithIndi(wife, e);
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
    private void updateEventSeriesWithIndi(Indi indi, Event e) {

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
     * Create our eventLayers from the sorted set of eventMap. Algorythm is
     * optimized for speed.
     */
    private void createEventLayers(ProgressHandle ph) {

        // Reset everything
        eventLayers.clear();

        // Use interim map 
        Double gap = Math.max(timeBeforeEvent, timeAfterEvent);
        SortedMap<Double, Integer> endLimits = new TreeMap<>();
        Double firstKey;
        Iterator<Double> iterator = eventMap.keySet().iterator();
        if (!iterator.hasNext()) {
            return;
        }

        // Init first element to avoid looping everytime on initial test
        Double key = iterator.next();
        Double lKey;
        Event event = eventMap.get(key);
        List<Event> layer = new LinkedList<>();
        layer.add(event);
        endLimits.put(key - event.from + event.to + gap, eventLayers.size());
        eventLayers.add(layer);
        ph.progress(progressCounter++);

        // Loop on remaining events after the first one
        while (iterator.hasNext()) {
            key = iterator.next();
            event = eventMap.get(key);
            firstKey = endLimits.firstKey();
            if (key > firstKey) {
                int l = endLimits.get(firstKey);
                layer = eventLayers.get(l);
                layer.add(event);
                endLimits.remove(firstKey);
                lKey = key - event.from + event.to + gap;
                while (endLimits.containsKey(lKey)) {
                    lKey += INCREMENT_D;
                }
                endLimits.put(lKey, l);
            } else {
                layer = new LinkedList<Event>();
                layer.add(event);
                lKey = key - event.from + event.to + gap;
                while (endLimits.containsKey(lKey)) {
                    lKey += INCREMENT_D;
                }
                endLimits.put(lKey, eventLayers.size());
                eventLayers.add(layer);
            }
            ph.progress(progressCounter++);
        }
    }

    private void createIndiPackedLayers(ProgressHandle ph) {

        Map<Double, EventSerie> indiMap = new TreeMap<>();
        Double tKey;
        for (EventSerie es : indiSeries.values()) {
            tKey = es.from;
            while (indiMap.containsKey(tKey)) {
                tKey += INCREMENT_D;
            }
            indiMap.put(tKey, es);
        }

        // Use interim map 
        Double gap = Math.max(timeBeforeEvent, timeAfterEvent);
        SortedMap<Double, Integer> endLimits = new TreeMap<>();
        Double firstKey;
        Iterator<Double> iterator = indiMap.keySet().iterator();

        // Init first element to avoid looping everytime on initial test
        Double key = iterator.next();
        Double lKey;
        EventSerie event = indiMap.get(key);
        List<EventSerie> layer = new LinkedList<>();
        layer.add(event);
        endLimits.put(key - event.from + event.to + gap, indiLayers.size());
        indiLayers.add(layer);
        ph.progress(progressCounter++);

        // Loop on remaining events after the first one
        while (iterator.hasNext()) {
            key = iterator.next();
            event = indiMap.get(key);
            firstKey = endLimits.firstKey();
            if (key > firstKey) {
                int l = endLimits.get(firstKey);
                layer = indiLayers.get(l);
                layer.add(event);
                endLimits.remove(firstKey);
                lKey = key - event.from + event.to + gap;
                endLimits.put(lKey, l);
            } else {
                layer = new LinkedList<EventSerie>();
                layer.add(event);
                lKey = key - event.from + event.to + gap;
                endLimits.put(lKey, indiLayers.size());
                indiLayers.add(layer);
            }
            ph.progress(progressCounter++);
        }
    }

    /**
     * Create our indiLayers traversing the trees from the ancestors
     */
    public void createIndiLayers(ProgressHandle ph) {

        if (context == null) {
            return;
        }

        // Reset everything
        indiLayers.clear();
        if (isPackIndi) {
            createIndiPackedLayers(ph);
            return;
        }
        Set<Indi> tmpTraversedIndi = new HashSet<>();
        indiSeries.values().forEach((es) -> {
            es.layered = false;
        });

        // Insert empty layer at the top
        List<EventSerie> layer = new LinkedList<>();
        indiLayers.add(layer);
        layer.add(new EventSerie(null));

        // (Re)build layers using scan traverse of the tree from root indi, getting ancestor-most individual along the way and descending kids
        Entity entity = context.getEntity();
        Indi rootIndi = getIndiFromEntity(entity);
        if (rootIndi == null) {
            return;
        }
        traverseTreeFromIndi(rootIndi, tmpTraversedIndi, ph);

        // Do the same for the remaining individuals, but in other layers, separating each "tree" by a blank line
        List<EventSerie> values = new LinkedList(indiSeries.values());
        Collections.sort(values, (Object o1, Object o2) -> {
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
        });
        for (EventSerie es : values) {
            if (es.layered) {
                continue;
            }
            // add empty layer and reset base layer
            if (indiLayers.size() > 1 && !isPackIndi) {
                layer = new LinkedList<>();
                indiLayers.add(layer);
                layer.add(new EventSerie(null));
            }
            traverseTreeFromIndi(es.indi, tmpTraversedIndi, ph);
        }

        // Insert empty layer at bottom
        layer = new LinkedList<>();
        indiLayers.add(layer);
        layer.add(new EventSerie(null));
        view.setRootTitle(rootIndi.toString(true));
    }

    private void traverseTreeFromIndi(Indi rootIndi, Set<Indi> set, ProgressHandle ph) {
        if (rootIndi == null) {
            return;
        }

        Stack<Indi> indiStack = new Stack<>();

        Indi indi = getOldestAgnaticAncestor(rootIndi, new HashSet<>());
        while (indi != null) {
            if (!set.contains(indi)) {
                set.add(indi);
                EventSerie es = indiSeries.get(indi);
                if (es != null && !es.layered) {
                    es.layered = true;
                    List<EventSerie> layer = new LinkedList<>();
                    layer.add(es);
                    indiLayers.add(layer);
                }
                ph.progress(progressCounter++);
            }
            indi = getNextIndiInTree(indi, set, indiStack);
        }
    }

    /**
     * Get oldest ancestor from agnatic line
     *
     * @param indi
     * @return oldest ancestor or individual itself. Never returns null.
     */
    private Indi getOldestAgnaticAncestor(Indi indi, Set<Indi> visited) {
        if (visited.contains(indi)) {
            return null;
        }
        visited.add(indi);
        
        Fam fam = indi.getFamilyWhereBiologicalChild();
        if (fam != null) {
            Indi father = fam.getHusband();
            if (father != null) {
                return getOldestAgnaticAncestor(father, visited);
            }
            Indi mother = fam.getWife();
            if (mother != null) {
                return getOldestAgnaticAncestor(mother, visited);
            }
        }
        return indi;
    }

    /**
     * Get next indi in tree from indi position Logical of next individual is as
     * follows: - spouse oldest ancestor, if not already done, or -
     *
     * @param indi
     * @param set
     * @return
     */
    public Indi getNextIndiInTree(Indi indi, Set<Indi> set, Stack<Indi> stack) {

        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            Indi spouse = fam.getOtherSpouse(indi);
            if (spouse != null && !set.contains(spouse)) {
                Indi ancestor = getOldestAgnaticAncestor(spouse, new HashSet<>());
                stack.push(indi);
                if (ancestor != null && !set.contains(ancestor)) {
                    return ancestor;
                } else {
                    return spouse;
                }
            }
            Indi[] children = fam.getChildren(true); // get children sorted
            for (Indi child : children) {
                if (!set.contains(child)) {
                    stack.push(child);
                    return child;
                }
            }
        }
        Fam fam = indi.getFamilyWhereBiologicalChild();
        if (fam != null) {
            Indi[] children = fam.getChildren(true); // get children sorted
            for (Indi child : children) {
                if (!set.contains(child)) {
                    stack.push(child);
                    return child;
                }
            }
        }
        if (!stack.empty()) {
            return stack.pop();
        }
        return null;
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
            events = new TreeSet<>();
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
                if (maxDate < (now - EST_LIVING)) {
                    return Math.min(maxDate + EST_SPAN, now);
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

    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {
            if (!isGedcomChanging) {
                WindowManager.getDefault().invokeWhenUIReady(() -> {
                    isGedcomChanging = true;
                    createAndLayoutAllLayers();
                    isGedcomChanging = false;
                });
            }
        }
    }

}
