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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Wrapper around ResourcesBundle for old style Resources
 */
public class Resources {

    private static final Logger LOG = Logger.getLogger("ancestris.util");
    /** bundle objet to delegate if not a report resource */
    private ResourceBundle bundle = null;
    private String description = "";

    /**
     * Accessor (cached)
     */
    // XXX: must be cached or use NbBundle equivalent code for getstring or NbBundle wrapper as sued elsewhere
    public static Resources get(Object packgeMember) {
        return get(packgeMember,(Locale)null);
    }
    public static Resources get(Object packgeMember, Locale locale) {
        if (packgeMember instanceof ResourcesProvider)
            return ((ResourcesProvider)packgeMember).getResources(locale);
        Class<?> clazz = packgeMember instanceof Class<?> ? (Class<?>) packgeMember : packgeMember.getClass();
        return get(clazz, locale);
    }
    public static Resources get(Class<?> clazz) {
        return get(clazz,null);
    }
    public static Resources get(Class<?> clazz, Locale locale) {
        try {
            ResourceBundle bundle;

            if (locale == null)
                 bundle = NbBundle.getBundle(clazz);
            else
                bundle = NbBundle.getBundle(findName(clazz), locale);
            Resources result = new Resources(bundle);
            result.description = "" + clazz;
            return result;
        } catch (MissingResourceException e) {
            LOG.log(Level.WARNING, "resources file is no longer supported for class {0}: use Bundles.properties", clazz);
            return new Resources();
        }
    }

    /**
     * from NbBundle package
     */
        /** Finds package name for given class */
    private static String findName(Class clazz) {
        String pref = clazz.getName();
        int last = pref.lastIndexOf('.');

        if (last >= 0) {
            pref = pref.substring(0, last + 1);

            return pref + "Bundle"; // NOI18N
        } else {
            // base package, search for bundle
            return "Bundle"; // NOI18N
        }
    }

    public Resources(){
        bundle = null;
        description = "";
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
        String formatStr = getString(key).replace("''", "'").replace("'", "''");
        return MessageFormat.format(formatStr, substitutes);
    }
    public interface ResourcesProvider {
        //XXX: getResources is equivalent to getResources(null). Should we remove this API?
        public Resources getResources();
        public Resources getResources(Locale locale);
    }
} //Resources

