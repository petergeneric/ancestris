/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Ancestris
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebStatsImplex extends WebSection {

    private static final boolean DEBUG = false;
    private static final String STACK_SEPARATOR = "\n";
    private double dImplexFactor;
    private final List<GenerationInfo> vecGenerationInfo = new ArrayList<>();
    private final HashSet<String> setIndi = new HashSet<>();
    private final HashSet<String> setCommonAncestor = new HashSet<>();
    private final TreeMap<String, Indi> mapImplexCommonIndi = new TreeMap<>();
    private double dConsanguinityFactor;
    private final TreeMap<String, ConsanguinityInfo> mapConsanguinityCommonIndi = new TreeMap<>();

    private class GenerationInfo {

        int iLevel;
        BigInteger iPossibleCount;
        BigInteger iPossibleCumul;
        int iKnownCount;
        int iKnownCumul;
        int iDiffCount;
        int iDiffCumul;
        double dCoverage;
        double dCoverageCumul;
        double dImplex;

        GenerationInfo(int iLevel) {
            this.iLevel = iLevel;
        }
    }

    private class ConsanguinityInfo {

        public Indi indi;
        public int count;
        public double consanguinityFactor;
        public Stack<String> stackIndi = new Stack<>();
    }

    /**
     * Constructor
     */
    public WebStatsImplex(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_StatsImplex"), "stats", "stats_", formatFromSize(wh.getNbIndis()), 2, 0);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // Generate links to the section below
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        // Opens page
        String fileStr = sectionPrefix + String.format(formatNbrs, 2) + sectionSuffix;
        File file = wh.getFileForName(dir, fileStr);
        try (PrintWriter out = wh.getWriter(file, UTF8)) {
            if (out == null) {
                return;
            }
            printOpenHTML(out, "TXT_StatsImplex", this);
            printHomeLink(out, this);
            
            // Initialize statistics if the report is executed several times
            clearStats();
            
            // Compute the implex factor
            computeImplexFactor(wh.getIndiDeCujus(wp.param_decujus));
            
            // Compute the consanguinity factor
            computeConsanguinityFactor(wh.indiDeCujus);
            
            // Print header
            printHeader(out, wh.indiDeCujus);
            
            // Print implexe statistics
            printImplexStats(out);
            
            // Print consanguinity statistics
            printConsanguinityStats(out);
            
            // Closes page
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

    }

    /**
     * Initialize statistics.
     */
    private void clearStats() {
        // Clear implex statistics
        dImplexFactor = 0;
        vecGenerationInfo.clear();
        setIndi.clear();
        setCommonAncestor.clear();
        mapImplexCommonIndi.clear();

        // Clear consanguinity statistics
        dConsanguinityFactor = 0;
        mapConsanguinityCommonIndi.clear();
    }

    /**
     * Print report header.
     *
     * @param indi Root individual.
     */
    private void printHeader(PrintWriter out, Indi indi) {

        // Print description
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("implex_description")) + "</span></p>");

        out.println("<p class=\"description\">" + htmlText(trs("implex_info")) + "</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        // Print root individual
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("implex_root_individual")) + "</span></p>");
        out.println("<p class=\"column1\">");
        out.println(wrapEntity(indi));
        out.println("<br /><br />");
        out.println(htmlText(trs("implex_implex_factor")) + SPACE + dImplexFactor + "%");
        out.println("<br />");
        out.println(htmlText(trs("implex_consanguinity_factor")) + SPACE + String.format("%.9f", dConsanguinityFactor));
        out.println("<br /></p>");
    }

    /**
     * Print implexe statistics.
     */
    private void printImplexStats(PrintWriter out) {
        // Print header
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"column1\"><thead><tr>");
        out.println("<th>" + htmlText(trs("implex_header_implex_generation")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_possible")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_known")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_known_percent")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_cumul")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_cumul_percent")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_diff")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_diffcumul")) + "</th>");
        out.println("<th>" + htmlText(trs("implex_header_implex_implex")) + "</th>");
        out.println("</tr></thead>");

        // Iteration on generations
        out.println("<tbody>");
        Iterator<GenerationInfo> itr = vecGenerationInfo.iterator();
        while (itr.hasNext()) {
            GenerationInfo info = itr.next();

            // Print line
            out.println("<tr>");
            out.println("<td>" + htmlText(info.iLevel) + "</td>");
            out.println("<td>" + htmlText(info.iPossibleCount) + "</td>");
            out.println("<td>" + htmlText(info.iKnownCount) + "</td>");
            out.println("<td>" + htmlText(info.dCoverage) + "</td>");
            out.println("<td>" + htmlText(info.iKnownCumul) + "</td>");
            out.println("<td>" + htmlText(info.dCoverageCumul) + "</td>");
            out.println("<td>" + htmlText(info.iDiffCount) + "</td>");
            out.println("<td>" + htmlText(info.iDiffCumul) + "</td>");
            out.println("<td>" + htmlText(info.dImplex) + "</td>");
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("<div class=\"spacer\">" + SPACE + "</div></div>");

        // Print table of common individuals
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("implex_header_implex_common_ancestors")) + "</span></p>");
        out.println("<p class=\"column1\">");
        // Scan common individuals
        Collection<Indi> col = mapImplexCommonIndi.values();
        Iterator<Indi> itr1 = col.iterator();
        while (itr.hasNext()) {
            out.println(wrapEntity(itr1.next()));
            out.println("<br />");
        }
        out.println("</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

    }

    /**
     * Print consanguinity statistics.
     */
    private void printConsanguinityStats(PrintWriter out) {
        // Print list header
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("implex_header_consanguinity_common_ancestors")) + "</span></p>");

        // Scan common individuals
        Collection<ConsanguinityInfo> col = mapConsanguinityCommonIndi.values();
        List<ConsanguinityInfo> list = new ArrayList<>(col);
        Collections.sort(list, new Comparator<ConsanguinityInfo>(){
            
            @Override
            public int compare(ConsanguinityInfo o1, ConsanguinityInfo o2) {
                return Double.compare(o1.consanguinityFactor, o2.consanguinityFactor) * (-1);
            }
        } );
        for (ConsanguinityInfo info : list) {
            out.println("<span class=\"column1f\">");
            out.println(wrapEntity(info.indi));
            out.println("</span>");
            out.println("<span class=\"column2f\">" + htmlText(String.format("%.9f", info.consanguinityFactor)) + "</span><br />");
            out.println("<p class=\"spacer\">" + SPACE + "</p>");
        }
        out.println("</div>");
    }

    /**
     * Compute the implex factor.
     *
     * @param indi Root individual.
     */
    @SuppressWarnings("unchecked")
    private void computeImplexFactor(Indi indi) {
        // Initialize the first generation with the selected individual
        List<Indi> listIndi = new ArrayList<>();
        listIndi.add(indi);

        // Compute statistics one generation after the other
        int iLevel = 1;
        while (!listIndi.isEmpty()) {
            List<Indi> listParent = new ArrayList<>();
            computeGeneration(iLevel, listIndi, listParent);
            listIndi = listParent;
            iLevel++;
        }

        // Compute cumul statistics
        BigInteger iPossibleCumul = BigInteger.ZERO;
        int iKnownCumul = 0;
        int iDiffCumul = 0;
        Iterator<GenerationInfo> itr = vecGenerationInfo.iterator();
        while (itr.hasNext()) {
            GenerationInfo info = itr.next();

            // Compute possible
            info.iPossibleCount = BigInteger.ONE.shiftLeft(info.iLevel - 1);

            // Compute cumuls
            iPossibleCumul = iPossibleCumul.add(info.iPossibleCount);
            iKnownCumul += info.iKnownCount;
            iDiffCumul += info.iDiffCount;

            // Store cumuls
            info.iPossibleCumul = iPossibleCumul;
            info.iKnownCumul = iKnownCumul;
            info.iDiffCumul = iDiffCumul;

            // Compute coverage
            BigInteger dC = BigInteger.valueOf(10000 * info.iKnownCount);
            info.dCoverage = (dC.divide(info.iPossibleCount)).longValue() / 100d;
            dC = BigInteger.valueOf(10000 * info.iKnownCumul);
            info.dCoverageCumul = (dC.divide(info.iPossibleCumul)).longValue() / 100d;

            // Compute implex
            if (iKnownCumul != 0) {
                info.dImplex = (10000 * (info.iKnownCumul - info.iDiffCumul) / info.iKnownCumul) / 100d;
                dImplexFactor = info.dImplex;
            }
        }
    }

    /**
     * Add an individual and all its ancestors in the common ancestor list.
     *
     * @param indi Common ancestor.
     */
    @SuppressWarnings("unchecked")
    private void addCommonAncestor(Indi indi) {
        if (indi == null) {
            return;
        }

        // Add individual to the list
        String strId = indi.getId();
        setCommonAncestor.add(strId);

        // Add parents to the list
        Fam famc = indi.getFamilyWhereBiologicalChild();
        if (famc != null) {
            addCommonAncestor(famc.getWife());
            addCommonAncestor(famc.getHusband());
        }
    }

    /**
     * Computes statistics for the specified generation.
     *
     * @param iLevel Current generation level.
     * @param listIndi Individuals of a generation.
     * @param listParent [return] Individuals of the next generation.
     */
    private void computeGeneration(int iLevel, List<Indi> listIndi, List<Indi> listParent) {
        // Prepare generation information
        GenerationInfo info = new GenerationInfo(iLevel);
        vecGenerationInfo.add(info);

        // Scan individual of the generation
        Iterator<Indi> itr = listIndi.iterator();
        while (itr.hasNext()) {
            Indi indi = itr.next();

            // Get ancestor ID and search it in the list
            String strId = indi.getId();
            if (setIndi.contains(strId)) {
                // Check if this indivual has already been listed
                if (!setCommonAncestor.contains(strId)) {
                    // Print individual description
                    mapImplexCommonIndi.put(strId, indi);

                    // Add individual and its ancestors to the list
                    addCommonAncestor(indi);
                }
            } else {
                // This is a new ancestor
                setIndi.add(strId);
                info.iDiffCount++;
            }

            // Count this ancestor in all case
            info.iKnownCount++;

            // Get parents
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                // Get mother
                Indi indiWife = famc.getWife();
                if (indiWife != null) {
                    listParent.add(indiWife);
                }

                // Get father
                Indi indiHusband = famc.getHusband();
                if (indiHusband != null) {
                    listParent.add(indiHusband);
                }
            }
        }
    }

    /**
     * Compute consanguinity factor.
     *
     * @param indi Root individual.
     */
    private void computeConsanguinityFactor(Indi indi) {
        // Initialize value
        dConsanguinityFactor = 0;

        // If no family, nothing to do
        Fam famc = indi.getFamilyWhereBiologicalChild();
        if (famc == null) {
            return;
        }

        Stack<String> vecWife = new Stack<>();
        Stack<String> vecHusband = new Stack<>();
        checkRightTree(famc.getWife(), 0, vecWife, famc.getHusband(), 0, vecHusband);
    }

    /**
     * Check the ancestors of one parent to compute the consanguinity factor.
     *
     * @param indiRight Current individual.
     * @param iLevelRight Current generation level.
     * @param stackRight Current ancestor list.
     * @param indiLeft Current individual of other the tree.
     * @param iLevelLeft Current generation level of other the tree.
     * @param stackLeft Current ancestor list of other the tree.
     */
    @SuppressWarnings("unchecked")
    private void checkRightTree(Indi indiRight, int iLevelRight, Stack<String> stackRight,
            Indi indiLeft, int iLevelLeft, Stack<String> stackLeft) {
        // Exit if an individual is missing
        if (indiRight == null || indiLeft == null) {
            return;
        }

        // There is consanguinity only if an individual appears in father and mother tree.
        // Search if this individual appears in the other tree
        searchInLeftTree(indiRight, iLevelRight, stackRight, indiLeft, 0, stackLeft);

        // Add individual ID to ancestor stack
        String strIdRight = indiRight.getId();
        stackRight.push(strIdRight);

        // If no family, nothing to do
        Fam famc = indiRight.getFamilyWhereBiologicalChild();
        if (famc != null) {
            // Continue to check the tree
            // Recursive call to mother and father
            checkRightTree(famc.getWife(), iLevelRight + 1, stackRight,
                    indiLeft, iLevelLeft, stackLeft);
            checkRightTree(famc.getHusband(), iLevelRight + 1, stackRight,
                    indiLeft, iLevelLeft, stackLeft);
        }

        // Remove individual ID from ancestor stack
        stackRight.pop();
    }

    /**
     * Search reference individual in the the ancestors of second parent.
     *
     * @param indiRight Reference individual.
     * @param iLevelRight Reference generation level.
     * @param stackRight Reference ancestor.
     * @param indiLeft Current individual.
     * @param iLevelLeft Current generation level.
     * @param stackLeft Current ancestor list.
     */
    @SuppressWarnings("unchecked")
    private void searchInLeftTree(Indi indiRight, int iLevelRight, Stack<String> stackRight,
            Indi indiLeft, int iLevelLeft, Stack<String> stackLeft) {
        // Exit if an individual is missing
        if (indiRight == null || indiLeft == null) {
            return;
        }

        // Do not check further if this individual is in the ancestor stack
        // on the other tree.
        String strIdLeft = indiLeft.getId();
        if (stackRight.contains(strIdLeft)) {
            return;
        }

        // Get ID of the reference individual and check if the current individual
        // is the same.
        String strIdRight = indiRight.getId();
        if (strIdRight.equals(strIdLeft)) {
            // Check if indivividual is already in list
            ConsanguinityInfo info = mapConsanguinityCommonIndi.get(strIdRight);
            if (info == null) {
                // Create info about individual
                info = new ConsanguinityInfo();
                info.indi = indiRight;
            }

            // Save ancestor list in debug mode
            if (DEBUG) {
                Iterator<String> itrStack = stackRight.iterator();
                while (itrStack.hasNext()) {
                    info.stackIndi.push(itrStack.next());
                }
                info.stackIndi.push(STACK_SEPARATOR);

                itrStack = stackLeft.iterator();
                while (itrStack.hasNext()) {
                    info.stackIndi.push(itrStack.next());
                }
                info.stackIndi.push(STACK_SEPARATOR);
            }

            // Add this common person in list
            mapConsanguinityCommonIndi.put(strIdRight, info);

            // Compute coefficient of consanguinity for this case
            double dPower = iLevelRight + iLevelLeft + 1;
            double dConsanguinityPart = Math.pow(0.5, dPower);
            dConsanguinityFactor += dConsanguinityPart;
            info.consanguinityFactor += dConsanguinityPart;
            info.count++;
            return;
        }

        // Add individual ID to ancestor stack
        stackLeft.push(strIdLeft);

        // If no family, nothing to do
        Fam famc = indiLeft.getFamilyWhereBiologicalChild();
        if (famc != null) {
            // Recursive call to mother and father
            searchInLeftTree(indiRight, iLevelRight, stackRight,
                    famc.getWife(), iLevelLeft + 1, stackLeft);
            searchInLeftTree(indiRight, iLevelRight, stackRight,
                    famc.getHusband(), iLevelLeft + 1, stackLeft);
        }

        // Remove individual ID from ancestor stack
        stackLeft.pop();
    }
} // End_of_Report

