/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package webbook;

import genj.report.Report;
import genj.gedcom.*;
import genj.util.swing.Action2;
import genj.app.Options;
import genj.util.EnvironmentChecker;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.lang.Class;
import java.text.DateFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JEditorPane;

/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 *
 * Instructions to add a section: (look for ***new section*** )
 * =============================
 * 
 * 1/ In ReportWebBook.java
 * - add definition option and websection
 * - create new section in main logic
 * - export link in index page
 * - add creation of section in initSections
 * - add retrieval of name in getSection
 *
 * 2/ In properties
 * - add section name in menu
 *
 *
 * 3/ From WebNewSection.java
 * - copy to another name (i.e. WebSources.java)
 *
 *
 */

public class ReportWebBook extends Report {

  Report report = null;

  /** Options for index page */
  public String  idxTitle         = "Genealogie Lapeyre";
  public String  idxAuthor        = "Frederic Lapeyre";
  public String  idxAddress       = "27 Bellevue Road - Dover Heights - 2030 Australia";
  public String  idxTel           = "+61 2 9371 6435";
  public String  idxEmail         = "me@mydomain.com";
  public boolean dispMessage      = false;
  public boolean dispLonguest     = true;
  public int     jurisdlvl        = 0;

  /** Options for several sections */
  public String  strBlank         = "?";
  public boolean displayId          = false;

  /** Options for details pages section */
  public boolean displayFamily      = true;
  public boolean displayChildren    = true;
  public boolean displayStepSibling = true;
  public boolean displayRelations   = true;
  public boolean displayNotes       = true;
  public boolean displayEmail       = true;

  /** Options for Sources section */
  public boolean displaySourceSec   = true;
  private final static int
    SRC_NOMEDIA = 0,
    SRC_MEDIAMIN = 1,
    SRC_MEDIARELTEXT = 2,
    SRC_MEDIARELMIN = 3;
  public int displaySrcImgType = SRC_MEDIARELMIN;
  public String displaySrcImgTypes[] = {
      translate("SrcDispNoMedia"),
      translate("SrcDispMediaMin"),
      translate("SrcDispMediaRelText"),
      translate("SrcDispMediaRelMin")
   };
  public boolean copySources        = false;

  /** Options for Media section */
  public boolean displayMediaSec    = true;
  public boolean copyMedia          = false;

  /** Options for Map section */
  public boolean displayMap         = true;
  public boolean displayUnkCities   = false;

  /** Options for Reports section */
  public boolean displayRepSosa     = true;

  /** Options for ftp connection */
  public boolean FTPuse           = false;
  public String  FTPhost          = "www.hostsite.com";
  public String  FTPTargetDir     = "/public_html/genealogy/";
  public String  FTPuser          = "myuser";
  public String  FTPpassword      = "mypassword";
  public String  siteDesc         = translate("siteDescDefault");
  private final static int
    FTP_SYSTEMATIC = 0,
    FTP_INCREMENTAL = 1,
    FTP_SYNCHRONISE = 2;
  public int uploadType = FTP_INCREMENTAL;
  public String uploadTypes[] = {
      translate("FtpSystematic"),
      translate("FtpIncremental"),
      translate("FtpSynchronise")
   };
  public boolean resetRegister = false;
  public String  shell         = translate("shellDefault");


  /** Sections of the web book */ 						/* ***new section*** */
  private boolean    pagesLastnames            = true;
  private WebSection sectionLastnames          = null;

  private boolean    pagesIndividuals          = true;
  private WebSection sectionIndividuals        = null;

  private boolean    pagesIndividualsDetails   = true;
  private WebSection sectionIndividualsDetails = null;

  private boolean    pagesSources              = true;
  private WebSection sectionSources            = null;

  private boolean    pagesMedia                = true;
  private WebSection sectionMedia              = null;

  private boolean    pagesCities               = true;
  private WebSection sectionCities             = null;

  private boolean    pagesCitiesDetails        = true;
  private WebSection sectionCitiesDetails      = null;

  private boolean    pagesDays                 = true;
  private WebSection sectionDays               = null;

  private boolean    pagesDaysDetails          = true;
  private WebSection sectionDaysDetails        = null;

  private boolean    pagesSearch               = true;
  private WebSection sectionSearch             = null;

  private boolean    pagesStatsImplex          = true;
  private WebSection sectionStatsImplex        = null;

  private boolean    pagesStatsFrequent        = true;
  private WebSection sectionStatsFrequent      = null;

  private boolean    pagesRepSosa              = true;
  private WebSection sectionRepSosa            = null;

  private boolean    pagesMap                  = true;
  private WebSection sectionMap                = null;

  /** Option for style sheet */
  private String themeDir = "theme";
  private final static String SEP = "/";
  private String css = themeDir + SEP + "style.css";
  private final static String SPACE = "&nbsp;";

  /**
   * Non public variables
   */
  private File dir = null;
  private final static String INDEX = "index.html";
  private final static String LANG = Locale.getDefault().getLanguage();
  private WebHelper wh = null;
  private GedcomStats stats = null;
  private final static String msgFile = "msgFile.txt";
  private              String msgFileAbsolute = "";
  private final static String uploadRegisterName = "upreg.xml";
  private FTPRegister uploadRegister = null;

  /**
   * One of the report's entry point
   */
  public void start(Gedcom gedcom) {
    start(gedcom, gedcom.getEntities(Gedcom.INDI), null);
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi[] indis) {
    start(indis[0].getGedcom(), Arrays.asList(indis), null);
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi indi) {
    start(indi.getGedcom(), indi.getGedcom().getEntities(Gedcom.INDI), indi);
  }

  /**
   * Our main logic
   */
  private void start(Gedcom gedcom, Collection indis, Indi indiDeCujus) {

    // Start helper
    logInit();
    wh = new WebHelper(this, css, INDEX, siteDesc, idxAuthor, LANG);
    wh.setKeywords(gedcom);

    // QUESTION TO USER #1 - get de-cujus (sosa 1) if entry point is generic
    if (indiDeCujus == null) {
      String msg = translate("AskDeCujus");
      indiDeCujus = (Indi)getEntityFromUser(msg, gedcom, Gedcom.INDI);
      if (indiDeCujus == null) {
         println(translate("user_cancel"));
         return;
         }
      }

    // QUESTION TO USER #2 - Get a directory to write to
    dir = getDirectoryFromUser(translate("AskTargetDir"), Action2.TXT_OK);
    if (dir==null) {
       println(translate("user_cancel"));
       return;
       }

    // log options and user inputs
    logParameters(indiDeCujus, dir);

    // Make sure directory is there
    if (!dir.exists() && !dir.mkdirs()) {
      println(translate("error_outputdir", dir));
      return;
      }

    // Warning if directory not empty
    if (dir.list().length > 1) {
      println(translate("warning_notEmpty", dir.toString()));
      println(" ");
      }

    // Load upload regitry in case user wants to upload pages
    if (FTPuse) {
      if (!cleanFTPParams()) { 
         return;
         }
      uploadRegister = new FTPRegister(wh.getGenjImagesDir() + File.separator + uploadRegisterName, FTPhost, FTPTargetDir, dir, uploadType, resetRegister);
      wh.setUploadRegister(uploadRegister);
      }

    // Create icons from webbook to where user requires
    File dirIcons = wh.createDir(getDir().getAbsolutePath() + File.separator + themeDir, true);
    wh.emptyDir(dirIcons, false);
    wh.createIcons(dirIcons);

    // Load individuals of Gedcom
    List indiWeb = wh.getIndividuals(gedcom);

    // Load sources of Gedcom
    List srcWeb = wh.getSources(gedcom);

    // Init book sections (need to int before running because sections use this class to get info about the other sections)
    initSections(gedcom);

    // Create book sections  								/* ***new section*** */
    WebLastnames reportPagesNames = null;
    WebIndividuals reportPagesIndi = null;
    WebIndividualsDetails reportPagesIndiDetails = null;
    WebSources reportPagesSource = null;
    WebMedia reportPagesMedia = null;
    WebCities reportPagesCities = null;
    WebCitiesDetails reportPagesCitiesDetails = null;
    WebDays reportPagesDays = null;
    WebDaysDetails reportPagesDaysDetails = null;
    WebSearch reportPagesSearch = null;
    WebStatsImplex reportPagesStatsImplex = null;
    WebStatsFrequent reportPagesStatsFrequent = null;
    WebRepSosa reportPagesRepSosa = null;
    WebMap reportPagesMap = null;

    if (pagesLastnames) {
       reportPagesNames = new WebLastnames(this, gedcom, indiDeCujus, sectionLastnames);
       println(sectionLastnames.sectionName);
       reportPagesNames.run();
       println(" ");
       }
    if (pagesIndividuals) {
       reportPagesIndi = new WebIndividuals(this, gedcom, indiDeCujus, sectionIndividuals);
       println(sectionIndividuals.sectionName);
       reportPagesIndi.run(indiWeb);
       println(" ");
       }
    if (pagesIndividualsDetails) {
       reportPagesIndiDetails = new WebIndividualsDetails(this, gedcom, indiDeCujus, sectionIndividualsDetails, indiWeb);
       }
    if (pagesSources && displaySourceSec) {
       reportPagesSource = new WebSources(this, gedcom, indiDeCujus, sectionSources, srcWeb);     // indiDeCujus not used for sources
       }

    if (pagesIndividualsDetails) {
       println(sectionIndividualsDetails.sectionName);
       reportPagesIndiDetails.run(indiWeb, reportPagesSource, sectionSources);
       stats = new GedcomStats(indiDeCujus, reportPagesIndiDetails.getPagesMap(), wh, sectionIndividualsDetails.sectionDir);
       println(" ");
       }
    if (pagesSources && displaySourceSec) {
       println(sectionSources.sectionName);
       reportPagesSource.run(srcWeb, reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesMedia && displayMediaSec) {
       reportPagesMedia = new WebMedia(this, gedcom, indiDeCujus, sectionMedia);     // indiDeCujus not used for media
       println(sectionMedia.sectionName);
       reportPagesMedia.run(reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesCities) {
       reportPagesCities = new WebCities(this, gedcom, indiDeCujus, sectionCities);
       println(sectionCities.sectionName);
       reportPagesCities.run();
       println(" ");
       }

    if (pagesCitiesDetails) {
       reportPagesCitiesDetails = new WebCitiesDetails(this, gedcom, indiDeCujus, sectionCitiesDetails);
       println(sectionCitiesDetails.sectionName);
       reportPagesCitiesDetails.run(reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesMap && displayMap) {
       reportPagesMap = new WebMap(this, gedcom, indiDeCujus, sectionMap);
       println(sectionMap.sectionName);
       reportPagesMap.run(indiDeCujus, reportPagesCitiesDetails, sectionCitiesDetails);
       println(" ");
       }

    if (pagesDays) {
       reportPagesDays = new WebDays(this, gedcom, indiDeCujus, sectionDays);
       println(sectionDays.sectionName);
       reportPagesDays.run();
       println(" ");
       }

    if (pagesDaysDetails) {
       reportPagesDaysDetails = new WebDaysDetails(this, gedcom, indiDeCujus, sectionDaysDetails);
       println(sectionDaysDetails.sectionName);
       reportPagesDaysDetails.run(reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesSearch) {
       reportPagesSearch = new WebSearch(this, gedcom, indiDeCujus, sectionSearch);     // indiDeCujus not used for search
       println(sectionSearch.sectionName);
       reportPagesSearch.run(indiWeb, reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesStatsFrequent) {
       reportPagesStatsFrequent = new WebStatsFrequent(this, gedcom, indiDeCujus, sectionStatsFrequent);
       println(sectionStatsFrequent.sectionName);
       reportPagesStatsFrequent.run(indiDeCujus, reportPagesIndi, sectionIndividuals, reportPagesCitiesDetails, sectionCitiesDetails, reportPagesDaysDetails, sectionDaysDetails);
       println(" ");
       }

    if (pagesStatsImplex) {
       reportPagesStatsImplex = new WebStatsImplex(this, gedcom, indiDeCujus, sectionStatsImplex);
       println(sectionStatsImplex.sectionName);
       reportPagesStatsImplex.run(indiDeCujus, reportPagesIndiDetails, sectionIndividualsDetails);
       println(" ");
       }

    if (pagesRepSosa && displayRepSosa) {
       reportPagesRepSosa = new WebRepSosa(this, gedcom, indiDeCujus, sectionRepSosa);
       println(sectionRepSosa.sectionName);
       reportPagesRepSosa.run(indiDeCujus, reportPagesIndiDetails, sectionIndividualsDetails, reportPagesSource, sectionSources);
       println(" ");
       }


    // Get dynamic message 
    if (dispMessage) {
       dispMessage = getDynMessage(msgFile);
       }

    // Create entry page (default: index.html) and style sheet (default style.css)
    try {
      exportIndex(gedcom, dir);
      println(" ");
      exportStylesheet(dir);
      println(" ");
    } catch (IOException e) {
      println(translate("error_export", e.getMessage()));
    }

    try {
      showBrowserToUser(getFileForIndex(dir).toURL());
    } catch (IOException e) {
      println(translate("error_showBrowser", e.getMessage()));
    }

    // Upload pages and close register
    if (FTPuse) {
       println(translate("FTP_message"));
       uploadRegister.save();
       new WebUploadBook(FTPhost, FTPuser, FTPpassword, dir, FTPTargetDir, this, uploadRegister);
       uploadRegister.close();
       }

    if (!shell.startsWith("0")) {
       try {
         println(translate("shell_launch", shell));
         Process process = Runtime.getRuntime().exec(shell);
         println(translate("shell_cannotwait"));
         } catch (IOException e) {
           println(translate("error_shell", new String[] { shell, e.getMessage() } ));
         }
       }

    println(" ");
    println(translate("Completed"));

  }  // end_of_start

  /** 
   * Exports index.html
   */
  private void exportIndex(Gedcom gedcom, File dir) throws IOException {

    File file = getFileForIndex(dir);
    println(translate("exporting", new String[]{ file.getName(), dir.toString() }));
    PrintWriter out = wh.getWriter(file);
    stats.update(gedcom, dispLonguest);

    // HEAD
    wh.printOpenHTML(out, "");

    out.println("<hr />");
    out.println("<div class=\"contindex\">");      // conteneur

    // 
    // MENU side of the index page
    // 
    out.println("<div class=\"menu\">");           /* ***new section*** */

    // Individuals
    out.println("<p>"+translate("menu_individuals")+"</p><ul>");
    if (pagesLastnames) 
       out.println("<li><a href=\""+sectionLastnames.sectionLink+"\">"+wh.htmlText(sectionLastnames.sectionName)+"</a></li>");
    if (pagesIndividuals) 
       out.println("<li><a href=\""+sectionIndividuals.sectionLink+"\">"+wh.htmlText(sectionIndividuals.sectionName)+"</a></li>");
    if (pagesIndividualsDetails) 
       out.println("<li><a href=\""+sectionIndividualsDetails.sectionLink+"\">"+wh.htmlText(sectionIndividualsDetails.sectionName)+"</a></li>");

    // Documents
    if (displaySourceSec || displayMediaSec) 
       out.println("</ul><p>"+translate("menu_documents")+"</p><ul>");
    if (pagesSources && displaySourceSec) 
       out.println("<li><a href=\""+sectionSources.sectionLink+"\">"+wh.htmlText(sectionSources.sectionName)+"</a></li>");
    if (pagesMedia && displayMediaSec) 
       out.println("<li><a href=\""+sectionMedia.sectionLink+"\">"+wh.htmlText(sectionMedia.sectionName)+"</a></li>");

    // Locations
    out.println("</ul><p>"+wh.htmlText(translate("menu_locations"))+"</p><ul>");
    out.println("<li><a href=\""+sectionMap.sectionLink+"\">"+wh.htmlText(sectionMap.sectionName)+"</a></li>");
    out.println("<li><a href=\""+sectionCities.sectionLink+"\">"+wh.htmlText(sectionCities.sectionName)+"</a></li>");
    out.println("<li><a href=\""+sectionCitiesDetails.sectionLink+"\">"+wh.htmlText(sectionCitiesDetails.sectionName)+"</a></li>");

    // Dates
    out.println("</ul><p>"+wh.htmlText(translate("menu_days"))+"</p><ul>");
    out.println("<li><a href=\""+sectionDays.sectionLink+"\">"+wh.htmlText(sectionDays.sectionName)+"</a></li>");
    out.println("<li><a href=\""+sectionDaysDetails.sectionLink+"\">"+wh.htmlText(sectionDaysDetails.sectionName)+"</a></li>");

    // Statistics
    out.println("</ul><p>"+wh.htmlText(translate("menu_statistics"))+"</p><ul>");
    if (pagesStatsFrequent) 
       out.println("<li><a href=\""+sectionStatsFrequent.sectionLink+"\">"+wh.htmlText(sectionStatsFrequent.sectionName)+"</a></li>");
    if (pagesStatsImplex) 
       out.println("<li><a href=\""+sectionStatsImplex.sectionLink+"\">"+wh.htmlText(sectionStatsImplex.sectionName)+"</a></li>");

    // Structured lists
    if (displayRepSosa) 
       out.println("</ul><p>"+wh.htmlText(translate("menu_structuredlist"))+"</p><ul>");
    if (pagesRepSosa && displayRepSosa) 
       out.println("<li><a href=\""+sectionRepSosa.sectionLink+"\">"+wh.htmlText(sectionRepSosa.sectionName)+"</a></li>");
if (false) {
    out.println("<li>"+wh.htmlText("Flash list")+"</li>");
}

    // Tools
    out.println("</ul><p>"+wh.htmlText(translate("menu_tools"))+"</p><ul>");
    if (pagesSearch) 
       out.println("<li><a href=\""+sectionSearch.sectionLink+"\">"+wh.htmlText(sectionSearch.sectionName)+"</a></li>");
    out.println("</ul></div>");



    // 
    // Right hand side of the index page
    // 
    out.println("<div class=\"intro\">");

    // Dynamic message 
    if (dispMessage) {
       out.println(readFile(msgFileAbsolute));
       out.println("<br /><br /><hr /><br />");
       }

    // Static message 
    out.println(translate("text_sosa", new String[] { " <a href=\""+stats.deCujusLink+"\">"+stats.deCujusName+"</a>", String.valueOf(stats.nbAncestors), String.valueOf(stats.nbGen) } )+"<br />");
    out.println(translate("text_old", new String[] { "<a href=\""+stats.olderLink+"\">"+stats.olderName+"</a>", stats.olderBirthDate } )+"<br />");
    if (dispLonguest) {
       out.println("<br />");
       if (stats.indiDeCujus == stats.longIndiG) { 
          if (stats.indiDeCujus == stats.longIndiA) { 
             out.println(translate("text_longuest1")+"<br />");
             out.println(translate("text_largest1", translate("text_largest1too"))+"<br />");
             }
          else {
             out.println(translate("text_longuest1")+"<br />");
             out.println(translate("text_largest2", new String[] { "<a href=\"" + stats.longIndiALink + "\">" + stats.longIndiAName + "</a>", String.valueOf(stats.nbAncestorsA) } )+"<br />");
             }
          }
       else {
          if (stats.indiDeCujus == stats.longIndiA) { 
             out.println(translate("text_largest1", translate("text_largest1too")+SPACE)+"<br />");
             out.println(translate("text_longuest2", new String[] { "<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)  } )+"<br />");
             }
          else {
             if (stats.longIndiG == stats.longIndiA) { 
                out.println(translate("text_longuest2", new String[] { "<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)  } )+"<br />");
                out.println(translate("text_largest1", translate("text_largest1too")+SPACE)+"<br />");
                }
             else {
                out.println(translate("text_longuest2", new String[] { "<a href=\"" + stats.longIndiGLink + "\">" + stats.longIndiGName + "</a>", String.valueOf(stats.nbGenG)  } )+"<br />");
                out.println(translate("text_largest2", new String[] { "<a href=\"" + stats.longIndiALink + "\">" + stats.longIndiAName + "</a>", String.valueOf(stats.nbAncestorsA)  } )+"<br />");
                } 
             }
          } 
       }
    out.println("<br /><hr /><br />");
    if (stats.place.length() > 0) out.println(translate("text_place", stats.place)+"<br />");
    out.println(translate("text_stats", new String[] { String.valueOf(stats.nbIndis), String.valueOf(stats.nbFams), String.valueOf(stats.nbNames), String.valueOf(stats.nbPlaces) } )+"<br />");
    out.println(translate("text_cousins", new String[] { String.valueOf(stats.nbAscendants), String.valueOf(stats.nbCousins), String.valueOf(stats.nbOthers) } )+"<br />");
    out.println(translate("text_family", new String[] { String.valueOf(stats.nbFams), String.valueOf(stats.nbFamsWithKids), String.valueOf(stats.avgKids) } )+"<br />");
    out.println("<br /><hr /><br />");

    out.println(translate("idxAuthor")+":"+SPACE+idxAuthor+"<br />");
    out.println(translate("idxAddress")+":"+SPACE+idxAddress+"<br />"+translate("idxTel")+":"+SPACE+idxTel+"<br />");
    if (displayEmail) {
       out.println("<a href=\"mailto:"+idxEmail+"?subject="+translate("idx_email_subject")+"&amp;body="+translate("idx_email_dear")+"%20"+idxAuthor+",%0a%0a"+translate("idx_email_body")+" \">" + translate("idx_email_link")+"</a><br />");
       }
    out.println("<hr /><br />");

    Calendar rightNow = Calendar.getInstance();
    out.println("<p class=\"legal\">"+translate("text_pages", new String[] { "<a href=\"http://genj.sourceforge.net/\">GenealogyJ</a>&nbsp;<a href=\"http://www.arvernes.com/wiki/index.php/Genj_-_Rapports#Le_livre_Web_sur_Internet_.28WebBook.29\">WebBook</a>", translate("version"), DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(rightNow.getTime()) } )+"</p>");
    out.println("</div>");

    out.println("<div class=\"spacer\">"+SPACE+"</div>");

    out.println("</div>"); // conteneur

    // TAIL
    wh.printCloseHTML(out);

    // done
    out.close();
  }

  /**
   * Export a stylesheet - if not there already
   */
  private void exportStylesheet(File dir) throws IOException {

    File file = getFileForStylesheet(dir);
    if (file==null) {
      return;
    }

    println(translate("exporting", new String[]{ file.getName(), dir.toString() }));
    PrintWriter out = wh.getWriter(file);

    for (int i=0 ; i < wh.defaultStylesheet.length ; i++) {
      out.println(wh.defaultStylesheet[i]);
      }

    out.close();
  }

  private File getFileForIndex(File dir) {
    return new File(dir, INDEX);
  }

  private File getFileForStylesheet(File dir) {
    return css.length()>0 ? new File(dir, css) : null;
  }

  /**
   * Get message from user 
   */
  private boolean getDynMessage(String msgFile) {

    String filename = "";

    try {
      // create our home directory
      File home = new File(EnvironmentChecker.getProperty(ReportWebBook.class, "user.home.genj", null, "determining home directory"));
      home.mkdirs();
      if (!home.exists()||!home.isDirectory()) {
         println(translate("indexmsg_errorcreate",home.getAbsolutePath()));
         return false;
         }

      // create our webbook directory
      File homewebbook = new File(home.getAbsolutePath()+File.separator+"webbook");
      homewebbook.mkdirs();
      if (!homewebbook.exists()||!homewebbook.isDirectory()) {
         println(translate("indexmsg_errorcreate",homewebbook.getAbsolutePath()));
         return false;
        }

      // create our message file
      File filewebbook = new File(homewebbook.getAbsolutePath()+File.separator+msgFile);
      filename = filewebbook.getAbsolutePath();
      msgFileAbsolute = ""+filename;
      if (!filewebbook.exists()) {
         println(translate("indexmsg_willcreate",filewebbook.getAbsolutePath()));
        }

      } catch (Throwable t) {
         println(translate("indexmsg_errorcreate",filename) + t.toString());
         return false;
      }

    // produce file
    String filetext = "";
    try {
       filetext = wh.readFile(filename);
      } catch (IOException e) { report.println(""); } 

    SimpleEditor se = new SimpleEditor(this, translate("indexmsg_please"), filetext, filename);
    return true;
  }

  /**
   * Read message from file and convert newline
   */
  private String readFile(String filename) {
     String filetext = "";
     try {
        filetext = wh.readFile(filename);
         } catch (IOException e) { report.println(e.toString()); } 
     return wh.htmlText(filetext, false);
     }


   /**
    * Clean FTP paramaters
    */
  private boolean cleanFTPParams() {

       // Host should be not empty and not include "/" 
       FTPhost = FTPhost.trim();
       if (FTPhost.length() == 0) {
          println(translate("error_hostEmpty"));
          return false;
          }
       if (FTPhost.startsWith("ftp://")) {
          FTPhost = FTPhost.substring(6);
          }
       while (FTPhost.startsWith("/")) {
          FTPhost = FTPhost.substring(1);
          }
       while (FTPhost.endsWith("/")) {
          FTPhost = FTPhost.substring(0, FTPhost.length()-1);
          }

       FTPTargetDir = FTPTargetDir.trim();
       if (!FTPTargetDir.endsWith("/")) {
          FTPTargetDir = FTPTargetDir + "/";
          }
       if (!FTPTargetDir.startsWith("/")) {
          FTPTargetDir = "/" + FTPTargetDir;
          }

       FTPuser = FTPuser.trim();
       if (FTPuser.length() == 0) {
          println(translate("error_userEmpty"));
          return false;
          }

       FTPpassword = FTPpassword.trim();

       if (uploadType == FTP_SYNCHRONISE) {
          return getOptionFromUser(translate("AskConfirmRemove"), OPTION_YESNO);
          }

       return true;
       }


  /**
   * Initialise webbook sections                ***new section*** 
   */
  private void initSections(Gedcom gedcom) {
     //
     int sizeIndiSection = 50;
     int l = (int)(Math.log10(wh.getNbIndis(gedcom) / sizeIndiSection)) + 1;
     String format = "%0"+l+"d";

     if (pagesLastnames) {
        sectionLastnames = new WebSection(translate("Lastnames"), "names", "names", "", ".html", 1, 0);
        }
     if (pagesIndividuals) {
        sectionIndividuals = new WebSection(translate("Individualslist"), "persons", "persons_", format, ".html", 1, sizeIndiSection*2);
        }
     if (pagesIndividualsDetails) {
        sectionIndividualsDetails = new WebSection(translate("Individualsdetails"), "details", "personsdetails_", format, ".html", 1, sizeIndiSection);
        }
     if (pagesSources) {
        sectionSources = new WebSection(translate("Sources"), "sources", "sources_", format, ".html", 0, sizeIndiSection);
        }
     if (pagesMedia) {
        sectionMedia = new WebSection(translate("Media"), "media", "media_", format, ".html", 0, 30);
        }
     if (pagesMap) {
        sectionMap = new WebSection(translate("Map"), "map", "map", "", ".html", 1, 0);
        }
     if (pagesCities) {
        sectionCities = new WebSection(translate("Citieslist"), "cities", "cities", "", ".html", 1, 0);
        }
     if (pagesCitiesDetails) {
        sectionCitiesDetails = new WebSection(translate("CitiesDetails"), "citiesdetails", "citiesdetails_", format, ".html", 1, sizeIndiSection);
        }
     if (pagesDays) {
        sectionDays = new WebSection(translate("Dayslist"), "days", "days", "", ".html", 1, 0);
        }
     if (pagesDaysDetails) {
        sectionDaysDetails = new WebSection(translate("DaysDetails"), "daysdetails", "daysdetails_", format, ".html", 1, sizeIndiSection);
        }
     if (pagesSearch) {
        sectionSearch = new WebSection(translate("Search"), "search", "search_", format, ".html", 0, 0);
        }
     if (pagesStatsFrequent) {
        sectionStatsFrequent = new WebSection(translate("StatsFrequent"), "stats", "stats_", format, ".html", 1, 0);
        }
     if (pagesStatsImplex) {
        sectionStatsImplex = new WebSection(translate("StatsImplex"), "stats", "stats_", format, ".html", 2, 0);
        }
     if (pagesRepSosa) {
        sectionRepSosa = new WebSection(translate("RepSosa"), "repsosa", "repsosa_", format, ".html", 1, sizeIndiSection);
        }

    return;
  }

  /**
   * Get sections from outside                ***new section*** 
   */
  public WebSection getSection(String name) {
    if (name.compareTo("Lastnames") == 0)          return sectionLastnames;
    if (name.compareTo("Individualslist") == 0)    return sectionIndividuals;
    if (name.compareTo("Individualsdetails") == 0) return sectionIndividualsDetails;
    if (name.compareTo("Sources") == 0)            return sectionSources;
    if (name.compareTo("Media") == 0)              return sectionMedia;
    if (name.compareTo("Map") == 0)                return sectionMap;
    if (name.compareTo("Citieslist") == 0)         return sectionCities;
    if (name.compareTo("Citiesdetails") == 0)      return sectionCitiesDetails;
    if (name.compareTo("Dayslist") == 0)           return sectionDays;
    if (name.compareTo("Daysdetails") == 0)        return sectionDaysDetails;
    if (name.compareTo("Search") == 0)             return sectionSearch;
    if (name.compareTo("StatsFrequent") == 0)      return sectionStatsFrequent;
    if (name.compareTo("StatsImplex") == 0)        return sectionStatsImplex;
    if (name.compareTo("RepSosa") == 0)            return sectionRepSosa;
    return null;
  }


  /**
   * Public methods
   */
  public boolean usesStandardOut() {
    return false;
  }

  public File getDir() {
    return dir;
  }

  public String getTitle() {
    return idxTitle;
  }
  public String getBirthSymbol() {
      return OPTIONS.getBirthSymbol();
  }
  public String getBaptismSymbol() {
      return OPTIONS.getBaptismSymbol();
  }
  public String getMarriageSymbol() {
      return OPTIONS.getMarriageSymbol();
  }
  public String getDeathSymbol() {
      return OPTIONS.getDeathSymbol();
  }
  public String getBurialSymbol() {
      return OPTIONS.getBurialSymbol();
  }
  public String getOccuSymbol() {
      return OPTIONS.getOccuSymbol();
  }
  public String getResiSymbol() {
      return OPTIONS.getResiSymbol();
  }
  public PrivacyPolicy getPrivacyPolicy() {
      return OPTIONS.getPrivacyPolicy();
  }
  public String getBlankIndi() {
      return strBlank;
  }
  public boolean getDisplayFamily() {
      return displayFamily;
  }
  public boolean getDisplayChildren() {
      return displayChildren;
  }
  public boolean getDisplayStepSibling() {
      return displayStepSibling;
  }
  public boolean getDisplayRelations() {
      return displayRelations;
  }
  public boolean getDisplayNotes() {
      return displayNotes;
  }
  public boolean getDisplayId() {
      return displayId;
  }
  public boolean getDisplayEmail() {
      return displayEmail;
  }
  public String getEmailAddress() {
      return idxEmail;
  }
  public boolean getDisplaySourceSec() {
      return displaySourceSec;
  }
  public int getDisplaySrcImg() {
      return displaySrcImgType;
  }
  public boolean getCopySources() {
      return copySources;
  }
  public boolean getCopyMedia() {
      return copyMedia;
  }
  public boolean getDisplayMap() {
      return displayMap;
  }
  public boolean getDisplayUnkCities() {
      return displayUnkCities;
  }
  public WebHelper getHelper() {
    return wh;
  }
  public String getThemeDir() {
    return themeDir;
  }
  public int getUploadType() {
      return uploadType;
  }


  public void logInit() {
    println("----------- " + translate("log_title") + " -----------");
    println(translate("log_version") + " = "+translate("version"));
    Calendar rightNow = Calendar.getInstance();
    println(translate("log_timestamp") + " = " + DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(rightNow.getTime()));
    return;
  }

  public void logParameters(Indi indi, File dir) {
    println("----------- " + translate("log_options") + " -----------");
    println(translate("idxTitle")+" = "+idxTitle);
    println(translate("idxAuthor")+" = "+idxAuthor);
    println(translate("idxAddress")+" = "+idxAddress);
    println(translate("idxTel")+" = "+idxTel);
    println(translate("dispMessage")+" = "+dispMessage);
    println(translate("jurisdlvl")+" = "+jurisdlvl);
    println(translate("dispLonguest")+" = "+dispLonguest);
    println(translate("strBlank")+" = "+strBlank);
    println(translate("displayId")+" = "+displayId);
    println(translate("displayFamily")+" = "+displayFamily);
    println(translate("displayChildren")+" = "+displayChildren);
    println(translate("displayStepSibling")+" = "+displayStepSibling);
    println(translate("displayRelations")+" = "+displayRelations);
    println(translate("displayNotes")+" = "+displayNotes);
    println(translate("displaySourceSec")+" = "+displaySourceSec);
    println(translate("displaySrcImgType")+" = "+displaySrcImgTypes[displaySrcImgType]);
    println(translate("copySources")+" = "+copySources);
    println(translate("displayMediaSec")+" = "+displayMediaSec);
    println(translate("copyMedia")+" = "+copyMedia);
    println(translate("displayMap")+" = "+displayMap);
    println(translate("displayUnkCities")+" = "+displayUnkCities);
    println(translate("displayRepSosa")+" = "+displayRepSosa);
    println(translate("FTPuse")+" = "+FTPuse);
    println(translate("FTPhost")+" = "+FTPhost);
    println(translate("FTPTargetDir")+" = "+FTPTargetDir);
    println(translate("FTPuser")+" = ********");
    println(translate("FTPpassword")+" = ********");
    println(translate("siteDesc")+" = "+siteDesc);
    println(translate("uploadType")+" = "+uploadTypes[uploadType]);
    println(translate("resetRegister")+" = "+resetRegister);
    println(translate("shell")+" = "+shell);
    println(translate("AskDeCujus")+" = "+indi.toString());
    println(translate("AskTargetDir")+" = "+dir.toString());
    println("------------------------------------");
    println("");
    return;
  }


 /////////////////////////////////////////////////////////////////////////////////////////////////

 class GedcomStats
    {
    public Indi indiDeCujus = null;
    public Map pagesMap = null;
    public WebHelper wh = null;
    public String sectionDir  = "";

    public String deCujusLink  = "";
    public String deCujusName  = "";
    public int nbGen = 0;
    public int nbAncestors = 0;

    public Indi indiOlder   = null;
    public String olderLink    = "";
    public String olderName  = "";
    public String olderBirthDate  = "";

    public Indi longIndiG = null;
    public String longIndiGLink  = "";
    public String longIndiGName  = "";
    public int nbGenG = 0;
    public Indi longIndiA = null;
    public String longIndiALink  = "";
    public String longIndiAName  = "";
    public int nbAncestorsA = 0;

    public String place = "";

    public int nbIndis = 0;
    public int nbFams = 0;
    public int nbNames = 0;
    public int nbPlaces = 0;
    public int nbAscendants = 0;
    public int nbCousins = 0;
    public int nbOthers = 0;
    public int nbFamsWithKids = 0;
    public double avgKids = 0;

    // private variables
    private int nbGenTemp = 0;
    private int nbAncestorsTemp = 0;
    private Map gens = null;

    // constructor
    public GedcomStats(Indi indiDeCujus, Map pagesMap, WebHelper wh, String sectionDir) {
       this.indiDeCujus = indiDeCujus;
       this.pagesMap = new TreeMap(pagesMap);
       this.wh = wh; 
       this.sectionDir = sectionDir; 
       }

    public boolean update(Gedcom gedcom, boolean dispLonguest) {

       // sosa number 1
       deCujusLink = getLink(indiDeCujus);
       deCujusName = getNameShort(indiDeCujus);

       // number of generations and ancestors
       nbGenTemp = 0;
       nbAncestorsTemp = 0;
       gens = new TreeMap();          // will map generation to indis
       calcGenAncestors(indiDeCujus); // calculates nbGenTemp and nbAncestorsTemp
       nbGen = nbGenTemp;
       nbAncestors = nbAncestorsTemp;

       // Older ancestor and his/her birthdate
       indiOlder = indiDeCujus;
       boolean foundDate = false;
       PropertyDate propDateMin =  indiOlder.getBirthDate();
       if (propDateMin == null) propDateMin = new PropertyDate(2100);
       for (Iterator it = gens.keySet().iterator(); it.hasNext();) {
          Indi indi = (Indi) it.next(); 
          Integer calcGens = (Integer) gens.get(indi);
          if (calcGens == nbGen) {
             PropertyDate propDate = indi.getBirthDate();
             if (propDate == null) {  
                indiOlder = indi;
                }
             if (propDate != null && propDate.compareTo(propDateMin) < 0) {  
                propDateMin = propDate;
                foundDate = true;
                indiOlder = indi;
                }
             }
          }

       olderLink = getLink(indiOlder);
       olderName = getName(indiOlder);
       String sosa = wh.getSosa(indiOlder);
       if (sosa != null && sosa.length() != 0) {
          olderName += " ("+sosa+")";
          }
       olderBirthDate =  (foundDate) ? propDateMin.getDisplayValue() : translate("text_unknown_date");

       // Finds out which individuals has the most ancestors and generations
       if (dispLonguest) {
          calcLonguestLine(indiDeCujus);
          longIndiGLink = getLink(longIndiG);
          longIndiGName = getNameShort(longIndiG);
          longIndiALink = getLink(longIndiA);
          longIndiAName = getNameShort(longIndiA);
          }

       nbGen--;
       nbAncestors--;

       // number of individuals
       nbIndis = wh.getNbIndis(gedcom);
       nbAscendants = wh.getAncestors(indiDeCujus).size();
       nbCousins = wh.getCousins(indiDeCujus).size();
       nbOthers = nbIndis - nbAscendants - nbCousins;
       nbAscendants = Math.max(nbAscendants - 1, 0);


       //  number of places, main locations
       Collection entities = gedcom.getEntities();
       List placesProps = new ArrayList();
       for (Iterator it = entities.iterator(); it.hasNext();) {
          Entity ent = (Entity) it.next();
          wh.getPropertiesRecursively((Property)ent, placesProps, "PLAC");
          }
       nbPlaces = wh.getCities(gedcom).size();

       int max = 0;
       String placeMax = "";
       Integer val = 0; 
       String juridic = "";
       Map placeTop = new TreeMap();
       for (Iterator it = placesProps.iterator(); it.hasNext();) {
          Property prop = (Property) it.next();
          if (prop instanceof PropertyPlace) {
             juridic = wh.getPlace((PropertyPlace) prop, jurisdlvl);
             }
          else {
             juridic = prop.getValue();
             }
          if (juridic != null && juridic.length() > 0) {
             val = 1;
             if (placeTop.get(juridic) != null) {
                val = (Integer) placeTop.get(juridic) + 1;
                }
             placeTop.put(juridic, val); 
             if (val > max) {
                max = val;
                placeMax = juridic;
                }
             }
          }
       place = placeMax;


       // number of families and those with kids and average number of kids per marriage
       Collection families = gedcom.getEntities(Gedcom.FAM);
       nbFams = families.size();
       int cptKids = 0, cptFams = 0; 
       for (Iterator it = families.iterator(); it.hasNext(); ) {
         Fam family = (Fam)it.next();
         int nb = family.getNoOfChildren();
         if (nb > 0) {
            cptFams++;
            cptKids += nb;
            } 
         }
       nbFamsWithKids = cptFams;
       avgKids = (nbFamsWithKids > 0) ? (cptKids * 100 / nbFamsWithKids) : 0;
       avgKids /= 100;

       // number of names
       List names = wh.getLastNames(gedcom);
       nbNames = names.size();

       return true;
       }


    private void calcGenAncestors(Indi indiStart) {
       Integer calcGens = 1;
       nbGenTemp = 1;
       Fam famc;
       Indi indi = null, indiOther = null;
       List sosaList = new ArrayList();
       Set hs = new HashSet();
       sosaList.add(indiStart);
       gens.clear();
       gens.put(indiStart, calcGens); 
       for (ListIterator listIter = sosaList.listIterator(); listIter.hasNext();) {
          indi = (Indi) listIter.next();
          if (!hs.contains(indi)) {
             hs.add(indi);
             }
          calcGens = (Integer)gens.get(indi);
          if (calcGens == null) calcGens = 1;
          famc = indi.getFamilyWhereBiologicalChild();
          if (famc!=null)  {
             calcGens++;
             if (calcGens > nbGenTemp) nbGenTemp = calcGens;
             indiOther = famc.getWife();
             if (indiOther!=null && !hs.contains(indiOther)) {
                listIter.add(indiOther);
                gens.put(indiOther, calcGens); 
                listIter.previous();
                }
             indiOther = famc.getHusband();
             if (indiOther!=null && !hs.contains(indiOther)) {
                listIter.add(indiOther);
                gens.put(indiOther, calcGens); 
                listIter.previous();
                }
             }
          }
       nbAncestorsTemp = sosaList.size();
       }

    public void calcLonguestLine(Indi indiRef) {
      int nbG1 = 0, nbG2 = 0;
      int nbA1 = 0, nbA2 = 0;

      // get all individuals from Gedcom
      List indis = wh.getIndividuals(indiRef.getGedcom());

      // loop to look for longuest line
      for (Iterator it = indis.iterator(); it.hasNext();) {
        Indi indi = (Indi)it.next();
        nbGenTemp = 0;
        nbAncestorsTemp = 0;
        gens = new TreeMap();   
        calcGenAncestors(indi); 
        if (nbGenTemp > nbG1) { 
           nbG1 = nbGenTemp;
           nbG2 = nbAncestorsTemp;
           longIndiG = indi;
           }
        if ((nbGenTemp == nbG1) && (indi == indiRef)) { 
           nbG1 = nbGenTemp;
           nbG2 = nbAncestorsTemp;
           longIndiG = indi;
           }
        if (nbAncestorsTemp > nbA2) { 
           nbA1 = nbGenTemp;
           nbA2 = nbAncestorsTemp;
           longIndiA = indi;
           }
        if ((nbAncestorsTemp == nbA2) && (indi == indiRef)) { 
           nbA1 = nbGenTemp;
           nbA2 = nbAncestorsTemp;
           longIndiA = indi;
           }
        }
      nbGenG = nbG1-1;
      nbAncestorsA = nbA2-1;

      return;
      }

    public String getLink(Indi indi) {
       return ((sectionDir.length() == 0) ? "" : sectionDir + SEP) + (String) pagesMap.get(indi.getId()) + "#" + indi.getId();
      }

    public String getName(Indi indi) {
       String name = indi.getFirstName() + " " + wh.getLastName(indi);
       if (wh.isPrivate(indi)) name = "... ...";
       return name;
      }

    public String getNameShort(Indi indi) {
       String name = indi.getFirstName().substring(0,1) + ". " + wh.getLastName(indi);
       if (wh.isPrivate(indi)) name = "... ...";
       return name;
      }

    }

} // End_of_Report
