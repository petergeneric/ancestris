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
import genj.gedcom.Source;
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
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyComparator;

import java.net.URL;
import java.lang.Class;
import java.lang.Integer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Comparator;


/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebMedia {

  private ReportWebBook report = null;
  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private final static String POPUP = "popup.htm";
  private int WIDTH_PICTURES = 200;

  private String themeDir = "";

  private final static String DEFCHAR = "-";
  public enum Letters { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z }
  private Map<String, String> linkForLetter = new TreeMap<String, String>();       // map is : letter to link 

  private Map<Integer, String> mediaPage = new TreeMap<Integer, String>();
  private Map<String, String> personPage = new TreeMap<String, String>();
  private WebIndividualsDetails reportIndi = null;
  private String here2indiDir = "";
  private int nbPhotoPerRow = 3;


  /**
   * Constructor
   */
  public WebMedia(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
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

    List<Entity> entities = new ArrayList<Entity>();
    entities.addAll(gedcom.getEntities(Gedcom.INDI));
    entities.addAll(gedcom.getEntities(Gedcom.FAM));

    // Build list of media, for medias of INDI and FAM but that are not under a SOUR.
    List<Property> medias = new ArrayList<Property>();
    List<Property> mediasOfEntity = new ArrayList<Property>();
    for (Iterator ite = entities.iterator(); ite.hasNext();) {
       Entity ent = (Entity)ite.next();
       mediasOfEntity.addAll(ent.getProperties(PropertyFile.class));
       for (Iterator itm = mediasOfEntity.iterator(); itm.hasNext();) {
         PropertyFile media = (PropertyFile)itm.next();
         if (!isUnderSource(media)) {
            medias.add(media);
            }
         }
       mediasOfEntity.clear();
       }
    Collections.sort(medias, sortEntities);

    calcLetters(medias);
    calcPages(medias);
    exportData(dir, medias);

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, List medias) {

    // Go through items to display and produce corresponding pages
    String fileStr = "";
    File file = null;
    PrintWriter out = null;
    String mediafile = "";
    int cpt = 0;
    int nbMedia = medias.size();
    int previousPage = 0,
        currentPage  = 0,
        nextPage     = 0,
        lastPage     = (nbMedia/section.nbPerPage)+1;

    wh.createPopup(wh.getFileForName(dir, POPUP));
    report.println(POPUP+" - Done.");

    // export first page (page '0' as the index page)
    mediafile = section.sectionPrefix + String.format(section.formatNbrs, 0) + section.sectionSuffix;
    fileStr = mediafile;
    file = wh.getFileForName(dir, mediafile);
    out = wh.getWriter(file);
    wh.printOpenHTML(out, "MediaIndex", section);
    out.println("<p class=\"letters\">");
    out.println("<br /><br />");
    for (Letters l : Letters.values()) {
       if (checkLink(l.toString())) { 
          out.println("<a href=\"#"+l+"\">"+l+"</a>"+SPACE+SPACE);
          }
       else {
          out.println(l+SPACE+SPACE);
          }
       }
    if (checkLink(DEFCHAR)) { 
       out.println("<a href=\"#"+DEFCHAR+"\">"+DEFCHAR+"</a>"+SPACE+SPACE);
       }
    else {
       out.println(DEFCHAR+SPACE+SPACE);
       }
    out.println("<br /><br /></p>");
    exportLinks(out, mediafile, 0, 1, lastPage);

    String file_title = "", file_line = "", href = "", anchor = "";
    char last = ' ';
    for (Iterator it = medias.iterator(); it.hasNext();) {
      PropertyFile media = (PropertyFile)it.next();
      href = getPageForMedia(media);
      file_title = wh.getTitle(media);
      file_line = wh.htmlText(media.getEntity().toString())+SPACE+SPACE+SPACE + ((file_title.length() > 0) ? "("+wh.htmlText(file_title)+")" : "");
      anchor = wh.htmlAnchorText(media.getEntity().toString());
      if (anchor.length() > 0 && Character.toUpperCase(anchor.charAt(0)) != last) {
         last = Character.toUpperCase(anchor.charAt(0));
         String l = String.valueOf(last);
         if (!(String.valueOf(last).matches("[a-zA-Z]"))) l = DEFCHAR;
         anchor = "<p class=\"letter\">"+"<a name=\""+l+"\"></a>"+l+"</p>";
         }
      else {
         anchor = "";
         }
      cpt++;
      out.println(anchor);
      out.println("<div class=\"conteneur\">");
      out.println("<p class=\"medlist\">"+cpt+"</p>");
      out.println("<p class=\"medlisttitle\"><a href=\""+href+"\">"+file_line+"</a></p><br />");
      out.println("<div class=\"spacer\">"+SPACE+"</div>");
      out.println("</div>");
      }
    exportLinks(out, mediafile, 0, 1, lastPage);
    wh.printCloseHTML(out);
    out.close();
    report.println(mediafile+" - Done.");

    // export detailed pages
    cpt = 0;
    out = null;
    for (Iterator it = medias.iterator(); it.hasNext();) {
      PropertyFile media = (PropertyFile)it.next();
      cpt++;
      currentPage = ((cpt-1)/section.nbPerPage)+1; 
      previousPage = (currentPage == 1) ? 1 : currentPage-1; 
      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1; 
      mediafile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
      if (fileStr.compareTo(mediafile) != 0) {
         if (out != null) { 
            closeTable(out);
            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
            wh.printCloseHTML(out);
            out.close();
            report.println(fileStr+" - Done.");
            }
         fileStr = mediafile;
         file = wh.getFileForName(dir, mediafile);
         out = wh.getWriter(file);
         wh.printOpenHTML(out, "Media", section);
         exportLinks(out, mediafile, previousPage, nextPage, lastPage);
         openTable(out);
         }
      exportSectionDetails(out, media, dir, mediafile, cpt);
      // .. next individual
     }
    if (out != null) { 
       closeTable(out);
       exportLinks(out, mediafile, previousPage, nextPage, lastPage);
       wh.printCloseHTML(out);
       report.println(fileStr+" - Done.");
       }

    // done
    if (out != null) out.close();

  }

  /**
   * Open table
   */
  private void openTable(PrintWriter out) {
    out.println("<script type=\"text/javascript\">");
    out.println("<!--");
    out.println("function popup(sPicURL)");
    out.println("{");
    out.println("window.open( \""+POPUP+"?\"+sPicURL, '', 'HEIGHT=100,WIDTH=100,scrollbars=0,toolbar=0,status=0,resizable=0,menubar=0');");
    out.println("}");
    out.println("function popup2(sPicURL)");
    out.println("{");
    out.println("media = window.open( sPicURL, '', 'WIDTH=500,HEIGHT=400,scrollbars=0,toolbar=0,status=0,resizable=0,menubar=0');");
    out.println("media.moveTo(self.screen.width/2-250, self.screen.height/2-200);");
    out.println("}");
    out.println("//-->");
    out.println("</script>");
    out.println("<div><br />");
    out.println("<table class=\"maintable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" >");
    out.println("<tr style=\"height:30px\">");
    out.println("<td style=\"width:30px; height:30px; background:url('"+themeDir+"upleft.png') no-repeat right bottom\">"+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("<td style=\"background:url('"+themeDir+"upbar.png') repeat-x left bottom\"></td>");
    out.println("<td style=\"width:30px; height:30px; background:url('"+themeDir+"upright.png') no-repeat left bottom\">"+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td style=\"background:url('"+themeDir+"leftbar.png') repeat-y right bottom\">"+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("<td>");
    out.println("<table border=\"0\" class=\"thumbnail-table\">");
    } 

  /**
   * Close table
   */
  private void closeTable(PrintWriter out) {
    out.println("</tr>");
    out.println("</table>");
    out.println("</td>");
    out.println("<td style=\"background:url('"+themeDir+"rightbar.png') repeat-y left\">"+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("</tr>");
    out.println("<tr style=\"height:30px\">");
    out.println("<td style=\"width:30px; height:30px; background:url('"+themeDir+"downleft.png') repeat-x right top\">"+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("<td style=\"background:url('"+themeDir+"downbar.png') repeat-x top\"></td>");
    out.println("<td style=\"width:30px; height:30px; background:url('"+themeDir+"downright.png') repeat-x left top\">"+SPACE+SPACE+SPACE+SPACE+SPACE+"</td>");
    out.println("</tr>");
    out.println("</table>");
    out.println("<br /></div>");
    } 

  /**
   * Exports section details
   */
  private void exportSectionDetails(PrintWriter out, PropertyFile media, File dir, String mediafile, int cpt) {
    // 
    // Small pictures are to be retrieved from the already created pictures (by report on individual details)
    // (e.g. ../details/media/individuals/xxx.jpg)
    // Large (or original size) pictures are to be copied from gedcom files
    //
    if (((cpt-1)/nbPhotoPerRow) * nbPhotoPerRow == (cpt-1)) {             // if multiple of nbPhotoPerRow
       if ((cpt-1) != (cpt-1)/section.nbPerPage * section.nbPerPage) {    // if NOT first media of page
          out.println("</tr>");
          }
       out.println("<tr class=\"thumbnail-row\" >");
       }

    Entity target = media.getEntity();
    String title = wh.getTitle(media);
    String thumbPic = "smallpic";
    String origFile =  "origFile";
    String link = SPACE;

    if ((media != null) && (media.getFile() != null) && (!wh.isPrivate(target))) {    // file tag is filled in

       origFile = wh.getCleanFileName(media.getValue());
       try { // copy locally (link or file itself)
          wh.copy(media.getFile().getAbsolutePath(), dir.getAbsolutePath()+File.separator+origFile, !report.getCopyMedia(), false);
          //System.out.println(dir.getAbsolutePath()+File.separator+str);
          } catch (IOException e) 
          { 
            //e.printStackTrace(); 
            report.println(e.toString());
          } 

       thumbPic = here2indiDir+origFile;    // this is the miniature picture
       if (!wh.isImage(media.getFile().getAbsolutePath())) { 
          thumbPic = themeDir+"mednopic.png";
          link = "<a href=\"javascript:popup2('"+origFile+"')\"><img alt=\""+wh.htmlText(target.toString())+"\" title=\""+wh.htmlText(title)+"\" src=\""+thumbPic+"\" /></a><br />";
          }
       else {
          if (media.getPath().toString().compareTo("INDI:OBJE:FILE") != 0 && media.getPath().toString().compareTo("FAM:OBJE:FILE") != 0) {
             thumbPic = "mini_"+origFile;
             wh.scaleImage(media.getFile().getAbsolutePath(), dir.getAbsolutePath()+File.separator+thumbPic, WIDTH_PICTURES, 0, 100, false);
             }
          link = "<a href=\"javascript:popup('"+origFile+"')\"><img alt=\""+wh.htmlText(target.toString())+"\" title=\""+wh.htmlText(title)+"\" src=\""+thumbPic+"\" /></a><br />";
          }
       }
    else if (wh.isPrivate(target)) { 
       link = "<img alt=\""+wh.htmlText(report.translate("med_priv"))+"\" title=\""+wh.htmlText(report.translate("med_priv"))+"\" src=\""+themeDir+"medpriv.png\" /><br />";
       }
    else {
       link = "<img alt=\""+wh.htmlText(target.toString())+"\" title=\""+wh.htmlText(report.translate("med_none"))+"\" src=\""+themeDir+"medno.png\" /><br />";
       }
    out.println("<td class=\"thumbnail-col\" ><a name=\""+media.hashCode()+"\"></a>"+link);
    if (title != null && title.length() != 0) {
       if (wh.isPrivate(target)) {
          out.println(wh.htmlText(report.translate("med_priv"))+"<br />");
          }
       else {
          out.println(wh.htmlText(title)+"<br />");
          }
       }
    if (target instanceof Indi) {
       Indi indiRel = (Indi)target;
       wrapName(out, indiRel);
       out.println("<br />");
       wrapDate(out, indiRel, true);
       } 
    if (target instanceof Fam) {
       Fam famRel = (Fam)target;
       Indi husband = famRel.getHusband();
       Indi wife    = famRel.getWife();
       wrapName(out, husband);
       wrapDate(out, husband, true);
       out.println(SPACE+"+");
       out.println("<br />");
       wrapName(out, wife);
       wrapDate(out, wife, true);
       } 
    out.println("<br />"+SPACE+"<br />"+SPACE+"</td>");


    // End of export section details ----------------------------------------------
  }

  /**
   * Print name with link
   */
  private void wrapName(PrintWriter out, Indi indi) {
     //
     String id = (indi == null) ? "" : indi.getId();
     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
     String personFile = (indi == null) ? "" : ( (personPage == null) ? "" : (String)personPage.get(id));
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
   * Exports page links
   */
  private void exportLinks(PrintWriter out, String pagename, int previous, int next, int last) {
      wh.printLinks(out, pagename, 
                    section.sectionPrefix + String.format(section.formatNbrs, 0) + section.sectionSuffix,            // start 
                    section.sectionPrefix + String.format(section.formatNbrs, previous) + section.sectionSuffix,     // previous 
                    section.sectionPrefix + String.format(section.formatNbrs, next) + section.sectionSuffix,         // next
                    section.sectionPrefix + String.format(section.formatNbrs, last) + section.sectionSuffix,         // end
                    section); 
     }

 /**
  * Comparator to sort entities
  */
  private Comparator sortEntities = new Comparator() {
     public int compare(Object o1, Object o2) {
        if ((o1 == null) && (o2 != null)) return -1; 
        if ((o1 != null) && (o2 == null)) return +1; 
        if ((o1 == null) && (o2 == null)) return 0; 

        Property p1 = (Property)o1;
        Property p2 = (Property)o2;

        String str1 = wh.htmlAnchorText(p1.getEntity().toString());
        String str2 = wh.htmlAnchorText(p2.getEntity().toString());

        if (str1.startsWith(DEFCHAR)) {  
           if (str2.startsWith(DEFCHAR)) {
              return str1.compareTo(str2);
              }
           return +1;   // DEFCHAR will be sorted after [A-Z] 
           } 
        if (str2.startsWith(DEFCHAR)) {  
           return -1;   // DEFCHAR will be sorted after [A-Z] 
           } 
        return str1.compareTo(str2);
        }
     };

  /**
   * Calculate pages for section details
   */
  private void calcPages(List medias) {
    String mediafile = "", fileStr = "";
    int cpt = 0;
    for (Iterator it = medias.iterator(); it.hasNext();) {
      PropertyFile media = (PropertyFile)it.next();
      mediafile = section.sectionPrefix + String.format(section.formatNbrs, (cpt/section.nbPerPage)+1) + section.sectionSuffix + "#" + media.hashCode();
      mediaPage.put(Integer.valueOf(media.hashCode()), mediafile);
      cpt++;
      }
    }

  /**
   * Provide link to id to outside caller
   */
  public String getPageForMedia(PropertyFile media) {
     return (media == null ? "" : (String)mediaPage.get(Integer.valueOf(media.hashCode())));
     }


  /**
   * Test for (recursive) containment
   */
  public boolean isUnderSource(Property in) {
    Property parent = in.getParent();
    if (parent == null) return false;
    return parent.getTag().compareTo("SOUR") == 0 ? true : isUnderSource(parent);
  }


  /**
   * Calculate if there is a link to the letters
   */
  private void calcLetters(List<Property> medias) {

    // Initialise to zero
    linkForLetter.put(DEFCHAR, "0");
    for (Letters l : Letters.values()) {
       linkForLetter.put(l.toString(), "0");
       }

    // Calculate
    char letter = ' ';
    for (Iterator it = medias.iterator(); it.hasNext();) {
      PropertyFile media = (PropertyFile)it.next();
      String str = wh.htmlAnchorText(media.getEntity().toString());
      if (str == null) continue;
      char cLetter = str.charAt(0);
      if (cLetter != letter) {
         letter = cLetter;
         String l = String.valueOf(letter);
         linkForLetter.put(l, "true");      // any string will do 
         }
      }
    }

  /**
   * Booleanise existance of link to a letter
   */
  private boolean checkLink(String str) {
    String flag = linkForLetter.get(str);
    if (flag == null || flag.compareTo("0") == 0) return false;
    return true;
    }




} // End_of_Report
