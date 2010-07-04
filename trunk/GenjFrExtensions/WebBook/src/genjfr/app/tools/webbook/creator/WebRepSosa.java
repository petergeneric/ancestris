/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Source;

import genjfr.app.App;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import genjfr.app.tools.webbook.WebBookVisualPanel4;

import java.io.File;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebRepSosa extends WebSection {

    private String indi2srcDir = "";
    private Map<Integer, String> linkForGen = new TreeMap<Integer, String>();
    private boolean maxGenReached = false;
    String[] events = {"BIRT", "CHR", "MARR", "DEAT", "BURI", "OCCU", "RESI"};
    String[] symbols = new String[7];

    /**
     * Constructor
     */
    public WebRepSosa(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_RepSosa"), "repsosa", "repsosa_", formatFromSize(wh.getNbIndis()), ".html", 1, sizeIndiSection/2);
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);

        // Generate links to the 2 sections below
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        if (wb.sectionSources != null) {
            sourcePage = wb.sectionSources.getPagesMap();
            indi2srcDir = buildLinkShort(this, wb.sectionSources);
        }

        initVariables();
        Indi indi = wh.getIndiDeCujus(wp.param_decujus);
        exportData(indi, dir, wh.getAncestorsList(indi));

    }

    /** Initialises variables  */
    void initVariables() {
        // Assign symbols (used only if parameter showSymbols is changed to true in individualDetails
        symbols = new String[]{
                    NbPreferences.forModule(App.class).get("symbolBirth", ""),
                    NbPreferences.forModule(App.class).get("symbolBapm", ""),
                    NbPreferences.forModule(App.class).get("symbolMarr", ""),
                    NbPreferences.forModule(App.class).get("symbolDeat", ""),
                    NbPreferences.forModule(App.class).get("symbolBuri", ""),
                    NbPreferences.forModule(App.class).get("symbolOccu", ""),
                    NbPreferences.forModule(App.class).get("symbolResi", "")};
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
        for (Iterator it = ancestors.iterator(); it.hasNext();) {
            Ancestor ancestor = (Ancestor) it.next();
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
            linkForGen.put(Integer.valueOf(ancestor.gen), sectionPrefix + String.format(formatNbrs, cptPage + 1) + sectionSuffix);
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
        SortedSet sources = new TreeSet(wh.sortSources);       // Ensure list is sorted with no duplicates

        for (Iterator it = ancestors.iterator(); it.hasNext();) {
            Ancestor ancestor = (Ancestor) it.next();
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
        doc.println("<h1>" + "<a name=\"top\">" + SPACE + "</a>" + htmlText(trs("RepSosaOptions.title", rootIndi.getName())) + "</h1>");
        exportGenLinks(doc);
        exportLinks(doc, sectionPrefix + String.format(formatNbrs, Math.min(cptPage + 1, maxPage)) + sectionSuffix, 1, Math.max(1, cptPage - 1), cptPage == maxPage ? maxPage : cptPage + 1, maxPage);
        return doc;
    }

    void openGeneration(int gen, PrintWriter doc) {
        doc.println("<div class=\"sosareport\">");
        doc.println("<a name=\"gen-" + gen + "\"></a>");
        doc.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("RepSosaOptions.generation") + " " + gen) + "</span></p>");
    }

    void formatIndi(Indi indi, int gen, int sosa, PrintWriter doc) {
        // Print individual
        doc.println("<p class=\"sosacolumn1\"><span class=\"gras\">" + sosa + "</span>" + SPACE + "-" + SPACE);
        wrapName(doc, indi);
        doc.println("</p>");
        // Get and write properies
        doc.println("<div class=\"sosacolumn2\">");
        writeEvents(indi, events, symbols, doc);
        doc.println(SPACE + "</div>");
    }

    void closeGeneration(int gen, SortedSet sources, PrintWriter doc, int cptPage, int maxPage) {
        doc.println("<div class=\"spacer\">" + SPACE + "</div>");
        doc.println("</div>");
        if (!wp.param_ancestorSource.equals(NbBundle.getMessage(WebBookVisualPanel4.class, "sourceTypeAncestor.type1")) && !sources.isEmpty()) {
            doc.println("<div class=\"sosasources\">");
            doc.println("<span class=\"undl\">" + htmlText(trs("RepSosaOptions.sourceList", gen)) + "</span><br />	");
            writeSourceList(sources, doc);
            doc.println("</div>");
        }
        exportLinks(doc, sectionPrefix + String.format(formatNbrs, Math.min(cptPage + 1, maxPage)) + sectionSuffix, 1, Math.max(1, cptPage - 1), cptPage, maxPage);

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
    void writeEvents(Indi indi, String events[], String evSymbols[], PrintWriter doc) {

        // Get list of events for that individual
        List<String> listEvents = wb.sectionIndividualsDetails.getNameDetails(indi, events, evSymbols);

        // Get list of events for all his/her families
        Fam[] families = indi.getFamiliesWhereSpouse();
        for (int i = 0; families != null && i < families.length; i++) {
            Fam family = families[i];
            listEvents.addAll(wb.sectionIndividualsDetails.getNameDetails(family, events, evSymbols));
        }
        Collections.sort(listEvents);

        // Display events
        for (Iterator s = listEvents.iterator(); s.hasNext();) {
            String event = (String) s.next();   // date . description . source id
            String[] eventBits = event.split("\\|", -1);
            String link = wrapSource(eventBits[2]);
            if (eventBits[2].length() != 0 && wp.param_media_GeneSources.equals("1")) {
                doc.println(eventBits[1].trim() + SPACE + SPACE + "<a href=\"" + link + "\"><img src=\"" + themeDir + "src.gif\" alt=\"" + eventBits[2] + "\" title=\"" + eventBits[2] + "\"/>" + SPACE + "(" + eventBits[2] + ")</a><br />");
            } else {
                doc.println(eventBits[1].trim() + "<br />");
            }
        }
        doc.println("<br />");

        return;
    }

    /**
     * Write sources
     */
    void writeSourceList(SortedSet sources, PrintWriter doc) {
        // display sources
        for (Iterator s = sources.iterator(); s.hasNext();) {
            Source src = (Source) s.next();
            // display source and title
            doc.println("<br /><a href=\"" + wrapSource(src.getId()) + "\">(" + src.getId() + ")</a>" + SPACE + htmlText(src.getTitle()) + "<br />");
            // display text
            if (wp.param_ancestorSource.equals(NbBundle.getMessage(WebBookVisualPanel4.class, "sourceTypeAncestor.type3")) && src.getText().length() != 0) {
                doc.println("<span class=\"sosatext\">" + htmlText(src.getText()) + "</span><br />");
            }
        }
        doc.println("<br />");
        return;
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
        for (Iterator it = linkForGen.keySet().iterator(); it.hasNext();) {
            Integer gen = (Integer) it.next();
            out.println("<a href=\"" + linkForGen.get(gen) + "#gen-" + gen + "\">" + gen + "</a>" + SPACE + SPACE);
        }
        out.println("</p>");
    }

    /**
     * Print name with link
     */
    private String wrapSource(String src) {
        //
        String link = "";
        String sourceFile = (src == null) ? "" : ((sourcePage == null) ? "" : sourcePage.get(src));
        if (src != null) {
            link = indi2srcDir + sourceFile + '#' + src;
        }
        return link;
    }
}

