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
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebMedia extends WebSection {

    private final static String POPUP = "popup.htm";
    private int WIDTH_PICTURES = 200;
    private int nbPhotoPerRow = 3;

    /**
     * Constructor
     */
    public WebMedia(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Media"), "media", "media_", formatFromSize(wh.getNbIndis()), ".html", 0, 30);
    }

    /**
     * Section's entry point
     */
    @Override
    @SuppressWarnings("unchecked")
    public void create() {

        // Preliminary build of individualsdetails link for links from sources to details
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Build list of media, for medias of INDI and FAM but that are not under a SOUR.
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(wh.gedcom.getEntities(Gedcom.INDI));
        entities.addAll(wh.gedcom.getEntities(Gedcom.FAM));
        List<Property> medias = new ArrayList<Property>();
        List<Property> mediasOfEntity = new ArrayList<Property>();
        for (Iterator ite = entities.iterator(); ite.hasNext();) {
            Entity ent = (Entity) ite.next();
            mediasOfEntity.addAll(ent.getProperties(PropertyFile.class));
            for (Iterator itm = mediasOfEntity.iterator(); itm.hasNext();) {
                PropertyFile media = (PropertyFile) itm.next();
                if (!isUnderSource(media)) {
                    medias.add(media);
                }
            }
            mediasOfEntity.clear();
        }
        Collections.sort(medias, sortEntities);

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        calcLetters(medias);
        calcPages(medias);
        exportData(dir, medias);

        wh.log.write(POPUP + trs("EXEC_DONE"));

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir, List medias) {

        // Go through items to display and produce corresponding pages
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String mediafile = "";
        int cpt = 0;
        int nbMedia = medias.size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbMedia / nbPerPage) + 1;

        // export first page (page '0' as the index page)
        mediafile = sectionPrefix + String.format(formatNbrs, 0) + sectionSuffix;
        fileStr = mediafile;
        file = wh.getFileForName(dir, mediafile);
        out = wh.getWriter(file, UTF8);
        printOpenHTML(out, "TXT_MediaIndex", this);
        out.println("<p class=\"letters\">");
        out.println("<br /><br />");
        for (Letters l : Letters.values()) {
            if (checkLink(l.toString())) {
                out.println("<a href=\"#" + l + "\">" + l + "</a>" + SPACE + SPACE);
            } else {
                out.println(l + SPACE + SPACE);
            }
        }
        if (checkLink(DEFCHAR)) {
            out.println("<a href=\"#" + DEFCHAR + "\">" + DEFCHAR + "</a>" + SPACE + SPACE);
        } else {
            out.println(DEFCHAR + SPACE + SPACE);
        }
        out.println("<br /><br /></p>");
        exportLinks(out, mediafile, 0, 0, 1, lastPage);

        String file_title = "", file_line = "", href = "", anchor = "";
        char last = ' ';
        for (Iterator it = medias.iterator(); it.hasNext();) {
            PropertyFile media = (PropertyFile) it.next();
            href = getPageForMedia(media);
            file_title = wh.getTitle(media, DEFCHAR);
            file_line = htmlText(media.getEntity().toString()) + SPACE + SPACE + SPACE + ((file_title.length() > 0) ? "(" + htmlText(file_title) + ")" : "");
            anchor = htmlAnchorText(media.getEntity().toString());
            if (anchor.length() > 0 && Character.toUpperCase(anchor.charAt(0)) != last) {
                last = Character.toUpperCase(anchor.charAt(0));
                String l = String.valueOf(last);
                if (!(String.valueOf(last).matches("[a-zA-Z]"))) {
                    l = DEFCHAR;
                }
                anchor = "<p class=\"letter\">" + "<a name=\"" + l + "\"></a>" + l + "</p>";
            } else {
                anchor = "";
            }
            cpt++;
            out.println(anchor);
            out.println("<div class=\"conteneur\">");
            out.println("<p class=\"medlist\">" + cpt + "</p>");
            out.println("<p class=\"medlisttitle\"><a href=\"" + href + "\">" + file_line + "</a></p><br />");
            out.println("<div class=\"spacer\">" + SPACE + "</div>");
            out.println("</div>");
        }
        exportLinks(out, mediafile, 0, 0, 1, lastPage);
        printCloseHTML(out);
        out.close();
        wh.log.write(mediafile + trs("EXEC_DONE"));

        // export detailed pages
        cpt = 0;
        out = null;
        for (Iterator it = medias.iterator(); it.hasNext();) {
            PropertyFile media = (PropertyFile) it.next();
            cpt++;
            currentPage = ((cpt - 1) / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            mediafile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (fileStr.compareTo(mediafile) != 0) {
                if (out != null) {
                    closeTable(out);
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 0, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    out.close();
                    wh.log.write(fileStr + trs("EXEC_DONE"));
                }
                fileStr = mediafile;
                file = wh.getFileForName(dir, mediafile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Media", this);
                exportLinks(out, mediafile, 0, previousPage, nextPage, lastPage);
                openTable(out);
            }
            exportSectionDetails(out, media, dir, mediafile, cpt);
            // .. next individual
        }
        if (out != null) {
            closeTable(out);
            exportLinks(out, mediafile, 0, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }

    }

    /**
     * Open table
     */
    private void openTable(PrintWriter out) {
        includePopupScript(out);
        out.println("<div><br />");
        out.println("<table class=\"maintable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" >");
        out.println("<tr style=\"height:30px\">");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDir + "upleft.png') no-repeat right bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td style=\"background:url('" + themeDir + "upbar.png') repeat-x left bottom\"></td>");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDir + "upright.png') no-repeat left bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td style=\"background:url('" + themeDir + "leftbar.png') repeat-y right bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td>");
        out.println("<table border=\"0\" class=\"thumbnail-table\">");
    }

    /**
     * Close table
     */
    private void closeTable(PrintWriter out) {
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("<td style=\"background:url('" + themeDir + "rightbar.png') repeat-y left\">" + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("<tr style=\"height:30px\">");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDir + "downleft.png') repeat-x right top\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td style=\"background:url('" + themeDir + "downbar.png') repeat-x top\"></td>");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDir + "downright.png') repeat-x left top\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<br /></div>");
    }

    /**
     * Exports section details
     */
    private void exportSectionDetails(PrintWriter out, PropertyFile media, File dir, String mediafile, int cpt) {
        //
        // Small pictures are to be retrieved from the already created pictures (by report on individual details)
        // (e.g. ../details/media/individuals/xxx.jpg)
        // Large (or original size) pictures are to be copied from gedcom files
        //
        if (((cpt - 1) / nbPhotoPerRow) * nbPhotoPerRow == (cpt - 1)) {             // if multiple of nbPhotoPerRow
            if ((cpt - 1) != (cpt - 1) / nbPerPage * nbPerPage) {    // if NOT first media of page
                out.println("</tr>");
            }
            out.println("<tr class=\"thumbnail-row\" >");
        }

        Entity target = media.getEntity();
        String title = wh.getTitle(media, DEFCHAR);
        String thumbPic = "smallpic";
        String origFile = "origFile";
        String link = SPACE;

        if ((media != null) && (media.getFile() != null) && (!wh.isPrivate(target))) {    // file tag is filled in

            origFile = wh.getCleanFileName(media.getValue(), DEFCHAR);
            try { // copy locally (link or file itself)
                wh.copy(media.getFile().getAbsolutePath(), dir.getAbsolutePath() + File.separator + origFile, !wp.param_media_GeneMedia.equals("1"), false);
                //System.out.println(dir.getAbsolutePath()+File.separator+str);
            } catch (IOException e) {
                //e.printStackTrace();
                wb.log.write(wb.log.ERROR, "exportSectionDetails - " + e.getMessage());
            }

            thumbPic = prefixPersonDetailsDir + origFile;    // this is the miniature picture
            if (!wh.isImage(media.getFile().getAbsolutePath())) {
                thumbPic = themeDir + "mednopic.png";
                link = "<a href=\"javascript:popup('" + origFile + "','100','100')\"><img alt=\"" + htmlText(target.toString()) + "\" title=\"" + htmlText(title) + "\" src=\"" + thumbPic + "\" /></a><br />";
            } else {
                if (media.getPath().toString().compareTo("INDI:OBJE:FILE") != 0 && media.getPath().toString().compareTo("FAM:OBJE:FILE") != 0) {
                    thumbPic = "mini_" + origFile;
                    wh.scaleImage(media.getFile().getAbsolutePath(), dir.getAbsolutePath() + File.separator + thumbPic, WIDTH_PICTURES, 0, 100, false);
                }
                link = "<a href=\"javascript:popup('" + origFile + "','" + wh.getImageSize(media.getFile().getAbsolutePath()) + "')\"><img alt=\"" + htmlText(target.toString()) + "\" title=\"" + htmlText(title) + "\" src=\"" + thumbPic + "\" /></a><br />";
            }
        } else if (wh.isPrivate(target)) {
            link = "<img alt=\"" + htmlText(trs("med_priv")) + "\" title=\"" + htmlText(trs("med_priv")) + "\" src=\"" + themeDir + "medpriv.png\" /><br />";
        } else {
            link = "<img alt=\"" + htmlText(target.toString()) + "\" title=\"" + htmlText(trs("med_none")) + "\" src=\"" + themeDir + "medno.png\" /><br />";
        }
        out.println("<td class=\"thumbnail-col\" ><a name=\"" + media.hashCode() + "\"></a>" + link);
        if (title != null && title.length() != 0) {
            if (wh.isPrivate(target)) {
                out.println(htmlText(trs("med_priv")) + "<br />");
            } else {
                out.println(htmlText(title) + "<br />");
            }
        }
        if (target instanceof Indi) {
            Indi indiRel = (Indi) target;
            wrapName(out, indiRel);
            out.println("<br />");
            wrapDate(out, indiRel, true);
        }
        if (target instanceof Fam) {
            Fam famRel = (Fam) target;
            Indi husband = famRel.getHusband();
            Indi wife = famRel.getWife();
            wrapName(out, husband);
            wrapDate(out, husband, true);
            out.println(SPACE + "+");
            out.println("<br />");
            wrapName(out, wife);
            wrapDate(out, wife, true);
        }
        out.println("<br />" + SPACE + "<br />" + SPACE + "</td>");


        // End of export section details ----------------------------------------------
    }

    /**
     * Comparator to sort entities
     */
    private Comparator sortEntities = new Comparator() {

        public int compare(Object o1, Object o2) {
            if ((o1 == null) && (o2 != null)) {
                return -1;
            }
            if ((o1 != null) && (o2 == null)) {
                return +1;
            }
            if ((o1 == null) && (o2 == null)) {
                return 0;
            }

            Property p1 = (Property) o1;
            Property p2 = (Property) o2;

            String str1 = htmlAnchorText(p1.getEntity().toString());
            String str2 = htmlAnchorText(p2.getEntity().toString());

            if (str1.startsWith(DEFCHAR)) {
                if (str2.startsWith(DEFCHAR)) {
                    return str1.compareTo(str2);
                }
                return +1;   // DEFCHAR will be sorted after [A-Z]
            }
            if (str2.startsWith(DEFCHAR)) {
                return -1;   // DEFCHAR will be sorted after [A-Z]
            }
            return str1.compareTo(str2);
        }
    };

    /**
     * Calculate pages for section details
     */
    private void calcPages(List medias) {
        String mediafile = "", fileStr = "";
        int cpt = 0;
        for (Iterator it = medias.iterator(); it.hasNext();) {
            PropertyFile media = (PropertyFile) it.next();
            mediafile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix + "#" + media.hashCode();
            mediaPage.put(Integer.valueOf(media.hashCode()), mediafile);
            cpt++;
        }
    }

    /**
     * Provide link to id to outside caller
     */
    public String getPageForMedia(PropertyFile media) {
        return (media == null ? "" : mediaPage.get(Integer.valueOf(media.hashCode())));
    }

    /**
     * Test for (recursive) containment
     */
    public boolean isUnderSource(Property in) {
        Property parent = in.getParent();
        if (parent == null) {
            return false;
        }
        return parent.getTag().compareTo("SOUR") == 0 ? true : isUnderSource(parent);
    }

    /**
     * Calculate if there is a link to the letters
     */
    private void calcLetters(List<Property> medias) {

        // Initialise to zero
        linkForLetter.put(DEFCHAR, "0");
        for (Letters l : Letters.values()) {
            linkForLetter.put(l.toString(), "0");
        }

        // Calculate
        char letter = ' ';
        for (Iterator it = medias.iterator(); it.hasNext();) {
            PropertyFile media = (PropertyFile) it.next();
            String str = htmlAnchorText(media.getEntity().toString());
            if (str == null) {
                continue;
            }
            char cLetter = str.charAt(0);
            if (cLetter != letter) {
                letter = cLetter;
                String l = String.valueOf(letter);
                linkForLetter.put(l, "true");      // any string will do
            }
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

