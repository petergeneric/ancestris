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

package ancestris.gedcom;

import org.openide.util.Lookup;

/**
 *
 * @author daniel
 */
public abstract class GedcomDirectoryLookup {

    /** Instance of dummy window manager. */
    private static GedcomDirectoryLookup dummyInstance;

    /** Singleton instance accessor method for gedcom directory.
     *
     * @return instance of gedcom directory installed in the system
     */
    public static final GedcomDirectoryLookup getDefault() {
        GedcomDirectoryLookup gdInstance = Lookup.getDefault().lookup(GedcomDirectoryLookup.class);

        return (gdInstance != null) ? gdInstance : getDummyInstance();
    }

    private static synchronized GedcomDirectoryLookup getDummyInstance() {
        if (dummyInstance == null) {
            dummyInstance = new DummyGedcomDirectory();
        }

        return dummyInstance;
    }

    private static final class DummyGedcomDirectory extends GedcomDirectoryLookup {
    }
}
