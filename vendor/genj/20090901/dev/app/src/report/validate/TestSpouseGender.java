/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.List;

/**
 * @author nmeier
 */
/*package*/ class TestSpouseGender extends Test {

  /**
   * Constructor
   */
  /*package*/ TestSpouseGender() {
    super(new String[]{"FAM"}, Property.class);
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    // assuming family
    Fam fam = (Fam)prop;
    
    // check husband/wife
    Indi husband = fam.getHusband();
    if (!testSex(husband, PropertySex.MALE))
      addIssue(issues, fam, "HUSB", report); 
      
    Indi wife = fam.getWife();
    if (!testSex(wife, PropertySex.FEMALE)) 
      addIssue(issues, fam, "WIFE", report); 

    // done
  }    

  /**
   * Test an individual's sex
   */
  private boolean testSex(Indi indi, int sex) {
    return indi==null ? true : indi.getSex()==sex;
  }

  /**
   * Calculate an issue for indi in fam 
   * @param role HUSB or WIFE
   */
  private void addIssue(List issues, Fam fam, String role, ReportValidate report) {
    
    PropertyXRef xref = (PropertyXRef)fam.getProperty(role);
    Indi indi = (Indi)xref.getTargetEntity();
     
    String[] format = new String[] {
      Gedcom.getName(role),
      indi.toString()
    };
    
    issues.add(new ViewContext(xref).setText(report.translate("err.spouse."+role, format)));
  }

} //TestHusbandGender