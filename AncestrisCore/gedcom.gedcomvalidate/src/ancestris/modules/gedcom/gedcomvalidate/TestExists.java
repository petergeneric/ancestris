/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test for existance of properties
 */
/*package*/ class TestExists extends Test {

  /** path1 pointing to property that triggers existance check */
  private final TagPath path1;

  /** path2 to check for in case path1 exists*/
  private final TagPath path2;

  /**
   * Constructor
   */
  /*package*/ TestExists(String trigger, String path1, String path2) {
    // delegate to super
    super(trigger, null);
    // keep paths
    this.path1 = new TagPath(path1);
    this.path2 = new TagPath(path2);
  }

  /**
   * test a prop for existance
   */
  @Override
  /*package*/ void test(Property prop, TagPath trigger, List<ViewContext> issues, GedcomValidate report) {

    // check for property by path1
    Property prop1 = prop.getProperty(path1);
    if (prop1==null)
      return;
    
    // then check for property by path2
    Property prop2 = prop.getProperty(path2);
    if (prop2!=null)
      return;

    // found path1 but not path2!
    String text = NbBundle.getMessage(this.getClass(), "err.exists.without", Gedcom.getName(prop1.getTag()), Gedcom.getName(path2.getLast()) );
    issues.add(new ViewContext(prop1).setCode(getCode()).setText(text));
    
  }

    @Override
    String getCode() {
        return "06";
    }
  
} //TestEventTime