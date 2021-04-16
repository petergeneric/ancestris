/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import org.jxmapviewer.viewer.DefaultWaypoint;

/**
 *
 * @author frederic
 */
public class STPoint extends DefaultWaypoint {

    private int type = 0;  // defines color : 0 is Main Gedcom, 1 is the other one
    private int time = 0;  // defines size

    public STPoint(int type, double lat, double lon, int time) {
        super(lat, lon);
        this.type = type;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public int getTime() {
        return time;
    }
}
