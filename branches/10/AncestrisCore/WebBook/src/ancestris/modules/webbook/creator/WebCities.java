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

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebCities extends WebSection {

    /**
     * Constructor
     */
    public WebCities(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Citieslist"), "cities", "cities", "", 1, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        File file = wh.getFileForName(dir, sectionPrefix + sectionSuffix);
        PrintWriter out = wh.getWriter(file, UTF8);

        // HEAD
        printOpenHTML(out, "TXT_Citieslist", this);

        // START OF PAGE ------------------
        calcLetters();
        exportData(dir, out);
        // END OF PAGE ------------------

        // TAIL
        printLinks(out, sectionPrefix + sectionSuffix, this);
        printCloseHTML(out);

        // done
        out.close();
        wh.log.write(sectionPrefix + sectionSuffix + trs("EXEC_DONE"));
    }

    /**
     * Exports data for page
     */
    private void exportData(File dir, PrintWriter out) {

        out.println("<p class=\"letters\">");
        out.println("<br /><br />");
        for (Letters l : Letters.values()) {
            if (checkLink(l.toString())) {
                out.println("<a href=\"" + '#' + l + "\">" + l + "</a>" + SPACE + SPACE);
            } else {
                out.println(l + SPACE + SPACE);
            }
        }
        if (checkLink(DEFCHAR)) {
            out.println("<a href=\"" + '#' + DEFCHAR + "\">" + DEFCHAR + "</a>" + SPACE + SPACE);
        } else {
            out.println(DEFCHAR + SPACE + SPACE);
        }
        out.println("</p>");
        printLinks(out, sectionPrefix + sectionSuffix, this);

        // Create link for each last name
        Iterator <String>it = wh.getCities(wh.gedcom).iterator();
        char last = ' ';
        int cpt = 1, iNames = 1;
        out.println("<p class=\"nameblock\">");
        while (it.hasNext()) {
            // create new name class (first char) if necessary
            String name = it.next().toString();
            String anchor = htmlAnchorText(name);
            if (anchor.length() > 0 && Character.toUpperCase(anchor.charAt(0)) != last) {
                last = Character.toUpperCase(anchor.charAt(0));
                String l = String.valueOf(last);
                out.println("</p>");
                out.println("<p class=\"char\">");
                out.println("<a name=\"" + l + "\"></a>");
                out.println(l + "<br /></p>");
                cpt = 1;
                out.println("<p class=\"nameblock\">");
            }
            // create link to name file
            String listfile = buildLink(this, wb.sectionCitiesDetails, iNames);
            out.print("<span class=\"name\">");
            out.print("<a href=\"" + listfile + '#' + anchor + "\">" + htmlText(name) + "</a>" + SPACE);
            out.print("<span class=\"occu\">(" + wh.getCitiesCount(name) + ")</span>");
            out.print("</span>" + SPACE + SPACE + SPACE);
            cpt++;
            iNames++;

            if (cpt > NB_WORDS) {
                out.print("<br />");
                cpt = 1;
            }
        }
        out.println("</p>");

    }

    /**
     * Calculate if there is a link to the letters
     */
    private void calcLetters() {

        linkForLetter.put(DEFCHAR, "0");
        for (Letters l : Letters.values()) {
            linkForLetter.put(l.toString(), "0");
        }

        Iterator<String> it = wh.getCities(wh.gedcom).iterator();
        while (it.hasNext()) {
            String name = it.next().toString();
            String l = (name.length() > 0) ? name.substring(0, 1).toUpperCase() : DEFCHAR;
            try {
                Letters.valueOf(l);
            } catch (IllegalArgumentException e) {
                l = DEFCHAR;
            }
            linkForLetter.put(l, "1");
        }
    }

    /**
     * Booleanise existance of link to a letter
     */
    private boolean checkLink(String str) {
        String flag = linkForLetter.get(str);
        if (flag == null || flag.compareTo("0") == 0) {
            return false;
        }
        return true;
    }
} // End_of_Report

