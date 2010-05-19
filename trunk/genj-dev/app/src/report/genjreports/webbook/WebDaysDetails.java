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
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyEvent;
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
import java.util.Comparator;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebDaysDetails {

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
  private Map<String, String> dayPage = new TreeMap<String, String>();
  private Map<String, String> personPage = new TreeMap<String, String>();
  private WebIndividualsDetails reportIndi = null;
  private String here2indiDir = "";

  public String[] Months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

  /**
   * Constructor
   */
  public WebDaysDetails(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run(WebIndividualsDetails reportIndi, WebSection webSectionIndi) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportIndi = reportIndi;
    if (reportIndi != null) {
       personPage = reportIndi.getPagesMap();
       here2indiDir = wh.buildLinkShort(section, webSectionIndi);
       }

    calcPages(wh.getDays(gedcom));
    exportData(dir, wh.getDays(gedcom));

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, List<String> days) {

    // Go through days
    String fileStr = "";
    File file = null;
    PrintWriter out = null;
    String dayfile = "";
    int cpt = 0;
    int nbDays = days.size();
    int previousPage = 0,
        currentPage  = 0,
        nextPage     = 0,
        lastPage     = (nbDays/section.nbPerPage)+1;

    for (Iterator it = days.iterator(); it.hasNext();) {
      String day = (String)it.next();
      cpt++;
      currentPage = (cpt/section.nbPerPage)+1; 
      previousPage = (currentPage == 1) ? 1 : currentPage-1; 
      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1; 
      dayfile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
      if (fileStr.compareTo(dayfile) != 0) {
         if (out != null) { 
            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
            wh.printCloseHTML(out);
            out.close();
            report.println(fileStr+" - Done.");
            }
         fileStr = dayfile;
         file = wh.getFileForName(dir, dayfile);
         out = wh.getWriter(file);
         wh.printOpenHTML(out, "Daysdetails", section);
         }
      exportLinks(out, dayfile, previousPage, nextPage, lastPage);
      exportDayDetails(out, day, dir, dayfile);
      // .. next day
     }
    if (out != null) { 
       exportLinks(out, dayfile, previousPage, nextPage, lastPage);
       wh.printCloseHTML(out);
       report.println(fileStr+" - Done.");
       }

    // done
    if (out != null) out.close();
  }

  /**
   * Exports individual details
   */
  private void exportDayDetails(PrintWriter out, String date, File dir, String dayfile) {

    // Day name 
    String month = report.translate(Months[Integer.valueOf(date.substring(0,2)) - 1]);
    String day = date.substring(2,4);
    String anchor = wh.htmlAnchorText(month) + day;
    out.println("<h2 class=\"unk\"><a name=\""+anchor+"\"></a>"+day+SPACE+wh.htmlText(month)+"</h2>");

    // All day properties that have that day  
    List<Property> listProps = wh.getDaysProps(date);
    Collections.sort(listProps, sortEvents);
    boolean first = true;
    for (Iterator p = listProps.iterator(); p.hasNext(); ) {
       Property prop = (Property)p.next(); 
       if ((prop == null) || (prop.getValue().length() == 0)) continue;
       boolean change = false;
       // Case of a change and a date
       if (first) {
          out.println("<div class=\"daycont\">");
          out.println("<span class=\"dayhd1\">"+wh.htmlText(report.translate("date_detail"))+"</span>");
          out.println("<span class=\"dayhd2\">"+wh.htmlText(report.translate("date_event"))+"</span>");
          out.println("<span class=\"dayhd3\">"+wh.htmlText(report.translate("date_indi"))+"</span>");
          out.println("<span class=\"spacer\">"+SPACE+"</span>");
          first = false;
          }
       out.println("<span class=\"dayevt1\">"+((PropertyDate) prop).toString()+"<br /></span>");
       out.println("<span class=\"dayevt2\">"+wh.htmlText(prop.getParent().getPropertyName())+"</span><span class=\"dayevt3\">");
       wrapEntity(out, prop.getEntity());
       out.println("</span>");
       }
    out.println("<span class=\"spacer\">"+SPACE+"</span>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");
    }


 /**
  * Comparator to sort events for a day
  */
  private Comparator sortEvents = new Comparator() {
     public int compare(Object o1, Object o2) {
        if ((o1 == null) && (o2 != null)) return -1; 
        if ((o1 != null) && (o2 == null)) return +1; 
        if ((o1 == null) && (o2 == null)) return 0; 

        Property prop1 = (Property)o1;
        Property prop2 = (Property)o2;

        // Otherwise, sort on dates
        PropertyDate date1 = (PropertyDate)prop1;
        PropertyDate date2 = (PropertyDate)prop2;
        if (date1 == null) return -1; 
        if (date2 == null) return +1; 
        if (date1.compareTo(date2) != 0)
           return date1.compareTo(date2);

        // Otherwise, sort on individuals
        Entity ent1 = prop1.getEntity();
        Entity ent2 = prop2.getEntity();

        if (ent1 == null) return -1; 
        if (ent2 == null) return +1; 
        return ent1.toString().compareTo(ent2.toString());
        }
     };

  /**
   * Print name with link
   */
  private void wrapEntity(PrintWriter out, Entity ent) {
     if (ent instanceof Indi) {
        Indi indi = (Indi)ent;
        wrapName(out, indi);
        wrapDate(out, indi, true);
        } 
     if (ent instanceof Fam) {
        Fam fam = (Fam)ent;
        Indi husband = fam.getHusband();
        Indi wife    = fam.getWife();
        wrapName(out, husband);
        wrapDate(out, husband, true);
        out.println(SPACE+"+");
        wrapName(out, wife);
        wrapDate(out, wife, true);
        } 
     }

  /**
   * Print name with link
   */
  private void wrapName(PrintWriter out, Indi indi) {
     //
     String id = (indi == null) ? "" : indi.getId();
     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
     String personFile = (indi == null) ? "" : (String)personPage.get(id);
     if (indi != null) out.print("<a href=\""+here2indiDir+personFile+'#'+id+"\">");
     if (wh.isPrivate(indi)) {
        name = "..., ...";
        }
     if (name.compareTo(",") == 0) name = ""; 
     out.print(wh.htmlText(name));
     String sosa = wh.getSosa(indi);
     if (sosa != null && sosa.length() != 0) {
        out.println(SPACE+"("+sosa+")");
        }
     if (report.getDisplayId() && id != null && id.length() != 0) {
        out.println(SPACE+"("+id+")");
        }
     if (indi != null) out.print("</a>");
     }


   /**
   * Print dates of individual
   */
  private void wrapDate(PrintWriter out, Indi indi, boolean parenthesis) {
     //
     String id = (indi == null) ? "" : indi.getId();
     PropertyDate bdate = (indi == null) ? null : indi.getBirthDate();
     PropertyDate ddate = (indi == null) ? null : indi.getDeathDate();
     String birthdate = (indi == null) || (bdate == null) ? "." : bdate.toString();
     String deathdate = (indi == null) || (ddate == null) ? "" : " - "+ddate.toString();
     String date = (birthdate+deathdate).trim();
     if (wh.isPrivate(indi)) {
        date = ". - .";
        }
     if (date.compareTo(".") != 0) {  
        out.print(SPACE + (parenthesis?"(":"") + wh.htmlText(date) + (parenthesis?")":""));
        }
     }

  /**
   * Calculate pages for day details
   */
  private void calcPages(List<String> days) {
    String dayfile = "", fileStr = "";
    int cpt = 0;
    for (Iterator it = days.iterator(); it.hasNext();) {
      String day = (String)it.next();
      cpt++;
      dayfile = section.sectionPrefix + String.format(section.formatNbrs, (cpt/section.nbPerPage)+1) + section.sectionSuffix;
      if (fileStr.compareTo(dayfile) != 0) {
         fileStr = dayfile;
         }
      dayPage.put(day, dayfile);
      }
    }

  /**
   * Provide links map to outside caller
   */
  public Map getPagesMap() {
     return dayPage;
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
