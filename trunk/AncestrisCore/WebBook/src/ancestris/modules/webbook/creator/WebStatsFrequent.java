/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;

import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Comparator;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebStatsFrequent extends WebSection {

    private String here2indiDir = "";
    private String here2cityDir = "";
    private String here2dayDir = "";
    private final static int TYPE_LASTNAME = 0,
            TYPE_LOCATION = 1,
            TYPE_DATE = 2;

    private class Info {

        String key;
        int frequency;
    }

    private List<Info> lastnamesList = new ArrayList<Info>();
    private List<Info> citiesList = new ArrayList<Info>();
    private List<Info> daysList = new ArrayList<Info>();
    /**
     * Constructor
     */
    public WebStatsFrequent(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_StatsFrequent"), "stats", "stats_", formatFromSize(wh.getNbIndis()), 1, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    @SuppressWarnings("unchecked")
    public void create() {

        // Generate links to the 3 sections below
        if (wb.sectionIndividuals != null) {
            namePage = wb.sectionIndividuals.getPagesMap();
            here2indiDir = buildLinkShort(this, wb.sectionIndividuals);
        }

        if (wb.sectionCitiesDetails != null) {
            cityPage = wb.sectionCitiesDetails.getPagesMap();
            here2cityDir = buildLinkShort(this, wb.sectionCitiesDetails);
        }

        if (wb.sectionDaysDetails != null) {
            dayPage = wb.sectionDaysDetails.getPagesMap();
            here2dayDir = buildLinkShort(this, wb.sectionDaysDetails);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        // Opens page
        String fileStr = sectionPrefix + String.format(formatNbrs, 1) + sectionSuffix;
        File file = wh.getFileForName(dir, fileStr);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }

        // Compute frequencies of LastName
        computeFrequency(TYPE_LASTNAME, lastnamesList);
        computeFrequency(TYPE_LOCATION, citiesList);
        computeFrequency(TYPE_DATE, daysList);

        printOpenHTML(out, "TXT_StatsFrequent", this);
        printHomeLink(out, this);

        // Print header
        printHeader(out);

        // Print implexe statistics
        printFrequency(out);

        // Closes page
        printCloseHTML(out);
        wh.log.write(fileStr + trs("EXEC_DONE"));
        out.close();

    }

    /**
     * Computes frequency of information for given information type
     */
    @SuppressWarnings("unchecked")
    private void computeFrequency(int type, List<Info> info) {

        if (type == TYPE_LASTNAME) {
            lastnamesList.clear();
            Iterator<String> itr = wh.getLastNames(DEFCHAR, sortLastnames).iterator();
            while (itr.hasNext()) {
                Info iOccu = new Info();
                iOccu.key = itr.next().toString();
                iOccu.frequency = wh.getLastNameCount(iOccu.key, DEFCHAR);
                lastnamesList.add(iOccu);
            }
            Collections.sort(lastnamesList, sortbyFrequency);
        }
        if (type == TYPE_LOCATION) {
            citiesList.clear();
            Iterator<String> itr = wh.getCities(wh.gedcom).iterator();
            while (itr.hasNext()) {
                Info iOccu = new Info();
                iOccu.key = itr.next().toString();
                iOccu.frequency = wh.getCitiesCount(iOccu.key);
                citiesList.add(iOccu);
            }
            Collections.sort(citiesList, sortbyFrequency);
        }
        if (type == TYPE_DATE) {
            daysList.clear();
            Iterator<String> itr = wh.getDays(wh.gedcom).iterator();
            while (itr.hasNext()) {
                Info iOccu = new Info();
                iOccu.key = itr.next().toString();
                iOccu.frequency = wh.getDaysCount(iOccu.key);
                daysList.add(iOccu);
            }
            Collections.sort(daysList, sortbyFrequency);
        }
    }
    /**
     * Comparator to sort by frequency
     */
    @SuppressWarnings("unchecked")
    private Comparator<Info> sortbyFrequency = new Comparator<Info>() {

        public int compare(Info info1, Info info2) {
            if (info2.frequency == info1.frequency) {
                return sortLastnames.compare(info1.key, info2.key);
            }
            return info2.frequency - info1.frequency;
        }
    };

    /**
     * Print report header.
     */
    private void printHeader(PrintWriter out) {

        // Print description
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("frequency_description")) + "</span></p>");
        out.println("<p class=\"description\">" + htmlText(trs("frequency_info")) + "</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

    }

    /**
     * Print implexe statistics.
     */
    private void printFrequency(PrintWriter out) {

        out.println("<div class=\"contreport2\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("frequency_table")) + "</span></p>");

        // Print header
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"column1\"><thead><tr>");
        out.println("<th>" + htmlText(trs("frequency_header_rank")) + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_lastname")) + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_frequency")) + "</th>");
        out.println("<th>" + SPACE + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_city")) + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_frequency")) + "</th>");
        out.println("<th>" + SPACE + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_day")) + "</th>");
        out.println("<th>" + htmlText(trs("frequency_header_frequency")) + "</th>");
        out.println("</tr></thead>");

        // Iteration on generations
        out.println("<tbody>");
        int l = Math.max(lastnamesList.size(), Math.max(citiesList.size(), daysList.size()));
        for (int i = 0; i < l; i++) {
            // Open row
            out.println("<tr>");
            // Print rank
            out.println("<td>" + (i + 1) + "</td>");
            // Print lastname
            printRowElement(TYPE_LASTNAME, out, i, lastnamesList, here2indiDir, namePage);
            out.println("<td>" + SPACE + "</td>");
            // Print city
            printRowElement(TYPE_LOCATION, out, i, citiesList, here2cityDir, cityPage);
            out.println("<td>" + SPACE + "</td>");
            // Print day
            printRowElement(TYPE_DATE, out, i, daysList, here2dayDir, dayPage);
            // Close row
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("<div class=\"spacer\">" + SPACE + "</div></div>");
    }

    /**
     * Print row element
     */
    private void printRowElement(int type, PrintWriter out, int i, List<Info> list, String here2Dir, Map<String, String> pages) {
        if (i < list.size()) {
            Info info = list.get(i);
            String text = null;
            String anchor = null;
            String page = null;
            if (type == TYPE_DATE) {
                String month = trs(Months[Integer.valueOf(info.key.substring(0, 2)) - 1]);
                String day = info.key.substring(2, 4);
                text = day + SPACE + htmlText(month);
                anchor = htmlAnchorText(month) + day;
                page = here2Dir + pages.get(info.key);
            } else {
                text = htmlText(info.key);
                anchor = htmlAnchorText(info.key);
                page = here2Dir + pages.get(anchor);
            }
            out.println("<td><a href=\"" + page + "#" + anchor + "\">" + text + "</a></td>");
            out.println("<td>" + htmlText(Integer.toString(info.frequency)) + "</td>");
        } else {
            out.println("<td>&nbsp;</td>");
            out.println("<td>&nbsp;</td>");
        }
    }
} // End_of_Report

