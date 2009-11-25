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
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.gedcom.MultiLineProperty;

import java.net.URL;
import java.lang.Class;
import java.net.URLClassLoader;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import java.util.Map;
import java.util.TreeMap;
import java.lang.Enum;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebDays {

  private ReportWebBook report = null;
  private Gedcom gedcom = null;
  private Indi indiDeCujus = null;
  private WebHelper wh = null;
  private WebSection section = null;

  private WebSection sectionList = null;

  private final static Charset UTF8 = Charset.forName("UTF-8");
  private final static String SPACE = "&nbsp;";
  private final static String SEP = "/";
  private static final int IMG_BUFFER_SIZE = 1024;
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

  private static final int NB_WORDS = 7;

  public String[] Months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

  /**
   * Constructor
   */
  public WebDays(ReportWebBook report, Gedcom gedcom, Indi indiDeCujus, WebSection section) {
     this.report = report;
     this.gedcom = gedcom;
     this.indiDeCujus = indiDeCujus;
     this.section = section;
     wh = report.getHelper();
     }  

  /**
   * Section's entry point
   */
  public void run() {

    File dir = wh.createDir(report.getDir().getAbsolutePath() + ((section.sectionDir.length() == 0) ? "" : File.separator + section.sectionDir), true);
    File file = wh.getFileForName(dir, section.sectionPrefix+section.sectionSuffix);
    PrintWriter out = wh.getWriter(file);
    sectionList = report.getSection("Daysdetails");

    wh.printOpenHTML(out, "Dayslist", section);
    wh.printHomeLink(out, section);
    exportData(dir, out);
    wh.printCloseHTML(out);

    out.close();
    report.println(section.sectionPrefix+section.sectionSuffix+" - Done.");
  }

  /**
   * Exports data for page
   */
  private void exportData(File dir, PrintWriter out) {

    // Let's pichk a bisextile year and loop on the 366 days of it
    SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
    Calendar calStart = Calendar.getInstance(); 
    calStart.set(2008, 0 , 1);
    Calendar calEnd = Calendar.getInstance(); 
    calEnd.set(2009, 0 , 1);

    Iterator it = wh.getDays(gedcom).iterator();
    String lastMonth = "";
    int cpt = 1, iDays = 1, cptm = 0;
    out.println("<div class=\"daycal\">");

    String dateIt = "";
    if (it.hasNext()) {
       dateIt = it.next().toString();
       } 
    while (calStart.compareTo(calEnd) < 0) {
       String date = sdf.format(calStart.getTime());
       String month = report.translate(Months[Integer.valueOf(date.substring(0,2)) - 1]);
       String day = date.substring(2,4);
       // Close block after every month
       if (month.compareTo(lastMonth) != 0) {
          if (cptm > 0) {
              out.println("</tr></table>");
              out.println("</div>");
              }
          if (cptm > 0 && (cptm/3 * 3) == cptm) {
              out.println("<div class=\"spacer\">"+SPACE+"</div>");
              out.println("</div><div class=\"daycal\">");
              } 
           out.println("<div class=\"daycal1\">");
           out.println("<table class=\"daytbl\">");
           out.println("<tr><td colspan=\""+NB_WORDS+"\"><span class=\"daychar\"><a name=\""+Months[Integer.valueOf(date.substring(0,2)) - 1]+"\"></a>"+month+"</span></td></tr><tr>");
           lastMonth = month;
           cptm++;
           cpt = 1;
           }

        // create link to name file
        String anchor = wh.htmlAnchorText(month) + day;
        String listfile = wh.buildLink(section, sectionList, iDays);
        out.print("<td>");
        if (date.compareTo(dateIt) == 0) {
           out.print("<a href=\""+listfile+'#'+anchor+"\">"+day+"</a>"+SPACE);
           out.print("<span class=\"dayo\">("+wh.getDaysCount(date)+")</span>");
           iDays++;
           if (it.hasNext()) {
              dateIt = it.next().toString();
              } 
           }
        else {
           out.print(day+SPACE);
           }
        out.print("</td>");
        cpt++;

        // Go to the line every NB_WORDS days
        if (cpt > NB_WORDS) {
           out.print("</tr><tr>");
           cpt = 1;
           }

       calStart.add(Calendar.DATE,1);
       }

    out.println("</tr></table>");
    out.println("</div></div>");
    }

} // End_of_Repor