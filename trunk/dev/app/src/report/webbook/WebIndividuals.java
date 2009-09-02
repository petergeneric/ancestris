/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package webbook;

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
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.gedcom.MultiLineProperty;

import java.net.URL;
import java.lang.Class;
import java.net.URLClassLoader;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;
import java.net.MalformedURLException;
import javax.swing.JFileChooser;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.io.InputStream;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Map;
import java.util.TreeMap;
import java.lang.Enum;
import java.util.List;



/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebIndividuals {

  private ReportWebBook report = null;
  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private WebSection sectionList = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private final static String DEFCHAR = "-";
  private static final int IMG_BUFFER_SIZE = 1024;
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];
  private String themeDir = "";

  public enum Letters { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z }

  private Map<String, String> linkForLetter = new TreeMap<String, String>();       // map is : letter to link 
  private Map<String, String> namePage = new TreeMap<String, String>();            // map is : lastname to link 

  /**
   * Constructor
   */
  public WebIndividuals(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Report's entry point
   */
  public void run(List indis) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + File.separator + section.sectionDir, true);
    sectionList = report.getSection("Individualsdetails");

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    calcLetters(indis);
    exportData(dir, indis);

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
        nbNames = wh.getLastNames(gedcom).size();
    int previousPage = 0,
        currentPage  = 0,
        nextPage     = 0,
        lastPage     = (nbNames/section.nbPerPage)+1;
    String listfile = "";

    // Go through individuals
    for (Iterator it = indis.iterator(); it.hasNext();) {
      Indi indi = (Indi)it.next();
      cpt++;

      // Check if need to increment lastname
      String lastName = wh.getLastName(indi);
      if (lastName.compareTo(previousLastName) != 0) {
         previousLastName = lastName;
         iNames++;
         }

      // Check if need to write anchor letter
      String anchorLastName = wh.htmlAnchorText(lastName);
      char cLetter = anchorLastName.charAt(0);
      if (cLetter != previousLetter) {
         previousLetter = cLetter;
         writeLetter = true;
         }
      else {
         writeLetter = false;
         }

      // Check if need to write anchor lastname
      if (anchorLastName.compareTo(previousAnchorLastName) != 0) {
         previousAnchorLastName = anchorLastName;
         writeAnchor = true;
         }
      else {
         writeAnchor = false;
         }

      currentPage = (iNames/section.nbPerPage)+1; 
      previousPage = (currentPage == 1) ? 1 : currentPage-1; 
      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1; 
      listfile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
      if (previousListFile.compareTo(listfile) != 0) {
         if (out != null) { 
            out.println("</p>");
            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
            wh.printCloseHTML(out);
            report.println(previousListFile+" - Done.");
            out.close();
            }
         previousListFile = listfile;
         file = wh.getFileForName(dir, listfile);
         out = wh.getWriter(file);
         wh.printOpenHTML(out, "Individualslist", section);
         out.println("<p class=\"letters\">");
         out.println("<br /><br />");
         for (Letters l : Letters.values()) {
            if (checkLink(l.toString())) { 
               out.println("<a href=\""+linkForLetter.get(l.toString())+"#"+l+"\">"+l+"</a>"+SPACE+SPACE);
               }
            else {
               out.println(l+SPACE+SPACE);
               }
            }
         if (checkLink(DEFCHAR)) { 
            out.println("<a href=\""+linkForLetter.get(DEFCHAR)+"#"+DEFCHAR+"\">"+DEFCHAR+"</a>"+SPACE+SPACE);
            }
         else {
            out.println(DEFCHAR+SPACE+SPACE);
            }
         out.println("<br /><br /></p>");
         if (!writeLetter) {
            exportLinks(out, listfile, previousPage, nextPage, lastPage); 
            out.println("<p>");
            }
         }

      String personfile = wh.buildLink(section, sectionList, cpt); 
      if (writeLetter) {
         if (!first) {
            out.println("</p>");
            }
         first = false;
         exportLinks(out, listfile, previousPage, nextPage, lastPage);
         String ancLet = String.valueOf(previousLetter); 
         if (!(ancLet.matches("[a-zA-Z]"))) ancLet = DEFCHAR;
         out.println("<p class=\"letter\">"+"<a name=\""+ancLet+"\"></a>"+ancLet+"</p>");
         out.println("<p>");
         }
      exportIndividualRow(out, indi, writeAnchor, personfile);
      // .. next individual
     }

    if (out != null) { 
       out.println("</p>");
       exportLinks(out, listfile, previousPage, nextPage, lastPage); 
       wh.printCloseHTML(out);
       report.println(previousListFile+" - Done.");
       }

    // done
    if (out != null) out.close();

  }

  /**
   * Exports individual row
   */
  private void exportIndividualRow(PrintWriter out, Indi indi, boolean writeAnchor, String personfile) {

    String NODATE = SPACE; 

    String sexString = DEFCHAR+SPACE;
    if (indi.getSex() == 1) {
       sexString = "<img src=\""+themeDir+"m.gif\" alt=\""+report.translate("alt_male")+"\" />";
       }
    else if (indi.getSex() == 2) {
       sexString = "<img src=\""+themeDir+"f.gif\" alt=\""+report.translate("alt_female")+"\" />";
       }
    else  {
       sexString = "<img src=\""+themeDir+"u.gif\" alt=\""+report.translate("alt_unknown")+"\" />";
       }

    PropertyDate birthDateProp = (PropertyDate) indi.getProperty(new TagPath("INDI:BIRT:DATE")); 
    String birthDateString = "";
    if (birthDateProp == null) {
       birthDateString = NODATE;
       } 
    else {
       birthDateString = wh.htmlText(birthDateProp.toString());
       }
    
    PropertyDate deathDateProp = (PropertyDate) indi.getProperty(new TagPath("INDI:DEAT:DATE")); 
    String deathDateString = "";
    if (deathDateProp == null) {
       deathDateString = NODATE;
       } 
    else {
       deathDateString = wh.htmlText(deathDateProp.toString());
       }

    String name = wh.getLastName(indi);
    String first = indi.getFirstName();
    String anchor = indi.getId();

    if (writeAnchor) {
       out.println("<a name=\""+wh.htmlAnchorText(name)+"\"></a>");
       }

    if (wh.isPrivate(indi)) { 
       name = "...";
       first = "...";
       birthDateString = "...";
       deathDateString = "...";
       }

    out.println(sexString+SPACE);
    out.print("<a href=\""+personfile+'#'+anchor+"\">");
    out.println(wh.htmlText(name+", "+first));
    out.print("</a>");
    out.println(SPACE+"("+SPACE+birthDateString+SPACE+"-"+SPACE+deathDateString+SPACE+")");
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
    int currentPage  = 0;
    String listfile = "";
    for (Iterator it = indis.iterator(); it.hasNext();) {
      Indi indi = (Indi)it.next();
      String lastname = wh.getLastName(indi);
      String str = wh.htmlAnchorText(lastname);
      if (str == null) continue;
      if (str.compareTo(name) != 0) {
         listfile = section.sectionPrefix + String.format(section.formatNbrs, (iNames/section.nbPerPage)+1) + section.sectionSuffix;
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
    if (flag == null || flag.compareTo("0") == 0) return false;
    return true;
    }


  /**
   * Exports page links
   */
  private void exportLinks(PrintWriter out, String pagename, int previous, int next, int last) {
      wh.printLinks(out, pagename, 
                    section.sectionPrefix + String.format(section.formatNbrs, 1) + section.sectionSuffix,            // start 
                    section.sectionPrefix + String.format(section.formatNbrs, previous) + section.sectionSuffix,     // previous 
                    section.sectionPrefix + String.format(section.formatNbrs, next) + section.sectionSuffix,         // next
                    section.sectionPrefix + String.format(section.formatNbrs, last) + section.sectionSuffix,         // end
                    section); 
     }

} // End_of_Report
