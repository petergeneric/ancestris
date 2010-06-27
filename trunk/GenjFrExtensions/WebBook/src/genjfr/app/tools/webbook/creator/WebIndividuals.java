package genjfr.app.tools.webbook.creator;

import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebIndividuals extends WebSection {

    /**
     * Constructor
     */
    public WebIndividuals(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Individualslist"), "persons", "persons_", formatFromSize(wh.getNbIndis()), ".html", 1, sizeIndiSection * 2);
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        calcLetters(wh.getIndividuals(wh.gedcom, sortIndividuals));

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir, wh.getIndividuals(wh.gedcom, sortIndividuals));

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir, List<Indi> indis) {

        // cpt counts the individuals to generate the links to the individuals details pages
        // iNames counts the different lastnames (not individuals) to be consistent with lastname page links
        // We have a change of letter every time the lastname anchored converted string changes
        // Therefore we need to detect both change of anchor and change of lastnames (not necessarily the same)
        char previousLetter = ' ';
        String previousAnchorLastName = "";
        String previousLastName = "";
        String previousListFile = "";

        File file = null;
        PrintWriter out = null;
        boolean writeLetter = false;
        boolean writeAnchor = false;
        boolean first = true;

        int cpt = 0;
        int iNames = 0,
                nbNames = wh.getLastNames(DEFCHAR, sortLastnames).size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbNames / nbPerPage) + 1;
        String listfile = "";

        // Go through individuals
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            cpt++;

            // Check if need to increment lastname
            String lastName = wh.getLastName(indi, DEFCHAR);
            if (lastName.compareTo(previousLastName) != 0) {
                previousLastName = lastName;
                iNames++;
            }

            // Check if need to write anchor letter
            String anchorLastName = htmlAnchorText(lastName);
            char cLetter = anchorLastName.charAt(0);
            if (cLetter != previousLetter) {
                previousLetter = cLetter;
                writeLetter = true;
            } else {
                writeLetter = false;
            }

            // Check if need to write anchor lastname
            if (anchorLastName.compareTo(previousAnchorLastName) != 0) {
                previousAnchorLastName = anchorLastName;
                writeAnchor = true;
            } else {
                writeAnchor = false;
            }

            currentPage = (iNames / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            listfile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (previousListFile.compareTo(listfile) != 0) {
                if (out != null) {
                    out.println("</p>");
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 1, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    wh.log.write(previousListFile + trs("EXEC_DONE"));
                    out.close();
                }
                previousListFile = listfile;
                file = wh.getFileForName(dir, listfile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Individualslist", this);
                out.println("<p class=\"letters\">");
                out.println("<br /><br />");
                for (Letters l : Letters.values()) {
                    if (checkLink(l.toString())) {
                        out.println("<a href=\"" + linkForLetter.get(l.toString()) + "#" + l + "\">" + l + "</a>" + SPACE + SPACE);
                    } else {
                        out.println(l + SPACE + SPACE);
                    }
                }
                if (checkLink(DEFCHAR)) {
                    out.println("<a href=\"" + linkForLetter.get(DEFCHAR) + "#" + DEFCHAR + "\">" + DEFCHAR + "</a>" + SPACE + SPACE);
                } else {
                    out.println(DEFCHAR + SPACE + SPACE);
                }
                out.println("<br /><br /></p>");
                if (!writeLetter) {
                    exportLinks(out, listfile, 1, previousPage, nextPage, lastPage);
                    out.println("<p>");
                }
            }

            String personfile = buildLink(this, wb.sectionIndividualsDetails, cpt);
            if (writeLetter) {
                if (!first) {
                    out.println("</p>");
                }
                first = false;
                exportLinks(out, listfile, 1, previousPage, nextPage, lastPage);
                String ancLet = String.valueOf(previousLetter);
                if (!(ancLet.matches("[a-zA-Z]"))) {
                    ancLet = DEFCHAR;
                }
                out.println("<p class=\"letter\">" + "<a name=\"" + ancLet + "\"></a>" + ancLet + "</p>");
                out.println("<p>");
            }
            exportIndividualRow(out, indi, writeAnchor, personfile);
            // .. next individual
        }

        if (out != null) {
            out.println("</p>");
            exportLinks(out, listfile, 1, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(previousListFile + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }

    }

    /**
     * Exports individual row
     */
    private void exportIndividualRow(PrintWriter out, Indi indi, boolean writeAnchor, String personfile) {

        String NODATE = SPACE;

        String sexString = DEFCHAR + SPACE;
        String themeDirLink = buildLinkTheme(this, themeDir);

        if (indi.getSex() == 1) {
            sexString = "<img src=\"" + themeDirLink + "m.gif\" alt=\"" + trs("alt_male") + "\" />";
        } else if (indi.getSex() == 2) {
            sexString = "<img src=\"" + themeDirLink + "f.gif\" alt=\"" + trs("alt_female") + "\" />";
        } else {
            sexString = "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";
        }

        PropertyDate birthDateProp = (PropertyDate) indi.getProperty(new TagPath("INDI:BIRT:DATE"));
        String birthDateString = "";
        if (birthDateProp == null) {
            birthDateString = NODATE;
        } else {
            birthDateString = htmlText(birthDateProp.toString());
        }

        PropertyDate deathDateProp = (PropertyDate) indi.getProperty(new TagPath("INDI:DEAT:DATE"));
        String deathDateString = "";
        if (deathDateProp == null) {
            deathDateString = NODATE;
        } else {
            deathDateString = htmlText(deathDateProp.toString());
        }

        String name = wh.getLastName(indi, DEFCHAR);
        String first = indi.getFirstName();
        String anchor = indi.getId();

        if (writeAnchor) {
            out.println("<a name=\"" + htmlAnchorText(name) + "\"></a>");
        }

        if (wh.isPrivate(indi)) {
            name = "...";
            first = "...";
            birthDateString = "...";
            deathDateString = "...";
        }

        out.println(sexString + SPACE);
        out.print("<a href=\"" + personfile + '#' + anchor + "\">");
        out.println(htmlText(name + ", " + first));
        out.print("</a>");
        out.println(SPACE + "(" + SPACE + birthDateString + SPACE + "-" + SPACE + deathDateString + SPACE + ")");
        out.println("<br />");
    }

    /**
     * Calculate if there is a link to the letters and initiates the names pages
     */
    private void calcLetters(List<Indi> indis) {

        // Initialise to zero
        linkForLetter.put(DEFCHAR, "0");
        for (Letters l : Letters.values()) {
            linkForLetter.put(l.toString(), "0");
        }

        // Calculate
        char letter = ' ';
        String name = "";
        boolean writeLetter = false;
        int iNames = 0;
        int currentPage = 0;
        String listfile = "";
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            String lastname = wh.getLastName(indi, DEFCHAR);
            String str = htmlAnchorText(lastname);
            if (str == null) {
                continue;
            }
            if (str.compareTo(name) != 0) {
                listfile = sectionPrefix + String.format(formatNbrs, (iNames / nbPerPage) + 1) + sectionSuffix;
                namePage.put(str, listfile);
                name = str;
                iNames++;
                char cLetter = str.charAt(0);
                if (cLetter != letter) {
                    letter = cLetter;
                    String l = String.valueOf(letter);
                    linkForLetter.put(l, listfile);
                }
            }
        }
    }

    /**
     * Provide links map to outside caller
     */
    public Map getPagesMap() {
        return namePage;
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

