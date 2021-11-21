/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author frederic
 */
public class WebHome extends WebSection {

    private String version = "";

    /**
     * Constructor
     */
    public WebHome(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init("", "", "", "", 0, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // Preliminary build of individuals link for links from details to individuals
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        File dir = wh.createDir(wh.getDir().getAbsolutePath(), true);
        File file = wh.getFileForName(dir, indexFile);
        // Calculate statistics
        try (PrintWriter out = wh.getWriter(file, UTF8)) {
            // Calculate statistics
            GedcomStats stats = new GedcomStats(wp, wh);
            // HEAD
            printOpenHTML(out, "", this);
            // START OF PAGE ------------------
            exportIndex(out, stats);
            // END OF PAGE ------------------
            // TAIL
            printCloseHTML(out);
            // done
        }
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
        out.println("<div class=\"menu\">");


        // Individuals
        out.println("<p>" + trs("TXT_menu_individuals") + "</p>");
        out.println("<ul>");
        if (wb.sectionLastnames.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionLastnames.sectionLink + "\">" + htmlText(wb.sectionLastnames.sectionName) + "</a></li>");
        }
        if (wb.sectionIndividuals.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionIndividuals.sectionLink + "\">" + htmlText(wb.sectionIndividuals.sectionName) + "</a></li>");
        }
        if (wb.sectionIndividualsDetails.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionIndividualsDetails.sectionLink + "\">" + htmlText(wb.sectionIndividualsDetails.sectionName) + "</a></li>");
        }
        out.println("</ul>");


        // Documents
        if (wp.param_media_GeneSources.equals("1") || wp.param_media_GeneMedia.equals("1")) {
            out.println("<p>" + trs("TXT_menu_documents") + "</p>");
            out.println("<ul>");
            if (wb.sectionSources.toBeGenerated) {
                out.println("<li><a href=\"" + wb.sectionSources.sectionLink + "\">" + htmlText(wb.sectionSources.sectionName) + "</a></li>");
            }
            if (wb.sectionMedia.toBeGenerated) {
                out.println("<li><a href=\"" + wb.sectionMedia.sectionLink + "\">" + htmlText(wb.sectionMedia.sectionName) + "</a></li>");
            }
            out.println("</ul>");
        }

        // Locations
        if (wp.param_media_GeneMap.equals("1")) {
            out.println("<p>" + htmlText(trs("TXT_menu_locations")) + "</p>");
            out.println("<ul>");
            out.println("<li><a href=\"" + wb.sectionMap.sectionLink + "\">" + htmlText(wb.sectionMap.sectionName) + "</a></li>");
            out.println("<li><a href=\"" + wb.sectionCities.sectionLink + "\">" + htmlText(wb.sectionCities.sectionName) + "</a></li>");
            out.println("<li><a href=\"" + wb.sectionCitiesDetails.sectionLink + "\">" + htmlText(wb.sectionCitiesDetails.sectionName) + "</a></li>");
            out.println("</ul>");
        }

        // Dates
        out.println("<p>" + htmlText(trs("TXT_menu_days")) + "</p>");
        out.println("<ul>");
        out.println("<li><a href=\"" + wb.sectionDays.sectionLink + "\">" + htmlText(wb.sectionDays.sectionName) + "</a></li>");
        out.println("<li><a href=\"" + wb.sectionDaysDetails.sectionLink + "\">" + htmlText(wb.sectionDaysDetails.sectionName) + "</a></li>");
        out.println("</ul>");

        // Statistics
        if (true) {
            out.println("<p>" + htmlText(trs("TXT_menu_statistics")) + "</p>");
            out.println("<ul>");
            if (wb.sectionStatsFrequent.toBeGenerated) {
                out.println("<li><a href=\"" + wb.sectionStatsFrequent.sectionLink + "\">" + htmlText(wb.sectionStatsFrequent.sectionName) + "</a></li>");
            }
            if (wb.sectionStatsImplex.toBeGenerated) {
                out.println("<li><a href=\"" + wb.sectionStatsImplex.sectionLink + "\">" + htmlText(wb.sectionStatsImplex.sectionName) + "</a></li>");
            }
            out.println("</ul>");
        }

        // Structured lists
        if (wp.param_dispAncestors.equals("1")) {
            out.println("<p>" + htmlText(trs("TXT_menu_structuredlist")) + "</p>");
            out.println("<ul>");
            if (wb.sectionRepSosa.toBeGenerated) {
                out.println("<li><a href=\"" + wb.sectionRepSosa.sectionLink + "\">" + htmlText(wb.sectionRepSosa.sectionName) + "</a></li>");
            }
            out.println("</ul>");
        }

        // Tools
        out.println("<p>" + htmlText(trs("TXT_menu_tools")) + "</p>");
        out.println("<ul>");
        if (wb.sectionSearch.toBeGenerated) {
            out.println("<li><a href=\"" + wb.sectionSearch.sectionLink + "\">" + htmlText(wb.sectionSearch.sectionName) + "</a></li>");
        }
        out.println("</ul></div>");



        //
        // Right hand side of the index page
        //
        out.println("<div class=\"intro\">");

        // Dynamic message
        if (wp.param_dispMsg.equals("1")) {
            out.println(wp.param_message);
            out.println("<br /><br /><hr /><br />");
        }

        // Static message
        out.println(trs("TXT_text_sosa", wrapEntity(stats.indiDeCujus, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbAncestors, stats.nbGen) + "<br />");
        out.println(trs("TXT_text_old", wrapEntity(stats.indiOlder, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID)) + "<br />");

        if (wp.param_dispStatAncestor.equals("1")) {
            stats.calcLonguestLine(stats.indiDeCujus);
            out.println("<br />");
            if (stats.indiDeCujus == stats.longIndiG) {
                if (stats.indiDeCujus == stats.longIndiA) {
                    out.println(trs("TXT_text_longuest1") + "<br />");
                    out.println(trs("TXT_text_largest1") + "<br />");
                } else {
                    out.println(trs("TXT_text_longuest1") + "<br />");
                    out.println(trs("TXT_text_largest2", wrapEntity(stats.longIndiA, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbAncestorsA) + "<br />");
                }
            } else {
                if (stats.indiDeCujus == stats.longIndiA) {
                    out.println(trs("TXT_text_largest1") + "<br />");
                    out.println(trs("TXT_text_longuest2", wrapEntity(stats.longIndiG, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbGenG) + "<br />");
                } else {
                    if (stats.longIndiG == stats.longIndiA) {
                        out.println(trs("TXT_text_longuest2", wrapEntity(stats.longIndiG, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbGenG) + "<br />");
                        out.println(trs("TXT_text_largest1") + "<br />");
                    } else {
                        out.println(trs("TXT_text_longuest2", wrapEntity(stats.longIndiG, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbGenG) + "<br />");
                        out.println(trs("TXT_text_largest2", wrapEntity(stats.longIndiA, DT_NOBREAK, DT_FIRSTLAST, DT_ICON, DT_LINK, DT_SOSA, DT_NOID), stats.nbAncestorsA) + "<br />");
                    }
                }
            }
        }
        out.println("<br /><hr /><br />");
        if (stats.place.length() > 0) {
            out.println(trs("TXT_text_place", stats.place) + "<br />");
        }
        out.println(trs("TXT_text_stats", stats.nbIndis, stats.nbFams, stats.nbNames, stats.nbPlaces) + "<br />");
        out.println(trs("TXT_text_cousins", stats.nbAscendants, stats.nbCousins, stats.nbOthers) + "<br />");
        out.println(trs("TXT_text_family", stats.nbFams, stats.nbFamsWithKids, stats.avgKids) + "<br />");
        out.println("<br /><hr /><br />");

        // Author
        out.println(trs("WebBookVisualPanel1.jLabel3.text") + ":" + SPACE + wp.param_author + "<br />");
        if (!wp.param_address.equals("-")) {
            out.println(trs("WebBookVisualPanel1.jLabel4.text") + ":" + SPACE + wp.param_address + "<br />");
        }
        if (!wp.param_phone.equals("-")) {
            out.println(trs("WebBookVisualPanel1.jLabel5.text") + ":" + SPACE + wp.param_phone + "<br />");
        }
        if (!wp.param_email.equals("-")) {
            out.println("<a href=\"mailto:" + wp.param_email + "?subject=" + trs("TXT_idx_email_subject") + "&amp;body=" + trs("TXT_idx_email_dear")
                    + "%20" + wp.param_author + ",%0a%0a" + trs("TXT_idx_email_body") + " \">" + trs("TXT_idx_email_link") + "</a><br />");
        }
        out.println("<br /><hr /><br />");

        // Footer
        out.println("<p class=\"legal\">" + trs("TXT_text_pages", "<a href=\"https://www.ancestris.org\" title=\"" + version + "\" >Ancestris WebBook</a>", new Date()) + "</p>");
        out.println("</div>");

        out.println("<div class=\"spacer\">" + SPACE + "</div>");



        // conteneur
        out.println("</div>");

    }

    public void setVersion(String version) {
        this.version = version;
    }
}
