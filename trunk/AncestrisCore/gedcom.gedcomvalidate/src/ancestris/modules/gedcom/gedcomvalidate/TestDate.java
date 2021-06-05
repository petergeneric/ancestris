/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test two dates
 */
/*package*/ class TestDate extends Test {

  /** comparisons */
  /*package*/ final static int
    AFTER = 0,
    BEFORE = 1;

  /** path1 pointing to date to compare */
  private TagPath path1;
  
  private TagPath path1Alternate;

  /** path2 to compare to */
  private TagPath path2;
  
  private TagPath path2Alternate;

  /** the mode AFTER, BEFORE, ... */
  private int comparison;

  /**
   * Constructor
   * @see TestDate#TestDate(String[], int, String)
   */
  /*package*/ TestDate(String trigger, int comp, String path2) {
    this(new String[]{trigger}, null, null, comp, path2, null);
  }
  
  /*package*/ TestDate(String trigger, int comp, String path2, String path2Alter) {
    this(new String[]{trigger}, null, null, comp, path2, path2Alter);
  }

  /**
   * Constructor
   * @see TestDate#TestDate(String[], int, String)
   */
  /*package*/ TestDate(String trigger, String path1, int comp, String path2) {
    this(new String[]{trigger}, path1, null, comp, path2, null);
  }

  /**
   * Constructor
   * @param paths to trigger test
   * @param comp either AFTER or BEFORE
   * @param path2 path to check against (pointing to date)
   */
  /*package*/ TestDate(String[] triggers, int comp, String path2) {
    this(triggers, null, null, comp, path2, null);
  }
  
   /*package*/ TestDate(String[] triggers, int comp, String path2, String path2Alter) {
    this(triggers, null, null, comp, path2, path2Alter);
  }

  /**
   * Constructor
   * @param paths to trigger test
   * @param path1 check against path2 (can be null)
   * @param comp either AFTER or BEFORE
   * @param path2 path to check against (pointing to date)
   */
  /*package*/ TestDate(String[] triggers, String path1, String path1Alter, int comp, String path2, String path2Alter) {
    // delegate to super
    super(triggers, path1==null?PropertyDate.class:Property.class);
    // remember
    comparison = comp;
    // keep other tag path
    this.path1 = path1!=null ? new TagPath(path1) : null;
    this.path1Alternate = path1Alter!=null ? new TagPath(path1Alter) : null;
    this.path2 = new TagPath(path2);
    this.path2Alternate = path2Alter!=null ? new TagPath(path2Alter) : null;
  }

  /**
   * test a prop (PropertyDate.class) at given path
   */
  @Override
  /*package*/ void test(Property prop, TagPath trigger, List<ViewContext> issues, GedcomValidate report) {

    Entity entity = prop.getEntity();
    PropertyDate date1;

    // did we get a path1 or assuming prop instanceof date?
    if (path1!=null) {
      date1 = (PropertyDate)prop.getProperty(path1);
    } else {
      date1 = (PropertyDate)prop;
    }
    if (date1 == null && path1Alternate != null) {
        date1 = (PropertyDate)prop.getProperty(path1Alternate);
    }
    if (date1==null)
      return;

    // get date to check against - won't continue if
    // that's not a PropertyDate
    Property date2 = entity.getProperty(path2);
    if (date2 == null && path2Alternate != null) {
        date2 = entity.getProperty(path2Alternate);
    }
    if (!(date2 instanceof PropertyDate))
      return;

    // test it
    if (isError(date1, (PropertyDate)date2)) {

      WordBuffer buf = new WordBuffer();

      String event1 = Gedcom.getName(trigger.get(trigger.length()-(trigger.getLast().equals("DATE")?2:1)));
      String event2 = Gedcom.getName(path2.get(path2.length()-2));
      
      if (comparison==BEFORE)
        buf.append(NbBundle.getMessage(this.getClass(),"err.date.before", event1, date1.getDisplayValue(), event2, date2.getDisplayValue()));
      else
        buf.append(NbBundle.getMessage(this.getClass(),"err.date.after", event1, date1.getDisplayValue(), event2, date2.getDisplayValue()));
      
      entity = date2.getEntity();
      if (entity instanceof Indi)
        buf.append(NbBundle.getMessage(this.getClass(), "err.date.of", entity.toString()));

      issues.add(new ViewContext(prop).setCode(getCode()).setText(buf.toString()).setImage(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false)));
    }

    // done
  }

  /**
   * test for error in date1 vs. date2
   */
  private boolean isError(PropertyDate date1, PropertyDate date2) {

    // check valid first
    if (!(date1.isComparable()&&date2.isComparable()))
      return false;

    // depending on comparison mode
    PointInTime pit1, pit2;
    int sign;
    if (comparison==AFTER) {
      // AFTER
      pit1 = date1.getStart();
      pit2 = date2.isRange() ? date2.getEnd() : date2.getStart();
      sign = 1;
    } else {
      // BEFORE
      pit1 = date1.isRange() ? date1.getEnd() : date1.getStart();
      pit2 = date2.getStart();
      sign = -1;
    }

    // if one date is incomplete and both have the same year, we assume there is no error
    if ((!pit1.isComplete() || !pit2.isComplete()) && pit1.getYear() == pit2.getYear()) {
        return false;
    }
    
    // result
    return pit1.compareTo(pit2)*sign>0;
  }

    @Override
    String getCode() {
        return "04";
    }

} //TestEventTime