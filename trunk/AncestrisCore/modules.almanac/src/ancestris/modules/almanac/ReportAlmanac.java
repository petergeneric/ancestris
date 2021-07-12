package ancestris.modules.almanac;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
import genj.almanac.Almanac;
import genj.almanac.Event;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.timeline.TimelineView;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author NMeier
 */
@ServiceProvider(service = Report.class)
public class ReportAlmanac extends Report {

    public boolean groupByYear = false;

    /**
     * main for Gedcom
     */
    @SuppressWarnings("unchecked")
    public void start(Gedcom gedcom) {
        report(gedcom, (Collection<Indi>) gedcom.getEntities(Gedcom.INDI));
    }

    /**
     * main for Indi
     */
    public void start(Indi indi) {
        report(indi.getGedcom(), Collections.singletonList(indi));
    }

    /**
     * main for Indis
     */
    public void start(Indi[] indis) {
        report(indis[0].getGedcom(), Arrays.asList(indis));
    }

    /**
     * main for dates
     */
    public void start(PropertyDate[] dates) {
        // collect 'lifespan'
        PointInTime from = new PointInTime(),
                to = new PointInTime();

        for (PropertyDate date : dates) {
            getTimespan(date, from, to);
        }

        if (!from.isValid() || !to.isValid()) {
            return;
        }

        // report it
        report(getAlmanac().getEvents(from, to, getAlmanacList(), getAlmanacCategories(), getSigLevel()));

    }

    /**
     * Report events for list of individuals
     */
    private void report(Gedcom ged, Collection<Indi> indis) {

        Iterator<Event> events = getEvents(ged, indis);
        if (events == null) {
            println(translate("norange", indis.size()));
            return;
        }

        report(events);

    }

    private void report(Iterator<Event> events) {

        int year = -Integer.MAX_VALUE;
        int num = 0;
        while (events.hasNext()) {

            Event event = events.next();

            if (groupByYear) {
                int y = event.getTime().getYear();
                if (y > year) {
                    year = y;
                    println(translate("year", "" + year));
                }
            }
            println(" + " + event);
            num++;
        }
        println("\n");
        println(translate("found", num));
        println("           -:-:-:-:-:-:-:-:-:-");

        // done
    }

    /**
     * Lookup almanac events for the given individuals
     */
    private Iterator<Event> getEvents(Gedcom gedcom, Collection<Indi> indis) {

        // collect 'lifespan'
        PointInTime from = new PointInTime(),
                to = new PointInTime();

        for (Indi indi : indis) {
            getLifespan(indi, from, to);
        }

        // got something?
        if (!from.isValid() || !to.isValid()) {
            return null;
        }

        println("--------------------------------------------------------");
        println(translate("header", new Object[]{gedcom, from, to}));
        println("--------------------------------------------------------");

        return getAlmanac().getEvents(from, to, getAlmanacList(), getAlmanacCategories(), getSigLevel());
    }

    /**
     * Get start end of indi
     */
    private void getLifespan(Indi indi, PointInTime from, PointInTime to) {

        // look at his events to find start and end
        List<? extends Property> events = indi.getProperties(PropertyEvent.class);
        for (int e = 0; e < events.size(); e++) {
            Property event = (Property) events.get(e);
            PropertyDate date = (PropertyDate) event.getProperty("DATE");
            getTimespan(date, from, to);
        }

        // done
    }

    private void getTimespan(PropertyDate date, PointInTime from, PointInTime to) {
        if (date == null || !date.isValid()) {
            return;
        }
        try {
            PointInTime start = date.getStart().getPointInTime(PointInTime.GREGORIAN),
                    end = date.isRange() ? date.getEnd().getPointInTime(PointInTime.GREGORIAN) : start;
            if (!from.isValid() || from.compareTo(start) > 0) {
                from.set(start);
            }
            if (!to.isValid() || to.compareTo(end) < 0) {
                to.set(end);
            }
        } catch (GedcomException ge) {
            // ignored
        }
    }

    /**
     * Get initialized almanac
     */
    private Almanac getAlmanac() {
        Almanac almanac = Almanac.getInstance();
        almanac.waitLoaded();
        return almanac;
    }

    public List<String> getAlmanacList() {
        List<String> result = new ArrayList<>(Almanac.getInstance().getAlmanacs());
        String[] ignoredNames = Registry.get(TimelineView.class).get("almanac.ignorenames", new String[0]);
        List<String> ignoredAlmanacsList = new ArrayList<>();
        ignoredAlmanacsList.addAll(Arrays.asList(ignoredNames));
        result.removeAll(ignoredAlmanacsList);
        return result;
    }

    public List<String> getAlmanacCategories() {
        List<String> result = new ArrayList<>(Almanac.getInstance().getCategories());
        String[] ignored = Registry.get(TimelineView.class).get("almanac.ignore", new String[0]);
        List<String> ignoredAlmanacCategories = new ArrayList<>();
        ignoredAlmanacCategories.addAll(Arrays.asList(ignored));
        result.removeAll(ignoredAlmanacCategories);
        return result;
    }

    private int getSigLevel() {
        return Registry.get(TimelineView.class).get("almanac.siglevel", 0);
    }

} //ReportAlmanac
