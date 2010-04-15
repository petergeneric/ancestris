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
import genj.gedcom.PropertySource;
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
public class WebIndividualsDetails {

  private ReportWebBook report = null;
  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private WebSection sectionList = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private final static String POPUP = "popupemail.htm";
  private static final int IMG_BUFFER_SIZE = 1024;
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];
  private final static TagPath INDI2IMAGES = new TagPath("INDI:OBJE:FILE");
  private final static TagPath FAM2IMAGES  = new TagPath("FAM:OBJE:FILE");
  private int maxImagesPerRecord = 5;
  private int WIDTH_PICTURES = 150;
  private String themeDir = "";

  private Map<String, String> personPage = new TreeMap<String, String>();
  private Map<String, String> sourcePage = new TreeMap<String, String>();
  private WebSources reportSource = null;
  private String indi2srcDir = "";

  private int NB_EVENTS = 7;
  private String[] events  = null;
  private String[] evSymbols = null;
  private String[] eventsMarr  = null;
  private String[] evSymbolsMarr = null;

  private boolean showDate = true;
  private boolean showPlace = true;
  private boolean showSymbols = false;
  private boolean showAllPlaceJurisdictions = true;

  private String fam_grandparents = "";
  private String fam_siblings = "";
  private String fam_family = "";
  private String fam_spouse = "";
  private String fam_kids = "";
  private String fam_stepsibfather = "";
  private String fam_stepsibmother = "";
  private String fam_stepkids = "";
  private String fam_relations = "";
  private String fam_relhas = "";
  private String fam_relis = "";
  private String fam_relunk = "";
  private String fam_relwhois = "";
  private String fam_relat = "";
  private String fam_relthe = "";
  private String fam_relof = "";
  private String fam_note = ""; 
  private String fam_noteSrc = ""; 

  /**
   * Constructor
   */
  public WebIndividualsDetails(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section, List indis) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     calcPages(indis);
     }  

  /**
   * Report's entry point
   */
  public void run(List indis, WebSources reportSource, WebSection webSectionSource) {

    initEvents();
    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);

    themeDir = wh.buildLinkTheme(section, report.getThemeDir());

    this.reportSource = reportSource;
    if (reportSource != null) {
       sourcePage = reportSource.getPagesMap();
       indi2srcDir = wh.buildLinkShort(section, webSectionSource);
       }

    if (report.getDisplayEmail()) {
       wh.createPopupEmail(wh.getFileForName(dir, POPUP));
       report.println(POPUP+" - Done.");
       }

    exportData(dir, indis);

  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, List indis) {

    // Go through individuals
    String fileStr = "";
    File file = null;
    PrintWriter out = null;
    String personfile = "";
    int cpt = 0;
    int nbIndis = indis.size();
    int previousPage = 0,
        currentPage  = 0,
        nextPage     = 0,
        lastPage     = (nbIndis/section.nbPerPage)+1;

    for (Iterator it = indis.iterator(); it.hasNext();) {
      Indi indi = (Indi)it.next();
      cpt++;
      currentPage = (cpt/section.nbPerPage)+1; 
      previousPage = (currentPage == 1) ? 1 : currentPage-1; 
      nextPage = (currentPage == lastPage) ? currentPage : currentPage+1; 
      personfile = section.sectionPrefix + String.format(section.formatNbrs, currentPage) + section.sectionSuffix;
      if (fileStr.compareTo(personfile) != 0) {
         if (out != null) { 
            exportLinks(out, section.sectionPrefix + String.format(section.formatNbrs, currentPage-1) + section.sectionSuffix, Math.max(1,previousPage-1), currentPage == lastPage ? lastPage : nextPage-1, lastPage);
            wh.printCloseHTML(out);
            out.close();
            report.println(fileStr+" - Done.");
            }
         fileStr = personfile;
         file = wh.getFileForName(dir, personfile);
         out = wh.getWriter(file);
         wh.printOpenHTML(out, "Individualsdetails", section);
         writeScript(out);
         }
      exportLinks(out, personfile, previousPage, nextPage, lastPage);
      exportIndividualDetails(out, indi, dir, personfile);
      // .. next individual
     }
    if (out != null) { 
       exportLinks(out, personfile, previousPage, nextPage, lastPage);
       wh.printCloseHTML(out);
       report.println(fileStr+" - Done.");
       }

    // done
    if (out != null) out.close();

  }

  /**
   * Exports individual details
   */
  private void exportIndividualDetails(PrintWriter out, Indi indi, File dir, String personfile) {

    String NODATE = "-"; 

    // Details
    String lastname = wh.getLastName(indi);
    String firstname = indi.getFirstName();
    String anchor = indi.getId();

    Indi father = indi.getBiologicalFather();
    Indi mother = indi.getBiologicalMother();
    Indi fatherfather = (father != null) ? father.getBiologicalFather() : null;
    Indi motherfather = (father != null) ? father.getBiologicalMother() : null;
    Indi fathermother = (mother != null) ? mother.getBiologicalFather() : null;
    Indi mothermother = (mother != null) ? mother.getBiologicalMother() : null;

    String sexString = "?"+SPACE;
    String sexStyle1 = "", sexStyle2 = "";
    if (indi.getSex() == 1) {
       sexString = "<img src=\""+themeDir+"m.gif\" alt=\""+report.translate("alt_male")+"\" />";
       sexStyle1 = "<h2 class=\"hom\">";
       sexStyle2 = "</h2>";
       }
    else if (indi.getSex() == 2) {
       sexString = "<img src=\""+themeDir+"f.gif\" alt=\""+report.translate("alt_female")+"\" />";
       sexStyle1 = "<h2 class=\"fem\">";
       sexStyle2 = "</h2>";
       }
    else {
       sexString = "<img src=\""+themeDir+"u.gif\" alt=\""+report.translate("alt_unknown")+"\" />";
       sexStyle1 = "<h2 class=\"unk\">";
       sexStyle2 = "</h2>";
       }

    // Start of details ----------------------------------------------

    // Individual name 
    out.println(sexStyle1);
    out.println("<a name=\""+anchor+"\"></a>");
    out.println(sexString+SPACE);
    String name = (lastname+", "+firstname).trim();
    if (wh.isPrivate(indi)) {
       name = "..., ...";
       }
    if (name.compareTo(",") == 0) name = ""; 
    out.print(wh.htmlText(name));
    String sosa = wh.getSosa(indi);
    if (sosa != null && sosa.length() != 0) {
       out.println(SPACE+"("+sosa+")");
       }
    out.println(sexStyle2);

    // Events of the individual 
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"parentm\">");
    if (report.getDisplayEmail() && !wh.isPrivate(indi)) {
       String str = report.translate("mail_comment");
       out.println("<a href=\"javascript:popup('"+indi.toString()+"')\"><img src=\""+themeDir+"mail.gif\" alt=\""+str+"\" title=\""+str+"\"/></a><br />");
       }
    List<String> listEvents = getNameDetails(indi, events, evSymbols);
    for (Iterator s = listEvents.iterator(); s.hasNext(); ) {
       String event = (String)s.next();   // date . description . source id
       String[] eventBits = event.split("\\|", -1);
       String link = wrapSource(eventBits[2]);
       if (eventBits[2].length() != 0 && report.getDisplaySourceSec()) {
          out.println(eventBits[1].trim()+SPACE+SPACE+"<a href=\""+link+"\"><img src=\""+themeDir+"src.gif\" alt=\""+eventBits[2]+"\" title=\""+eventBits[2]+"\"/></a><br />");
          } 
       else {
          out.println(eventBits[1].trim()+"<br />");
          }
       }
    out.println("<br />");
    out.println("</p>");


    // Images (only if not private)
    if (!wh.isPrivate(indi)) {
       List<Property> files = new ArrayList<Property>();
       files.addAll(Arrays.asList(indi.getProperties(INDI2IMAGES)));
       Fam[] families = indi.getFamiliesWhereSpouse();
       for (int i=0;i<families.length; i++) {
         Fam family = families[i];
         files.addAll(Arrays.asList(family.getProperties(FAM2IMAGES)));
         }
       if (!files.isEmpty()) out.println("<p class=\"image\">");
       for (Iterator it = files.iterator(); it.hasNext(); ) {
          PropertyFile file = (PropertyFile)it.next();
         if ((file == null) || (file.getFile() == null)) break;
         String origFile = wh.getCleanFileName(file.getValue());
         if (wh.scaleImage(file.getFile().getAbsolutePath(), dir.getAbsolutePath()+File.separator+origFile, WIDTH_PICTURES, 0, 100, false))
            out.println("<img src=\""+origFile+"\" alt=\""+wh.htmlText(name)+"\" />");
         }
       if (!files.isEmpty()) out.println("</p>");
       }
    // end of container
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    // Grand Parents
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"decal\"><span class=\"gras\">"+fam_grandparents+"</span></p>");
    out.println("<p class=\"parentgp\">");
    wrapName(out, fatherfather);
    out.println("<br />");
    wrapDate(out, fatherfather, false);
    out.println("</p>");
    out.println("<p class=\"parentgp\">");
    wrapName(out, motherfather);
    out.println("<br />");
    wrapDate(out, motherfather, false);
    out.println("</p>");
    out.println("<p class=\"parentgp\">");
    wrapName(out, fathermother);
    out.println("<br />");
    wrapDate(out, fathermother, false);
    out.println("</p>");
    out.println("<p class=\"parentgp\">");
    wrapName(out, mothermother);
    out.println("<br />");
    wrapDate(out, mothermother, false);
    out.println("</p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    // Lines
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl2\"></p>");
    out.println("<p class=\"parentgpl3\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl2\"></p>");
    out.println("<p class=\"parentgpl3\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl4\"></p>");
    out.println("<p class=\"parentgpl5\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<p class=\"parentgpl4\"></p>");
    out.println("<p class=\"parentgpl5\"></p>");
    out.println("<p class=\"parentgpl1\"></p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    // Parents
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"parentp\">");
    wrapName(out, father);
    out.println("<br />");
    wrapDate(out, father, false);
    out.println("</p>");
    out.println("<p class=\"parentp\">");
    wrapName(out, mother);
    out.println("<br />");
    wrapDate(out, mother, false);
    out.println("</p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    // Siblings
    out.println("<div class=\"conteneur\">");
    out.println("<p class=\"decal\"><br /><span class=\"gras\">"+fam_siblings+"</span></p>");
    out.println("<p class=\"parents\">");
    Indi[] osiblings = indi.getOlderSiblings();
    Arrays.sort(osiblings, new PropertyComparator("INDI:BIRT:DATE"));
    for (int i=0;i<osiblings.length; i++) {
      Indi osibling = osiblings[i];
      wrapSex (out, osibling);
      wrapName(out, osibling);
      wrapDate(out, osibling, true);
      out.println("<br />");
      }
    out.println("<span class=\"grasplus\">");
    wrapSex (out, indi);
    wrapName(out, indi);
    wrapDate(out, indi, true);
    out.println("</span><br />");
    Indi[] ysiblings = indi.getYoungerSiblings();
    Arrays.sort(ysiblings, new PropertyComparator("INDI:BIRT:DATE"));
    for (int i=0;i<ysiblings.length; i++) {
      Indi ysibling = ysiblings[i];
      wrapSex (out, ysibling);
      wrapName(out, ysibling);
      wrapDate(out, ysibling, true);
      out.println("<br />");
      }
    out.println("<br /></p>");
    out.println("<div class=\"spacer\">"+SPACE+"</div>");
    out.println("</div>");

    // Step sibling by father
    boolean hasStepSibFather = false;
    if (report.getDisplayStepSibling() && father != null) {
         Fam[] stepFamilies = father.getFamiliesWhereSpouse();
         for (int si=0;si<stepFamilies.length; si++) {
           Fam stepFamily = stepFamilies[si];
           Indi spouse = stepFamily.getWife();
           if (spouse != mother) {
              Indi[] stepSiblings = stepFamily.getChildren() ;
              Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
              if (!hasStepSibFather && stepSiblings.length > 0) {
                 hasStepSibFather = true;
                 out.println("<div class=\"conteneur\">");
                 out.println("<p class=\"decal\"><br /><span class=\"gras\">"+fam_stepsibfather+"</span></p>");
                 out.println("<p class=\"parents\">");
                 }
              for (int sj=0;sj<stepSiblings.length; sj++) {
                Indi stepSbling = stepSiblings[sj];
                wrapSex (out, stepSbling);
                wrapName(out, stepSbling);
                wrapDate(out, stepSbling, true);
                out.println("<br />");
                }
              }
           }
         if (hasStepSibFather) {
            out.println("<br /></p>");
            out.println("<div class=\"spacer\">"+SPACE+"</div>");
            out.println("</div>");
           }
         }

    // Step sibling by mother
    boolean hasStepSibMother = false;
    if (report.getDisplayStepSibling() && mother != null) {
         Fam[] stepFamilies = mother.getFamiliesWhereSpouse();
         for (int si=0;si<stepFamilies.length; si++) {
           Fam stepFamily = stepFamilies[si];
           Indi spouse = stepFamily.getHusband();
           if (spouse != father) {
              Indi[] stepSiblings = stepFamily.getChildren() ;
              Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
              if (!hasStepSibMother && stepSiblings.length > 0) {
                 hasStepSibMother = true;
                 out.println("<div class=\"conteneur\">");
                 out.println("<p class=\"decal\"><br /><span class=\"gras\">"+fam_stepsibmother+"</span></p>");
                 out.println("<p class=\"parents\">");
                 }
              for (int sj=0;sj<stepSiblings.length; sj++) {
                Indi stepSbling = stepSiblings[sj];
                wrapSex (out, stepSbling);
                wrapName(out, stepSbling);
                wrapDate(out, stepSbling, true);
                out.println("<br />");
                }
              }
           }
         if (hasStepSibMother) {
            out.println("<br /></p>");
            out.println("<div class=\"spacer\">"+SPACE+"</div>");
            out.println("</div>");
           }
         }

    // Families (spouses and corresponding kids)
    // (note: will need xref for the relations of the weddings XREF later so better do it here)
    List xrefList = indi.getProperties(PropertyXRef.class);
    Fam[] families = indi.getFamiliesWhereSpouse();
    if (!report.getDisplayFamily()) {
       families = null;            // so that families are not displayed by skipping the loop which follows
       }
    for (int i=0;families != null && i<families.length; i++) {
      Fam family = families[i];
      xrefList.addAll(family.getProperties(PropertyXRef.class));
      Indi spouse = family.getHusband();
      if (spouse == indi) {
         spouse = family.getWife();
         }
      out.println("<div class=\"conteneur\">");
      out.println("<p class=\"decal\"><span class=\"gras\">"+fam_family+(families.length > 1 ? " ("+(i+1)+")" : "")+"</span></p>");
      out.println("<p class=\"parentf\"><span class=\"gras\">"+fam_spouse+"</span>:&nbsp;");
      wrapName(out, spouse);
      wrapDate(out, spouse, true);
      out.println("<br />");
      listEvents = getNameDetails(family, eventsMarr, evSymbolsMarr);
      for (Iterator s = listEvents.iterator(); s.hasNext(); ) {
         String event = (String)s.next();   // date . description . source id
         String[] eventBits = event.split("\\|", -1);
         String link = wrapSource(eventBits[2]);
         if (eventBits[2].length() != 0 && report.getDisplaySourceSec()) {
            out.println(eventBits[1].trim()+SPACE+SPACE+"<a href=\""+link+"\"><img src=\""+themeDir+"src.gif\" alt=\""+eventBits[2]+"\" title=\""+eventBits[2]+"\"/></a><br />");
            } 
         else {
            out.println(eventBits[1].trim()+"<br />");
            }
         }

      out.println("</p>");
      Indi[] children = family.getChildren() ;
      Arrays.sort(children, new PropertyComparator("INDI:BIRT:DATE"));
      if (report.getDisplayChildren() && children.length > 0) {
         out.println("<p class=\"parentf\"><span class=\"gras\">"+fam_kids+"</span>:<br /></p>");
         out.println("<p class=\"parentfc\">");
         for (int j=0;j<children.length; j++) {
           Indi child = children[j];
           wrapSex (out, child);
           wrapName(out, child);
           wrapDate(out, child, true);
           out.println("<br />");
           }
         out.println("</p>");
         }
      // get step brothers and sisters by looking for other families 
      if (report.getDisplayStepSibling() && spouse != null) {
         Fam[] stepFamilies = spouse.getFamiliesWhereSpouse();
         for (int si=0;si<stepFamilies.length; si++) {
           Fam stepFamily = stepFamilies[si];
           if (stepFamily != family) {
              Indi[] stepSiblings = stepFamily.getChildren() ;
              Arrays.sort(stepSiblings, new PropertyComparator("INDI:BIRT:DATE"));
              if (stepSiblings.length > 0) {
                 out.println("<p class=\"parentf\"><span class=\"gras\">"+fam_stepkids+"</span>:<br /></p>");
                 out.println("<p class=\"parentfc\">");
                 for (int sj=0;sj<stepSiblings.length; sj++) {
                   Indi stepSbling = stepSiblings[sj];
                   wrapSex (out, stepSbling);
                   wrapName(out, stepSbling);
                   wrapDate(out, stepSbling, true);
                   out.println("<br />");
                   }
                 out.println("</p>");
                 }
              }
           }
         }
      out.println("<br />");
      out.println("<div class=\"spacer\">"+SPACE+"</div>");
      out.println("</div>");
      }

    // Relations
    // (xrefList loaded earlier)
    boolean displayRelation = false;
    if (report.getDisplayRelations() && !wh.isPrivate(indi) && (xrefList.size() > 0)) {
       for (Iterator it = xrefList.iterator(); it.hasNext();) {
          PropertyXRef xref = (PropertyXRef)it.next();
          Entity target = (Entity) xref.getTargetEntity();
          boolean isXref = xref.getTag().compareTo("XREF") == 0; 
          boolean isAsso = xref.getTag().compareTo("ASSO") == 0; 
          if (!isXref && !isAsso) {
             continue;
             }
          if (!displayRelation) {
             out.println("<div class=\"conteneur\">");
             out.println("<p class=\"decal\"><span class=\"gras\">"+fam_relations+"</span></p>");
             out.println("<p class=\"rela2\">");
             displayRelation = true;
             }
          out.println("&bull;"+SPACE);
          if (isXref) {
             out.println(wh.htmlText(fam_relhas));
             Property prop = xref.getTarget().getProperty("RELA");
             String link = (prop == null) ? fam_relunk : prop.toString().toLowerCase();
             String event = xref.getParent().getPropertyName().toLowerCase();
             boolean doubleup = link.indexOf(event) >= 0; 
             PropertyDate date = (PropertyDate)xref.getParent().getProperty("DATE");
             if (!doubleup) {
                out.println(wh.htmlText(link));
                out.println(fam_relat+SPACE+wh.htmlText(event)+",");
                }
             else {
                out.println(wh.htmlText(link)+",");
                }
             if (date != null) {
                out.println(fam_relthe+SPACE+wh.htmlText(date.toString().toLowerCase())+", ");
             }
             out.println(fam_relwhois);
             }
          if (isAsso) {
             out.println(wh.htmlText(fam_relis));
             Property prop = xref.getProperty("RELA");
             String link = (prop == null) ? fam_relunk : prop.toString().toLowerCase();
             String event = xref.getTarget().getParent().getPropertyName().toLowerCase();
             boolean doubleup = link.indexOf(event) >= 0; 
             PropertyDate date = (PropertyDate)xref.getTarget().getParent().getProperty("DATE");
             if (!doubleup) {
                out.println(wh.htmlText(link));
                out.println(fam_relat+SPACE+wh.htmlText(event)+",");
                }
             else {
                out.println(wh.htmlText(link)+",");
                }
             if (date != null) {
                out.println(fam_relthe+SPACE+wh.htmlText(date.toString().toLowerCase())+", ");
             }
             out.println(fam_relof);
             }

          if (target instanceof Indi) {
             Indi indiRel = (Indi)target;
             wrapName(out, indiRel);
             wrapDate(out, indiRel, true);
             } 

          if (target instanceof Fam) {
             Fam famRel = (Fam)target;
             Indi husband = famRel.getHusband();
             Indi wife    = famRel.getWife();
             wrapName(out, husband);
             wrapDate(out, husband, true);
             out.println(SPACE+"+");
             //out.println(wh.htmlText(famRel.getMarriageDate().toString()));
             //out.println(")"+SPACE);
             wrapName(out, wife);
             wrapDate(out, wife, true);
             } 

          out.println("<br />");
          }
       if (displayRelation) {
          out.println("<br />");
          out.println("</p>");
          out.println("<div class=\"spacer\">"+SPACE+"</div>");
          out.println("</div>");
          }
       }

    // Note
    boolean displayNote = false;
    if (report.getDisplayNotes() && !wh.isPrivate(indi)) {
       List notes = getNotes(indi);
       if (notes.size() > 0) {
          for (Iterator it = notes.iterator(); it.hasNext(); ) {
             Property note = (Property)it.next();
             String noteStr = note.toString().trim();
             Property parent = note.getParent();
             String parentTag = wh.htmlText(parent.getPropertyName());
             if (noteStr.length() > 0 && parent.getTag().compareTo("ASSO") != 0) {
                if (!displayNote) {
                   out.println("<div class=\"conteneur\">");
                   out.println("<p class=\"decal\"><span class=\"gras\">"+fam_note+"</span>:</p>");
                   out.println("<p class=\"note\" >");
                   displayNote = true;
                   }
                String parentNote = (parent.getTag().compareTo("SOUR") == 0) ? " "+fam_noteSrc+
                                    " "+wh.htmlText(parent.getParent().getPropertyName().toLowerCase())+":" : ":";
                out.println("<span class=\"undl\">"+ (parent.getTag().compareTo(Gedcom.INDI) == 0 ? "" : parentTag+parentNote) +"</span>");
                out.println("<span class=\"ital\">");
                out.println(wh.htmlText(noteStr));
                out.println("</span><br />");
                }
             }
          if (displayNote) {
             out.println("</p>");
             out.println("<div class=\"spacer\">"+SPACE+"</div>");
             out.println("</div>");
             }
          }
       }

    // End of details ----------------------------------------------
  }

  /**
   * Returns this indi's notes 
   */
  public List getNotes(Indi indi) {
    List notes = new ArrayList(100);
    getPropertiesRecursively((Property)indi, notes);

    if (indi != null) {
       Fam[] families = indi.getFamiliesWhereSpouse();
       for (int i=0;i<families.length; i++) {
          Fam family = families[i];
          getPropertiesRecursively((Property)family, notes);
          }
       }
    return notes;
  }
  
  private void getPropertiesRecursively(Property parent, List notes) {
    List props = Arrays.asList(parent.getProperties());
    for (Iterator it = props.iterator(); it.hasNext(); ) {
       Property child = (Property)it.next();
       if ((child.getTag().compareTo("NOTE") == 0) || (child.getTag().compareTo("TEXT") == 0)) {
         notes.add(child);
         }
       getPropertiesRecursively(child, notes);
       }
  }



  /**
   * Print sex icons of individuals
   */
  private void wrapSex(PrintWriter out, Indi indi) {
     //
     String id = (indi == null) ? "" : indi.getId();
     String sexString = "?"+SPACE;
     int iSex = (indi == null) ? 0 : indi.getSex();
     if (iSex == 1) {
       sexString = "<img src=\""+themeDir+"m.gif\" alt=\""+report.translate("alt_male")+"\" />";
       }
     else if (iSex == 2) {
       sexString = "<img src=\""+themeDir+"f.gif\" alt=\""+report.translate("alt_female")+"\" />";
       }
     else {
       sexString = "<img src=\""+themeDir+"u.gif\" alt=\""+report.translate("alt_unknown")+"\" />";
       }
     out.print(sexString+SPACE);
     }

  /**
   * Print name with link
   */
  private void wrapName(PrintWriter out, Indi indi) {
     //
     String id = (indi == null) ? "" : indi.getId();
     String name = (indi == null) ? report.getBlankIndi() : (wh.getLastName(indi)+", "+indi.getFirstName()).trim();
     String personFile = (indi == null) ? "" : (String)personPage.get(id);
     if (indi != null) out.print("<a href=\""+personFile+'#'+id+"\">");
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
   * Init events
   */
  private void initEvents() {
     events = new String[] { "BIRT", "CHR", "DEAT", "BURI", "OCCU", "RESI" };
     evSymbols = new String[] { 
                   report.getBirthSymbol(),
                   report.getBaptismSymbol(),
                   report.getDeathSymbol(),
                   report.getBurialSymbol(),
                   report.getOccuSymbol(),
                   report.getResiSymbol() };

     eventsMarr = new String[] { "MARR" };
     evSymbolsMarr = new String[] { report.getMarriageSymbol() };

     fam_grandparents     = wh.htmlText(report.translate("fam_grandparents"));
     fam_siblings         = wh.htmlText(report.translate("fam_siblings"));
     fam_family           = wh.htmlText(report.translate("fam_family"));
     fam_spouse           = wh.htmlText(report.translate("fam_spouse"));
     fam_kids             = wh.htmlText(report.translate("fam_kids"));
     fam_stepsibfather    = wh.htmlText(report.translate("fam_stepsibfather"));
     fam_stepsibmother    = wh.htmlText(report.translate("fam_stepsibmother"));
     fam_stepkids         = wh.htmlText(report.translate("fam_stepkids"));
     fam_relations        = wh.htmlText(report.translate("fam_relations"));
     fam_relhas           = wh.htmlText(report.translate("fam_relhas"));
     fam_relis            = wh.htmlText(report.translate("fam_relis"));
     fam_relunk           = wh.htmlText(report.translate("fam_relunk"));
     fam_relwhois         = wh.htmlText(report.translate("fam_relwhois"));
     fam_relat            = wh.htmlText(report.translate("fam_relat"));
     fam_relthe           = wh.htmlText(report.translate("fam_relthe"));
     fam_relof            = wh.htmlText(report.translate("fam_relof"));
     fam_note             = wh.htmlText(report.translate("fam_note"));
     fam_noteSrc          = wh.htmlText(report.translate("fam_noteSrc"));
     }

  /**
   * Calculate pages for individual details
   */
  private void calcPages(List indis) {
    String personfile = "", fileStr = "";
    int cpt = 0;
    for (Iterator it = indis.iterator(); it.hasNext();) {
      Indi indi = (Indi)it.next();
      cpt++;
      personfile = section.sectionPrefix + String.format(section.formatNbrs, (cpt/section.nbPerPage)+1) + section.sectionSuffix;
      if (fileStr.compareTo(personfile) != 0) {
         fileStr = personfile;
         }
      personPage.put(indi.getId(), personfile);
      }
    }

  /**
   * Get individual details
   */
  public List<String> getNameDetails(Entity entity, String ev[], String evS[]) {
    List<String> list = new ArrayList<String>();
    String description = "";
    String date = "";
    for (int i=0; i<ev.length; i++) {
       Property[] props = entity.getProperties(ev[i]);
       for (int j=0; j<props.length; j++) {
          // date?
          Property p = (Property)props[j].getProperty("DATE");
          PropertyDate pDate = (p instanceof PropertyDate ? (PropertyDate)p : null);

          //events = new String[] { "BIRT", "CHR", "DEAT", "BURI", "OCCU", "RESI" };
          if (ev[i] == "BIRT") {
             date = "0-";
             }
          else if (ev[i] == "DEAT") {
             date = "8-";
             }
          else if (ev[i] == "BURI") {
             date = "9-";
             }
          else {
             date = "5-";
             }

          if (pDate == null) {
             date += "";
             }
          else {  
             try {
             date += ""+pDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear() +
               pDate.getStart().getPointInTime(PointInTime.GREGORIAN).getMonth() +
               pDate.getStart().getPointInTime(PointInTime.GREGORIAN).getJulianDay();
                 } catch (GedcomException e) {
                   //e.printStackTrace(); 
                   report.println(e.toString());
                   return list;
                 } 
             }
          // description?
          String format1 = "<span class=\"gras\">" + (showSymbols ? evS[i] : "") + "{ $T}:" + "</span>";
          String format2 = "{ $V}";
          if ("RESI".compareTo(ev[i]) == 0) {
             Property city = props[j].getProperty(new TagPath(".:ADDR:CITY"));
             Property ctry = props[j].getProperty(new TagPath(".:ADDR:CTRY"));
             format2 = " " + ((city == null) ? "" : city.toString()+", ") + ((ctry == null) ? "" : ctry.toString());
             }
          String format3 = (showDate ? "{ $D}" : "");
          String format = format1 + format2 + format3;
          description = props[j].format(format, report.getPrivacyPolicy()).trim();
          if (showPlace) {
             format = (showAllPlaceJurisdictions ? "{ $P}" : "{ $p}");
             String juridic = props[j].format(format, report.getPrivacyPolicy()).trim();
             if (juridic != null) {
                description += " "+juridic.replaceAll(",", " ");;
                }
             }
          // source?
          String source = "";
          Property pSourceProp = (Property)props[j].getProperty("SOUR");
          if (pSourceProp instanceof PropertySource) {
             PropertySource pSource = (PropertySource)pSourceProp;
             if (pSource != null && pSource.getTargetEntity() != null) {
                source = pSource.getTargetEntity().getId();
                }
             }
          list.add(date+"|"+description+"|"+source);
          }
       }
    Collections.sort(list);
    return list;
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
   * Provide link to id to outside caller
   */
  public String getPageForIndi(Indi indi) {
     return (indi == null ? "" : (String)personPage.get(indi.getId()));
     }

  /**
   * Provide links map to outside caller
   */
  public Map getPagesMap() {
     return personPage;
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

  /**
   * Write script for email popup
   */
  private void writeScript(PrintWriter out) {
    out.println("<script type=\"text/javascript\">");
    out.println("<!--");
    out.println("function popup(sText)");
    out.println("{");
    out.println("window.open( \""+POPUP+"?\"+sText, '', 'HEIGHT=650,WIDTH=620,toolbar=0,status=0,menubar=0');");
    out.println("}");
    out.println("//-->");
    out.println("</script>");
    } 



} // End_of_Report
