/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * A report that shows pie charts with events by months
 */
public class ReportEventsByMonths extends Report {

  /** whether we give a Chart for births - default is true */
  public boolean BirthsChart = true;
  /** whether we give a Chart for baptisms - default is false */
  public boolean BaptismsChart = false;
  /** whether we give a Chart for adoptions - default is false  */
  public boolean AdoptionsChart = false;
  /** whether we give a Chart for marriages - default is true */
  public boolean MarriagesChart = true;
  /** whether we give a Chart for divorces - default is true */
  public boolean DivorcesChart = true;
  /** whether we give a Chart for deaths - default is true */
  public boolean DeathsChart = true;
  /** the inferior date of the period to take into account*/
  public int inferiorYearLimit = 0;
  /** the superior date of the period to take into account*/
  public int superiorYearLimit = 2100;
  
  /** calendar we use */
  private int calendar;

  /** calendars */
  public final static Calendar[] CALENDARS = {
    PointInTime.GREGORIAN,
    PointInTime.FRENCHR,
    PointInTime.JULIAN,
    PointInTime.HEBREW
  };

  /** accessor - calendar */
  public int getCalendar() {
    return calendar;
  }

  /** accessor - calendar */
  public void setCalendar(int set) {
    calendar = Math.max(0, Math.min(CALENDARS.length-1, set));
  }

  /** accessor - calendars */
  public Calendar[] getCalendars() {
    return CALENDARS;
  }

  /**
   * No STDOUT necessary
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Report's main
   */
  public void start(Gedcom gedcom) {
	  Map<String, String> labels = new HashMap<String, String>();
	  
    // look for events we consider
    List<IndexedSeries> series = new ArrayList<IndexedSeries>();
    if (BirthsChart) {
    series.add(analyze(gedcom.getEntities("INDI"), "BIRT"));
    labels.put("BIRT",translate("birt"));
    }
    if (BaptismsChart) {
    series.add(analyze(gedcom.getEntities("INDI"), "BAPM"));
    labels.put("BAPM",translate("bapm"));
    }
    if (AdoptionsChart) {
    series.add(analyze(gedcom.getEntities("INDI"), "ADOP"));
    labels.put("ADOP",translate("adop"));
    }
    if (MarriagesChart) {
    series.add(analyze(gedcom.getEntities("FAM" ), "MARR"));
    labels.put("MARR",translate("marr"));
    }
    if (DivorcesChart) {
    series.add(analyze(gedcom.getEntities("FAM" ), "DIV"));
    labels.put("DIV",translate("div"));
    }
    if (DeathsChart) {
    series.add(analyze(gedcom.getEntities("INDI"), "DEAT"));
    labels.put("DEAT",translate("deat"));
    }

    // show it in a chart per series
    String[] categories = CALENDARS[calendar].getMonths(true);

    JTabbedPane charts = new JTabbedPane();
    for (Iterator<IndexedSeries> it=series.iterator(); it.hasNext(); ) {
      IndexedSeries is = (IndexedSeries)it.next();
      //String label = Gedcom.getName(is.getName());
      String label = (String)labels.get(is.getName());
      // the chart title
      String[] chartTitleParameters = {label, new Integer(inferiorYearLimit).toString(),new Integer(superiorYearLimit).toString()};
      Chart chart = new Chart(translate("chart.title",chartTitleParameters), is, categories, false);
      
      charts.addTab(label, chart);
    }
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, charts);

    showComponentToUser(panel);

    // done
  }

  private IndexedSeries analyze(Collection entities, String tag) {

    int months = CALENDARS[calendar].getMonths(true).length;

    IndexedSeries series = new IndexedSeries(tag, months);

    // loop over entities
    Iterator it = entities.iterator();
    while (it.hasNext()) {

      Entity e = (Entity)it.next();

      // check it out
      Property event = e.getProperty(series.getName());
      if (!(event instanceof PropertyEvent))
        continue;
      PropertyDate date = ((PropertyEvent)event).getDate();
      if (date==null)
        continue;

      // inc appropriate month if the year belongs to the authorized period
      try {
    	  if(date.getStart().getPointInTime(CALENDARS[calendar]).getYear()<inferiorYearLimit || 
    			  date.getStart().getPointInTime(CALENDARS[calendar]).getYear()>superiorYearLimit){
    		 
    	  }
    	  else{
    		  series.inc(date.getStart().getPointInTime(CALENDARS[calendar]).getMonth());
    	  }
    	  
        
      } catch (Throwable t) {
      }

      // next
    }

    // done
    return series;
  }

} //ReportBirthMonths
