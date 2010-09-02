package genjfr.app.tools.webbook.creator;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genjfr.app.App;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.util.NbPreferences;

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
    //Meta tags
    private String htmlTitle = "";
    private String owner = "";
    private String author = "";
    private String replyto = "";
    private String keywords = "";
    private String siteDesc = "";
    private String language = "";
    //
    public String themeDir = "theme";
    public String indexFile = "index.html";
    public String styleFile = "style.css";
    public String css = themeDir + SEP + styleFile;
    public String POPUP = "popup.htm";
    public final String DEFPOPUPWIDTH = "400";
    public final String DEFPOPUPLENGTH = "500";
    private final int WIDTH_PICTURES = 200;
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
    //
    public Map<String, String> linkForLetter = new TreeMap<String, String>();       // map is : letter to link
    public Map<String, String> namePage = new TreeMap<String, String>();            // map is : lastname to link
    public Map<String, String> personPage = new TreeMap<String, String>();          // map is : individualdetails to link
    public Map<String, String> sourcePage = new TreeMap<String, String>();          // map is : source to link
    public Map<Integer, String> mediaPage = new TreeMap<Integer, String>();         // map is : media to link
    public Map<String, String> cityPage = new TreeMap<String, String>();            // map is : city to link
    public Map<String, String> dayPage = new TreeMap<String, String>();             // map is : days to link
    //
    public String[] Months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    //
    //
    private String[] events = null;
    private String[] eventsMarr = null;
    private boolean showDate = true;
    private boolean showPlace = true;
    private boolean showAllPlaceJurisdictions = true;

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
        // init meta tags
        htmlTitle = htmlText(wp.param_title);
        siteDesc = ""; // not used for now
        owner = htmlText(wp.param_author);
        author = htmlText(wp.param_author);
        replyto = wp.param_email;
        keywords = getKeywords();
        language = Locale.getDefault().getLanguage();
        //
        wh.log.write(sectionName);
        return;
    }

    /**
     * Init events
     */
    public void initEvents() {
        events = new String[]{// Events
                    "BIRT", "CHR",
                    "DEAT", "BURI", "CREM",
                    "ADOP",
                    "BAPM", "BARM", "BASM", "BLES",
                    "CHRA", "CONF", "FCOM", "ORDN",
                    "NATU", "EMIG", "IMMI",
                    "CENS", "PROB", "WILL",
                    "GRAD", "RETI",
                    "EVEN",
                    // Attributes
                    "CAST", "DSCR", "EDUC", "IDNO", "NATI", "NCHI", "NMR", "OCCU", "PROP", "RELI", "RESI", "SSN", "TITL"
                };

        eventsMarr = new String[]{// Events
                    "ANUL", "CENS", "DIV", "DIVF",
                    "ENGA", "MARR", "MARB", "MARC",
                    "MARL", "MARS",
                    "EVEN"
                };
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
     * HTML header
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
        if (title != null && title.length() != 0) {
            htmlTitle = htmlText(wp.param_title) + SPACE + "-" + SPACE + htmlText(trs(title));
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
        out.println("<meta name=\"keywords\" content=\"" + keywords + "\" />");
        out.println("<meta http-equiv=\"Content-language\" content=\"" + language + "\" />");
        out.println("<meta name=\"author\" content=\"" + author + "\" />");
        out.println("<meta name=\"generator\" content=\"Ancestris\" />");
        out.println("<meta name=\"robots\" content=\"all\" />");
        out.println("<meta name=\"reply-to\" content=\"" + replyto + "\" />");
        out.println("<meta name=\"owner\" content=\"" + owner + "\" />");
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
     * HTML end body
     */
    public void printCloseHTML(PrintWriter out) {

        // Close page
        out.println("<p>" + "<a name=\"bot\"></a>" + SPACE + "</p>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Page links
     */
    public void exportLinks(PrintWriter out, String pagename, int start, int previous, int next, int last) {
        printLinks(out, pagename,
                sectionPrefix + String.format(formatNbrs, start) + sectionSuffix, // start
                sectionPrefix + String.format(formatNbrs, previous) + sectionSuffix, // previous
                sectionPrefix + String.format(formatNbrs, next) + sectionSuffix, // next
                sectionPrefix + String.format(formatNbrs, last) + sectionSuffix, // end
                this);
    }

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
     * Include Popup script
     */
    public void includePopupScript(PrintWriter out) {
        out.println("<script type=\"text/javascript\">");
        out.println("<!--");
        out.println("function popup(sPicURL, w, h)");
        out.println("{");
        out.println("l=screen.width/2-w/2;");
        out.println("t=screen.height/2-h/2;");
        out.println("window.open( sPicURL, '', 'width='+w+', height='+h+', left='+l+', top='+t+', position=0,scrollbars=yes,toolbar=0,status=0,resizable=0,menubar=0');");
        out.println("}");
        out.println("//-->");
        out.println("</script>");
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
        if (!keywords.isEmpty()) {
            return keywords;
        }
        String kw = "Généalogie, Genealogy, ancestors, ancêtres, descendance, famille, family tree";
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
            kw += ", " + volumePerName.get(k);
            cpt++;
            if (cpt > 50) {
                break;
            }
        }
        kw += ", fredwebbook";
        this.keywords = kw;
        return keywords;
    }

    /**
     * Convert string into anchor compatible text to ensure links will work
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
        for (int i = 0; i < charInput.length; i++) {
            strOutput.append(wh.convertChar(charInput[i], true, DEFCHAR));
        }
        return strOutput.toString().toUpperCase();
    }
    /**
     * Display types
     */
    public final boolean DT_NOBREAK = false;
    public final boolean DT_BREAK = true;
    public final int DT_LASTFIRST = 0;
    public final int DT_FIRSTLAST = 1;
    public final int DT_LAST = 2;
    public final boolean DT_NOICON = false;
    public final boolean DT_ICON = true;
    public final boolean DT_NOLINK = false;
    public final boolean DT_LINK = true;
    public final boolean DT_NOSOSA = false;
    public final boolean DT_SOSA = true;
    public final boolean DT_NOID = false;
    public final boolean DT_ID = true;

    /**
     * Wrapper for entity
     */
    public String wrapEntity(Entity ent) {
        return wrapEntity(ent, DT_NOBREAK, DT_LASTFIRST, DT_ICON, DT_LINK, DT_SOSA, DT_ID);
    }

    public String wrapEntity(Entity ent, boolean linebreak, int nameType, boolean icon, boolean link, boolean sosa, boolean dispId) {
        //
        String str = "";

        if (ent instanceof Indi) {
            Indi indi = (Indi) ent;
            if (icon) {
                str += wrapSex(indi) + SPACE;
            }
            str += wrapName(indi, nameType, link, sosa, dispId);
            if (linebreak) {
                str += "<br />";
            }
            str += wrapDate(indi, true);
        }
        if (ent instanceof Fam) {
            Fam fam = (Fam) ent;
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();
            if (icon) {
                str += wrapSex(fam) + SPACE;
            }
            str += wrapName(husband, nameType, link, sosa, dispId);
            str += wrapDate(husband, true);
            str += SPACE + "+" + SPACE;
            if (linebreak) {
                str += "<br />";
            }
            str += wrapName(wife, nameType, link, sosa, dispId);
            str += wrapDate(wife, true);
        }
        return str;
    }

    /**
     * Wrapper for name
     */
    public String wrapName(Indi indi) {
        return wrapName(indi, DT_LASTFIRST, DT_LINK, DT_SOSA, DT_ID);
    }

    public String wrapName(Indi indi, int nameType, boolean link, boolean sosa, boolean dispId) {

        // Eliminate case where indi is null
        if (indi == null) {
            return wp.param_unknown;
        }

        // Returned string
        String str = "";

        // Get id
        String id = indi.getId();

        // Build opening of link
        String personFile = personPage.get(id);
        if (link) {
            str += "<a href=\"" + prefixPersonDetailsDir + personFile + '#' + id + "\">";
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();

        // Build name to display
        String name = "";
        String lastname = "";
        String firstname = "";
        if (wh.isPrivate(indi)) {
            lastname = privDisplay;
            firstname = privDisplay;
        } else {
            lastname = wh.getLastName(indi, DEFCHAR);
            firstname = indi.getFirstName().trim();
        }
        switch (nameType) {
            case DT_LASTFIRST:
                name = lastname + ", " + firstname;
                break;
            case DT_FIRSTLAST:
                name = firstname + " " + lastname;
                break;
            case DT_LAST:
                name = lastname;
                break;
            default:
                name = lastname + ", " + firstname;
        }
        str += htmlText(name);

        // Build sosa
        String sosaNb = "";
        if (sosa) {
            if (wh.isPrivate(indi)) {
                sosaNb = privDisplay;
            } else {
                sosaNb = wh.getSosa(indi);
            }
            if (sosaNb != null && sosaNb.length() != 0) {
                str += SPACE + "(" + sosaNb + ")";
            }
        }

        // Build id
        String idNb = "";
        if (wp.param_dispId.equals("1") && id != null && !id.isEmpty() && dispId) {
            if (wh.isPrivate(indi)) {
                idNb = privDisplay;
            } else {
                idNb = id;
            }
            str += SPACE + "(" + idNb + ")";
        }
        // Close link
        if (link) {
            str += "</a>";
        }

        return str;
    }

    /**
     * Wrapper for dates
     */
    public String wrapDate(Indi indi, boolean parenthesis) {

        // Returned string
        String str = "";

        // Eliminate case where indi is null
        if (indi == null) {
            return "";
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();

        // Get date
        String date = "";
        PropertyDate bdate = indi.getBirthDate();
        PropertyDate ddate = indi.getDeathDate();
        String birthdate = (bdate == null) ? "." : bdate.toString();
        String deathdate = (ddate == null) ? "" : " - " + ddate.toString();
        if (wh.isPrivate(indi)) {
            date = privDisplay;
        } else {
            date = (birthdate + deathdate).trim();
        }
        if (!date.equals(".")) {
            str += SPACE + (parenthesis ? "(" : "") + htmlText(date) + (parenthesis ? ")" : "");
        }
        return str;
    }

    /**
     * Wrapper for sex icon
     */
    public String wrapSex(Indi indi) {

        // Returned string
        String str = "";

        // Get privacy string
        String themeDirLink = buildLinkTheme(this, themeDir);
        String privDisplay = "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";

        // Build sex icon
        int iSex = (indi == null) ? 0 : indi.getSex();
        if (iSex == 1) {
            if (wh.isPrivate(indi)) {
                str = privDisplay;
            } else {
                str = "<img src=\"" + themeDirLink + "m.gif\" alt=\"" + trs("alt_male") + "\" />";
            }
        } else if (iSex == 2) {
            if (wh.isPrivate(indi)) {
                str = privDisplay;
            } else {
                str = "<img src=\"" + themeDirLink + "f.gif\" alt=\"" + trs("alt_female") + "\" />";
            }
        } else {
            if (wh.isPrivate(indi)) {
                str = privDisplay;
            } else {
                str = "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";
            }
        }
        return str;
    }

    public String wrapSex(Fam fam) {

        String themeDirLink = buildLinkTheme(this, themeDir);
        return "<img src=\"" + themeDirLink + "u.gif\" alt=\"" + trs("alt_unknown") + "\" />";
    }

    public String getSexStyle(Indi indi) {

        // Get privacy style
        String privDisplay = "unk";

        // Returned string
        String str = "";

        if (indi.getSex() == 1) {
            if (wh.isPrivate(indi)) {
                str += privDisplay;
            } else {
                str += "hom";
            }
        } else if (indi.getSex() == 2) {
            if (wh.isPrivate(indi)) {
                str += privDisplay;
            } else {
                str += "fem";
            }
        } else {
            if (wh.isPrivate(indi)) {
                str += privDisplay;
            } else {
                str += "unk";
            }
        }
        return str;
    }

    public String wrapEvents(Entity entity, boolean includeFamilies, String from2sourceDir, String from2mediaDir) {

        // Eliminate case where indi is null
        if (entity == null) {
            return wp.param_unknown;
        }

        // Returned string
        String str = "";
        String themeDirLink = buildLinkTheme(this, themeDir);

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();

        // Generate event string
        List<String> listEvents = getEventDetails(entity, from2sourceDir, from2mediaDir);
        if (entity instanceof Indi && includeFamilies) {
            Indi indi = (Indi) entity;
            // Get list of events for all his/her families
            Fam[] families = indi.getFamiliesWhereSpouse();
            for (int i = 0; families != null && i < families.length; i++) {
                Fam family = families[i];
                listEvents.addAll(getEventDetails(family, from2sourceDir, from2mediaDir));
            }
        }
        Collections.sort(listEvents);

        for (Iterator s = listEvents.iterator(); s.hasNext();) {
            String event = (String) s.next();   // [0]date . [1]description . [2]source_id . [3]event_tag . [4]media_id . [5] notes
            String[] eventBits = event.split("\\|", -1);
            // eventIcon
            str += "<img src=\"" + themeDirLink + "ev_" + eventBits[3] + ".png" + "\" alt=\"\" />";
            // eventname : date description
            str += eventBits[1].trim();
            // [source link]
            if (wp.param_media_GeneSources.equals("1") && eventBits[2].length() != 0) {
                str += eventBits[2].trim();
            }
            // [media link]
            if (wp.param_media_GeneMedia.equals("1") && eventBits[4].length() != 0) {
                str += eventBits[4];
            }
            // [note link]
            if (eventBits[5].length() != 0) {
                str += eventBits[5];
            }
            //
            str += "<br />";
        }

        // if the whole entity is private, just replace the whole lot with privacy string
        // (if it is not, individual private events will still show as private)
        if (wh.isPrivate(entity)) {
            return privDisplay + "<br />";
        }
        return str;
    }

    /**
     * Get individual events details
     */
    public List<String> getEventDetails(Entity entity, String from2sourceDir, String from2mediaDir) {

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();

        String ev[] = null;
        if (entity == null) {
            return null;
        }
        if (entity instanceof Indi) {
            ev = events;
        } else if (entity instanceof Fam) {
            ev = eventsMarr;
        } else {
            return null;
        }
        List<String> list = new ArrayList<String>();
        String description = "";
        String date = "";
        for (int i = 0; i < ev.length; i++) {
            Property[] props = entity.getProperties(ev[i]);
            for (int j = 0; j < props.length; j++) {
                // date? (used to sort only)
                Property p = props[j].getProperty("DATE");
                PropertyDate pDate = (p instanceof PropertyDate ? (PropertyDate) p : null);

                if (ev[i].equals("BIRT")) {
                    date = "0-";
                } else if (ev[i].equals("DEAT")) {
                    date = "8-";
                } else if (ev[i].equals("BURI") || ev[i].equals("CREM")) {
                    date = "9-";
                } else {
                    date = "5-";
                }

                if (pDate == null) {
                    date += "-";
                } else {
                    PointInTime pit = null;
                    try {
                        pit = pDate.getStart().getPointInTime(PointInTime.GREGORIAN);
                        date += "";
                        date += pit.getYear();
                        date += pit.getMonth();
                        date += pit.getJulianDay();
                    } catch (GedcomException e) {
                        //e.printStackTrace();
                        //wb.log.write(wb.log.ERROR, "getNameDetails - " + e.getMessage());
                        //wb.log.write(wb.log.ERROR, "getNameDetails - date = " + pDate.getStart());
                        //wb.log.write(wb.log.ERROR, "getNameDetails - entity = " + pDate.getEntity());
                        date += pDate.getStart();
                    }
                }
                // description?
                //   {$t} property tag (doesn't count as matched)
                //   {$T} property name(doesn't count as matched)
                //   {$D} date as fully localized string
                //   {$y} year
                //   {$p} place (city)
                //   {$P} place (all jurisdictions)
                //   {$v} value
                //   {$V} display value
                // format 1 : event name
                String format1 = "<span class=\"gras\"> { $T}: </span>";
                // format 2 : date
                String format2 = (showDate ? "{ $D}" : "");
                // format 3 : description
                String format3 = "{ $V}";
                if (showPlace) {
                    String format = (showAllPlaceJurisdictions ? "{ $P}" : "{ $p}");
                    String juridic = props[j].format(format).trim();
                    if (juridic != null) {
                        format3 += " " + juridic.replaceAll(",", " ");
                    }
                }
                if ("RESI".compareTo(ev[i]) == 0) {
                    Property city = props[j].getProperty(new TagPath(".:ADDR:CITY"));
                    Property ctry = props[j].getProperty(new TagPath(".:ADDR:CTRY"));
                    format3 = " " + ((city == null) ? "" : city.toString() + ", ") + ((ctry == null) ? "" : ctry.toString());
                }
                String format = format1 + format2 + " : " + format3;
                description = props[j].format(format).trim();

                // source?
                String source = "";
                Property[] pSources = props[j].getProperties("SOUR");
                if (pSources != null && pSources.length > 0) {
                    for (int k = 0; k < pSources.length; k++) {
                        if (pSources[k] instanceof PropertySource) {
                            PropertySource pSource = (PropertySource) pSources[k];
                            source += wrapSource(buildLinkTheme(this, themeDir) + "src.gif", pSource, from2sourceDir);
                        }
                    }
                }
                // event tag in lowercase (will be used for image associated with event for instance)
                String event_tag = props[j].getTag().toLowerCase();

                // media?
                String media = "";
                Property[] pMedias = props[j].getProperties("OBJE");
                if (pMedias != null && pMedias.length > 0) {
                    for (int k = 0; k < pMedias.length; k++) {
                        PropertyFile pFile = (PropertyFile) pMedias[k].getProperty("FILE");
                        media += wrapMedia(null, pFile, from2mediaDir, false, false, true, false, buildLinkTheme(this, themeDir) + "media.png", "", false, "OBJE:NOTE", "tooltip");
                    }
                }
                // note?
                String note = "";
                Property[] pNotes = props[j].getProperties("NOTE");
                if (pNotes != null && pNotes.length > 0) {
                    for (int k = 0; k < pNotes.length; k++) {
                        Property pNote = pNotes[k];
                        note += wrapNote(buildLinkTheme(this, themeDir) + "note.png", pNote);
                    }
                }

                // write data (date is used to sort only, description includes the date of the event
                if (wh.isPrivate(props[j])) {
                    list.add(date + "|" + privDisplay + "|" + privDisplay + "|" + privDisplay + "|" + privDisplay + "|" + privDisplay);
                } else {
                    list.add(date + "|" + description + "|" + source + "|" + event_tag + "|" + media + "|" + note);
                }


            }
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Buld source bloc (assuming link to a source entity)
     */
    private String wrapSource(String origFile, PropertySource source, String from2sourceDir) {
        //
        String id = "";
        if (source != null && source.getTargetEntity() != null) {
            id = source.getTargetEntity().getId();
        }
        String link = "";
        String sourceFile = (id == null) ? "" : ((sourcePage == null) ? "" : sourcePage.get(id));
        if (id != null) {
            link = from2sourceDir + sourceFile + '#' + id;
        }
        // display image
        String ret = "<a class=tooltip href=\"" + link + "\">";
        ret += "<img src=\"" + origFile + "\" alt=\"" + id + "\" />";
        ret += "<span>" + htmlText(trs("TXT_src_comment")) + "&nbsp;" + id + "</span></a>";
        return ret;

    }

    /**
     * Buld media bloc (assuming media record included in property)
     * @param dir           : section directory where WebBook is stored
     * @param file          : property file in Gedcom
     * @param from2mediaDir : path to go from current directory of section to media directory of picture
     * @param toBeCopied    : true to copy media from gedcom location to webbook location
     * @param useLink       : true to actually use a link rather than actual copy (works on Linux only)
     * @param displayMin    : true if miniature picture is to be displayed, otherwise only a text title is displayed
     * @param popup         : true if link to popup picture or else to move to media page
     * @param forcedIcon    : forced icon to use
     * @param defaultTitle  : default title in case none is associated with file
     * @param displayTitle  : true if title is to be displayed below icon or miniture
     * @param textPath      : text path to get text for tooltip
     * @param style         : tooltip style
     * @return              : html string to put on the web page
     *                        <a class=[style]
     *                        href='[javascript:popup('filename','DEFPOPUPWIDTH','DEFPOPUPLENGTH')'] | ['../media/media_file'] | [''] >
     *                        [<img
     *                           alt='htmlText(title)'
     *                           title='htmlText(title)'
     *                           src='miniature_pic' />]
     *                        <span>
     *                           <b>title</b><br>
     *                           <i>text</i></span>
     *                        </a><br />
     *
     */
    public String wrapMedia(File dir, PropertyFile file, String from2mediaDir, boolean toBeCopied, boolean useLink,
            boolean displayMin, boolean popup, String forcedIcon, String defaultTitle, boolean displayTitle, String textPath, String style) {

        // Returned string and other variables
        String str = "";
        boolean isFileValid = (file != null) && (file.getFile() != null);
        boolean isImage = isFileValid ? wh.isImage(file.getFile().getAbsolutePath()) : false;
        String miniPrefix = "mini_";

        // Get privacy string and media
        String privDisplay = wh.getPrivDisplay();
        String privMedia = isUnderSource(file) ? "medprivSour.png" : "medprivPic.png";
        boolean isMediaPrivate = wh.isPrivate(file);

        // Build filename
        String filename = wh.getCleanFileName(file.getValue(), DEFCHAR);

        // Copy file if required
        if (isFileValid && toBeCopied) {
            // Copy
            try {
                wh.copy(file.getFile().getAbsolutePath(), dir.getAbsolutePath() + File.separator + filename, useLink, false);
            } catch (IOException e) {
                wb.log.write(wb.log.ERROR, "wrapMedia - " + e.getMessage());
            }
            // Create mini
            if (displayMin && isImage) {
                wh.scaleImage(file.getFile().getAbsolutePath(), dir.getAbsolutePath() + File.separator + miniPrefix + filename, WIDTH_PICTURES, 0, 100, false);
            }
        }

        // Build href link
        String href = "";
        if (isFileValid) {
            if (popup) {
                if (isImage) {
                    href = "\"javascript:popup('" + filename + "','" + wh.getImageSize(file.getFile().getAbsolutePath()) + "')\"";
                } else {
                    href = "\"javascript:popup('" + filename + "','" + DEFPOPUPWIDTH + "','" + DEFPOPUPLENGTH + "')\"";
                }
            } else {
                href = "\"" + from2mediaDir + wb.sectionMedia.getPageForMedia(file) + "\"";
            }
        } else {
            href = "";
        }
        if (isMediaPrivate) {
            href = "";
        }

        // Build title
        String title = "";
        Property pProp = file.getParent().getProperty("TITL");
        if (pProp == null || pProp.getValue().trim().isEmpty()) {
            if (defaultTitle != null && !defaultTitle.trim().isEmpty()) {
                title = defaultTitle;
            } else {
                title = wh.getTitle(file, DEFCHAR);
            }
        } else {
            title = pProp.getValue();
        }
        if (isMediaPrivate) {
            title = privDisplay;
        }

        // Build source image
        String src = "";
        if (forcedIcon.isEmpty()) {
            if (displayMin) {
                if (isFileValid) {
                    if (isImage) {
                        src = from2mediaDir + miniPrefix + filename;
                    } else {
                        src = buildLinkTheme(this, themeDir) + "mednopic.png";
                    }
                } else {
                    src = buildLinkTheme(this, themeDir) + "medno.png";
                }
            }
        } else {
            src = buildLinkTheme(this, themeDir) + forcedIcon;
        }
        if (isMediaPrivate) {
            src = buildLinkTheme(this, themeDir) + privMedia;
        }

        // Build tooltip text, up the file, and under tagPath. TagPath can be SOUR:DATA:TEXT or OBJE:NOTE or etc.
        String text = "";
        Property prop = file.getParent();
        while (prop != null && !(prop instanceof Entity)) {
            Property pText = prop.getProperty(new TagPath(textPath));
            if (pText == null) {
                prop = prop.getParent();
            } else {
                text = pText.getDisplayValue();
                break;
            }
        }
        if (isMediaPrivate) {
            text = privDisplay;
        }

        // Compose final html
        if (!href.isEmpty()) {
            str += "<a class=" + style + " href=" + href + " >";
        } else {
            str += "<a class=" + style + " >";
        }
        if (displayMin) {
            str += "<img alt=\"" + htmlText(title) + "\" title=\"" + htmlText(title) + "\" src=\"" + src + "\" />";
        } else {
            str += htmlText(title);
        }
        str += "<span>";
        if (!title.isEmpty()) {
            str += "<b>" + htmlText(title) + "</b>";
            str += "<br>";
        }
        str += "<i>" + htmlText(text) + "</i>";
        str += "</span>";
        str += "</a>";

        // Add a title line if required
        if (displayTitle) {
            str += "<br />" + htmlText(title) + "<br />";
        }

        return str;
    }

    /**
     * Buld note bloc (assuming note record included in property)
     */
    private String wrapNote(String pictureFile, Property note) {
        // Get text
        String noteText = "<i>" + ((note == null || note.getValue().trim().isEmpty()) ? "" : htmlText(note.getValue())) + "</i>";
        // display note
        String ret = "<a class=tooltip \">";
        ret += "<img src=\"" + pictureFile + "\" />";
        ret += "<span>" + noteText + "</span></a>";
        return ret;
    }

    public String wrapPropertyName(Property prop) {

        if (prop == null) {
            return "";
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();
        if (wh.isPrivate(prop)) {
            return htmlText(privDisplay);
        }

        return htmlText(prop.getPropertyName());
    }

    public String wrapPropertyValue(Property prop) {

        if (prop == null) {
            return "";
        }

        String str = prop.getValue();
        if (prop instanceof PropertyXRef) {
            str = ((PropertyXRef) prop).getTarget().toString();
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();
        if (wh.isPrivate(prop)) {
            return htmlText(privDisplay);
        }

        return htmlText(str);
    }

    public String wrapEventDate(PropertyDate date) {

        String str = trs("place_nodate");
        if (date != null) {
            str = date.toString();
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();
        if (wh.isPrivate(date)) {
            return htmlText(privDisplay);
        }

        return htmlText(str);
    }

    public String wrapString(Property prop, String str) {

        // use prop to detect privacy
        if (str == null) {
            return "";
        }

        // Get privacy string
        String privDisplay = wh.getPrivDisplay();
        if (wh.isPrivate(prop)) {
            return htmlText(privDisplay);
        }

        return htmlText(str);
    }

    /**
     * Test for (recursive) containment
     */
    public boolean isUnderSource(Property prop) {
        Property parent = prop.getParent();
        if (parent == null) {
            return false;
        }
        return parent.getTag().compareTo("SOUR") == 0 ? true : isUnderSource(parent);
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



