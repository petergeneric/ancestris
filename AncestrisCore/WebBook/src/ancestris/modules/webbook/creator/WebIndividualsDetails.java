/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertyComparator;
import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;

import java.io.File;
import java.util.Arrays;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Ancestris
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebIndividualsDetails extends WebSection {

    private final static String POPUPTAG = "popupemail.htm";
    private String indi2srcDir = "";
    private String indi2mediaDir = "";
    private String fam_chronologie = "";
    private String fam_grandparents = "";
    private String fam_siblings = "";
    private String fam_family = "";
    private String fam_spouse = "";
    private String fam_kids = "";
    private String fam_stepsibfather = "";
    private String fam_stepsibmother = "";
    private String fam_stepkids = "";
    private String fam_relations = "";
    private String fam_relhas = "";
    private String fam_relis = "";
    private String fam_relunk = "";
    private String fam_relwhois = "";
    private String fam_relat = "";
    private String fam_relthe = "";
    private String fam_relof = "";
    private String fam_note = "";
    private String fam_noteSrc = "";

    /**
     * Constructor
     */
    public WebIndividualsDetails(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Individualsdetails"), "details", "personsdetails_", formatFromSize(wh.getNbIndis()), 1, sizeIndiSection);
        calcPages();
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        initEvents();

        fam_chronologie = htmlText(trs("fam_chronologie"));
        fam_grandparents = htmlText(trs("fam_grandparents"));
        fam_siblings = htmlText(trs("fam_siblings"));
        fam_family = htmlText(trs("fam_family"));
        fam_spouse = htmlText(trs("fam_spouse"));
        fam_kids = htmlText(trs("fam_kids"));
        fam_stepsibfather = htmlText(trs("fam_stepsibfather"));
        fam_stepsibmother = htmlText(trs("fam_stepsibmother"));
        fam_stepkids = htmlText(trs("fam_stepkids"));
        fam_relations = htmlText(trs("fam_relations"));

        fam_relhas = trs("fam_relhas");
        fam_relis = trs("fam_relis");
        fam_relunk = trs("fam_relunk");
        fam_relwhois = trs("fam_relwhois");
        fam_relat = trs("fam_relat");
        fam_relthe = trs("fam_relthe");
        fam_relof = trs("fam_relof");

        fam_note = htmlText(trs("fam_note"));
        fam_noteSrc = htmlText(trs("fam_noteSrc"));

        // Preliminary build of sources link for links from details to sources
        if (wb.sectionSources != null && wb.sectionSources.toBeGenerated) {
            sourcePage = wb.sectionSources.getPagesMap();
            indi2srcDir = buildLinkShort(this, wb.sectionSources);
        }
        if (wb.sectionMedia != null && wb.sectionMedia.toBeGenerated) {
            indi2mediaDir = buildLinkShort(this, wb.sectionMedia);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

        // Generate email popup
        if (wp.param_dispEmailButton.equals("1")) {
            createPopupEmail(wh.getFileForName(dir, POPUPTAG));
            wh.log.write(POPUPTAG + trs("EXEC_DONE"));
        }

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        // Go through individuals
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String personfile = "";
        int cpt = 0;
        int nbIndis = wh.getIndividuals(wh.gedcom, sortIndividuals).size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbIndis / nbPerPage) + 1;

        for (Indi indi : wh.getIndividuals(wh.gedcom, sortIndividuals)) {
            cpt++;
            currentPage = (cpt / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            personfile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (fileStr.compareTo(personfile) != 0) {
                if (out != null) {
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 1, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    out.close();
                    wh.log.write(fileStr + trs("EXEC_DONE"));
                }
                fileStr = personfile;
                file = wh.getFileForName(dir, personfile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Individualsdetails", this);
                writeScript(out);
            }
            exportLinks(out, personfile, 1, previousPage, nextPage, lastPage);
            exportIndividualDetails(out, indi, dir);
            // .. next individual
        }
        if (out != null) {
            exportLinks(out, personfile, 1, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }

    }

    /**
     * Exports individual details
     */
    @SuppressWarnings("unchecked")
    private void exportIndividualDetails(PrintWriter out, Indi indi, File dir) {

        // Details
        String themeDirLink = buildLinkTheme(this, themeDir);
        String anchor = indi.getId();
        String name = wrapName(indi, DT_LASTFIRST, DT_NOLINK, DT_SOSA, DT_ID);
        Indi father = indi.getBiologicalFather();
        Indi mother = indi.getBiologicalMother();
        Indi fatherfather = (father != null) ? father.getBiologicalFather() : null;
        Indi motherfather = (father != null) ? father.getBiologicalMother() : null;
        Indi fathermother = (mother != null) ? mother.getBiologicalFather() : null;
        Indi mothermother = (mother != null) ? mother.getBiologicalMother() : null;

        // Start of details ----------------------------------------------
        // Individual name
        out.println("<h2 class=\"" + getSexStyle(indi) + "\">");
        out.println("<a id=\"" + anchor + "\"></a>");
        out.println(wrapSex(indi));
        out.println(SPACE);
        out.println(name);
        out.println("</h2>");

        // First container
        out.println("<div class=\"conteneur\">");

        // Email button
        if (wp.param_dispEmailButton.equals("1")) {
            String str = htmlText(trs("TXT_mail_comment"));
            out.println(wrapEmailButton(indi, "mail.gif", str));
            out.println("<br /><br />");
        }

        // Events of the individual
        // Format is: eventIcon, eventname, date, description, [source link], [note link], [media link]
        out.println("<p class=\"decal\"><span class=\"gras\">" + fam_chronologie + "</span></p>");
        out.println("<p class=\"parentm\">");
        out.println(wrapEvents(indi, false, indi2srcDir, indi2mediaDir));
        out.println("<br />");
        out.println("</p>");

        // Images and other media (only if media are to be generated)
        if (wp.param_media_GeneMedia.equals("1")) {
            List<WebMedia.Photo> photos = wh.getPhoto(indi);
            Fam[] families = indi.getFamiliesWhereSpouse();
            for (Fam family : families) {
                photos.addAll(wh.getPhoto(family));
            }
            if (!photos.isEmpty()) {
                out.println("<p class=\"image\">");
                for (WebMedia.Photo photo : photos) {
                    if (photo.getFile() == null) {
                        continue;
                    }
                    out.println(wrapMedia(null, photo.getFile(), indi2mediaDir, false, false, true, false, "", photo.getTitle(), false, "OBJE:NOTE", "tooltip"));
                }
                out.println("</p>");
            }
        }

        // end of container
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Grand Parents
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"decal\"><span class=\"gras\">" + fam_grandparents + "</span></p>");
        out.println("<p class=\"parentgp\">");
        out.println(wrapName(fatherfather));
        out.println("<br />");
        out.println(wrapDate(fatherfather, false));
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        out.println(wrapName(motherfather));
        out.println("<br />");
        out.println(wrapDate(motherfather, false));
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        out.println(wrapName(fathermother));
        out.println("<br />");
        out.println(wrapDate(fathermother, false));
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        out.println(wrapName(mothermother));
        out.println("<br />");
        out.println(wrapDate(mothermother, false));
        out.println("</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Lines
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl2\"></p>");
        out.println("<p class=\"parentgpl3\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl2\"></p>");
        out.println("<p class=\"parentgpl3\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl4\"></p>");
        out.println("<p class=\"parentgpl5\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<p class=\"parentgpl4\"></p>");
        out.println("<p class=\"parentgpl5\"></p>");
        out.println("<p class=\"parentgpl1\"></p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Parents
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"parentp\">");
        out.println(wrapName(father));
        out.println("<br />");
        out.println(wrapDate(father, false));
        out.println("</p>");
        out.println("<p class=\"parentp\">");
        out.println(wrapName(mother));
        out.println("<br />");
        out.println(wrapDate(mother, false));
        out.println("</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Siblings
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + fam_siblings + "</span></p>");
        out.println("<p class=\"parents\">");
        Indi[] osiblings = indi.getOlderSiblings();
        Arrays.sort(osiblings, new PropertyComparator("INDI:BIRT:DATE"));
        for (Indi osibling : osiblings) {
            out.println(wrapEntity(osibling));
            out.println("<br />");
        }
        out.println("<span class=\"grasplus\">");
        out.println(wrapEntity(indi));
        out.println("</span><br />");
        Indi[] ysiblings = indi.getYoungerSiblings();
        Arrays.sort(ysiblings, new PropertyComparator("INDI:BIRT:DATE"));
        for (Indi ysibling : ysiblings) {
            out.println(wrapEntity(ysibling));
            out.println("<br />");
        }
        out.println("<br /></p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Step sibling by father
        boolean hasStepSibFather = false;
        if (wp.param_dispSiblings.equals("1") && father != null) {
            Fam[] stepFamilies = father.getFamiliesWhereSpouse();
            for (Fam stepFamily : stepFamilies) {
                Indi spouse = stepFamily.getWife();
                if (spouse != mother) {
                    Indi[] stepSiblings = stepFamily.getChildren();
                    Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
                    if (!hasStepSibFather && stepSiblings.length > 0) {
                        hasStepSibFather = true;
                        out.println("<div class=\"conteneur\">");
                        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + fam_stepsibfather + "</span></p>");
                        out.println("<p class=\"parents\">");
                    }
                    for (Indi stepSbling : stepSiblings) {
                        out.println(wrapEntity(stepSbling));
                        out.println("<br />");
                    }
                }
            }
            if (hasStepSibFather) {
                out.println("<br /></p>");
                out.println("<div class=\"spacer\">" + SPACE + "</div>");
                out.println("</div>");
            }
        }

        // Step sibling by mother
        boolean hasStepSibMother = false;
        if (wp.param_dispSiblings.equals("1") && mother != null) {
            Fam[] stepFamilies = mother.getFamiliesWhereSpouse();
            for (Fam stepFamily : stepFamilies) {
                Indi spouse = stepFamily.getHusband();
                if (spouse != father) {
                    Indi[] stepSiblings = stepFamily.getChildren();
                    Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
                    if (!hasStepSibMother && stepSiblings.length > 0) {
                        hasStepSibMother = true;
                        out.println("<div class=\"conteneur\">");
                        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + fam_stepsibmother + "</span></p>");
                        out.println("<p class=\"parents\">");
                    }
                    for (Indi stepSbling : stepSiblings) {
                        out.println(wrapEntity(stepSbling));
                        out.println("<br />");
                    }
                }
            }
            if (hasStepSibMother) {
                out.println("<br /></p>");
                out.println("<div class=\"spacer\">" + SPACE + "</div>");
                out.println("</div>");
            }
        }

        // Families (spouses and corresponding kids)
        // (note: will need xref for the relations of the weddings XREF later so better do it here)
        List<PropertyXRef> xrefList = indi.getProperties(PropertyXRef.class);
        Fam[] families = indi.getFamiliesWhereSpouse();
        Arrays.sort(families, new PropertyComparator("FAM:MARR:DATE"));
        if (!wp.param_dispSpouse.equals("1")) {
            families = null;            // so that families are not displayed by skipping the loop which follows
        }
        for (int i = 0; families != null && i < families.length; i++) {
            Fam family = families[i];
            xrefList.addAll(family.getProperties(PropertyXRef.class));
            Indi spouse = family.getHusband();
            if (spouse == indi) {
                spouse = family.getWife();
            }
            out.println("<div class=\"conteneur\">");
            out.println("<p class=\"decal\"><span class=\"gras\">" + fam_family + (families.length > 1 ? " (" + (i + 1) + ")" : "") + "</span></p>");
            out.println("<p class=\"parentf\">");
            out.println(wrapSex(spouse));
            out.println("<span class=\"gras\">" + fam_spouse + "</span>:" + SPACE);
            out.println(wrapName(spouse));
            out.println(wrapDate(spouse, true));
            out.println("<br />");
            out.println(wrapEvents(family, false, indi2srcDir, indi2mediaDir));
            out.println("</p>");
            Indi[] children = family.getChildren();
            Arrays.sort(children, new PropertyComparator("INDI:BIRT:DATE"));
            if (wp.param_dispKids.equals("1") && children.length > 0) {
                out.println("<p class=\"parentf\"><img src=\"" + themeDirLink + "chld.png\" alt=\"icon\" />");
                out.println("<span class=\"gras\">" + fam_kids + "</span>:<br /></p>");
                out.println("<p class=\"parentfc\">");
                for (Indi child : children) {
                    out.println(wrapEntity(child));
                    out.println("<br />");
                }
                out.println("</p>");
            }
            // get step brothers and sisters by looking for other families
            if (wp.param_dispSiblings.equals("1") && spouse != null) {
                Fam[] stepFamilies = spouse.getFamiliesWhereSpouse();
                for (Fam stepFamily : stepFamilies) {
                    if (stepFamily != family) {
                        Indi[] stepSiblings = stepFamily.getChildren();
                        Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
                        if (stepSiblings.length > 0) {
                            out.println("<p class=\"parentf\"><span class=\"gras\">" + fam_stepkids + "</span>:<br /></p>");
                            out.println("<p class=\"parentfc\">");
                            for (Indi stepSbling : stepSiblings) {
                                out.println(wrapEntity(stepSbling));
                                out.println("<br />");
                            }
                            out.println("</p>");
                        }
                    }
                }
            }
            out.println("<br />");
            out.println("<div class=\"spacer\">" + SPACE + "</div>");
            out.println("</div>");
        }

        // Relations
        // (xrefList loaded earlier)
        boolean displayRelation = false;
        if (wp.param_dispRelations.equals("1") && (xrefList.size() > 0)) {
            for (PropertyXRef xref : xrefList) {
                Entity target = xref.getTargetEntity();
                boolean isXref = xref.getTag().compareTo("XREF") == 0;
                boolean isAsso = xref.getTag().compareTo("ASSO") == 0;
                if (!isXref && !isAsso) {
                    continue;
                }
                if (!displayRelation) {
                    out.println("<div class=\"conteneur\">");
                    out.println("<p class=\"decal\"><span class=\"gras\">" + fam_relations + "</span></p>");
                    out.println("<p class=\"rela2\">");
                    displayRelation = true;
                }
                out.println("&bull;" + SPACE);
                if (isXref) {
                    String str = "";
                    str += fam_relhas + " ";
                    Property prop = xref.getTarget().getProperty("RELA");
                    String link = (prop == null) ? fam_relunk : prop.getDisplayValue().toLowerCase();
                    String event = xref.getParent().getPropertyName().toLowerCase();
                    boolean doubleup = link.contains(event);
                    PropertyDate date = (PropertyDate) xref.getParent().getProperty("DATE");
                    if (!doubleup) {
                        str += link + " " + fam_relat + " " + event + ", ";
                    } else {
                        str += link + ", ";
                    }
                    if (date != null) {
                        str += fam_relthe + " " + date.getDisplayValue().toLowerCase() + ", ";
                    }
                    str += fam_relwhois;
                    out.println(wrapString(indi, str));
                }
                if (isAsso) {
                    String str = "";
                    str += fam_relis + " ";
                    Property prop = xref.getProperty("RELA");
                    String link = (prop == null) ? fam_relunk : prop.getDisplayValue().toLowerCase();
                    String event = xref.getTarget().getParent().getPropertyName().toLowerCase();
                    boolean doubleup = link.contains(event);
                    PropertyDate date = (PropertyDate) xref.getTarget().getParent().getProperty("DATE");
                    if (!doubleup) {
                        str += link + " " + fam_relat + " " + event + ", ";
                    } else {
                        str += link + ", ";
                    }
                    if (date != null) {
                        str += fam_relthe + " " + date.getDisplayValue().toLowerCase() + ", ";
                    }
                    str += fam_relof;
                    out.println(wrapString(indi, str));
                }
                out.println(wrapEntity(target));
                out.println("<br />");
            }
            if (displayRelation) {
                out.println("<br />");
                out.println("</p>");
                out.println("<div class=\"spacer\">" + SPACE + "</div>");
                out.println("</div>");
            }
        }

        // Note
        boolean displayNote = false;
        if (wp.param_dispNotes.equals("1")) {
            HashSet<Property> notes = getNotes(indi);
            if (notes.size() > 0) {
                for (Property note : notes) {
                    String noteStr = note.getDisplayValue().trim();
                    Property parent = note.getParent();
                    String parentTag = htmlText(parent.getPropertyName());
                    if (noteStr.length() > 0 && parent.getTag().compareTo("ASSO") != 0) {
                        if (!displayNote) {
                            out.println("<div class=\"conteneur\">");
                            out.println("<p class=\"decal\"><span class=\"gras\">" + fam_note + "</span>:</p>");
                            out.println("<p class=\"note\" >");
                            displayNote = true;
                        }
                        String parentNote = (parent.getTag().compareTo("SOUR") == 0) ? " " + fam_noteSrc
                                + " " + parent.getParent().getPropertyName().toLowerCase() + ":" : ":";
                        out.println("<span class=\"undl\">");
                        out.println(parent.getTag().compareTo(Gedcom.INDI) == 0 ? "" : wrapString(indi, parentTag + parentNote));
                        out.println("</span>");
                        out.println("<span class=\"ital\">");
                        out.println(wrapString(indi, noteStr));
                        out.println("</span><br />");
                    }
                }
                if (displayNote) {
                    out.println("</p>");
                    out.println("<div class=\"spacer\">" + SPACE + "</div>");
                    out.println("</div>");
                }
            }
        }

        // End of details ----------------------------------------------
    }

    /**
     * Returns this indi's notes
     */
    public HashSet<Property> getNotes(Indi indi) {
        HashSet<Property> notes = new HashSet<>();
        notes.addAll(Arrays.asList(indi.getProperties("NOTE")));

        Fam[] families = indi.getFamiliesWhereSpouse();
        for (Fam family : families) {
            notes.addAll(Arrays.asList(family.getProperties("NOTE")));
        }

        return notes;
    }

    /**
     * Calculate pages for individual details
     */
    private void calcPages() {
        String personfile = "", fileStr = "";
        int cpt = 0;
        for (Indi indi : wh.getIndividuals(wh.gedcom, sortIndividuals)) {
            cpt++;
            personfile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix;
            if (fileStr.compareTo(personfile) != 0) {
                fileStr = personfile;
            }
            personPage.put(indi.getId(), personfile);
        }
    }

    /**
     * Provide link to id to outside caller
     */
    public String getPageForIndi(Indi indi) {
        return (indi == null ? "" : personPage.get(indi.getId()));
    }

    /**
     * Provide links map to outside caller
     */
    public Map<String, String> getPagesMap() {
        return personPage;
    }

    /**
     * Write script for email popup
     */
    private void writeScript(PrintWriter out) {
        out.println("<script>");
        out.println("<!--");
        out.println("function popup(sText)");
        out.println("{");
        out.println("window.open( \"" + POPUPTAG + "?\"+sText, '', 'HEIGHT=650,WIDTH=620,toolbar=0,status=0,menubar=0');");
        out.println("}");
        out.println("//-->");
        out.println("</script>");

    }
} // End_of_Report

