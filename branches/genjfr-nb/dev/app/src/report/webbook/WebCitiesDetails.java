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
public class WebCitiesDetails {

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
  private Map<String, String> cityPage = new TreeMap<String, String>();
  private Map<String, String> personPage = new TreeMap<String, String>();
  private WebIndividualsDetails reportIndi = null;
  private String here2indiDir = "";


  /**
   * Constructor
   */
  public WebCitiesDetails(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
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

    calcPages(wh.getCities(gedcom));
    exportData(dir, wh.getCities(gedcom));

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, List<String> cities) {

    // Go through cities
    String fileStr = "";
    File file = null;
    PrintWriter out = null;
    String cityfile = "";
    int cpt = 0;
    int nbCities = cities.size();
    int previousPage = 0,
        currentPage  = 0,
        nextPage     = 0,
        lastPage     = (nbCities/section.nbPerPage)+1;

    for (Iterator it = cities.iterator(); it.hasNext();) {
      String city = (String)it.next();
      cpt++;
      currentPage = (cpt/section.nbPerPage)+1; 
      previousPage = (currentPage == 1) ? 1 : currentPage-1; 
      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1; 
      cityfile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
      if (fileStr.compareTo(cityfile) != 0) {
         if (out != null) { 
            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
            wh.printCloseHTML(out);
            out.close();
            report.println(fileStr+" - Done.");
            }
         fileStr = cityfile;
         file = wh.getFileForName(dir, cityfile);
         out = wh.getWriter(file);
         wh.printOpenHTML(out, "Citiesdetails", section);
         }
      exportLinks(out, cityfile, previousPage, nextPage, lastPage);
      exportCityDetails(out, city, dir, cityfile);
      // .. next city
     }
    if (out != null) { 
       exportLinks(out, cityfile, previousPage, nextPage, lastPage);
       wh.printCloseHTML(out);
       report.println(fileStr+" - Done.");
       }

    // done
    if (out != null) out.close();
  }

  /**
   * Exports individual details
   */
  private void exportCityDetails(PrintWriter out, String city, File dir, String cityfile) {

    // City name 
    out.println("<h2 class=\"unk\"><a name=\""+wh.htmlAnchorText(city)+"\"></a>"+wh.htmlText(city)+"</h2>");

    // All city properties that have that city  
    List<Property> listProps = wh.getCitiesProps(city);
    Collections.sort(listProps, sortEvents);
    String lastFullname = "";
    boolean first = true;
    for (Iterator p = listProps.iterator(); p.hasNext(); ) {
       Property prop = (Property)p.next(); 
       if ((prop == null) || (prop.getValue().length() == 0)) continue;
       String fullname = getFullname(prop);
       boolean change = false;
       if (fullname.compareTo(lastFullname) != 0) change = true;
       lastFullname = fullname;
       // Case of a change and a place, display formatted place
       if (change) {
          if (!first) {
             out.println("<span class=\"spacer\">"+SPACE+"</span></div>");
             out.println("<div class=\"spacer\">"+SPACE+"</div>");
             out.println("</div>");
             }
          out.println("<div class=\"citycont\">");
          out.println("<div class=\"citycont1\">");
          first = false;
          if (prop instanceof PropertyPlace) {
             displayPlace(out, city, prop);
             }
          else {
             displayAddr(out, city, prop);
             }
          displayHeader(out);
          }
       displayEvent(out, prop);
       }
    out.println("<span class=\"spacer\">"+SPACE+"</span></div>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");
    }


 /**
  * Comparator to sort events for a city
  */
  private Comparator sortEvents = new Comparator() {
     public int compare(Object o1, Object o2) {
        if ((o1 == null) && (o2 != null)) return -1; 
        if ((o1 != null) && (o2 == null)) return +1; 
        if ((o1 == null) && (o2 == null)) return 0; 

        Property prop1 = (Property)o1;
        Property prop2 = (Property)o2;

        // If fullnames different, return sorted strings
        String fullname1 = getFullname(prop1);
        String fullname2 = getFullname(prop2);
        if (fullname1 == null) return -1; 
        if (fullname2 == null) return +1; 
        if (fullname1.compareTo(fullname2) != 0)
           return fullname1.compareTo(fullname2);

        // Otherwise, sort on dates
        PropertyDate date1 = getDate(prop1);
        PropertyDate date2 = getDate(prop2);
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
   * Get fullname of place
   */
  public String getFullname(Property prop) {
     String fullname = "";
     if (prop instanceof PropertyPlace) {
        fullname = prop.toString();
        }
     else {
        Property addr = prop.getParent();
        Property stae = null, post = null, country = null;
        if (addr != null) { 
           stae = addr.getProperty("STAE");
           post = addr.getProperty("POST");
           country = addr.getProperty("CTRY");
           }
        fullname = prop.toString();
        if (stae != null) {
           fullname += ","+stae.toString();
           }
        if (post != null) {
           fullname += ","+post.toString();
           }
        if (country != null) {
           fullname += ","+country.toString();
           }
        }
     return fullname;
     }

  /**
   * Get city
   */
  public String getCity(Property prop) {
     String city = "";
     if (prop instanceof PropertyPlace) {
        city = ((PropertyPlace)prop).getCity().trim();
        }
     else {
        city = prop.getValue().trim();
        }
     return city;
     }

  /**
   * Get country
   */
  public String getCountry(Property prop) {
     String ctry = "";
     if (prop instanceof PropertyPlace) {
        String[] dataBits = prop.toString().split("\\,", -1);
        ctry = dataBits[dataBits.length-1].trim();
        }
     else {
        Property addr = prop.getParent();
        Property country = null;
        if (addr != null) 
           country = addr.getProperty("CTRY");
        if (country != null)
           ctry = country.toString().trim();
        }
     return ctry;
     }

  /**
   * Get date of event of place
   */
  public PropertyDate getDate(Property prop) {
     Property date = null;
     if (prop instanceof PropertyPlace) {
        Property parent = prop.getParent();
        if (parent != null) 
           date = parent.getProperty("DATE");
        }
     else {
        Property parent = prop.getParent();
        Property gparent = null;
        if (parent != null) 
           gparent = parent.getParent();
        if (gparent != null)
           date = gparent.getProperty("DATE");
        }
     if (date instanceof PropertyDate)
        return (PropertyDate)date;
     else
        return null;
     }

  /**
   * Display formatted place
   */
  private void displayPlace(PrintWriter out, String city, Property prop) {
     out.println("<p class=\"cityloc\"><span class=\"gras\">"+wh.htmlText(report.translate("place_loc"))+"</span></p>");
     out.println("<span class=\"cityloc1\">");
     //String[] placeBits = gedcom.getPlaceFormat().split("\\,", -1);
     String[] dataBits = prop.toString().split("\\,", -1);
     boolean display = false;
     for (int i = 0 ; i < dataBits.length; i++) { 
        if (dataBits[i].length() > 0) {
           out.println(wh.htmlText(dataBits[i]));
           if (!display) { 
              displayLink2Map(out, prop, city);
              display = true;
              }
           out.println("<br />");
           }
        }
     out.println("</span></div>");
     }

  /**
   * Display formatted address
   */
  private void displayAddr(PrintWriter out, String city, Property prop) {
     out.println("<p class=\"cityloc\"><span class=\"gras\">"+wh.htmlText(report.translate("place_loc"))+"</span></p>");
     out.println("<span class=\"cityloc1\">");
     out.println(wh.htmlText(prop.getValue()));
     displayLink2Map(out, prop, city);
     out.println("<br />");
     Property addr = prop.getParent();
     Property stae = null, post = null, country = null;
     if (addr != null) { 
        stae = addr.getProperty("STAE");
        post = addr.getProperty("POST");
        country = addr.getProperty("CTRY");
        }
     if (stae != null && stae.getValue().length() != 0) {
        out.println(wh.htmlText(stae.getValue())+"<br />");
        }
     if (post != null && post.getValue().length() != 0) {
        out.println(wh.htmlText(post.getValue())+"<br />");
        }
     if (country != null && country.getValue().length() != 0) {
        out.println(wh.htmlText(country.getValue())+"<br />");
        }
     out.println("</span></div>");
     }

  /**
   * Display link to map if needed
   */
  private void displayLink2Map(PrintWriter out, Property prop, String city) {
    if (report.getDisplayMap()) {
       out.println(SPACE+SPACE+"<a href=\"../map/map.html?"+wh.htmlAnchorText(getFullname(prop))+"\"><img src=\""+themeDir+"map.gif\" alt=\""+wh.htmlText(city)+"\" title=\""+wh.htmlText(report.translate("map_of", city))+"\"/></a>");
       }
    }


  /**
   * Display event header
   */
  private void displayHeader(PrintWriter out) {
     out.println("<div class=\"citycont2\">");
     out.println("<p class=\"cityevt\"><span class=\"gras\">"+wh.htmlText(report.translate("place_event"))+"</span></p>");
     }

  /**
   * Display event 
   */
  private void displayEvent(PrintWriter out, Property prop) {
     Property parent = prop.getParent();
     Property event = null;
     if (parent == null) return;
     if (prop instanceof PropertyPlace) {
        event = parent;
        }
     else {
        event = parent.getParent();
        }
     if (event == null) return;
     String date = report.translate("place_nodate");
     if (getDate(prop) != null) {
        date = getDate(prop).toString();
        }
     out.println("<span class=\"cityevt1\">"+wh.htmlText(date)+"</span><span class=\"cityevt2\">"+wh.htmlText(event.getPropertyName())+"</span><span class=\"cityevt3\">");
     wrapEntity(out, prop.getEntity());
     out.println("</span>");
     out.println("<span class=\"spacer\">"+SPACE+"</span>");
     }

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
   * Calculate pages for city details
   */
  private void calcPages(List<String> cities) {
    String cityfile = "", fileStr = "";
    int cpt = 0;
    for (Iterator it = cities.iterator(); it.hasNext();) {
      String city = (String)it.next();
      cpt++;
      cityfile = section.sectionPrefix + String.format(section.formatNbrs, (cpt/section.nbPerPage)+1) + section.sectionSuffix;
      if (fileStr.compareTo(cityfile) != 0) {
         fileStr = cityfile;
         }
      cityPage.put(wh.htmlAnchorText(city), cityfile);
      }
    }

  /**
   * Provide links map to outside caller
   */
  public Map getPagesMap() {
     return cityPage;
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
