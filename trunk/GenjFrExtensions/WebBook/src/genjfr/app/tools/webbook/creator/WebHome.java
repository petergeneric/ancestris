/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Indi;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;

/**
 *
 * @author frederic
 */
public class WebHome extends WebSection {

    /**
     * Constructor
     */
    public WebHome(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        File dir = wh.createDir(wh.getDir().getAbsolutePath(), true);
        File file = wh.getFileForName(dir, indexFile);
        PrintWriter out = wh.getWriter(file, UTF8);

        // Calculate statistics
        GedcomStats stats = new GedcomStats(wp, wh);

        // HEAD
        printOpenHTML(out, "", this);

        // START OF PAGE ------------------
        trs("exporting", new String[]{file.getName(), dir.toString()});
        exportIndex(out, stats);
        // END OF PAGE ------------------

        // TAIL
        printCloseHTML(out);

        // done
        out.close();
        wh.log.write(indexFile + trs("EXEC_DONE"));
    }

    /**
     * Exports index.html
     */
    private void exportIndex(PrintWriter out, GedcomStats stats) {

        out.println("<hr />");
        out.println("<div class=\"contindex\">");      // conteneur

        //
        // MENU side of the index page
        //
        out.println("<div class=\"menu\">");           /* ***new section*** */

        // Individuals
        out.println("<p>" + trs("menu_individuals") + "</p><ul>");
        if (wb.sectionLastnames.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionLastnames.sectionLink + "\">" + htmlText(wb.sectionLastnames.sectionName) + "</a></li>");
        }
        if (wb.sectionIndividuals.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionIndividuals.sectionLink + "\">" + htmlText(wb.sectionIndividuals.sectionName) + "</a></li>");
        }

//        if (wb.sectionIndividualsDetails) {
//            out.println("<li><a href=\"" + wb.sectionIndividualsDetails.sectionLink + "\">" + htmlText(wb.sectionIndividualsDetails.sectionName) + "</a></li>");
//        }

        // Documents
//        if (displaySourceSec || displayMediaSec) {
//            out.println("</ul><p>" + trs("menu_documents") + "</p><ul>");
//        }
//        if (pagesSources && displaySourceSec) {
//            out.println("<li><a href=\"" + sectionSources.sectionLink + "\">" + htmlText(sectionSources.sectionName) + "</a></li>");
//        }
//        if (pagesMedia && displayMediaSec) {
//            out.println("<li><a href=\"" + sectionMedia.sectionLink + "\">" + htmlText(sectionMedia.sectionName) + "</a></li>");
//        }

        // Locations
//        out.println("</ul><p>" + htmlText(trs("menu_locations")) + "</p><ul>");
//        out.println("<li><a href=\"" + sectionMap.sectionLink + "\">" + htmlText(sectionMap.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionCities.sectionLink + "\">" + htmlText(sectionCities.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionCitiesDetails.sectionLink + "\">" + htmlText(sectionCitiesDetails.sectionName) + "</a></li>");

        // Dates
//        out.println("</ul><p>" + htmlText(trs("menu_days")) + "</p><ul>");
//        out.println("<li><a href=\"" + sectionDays.sectionLink + "\">" + htmlText(sectionDays.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionDaysDetails.sectionLink + "\">" + htmlText(sectionDaysDetails.sectionName) + "</a></li>");

        // Statistics
//        out.println("</ul><p>" + htmlText(trs("menu_statistics")) + "</p><ul>");
//        if (pagesStatsFrequent) {
//            out.println("<li><a href=\"" + sectionStatsFrequent.sectionLink + "\">" + htmlText(sectionStatsFrequent.sectionName) + "</a></li>");
//        }
//        if (pagesStatsImplex) {
//            out.println("<li><a href=\"" + sectionStatsImplex.sectionLink + "\">" + htmlText(sectionStatsImplex.sectionName) + "</a></li>");
//        }

        // Structured lists
//        if (displayRepSosa) {
//            out.println("</ul><p>" + htmlText(trs("menu_structuredlist")) + "</p><ul>");
//        }
//        if (pagesRepSosa && displayRepSosa) {
//            out.println("<li><a href=\"" + sectionRepSosa.sectionLink + "\">" + htmlText(sectionRepSosa.sectionName) + "</a></li>");
//        }

        // Tools
        out.println("</ul><p>" + htmlText(trs("menu_tools")) + "</p><ul>");
//        if (pagesSearch) {
//            out.println("<li><a href=\"" + sectionSearch.sectionLink + "\">" + htmlText(sectionSearch.sectionName) + "</a></li>");
//        }
        out.println("</ul></div>");



        //
        // Right hand side of the index page
        //
        out.println("<div class=\"intro\">");

        // Dynamic message
        if (wp.param_dispMsg.equals("1")) {
            out.println(wp.param_dispMsg);
            out.println("<br /><br /><hr /><br />");
        }

        // Static message
        out.println(trs("text_sosa", new String[]{" <a href=\"" + getLink(stats.indiDeCujus) + "\">"
                    + getNameShort(stats.indiDeCujus) + "</a>", String.valueOf(stats.nbAncestors), String.valueOf(stats.nbGen)}) + "<br />");

        out.println(trs("text_old", new String[]{"<a href=\"" + getLink(stats.indiOlder) + "\">"
                    + getName(stats.indiOlder) + "</a>", stats.olderBirthDate == null ? trs("") : stats.olderBirthDate}) + "<br />");

        if (wp.param_dispStatAncestor.equals("1")) {
            out.println("<br />");
            if (stats.indiDeCujus == stats.longIndiG) {
                if (stats.indiDeCujus == stats.longIndiA) {
                    out.println(trs("text_longuest1") + "<br />");
                    out.println(trs("text_largest1", trs("text_largest1too")) + "<br />");
                } else {
                    out.println(trs("text_longuest1") + "<br />");
                    out.println(trs("text_largest2", new String[]{"<a href=\"" + getLink(stats.longIndiA) + "\">" + getNameShort(stats.longIndiA) + "</a>", String.valueOf(stats.nbAncestorsA)}) + "<br />");
                }
            } else {
                if (stats.indiDeCujus == stats.longIndiA) {
                    out.println(trs("text_largest1", trs("text_largest1too") + SPACE) + "<br />");
                    out.println(trs("text_longuest2", new String[]{"<a href=\"" + getLink(stats.longIndiG) + "\">" + getNameShort(stats.longIndiG) + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
                } else {
                    if (stats.longIndiG == stats.longIndiA) {
                        out.println(trs("text_longuest2", new String[]{"<a href=\"" + getLink(stats.longIndiG) + "\">" + getNameShort(stats.longIndiG) + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
                        out.println(trs("text_largest1", trs("text_largest1too") + SPACE) + "<br />");
                    } else {
                        out.println(trs("text_longuest2", new String[]{"<a href=\"" + getLink(stats.longIndiG) + "\">" + getNameShort(stats.longIndiG) + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
                        out.println(trs("text_largest2", new String[]{"<a href=\"" + getLink(stats.longIndiA) + "\">" + getNameShort(stats.longIndiA) + "</a>", String.valueOf(stats.nbAncestorsA)}) + "<br />");
                    }
                }
            }
        }
        out.println("<br /><hr /><br />");
        if (stats.place.length() > 0) {
            out.println(trs("text_place", stats.place) + "<br />");
        }
        out.println(trs("text_stats", new String[]{String.valueOf(stats.nbIndis), String.valueOf(stats.nbFams), String.valueOf(stats.nbNames), String.valueOf(stats.nbPlaces)}) + "<br />");
        out.println(trs("text_cousins", new String[]{String.valueOf(stats.nbAscendants), String.valueOf(stats.nbCousins), String.valueOf(stats.nbOthers)}) + "<br />");
        out.println(trs("text_family", new String[]{String.valueOf(stats.nbFams), String.valueOf(stats.nbFamsWithKids), String.valueOf(stats.avgKids)}) + "<br />");
        out.println("<br /><hr /><br />");

        out.println(trs("idxAuthor") + ":" + SPACE + wp.param_author + "<br />");
        out.println(trs("idxAddress") + ":" + SPACE + wp.param_address + "<br />" + trs("idxTel") + ":" + SPACE + wp.param_phone + "<br />");
        //
        if (wp.param_dispEmailButton.equals("1")) {
            out.println("<a href=\"mailto:" + wp.param_email + "?subject=" + trs("idx_email_subject") + "&amp;body=" + trs("idx_email_dear") + "%20" + wp.param_author + ",%0a%0a" + trs("idx_email_body") + " \">" + trs("idx_email_link") + "</a><br />");
        }
        out.println("<hr /><br />");
        //
        Calendar rightNow = Calendar.getInstance();
        out.println("<p class=\"legal\">" + trs("text_pages", new String[]{"<a href=\"http://www.arvernes.com/wiki/index.php/GenJ\">GenealogyJ</a>&nbsp;<a href=\"http://www.arvernes.com/wiki/index.php/Genj_-_Rapports_-_WebBook\">WebBook</a>", trs("version"), DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(rightNow.getTime())}) + "</p>");
        out.println("</div>");

        out.println("<div class=\"spacer\">" + SPACE + "</div>");

        out.println("</div>"); // conteneur

    }

    public String getLink(Indi indi) {
        return ""; // ((sectionDir.length() == 0) ? "" : sectionDir + SEP) + (String) pagesMap.get(indi.getId()) + "#" + indi.getId();
    }

    public String getName(Indi indi) {
        String name = indi.getFirstName() + " " + wh.getLastName(indi, DEFCHAR);
        if (wh.isPrivate(indi)) {
            name = "... ...";
        } else {
            // add sosa number
            String sosa = wh.getSosa(indi);
            if (sosa != null && sosa.length() != 0) {
                name += " (" + sosa + ")";
            }
        }
        return name;
    }

    public String getNameShort(Indi indi) {
        String name = indi.getFirstName().substring(0, 1) + ". " + wh.getLastName(indi, DEFCHAR);
        if (wh.isPrivate(indi)) {
            name = "... ...";
        }
        return name;
    }
}
