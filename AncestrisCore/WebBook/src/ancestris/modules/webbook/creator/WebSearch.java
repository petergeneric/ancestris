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
import genj.gedcom.GedcomException;
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
        try (PrintWriter out = wh.getWriter(file, UTF8)) {
            if (out == null) {
                return;
            }
            printOpenHTML(out, null, this);
            out.println("<script src=\"" + resourceFile + "\"></script>");
            out.println("<script>");
            try {
                String str = wh.readStream(javascriptDir + "search.js");
                out.println(filter(str));
            } catch (IOException e) {
                wb.log.write(wb.log.ERROR, "exportResults - " + e.getMessage());
            }
            out.println("</script>");
            out.println("<div class=\"title\"><a id=\"top\">&nbsp;</a>" + trs("TXT_Search") + "</div>");
            printHomeLink(out, this);
            out.println("<p class=\"searchdecal\">" + htmlText(trs("search_criteria")) + "</p>");
            out.println("<form id=\"searchInputForm\" method=\"get\" action=\"" + exportfile + "\">");
            out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"searchtable\">");
            out.println("<tr><td>" + htmlText(trs("search_firstname")) + ":</td><td><input id=\"key_fn\" name=\"key_fn\" type=\"text\" size=\"15\" />&nbsp;<input id=\"key_xfn\" name=\"key_xfn\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_exact")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_lastname")) + ":</td><td><input id=\"key_ln\" name=\"key_ln\" type=\"text\" size=\"15\" />&nbsp;<input id=\"key_xln\" name=\"key_xln\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_exact")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_place")) + ":</td><td><input id=\"key_pl\" name=\"key_pl\" type=\"text\" size=\"15\" />&nbsp;<input id=\"key_xpl\" name=\"key_xpl\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_exact")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_id")) + ":</td><td><input id=\"key_id\" name=\"key_id\" type=\"text\" size=\"15\" />&nbsp;<input id=\"key_xid\" name=\"key_xid\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_exact")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_sosa")) + ":</td><td><input id=\"key_so\" name=\"key_so\" type=\"text\" size=\"15\" />&nbsp;<input id=\"key_xso\" name=\"key_xso\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_exact")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_birthd")) + ":</td><td>" + htmlText(trs("search_between")) + "&nbsp;<input id=\"key_1bi\" name=\"key_1bi\" type=\"text\" size=\"5\" />&nbsp;" + htmlText(trs("search_dateand")) + "&nbsp;<input id=\"key_2bi\" name=\"key_2bi\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input id=\"key_xbi\" name=\"key_xbi\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_not")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_marrid")) + ":</td><td>" + htmlText(trs("search_between")) + "&nbsp;<input id=\"key_1ma\" name=\"key_1ma\" type=\"text\" size=\"5\" />&nbsp;" + htmlText(trs("search_dateand")) + "&nbsp;<input id=\"key_2ma\" name=\"key_2ma\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input id=\"key_xma\" name=\"key_xma\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_not")) + "</td></tr>");
            out.println("<tr><td>" + htmlText(trs("search_deathd")) + ":</td><td>" + htmlText(trs("search_between")) + "&nbsp;<input id=\"key_1de\" name=\"key_1de\" type=\"text\" size=\"5\" />&nbsp;" + htmlText(trs("search_dateand")) + "&nbsp;<input id=\"key_2de\" name=\"key_2de\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input id=\"key_xde\" name=\"key_xde\" type=\"checkbox\" value=\"on\" />" + htmlText(trs("search_not")) + "</td></tr>");
            out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input id=\"andor1\" name=\"andor\" type=\"radio\" value=\"and\" checked />" + htmlText(trs("search_and")) + "&nbsp;&nbsp;&nbsp;<input id=\"andor2\" name=\"andor\" type=\"radio\" value=\"or\" />" + htmlText(trs("search_or")) + "</td></tr>");
            out.println("<tr><td colspan=\"2\" align=\"center\"><br />");
            out.println("<input id=\"OK\" type=\"submit\" value=\"" + trs("search_go") + "\" />&nbsp;&nbsp;");
            out.println("<input id=\"reset\" type=\"reset\" value=\"" + trs("search_reset") + "\" />&nbsp;&nbsp;");
            out.println("</td></tr>");
            out.println("</table>");
            out.println("</form>");
            out.println("<br>");
            out.println("<hr>");
            out.println("<p class=\"searchdecal\">" + htmlText(trs("search_result")) + "</p>");
            out.println("<br>");
            out.println("<p id=\"result\"></p>");
            printCloseHTML(out);
            wh.log.write(exportfile + trs("EXEC_DONE"));
        }    
    }

    /**
     * Exports resource file
     */
    @SuppressWarnings("unchecked")
    private void exportResources(File dir, String exportfile, List<Indi> indis) {
        File file = wh.getFileForName(dir, exportfile);
        try (PrintWriter out = wh.getWriter(file, UTF8)) {
            if (out == null) {
                return;
            }
            
            Map<String, List<String>> table = new TreeMap<>();
            
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
                    ids = new ArrayList<>();
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
                    ids = new ArrayList<>();
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
                        ids = new ArrayList<>();
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
                    ids = new ArrayList<>();
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
                    ids = new ArrayList<>();
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
                } catch (GedcomException t) {
                    //t.printStackTrace();
                }
                String word = Integer.toString(start);
                String key = "";
                if (word != null) {
                    key = cleanString(word);
                }
                List<String> ids = table.get(key);
                if (ids == null) {
                    ids = new ArrayList<>();
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
                } catch (GedcomException t) {
                    //t.printStackTrace();
                }
                String word = Integer.toString(start);
                String key = cleanString(word);
                
                List<String> ids = table.get(key);
                if (ids == null) {
                    ids = new ArrayList<>();
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
                } catch (GedcomException t) {
                    //t.printStackTrace();
                }
                String word = Integer.toString(start);
                String key = "";
                if (word != null) {
                    key = cleanString(word);
                }
                List<String> ids = table.get(key);
                if (ids == null) {
                    ids = new ArrayList<>();
                }
                ids.add(indi.getId());
                table.put(key, ids);
            }
            writeTable(out, "list_deaths", table);
            
            writeTableIndis(out, indis);
            
            wh.log.write(exportfile + trs("EXEC_DONE"));
        }
    }

    /**
     * Write file from table
     */
    @SuppressWarnings("unchecked")
    private void writeTable(PrintWriter out, String tableName, Map<String, List<String>> table) {

        StringBuilder list = new StringBuilder("var " + tableName + " = [");
        StringBuilder listID = new StringBuilder("var " + tableName + "ID = [");
        int cpt = 0, cptID = 0;
        for (String key : table.keySet()) {
            list.append((cpt == 0 ? "" : ",")).append("\"").append(key).append("\"");
            listID.append((cpt == 0 ? "" : ",")).append("\"");
            List<String> ids = table.get(key);
            cptID = 0;
            for (String id : ids) {
                listID.append((cptID == 0 ? "" : "|")).append(id);
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

        StringBuilder list = new StringBuilder("var ID = [");
        StringBuilder listID = new StringBuilder("var IDdisplay = [");
        int cpt = 0;
        for (Indi indi : indis) {
            list.append((cpt == 0 ? "" : ",")).append("\"").append(indi.getId()).append("\"");
            listID.append((cpt == 0 ? "" : ",")).append("\"");
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
        String date = (bdate == null) ? "" : bdate.getDisplayValue().trim();
        return date;
    }

    private String getDDate(Indi indi) {
        if (indi == null) {
            return "";
        }
        PropertyDate ddate = indi.getDeathDate();
        String date = (ddate == null) ? "" : ddate.getDisplayValue().trim();
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

