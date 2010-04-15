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
import java.util.Comparator;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebStatsFrequent {

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

  private Map<String, String> namePage = new TreeMap<String, String>();
  private WebIndividuals reportIndi = null;
  private String here2indiDir = "";
  private Map<String, String> cityPage = new TreeMap<String, String>();
  private WebCitiesDetails reportCity = null;
  private String here2cityDir = "";
  private Map<String, String> dayPage = new TreeMap<String, String>();
  private WebDaysDetails reportDay = null;
  private String here2dayDir = "";

  private final static int
    TYPE_LASTNAME = 0,
    TYPE_LOCATION = 1,
    TYPE_DATE = 2;

  private class Info {
     String key;
     int    frequency;
     }

  private List<Info> lastnamesList = new ArrayList<Info>();
  private List<Info> citiesList = new ArrayList<Info>();
  private List<Info> daysList = new ArrayList<Info>();

  public String[] Months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };


  /**
   * Constructor
   */
  public WebStatsFrequent(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run(Indi indi, WebIndividuals reportIndi, WebSection webSectionIndi, WebCitiesDetails reportCity, WebSection webSectionCity, WebDaysDetails reportDay, WebSection webSectionDay) {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportIndi = reportIndi;
    if (reportIndi != null) {
       namePage = reportIndi.getPagesMap();
       here2indiDir = wh.buildLinkShort(section, webSectionIndi);
       }

    this.reportCity = reportCity;
    if (reportCity != null) {
       cityPage = reportCity.getPagesMap();
       here2cityDir = wh.buildLinkShort(section, webSectionCity);
       }

    this.reportDay = reportDay;
    if (reportDay != null) {
       dayPage = reportDay.getPagesMap();
       here2dayDir = wh.buildLinkShort(section, webSectionDay);
       }

    exportData(dir, indi);

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, Indi indi) {

    // Opens page
    String fileStr = section.sectionPrefix + String.format(section.formatNbrs, 1) + section.sectionSuffix;
    File file = wh.getFileForName(dir, fileStr);
    PrintWriter out = wh.getWriter(file);
    if (out == null) return;

    // Compute frequencies of LastName
    computeFrequency(TYPE_LASTNAME, lastnamesList);
    computeFrequency(TYPE_LOCATION, citiesList);
    computeFrequency(TYPE_DATE, daysList);
 
    wh.printOpenHTML(out, "StatsFrequent", section);
    wh.printHomeLink(out, section);

    // Print header
    printHeader(out, indi);

    // Print implexe statistics
    printFrequency(out);

    // Closes page
    wh.printCloseHTML(out);
    report.println(fileStr+" - Done.");
    out.close();

  }

  /**
   * Computes frequency of information for given information type
   */
  private void computeFrequency(int type, List<Info> info) {

     if (type == TYPE_LASTNAME) {
        lastnamesList.clear();
        Iterator itr = wh.getLastNames(gedcom).iterator();
        while (itr.hasNext()) {
           Info iOccu = new Info();
           iOccu.key = itr.next().toString();
           iOccu.frequency = wh.getLastNameCount(iOccu.key);
           lastnamesList.add(iOccu);
           }
        Collections.sort(lastnamesList, sortbyFrequency);
        }
     if (type == TYPE_LOCATION) {
        citiesList.clear();
        Iterator itr = wh.getCities(gedcom).iterator();
        while (itr.hasNext()) {
           Info iOccu = new Info();
           iOccu.key = itr.next().toString();
           iOccu.frequency = wh.getCitiesCount(iOccu.key);
           citiesList.add(iOccu);
           }
        Collections.sort(citiesList, sortbyFrequency);
        }
     if (type == TYPE_DATE) {
        daysList.clear();
        Iterator itr = wh.getDays(gedcom).iterator();
        while (itr.hasNext()) {
           Info iOccu = new Info();
           iOccu.key = itr.next().toString();
           iOccu.frequency = wh.getDaysCount(iOccu.key);
           daysList.add(iOccu);
           }
        Collections.sort(daysList, sortbyFrequency);
        }
     }

 /**
  * Comparator to sort by frequency
  */
  private Comparator sortbyFrequency = new Comparator() {
     public int compare(Object o1, Object o2) {
        Info info1 = (Info)o1;
        Info info2 = (Info)o2;
        if (info2.frequency == info1.frequency)
           return wh.sortLastnames.compare(info1.key, info2.key);
        return info2.frequency - info1.frequency;
        }
     };

    /**
     * Print report header.
     */
    private void printHeader(PrintWriter out, Indi indi) {

        // Print description
        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("frequency_description"))+"</span></p>");
        out.println("<p class=\"description\">"+wh.htmlText(report.translate("frequency_info"))+"</p>");
        out.println("<div class=\"spacer\">"+SPACE+"</div>");
        out.println("</div>");

    }

    /**
     * Print implexe statistics.
     */
    private void printFrequency(PrintWriter out) {

        out.println("<div class=\"contreport2\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">"+wh.htmlText(report.translate("frequency_table"))+"</span></p>");

        // Print header
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"column1\"><thead><tr>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_rank"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_lastname"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_frequency"))+"</th>");
        out.println("<th>"+SPACE+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_city"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_frequency"))+"</th>");
        out.println("<th>"+SPACE+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_day"))+"</th>");
        out.println("<th>"+wh.htmlText(report.translate("frequency_header_frequency"))+"</th>");
        out.println("</tr></thead>");

        // Iteration on generations
        out.println("<tbody>");
        int l = Math.max(lastnamesList.size(), Math.max(citiesList.size(), daysList.size()));
        for (int i = 0; i < l ; i++) {
            // Open row
            out.println("<tr>");
            // Print rank
            out.println("<td>"+(i+1)+"</td>");
            // Print lastname
            printRowElement(TYPE_LASTNAME, out, i, lastnamesList, here2indiDir, namePage);
            out.println("<td>"+SPACE+"</td>");
            // Print city
            printRowElement(TYPE_LOCATION, out, i, citiesList, here2cityDir, cityPage);
            out.println("<td>"+SPACE+"</td>");
            // Print day
            printRowElement(TYPE_DATE, out, i, daysList, here2dayDir, dayPage);
            // Close row
            out.println("</tr>");
            }
        out.println("</tbody></table>");
        out.println("<div class=\"spacer\">"+SPACE+"</div></div>");
    }


    /**
     * Print row element
     */
    private void printRowElement(int type, PrintWriter out, int i, List list, String here2Dir, Map<String, String> pages) {
       if (i < list.size()) {
          Info info = (Info) list.get(i);
          String text = null;
          String anchor = null;
          String page = null;
          if (type == TYPE_DATE) {
             String month = report.translate(Months[Integer.valueOf(info.key.substring(0,2)) - 1]);
             String day = info.key.substring(2,4); 
             text = day + SPACE + wh.htmlText(month);
             anchor = wh.htmlAnchorText(month) + day;
             page = here2Dir+pages.get(info.key);
             }
          else {
             text = wh.htmlText(info.key);
             anchor = wh.htmlAnchorText(info.key); 
             page = here2Dir+pages.get(anchor);
             }
          out.println("<td><a href=\""+page+"#"+anchor+"\">"+text+"</a></td>");
          out.println("<td>"+wh.htmlText(Integer.toString(info.frequency))+"</td>");
          }
       else {
          out.println("<td>&nbsp;</td>");
          out.println("<td>&nbsp;</td>");
          }
       }

} // End_of_Report
