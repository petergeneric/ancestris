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
import genj.gedcom.PropertyPlace;
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
import java.lang.Class;
import java.net.URLClassLoader;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.SortedSet;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSearch {

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

  private String searchFile = "";
  private String titleFile = "";
  private String inputFile = "";
  private String resultsFile = "";
  private String resourceFile = "";


  /**
   * Constructor
   */
  public WebSearch(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run(List indis, WebIndividualsDetails reportIndi, WebSection webSectionIndi) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportIndi = reportIndi;
    if (reportIndi != null) {
       personPage = reportIndi.getPagesMap();
       here2indiDir = wh.buildLinkShort(section, webSectionIndi);
       }

    searchFile = section.sectionPrefix + String.format(section.formatNbrs, 0) + section.sectionSuffix;
    titleFile = section.sectionPrefix + String.format(section.formatNbrs, 1) + section.sectionSuffix;
    inputFile = section.sectionPrefix + String.format(section.formatNbrs, 2) + section.sectionSuffix;
    resultsFile = section.sectionPrefix + String.format(section.formatNbrs, 3) + section.sectionSuffix;
    resourceFile = section.sectionPrefix + String.format(section.formatNbrs, 4) + ".js";

    exportData(dir, indis);

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, List indis) {

    // Create search frames page
    exportFrames(dir, searchFile);

    // Create search frames page
    exportTitle(dir, titleFile);

    // Create search input page
    exportInput(dir, inputFile);

    // Create search results page
    exportResults(dir, resultsFile);

    // Create js content file
    exportResources(dir, resourceFile, indis);
  }


  /**
   * Exports frames
   */
  private void exportFrames(File dir, String exportfile) {
    File file = wh.getFileForName(dir, exportfile);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return; 
    out.println("<!DOCTYPE html PUBLIC  \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">");
    out.println("<html>");
    out.println("<head>");
    out.println("<title>"+wh.htmlText(report.getTitle())+SPACE+"-"+SPACE+wh.htmlText(report.translate("Search"))+"</title>");
    out.println("</head>");
    out.println("<frameset rows=\"70,*\" framespacing=\"0\">");
    out.println("<frame src=\""+titleFile+"\" name=input_frame frameborder=\"0\" scrolling=\"no\" noresize >");
    out.println("<frameset cols=\"30%,*\" framespacing=\"0\">");
    out.println("<frame src=\""+inputFile+"\" name=input_frame frameborder=\"0\" scrolling=\"auto\">");
    out.println("<frame src=\""+resultsFile+"\" name=\"resultat\" frameborder=\"0\" scrolling=\"auto\">");
    out.println("</frameset>");
    out.println("</frameset>");
    out.println("</html>");
    report.println(searchFile+" - Done.");
    out.close();
    }

  /**
   * Exports input Frame
   */
  private void exportTitle(File dir, String exportfile) {
    File file = wh.getFileForName(dir, exportfile);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return; 
    wh.printOpenHTML(out, "Search", section);
    wh.printCloseHTML(out);
    report.println(exportfile+" - Done.");
    out.close();
    }

  /**
   * Exports input Frame
   */
  private void exportInput(File dir, String exportfile) {
    File file = wh.getFileForName(dir, exportfile);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return;
    wh.printOpenHTML(out, null, section);
    out.println("<p class=\"searchdecal\">"+report.translate("search_criteria")+"</p>");
    out.println("<form method=\"get\" action=\""+resultsFile+"\" target=\"resultat\" accept-charset=\"iso-8859-1\">");
    out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"searchtable\">");
    out.println("<tr><td>"+report.translate("search_firstname")+":</td><td><input name=\"key_fn\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xfn\" type=\"checkbox\" value=\"on\" />"+report.translate("search_exact")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_lastname")+":</td><td><input name=\"key_ln\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xln\" type=\"checkbox\" value=\"on\" />"+report.translate("search_exact")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_place")+":</td><td><input name=\"key_pl\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xpl\" type=\"checkbox\" value=\"on\" />"+report.translate("search_exact")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_genjid")+":</td><td><input name=\"key_id\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xid\" type=\"checkbox\" value=\"on\" />"+report.translate("search_exact")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_sosa")+":</td><td><input name=\"key_so\" type=\"text\" size=\"15\" />&nbsp;<input name=\"key_xso\" type=\"checkbox\" value=\"on\" />"+report.translate("search_exact")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_birthd")+":</td><td>"+report.translate("search_between")+"&nbsp;<input name=\"key_1bi\" type=\"text\" size=\"5\" />&nbsp;"+report.translate("search_dateand")+"&nbsp;<input name=\"key_2bi\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xbi\" type=\"checkbox\" value=\"on\" />"+report.translate("search_not")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_marrid")+":</td><td>"+report.translate("search_between")+"&nbsp;<input name=\"key_1ma\" type=\"text\" size=\"5\" />&nbsp;"+report.translate("search_dateand")+"&nbsp;<input name=\"key_2ma\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xma\" type=\"checkbox\" value=\"on\" />"+report.translate("search_not")+"</td></tr>");

    out.println("<tr><td>"+report.translate("search_deathd")+":</td><td>"+report.translate("search_between")+"&nbsp;<input name=\"key_1de\" type=\"text\" size=\"5\" />&nbsp;"+report.translate("search_dateand")+"&nbsp;<input name=\"key_2de\" type=\"text\" size=\"5\" />&nbsp;&nbsp;<input name=\"key_xde\" type=\"checkbox\" value=\"on\" />"+report.translate("search_not")+"</td></tr>");

    out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input name=\"andor\" type=\"radio\" value=\"and\" checked />"+report.translate("search_and")+"&nbsp;&nbsp;&nbsp;<input name=\"andor\" type=\"radio\" value=\"or\" />"+report.translate("search_or")+"</td></tr>");

    out.println("<tr><td colspan=\"2\" align=\"center\"><br /><input name=\"OK\" type=\"submit\" value=\""+report.translate("search_go")+"\" />&nbsp;&nbsp;<input name=\"reset\" type=\"reset\" value=\""+report.translate("search_reset")+"\" />&nbsp;&nbsp;<input name=\"home\" type=\"button\" value=\""+report.translate("alt_home")+"\" onclick=\"top.window.location.href='"+wh.getHomeLink(section)+"'\" /></td></tr>");
    out.println("</table>");
    out.println("</form>");
    wh.printCloseHTML(out);
    report.println(exportfile+" - Done.");
    out.close();
    }

  /**
   * Exports results Frame
   */
  private void exportResults(File dir, String exportfile) {
    File file = wh.getFileForName(dir, exportfile);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return; 
    wh.printOpenHTML(out, null, section);
    out.println("<script src=\""+resourceFile+"\"></script>");
    out.println("<script language=\"JavaScript\">");
    try {
       out.println(readFile(wh.getGenjImagesDir()+File.separator+"search.js"));
       } catch (IOException e) 
       { 
         //e.printStackTrace(); 
         report.println(e.toString());
       } 
    out.println("</script>");
    wh.printCloseHTML(out);
    report.println(exportfile+" - Done.");
    out.close();
    }

  /**
   * Exports resource file
   */
  private void exportResources(File dir, String exportfile, List indis) {
    File file = wh.getFileForName(dir, exportfile);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return;

    Map<String,List> table = new TreeMap<String, List>();

    //Produce firstNames list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       String word = indi.getFirstName();
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_firstnames", table); 

    //Produce lastNames list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       String word = wh.getLastName(indi);
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_lastnames", table); 

    //Produce places list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       List places = indi.getProperties(PropertyPlace.class);
       for (Iterator itp = places.iterator(); itp.hasNext();) {
          PropertyPlace place = (PropertyPlace)itp.next();
          if (place == null) continue;
          String word = place.toString();
          String key = "";
          if (word != null) key = cleanString(word);
          List<String> ids = (List<String>)table.get(key);
          if (ids == null) {
             ids = new ArrayList<String>();
             }
          ids.add(indi.getId());
          table.put(key, ids);
          }
       }
    writeTable(out, "list_places", table); 

    //Produce ids list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       String word = indi.getId();
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_ids", table); 

    //Produce sosa list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       String word = wh.getSosa(indi);
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_sosas", table);

    //Produce births list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       PropertyDate date = (indi == null) ? null : indi.getBirthDate();
       if ((indi == null) || (date == null)) continue;
       if (!date.isValid()) continue;
       int start = 0;
       try {
          start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
       } catch (Throwable t) { 
         //t.printStackTrace(); 
         report.println(t.toString()+" for "+indi.toString()+" "+date.toString()); 
       }
       String word = Integer.toString(start);
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_births", table);

    //Produce marriages list
    table.clear();
    List families = new ArrayList(gedcom.getEntities(Gedcom.FAM));
    for (Iterator it = families.iterator(); it.hasNext();) {
       Fam family = (Fam)it.next();
       PropertyDate date = (family == null) ? null : family.getMarriageDate();
       if ((family == null) || (date == null)) continue;
       if (!date.isValid()) continue;
       int start = 0;
       try {
          start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
       } catch (Throwable t) { 
         //t.printStackTrace(); 
         report.println(t.toString()+" for "+family.toString()+" "+date.toString()); 
       }
       String word = Integer.toString(start);
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       Indi husband = family.getHusband();
       if (husband != null) ids.add(husband.getId());
       Indi wife = family.getWife();
       if (wife != null) ids.add(wife.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_marriages", table);

    //Produce death list
    table.clear();
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       PropertyDate date = (indi == null) ? null : indi.getDeathDate();
       if ((indi == null) || (date == null)) continue;
       if (!date.isValid()) continue;
       int start = 0;
       try {
          start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
       } catch (Throwable t) { 
         //t.printStackTrace(); 
         report.println(t.toString()+" for "+indi.toString()+" "+date.toString()); 
       }
       String word = Integer.toString(start);
       String key = "";
       if (word != null) key = cleanString(word);
       List<String> ids = (List<String>)table.get(key);
       if (ids == null) {
          ids = new ArrayList<String>();
          }
       ids.add(indi.getId());
       table.put(key, ids);
       }
    writeTable(out, "list_deaths", table);

    writeTableIndis(out, indis);

    report.println(exportfile+" - Done.");
    out.close();
    }


  /**
   * Read input file and put into string
   */
  private String readFile(String filename) throws IOException {
    String text0 = wh.readFile(filename);
    String text  = text0.replaceAll("search_please", report.translate("search_please"))
                        .replaceAll("search_results1", report.translate("search_results1"))
                        .replaceAll("search_results2", report.translate("search_results2"))
                        .replaceAll("alt_male", report.translate("alt_male"))
                        .replaceAll("alt_female", report.translate("alt_female"))
                        .replaceAll("alt_unknown", report.translate("alt_unknown"))
                        .replaceAll("searcht_sex", report.translate("searcht_sex"))
                        .replaceAll("searcht_id", report.translate("searcht_id"))
                        .replaceAll("searcht_name", report.translate("searcht_name"))
                        .replaceAll("searcht_sosa", report.translate("searcht_sosa"))
                        .replaceAll("searcht_bdate", report.translate("searcht_bdate"))
                        .replaceAll("searcht_ddate", report.translate("searcht_ddate"));
    return text;
    }

  /**
   * Write file from table
   */
  private void writeTable(PrintWriter out, String tableName, Map table) {

    StringBuffer list = new StringBuffer("var "+tableName+" = [");
    StringBuffer listID = new StringBuffer("var "+tableName+"ID = [");
    int cpt = 0, cptID = 0;
    for (Iterator itk = table.keySet().iterator(); itk.hasNext(); ) {
       String key = (String)itk.next();
       list.append((cpt == 0 ? "" : ",")+"\""+key+"\"");
       listID.append((cpt == 0 ? "" : ",")+"\"");
       List<String> ids = (List<String>)table.get(key);
       cptID = 0;
       for (Iterator it = ids.iterator(); it.hasNext();) {
          String id = (String)it.next();
          listID.append((cptID == 0 ? "" : "|")+id);
          cptID++;
          }
       listID.append("\"");
       cpt++;
       }
    list.append("]");
    listID.append("]");
    out.println(list.toString());
    out.println(listID.toString());
    return;
    }

  /**
   * Write file from table
   */
  private void writeTableIndis(PrintWriter out, List indis) {

    //out.println("var ID = [\"I001\",\"I002\",\"I003\",\"I004\",\"I005\"]");
    //out.println("var IDdisplay = [\"Frederic Lapeyre (29 Oct 1968) (I001) |001|I001\",\"Jean Sebastien Frederic Surrel|001|I002\",\"Sebastien Lapeyre|002|I003\",\"Raymond Fred|003|I004\",\"Fred Surrel|001|I005\"]");
    StringBuffer list = new StringBuffer("var ID = [");
    StringBuffer listID = new StringBuffer("var IDdisplay = [");
    int cpt = 0;
    for (Iterator it1 = indis.iterator(); it1.hasNext(); ) {
       Indi indi = (Indi)it1.next();
       list.append((cpt == 0 ? "" : ",")+"\""+indi.getId()+"\"");
       listID.append((cpt == 0 ? "" : ",")+"\"");
       listID.append(indi.getSex()+"|"+indi.getId()+"|"+getPage(indi)+"|"+getName(indi)+"|"+getSosa(indi)+"|"+getBDate(indi)+"|"+getDDate(indi));
       listID.append("\"");
       cpt++;
       }
    list.append("]");
    listID.append("]");
    out.println(list.toString());
    out.println(listID.toString());
    return;
    }

  /**
   * Get page of individual
   */
  private String getPage(Indi indi) {
     String id = (indi == null) ? "" : indi.getId();
     String page = (indi == null) ? "" : ( (personPage == null) ? "" : (String)personPage.get(id));
     return ""+wh.extractNumber(page);
     }

  /**
   * Get name of individual
   */
  private String getName(Indi indi) {
     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
     if (wh.isPrivate(indi)) { name = "..., ..."; }
     if (name.compareTo(",") == 0) name = ""; 
     String result = name.replaceAll("\"","");
     return result;
     }

  /**
   * Get sosa of individual
   */
  private String getSosa(Indi indi) {
     String sosa = wh.getSosa(indi);
     return ((sosa != null && sosa.length() != 0) ? sosa : "");
     }

  /**
   * Get dates of individual
   */
  private String getBDate(Indi indi) {
     PropertyDate bdate = (indi == null) ? null : indi.getBirthDate();
     String date = (indi == null) || (bdate == null) ? "" : bdate.toString().trim();
     return date;
     }

  private String getDDate(Indi indi) {
     PropertyDate ddate = (indi == null) ? null : indi.getDeathDate();
     String date = (indi == null) || (ddate == null) ? "" : ddate.toString().trim();
     return date;
     }

  /**
   * Clean strings
   */
  private String cleanString(String str) {
     return str.toUpperCase().replaceAll("\"", "").replaceAll("\\\\", "/");
     }


} // End_of_Report
