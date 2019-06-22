/**
 * GenJ - GenealogyJ
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
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background search worker
 */
public class WorkerTag extends Worker {

    private List<String> tags;

    public WorkerTag(WorkerListener listener) {
        super(listener);
    }

    /**
     * start search
     */
    @Override
    public void start(Gedcom gedcom, int max_hits, boolean case_sensitive, Object... args) {
        String localTags = (String) args[0];
        String value = (String) args[1];
        Boolean regexp = (Boolean) args[2];

        // sync up
        synchronized (lock) {

            // bail if already running
            if (thread != null) {
                throw new IllegalStateException("can't start while running");
            }

            // prepare matcher & path
            this.gedcom = gedcom;
            this.max_hits = max_hits;
            this.case_sensitive = case_sensitive;
            this.matcher = getMatcher(value, regexp);
            this.tags = split(localTags);
            this.hits.clear();
            this.entities.clear();
            this.hitCount = 0;

            lock.set(true);

            thread = new Thread(() -> {
                try {
                    WorkerTag.this.listener.started();
                    search(WorkerTag.this.gedcom);
                    flush();
                } catch (Throwable t) {
                    Logger.getLogger("ancestris.search").log(Level.FINE, "worker bailed", t);
                } finally {
                    synchronized (lock) {
                        thread = null;
                        lock.set(false);
                        lock.notifyAll();
                    }
                    try {
                        WorkerTag.this.listener.stopped();
                    } catch (Throwable t) {
                        // this will happen if we are being interrupted
                        // going through Spin's transition to EDT
                        // and we don't care about the result
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        // done
    }

    private List<String> split(String tags) {
        String[] ss = tags.split(",|\\s");
        ArrayList<String> result = new ArrayList<>(ss.length);
        for (String s : ss) {
            s = s.trim();
            if (s.length() > 0 && !result.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean checkPath(Entity entity, Property prop) {
        if (tags.isEmpty()) {
            return true;
        }
        // all localTags in path?
        TagPath path = prop.getPath();
        for (String tag : tags) {
            if (!path.contains(tag)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * search property (not on EDT)
     */
    @Override
    public void search(Entity entity, Property prop) {
        // parse all where path ok and not transient
        if (checkPath(entity, prop) && !prop.isTransient()) {
            // check prop's value
            search(entity, prop, prop.getDisplayValue(), false);
        }
        // check subs
        int n = prop.getNoOfProperties();
        for (int i = 0; i < n; i++) {
            search(entity, prop.getProperty(i));
        }
        // done
    }

    /**
     * search property's value
     */
    private void search(Entity entity, Property prop, String value, boolean isID) {
        // look for matches
        Matcher.Match[] matches = matcher.match(value);
        if (matches.length == 0) {
            return;
        }
        // too many?
        if (hitCount >= max_hits) {
            return;
        }
        // keep entity
        entities.add(entity);
        // create a hit
        Hit hit = new Hit(prop, value, matches, entities.size(), isID);
        // keep it
        hits.add(hit);
        hitCount++;
        // sync every 500ms
        long now = System.currentTimeMillis();
        if (now - lastFlush > 500) {
            flush();
        }
        lastFlush = now;
        // done
    }


}
