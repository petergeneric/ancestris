/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Test for dupes in information about being biological child
 */
public class TestBiologicalChild extends Test {

  /**
   * Constructor
   */
  /*package*/ TestBiologicalChild() {
    // delegate to super
    super("INDI", Indi.class);
  }
  
  /**
   * Test individual(s)'s being-child associations  
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {

    // loop over all famc
    List famcs = prop.getProperties(PropertyFamilyChild.class);
    for (ListIterator it = famcs.listIterator(); it.hasNext(); ) {
      PropertyFamilyChild famc = (PropertyFamilyChild)it.next();
      if (famc.isValid() && Boolean.FALSE.equals(famc.isBiological()))
        it.remove();
    }
    
    // more than one?
    if (famcs.size()>1) for (Iterator it = famcs.iterator(); it.hasNext() ;) {
      issues.add(new ViewContext((Property)it.next()).setText(report.translate("warn.famc.biological")));
    }
    
    // done
  }
  

} //TestBiologicalChild