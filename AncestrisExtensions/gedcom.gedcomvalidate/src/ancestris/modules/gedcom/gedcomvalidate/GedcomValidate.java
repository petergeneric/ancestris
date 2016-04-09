/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.util.Validator;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.view.ViewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import static ancestris.modules.gedcom.gedcomvalidate.Bundle.*;

/**
 * A report that validates a Gedcom file and displays anomalies and 'standard'
 * compliancy issues
 */
public class GedcomValidate implements Validator {

    Preferences modulePreferences = NbPreferences.forModule(Gedcom.class);

    /**
     * whether order of properties counts or not
     */
    public boolean isOrderDiscretionary = modulePreferences.getBoolean("isOrderDiscretionary", true);
    /**
     * whether we consider an empty value to be valid
     */
    public boolean isEmptyValueValid = modulePreferences.getBoolean("isEmptyValueValid", true);
    /**
     * whether we consider 'private' information valid or not
     */
    public boolean isPrivateValueValid = modulePreferences.getBoolean("isPrivateValueValid", true);
    /**
     * whether we consider missing files as valid or not
     */
    public boolean isFileNotFoundValid = modulePreferences.getBoolean("isFileNotFoundValid", true);
    /**
     * whether we consider underscore tags to be valid custom tags
     */
    public boolean isUnderscoreValid = modulePreferences.getBoolean("isUnderscoreValid", true);
    /**
     * whether we consider extramarital children (before MARR after DIV) to be
     * valid
     */
    public boolean isExtramaritalValid = modulePreferences.getBoolean("isExtramaritalValid", true);
    /**
     * whether a place format is binding and has to be adhered to
     */
    public boolean isRelaxedPlaceFormat = modulePreferences.getBoolean("isRelaxedPlaceFormat", false);
    /**
     * whether Same Sex families are allowed
     */
    public boolean isSameSexFamValid = modulePreferences.getBoolean("isSameSexFamValid", false);
    /**
     * options of reports are picked up via field-introspection
     */
    public int maxLife = modulePreferences.getInt("maxLife", 120);
    public int minAgeMARR = modulePreferences.getInt("minAgeMARR", 12);
    public int maxAgeBAPM = modulePreferences.getInt("maxAgeBAPM", 120);
    public int minAgeRETI = modulePreferences.getInt("minAgeRETI", 30);
    public int minAgeFather = modulePreferences.getInt("minAgeFather", 14);
    public int minAgeMother = modulePreferences.getInt("minAgeMother", 10);
    public int maxAgeMother = modulePreferences.getInt("maxAgeMother", 48);
    public PointInTime minYear = new PointInTime(0, 0, modulePreferences.getInt("minYear", 1));
    public PointInTime maxYear = new PointInTime(0, 0, modulePreferences.getInt("maxYear", 3000));
    /* TODO
     *
     * [ ] age difference between husband and wife is not greater than SOME_VALUE.
     * [ ] age difference between siblings is not greater than SOME_VALUE.
    
     * [ ] individuals who are cremated more than MAX_BURRYING_OR_CREM years after they die
    
     * [ ] families containing a man who has fathered a child (more than 9 months) after they have died
     * [ ] women who have given birth more than once within 9 months (discounting twins)
     *
     */
    static public String[] ALL_EVENTS = new String[] {
        "INDI:BIRT", "INDI:CHR",  "FAM:MARR",  "INDI:OCCU", "INDI:RESI", "FAM:RESI",  "INDI:RETI", "INDI:DEAT", "INDI:BURI",
        "INDI:BAPM", "INDI:BLES", "INDI:ADOP", "INDI:FCOM", "INDI:BARM", "INDI:BASM", "INDI:CONF", "INDI:NATI", "INDI:RELI", 
        "INDI:CAST", "INDI:CHRA", "INDI:GRAD", "INDI:ORDN", "FAM:ENGA",  "FAM:MARB",  "FAM:MARL",  "FAM:MARS",  "FAM:MARC", 
        "FAM:DIVF",  "FAM:DIV",   "FAM:ANUL",  "INDI:EMIG", "INDI:IMMI", "INDI:NATU", "INDI:TITL", "INDI:SSN",  "INDI:IDNO", 
        "INDI:DSCR", "INDI:EDUC", "INDI:CENS", "FAM:CENS",  "INDI:PROP", "FAM:EVEN",  "INDI:EVEN", "INDI:FACT", "INDI:PROB", "INDI:WILL", "INDI:CREM"
    };
    
    private final static String[] LIFETIME_DATES = new String[ALL_EVENTS.length];
    private boolean cancel;
    private int entitiesNumber;
    private int entitiesCounter;
    private String entityType;
    private Gedcom gedcom;

    public GedcomValidate() {
        // prepare lifetime tags
        for (int i = 0 ; i< ALL_EVENTS.length ; i++) {
            LIFETIME_DATES[i] = ALL_EVENTS[i] + ":DATE";
        }
        
    }

    /**
     * Start for argument gedcom
     */
    @Override
    public List<ViewContext> start(Gedcom gedcom) {

        this.gedcom = gedcom;
        
        // prepare tests
        final List<Test> tests = createTests(gedcom);
        final List<ViewContext> issues = new ArrayList<ViewContext>();

        // Loop through entities and test 'em
        entitiesNumber = gedcom.getEntities().size();
        entitiesCounter = 0;
        entityType = "";

        cancel:
        for (String ENTITIES : Gedcom.ENTITIES) {
            entityType = ENTITIES;
            for (Entity e : gedcom.getEntities(ENTITIES)) {
                entitiesCounter++;
                if (cancel) {
                    break cancel;
                }
                TagPath path = new TagPath(e.getTag());
                test(e, path, gedcom.getGrammar().getMeta(path), tests, issues);
            }
        }

        return issues.isEmpty() ? null : issues;
    }

    /**
     * Start for argument entity
     */
    @Override
    public List<ViewContext> start(Entity e) {
        if (e == null ) {
            return null;
        }
        
        final List<Test> tests = createTests(e.getGedcom());
        final List<ViewContext> issues = new ArrayList<ViewContext>();
        
        TagPath path = new TagPath(e.getTag());
        test(e, path, e.getGedcom().getGrammar().getMeta(path), tests, issues);
        
        return issues.isEmpty() ? null : issues;
    }
    
    
    
    /**
     * Test a property (recursively)
     */
    private void test(Property prop, TagPath path, MetaProperty meta, List<Test> tests, List<ViewContext> issues) {
        // test tests
        for (int i = 0, j = tests.size(); i < j; i++) {
            Test tst = tests.get(i);
            // applicable?
            if (!tst.applies(prop, path)) {
                continue;
            }
            // test it
            tst.test(prop, path, issues, this);
            // next
        }
        // don't recurse into custom underscore tags
        if (isUnderscoreValid && prop.getTag().startsWith("_")) {
            return;
        }
        // recurse into all its properties
        for (int i = 0, j = prop.getNoOfProperties(); i < j; i++) {
            // for non-system, non-transient children
            Property child = prop.getProperty(i);
            if (child.isTransient()) {
                continue;
            }
            // get child tag
            String ctag = child.getTag();
            // check if it's a custom tag
            if (isUnderscoreValid && ctag.startsWith("_")) {
                continue;
            }
            // check if Gedcom grammar allows it
            if (!meta.allows(ctag)) {
                String msg = NbBundle.getMessage(this.getClass(), "err.notgedcom", ctag, prop.getGedcom().getGrammar().getVersion(), path.toString());
                issues.add(new ViewContext(child).setText(msg).setImage(MetaProperty.IMG_ERROR));
                continue;
            }
            // dive into
            test(child, new TagPath(path, ctag), meta.getNested(child.getTag(), false), tests, issues);
            // next child
        }
        // done
    }

    /**
     * Create the tests we're using
     */
    private List<Test> createTests(Gedcom gedcom) {

        List<Test> result = new ArrayList<Test>();

        // ******************** SPECIALIZED TESTS *******************************
        // singleton properties
        result.add(new TestCardinality());

        // unique IDs
        result.add(new TestUniqueIDs());

        // non-valid properties
        result.add(new TestValid(this));

        // year out of range
        result.add(new TestDateRangeValid(this));

        // spouses with wrong gender
        if (!isSameSexFamValid) {
            result.add(new TestSpouseGender());
        }

        // non existing files
        if (!isFileNotFoundValid) {
            result.add(new TestFile());
        }

        result.add(new TestFamilyClone());

        result.add(new TestBiologicalChild());

        // order of FAMS
        if (!isOrderDiscretionary) {
            result.add(new TestOrder("INDI", "FAMS", "FAMS:*:..:MARR:DATE"));
        }

        // order of CHILdren
        if (!isOrderDiscretionary) {
            result.add(new TestOrder("FAM", "CHIL", "CHIL:*:..:BIRT:DATE"));
        }

        // place format
        if (!isRelaxedPlaceFormat) {
            result.add(new TestPlace(gedcom));
        }

        // ****************** DATE COMPARISON TESTS *****************************
        // birth after death
        result.add(new TestDate("INDI:BIRT:DATE", TestDate.AFTER, "INDI:DEAT:DATE"));

        // burial before death
        result.add(new TestDate("INDI:BURI:DATE", TestDate.BEFORE, "INDI:DEAT:DATE"));

        // events before birth
        result.add(new TestDate(LIFETIME_DATES, TestDate.BEFORE, "INDI:BIRT:DATE"));

        // events after death
        result.add(new TestDate(LIFETIME_DATES, TestDate.AFTER, "INDI:DEAT:DATE"));

        // divorce before marriage
        result.add(new TestDate("FAM:DIV:DATE", TestDate.BEFORE, "FAM:MARR:DATE"));

        // marriage outside lifespan of husband/wife
        result.add(new TestDate("FAM:MARR:DATE", TestDate.AFTER, "FAM:HUSB:*:..:DEAT:DATE"));
        result.add(new TestDate("FAM:MARR:DATE", TestDate.AFTER, "FAM:WIFE:*:..:DEAT:DATE"));
        result.add(new TestDate("FAM:MARR:DATE", TestDate.BEFORE, "FAM:HUSB:*:..:BIRT:DATE"));
        result.add(new TestDate("FAM:MARR:DATE", TestDate.BEFORE, "FAM:WIFE:*:..:BIRT:DATE"));

        // childbirth after death of mother
        result.add(new TestDate("FAM:CHIL", "*:..:BIRT:DATE", TestDate.AFTER, "FAM:WIFE:*:..:DEAT:DATE"));

        // childbirth before marriage / after div
        if (!isExtramaritalValid) {
            result.add(new TestDate("FAM:CHIL", "*:..:BIRT:DATE", TestDate.BEFORE, "FAM:MARR:DATE"));
            result.add(new TestDate("FAM:CHIL", "*:..:BIRT:DATE", TestDate.AFTER, "FAM:DIV:DATE"));
            result.add(new TestExists("FAM:CHIL", ".", "..:MARR"));
        }

        // ************************* AGE TESTS **********************************
        // max lifespane
        if (maxLife > 0) {
            result.add(new TestAge("INDI:DEAT:DATE", "..:..", TestAge.OVER, maxLife, "maxLife"));
        }

        // max BAPM age
        if (maxAgeBAPM > 0) {
            result.add(new TestAge("INDI:BAPM:DATE", "..:..", TestAge.OVER, maxAgeBAPM, "maxAgeBAPM"));
        }

        // max CHR age
        if (maxAgeBAPM > 0) {
            result.add(new TestAge("INDI:CHR:DATE", "..:..", TestAge.OVER, maxAgeBAPM, "maxAgeBAPM"));
        }

        // min RETI age
        if (minAgeRETI > 0) {
            result.add(new TestAge("INDI:RETI:DATE", "..:..", TestAge.UNDER, minAgeRETI, "minAgeRETI"));
        }

        // min MARR age of husband, wife
        if (minAgeMARR > 0) {
            result.add(new TestAge("FAM:MARR:DATE", "..:..:HUSB:*:..", TestAge.UNDER, minAgeMARR, "minAgeMARR"));
        }
        if (minAgeMARR > 0) {
            result.add(new TestAge("FAM:MARR:DATE", "..:..:WIFE:*:..", TestAge.UNDER, minAgeMARR, "minAgeMARR"));
        }

        // min/max age for father, mother
        if (minAgeMother > 0) {
            result.add(new TestAge("FAM:CHIL", "*:..:BIRT:DATE", "..:WIFE:*:..", TestAge.UNDER, minAgeMother, "minAgeMother"));
        }
        if (maxAgeMother > 0) {
            result.add(new TestAge("FAM:CHIL", "*:..:BIRT:DATE", "..:WIFE:*:..", TestAge.OVER, maxAgeMother, "maxAgeMother"));
        }
        if (minAgeFather > 0) {
            result.add(new TestAge("FAM:CHIL", "*:..:BIRT:DATE", "..:HUSB:*:..", TestAge.UNDER, minAgeFather, "minAgeFather"));
        }

        // **********************************************************************
        return result;
    }

    @NbBundle.Messages({
        "# {0} - entity type being checked",
        "# {1} - entity number being checked",
        "validate.progress=Checking {0}, Entity No {1}",
        "# {0} - gedcom name",
        "validate.title=Checking {0}"
    })

    @Override
    public void cancelTrackable() {
        cancel = true;
    }

    @Override
    public int getProgress() {
        return 100 * entitiesCounter / entitiesNumber;
    }

    @Override
    public String getState() {
        return validate_progress(entityType, entitiesNumber);
    }

    @Override
    public String getTaskName() {
        String name = gedcom == null ? "" : gedcom.getName();
        return validate_title(name);
    }
} //GedcomValidate

