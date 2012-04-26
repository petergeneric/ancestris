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
package ancestris.util;

/**
 *
 * @author daniel
 */
public class Utilities {

    public static String getClassName(Object o) {
        return getClassName(o.getClass());
    }

    public static String getClassName(Class c) {
        return c.getName().replace('.', '/');
    }

    /**
     * Helper to compare a string agains several words.
     * @param text
     * @param pattern
     * @return
     */
    public static boolean wordsMatch(String text, String pattern) {
        pattern = pattern.replaceAll(" +", ".+");
        return text.matches(".*" + pattern + ".*");
    }
}
