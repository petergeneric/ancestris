/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright Ancestris
 *
 * Author: Frederic Lapeyre (frederic-at-ancestris-dot-org). 2006-2016
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org). 2012
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import java.math.BigInteger;
import java.util.Comparator;

/**
 *
 * @author Zurga
 */
class SosaComparator implements Comparator<Pair> {

    @Override
    public int compare(Pair p1, Pair p2) {
        BigInteger bi1 = new BigInteger(p1.getValue());
        BigInteger bi2 = new BigInteger(p2.getValue());
        return bi1.compareTo(bi2);
    }

}
