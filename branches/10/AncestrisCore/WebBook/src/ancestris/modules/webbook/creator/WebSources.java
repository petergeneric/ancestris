/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import ancestris.modules.webbook.WebBookVisualPanel3;
import genj.gedcom.Media;
import genj.gedcom.PropertyMedia;
import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebSources extends WebSection {

    private final static TagPath PATH2ABBR = new TagPath("SOUR:ABBR");
    private final static TagPath PATH2AUTH = new TagPath("SOUR:AUTH");
    private final static TagPath PATH2TEXT = new TagPath("SOUR:TEXT");
    private final static TagPath PATH2DATATEXT = new TagPath("SOUR:DATA:TEXT");
    private final static TagPath PATH2REPO = new TagPath("SOUR:REPO");
    private final static TagPath PATH2EVEN = new TagPath("SOUR:DATA:EVEN");
    private final static TagPath PATH2NOTE = new TagPath("SOUR:NOTE");
    private final static TagPath PATH2XREF = new TagPath("SOUR:XREF");

    /**
     * Constructor
     */
    public WebSources(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        if (!toBeGenerated) {
            return;
        }
        init(trs("TXT_Sources"), "sources", "sources_", formatFromSize(wh.getNbIndis()), 0, 15);
        calcPages();
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        if (!toBeGenerated) {
            return;
        }

        // Preliminary build of individualsdetails link for links from sources to details
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

        wh.log.write(POPUP + trs("EXEC_DONE"));

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        List<Source> sources = wh.getSources(wh.gedcom);
        // Go through items to display and produce corresponding pages
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String sourcefile = "";
        int cpt = 0;
        int nbSources = sources.size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbSources / nbPerPage) + 1;

        // export first page (page '0' as the index page)
        sourcefile = sectionPrefix + String.format(formatNbrs, 0) + sectionSuffix;
        fileStr = sourcefile;
        file = wh.getFileForName(dir, sourcefile);
        out = wh.getWriter(file, UTF8);
        printOpenHTML(out, "TXT_SourcesIndex", this);
        out.println("<br /><br />");
        exportLinks(out, sourcefile, 0, 0, 1, lastPage);
        out.println("<br /><br />");
        String src_title = "";
        String src_abbr = "";
        String src_author = "";
        String description = "";
        for (Iterator <Source>it = sources.iterator(); it.hasNext();) {
            Source src = it.next();
            src_title = src.getTitle();
            if ((src_title == null) || (src_title.length() == 0)) {
                src_title = src.toString();
            }
            src_abbr = (src.getProperty(PATH2ABBR) == null) ? "" : src.getProperty(PATH2ABBR).getValue();
            if ((src_abbr == null) || (src_abbr.length() == 0)) {
                src_abbr = "";
            } else {
                src_title += "   -   ";
            }
            src_author = (src.getProperty(PATH2AUTH) == null) ? "" : src.getProperty(PATH2AUTH).getValue();
            if ((src_author == null) || (src_author.length() == 0)) {
                src_author = "";
            } else {
                src_abbr += "   -   ";
            }
            description = htmlText(src_title + src_abbr + src_author);
            out.println("<div class=\"conteneur\">");
            out.println("<p class=\"srclist\">" + src.getId() + "</p>");
            out.println("<p class=\"srclisttitle\"><a href=\"" + getPageForSource(src) + '#' + src.getId() + "\">" + description + "</a></p><br />");
            out.println("<div class=\"spacer\">" + SPACE + "</div>");
            out.println("</div>");
        }
        exportLinks(out, sourcefile, 0, 0, 1, lastPage);
        printCloseHTML(out);
        out.close();
        wh.log.write(sourcefile + trs("EXEC_DONE"));

        // export detailed pages
        cpt = 0;
        out = null;
        for (Iterator<Source> it = sources.iterator(); it.hasNext();) {
            Source src = it.next();
            cpt++;
            currentPage = (cpt / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            sourcefile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (fileStr.compareTo(sourcefile) != 0) {
                if (out != null) {
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 0, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    out.close();
                    wh.log.write(fileStr + trs("EXEC_DONE"));
                }
                fileStr = sourcefile;
                file = wh.getFileForName(dir, sourcefile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Sources", this);
                includePopupScript(out);
            }
            if ((cpt == 1) || ((cpt / 5) * 5) == cpt) {
                exportLinks(out, sourcefile, 0, previousPage, nextPage, lastPage);
            }
            exportSectionDetails(out, src, dir);
            // .. next individual
        }
        if (out != null) {
            exportLinks(out, sourcefile, 0, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }

    }

    /**
     * Exports section details
     */
    @SuppressWarnings("unchecked")
    private void exportSectionDetails(PrintWriter out, Source src, File dir) {

        /**
         * Sources generally have the following structure
         *
         * SOUR
        o TITL Title of the source
        o ABBR Abreviation of source or related organisation
        o AUTH Author of the source
        o OBJE Multimedia (picture of the source content)
        o TEXT Text of the content
        o DATA Data described in the source, generally the events that it describes
        + EVEN BIRT, MARR, DEAT
        # DATE of event
        # PLAC of event
        o REPO Storage of the source
        + CALN Call number
        # MEDI type of media
        o NOTE Note about the source
        o XREF Associated entities
         */
        // Initialises anchor, list of files and list of related entities and determine if source is private along the way
        String anchor = src.getId();
        List<PropertyFile> files = new ArrayList<PropertyFile>();              // Files of sources in SOUR and related entities for that SOUR
        
        // First, put all files attached to SOUR entity
        List<PropertyFile> propsToAdd = new ArrayList<PropertyFile>();
        for (Property obje : src.getAllProperties("OBJE")) {
            if (obje != null) {
                if (obje instanceof PropertyMedia) {
                    Media media = (Media) ((PropertyMedia) obje).getTargetEntity();
                    propsToAdd = media.getProperties(PropertyFile.class);
                } else {
                    propsToAdd = obje.getProperties(PropertyFile.class);
                }
                for (PropertyFile pFile : propsToAdd) {
                    files.add(pFile);
                }
            }
        }

        Property[] props = src.getProperties(PATH2XREF);
        List<Entity> list = new ArrayList<Entity>();                           // List of related entities
        if (props != null && props.length > 0) {
            for (int i = 0; i < props.length; i++) {
                PropertyXRef p = (PropertyXRef) props[i];
                Entity target = p.getTargetEntity();
                if (list.contains(target)) {
                    continue;
                }
                if ((target instanceof Indi) || (target instanceof Fam)) {
                    list.add(target);
                }
            }
        }
        Collections.sort(list, sortEntities);

        // Starts the output on the page
        out.println("<p><a name=\"" + anchor + "\"></a>" + SPACE + "</p>");

        String src_title = src.getTitle();
        if ((src_title == null) || (src_title.length() == 0)) {
            src_title = src.toString();
        }

        // Opens the block
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"srcdecal\"><span class=\"gras\">" + src.getId() + SPACE + htmlText(src_title) + "</span></p>");


        // Prepare style based on whether there will be images or not
        out.println("<p class=\"srcitems\">");
        Property prop = null;

        // Print all properties
        prop = src.getProperty(PATH2ABBR);
        printProperty(out, prop, "", "srcitems2");

        prop = src.getProperty(PATH2AUTH);
        printProperty(out, prop, "", "srcitems2");

        prop = src.getProperty(PATH2EVEN);
        if (prop != null) {
            Property pdate = prop.getProperty("DATE");
            String date = "";
            if (pdate != null) {
                date = pdate.getDisplayValue();
            }
            Property pplace = prop.getProperty("PLAC");
            String place = "";
            if (pplace != null) {
                place = pplace.getDisplayValue().replaceAll(",", " ");
            }
            printProperty(out, prop, ", " + date + " - " + place, "srcitems2");
        }

        prop = src.getProperty(PATH2REPO);
        printProperty(out, prop, "", "srcitems2");

        prop = src.getProperty(PATH2TEXT);
        printProperty(out, prop, "", "srcitems3");

        prop = src.getProperty(PATH2DATATEXT);
        printProperty(out, prop, "", "srcitems3");

        prop = src.getProperty(PATH2NOTE);
        printProperty(out, prop, "", "srcitems2");

        // Print pictures of SOUR entity
        if ((wp.param_media_GeneSources.equals("1")) && !files.isEmpty()) {
            out.println("<span class=\"srcitems1\">" + htmlText(trs("src_media")) + ":</span><span class=\"srcimage\">");
            for (PropertyFile file : files) {
                out.println("<span class=\"srcimage1\">");
                out.println(wrapMedia(dir, file, "", true, !wp.param_media_CopySources.equals("1"), true, true, "", "", true, "OBJE:NOTE", "tooltipL"));
                out.println("</span><span class=\"srcimage2\">" + SPACE + "</span>");
            }
            out.println("</span>");
            files.clear();
        }

        if (!list.isEmpty()) {
            List<PropertyFile> mediasOfEntity = new ArrayList<PropertyFile>();     // temp list
            out.println("<span class=\"srcitems1\">" + htmlText(trs("src_associations")) + ":</span>");
            out.println("<span class=\"srcitems2\">");
            for (Iterator <Entity>it = list.iterator(); it.hasNext();) {
                Entity target = it.next();
                out.println(wrapEntity(target));
                out.println("<br />");
                if (!(wp.param_media_DisplaySources.equals(NbBundle.getMessage(WebBookVisualPanel3.class, "sourceType.type1")))) {
                    mediasOfEntity.addAll(target.getProperties(PropertyFile.class));
                    for (Iterator <PropertyFile>itm = mediasOfEntity.iterator(); itm.hasNext();) {
                        PropertyFile file = itm.next();
                        if (isUnderSource(file, anchor)) {                        // Add files only for files under same id source
                            files.add(file);
                        }
                    }
                    mediasOfEntity.clear();
                    if (!files.isEmpty()) {
                        out.println("</span><span class=\"srcimage0\">");
                        for (Iterator<PropertyFile> itm = files.iterator(); itm.hasNext();) {
                            PropertyFile file = itm.next();
                            out.println("<span class=\"srcimage1\">");
                            out.println(wrapMedia(dir, file, "", true, !wp.param_media_CopySources.equals("1"),
                                    wp.param_media_DisplaySources.equals(NbBundle.getMessage(WebBookVisualPanel3.class, "sourceType.type3")),
                                    true, "", "", false, PATH2DATATEXT.toString(), "tooltipL"));
                            out.println("</span><span class=\"srcimage2\">" + SPACE + "</span>");
                        }
                        out.println("</span><br />");
                        files.clear();
                        out.println("<span class =\"srcitems0\">");
                    } else {
                        out.println("<br />");
                    }
                }
            }
            out.println("</span><br />");
        }

        // Closes the block
        out.println("</p><div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");


        // End of export section details ----------------------------------------------
    }

    /**
     * Calculate pages for section details
     */
    private void calcPages() {
        String sourcefile = "", fileStr = "";
        int cpt = 0;
        for (Iterator<Source> it = wh.getSources(wh.gedcom).iterator(); it.hasNext();) {
            Source src = it.next();
            cpt++;
            sourcefile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix;
            if (fileStr.compareTo(sourcefile) != 0) {
                fileStr = sourcefile;
            }
            sourcePage.put(src.getId(), sourcefile);
        }
    }

    /**
     * Provide link to id to outside caller
     */
    public String getPageForSource(Source src) {
        return (src == null ? "" : sourcePage.get(src.getId()));
    }

    /**
     * Provide links map to outside caller
     */
    public Map<String, String> getPagesMap() {
        return sourcePage;
    }
    /**
     * Comparator to sort entities
     */
    private Comparator<Entity> sortEntities = new Comparator<Entity>() {

        public int compare(Entity ent1, Entity ent2) {
            if ((ent1 == null) && (ent2 != null)) {
                return -1;
            }
            if ((ent1 != null) && (ent2 == null)) {
                return +1;
            }
            if ((ent1 == null) && (ent2 == null)) {
                return 0;
            }

            String str1 = "";
            String str2 = "";

            if (ent1 instanceof Indi) {
                Indi indi = (Indi) ent1;
                str1 = (wh.getLastName(indi, DEFCHAR) + ", " + indi.getFirstName()).trim();
            }
            if (ent1 instanceof Fam) {
                Fam famRel = (Fam) ent1;
                Indi husband = famRel.getHusband();
                Indi wife = famRel.getWife();
                if (husband != null) {
                    str1 += (wh.getLastName(husband, DEFCHAR) + ", " + husband.getFirstName()).trim();
                }
                if (husband != null && wife != null) {
                    str1 += " + ";
                }
                if (wife != null) {
                    str1 += (wh.getLastName(wife, DEFCHAR) + ", " + wife.getFirstName()).trim();
                }
            }
            if (ent2 instanceof Indi) {
                Indi indi = (Indi) ent2;
                str2 = (wh.getLastName(indi, DEFCHAR) + ", " + indi.getFirstName()).trim();
            }
            if (ent2 instanceof Fam) {
                Fam famRel = (Fam) ent2;
                Indi husband = famRel.getHusband();
                Indi wife = famRel.getWife();
                if (husband != null) {
                    str2 += (wh.getLastName(husband, DEFCHAR) + ", " + husband.getFirstName()).trim();
                }
                if (husband != null && wife != null) {
                    str2 += " + ";
                }
                if (wife != null) {
                    str2 += (wh.getLastName(wife, DEFCHAR) + ", " + wife.getFirstName()).trim();
                }
            }

            return str1.toLowerCase().compareTo(str2.toLowerCase());
        }
    };

    /**
     * Test for (recursive) containment
     */
    public boolean isUnderSource(Property in, String srcId) {
        Property parent = in.getParent();
        if (parent == null) {
            return false;
        }
        if (parent.getTag().compareTo("SOUR") == 0) {
            if (parent.getValue().compareTo("@" + srcId + "@") == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return isUnderSource(parent, srcId);
        }
    }

    private void printProperty(PrintWriter out, Property prop, String str, String style) {
        if ((prop == null) || (prop.getValue().trim().isEmpty())) {
            return;
        }
        out.println("<span class=\"srcitems1\">");
        out.println(wrapPropertyName(prop));
        out.println(":</span>");
        out.println("<span class=\"" + style + "\">");
        out.println(wrapPropertyValue(prop));
        out.println(wrapString(prop, str));
        out.println("</span><br />");
    }
} // End_of_Report

