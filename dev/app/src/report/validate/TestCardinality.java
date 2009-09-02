/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test whether properties adhere to their cardinalities
 */
public class TestCardinality extends Test {

  /**
   * Constructor
   */
  public TestCardinality() {
    super((String[])null, Property.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    MetaProperty itsmeta = prop.getMetaProperty();

    // check children that occur more than once
    Map seen = new HashMap();
    for (int i=0,j=prop.getNoOfProperties(); i<j ; i++) {
      Property child = prop.getProperty(i);
      String tag = child.getTag();
      MetaProperty meta = itsmeta.getNested(tag, false); 
      if (meta.isSingleton()) {
        if (!seen.containsKey(tag))
          seen.put(tag, child);
        else {
          Property first = (Property)seen.get(tag);
          if (first!=null) {
            seen.put(tag, null);
            issues.add(new ViewContext(first).setText(report.translate("err.cardinality.max", new String[]{ prop.getTag(), first.getTag(), prop.getGedcom().getGrammar().getVersion(), meta.getCardinality() })));
          }
        }
      }
    }
    
    // check children that are missing
    MetaProperty[] metas = prop.getNestedMetaProperties(0);
    for (int i = 0; i < metas.length; i++) {
      if (metas[i].isRequired() && seen.get(metas[i].getTag())==null) {
        String txt = report.translate("err.cardinality.min", new String[]{ prop.getTag(), metas[i].getTag(), prop.getGedcom().getGrammar().getVersion(), metas[i].getCardinality() });
        issues.add(new ViewContext(prop).setImage(metas[i].getImage()).setText(txt));
      }
    }

    // done
  }

} //TestFiles