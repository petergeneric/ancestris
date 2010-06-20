package genjfr.app.tools.webbook.creator;

import genj.gedcom.Indi;
import genjfr.app.tools.webbook.WebBook;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.NbBundle;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSection {

    public boolean toBeGenerated = true;
    public Indi indiDeCujus = null;
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
    public Charset UTF8;
    public String SPACE;
    public String SEP;
    public String DEFCHAR;
    public static final int NB_WORDS = 7;
    private static final int IMG_BUFFER_SIZE = 1024;
    public byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];
    //

    public enum Letters {

        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
    }
    public Map<String, String> linkForLetter = new TreeMap<String, String>();
    public Map<String, String> namePage = new TreeMap<String, String>();            // map is : lastname to link

    /**
     * Constructor
     */
    public WebSection(boolean generate) {
        this.toBeGenerated = generate;
    }

    public void init(WebHelper wh, String sectionName, String sectionDir, String sectionPrefix, String formatNbrs, String sectionSuffix, int firstPage, int nbPerPage) {
        this.wh = wh;
        this.sectionName = sectionName;
        this.sectionDir = sectionDir;
        this.sectionPrefix = sectionPrefix;
        this.formatNbrs = formatNbrs;
        this.sectionSuffix = sectionSuffix;
        this.nbPerPage = nbPerPage;
        this.sectionLink = sectionDir + SEP + sectionPrefix + ((formatNbrs.length() == 0) ? "" : String.format(formatNbrs, firstPage)) + sectionSuffix;
        UTF8 = wh.UTF8;
        SPACE = wh.SPACE;
        SEP = wh.SEP;
        DEFCHAR = wh.DEFCHAR;
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
        System.out.println("DEBUG - nbIndis = " + nbIndis);
        System.out.println("DEBUG - format = " + "%0" + l + "d");
        return "%0" + l + "d";
    }

    /**
     * Translators methods to make it quicker to code
     */
    public String trs(String string) {
        return NbBundle.getMessage(WebBook.class, string);
    }

    public String trs(String string, Object param1) {
        return NbBundle.getMessage(WebBook.class, string, param1);
    }

    public String trs(String string, Object[] arr) {
        return NbBundle.getMessage(WebBook.class, string, arr);
    }
} // End_of_Class



