/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Wrapper around ResourcesBundle for old style Resources
 */
public class Resources {

    private static final Logger LOG = Logger.getLogger("genj.util");
    /** bundle objet to delegate if not a report resource */
    private ResourceBundle bundle = null;
    private String description = "";

    /**
     * Accessor (cached)
     */
    // XXX: must be cached or use NbBundle equivalent code for getstring or NbBundle wrapper as sued elsewhere
    public static Resources get(Object packgeMember) {
        Class<?> clazz = packgeMember instanceof Class<?> ? (Class<?>) packgeMember : packgeMember.getClass();
        try {
            Resources result = new Resources(NbBundle.getBundle(clazz));
            result.description = "" + clazz;
            return result;
        } catch (MissingResourceException e) {
            LOG.log(Level.WARNING, "resources file is non longer supported for class {0} ({1}, use Bundles.properties", new Object[]{clazz, packgeMember});
            return null;
        }
    }

    private Resources(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns a localized string
     * @param key identifies string to return
     * @param notNull will return key if resource is not defined
     */
    public String getString(String key, boolean notNull) {
        String result = null;
        if (bundle == null) {
            LOG.log(Level.FINER, "Resource with null bundle({0})", key);
        } else {
            try {
                result = bundle.getString(key);
            } catch (Exception e) {
                LOG.log(Level.FINER, "key {0} not found in {1} bundle", new Object[]{key, description});
            }
        }
        if (result == null && notNull) {
            result = key;
        }
        return result;
    }

    /**
     * Returns a localized string
     * @param key identifies string to return
     */
    public String getString(String key) {
        return getString(key, true);
    }

    /**
     * Returns a localized string
     * @param key identifies string to return
     * @param values array of values to replace placeholders in value
     */
    public String getString(String key, Object... substitutes) {
        return MessageFormat.format(getString(key), substitutes);
    }
} //Resources

