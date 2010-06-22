/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Gedcom;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.PrintWriter;

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

        // HEAD
        printOpenHTML(out, "", this);

        // START OF PAGE ------------------
        trs("exporting", new String[]{file.getName(), dir.toString()});
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
//    private void exportIndex(PrintWriter out) {
//
//        Gedcom gedcom = wh.gedcom;
//
//        stats.update(gedcom, dispLonguest);
//
//        out.println("<hr />");
//        out.println("<div class=\"contindex\">");      // conteneur
//
//        //
//        // MENU side of the index page
//        //
//        out.println("<div class=\"menu\">");           /* ***new section*** */
//
//        // Individuals
//        out.println("<p>" + trs("menu_individuals") + "</p><ul>");
//        if (pagesLastnames) {
//            out.println("<li><a href=\"" + wb.sectionLastnames.sectionLink + "\">" + wh.htmlText(sectionLastnames.sectionName) + "</a></li>");
//        }
//        if (pagesIndividuals) {
//            out.println("<li><a href=\"" + wb.sectionIndividuals.sectionLink + "\">" + wh.htmlText(sectionIndividuals.sectionName) + "</a></li>");
//        }
//        if (pagesIndividualsDetails) {
//            out.println("<li><a href=\"" + wb.sectionIndividualsDetails.sectionLink + "\">" + wh.htmlText(sectionIndividualsDetails.sectionName) + "</a></li>");
//        }
//
//        // Documents
//        if (displaySourceSec || displayMediaSec) {
//            out.println("</ul><p>" + trs("menu_documents") + "</p><ul>");
//        }
//        if (pagesSources && displaySourceSec) {
//            out.println("<li><a href=\"" + sectionSources.sectionLink + "\">" + wh.htmlText(sectionSources.sectionName) + "</a></li>");
//        }
//        if (pagesMedia && displayMediaSec) {
//            out.println("<li><a href=\"" + sectionMedia.sectionLink + "\">" + wh.htmlText(sectionMedia.sectionName) + "</a></li>");
//        }
//
//        // Locations
//        out.println("</ul><p>" + wh.htmlText(trs("menu_locations")) + "</p><ul>");
//        out.println("<li><a href=\"" + sectionMap.sectionLink + "\">" + wh.htmlText(sectionMap.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionCities.sectionLink + "\">" + wh.htmlText(sectionCities.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionCitiesDetails.sectionLink + "\">" + wh.htmlText(sectionCitiesDetails.sectionName) + "</a></li>");
//
//        // Dates
//        out.println("</ul><p>" + wh.htmlText(trs("menu_days")) + "</p><ul>");
//        out.println("<li><a href=\"" + sectionDays.sectionLink + "\">" + wh.htmlText(sectionDays.sectionName) + "</a></li>");
//        out.println("<li><a href=\"" + sectionDaysDetails.sectionLink + "\">" + wh.htmlText(sectionDaysDetails.sectionName) + "</a></li>");
//
//        // Statistics
//        out.println("</ul><p>" + wh.htmlText(trs("menu_statistics")) + "</p><ul>");
//        if (pagesStatsFrequent) {
//            out.println("<li><a href=\"" + sectionStatsFrequent.sectionLink + "\">" + wh.htmlText(sectionStatsFrequent.sectionName) + "</a></li>");
//        }
//        if (pagesStatsImplex) {
//            out.println("<li><a href=\"" + sectionStatsImplex.sectionLink + "\">" + wh.htmlText(sectionStatsImplex.sectionName) + "</a></li>");
//        }
//
//        // Structured lists
//        if (displayRepSosa) {
//            out.println("</ul><p>" + wh.htmlText(trs("menu_structuredlist")) + "</p><ul>");
//        }
//        if (pagesRepSosa && displayRepSosa) {
//            out.println("<li><a href=\"" + sectionRepSosa.sectionLink + "\">" + wh.htmlText(sectionRepSosa.sectionName) + "</a></li>");
//        }
//        if (false) {
//            out.println("<li>" + wh.htmlText("Flash list") + "</li>");
//        }
//
//        // Tools
//        out.println("</ul><p>" + wh.htmlText(trs("menu_tools")) + "</p><ul>");
//        if (pagesSearch) {
//            out.println("<li><a href=\"" + sectionSearch.sectionLink + "\">" + wh.htmlText(sectionSearch.sectionName) + "</a></li>");
//        }
//        out.println("</ul></div>");
//
//
//
//        //
//        // Right hand side of the index page
//        //
//        out.println("<div class=\"intro\">");
//
//        // Dynamic message
//        if (dispMessage) {
//            out.println(readFile(msgFileAbsolute));
//            out.println("<br /><br /><hr /><br />");
//        }
//
//        // Static message
//        out.println(trs("text_sosa", new String[]{" <a href=\"" + stats.deCujusLink + "\">" + stats.deCujusName + "</a>", String.valueOf(stats.nbAncestors), String.valueOf(stats.nbGen)}) + "<br />");
//        out.println(trs("text_old", new String[]{"<a href=\"" + stats.olderLink + "\">" + stats.olderName + "</a>", stats.olderBirthDate}) + "<br />");
//        if (dispLonguest) {
//            out.println("<br />");
//            if (stats.indiDeCujus == stats.longIndiG) {
//                if (stats.indiDeCujus == stats.longIndiA) {
//                    out.println(trs("text_longuest1") + "<br />");
//                    out.println(trs("text_largest1", trs("text_largest1too")) + "<br />");
//                } else {
//                    out.println(trs("text_longuest1") + "<br />");
//                    out.println(trs("text_largest2", new String[]{"<a href=\"" + stats.longIndiALink + "\">" + stats.longIndiAName + "</a>", String.valueOf(stats.nbAncestorsA)}) + "<br />");
//                }
//            } else {
//                if (stats.indiDeCujus == stats.longIndiA) {
//                    out.println(trs("text_largest1", trs("text_largest1too") + SPACE) + "<br />");
//                    out.println(trs("text_longuest2", new String[]{"<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
//                } else {
//                    if (stats.longIndiG == stats.longIndiA) {
//                        out.println(trs("text_longuest2", new String[]{"<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
//                        out.println(trs("text_largest1", trs("text_largest1too") + SPACE) + "<br />");
//                    } else {
//                        out.println(trs("text_longuest2", new String[]{"<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)}) + "<br />");
//                        out.println(trs("text_largest2", new String[]{"<a href=\"" + stats.longIndiALink + "\">" + stats.longIndiAName + "</a>", String.valueOf(stats.nbAncestorsA)}) + "<br />");
//                    }
//                }
//            }
//        }
//        out.println("<br /><hr /><br />");
//        if (stats.place.length() > 0) {
//            out.println(trs("text_place", stats.place) + "<br />");
//        }
//        out.println(trs("text_stats", new String[]{String.valueOf(stats.nbIndis), String.valueOf(stats.nbFams), String.valueOf(stats.nbNames), String.valueOf(stats.nbPlaces)}) + "<br />");
//        out.println(trs("text_cousins", new String[]{String.valueOf(stats.nbAscendants), String.valueOf(stats.nbCousins), String.valueOf(stats.nbOthers)}) + "<br />");
//        out.println(trs("text_family", new String[]{String.valueOf(stats.nbFams), String.valueOf(stats.nbFamsWithKids), String.valueOf(stats.avgKids)}) + "<br />");
//        out.println("<br /><hr /><br />");
//
//        out.println(trs("idxAuthor") + ":" + SPACE + idxAuthor + "<br />");
//        out.println(trs("idxAddress") + ":" + SPACE + idxAddress + "<br />" + trs("idxTel") + ":" + SPACE + idxTel + "<br />");
//        if (displayEmail) {
//            out.println("<a href=\"mailto:" + idxEmail + "?subject=" + trs("idx_email_subject") + "&amp;body=" + trs("idx_email_dear") + "%20" + idxAuthor + ",%0a%0a" + trs("idx_email_body") + " \">" + trs("idx_email_link") + "</a><br />");
//        }
//        out.println("<hr /><br />");
//
//        Calendar rightNow = Calendar.getInstance();
//        out.println("<p class=\"legal\">" + trs("text_pages", new String[]{"<a href=\"http://www.arvernes.com/wiki/index.php/GenJ\">GenealogyJ</a>&nbsp;<a href=\"http://www.arvernes.com/wiki/index.php/Genj_-_Rapports_-_WebBook\">WebBook</a>", trs("version"), DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(rightNow.getTime())}) + "</p>");
//        out.println("</div>");
//
//        out.println("<div class=\"spacer\">" + SPACE + "</div>");
//
//        out.println("</div>"); // conteneur
//
//        // TAIL
//        wh.printCloseHTML(out);
//
//        // done
//        out.close();
//    }
}
