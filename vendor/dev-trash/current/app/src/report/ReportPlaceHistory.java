/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.chart.Chart;
import genj.chart.XYSeries;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.util.DirectAccessTokenizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Show a chart of places over time
 */
public class ReportPlaceHistory extends Report {

  private final static String
  	PLAC = "PLAC",
  	DATE = "DATE";

  /** top n */
  private int topn = 10;

  /** resolution */
  private int resolution = 10;

  /**
   * Accessor - top n
   */
  public int getTopn() {
    return topn;
  }

  /**
   * Accessor - top n
   */
  public void setTopn(int set) {
    topn = Math.max(0, Math.max(3, set));
  }

  /**
   * no need for STDOUT
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Report's main
   */
  public void start(Gedcom gedcom) {

    // check for place format / jurisdiction to look at
    int jurisdiction = 0;
    String format  = gedcom.getPlaceFormat();
    if (format.length()>0) {
      String [] jurisdictions = new DirectAccessTokenizer(format,PropertyPlace.JURISDICTION_SEPARATOR).getTokens(true);
      if (jurisdictions.length>0) {
        Object choice = getValueFromUser(translate("jurisdiction"), jurisdictions, jurisdictions[0]);
        if (choice==null) return;
        for (int i=0; i<jurisdictions.length; i++) if (jurisdictions[i]==choice) jurisdiction = i;
      }
    }

    // find series for the top n places
    Map plac2series = getSeriesForPlaces(gedcom, jurisdiction);

    // loop over individuals
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      analyze((Indi)indis.next(), plac2series, jurisdiction);
    }

    // loop over families
    Iterator fams = gedcom.getEntities(Gedcom.FAM).iterator();
    while (fams.hasNext()) {
      analyze((Fam)fams.next(), plac2series, jurisdiction);
    }

    // get the series array
    XYSeries[] series = new XYSeries[plac2series.size()];
    plac2series.values().toArray(series);

    // show it
    String title = translate("title", gedcom.getName());
    String xaxis = translate("xaxis", resolution);
    String yaxis = translate("yaxis");
    Chart chart = new Chart(title, xaxis, yaxis, series, null, true);
    showChartToUser(chart);

    // done
  }

  /**
   * Analyze indi or fam
   */
  private void analyze(Entity ent, Map plac2series, int jurisdiction) {

    // look into entity's PropertyChoiceValues
    Iterator it = ent.getProperties(PropertyPlace.class).iterator();
    while (it.hasNext()) {
      // check whether we have a series for that PLACe
      PropertyPlace place = (PropertyPlace)it.next();
      XYSeries series = (XYSeries)plac2series.get(place.getJurisdiction(jurisdiction));
      if (series==null)
        continue;
      // look for a date we could use
      Property parent = place.getParent();
      Property date = parent.getProperty(DATE);
      if (!(date instanceof PropertyDate))
        continue;
      // try to add it to series
      try {
	      int year = ((PropertyDate)date).getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	      if (year!=PointInTime.UNKNOWN) {
	        series.inc( year/resolution*resolution );
            System.out.println(year);
          }
      } catch (GedcomException e) {
        // ignore dates where we can't get a GREGORIAN time
      }
    }

    // done
  }

  /**
   * Find the top n places and create series for them
   */
  private Map getSeriesForPlaces(Gedcom gedcom, int jurisdiction) {

    // find what values are in gedcom for PLAC (sorted by ranking)
    String[] jurisdictions = PropertyPlace.getAllJurisdictions(gedcom, jurisdiction, false);

    // create series for the top n
    Map result = new HashMap();
    for (int s=jurisdictions.length-1, i=0; s>=0&&i<topn; s--,i++) {
      result.put(jurisdictions[s], new XYSeries(jurisdictions[s]));
    }

    // done
    return result;
  }

} //ReportPlaceHistory
