/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.List;

/**
 * Test for place formatting
 */
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
  /*package*/ void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    PropertyPlace place = (PropertyPlace)prop;
    
    // check if different hierarchy
    String hierarchy = place.getFormatAsString(); 
    if (!hierarchy.equals(globalHierarchy)) {
      issues.add(new ViewContext(place).setText(report.translate("warn.plac.format")));
    }
    
    // check if place doesn't match hierarchy 
    if (hierarchy.length()>0 && (place.getValue().length()>0 || !report.isEmptyValueValid)) {
      String[] jurisdictions = place.getJurisdictions();
      String[] format = place.getFormat();
      if (format.length!=jurisdictions.length) {
        String[] counts = new String[]{ String.valueOf(jurisdictions.length), String.valueOf(format.length) };
        issues.add(new ViewContext(place).setText(report.translate("warn.plac.value", counts)));
      }
    }

    // done
  }

}
