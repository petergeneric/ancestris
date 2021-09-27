package ancestris.reports.sosa;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
import ancestris.gedcom.privacy.PrivacyPolicy;
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.report.Report;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * ReportSosa
 */
@SuppressWarnings("unchecked")
@ServiceProvider(service = Report.class)
public class ReportSosa extends Report {

    /**
     * option - our report types defined, the value and choices
     */
    private final static int SOSA_REPORT = 0,
            TABLE_REPORT = 1,
            LINEAGE_REPORT = 2,
            AGNATIC_REPORT = 3;
    public int reportType = SOSA_REPORT;
    public String reportTypes[] = {
        translate("SosaReport"),
        translate("TableReport"),
        translate("LineageReport"),
        translate("AgnaticReport")
    };
    /**
     * option - individual per line or event per line
     */
    private final static int ONE_LINE = 0,
            ONE_EVT_PER_LINE = 1;
    public int reportFormat = ONE_LINE;
    public String reportFormats[] = {
        translate("IndiPerLine"),
        translate("EventPerLine")
    };
    public boolean displayBullet = true;
    public boolean showEventName = false;
    public Integer startSosa = 1;
    /**
     * option - number of generations from root considered to be private and to
     * display
     */
    public int privateGen = 0;
    public int reportMinGenerations = 1;
    public int reportMaxGenerations = 999;
    /**
     * option - Events to display
     */
    public boolean reportPlaceOfBirth = true;
    public boolean reportDateOfBirth = true;
    public boolean reportPlaceOfBaptism = true;
    public boolean reportDateOfBaptism = true;
    public boolean reportPlaceOfMarriage = true;
    public boolean reportDateOfMarriage = true;
    public boolean reportPlaceOfDeath = true;
    public boolean reportDateOfDeath = true;
    public boolean reportPlaceOfBurial = true;
    public boolean reportDateOfBurial = true;
    public boolean reportOccu = true;
    public boolean reportPlaceOfOccu = true;
    public boolean reportDateOfOccu = true;
    public boolean reportResi = true;
    public boolean reportPlaceOfResi = true;
    public boolean reportDateOfResi = true;
    public boolean reportIndiNumber = true;
    /**
     * option - Information to display for each event
     */
    public boolean showAllPlaceJurisdictions = false;
    private final static int SRC_NO = 0,
            SRC_TITLE_NO_TEXT = 1,
            SRC_TITLE_GEN_NO_TEXT = 2,
            SRC_TITLE_END_NO_TEXT = 3,
            SRC_TITLE_TEXT_GEN = 4,
            SRC_TITLE_TEXT_END = 5,
            SRC_TITLE_GEN_TEXT_GEN = 6,
            SRC_TITLE_END_TEXT_END = 7;
    public int displaySource = SRC_TITLE_NO_TEXT;
    public String displaySources[] = {
        translate("src_no"), // no source title, no source text anywhere
        translate("src_title_no_text"), // source title with event, no source text anywhere
        translate("src_title_gen_no_text"), // "source" link to title, title at gen, no text
        translate("src_title_end_no_text"), // "source" link, title at end, no source text
        translate("src_title_text_gen"), // "source" link, title with event, text at gen
        translate("src_title_text_end"), // "source" link, title with event, text at end
        translate("src_title_gen_text_gen"),// "source" link, title at gen, text after title
        translate("src_title_end_text_end") // "source" link, title at end, text after title
    };
    public boolean displayEmpty = false;
    public boolean prefixEvent = true;
    public String prefixSource = "Src: ";
    /**
     * Formatting COLORs
     */
    private static String format_one_line = "";
    private static String format_multi_lines = "";
    private final static int COLOR_BLACK = 0,
            COLOR_GREY = 1,
            COLOR_PURPLE = 2,
            COLOR_INDIGO = 3,
            COLOR_BLUE = 4,
            COLOR_GREEN = 5,
            COLOR_YELLOW = 6,
            COLOR_ORANGE = 7,
            COLOR_RED = 8;
    public int srcColor = COLOR_BLUE;
    public String srcColors[] = {
        translate("Black"),
        translate("Grey"),
        translate("Purple"),
        translate("Indigo"),
        translate("Blue"),
        translate("Green"),
        translate("Yellow"),
        translate("Orange"),
        translate("Red")
    };
    /**
     * Globale variables
     */
    // Logic of sources:
    // ----------------
    // If 2 individuals (I34 and I928) are associated for an event to source S21, each one with a note under the source like "abc" for one, and "xyz" for the other; if source title is "title_of_source"; if the text of the source is "text_of_source", and if there is a note to the source "note_of_source", then the following would be displayed for the list of sources:
    // (S21) title_of_source
    // text_of_source
    // note_of_source
    // I34:BIRT: abc
    // I928:DEAT: xyz
    // We will store sources in a global list for display at end of generation or end of report; we will store text and note in a list of strings mapped to a source.
    private final static SortedSet<Source> GLOBAL_SRC_LIST = new TreeSet<>();  // list of source
    private final static Map<Source, List<String>> GLOBAL_SRC_NOTES = new TreeMap<>();  // map of sources to a list of strings
    // Events (BIRT and CHR will be lumped together in terms of options to display)
    String[] events = {"AAA", "BIRT", "CHR", "MARR", "DEAT", "BURI", "OCCU", "RESI"};
    boolean[] dispEv = {true, true, true, true, true, false, false, false};
    String[] symbols = new String[8];
    private boolean srcLinkSrc = false,
            srcLinkGenSrc = false,
            srcTitle = false,
            srcAtGen = false,
            srcAtEnd = false,
            srcTextAtGen = false,
            srcTextAtEnd = false,
            srcDisplay = false;
    private final static String NOTE = ".:NOTE";       // Notes tag in Sources (SOUR and INDI)
    private final static String DATA = ".:DATA:TEXT";  // Data tag in Sources (INDI)

    /**
     * Main for argument individual
     */
    public Document start(Indi indi) {

        // Init some stuff
        PrivacyPolicy policy = PrivacyPolicy.getDefault();
        InitVariables();
        assignColor(srcColor);
//TODO: a reactiver plus tard    if (!getOptionsFromUser(translate("name"), this)) return;
        if (startSosa == 0) {
            Property sosaProp = indi.getProperty(Indi.TAG_SOSA);
            if (sosaProp != null) {
                try {
                    startSosa = Integer.parseInt(sosaProp.getValue(), 10);
                } catch (NumberFormatException e) {
                }
            }
        }
        if (startSosa == 0) {
            startSosa = 1;
        }
        // check recursion type
        Recursion recursion;
        switch (reportType) {
            case AGNATIC_REPORT:
                recursion = new Agnatic();
                break;
            case SOSA_REPORT:
                recursion = new Sosa();
                break;
            case LINEAGE_REPORT:
                recursion = new Lineage();
                break;
            case TABLE_REPORT:
                recursion = new Table();
                break;
            default:
                throw new IllegalArgumentException("no such report type");
        }

        // start with a title in a document
        String title = recursion.getTitle(indi);
        Document doc = new Document(title);
        doc.startSection(title);

        // iterate into individual and all its ascendants
        recursion.start(indi, policy, doc);

        // Done
        return doc;
    }

    /**
     * base type for our rescursion into ancestors - either Sosa, Lineage,
     * Agnatic or Table
     */
    abstract class Recursion {

        /**
         * start the recursion
         */
        abstract void start(Indi indi, PrivacyPolicy policy, Document doc);

        /**
         * title - implement in sub-class
         */
        abstract String getTitle(Indi root);

        /**
         * recursion step for formatting the start of the recursion - implement
         * in sub-classes
         */
        abstract void formatStart(Indi indi, Document doc);

        /**
         * recursion step for formatting an individual - implement in
         * sub-classes
         */
        abstract void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc);

        /**
         * recursion step for formatting the end of the recursion - implement in
         * sub-classes
         */
        abstract void formatEnd(Document doc);

        /**
         * Get description about an entity's event (represented by the tag)
         */
        String getProperty(Entity entity, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
            Property prop = entity.getProperty(tag);
            if (prop == null) {
                return "";
            }
            String format = (showEventName ? prop.getPropertyName() + " " : prefix) + (date ? "{ $D}" : "") + (place && showAllPlaceJurisdictions ? "{ $P}" : "") + (place && !showAllPlaceJurisdictions ? "{ $p}" : "");
            return prop.format(format, policy).trim();
        }

        /**
         * Get source information about an entity's event
         */
        List<Source> getSources(Entity entity, String tagPath, String description) {
            String descStr = entity.toString();
            List<Source> src = new ArrayList<>();
            if (description.length() != 0) {
                descStr += " " + description + " :$#@ ";
            }

            for (Property p : entity.getProperties(new TagPath(tagPath))) {
                if ((p != null) && (p.toString().trim().length() != 0) && (p instanceof PropertySource)) {
                    PropertySource propSrc = (PropertySource) p;
                    Source source = (Source) (propSrc.getTargetEntity());
                    src.add(source);
                    // Add source to global list
                    GLOBAL_SRC_LIST.add(source);
                    // Add individual notes to global notes list:
                    //  1. First get list of notes for this source
                    List<String> listOfNotes = GLOBAL_SRC_NOTES.get(source);
                    //  2. If no list found for this source, create one and initialise it
                    if (listOfNotes == null) {
                        listOfNotes = new ArrayList<>();
                        String sText = source.getText();
                        if ((sText != null) && (sText.trim().length() > 0)) {
                            listOfNotes.add(sText);
                        }
                        String sNote = "";
                        Property sProp = source.getPropertyByPath(NOTE);
                        if (sProp != null) {
                            sNote = sProp.getValue();
                        }
                        if ((sNote != null) && (sNote.trim().length() > 0)) {
                            listOfNotes.add(sNote);
                        }
                        GLOBAL_SRC_NOTES.put(source, listOfNotes);
                    }
                    //  3. Then get individual notes and if it exists, add it to the list
                    // if it is not already in there (for Family entities, both
                    // husband and wife would get it and it would be redundant!)
                    String strNote = "";
                    Property sProp2 = propSrc.getPropertyByPath(NOTE);
                    if (sProp2 != null) {
                        strNote = sProp2.getValue();
                    }
                    if ((strNote != null) && (strNote.trim().length() > 0) && (!isAlreadyIn(listOfNotes, strNote))) {
                        listOfNotes.add(descStr + strNote);
                    }
                    strNote = "";
                    sProp2 = propSrc.getPropertyByPath(DATA);
                    if (sProp2 != null) {
                        strNote = sProp2.getValue();
                    }
                    if ((strNote != null) && (strNote.trim().length() > 0) && (!isAlreadyIn(listOfNotes, strNote))) {
                        listOfNotes.add(descStr + strNote);
                    }
                }
            }
            return src;
        }

        /**
         * dump individual's name
         */
        String getName(Indi indi, int sosa, PrivacyPolicy privacy) {
            if (reportIndiNumber) {
                return (sosa > 0 ? sosa + " " : "") + privacy.getDisplayValue(indi, "NAME") + " (" + indi.getId() + ")";
            } else {
                return (sosa > 0 ? sosa + " " : "") + privacy.getDisplayValue(indi, "NAME");
            }

        }

        /**
         * resolve standard set of properties of an individual
         *
         * @param indi the individual to get properties for
         * @param fam the family to consider as THE spousal family
         * @param privacy privacy policy
         * @param usePrefixes whether to user prefixes in info generation
         * @param returnEmpties whether to return or skip empty values
         */
        void getProperties(Indi indi, Fam fam, PrivacyPolicy privacy, boolean usePrefixes, boolean returnEmpties, Map<String, String> eDesc, Map<String, List<Source>> eSrc) {

            // Variables
            String description;
            List<Source> sources;

            // general?
            int ev = 0;
            if (dispEv[ev]) {
                if (srcDisplay) {
                    sources = getSources(indi, "INDI:SOUR", "");
                    if (displayEmpty || sources.size() > 0) {
                        eSrc.put("AAA", sources);
                    }
                }
            }

            // birth?
            ev = 1;
            String event = "BIRT";
            if (dispEv[ev]) {
                description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfBirth, reportPlaceOfBirth, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (displayEmpty || sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }

            // baptism?
            ev = 2;
            event = "CHR";
            if (dispEv[ev]) {
                description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfBaptism, reportPlaceOfBaptism, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (displayEmpty || sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }

            // marriage?
            ev = 3;
            event = "MARR";
            if (dispEv[ev]) {
                if (fam != null) {
                    String prefix = "";
                    description = getProperty(fam, event, prefix, reportDateOfMarriage, reportPlaceOfMarriage, privacy);
                    if (usePrefixes) {
                        final Indi otherSpouse = fam.getOtherSpouse(indi);
                        String otherSpouseName = "";
                        if (otherSpouse != null) {
                            if (privacy.isPrivate(otherSpouse)) {
                                otherSpouseName = privacy.getPrivateMask();
                            } else {
                                otherSpouseName = otherSpouse.getName();
                            }
                        }
                        prefix = symbols[ev] + " " + otherSpouseName;
                    }
                    if (returnEmpties || description.length() > 0) {
                        eDesc.put(event, prefix + " " + description);
                    }
                    if (srcDisplay) {
                        sources = getSources(fam, "FAM:" + event + ":SOUR", (usePrefixes ? symbols[ev] : "") + " " + description);
                        if (sources.size() > 0) {
                            eSrc.put(event, sources);
                        }
                    }
                }
            }

            // death?
            ev = 4;
            event = "DEAT";
            if (dispEv[ev]) {
                description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfDeath, reportPlaceOfDeath, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay && (reportDateOfDeath || reportPlaceOfDeath)) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }

            // burial?
            ev = 5;
            event = "BURI";
            if (dispEv[ev]) {
                description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfBurial, reportPlaceOfBurial, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay && (reportDateOfBurial || reportPlaceOfBurial)) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }

            // occupation?
            ev = 6;
            event = "OCCU";
            if (reportOccu) {
                description = getProperty(indi, event, (usePrefixes ? symbols[ev] : "") + "{ $V} ", reportDateOfOccu, reportPlaceOfOccu, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }

            // residence?
            ev = 7;
            event = "RESI";
            if (reportResi) {
                description = getProperty(indi, event, (usePrefixes ? symbols[ev] : "") + "{ $V} ", reportDateOfResi, reportPlaceOfResi, privacy);
                if (returnEmpties || description.length() > 0) {
                    eDesc.put(event, description);
                }

                if (srcDisplay) {
                    sources = getSources(indi, "INDI:" + event + ":SOUR", description);
                    if (sources.size() > 0) {
                        eSrc.put(event, sources);
                    }
                }
            }
            // done
        }
    } //Layout

    /**
     * base type for our rescursion into ancestors - either Sosa, Lineage,
     * Agnatic or Table
     */
    abstract class DepthFirst extends Recursion {

        /**
         * start
         */
        @Override
        void start(Indi indi, PrivacyPolicy policy, Document doc) {
            formatStart(indi, doc);
            Fam[] fams = indi.getFamiliesWhereSpouse();
            Fam fam;
            if ((fams != null) && (fams.length > 0)) {
                fam = fams[0];
            } else {
                fam = null;
            }
            recursion(indi, fam, 0, startSosa, policy, doc);
            formatEnd(doc);
        }

        /**
         * each layout iterates over all individuals starting with the root up
         * to the maximum number of generations
         *
         * @param indi the current individual
         * @param fam the family that this individual was pulled out of (null
         * for root)
         * @param gen the current generation
         * @param sosa the sosa index
         * @param policy the privacy policy
         */
        void recursion(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

            // stop here?
            if (gen > reportMaxGenerations) {
                return;
            }

            // let implementation handle individual
            formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.getDefault().getAllPrivate() : PrivacyPolicy.getDefault().getAllPublic(), doc);

            // go one generation up to father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc == null) {
                return;
            }

            Indi father = famc.getHusband();
            Indi mother = famc.getWife();

            if (father == null && mother == null) {
                return;
            }

            // recurse into father
            if (father != null) {
                recursion(father, famc, gen + 1, sosa * 2, policy, doc);
            }

            // recurse into mother
            if (mother != null) {
                recursion(mother, famc, gen + 1, sosa * 2 + 1, policy, doc);
            }

            // done
        }
    } //DepthFirst

    /**
     * a breadth first recursion
     */
    abstract class BreadthFirst extends Recursion {

        /**
         * start
         */
        @Override
        void start(Indi indi, PrivacyPolicy policy, Document doc) {
            formatStart(indi, doc);
            List<Object> list = new ArrayList<>(3);
            list.add(startSosa);
            list.add(indi);
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if ((fams != null) && (fams.length > 0)) {
                list.add(fams[0]);
            } else {
                list.add(null);
            }
            recursion(list, 0, policy, doc);
            formatEnd(doc);
        }

        /**
         * recurse over a generation list up to the maximum number of
         * generations
         *
         * @param generation the current generation (sosa,indi,fam)
         *
         * @param gen the current generation
         * @param policy the privacy policy
         * @param doc the document to fill
         */
        void recursion(List<Object> generation, int gen, PrivacyPolicy policy, Document doc) {

            // stop here?
            if (gen > reportMaxGenerations) {
                return;
            }

            // format this generation
            formatGeneration(gen, doc);

            // report the whole generation from 'left to right'
            List<Object> nextGeneration = new ArrayList<>();
            for (int i = 0; i < generation.size();) {

                // next triplet
                int sosa = ((Integer) generation.get(i++));
                Indi indi = (Indi) generation.get(i++);
                Fam fam = (Fam) generation.get(i++);

                // grab father and mother
                Fam famc = indi.getFamilyWhereBiologicalChild();
                if (famc != null) {
                    Indi father = famc.getHusband();
                    if (father != null) {
                        nextGeneration.add(new Integer(sosa * 2));
                        nextGeneration.add(father);
                        nextGeneration.add(famc);
                    }
                    Indi mother = famc.getWife();
                    if (mother != null) {
                        nextGeneration.add(new Integer(sosa * 2 + 1));
                        nextGeneration.add(mother);
                        nextGeneration.add(famc);
                    }
                }

                // let implementation handle individual
                formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.getDefault().getAllPrivate() : PrivacyPolicy.getDefault().getAllPublic(), doc);
            } // end of scanning generations

            // recurse into next generation
            if ((srcDisplay) && (srcAtGen || srcTextAtGen)) {
                if (gen >= reportMinGenerations - 1) {
                    writeSourceList(doc, gen, srcAtGen, srcTextAtGen);
                } else {
                    GLOBAL_SRC_LIST.clear();
                }
            }
            if (!nextGeneration.isEmpty()) {
                recursion(nextGeneration, gen + 1, policy, doc);
            }

            // done
        }

        /**
         * formatting the begin of a generation - implement in sub-classes
         */
        abstract void formatGeneration(int gen, Document doc);
    } //BreadthFirst

    /**
     * ********************************************************************************
     * The pretties report with breadth first
     *
     * GENERATION 1 1 root GENERATION 2 2 father 3 mother GENERATION 3 4
     * grandfather 1 5 grandmother 1 6 grandfather 2 7 grandfather 2 GENERATION
     * 4 ...
     */
    class Sosa extends BreadthFirst {

        /**
         * our title - simply the column header values
         */
        @Override
        String getTitle(Indi root) {
            return translate("title.sosa", root.getName());
        }

        /**
         * this is called once at the beginning of the recursion - we add our
         * table around it
         */
        @Override
        void formatStart(Indi root, Document doc) {
            // open table first
            doc.startTable("width=100%");
            doc.addTableColumn("");
            doc.addTableColumn("");
        }

        /**
         * called at each generation add a generation info row
         */
        @Override
        void formatGeneration(int gen, Document doc) {
            if (gen < reportMinGenerations - 1) {
                return;
            }
            doc.nextTableRow();
            doc.nextTableCell("color=#ffffff");
            doc.addText(".");
            doc.nextTableRow();
            doc.nextTableCell("number-columns-spanned=2,font-size=18pt,background-color=#f0f0f0,border-after-width=0.5pt");
            doc.addText(translate("Generation") + " " + (gen + 1));
        }

        /**
         * this is called at each recursion step - output table rows
         */
        @Override
        void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

            // Go back if generation too low
            if (gen < reportMinGenerations - 1) {
                return;
            }

            // For each individual, we will store list of events, their descriptions and sources
            Map<String, String> eventDesc = new TreeMap<>();     // Maps event to their descriptions
            Map<String, List<Source>> eventSources = new TreeMap<>();  // Maps event to source list

            // start with a new row
            doc.nextTableRow();

            // a cell with sosa# and name
            doc.addText(getName(indi, sosa, policy)); // [sosa] name (id)

            // then a cell with properies
            getProperties(indi, fam, policy, true, false, eventDesc, eventSources);

            if (eventDesc.size() > 0) {
                doc.nextTableCell();
                writeEvents(doc, gen, eventDesc, eventSources, false);
            }
            // done for now
        }

        /**
         * called at the end of the recursion - end our table
         */
        @Override
        void formatEnd(Document doc) {
            if ((srcDisplay) && (srcAtEnd || srcTextAtEnd)) {
                writeSourceList(doc, -1, srcAtEnd, srcTextAtEnd);
            }
            // close table
            doc.endTable();
        }
    } //Sosa

    /**
     * ********************************************************************************
     * A Lineage recursion goes depth first and generates a nested tree of
     * ancestors and their properties
     *
     * 1 root 2 father 4 grandfather 5 grandmother 3 mother 6 grandfrather 7
     * grandmother ...
     */
    class Lineage extends DepthFirst {

        /**
         * our title
         */
        @Override
        String getTitle(Indi root) {
            return translate("title.lineage", root.getName());
        }

        /**
         * start formatting
         */
        @Override
        void formatStart(Indi indi, Document doc) {
            // noop
        }

        /**
         * how we format an individual
         */
        @Override
        void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

            if (gen < reportMinGenerations - 1) {
                return;
            }

            // dump the indi's name
            doc.nextParagraph("space-after=10pt,space-before=10pt,start-indent=" + (gen * 20) + "pt");
            doc.addText(getName(indi, sosa, policy) + " ", "font-weight=bold");
            doc.nextParagraph("start-indent=" + (gen * 20 + 10) + "pt");

            // dump its properties
            // For each individual, we will store list of events, their descriptions and sources
            Map<String, String> eventDesc = new TreeMap<>();     // Maps event to their descriptions
            Map<String, List<Source>> eventSources = new TreeMap<>();  // Maps event to GenJ source list

            getProperties(indi, fam, policy, true, false, eventDesc, eventSources);
            if (eventDesc.size() > 0) {
                writeEvents(doc, gen, eventDesc, eventSources, true);
            }
            // done
        }

        /**
         * end formatting
         */
        @Override
        void formatEnd(Document doc) {
            if ((srcDisplay) && (srcAtEnd || srcTextAtEnd)) {
                writeSourceList(doc, -1, srcAtEnd, srcTextAtEnd);
            }
            // noop
        }
    } //Lineage

    /**
     * ********************************************************************************
     * 1 root 2 father 4 grandfather 8 great grandfather ...
     */
    class Agnatic extends DepthFirst {

        /**
         * each layout iterates over all individuals starting with the root up
         * to the maximum number of generations
         *
         * @param indi the current individual
         * @param fam the family that this individual was pulled out of (null
         * for root)
         * @param gen the current generation
         * @param sosa the sosa index
         * @param policy the privacy policy
         */
        @Override
        void recursion(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

            // stop here?
            if (gen > reportMaxGenerations) {
                return;
            }

            // let implementation handle individual
            formatIndi(indi, fam, gen, sosa, gen < privateGen ? PrivacyPolicy.getDefault().getAllPrivate() : PrivacyPolicy.getDefault().getAllPublic(), doc);

            // go one generation up to father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc == null) {
                return;
            }

            Indi father = famc.getHusband();

            // recurse into father
            if (father != null) {
                recursion(father, famc, gen + 1, sosa * 2, policy, doc);
            }

            // done
        }

        /**
         * our title
         */
        @Override
        String getTitle(Indi root) {
            return translate("title.agnatic", root.getName());
        }

        /**
         * start formatting
         */
        @Override
        void formatStart(Indi indi, Document doc) {
            // noop
        }

        /**
         * how we format an individual
         */
        @Override
        void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {
            if (gen < reportMinGenerations - 1) {
                return;
            }

            // only consider fathers
            if (gen > 1 && fam != null && fam.getHusband() != indi) {
                return;
            }

            // dump the indi's name
            doc.nextParagraph("space-after=10pt,space-before=10pt,start-indent=" + (gen * 20) + "pt");
            doc.addText(getName(indi, sosa, policy) + " ", "font-weight=bold");
            doc.nextParagraph("start-indent=" + (gen * 20 + 10) + "pt");

            // dump its properties
            // For each individual, we will store list of events, their descriptions and sources
            Map<String, String> eventDesc = new TreeMap<>();     // Maps event to their descriptions
            Map<String, List<Source>> eventSources = new TreeMap<>();  // Maps event to GenJ source list

            getProperties(indi, fam, policy, true, false, eventDesc, eventSources);

            if (eventDesc.size() > 0) {
                writeEvents(doc, gen, eventDesc, eventSources, true);
            }
            // done
        }

        /**
         * end formatting
         */
        @Override
        void formatEnd(Document doc) {
            if ((srcDisplay) && (srcAtEnd || srcTextAtEnd)) {
                writeSourceList(doc, -1, srcAtEnd, srcTextAtEnd);
            }
            // noop
        }
    }

    /**
     * ********************************************************************************
     * A Sosa Table goes breadth first and generates a sosa-ascending table of
     * properties
     *
     * 1;root;... 2;father;... 3;mother;... 4;grandfather 1;... 5;grandmother
     * 1;... 6;grandfather 2;... 7;grandfather 3;...
     */
    class Table extends BreadthFirst {

        String[] header = {"#", Gedcom.getName("NAME"), Gedcom.getName("BIRT"), Gedcom.getName("BAPM"), Gedcom.getName("MARR"), Gedcom.getName("DEAT"), Gedcom.getName("BURI"), Gedcom.getName("OCCU"), Gedcom.getName("RESI")};
        int[] widths = {3, 22, 12, 10, 10, 10, 10, 10, 10};

        /**
         * our title - simply the column header values
         */
        @Override
        String getTitle(Indi root) {
            return translate("title.sosa", root.getName());
        }

        /**
         * this is called once at the beginning of the recursion - we add our
         * table around it
         */
        @Override
        void formatStart(Indi root, Document doc) {
            // open CSV compatible table
            //doc.startTable("border=0.5pt solid black,genj:csv=true,width=100%");
            doc.startTable("genj:csv=true,width=100%");
            // define columns
            for (int i = 0; i < header.length; i++) {
                doc.addTableColumn("column-width=" + widths[i] + "%");
            }
            // define header
            doc.nextTableRow("background-color=#f0f0f0");
            for (int i = 0; i < header.length; i++) {
                if (i > 0) {
                    doc.nextTableCell("background-color=#f0f0f0");
                }
                doc.addText(header[i], "font-weight=bold");
            }
        }

        /**
         * called at each generation - ignored
         */
        @Override
        void formatGeneration(int gen, Document doc) {
            // noop
        }

        /**
         * this is called at each recursion step - output table rows
         */
        @Override
        void formatIndi(Indi indi, Fam fam, int gen, int sosa, PrivacyPolicy policy, Document doc) {

            if (gen < reportMinGenerations - 1) {
                return;
            }

            // grab properties - no prefixes, but all properties empty or not
            Map<String, String> eventDesc = new TreeMap<>();     // Maps event to their descriptions
            Map<String, List<Source>> eventSources = new TreeMap<>();  // Maps event to GenJ source list

            getProperties(indi, fam, policy, false, true, eventDesc, eventSources);

            // start with a new row, sosa and name
            doc.nextTableRow();
            doc.addText("" + sosa);
            doc.nextTableCell();
            doc.addText(getName(indi, 0, policy)); //pass in 0 as sosa - don't want it as part of name

            if (eventDesc.size() > 0) {
                doc.nextTableCell();
                writeEvents(doc, gen, eventDesc, eventSources, false);
            }
            // done for now
        }

        /**
         * called at the end of the recursion - end our table
         */
        @Override
        void formatEnd(Document doc) {
            // close table
            doc.endTable();
            if ((srcDisplay) && (srcAtEnd || srcTextAtEnd)) {
                writeSourceList(doc, -1, srcAtEnd, srcTextAtEnd);
            }
        }
    } //Table

    /**
     * *******************************************************************************
     */
    /**
     * Initialises variables for all displays
     */
    void InitVariables() {
        // Assign events to consider and their characteristics
        symbols[0] = "";
        symbols[1] = OPTIONS.getBirthSymbol();
        symbols[2] = OPTIONS.getBaptismSymbol();
        symbols[3] = OPTIONS.getMarriageSymbol();
        symbols[4] = OPTIONS.getDeathSymbol();
        symbols[5] = OPTIONS.getBurialSymbol();
        symbols[6] = OPTIONS.getOccuSymbol();
        symbols[7] = OPTIONS.getResiSymbol();

        // No source should be displayed for events that are not to be displayed
        dispEv[0] = true;
        dispEv[1] = reportDateOfBirth || reportPlaceOfBirth;
        dispEv[2] = reportDateOfBaptism || reportPlaceOfBaptism;
        dispEv[3] = reportDateOfMarriage || reportPlaceOfMarriage;
        dispEv[4] = reportDateOfDeath || reportPlaceOfDeath;
        dispEv[5] = reportDateOfBurial || reportPlaceOfBurial;
        //dispEv[6] = reportOccu;
        //dispEv[7] = reportResi;

        // In case of sosa table, force bullet on and prevent writing at gen level
        if (reportType == TABLE_REPORT) {
            displayBullet = true;
            if (displaySource == SRC_TITLE_GEN_NO_TEXT) {
                displaySource = SRC_TITLE_END_NO_TEXT;
            }
            if (displaySource == SRC_TITLE_TEXT_GEN) {
                displaySource = SRC_TITLE_TEXT_END;
            }
            if (displaySource == SRC_TITLE_GEN_TEXT_GEN) {
                displaySource = SRC_TITLE_END_TEXT_END;
            }
        }

        // What to display where depending on user's choice
        srcLinkSrc = false;   // true if link to source '0-srcId'
        srcLinkGenSrc = false;   // true if link to source 'gen nb-srcId'
        srcTitle = false;   // true if source title to display with event
        srcAtGen = false;   // true if source title to display at end of generation
        srcAtEnd = false;   // true if source title to display at end of report
        srcTextAtGen = false;   // true if text title to display at end of generation
        srcTextAtEnd = false;   // true if text title to display at end of report
        srcDisplay = false;   // true if a source information is to be displayed
        switch (displaySource) {
            case SRC_NO:
                break;
            case SRC_TITLE_NO_TEXT:
                srcTitle = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_GEN_NO_TEXT:
                srcLinkGenSrc = true;
                srcAtGen = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_TEXT_GEN:
                srcLinkGenSrc = true;
                srcTitle = true;
                srcTextAtGen = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_GEN_TEXT_GEN:
                srcLinkGenSrc = true;
                srcAtGen = true;
                srcTextAtGen = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_END_NO_TEXT:
                srcLinkSrc = true;
                srcAtEnd = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_TEXT_END:
                srcLinkSrc = true;
                srcTitle = true;
                srcTextAtEnd = true;
                srcDisplay = true;
                break;
            case SRC_TITLE_END_TEXT_END:
                srcLinkSrc = true;
                srcAtEnd = true;
                srcTextAtEnd = true;
                srcDisplay = true;
                break;
            default:
                ;
        }

    }

    /**
     * Assign format of colors
     */
    void assignColor(int srcColor) {
        // init color formats
        String cs;

        switch (srcColor) {
            case COLOR_BLACK:
                cs = "#000000";
                break;
            case COLOR_GREY:
                cs = "#a0a0a0";
                break;
            case COLOR_PURPLE:
                cs = "#ff60ff";
                break;
            case COLOR_INDIGO:
                cs = "#8560ff";
                break;
            case COLOR_BLUE:
                cs = "#6060ff";
                break;
            case COLOR_GREEN:
                cs = "#00a71c";
                break;
            case COLOR_YELLOW:
                cs = "#d1de00";
                break;
            case COLOR_ORANGE:
                cs = "#ffb260";
                break;
            case COLOR_RED:
                cs = "#ff6060";
                break;
            default:
                cs = "#000000";
        }
        format_one_line = "font-style=italic,color=" + cs;
        format_multi_lines = "margin-left=0px,font-style=italic,color=" + cs;
    }

    /**
     * Display functions
     */
    void writeEvents(Document doc, int gen, Map<String, String> eventDesc, Map<String, List<Source>> eventSources, boolean isIndented) {
        // Calculate indent if any
        String indent = "";
        if (isIndented) {
            indent = "start-indent=" + (gen * 20 + 10) + "pt";
        }

        // Display the events of an individual for all types of sosa report
        // (sosa, linage, etc)
        if ((reportFormat == ONE_EVT_PER_LINE) && (displayBullet) && (reportType != TABLE_REPORT)) {
            doc.startList(indent);
        }
        for (int ev = 0; ev < events.length; ev++) {
            String evStr = events[ev];
            String description = eventDesc.get(evStr);
            if (description == null) {
                description = "";
            }
            List<Source> sources = eventSources.get(evStr);
            boolean noSrc = false;
            if ((sources == null) || (sources.isEmpty())) {
                noSrc = true;
            }
            String preSrc = " ";
            if ((prefixEvent) && (symbols[ev].trim().length() > 0)) {
                preSrc = " (" + symbols[ev] + ") ";
            }

            // Go to next line or column if after 1 event
            if (ev != 0) {
                writeStartNextItem(doc, reportFormat, displayBullet, description.length() != 0, indent);
            }
            // Write event description
            writeDescription(doc, description);
            // Display source if any and required
            if (!noSrc && srcDisplay) {
                for (Source source : sources) { // NPE not possible due to definition of noSrc
                    String sId = source.getId();
                    if (srcTitle) {
                        if (!isValidText(source)) {
                            sId = "none";
                        }
                        writeStartNextParagraph(doc, reportFormat, indent);
                        writeSourceWithEvent(doc, reportFormat, noSrc, prefixSource, preSrc + source.getTitle() + " (" + source.getId() + ")", gen, sId);
                    } else {
                        writeSourceWithEvent(doc, reportFormat, noSrc, " ( " + prefixSource + preSrc + " " + source.getId() + " )", "", gen, sId);
                    }
                }
            }
            // Display noSource if no source found, source required, display when empty and event sources are to be displayed
            if (noSrc && srcDisplay && displayEmpty && dispEv[ev]) {
                if ((displayBullet) && (description.length() == 0) && (reportType != TABLE_REPORT)) {
                    writeStartNextItem(doc, reportFormat, displayBullet, true, indent);
                } else if (displayBullet) {
                    writeStartNextParagraph(doc, reportFormat, "");
                } else {
                    writeStartNextParagraph(doc, reportFormat, indent);
                }
                writeSourceWithEvent(doc, reportFormat, noSrc, prefixSource, preSrc + translate("noSource"), 0, "");
            }
        } // end of for loop
        if ((reportFormat == ONE_EVT_PER_LINE) && (displayBullet) && (reportType != TABLE_REPORT)) {
            doc.endList();
        }
    }

    void writeStartNextItem(Document doc, int format, boolean bullet, boolean isDescription, String style) {
        if (reportType == TABLE_REPORT) {
            doc.nextTableCell();
            return;
        }
        if (!isDescription) {
            return;
        }
        if (format == ONE_EVT_PER_LINE) {
            if (bullet) {
                doc.nextListItem();
            } else {
                doc.nextParagraph(style);
            }
        } else {
            doc.addText(", ");
        }
    }

    void writeStartNextParagraph(Document doc, int format, String style) {
        if (format == ONE_EVT_PER_LINE) {
            doc.nextParagraph(style);
        } else {
            doc.addText(", ");
        }
    }

    void writeDescription(Document doc, String text) {
        if (text.length() != 0) {
            doc.addText(text);
        }
    }

    void writeSourceWithEvent(Document doc, int format, boolean noSrcFound, String link, String source, int gen, String id) {
        String formatText;
        if (format == ONE_EVT_PER_LINE) {
            formatText = format_multi_lines;
        } else {
            formatText = format_one_line;
        }

        if (noSrcFound) {
            // display no source with event in case no source found
            doc.addText(link + source, formatText);
            return;
        }
        // if there is a source, add link to end of report...
        if (srcLinkSrc) {
            //doc.nextParagraph(formatText);
            doc.addLink(link, "0-" + id);
        } // ...or to end of generation
        else if (srcLinkGenSrc) {
            //doc.nextParagraph(formatText);
            doc.addLink(link, (gen + 1) + "-" + id);
        } // ...or else simply display the link as text,...
        else {
            doc.addText(link, formatText);
        }
        // ... and the source title if required.
        if (srcTitle) {
            doc.addText(source, formatText);
        }
    }

    boolean isValidText(Source source) {
        // Text associated with source is valid if corresponding list is not empty
        List<String> listOfNotes = GLOBAL_SRC_NOTES.get(source);
        return (!listOfNotes.isEmpty());
    }

    boolean isAlreadyIn(List<String> listOfStr, String strNote) {
        return listOfStr.stream().anyMatch((str) -> (str.contains(strNote)));
    }

    void writeSourceNotes(Document doc, Source source, String format) {
        List<String> listOfNotes = GLOBAL_SRC_NOTES.get(source);
        format += ",margin-left=8px,font-style=italic,color=#707070";
        for (String strNote : listOfNotes) {
            doc.nextParagraph(format);
            // Write straight before $#@ mark, italic afterwards
            int i = strNote.indexOf("$#@");
            if (i != -1) { // found
                String beg = strNote.substring(0, i);
                String end = strNote.substring(i + 3);
                doc.addText(beg, "font-style=normal");
                doc.addText(end, "font-style=italic");
            } else {
                doc.addText(strNote);
            }
        }
    }

    void writeSourceList(Document doc, int gen, boolean isTitle, boolean isText) {

        // This function writes all sources from the global source list and then clears the list
        // The logic is to display first all sources that have valid text and then the list of source ids that have no text associated to them
        // (if title is to be displayed, given there is always a valid one, the list is the normal one; it is only in the case of displaying text only that the invalid list needs to be displayed).
        // Return if nothing to display
        if (GLOBAL_SRC_LIST.isEmpty()) {
            return;
        }

        // Will hold string ids of sources with nothing to display
        List<String> noTextSources = new ArrayList<>();

        // The start depends on the reportType (whether there is a table or not)
        String format = "space-after=8pt";
        if (reportType == SOSA_REPORT) {
            if (gen == -1) {
                doc.nextPage();
            }
            doc.nextTableRow();
            doc.nextTableCell("margin-left=0px,number-columns-spanned=2,text-decoration=underline");
            doc.nextParagraph("space-before=20pt");
            if (gen == -1) {
                doc.addText("________________________________________________");
                doc.nextParagraph();
            }
            doc.addText(translate("sourceList"));
            doc.nextTableRow();
            doc.nextTableCell("margin-left=10px,number-columns-spanned=2");
        }
        if ((reportType == LINEAGE_REPORT) || (reportType == AGNATIC_REPORT) || (reportType == TABLE_REPORT)) {
            doc.nextPage();
            doc.nextParagraph("space-before=10pt");
            doc.addText("________________________________________________");
            doc.nextParagraph("space-after=10pt,space-before=10pt,text-decoration=underline");
            doc.addText(translate("sourceList"));
            doc.nextParagraph(format);
        }

        // Display sources, storing the "invalid" ones along the way
        for (Source source : GLOBAL_SRC_LIST) {
            String sId = source.getId();
            String sTitle = source.getTitle();
            if (isTitle) {
                doc.nextParagraph(format);
                doc.addAnchor((gen + 1) + "-" + sId);
                doc.addText("(" + sId + ") " + sTitle, "color=#303030");
                if (isText) {
                    if (isValidText(source)) {
                        writeSourceNotes(doc, source, format);
                    }
                }
            } else if (isText) {
                if (isValidText(source)) {
                    doc.nextParagraph(format);
                    doc.addAnchor((gen + 1) + "-" + sId);
                    doc.addText("(" + sId + ") ");
                    writeSourceNotes(doc, source, format);
                } else { // this is a source with nothing to display, store its id
                    noTextSources.add(sId);
                }
            }
        } // end of for loop

        // Display list of empty source ids if any
        if (noTextSources.size() > 0) {
            doc.nextParagraph("space-after=0pt");
            doc.addAnchor((gen + 1) + "-none");
            for (String n : noTextSources) {
                doc.addText("(" + n + ") ");
            }
            doc.nextParagraph(format);
            doc.addText(translate("noText"));
            doc.nextParagraph(format);
        }

        GLOBAL_SRC_LIST.clear();
        GLOBAL_SRC_NOTES.clear();
    }
}
