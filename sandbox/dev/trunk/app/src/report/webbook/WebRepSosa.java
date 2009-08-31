/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package webbook;

import genj.report.Report;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;

import genj.report.Report;

import genj.util.swing.Action2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.Class;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Comparator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebRepSosa {

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

  private Map<String, String> sourcePage = new TreeMap<String, String>();
  private WebSources reportSource = null;
  private String indi2srcDir = "";

  private Map<Integer, String> linkForGen = new TreeMap();

  public class RepSosaOptions {
     public int startSosa=1;
     public int minGen = 1;
     public int maxGen = 999;
     public int displaySource = SRC_ALL;
     public String displaySources[] = { "", "", "" };
     public RepSosaOptions(Report report) {
        displaySources[0] = report.translate("RepSosaOptions.src_no");
        displaySources[1] = report.translate("RepSosaOptions.src_no_text");
        displaySources[2] = report.translate("RepSosaOptions.src_all");
        }
     }

  private final static int
     SRC_NO = 0,
     SRC_NO_TEXT = 1,
     SRC_ALL = 2;

  String[] events = { "BIRT", "CHR", "MARR", "DEAT", "BURI", "OCCU", "RESI" };
  String[] symbols = new String[7];


  /**
   * Constructor
   */
  public WebRepSosa(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run(Indi indi, WebIndividualsDetails reportIndi, WebSection webSectionIndi, WebSources reportSource, WebSection webSectionSource) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportIndi = reportIndi;
    if (reportIndi != null) {
       personPage = reportIndi.getPagesMap();
       here2indiDir = wh.buildLinkShort(section, webSectionIndi);
       }

    this.reportSource = reportSource;
    if (reportSource != null) {
       sourcePage = reportSource.getPagesMap();
       indi2srcDir = wh.buildLinkShort(section, webSectionSource);
       }

    initVariables();
    RepSosaOptions repSosaOptions = new RepSosaOptions(report);
    if (!report.getOptionsFromUser(report.translate("OptionsSosa"), repSosaOptions)) {
       report.println("Cancelled by user.");
       return;
       }
    logParameters(repSosaOptions);
    exportData(indi, dir, wh.getAncestors(indi, repSosaOptions.startSosa), repSosaOptions);

  }


  /**
   * Exports data for page
   */
  private void exportData(Indi rootIndi, File dir, List<Ancestor> ancestors, RepSosaOptions repSosaOptions) {

   // Calculate max pages

   int cptPage = 0;
   int maxPage = 0;
   int cptIndi = 0;
   int gen = 0;

   linkForGen.clear();
   for (Iterator it = ancestors.iterator(); it.hasNext();) {
     Ancestor ancestor = (Ancestor)it.next();
     if (ancestor.gen < repSosaOptions.minGen || ancestor.gen > repSosaOptions.maxGen) {
        continue;
        }
     cptIndi++;
     if (ancestor.gen != gen) {
        if (cptIndi > section.nbPerPage) {
           cptPage++;
           cptIndi = 1;
           }
        }
        linkForGen.put( Integer.valueOf(ancestor.gen), section.sectionPrefix + String.format(section.formatNbrs, cptPage+1) + section.sectionSuffix );
        gen = ancestor.gen;
     }
     maxPage = cptPage+1;

   // Print pages
   PrintWriter doc = null;
   boolean newPage = true;
   boolean pageInProgress = false;
   boolean genInProgress = false;
   cptPage = 0;
   cptIndi = 0;
   gen = 0;
   SortedSet sources = new TreeSet(wh.sortSources);       // Ensure list is sorted with no duplicates

   for (Iterator it = ancestors.iterator(); it.hasNext();) {
      Ancestor ancestor = (Ancestor)it.next();
      if (ancestor.gen < repSosaOptions.minGen || ancestor.gen > repSosaOptions.maxGen) {
         continue;
         }

      // Is it a new generation?
      if (gen != ancestor.gen) {
         if (genInProgress) {
            closeGeneration(gen, sources, doc, cptPage, maxPage, repSosaOptions);
            sources.clear();
            if (cptIndi > section.nbPerPage) {
               newPage = true;
               }
            }
         // Is it a new page?
         if (newPage) {
            cptIndi = 1;
            newPage = false;
             if (pageInProgress) {
               closePage(doc, cptPage);
               }
            cptPage++;
            doc = openPage(rootIndi, dir, cptPage, maxPage);
            if (doc == null) {
               pageInProgress = false;
               genInProgress = false;
               break;
               }
            pageInProgress = true;
            }
         gen = ancestor.gen;
         openGeneration(gen, doc);
         genInProgress = true;
         }

      // Print individual and store sources
      formatIndi(ancestor.indi, ancestor.gen, ancestor.sosa, doc);
      sources.addAll(wh.getSources(ancestor.indi));
      cptIndi++;
      }

   // done, no more recursion
   if (genInProgress) {
      closeGeneration(gen, sources, doc, cptPage, maxPage, repSosaOptions);
      }
   if (pageInProgress) {
      closePage(doc, cptPage);
      }

   }

   /**
    * Display functions
    */

    PrintWriter openPage(Indi rootIndi, File dir, int cptPage, int maxPage) {

      String fileStr = section.sectionPrefix + String.format(section.formatNbrs, cptPage) + section.sectionSuffix;
      File file = wh.getFileForName(dir, fileStr);
      PrintWriter doc = wh.getWriter(file);
      if (doc == null) return null; 
      wh.printOpenHTML(doc, null, section);
      doc.println("<h1>"+"<a name=\"top\">"+SPACE+"</a>"+wh.htmlText(report.translate("RepSosaOptions.title.sosa", rootIndi.getName()))+"</h1>");
      exportGenLinks(doc);
      exportLinks(doc, wh, section, Math.max(1, cptPage-1), cptPage, Math.min(cptPage+1, maxPage), maxPage);
      return doc;
      }

    void openGeneration(int gen, PrintWriter doc) {
      doc.println("<div class=\"sosareport\">");
      doc.println("<a name=\"gen-"+gen+"\"></a>");
      doc.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("RepSosaOptions.Generation")+" "+gen)+"</span></p>");
      }

    void formatIndi(Indi indi, int gen, int sosa, PrintWriter doc) {
      // Print individual
      doc.println("<p class=\"sosacolumn1\"><span class=\"gras\">"+sosa+"</span>"+SPACE+"-"+SPACE+wrapName(indi)+"</p>");
      // Get and write properies
      doc.println("<div class=\"sosacolumn2\">");
      writeEvents(indi, events, symbols, doc);
      doc.println(SPACE+"</div>");
      }

    void closeGeneration(int gen, SortedSet sources, PrintWriter doc, int cptPage, int maxPage, RepSosaOptions repSosaOptions) {
      doc.println("<div class=\"spacer\">"+SPACE+"</div>");
      doc.println("</div>");
      if (repSosaOptions.displaySource != SRC_NO && !sources.isEmpty()) {
         doc.println("<div class=\"sosasources\">");
         doc.println("<span class=\"undl\">"+wh.htmlText(report.translate("RepSosaOptions.sourceList", gen))+"</span><br />	"); 
         writeSourceList(sources, repSosaOptions, doc);
         doc.println("</div>");
         }
      exportLinks(doc, wh, section, Math.max(1, cptPage-1), cptPage, Math.min(cptPage+1, maxPage), maxPage);
      }

    void closePage(PrintWriter doc, int cptPage) {
      wh.printCloseHTML(doc);
      String fileStr = section.sectionPrefix + String.format(section.formatNbrs, cptPage) + section.sectionSuffix;
      report.println(fileStr+" - Done.");
      doc.close();
    }

  /**
    * writeEvents
    */
  void writeEvents(Indi indi, String events[], String evSymbols[], PrintWriter doc) {

    // Get list of events for that individual
    List<String> listEvents = reportIndi.getNameDetails(indi, events, evSymbols);

    // Get list of events for all his/her families
    Fam[] families = indi.getFamiliesWhereSpouse();
    for (int i=0;families != null && i<families.length; i++) {
      Fam family = families[i];
      listEvents.addAll(reportIndi.getNameDetails(family, events, evSymbols));
      }
    Collections.sort(listEvents);

    // Display events
    for (Iterator s = listEvents.iterator(); s.hasNext(); ) {
       String event = (String)s.next();   // date . description . source id
       String[] eventBits = event.split("\\|", -1);
       String link = wrapSource(eventBits[2]);
       if (eventBits[2].length() != 0 && report.getDisplaySourceSec()) {
          doc.println(eventBits[1].trim()+SPACE+SPACE+"<a href=\""+link+"\"><img src=\""+themeDir+"src.gif\" alt=\""+eventBits[2]+"\" title=\""+eventBits[2]+"\"/>"+SPACE+"("+eventBits[2]+")</a><br />");
          } 
       else {
          doc.println(eventBits[1].trim()+"<br />");
          }
       }
    doc.println("<br />");

    return;
    }

  /**
   * Write sources
   */
  void writeSourceList(SortedSet sources, RepSosaOptions repSosaOptions, PrintWriter doc) {
     // display sources
     for (Iterator s = sources.iterator(); s.hasNext(); ) {
        Source src = (Source)s.next(); 
        // display source and title 
        doc.println("<br /><a href=\""+wrapSource(src.getId())+"\">("+src.getId()+")</a>"+SPACE+wh.htmlText(src.getTitle())+"<br />");
        // display text 
        if (repSosaOptions.displaySource == SRC_ALL && src.getText().length() != 0) {
           doc.println("<span class=\"sosatext\">"+wh.htmlText(src.getText())+"</span><br />");
           }
        }
     doc.println("<br />");
     return;
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
   * Print name with link
   */
  private String wrapSource(String src) {
     //
     String link = "";
     String sourceFile = (src == null) ? "" : ( (sourcePage == null) ? "" : (String)sourcePage.get(src));
     if (src != null) link = indi2srcDir+sourceFile+'#'+src;
     return link;
     }


  /**
   * Exports gen links bar
   */
  private void exportGenLinks(PrintWriter out) {
     out.println("<p class=\"letters\"><br /><br />");
     for (Iterator it = linkForGen.keySet().iterator(); it.hasNext(); ) {
        Integer gen = (Integer)it.next();
        out.println("<a href=\""+linkForGen.get(gen)+"#gen-"+gen+"\">"+gen+"</a>"+SPACE+SPACE);
        }
     out.println("</p>");
     }

  /**
   * Exports page links
   */
  private void exportLinks(PrintWriter out, WebHelper wh, WebSection section, int previous, int cptPage, int next, int last) {
     String pagename = section.sectionPrefix + String.format(section.formatNbrs, cptPage) + section.sectionSuffix;
     wh.printLinks(out, pagename, 
                    section.sectionPrefix + String.format(section.formatNbrs, 1) + section.sectionSuffix,            // start 
                    section.sectionPrefix + String.format(section.formatNbrs, previous) + section.sectionSuffix,     // previous 
                    section.sectionPrefix + String.format(section.formatNbrs, next) + section.sectionSuffix,         // next
                    section.sectionPrefix + String.format(section.formatNbrs, last) + section.sectionSuffix,         // end
                    section); 
     }



  /** Initialises variables  */
  void initVariables() {
    // Assign symbols (used only if parameter showSymbols is changed to true in individualDetails
    symbols[0] = report.getBirthSymbol();
    symbols[1] = report.getBaptismSymbol();
    symbols[2] = report.getMarriageSymbol();
    symbols[3] = report.getDeathSymbol();
    symbols[4] = report.getBurialSymbol();
    symbols[5] = report.getOccuSymbol();
    symbols[6] = report.getResiSymbol();
    }

  public void logParameters(RepSosaOptions repSosaOptions) {
    report.println("------- OPTIONS USED FOR SOSA -------");
    report.println(report.translate("RepSosaOptions.startSosa")+" = "+repSosaOptions.startSosa);
    report.println(report.translate("RepSosaOptions.minGen")+" = "+repSosaOptions.minGen);
    report.println(report.translate("RepSosaOptions.maxGen")+" = "+repSosaOptions.maxGen);
    report.println(report.translate("RepSosaOptions.displaySources")+" = "+repSosaOptions.displaySources[repSosaOptions.displaySource]);
    report.println("------------------------------------");
    return;
  }

} // End_of_Report
