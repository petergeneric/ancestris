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

import genj.gedcom.Indi;

/**
 *
 * @author Zurga
 */
class Pair {

    private Indi indi;
    private String value;

    public Pair(Indi indi, String value) {
        this.indi = indi;
        this.value = value;
    }

    public Indi getIndi() {
        return indi;
    }

    public void setIndi(Indi indi) {
        this.indi = indi;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
