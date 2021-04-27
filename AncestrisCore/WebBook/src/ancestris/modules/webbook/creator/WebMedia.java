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
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebMedia extends WebSection {

    private final static String POPUPTAG = "popup.htm";
    private int nbPhotoPerRow = 3;
    //
    private List<Photo> photos = null;

    /**
     * Constructor
     */
    public WebMedia(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    @SuppressWarnings("unchecked")
    public void init() {

        if (!toBeGenerated) {
            return;
        }

        // Regular initialisation
        init(trs("TXT_Media"), "media", "media_", formatFromSize(wh.getNbIndis()), 0, 30);

        // Build list of media, for photos of INDI and FAM but that are not under a SOUR.
        List<Entity> entities = new ArrayList<>();
        entities.addAll(wh.gedcom.getEntities(Gedcom.INDI));
        entities.addAll(wh.gedcom.getEntities(Gedcom.FAM));
        photos = new ArrayList<>();
        List<PropertyFile> propsToAdd = new ArrayList<>();
        for (Entity ent : entities) {
            // Look for OBJE which are not underneath SOUR
            for (Property obje : ent.getAllProperties("OBJE")) {
                if (obje != null && !isUnderSource(obje)) {  // obje exists and is not a source
                    if (obje instanceof PropertyMedia) {
                        Media media = (Media) ((PropertyMedia) obje).getTargetEntity();
                        propsToAdd = media.getProperties(PropertyFile.class);
                    } else {
                        propsToAdd = obje.getProperties(PropertyFile.class);
                    }
                    for (PropertyFile pFile : propsToAdd) {
                        photos.add(new Photo(ent, pFile));
                    }
                }
            }
        }
        Collections.sort(photos, sortPhotos);

        // Calculations
        calcLetters(photos);
        calcPages(photos);
        
        // memorise photos
        wh.setPhotos(photos);
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
        exportData(dir, photos);

        wh.log.write(POPUPTAG + trs("EXEC_DONE"));

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir, List<Photo> photos) {

        // Go through items to display and produce corresponding pages
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String mediafile = "";
        int cpt = 0;
        int nbMedia = photos.size();
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

        String file_title = "", file_entity = "", href = "", anchor = "";
        char last = ' ';
        for (Photo photo : photos) {
            href = getPageForMedia(photo.getFile());
            file_entity = wrapEntity(photo.getEntity());
            file_title = wrapString(photo.getFile(), photo.getTitle());
            anchor = htmlAnchorText(getEntityName(photo.getEntity()));
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
            out.println("<p class=\"medlisttitle\">" + file_entity + SPACE + SPACE + ":" + SPACE + SPACE + "<a href=\"" + href + "\">" + file_title + "</a></p><br />");
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
        for (Photo photo : photos) {
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
            exportSectionDetails(out, photo, dir, cpt);
            // .. next source
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
        String themeDirMedia = buildLinkTheme(this, themeDir);
        includePopupScript(out);
        out.println("<div><br />");
        out.println("<table class=\"maintable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" >");
        out.println("<tr style=\"height:30px\">");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDirMedia + "upleft.png') no-repeat right bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td style=\"background:url('" + themeDirMedia + "upbar.png') repeat-x left bottom\"></td>");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDirMedia + "upright.png') no-repeat left bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td style=\"background:url('" + themeDirMedia + "leftbar.png') repeat-y right bottom\">" + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td>");
        out.println("<table border=\"0\" class=\"thumbnail-table\">");
    }

    /**
     * Close table
     */
    private void closeTable(PrintWriter out) {
        String themeDirMedia = buildLinkTheme(this, themeDir);
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("<td style=\"background:url('" + themeDirMedia + "rightbar.png') repeat-y left\">" + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("<tr style=\"height:30px\">");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDirMedia + "downleft.png') repeat-x right top\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("<td style=\"background:url('" + themeDirMedia + "downbar.png') repeat-x top\"></td>");
        out.println("<td style=\"width:30px; height:30px; background:url('" + themeDirMedia + "downright.png') repeat-x left top\">" + SPACE + SPACE + SPACE + SPACE + SPACE + "</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<br /></div>");
    }

    /**
     * Exports section details
     */
    private void exportSectionDetails(PrintWriter out, Photo photo, File dir, int cpt) {
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


        // open cell
        out.println("<td class=\"thumbnail-col\" >");
        out.println("<a name=\"" + photo.getFile().hashCode() + "\"></a>");

        // print media picture
        Entity target = photo.getEntity();
        out.println(wrapMedia(dir, photo.getFile(), "", true, !wp.param_media_CopyMedia.equals("1"), true, true, "", target.getValue().trim(), true, "OBJE:NOTE", "tooltip"));

        // print entity name
        out.println(wrapEntity(target, DT_BREAK, DT_LASTFIRST, DT_ICON, DT_LINK, DT_SOSA, DT_ID));
        out.println("<br />" + SPACE + "<br />" + SPACE);

        // close cell
        out.println("</td>");


        // End of export section details ----------------------------------------------
    }
    /**
     * Comparator to sort entities
     */
    private Comparator<Photo> sortPhotos = new Comparator<Photo>() {

        public int compare(Photo p1, Photo p2) {
            if ((p1 == null) && (p2 != null)) {
                return -1;
            }
            if ((p1 != null) && (p2 == null)) {
                return +1;
            }
            if ((p1 == null) && (p2 == null)) {
                return 0;
            }

            String str1 = htmlAnchorText(getEntityName(p1.getEntity()));
            String str2 = htmlAnchorText(getEntityName(p2.getEntity()));

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
     * Gets name from Indi or Fam entity starting with lastname
     */
    public String getEntityName(Entity ent) {
        String name = "";
        if (ent == null) return "";
        if (ent instanceof Indi) {
            name = ((Indi) ent).getLastName() + ((Indi) ent).getFirstName();
        } else if (ent instanceof Fam) {
            Indi mainIndi = ((Fam) ent).getHusband();
            if (mainIndi == null) {
                mainIndi = ((Fam) ent).getWife();
            }
            if (mainIndi == null) {
                return "";
            }
            name = mainIndi.getLastName() + mainIndi.getFirstName();
        } else {
            return "";
        }
        return name;
    }

    /**
     * Calculate pages for section details
     */
    private void calcPages(List<Photo> photos) {
        String mediafile = "";
        int cpt = 0;
        for (Photo photo : photos) {
            mediafile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix + "#" + photo.getFile().hashCode();
            mediaPage.put(photo.getFile().hashCode(), mediafile);
            cpt++;
        }
    }

    /**
     * Provide link to id to outside caller
     */
    public String getPageForMedia(PropertyFile pFile) {
        return (pFile == null ? "" : mediaPage.get(pFile.hashCode()));
    }

    /**
     * Calculate if there is a link to the letters
     */
    private void calcLetters(List<Photo> photos) {

        // Initialise to zero
        linkForLetter.put(DEFCHAR, "0");
        for (Letters l : Letters.values()) {
            linkForLetter.put(l.toString(), "0");
        }

        // Calculate
        char letter = ' ';
        for (Photo photo : photos) {
            String str = htmlAnchorText(getEntityName(photo.getEntity()));
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
    
    
    
    public class Photo {
        private Entity entity = null;
        private PropertyFile pFile = null;
        private String title = null;
        
        public Photo(Entity entity, PropertyFile pFile) {
            this.entity = entity;
            this.pFile = pFile;
            this.title = wh.getTitle(pFile, DEFCHAR);
        }
        
        public Entity getEntity() {
            return entity;
        }

        public String getTitle() {
            return entity.getDisplayValue() + " " + title;
        }

        public PropertyFile getFile() {
            return pFile;
        }
    }
    
    
} // End_of_Report

