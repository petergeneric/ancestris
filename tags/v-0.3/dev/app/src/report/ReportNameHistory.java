/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Chart names and their usage in a gedcom file
 */
public class ReportNameHistory extends Report {

  /** how to treat a lower case prefix of the last name (such as "van der")  */
  public int prefixPresentation = PropertyName.PREFIX_AS_IS;
  public String[] prefixPresentations = { 
          // TODO use enum in PropertyName (when implemented) for the string constants   
          translate("prefixAsIs"), 
          translate("prefixLast"), 
          translate("prefixIgnore") 
  };
  
  /** whether to group non-considered names */
  public boolean makeGroupOther = false;

  /** lifespan we assume if there's no death */
  private int lifespanWithoutDEAT = 80;

  /** minimum percentage of name to be considered */
  private float minUseOfName = 2;

  /** Accessor - minimum percentage of name to be considered */
  public float getMinUseOfName() {
    return minUseOfName;
  }

  /** Accessor - minimum percentage of name to be considered */
  public void setMinUseOfName(float set) {
    minUseOfName = Math.max(0, Math.min(set, 50));
  }

  /** Accessor - lifespan we assume when there's no DEAT */
  public int getLifespanWithoutDEAT() {
    return lifespanWithoutDEAT;
  }

  /** Accessor - lifespan we assume when there's no DEAT */
  public void setLifespanWithoutDEAT(int set) {
    lifespanWithoutDEAT = Math.max(20, Math.min(120, set));
  }

  /**
   * no stdout necessary for this report
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Main
   */
  public void start(Gedcom gedcom) {

    Collection indis = gedcom.getEntities(Gedcom.INDI);

    // determine range
    int
      start  = findStart(indis),
      length = PointInTime.getNow().getYear() - start + 1;

    // prepare a series of 'others'
    IndexedSeries others = new IndexedSeries("", start, length);

    // loop over individuals
    Map name2series = new TreeMap();
    Iterator iterator = indis.iterator();
    while (iterator.hasNext()) {
      Indi indi = (Indi)iterator.next();
      analyze(gedcom, indis, indi, name2series, others);
    }

    // check if got something
    if (name2series.isEmpty())
      return;

    // name the group 'other' now: "14 Other Names"
    if (makeGroupOther) {
        int numOthers = (PropertyName.getLastNames(gedcom, false).size()-name2series.size());
        if (numOthers>0) {
    	    others.setName(translate("others", numOthers));
    	    name2series.put(String.valueOf('\uffff'), others);
        }
    }

    // show it
    showChartToUser(new Chart(translate("title", gedcom.getName()), null, translate("yaxis"), IndexedSeries.toArray(name2series.values()), new DecimalFormat("#"), true));

    // done
  }

  /**
   * Find earliest year
   */
  private int findStart(Collection indis) {
    // start with year now-100
    int result = PointInTime.getNow().getYear()-100;
    // loop over indis
    Iterator it = indis.iterator();
    while (it.hasNext()) {
      Indi indi = (Indi)it.next();
      PropertyDate birth = indi.getBirthDate();
      if (birth!=null) {
        PointInTime start = birth.getStart();
        if (start.isValid()) try {
          // try to change by birth's year
          result = Math.min(result, start.getPointInTime(PointInTime.GREGORIAN).getYear());
        } catch (GedcomException e) {
        }
      }
    }
    // done
    return result;
  }

  /**
   * Analyze one individual
   */
  private void analyze(Gedcom gedcom, Collection indis, Indi indi, Map name2series, IndexedSeries others) {

    // check name
	  PropertyName name = (PropertyName)indi.getProperty("NAME");
	  if (name==null||!name.isValid())
	    return;
	  String last = name.getLastName(prefixPresentation);
	  if (last.length()==0)
	    return;

	  // calculate start
	  int start;
	  try {
	    start = indi.getBirthDate().getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	    if (start==PointInTime.UNKNOWN)
	      return;
	  } catch (Throwable t) {
	    return;
	  }

	  // calculate end
	  int end = PointInTime.UNKNOWN;
	  try {
		  end = indi.getDeathDate().getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	  } catch (Throwable t) {
	  }
    if (end==PointInTime.UNKNOWN)
	    end = start+lifespanWithoutDEAT;

	  // check minimum percentage of name
	  IndexedSeries series;
	  if (name.getLastNameCount()<indis.size()*minUseOfName/100) {
	    if (!makeGroupOther)
	      return;
	    series = others;
	  } else {
		  series = (IndexedSeries)name2series.get(last);
		  if (series==null) {
		    series = new IndexedSeries(last, others);
		    name2series.put(last, series);
		  }
	  }

	  // increase indexedseries for last-name throughout lifespan (start to end)
	  for (;start<=end;start++)
	    series.inc(start);

	  // done
	}

} //ReportNameUsage