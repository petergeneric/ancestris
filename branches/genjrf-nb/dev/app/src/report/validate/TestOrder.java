/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test for dupes in information about being biological child
 */
public class TestOrder extends Test {

  private String tagToSort;
  private TagPath pathToSort;
  
  /**
   * Constructor
   */
  /*package*/ TestOrder(String trigger, String tagToSort, String pathToSortBy) {
    // delegate to super
    super(trigger, Property.class);
    this.tagToSort = tagToSort;
    this.pathToSort = new TagPath(pathToSortBy);
  }
  
  /**
   * Test properties for order
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {

    List unsorted = new ArrayList(prop.getNoOfProperties());
    for (int i=0, j=prop.getNoOfProperties(); i<j; i++) {
      Property sort = prop.getProperty(i);
      if (sort.getTag().equals(tagToSort)) {
        Property by = sort.getProperty(pathToSort);
        if (by!=null && by.isValid())
          unsorted.add(sort);
      }
    }
    
    Property[] sorted = Property.toArray(unsorted);
    Arrays.sort(sorted, new PropertyComparator(pathToSort));

    if (!Arrays.asList(sorted).equals(unsorted))
        issues.add(new ViewContext(prop).setText(report.translate("warn.order."+tagToSort)));
    
    // done
  }
  

} //TestBiologicalChild