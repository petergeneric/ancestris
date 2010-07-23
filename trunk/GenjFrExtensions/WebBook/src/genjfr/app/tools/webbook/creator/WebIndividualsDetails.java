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
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyComparator;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.io.File;
import java.util.Collections;
import java.util.Arrays;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebIndividualsDetails extends WebSection {

    private final static String POPUPTAG = "popupemail.htm";
    private final static TagPath INDI2IMAGES = new TagPath("INDI:OBJE:FILE");
    private final static TagPath FAM2IMAGES = new TagPath("FAM:OBJE:FILE");
    private int WIDTH_PICTURES = 150;
    private String indi2srcDir = "";
    private String indi2mediaDir = "";
    private String[] events = null;
    private String[] eventsMarr = null;
    private boolean showDate = true;
    private boolean showPlace = true;
    private boolean showAllPlaceJurisdictions = true;
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
        init(trs("TXT_Individualsdetails"), "details", "personsdetails_", formatFromSize(wh.getNbIndis()), ".html", 1, sizeIndiSection);
        calcPages();
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        initEvents();

        // Preliminary build of sources link for links from details to sources
        if (wb.sectionSources != null) {
            sourcePage = wb.sectionSources.getPagesMap();
            indi2srcDir = buildLinkShort(this, wb.sectionSources);
        }
        if (wb.sectionMedia != null) {
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
     * Init events
     */
    private void initEvents() {
        events = new String[]{// Events
                    "BIRT", "CHR",
                    "DEAT", "BURI", "CREM",
                    "ADOP",
                    "BAPM", "BARM", "BASM", "BLES",
                    "CHRA", "CONF", "FCOM", "ORDN",
                    "NATU", "EMIG", "IMMI",
                    "CENS", "PROB", "WILL",
                    "GRAD", "RETI",
                    "EVEN",
                    // Attributes
                    "CAST", "DSCR", "EDUC", "IDNO", "NATI", "NCHI", "NMR", "OCCU", "PROP", "RELI", "RESI", "SSN", "TITL"
                };

        eventsMarr = new String[]{// Events
                    "ANUL", "CENS", "DIV", "DIVF",
                    "ENGA", "MARR", "MARB", "MARC",
                    "MARL", "MARS",
                    "EVEN"
                };

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
        fam_relhas = htmlText(trs("fam_relhas"));
        fam_relis = htmlText(trs("fam_relis"));
        fam_relunk = htmlText(trs("fam_relunk"));
        fam_relwhois = htmlText(trs("fam_relwhois"));
        fam_relat = htmlText(trs("fam_relat"));
        fam_relthe = htmlText(trs("fam_relthe"));
        fam_relof = htmlText(trs("fam_relof"));
        fam_note = htmlText(trs("fam_note"));
        fam_noteSrc = htmlText(trs("fam_noteSrc"));
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

        for (Iterator it = wh.getIndividuals(wh.gedcom, sortIndividuals).iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
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
            exportIndividualDetails(out, indi, dir, personfile);
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
    private void exportIndividualDetails(PrintWriter out, Indi indi, File dir, String personfile) {

        // Details
        String lastname = wh.getLastName(indi, DEFCHAR);
        String firstname = indi.getFirstName();
        String anchor = indi.getId();

        Indi father = indi.getBiologicalFather();
        Indi mother = indi.getBiologicalMother();
        Indi fatherfather = (father != null) ? father.getBiologicalFather() : null;
        Indi motherfather = (father != null) ? father.getBiologicalMother() : null;
        Indi fathermother = (mother != null) ? mother.getBiologicalFather() : null;
        Indi mothermother = (mother != null) ? mother.getBiologicalMother() : null;

        String sexString = "?" + SPACE;
        String sexStyle1 = "", sexStyle2 = "";
        String themeDirLink = buildLinkTheme(this, themeDir);
        if (indi.getSex() == 1) {
            sexString = "<img src=\"" + themeDirLink + "m.gif\" alt=\"" + trs("alt_male") + "\" />";
            sexStyle1 = "<h2 class=\"hom\">";
            sexStyle2 = "</h2>";
        } else if (indi.getSex() == 2) {
            sexString = "<img src=\"" + themeDirLink + "f.gif\" alt=\"" + trs("alt_female") + "\" />";
            sexStyle1 = "<h2 class=\"fem\">";
            sexStyle2 = "</h2>";
        } else {
            sexString = "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";
            sexStyle1 = "<h2 class=\"unk\">";
            sexStyle2 = "</h2>";
        }

        // Start of details ----------------------------------------------

        // Individual name
        out.println(sexStyle1);
        out.println("<a name=\"" + anchor + "\"></a>");
        out.println(sexString + SPACE);
        String name = (lastname + ", " + firstname).trim();
        if (wh.isPrivate(indi)) {
            name = "..., ...";
        }
        if (name.compareTo(",") == 0) {
            name = "";
        }
        out.print(htmlText(name));
        String sosa = wh.getSosa(indi);
        if (sosa != null && sosa.length() != 0) {
            out.println(SPACE + "(" + sosa + ")");
        }
        out.println(sexStyle2);

        // First container
        out.println("<div class=\"conteneur\">");

        // Email button
        if (wp.param_dispEmailButton.equals("1") && !wh.isPrivate(indi)) {
            String str = htmlText(trs("TXT_mail_comment"));
            out.println("<a href=\"javascript:popup('" + indi.toString() + "')\"><img src=\"" + themeDirLink + "mail.gif\" alt=\"" + str + "\" title=\"" + str + "\"/></a><br /><br />");
        }

        // Events of the individual
        // Format is: eventIcon, eventname, date, description, [source link], [note link], [media link]
        out.println("<p class=\"decal\"><span class=\"gras\">" + fam_chronologie + "</span></p>");
        out.println("<p class=\"parentm\">");
        List<String> listEvents = getNameDetails(indi);
        for (Iterator s = listEvents.iterator(); s.hasNext();) {
            String event = (String) s.next();   // [0]date . [1]description . [2]source_id . [3]event_tag . [4]media_id . [5] notes
            String[] eventBits = event.split("\\|", -1);
            // eventIcon
            out.println("<img src=\"" + themeDirLink + "ev_" + eventBits[3] + ".png" + "\" alt=\"\"");
            // eventname : date description
            out.println(eventBits[1].trim());
            // [source link]
            if (wp.param_media_GeneSources.equals("1") && eventBits[2].length() != 0) {
                out.println(eventBits[2].trim());
            }
            // [media link]
            if (wp.param_media_GeneMedia.equals("1") && eventBits[4].length() != 0) {
                out.println(eventBits[4]);
            }
            // [note link]
            if (eventBits[5].length() != 0) {
                out.println(eventBits[5]);
            }
            // 
            out.println("<br />");
        }
        out.println("<br />");
        out.println("</p>");


        // Images (only if not private)
        if (!wh.isPrivate(indi)) {
            List<Property> files = new ArrayList<Property>();
            files.addAll(Arrays.asList(indi.getProperties(INDI2IMAGES)));
            Fam[] families = indi.getFamiliesWhereSpouse();
            for (int i = 0; i < families.length; i++) {
                Fam family = families[i];
                files.addAll(Arrays.asList(family.getProperties(FAM2IMAGES)));
            }
            if (!files.isEmpty()) {
                out.println("<p class=\"image\">");
                for (Iterator it = files.iterator(); it.hasNext();) {
                    PropertyFile file = (PropertyFile) it.next();
                    if ((file == null) || (file.getFile() == null)) {
                        continue;
                    }
                    // get file name
                    String origFile = wh.getCleanFileName(file.getValue(), DEFCHAR);
                    if (wh.isImage(origFile) && wh.scaleImage(file.getFile().getAbsolutePath(), dir.getAbsolutePath() + File.separator + origFile, WIDTH_PICTURES, 0, 100, false)) {
                        out.println(wrapMedia(origFile, name, file));
                    }
                    if (!wh.isImage(origFile)) {
                        out.println(wrapMedia(origFile, name, file));
                    }

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
        wrapName(out, fatherfather);
        out.println("<br />");
        wrapDate(out, fatherfather, false);
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        wrapName(out, motherfather);
        out.println("<br />");
        wrapDate(out, motherfather, false);
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        wrapName(out, fathermother);
        out.println("<br />");
        wrapDate(out, fathermother, false);
        out.println("</p>");
        out.println("<p class=\"parentgp\">");
        wrapName(out, mothermother);
        out.println("<br />");
        wrapDate(out, mothermother, false);
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
        wrapName(out, father);
        out.println("<br />");
        wrapDate(out, father, false);
        out.println("</p>");
        out.println("<p class=\"parentp\">");
        wrapName(out, mother);
        out.println("<br />");
        wrapDate(out, mother, false);
        out.println("</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Siblings
        out.println("<div class=\"conteneur\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + fam_siblings + "</span></p>");
        out.println("<p class=\"parents\">");
        Indi[] osiblings = indi.getOlderSiblings();
        Arrays.sort(osiblings, new PropertyComparator("INDI:BIRT:DATE"));
        for (int i = 0; i < osiblings.length; i++) {
            Indi osibling = osiblings[i];
            wrapSex(out, osibling);
            wrapName(out, osibling);
            wrapDate(out, osibling, true);
            out.println("<br />");
        }
        out.println("<span class=\"grasplus\">");
        wrapSex(out, indi);
        wrapName(out, indi);
        wrapDate(out, indi, true);
        out.println("</span><br />");
        Indi[] ysiblings = indi.getYoungerSiblings();
        Arrays.sort(ysiblings, new PropertyComparator("INDI:BIRT:DATE"));
        for (int i = 0; i < ysiblings.length; i++) {
            Indi ysibling = ysiblings[i];
            wrapSex(out, ysibling);
            wrapName(out, ysibling);
            wrapDate(out, ysibling, true);
            out.println("<br />");
        }
        out.println("<br /></p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Step sibling by father
        boolean hasStepSibFather = false;
        if (wp.param_dispSiblings.equals("1") && father != null) {
            Fam[] stepFamilies = father.getFamiliesWhereSpouse();
            for (int si = 0; si < stepFamilies.length; si++) {
                Fam stepFamily = stepFamilies[si];
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
                    for (int sj = 0; sj < stepSiblings.length; sj++) {
                        Indi stepSbling = stepSiblings[sj];
                        wrapSex(out, stepSbling);
                        wrapName(out, stepSbling);
                        wrapDate(out, stepSbling, true);
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
            for (int si = 0; si < stepFamilies.length; si++) {
                Fam stepFamily = stepFamilies[si];
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
                    for (int sj = 0; sj < stepSiblings.length; sj++) {
                        Indi stepSbling = stepSiblings[sj];
                        wrapSex(out, stepSbling);
                        wrapName(out, stepSbling);
                        wrapDate(out, stepSbling, true);
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
        List xrefList = indi.getProperties(PropertyXRef.class);
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
            out.println("<p class=\"parentf\"><img src=\"" + themeDirLink + getSexIcon(spouse) + "\" />");
            out.println("<span class=\"gras\">" + fam_spouse + "</span>:&nbsp;");
            wrapName(out, spouse);
            wrapDate(out, spouse, true);
            out.println("<br />");
            listEvents = getNameDetails(family);
            for (Iterator s = listEvents.iterator(); s.hasNext();) {
                String event = (String) s.next();     // [0]date . [1]description . [2]source_id . [3]event_tag . [4]media_id . [5] notes
                String[] eventBits = event.split("\\|", -1);
                // eventIcon
                out.println("<img src=\"" + themeDirLink + "ev_" + eventBits[3] + ".png" + "\" alt=\"\"");
                // eventname : date description
                out.println(eventBits[1].trim());
                // [source link]
                if (wp.param_media_GeneSources.equals("1") && eventBits[2].length() != 0) {
                    out.println(eventBits[2].trim());
                }
                // [media link]
                if (wp.param_media_GeneMedia.equals("1") && eventBits[4].length() != 0) {
                    out.println(eventBits[4]);
                }
                // [note link]
                if (eventBits[5].length() != 0) {
                    out.println(eventBits[5]);
                }
                //
                out.println("<br />");
            }
            out.println("</p>");
            Indi[] children = family.getChildren();
            Arrays.sort(children, new PropertyComparator("INDI:BIRT:DATE"));
            if (wp.param_dispKids.equals("1") && children.length > 0) {
                out.println("<p class=\"parentf\">");
                out.println("<p class=\"parentf\"><img src=\"" + themeDirLink + "chld.png\" />");
                out.println("<span class=\"gras\">" + fam_kids + "</span>:<br /></p>");
                out.println("<p class=\"parentfc\">");
                for (int j = 0; j < children.length; j++) {
                    Indi child = children[j];
                    wrapSex(out, child);
                    wrapName(out, child);
                    wrapDate(out, child, true);
                    out.println("<br />");
                }
                out.println("</p>");
            }
            // get step brothers and sisters by looking for other families
            if (wp.param_dispSiblings.equals("1") && spouse != null) {
                Fam[] stepFamilies = spouse.getFamiliesWhereSpouse();
                for (int si = 0; si < stepFamilies.length; si++) {
                    Fam stepFamily = stepFamilies[si];
                    if (stepFamily != family) {
                        Indi[] stepSiblings = stepFamily.getChildren();
                        Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
                        if (stepSiblings.length > 0) {
                            out.println("<p class=\"parentf\"><span class=\"gras\">" + fam_stepkids + "</span>:<br /></p>");
                            out.println("<p class=\"parentfc\">");
                            for (int sj = 0; sj < stepSiblings.length; sj++) {
                                Indi stepSbling = stepSiblings[sj];
                                wrapSex(out, stepSbling);
                                wrapName(out, stepSbling);
                                wrapDate(out, stepSbling, true);
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
        if (wp.param_dispRelations.equals("1") && !wh.isPrivate(indi) && (xrefList.size() > 0)) {
            for (Iterator it = xrefList.iterator(); it.hasNext();) {
                PropertyXRef xref = (PropertyXRef) it.next();
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
                    out.println(htmlText(fam_relhas));
                    Property prop = xref.getTarget().getProperty("RELA");
                    String link = (prop == null) ? fam_relunk : prop.toString().toLowerCase();
                    String event = xref.getParent().getPropertyName().toLowerCase();
                    boolean doubleup = link.indexOf(event) >= 0;
                    PropertyDate date = (PropertyDate) xref.getParent().getProperty("DATE");
                    if (!doubleup) {
                        out.println(htmlText(link));
                        out.println(fam_relat + SPACE + htmlText(event) + ",");
                    } else {
                        out.println(htmlText(link) + ",");
                    }
                    if (date != null) {
                        out.println(fam_relthe + SPACE + htmlText(date.toString().toLowerCase()) + ", ");
                    }
                    out.println(fam_relwhois);
                }
                if (isAsso) {
                    out.println(htmlText(fam_relis));
                    Property prop = xref.getProperty("RELA");
                    String link = (prop == null) ? fam_relunk : prop.toString().toLowerCase();
                    String event = xref.getTarget().getParent().getPropertyName().toLowerCase();
                    boolean doubleup = link.indexOf(event) >= 0;
                    PropertyDate date = (PropertyDate) xref.getTarget().getParent().getProperty("DATE");
                    if (!doubleup) {
                        out.println(htmlText(link));
                        out.println(fam_relat + SPACE + htmlText(event) + ",");
                    } else {
                        out.println(htmlText(link) + ",");
                    }
                    if (date != null) {
                        out.println(fam_relthe + SPACE + htmlText(date.toString().toLowerCase()) + ", ");
                    }
                    out.println(fam_relof);
                }

                if (target instanceof Indi) {
                    Indi indiRel = (Indi) target;
                    wrapName(out, indiRel);
                    wrapDate(out, indiRel, true);
                }

                if (target instanceof Fam) {
                    Fam famRel = (Fam) target;
                    Indi husband = famRel.getHusband();
                    Indi wife = famRel.getWife();
                    wrapName(out, husband);
                    wrapDate(out, husband, true);
                    out.println(SPACE + "+");
                    //out.println(wh.htmlText(famRel.getMarriageDate().toString()));
                    //out.println(")"+SPACE);
                    wrapName(out, wife);
                    wrapDate(out, wife, true);
                }

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
        if (wp.param_dispNotes.equals("1") && !wh.isPrivate(indi)) {
            HashSet notes = getNotes(indi);
            if (notes.size() > 0) {
                for (Iterator it = notes.iterator(); it.hasNext();) {
                    Property note = (Property) it.next();
                    String noteStr = note.toString().trim();
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
                                + " " + htmlText(parent.getParent().getPropertyName().toLowerCase()) + ":" : ":";
                        out.println("<span class=\"undl\">" + (parent.getTag().compareTo(Gedcom.INDI) == 0 ? "" : parentTag + parentNote) + "</span>");
                        out.println("<span class=\"ital\">");
                        out.println(htmlText(noteStr));
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
        HashSet<Property> notes = new HashSet<Property>();
        //getPropertiesRecursively((Property) indi, notes);
        notes.addAll(Arrays.asList(indi.getProperties("NOTE")));

        if (indi != null) {
            Fam[] families = indi.getFamiliesWhereSpouse();
            for (int i = 0; i < families.length; i++) {
                Fam family = families[i];
                //getPropertiesRecursively((Property) family, notes);
                notes.addAll(Arrays.asList(indi.getProperties("NOTE")));
            }
        }
        return notes;
    }

    private void getPropertiesRecursively(Property parent, List<Property> notes) {
        List<Property> props = Arrays.asList(parent.getProperties());
        for (Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property child = it.next();
            if ((child.getTag().compareTo("NOTE") == 0) || (child.getTag().compareTo("TEXT") == 0)) {
                notes.add(child);
            }
            getPropertiesRecursively(child, notes);
        }
    }

    /**
     * Print sex icons of individuals
     */
    private void wrapSex(PrintWriter out, Indi indi) {
        //
        String themeDirLink = buildLinkTheme(this, themeDir);
        String id = (indi == null) ? "" : indi.getId();
        String sexString = "?" + SPACE;
        int iSex = (indi == null) ? 0 : indi.getSex();
        if (iSex == 1) {
            sexString = "<img src=\"" + themeDirLink + "m.gif\" alt=\"" + trs("alt_male") + "\" />";
        } else if (iSex == 2) {
            sexString = "<img src=\"" + themeDirLink + "f.gif\" alt=\"" + trs("alt_female") + "\" />";
        } else {
            sexString = "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";
        }
        out.print(sexString + SPACE);
    }

    /**
     * Calculate pages for individual details
     */
    private void calcPages() {
        String personfile = "", fileStr = "";
        int cpt = 0;
        for (Iterator it = wh.getIndividuals(wh.gedcom, sortIndividuals).iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            cpt++;
            personfile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix;
            if (fileStr.compareTo(personfile) != 0) {
                fileStr = personfile;
            }
            personPage.put(indi.getId(), personfile);
        }
    }

    /**
     * Get individual details
     */
    public List<String> getNameDetails(Entity entity) {

        String ev[] = null;
        if (entity == null) {
            return null;
        }
        if (entity instanceof Indi) {
            ev = events;
        } else if (entity instanceof Fam) {
            ev = eventsMarr;
        } else {
            return null;
        }
        List<String> list = new ArrayList<String>();
        String description = "";
        String date = "";
        for (int i = 0; i < ev.length; i++) {
            Property[] props = entity.getProperties(ev[i]);
            for (int j = 0; j < props.length; j++) {
                // date? (used to sort only)
                Property p = props[j].getProperty("DATE");
                PropertyDate pDate = (p instanceof PropertyDate ? (PropertyDate) p : null);

                if (ev[i].equals("BIRT")) {
                    date = "0-";
                } else if (ev[i].equals("DEAT")) {
                    date = "8-";
                } else if (ev[i].equals("BURI") || ev[i].equals("CREM")) {
                    date = "9-";
                } else {
                    date = "5-";
                }

                if (pDate == null) {
                    date += "-";
                } else {
                    PointInTime pit = null;
                    try {
                        pit = pDate.getStart().getPointInTime(PointInTime.GREGORIAN);
                        date += "";
                        date += pit.getYear();
                        date += pit.getMonth();
                        date += pit.getJulianDay();
                    } catch (GedcomException e) {
                        //e.printStackTrace();
                        //wb.log.write(wb.log.ERROR, "getNameDetails - " + e.getMessage());
                        //wb.log.write(wb.log.ERROR, "getNameDetails - date = " + pDate.getStart());
                        //wb.log.write(wb.log.ERROR, "getNameDetails - entity = " + pDate.getEntity());
                        date += pDate.getStart();
                    }
                }
                // description?
                //   {$t} property tag (doesn't count as matched)
                //   {$T} property name(doesn't count as matched)
                //   {$D} date as fully localized string
                //   {$y} year
                //   {$p} place (city)
                //   {$P} place (all jurisdictions)
                //   {$v} value
                //   {$V} display value
                // format 1 : event name
                String format1 = "<span class=\"gras\"> { $T}: </span>";
                // format 2 : date
                String format2 = (showDate ? "{ $D}" : "");
                // format 3 : description
                String format3 = "{ $V}";
                if (showPlace) {
                    String format = (showAllPlaceJurisdictions ? "{ $P}" : "{ $p}");
                    String juridic = props[j].format(format, wh.getPrivacyPolicy()).trim();
                    if (juridic != null) {
                        format3 += " " + juridic.replaceAll(",", " ");
                    }
                }
                if ("RESI".compareTo(ev[i]) == 0) {
                    Property city = props[j].getProperty(new TagPath(".:ADDR:CITY"));
                    Property ctry = props[j].getProperty(new TagPath(".:ADDR:CTRY"));
                    format3 = " " + ((city == null) ? "" : city.toString() + ", ") + ((ctry == null) ? "" : ctry.toString());
                }
                String format = format1 + format2 + " : " + format3;
                description = props[j].format(format, wh.getPrivacyPolicy()).trim();

                // source?
                String source = "";
                Property[] pSources = props[j].getProperties("SOUR");
                if (pSources != null && pSources.length > 0) {
                    for (int k = 0; k < pSources.length; k++) {
                        if (pSources[k] instanceof PropertySource) {
                            PropertySource pSource = (PropertySource) pSources[k];
                            source += wrapSource(buildLinkTheme(this, themeDir) + "src.gif", pSource);
                        }
                    }
                }
                // event tag in lowercase (will be used for image associated with event for instance)
                String event_tag = props[j].getTag().toLowerCase();

                // media?
                String media = "";
                Property[] pMedias = props[j].getProperties("OBJE");
                if (pMedias != null && pMedias.length > 0) {
                    for (int k = 0; k < pMedias.length; k++) {
                        PropertyFile pFile = (PropertyFile) pMedias[k].getProperty("FILE");
                        media += wrapMedia(buildLinkTheme(this, themeDir) + "media.png", "", pFile);
                    }
                }
                // note?
                String note = "";
                Property[] pNotes = props[j].getProperties("NOTE");
                if (pNotes != null && pNotes.length > 0) {
                    for (int k = 0; k < pNotes.length; k++) {
                        Property pNote = pNotes[k];
                        note += wrapNote(buildLinkTheme(this, themeDir) + "note.png", pNote);
                    }
                }

                // write data (date is used to sort only, description includes the date of the event
                list.add(date + "|" + description + "|" + source + "|" + event_tag + "|" + media + "|" + note);
            }
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Buld source bloc (assuming link to a source entity)
     */
    private String wrapSource(String origFile, PropertySource source) {
        //
        String id = "";
        if (source != null && source.getTargetEntity() != null) {
            id = source.getTargetEntity().getId();
        }
        String link = "";
        String sourceFile = (id == null) ? "" : ((sourcePage == null) ? "" : sourcePage.get(id));
        if (id != null) {
            link = indi2srcDir + sourceFile + '#' + id;
        }
        // display image
        String ret = "<a class=tooltip href=\"" + link + "\">";
        ret += "<img src=\"" + origFile + "\" alt=\"" + id + "\" />";
        ret += "<span>" + htmlText(trs("TXT_src_comment")) + "&nbsp;" + id + "</span></a>";
        return ret;

    }

    /**
     * Buld media bloc (assuming media record included in property)
     */
    private String wrapMedia(String origFile, String name, PropertyFile file) {
        // build title from TITL and NOTE
        Property pProp = file.getParent().getProperty("TITL");
        String title = "<b>" + ((pProp == null || pProp.getValue().trim().isEmpty()) ? htmlText(name) : htmlText(pProp.getValue())) + "</b>";
        pProp = file.getParent().getProperty("NOTE");
        title += (pProp == null || pProp.getValue().trim().isEmpty()) ? "" : ((!title.isEmpty() ? "<br>" : "") + "<i>" + htmlText(pProp.getPropertyName()) + " : " + htmlText(pProp.getValue()) + "</i>");
        if (title.trim().isEmpty()) {
            title = file.getValue();
        }
        // display image
        String ret = "<a class=tooltip href=\"" + indi2mediaDir + wb.sectionMedia.getPageForMedia(file) + "\">";
        ret += "<img src=\"" + wh.getImage(origFile, buildLinkTheme(this, themeDir)) + "\" alt=\"" + name + "\" />";
        ret += "<span>" + title + "</span></a>";
        return ret;
    }

    /**
     * Buld note bloc (assuming note record included in property)
     */
    private String wrapNote(String origFile, Property note) {
        // Get text
        String noteText = "<i>" + ((note == null || note.getValue().trim().isEmpty()) ? "" : htmlText(note.getValue())) + "</i>";
        // display note
        String ret = "<a class=tooltip \">";
        ret += "<img src=\"" + origFile + "\" />";
        ret += "<span>" + noteText + "</span></a>";
        return ret;
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
        out.println("<script type=\"text/javascript\">");
        out.println("<!--");
        out.println("function popup(sText)");
        out.println("{");
        out.println("window.open( \"" + POPUPTAG + "?\"+sText, '', 'HEIGHT=650,WIDTH=620,toolbar=0,status=0,menubar=0');");
        out.println("}");
        out.println("//-->");
        out.println("</script>");

    }
} // End_of_Report

