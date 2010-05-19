/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjreports.webbook;

import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Source;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySource;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;

import java.io.*;

import java.nio.charset.Charset;
import java.nio.CharBuffer;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.sun.image.codec.jpeg.*;





/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebHelper {

  /**
   * Variables
   */
  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private final static String DEFCHAR = "-";

  private ReportWebBook report = null;
  private String language = "en";
  private String css = "";
  private String index = "";
  private String siteDesc = "";
  private String author = "";
  private String keywords = "";

  public final static String SOSA_TAG = "_SOSA";

  private MediaTracker mediaTracker = new MediaTracker(new Container());

  private class Info {
     Integer counter = 0;
     List    props   = null;
     }

  private SortedMap<String, Integer> listOfLastnames = null;
  private boolean initLastname = false;
  private SortedMap<String, Info> listOfCities = null;
  private boolean initCity = false;
  private SortedMap<String, Info> listOfDays = null;
  private boolean initDay = false;
  private List<Ancestor> listOfAncestors = new ArrayList<Ancestor>();
  private boolean initAncestors = false;
  private Set<Indi> listOfCousins = new HashSet<Indi>();
  private boolean initCousins = false;

  private FTPRegister uploadRegister = null;


  /**
   * This is the default stylesheet for the generated report sheets 
   */
  public static final String[] defaultStylesheet = {
"@charset \"iso-8859-1\";",
"",
"/*",
" Used for all pages", 
"*/",
"body {background: white; font-family: Arial,Helvetica,Verdana,sans-serif; font-size: 90%; color: black; text-align: left; }",
"a { color: blue; text-decoration: none; }",
"a:hover { color: blue; text-decoration: underline; }",
"p {margin-left: 1em; }",
"h1 {font-size:180%; text-align: center; margin-top: 30px; }",
"h2.hom {font-size: 150%; color: #0000ff;}",
"h2.fem {font-size: 150%; color: #ff78d0;}",
"h2.unk {font-size: 150%; color: #000000;}",
"h3 {font-size:120%; text-align: center; font-weight: bold; }",
"h4 {font-size: 100%; font-weight: bold;}",
"img { border-style: none; }",
".cent { text-align: center;}",
".gras {font-weight: bold;}",
".undl {text-decoration: underline; }",
".grasplus {font-weight: bold; background: #f5fc7d; }",
".marge{margin-left: 3em;}",
"",
"/*",
" Used for menu", 
"*/",
".contindex { ",
"  width: 98%; margin-left: 0; margin-top: 0; margin-bottom: 0; padding: 0px;",
"}",
".menu {",
"  margin-left: 0; margin-right: 1em; margin-top: 10px; margin-bottom: 0; float: left; width: 25%; text-align:left; font-weight: bold; color:#555;",
"}",
".menu p {",
"  margin-left: 15px; margin-right: 1em; margin-top: 0; margin-bottom: 0; padding: 0px;", 
"}",
".menu ul {",
"  list-style: disc url(u.gif) inside; padding-bottom: 3px; margin: 0; font-weight: normal; border-bottom: none;",
"}",
".intro {",
"  margin: 1em; float: left; width: 70%; text-align:left;",
"}",
".legal {",
"  font-size: 80%; margin: 1em; text-align: center;",
"}",
".footer { /* footer */",
" margin-left: 80%; text-align: center; margin-top: 0; margin-bottom: 0; padding: 0px; font-size: 0%;",
" }",
"",
"/*",
" Used for lastname page", 
"*/",
".letters {font-size: 140%; text-align: center; }",
".nameblock { display: block; margin-bottom: 1em; text-align: center; }",
".char { display: block; color: #0000A0; font-weight: bold; font-size: 140%; text-align: center; }",
".name { font-style: italic; }",
".occu { color: #3299cc; }",
"",
"/*",
" Used for list of individuals page", 
"*/",
".letter {font-size: 140%; text-align: left; font-weight: bold; color: #0000A0; }",
"",
"",
"/*",
" Used for individual details page", 
"*/",
".conteneur { /* conteneur des paragraphes précédents ce qui permet de les mettre cote à cote*/",
"  width: 98%; margin-left: 2em; margin-top: 0; margin-bottom: 0; padding: 0px;", 
"}",
".spacer { /*égalise la taille des deux colonnes du conteneur*/",
"  clear: both; margin-top: 0; margin-bottom: 0; height: 0px",
"}",
".decal {",
"  margin-left: 0; font-size: 120%;color: #1aa02d; margin-top: 0; margin-bottom: 1em; text-decoration: underline;", 
"}",
".image {",
"  text-align: right; margin-right: 5em; margin-top: 0; margin-bottom: 0;  margin-left: 0;", 
"}",
".parentm {    /* used for information about main person */",
"  float: left; width: 55%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin;",  
"}",
".parentgp {   /* used for information about grand parents */",
"  float: left; width: 24%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-width: thin; padding: 0px;", 
"}",
".parentp {    /* used for information about parents */",
"  float: left; width: 48%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-width: thin; padding: 1px;", 
"}",
".parents {   /* used for information about siblings */",
"  margin-left: 25%; text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0;",
"}",
".parentf {   /* used for information about family */",
"  margin-left: 25%; text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0;",
"}",
".parentfc {   /* used for information about children */",
"  margin-left: 28%; text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0;",
"}",
".rela1 {    /* used for information about main person */",
"  float: left; width: 100%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin;",  
"}",
".rela2 {   /* used for information about relation */",
"  float: left; margin-left: 5em; text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0;",
"}",
".parentgpl1 {   /* used for lines under grand parents */",
"  float: left; width: 12%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border: thin; height: 5px",
"}",
".parentgpl2 {   /* used for lines under grand parents */",
"  float: left; width: 12%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-left: groove thin; border-bottom: groove thin; height: 5px",
"}",
".parentgpl3 {   /* used for lines under grand parents */",
"  float: left; width: 12%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-right: groove thin; border-bottom: groove thin; height: 5px",
"}",
".parentgpl4 {   /* used for lines under grand parents */",
"  float: left; width: 12%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-right: groove thin; border-top: groove thin; height: 5px",
"}",
".parentgpl5 {   /* used for lines under grand parents */",
"  float: left; width: 12%; text-align:center; margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;",
"  border-left: groove thin; border-top: groove thin; height: 5px",
"}",
".note {    /* used for notes paragraph */",
"  float: left; width: 90%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin;",  
"}",
".ital {  /* used for notes */",
"  font-style: italic;font-size: 80%;", 
"}",
"/*",
" Used for sources", 
"*/",
".srclist {",
"  margin-left: 1em; float: left; width: 8%;  font-size: 120%;color: #1aa02d; margin-top: 3px; margin-bottom: 0;", 
"}",
".srclisttitle {",
"  margin-left: 0%; float: left; width: 88%;  font-size: 100%;margin-top: 5px; margin-bottom: 0;", 
"}",
".srcdecal {",
"  margin-left: 0; font-size: 120%;color: #1aa02d; margin-top: 0; margin-bottom: 1em; text-decoration: underline;", 
"}",
".srcitems {",
"  float: left; width: 98%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 1em;", 
"  border-width: thin; ",  
"}",
".srcitems0 {",
"  float: left; width: 80%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 18%;", 
"  border-width: thin; ",  
"}",
".srcitems1 {",
"  float: left; width: 18%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin; font-weight: bold; ",  
"}",
".srcitems2 {",
"  float: left; width: 82%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin;     ",  
"}",
".srcitems3 {",
"  float: left; width: 82%;  text-align:left; margin-top: 0; margin-bottom: 0; margin-right: 0; margin-left: 0;", 
"  border-width: thin; font-style: italic;",  
"}",
".srcimage {",
"  float: left; width: 82%;  text-align: left; margin-right: 0; margin-top: 0; margin-bottom: 1em;  margin-left: 0;", 
"  border-width: thin; vertical-align: bottom;            ", 
"}",
".srcimage0 {",
"  float: left; width: 80%;  text-align: left; margin-right: 0; margin-top: 0; margin-bottom: 1em;  margin-left: 18%;", 
"  border-width: thin; vertical-align: bottom;            ", 
"}",
".srcimage1 {",
"  float: left; width: 200px;  text-align: center; margin-right: 0; margin-top: 0; margin-bottom: 0;  margin-left: 0;", 
"  border-width: thin; border-style: outset; ", 
"}",
".srcimage2 {",
"  float: left; text-align: center; margin-right: 0; margin-top: 0; margin-bottom: 0;  margin-left: 0;", 
"  border-width: thin; ", 
"}",
"/*",
" Used for media", 
"*/",
".medlist {",
"  margin-left: 1em; float: left; width: 8%;  font-size: 120%;color: #1aa02d; margin-top: 3px; margin-bottom: 0;", 
"}",
".medlisttitle {",
"  margin-left: 0%; float: left; width: 88%;  font-size: 100%;margin-top: 5px; margin-bottom: 0;", 
"}",
".maintable {",
"  margin-left: auto; margin-right: auto;", 
"}",
".thumbnail-table {",
"  text-align: center; vertical-align: bottom; width: 100%; ",
"}",
".thumbnail-row {",
"  vertical-align: bottom; ", 
"}",
".thumbnail-col {",
"  text-align:center; height:150px; width:300px; ", 
"}",
"/*",
" Used for cities",
"*/",
".citycont {",
"  width: 99%; margin-left: 1em; margin-top: 0; margin-bottom: 1em; padding: 0px; ",
"}",
".citycont1 {",
"  float: left; width: 15%; margin: 0; padding: 0px;  ",
"}",
".citycont2 {",
"  float: left; width: 84%; margin: 0; padding: 0px;  ",
"}",
".cityloc {",
"  font-size: 120%; color: #1aa02d; text-decoration: underline; margin: 0; margin-bottom: 5px; ",
"}",
".cityloc1 {",
"  float: left; width: 90%; text-align:left; margin: 0; border-width: thin; ",
"}",
".cityevt {",
"  font-size: 120%; color: #1aa02d; text-decoration: underline; margin: 0; margin-bottom: 5px; ",
"}",
".cityevt1 {",
"  float: left; width: 16%;  text-align:left; margin: 0;  ",
"}",
".cityevt2 {",
"  float: left; width: 16%;  text-align:left; margin: 0;  ",
"}",
".cityevt3 {",
"  float: left; width: 67%;  text-align:left; margin: 0;  ",
"}",
"/*",
" Used for days",
"*/",
".daycal {",
"  float: left; width: 99%; margin: 0; margin-top: 1em; padding: 0px;   ",
"}",
".daycal1 {",
"  float: left; width: 33%; margin: 0; padding: 0px;  ",
"}",
".daychar { ",
"  display: block; margin: 0; color: #0000A0; font-weight: bold; font-size: 140%; text-align: center; ",
"}",
".dayblock { ",
"  display: block; margin: 0; border: solid thin red;",
"}",
".daytbl { ",
"   margin: 0; margin-left: auto; margin-right: auto; text-align: left; ",
"}",
".daytbl td { ",
"   padding: 0; width: 46px;",
"}",
".dayo { ",
"   font-style: italic; color: #3299cc;", 
"}",
".daycont {",
"  width: 99%; margin: 0; margin-left: 1em; padding: 0px;   ",
"}",
".dayhd1 {",
" float: left; width: 15%;  font-size: 120%; color: #1aa02d; text-decoration: underline; font-weight: bold; margin: 0; margin-bottom: 5px; ",
"}",
".dayhd2 {",
"  float: left; width: 15%;  font-size: 120%; color: #1aa02d; text-decoration: underline; font-weight: bold; margin: 0; margin-bottom: 5px;",
"}",
".dayhd3 {",
"  float: left; width: 69%;  font-size: 120%; color: #1aa02d; text-decoration: underline; font-weight: bold; margin: 0; margin-bottom: 5px;",
"}",
".dayevt1 {",
"  float: left; width: 15%;  text-align:left; margin: 0; ",
"}",
".dayevt2 {",
"  float: left; width: 15%;  text-align:left; margin: 0; ",
"}",
".dayevt3 {",
"  float: left; width: 69%;  text-align:left; margin: 0;  ",
"}",
"/*",
" Used for search", 
"*/",
".searchdecal {",
"  text-align: center; font-weight: bold; font-size: 120%; ", 
"}",
".searchtable {",
"  margin-left: auto; margin-right: auto;", 
"}",
".searchheader {",
"  font-weight: bold; ", 
"}",
"div.tableContainer {",
"   clear: both; height: 600px; width: 100%; overflow: auto;",
"}",
"html>body div.tableContainer {",
"   overflow: hidden; width: 100%;",
"}",
"div.tableContainer table {",
"   float: left; width: 100%;",
"}",
"html>body div.tableContainer table {",
"   width: 100%;",
"}",
"thead.fixedHeader tr {",
"   position: relative",
"}",
"thead.fixedHeader th {",
"   background: #a0a0FF; border-left: none; border-right: 1px solid #CCC; border-top: 1px solid #DDD; font-weight: bold;",
"}",
"html>body tbody.scrollContent {",
"   height: 588px; overflow: auto; width: 100%",
"}",
"tbody.scrollContent td, tbody.scrollContent tr.normalRow td {",
"   background: #FFF; border-bottom: none; border-left: none; border-right: 1px solid #CCC; border-top: 1px solid #DDD; ",
"}",
"tbody.scrollContent tr.alternateRow td {",
"   background: #EEE; border-bottom: none; border-left: none; border-right: 1px solid #CCC; border-top: 1px solid #DDD; ",
"}",
"html>body thead.fixedHeader th {",
"   text-align: center; padding: 4px 3px 4px 3px;", 
"}",
"html>body tbody.scrollContent td {",
"   text-align: center; padding: 2px 3px 2px 4px;",
"}",
"html>body tbody.scrollContent td + td {",
"   text-align: left; padding: 2px 3px 2px 4px;",
"}",
"html>body tbody.scrollContent td + td + td {",
"   text-align: left; padding: 2px 3px 2px 4px;",
"}",
"html>body tbody.scrollContent td + td + td + td {",
"   text-align: left; padding: 2px 3px 2px 4px;",
"}",
"html>body tbody.scrollContent td + td + td + td + td {",
"   text-align: center; padding: 2px 3px 2px 4px;",
"}",
"html>body tbody.scrollContent td + td + td + td + td + td {",
"   text-align: center; padding: 2px 20px 2px 4px;", 
"}",
"/*",
" Used for reports",
"*/",
".contreport {", 
"  width: 96%; margin-left: 2em; margin-top: 0; margin-bottom: 0; padding: 0px;", 
"}",
".description {",
"   text-align: left; margin-left: 3em; margin-right: 3em; margin-top: 0;", 
"}",
".column1 {",
"   float: left; text-align: left; margin-left: 3em; margin-right: 0; margin-top: 0;", 
"}",
".column1f {",
"   float: left; width: 30%; text-align: left; margin-left: 3em; margin-right: 0; margin-top: 0;", 
"}",
".column2f {",
"   float: left; width: 10%; text-align: right; margin-left: 0; margin-right: 0; margin-top: 0;", 
"}",
".column2 {",
"   float: right; text-align: left; width: 45%; margin-left: 0; margin-right: 3em; margin-top: 0;", 
"}",
".contreport th {",
"   background: #a0a0FF; border: solid thin #CCC; font-weight: bold; text-align: center; width: 60px;", 
"}",
".contreport td {",
"   border: solid thin #CCC; text-align: right; padding-right: 15px;",
"}",
".contreport2 {", 
"  width: 96%; margin-left: 2em; margin-top: 0; margin-bottom: 0; padding: 0px;", 
"}",
".contreport2 th {",
"   background: #a0a0FF; border: solid thin #CCC; font-weight: bold; text-align: center; ", 
"}",
".contreport2 th + th + th + th {",
"   border: solid thin #CCC; border-top: none; border-bottom: none; font-weight: bold; text-align: center; background: #FFF;",
"}",
".contreport2 th + th + th + th + th {",
"   background: #a0a0FF; border: solid thin #CCC; font-weight: bold; text-align: center;", 
"}",
".contreport2 th + th + th + th + th + th + th {",
"   border: solid thin #CCC; border-top: none; border-bottom: none; font-weight: bold; text-align: center; background: #FFF;",
"}",
".contreport2 th + th + th + th + th + th + th + th {",
"   background: #a0a0FF; border: solid thin #CCC; font-weight: bold; text-align: center;", 
"}",
".contreport2 td {",
"   border: solid thin #CCC; text-align: right; padding-left: 15px; padding-right: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td {",
"   border: solid thin #CCC; text-align: left; padding-left: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td {",
"   border: solid thin #CCC; text-align: right; padding-right: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td {",
"   border: solid thin #CCC; border-top: none; border-bottom: none; text-align: left; padding-left: 0px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td + td {",
"   border: solid thin #CCC; text-align: left; padding-left: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td + td + td  {",
"   border: solid thin #CCC; text-align: right; padding-right: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td + td + td + td {",
"   border: solid thin #CCC; border-top: none; border-bottom: none; text-align: left; padding-left: 0px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td + td + td + td + td {",
"   border: solid thin #CCC; text-align: left; padding-left: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".contreport2 td + td + td + td + td + td + td + td + td {",
"   border: solid thin #CCC; text-align: right; padding-right: 15px; padding-top: 0px; padding-bottom: 0px;",
"}",
".sosareport {", 
"  width: 99%; margin: 0; margin-bottom: 1em; padding: 5px; ",
"}",
".sosacolumn1 {",
"   float: left; width: 25%; margin-left: 1em; margin-right: 0; margin-top: 0;",
"}",
".sosacolumn2 {",
"   float: left; width: 73%; margin: 0; margin-bottom: 1em;",
"}",
".sosacolumn2 ul {",
"   margin: 0; padding: 0; padding-left: 15px; ",
"}",
".sosasources {",
"   margin: 0; padding: 0; margin-left: 1em; ",
"}",
".sosatext {", 
"  width: 90%; display: block; font-style: italic; margin: 0; margin-left: 3em; color: #6060ff",
"}",
".map {",
"  width: 95%; height: 100%; margin-right: auto; margin-left: auto; margin-top: 1em; border: thin solid blue;",
"}",
".mapctrl {",
"  width: 95%; margin-right: auto; margin-left: auto; margin-top:5px;",
"}",
".mapctrlbox {",
"  float: left; height: 20px; margin:4px; margin-left: 0; margin-right: 3em; background: #ddd;  border: thin inset black; padding:0; padding-left:5px; padding-right:5px",
"}",
""



  };

  /**
   * Constructor
   */
  public WebHelper(ReportWebBook report, String css, String index, String siteDesc, String author, String language) {

     this.report = report;
     this.css = css;
     this.index = index;
     this.siteDesc = siteDesc;
     this.author = author;
     this.language = language;
     }


  /**
   * Returns nb of individuals
   */
  public int getNbIndis(Gedcom gedcom) {
    return gedcom.getEntities(Gedcom.INDI, "INDI:NAME").length;
  }
  

  /**
   * Helper - Writes HTML table cell information
   */
  public void printCell(PrintWriter out, Object content) {

  
    // We ask a property for it's value instead of just toString()
    if (content instanceof Property) 
      content = ((Property)content).toString();
      
    // We don't want to see 'null' but ''
    if (content == null || content.toString().length() == 0)
      content = SPACE;

    // Here comes the HTML
    out.println("<td>"+content.toString()+"</td>");

  }
  


  /**
   * Helper - Calculate a url for individual's id 
   */
  public String wrapID(Indi indi) {
    StringBuffer result = new StringBuffer();
    result.append("<a name=\"");
    result.append(getLastName(indi));
    result.append("\"/>");

    result.append("<a href=\"");
    result.append(getFileForEntity(null, indi).getName());
    result.append("\">");
    result.append(indi.getId());
    result.append("</a>");
    return result.toString();
  }
  

  /**
   * Helper that resolves a filename for given entity
   */
  public File getFileForEntity(File dir, Entity entity) {
    return new File(dir, entity.getId()+".html");
  }
  


  /**
   * Helper - Create a PrintWriter wrapper for output stream
   */
  public PrintWriter getWriter(File file) {
    
    PrintWriter pw = null;

    try {
      pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), UTF8));

      // Update register
      if (uploadRegister != null) uploadRegister.update(file);

    } catch (IOException e) {
      //e.printStackTrace();
      report.println("Error in WebHelper-getWriter: "+e.getMessage());
      report.println(e.toString());
      System.exit(1);
    }
    return pw;
  }

  /**
   * Helper - Create icons
   */
  public void createIcons(File dir) {
    
     // Get male and female icons in the dierctory
     String genjImagesDir = getGenjImagesDir();

     try {
       copy(genjImagesDir+File.separator+"m.gif", dir.getAbsolutePath()+File.separator+"m.gif");
       copy(genjImagesDir+File.separator+"f.gif", dir.getAbsolutePath()+File.separator+"f.gif");
       copy(genjImagesDir+File.separator+"u.gif", dir.getAbsolutePath()+File.separator+"u.gif");
       copy(genjImagesDir+File.separator+"s.gif", dir.getAbsolutePath()+File.separator+"s.gif");
       copy(genjImagesDir+File.separator+"p.gif", dir.getAbsolutePath()+File.separator+"p.gif");
       copy(genjImagesDir+File.separator+"t.gif", dir.getAbsolutePath()+File.separator+"t.gif");
       copy(genjImagesDir+File.separator+"h.gif", dir.getAbsolutePath()+File.separator+"h.gif");
       copy(genjImagesDir+File.separator+"b.gif", dir.getAbsolutePath()+File.separator+"b.gif");
       copy(genjImagesDir+File.separator+"n.gif", dir.getAbsolutePath()+File.separator+"n.gif");
       copy(genjImagesDir+File.separator+"e.gif", dir.getAbsolutePath()+File.separator+"e.gif");
       copy(genjImagesDir+File.separator+"src.gif", dir.getAbsolutePath()+File.separator+"src.gif");
       copy(genjImagesDir+File.separator+"downbar.png", dir.getAbsolutePath()+File.separator+"downbar.png");
       copy(genjImagesDir+File.separator+"downleft.png", dir.getAbsolutePath()+File.separator+"downleft.png");
       copy(genjImagesDir+File.separator+"downright.png", dir.getAbsolutePath()+File.separator+"downright.png");
       copy(genjImagesDir+File.separator+"leftbar.png", dir.getAbsolutePath()+File.separator+"leftbar.png");
       copy(genjImagesDir+File.separator+"rightbar.png", dir.getAbsolutePath()+File.separator+"rightbar.png");
       copy(genjImagesDir+File.separator+"upbar.png", dir.getAbsolutePath()+File.separator+"upbar.png");
       copy(genjImagesDir+File.separator+"upleft.png", dir.getAbsolutePath()+File.separator+"upleft.png");
       copy(genjImagesDir+File.separator+"upright.png", dir.getAbsolutePath()+File.separator+"upright.png");
       copy(genjImagesDir+File.separator+"medno.png", dir.getAbsolutePath()+File.separator+"medno.png");
       copy(genjImagesDir+File.separator+"mednopic.png", dir.getAbsolutePath()+File.separator+"mednopic.png");
       copy(genjImagesDir+File.separator+"medpriv.png", dir.getAbsolutePath()+File.separator+"medpriv.png");
       copy(genjImagesDir+File.separator+"map.gif", dir.getAbsolutePath()+File.separator+"map.gif");
       copy(genjImagesDir+File.separator+"mail.gif", dir.getAbsolutePath()+File.separator+"mail.gif");
       } catch (IOException e) 
       { 
          //e.printStackTrace(); 
          report.println(e.toString());
       } 
   }

  /**
   * Helper - Writes HTML header and body information
   */
  public void printOpenHTML(PrintWriter out) {
     printOpenHTML(out, null, null);
     }

  public void printOpenHTML(PrintWriter out, String title) {
     printOpenHTML(out, title, null);
     }

  public void printOpenHTML(PrintWriter out, String title, WebSection section) {
     printOpenHTMLHead(out, title, section);
     printOpenHTMLBody(out, title, section);
     }

  public void printOpenHTMLHead(PrintWriter out, String title, WebSection section) {

    // HEAD
    String htmlTitle = htmlText(report.getTitle());
    if (title != null && title.length() != 0) htmlTitle += SPACE + "-" + SPACE + htmlText(report.translate(title));
    String path = (section == null || section.sectionDir == null) ? "" : ((section.sectionDir.length() == 0) ? "" : "..");

    out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
    out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\""+language+"\" lang=\""+language+"\" >");
    out.println("<head>");
    out.println("<title>"+htmlTitle+"</title>");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />");
    out.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
    out.println("<meta name=\"description\" content=\""+htmlTitle+" "+siteDesc+"\" />");
    out.println("<meta name=\"keywords\" content=\""+"fredwebbook "+keywords+"\" />");
    out.println("<meta http-equiv=\"Content-language\" content=\""+language+"\" />");
    out.println("<meta name=\"author\" content=\""+author+"\" />");
    out.println("<meta name=\"generator\" content=\"genj\" />");
    out.println("<meta name=\"robots\" content=\"all\" />");
    out.println("<meta name=\"reply-to\" content=\"\" />");
    out.println("<meta name=\"owner\" content=\""+language+"\" />");
    if (css.length()>0) {
      String parent = (path == null) || (path.length() == 0) ? "" : path + SEP;
      out.println("<link rel=\"StyleSheet\" href=\""+parent+css+"\" type=\"text/css\"/>");
      }
    }

  public void printOpenHTMLBody(PrintWriter out, String title, WebSection section) {
    // Close HEAD
    out.println("</head>");
    // BODY
    out.println("<body>");
    if (title != null) {
       String titlePage = "";
       if (title.length() == 0) titlePage = report.getTitle();
       else titlePage = report.translate(title);
       out.println("<h1>"+"<a name=\"top\">"+SPACE+"</a>"+htmlText(titlePage)+"</h1>");
       }
    // done
  }


  /**
   * Helper - Writes HTML end header and end body information
   */
  public void printLinks(PrintWriter out, String here, WebSection wsFrom) {
    printLinks(out, here, "", "", "", "", wsFrom);
  }

  public void printLinks(PrintWriter out, String here, String s, String p, String n, String e, WebSection wsFrom) {

    String themeDir = (wsFrom == null) ? (report.getThemeDir() + SEP) : buildLinkTheme(wsFrom, report.getThemeDir());
    String home = ((wsFrom == null) ? index : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + index);

    out.println("<p class=\"footer\">");
    out.println("<br /><br />");
    out.println("<a href=\""+here+"#top\"><img src=\""+themeDir+"t.gif\" alt=\""+report.translate("alt_top")+"\" title=\""+report.translate("alt_top")+"\" /></a><br />");
    if (s != null && s.length() > 0) {
       out.println("<a href=\""+s+"\"><img src=\""+themeDir+"s.gif\" alt=\""+report.translate("alt_start")+"\" title=\""+report.translate("alt_start")+"\" /></a>");
       }
    if (p != null && p.length() > 0) {
       out.println("<a href=\""+p+"\"><img src=\""+themeDir+"p.gif\" alt=\""+report.translate("alt_previous")+"\" title=\""+report.translate("alt_previous")+"\" /></a>");
       }
    out.println("<a href=\""+home+"#top\"><img src=\""+themeDir+"h.gif\" alt=\""+report.translate("alt_home")+"\" title=\""+report.translate("alt_home")+"\" /></a>");
    if (n != null && n.length() > 0) {
       out.println("<a href=\""+n+"\"><img src=\""+themeDir+"n.gif\" alt=\""+report.translate("alt_next")+"\" title=\""+report.translate("alt_next")+"\" /></a>");
       }
    if (e != null && e.length() > 0) {
       out.println("<a href=\""+e+"\"><img src=\""+themeDir+"e.gif\" alt=\""+report.translate("alt_end")+"\" title=\""+report.translate("alt_end")+"\"  /></a>");
       }
    out.println("<br /><a href=\""+here+"#bot\"><img src=\""+themeDir+"b.gif\" alt=\""+report.translate("alt_bottom")+"\" title=\""+report.translate("alt_bottom")+"\" /></a>");
    out.println("</p>");
    //out.println("<hr />");

  }

  public void printHomeLink(PrintWriter out, WebSection wsFrom) {

    String themeDir = (wsFrom == null) ? (report.getThemeDir() + SEP) : buildLinkTheme(wsFrom, report.getThemeDir());
    String home = ((wsFrom == null) ? index : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + index);

    out.println("<p class=\"footer\">");
    out.println("<br /><br />");
    out.println("<a href=\""+home+"#top\" ><img src=\""+themeDir+"h.gif\" alt=\""+report.translate("alt_home")+"\" title=\""+report.translate("alt_home")+"\"/></a>");
    out.println("</p>");
  }

  public String getHomeLink(WebSection wsFrom) {

    String themeDir = (wsFrom == null) ? (report.getThemeDir() + SEP) : buildLinkTheme(wsFrom, report.getThemeDir());
    String home = ((wsFrom == null) ? index : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + index);
    return home+"#top";
  }

  /**
   * Helper - Writes HTML end header and end body information
   */
  public void printCloseHTML(PrintWriter out) {

    // Close page
    out.println("<p>"+"<a name=\"bot\"></a>"+SPACE+"</p>");
    out.println("</body>");
    out.println("</html>");
  }


  /**
   * Uploade register for all other modules
   */
  public void setUploadRegister(FTPRegister uploadRegister) {
    this.uploadRegister = uploadRegister;
  }

  public FTPRegister getUploadRegister() {
    return uploadRegister;
  }



  /**
   * Generates Spaces
   */
  public void exportSpaces(PrintWriter out, int num) {
    for (int c=0;c<num;c++) {
      out.print(SPACE);
    }
  }


  /**
   * Generates file name
   */
  public File getFileForName(File dir, String fileName) {
    return new File(dir, fileName);
  }

  /**
   * Get directory where images are located for male and female icons.
   */
  public String getGenjImagesDir() {
    String fullname = (new File("report"+File.separator+"webbook")).getAbsolutePath();
    return fullname;
    }

  /**
   * Clean file name from drive letters (windows), colon ":", starting file separators and spaces
   */
  public String getCleanFileName(String input) {

    // Eliminate drive letter by starting after ":"
    String str = input.substring(Math.max(0,input.lastIndexOf(":")+1));

    // Eliminate back-slashes in case of windows or any other case
    while (str.startsWith("\\")) {  
       str = str.substring(1);
       }

    // Eliminate slashes
    while (str.startsWith("/")) {
       str = str.substring(1);
       }

    // Eliminate blank spaces
    String temp = str.replaceAll("\\s","_");

    // Eliminate accents
    String cleanName = fileNameConvert(temp);

    return cleanName;
    }

  /**
   * Get title of a media
   */
  public String getTitle(PropertyFile media) {
    Property ptitle = media.getParent().getProperty("TITL");
    if (ptitle != null && ptitle.toString().length() != 0) return ptitle.toString();
    File file = media.getFile();
    if (file != null) {
       String filename = file.getName();
       return getCleanFileName(filename);
       }
    String str = media.toString();
    return str.substring(str.lastIndexOf(File.separator)+1);
  }



  /**
   * The static method that actually performs the file copy.
   * Before copying the file, however, it performs a lot of tests to make
   * sure everything is as it should be.
   * Filenames need to be aboslute path
   */
  public void copy(String from_name, String to_name) throws IOException{
     copy(from_name, to_name, false, true);
     }

  public void copy(String from_name, String to_name, boolean linkOnly) throws IOException{
     copy(from_name, to_name, linkOnly, true);
     }

  public void copy(String from_name, String to_name, boolean linkOnly, boolean force) throws IOException{

    File from_file = new File(from_name);  // Get File objects from Strings
    File to_file = new File(to_name);

    // if file exists and force is false, exit as there is no need to recreate an existing file (optimisation of performance for user)
    if (to_file.exists() && !force) {
       if (uploadRegister != null) uploadRegister.update(to_file);
       return;
       }

    // First make sure the source file exists, is a file, and is readable.
    if (!from_file.exists())
      abort("FileCopy: no such source file: " + from_name);
    if (!from_file.isFile())
      abort("FileCopy: can't copy directory: " + from_name);
    if (!from_file.canRead())
      abort("FileCopy: source file is unreadable: " + from_name);

    // If the destination is a directory, use the source file name as the destination file name
    if (to_file.isDirectory())
      to_file = new File(to_file, from_file.getName());
 
    // If the destination exists, make sure it is a writeable file and ask before overwriting it.  
    // If the destination doesn't exist, make sure the directory exists and is writeable.
    if (to_file.exists()) {
      if (!to_file.canWrite())
        abort("FileCopy: destination file is unwriteable: " + to_name);
    }
    else {  
      // if file doesn't exist, check if directory exists and is writeable.
      // If getParent() returns null, then the directory is the current dir.
      // so look up the user.dir system property to find out what that is.
      String parent = to_file.getParent();  // Get the destination directory
      if (parent == null) parent = System.getProperty("user.dir"); // or CWD
      File dir = new File(parent);          // Convert it to a file.
      if (!dir.exists()) {
        createDir(to_name, false);
        // abort("FileCopy: destination directory doesn't exist: " + parent);
        }
      if (dir.isFile()) {
        abort("FileCopy: destination is not a directory: " + parent);
        }
      if (!dir.canWrite()) {
        abort("FileCopy: destination directory is unwriteable: " + parent);
        }
    }

    // If we've gotten this far, then everything is okay.
    // Only use link for linux, force copy for other systems.
    String OS_NAME = System.getProperty("os.name");
    if (OS_NAME == null) OS_NAME = "";
    boolean IS_OS2 = OS_NAME.startsWith("OS/2");
    boolean IS_MAC = OS_NAME.startsWith("Mac");
    boolean IS_OSX = OS_NAME.startsWith("mac os x");
    boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
    boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS && !IS_MAC;

    if (!IS_UNIX) linkOnly =false;
    // Copy symbolic link if that is the option....
    try {
       if (linkOnly) { // will only work for linux
          String[] command = { "ln", "-s", from_name, to_name };
          Runtime.getRuntime().exec(command);
          }
       else {
          // ...Otherwise copy the file, a buffer of bytes at a time.
          //String[] command = { "cp", from_name, to_name }; (cp for linux, copy for dos
          //Runtime.getRuntime().exec(command);
          FileInputStream from = null;  // Stream to read from source
          FileOutputStream to = null;   // Stream to write to destination
          try {
            from = new FileInputStream(from_file);  // Create input stream
            to = new FileOutputStream(to_file);     // Create output stream
            byte[] buffer = new byte[4096];         // A buffer to hold file contents
            int bytes_read;                         // How many bytes in buffer
            // Read a chunk of bytes into the buffer, then write them out, 
            // looping until we reach the end of the file (when read() returns -1).
            // Note the combination of assignment and comparison in this while
            // loop.  This is a common I/O programming idiom.
            while((bytes_read = from.read(buffer)) != -1) { // Read bytes until EOF
              to.write(buffer, 0, bytes_read);             //   write bytes
              } 
          }
          // Always close the streams, even if exceptions were thrown
          finally {
            if (from != null) try { from.close(); } catch (IOException e) { 
                //e.printStackTrace(); 
                report.println(e.toString()); 
                }
            if (to != null) try { to.close(); } catch (IOException e) { 
                //e.printStackTrace(); 
                report.println(e.toString()); 
                }
          }
          }
     } catch (Exception e) { 
       //e.printStackTrace(); 
       report.println(e.toString());
     }

    // Update register
    if (uploadRegister != null) uploadRegister.update(to_file);


  }

  /** A convenience method to throw an exception */
  private void abort(String msg) throws IOException { 
    report.println(msg.toString()); throw new IOException(msg); 
  }


  /** Detect image */
  public boolean isImage(String infile) {
    if (infile == null) return false;
    return (infile.toLowerCase().endsWith(".jpg") || infile.toLowerCase().endsWith(".png") || infile.toLowerCase().endsWith(".gif"));
    } 


  /** Scale image */
  public boolean scaleImage(String infile, String outfile, int width, int height, int quality) { 
     return scaleImage(infile, outfile, width, height, quality, true);  
     }

  public boolean scaleImage(String infile, String outfile, int width, int height, int quality, boolean force) { 

    // if file exists and force is false, exit as there is no need to recreate an existing file (optimisation of performance for user)
    File out_file = new File(outfile);
    if (out_file.exists() && !force) {
       if (uploadRegister != null) uploadRegister.update(out_file);
       return true;
       }

    boolean result = false;
    Image image = Toolkit.getDefaultToolkit().getImage(infile);
    mediaTracker.addImage(image, 0);

    try {
       mediaTracker.waitForID(0);
    } catch (Exception e) { 
       //e.printStackTrace(); 
       report.println(e.toString()); 
    }

    // determine thumbnail size from WIDTH and HEIGHT
    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);
    if ((imageWidth <= 0) || (imageHeight <= 0)) {
       mediaTracker.removeImage(image);
       image.flush();
       return false;   // a non picture file will have size <=0 (sound for instance can be -1)
       }
    double imageRatio = (double)imageWidth / (double)imageHeight;
    int thumbWidth = width;
    int thumbHeight = height;
    if (width == 0) {
      thumbWidth = (int)(thumbHeight * imageRatio);
      }
    if (height == 0) {
      thumbHeight = (int)(thumbWidth / imageRatio);
      }
    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
    if (thumbRatio < imageRatio) {
      thumbHeight = (int)(thumbWidth / imageRatio);
    } else {
      thumbWidth = (int)(thumbHeight * imageRatio);
    }

    BufferedOutputStream out = null;
    try {
       // draw original image to thumbnail image object and
       // scale it to the new size on-the-fly
       BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
       Graphics2D graphics2D = thumbImage.createGraphics();
       graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

       createDir(outfile, false);
       
       // save thumbnail image to OUTFILE
       out = new BufferedOutputStream(new FileOutputStream(outfile));
       JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
       JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
       int qual = Math.max(0, Math.min(quality, 100));
       param.setQuality((float)qual / 100.0f, false);
       encoder.setJPEGEncodeParam(param);
       encoder.encode(thumbImage);
       out.close(); 
       result = true;
     } catch (Exception e) { 
       // e.printStackTrace(); 
       report.println(e.toString()); 
     } finally {
       if (out != null) try { out.close(); } catch (IOException e) { ; }
     }

    // Update register
    if (uploadRegister != null) uploadRegister.update(out_file);

   // Flush resources and exit
   mediaTracker.removeImage(image);
   image.flush();
   return result;
   }


  /** 
    * Create Dir  
    **/
  public File createDir(String outfile, boolean create) { 

        String parent = (new File(outfile)).getAbsoluteFile().getParent();
        if (parent == null || parent.length() == 0) return null;
        File fp = new File(parent);
        if (!fp.exists()) {
          String[] dirs = parent.split("["+File.separator+File.separator+"]");
          String absoluteDir = dirs[0]+File.separator;
          for (int i = 1; i < dirs.length; i++) {
             String dir = dirs[i];
             absoluteDir += dir+File.separator;
             //System.err.println("AD= " + absoluteDir); 
             File ad = new File(absoluteDir);
             if (!ad.exists()) {
                //System.err.println("does not exist" + dir); 
                ad.mkdir(); 
                }
             }
          }
        File f = new File(outfile);
        if (create) {
           f.mkdir();
           }
        return (f);
     }

  /** 
    * Empty Dir  
    **/
  public boolean emptyDir(File dir, boolean removeDir) { 
     if (dir.isDirectory()) {
         String[] children = dir.list();
         for (int i=0; i<children.length; i++) {
             boolean success = emptyDir(new File(dir, children[i]), true);
             if (!success) {
                 return false;
             }
         }
     }
     // The directory is now empty so delete it
     if (removeDir)
        return dir.delete();
     return true;
     }


  /** 
   * Read a file into a string 
   **/
  public String readFile(String filename) throws IOException {

    if ((filename == null) || (filename.length() == 0)) return "";
    File f;
    FileInputStream in = null;
    StringBuffer sb = new StringBuffer("");

    try {
      f = new File(filename);
      in = new FileInputStream(f);
      int size = (int) f.length();
      byte[] data = new byte[size];
      int chars_read = 0;
      while(chars_read < size)
        chars_read += in.read(data, chars_read, size - chars_read);
      sb.append(new String(data, "UTF8"));
    }
    catch (IOException e) {
       report.println(e.toString());
    }
    finally { try { if (in != null) in.close(); } catch (IOException e) {} }

    return sb.toString();
    }

  /** 
   * Writes a file from a string 
   **/
  public boolean writeFile(String filename, String text) throws IOException {
    if ((filename == null) || (filename.length() == 0)) return false;
    try {
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(text);
        out.close();
        } catch (IOException e) {
          report.println(e.toString());
          return false;
        }
    return true;
    }

   /** 
    * Build link from one section to another 
    **/
   public String buildLink(WebSection wsFrom, WebSection wsTo, int nbItem) {
      String relPath = "";
      if ((wsFrom.sectionDir.length() == 0) && (wsTo.sectionDir.length() == 0)) {
         relPath = "";
         }
      if ((wsFrom.sectionDir.length() == 0) && (wsTo.sectionDir.length() != 0)) {
         relPath = wsTo.sectionDir + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (wsTo.sectionDir.length() == 0)) {
         relPath = ".." + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (wsTo.sectionDir.length() != 0)) {
         relPath = ".." + SEP + wsTo.sectionDir + SEP;
         }
      return relPath + wsTo.sectionPrefix + String.format(wsTo.formatNbrs, (nbItem/wsTo.nbPerPage)+1) + wsTo.sectionSuffix;
      }

   public String buildLinkTheme(WebSection wsFrom, String themeDir) {
      String relPath = "";
      if ((wsFrom.sectionDir.length() == 0) && (themeDir.length() == 0)) {
         relPath = "";
         }
      if ((wsFrom.sectionDir.length() == 0) && (themeDir.length() != 0)) {
         relPath = themeDir + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (themeDir.length() == 0)) {
         relPath = ".." + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (themeDir.length() != 0)) {
         relPath = ".." + SEP + themeDir+ SEP;
         }
      return relPath;
      }

   public String buildLinkShort(WebSection wsFrom, WebSection wsTo) {
      String relPath = "";
      if ((wsFrom.sectionDir.length() == 0) && (wsTo.sectionDir.length() == 0)) {
         relPath = "";
         }
      if ((wsFrom.sectionDir.length() == 0) && (wsTo.sectionDir.length() != 0)) {
         relPath = wsTo.sectionDir + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (wsTo.sectionDir.length() == 0)) {
         relPath = ".." + SEP;
         }
      if ((wsFrom.sectionDir.length() != 0) && (wsTo.sectionDir.length() != 0)) {
         relPath = ".." + SEP + wsTo.sectionDir + SEP;
         }
      return relPath;
      }

   /**
    * Get sosa if available
    */
   public String getSosa(Indi indi) {
     if (indi == null) 
        return "";
     Property prop = indi.getProperty(SOSA_TAG);
     if (prop == null)
       return "";
     String str = prop.getDisplayValue();
     while (str.startsWith("0")) str = str.substring(1);  
     return str;
   }

  /**
   * Return sorted list of lastnames of Gedcom file
   * Do NOT use getLastNames() function of genj because it returns all lastnames found and we only want one last name per person here
   * (Genj itself only accesses individuals using the first lastname found for them) 
   */
  public List<String> getLastNames(Gedcom gedcom) {
     if (!initLastname) {
        initLastname = buildLastnamesList(gedcom);
        }
     return (List)new ArrayList((Collection)listOfLastnames.keySet());
     }

  public int getLastNameCount(String lastname) {
     String str = lastname;
     if (str == null) {
        str = DEFCHAR;
        }
     if (listOfLastnames.get(str) == null) {
        return -1; 
        }
     return (int)listOfLastnames.get(str);
     }

  private boolean buildLastnamesList(Gedcom gedcom) {
     listOfLastnames = new TreeMap<String, Integer>(sortLastnames);
     List indis = new ArrayList(gedcom.getEntities(Gedcom.INDI));
     for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       String str = getLastName(indi);
       Integer counter = listOfLastnames.get(str);
       if (counter == null) {
          counter = 1;
          }
       else {
          counter++;
          }
       listOfLastnames.put(str, counter);
       }
     return true;
     }

  public String getLastName(Indi indi) {
     if (indi == null) return DEFCHAR;
     String str = indi.getLastName();
     return (str == null ? DEFCHAR : str.length() == 0 ? DEFCHAR : str);
     }


 /**
  * Comparator to sort Lastnames
  */
  public Comparator sortLastnames = new Comparator() {
     public int compare(Object o1, Object o2) {
        String orig1 = (String)o1;
        String orig2 = (String)o2;
        String name1 = htmlAnchorText((String)o1);
        String name2 = htmlAnchorText((String)o2);
        if (name1.startsWith(DEFCHAR)) {  
           if (name2.startsWith(DEFCHAR)) {
              return orig1.compareTo(orig2);
              }
           return +1;   // "-" will be sorted after [A-Z] 
           } 
        if (name2.startsWith(DEFCHAR)) {  
           return -1;   // "-" will be sorted after [A-Z] 
           } 
        if (name2.compareTo(name1) == 0) {
           return orig1.compareTo(orig2);   // if name 1 = name2, there might be a difference in accent 
           }
        return name1.compareTo(name2);
        }
     };

  /**
   * Return sorted list of individuals (Indi) of Gedcom file
   * Lastnames are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only) 
   */
  public List getIndividuals(Gedcom gedcom) {
     List indis = new ArrayList(gedcom.getEntities(Gedcom.INDI));
     Collections.sort(indis, sortIndividuals);
     return indis; 
     }

 /**
  * Comparator to sort Individuals
  */
  private Comparator sortIndividuals = new Comparator() {
     public int compare(Object o1, Object o2) {
        Indi indi1 = (Indi)o1;
        Indi indi2 = (Indi)o2;
        int sort = sortLastnames.compare(getLastName(indi1), getLastName(indi2)); 
        if (sort == 0) {
           return sortLastnames.compare(indi1.getFirstName(), indi2.getFirstName());
           }
        else {
           return sort;
           }
        }
     };

  /**
   * Return sorted list of sources of Gedcom file
   * Sources are sorted by codes 
   */
  public List getSources(Gedcom gedcom) {
     List sources = new ArrayList(gedcom.getEntities(Gedcom.SOUR));
     Collections.sort(sources, sortSources);
     return sources; 
     }

  /**
   * Return sorted list of sources of entity
   * Sources are sorted by codes 
   */
  public List getSources(Indi indi) {
     // get sources of individual
     List sources = new ArrayList();
     getPropertiesRecursively((Property)indi, sources, "SOUR");

     // get sources of the associated families
     Fam[] families = indi.getFamiliesWhereSpouse();
     for (int i=0 ; families != null && i<families.length; i++) {
        Fam family = families[i];
        getPropertiesRecursively((Property)family, sources, "SOUR");
        }

     List sourcesOutput = new ArrayList();
     for (Iterator s = sources.iterator(); s.hasNext(); ) {
        Property propSrc = (Property)s.next(); 
        if (propSrc instanceof PropertySource) {
           PropertySource pSource = (PropertySource)propSrc;
           if (pSource != null && pSource.getTargetEntity() != null) {
              Source src = (Source)pSource.getTargetEntity();
              if (!sourcesOutput.contains(src)) {
                 sourcesOutput.add(src);
                 }
              }
           }
        }

     Collections.sort(sourcesOutput, sortSources);
     return sourcesOutput; 
     }

  /**
   * Extract the first number bit in the string going from left to right
   */
  public int extractNumber(String str) {

     int start = 0, end = 0;
     while (start<=end&&!Character.isDigit(str.charAt(start))) start++;
     end = start;
     while ((end<=str.length()-1)&&Character.isDigit(str.charAt(end))) end++;
     if (end == start) return 0;
     else return (int)Integer.parseInt(str.substring(start, end));
     }

 /**
  * Comparator to sort Individuals
  */
  public Comparator sortSources = new Comparator() {
     public int compare(Object o1, Object o2) {
        Source src1 = (Source)o1;
        Source src2 = (Source)o2;
        return (extractNumber(src1.getId()) - extractNumber(src2.getId()));
        }
     };

  /**
   * Return sorted list of cities of Gedcom file
   * Cities are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only) 
   */
  public List<String> getCities(Gedcom gedcom) {
     if (!initCity) {
        initCity = buildCitiesList(gedcom);
        }
     return (List)new ArrayList((Collection)listOfCities.keySet());
     }

  public int getCitiesCount(String city) {
     if (listOfCities.get(city) == null) {
        return -1; 
        }
     Info infoCity = (Info)listOfCities.get(city);
     return (int)infoCity.counter;
     }

  public List<Property> getCitiesProps(String city) {
     if (listOfCities.get(city) == null) {
        return null; 
        }
     Info infoCity = (Info)listOfCities.get(city);
     return (List<Property>)infoCity.props;
     }

  private boolean buildCitiesList(Gedcom gedcom) {

     listOfCities = new TreeMap<String, Info>(sortLastnames);
     Collection entities = gedcom.getEntities();
     List placesProps = new ArrayList();
     for (Iterator it = entities.iterator(); it.hasNext();) {
        Entity ent = (Entity) it.next();
        getPropertiesRecursively((Property)ent, placesProps, "PLAC");
        }
     for (Iterator it = entities.iterator(); it.hasNext();) {
        Entity ent = (Entity) it.next();
        getPropertiesRecursively((Property)ent, placesProps, "CITY");
        }

     String juridic = "";
     for (Iterator it = placesProps.iterator(); it.hasNext();) {
        Property prop = (Property) it.next();
        if (prop instanceof PropertyPlace) {
           juridic = getPlace((PropertyPlace) prop);
           }
        else {
           juridic = prop.getValue().trim();
           }
        if (juridic != null && juridic.length() > 0) {
           Integer val = null;
           List<Property> listProps = null;
           Info infoCity = (Info)listOfCities.get(juridic); 
           if (infoCity == null) {
              val = 0;
              listProps = new ArrayList<Property>();
              infoCity = new Info();
              }
           else {
              val = infoCity.counter;
              listProps = infoCity.props;
              }
           val += 1;
           listProps.add(prop);
           infoCity.counter = val;
           infoCity.props = listProps;
           listOfCities.put(juridic, infoCity); 
           }
        }
     return true;
     }


  public void getPropertiesRecursively(Property parent, List props, String tag) {
      Property[] children = parent.getProperties();
      for (int c=0;c<children.length;c++) {
        Property child = children[c];
        if (child.getTag().compareTo(tag) == 0) {
          props.add(child);
          }
        getPropertiesRecursively(child, props, tag);
        }
      }

  public String getPlace(PropertyPlace prop) {
      return prop.getCity().trim();
      }

  public String getPlace(PropertyPlace prop, int level) {
      String place = "";
      int i = level;
      String str = prop.getJurisdiction(i);
      while (str != null && str.length() != 0) {
         if (i > level) {
            place += ", ";
            }
         place += str;
         i++;
         str = prop.getJurisdiction(i);
         } 
      return place;
      }


  /**
   * Return sorted list of days in the year for Gedcom file
   * Days are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only) 
   */
  public List<String> getDays(Gedcom gedcom) {
     if (!initDay) {
        initDay = buildDaysList(gedcom);
        }
     return (List)new ArrayList((Collection)listOfDays.keySet());
     }

  public int getDaysCount(String day) {
     if (listOfDays.get(day) == null) {
        return -1; 
        }
     Info infoDay = (Info)listOfDays.get(day);
     return (int)infoDay.counter;
     }

  public List<Property> getDaysProps(String day) {
     if (listOfDays.get(day) == null) {
        return null; 
        }
     Info infoDay = (Info)listOfDays.get(day);
     return (List<Property>)infoDay.props;
     }

  private boolean buildDaysList(Gedcom gedcom) {

     listOfDays = new TreeMap<String, Info>();
     Collection entities = gedcom.getEntities();
     List datesProps = new ArrayList();
     for (Iterator it = entities.iterator(); it.hasNext();) {
        Entity ent = (Entity) it.next();
        getPropertiesRecursively((Property)ent, datesProps, "DATE");
        }

     String day = "";
     for (Iterator it = datesProps.iterator(); it.hasNext();) {
        Property prop = (Property) it.next();
        day = getDay(prop);
        if (day != null) {
           Integer val = null;
           List<Property> listProps = null;
           Info infoDay = (Info)listOfDays.get(day); 
           if (infoDay == null) {
              val = 0;
              listProps = new ArrayList<Property>();
              infoDay = new Info();
              }
           else {
              val = infoDay.counter;
              listProps = infoDay.props;
              }
           val += 1;
           listProps.add(prop);
           infoDay.counter = val;
           infoDay.props = listProps;
           listOfDays.put(day, infoDay);
           }
        }
     return true;
     }


  public String getDay(Property prop) {
      if (!(prop instanceof PropertyDate)) return null;
      PropertyDate date = (PropertyDate) prop;
      if (!date.isValid() || date.isRange()) return null;
      PointInTime pit = null;
      try {
         pit = date.getStart().getPointInTime(PointInTime.GREGORIAN);
         if (pit.getMonth() < 0 || pit.getMonth() > 11) return null;
         if (pit.getDay() < 0 || pit.getDay() > 30) return null;
         return String.format("%02d", pit.getMonth()+1) + String.format("%02d", pit.getDay()+1);
         } catch (GedcomException e) {
           // e.printStackTrace(); 
           report.println(e.toString());
         }
      return null;
      }



  /**
   * Get Ancestors
   */
  public List<Ancestor> getAncestors(Indi rootIndi, int startSosa) {
     if (!initAncestors) {
        initAncestors = buildAncestors(rootIndi, startSosa);
        }
     return listOfAncestors;
     }

  public Set<Indi> getAncestors(Indi rootIndi) {
     if (!initAncestors) {
        initAncestors = buildAncestors(rootIndi, 0);
        }
    Set<Indi> list = new HashSet<Indi>();
    for (Iterator it = listOfAncestors.iterator(); it.hasNext();) {
       Ancestor ancestor = (Ancestor)it.next();
       list.add(ancestor.indi);
       }
    return list;
    }

  private boolean buildAncestors(Indi rootIndi, int startSosa) {
    // Depending on option, start at sosa number 1 or if option says 0, the one of the individual selected
    if (startSosa == 0){
       Property sosaProp = rootIndi.getProperty(SOSA_TAG);
       if (sosaProp != null){
          try {
             startSosa = Integer.parseInt(sosaProp.getValue(),10);
             } catch (NumberFormatException e){
             }
          }
       }
    // Start at 1 in case something has gone wrong
    if (startSosa == 0){
       startSosa = 1;
    }

    // Run recursion
    List list = new ArrayList(3);
    list.add(new Integer(startSosa));
    list.add(rootIndi);
    Fam[] fams = rootIndi.getFamiliesWhereSpouse();
    if ((fams != null) && (fams.length > 0)) {
       list.add(fams[0]);
       }
    else {
       list.add(null);
       }
    recursion(list, 1);

    Collections.sort(listOfAncestors, sortAncestors);
    return true;
    }

   /**
    * Recurse over a generation list up to the maximum number of generations
    * @param generation the current generation (sosa,indi,fam) - the list of all individuals in that generation
    * @param gen the current generation
    */
   void recursion(List generation, int gen) {

     // Build mext generation (scan individuals in that generation and build next one)
     List nextGeneration = new ArrayList();

     for (int i=0; i<generation.size(); ) {
       // next triplet
       int sosa = ((Integer)generation.get(i++)).intValue();
       Indi indi = (Indi)generation.get(i++);
       Fam fam = (Fam)generation.get(i++);

       // grab father and mother
       Fam famc = indi.getFamilyWhereBiologicalChild();
       if (famc!=null)  {
         Indi father = famc.getHusband();
         if (father!=null) {
           nextGeneration.add(new Integer(sosa*2));
           nextGeneration.add(father);
           nextGeneration.add(famc);
           }
         Indi mother = famc.getWife();
         if (mother!=null) {
           nextGeneration.add(new Integer(sosa*2+1));
           nextGeneration.add(mother);
           nextGeneration.add(famc);
           }
         }
       }

       // store ancestor information
       for (int i=0; i<generation.size(); ) {
           int sosa = ((Integer)generation.get(i++)).intValue();
           Indi indi = (Indi)generation.get(i++);
           i++;
           Ancestor ancestor = new Ancestor();
           ancestor.sosa = sosa;
           ancestor.gen = gen;
           ancestor.indi = indi;
           listOfAncestors.add(ancestor);
           }

     // Recurse into next generation
     if (!nextGeneration.isEmpty()) {
       recursion(nextGeneration, gen+1);
       }
   }

  Comparator sortAncestors = new Comparator() {
    public int compare(Object o1, Object o2) {
       Ancestor a1 = (Ancestor)o1;
       Ancestor a2 = (Ancestor)o2;
       return a1.sosa - a2.sosa;
       }
    };



  /**
   * Get Cousins
   */
  public Set<Indi> getCousins(Indi rootIndi) {
     if (!initCousins) {
        initCousins = buildCousins(rootIndi);
        }
     return listOfCousins;
     }

  private boolean buildCousins(Indi rootIndi) {
    // declarations
    List indis = new ArrayList(rootIndi.getGedcom().getEntities(Gedcom.INDI));
    Set ancestors = getAncestors(rootIndi);
    Set otherIndis = new HashSet();

    // get all non ancestors
    for (Iterator it = indis.iterator(); it.hasNext();) {
       Indi indi = (Indi)it.next();
       if (!ancestors.contains(indi)) {
          otherIndis.add(indi);
          }
       }

    // Get cousins now by flaging all non ancestors that are descendants of ancestors
    for (Iterator it = ancestors.iterator(); it.hasNext();) {
       Indi ancestor = (Indi)it.next();
       Set<Indi> descendants = new HashSet<Indi>();
       getDescendants(ancestor, otherIndis, descendants);
       listOfCousins.addAll(descendants);
       otherIndis.removeAll(descendants);
       }
    return true;
    }

  private void getDescendants(Indi ancestor, Set<Indi> inSet, Set<Indi> descendants) {
    Indi[] children = ancestor.getChildren();
    for (int i = 0 ; i < children.length; i++) {
       Indi indi = children[i];
       if (!inSet.contains(indi)) {
          continue;
          }
       descendants.add(indi);
       inSet.remove(indi);
       getDescendants(indi, inSet, descendants);
       }
    return;
    }

  /**
   * Check if individual or entity
   */
  public boolean isPrivate(Indi indi) {
     return ((indi != null) && (indi.getBirthDate() != null) && (report.getPrivacyPolicy().isPrivate(indi.getBirthDate())));
     }

  public boolean isPrivate(Entity ent) {
     if (ent instanceof Indi) {
       return isPrivate((Indi) ent);
       } 
     if (ent instanceof Fam) {
       Fam famRel = (Fam)ent;
       Indi husband = famRel.getHusband();
       Indi wife    = famRel.getWife();
       return isPrivate(husband) || isPrivate(wife);
       }
     return false; 
     }


  /**
   * Set keywords for meta tag
   */
  public void setKeywords(Gedcom gedcom) {
    String keywords = "";
    List<String> listnames = getLastNames(gedcom);
    SortedMap<Integer, String> volumePerName = new TreeMap<Integer, String>(new Comparator() {
     public int compare(Object o1, Object o2) {
        Integer i1 = (Integer)o1;
        Integer i2 = (Integer)o2;
        return (i2.compareTo(i1));
        }
     });
    Iterator it = listnames.iterator();
    while (it.hasNext()) {
      String key = it.next().toString();
      if (key.length() > 0) {
        volumePerName.put((Integer)getLastNameCount(key), key.trim().toLowerCase());
        }
      }
    int cpt = 0;
    for (it = volumePerName.keySet().iterator(); it.hasNext(); ) {
       Integer k = (Integer)it.next();
       keywords += volumePerName.get(k) + " ";
       cpt++;
       if (cpt > 50) break;
       }
    this.keywords = keywords; 
  }

  /**
   * Convert string into anchor compatible text
   */
  public String htmlAnchorText(String anchor) {
     // trim and only AZaz-
     String strInput = "";
     if (anchor == null) {
        return DEFCHAR;
        } 
     strInput = anchor.trim().toLowerCase();
     if (strInput.length() == 0) {
        return DEFCHAR;
        } 
     char[] charInput = strInput.toCharArray();
     StringBuffer strOutput = new StringBuffer(1000);
     for (int i = 0; i<charInput.length; i++) {
        strOutput.append(convertChar(charInput[i], true));
        }
     return strOutput.toString().toUpperCase();
     }

  public String fileNameConvert(String filename) {
     if (filename == null) {
        return DEFCHAR;
        } 
     String text = filename.toLowerCase();
     char[] charInput = text.toCharArray();
     StringBuffer strOutput = new StringBuffer(1000);
     for (int i = 0; i < charInput.length; i++) {
        strOutput.append(convertChar(charInput[i], false));
        }
     return strOutput.toString();
     }

  public String convertChar(char c, boolean isAnchor) {
     String str = null;
        switch (c) {
           case 'à' : str = "a"; break;
           case 'á' : str = "a"; break;
           case 'â' : str = "a"; break;
           case 'ã' : str = "a"; break;
           case 'ä' : str = "a"; break;
           case 'å' : str = "a"; break;
           case 'æ' : str = "ae"; break;
           case 'ç' : str = "c"; break;
           case 'è' : str = "e"; break;
           case 'é' : str = "e"; break;
           case 'ê' : str = "e"; break;
           case 'ë' : str = "e"; break;
           case 'ì' : str = "i"; break;
           case 'í' : str = "i"; break;
           case 'î' : str = "i"; break;
           case 'ï' : str = "i"; break;
           case 'ð' : str = "o"; break;
           case 'ñ' : str = "n"; break;
           case 'ò' : str = "o"; break;
           case 'ó' : str = "o"; break;
           case 'ô' : str = "o"; break;
           case 'õ' : str = "o"; break;
           case 'ö' : str = "o"; break;
           case 'ø' : str = "o"; break;
           case 'ù' : str = "u"; break;
           case 'ú' : str = "u"; break;
           case 'û' : str = "u"; break;
           case 'ü' : str = "u"; break;
           case 'ý' : str = "y"; break;
           case 'þ' : str = "p"; break;
           case 'ÿ' : str = "y"; break;
           case 'ß' : str = "ss"; break;
           default  : str = String.valueOf(c);
                      if (str.matches("[a-zA-Z0-9]")) {
                         return str;
                         }
                      else if (str.compareTo(".") == 0) {
                         return (isAnchor ? DEFCHAR : str);
                         }
                      else if (str.compareTo("/") == 0) {
                         return (isAnchor ? DEFCHAR : str);
                         }
                      else if (str.compareTo("\\") == 0) {
                         return (isAnchor ? DEFCHAR : str);
                         }
                      else {
                         return DEFCHAR;
                         }
                    }
     return str;
     }

  /**
   * Convert string into html compatible text
   */
  public String htmlText(int i) {
     return htmlText(Integer.toString(i));
     }

  public String htmlText(double d) {
     return htmlText(Double.toString(d));
     }

  public String htmlText(Object o) {
     return htmlText(o.toString());
     }

  public String htmlText(String text) {
     return htmlText(text, true);
     }

  public String htmlText(String text, boolean convertTags) {
     // No accent, <, >, etc
     char[] charInput = text.toCharArray();
     StringBuffer strOutput = new StringBuffer(1000);
     for (int i = 0; i < charInput.length; i++) {
        switch (charInput[i]) {
           // line breaks
           case '\n' : strOutput.append("<br />"); break;

           // html tags
           case '<' : strOutput.append(convertTags ? "&lt;" : String.valueOf(charInput[i])); break;
           case '>' : strOutput.append(convertTags ? "&gt;" : String.valueOf(charInput[i])); break;
           case '&' : strOutput.append(convertTags ? "&amp;" : String.valueOf(charInput[i])); break;
           case '"': strOutput.append(convertTags ? "&quot;" : String.valueOf(charInput[i])); break;

           // accented characters
           case '\u00a1' : strOutput.append("&iexcl;"); break;
           case '\u00a2' : strOutput.append("&cent;"); break;
           case '\u00a3' : strOutput.append("&pound;"); break;
           case '\u00a4' : strOutput.append("&curren;"); break;
           case '\u00a5' : strOutput.append("&yen;"); break;
           case '\u00a6' : strOutput.append("&brvbar;"); break;
           case '\u00a7' : strOutput.append("&sect;"); break;
           case '\u00a8' : strOutput.append("&uml;"); break;
           case '\u00a9' : strOutput.append("&copy;"); break;
           case '\u00aa' : strOutput.append("&ordf;"); break;
           case '\u00ab' : strOutput.append("&laquo;"); break;
           case '\u00ac' : strOutput.append("&not;"); break;

           case '\u00ad' : strOutput.append("&shy;"); break;
           case '\u00ae' : strOutput.append("&reg;"); break;
           case '\u00af' : strOutput.append("&hibar;"); break;

           case '\u00b0' : strOutput.append("&deg;"); break;
           case '\u00b1' : strOutput.append("&plusmn;"); break;
           case '\u00b2' : strOutput.append("&sup2;"); break;
           case '\u00b3' : strOutput.append("&sup3;"); break;
           case '\u00b4' : strOutput.append("&acute;"); break;
           case '\u00b5' : strOutput.append("&micro;"); break;
           case '\u00b6' : strOutput.append("&para;"); break;
           case '\u00b7' : strOutput.append("&middot;"); break;
           case '\u00b8' : strOutput.append("&cedil;"); break;
           case '\u00b9' : strOutput.append("&sup1;"); break;
           case '\u00ba' : strOutput.append("&ordm;"); break;
           case '\u00bb' : strOutput.append("&raquo;"); break;
           case '\u00bc' : strOutput.append("&frac14;"); break;
           case '\u00bd' : strOutput.append("&frac12;"); break;
           case '\u00be' : strOutput.append("&frac34;"); break;
           case '\u00bf' : strOutput.append("&iquest;"); break;

           case '\u00c0' : strOutput.append("&Agrave;"); break;
           case '\u00c1' : strOutput.append("&Aacute;"); break;
           case '\u00c2' : strOutput.append("&Acirc;"); break;
           case '\u00c3' : strOutput.append("&Atilde;"); break;
           case '\u00c4' : strOutput.append("&Auml;"); break;
           case '\u00c5' : strOutput.append("&Aring;"); break;
           case '\u00c6' : strOutput.append("&AElig;"); break;
           case '\u00c7' : strOutput.append("&Ccedil;"); break;
           case '\u00c8' : strOutput.append("&Egrave;"); break;
           case '\u00c9' : strOutput.append("&Eacute;"); break;
           case '\u00ca' : strOutput.append("&Ecirc;"); break;
           case '\u00cb' : strOutput.append("&Euml;"); break;
           case '\u00cc' : strOutput.append("&Igrave;"); break;
           case '\u00cd' : strOutput.append("&Iacute;"); break;
           case '\u00ce' : strOutput.append("&Icirc;"); break;
           case '\u00cf' : strOutput.append("&Iuml;"); break;

           case '\u00d0' : strOutput.append("&ETH;"); break;
           case '\u00d1' : strOutput.append("&Ntilde;"); break;
           case '\u00d2' : strOutput.append("&Ograve;"); break;
           case '\u00d3' : strOutput.append("&Oacute;"); break;
           case '\u00d4' : strOutput.append("&Ocirc;"); break;
           case '\u00d5' : strOutput.append("&Otilde;"); break;
           case '\u00d6' : strOutput.append("&Ouml;"); break;
           case '\u00d7' : strOutput.append("&times;"); break;
           case '\u00d8' : strOutput.append("&Oslash;"); break;
           case '\u00d9' : strOutput.append("&Ugrave;"); break;
           case '\u00da' : strOutput.append("&Uacute;"); break;
           case '\u00db' : strOutput.append("&Ucirc;"); break;
           case '\u00dc' : strOutput.append("&Uuml;"); break;
           case '\u00dd' : strOutput.append("&Yacute;"); break;
           case '\u00de' : strOutput.append("&THORN;"); break;
           case '\u00df' : strOutput.append("&szlig;"); break;

           case '\u00e0' : strOutput.append("&agrave;"); break;
           case '\u00e1' : strOutput.append("&aacute;"); break;
           case '\u00e2' : strOutput.append("&acirc;"); break;
           case '\u00e3' : strOutput.append("&atilde;"); break;
           case '\u00e4' : strOutput.append("&auml;"); break;
           case '\u00e5' : strOutput.append("&aring;"); break;
           case '\u00e6' : strOutput.append("&aelig;"); break;
           case '\u00e7' : strOutput.append("&ccedil;"); break;
           case '\u00e8' : strOutput.append("&egrave;"); break;
           case '\u00e9' : strOutput.append("&eacute;"); break;
           case '\u00ea' : strOutput.append("&ecirc;"); break;
           case '\u00eb' : strOutput.append("&euml;"); break;
           case '\u00ec' : strOutput.append("&igrave;"); break;
           case '\u00ed' : strOutput.append("&iacute;"); break;
           case '\u00ee' : strOutput.append("&icirc;"); break;
           case '\u00ef' : strOutput.append("&iuml;"); break;

           case '\u00f0' : strOutput.append("&eth;"); break;
           case '\u00f1' : strOutput.append("&ntilde;"); break;
           case '\u00f2' : strOutput.append("&ograve;"); break;
           case '\u00f3' : strOutput.append("&oacute;"); break;
           case '\u00f4' : strOutput.append("&ocirc;"); break;
           case '\u00f5' : strOutput.append("&otilde;"); break;
           case '\u00f6' : strOutput.append("&ouml;"); break;
           case '\u00f7' : strOutput.append("&divide;"); break;
           case '\u00f8' : strOutput.append("&oslash;"); break;
           case '\u00f9' : strOutput.append("&ugrave;"); break;
           case '\u00fa' : strOutput.append("&uacute;"); break;
           case '\u00fb' : strOutput.append("&ucirc;"); break;
           case '\u00fc' : strOutput.append("&uuml;"); break;
           case '\u00fd' : strOutput.append("&yacute;"); break;
           case '\u00fe' : strOutput.append("&thorn;"); break;
           case '\u00ff' : strOutput.append("&yuml;"); break;

           case '\u0152' : strOutput.append("&OElig;"); break;
           case '\u0153' : strOutput.append("&oelig;"); break;
           case '\u0160' : strOutput.append("&Scaron;"); break;
           case '\u0161' : strOutput.append("&scaron;"); break;
           case '\u0178' : strOutput.append("&Yuml;"); break;
           case '\u017d' : strOutput.append("&Zcaron;"); break;
           case '\u017e' : strOutput.append("&zcaron;"); break;

           case '\u0192' : strOutput.append("&fnof;"); break;
           case '\u02c6' : strOutput.append("&circ;"); break;
           case '\u02dc' : strOutput.append("&tilde;"); break;
           case '\u03a9' : strOutput.append("&Omega;"); break;
           case '\u03c0' : strOutput.append("&pi;"); break;
           case '\u2013' : strOutput.append("&ndash;"); break;
           case '\u2014' : strOutput.append("&mdash;"); break;
           case '\u2018' : strOutput.append("&lsquo;"); break;
           case '\u2019' : strOutput.append("&rsquo;"); break;
           case '\u201a' : strOutput.append("&sbaquo;"); break;
           case '\u201c' : strOutput.append("&ldquo;"); break;
           case '\u201d' : strOutput.append("&rdquo;"); break;
           case '\u201e' : strOutput.append("&bdquote;"); break;
           case '\u2020' : strOutput.append("&dagger;"); break;
           case '\u2021' : strOutput.append("&Dagger;"); break;
           case '\u2022' : strOutput.append("&bull;"); break;
           case '\u2026' : strOutput.append("&hellip;"); break;
           case '\u2030' : strOutput.append("&permil;"); break;
           case '\u2039' : strOutput.append("&lsaquo;"); break;
           case '\u203a' : strOutput.append("&rsaquo;"); break;
           case '\u2044' : strOutput.append("&frasl;"); break;
           case '\u20ac' : strOutput.append("&euro;"); break;
           case '\u2122' : strOutput.append("&trade;"); break;
           case '\u2202' : strOutput.append("&part;"); break;
           case '\u220f' : strOutput.append("&prod;"); break;
           case '\u2211' : strOutput.append("&sum;"); break;
           case '\u221a' : strOutput.append("&radic;"); break;
           case '\u221e' : strOutput.append("&infin;"); break;
           case '\u222b' : strOutput.append("&int;"); break;
           case '\u2248' : strOutput.append("&asymp;"); break;
           case '\u2260' : strOutput.append("&ne;"); break;
           case '\u2264' : strOutput.append("&le;"); break;
           case '\u2265' : strOutput.append("&ge;"); break;
           case '\u25ca' : strOutput.append("&loz;"); break;

           default  : strOutput.append(String.valueOf(charInput[i]));
           }
        }
     return strOutput.toString();
     }

  /**
   * Create popup html file
   */
  public void createPopup(File file) {
    PrintWriter out = getWriter(file);
    out.println("<html><head><title>"+report.translate("popup_title")+"</title>");
    out.println("<script language='javascript'>");
    out.println("var arrTemp=self.location.href.split(\"?\");");
    out.println("var picUrl = (arrTemp.length>0)?arrTemp[1]:\"\";");
    out.println("function setup() {");
    out.println("w = document.images[0].width; h = document.images[0].height+20;");
    out.println("window.resizeTo(w, h); window.moveTo(self.screen.width/2-w/2, self.screen.height/2-h/2);");
    out.println("}");
    out.println("</script>");
    out.println("</head><body  onload='setup();'  onclick='self.close();' bgcolor=\"#ffffff\" topmargin=\"0\" marginheight=\"0\" leftmargin=\"0\" marginwidth=\"0\">");
    out.println("<script language='javascript'>");
    out.println("document.write( \"<img src='\" + picUrl + \"' border=0>\" );");
    out.println("</script>");
    out.println("</body></html>");
    out.close();
    }

  /**
   * Create popup email form html file
   */
  public void createPopupEmail(File file) {
    PrintWriter out = getWriter(file);
    out.println("<html><head><title>"+report.translate("popupemail_title")+"</title>");
    out.println("<link rel=\"StyleSheet\" href=\"../"+css+"\" type=\"text/css\"/>");
    out.println("<script language='javascript'>");
    out.println("var arrTemp=self.location.href.split(\"?\");");
    out.println("var person = (arrTemp.length>0) ? \": \"+arrTemp[1] : \"\";");
    out.println("</script>");
    out.println("</head>");
    out.println("<body bgcolor=\"#ffffff\" topmargin=\"10\" marginheight=\"10\" leftmargin=\"10\" marginwidth=\"10\">");

    out.println("<div class=\"contreport\">");
    out.println("<p class=\"decal\"><br /><span class=\"gras\">"+htmlText(report.translate("emailform_description"))+"</span></p>");
    out.println("<p class=\"description\">"+htmlText(report.translate("emailform_info"))+"</p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    out.println("<div class=\"contreport\">");
    out.println("<p class=\"decal\"><br /><span class=\"gras\">"+ htmlText(report.translate("emailform_form")) + "</span></p>");
    out.println("<p class=\"description\">");
    out.println("  <script language='javascript'>");
    out.println("  document.write( \"<form class='description' action='mailto:" + report.getEmailAddress() + "?subject=" + report.translate("idx_email_subject") + "\" + person + \"' method='post' enctype='text/plain' >\" );");
    out.println("  </script>");
    out.println("  "+ htmlText(report.translate("emailform_name")) +":&nbsp;<input type=\"text\" size=\"60\" name=\"" + htmlText(report.translate("emailform_mynameis")) +"\"><br /><br />");
    out.println("  "+ htmlText(report.translate("emailform_reason")) +":&nbsp;<input type=\"text\" size=\"60\" name=\"" + htmlText(report.translate("emailform_reason")) +"\"><br /><br />");
    out.println("  "+ htmlText(report.translate("emailform_message")) +":&nbsp;");
    out.println("  <textarea name=\""+ htmlText(report.translate("emailform_message")) +"\" cols=60 rows=14 wrap=virtual></textarea><br /><br />");
    out.println("  <center><input onclick='self.close();' type=\"submit\" value=\""+ htmlText(report.translate("emailform_send")) +"\">&nbsp;&nbsp;&nbsp;<input onclick='self.close();' type=\"reset\" value=\""+ htmlText(report.translate("emailform_cancel")) +"\"></center>");
    out.println("  </form>");
    out.println("</p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    out.println("</body></html>");
    out.close();
    }

} // End_of_Report



