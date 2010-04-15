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
import java.util.Map;
import java.util.TreeMap;
import java.lang.Enum;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebLastnames {

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

  private static final int NB_WORDS = 7;

  public enum Letters { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z; }

  private Map<String, String> linkExistsForLetter = new TreeMap();


  /**
   * Constructor
   */
  public WebLastnames(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run() {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);
    File file = wh.getFileForName(dir, section.sectionPrefix+section.sectionSuffix);
    PrintWriter out = wh.getWriter(file);
    sectionList = report.getSection("Individualslist");

    // HEAD
    wh.printOpenHTML(out, "Lastnames", section);

    // START OF PAGE ------------------
    calcLetters();
    exportData(dir, out);
    // END OF PAGE ------------------

    // TAIL
    wh.printLinks(out, section.sectionPrefix+section.sectionSuffix, section);
    wh.printCloseHTML(out);

    // done
    out.close();
    report.println(section.sectionPrefix+section.sectionSuffix+" - Done.");
  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, PrintWriter out) {

    out.println("<p class=\"letters\">");
    out.println("<br /><br />");
    for (Letters l : Letters.values()) {
       if (checkLink(l.toString())) { 
          out.println("<a href=\""+'#'+l+"\">"+l+"</a>"+SPACE+SPACE);
          }
       else {
          out.println(l+SPACE+SPACE);
          }
       }
    if (checkLink(DEFCHAR)) { 
       out.println("<a href=\""+'#'+DEFCHAR+"\">"+DEFCHAR+"</a>"+SPACE+SPACE);
       }
    else {
       out.println(DEFCHAR+SPACE+SPACE);
       }
    out.println("</p>");
    wh.printLinks(out, section.sectionPrefix+section.sectionSuffix, section);

    // Create link for each last name
    Iterator it = wh.getLastNames(gedcom).iterator();
    char last = ' ';
    int cpt = 1, iNames = 1;
    out.println("<p class=\"nameblock\">");
    while (it.hasNext()) {
      // create new name class (first char) if necessary
      String name = it.next().toString();
      String anchor = wh.htmlAnchorText(name);
      if (anchor.length() > 0 && Character.toUpperCase(anchor.charAt(0)) != last) {
        last = Character.toUpperCase(anchor.charAt(0));
        String l = String.valueOf(last);
        out.println("</p>");
        out.println("<p class=\"char\">");
        out.println("<a name=\""+l+"\"></a>");
        out.println(l+"<br /></p>");
        cpt = 1;
        out.println("<p class=\"nameblock\">");
      }
      // create link to name file
      String listfile = wh.buildLink(section, sectionList, iNames);
      out.print("<span class=\"name\">");
      out.print("<a href=\""+listfile+'#'+anchor+"\">"+wh.htmlText(name)+"</a>"+SPACE);
      out.print("<span class=\"occu\">("+wh.getLastNameCount(name)+")</span>");
      out.print("</span>"+SPACE+SPACE+SPACE);
      cpt++;
      iNames++;

      if (cpt > NB_WORDS) {
         out.print("<br />");
         cpt = 1;
         }
    }
    out.println("</p>");

  }

  /**
   * Calculate if there is a link to the letters
   */
  private void calcLetters() {

    linkExistsForLetter.put(DEFCHAR, "0");
    for (Letters l : Letters.values()) {
       linkExistsForLetter.put(l.toString(), "0");
       }

    Iterator it = wh.getLastNames(gedcom).iterator();
    while (it.hasNext()) {
      String name = it.next().toString();
      String l = (name.length()>0) ? name.substring(0,1).toUpperCase() : DEFCHAR;
      try { Letters.valueOf(l); } catch (IllegalArgumentException e) { l = DEFCHAR; }
      linkExistsForLetter.put(l, "1");
      }
    }

  /**
   * Booleanise existance of link to a letter
   */
  private boolean checkLink(String str) {
    String flag = linkExistsForLetter.get(str);
    if (flag == null || flag.compareTo("0") == 0) return false;
    return true;
    }

} // End_of_Report
