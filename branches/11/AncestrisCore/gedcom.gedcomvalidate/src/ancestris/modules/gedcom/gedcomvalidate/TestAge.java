/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test age of individuals at specific dates
 */
@SuppressWarnings("unchecked")
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
  @Override
  /*package*/ void test(Property prop, TagPath trigger, List<ViewContext> issues, GedcomValidate report) {

    // get to the date
    PropertyDate date ;
    if (path2date!=null) {
      date = (PropertyDate)prop.getProperty(path2date);
    } else {
      date = (PropertyDate)prop;
    }
      
    if (date==null||!date.isValid())
      return;

    // Check date against those of indis and we will compute age for each
    Property[] props = prop.getProperties(path2indi);
    if (props == null) {
        return;
    }
    final Entity mainEntity;
    if (prop instanceof PropertyXRef) {
        PropertyXRef pxref = (PropertyXRef) prop;
        mainEntity = pxref.getTargetEntity();
    } else {
        mainEntity = prop.getEntity();
    }
    
    for (Property pindi : props) {
        if (!(pindi instanceof Indi)) {
            continue;
        }
        Indi indi = (Indi) pindi;
        if (indi == mainEntity) {
            continue;
        }

        // calc pit of date
        PointInTime pit2 = date.getStart();

        // get birth
        PropertyDate birt = indi.getBirthDate();
        if (birt == null || !birt.isValid()) {
            continue;
        }
        PointInTime pit1 = birt.getStart();

        // calculate delta
        Delta delta = Delta.get(pit1, pit2);
        if (delta == null) {
            continue;
        }

        // test it 
        boolean error = isError(delta.getYears());
        if (explanation.equals("minDiffAgeSibling")) {
            int m = delta.getMonths() + (12 * delta.getYears());
            int j = delta.getDays();
            boolean datesAreComplete = pit1.isComplete() && pit2.isComplete();
            boolean likelyTwins = (m == 0) && (j < 2);
            if (report.showTwins && datesAreComplete && likelyTwins) {
                WordBuffer words = new WordBuffer();
                words.append(NbBundle.getMessage(this.getClass(), "err.twins", mainEntity.toString(), indi.toString(), String.valueOf(years)));
                issues.add(new ViewContext(mainEntity).setCode(getCode()+"-1").setText(words.toString()).setImage(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false)));
                continue;
            }
            error = isError(m) && datesAreComplete && !likelyTwins;
        }
        if (error) {
            // Builds the error context
            // Prop : property being tested (indi or fam) - make sure context is the person involed rather than the "fam" event, so use mainEntity
            // Indi : person involed
            String code = getCode() + "-";
            WordBuffer words = new WordBuffer();
            if (explanation.equals("minAgeMARR")) {
                code += "2";
                words.append(NbBundle.getMessage(this.getClass(), "err."+explanation, mainEntity.toString(), delta.getYears(), String.valueOf(years)));
            }
            if (explanation.equals("minAgeMother") || explanation.equals("maxAgeMother") || explanation.equals("minAgeFather")) {
                if (explanation.equals("minAgeMother")) {
                    code += "3";
                }
                if (explanation.equals("maxAgeMother")) {
                    code += "4";
                }
                if (explanation.equals("minAgeFather")) {
                    code += "5";
                }
                if (pit1.compareTo(pit2) < 0) {
                    words.append(NbBundle.getMessage(this.getClass(), "err."+explanation, indi.toString(), delta.getYears(), mainEntity.toString(), String.valueOf(years)));
                } else {
                    words.append(NbBundle.getMessage(this.getClass(), "err."+explanation, mainEntity.toString(), delta.getYears(), indi.toString(), String.valueOf(years)));
                }
            }
            if (explanation.equals("maxDiffAgeSibling") || explanation.equals("maxDiffAgeSpouses")) {
                if (explanation.equals("maxDiffAgeSibling")) {
                    code += "7";
                }
                if (explanation.equals("maxDiffAgeSpouses")) {
                    code += "8";
                }
                words.append(NbBundle.getMessage(this.getClass(), "err."+explanation, mainEntity.toString(), indi.toString(), delta.getYears(), String.valueOf(years)));
            }
            if (explanation.equals("minDiffAgeSibling")) {
                code += "6";
                words.append(NbBundle.getMessage(this.getClass(), "err."+explanation, mainEntity.toString(), indi.toString(), 12*delta.getYears()+delta.getMonths(), String.valueOf(years)));
            }
            words.append(", ");
            words.append(NbBundle.getMessage(this.getClass(), explanation).toLowerCase());

            issues.add(new ViewContext(mainEntity).setCode(code).setText(words.toString()).setImage(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false)));
        }
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

    @Override
    String getCode() {
        return "01";
    }

} //TestAge