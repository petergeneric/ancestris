/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;

/**
 * A test for validation
 *
 * @author nmeier
 */
@SuppressWarnings("unchecked")
abstract class Test {

    private TagPath[] pathTriggers;
    private Class<? extends Object> typeTrigger;

    /**
     * Constructor
     */
    Test(String pathTrigger, Class<? extends Object> typeTrigger) {
        this(new String[]{pathTrigger}, typeTrigger);
    }

    /**
     * Constructor
     */
    Test(String[] pathTriggers, Class<? extends Object> typeTrigger) {
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
            for (TagPath pathTrigger : pathTriggers) {
                if (pathTrigger.equals(path)) {
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
    abstract void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report);

    abstract String getCode();
    
} //Test

