/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSearch extends WebSection {

    private String searchFile = "";
    private String titleFile = "";
    private String inputFile = "";
    private String resultsFile = "";
    private String resourceFile = "";

    /**
     * Constructor
     */
    public WebSearch(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Search"), "search", "search_", formatFromSize(wh.getNbIndis()), ".html", 0, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // Preliminary build of individualsdetails link for links from sources to details
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Generate detail pages
        searchFile = sectionPrefix + String.format(formatNbrs, 0) + sectionSuffix;
        titleFile = sectionPrefix + String.format(formatNbrs, 1) + sectionSuffix;
        inputFile = sectionPrefix + String.format(formatNbrs, 2) + sectionSuffix;
        resultsFile = sectionPrefix + String.format(formatNbrs, 3) + sectionSuffix;
        resourceFile = sectionPrefix + String.format(formatNbrs, 4) + ".js";

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        // Create search frames page
        exportFrames(dir, searchFile);

        // Create search frames page
        exportTitle(dir, titleFile);

        // Create search input page
        exportInput(dir, inputFile);

        // Create search results page
        exportResults(dir, resultsFile);

        // Create js content file
        List indis = wh.getIndividuals(wh.gedcom, null);
        exportResources(dir, resourceFile, indis);
    }

    /**
     * Exports frames
     */
    private void exportFrames(File dir, String exportfile) {
        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }
        out.println("<!DOCTYPE html PUBLIC  \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + htmlText(wp.param_title) + SPACE + "-" + SPACE + htmlText(trs("TXT_Search")) + "</title>");
        out.println("</head>");
        out.println("<frameset rows=\"70,*\" framespacing=\"0\">");
        out.println("<frame src=\"" + titleFile + "\" name=input_frame frameborder=\"0\" scrolling=\"no\" noresize >");
        out.println("<frameset cols=\"30%,*\" framespacing=\"0\">");
        out.println("<frame src=\"" + inputFile + "\" name=input_frame frameborder=\"0\" scrolling=\"auto\">");
        out.println("<frame src=\"" + resultsFile + "\" name=\"resultat\" frameborder=\"0\" scrolling=\"auto\">");
        out.println("</frameset>");
        out.println("</frameset>");
        out.println("</html>");
        wh.log.write(searchFile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Exports input Frame
     */
    private void exportTitle(File dir, String exportfile) {
        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }
        printOpenHTML(out, "TXT_Search", this);
        printCloseHTML(out);
        wh.log.write(exportfile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Exports input Frame
     */
    private void exportInput(File dir, String exportfile) {
        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }
        printOpenHTML(out, null, this);
        out.println("<p class=\"searchdecal\">" + trs("search_criteria") + "</p>");
        out.println("<form method=\"get\" action=\"" + resultsFile + "\" target=\"resultat\" accept-charset=\"iso-8859-1\">");
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"searchtable\">");
        out.println("<tr><td>" + trs("search_firstname") + ":</td><td><input name=\"key_fn\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xfn\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");

        out.println("<tr><td>" + trs("search_lastname") + ":</td><td><input name=\"key_ln\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xln\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");

        out.println("<tr><td>" + trs("search_place") + ":</td><td><input name=\"key_pl\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xpl\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");

        out.println("<tr><td>" + trs("search_genjid") + ":</td><td><input name=\"key_id\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xid\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");

        out.println("<tr><td>" + trs("search_sosa") + ":</td><td><input name=\"key_so\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xso\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");

        out.println("<tr><td>" + trs("search_birthd") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1bi\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2bi\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xbi\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");

        out.println("<tr><td>" + trs("search_marrid") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1ma\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2ma\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xma\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");

        out.println("<tr><td>" + trs("search_deathd") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1de\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2de\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xde\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");

        out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input name=\"andor\" type=\"radio\" value=\"and\" checked />" + trs("search_and") + "&nbsp;&nbsp;&nbsp;<input name=\"andor\" type=\"radio\" value=\"or\" />" + trs("search_or") + "</td></tr>");

        out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input name=\"OK\" type=\"submit\" value=\"" + trs("search_go") + "\" />&nbsp;&nbsp;<input name=\"reset\" type=\"reset\" value=\"" + trs("search_reset") + "\" />&nbsp;&nbsp;<input name=\"home\" type=\"button\" value=\"" + trs("alt_home") + "\" onclick=\"top.window.location.href='" + getHomeLink(this) + "'\" /></td></tr>");
        out.println("</table>");
        out.println("</form>");
        printCloseHTML(out);
        wh.log.write(exportfile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Exports results Frame
     */
    private void exportResults(File dir, String exportfile) {

        String genjJavascriptDir = "js/";

        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }
        printOpenHTML(out, null, this);
        out.println("<script src=\"" + resourceFile + "\"></script>");
        out.println("<script language=\"JavaScript\">");
        try {
            String str = wh.readStream(genjJavascriptDir + "search.js");
            out.println(filter(str));
        } catch (IOException e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "exportResults - " + e.getMessage());
        }
        out.println("</script>");
        printCloseHTML(out);
        wh.log.write(exportfile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Exports resource file
     */
    @SuppressWarnings("unchecked")
    private void exportResources(File dir, String exportfile, List indis) {
        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }

        Map<String, List> table = new TreeMap<String, List>();

        //Produce firstNames list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            String word = indi.getFirstName();
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_firstnames", table);

        //Produce lastNames list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            String word = wh.getLastName(indi, DEFCHAR);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_lastnames", table);

        //Produce places list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            List places = indi.getProperties(PropertyPlace.class);
            for (Iterator itp = places.iterator(); itp.hasNext();) {
                PropertyPlace place = (PropertyPlace) itp.next();
                if (place == null) {
                    continue;
                }
                String word = place.toString();
                String key = "";
                if (word != null) {
                    key = cleanString(word);
                }
                List<String> ids = (List<String>) table.get(key);
                if (ids == null) {
                    ids = new ArrayList<String>();
                }
                ids.add(indi.getId());
                table.put(key, ids);
            }
        }
        writeTable(out, "list_places", table);

        //Produce ids list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            String word = indi.getId();
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_ids", table);

        //Produce sosa list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            String word = wh.getSosa(indi);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_sosas", table);

        //Produce births list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            PropertyDate date = (indi == null) ? null : indi.getBirthDate();
            if ((indi == null) || (date == null)) {
                continue;
            }
            if (!date.isValid()) {
                continue;
            }
            int start = 0;
            try {
                start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
            String word = Integer.toString(start);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_births", table);

        //Produce marriages list
        table.clear();
        List families = new ArrayList(wh.gedcom.getEntities(Gedcom.FAM));
        for (Iterator it = families.iterator(); it.hasNext();) {
            Fam family = (Fam) it.next();
            PropertyDate date = (family == null) ? null : family.getMarriageDate();
            if ((family == null) || (date == null)) {
                continue;
            }
            if (!date.isValid()) {
                continue;
            }
            int start = 0;
            try {
                start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
            String word = Integer.toString(start);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            Indi husband = family.getHusband();
            if (husband != null) {
                ids.add(husband.getId());
            }
            Indi wife = family.getWife();
            if (wife != null) {
                ids.add(wife.getId());
            }
            table.put(key, ids);
        }
        writeTable(out, "list_marriages", table);

        //Produce death list
        table.clear();
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            PropertyDate date = (indi == null) ? null : indi.getDeathDate();
            if ((indi == null) || (date == null)) {
                continue;
            }
            if (!date.isValid()) {
                continue;
            }
            int start = 0;
            try {
                start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
            String word = Integer.toString(start);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = (List<String>) table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_deaths", table);

        writeTableIndis(out, indis);

        wh.log.write(exportfile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Write file from table
     */
    @SuppressWarnings("unchecked")
    private void writeTable(PrintWriter out, String tableName, Map table) {

        StringBuffer list = new StringBuffer("var " + tableName + " = [");
        StringBuffer listID = new StringBuffer("var " + tableName + "ID = [");
        int cpt = 0, cptID = 0;
        for (Iterator itk = table.keySet().iterator(); itk.hasNext();) {
            String key = (String) itk.next();
            list.append((cpt == 0 ? "" : ",") + "\"" + key + "\"");
            listID.append((cpt == 0 ? "" : ",") + "\"");
            List<String> ids = (List<String>) table.get(key);
            cptID = 0;
            for (Iterator it = ids.iterator(); it.hasNext();) {
                String id = (String) it.next();
                listID.append((cptID == 0 ? "" : "|") + id);
                cptID++;
            }
            listID.append("\"");
            cpt++;
        }
        list.append("]");
        listID.append("]");
        out.println(list.toString());
        out.println(listID.toString());
        return;
    }

    /**
     * Write file from table
     */
    private void writeTableIndis(PrintWriter out, List indis) {

        //out.println("var ID = [\"I001\",\"I002\",\"I003\",\"I004\",\"I005\"]");
        //out.println("var IDdisplay = [\"Frederic Lapeyre (29 Oct 1968) (I001) |001|I001\",\"Jean Sebastien Frederic Surrel|001|I002\",\"Sebastien Lapeyre|002|I003\",\"Raymond Fred|003|I004\",\"Fred Surrel|001|I005\"]");
        StringBuffer list = new StringBuffer("var ID = [");
        StringBuffer listID = new StringBuffer("var IDdisplay = [");
        int cpt = 0;
        for (Iterator it1 = indis.iterator(); it1.hasNext();) {
            Indi indi = (Indi) it1.next();
            list.append((cpt == 0 ? "" : ",") + "\"" + indi.getId() + "\"");
            listID.append((cpt == 0 ? "" : ",") + "\"");
            listID.append(indi.getSex() + "|" + indi.getId() + "|" + getPage(indi) + "|" + getName(indi) + "|" + getSosa(indi) + "|" + getBDate(indi) + "|" + getDDate(indi));
            listID.append("\"");
            cpt++;
        }
        list.append("]");
        listID.append("]");
        out.println(list.toString());
        out.println(listID.toString());
        return;
    }

    /**
     * Get page of individual
     */
    private String getPage(Indi indi) {
        String id = (indi == null) ? "" : indi.getId();
        String page = (indi == null) ? "" : ((personPage == null) ? "" : personPage.get(id));
        int start = wb.sectionIndividualsDetails.sectionPrefix.length();
        int end = page.indexOf(wb.sectionIndividualsDetails.sectionSuffix);
        return page.substring(start, end);
    }

    /**
     * Get name of individual
     */
    private String getName(Indi indi) {
        String name = (indi == null) ? wp.param_unknown : (wh.getLastName(indi, DEFCHAR) + ", " + indi.getFirstName()).trim();
        if (wh.isPrivate(indi)) {
            name = "..., ...";
        }
        if (name.compareTo(",") == 0) {
            name = "";
        }
        String result = name.replaceAll("\"", "");
        return result;
    }

    /**
     * Get sosa of individual
     */
    private String getSosa(Indi indi) {
        String sosa = wh.getSosa(indi);
        return ((sosa != null && sosa.length() != 0) ? sosa : "");
    }

    /**
     * Get dates of individual
     */
    private String getBDate(Indi indi) {
        PropertyDate bdate = (indi == null) ? null : indi.getBirthDate();
        String date = (indi == null) || (bdate == null) ? "" : bdate.toString().trim();
        return date;
    }

    private String getDDate(Indi indi) {
        PropertyDate ddate = (indi == null) ? null : indi.getDeathDate();
        String date = (indi == null) || (ddate == null) ? "" : ddate.toString().trim();
        return date;
    }

    /**
     * Clean strings
     */
    private String cleanString(String str) {
        return str.toUpperCase().replaceAll("\"", "").replaceAll("\\\\", "/");
    }

    /**
     * Read input file and put into string
     */
    private String filter(String inputStr) {
        String text = inputStr.replaceAll("search_please",
                trs("search_please")).replaceAll("search_results1",
                trs("search_results1")).replaceAll("search_results2",
                trs("search_results2")).replaceAll("alt_male",
                trs("alt_male")).replaceAll("alt_female",
                trs("alt_female")).replaceAll("alt_unknown",
                trs("alt_unknown")).replaceAll("searcht_sex",
                trs("searcht_sex")).replaceAll("searcht_id",
                trs("searcht_id")).replaceAll("searcht_name",
                trs("searcht_name")).replaceAll("searcht_sosa",
                trs("searcht_sosa")).replaceAll("searcht_bdate",
                trs("searcht_bdate")).replaceAll("searcht_ddate",
                trs("searcht_ddate"));
        return text;
    }
} // End_of_Report

