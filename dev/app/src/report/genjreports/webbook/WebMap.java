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

import genj.util.swing.Action2;

//FIXME: dependance croisee
//import genj.geo.GeoLocation;
//import genj.geo.GeoService;
//import genj.geo.Country;
//import genj.geo.GeoServiceException;

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
import java.io.FileReader;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import java.util.Comparator;

import java.text.DecimalFormat;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebMap {

//FIXME: dependance croisee
//  private ReportWebBook report = null;
//  private Gedcom gedcom = null;
//  private Indi indiDeCujus = null;
//  private WebHelper wh = null;
//  private WebSection section = null;
//
//  private WebSection sectionList = null;
//
//  private final static Charset UTF8 = Charset.forName("UTF-8");
//  private final static String SPACE = "&nbsp;";
//  private final static String SEP = "/";
//
//  private String themeDir = "";
//  private Map<String, String> cityPage = new TreeMap<String, String>();
//  private WebCitiesDetails reportCity = null;
//  private String here2cityDir = "";
//
//  private String  mapKey             = "";
//
//  /**
//   * Constructor
//   */
//  public WebMap(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
//     this.report = report;
//     this.gedcom = gedcom;
//     this.indiDeCujus = indiDeCujus;
//     this.section = section;
//     wh = report.getHelper();
//     }
//
//  /**
//   * Section's entry point
//   */
//  public void run(Indi indi, WebCitiesDetails reportCity, WebSection webSectionCity) {
//
//    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);
//
//    themeDir = wh.buildLinkTheme(section, report.getThemeDir());
//
//    this.reportCity = reportCity;
//    if (reportCity != null) {
//       cityPage = reportCity.getPagesMap();
//       here2cityDir = wh.buildLinkShort(section, webSectionCity);
//       }
//
//    mapKey = report.getValueFromUser("mapkey", report.translate("mapKey"), new String[]{"abcdefg"});
//    if (mapKey==null)
//      return;
//
//    exportPage(dir);
//
//    exportXMLData(dir, indi);
//
//    exportIcons(dir);
//  }
//
//  /**
//   * Exports page
//   */
//  private void exportPage(File dir) {
//
//    // Opens page
//    String fileStr = section.sectionPrefix + String.format(section.formatNbrs, 1) + section.sectionSuffix;
//    File file = wh.getFileForName(dir, fileStr);
//    PrintWriter out = wh.getWriter(file);
//    if (out == null) return;
//
//    wh.printOpenHTMLHead(out, "Map", section);
//
//    // include style element to ensure vertical sizing of maps in conjunction with body height
//    out.println("<?xml-stylesheet href=\"#internalStyle\" type=\"text/css\"?>");
//    out.println("<style type=\"text/css\" id=\"internalStyle\">");
//    out.println("  html { height: 100%; overflow: hidden; }");
//    out.println("</style>");
//    out.println("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key="+mapKey+"\" type=\"text/javascript\"></script>");
//
//    // include javascript
//    try {
//       out.println(readFile(wh.getGenjImagesDir()+File.separator+"map.js"));
//       } catch (IOException e)
//       {
//         report.println(e.toString());
//       }
//
//    // include body declaration and title
//    out.println("</head>");
//    out.println("<body onload=\"initialize()\" onresize=\"resizeApp()\" onunload=\"GUnload()\" style=\"height:77%;\" >");
//    out.println("<h1><a name=\"top\">"+SPACE+"</a>"+wh.htmlText(report.translate("map"))+"</h1>");
//    wh.printHomeLink(out, section);
//
//    // Include page itself
//    out.println("<div id=\"map\" class=\"map\"></div>");
//    out.println("<div class=\"mapctrl\">");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_ancestors"))+"</span>&nbsp;&nbsp;<input id=\"anca\" name=\"ancestor\" type=\"radio\" value=\"all\" onclick=\"boxclick()\" checked=\"checked\" />"+wh.htmlText(report.translate("map_all")));
//    out.println("&nbsp;&nbsp;<input id=\"ancs\" name=\"ancestor\" type=\"radio\" value=\"sosa\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_ascendants")));
//    out.println("&nbsp;&nbsp;<input id=\"ancc\" name=\"ancestor\" type=\"radio\" value=\"cousins\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_cousins")));
//    out.println("&nbsp;&nbsp;<input id=\"anco\" name=\"ancestor\" type=\"radio\" value=\"others\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_others")));
//    out.println("</p>");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_events"))+"</span>&nbsp;&nbsp;<input id=\"evea\" name=\"event\" type=\"radio\" value=\"all\" onclick=\"boxclick()\"  checked=\"checked\" />"+wh.htmlText(report.translate("map_all")));
//    out.println("&nbsp;&nbsp;<input id=\"even\" name=\"event\" type=\"radio\" value=\"births\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_birth")));
//    out.println("&nbsp;&nbsp;<input id=\"evem\" name=\"event\" type=\"radio\" value=\"marriages\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_marriages")));
//    out.println("&nbsp;&nbsp;<input id=\"eved\" name=\"event\" type=\"radio\" value=\"deaths\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_deaths")));
//    out.println("</p>");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_years"))+"</span>&nbsp;<input id=\"min\" name=\"min\" type=\"text\" size=\"4\" value=\"0\" onchange=\"boxclick()\" style=\"text-align: center\" />");
//    out.println("&nbsp;"+wh.htmlText(report.translate("map_to"))+"&nbsp;<input id=\"max\" name=\"max\" type=\"text\" size=\"4\" value=\"2100\" onchange=\"boxclick()\" style=\"text-align: center\" />");
//    out.println("</p>");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_volume"))+"</span>&nbsp;&nbsp;<input id=\"vola\" name=\"volume\" type=\"radio\" value=\"all\" onclick=\"boxclick()\"  checked=\"checked\" />"+wh.htmlText(report.translate("map_all")));
//    out.println("&nbsp;&nbsp;<input id=\"volh\" name=\"volume\" type=\"radio\" value=\"high\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_high")));
//    out.println("&nbsp;&nbsp;<input id=\"volm\" name=\"volume\" type=\"radio\" value=\"medium\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_medium")));
//    out.println("&nbsp;&nbsp;<input id=\"voll\" name=\"volume\" type=\"radio\" value=\"low\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_low")));
//    out.println("</p>");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_density"))+"</span>&nbsp;&nbsp;<input id=\"den1\" name=\"density\" type=\"radio\" value=\"dense\" onclick=\"boxclick()\"  checked=\"checked\" />"+wh.htmlText(report.translate("map_high")));
//    out.println("&nbsp;&nbsp;<input id=\"den2\" name=\"density\" type=\"radio\" value=\"spread\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_medium")));
//    out.println("&nbsp;&nbsp;<input id=\"den3\" name=\"density\" type=\"radio\" value=\"scarce\" onclick=\"boxclick()\" />"+wh.htmlText(report.translate("map_low")));
//    out.println("</p>");
//    out.println("<p class=\"mapctrlbox\">");
//    out.println("<span class=\"gras\">"+wh.htmlText(report.translate("map_markersize"))+"</span>&nbsp;&nbsp;");
//    out.println("<input type=\"button\" onclick=\"sub()\" style=\"font-weight: bold; height:15px; vertical-align: middle; background: url('../theme/p.gif')\"  />");
//    out.println("<input type=\"text\" value=\"32\" size=\"3\" id=\"markersize\" name=\"markersize\" onchange=\"chg();\" style=\"text-align: center\" />");
//    out.println("<input type=\"button\" onclick=\"add()\" style=\"font-weight: bold; height:15px; vertical-align: middle; background: url('../theme/n.gif')\"  />");
//    out.println("</p>");
//    out.println("</div>");
//    out.println("</body>");
//    out.println("</html>");
//
//    report.println(fileStr+" - Done.");
//    out.close();
//
//  }
//
//  /**
//   * Read input file and put into string
//   */
//  private String readFile(String filename) throws IOException {
//    String text0 = wh.readFile(filename);
//    String text  = text0.replaceAll("detailed_events", report.translate("map_detailed_events"));
//    return text;
//    }
//
//  /**
//   * Exports icons
//   */
//  private void exportIcons(File dir) {
//     String genjImagesDir = wh.getGenjImagesDir();
//     try {
//       wh.copy(genjImagesDir+File.separator+"cbd.png", dir.getAbsolutePath()+File.separator+"cbd.png");
//       wh.copy(genjImagesDir+File.separator+"cbmd.png", dir.getAbsolutePath()+File.separator+"cbmd.png");
//       wh.copy(genjImagesDir+File.separator+"cbm.png", dir.getAbsolutePath()+File.separator+"cbm.png");
//       wh.copy(genjImagesDir+File.separator+"cx.png", dir.getAbsolutePath()+File.separator+"cx.png");
//       wh.copy(genjImagesDir+File.separator+"cb.png", dir.getAbsolutePath()+File.separator+"cb.png");
//       wh.copy(genjImagesDir+File.separator+"cd.png", dir.getAbsolutePath()+File.separator+"cd.png");
//       wh.copy(genjImagesDir+File.separator+"cmd.png", dir.getAbsolutePath()+File.separator+"cmd.png");
//       wh.copy(genjImagesDir+File.separator+"cm.png", dir.getAbsolutePath()+File.separator+"cm.png");
//       wh.copy(genjImagesDir+File.separator+"obd.png", dir.getAbsolutePath()+File.separator+"obd.png");
//       wh.copy(genjImagesDir+File.separator+"obmd.png", dir.getAbsolutePath()+File.separator+"obmd.png");
//       wh.copy(genjImagesDir+File.separator+"obm.png", dir.getAbsolutePath()+File.separator+"obm.png");
//       wh.copy(genjImagesDir+File.separator+"ob.png", dir.getAbsolutePath()+File.separator+"ob.png");
//       wh.copy(genjImagesDir+File.separator+"od.png", dir.getAbsolutePath()+File.separator+"od.png");
//       wh.copy(genjImagesDir+File.separator+"omd.png", dir.getAbsolutePath()+File.separator+"omd.png");
//       wh.copy(genjImagesDir+File.separator+"om.png", dir.getAbsolutePath()+File.separator+"om.png");
//       wh.copy(genjImagesDir+File.separator+"ox.png", dir.getAbsolutePath()+File.separator+"ox.png");
//       wh.copy(genjImagesDir+File.separator+"sbd.png", dir.getAbsolutePath()+File.separator+"sbd.png");
//       wh.copy(genjImagesDir+File.separator+"sbmd.png", dir.getAbsolutePath()+File.separator+"sbmd.png");
//       wh.copy(genjImagesDir+File.separator+"sbm.png", dir.getAbsolutePath()+File.separator+"sbm.png");
//       wh.copy(genjImagesDir+File.separator+"sb.png", dir.getAbsolutePath()+File.separator+"sb.png");
//       wh.copy(genjImagesDir+File.separator+"sd.png", dir.getAbsolutePath()+File.separator+"sd.png");
//       wh.copy(genjImagesDir+File.separator+"smd.png", dir.getAbsolutePath()+File.separator+"smd.png");
//       wh.copy(genjImagesDir+File.separator+"sm.png", dir.getAbsolutePath()+File.separator+"sm.png");
//       wh.copy(genjImagesDir+File.separator+"sx.png", dir.getAbsolutePath()+File.separator+"sx.png");
//       wh.copy(genjImagesDir+File.separator+"z.png", dir.getAbsolutePath()+File.separator+"z.png");
//       } catch (IOException e)
//       {
//          //e.printStackTrace();
//          report.println(e.toString());
//       }
//     }
//
//  /**
//   * Exports XML data
//   *
//   */
//  private class CityFlash {
//     // key
//     String fullName = "";
//     // city info unique to city
//     String city = "";
//     String country = "";
//     String linkToPage = "";
//     String linkAnchor = "";
//     double lng = 0;
//     double lat = 0;
//     int density = 0;
//     // ancestors related to events
//     Set<Indi> ascendants = new HashSet();
//     Set<Indi> cousins = new HashSet();
//     Set<Indi> others = new HashSet();
//     Map<String, Integer> names = new TreeMap<String, Integer>();
//     // events information
//     int nbBirths = 0;
//     int nbMarriages = 0;
//     int nbDeaths = 0;
//     int nbOther = 0;
//     PropertyDate minDate = null;
//     PropertyDate maxDate = null;
//     // derived
//     String size = "";
//     String ancestor = "";
//     String type = "";
//     String text = "";
//     int min = 0;
//     int max = 0;
//     }
//
//  private Map<String, CityFlash> citiesFlash = new TreeMap<String, CityFlash>();               // key is fullName
//  private Map<Integer, GeoLocation> prop2location = new TreeMap<Integer, GeoLocation>();       // key is hascode of property parent of PLAC or ADDR
//
//  private Collection locations = null;
//  private Set ancestors = null;
//  private Set cousins = null;
//
//  /**
//   *  Main export function for the data
//   */
//  private void exportXMLData(File dir, Indi indi) {
//
//    // Opens page
//    String fileStr = section.sectionPrefix + ".xml";
//    File file = wh.getFileForName(dir, fileStr);
//    PrintWriter out = wh.getWriter(file);
//    if (out == null) return;
//
//    // Get geolocations
//    locations = getGeoLocations();
//    buildProp2Location();
//    report.println("Number of geolocations: "+locations.size());
//
//    // Get ancestor of sosa #1 of webbook
//    ancestors = wh.getAncestors(indi);
//    report.println("Number of ascendants: "+ancestors.size());
//
//    // Get cousins of sosa #1 of webbook
//    cousins = wh.getCousins(indi);
//    report.println("Number of cousins: "+cousins.size());
//
//    // Calculate data
//    calculateCitiesFlash(indi);
//
//    // Export data
//    exportCitiesFlash(out);
//
//    // Closes page
//    report.println(fileStr+" - Done.");
//    out.close();
//
//  }
//
//
//  /**
//   *  Get geoLocations
//   */
//  private Collection getGeoLocations() {
//
//    Collection locs = null;
//
//    // find locations
//    List props = new ArrayList();
//    for (Iterator it = wh.getCities(gedcom).iterator(); it.hasNext();) {
//       String city = (String)it.next();
//       List<Property> listProps = wh.getCitiesProps(city);
//       for (Iterator p = listProps.iterator(); p.hasNext(); ) {
//          Property prop = (Property)p.next();
//          if ((prop == null) || (prop.getValue().length() == 0)) continue;
//          if (prop instanceof PropertyPlace) {
//             props.add(prop.getParent());
//             }
//          else {
//             props.add(prop.getParent().getParent());
//             }
//          }
//      }
//    locs = (Collection)GeoLocation.parseProperties(props);
//
//    // match locations
//    try {
//      locs = GeoService.getInstance().match(gedcom, locs, true);
//       } catch (GeoServiceException e) {
//         report.println(e.getMessage());
//       }
//    if (locs.isEmpty()) {
//       report.println(report.translate("map_error"));
//       }
//    return locs;
//    }
//
//  /**
//   *  Build map of property to locations for matching later
//   */
//  private void buildProp2Location() {
//     prop2location.clear();
//     for (Iterator it = locations.iterator(); it.hasNext();) {
//        GeoLocation location = (GeoLocation)it.next();
//        for (int i = 0 ; i < location.getNumProperties(); i++) {
//           prop2location.put(Integer.valueOf(location.getProperty(i).hashCode()), location);
//           }
//        }
//     }
//
//
//  /**
//   *  Does all the calculations
//   */
//  private void calculateCitiesFlash(Indi indi) {
//
//    // Creates the citiesFlash records
//    for (Iterator it = wh.getCities(gedcom).iterator(); it.hasNext();) {
//       String city = (String)it.next();
//       List<Property> listProps = wh.getCitiesProps(city);
//       String lastFullname = "";
//       boolean first = true;
//       for (Iterator p = listProps.iterator(); p.hasNext(); ) {
//          Property prop = (Property)p.next();
//          if ((prop == null) || (prop.getValue().length() == 0)) continue;
//          String fullname = reportCity.getFullname(prop);
//          CityFlash cityFlash = citiesFlash.get(fullname);
//          if (cityFlash == null) {
//             cityFlash = createCityFlashRecord(fullname, prop);
//             }
//          addDetails2cityFlashRecord(cityFlash, prop);
//          citiesFlash.put(fullname, cityFlash);
//          }
//       }
//
//    // Calculates max volumes for later
//    int maxVolume = 0;
//    for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext(); ) {
//      String city = (String)it.next();
//      CityFlash cityFlash = citiesFlash.get(city);
//      if (cityFlash != null) {
//         Integer total = (Integer) (cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther);
//         if (total > maxVolume) {
//            maxVolume = total;
//            }
//         }
//      }
//    // Calculates derived measures
//    for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext(); ) {
//      String city = (String)it.next();
//      CityFlash cityFlash = citiesFlash.get(city);
//      if (cityFlash != null) {
//         calculateMeasures(cityFlash, maxVolume);
//         }
//      }
//
//    // Calculates density of points
//    calculateDensity();
//
//    }
//
//  /**
//   *  Creates cityFlash record
//   */
//  private CityFlash createCityFlashRecord(String fullname, Property prop) {
//     CityFlash cityFlash = new CityFlash();
//     cityFlash.fullName = fullname;
//     cityFlash.city = reportCity.getCity(prop);
//     cityFlash.country = reportCity.getCountry(prop);
//     cityFlash.linkToPage = (String)(reportCity.getPagesMap().get(wh.htmlAnchorText(cityFlash.city)));
//     cityFlash.linkAnchor = wh.htmlAnchorText(cityFlash.fullName);
//     GeoLocation location = findLocation(prop);
//     cityFlash.lng = location == null || !location.isValid() ? -45 : location.getCoordinate().x;
//     cityFlash.lat = location == null || !location.isValid() ? +30: location.getCoordinate().y;
//     return cityFlash;
//     }
//
//  /**
//   *  Look for geoLocations from property
//   */
//  private GeoLocation findLocation(Property prop) {
//     return (prop instanceof PropertyPlace) ? prop2location.get(prop.getParent().hashCode()) : prop2location.get(prop.getParent().getParent().hashCode());
//     }
//
//  /**
//   *  Addd details of event to cityFlash Record
//   */
//  private void addDetails2cityFlashRecord(CityFlash cityFlash, Property prop) {
//
//     // Get individuals related to this event
//     Entity ent = prop.getEntity();
//     if (ent instanceof Indi) {
//        addIndividual(cityFlash, (Indi)ent);
//        }
//     if (ent instanceof Fam) {
//        addIndividual(cityFlash, ((Fam)ent).getHusband());
//        addIndividual(cityFlash, ((Fam)ent).getWife());
//        }
//
//     // Get event type
//     Property parent = prop.getParent();
//     Property event = null;
//     if (parent != null) {
//        if (prop instanceof PropertyPlace) {
//           event = parent;
//           }
//        else {
//           event = parent.getParent();
//           }
//        }
//     if (event != null) {
//        addEvent(cityFlash, event.getTag());
//        }
//
//     // Get date of event
//     addDate(cityFlash, reportCity.getDate(prop));
//
//     return;
//     }
//
//  private void addIndividual(CityFlash cityFlash, Indi indi) {
//     if (ancestors.contains(indi)) {
//        cityFlash.ascendants.add(indi);
//        }
//     else if (cousins.contains(indi)) {
//        cityFlash.cousins.add(indi);
//        }
//     else {
//        cityFlash.others.add(indi);
//        }
//     if (wh.isPrivate(indi)) {
//        return;
//        }
//     Integer counter = cityFlash.names.get(wh.getLastName(indi));
//     if (counter == null) {
//        counter = 0;
//        }
//     counter++;
//     cityFlash.names.put(wh.getLastName(indi), counter);
//     return;
//     }
//
//  private void addEvent(CityFlash cityFlash, String tag) {
//     if (tag.compareTo("BIRT") == 0) {
//        cityFlash.nbBirths++;
//        }
//     else if (tag.compareTo("MARR") == 0) {
//        cityFlash.nbMarriages++;
//        }
//     else if (tag.compareTo("DEAT") == 0) {
//        cityFlash.nbDeaths++;
//        }
//     else {
//        cityFlash.nbOther++;
//        }
//     return;
//     }
//
//  private void addDate(CityFlash cityFlash, PropertyDate pDate) {
//     if (pDate == null) {
//        return;
//        }
//     if (cityFlash.minDate == null) {
//        cityFlash.minDate = pDate;
//        }
//     if (cityFlash.maxDate == null) {
//        cityFlash.maxDate = pDate;
//        }
//     if (pDate.compareTo(cityFlash.minDate) < 0) {
//        cityFlash.minDate = pDate;
//        }
//     if (pDate.compareTo(cityFlash.maxDate) > 0) {
//        cityFlash.maxDate = pDate;
//        }
//     return;
//     }
//
//  /**
//   *  Calculates measures
//   */
//  private void calculateMeasures(CityFlash cityFlash, int maxVolume) {
//     // Size
//     int total = cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther;
//     cityFlash.size = (total > (maxVolume / 3)) ? "h" : (total > (maxVolume / 9)) ? "m" : "l";
//
//     // Ancestor
//     if (cityFlash.ascendants.size() > 0) {
//        cityFlash.ancestor = "s";
//        }
//     else if (cityFlash.cousins.size() > 0) {
//        cityFlash.ancestor = "c";
//        }
//     else {
//        cityFlash.ancestor = "o";
//        }
//     String lastnames = getLastNames(cityFlash.names);
//
//     // type
//     cityFlash.type = "";
//     if (cityFlash.nbBirths > 0) {
//        cityFlash.type += "b";
//        }
//     if (cityFlash.nbMarriages > 0) {
//        cityFlash.type += "m";
//        }
//     if (cityFlash.nbDeaths > 0) {
//        cityFlash.type += "d";
//        }
//     if (cityFlash.type.length() == 0) {
//        cityFlash.type = "x";
//        }
//
//     // min and max year
//     try {
//        if (cityFlash.minDate != null) {
//           cityFlash.min = (int)cityFlash.minDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
//           }
//        if (cityFlash.maxDate != null) {
//           if (cityFlash.maxDate.isRange()) {
//              cityFlash.max = (int)cityFlash.maxDate.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear();
//              }
//           else {
//              cityFlash.max = (int)cityFlash.maxDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
//              }
//           }
//        } catch (GedcomException e) {
//        report.println(e.toString());
//        };
//
//     // text
//     String text = "";
//     text += report.translate("map_box_city") + " " + cityFlash.city + ", " + cityFlash.country + " (" + getCoordinateAsString(cityFlash.lng, cityFlash.lat) + ").;";
//
//     text += Integer.toString(cityFlash.ascendants.size() + cityFlash.cousins.size() + cityFlash.others.size()) + " " + report.translate("map_box_individual") + ": ";
//     int cpt = 0;
//     if (cityFlash.ascendants.size() > 0) text += Integer.toString(cityFlash.ascendants.size()) + " " + report.translate("map_box_ascendants");
//     cpt += cityFlash.ascendants.size();
//     if (cityFlash.cousins.size() > 0) text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.cousins.size()) + " " + report.translate("map_box_cousins");
//     cpt += cityFlash.cousins.size();
//     if (cityFlash.others.size() > 0) text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.others.size()) + " " + report.translate("map_box_others");
//     text += ".;";
//
//     if (lastnames.trim().length() != 0) text += report.translate("map_box_most") + ": " + lastnames + ".;";
//
//     text += Integer.toString(cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther) + " " + report.translate("map_box_events") + ": ";
//     cpt = 0;
//     if (cityFlash.nbBirths > 0) text += Integer.toString(cityFlash.nbBirths) + " " + report.translate("map_box_births");
//     cpt += cityFlash.nbBirths;
//     if (cityFlash.nbMarriages > 0) text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbMarriages) + " " + report.translate("map_box_marriages");
//     cpt += cityFlash.nbMarriages;
//     if (cityFlash.nbDeaths > 0) text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbDeaths) + " " + report.translate("map_box_deaths");
//     cpt += cityFlash.nbDeaths;
//     if (cityFlash.nbOther > 0) text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbOther) + " " + report.translate("map_box_others");
//     text += ".;";
//
//     if (cityFlash.minDate != null && cityFlash.minDate.compareTo(cityFlash.maxDate) == 0) {
//        text += report.translate("map_box_occon") + " \"" + cityFlash.minDate + "\".";
//        }
//     else if (cityFlash.minDate != null && cityFlash.maxDate != null) {
//        text += report.translate("map_box_occbet") + " \"" + cityFlash.minDate + "\" "+report.translate("map_box_occand")+" \"" + cityFlash.maxDate + "\".";
//        }
//     cityFlash.text = text;
//     return;
//     }
//
//  /**
//   *  Get top 5 most frequent lastnames and take privacy into account
//   */
//  private String getLastNames(Map<String, Integer> map) {
//     String output = "";
//     for (int i = 0; i< 5; i++) {
//        Integer max = 0;
//        String maxname = "";
//        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
//           String name = (String)it.next();
//           Integer counter = (Integer)map.get(name);
//           if (counter > max) {
//              max = counter;
//              maxname = name;
//              }
//           }
//        if (maxname.length() != 0) {
//           output += (i == 0 ? "" : ", ") + maxname;
//           map.put(maxname, 0);
//           }
//       }
//     return output;
//     }
//
//  /**
//   *  Get coordinates as string
//   */
//  public String getCoordinateAsString(double lon, double lat) {
//    if (Double.isNaN(lat)||Double.isNaN(lon))
//      return "n/a";
//    char we = 'E', ns = 'N';
//    if (lat<0) { lat = -lat; ns='S'; }
//    if (lon<0) { lon = -lon; we='W'; }
//    DecimalFormat format = new DecimalFormat("0.0");
//    return ns + format.format(lat) + " " + we + format.format(lon);
//  }
//
//
//  /**
//   *  Does the export to XML
//   */
//  private void exportCitiesFlash(PrintWriter out) {
//     out.println("<ls>");
//
//     for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext(); ) {
//       String city = (String)it.next();
//       CityFlash cityFlash = citiesFlash.get(city);
//       if (cityFlash != null && (report.getDisplayUnkCities() || cityFlash.lng != -45 || cityFlash.lat != 30) ) {
//          String line = "";
//          line += "<l ";
//          line += "x=\""+cityFlash.lng+"\" ";
//          line += "y=\""+cityFlash.lat+"\" ";
//          line += "s=\""+cityFlash.size+"\" ";
//          line += "a=\""+cityFlash.ancestor+"\" ";
//          line += "t=\""+cityFlash.type+"\" ";
//          line += "d=\""+cityFlash.density+"\" ";
//          line += "min=\""+cityFlash.min+"\" ";
//          line += "max=\""+cityFlash.max+"\" ";
//          line += "lkp=\""+cityFlash.linkToPage+"\" ";
//          line += "lki=\""+cityFlash.linkAnchor+"\" ";
//          line += "lko=\""+wh.htmlAnchorText(cityFlash.city)+"\" ";
//          line += "cty=\""+cityFlash.city+"\" ";
//          line += ">";
//          out.println(line);
//          out.println(cityFlash.text);
//          out.println("</l>");
//          }
//       }
//     out.println("</ls>");
//     }
//
//
//  /**
//   *  Calculates measures
//   */
//  private void calculateDensity() {
//    // Build city[i]
//    String[] city = new String[citiesFlash.keySet().size()];
//    citiesFlash.keySet().toArray(city);
//
//    // Build distance[i][j] between city[i] and city[j] and get average
//    int totalDistance = 0;
//    int counter = 0;
//    double[][] distance = new double[city.length][city.length];
//    for (int i = 0; i < city.length; i++) {
//       if (city[i].compareTo("") == 0) { continue; }
//       CityFlash cityFlashi = citiesFlash.get(city[i]);
//       if (cityFlashi == null) { city[i] = ""; }
//       for (int j = 0; j < i; j++) {
//          if (city[j].compareTo("") == 0) { continue; }
//          CityFlash cityFlashj = citiesFlash.get(city[j]);
//          if (cityFlashj == null) { city[j] = ""; }
//          distance[i][j] = Math.pow(cityFlashi.lng - cityFlashj.lng, 2) + Math.pow(cityFlashi.lat - cityFlashj.lat, 2);
//          totalDistance += distance[i][j];
//          counter++;
//          }
//       }
//    double averageDistance = (counter == 0) ? 0 : totalDistance/counter;
//
//    // For all distances lower than average distance, remove smallest volume city (city[i] = "")
//   checkDistances(Math.sqrt(averageDistance)/60, "1", city, distance);
//   checkDistances(Math.sqrt(averageDistance)/30, "2", city, distance);
//
//    // Allocate resulting density
//    int i = 0;
//    for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext(); ) {
//       String name = (String)it.next();
//       CityFlash cityFlash = citiesFlash.get(name);
//       if (city[i].compareTo("1") == 0) {
//          cityFlash.density = 1;
//          }
//       else if (city[i].compareTo("2") == 0) {
//          cityFlash.density = 2;
//          }
//       else {
//          cityFlash.density = 3;
//          }
//       i++;
//       }
//
//    }
//
//  /**
//   *  Check distances
//   */
//  private void checkDistances(double threshold, String value, String[] city, double[][] distance) {
//    for (int i = 0; i < city.length; i++) {
//       if (city[i].length() == 1) { continue; }
//       for (int j = 0; j < i; j++) {
//          if (city[j].length() == 1) { continue; }
//          if (distance[i][j] < threshold) {
//             city[getBiggerCity(i, j, city[i], city[j])] = value;
//             continue;
//             }
//          }
//       }
//     }
//
//  /**
//   *  Get bigger city
//   */
//  private int getBiggerCity(int i, int j, String cityI, String cityJ) {
//     CityFlash cityFlashi = citiesFlash.get(cityI);
//     CityFlash cityFlashj = citiesFlash.get(cityJ);
//     if (cityFlashi == null) { return i; }
//     if (cityFlashj == null) { return j; }
//     if (cityFlashi.ascendants.size() > cityFlashj.ascendants.size()) { return j; }
//     else if (cityFlashi.ascendants.size() < cityFlashj.ascendants.size()) { return i; }
//     if (cityFlashi.cousins.size() > cityFlashj.cousins.size()) { return j; }
//     else if (cityFlashi.cousins.size() < cityFlashj.cousins.size()) { return i; }
//     int totali = cityFlashi.nbBirths + cityFlashi.nbMarriages + cityFlashi.nbDeaths + cityFlashi.nbOther;
//     int totalj = cityFlashj.nbBirths + cityFlashj.nbMarriages + cityFlashj.nbDeaths + cityFlashj.nbOther;
//     if (totali > totalj) { return j; } else { return i; }
//     }

} // End_of_Report
