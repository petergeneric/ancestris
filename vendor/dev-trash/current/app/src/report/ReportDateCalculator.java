/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import javax.swing.JLabel;
/*
 * todo:
 * - convertir en gregorien avant
 * - traiter l'evt BAPT comme BIRT
 * - mettre un champ info
 */
public class ReportDateCalculator extends Report {

	  public String accepts(Object context) {

	// accepting all PropertyChoices and PropertyNames
	String val = null;
	if (context instanceof PropertyDate) {
	    if (((Property)context).getParent().getTag().equals("BIRT")){
		return translate("xname.evt");
	    } else {
		return translate("xname.birt");
	    }
	    // return a meaningfull text for that context
	    //    return i18n("xname", new String[]{ ((Property)context).getPropertyName(), val } );
	} else {
	    return null;
	}
    }

  /**
   * We don't use STDOUT
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Our entry point
   */
  public void start(PropertyDate date) {
      // Date in gregorian format
      //      PointInTime pit = GregorianCalendar.toPointInTime(date.getStart().getCalendar().toJulianDay(date.getStart().getDay(),
      //											  date.getStart().getMonth(),
      //											  date.getStart().getYear()));
      PointInTime pit = date.getStart();
      String result;
      if (pit == null){
	  result = translate("date.undef");
      } else {
	  try {
	      pit = PointInTime.getPointInTime(pit.getTimeMillis());
	  } catch (GedcomException e) {
	  }
	  Property parent = date.getParent();
	  String ageStr;
	  if (parent.getProperty("AGE") != null){
	      ageStr = parent.getProperty("AGE").getValue();
	  } else {
	      ageStr = getValueFromUser( translate("date.title"), translate("age.title"), new String[0]);
        // check if the user cancelled this
        if (ageStr==null)
          return;

	  }

	  Delta age = new Delta(0,0,0);
	  if (age.setValue(ageStr) || age.setValue(ageStr+"y")){
	      if (date.getParent().getTag().equals("BIRT")){
		  PointInTime calcDate = getDateFromDateAndAge(pit,age,1);
		  result = translate("date.evt.label",calcDate.toString());
	      } else {
		  PointInTime calcDate = getDateFromDateAndAge(pit,age,-1);
		  result = translate("date.birth.label",calcDate.toString());
	      }
	  } else {
	      result = translate("age.invalid");
	  }
      }
      showComponentToUser(new JLabel(result));
  }

    // only for gregorian
    private static PointInTime getDateFromDateAndAge(PointInTime date, Delta age, int op) {

    // null check
    if (date==null||age==null)
      return null;

    // valid?
    if (!date.isValid())
      return null;

    // same calendar?
    Calendar calendar = date.getCalendar();
    if (calendar!=age.getCalendar())
      return null;

    // grab earlier values
    int
      y =  date.getYear (),
      m = date.getMonth(),
      d = date.getDay();

    // make sure years are not empty (could be on all UNKNOWN PIT)
    if (date.getYear()==PointInTime.UNKNOWN)
	return null;
    int year  = date.getYear() + op * age.getYears();
    int month = date.getMonth();
    int day = date.getDay();
    if (date.getMonth()!=PointInTime.UNKNOWN){
	// got the month
	month += op*age.getMonths();

	// check days
	if (day!=PointInTime.UNKNOWN){
	    // got the days
	    day += op*age.getDays();
	}
    }
    // done
    return normalize(new PointInTime(day, month, year, calendar));
  }

    private static PointInTime normalize(PointInTime pit){
	int year=pit.getYear();
	int month=pit.getMonth();
	int day=pit.getDay();
	Calendar cal = pit.getCalendar();

	if (month == PointInTime.UNKNOWN)
	    return pit;
	if (month > cal.getMonths())
	    return normalize(new PointInTime(day,month-cal.getMonths(),year+1,cal));
	if (month <0 )
	    return normalize(new PointInTime(day,month+cal.getMonths(),year-1,cal));
	if (day == PointInTime.UNKNOWN)
	    return pit;
	if (day > cal.getDays(month,year))
	    return normalize(new PointInTime(day-cal.getDays(month,year),month+1,year,cal));
	if (day <0 )
	    return normalize(new PointInTime(day+cal.getDays(month-1,year),month-1,year,cal));
	return pit;
    }


} //ReportDateCalculator