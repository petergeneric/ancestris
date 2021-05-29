/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
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
package genj.search;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background search worker
 */
/*package*/ abstract class Worker {

    /**
     * one listener
     */
    public WorkerListener listener;
    public int max_hits;
    public boolean case_sensitive;

    /**
     * current search state
     */
    public Gedcom gedcom;
    public Set<Entity> entities = new HashSet<>();
    protected List<Hit> hits = new ArrayList<>();
    public int hitCount = 0;
    public Matcher matcher;

    /**
     * thread
     */
    public Thread thread;
    public AtomicBoolean lock = new AtomicBoolean(false);
    public long lastFlush;

    Worker(WorkerListener listener) {
        this.listener = listener;
    }

    /*package*/ void stop() {

        synchronized (lock) {
            try {
                lock.set(false);
                if (thread != null) {
                    thread.interrupt();
                }
            } catch (Throwable t) {
            }
        }
    }

    public abstract void start(Gedcom gedcom, int max_hits, boolean case_sensitive, Object... args);
    
    public void flush() {
        // still more data to report?
        if (!hits.isEmpty()) {
            listener.more(Collections.unmodifiableList(hits));
            hits.clear();
        }
    }

    /**
     * search in gedcom (not on EDT)
     */
    public void search(Gedcom gedcom) {
        
        // Sort entities by id number in the results
        Comparator<Property> comparator = new Comparator<Property>() {
            @Override
            public int compare(Property p1, Property p2) {
                //return p1.toString().compareTo(p2.toString());
                return getValue(p1.getEntity().getId()).compareTo(getValue(p2.getEntity().getId()));
            }
            
            private Integer getValue(String id) {
                return Integer.parseInt(id.replaceAll("[^0-9]", ""));
            }
        };
        
        for (int t = 0; t < Gedcom.ENTITIES.length && hitCount < max_hits; t++) {
            for (Entity entity : gedcom.getEntities(Gedcom.ENTITIES[t], comparator)) {

                // next
                search(entity, entity);

                // still going?
                if (!lock.get()) {
                    return;
                }
            }
        }
    }

    
    public abstract void search(Entity entity, Property prop);
    
    /**
     * Returns a matcher for given pattern and regex flag
     */
    public Matcher getMatcher(String pattern, boolean regex) {

        Matcher result = regex ? (Matcher) new RegExMatcher() : (Matcher) new SimpleMatcher();

        // init
        result.init(pattern);

        // done
        return result;
    }


}
