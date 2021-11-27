package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.Indi;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ancestris
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebIndividuals extends WebSection {
 
     final List<String> lastNames = wh.getLastNames(DEFCHAR, sortLastnames);

    /**
     * Constructor
     */
    public WebIndividuals(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Individualslist"), "persons", "persons_", formatFromSize(wh.getNbIndis()), 1, sizeIndiSection * 2);
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        // Preliminary build of individuals link for links from details to individuals
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        calcLetters();

        final File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {
        
        List<Indi> indis = wh.getIndividuals(wh.gedcom, sortIndividuals);

        // cpt counts the individuals to generate the links to the individuals details pages
        // iNames counts the different lastnames (not individuals) to be consistent with lastname page links
        // We have a change of letter every time the lastname anchored converted string changes
        // Therefore we need to detect both change of anchor and change of lastnames (not necessarily the same)
        char previousLetter = ' ';
        String previousLastName = "";
        String previousListFile = "";

        File file;
        PrintWriter out = null;
        boolean writeLetter;
        boolean writeAnchor;
        boolean first = true;

        int iNames = -1; // Begin at -1 because upgrade before print and when calcultating letters, print before upgrade numbers.
        // Allow also to have the correct number of names at the first page.
       
        final Set<String> anchorNames = new HashSet<>();
        lastNames.forEach(name -> {
            anchorNames.add(htmlAnchorText(name));
        });
        int nbNames = anchorNames.size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbNames / nbPerPage) + 1;
        String listfile = "";

        // Go through individuals
        for (Indi indi : indis) {
            // Check if need to increment lastname
            String lastName = wh.getLastName(indi, DEFCHAR);
            String anchorLastName = htmlAnchorText(lastName);
            if (anchorLastName.compareTo(previousLastName) != 0) {
                previousLastName = anchorLastName;
                iNames++;
                writeAnchor = true;
            } else {
                writeAnchor = false;
            }

            // Check if need to write anchor letter
            char cLetter = anchorLastName.charAt(0);
            if (cLetter != previousLetter) {
                previousLetter = cLetter;
                writeLetter = true;
            } else {
                writeLetter = false;
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
            if (out != null) {
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
                    out.println("<p class=\"letter\">" + "<a id=\"" + ancLet + "\"></a>" + ancLet + "</p>");
                    out.println("<p>");
                }
                if (writeAnchor) {
                    out.println("<a id=\"" + htmlAnchorText(wh.getLastName(indi, DEFCHAR)) + "\"></a>");
                }
                out.println(wrapEntity(indi));
                out.println("<br />");
            }
            // .. next individual
        }

        if (out != null) {
            out.println("</p>");
            exportLinks(out, listfile, 1, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(previousListFile + trs("EXEC_DONE"));
            out.close();
        }
    }

    /**
     * Calculate if there is a link to the letters and initiates the names pages
     */
    private void calcLetters() {

        // Initialise to zero
        linkForLetter.put(DEFCHAR, "0");
        for (Letters l : Letters.values()) {
            linkForLetter.put(l.toString(), "0");
        }

        // Calculate
        char letter = ' ';
        String name = "";
        int iNames = 0;
        for (String lastname : lastNames) {
            final String str = htmlAnchorText(lastname);
            if (str == null) {
                continue;
            }
            if (str.compareTo(name) != 0) {
                final String listfile = sectionPrefix + String.format(formatNbrs, (iNames / nbPerPage) + 1) + sectionSuffix;
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
    public Map<String, String> getPagesMap() {
        return namePage;
    }

    /**
     * Booleanise existance of link to a letter
     */
    private boolean checkLink(String str) {
        String flag = linkForLetter.get(str);
        return !(flag == null || flag.compareTo("0") == 0);
    }
}
