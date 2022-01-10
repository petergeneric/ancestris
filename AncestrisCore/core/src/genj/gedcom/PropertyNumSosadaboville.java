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
package genj.gedcom;

import ancestris.util.ComparableList;

/**
 *
 * @author frederic
 */
public class PropertyNumSosadaboville extends PropertyNumDaboville {

    public PropertyNumSosadaboville(String tag) {
        super(tag);
    }

    protected void calcNumbers(String value, ComparableList<Integer> array) {
        super.calcNumbers(value.split(" ")[0], array);
    }

}
