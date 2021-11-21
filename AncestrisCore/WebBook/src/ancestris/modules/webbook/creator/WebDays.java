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
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebDays extends WebSection {

    /**
     * Constructor
     */
    public WebDays(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Dayslist"), "days", "days", "", 1, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        File file = wh.getFileForName(dir, sectionPrefix + sectionSuffix);
        
        try (PrintWriter out = wh.getWriter(file, UTF8)) {
            // HEAD
            printOpenHTML(out, "TXT_Dayslist", this);
            // START OF PAGE ------------------
            printHomeLink(out, this);
            exportData(dir, out);
            // END OF PAGE ------------------
            // TAIL
            printCloseHTML(out);
            // done
        }
        wh.log.write(sectionPrefix + sectionSuffix + trs("EXEC_DONE"));
    }

    /**
     * Exports data for page
     */
    private void exportData(File dir, PrintWriter out) {

        // Let's pichk a bisextile year and loop on the 366 days of it
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
        Calendar calStart = Calendar.getInstance();
        calStart.set(2008, 0, 1);
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(2009, 0, 1);

        Iterator <String>it = wh.getDays(wh.gedcom).iterator();
        String lastMonth = "";
        int cpt = 1, iDays = 1, cptm = 0;
        out.println("<div class=\"daycal\">");

        String dateIt = "";
        if (it.hasNext()) {
            dateIt = it.next();
        }
        while (calStart.compareTo(calEnd) < 0) {
            String date = sdf.format(calStart.getTime());
            String month = trs(Months[Integer.valueOf(date.substring(0, 2)) - 1]);
            String day = date.substring(2, 4);
            // Close block after every month
            if (month.compareTo(lastMonth) != 0) {
                if (cptm > 0) {
                    out.println("</tr></table>");
                    out.println("</div>");
                }
                if (cptm > 0 && (cptm / 3 * 3) == cptm) {
                    out.println("<div class=\"spacer\">" + SPACE + "</div>");
                    out.println("</div><div class=\"daycal\">");
                }
                out.println("<div class=\"daycal1\">");
                out.println("<table class=\"daytbl\">");
                out.println("<tr><td colspan=\"" + NB_WORDS + "\"><span class=\"daychar\"><a id=\"" + Months[Integer.valueOf(date.substring(0, 2)) - 1] + "\"></a>" + month + "</span></td></tr><tr>");
                lastMonth = month;
                cptm++;
                cpt = 1;
            }

            // create link to name file
            String anchor = htmlAnchorText(month) + day;
            String listfile = buildLink(this, wb.sectionDaysDetails, iDays);
            out.print("<td>");
            if (date.compareTo(dateIt) == 0) {
                out.print("<a href=\"" + listfile + '#' + anchor + "\">" + day + "</a>" + SPACE);
                out.print("<span class=\"dayo\">(" + wh.getDaysCount(date) + ")</span>");
                iDays++;
                if (it.hasNext()) {
                    dateIt = it.next();
                }
            } else {
                out.print(day + SPACE);
            }
            out.print("</td>");
            cpt++;

            // Go to the line every NB_WORDS days
            if (cpt > NB_WORDS) {
                out.print("</tr><tr>");
                cpt = 1;
            }

            calStart.add(Calendar.DATE, 1);
        }

        out.println("</tr></table>");
        out.println("</div></div>");
    }
} // End_of_Repor
