/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcomvalidate;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test for place formatting
 */
@SuppressWarnings("unchecked")
public class TestPlace extends Test {
  
  private String globalHierarchy;
  
  /** constructor */
  /*package*/ TestPlace(Gedcom gedcom) {
    super((String[])null, PropertyPlace.class);
    globalHierarchy = gedcom.getPlaceFormat();
  }

  /**
   * test place for place format
   */
  /*package*/ void test(Property prop, TagPath path, List issues, GedcomValidate report) {
    
    PropertyPlace place = (PropertyPlace)prop;
    
    // check if different hierarchy
    String hierarchy = place.getFormatAsString(); 
    if (!hierarchy.equals(globalHierarchy)) {
      issues.add(new ViewContext(place).setText(NbBundle.getMessage(this.getClass(),"warn.plac.format")));
    }
    
    // check if place doesn't match hierarchy 
    if (hierarchy.length()>0 && (place.getValue().length()>0 || !report.isEmptyValueValid)) {
      String[] jurisdictions = place.getJurisdictions();
      String[] format = place.getFormat();
      if (format.length!=jurisdictions.length) {
        issues.add(new ViewContext(place).setText(NbBundle.getMessage(this.getClass(),"warn.plac.value", String.valueOf(jurisdictions.length), String.valueOf(format.length))));
      }
    }

    // done
  }

}
