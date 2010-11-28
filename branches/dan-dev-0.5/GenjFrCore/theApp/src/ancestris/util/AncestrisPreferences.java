/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.util;

import genj.util.IRegistryStorage;
import genj.util.IRegistryStorageFactory;
import genj.util.Registry;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author daniel
 */
public class AncestrisPreferences extends Registry {

    private AncestrisPreferences(IRegistryStorage preferences) {
        super(preferences);
    }

    public static AncestrisPreferences get(Object source) {
        return get(source.getClass());
    }

    /**
     * Accessor
     */
    public static AncestrisPreferences get(Class<?> source) {
        return new AncestrisPreferences(getStorageFactory().get(source));
    }

    public static IRegistryStorageFactory getStorageFactory() {
        if (Registry.getStorageFactory() == null) {
            setStorageFactory(RegistryStorageFactory.getFactory());
        }
        return Registry.getStorageFactory();
    }

    /**
     * Returns a collection of strings by key
     */
    public Collection get(String key, Collection def) {

        // Get size of array
        int size = get(key, -1);
        if (size == -1) {
            return def;
        }

        // Create result
        Collection result;
        if (def == null) {
            def = new HashSet();
        }

        try {
            result = (Collection) def.getClass().newInstance();
        } catch (Throwable t) {
            return def;
        }

        // Collection content
        for (int i = 0; i < size; i++) {
            result.add(get(key + "." + (i + 1), ""));
        }

        // Done
        return result;
    }
}
