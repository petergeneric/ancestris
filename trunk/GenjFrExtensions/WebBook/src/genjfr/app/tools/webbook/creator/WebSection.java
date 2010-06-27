package genjfr.app.tools.webbook.creator;

import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSection {

    public boolean toBeGenerated = true;
    public WebBook wb = null;
    public WebBookParams wp = null;
    public WebHelper wh = null;
    //
    public int sizeIndiSection = 50;
    //
    public String sectionName;         // e.g. "Individuals of my genealogy"
    public String sectionDir;          // e.g. individuals
    public String sectionPrefix;       // e.g. "persons_"
    public String formatNbrs;          // e.g. "%03d"
    public String sectionSuffix;       // e.g. ".html"
    public int nbPerPage;               // e.g. 50
    public String sectionLink;         // e.g. individuals/persons_001.html
    //
    public final Charset UTF8 = Charset.forName("UTF-8");
    public final String SPACE = "&nbsp;";
    public final String SEP = "/";
    public final String DEFCHAR = "-";
    //
    private String siteDesc = "";
    private String author = "";
    private String keywords = null;
    private String language = Locale.getDefault().getLanguage();
    public String themeDir = "theme";
    public String indexFile = "index.html";
    public String styleFile = "style.css";
    public String css = themeDir + SEP + styleFile;
    //
    public String prefixPersonDetailsDir = "";
    //
    //
    public static final int NB_WORDS = 7;
    private static final int IMG_BUFFER_SIZE = 1024;
    public byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];
    //

    public enum Letters {

        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
    }
    public Map<String, String> linkForLetter = new TreeMap<String, String>();       // map is : letter to link
    public Map<String, String> namePage = new TreeMap<String, String>();            // map is : lastname to link
    public Map<String, String> personPage = new TreeMap<String, String>();          // map is : individualdetails to link
    public Map<String, String> sourcePage = new TreeMap<String, String>();          // map is : source to link

    /**
     * Constructor
     */
    public WebSection(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        this.toBeGenerated = generate;
        this.wb = wb;
        this.wp = wp;
        this.wh = wh;
    }

    public void init(String sectionName, String sectionDir, String sectionPrefix, String formatNbrs, String sectionSuffix, int firstPage, int nbPerPage) {
        this.sectionName = sectionName;
        this.sectionDir = sectionDir;
        this.sectionPrefix = sectionPrefix;
        this.formatNbrs = formatNbrs;
        this.sectionSuffix = sectionSuffix;
        this.nbPerPage = nbPerPage;
        this.sectionLink = sectionDir + SEP + sectionPrefix + ((formatNbrs.length() == 0) ? "" : String.format(formatNbrs, firstPage)) + sectionSuffix;
        keywords = getKeywords();
        return;
    }

    public void create() {
        return;
    }

    public String formatFromSize(int nbIndis) {
        int l = 1;
        if (nbIndis > sizeIndiSection) {
            l = (int) (Math.log10(nbIndis / sizeIndiSection)) + 1;
        }
        return "%0" + l + "d";
    }

    /**
     * Translators methods to make it quicker to code
     */
    public String trs(String string) {
        return wb.log.trs(string);
    }

    public String trs(String string, Object param1) {
        return wb.log.trs(string, param1);
    }

    public String trs(String string, Object param1, Object param2) {
        return wb.log.trs(string, param1, param2);
    }

    public String trs(String string, Object param1, Object param2, Object param3) {
        return wb.log.trs(string, param1, param2, param3);
    }

    public String trs(String string, Object param1, Object param2, Object param3, Object param4) {
        return wb.log.trs(string, param1, param2, param3, param4);
    }

    public String trs(String string, Object[] arr) {
        return wb.log.trs(string, arr);
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
    //USED

    public void printOpenHTML(PrintWriter out, String title, WebSection section) {
        printOpenHTMLHead(out, title, section);
        printOpenHTMLBody(out, title, section);
    }

    public void printOpenHTMLHead(PrintWriter out, String title, WebSection section) {

        // HEAD
        String htmlTitle = htmlText(wp.param_title);
        if (title != null && title.length() != 0) {
            htmlTitle += SPACE + "-" + SPACE + htmlText(trs(title));
        }
        String path = (section == null || section.sectionDir == null) ? "" : ((section.sectionDir.length() == 0) ? "" : "..");

        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"" + language + "\" lang=\"" + language + "\" >");
        out.println("<head>");
        out.println("<title>" + htmlTitle + "</title>");
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />");
        out.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
        out.println("<meta name=\"description\" content=\"" + htmlTitle + " " + siteDesc + "\" />");
        out.println("<meta name=\"keywords\" content=\"" + "fredwebbook " + keywords + "\" />");
        out.println("<meta http-equiv=\"Content-language\" content=\"" + language + "\" />");
        out.println("<meta name=\"author\" content=\"" + author + "\" />");
        out.println("<meta name=\"generator\" content=\"Ancestris\" />");
        out.println("<meta name=\"robots\" content=\"all\" />");
        out.println("<meta name=\"reply-to\" content=\"\" />");
        out.println("<meta name=\"owner\" content=\"" + language + "\" />");
        if (css.length() > 0) {
            String parent = (path == null) || (path.length() == 0) ? "" : path + SEP;
            out.println("<link rel=\"StyleSheet\" href=\"" + parent + css + "\" type=\"text/css\"/>");
        }
    }

    public void printOpenHTMLBody(PrintWriter out, String title, WebSection section) {
        // Close HEAD
        out.println("</head>");
        // BODY
        out.println("<body>");
        if (title != null) {
            String titlePage = "";
            if (title.length() == 0) {
                titlePage = wp.param_title;
            } else {
                titlePage = trs(title);
            }
            out.println("<h1>" + "<a name=\"top\">" + SPACE + "</a>" + htmlText(titlePage) + "</h1>");
        }
        // done
    }

    /**
     * Exports page links
     */
    public void exportLinks(PrintWriter out, String pagename, int start, int previous, int next, int last) {
        printLinks(out, pagename,
                sectionPrefix + String.format(formatNbrs, start) + sectionSuffix, // start
                sectionPrefix + String.format(formatNbrs, previous) + sectionSuffix, // previous
                sectionPrefix + String.format(formatNbrs, next) + sectionSuffix, // next
                sectionPrefix + String.format(formatNbrs, last) + sectionSuffix, // end
                this);
    }


    /**
     * Helper - Writes HTML end header and end body information
     *///USED
    public void printLinks(PrintWriter out, String here, WebSection wsFrom) {
        printLinks(out, here, "", "", "", "", wsFrom);
    }

    public void printLinks(PrintWriter out, String here, String s, String p, String n, String e, WebSection wsFrom) {

        String theme = (wsFrom == null) ? (themeDir + SEP) : buildLinkTheme(wsFrom, themeDir);
        String home = ((wsFrom == null) ? indexFile : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + indexFile);

        out.println("<p class=\"footer\">");
        out.println("<br /><br />");
        out.println("<a href=\"" + here + "#top\"><img src=\"" + theme + "t.gif\" alt=\"" + trs("alt_top") + "\" title=\"" + trs("alt_top") + "\" /></a><br />");
        if (s != null && s.length() > 0) {
            out.println("<a href=\"" + s + "\"><img src=\"" + theme + "s.gif\" alt=\"" + trs("alt_start") + "\" title=\"" + trs("alt_start") + "\" /></a>");
        }
        if (p != null && p.length() > 0) {
            out.println("<a href=\"" + p + "\"><img src=\"" + theme + "p.gif\" alt=\"" + trs("alt_previous") + "\" title=\"" + trs("alt_previous") + "\" /></a>");
        }
        out.println("<a href=\"" + home + "#top\"><img src=\"" + theme + "h.gif\" alt=\"" + trs("alt_home") + "\" title=\"" + trs("alt_home") + "\" /></a>");
        if (n != null && n.length() > 0) {
            out.println("<a href=\"" + n + "\"><img src=\"" + theme + "n.gif\" alt=\"" + trs("alt_next") + "\" title=\"" + trs("alt_next") + "\" /></a>");
        }
        if (e != null && e.length() > 0) {
            out.println("<a href=\"" + e + "\"><img src=\"" + theme + "e.gif\" alt=\"" + trs("alt_end") + "\" title=\"" + trs("alt_end") + "\"  /></a>");
        }
        out.println("<br /><a href=\"" + here + "#bot\"><img src=\"" + theme + "b.gif\" alt=\"" + trs("alt_bottom") + "\" title=\"" + trs("alt_bottom") + "\" /></a>");
        out.println("</p>");
        //out.println("<hr />");

    }

    public void printHomeLink(PrintWriter out, WebSection wsFrom) {

        String theme = (wsFrom == null) ? (themeDir + SEP) : buildLinkTheme(wsFrom, themeDir);
        String home = ((wsFrom == null) ? indexFile : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + indexFile);

        out.println("<p class=\"footer\">");
        out.println("<br /><br />");
        out.println("<a href=\"" + home + "#top\" ><img src=\"" + theme + "h.gif\" alt=\"" + trs("alt_home") + "\" title=\"" + trs("alt_home") + "\"/></a>");
        out.println("</p>");
    }

    public String getHomeLink(WebSection wsFrom) {

        String home = ((wsFrom == null) ? indexFile : ((wsFrom.sectionDir.length() == 0) ? "" : (".." + SEP)) + indexFile);
        return home + "#top";
    }

    /**
     * Helper - Writes HTML end header and end body information
     *///USED
    public void printCloseHTML(PrintWriter out) {

        // Close page
        out.println("<p>" + "<a name=\"bot\"></a>" + SPACE + "</p>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Build link from one section to another
     **///USED
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
        return relPath + wsTo.sectionPrefix + String.format(wsTo.formatNbrs, (nbItem / wsTo.nbPerPage) + 1) + wsTo.sectionSuffix;
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
            relPath = ".." + SEP + themeDir + SEP;
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
     * Generates Spaces
     */
    public void exportSpaces(PrintWriter out, int num) {
        for (int c = 0; c < num; c++) {
            out.print(SPACE);
        }
    }

    /**
     * Helper - Writes HTML table cell information
     */
    public void printCell(PrintWriter out, Object content) {


        // We ask a property for it's value instead of just toString()
        if (content instanceof Property) {
            content = ((Property) content).toString();
        }

        // We don't want to see 'null' but ''
        if (content == null || content.toString().length() == 0) {
            content = SPACE;
        }

        // Here comes the HTML
        out.println("<td>" + content.toString() + "</td>");

    }

    /**
     * Helper - Calculate a url for individual's id
     */
    public String wrapID(Indi indi) {
        StringBuffer result = new StringBuffer();
        result.append("<a name=\"");
        result.append(wh.getLastName(indi, DEFCHAR));
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
        return new File(dir, entity.getId() + ".html");
    }

    /**
     * Convert string into anchor compatible text
     *///USED
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
        for (int i = 0; i < charInput.length; i++) {
            strOutput.append(wh.convertChar(charInput[i], true, DEFCHAR));
        }
        return strOutput.toString().toUpperCase();
    }

    /**
     * Create popup html file
     */
    public void createPopup(File file) {
        PrintWriter out = wh.getWriter(file, UTF8);
        out.println("<html><head><title>" + trs("popup_title") + "</title>");
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
        PrintWriter out = wh.getWriter(file, UTF8);
        out.println("<html><head><title>" + trs("TXT_popupemail_title") + "</title>");
        out.println("<link rel=\"StyleSheet\" href=\"../" + css + "\" type=\"text/css\"/>");
        out.println("<script language='javascript'>");
        out.println("var arrTemp=self.location.href.split(\"?\");");
        out.println("var person = (arrTemp.length>0) ? \": \"+arrTemp[1] : \"\";");
        out.println("</script>");
        out.println("</head>");
        out.println("<body bgcolor=\"#ffffff\" topmargin=\"10\" marginheight=\"10\" leftmargin=\"10\" marginwidth=\"10\">");

        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("TXT_emailform_description")) + "</span></p>");
        out.println("<p class=\"description\">" + htmlText(trs("TXT_emailform_info")) + "</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        out.println("<div class=\"contreport\">");
        out.println("<p class=\"decal\"><br /><span class=\"gras\">" + htmlText(trs("TXT_emailform_form")) + "</span></p>");
        out.println("<p class=\"description\">");
        out.println("  <script language='javascript'>");
        out.println("  document.write( \"<form class='description' action='mailto:" + wp.param_email + "?subject=" + trs("TXT_idx_email_subject") + "\" + person + \"' method='post' enctype='text/plain' >\" );");
        out.println("  </script>");
        out.println("  " + htmlText(trs("TXT_emailform_name")) + ":&nbsp;<input type=\"text\" size=\"60\" name=\"" + htmlText(trs("TXT_emailform_mynameis")) + "\"><br /><br />");
        out.println("  " + htmlText(trs("TXT_emailform_reason")) + ":&nbsp;<input type=\"text\" size=\"60\" name=\"" + htmlText(trs("TXT_emailform_reason")) + "\"><br /><br />");
        out.println("  " + htmlText(trs("TXT_emailform_message")) + ":&nbsp;");
        out.println("  <textarea name=\"" + htmlText(trs("TXT_emailform_message")) + "\" cols=60 rows=14 wrap=virtual></textarea><br /><br />");
        out.println("  <center><input onclick='self.close();' type=\"submit\" value=\"" + htmlText(trs("TXT_emailform_send")) + "\">&nbsp;&nbsp;&nbsp;<input onclick='self.close();' type=\"reset\" value=\"" + htmlText(trs("TXT_emailform_cancel")) + "\"></center>");
        out.println("  </form>");
        out.println("</p>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");

        out.println("</body></html>");
        out.close();
    }

    /**
     * Print name with link
     */
    public void wrapName(PrintWriter out, Indi indi) {
        //
        String id = (indi == null) ? "" : indi.getId();
        String name = (indi == null) ? wp.param_unknown : (wh.getLastName(indi, DEFCHAR) + ", " + indi.getFirstName()).trim();
        String personFile = (indi == null) ? "" : personPage.get(id);
        if (indi != null) {
            out.print("<a href=\"" + prefixPersonDetailsDir + personFile + '#' + id + "\">");
        }
        if (wh.isPrivate(indi)) {
            name = "..., ...";
        }
        if (name.compareTo(",") == 0) {
            name = "";
        }
        out.print(htmlText(name));
        String sosa = wh.getSosa(indi);
        if (sosa != null && sosa.length() != 0) {
            out.println(SPACE + "(" + sosa + ")");
        }
        if (wp.param_dispId.equals("1") && id != null && id.length() != 0) {
            out.println(SPACE + "(" + id + ")");
        }
        if (indi != null) {
            out.print("</a>");
        }
    }

    /**
     * Print dates of individual
     */
    public void wrapDate(PrintWriter out, Indi indi, boolean parenthesis) {
        //
        String id = (indi == null) ? "" : indi.getId();
        PropertyDate bdate = (indi == null) ? null : indi.getBirthDate();
        PropertyDate ddate = (indi == null) ? null : indi.getDeathDate();
        String birthdate = (indi == null) || (bdate == null) ? "." : bdate.toString();
        String deathdate = (indi == null) || (ddate == null) ? "" : " - " + ddate.toString();
        String date = (birthdate + deathdate).trim();
        if (wh.isPrivate(indi)) {
            date = ". - .";
        }
        if (date.compareTo(".") != 0) {
            out.print(SPACE + (parenthesis ? "(" : "") + htmlText(date) + (parenthesis ? ")" : ""));
        }
    }


    /**
     * Comparator to sort Lastnames
     */
    public Comparator sortLastnames = new Comparator() {

        public int compare(Object o1, Object o2) {
            String orig1 = (String) o1;
            String orig2 = (String) o2;
            String name1 = htmlAnchorText((String) o1);
            String name2 = htmlAnchorText((String) o2);
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
     * Comparator to sort Individuals
     */
    @SuppressWarnings("unchecked")
    public Comparator sortIndividuals = new Comparator() {

        public int compare(Object o1, Object o2) {
            Indi indi1 = (Indi) o1;
            Indi indi2 = (Indi) o2;
            int sort = sortLastnames.compare(wh.getLastName(indi1, DEFCHAR), wh.getLastName(indi2, DEFCHAR));
            if (sort == 0) {
                return sortLastnames.compare(indi1.getFirstName(), indi2.getFirstName());
            } else {
                return sort;
            }
        }
    };

    /**
     * Set keywords for meta tag
     */
    @SuppressWarnings("unchecked")
    public String getKeywords() {
        if (keywords != null) {
            return keywords;
        }
        String kw = "";
        List<String> listnames = wh.getLastNames(DEFCHAR, sortLastnames);
        SortedMap<Integer, String> volumePerName = new TreeMap<Integer, String>(new Comparator() {

            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return (i2.compareTo(i1));
            }
        });
        Iterator it = listnames.iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (key.length() > 0) {
                volumePerName.put((Integer) wh.getLastNameCount(key, DEFCHAR), key.trim().toLowerCase());
            }
        }
        int cpt = 0;
        for (it = volumePerName.keySet().iterator(); it.hasNext();) {
            Integer k = (Integer) it.next();
            kw += volumePerName.get(k) + " ";
            cpt++;
            if (cpt > 50) {
                break;
            }
        }
        this.keywords = kw;
        return keywords;
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
    //USED

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
                case '\n':
                    strOutput.append("<br />");
                    break;

                // html tags
                case '<':
                    strOutput.append(convertTags ? "&lt;" : String.valueOf(charInput[i]));
                    break;
                case '>':
                    strOutput.append(convertTags ? "&gt;" : String.valueOf(charInput[i]));
                    break;
                case '&':
                    strOutput.append(convertTags ? "&amp;" : String.valueOf(charInput[i]));
                    break;
                case '"':
                    strOutput.append(convertTags ? "&quot;" : String.valueOf(charInput[i]));
                    break;

                // accented characters
                case '\u00a1':
                    strOutput.append("&iexcl;");
                    break;
                case '\u00a2':
                    strOutput.append("&cent;");
                    break;
                case '\u00a3':
                    strOutput.append("&pound;");
                    break;
                case '\u00a4':
                    strOutput.append("&curren;");
                    break;
                case '\u00a5':
                    strOutput.append("&yen;");
                    break;
                case '\u00a6':
                    strOutput.append("&brvbar;");
                    break;
                case '\u00a7':
                    strOutput.append("&sect;");
                    break;
                case '\u00a8':
                    strOutput.append("&uml;");
                    break;
                case '\u00a9':
                    strOutput.append("&copy;");
                    break;
                case '\u00aa':
                    strOutput.append("&ordf;");
                    break;
                case '\u00ab':
                    strOutput.append("&laquo;");
                    break;
                case '\u00ac':
                    strOutput.append("&not;");
                    break;

                case '\u00ad':
                    strOutput.append("&shy;");
                    break;
                case '\u00ae':
                    strOutput.append("&reg;");
                    break;
                case '\u00af':
                    strOutput.append("&hibar;");
                    break;

                case '\u00b0':
                    strOutput.append("&deg;");
                    break;
                case '\u00b1':
                    strOutput.append("&plusmn;");
                    break;
                case '\u00b2':
                    strOutput.append("&sup2;");
                    break;
                case '\u00b3':
                    strOutput.append("&sup3;");
                    break;
                case '\u00b4':
                    strOutput.append("&acute;");
                    break;
                case '\u00b5':
                    strOutput.append("&micro;");
                    break;
                case '\u00b6':
                    strOutput.append("&para;");
                    break;
                case '\u00b7':
                    strOutput.append("&middot;");
                    break;
                case '\u00b8':
                    strOutput.append("&cedil;");
                    break;
                case '\u00b9':
                    strOutput.append("&sup1;");
                    break;
                case '\u00ba':
                    strOutput.append("&ordm;");
                    break;
                case '\u00bb':
                    strOutput.append("&raquo;");
                    break;
                case '\u00bc':
                    strOutput.append("&frac14;");
                    break;
                case '\u00bd':
                    strOutput.append("&frac12;");
                    break;
                case '\u00be':
                    strOutput.append("&frac34;");
                    break;
                case '\u00bf':
                    strOutput.append("&iquest;");
                    break;

                case '\u00c0':
                    strOutput.append("&Agrave;");
                    break;
                case '\u00c1':
                    strOutput.append("&Aacute;");
                    break;
                case '\u00c2':
                    strOutput.append("&Acirc;");
                    break;
                case '\u00c3':
                    strOutput.append("&Atilde;");
                    break;
                case '\u00c4':
                    strOutput.append("&Auml;");
                    break;
                case '\u00c5':
                    strOutput.append("&Aring;");
                    break;
                case '\u00c6':
                    strOutput.append("&AElig;");
                    break;
                case '\u00c7':
                    strOutput.append("&Ccedil;");
                    break;
                case '\u00c8':
                    strOutput.append("&Egrave;");
                    break;
                case '\u00c9':
                    strOutput.append("&Eacute;");
                    break;
                case '\u00ca':
                    strOutput.append("&Ecirc;");
                    break;
                case '\u00cb':
                    strOutput.append("&Euml;");
                    break;
                case '\u00cc':
                    strOutput.append("&Igrave;");
                    break;
                case '\u00cd':
                    strOutput.append("&Iacute;");
                    break;
                case '\u00ce':
                    strOutput.append("&Icirc;");
                    break;
                case '\u00cf':
                    strOutput.append("&Iuml;");
                    break;

                case '\u00d0':
                    strOutput.append("&ETH;");
                    break;
                case '\u00d1':
                    strOutput.append("&Ntilde;");
                    break;
                case '\u00d2':
                    strOutput.append("&Ograve;");
                    break;
                case '\u00d3':
                    strOutput.append("&Oacute;");
                    break;
                case '\u00d4':
                    strOutput.append("&Ocirc;");
                    break;
                case '\u00d5':
                    strOutput.append("&Otilde;");
                    break;
                case '\u00d6':
                    strOutput.append("&Ouml;");
                    break;
                case '\u00d7':
                    strOutput.append("&times;");
                    break;
                case '\u00d8':
                    strOutput.append("&Oslash;");
                    break;
                case '\u00d9':
                    strOutput.append("&Ugrave;");
                    break;
                case '\u00da':
                    strOutput.append("&Uacute;");
                    break;
                case '\u00db':
                    strOutput.append("&Ucirc;");
                    break;
                case '\u00dc':
                    strOutput.append("&Uuml;");
                    break;
                case '\u00dd':
                    strOutput.append("&Yacute;");
                    break;
                case '\u00de':
                    strOutput.append("&THORN;");
                    break;
                case '\u00df':
                    strOutput.append("&szlig;");
                    break;

                case '\u00e0':
                    strOutput.append("&agrave;");
                    break;
                case '\u00e1':
                    strOutput.append("&aacute;");
                    break;
                case '\u00e2':
                    strOutput.append("&acirc;");
                    break;
                case '\u00e3':
                    strOutput.append("&atilde;");
                    break;
                case '\u00e4':
                    strOutput.append("&auml;");
                    break;
                case '\u00e5':
                    strOutput.append("&aring;");
                    break;
                case '\u00e6':
                    strOutput.append("&aelig;");
                    break;
                case '\u00e7':
                    strOutput.append("&ccedil;");
                    break;
                case '\u00e8':
                    strOutput.append("&egrave;");
                    break;
                case '\u00e9':
                    strOutput.append("&eacute;");
                    break;
                case '\u00ea':
                    strOutput.append("&ecirc;");
                    break;
                case '\u00eb':
                    strOutput.append("&euml;");
                    break;
                case '\u00ec':
                    strOutput.append("&igrave;");
                    break;
                case '\u00ed':
                    strOutput.append("&iacute;");
                    break;
                case '\u00ee':
                    strOutput.append("&icirc;");
                    break;
                case '\u00ef':
                    strOutput.append("&iuml;");
                    break;

                case '\u00f0':
                    strOutput.append("&eth;");
                    break;
                case '\u00f1':
                    strOutput.append("&ntilde;");
                    break;
                case '\u00f2':
                    strOutput.append("&ograve;");
                    break;
                case '\u00f3':
                    strOutput.append("&oacute;");
                    break;
                case '\u00f4':
                    strOutput.append("&ocirc;");
                    break;
                case '\u00f5':
                    strOutput.append("&otilde;");
                    break;
                case '\u00f6':
                    strOutput.append("&ouml;");
                    break;
                case '\u00f7':
                    strOutput.append("&divide;");
                    break;
                case '\u00f8':
                    strOutput.append("&oslash;");
                    break;
                case '\u00f9':
                    strOutput.append("&ugrave;");
                    break;
                case '\u00fa':
                    strOutput.append("&uacute;");
                    break;
                case '\u00fb':
                    strOutput.append("&ucirc;");
                    break;
                case '\u00fc':
                    strOutput.append("&uuml;");
                    break;
                case '\u00fd':
                    strOutput.append("&yacute;");
                    break;
                case '\u00fe':
                    strOutput.append("&thorn;");
                    break;
                case '\u00ff':
                    strOutput.append("&yuml;");
                    break;

                case '\u0152':
                    strOutput.append("&OElig;");
                    break;
                case '\u0153':
                    strOutput.append("&oelig;");
                    break;
                case '\u0160':
                    strOutput.append("&Scaron;");
                    break;
                case '\u0161':
                    strOutput.append("&scaron;");
                    break;
                case '\u0178':
                    strOutput.append("&Yuml;");
                    break;
                case '\u017d':
                    strOutput.append("&Zcaron;");
                    break;
                case '\u017e':
                    strOutput.append("&zcaron;");
                    break;

                case '\u0192':
                    strOutput.append("&fnof;");
                    break;
                case '\u02c6':
                    strOutput.append("&circ;");
                    break;
                case '\u02dc':
                    strOutput.append("&tilde;");
                    break;
                case '\u03a9':
                    strOutput.append("&Omega;");
                    break;
                case '\u03c0':
                    strOutput.append("&pi;");
                    break;
                case '\u2013':
                    strOutput.append("&ndash;");
                    break;
                case '\u2014':
                    strOutput.append("&mdash;");
                    break;
                case '\u2018':
                    strOutput.append("&lsquo;");
                    break;
                case '\u2019':
                    strOutput.append("&rsquo;");
                    break;
                case '\u201a':
                    strOutput.append("&sbaquo;");
                    break;
                case '\u201c':
                    strOutput.append("&ldquo;");
                    break;
                case '\u201d':
                    strOutput.append("&rdquo;");
                    break;
                case '\u201e':
                    strOutput.append("&bdquote;");
                    break;
                case '\u2020':
                    strOutput.append("&dagger;");
                    break;
                case '\u2021':
                    strOutput.append("&Dagger;");
                    break;
                case '\u2022':
                    strOutput.append("&bull;");
                    break;
                case '\u2026':
                    strOutput.append("&hellip;");
                    break;
                case '\u2030':
                    strOutput.append("&permil;");
                    break;
                case '\u2039':
                    strOutput.append("&lsaquo;");
                    break;
                case '\u203a':
                    strOutput.append("&rsaquo;");
                    break;
                case '\u2044':
                    strOutput.append("&frasl;");
                    break;
                case '\u20ac':
                    strOutput.append("&euro;");
                    break;
                case '\u2122':
                    strOutput.append("&trade;");
                    break;
                case '\u2202':
                    strOutput.append("&part;");
                    break;
                case '\u220f':
                    strOutput.append("&prod;");
                    break;
                case '\u2211':
                    strOutput.append("&sum;");
                    break;
                case '\u221a':
                    strOutput.append("&radic;");
                    break;
                case '\u221e':
                    strOutput.append("&infin;");
                    break;
                case '\u222b':
                    strOutput.append("&int;");
                    break;
                case '\u2248':
                    strOutput.append("&asymp;");
                    break;
                case '\u2260':
                    strOutput.append("&ne;");
                    break;
                case '\u2264':
                    strOutput.append("&le;");
                    break;
                case '\u2265':
                    strOutput.append("&ge;");
                    break;
                case '\u25ca':
                    strOutput.append("&loz;");
                    break;

                default:
                    strOutput.append(String.valueOf(charInput[i]));
            }
        }
        return strOutput.toString();
    }
} // End_of_Class



