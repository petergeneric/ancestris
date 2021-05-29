/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2010 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author daniel
 */
public abstract class RegistryStorage implements IRegistryStorage {

    private final static Logger LOG = Logger.getLogger("ancestris.util");
    String prefix = "";

    public static IRegistryStorage get(Class cls) {
        return new Preferences(cls);
    }

    public static IRegistryStorage get(String pckg) {
        return new Preferences(pckg);
    }

    // FIXME: DAN 20101230: not used shall we remove it?
    protected static class Properties extends RegistryStorage {

        java.util.Properties properties = null;
        private File file = null;

        protected Properties(File file) {
            properties = new SortingProperties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                properties.load(in);
            } catch (Exception ex) {
            } finally {
                try {
                    in.close();
                } catch (Throwable t) {
                }
                this.file = file;
            }
        }

        protected Properties(InputStream in) {
            properties = new SortingProperties();
            try {
                properties.load(in);
            } catch (Exception ex) {
            }
        }

        public void persist() {
            try {
                LOG.fine("Storing registry in file " + file.getAbsolutePath());
                File dir = file.getParentFile();
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("dir is bad " + dir);
                }

                FileOutputStream out = new FileOutputStream(file);
                //properties.store(out, prefix);
                out.flush();
                out.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Can't store registry in file " + file.getAbsolutePath(), ex);
            }
        }

        public void remove(String prefix) {
            List<Object> keys = new ArrayList<Object>(properties.keySet());
            for (int i = 0, j = keys.size(); i < j; i++) {
                String key = (String) keys.get(i);
                if (key.startsWith(prefix)) {
                    properties.remove(key);
                }
            }
        }

        /**
         * Returns all preperties
         */
        public java.util.Properties getProperties() {
            return properties;
        }

        /**
         * Returns String parameter to key
         */
        public String get(String key, String def) {
            // prepend prefix
            if (prefix.length() > 0) {
                key = prefix + "." + key;
            }

            // Get property by key
            String result = (String) properties.get(key);

            // verify it exists
            if (result == null) {
                return def;
            }

            // Done
            return result;
        }

        /**
         * Remembers a String value
         */
        public void put(String key, String value) {
            // prepend prefix
            if (prefix.length() > 0) {
                key = prefix + "." + key;
            }

            if (value == null) {
                properties.remove(key);
            } else {
                properties.put(key, value);
            }
        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            // Nothing for properties
        }
    }

    private static class SortingProperties extends java.util.Properties {

        @SuppressWarnings("unchecked")
        @Override
        public synchronized Enumeration<Object> keys() {
            Vector result = new Vector(super.keySet());
            Collections.sort(result);
            return result.elements();
        }
    };

    protected static class Preferences extends RegistryStorage {

        private java.util.prefs.Preferences preferences = null;

        protected Preferences(Class cls) {
            this(cls,cls.getPackage().getName().replace('.', '-'));
        }
        protected Preferences(Class cls, String fileName){
            preferences = NbPreferences.forModule(cls);
            if (fileName != null && !fileName.isEmpty())
                preferences = preferences.node(fileName);
        }

        protected Preferences(String path) {
            preferences = NbPreferences.root().node(path);
        }

        /**
         * Returns all preperties
         */
        public Set<String> getProperties() {
            Set<String> ret = new HashSet<>();
            try {
                String[] array = preferences.keys();
                Collections.addAll(ret, array);
            } catch (BackingStoreException ex) {
            }
            return ret;
        }


        public void remove(String key) { // remove all keys starting with "key" or "key."
         // prepend prefix
            if (prefix.length() > 0) {
                key = prefix + "." + key;
            }
            preferences.remove(key);
            key += ".";
            try {
                String[] keys = preferences.keys();
                for (String k:keys) {
                    if (k.startsWith(key)) {
                       preferences.remove(k);
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        /**
         * Returns String parameter to key
         */
        public String get(String key, String def) {

            // prepend prefix
            if (prefix.length() > 0) {
                key = prefix + "." + key;
            }

            return preferences.get(key, def);
        }

        /**
         * Remembers a String value
         */
        public void put(String key, String value) {

            // prepend prefix
            if (prefix.length() > 0) {
                key = prefix + "." + key;
            }

            if (value == null) {
                preferences.remove(key);
            } else {
                preferences.put(key, value);
            }
        }

        public void persist() {
            try {
                preferences.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            preferences.addPreferenceChangeListener(pcl);
        }
    }
}
