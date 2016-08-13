/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.usage;

/**
 *
 * @author frederic
 */
public class UsageDataSet {
    
    private final String period;
    private final String value;

    /**
     * Constructor
     */
    public UsageDataSet(String period, String value) {
        this.period = period;
        this.value = value;
    }
    
}
