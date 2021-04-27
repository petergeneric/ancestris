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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebSearch extends WebSection {

    private String searchFile = "";
    private String resourceFile = "";

    /**
     * Constructor
     */
    public WebSearch(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Search"), "search", "search_", formatFromSize(wh.getNbIndis()), 0, 0);
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
        resourceFile = sectionPrefix + String.format(formatNbrs, 4) + ".js";

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        // Create search input page
        exportSearch(dir, searchFile);

        // Create js content file
        List<Indi> indis = wh.getIndividuals(wh.gedcom, null);
        exportResources(dir, resourceFile, indis);
    }

    /**
     * Exports search file
     */
    private void exportSearch(File dir, String exportfile) {

        String javascriptDir = "js/";

        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }
        printOpenHTML(out, null, this);
        out.println("<script src=\"" + resourceFile + "\"></script>");
        out.println("<script language=\"JavaScript\">");
        try {
            String str = wh.readStream(javascriptDir + "search.js");
            out.println(filter(str));
        } catch (IOException e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "exportResults - " + e.getMessage());
        }
        out.println("</script>");
        out.println("<div class=\"title\"><a name=\"top\">&nbsp;</a>" + trs("TXT_Search") + "</div>");
        printHomeLink(out, this);
        out.println("<p class=\"searchdecal\">" + trs("search_criteria") + "</p>");
        out.println("<form name=\"searchInputForm\" method=\"get\" action=\"" + exportfile + "\" accept-charset=\"iso-8859-1\">");
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"searchtable\">");
        out.println("<tr><td>" + trs("search_firstname") + ":</td><td><input name=\"key_fn\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xfn\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");
        out.println("<tr><td>" + trs("search_lastname") + ":</td><td><input name=\"key_ln\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xln\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");
        out.println("<tr><td>" + trs("search_place") + ":</td><td><input name=\"key_pl\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xpl\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");
        out.println("<tr><td>" + trs("search_id") + ":</td><td><input name=\"key_id\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xid\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");
        out.println("<tr><td>" + trs("search_sosa") + ":</td><td><input name=\"key_so\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xso\" type=\"checkbox\" value=\"on\" />" + trs("search_exact") + "</td></tr>");
        out.println("<tr><td>" + trs("search_birthd") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1bi\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2bi\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xbi\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");
        out.println("<tr><td>" + trs("search_marrid") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1ma\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2ma\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xma\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");
        out.println("<tr><td>" + trs("search_deathd") + ":</td><td>" + trs("search_between") + "&nbsp;<input name=\"key_1de\" type=\"text\" size=\"5\" />&nbsp;" + trs("search_dateand") + "&nbsp;<input name=\"key_2de\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xde\" type=\"checkbox\" value=\"on\" />" + trs("search_not") + "</td></tr>");
        out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input name=\"andor\" type=\"radio\" value=\"and\" checked />" + trs("search_and") + "&nbsp;&nbsp;&nbsp;<input name=\"andor\" type=\"radio\" value=\"or\" />" + trs("search_or") + "</td></tr>");
        out.println("<tr><td colspan=\"2\" align=\"center\"><br />");
        out.println("<input name=\"OK\" type=\"submit\" value=\"" + trs("search_go") + "\" />&nbsp;&nbsp;");
        out.println("<input name=\"reset\" type=\"reset\" value=\"" + trs("search_reset") + "\" />&nbsp;&nbsp;");
        //out.println("<input name=\"home\" type=\"button\" value=\"" + trs("alt_home") + "\" onclick=\"top.window.location.href='" + getHomeLink(this) + "'\" />");
        out.println("</td></tr>");
        out.println("</table>");
        out.println("</form>");
        out.println("<br>");
        out.println("<hr>");
        out.println("<p class=\"searchdecal\">" + trs("search_result") + "</p>");
        out.println("<br>");
        out.println("<script language=javascript>");
        out.println("   processSearch();");
        out.println("</script>");
        printCloseHTML(out);
        wh.log.write(exportfile + trs("EXEC_DONE"));
        out.close();
    }

    /**
     * Exports resource file
     */
    @SuppressWarnings("unchecked")
    private void exportResources(File dir, String exportfile, List<Indi> indis) {
        File file = wh.getFileForName(dir, exportfile);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }

        Map<String, List<String>> table = new TreeMap<String, List<String>>();

        //Produce firstNames list
        table.clear();
        for (Indi indi : indis) {
            String word = indi.getFirstName();
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_firstnames", table);

        //Produce lastNames list
        table.clear();
        for (Indi indi : indis) {
            String word = wh.getLastName(indi, DEFCHAR);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_lastnames", table);

        //Produce places list
        table.clear();
        for (Indi indi : indis) {
            List<PropertyPlace> places = indi.getProperties(PropertyPlace.class);
            for (PropertyPlace place : places) {
                if (place == null) {
                    continue;
                }
                String word = place.getDisplayValue();
                String key = "";
                if (word != null) {
                    key = cleanString(word);
                }
                List<String> ids = table.get(key);
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
        for (Indi indi : indis) {
            String word = indi.getId();
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_ids", table);

        //Produce sosa list
        table.clear();
        for (Indi indi : indis) {
            String word = wh.getSosa(indi);
            String key = "";
            if (word != null) {
                key = cleanString(word);
            }
            List<String> ids = table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_sosas", table);

        //Produce births list
        table.clear();
        for (Indi indi : indis) {
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
            List<String> ids = table.get(key);
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(indi.getId());
            table.put(key, ids);
        }
        writeTable(out, "list_births", table);

        //Produce marriages list
        table.clear();
        Collection<Fam> families = (Collection<Fam>)wh.gedcom.getEntities(Gedcom.FAM);
        for (Fam family : families) {
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
            String key = cleanString(word);
            
            List<String> ids = table.get(key);
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
        for (Indi indi : indis) {
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
            List<String> ids = table.get(key);
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
    private void writeTable(PrintWriter out, String tableName, Map<String, List<String>> table) {

        StringBuffer list = new StringBuffer("var " + tableName + " = [");
        StringBuffer listID = new StringBuffer("var " + tableName + "ID = [");
        int cpt = 0, cptID = 0;
        for (String key : table.keySet()) {
            list.append((cpt == 0 ? "" : ",") + "\"" + key + "\"");
            listID.append((cpt == 0 ? "" : ",") + "\"");
            List<String> ids = table.get(key);
            cptID = 0;
            for (String id : ids) {
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
    }

    /**
     * Write file from table
     */
    private void writeTableIndis(PrintWriter out, List<Indi> indis) {

        //out.println("var ID = [\"I001\",\"I002\",\"I003\",\"I004\",\"I005\"]");
        //out.println("var IDdisplay = [\"Frederic Lapeyre (01 Mar 1952) (I001) |001|I001\",\"Jean Philippe Frederic Surrel|001|I002\",\"Sebastien Aubry|002|I003\",\"Raymond Fred|003|I004\",\"Fred Surrel|001|I005\"]");
        StringBuffer list = new StringBuffer("var ID = [");
        StringBuffer listID = new StringBuffer("var IDdisplay = [");
        int cpt = 0;
        for (Indi indi : indis) {
            list.append((cpt == 0 ? "" : ",") + "\"" + indi.getId() + "\"");
            listID.append((cpt == 0 ? "" : ",") + "\"");
            listID.append(phpText(indi));
            listID.append("\"");
            cpt++;
        }
        list.append("]");
        listID.append("]");
        out.println(list.toString());
        out.println(listID.toString());
    }

    private String phpText(Indi indi) {
        String strPriv = wh.getPrivDisplay();
        if (hidePrivateData && wh.isPrivate(indi)) {
            return "0" + "|" + indi.getId() + "|" + getPage(indi) + "|" + strPriv + "|" + strPriv + "|" + strPriv + "|" + strPriv;
        } else {
            return getSex(indi) + "|" + indi.getId() + "|" + getPage(indi) + "|" + getName(indi) + "|" + getSosa(indi) + "|" + getBDate(indi) + "|" + getDDate(indi);
        }
    }

    /**
     * Get sex
     */
    private String getSex(Indi indi) {
        if (indi == null) {
            return "0";
        }
        return "" + indi.getSex();
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
        String name = wrapName(indi, DT_LASTFIRST, DT_NOLINK, DT_NOSOSA, DT_NOID);
        String result = name.replaceAll("\"", "");
        return result;
    }

    /**
     * Get sosa of individual
     */
    private String getSosa(Indi indi) {
        String sosa = wh.getSosa(indi);
        return ((sosa != null && sosa.length() != 0) ? wrapString(indi, sosa) : "");
    }

    /**
     * Get dates of individual
     */
    private String getBDate(Indi indi) {
        if (indi == null) {
            return "";
        }
        PropertyDate bdate = indi.getBirthDate();
        String date = (indi == null) || (bdate == null) ? "" : bdate.getDisplayValue().trim();
        return date;
    }

    private String getDDate(Indi indi) {
        if (indi == null) {
            return "";
        }
        PropertyDate ddate = indi.getDeathDate();
        String date = (indi == null) || (ddate == null) ? "" : ddate.getDisplayValue().trim();
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
        String text = inputStr.replaceAll("search_please", trs("search_please")).replaceAll("search_results", trs("search_results")).replaceAll("alt_male", trs("alt_male")).replaceAll("alt_female", trs("alt_female")).replaceAll("alt_unknown", trs("alt_unknown")).replaceAll("searcht_sex", trs("searcht_sex")).replaceAll("searcht_id", trs("searcht_id")).replaceAll("searcht_name", trs("searcht_name")).replaceAll("searcht_sosa", trs("searcht_sosa")).replaceAll("searcht_bdate", trs("searcht_bdate")).replaceAll("searcht_ddate", trs("searcht_ddate")).replaceAll(".html#", (wp.param_PHP_Support.equals("1")) ? ".php#" : ".html#");
        return text;
    }
} // End_of_Report

