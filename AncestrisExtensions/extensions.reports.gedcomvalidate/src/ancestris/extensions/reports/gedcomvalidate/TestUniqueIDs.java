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
import genj.view.ViewContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * Test whether properties adhere to their singleton status
 */
@SuppressWarnings("unchecked")
public class TestUniqueIDs extends Test {
  
  private final static String[] PATHS = { "INDI:RIN" };
  
  private Map path2id2first = new HashMap();

  /**
   * Constructor
   */
  public TestUniqueIDs() {
    super(PATHS, Property.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, List issues, GedcomValidate report) {
    
    // need path mapping
    Map id2first = (Map)path2id2first.get(path);
    if (id2first==null) {
      id2first = new HashMap();
      path2id2first.put(path, id2first);
    }
    
    // not known yet?
    String value =prop.getValue();
    if (!id2first.containsKey(value)) {
      id2first.put(value, prop);
      return;
    }
    
    // mark first as dupe
    Property first = (Property)id2first.get(value);
    if (first!=null) {
      issues.add(new ViewContext(first).setText(NbBundle.getMessage(this.getClass(),"err.notuniqueid",first.getTag(), first.getValue() )));
      id2first.put(value, null);
    }
    
    // mark duplicates
    issues.add(new ViewContext(prop).setText(NbBundle.getMessage(this.getClass(),"err.notuniqueid", prop.getTag(), prop.getValue() )));
    
    
    // done
  }

} //TestFiles