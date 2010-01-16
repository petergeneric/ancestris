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
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;
import genj.view.ViewContext;

import java.util.List;

/**
 * Test age of individuals at specific dates
 */
public class TestAge extends Test {

  /** comparisons */
  /*package*/ final static int 
    OVER = 0,
    UNDER = 1;
    
  /** tag path to date (optional) */
  private TagPath path2date;    
    
  /** tag path to indi */
  private TagPath path2indi;    
    
  /** the mode GREATER, LESS, ... */
  private int comparison;
  
  /** the value */
  private int years;

  /** the explanation */
  private String explanation;

  /**
   * Constructor
   * @param trigger the path that triggers this test (pointing to date)
   * @param p2indi to get to indi to test for age
   * @param comp either OVER or UNDER
   * @param yrs age in years 
   */
  /*package*/ TestAge(String trigger, String p2indi, int comp, int yrs, String expltn) {
    this(trigger, null, p2indi, comp, yrs, expltn);
  }

  /**
   * Constructor
   * @param trigger the path that triggers this test
   * @param p2date path in entity to a date as basis for age calculation
   * @param p2indi path to get to indi to test for age
   * @param comp either OVER or UNDER
   * @param yrs age in years 
   */
  /*package*/ TestAge(String trigger, String p2date, String p2indi, int comp, int yrs, String expltn) {
    // delegate to super
    super(trigger, p2date!=null?Property.class:PropertyDate.class);
    // remember
    explanation = expltn;
    path2date = p2date!=null?new TagPath(p2date):null;
    path2indi = new TagPath(p2indi);
    comparison = comp;
    years = yrs;
  }
  
  /**
   * Test individual(s)'s age at given date property 
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {
    
    // get to the date
    PropertyDate date ;
    if (path2date!=null) {
      date = (PropertyDate)prop.getProperty(path2date);
    } else {
      date = (PropertyDate)prop;
    }
      
    if (date==null||!date.isValid())
      return;

    // find indi we compute age for 
    Property pindi = prop.getProperty(path2indi);
    if (!(pindi instanceof Indi))
      return;
    Indi indi = (Indi)pindi;      

    // calc pit of date
    PointInTime pit2 = date.getStart();

    // get birth
    PropertyDate birt = indi.getBirthDate();
    if (birt==null||!birt.isValid())
      return;
    PointInTime pit1 = birt.getStart();
    
    // don't test if birth<date?
    if (pit1.compareTo(pit2)>0)
      return;
    
    // calculate delta
    Delta delta = Delta.get(pit1, pit2);
    if (delta==null)
      return;
      
    // test it 
    if (isError(delta.getYears()))  {
      
      WordBuffer words = new WordBuffer();
      String[] format = new String[]{ indi.toString(), String.valueOf(years)}; 
      if (comparison==UNDER) {
        words.append(report.translate("err.age.under", format));
      } else {
        words.append(report.translate("err.age.over", format));
      }
      words.append("-");
      words.append(report.translate(explanation));

      issues.add(new ViewContext(prop).setText(words.toString()).setImage(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false)));
    }
    
    // done
  }
  
  /**
   * test
   */
  private boolean isError(int age) {
    switch (comparison) {
      case OVER:
        return age > years;
      case UNDER:
        return age < years;
    }
    return false;
  }

} //TestAge