/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjreports.webbook;

import genj.report.Report;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Entity;
import genj.util.swing.Action2;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyComparator;


import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.lang.Class;

import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebStatsImplex {

  private ReportWebBook report = null;
  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private WebSection sectionList = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";

  private String themeDir = "";
  private Map<String, String> personPage = new TreeMap<String, String>();
  private WebIndividualsDetails reportIndi = null;
  private String here2indiDir = "";

  private static final boolean DEBUG = false;
  private static final String STACK_SEPARATOR = "\n";

  private double dImplexFactor;
  Vector vecGenerationInfo = new Vector();
  private HashSet setIndi = new HashSet();
  private HashSet setCommonAncestor = new HashSet();
  private TreeMap mapImplexCommonIndi = new TreeMap();

  private double dConsanguinityFactor;
  private TreeMap mapConsanguinityCommonIndi = new TreeMap();

  private class GenerationInfo {
        int iLevel;

        int iPossibleCount;
        int iKnownCount;
        int iDiffCount;

        int iPossibleCumul;
        int iKnownCumul;
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
      public Stack stackIndi = new Stack();
  }


  /**
   * Constructor
   */
  public WebStatsImplex(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run(Indi indi, WebIndividualsDetails reportIndi, WebSection webSectionIndi) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportIndi = reportIndi;
    if (reportIndi != null) {
       personPage = reportIndi.getPagesMap();
       here2indiDir = wh.buildLinkShort(section, webSectionIndi);
       }

    exportData(dir, indi);

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, Indi indi) {

    // Opens page
    String fileStr = section.sectionPrefix + String.format(section.formatNbrs, 2) + section.sectionSuffix;
    File file = wh.getFileForName(dir, fileStr);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return; 
    wh.printOpenHTML(out, "StatsImplex", section);
    wh.printHomeLink(out, section);

    // Initialize statistics if the report is executed several times
    clearStats();

    // Compute the implex factor
    computeImplexFactor(indi);

    // Compute the consanguinity factor
    computeConsanguinityFactor(indi);

    // Print header
    printHeader(out, indi);

    // Print implexe statistics
    printImplexStats(out);

    // Print consanguinity statistics
    printConsanguinityStats(out, indi);

    // Closes page
    wh.printCloseHTML(out);
    report.println(fileStr+" - Done.");
    out.close();

  }

  /**
   * Print name with link
   */
  private String wrapName(Indi indi) {
     //
     String output = "";
     String id = (indi == null) ? "" : indi.getId();
     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
     String personFile = (indi == null) ? "" : ( (personPage == null) ? "" : (String)personPage.get(id));
     if (indi != null) {
        output = "<a href=\""+here2indiDir+personFile+'#'+id+"\">";
        }
     if (wh.isPrivate(indi)) {
        name = "..., ...";
        }
     if (name.compareTo(",") == 0) name = ""; 
     output += wh.htmlText(name);
     String sosa = wh.getSosa(indi);
     if (sosa != null && sosa.length() != 0) {
        output += SPACE+"("+sosa+")";
        }
     if (report.getDisplayId() && id != null && id.length() != 0) {
        output += SPACE+"("+id+")";
        }
     if (indi != null) {
        output += "</a>";
        }
     return output;
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
     * @param indi  Root individual.
     */
    private void printHeader(PrintWriter out, Indi indi) {

        // Print description
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("implex_description"))+"</span></p>");

        out.println("<p class=\"description\">"+wh.htmlText(report.translate("implex_info"))+"</p>");
        out.println("<div class=\"spacer\">"+SPACE+"</div>");
        out.println("</div>");

        // Print root individual
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("implex_root_individual"))+"</span></p>");
        out.println("<p class=\"column1\">");
        if (wh.isPrivate(indi)) {
           out.println("..., ...");
           }
        else {
           out.println(wrapName(indi));
           }
        out.println("<br />");
        out.println(wh.htmlText(report.translate("implex_implex_factor", new Double(dImplexFactor))));
        out.println("<br />");
        out.println(wh.htmlText(report.translate("implex_consanguinity_factor", new Double(dConsanguinityFactor))));
        out.println("<br /></p>");
    }

    /**
     * Print implexe statistics.
     */
    private void printImplexStats(PrintWriter out) {
        // Print header
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"column1\"><thead><tr>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_generation"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_possible"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_known"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_known_percent"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_cumul"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_cumul_percent"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_diff"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_diffcumul"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("implex_header_implex_implex"))+"</th>");
        out.println("</tr></thead>");

        // Iteration on generations
        out.println("<tbody>");
        Iterator itr = vecGenerationInfo.iterator();
        while (itr.hasNext()) {
            GenerationInfo info = (GenerationInfo) itr.next();

            // Print line
            out.println("<tr>");
            out.println("<td>"+wh.htmlText(info.iLevel)+"</td>");
            out.println("<td>"+wh.htmlText(info.iPossibleCount)+"</td>");
            out.println("<td>"+wh.htmlText(info.iKnownCount)+"</td>");
            out.println("<td>"+wh.htmlText(info.dCoverage)+"</td>");
            out.println("<td>"+wh.htmlText(info.iKnownCumul)+"</td>");
            out.println("<td>"+wh.htmlText(info.dCoverageCumul)+"</td>");
            out.println("<td>"+wh.htmlText(info.iDiffCount)+"</td>");
            out.println("<td>"+wh.htmlText(info.iDiffCumul)+"</td>");
            out.println("<td>"+wh.htmlText(info.dImplex)+"</td>");
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("<div class=\"spacer\">"+SPACE+"</div></div>");

        // Print table of common individuals
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("implex_header_implex_common_ancestors"))+"</span></p>");
        out.println("<p class=\"column1\">");
        // Scan common individuals
        Collection col = mapImplexCommonIndi.values();
        itr = col.iterator();
        while (itr.hasNext()) {
            out.println(wrapName((Indi)itr.next())+"<br />");
        }
        out.println("</p>");
        out.println("<div class=\"spacer\">"+SPACE+"</div>");
        out.println("</div>");

    }

    /**
     * Print consanguinity statistics.
     */
    private void printConsanguinityStats(PrintWriter out, Indi indi) {
        // Print list header
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("implex_header_consanguinity_common_ancestors"))+"</span></p>");

        // Scan common individuals
        Collection col = mapConsanguinityCommonIndi.values();
        Iterator itr = col.iterator();
        while (itr.hasNext()) {
            ConsanguinityInfo info = (ConsanguinityInfo) itr.next();
            out.println("<span class=\"column1f\">"+wrapName(info.indi)+"</span>");
            out.println("<span class=\"column2f\">"+wh.htmlText(info.consanguinityFactor)+"</span><br />");
            out.println("<p class=\"spacer\">"+SPACE+"</p>");
            }
        out.println("</div>");
        }




    /**
     * Compute the implex factor.
     * @param indi  Root individual.
     */
    private void computeImplexFactor(Indi indi) {
        // Initialize the first generation with the selected individual
        List listIndi = new ArrayList();
        listIndi.add(indi);

        // Compute statistics one generation after the other
        int iLevel = 1;
        while (!listIndi.isEmpty()) {
            List listParent = new ArrayList();
            computeGeneration(iLevel, listIndi, listParent);
            listIndi = listParent;
            iLevel++;
        }

        // Compute cumul statistics
        int iPossibleCumul = 0;
        int iKnownCumul = 0;
        int iDiffCumul = 0;
        Iterator itr = vecGenerationInfo.iterator();
        while (itr.hasNext()) {
            GenerationInfo info = (GenerationInfo) itr.next();

            // Compute possible
            info.iPossibleCount = (int) Math.pow(2.0f, info.iLevel - 1);

            // Compute cumuls
            iPossibleCumul += info.iPossibleCount;
            iKnownCumul += info.iKnownCount;
            iDiffCumul += info.iDiffCount;

            // Store cumuls
            info.iPossibleCumul = iPossibleCumul;
            info.iKnownCumul = iKnownCumul;
            info.iDiffCumul = iDiffCumul;

            // Compute coverage
            info.dCoverage = (10000 * info.iKnownCount / info.iPossibleCount) / 100d;
            info.dCoverageCumul = (10000 * info.iKnownCumul / info.iPossibleCumul) / 100d;

            // Compute implex
            if (iKnownCumul != 0) {
                info.dImplex = (10000 * (info.iKnownCumul - info.iDiffCumul) / info.iKnownCumul) / 100d;
                dImplexFactor = info.dImplex;
            }
        }
    }

    /**
     * Add an individual and all its ancestors in the common ancestor list.
     * @param indi   Common ancestor.
     */
    private void addCommonAncestor(Indi indi) {
        if (indi == null)
            return;

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
     * @param iLevel       Current generation level.
     * @param listIndi     Individuals of a generation.
     * @param listParent   [return] Individuals of the next generation.
     */
    private void computeGeneration(int iLevel, List listIndi, List listParent) {
        // Prepare generation information
        GenerationInfo info = new GenerationInfo(iLevel);
        vecGenerationInfo.add(info);

        // Scan individual of the generation
        Iterator itr = listIndi.iterator();
        while (itr.hasNext()) {
            Indi indi = (Indi) itr.next();

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
            }
            else {
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
                if (indiWife != null)
                    listParent.add(indiWife);

                // Get father
                Indi indiHusband = famc.getHusband();
                if (indiHusband != null)
                    listParent.add(indiHusband);
            }
        }
    }

    /**
     * Compute consanguinity factor.
     * @param indi  Root individual.
     */
    private void computeConsanguinityFactor(Indi indi) {
        // Initialize value
        dConsanguinityFactor = 0;

        // If no family, nothing to do
        Fam famc = indi.getFamilyWhereBiologicalChild();
        if (famc == null)
            return;

        Stack vecWife = new Stack();
        Stack vecHusband = new Stack();
        checkRightTree(famc.getWife(), 0, vecWife, famc.getHusband(), 0, vecHusband);
    }

    /**
     * Check the ancestors of one parent to compute the consanguinity factor.
     * @param indiRight    Current individual.
     * @param iLevelRight  Current generation level.
     * @param stackRight   Current ancestor list.
     * @param indiLeft     Current individual of other the tree.
     * @param iLevelLeft   Current generation level of other the tree.
     * @param stackLeft    Current ancestor list of other the tree.
     */
    private void checkRightTree(Indi indiRight, int iLevelRight, Stack stackRight,
    Indi indiLeft, int iLevelLeft, Stack stackLeft) {
        // Exit if an individual is missing
        if (indiRight == null || indiLeft == null)
            return;

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
     * @param indiRight    Reference individual.
     * @param iLevelRight  Reference generation level.
     * @param stackRight   Reference ancestor.
     * @param indiLeft     Current individual.
     * @param iLevelLeft   Current generation level.
     * @param stackLeft    Current ancestor list.
     */
    private void searchInLeftTree(Indi indiRight, int iLevelRight, Stack stackRight,
    Indi indiLeft, int iLevelLeft, Stack stackLeft) {
        // Exit if an individual is missing
        if (indiRight == null || indiLeft == null)
            return;

        // Do not check further if this individual is in the ancestor stack
        // on the other tree.
        String strIdLeft = indiLeft.getId();
        if (stackRight.contains(strIdLeft))
            return;

        // Get ID of the reference individual and check if the current individual
        // is the same.
        String strIdRight = indiRight.getId();
        if (strIdRight == strIdLeft) {
            // Check if indivividual is already in list
            ConsanguinityInfo info = (ConsanguinityInfo) mapConsanguinityCommonIndi.get(strIdRight);
            if (info == null) {
                // Create info about individual
                info = new ConsanguinityInfo();
                info.indi = indiRight;
            }

            // Save ancestor list in debug mode
            if (DEBUG) {
                Iterator itrStack = stackRight.iterator();
                while (itrStack.hasNext())
                    info.stackIndi.push(itrStack.next());
                info.stackIndi.push(STACK_SEPARATOR);

                itrStack = stackLeft.iterator();
                while (itrStack.hasNext())
                    info.stackIndi.push(itrStack.next());
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
