/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import java.util.ArrayList;

/**
 *
 * @author frederic
 */
public class ComparableList<T> extends ArrayList implements Comparable {

    @Override
    public int compareTo(Object other) {
        if (other.getClass() != this.getClass()) {
            return 1;
        }
        ComparableList that = (ComparableList) other;
        int length = Math.max(this.size(), that.size());
        for (int i = 0; i < length; i++) {
            if (i >= this.size() && i >= that.size()) {
                return 0;
            }
            if (i >= this.size() && i < that.size()) {
                return -1;
            }
            if (i < this.size() && i >= that.size()) {
                return +1;
            }
            Object o1 = this.get(i);
            Object o2 = that.get(i);
            int c = ((Comparable<Object>) o1).compareTo((Comparable<Object>) o2);
            if (c == 0) {
                continue;
            } else {
                return c;
            }
        }
        return 0;
    }
}
