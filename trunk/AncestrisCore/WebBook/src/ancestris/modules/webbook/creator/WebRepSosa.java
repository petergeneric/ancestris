/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import genj.gedcom.Indi;
import genj.gedcom.Source;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import ancestris.modules.webbook.WebBookVisualPanel4;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openide.util.NbBundle;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebRepSosa extends WebSection {

    private String indi2srcDir = "";
    private String indi2mediaDir = "";
    private Map<Integer, String> linkForGen = new TreeMap<>();
    private boolean maxGenReached = false;
    String[] events = {"BIRT", "CHR", "MARR", "DEAT", "BURI", "OCCU", "RESI"};

    /**
     * Constructor
     */
    public WebRepSosa(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_RepSosa"), "repsosa", "repsosa_", formatFromSize(wh.getNbIndis()), 1, sizeIndiSection/2);
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        initEvents();

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);

        // Generate links to the 2 sections below
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Preliminary build of sources link for links from details to sources
        if (wb.sectionSources != null && wb.sectionSources.toBeGenerated) {
            sourcePage = wb.sectionSources.getPagesMap();
            indi2srcDir = buildLinkShort(this, wb.sectionSources);
        }
        if (wb.sectionMedia != null && wb.sectionMedia.toBeGenerated) {
            indi2mediaDir = buildLinkShort(this, wb.sectionMedia);
        }

        Indi indi = wh.getIndiDeCujus(wp.param_decujus);
        exportData(indi, dir, wh.getAncestorsList(indi));

    }

    /**
     * Exports data for page
     */
    @SuppressWarnings("unchecked")
    private void exportData(Indi rootIndi, File dir, List<Ancestor> ancestors) {

        // Calculate max pages

        int cptPage = 0;
        int maxPage = 0;
        int cptIndi = 0;
        int gen = 0;

        linkForGen.clear();
        for (Ancestor ancestor : ancestors) {
            if (ancestor.gen < Integer.valueOf(wp.param_ancestorMinGen)) {
                continue;
            }
            if (ancestor.gen > Integer.valueOf(wp.param_ancestorMaxGen)) {
                maxGenReached = true;
                break;
            }
            cptIndi++;
            if (ancestor.gen != gen) {
                if (cptIndi > nbPerPage) {
                    cptPage++;
                    cptIndi = 1;
                }
            }
            linkForGen.put(ancestor.gen, sectionPrefix + String.format(formatNbrs, cptPage + 1) + sectionSuffix);
            gen = ancestor.gen;
        }
        maxPage = cptPage + 1;

        // Print pages
        PrintWriter doc = null;
        boolean newPage = true;
        boolean pageInProgress = false;
        boolean genInProgress = false;
        cptPage = 0;
        cptIndi = 0;
        gen = 0;
        SortedSet<Source> sources = new TreeSet<>(wh.sortSources);       // Ensure list is sorted with no duplicates
        
        for (Ancestor ancestor : ancestors) {
            if (ancestor.gen < Integer.valueOf(wp.param_ancestorMinGen) || ancestor.gen > Integer.valueOf(wp.param_ancestorMaxGen)) {
                continue;
            }

            // Is it a new generation?
            if (gen != ancestor.gen) {
                if (genInProgress) {
                    closeGeneration(gen, sources, doc, cptPage, maxPage);
                    sources.clear();
                    if (cptIndi > nbPerPage) {
                        newPage = true;
                    }
                }
                // Is it a new page?
                if (newPage) {
                    cptIndi = 1;
                    newPage = false;
                    if (pageInProgress) {
                        closePage(doc, cptPage);
                    }
                    cptPage++;
                    doc = openPage(rootIndi, dir, cptPage, maxPage);
                    if (doc == null) {
                        pageInProgress = false;
                        genInProgress = false;
                        break;
                    }
                    pageInProgress = true;
                }
                gen = ancestor.gen;
                openGeneration(gen, doc);
                genInProgress = true;
            }

            // Print individual and store sources
            formatIndi(ancestor.indi, ancestor.gen, ancestor.sosa, doc);
            sources.addAll(wh.getSources(ancestor.indi));
            cptIndi++;
        }

        // done, no more recursion
        if (genInProgress) {
            closeGeneration(gen, sources, doc, cptPage, maxPage);
        }
        if (pageInProgress) {
            closePage(doc, cptPage);
        }

    }

    /**
     * Display functions
     */
    PrintWriter openPage(Indi rootIndi, File dir, int cptPage, int maxPage) {

        String fileStr = sectionPrefix + String.format(formatNbrs, cptPage) + sectionSuffix;
        File file = wh.getFileForName(dir, fileStr);
        PrintWriter doc = wh.getWriter(file, UTF8);
        if (doc == null) {
            return null;
        }
        printOpenHTML(doc, null, this);
        doc.println("<div class=\"title\">" + "<a id=\"top\">" + SPACE + "</a>" + htmlText(trs("RepSosaOptions.title")) + SPACE + wrapName(rootIndi, DT_FIRSTLAST, DT_NOLINK, DT_SOSA, DT_NOID) + "</div>");
        exportGenLinks(doc);
        exportLinks(doc, sectionPrefix + String.format(formatNbrs, Math.min(cptPage + 1, maxPage)) + sectionSuffix, 1, Math.max(1, cptPage - 1), cptPage == maxPage ? maxPage : cptPage + 1, maxPage);
        return doc;
    }

    void openGeneration(int gen, PrintWriter doc) {
        doc.println("<div class=\"sosareport\">");
        doc.println("<a id=\"gen-" + gen + "\"></a>");
        doc.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("RepSosaOptions.generation") + " " + gen) + "</span></p>");
    }

    void formatIndi(Indi indi, int gen, BigInteger sosa, PrintWriter doc) {
        // Print individual
        doc.println("<p class=\"sosacolumn1\"><span class=\"gras\">" + sosa + "</span>" + SPACE + "-</p>");
        doc.println("<p class=\"sosacolumn2\">");
        doc.println(wrapEntity(indi, DT_BREAK, DT_LASTFIRST, DT_ICON, DT_LINK, DT_SOSA, DT_ID));
        doc.println("</p>");
        // Get and write properies
        doc.println("<div class=\"sosacolumn3\">");
        writeEvents(indi, events, doc);
        doc.println(SPACE + "</div>");
    }

    void closeGeneration(int gen, SortedSet<Source> sources, PrintWriter doc, int cptPage, int maxPage) {
        doc.println("<div class=\"spacer\">" + SPACE + "</div>");
        doc.println("</div>");
        if (!wp.param_ancestorSource.equals(NbBundle.getMessage(WebBookVisualPanel4.class, "sourceTypeAncestor.type1")) && !sources.isEmpty()) {
            doc.println("<div class=\"sosasources\">");
            doc.println("<span class=\"undl\">" + htmlText(trs("RepSosaOptions.sourceList", gen)) + "</span><br />	");
            writeSourceList(sources, doc);
            doc.println("</div>");
        }
        exportLinks(doc, sectionPrefix + String.format(formatNbrs, Math.min(cptPage + 1, maxPage)) + sectionSuffix, 1, Math.max(1, cptPage - 1), cptPage == maxPage ? maxPage : cptPage + 1, maxPage);

    }

    void closePage(PrintWriter doc, int cptPage) {
        printCloseHTML(doc);
        String fileStr = sectionPrefix + String.format(formatNbrs, cptPage) + sectionSuffix;
        wh.log.write(fileStr + trs("EXEC_DONE"));
        doc.close();
    }

    /**
     * writeEvents
     */
    void writeEvents(Indi indi, String events[], PrintWriter doc) {

        doc.println(wrapEvents(indi, true, indi2srcDir, indi2mediaDir));
        doc.println("<br />");
    }

    /**
     * Write sources
     */
    void writeSourceList(SortedSet<Source> sources, PrintWriter doc) {
        // display sources
        for (Source src : sources) {
            // display source and title
            doc.println("<br /><a href=\"" + linkSource(src.getId()) + "\">(" + src.getId() + ")</a>");
            doc.println(SPACE);
            doc.println(wrapString(src, src.getTitle()));
            doc.println("<br />");
            // display text
            if (wp.param_ancestorSource.equals(NbBundle.getMessage(WebBookVisualPanel4.class, "sourceTypeAncestor.type3")) && src.getText().length() != 0) {
                doc.println("<span class=\"sosatext\">");
                doc.println(wrapString(src, src.getText()));
                doc.println("</span><br />");
            }
        }
        doc.println("<br />");
    }

    /**
     * Exports gen links bar
     */
    private void exportGenLinks(PrintWriter out) {
        out.println("<p class=\"letters\"><br />");
        out.println(htmlText(trs("RepSosaOptions.generations")) + "<br />");
        if (maxGenReached) {
            out.println("<small>" + htmlText(trs("RepSosaOptions.limited", wp.param_ancestorMaxGen)) + "</small><br />");
        } else {
            out.println("<small>" + htmlText(trs("RepSosaOptions.unlimited")) + "</small><br />");
        }
        for (Integer gen : linkForGen.keySet()) {
            out.println("<a href=\"" + linkForGen.get(gen) + "#gen-" + gen + "\">" + gen + "</a>" + SPACE + SPACE);
        }
        out.println("</p>");
    }

    /**
     * Print name with link
     */
    private String linkSource(String src) {
        //
        String link = "";
        String sourceFile = (src == null) ? "" : ((sourcePage == null) ? "" : sourcePage.get(src));
        if (src != null) {
            link = indi2srcDir + sourceFile + '#' + src;
        }
        return link;
    }
}

