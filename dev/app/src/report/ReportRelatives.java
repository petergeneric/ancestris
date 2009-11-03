/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.AnnotationsReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A report for displaying relatives of a person
 */
public class ReportRelatives extends AnnotationsReport {

  private final static int
    UNKNOWN = PropertySex.UNKNOWN,
    MALE = PropertySex.MALE,
    FEMALE = PropertySex.FEMALE;

  /**
   * A relative
   */
  static class Relative {

    /** how to get to it */
    String key;
    String expression;
    int sex;

    /** constructor */
    Relative(String key, String expression) {
      this(key, expression, UNKNOWN);
    }

    /** constructor */
    Relative(String key, String expression, int sex) {
      this.key = key;
      this.expression = expression.trim();
      this.sex = sex;
    }

  } //Relative

  private final static Relative[] RELATIVES = {
    new Relative("farfar"     , "father+father"),
    new Relative("farmor"     , "father+mother"),
    new Relative("morfar"     , "mother+father"),
    new Relative("mormor"     , "mother+mother"),
    new Relative("father"     , "INDI:FAMC:*:..:HUSB:*"   ),
    new Relative("mother"     , "INDI:FAMC:*:..:WIFE:*"   ),
    new Relative("husband"    , "INDI:FAMS:*:..:HUSB:*"   ),
    new Relative("wife"       , "INDI:FAMS:*:..:WIFE:*"   ),
    new Relative("daughter"   , "INDI:FAMS:*:..:CHIL:*"   , FEMALE),
    new Relative("son"        , "INDI:FAMS:*:..:CHIL:*"   , MALE),
    new Relative("brother"    , "INDI:FAMC:*:..:CHIL:*"   , MALE),
    new Relative("sister"     , "INDI:FAMC:*:..:CHIL:*"   , FEMALE),

    new Relative("grandson"     , "son+son|daughter+son"          , MALE),
    new Relative("granddaughter", "son+daughter|daughter+daughter", FEMALE),

    new Relative("uncle.paternal", "father+brother|father+sister +husband"),
    new Relative("uncle.maternal", "mother+brother|mother+sister +husband"),
    new Relative( "aunt.paternal", "father+sister |father+brother+wife"   ),
    new Relative( "aunt.maternal", "mother+sister |mother+brother+wife"   ),

    new Relative("nephew.fraternal", "brother+son"),
    new Relative("niece.fraternal" , "brother+daughter"),
    new Relative("nephew.sororal"  , "sister+son"),
    new Relative("niece.sororal"   , "sister+daughter"),

    new Relative("cousin.paternal" , "uncle.paternal+son"),
    new Relative("cousin.maternal" , "uncle.maternal+son"),
    new Relative("cousine.paternal", "uncle.paternal+daughter"),
    new Relative("cousine.maternal", "uncle.maternal+daughter")
  };

  /**
   * Reports main
   */
  public void start(Indi indi) {

    setMessage(translate("title", indi));

    // prepare map of relationships
    Map key2relative = new HashMap();
    for (int i=0; i<RELATIVES.length;i++) {
      Relative relative = RELATIVES[i];
      key2relative.put(relative.key, relative);
    }

    // Loop over relative descriptions
    addAnnotation(indi);
    for (int i=0; i<RELATIVES.length; i++) {
      Relative relative = RELATIVES[i];
      List result = find(indi, relative.expression, relative.sex, key2relative);
      for (int j=0;j<result.size();j++) {
        Indi found = (Indi)result.get(j);
        String name = translate(relative.key) + ": " + found;
        addAnnotation(found, name);
      }
    }

    // done
  }

  /**
   * Find all relatives of given roots and expression
   */
  private List find(List roots, String expression, int sex, Map key2relative) {

    List result = new ArrayList();
    for (int i=0;i<roots.size();i++) {
      result.addAll(find((Property)roots.get(i), expression, sex, key2relative));
    }

    return result;

  }

  /**
   * Find all relatives of given root and expression
   */
  private List find(Property root, String expression, int sex, Map key2relative) {

    // any 'OR's?
    int or = expression.indexOf('|');
    if (or>0) {
      List result = new ArrayList();
      StringTokenizer ors = new StringTokenizer(expression, "|");
      while (ors.hasMoreTokens())
        result.addAll(find(root, ors.nextToken().trim(), sex, key2relative));
      return result;
    }

    // is relationship recursive?
    int dot = expression.indexOf('+');
    if (dot>0) {
      List roots = new ArrayList();
      roots.add(root);
      StringTokenizer cont = new StringTokenizer(expression, "+");
      while (cont.hasMoreTokens()) {
        roots = find(roots, cont.nextToken(), sex, key2relative);
      }
      return roots;
    }

    // a recursive path?
    int colon = expression.indexOf(':');
    if (colon<0) {
      Relative relative = (Relative)key2relative.get(expression.trim());
      return find(root, relative.expression, relative.sex, key2relative);
    }

    // assuming expression consists of tagpath from here
    List result = new ArrayList();
    Property[] found = root.getProperties(new TagPath(expression));
    for (int i = 0; i < found.length; i++) {
      Indi indi = (Indi)found[i].getEntity();
      if (indi!=root) {
        if (sex==UNKNOWN||indi.getSex()==sex)
          result.add(indi);
      }
    }

    // done
    return result;
  }

} //ReportRelatives
