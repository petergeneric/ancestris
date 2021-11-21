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
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebDaysDetails extends WebSection {

    /**
     * Constructor
     */
    public WebDaysDetails(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Daysdetails"), "daysdetails", "daysdetails_", formatFromSize(wh.getNbIndis()), 1, sizeIndiSection);
        calcPages();
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        // Preliminary build of sources link for links from details to sources
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        List <String>days = wh.getDays(wh.gedcom);
        // Go through days
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String dayfile = "";
        int cpt = 0;
        int nbDays = days.size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbDays / nbPerPage) + 1;

        for (Iterator <String>it = days.iterator(); it.hasNext();) {
            String day = it.next();
            cpt++;
            currentPage = (cpt / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            dayfile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (fileStr.compareTo(dayfile) != 0) {
                if (out != null) {
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 1, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    out.close();
                    wh.log.write(fileStr + trs("EXEC_DONE"));
                }
                fileStr = dayfile;
                file = wh.getFileForName(dir, dayfile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Daysdetails", this);
            }
            exportLinks(out, dayfile, 1, previousPage, nextPage, lastPage);
            exportDayDetails(out, day);
            // .. next day
        }
        if (out != null) {
            exportLinks(out, dayfile, 1, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }
    }

    /**
     * Exports days details
     */
    @SuppressWarnings("unchecked")
    private void exportDayDetails(PrintWriter out, String date) {

        // Day name
        String month = trs(Months[Integer.valueOf(date.substring(0, 2)) - 1]);
        String day = date.substring(2, 4);
        String anchor = htmlAnchorText(month) + day;
        out.println("<h2 class=\"unk\"><a id=\"" + anchor + "\"></a>" + day + SPACE + htmlText(month) + "</h2>");

        // All day properties that have that day
        List<Property> listProps = wh.getDaysProps(date);
        Collections.sort(listProps, sortEvents);
        boolean first = true;
        for (Iterator<Property> p = listProps.iterator(); p.hasNext();) {
            Property prop = p.next();
            if ((prop == null) || (prop.getValue().length() == 0)) {
                continue;
            }
            // Case of a change and a date
            if (first) {
                out.println("<div class=\"daycont\">");
                out.println("<span class=\"dayhd1\">" + htmlText(trs("date_detail")) + "</span>");
                out.println("<span class=\"dayhd2\">" + htmlText(trs("date_event")) + "</span>");
                out.println("<span class=\"dayhd3\">" + htmlText(trs("date_indi")) + "</span>");
                out.println("<span class=\"spacer\">" + SPACE + "</span>");
                first = false;
            }
            out.println("<span class=\"dayevt1\">" + wrapEventDate(((PropertyDate) prop)) + "</span>");
            out.println("<span class=\"dayevt2\">" + wrapPropertyName(prop.getParent()) + "</span>");
            out.println("<span class=\"dayevt3\">" + wrapEntity(prop.getEntity()) + "</span>");
        }
        out.println("<span class=\"spacer\">" + SPACE + "</span>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");
    }
    /**
     * Comparator to sort events for a day
     */
    private Comparator<Property> sortEvents = new Comparator<Property>() {

        public int compare(Property prop1, Property prop2) {
            if ((prop1 == null) && (prop2 != null)) {
                return -1;
            }
            if ((prop1 != null) && (prop2 == null)) {
                return +1;
            }
            if ((prop1 == null) && (prop2 == null)) {
                return 0;
            }

            // Otherwise, sort on dates
            PropertyDate date1 = (PropertyDate) prop1;
            PropertyDate date2 = (PropertyDate) prop2;
            if (date1 == null) {
                return -1;
            }
            if (date2 == null) {
                return +1;
            }
            if (date1.compareTo(date2) != 0) {
                return date1.compareTo(date2);
            }

            // Otherwise, sort on individuals
            Entity ent1 = prop1.getEntity();
            Entity ent2 = prop2.getEntity();

            if (ent1 == null) {
                return -1;
            }
            if (ent2 == null) {
                return +1;
            }
            return ent1.toString().compareTo(ent2.toString());
        }
    };

    /**
     * Calculate pages for day details
     */
    private void calcPages() {
        String dayfile = "", fileStr = "";
        int cpt = 0;
        for (Iterator<String> it = wh.getDays(wh.gedcom).iterator(); it.hasNext();) {
            String day = it.next();
            cpt++;
            dayfile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix;
            if (fileStr.compareTo(dayfile) != 0) {
                fileStr = dayfile;
            }
            dayPage.put(day, dayfile);
        }
    }

    /**
     * Provide links map to outside caller
     */
    public Map<String, String> getPagesMap() {
        return dayPage;
    }

} // End_of_Report

