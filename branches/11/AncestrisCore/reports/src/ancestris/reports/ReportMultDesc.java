package ancestris.reports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import ancestris.gedcom.privacy.PrivacyPolicy;
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyMultilineValue;
import genj.report.Report;
import java.util.HashMap;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * ReportMultDesc
 * 
 */
@ServiceProvider(service=Report.class)
public class ReportMultDesc extends Report {

    // Statistics
    private int nbIndi = 0;
    private int nbFam = 0;
    private int nbLiving = 0;
    private Output output;
    
    // Options definitions
    
    // Private formatting options
    private final static int ONE_LINE = 0, ONE_EVT_PER_LINE = 1, TABLE = 2;
    private final static int ORIENTATION_PORTRAIT = 0, ORIENTATION_LANDSCAPE = 1;
    private final static int FONT_NOSERIF = 0, FONT_SERIF = 1;
    private String fonts[] = { "Helvetica", "Times", "Arial" };
    private static int fontSizes[] = { 4, 6, 8, 10, 12, 14, 16 };
    private static int sectionSizes[] = { Document.FONT_XX_SMALL, Document.FONT_X_SMALL, Document.FONT_SMALL, Document.FONT_MEDIUM,
                                          Document.FONT_LARGE, Document.FONT_X_LARGE, Document.FONT_XX_LARGE };
    private final static String FORMAT_STRONG = "font-weight=bold";
    private final static String FORMAT_UNDERLINE = "text-decoration=underline";

    // Private data options
    private final static int NUM_NONE = 0, NUM_ABBO = 1;

    
    // Displayed options

    // Formatting options
    public FormatOptions formatOptions = new FormatOptions();
    public class FormatOptions {

        // Structure
        public int reportFormat = ONE_LINE;
        public String reportFormats[] = {translate("IndiPerLine"), translate("EventPerLine"), translate("Table")};

        // Orientation
        public int reportOrientation = ORIENTATION_LANDSCAPE;
        public String reportOrientations[] = {translate("OrientationPO"), translate("OrientationLA")};

        // Font type
        public int reportFont = FONT_NOSERIF;
        public String reportFonts[] = {translate("FontNoSerif"), translate("FontSerif"), translate("FontAllChar")};

        // Font size
        public int reportFontSize = 3;
        public String reportFontSizes[] = {translate("FontSizeXXS"), translate("FontSizeXS"), translate("FontSizeS"), translate("FontSizeM"), 
                                           translate("FontSizeL"), translate("FontSizeXL"), translate("FontSizeXXL"), };
    }

    // Numbering Options
    public DataNumberingOptions numberingOptions = new DataNumberingOptions();
    public class DataNumberingOptions {

        // Ids
        public boolean reportIds = true;

        // Daboville
        public int reportNumberScheme = NUM_ABBO;
        public String reportNumberSchemes[] = {translate("NumNone"), translate("NumAbbo")};

    }

    // Generation limitation options
    public DataGenerationsOptions generationOptions = new DataGenerationsOptions();
    public class DataGenerationsOptions {

        // Total limit
        public int reportMaxGenerations = 999;

        // Privacy limit
        public int publicGen = 0;

    }

    // Event data option
    public DataEventOptions eventOptions = new DataEventOptions();
    public class DataEventOptions {

        public boolean reportPlaceOfBirth = true;
        public boolean reportDateOfBirth = true;
        public boolean reportPlaceOfMarriage = true;
        public boolean reportDateOfMarriage = true;
        public boolean reportPlaceOfDeath = true;
        public boolean reportDateOfDeath = true;
        public boolean reportOccu = true;
        public boolean reportPlaceOfOccu = true;
        public boolean reportDateOfOccu = true;
        public boolean reportResi = true;
        public boolean reportPlaceOfResi = true;
        public boolean reportDateOfResi = true;
        public boolean reportMailingAddress = true;
        public boolean showAllPlaceJurisdictions = false;
    }


    
    /**
     * Main for argument individual
     */
    public Object start(Indi indi) {
        return start(new Indi[]{indi}, translate("title.descendant", indi.getName()));
    }

    /**
     * One of the report's entry point
     */
    public Object start(Indi[] indis) {
        return start(indis, getName() + " - " + indis[0].getGedcom().getName());
    }

    /**
     * Our main private report point
     */
    private Document start(Indi[] indis, String title) {

        nbIndi = 0;
        nbFam = 0;
        nbLiving = 0;
        
        switch (formatOptions.reportFormat) {
            case TABLE:
                output = new OutputTable();
                break;
            case ONE_LINE:
            case ONE_EVT_PER_LINE:
                output = new OutputStandard();
                break;
            default:
                throw new IllegalArgumentException("no such report type");
        }
        // keep track of who we looked at already
        HashMap<Fam, String> done = new HashMap<Fam, String>();

        // Init some stuff
        PrivacyPolicy policy = PrivacyPolicy.getDefault();

        Document doc = new Document(title, fonts[formatOptions.reportFont], fontSizes[formatOptions.reportFontSize], Document.FONT_XX_SMALL, sectionSizes[formatOptions.reportFontSize], formatOptions.reportOrientation);

        // iterate into individuals and all its descendants
        for (int i = 0; i < indis.length; i++) {
            Indi indi = indis[i];
            output.title(indi, doc);
            iterate(indi, 1, (new Integer(i + 1).toString()), done, policy, doc);
        }

        output.statistiques(doc);

        // done
        return doc;

    }

    /**
     * Generate descendants information for one individual
     */
    private void iterate(Indi indi, int level, String num, HashMap<Fam, String> done, PrivacyPolicy policy, Document doc) {

        nbIndi++;
        if (indi != null && !indi.isDeceased()) {
            nbLiving++;
        }

        // no more?
        if (level > generationOptions.reportMaxGenerations) {
            return;
        }

        // still in a public generation?
        PrivacyPolicy localPolicy = generationOptions.publicGen == 0 || level < generationOptions.publicGen + 1 ? PrivacyPolicy.getDefault().getAllPublic() : policy;

        output.startIndi(doc);
        format(indi, (Fam) null, level + "-", num, localPolicy, doc);
        Character suffix = 'a';

        // And we loop through its families
        Fam[] fams = indi.getFamiliesWhereSpouse();
        boolean several = fams.length > 1;
        for (int f = 0; f < fams.length; f++) {

            // .. here's the fam and spouse
            Fam fam = fams[f];

            Indi spouse = fam.getOtherSpouse(indi);

            // output the spouse
            output.startSpouse(doc);
            format(spouse, fam, level + "-", num + suffix.toString(), localPolicy, doc);

            // put out a link if we've seen the spouse already
            if (done.containsKey(fam)) {
                output.link(fam, done.get(fam), doc);
            } else {

                output.anchor(fam, doc);
                done.put(fam, num);
                nbIndi++;
                nbFam++;
                if (spouse != null && !spouse.isDeceased()) {
                    nbLiving++;
                }

                // .. and all the kids
                Indi[] children = fam.getChildren();
                for (int c = 0; c < children.length; c++) {
                    // do the recursive step
                    iterate(children[c], level + 1, num + (several ? suffix.toString() : "") + (c + 1), done, policy, doc);

                    // .. next child
                }

            }
            // .. next family
            suffix++;
        }

        // done
        output.endIndi(indi, doc);
    }

    /**
     * resolves the information of one Indi
     */
    private void format(Indi indi, Fam fam, String level, String prefix, PrivacyPolicy policy, Document doc) {

        // Might be null
        if (indi == null) {
            return;
        }

        // FIXME Nils re-enable anchors for individuals processes
        output.number(level, prefix, doc);
        output.name(policy.getDisplayValue(indi, "NAME"), doc);
        if (numberingOptions.reportIds) {
            output.id(indi.getId(), doc);
        }

        String birt = output.format(indi, "BIRT", OPTIONS.getBirthSymbol(), eventOptions.reportDateOfBirth, eventOptions.reportPlaceOfBirth, policy);
        String marr = fam != null ? output.format(fam, "MARR", OPTIONS.getMarriageSymbol(), eventOptions.reportDateOfMarriage, eventOptions.reportPlaceOfMarriage, policy) : "";
        String deat = output.format(indi, "DEAT", OPTIONS.getDeathSymbol(), eventOptions.reportDateOfDeath, eventOptions.reportPlaceOfDeath, policy);
        String occu = eventOptions.reportOccu ? output.format(indi, "OCCU", "", eventOptions.reportDateOfOccu, eventOptions.reportPlaceOfOccu, policy) : "";  // {$T} for symbol
        String resi = eventOptions.reportResi ? output.format(indi, "RESI", "", eventOptions.reportDateOfResi, eventOptions.reportPlaceOfResi, policy) : "" ; // {$T} for symbol
        PropertyMultilineValue addr = eventOptions.reportMailingAddress ? indi.getAddress() : null;
        if (addr != null && policy.isPrivate(addr)) {
            addr = null;
        }

        // dump the information

        output.startEvents(doc);

        String[] infos = new String[]{birt, marr, deat, occu, resi};
        for (int i = 0, j = 0; i < infos.length; i++) {
            output.event(infos[i], doc);
        }
        if (addr != null) {
            output.addressPrefix(doc);
            String[] lines = addr.getLines();
            output.startEvents(doc);
            for (int i = 0; i < lines.length; i++) {
                output.event(lines[i], doc);
            }
            output.endEvents(doc);
        }
        output.endEvents(doc);
        // done
    }


    
    
    
    abstract class Output {

        abstract void title(Indi indi, Document doc);

        abstract void statistiques(Document doc);

        abstract void startIndi(Document doc);

        abstract void startSpouse(Document doc);

        abstract void link(Fam fam, String label, Document doc);

        abstract void anchor(Fam fam, Document doc);

        abstract void endIndi(Indi indi, Document doc);

        abstract void name(String name, Document doc);

        abstract void id(String id, Document doc);

        abstract void startEvents(Document doc);

        abstract void endEvents(Document doc);

        abstract void event(String event, Document doc);

        abstract void number(String level, String num, Document doc);

        abstract void addressPrefix(Document doc);

        /**
         * convert given prefix, date and place switches into a format string
         */
        String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {

            Property prop = e.getProperty(tag);
            if (prop == null) {
                return "";
            }
            
            String fdate = "OCCU".equals(tag) || "RESI".equals(tag) ? "{ $D}" : "{$D}";
            
            String vdate = prop.format("{$v}" + (date ? fdate : ""), policy);
            
            String vplace = prop.format((place && eventOptions.showAllPlaceJurisdictions ? "{ $P}" : "")
                          + (place && !eventOptions.showAllPlaceJurisdictions ? "{ $p}" : ""), policy);
            
            if (vdate.trim().isEmpty() || (prop instanceof PropertyEvent && ((PropertyEvent) prop).isKnownToHaveHappened())) {
                return vplace;
            } else {
                return prefix + vdate + vplace;
            }
        }
    }

    class OutputStandard extends Output {

        private boolean isFirstEvent = true;

        @Override
        void title(Indi indi, Document doc) {
            doc.startSection(translate("title.descendant", indi.getName()));
        }

        @Override
        void statistiques(Document doc) {
            doc.startSection(translate("title.stats"));
            doc.addText(translate("nb.fam", nbFam));
            doc.nextParagraph();
            doc.addText(translate("nb.indi", nbIndi));
            doc.nextParagraph();
            doc.addText(translate("nb.living", nbLiving));
        }

        @Override
        void startIndi(Document doc) {
            doc.startList();
        }

        @Override
        void startSpouse(Document doc) {
        }

        @Override
        void link(Fam fam, String label, Document doc) {
            doc.nextParagraph();
            doc.addText("====> " + translate("see") + " ");
            if (numberingOptions.reportNumberScheme != NUM_NONE) {
                doc.addLink(label, fam.getAnchor());
            } else {
                doc.addLink(fam.getDisplayTitle(numberingOptions.reportIds), fam.getAnchor());
            }
        }

        @Override
        void anchor(Fam fam, Document doc) {
            doc.addAnchor(fam.getAnchor());
        }

        @Override
        void endIndi(Indi indi, Document doc) {
            doc.endList();
        }

        @Override
        void number(String level, String number, Document doc) {
            //FIXME: should be in startindi?
            doc.nextParagraph();
            if (numberingOptions.reportNumberScheme == NUM_NONE) {
                doc.nextListItem("genj:label=" + level);
            } else {
                doc.nextListItem("genj:label=" + level+number);
            }
        }

        @Override
        void name(String name, Document doc) {
            doc.addText(name, FORMAT_STRONG);
        }

        @Override
        void id(String id, Document doc) {
            doc.addText(" (" + id + ") ");
        }

        @Override
        void startEvents(Document doc) {
            if (formatOptions.reportFormat != ONE_LINE) {
                doc.startList();
            }
            isFirstEvent = true;
        }

        @Override
        void endEvents(Document doc) {
            if (formatOptions.reportFormat != ONE_LINE) {
                doc.endList();
            }
        }

        @Override
        void event(String event, Document doc) {
            if (event.length() == 0) {
                return;
            }
            // dump the information
            if (!isFirstEvent) {
                if (formatOptions.reportFormat == ONE_LINE) {
                    doc.addText(" ");
                } else {
                    doc.nextListItem();
                }
            }
            doc.addText(event);
            isFirstEvent = false;
        }

        @Override
        void addressPrefix(Document doc) {
            // dump the information
            if (!isFirstEvent) {
                if (formatOptions.reportFormat == ONE_LINE) {
                    doc.addText(" ");
                } else {
                    doc.nextListItem();
                }
            }
        }
    }

    // Loop through individuals & families
    class OutputTable extends Output {

        @Override
        String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
            return super.format(e, tag, "", date, place, policy);
        }

        @Override
        void title(Indi indi, Document doc) {
            doc.startTable("genj:csv=true");

            doc.nextTableRow();
            /*		  doc.addTableColumn("");
            doc.addTableColumn("");
            doc.addTableColumn("");
            doc.addTableColumn("");
             */
            doc.nextTableCell("number-columns-spanned=7," + FORMAT_STRONG);
            doc.addText(translate("title.descendant", indi.getName()));

            doc.nextTableRow();
            doc.addText(translate("num.col"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("NAME"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("BIRT"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("MARR"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("DEAT"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("OCCU"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(Gedcom.getName("RESI"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(translate("addr1.col"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(translate("addr2.col"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(translate("addr3.col"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(translate("addr4.col"), FORMAT_STRONG);
            doc.nextTableCell();
            doc.addText(translate("addr5.col"), FORMAT_STRONG);
        }

        @Override
        void statistiques(Document doc) {
            doc.startSection(translate("title.stats"));
            doc.addText(translate("nb.fam", nbFam));
            doc.nextParagraph();
            doc.addText(translate("nb.indi", nbIndi));
            doc.nextParagraph();
            doc.addText(translate("nb.living", nbLiving));
        }

        @Override
        void startIndi(Document doc) {
            // format the indi's information
            doc.nextTableRow();
        }

        @Override
        void startSpouse(Document doc) {
            // format the indi's information
            doc.nextTableRow();
        }

        @Override
        void link(Fam fam, String label, Document doc) {
            doc.nextTableRow();
            doc.nextTableCell();
            doc.nextTableCell();
            doc.addText(">==> " + translate("see") + " ");
            if (numberingOptions.reportNumberScheme != NUM_NONE) {
                doc.addText(label);
            } else {
                doc.addText(fam.getDisplayValue());
            }
        }

        @Override
        void anchor(Fam fam, Document doc) {
        }

        @Override
        void endIndi(Indi indi, Document doc) {
        }

        @Override
        void name(String name, Document doc) {
            doc.nextTableCell();
            doc.addText(name, FORMAT_STRONG);
        }

        @Override
        void id(String id, Document doc) {
            doc.addText(" (" + id + ")");
        }

        @Override
        void startEvents(Document doc) {
        }

        @Override
        void endEvents(Document doc) {
        }

        @Override
        void event(String event, Document doc) {
            doc.nextTableCell();
            doc.addText(event);
        }

        @Override
        void number(String level, String num, Document doc) {
            doc.nextTableCell();
            if (numberingOptions.reportNumberScheme == NUM_NONE) {
                doc.addText(level);
            } else {
                doc.addText(level+num);
            }
        }

        @Override
        void addressPrefix(Document doc) {
        }
    }
} // ReportMulDesv

