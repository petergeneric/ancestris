/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.extensions.reports.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;

import java.util.List;

/**
 * A test for validation
 * @author nmeier
 */
@SuppressWarnings("unchecked")
abstract class Test {

    private TagPath[] pathTriggers;
    private Class typeTrigger;

    /**
     * Constructor
     */
    Test(String pathTrigger, Class typeTrigger) {
        this(new String[]{pathTrigger}, typeTrigger);
    }

    /**
     * Constructor
     */
    Test(String[] pathTriggers, Class typeTrigger) {
        if (pathTriggers != null) {
            this.pathTriggers = new TagPath[pathTriggers.length];
            for (int i = 0; i < pathTriggers.length; i++) {
                this.pathTriggers[i] = new TagPath(pathTriggers[i]);
            }
        }
        this.typeTrigger = typeTrigger;
    }

    /**
     * Test whether test applies or not
     */
    boolean applies(Property prop, TagPath path) {
        // gotta match a path
        outer:
        while (pathTriggers != null) {
            for (int j = 0; j < pathTriggers.length; j++) {
                if (pathTriggers[j].equals(path)) {
                    break outer;
                }
            }
            return false;
        }
        // and type
        return typeTrigger == null || typeTrigger.isAssignableFrom(prop.getClass());
    }

    /**
     * Perform Test on prop&path - fill issues with instances of Issue
     */
    abstract void test(Property prop, TagPath path, List issues, GedcomValidate report);
} //Test

