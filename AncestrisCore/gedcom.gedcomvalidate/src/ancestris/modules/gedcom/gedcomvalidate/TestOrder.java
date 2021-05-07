/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test for dupes in information about being biological child
 */
@SuppressWarnings("unchecked")
public class TestOrder extends Test {

    private final String tagToSort;
    private final TagPath pathToSort;

    /**
     * Constructor
     */
    TestOrder(String trigger, String tagToSort, String pathToSortBy) {
        // delegate to super
        super(trigger, Property.class);
        this.tagToSort = tagToSort;
        this.pathToSort = new TagPath(pathToSortBy);
    }

    /**
     * Test properties for order
     */
    @Override
    void test(Property prop, TagPath trigger, List<ViewContext> issues, GedcomValidate report) {
        List<Property> unsorted = new ArrayList<>(prop.getNoOfProperties());
        for (Property sort : prop.getProperties()) {
            if (sort.getTag().equals(tagToSort)) {
                Property by = sort.getProperty(pathToSort);
                if (by != null && by.isValid()) {
                    unsorted.add(sort);
                }
            }
        }

        Property[] sorted = Property.toArray(unsorted);
        Arrays.sort(sorted, new PropertyComparator(pathToSort));

        if (!Arrays.asList(sorted).equals(unsorted)) {
            issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "warn.order." + tagToSort)));
        }

        // done
    }

    @Override
    String getCode() {
        return "09";
    }
} //TestBiologicalChild
