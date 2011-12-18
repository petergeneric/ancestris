/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test for property validity
 * @author nmeier
 */
@SuppressWarnings("unchecked")
/*package*/ class TestValid extends Test {

  /** the report */
  private GedcomValidate report;
  
  /**
   * Constructor
   */
  /*package*/ TestValid(GedcomValidate report) {
    super((String[])null, Property.class);
    this.report = report;
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, List issues, GedcomValidate report) {
    
    // always an issue with private
    if (!report.isPrivateValueValid&&prop.isPrivate()) {
      // got an issue with that
      issues.add(new ViewContext(prop).setText(NbBundle.getMessage(this.getClass(), "err.private", path.toString())));
    }

    // no issue if valid 
    if (prop.isValid())
      return;
      
    // no issue if isEmptyValid&&getValue() is empty
    if (report.isEmptyValueValid&&prop.getValue().length()==0)
      return;
      
    // got an issue with that
    issues.add(new ViewContext(prop).setText(NbBundle.getMessage(this.getClass(),"err.notvalid", path.toString())));
    
    // done
  }

} //TestValid