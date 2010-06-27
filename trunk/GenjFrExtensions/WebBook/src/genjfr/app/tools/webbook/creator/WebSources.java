/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjfr.app.tools.webbook.creator;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.nio.charset.Charset;
import java.util.TreeMap;
import java.util.Map;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSources extends WebSection {

  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private final static String POPUP = "popup.htm";
  private final static TagPath PATH2ABBR = new TagPath("SOUR:ABBR");
  private final static TagPath PATH2AUTH = new TagPath("SOUR:AUTH");
  private final static TagPath PATH2TEXT = new TagPath("SOUR:TEXT");
  private final static TagPath PATH2REPO = new TagPath("SOUR:REPO");
  private final static TagPath PATH2EVEN = new TagPath("SOUR:DATA:EVEN");
  private final static TagPath PATH2NOTE = new TagPath("SOUR:NOTE");
  private final static TagPath PATH2XREF = new TagPath("SOUR:XREF");
  private final static TagPath PATH2IMAGES = new TagPath("SOUR:OBJE:FILE");
  private int WIDTH_PICTURES = 200;

  private final static int
    SRC_NOMEDIA = 0,
    SRC_MEDIAMIN = 1,
    SRC_MEDIARELTEXT = 2,
    SRC_MEDIARELMIN = 3;

  private String themeDir = "";

  private Map<String, String> sourcePage = new TreeMap<String, String>();
  private Map<String, String> personPage = new TreeMap<String, String>();
  private WebIndividualsDetails reportIndi = null;
  private String src2indiDir = "";

  private boolean showDate = true;
  private boolean showAllPlaceJurisdictions = true;

  /**
   * Constructor
   */
    public WebSources(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
//        init(trs("TXT_Individualsdetails"), "details", "personsdetails_", formatFromSize(wh.getNbIndis()), ".html", 1, sizeIndiSection);
//     calcPages(sources);
    }

//  /**
//   * Section's entry point
//   */
//  public void run(List sources, WebIndividualsDetails reportIndi, WebSection webSectionIndi) {
//
//    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);
//
//    themeDir = wh.buildLinkTheme(section, report.getThemeDir());
//
//    this.reportIndi = reportIndi;
//    if (reportIndi != null) {
//       personPage = reportIndi.getPagesMap();
//       src2indiDir = wh.buildLinkShort(section, webSectionIndi);
//       }
//    exportData(dir, sources);
//
//  }
//
//  /**
//   * Exports data for page
//   */
//  private void exportData(File dir, List sources) {
//
//    // Go through items to display and produce corresponding pages
//    String fileStr = "";
//    File file = null;
//    PrintWriter out = null;
//    String sourcefile = "";
//    int cpt = 0;
//    int nbSources = sources.size();
//    int previousPage = 0,
//        currentPage  = 0,
//        nextPage     = 0,
//        lastPage     = (nbSources/section.nbPerPage)+1;
//
//    wh.createPopup(wh.getFileForName(dir, POPUP));
//    report.println(POPUP+" - Done.");
//
//    // export first page (page '0' as the index page)
//    sourcefile = section.sectionPrefix + String.format(section.formatNbrs, 0) + section.sectionSuffix;
//    fileStr = sourcefile;
//    file = wh.getFileForName(dir, sourcefile);
//    out = wh.getWriter(file);
//    wh.printOpenHTML(out, "SourcesIndex", section);
//    out.println("<br /><br />");
//    exportLinks(out, sourcefile, 0, 1, lastPage);
//    out.println("<br /><br />");
//    String src_title = "";
//    String src_abbr = "";
//    String src_author = "";
//    String description = "";
//    for (Iterator it = sources.iterator(); it.hasNext();) {
//      Source src = (Source)it.next();
//      src_title = src.getTitle();
//      if ((src_title == null) || (src_title.length() == 0)) {
//         src_title = src.toString();
//         }
//      src_abbr = (src.getProperty(PATH2ABBR) == null) ? "" : src.getProperty(PATH2ABBR).getValue();
//      if ((src_abbr == null) || (src_abbr.length() == 0)) src_abbr = "";
//      else src_title += "   -   ";
//      src_author = (src.getProperty(PATH2AUTH) == null) ? "" : src.getProperty(PATH2AUTH).getValue();
//      if ((src_author == null) || (src_author.length() == 0)) src_author = "";
//      else src_abbr += "   -   ";
//      description = wh.htmlText(src_title+src_abbr+src_author);
//      out.println("<div class=\"conteneur\">");
//      out.println("<p class=\"srclist\">"+src.getId()+"</p>");
//      out.println("<p class=\"srclisttitle\"><a href=\""+getPageForSource(src)+'#'+src.getId()+"\">"+description+"</a></p><br />");
//      out.println("<div class=\"spacer\">"+SPACE+"</div>");
//      out.println("</div>");
//      }
//    exportLinks(out, sourcefile, 0, 1, lastPage);
//    wh.printCloseHTML(out);
//    out.close();
//    report.println(sourcefile+" - Done.");
//
//    // export detailed pages
//    cpt = 0;
//    out = null;
//    for (Iterator it = sources.iterator(); it.hasNext();) {
//      Source src = (Source)it.next();
//      cpt++;
//      currentPage = (cpt/section.nbPerPage)+1;
//      previousPage = (currentPage == 1) ? 1 : currentPage-1;
//      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1;
//      sourcefile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
//      if (fileStr.compareTo(sourcefile) != 0) {
//         if (out != null) {
//            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
//            wh.printCloseHTML(out);
//            out.close();
//            report.println(fileStr+" - Done.");
//            }
//         fileStr = sourcefile;
//         file = wh.getFileForName(dir, sourcefile);
//         out = wh.getWriter(file);
//         wh.printOpenHTML(out, "Sources", section);
//         openBody(out);
//         }
//      if ((cpt == 1) || ((cpt/5) * 5) == cpt) exportLinks(out, sourcefile, previousPage, nextPage, lastPage);
//      exportSectionDetails(out, src, dir, sourcefile);
//      // .. next individual
//     }
//    if (out != null) {
//       exportLinks(out, sourcefile, previousPage, nextPage, lastPage);
//       wh.printCloseHTML(out);
//       report.println(fileStr+" - Done.");
//       }
//
//    // done
//    if (out != null) out.close();
//
//  }
//
//  /**
//   * Exports section details
//   */
//  private void exportSectionDetails(PrintWriter out, Source src, File dir, String sourcefile) {
//    // Display an item details
//    /*
//    * Sources generally have the following structure
//    *
//    * SOUR
//          o TITL Title of the source
//          o ABBR Abreviation of source or related organisation
//          o AUTH Author of the source
//          o OBJE Multimedia (picture of the source content)
//          o TEXT Text of the content
//          o DATA Data described in the source, generally the events that it describes
//                + EVEN BIRT, MARR, DEAT
//                      # DATE of event
//                      # PLAC of event
//          o REPO Storage of the source
//                + CALN Call number
//                      # MEDI type of media
//          o NOTE Note about the source
//          o XREF Associated entities
//    */
//
//    // Initialises anchor, list of files and list of related entities and determine if source is private along the way
//    String anchor = src.getId();
//    List<PropertyFile> files = new ArrayList<PropertyFile>();              // Files of sources in SOUR and related entities for that SOUR
//    files.addAll(src.getProperties(PropertyFile.class));                   // Put first all files directly attached to SOUR entity
//
//    boolean sourceIsPrivate = false;
//    Property[] props = src.getProperties(PATH2XREF);
//    List<Entity> list = new ArrayList<Entity>();                           // List of related entities
//    if (props != null && props.length > 0) {
//       for (int i = 0 ; i < props.length ; i++) {
//          PropertyXRef p = (PropertyXRef)props[i];
//          Entity target = (Entity)p.getTargetEntity();
//          if (list.contains(target)) continue;
//          if ((target instanceof Indi) || (target instanceof Fam)) {
//             list.add(target);
//             sourceIsPrivate = sourceIsPrivate || wh.isPrivate(target);
//             }
//          }
//       }
//    Collections.sort(list, sortEntities);
//
//    // Starts the output on the page
//    if (wh.isPrivate(src)) {
//       out.println("<a name=\""+anchor+"\"></a>");
//       out.println("...");
//       out.println("<br />");
//       return;
//       }
//    out.println("<p><a name=\""+anchor+"\"></a>"+SPACE+"</p>");
//
//    String src_title = src.getTitle();
//    if ((src_title == null) || (src_title.length() == 0)) {
//       src_title = src.toString();
//       }
//
//    // Opens the block
//    out.println("<div class=\"conteneur\">");
//    out.println("<p class=\"srcdecal\"><span class=\"gras\">"+src.getId()+SPACE+wh.htmlText(src_title)+"</span></p>");
//
//
//    // Prepare style based on whether there will be images or not
//    out.println("<p class=\"srcitems\">");
//    Property prop  = null;
//
//    // Print all properties
//    prop = src.getProperty(PATH2ABBR);
//    if ((prop != null) && (prop.getValue().length() > 0)) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems2\">"+wh.htmlText(sourceIsPrivate ? report.translate("med_priv") : prop.getValue())+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    prop = src.getProperty(PATH2AUTH);
//    if ((prop != null) && (prop.getValue().length() > 0)) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems2\">"+wh.htmlText(sourceIsPrivate ? report.translate("med_priv") : prop.getValue())+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    prop = src.getProperty(PATH2EVEN);
//    if (prop != null) {
//       Property pdate   = prop.getProperty("DATE");
//       String date = "";
//       if (pdate != null) date = pdate.toString();
//       Property pplace = prop.getProperty("PLAC");
//       String place = "";
//       if (pplace != null) place = pplace.toString().replaceAll(",", " ");
//       String description = prop.getValue().replaceAll("BIRT", report.translate("src_BIRT")).replaceAll("MARR", report.translate("src_MARR")).replaceAll("DEAT", report.translate("src_DEAT"));
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems2\">"+description+","+SPACE+wh.htmlText(date)+","+SPACE+wh.htmlText(place)+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    prop = src.getProperty(PATH2REPO);
//    if ((prop != null) && (prop.toString().length() > 0)) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems2\">"+wh.htmlText(sourceIsPrivate ? report.translate("med_priv") : prop.toString())+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    prop = src.getProperty(PATH2TEXT);
//    if ((prop != null) && (prop.getValue().length() > 0)) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems3\">"+wh.htmlText(sourceIsPrivate ? report.translate("med_priv") : prop.getValue())+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    prop = src.getProperty(PATH2NOTE);
//    if ((prop != null) && (prop.getValue().length() > 0)) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(prop.getPropertyName())+":</span><span class=\"srcitems2\">"+wh.htmlText(sourceIsPrivate ? report.translate("med_priv") : prop.getValue())+"</span><br />");
//       out.println("<span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    // Print pictures of SOUR entity
//    if (!sourceIsPrivate && (report.getDisplaySrcImg() >= SRC_MEDIAMIN) && !files.isEmpty()) {
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(report.translate("src_media"))+":</span><span class=\"srcimage\">");
//       for (Iterator it = files.iterator(); it.hasNext();) {
//          PropertyFile file = (PropertyFile)it.next();
//          out.println("<span class=\"srcimage1\">");
//          wrapMedia(out, dir, file);
//          out.println("</span><span class=\"srcimage2\">"+SPACE+"</span>");
//          }
//       out.println("</span><br /><span class=\"spacer\">"+SPACE+"</span>");
//       files.clear();
//       }
//
//    if (!list.isEmpty()) {
//       List<PropertyFile> mediasOfEntity = new ArrayList<PropertyFile>();     // temp list
//       out.println("<span class=\"srcitems1\">"+wh.htmlText(report.translate("src_associations"))+":</span>");
//       out.println("<span class=\"srcitems2\">");
//       for (Iterator it = list.iterator(); it.hasNext();) {
//          Entity target = (Entity)it.next();
//          if (target instanceof Indi) {
//             Indi indiRel = (Indi)target;
//             wrapName(out, indiRel);
//             wrapDate(out, indiRel, true);
//             }
//          if (target instanceof Fam) {
//             Fam famRel = (Fam)target;
//             Indi husband = famRel.getHusband();
//             Indi wife    = famRel.getWife();
//             wrapName(out, husband);
//             wrapDate(out, husband, true);
//             out.println(SPACE+"+");
//             wrapName(out, wife);
//             wrapDate(out, wife, true);
//             }
//          out.println("<br />");
//          if (!sourceIsPrivate && report.getDisplaySrcImg() >= SRC_MEDIARELTEXT) {
//             mediasOfEntity.addAll(target.getProperties(PropertyFile.class));
//             for (Iterator itm = mediasOfEntity.iterator(); itm.hasNext();) {
//                PropertyFile file = (PropertyFile)itm.next();
//                if (isUnderSource(file, anchor)) {                        // Add files only for files under same id source
//                   files.add(file);
//                   }
//                }
//             mediasOfEntity.clear();
//             if (!files.isEmpty()) {
//                out.println("</span><span class=\"srcimage0\">");
//                for (Iterator itm = files.iterator(); itm.hasNext();) {
//                   PropertyFile file = (PropertyFile)itm.next();
//                   out.println("<span class=\"srcimage1\">");
//                   wrapMedia(out, dir, file, report.getDisplaySrcImg() >= SRC_MEDIARELMIN);
//                   out.println("</span><span class=\"srcimage2\">"+SPACE+"</span>");
//                   }
//                out.println("</span><br /><span class=\"spacer\">"+SPACE+"</span>");
//                files.clear();
//                out.println("<span class =\"srcitems0\">");
//                }
//             }
//          }
//       out.println("</span><br /><span class=\"spacer\">"+SPACE+"</span>");
//       }
//
//    // Closes the block
//    out.println("</p><div class=\"spacer\">"+SPACE+"</div>");
//    out.println("</div>");
//
//
//    // End of export section details ----------------------------------------------
//  }
//
//  /**
//   * Open body
//   */
//  private void openBody(PrintWriter out) {
//    out.println("<script type=\"text/javascript\">");
//    out.println("<!--");
//    out.println("function popup(sPicURL)");
//    out.println("{");
//    out.println("window.open( \""+POPUP+"?\"+sPicURL, '', 'HEIGHT=100,WIDTH=100,scrollbars=0,toolbar=0,status=0,resizable=0,menubar=0');");
//    out.println("}");
//    out.println("function popup2(sPicURL)");
//    out.println("{");
//    out.println("media = window.open( sPicURL, '', 'WIDTH=500,HEIGHT=400,scrollbars=0,toolbar=0,status=0,resizable=0,menubar=0');");
//    out.println("media.moveTo(self.screen.width/2-250, self.screen.height/2-200);");
//    out.println("}");
//    out.println("//-->");
//    out.println("</script>");
//    }
//
//  /**
//   * Print name with link
//   */
//  private void wrapName(PrintWriter out, Indi indi) {
//     //
//     String id = (indi == null) ? "" : indi.getId();
//     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
//     String personFile = (indi == null) ? "" : ( (personPage == null) ? "" : (String)personPage.get(id));
//     if (indi != null) out.print("<a href=\""+src2indiDir+personFile+'#'+id+"\">");
//     if (wh.isPrivate(indi)) {
//        name = "..., ...";
//        }
//     if (name.compareTo(",") == 0) name = "";
//     out.print(wh.htmlText(name));
//     String sosa = wh.getSosa(indi);
//     if (sosa != null && sosa.length() != 0) {
//        out.println(SPACE+"("+sosa+")");
//        }
//     if (report.getDisplayId() && id != null && id.length() != 0) {
//        out.println(SPACE+"("+id+")");
//        }
//     if (indi != null) out.print("</a>");
//     }
//
//  /**
//   * Print dates of individual
//   */
//  private void wrapDate(PrintWriter out, Indi indi, boolean parenthesis) {
//     //
//     String id = (indi == null) ? "" : indi.getId();
//     PropertyDate bdate = (indi == null) ? null : indi.getBirthDate();
//     PropertyDate ddate = (indi == null) ? null : indi.getDeathDate();
//     String birthdate = (indi == null) || (bdate == null) ? "." : bdate.toString();
//     String deathdate = (indi == null) || (ddate == null) ? "" : " - "+ddate.toString();
//     String date = (birthdate+deathdate).trim();
//     if (wh.isPrivate(indi)) {
//        date = ". - .";
//        }
//     if (date.compareTo(".") != 0) {
//        out.print(SPACE + (parenthesis?"(":"") + wh.htmlText(date) + (parenthesis?")":""));
//        }
//     }
//
//  /**
//   * Print media
//   */
//  private void wrapMedia(PrintWriter out, File dir, PropertyFile file) {
//     wrapMedia(out, dir, file, true);
//     }
//
//  private void wrapMedia(PrintWriter out, File dir, PropertyFile file, boolean dispMin) {
//     //
//     String link = SPACE;
//
//     if ((file == null) || (file.getFile() == null)) return;
//     String title = wh.getTitle(file);
//
//     String origFile = wh.getCleanFileName(file.getValue());
//
//     try {
//        wh.copy(file.getFile().getAbsolutePath(), dir.getAbsolutePath()+File.separator+origFile, !report.getCopySources(), false);
//        } catch (IOException e)
//        {
//          //e.printStackTrace();
//          report.println(e.toString());
//        }
//
//     String thumbPic = "mini_"+origFile;    // this is the miniature picture
//     if (!wh.isImage(file.getFile().getAbsolutePath())) {
//        thumbPic = themeDir+"mednopic.png";
//        link = "<a href=\"javascript:popup2('"+origFile+"')\">";
//        if (dispMin) {
//           link += "<img alt=\""+wh.htmlText(title)+"\" title=\""+wh.htmlText(title)+"\" src=\""+thumbPic+"\" />";
//           }
//        else {
//           link += wh.htmlText(title);
//           }
//        link += "</a><br />";
//        }
//     else {
//        thumbPic = "mini_"+origFile;
//        link = "<a href=\"javascript:popup('"+origFile+"')\">";
//        if (dispMin) {
//           wh.scaleImage(file.getFile().getAbsolutePath(), dir.getAbsolutePath()+File.separator+thumbPic, WIDTH_PICTURES, 0, 100, false);
//           link += "<img alt=\""+wh.htmlText(title)+"\" title=\""+wh.htmlText(title)+"\" src=\""+thumbPic+"\" />";
//           }
//        else {
//           link += wh.htmlText(title);
//           }
//        link += "</a><br />";
//        }
//     out.println(link);
//     if (dispMin) {
//        out.println(wh.htmlText(title));
//        }
//     return;
//     }
//
//
//
//  /**
//   * Calculate pages for section details
//   */
//  private void calcPages(List sources) {
//    String sourcefile = "", fileStr = "";
//    int cpt = 0;
//    for (Iterator it = sources.iterator(); it.hasNext();) {
//      Source src = (Source)it.next();
//      cpt++;
//      sourcefile = section.sectionPrefix + String.format(section.formatNbrs, (cpt/section.nbPerPage)+1) + section.sectionSuffix;
//      if (fileStr.compareTo(sourcefile) != 0) {
//         fileStr = sourcefile;
//         }
//      sourcePage.put(src.getId(), sourcefile);
//      }
//    }
//
//  /**
//   * Provide link to id to outside caller
//   */
//  public String getPageForSource(Source src) {
//     return (src == null ? "" : (String)sourcePage.get(src.getId()));
//     }
//
//  /**
//   * Provide links map to outside caller
//   */
//  public Map getPagesMap() {
//     return sourcePage;
//     }
//
//  /**
//   * Exports page links
//   */
//  private void exportLinks(PrintWriter out, String pagename, int previous, int next, int last) {
//      wh.printLinks(out, pagename,
//                    section.sectionPrefix + String.format(section.formatNbrs, 0) + section.sectionSuffix,            // start
//                    section.sectionPrefix + String.format(section.formatNbrs, previous) + section.sectionSuffix,     // previous
//                    section.sectionPrefix + String.format(section.formatNbrs, next) + section.sectionSuffix,         // next
//                    section.sectionPrefix + String.format(section.formatNbrs, last) + section.sectionSuffix,         // end
//                    section);
//     }
//
// /**
//  * Comparator to sort entities
//  */
//  private Comparator sortEntities = new Comparator() {
//     public int compare(Object o1, Object o2) {
//        if ((o1 == null) && (o2 != null)) return -1;
//        if ((o1 != null) && (o2 == null)) return +1;
//        if ((o1 == null) && (o2 == null)) return 0;
//
//        Entity ent1 = (Entity)o1;
//        Entity ent2 = (Entity)o2;
//        String str1 = "";
//        String str2 = "";
//
//        if (ent1 instanceof Indi) {
//             Indi indi = (Indi)ent1;
//             str1 = (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
//             }
//        if (ent1 instanceof Fam) {
//             Fam famRel = (Fam)ent1;
//             Indi husband = famRel.getHusband();
//             Indi wife    = famRel.getWife();
//             if (husband != null) str1 += (wh.getLastName(husband)+", "+husband.getFirstName()).trim();
//             if (husband != null && wife != null) str1 += " + ";
//             if (wife != null) str1 += (wh.getLastName(wife)+", "+wife.getFirstName()).trim();
//             }
//        if (ent2 instanceof Indi) {
//             Indi indi = (Indi)ent2;
//             str2 = (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
//             }
//        if (ent2 instanceof Fam) {
//             Fam famRel = (Fam)ent2;
//             Indi husband = famRel.getHusband();
//             Indi wife    = famRel.getWife();
//             if (husband != null) str2 += (wh.getLastName(husband)+", "+husband.getFirstName()).trim();
//             if (husband != null && wife != null) str2 += " + ";
//             if (wife != null) str2 += (wh.getLastName(wife)+", "+wife.getFirstName()).trim();
//             }
//
//        return str1.toLowerCase().compareTo(str2.toLowerCase());
//        }
//     };
//
//  /**
//   * Test for (recursive) containment
//   */
//  public boolean isUnderSource(Property in, String srcId) {
//    Property parent = in.getParent();
//    if (parent == null) return false;
//    if (parent.getTag().compareTo("SOUR") == 0) {
//       if (parent.getValue().compareTo("@"+srcId+"@") == 0) return true;
//       else return false;
//       }
//    else {
//       return isUnderSource(parent, srcId);
//       }
//  }


} // End_of_Report
