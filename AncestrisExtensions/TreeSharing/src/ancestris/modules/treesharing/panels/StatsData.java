/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.treesharing.panels;

import java.util.Date;

/**
 *
 * @author frederic
 */
public class StatsData {

    // Data collected for each member
    public int connections;
    public boolean match;
    public Date startDate;
    public Date endDate;
    
    
    public StatsData() {
        connections = 0;
        match = false;
        startDate = null;
        endDate = null;
    }

}
