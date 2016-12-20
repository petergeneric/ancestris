/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom;

import genj.util.Registry;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author daniel
 */
public abstract class RecentFiles {

    final static Logger LOG = Logger.getLogger("ancestris.app");
    final static Registry REGISTRY = Registry.get(RecentFiles.class);
    public static final String PROP_RECENT_FILE_INFO = "RecentFiletInformation"; // NOI18N
    protected PropertyChangeSupport pch;

    /**
     * Adds a FileObject to the top of history. If this FO is already in history remove the old
     * instance so only one instance is kept.
     *
     * @param gedcomFile
     */
    public abstract void add(FileObject gedcomFile);

    /**
     * Remove a FileObject from recent file.
     * @param gedcomFile 
     */
    public abstract void remove(FileObject gedcomFile);

    public abstract List<FileObject> getAll();

    public RecentFiles() {
            pch = new PropertyChangeSupport(this);
    }

    
    /**
     * Adds a listener, use WeakListener or properly remove listeners
     *
     * @param listener listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pch.addPropertyChangeListener(listener);
    }

    /**
     * Removes a listener
     *
     * @param listener listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pch.removePropertyChangeListener(listener);
    }
    /** Instance of default manager. */
    private static RecentFiles defaultInstance;

    /** Singleton instance accessor
     *
     * @return instance of history manager installed in the system
     */
    public static RecentFiles getDefault() {
        RecentFiles instance = Lookup.getDefault().lookup(RecentFiles.class);

        return (instance != null) ? instance : getDefaultInstance();
    }

    private static synchronized RecentFiles getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultHistoryImpl();
        }

        return defaultInstance;
    }

    private static class DefaultHistoryImpl extends RecentFiles {

        @Override
        public void add(FileObject gedcomFile) {
            if (!gedcomFile.isValid())
                return;
            String url;
            url = gedcomFile.toURL().toString();
            // remember
            List<String> history = REGISTRY.get("history", new ArrayList<String>());
            history.remove(url);
            history.add(0, url);
            if (history.size() > 5) {
                history.remove(history.size() - 1);
            }
            REGISTRY.put("history", history);
            pch.firePropertyChange(new PropertyChangeEvent(RecentFiles.class,
                    PROP_RECENT_FILE_INFO, null, null));
        }

        @Override
        public void remove(FileObject gedcomFile) {
            String url;
            try {
                url = gedcomFile.toURL().toString();
            } catch (Throwable ex) {
                return;
            }
            // remove
            List<String> history = REGISTRY.get("history", new ArrayList<String>());
            history.remove(url);
            REGISTRY.put("history", history);
            pch.firePropertyChange(new PropertyChangeEvent(RecentFiles.class,
                    PROP_RECENT_FILE_INFO, null, null));
        }
        
        

        @Override
        public List<FileObject> getAll() {
            List<String> list = REGISTRY.get("history", new ArrayList<String>());
            List<FileObject> result = new ArrayList<FileObject>(5);
            for (String file : list) {
                try {
                    FileObject fo = URLMapper.findFileObject(new URL(file));
                    // if fo is null no FileObject can be found for this file
                    // (ie file hase been deleted) so don't put in history
                    if (fo != null){
                        result.add(URLMapper.findFileObject(new URL(file)));
                    }
                } catch (MalformedURLException ex) {
                }
            }
            return result;
        }
    }
}
