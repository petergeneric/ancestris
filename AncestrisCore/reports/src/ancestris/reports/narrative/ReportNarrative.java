/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.reports.narrative;

import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.io.InputSource;
import genj.renderer.MediaRenderer;
import genj.report.Report;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * ReportNarrative generates a text document containing the ancestors or descendants of a particular individual. The report itself can be in HTML, tex, rtf, or plain text format, and additional formats can be added.
 *
 * @author Bill Kelly (original)
 * @author Frédéric Lapeyre (modifications from august 2012)
 * @version 0.91
 */
@SuppressWarnings("unchecked")
@ServiceProvider(service = Report.class)
public class ReportNarrative extends Report {
    
    private static final Logger LOG = Logger.getLogger("ancestris.app", null);

    /* TODO priorities:
   _ in text
   Full text instead of abbreviations (Francois)
   Formatting: page title; page break before index start
   Title in report (unique to HTML)
   CSS for fonts, etc.
   Bibliography!
   More options for which individuals...also limit on number of generations (user wish)
     */
    public static final int DETAIL_NO_SHOW = 0;
    public static final int DETAIL_NAME = 1;
    public static final int DETAIL_BRIEF = 2;
    public static final int DETAIL_DATES = 3;
    public static final int DETAIL_BRIEF_WITH_DATES = 2;
    public static final int DETAIL_FULL = 5;
    public static final int DETAIL_EVERYTHING = 6;

    public boolean ancestors = true;
    public boolean showIds = false;
    public boolean showRefns = false;
    public boolean withNameIndex = true;
    public boolean withPlaceIndex = true;
    private boolean withBibliography = false; // todo make public when implemented
    public boolean withNotes = true;
    public boolean withSources = true;
    public boolean withUserTags = true;
    public boolean withOtherDetails = true;
    public boolean withChildrenList = true;
    public boolean showImages = true;
    public boolean includePersonalTags = false;
    public boolean includeUnknownTags = false;
    public boolean useAbbrevations = false;
    public int maxGenerations = 99;

    public String htmlStylesheet = null;
    private boolean alignImages = true; // TODO: option - in formatter.DocumentWriter or doc-wide options; make public when implemented
    public boolean noAds = false;

    private String nameIndexTitle;
    private String placeIndexTitle;
    private String sourceIndexTitle;

    // For tests without user interaction
    public Object startTest(Gedcom gedcom, String startingIndiTag) {
        Indi indi = (Indi) gedcom.getEntity(Gedcom.INDI, startingIndiTag); // Pale Black
        return start(indi);

    }

    /**
     * Starting point for ancestors or descendants. Overridden in tests.
     *
     * @param msg Prompt if interactive
     * @param gedcom Family tree
     * @param tag z.B. INDI
     * @return Entity from family tree
     */
    public Entity getStartingEntity(String msg, Gedcom gedcom, String tag) {
        return getEntityFromUser(msg, gedcom, tag);
    }

    /**
     * The report's entry point
     */
    public Document start(Indi indi) {

        println("indi = " + indi.getName());

        nameIndexTitle = withNameIndex ? translate("index.names") : null;
        placeIndexTitle = withPlaceIndex ? translate("index.places") : null;
        sourceIndexTitle = withBibliography ? translate("bibliography") : null;

        // 1st pass is not necessary anymore - links between parts of the document are automatically resolved if available
        // 2nd pass - fill document content
        String title = getUtterance(ancestors ? "doc.ancestors.title" : "doc.descendants.title",
                new String[]{new IndiWriter(indi, null).getName(indi)}).toString();
        Document doc = new Document(title);
        doc.startSection(title, 1);

        // todo bk: used to set style inline if no .css given.  Can of course also
        // put it into a defualt .css.  The text was
        /*
<style>
  body { color: black; background: white; }
  body { font-family: Verdana, sans-serif; }
</style>
         */
        if (!noAds) {
            // e.g. This report was generated on GenealogyJ.
            // Text is split into 2 parts, since need to call addLink() in between.
            Utterance ad = getUtterance("doc.ad.1");

            PropertyDate dateFormatter = new PropertyDate();
            dateFormatter.setValue(PropertyDate.DATE, PointInTime.getPointInTime(System.currentTimeMillis()), null, "");
            ad.set("DATE", dateFormatter.getDisplayValue());
            doc.addText(ad.toString());
            doc.addText(" ");
            doc.addExternalLink("Ancestris", "https://www.ancestris.org");
            ad = getUtterance("doc.ad.2");
            ad.set("DATE", new Date().toString());
            doc.addText(ad.toString());
        }

        Set printed = new HashSet();
        Set gen = new HashSet();
        gen.add(indi);
        Set nextGen;
        int generationNumber = 1;
        do {
            nextGen = printGenerations(doc, generationNumber, gen, printed);
            generationNumber++;
            gen = nextGen;
        } while (gen.size() > 0 && generationNumber <= maxGenerations);

        if (withNameIndex) {
            println(translate("log.printingNameIndex")); // Printing name index
        }
        if (withPlaceIndex) {
            println(translate("log.printingPlaceIndex"));
        }

        if (withBibliography) {
            // Generate a list with an entry for each referenced source
            // todo bk: generate bibliography
        }

        // done
        println(translate("log.finished"));
        return doc;

    }

    private Utterance getUtterance(String key) {
        return getUtterance(key, new String[0]);
    }

    private Utterance getUtterance(String key, String[] params) {
        String template1 = translate(key);
        if (template1 == null) {
            template1 = key;
        }
        return Utterance.forTemplate(getResources(), template1, params);
    }

    private Set printGenerations(Document doc, int n, Set gen, Set printed) {

        Utterance generations = getUtterance("individuals.in.generation",
                new String[]{
                    Integer.toString(gen.size()),
                    Integer.toString(n)
                });
        println(generations.toString());

        Set nextGen = new LinkedHashSet(); // important: LinkedHashSet preserves insertion order

        Utterance docTitle = getUtterance("section.title", new String[]{Integer.toString(n)});
        doc.startSection(docTitle.toString(), 2);

        for (Iterator i = gen.iterator(); i.hasNext();) {

            Indi indi = (Indi) i.next();
            IndiWriter writer = new IndiWriter(indi, doc);

            String sectionTitle = indi.getName();
            Property title = indi.getProperty("TITL");
            if (title != null) {
                if (title.getValue().contains(" of ") || title.getValue().startsWith("of ")) {
                    // Sounds better after name
                    sectionTitle += ", " + title;
                } else {
                    sectionTitle = title + " " + sectionTitle;
                }
            }

            // if indi already listed via different lineage, just write a link with no details.
            if (printed.contains(indi)) {
                doc.startSection(sectionTitle, 3); // section without (dupe) key
                doc.addLink("Refer to entry via different lineage", indi.getLinkAnchor());
            } else {
                doc.startSection(sectionTitle, indi.getLinkAnchor(), 3); // section with 
                if (withNameIndex) {
                    doc.addIndexTerm(nameIndexTitle, indi.getLastName(), indi.getFirstName());
                }
                boolean showKids = indi.getSex() == PropertySex.MALE; // TODO: track families shown, print with first parent
                writer.writeEntry(showKids, DETAIL_FULL, true, /*linkToIndi*/ false, showImages);
            }

            addNextGeneration(nextGen, indi);

            printed.add(indi); // printed in pass 2
        }

        return nextGen;
    }

    /**
     * Add the next generation of relations of 'indi' to 'indis'. The option 'ancestors' determines which direction we go.
     *
     * @param indis Collection of relatives comprising 'next' generation
     * @param indi Individual to start with
     */
    private void addNextGeneration(Set indis, Indi indi) {

        if (ancestors) {
            Indi parent = indi.getBiologicalFather();
            if (parent != null) {
                indis.add(parent);
            }
            parent = indi.getBiologicalMother();
            if (parent != null) {
                indis.add(parent);
            }
        } else {
            indis.addAll(Arrays.asList(indi.getChildren()));
        }
    }

    /**
     * IndiWriter writes information about an individual to a Formatter.
     */
    public class IndiWriter {

        private Indi indi;
        private Document doc;

        /**
         * constructor
         */
        public IndiWriter(Indi indi, Document doc) {
            this.indi = indi;
            this.doc = doc;
        }

        // From definition of INDIVIDUAL_ATTRIBUTE_STRUCTURE in Gedcom 5.5 spec
        // at http://homepages.rootsweb.com/~pmcbride/gedcom/55gcch2.htm
        private final Set INDIVIDUAL_ATTRIBUTES = new HashSet(Arrays.asList(
                new String[]{
                    "CAST", // <CASTE_NAME>   {1:1}
                    "DSCR", // <PHYSICAL_DESCRIPTION>   {1:1}
                    "EDUC", // <SCHOLASTIC_ACHIEVEMENT>   {1:1}
                    "IDNO", // <NATIONAL_ID_NUMBER>   {1:1}*
                    "NATI", // <NATIONAL_OR_TRIBAL_ORIGIN>   {1:1}
                    "NCHI", // <COUNT_OF_CHILDREN>   {1:1}
                    "NMR", // <COUNT_OF_MARRIAGES>   {1:1}
                    "PROP", // <POSSESSIONS>   {1:1}
                    "RELI", // <RELIGIOUS_AFFILIATION>   {1:1}
                    "SSN", // <SOCIAL_SECURITY_NUMBER>   {0:1}
                // handled as part of name: "TITL", // <NOBILITY_TYPE_TITLE>  {1:1}
                }
        ));
        // TODO bk: sentence.NCHI.singular defined in properties but not used.
        // TODO bk: added this June 2005: public static boolean alignImages = true; // TODO: option - in DocumentWriter or doc-wide options

        /**
         * Returns the name in the format specified by the options, plus ID and reference in parentheses if so specified in the options.
         */
        public String getNamePlusIdAndReference(Indi i) {
            StringBuffer name = new StringBuffer(i.getFirstName());
            appendName(name, i.getLastName());
            appendName(name, i.getNameSuffix());
            boolean doShowRefn = showRefns && i.getProperties("REFN").length > 0;
            if (showIds || doShowRefn) {
                name.append(" (");
                if (showIds) {
                    name.append(i.getId());
                    if (doShowRefn) {
                        name.append("; ");
                    }
                }
                if (doShowRefn) {
                    Property[] refns = i.getProperties("REFN"); // todo bk is that correct?
                    for (int j = 0; j < refns.length; j++) {
                        Property refn = refns[j];
                        if (j > 0) {
                            name.append(", ");
                        }
                        name.append(refn.getValue());
                    }
                }
                name.append(")");
            }
            return name.toString();
        }

        /**
         * Returns the name in the format specified by the options.
         *
         * @param i
         * @return
         */
        public String getName(Indi i) {
            if (i == null) {
                return "";
            }
            StringBuffer name = new StringBuffer(i.getFirstName());
            appendName(name, i.getLastName());
            appendName(name, i.getNameSuffix());
            return name.toString();
        }

        private void appendName(StringBuffer name, String element) {
            if (element != null && element.length() > 0) {
                name.append(' ');
                name.append(element);
            }
        }

        public void writeEntry(boolean withChildren, int defaultDetailLevel, boolean withParents, boolean linkToIndi, boolean showImages) {

            int detailLevel = defaultDetailLevel;
            try {

                // FIXME
                if (withNameIndex) {
                    doc.addIndexTerm(nameIndexTitle, indi.getLastName(), indi.getFirstName());
                }

                // TODO: option for image positioning
                if (showImages && alignImages && detailLevel >= DETAIL_FULL) {
                    insertImages();
                }
                // TODO bk: other kinds of objects...e.g. mp3, wav...

                if (linkToIndi) {
                    doc.addLink(getNamePlusIdAndReference(indi), indi.getLinkAnchor());
                } else {
                    doc.addText(getNamePlusIdAndReference(indi));
                }
                // Nils used indi.toString()); but in narrative text, you'd rather
                // say John Smith than Smith, John.  This way we can also be more
                // flexible, adding options to capitalize family names, etc.
                // However, since doc.addLink() does its own indi.toString(), that's not
                // feasible at the moment.  You may also choose to use only the first
                // name in a known context.

                // TODO: alternate names
                // TODO: print REFN and/or ID if desired
                // TODO: Some other 1-level tags in my files:  DESC - almost certainly wrong in usage
                // EDIT (not in standard), EMAIL (not in standard),
                // INFT = informant (not in standard), INTV (not in standard), ORGA (not in standard),
                // ORIG (not in standard), OWNR (not in standard)
                // FILE - only as part of multimedia and in header:
                // MULTIMEDIA_LINK: =
                //
                //  n  OBJE           {1:1}
                //    +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
                //    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
                //    +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
                //    +1 <<NOTE_STRUCTURE>>  {0:M}
                //  ]
                //
                // TODO: ASSO describes an association between individuals, e.g.:
                // 1 ASSO @I2@
                //    2 RELA Godfather
                // TODO: ALIA references another individual who is really the same
                // TODO:  for privacy: RESN <RESTRICTION_NOTICE>
                if (detailLevel >= DETAIL_DATES) {
                    String date = getDateString(indi.getBirthDate());
                    if (date.length() > 0) {
                        doc.addText(", ");
                        addGenderSpecificUtterance("born", indi, date);

                    }
                    String placeB = getPlaceString(indi.getProperty("BIRT"), null);
                    if (placeB.length() > 0) {
                        doc.addText(" " + placeB);
                    }

                    // (child of X and Y)
                    Indi father = indi.getBiologicalFather(), mother = indi.getBiologicalMother();
                    if (withParents && (father != null || mother != null)) {
                        Utterance parenPhrase = null;
                        if (indi.getBiologicalFather() != null && indi.getBiologicalMother() != null) {
                            parenPhrase = Utterance.forProperty(getResources(), "phrase.childof.parents",
                                    new String[]{getName(indi.getBiologicalFather()), getName(indi.getBiologicalMother())},
                                    new Entity[]{indi.getBiologicalFather(), indi.getBiologicalMother()});
                            // TODO: how manage links to parents within the phrase
                        } else {
                            Indi parent = indi.getBiologicalFather() != null ? indi.getBiologicalFather()
                                    : indi.getBiologicalMother();
                            parenPhrase = Utterance.forProperty(getResources(), "phrase.childof.parent",
                                    new String[]{getName(parent)},
                                    new Entity[]{parent});
                        }
                        if (parenPhrase != null) {
                            parenPhrase.setSubject(indi);
                            doc.addText(" (");
                            parenPhrase.addText(doc); // knows how to link the parents
                            doc.addText(")");
                        }
                    }

                    Fam[] fams = indi.getFamiliesWhereSpouse();
                    for (int i = 0; i < fams.length; i++) {
                        Fam fam = fams[i];
                        PropertyDate marriage = fam.getMarriageDate();
                        doc.addText(", ");
                        if (!useAbbrevations) {
                            addUtterance(indi, genderSpecificKey("phrase.married", indi.getSex()), date);
                        } else {
                            addUtterance("abbrev.married");
                        }
                        doc.addText(" ");
                        if (fams.length > 1) {
                            doc.addText("(" + (i + 1) + ") ");
                        }
                        if (marriage != null) {
                            doc.addText(getDateString(marriage));
                            String placeM = getPlaceString(fam.getProperty("MARR"), null);
                            if (placeM.length() > 0) {
                                doc.addText(" " + placeM);
                            }
                        }
                        Property age = (indi.getSex() == PropertySex.MALE)
                                ? fam.getProperty(new TagPath("FAM:HUSB:AGE"))
                                : fam.getProperty(new TagPath("FAM:WIFE:AGE"));
                        if (age != null) {
                            doc.addText(Utterance.forProperty(getResources(), "phrase.at_age", new String[]{age.getValue()}).toString());
                        }
                        Indi spouse = fam.getOtherSpouse(indi);
                        if (spouse == null) {
                            addUtterance("phrase.spouses_name_unknown");
                        } else {
                            doc.addText(" ");
                            addUtterance("prep.married_to");
                            doc.addText(" ");
                            doc.addLink(getNamePlusIdAndReference(spouse), spouse.getLinkAnchor());
                        }
                        // A MARR event can also have an AGE prop under HUSB and WIFE
                    }

                    date = getDateString(indi.getDeathDate());
                    if (date.length() > 0) {
                        doc.addText(", ");
                        if (!useAbbrevations) {
                            addUtterance(indi, genderSpecificKey("phrase.died", indi.getSex()), date);
                        } else {
                            addUtterance("abbrev.died", date);
                        }
                        String placeD = getPlaceString(indi.getProperty("DEAT"), null);
                        if (placeD.length() > 0) {
                            doc.addText(" " + placeD);
                        }
                    }

                    doc.addText(".");

                    Set tagsProcessed = new HashSet(Arrays.asList(new String[]{
                        "REFN", "CHAN", "SEX", "BIRT", "DEAT", "FAMC", "FAMS",
                        "NAME", // TODO: print alternative forms
                        "OBJE",
                        "ASSO", // exclude until implemented
                    }));
                    // TODO: details from FAMS - div, divf, ...
                    if (detailLevel >= DETAIL_FULL) {

                        Property[] props = indi.getProperties();
                        for (int i = 0; i < props.length; i++) {
                            Property prop = props[i];
                            if (tagsProcessed.contains(prop.getTag())) {
                                // ignore
                                continue;
                            }

                            // Allow for sentences grouping multiple properties of the
                            // same type.
                            int numberOfLikeProperties = 0;
                            for (int j = i + 1; j < props.length; j++) {
                                if (props[j].getPropertyName().equals(props[i].getPropertyName())) {
                                    numberOfLikeProperties++;
                                } else {
                                    break;
                                }
                            }
                            Property[] likeProps = new Property[numberOfLikeProperties + 1];
                            numberOfLikeProperties = 0;
                            for (int j = i; j < props.length; j++) {
                                if (props[j].getPropertyName().equals(props[i].getPropertyName())) {
                                    likeProps[numberOfLikeProperties] = props[j];
                                    numberOfLikeProperties++;
                                } else {
                                    break;
                                }
                            }

                            doc.nextParagraph();

                            // Increment i by n-1 if n (> 1) events were processed below.
                            if (prop instanceof PropertyEvent) {
                                i += (writeEvents(likeProps) - 1);
                            } else if (INDIVIDUAL_ATTRIBUTES.contains(prop.getTag())) {
                                i += (writeEvents(likeProps) - 1);
                            } else if (prop.getTag().equals("RESI") || prop.getTag().equals("ADDR")) {
                                i += (writeEvents(likeProps) - 1);
                            } else if (prop.getTag().equals("OCCU")) {
                                if (prop.getValue().length() > 0) {
                                    doc.addText(" ");
                                    doc.nextParagraph();
                                    boolean past = true;
                                    if (indi.getDeathDate() == null) {
                                        Delta age = indi.getAge(PointInTime.getPointInTime(System.currentTimeMillis()));
                                        if (age != null && age.getYears() < 65) {
                                            past = false;
                                        }
                                    }

                                    Utterance u = Utterance.forProperty(getResources(), "sentence.OCCU",
                                            new String[]{prop.getValue()});
                                    // TODO English a/an
                                    u.setSubject(indi);
                                    u.set("tense", past ? "past" : "present"); // todo: distinguish past/present for living individuals
                                    doc.addText(u.toString());

                                    writeNodeSource(prop);
                                }
                            } else if (prop.getTag().equals("NOTE")) {
                                if (withNotes) {
                                    doc.nextParagraph("");
                                    doc.addText("*");
                                    doc.nextParagraph("");
                                    if (prop instanceof PropertyXRef) {
                                        Entity ref = ((PropertyXRef) prop).getTargetEntity();
                                        addUtterance("phrase.note", ref.getValue());
                                        // print SOUR etc of NOTE
                                        Property source = ref.getProperty("SOUR");
                                        if (source != null && withSources) {
                                            writeSource((Source) ((PropertySource) source).getTargetEntity());
                                        }
                                        // TODO: avoid printing same note twice (means referring to a previous note, hmm)
                                        // TODO: what else can a NOTE @xx@ have?
                                    } else {
                                        addUtterance("phrase.note", prop.getValue());
                                    }
                                }
                            } else if (prop.getTag().equals("SOUR") && prop instanceof PropertySource) {
                                if (withSources) {
                                    writeSource((Source) ((PropertySource) prop).getTargetEntity());
                                }
                            } else if (prop.getTag().equals("SOUR")) {
                                if (withSources) {
                                    // One can also record a text description of the source directly
                                    addUtterance("phrase.source", prop.getValue());
                                }
                            } else if (prop.getTag().startsWith("_")) {
                                if (withUserTags) {
                                    if (detailLevel >= DETAIL_EVERYTHING) {
                                        // Including personal tags too
                                        addUtterance("phrase.property", prop.getValue()); // todo bk look up language-specific
                                    }
                                }
                            } else if (withOtherDetails) {
                                // Unknown tag...might be interesting to put it in
                                String val = prop.getValue();
                                if (!val.isEmpty()) {
                                    val = prop.getPropertyName() + ": " + val;
                                    addUtterance("", val);
                                }
        
                            }
                        }
                    } else if (detailLevel <= DETAIL_BRIEF_WITH_DATES) {
                        // TODO: a bit of detail on number of kids, occu?
                    }
                }

                if (withChildren && withChildrenList) {
                    // todo bk better to number the families if there's more than one
                    // than to print the full details with every child.
                    Indi[] children = indi.getChildren();
                    if (children.length > 0) {
                        doc.nextParagraph("");
                        doc.addText("*");
                        doc.nextParagraph("");
                        Fam[] families = indi.getFamiliesWhereSpouse();
                        if (families.length > 1) {
                            doc.startList();
                        }
                        for (int i = 0; i < families.length; i++) {
                            Fam family = families[i];
                            if (families.length > 1) {
                                doc.nextListItem("genj:label=" + (i + 1) + ".");
                            }
                            String str = Utterance.forProperty(getResources(), "phrase.children.of.parents",
                                            new String[]{getName(family.getHusband()), getName(family.getWife())},
                                            new Entity[]{family.getHusband(), family.getWife()}).toString(); 
                            doc.addText(str.substring(0, 1).toUpperCase() + str.substring(1));
                            children = family.getChildren();
                            doc.startList();
                            for (int j = 0; j < children.length; j++) {
                                doc.nextListItem("genj:label=" + (j + 1) + ".");
                                Indi child = children[j];
                                IndiWriter w = new IndiWriter(child, doc);
                                // Parents clear from the context, don't print them.
                                w.writeEntry(/*withChildren*/false, DETAIL_DATES,
                                        /*withParents*/ false, /*linkToIndi*/ true, false);
                            }
                            doc.endList();
                        }
                        if (families.length > 1) {
                            doc.endList();
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.INFO, "Error during narrative report.", e);
                addUtterance("sentence.error");
            }
        }

        /**
         * Translates with abbreviation, if useAbbrevations set and abbreviation available, else with phrase. phrase.KEY.GENDER.LANG or abbrev.KEY.LANG must exist in the resources.
         *
         * @param key
         * @param indi
         * @param param1
         */
        private void addGenderSpecificUtterance(String key, Indi indi, String param1) {
            // todo bk should pass indi to Utterance, as it also resolves gender
            String abbrevKey = "abbrev" + key;
            if (useAbbrevations && Utterance.isTranslatable(abbrevKey, getResources())) {
                addUtterance(indi, abbrevKey, param1); // phrase w gender, later case
            } else {
                addUtterance(indi, genderSpecificKey("phrase." + key, indi.getSex()), param1);
            }
        }

        private void addUtterance(String key) {
            doc.addText(Utterance.forProperty(getResources(), key).toString());
        }

        private void addUtterance(String key, String value1) {
            if (!value1.isEmpty()) {
                if (key.isEmpty()) {
                    doc.addText(value1);
                } else {
                    doc.addText(Utterance.forProperty(getResources(), key, new String[]{value1}).toString());
                }
            }
        }

        private void addUtterance(Indi indi, String key, String value1) {
            Utterance u = Utterance.forProperty(getResources(), key, new String[]{value1});
            u.setSubject(indi);
            doc.addText(u.toString());
        }


        /* Get date in nicely formatted, localized, human-readable form. */
        private String getDateString(Property prop) {
            if (prop == null || !prop.isValid()) {
                return "";
            }
            return prop.getDisplayValue();
        }

        private void insertImages() {
            // Get images from OBJE tags...align options work in HTML best if the images
            // are printed before the text, but this may vary with other formatter.Formatter
            // implementations, should allow them to give a hint.
            // Should also have option where to put the images; even in HTML
            // if could be preferable to override the placement.
            // GEDCOM Example:
            //1 OBJE
            //2 TITL Pilot Error
            //2 FORM JPG
            //2 FILE meiern.jpg
            //2 NOTE More explanatory text.
            Property[] props = indi.getProperties(new TagPath("INDI:OBJE"));
            for (Property prop : props) {
                PropertyFile file = null;
                if (prop instanceof PropertyXRef){
                    PropertyXRef pxref = (PropertyXRef) prop;
                    Entity e = pxref.getTargetEntity();
                    if (e.getProperty("FILE") != null) {
                        file = (PropertyFile) e.getProperty("FILE");
                    }
                } else if (prop.getProperty("FILE") != null) {
                    file = (PropertyFile) prop.getProperty("FILE");
                }
                if (file != null) {
                    InputSource inputSource = MediaRenderer.getSource(file);
                    doc.addImage(new File(inputSource.getLocation()), "");
                    doc.nextParagraph();
                    doc.addText(" ");
                }
            }
        }

        private void writeNodeSource(Property node) {
            Property prop = node.getProperty("SOUR");
            if (prop != null && prop instanceof PropertySource) {
                writeSource((Source) ((PropertySource) prop).getTargetEntity());
            }
        }

        private void writeSource(Source prop) {
            // TODO: source #, full info at first citation, page #, "ibid" etc,
            // variations depending on whether part of bibliography?

            // TODO: ref to bibliography
            if (sourceIndexTitle != null) {
                String key = prop.getProperty("REFN") != null ? prop.getProperty("REFN").getValue()
                        : prop.getId();
                doc.addLink("[" + key + "]", prop.getLinkAnchor()); // would want [sourceRefn]
                // todo: also page number of reference
            }

            // Definition from GEDCOM 5.5 standard:
            //    SOURCE_RECORD: =
            //
            //      n  @<XREF:SOUR>@ SOUR  {1:1}
            //        +1 DATA        {0:1}
            //          +2 EVEN <EVENTS_RECORDED>  {0:M}
            //            +3 DATE <DATE_PERIOD>  {0:1}
            //            +3 PLAC <SOURCE_JURISDICTION_PLACE>  {0:1}
            //          +2 AGNC <RESPONSIBLE_AGENCY>  {0:1}
            //          +2 <<NOTE_STRUCTURE>>  {0:M}
            //        +1 AUTH <SOURCE_ORIGINATOR>  {0:1}
            //          +2 [CONT|CONC] <SOURCE_ORIGINATOR>  {0:M}
            //        +1 TITL <SOURCE_DESCRIPTIVE_TITLE>  {0:1}
            //          +2 [CONT|CONC] <SOURCE_DESCRIPTIVE_TITLE>  {0:M}
            //        +1 ABBR <SOURCE_FILED_BY_ENTRY>  {0:1}
            //        +1 PUBL <SOURCE_PUBLICATION_FACTS>  {0:1}
            //          +2 [CONT|CONC] <SOURCE_PUBLICATION_FACTS>  {0:M}
            //        +1 TEXT <TEXT_FROM_SOURCE>  {0:1}
            //          +2 [CONT|CONC] <TEXT_FROM_SOURCE>  {0:M}
            //        +1 <<SOURCE_REPOSITORY_CITATION>>  {0:1}
            //        +1 <<MULTIMEDIA_LINK>>  {0:M}
            //        +1 <<NOTE_STRUCTURE>>  {0:M}
            //        +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
            //          +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
            //        +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
            //        +1 <<CHANGE_DATE>>  {0:1}
            Utterance u = getSentenceForTag("phrase.SOUR");
            String[] tags = new String[]{"REFN", "TYPE", "TITL", "AUTH", "EDIT",
                "INTV", "INFT", "OWNR"};
            for (String tag : tags) {
                addOptionalParam(u, prop, tag);
            }
            if (prop.getProperty("PAGE") != null) {
                u.set("OPTIONAL_PAGE", ", " + Utterance.forProperty(getResources(), "abbrev.page") + " " + prop.getProperty("PAGE").getValue());
            }
            String date = getDatePhrase(prop);
            if (date.length() > 0) {
                u.set("OPTIONAL_DATE", date);
            }
            doc.addText(" " + u.toString() + "");

            /* Old non-configurable version:
      // TODO: Date used to be " on DATE", or "in MONTH" which sounds better,
      // but needs to be pretty configurable to work in different languages!
      // TODO: had special phrasing below when both INTV and INFT present
      // Should one traverse all tags and take those where a phrase is configured,
      // instead of a preconfigured list?
      // TODO: Print contained NOTE if any.
      doc.addText("[" + reportProperties.getProp("phrase.Source") + " ");
      // TODO phrase.SOUR={phrase.Source}{OPTIONAL_REFN}{OPTIONAL_TYPE}{OPTIONAL_AUTH}{OPTIONAL_EDIT}{OPTIONAL_INTV}{OPTIONAL_INFT}{OPTIONAL_OWNR}

      // TODO: DATA, REPO, PUBL, OBJE.  Perhaps also the GEDCOM 5.0 fields
      // TYPE,
      // which I haven't expunged from my data (Bill Kelly).
      // Probably not of interest: ABBR is mainly a sort key ("a short title used
      // for sorting, filing, and retrieving source records"), but depends on
      // popular usage and demand; RIN, CHAN.
      doc.addText("] ");
             */
        }

        private void addOptionalParam(Utterance u, Source prop, String tag) {
            if (prop.getProperty(tag) != null) {
                // Look for report property for this attribute of the source.
                String phraseKey = "phrase." + prop.getTag() + "." + tag;
                String value;
                if (translate(phraseKey) != null) {
                    Utterance phrase = Utterance.forTemplate(getResources(), translate(phraseKey),
                            new String[]{prop.getProperty(tag).getValue()});
                    value = phrase.toString();
                } else {
                    value = prop.getProperty(tag).getValue();
                }
                // Special punctuation: maybe find a better way.
                // todo This method is not sensitive to context (don't know what the parent
                // node or part of the document is) and it would be nice to render the title
                // in italics, which isn't possible when we build up the whole text at once.
                if (tag.equals("TYPE")) {
                    value = "(" + value + ")";
                } else if (tag.equals("TITL")) {
                    value = "\"" + value + "\"";
                }
                u.set("OPTIONAL_" + tag, value);  // todo bk: optional. better?

            }
        }

        private int writeEvents(Property[] likeProps) {
            return printEventUtterance(likeProps);
        }

        /**
         * prop is most likely a PropertyEvent. Returns a date phrase like " on February 28, 1997".
         *
         * @param prop most likely a PropertyEvent
         * @return grammatically appropriate date phrase in the local language
         */
        private String getDatePhrase(Property prop) {
            String date = "";
            PropertyDate propDate = null;
            if (prop instanceof PropertyEvent) {
                propDate = ((PropertyEvent) prop).getDate(true);
                date = getDateString(prop.getProperty("DATE"));
            }
            if (date.length() > 0) {
                if (date.startsWith("FROM")) { // TODO fix case
                    date = " " + date;
                } else if (propDate != null && propDate.getStart().getDay() == PointInTime.UNKNOWN) {
                    Utterance phrase;
                    if (propDate.getStart().getMonth() == PointInTime.UNKNOWN) {
                        phrase = Utterance.forProperty(getResources(), "phrase.date.year", new String[]{date});
                    } else {
                        phrase = Utterance.forProperty(getResources(), "phrase.date.month", new String[]{date});
                    }
                    date = phrase.toString();
                } else {
                    Utterance phrase = Utterance.forProperty(getResources(), "phrase.date.day", new String[]{date});
                    date = phrase.toString();
                }
            }
            return date;
        }

        private boolean propertyDefined(String key) {
            return !translate(key).equals(key);
        }

        private String genderSpecificKey(String key, int gender) {
            String suffix = gender == PropertySex.MALE ? ".male"
                    : gender == PropertySex.FEMALE ? ".female"
                            : ".genderUnknown";
            if (propertyDefined(key + suffix)) {
                return key + suffix;
            }
            // The language may not require gender-specific forms, or may have that information embedded within the template.
            return key;
        }

        private int printEventUtterance(Property[] props) {
//      System.err.println("Printing event utterance for " + props[0].getTag());
            String seriesKey = "listFirst." + props[0].getTag();
            String itemKey = "listItem." + props[0].getTag();
            if (props.length == 1
                    || (!propertyDefined(seriesKey) && !propertyDefined(itemKey))) {
                printEventUtterance(props[0]);
                return 1;
            }

            String list = getListUtterance(props); // todo 1 (bk) ooops, need int return too

            // Here we add the list to the sentence...is that a good idea?
            Property prop = props[0];
            Utterance s = getSentenceForTag(prop.getTag(), new String[]{list});
            s.setSubject(indi);

            doc.addText(" " + s.toString());

            return props.length; // right most of the time
        }

        /**
         * Generate utterance for a list of properties. If resource listItem.PROP is defined, uses the standard list format for this language: [listFirst][listNext][listNext]...[listLast] for instance, for English:
         * <pre>
         * listFirst.RESI=He resided [1]
         * listNext=, [1]
         * listLast=\ and [1]
         * </pre> and filling into listItem.PROP for [1] in each case.
         *
         * @param props
         * @return
         */
        private String getListUtterance(Property[] props) {
            StringBuilder result = new StringBuilder(100);
            Property prop = props[0];

            // In many languages we can use the same list construct, just varying the case.
            String listFirst = "listFirst." + prop.getTag();
            if (translate(listFirst).equals(listFirst)) {
                listFirst = "listFirst";
            }
            String listNextKey = "listNext." + prop.getTag();
            if (translate(listNextKey).equals(listNextKey)) {
                listNextKey = "listNext";
            }
            String listLastKey = "listLast." + prop.getTag();
            if (translate(listLastKey).equals(listLastKey)) {
                listLastKey = "listLast";
            }

            Utterance item = getListItemUtterance(prop);
            result.append(Utterance.forTemplate(getResources(), translate(listFirst),
                    new String[]{item.toString()}));

            for (int i = 1; i < props.length - 1; i++) {
                prop = props[i];
                item = getListItemUtterance(prop);
                result.append(Utterance.forTemplate(getResources(), translate(listNextKey),
                        new String[]{item.toString()}));
            }

            prop = props[props.length - 1];
            item = getListItemUtterance(prop);
            result.append(Utterance.forTemplate(getResources(), translate(listLastKey),
                    new String[]{item.toString()}));

            return result.toString();
        }

        private Utterance getListItemUtterance(Property prop) {
            String listItemKey = "listItem." + prop.getTag();
            Utterance s = Utterance.forTemplate(getResources(), translate(listItemKey), new String[]{prop.getValue()});
            completeEventUtterance(s, prop);
            return s;
        }

        private void printEventUtterance(Property prop) {
            Utterance s = getSentenceForTag(prop.getTag(), new String[]{prop.getValue()});
            s.setSubject(indi);
            completeEventUtterance(s, prop);
            doc.addText(" " + s.toString());
        }

        private void completeEventUtterance(Utterance s, Property prop) {
            String place = getPlaceString(prop, null);
            if (place.length() > 0) {
                s.set("OPTIONAL_PP_PLACE", place);
            }
            if (prop.getProperty("AGNC") != null) {
                // Default agency phrase if none for tag?
                Utterance agency = Utterance.forProperty(getResources(), "phrase." + prop.getTag() + ".AGENCY",
                        new String[]{prop.getProperty("AGNC").getValue()});
                s.set("OPTIONAL_AGENCY", agency.toString());
            }
            String date = "";
            if (prop instanceof PropertyEvent) {
                date = getDateString(prop.getProperty("DATE"));
            }
            if (date.length() > 0) {
                s.set("OPTIONAL_PP_DATE", date); // TODO: still needs prep
            }
        }

        private Utterance getSentenceForTag(String tag) {
            return getSentenceForTag(tag, new String[0]);
        }

        private Utterance getSentenceForTag(String tag, String[] params) {
            String template1 = translate("sentence." + tag);
            if (template1 == null) {
                template1 = "{SUBJECT} " + tag + "{OPTIONAL_AGENCY}{OPTIONAL_PP_PLACE}{OPTIONAL_PP_DATE}.";
            }
//      System.err.println("getSentenceForTag: tag=" + tag + " => " + template1);
            Utterance u = Utterance.forTemplate(getResources(), template1, params);
            u.setSubject(indi);
            return u;
            // TODO: should also check config for cases, etc and set in Utterance
        }

        /**
         * Write a placename in a natural form for humans.
         *
         * @param prop
         */
        private String getPlaceString(Property prop, String preposition) {

            if (prop == null) {
                return "";
            }

            StringBuffer result = new StringBuffer();
            Property addr = prop.getProperty("ADDR");
            if (addr != null) {
                appendToPlace(result, addr);
                appendToPlace(result, addr.getProperty("ADR1"));
                appendToPlace(result, addr.getProperty("ADR2"));
                appendToPlace(result, addr.getProperty("CITY"));
                // POST is postal code - should have rules to know where it belongs
                appendToPlace(result, addr.getProperty("STAE"));
                appendToPlace(result, addr.getProperty("CTRY"));
            } else if (prop.getProperty("PLAC") != null) {
                result.append(prop.getProperty("PLAC").getValue());
            }
            if (result.length() == 0) {
                return "";
            }

            if (placeIndexTitle !=  null) {
                doc.addIndexTerm(placeIndexTitle, result.toString()); // sort key?
            }

            if (preposition == null) {
                String key = "prep.in_city";
                if (Character.isDigit(result.charAt(0))) {
                    key = "prep.at_street_address"; // likely street address (crude heuristic)
                }
                preposition = translate(key);
            }
            return " " + preposition + " " + result;
        }

        /**
         * Append , prop if not null.
         */
        private void appendToPlace(StringBuffer result, Property prop) {
            if (prop != null && !prop.getValue().isEmpty()) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(prop.getValue());
            }
        }
    }

}
