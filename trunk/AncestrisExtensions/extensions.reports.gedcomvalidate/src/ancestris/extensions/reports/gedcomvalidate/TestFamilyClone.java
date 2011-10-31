/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.extensions.reports.gedcomvalidate;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.NbBundle;

/**
 * Test for a bad merge where dupe families contain the same parents but different children
 */
@SuppressWarnings("unchecked")
public class TestFamilyClone extends Test {
  
  // keeping some state here
  private Set reportedFams = new HashSet();

  /**
   * Constructor
   */
  public TestFamilyClone() {
    super("FAM", Property.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, List issues, GedcomValidate report) {
    
    // assuming Family
    Fam fam = (Fam)prop;
    if (reportedFams.contains(fam))
      return;
    
    // check it
    Indi husband = fam.getHusband();
    Indi wife = fam.getWife();

    if (husband!=null)
      test(fam, husband.getFamiliesWhereSpouse(), issues, report);
    else if (wife!=null)
      test(fam, wife.getFamiliesWhereSpouse(), issues, report);
  }
  
  private void test(Fam fam, Fam[] others, List issues, GedcomValidate report) {

    for (int i = 0; i < others.length; i++) {
      Fam other = others[i];
      if (fam==other) continue;
      if (isClone(fam, other)) {
        if (!reportedFams.contains(fam)) {
          issues.add(new ViewContext(fam).setText(NbBundle.getMessage(this.getClass(),"warn.fam.cloned", fam.getId() )));
          reportedFams.add(fam);
        }
        issues.add(new ViewContext(other).setText(NbBundle.getMessage(this.getClass(),"warn.fam.clone", other.getId(), fam .getId() )));
        reportedFams.add(other);
      }
    }

  }
  
  private boolean isClone(Fam fam, Fam other) {
    // differenthusband or wife?
    if (fam.getHusband()!=other.getHusband())
      return false;
    if (fam.getWife()!=other.getWife())
      return false;
    // divorce in there?
    if (fam.getProperty("DIV")!=null||other.getProperty("DIV")!=null)
      return false;
    // yeah, looks like one
    return true;
  }

} //TestFiles