/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.gedcom;

import java.util.Comparator;
import org.openide.util.Parameters;

/**
 * Comparator used in Property.compareTo.
 *
 * @author daniel
 */
public interface PropertyComparator2<P extends Property> extends Comparator<P> {

    /**
     * Returns the first key (or subkey) this comparator uses.
     * This can be used for shortcut creation or tree representation.
     *
     * @param p
     *
     * @return
     */
    public String getSortGroup(P p);

    /**
     * Default implementation for PropertyComparator2.
     * Just compare two properties by their display value.
     */
    public class Default<P extends Property> implements PropertyComparator2<P> {

        private static final Default INSTANCE = new Default();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        /**
         * Compare two Gedcom Properties by their display value.
         *
         * @param p1: cannot be null
         * @param p2: cannot be null
         *
         * @return see Comparator
         */
        @Override
        public int compare(P p1, P p2) {
            Parameters.notNull("p1", p1);
            Parameters.notNull("p2", p2);
            return p1.getGedcom().getCollator().compare(p1.getDisplayValue(), p2.getDisplayValue());
        }

        /**
         * Check p1 and p2 if null. A null value is considered lower than everything.
         * if both are null return 0, returns MAX_INT if both properties are not null.
         * The caller must check return value and if equal to MAX_VALUE perform additionnal
         * comparisons.
         *
         * @param p1
         * @param p2
         *
         * @return
         */
        int compareNull(Property p1, Property p2) {
            if (p1 == p2) {
                return 0;
            }
            if (p1 == null) {
                return -1;
            }
            if (p2 == null) {
                return 1;
            }
            return Integer.MAX_VALUE;
        }

        /**
         * The default implementation just returns the first character
         * of the display value (used in compare) or "".
         *
         * @param p
         *
         * @return
         */
        @Override
        public String getSortGroup(P p) {
            return shortcut(p.getDisplayValue(), 1);
        }

        protected String shortcut(String s, int n) {
            if (s == null) {
                return "";
            }
            s = s.trim();
            if (s.isEmpty()) {
                return "";
            }
            return s.substring(0, Math.min(n, s.length()));
        }

    }
}
