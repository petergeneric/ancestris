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
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import genj.gedcom.Context;
import genj.util.Registry;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author daniel
 */
public abstract class StartupFiles {

    final static Logger LOG = Logger.getLogger("ancestris.app");
    final static Registry REGISTRY = Registry.get(StartupFiles.class);
    private static final String KEY = "startup.files"; // NOI18N
    //FIXME: is propertyChangeSupport necessary here?
    public static final String PROP_STARTUP_FILE_INFO = "StartupFiletInformation"; // NOI18N
    protected PropertyChangeSupport pch;
    
    private List<FileObject> commandLineFiles = new ArrayList<FileObject>(2);

    public List<FileObject> getCommandLineFiles() {
        return commandLineFiles;
    }

    public void setCommandLineFiles(List<File> commandLineFiles) {
        for (File file : commandLineFiles) {
            LOG.fine("set command files: "+file.getAbsolutePath());
            try {
                this.commandLineFiles.add(URLMapper.findFileObject(file.toURI().toURL()));
            }
            catch (MalformedURLException ex) {
                LOG.warning(ex.getMessage());
            }
        }
    }

    /**
     * Adds a FileObject to the files do be opened at startup time.
     *
     * @param gedcomFile
     */
    public abstract void add(FileObject gedcomFile);

    /**
     * Gets all Gedcoms to open at startup:
     * <li>All gedcoms opened when closing the application
     * <li>every other gedcom (eg default gedcom in preferences as
     * done in default implementation)
     *
     * @return
     */
    public abstract List<FileObject> getAll();

    /**
     * Remove all gedcoms from registry
     */
    public abstract void removeAll();

    /**
     * Remember all opened gedcom files
     */
    public void addOpenedGedcoms() {
        // Remember open gedcoms
        removeAll();
        for (Context context : GedcomDirectory.getDefault().getContexts()) {
            try {
                add(GedcomDirectory.getDefault().getDataObject(context).getPrimaryFile());
            } catch (ContextNotFoundException ex) {
                // nothing to do for none existent gedcom
            }
            // FIXME: should we put password?
//            File file = gedcom.getOrigin().getFile();
//            if (file == null || file.exists()) {
//                StringBuffer restore = new StringBuffer();
//                restore.append(gedcom.getOrigin());
//                restore.append(",");
//                if (gedcom.hasPassword()) {
//                    restore.append(gedcom.getPassword());
//                }
//                save.add(restore.toString());
//            }
            // next gedcom
        }
    }

    public StartupFiles() {
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
    private static StartupFiles defaultInstance;

    /** Singleton instance accessor
     *
     * @return instance of history manager installed in the system
     */
    public static StartupFiles getDefault() {
        StartupFiles instance = Lookup.getDefault().lookup(StartupFiles.class);

        return (instance != null) ? instance : getDefaultInstance();
    }

    private static synchronized StartupFiles getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultStartupFilesImpl();
        }

        return defaultInstance;
    }

    private static class DefaultStartupFilesImpl extends StartupFiles {

        @Override
        public void add(FileObject gedcomFile) {
            String url;
            url = gedcomFile.toURL().toString();
            // remember
            List<String> history = REGISTRY.get(KEY, new ArrayList<String>());
            history.remove(url);
            history.add(0, url);
            REGISTRY.put(KEY, history);
            pch.firePropertyChange(new PropertyChangeEvent(StartupFiles.class,
                    PROP_STARTUP_FILE_INFO, null, null));
        }

        /**
         * add default gedcom (always or only in no file is to be opened)
         */
        @Override
        public List<FileObject> getAll() {
            List<String> list = REGISTRY.get(KEY, new ArrayList<String>());
            List<FileObject> result = new ArrayList<FileObject>(5);
            if (!ancestris.core.CoreOptions.getInstance().getOpenNothingAtStartup()) {
                for (String file : list) {
                    try {
                        result.add(URLMapper.findFileObject(new URL(file)));
                    } catch (MalformedURLException ex) {
                    }
                }
                // ne pas ouvrir si onlyempty est positionne
                if (result == null || result.isEmpty() || ancestris.core.CoreOptions.getInstance().getAlwaysOpenDefault()) {
                    URL defaultURL = ancestris.core.CoreOptions.getInstance().getDefaultGedcom();
                    if (defaultURL != null) {
                        FileObject defaultGedcom = URLMapper.findFileObject(defaultURL);
                        if (defaultGedcom != null && !result.contains(defaultGedcom)) {
                            result.add(defaultGedcom);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void removeAll() {
            REGISTRY.put(KEY, (List<String>) null);
        }
    }
}
