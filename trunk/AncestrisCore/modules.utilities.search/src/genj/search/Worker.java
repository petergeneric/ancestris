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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background search worker
 */
/*package*/ abstract class Worker {

    /**
     * max # hits
     */
    public final static int MAX_HITS = 5000;

    /**
     * one listener
     */
    public WorkerListener listener;

    /**
     * current search state
     */
    public Gedcom gedcom;
    public Set<Entity> entities = new HashSet<Entity>();
    public List<Hit> hits = new ArrayList<Hit>(MAX_HITS);
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

    public abstract void start(Gedcom gedcom, Object... args);
    
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
        for (int t = 0; t < Gedcom.ENTITIES.length && hitCount < MAX_HITS; t++) {
            for (Entity entity : gedcom.getEntities(Gedcom.ENTITIES[t])) {

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
