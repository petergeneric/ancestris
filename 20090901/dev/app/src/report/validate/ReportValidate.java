/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.Submitter;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.report.Report;
import genj.util.EnvironmentChecker;
import genj.util.swing.Action2;
import genj.view.ViewContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A report that validates a Gedcom file and displays
 * anomalies and 'standard' compliancy issues
 */
public class ReportValidate extends Report {
  
  /** whether order of properties counts or not */
  public boolean isOrderDiscretionary = true;
  
  /** whether we consider an empty value to be valid */
  public boolean isEmptyValueValid = true;

  /** whether we consider 'private' information valid or not */
  public boolean isPrivateValueValid = true;

  /** whether we consider missing files as valid or not */
  public boolean isFileNotFoundValid = true;

  /** whether we consider underscore tags to be valid custom tags */
  public boolean isUnderscoreValid = true;

  /** whether we consider extramarital children (before MARR after DIV) to be valid */
  public boolean isExtramaritalValid = false;
  
  /** whether a place format is binding and has to be adhered to */
  public boolean isRelaxedPlaceFormat = false;

  /** options of reports are picked up via field-introspection */
  public int
    maxLife      = 95,
    minAgeMARR   = 15,
    maxAgeBAPM   =  6,
    minAgeRETI   = 45,
    minAgeFather = 14,
    minAgeMother = 16,
    maxAgeMother = 44;

  /** Jerome's checks that haven't made it yet

   [ ] individuals who are cremated more than MAX_BURRYING_OR_CREM years after they die
   [ ] families containing a man who has fathered a child (more than 9 months) after they have died
   [ ] age difference between husband and wife is not greater than SOME_VALUE.
   [ ] women who have given birth more than once within 9 months (discounting twins)

  **/

  private final static String[] LIFETIME_DATES = {
    "INDI:ADOP:DATE",
    "INDI:ADOP:DATE",
    "INDI:BAPM:DATE",
    "INDI:BAPL:DATE",
    "INDI:BARM:DATE",
    "INDI:BASM:DATE",
    "INDI:BLES:DATE",
    "INDI:CHRA:DATE",
    "INDI:CONF:DATE",
    "INDI:ORDN:DATE",
    "INDI:NATU:DATE",
    "INDI:EMIG:DATE",
    "INDI:IMMI:DATE",
    "INDI:CENS:DATE",
    "INDI:RETI:DATE"
  };

  /**
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Start for argument properties
   */
  public void start(Property[] props) {

    List issues = new ArrayList();

    if (props.length>0) {
      List tests = createTests(props[0].getGedcom());
  
      for (int i=0;i<props.length;i++) {
        TagPath path = props[i].getPath();
        test(props[i], path, props[i].getGedcom().getGrammar().getMeta(path), tests, issues);
      }
    }
    
    // show results
    results(props[0].getGedcom(), issues);
  }

  /**
   * Start for argument entity
   */
  public void start(Entity entity) {
    start(new Entity[]{ entity });
  }

  public void start(Entity[] entities) {

    Gedcom gedcom = entities[0].getGedcom();
    List tests = createTests(gedcom);

    List issues = new ArrayList();
    for (int i=0;i<entities.length;i++) {
      TagPath path = new TagPath(entities[i].getTag());
      test(entities[i], path, entities[i].getGedcom().getGrammar().getMeta(path), tests, issues);
    }

    // show results
    results(gedcom, issues);
  }

  /**
   * Start for argument gedcom
   */
  public void start(final Gedcom gedcom) {

    // prepare tests
    List tests = createTests(gedcom);
    List issues = new ArrayList();

    // test if there's a submitter
    if (gedcom.getSubmitter()==null) {
      final ViewContext ctx = new ViewContext(gedcom);
      ctx.setText(translate("err.nosubmitter", gedcom.getName())).setImage(Gedcom.getImage());
      ctx.addAction(new Action2(translate("fix")) {
        protected void execute() {
          setEnabled(false);
          gedcom.doMuteUnitOfWork(new UnitOfWork() {
            public void perform(Gedcom gedcom) throws GedcomException {
              Submitter sub = (Submitter)gedcom.createEntity(Gedcom.SUBM);
              sub.setName(EnvironmentChecker.getProperty(ReportValidate.this, "user.name", "?", "using user.name for fixing missing submitter"));
            }
          });
        }
      });
      issues.add(ctx);
    }

    // Loop through entities and test 'em
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      for (Iterator es=gedcom.getEntities(Gedcom.ENTITIES[t]).iterator();es.hasNext();) {
        Entity e = (Entity)es.next();
        TagPath path = new TagPath(e.getTag());
        test(e, path, gedcom.getGrammar().getMeta(path), tests, issues);
      }
    }

    // show results
    results(gedcom, issues);
  }
  
  /**
   * show validation results
   */
  private void results(Gedcom gedcom, List issues) {

    // any fixes proposed at all?
    if (issues.size()==0) {
      getOptionFromUser(translate("noissues"), Report.OPTION_OK);
      return;
    }

    // show fixes
    showAnnotationsToUser(gedcom, translate("issues", Integer.toString(issues.size())), issues);

    // done
  }

  /**
   * Test a property (recursively)
   */
  private void test(Property prop, TagPath path, MetaProperty meta, List tests, List issues) {
    // test tests
    for (int i=0, j=tests.size(); i<j; i++) {
      Test tst = (Test)tests.get(i);
      // applicable?
      if (!tst.applies(prop, path))
        continue;
      // test it
      tst.test(prop, path, issues, this);
      // next
    }
    // don't recurse into custom underscore tags
    if (isUnderscoreValid&&prop.getTag().startsWith("_"))
      return;
    // recurse into all its properties
    for (int i=0,j=prop.getNoOfProperties();i<j;i++) {
      // for non-system, non-transient children
      Property child = prop.getProperty(i);
      if (child.isTransient())
        continue;
      // get child tag
      String ctag = child.getTag();
      // check if it's a custom tag
      if (isUnderscoreValid&&ctag.startsWith("_"))
        continue;
      // check if Gedcom grammar allows it
      if (!meta.allows(ctag)) {
        String msg = translate("err.notgedcom", new String[]{ctag, prop.getGedcom().getGrammar().getVersion(), path.toString()});
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
  private List createTests(Gedcom gedcom) {

    List result = new ArrayList();

    // ******************** SPECIALIZED TESTS *******************************
    
    // singleton properties
    result.add(new TestCardinality());

    // unique IDs
    result.add(new TestUniqueIDs());

    // non-valid properties
    result.add(new TestValid(this));

    // spouses with wrong gender
    result.add(new TestSpouseGender());

    // non existing files
    if (!isFileNotFoundValid)
      result.add(new TestFile());
    
    result.add(new TestFamilyClone());
    
    result.add(new TestBiologicalChild());

    // order of FAMS
    if (!isOrderDiscretionary)
      result.add(new TestOrder("INDI", "FAMS", "FAMS:*:..:MARR:DATE"));
    
    // order of CHILdren
    if (!isOrderDiscretionary)
      result.add(new TestOrder("FAM", "CHIL", "CHIL:*:..:BIRT:DATE"));
    
    // place format
    if (!isRelaxedPlaceFormat)
      result.add(new TestPlace(gedcom));

    // ****************** DATE COMPARISON TESTS *****************************

    // birth after death
    result.add(new TestDate("INDI:BIRT:DATE",TestDate.AFTER  ,"INDI:DEAT:DATE"));

    // burial before death
    result.add(new TestDate("INDI:BURI:DATE",TestDate.BEFORE ,"INDI:DEAT:DATE"));

    // events before birth
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.BEFORE ,"INDI:BIRT:DATE"));

    // events after death
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.AFTER  ,"INDI:DEAT:DATE"));

    // divorce before marriage
    result.add(new TestDate("FAM:DIV:DATE" ,TestDate.BEFORE ,"FAM:MARR:DATE"));

    // marriage outside lifespan of husband/wife
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:HUSB:*:..:DEAT:DATE"));
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:WIFE:*:..:DEAT:DATE"));
    result.add(new TestDate("FAM:MARR:DATE", TestDate.BEFORE , "FAM:HUSB:*:..:BIRT:DATE"));
    result.add(new TestDate("FAM:MARR:DATE", TestDate.BEFORE , "FAM:WIFE:*:..:BIRT:DATE"));

    // childbirth after death of mother
    result.add(new TestDate("FAM:CHIL"      ,"*:..:BIRT:DATE", TestDate.AFTER  ,"FAM:WIFE:*:..:DEAT:DATE"));

    // childbirth before marriage / after div
    if (!isExtramaritalValid) {
	    result.add(new TestDate("FAM:CHIL"      ,"*:..:BIRT:DATE", TestDate.BEFORE ,"FAM:MARR:DATE"));
	    result.add(new TestDate("FAM:CHIL"      ,"*:..:BIRT:DATE", TestDate.AFTER  ,"FAM:DIV:DATE"));
      result.add(new TestExists("FAM:CHIL", ".", "..:MARR"));
    }

    // ************************* AGE TESTS **********************************

    // max lifespane
    if (maxLife>0)
      result.add(new TestAge ("INDI:DEAT:DATE","..:..", TestAge.OVER ,   maxLife, "maxLife"  ));

    // max BAPM age
    if (maxAgeBAPM>0)
      result.add(new TestAge ("INDI:BAPM:DATE","..:..", TestAge.OVER ,maxAgeBAPM,"maxAgeBAPM"));

    // max CHRI age
    if (maxAgeBAPM>0)
      result.add(new TestAge ("INDI:CHRI:DATE","..:..", TestAge.OVER ,maxAgeBAPM,"maxAgeBAPM"));

    // min RETI age
    if (minAgeRETI>0)
      result.add(new TestAge ("INDI:RETI:DATE","..:..", TestAge.UNDER,minAgeRETI,"minAgeRETI"));

    // min MARR age of husband, wife
    if (minAgeMARR>0)
      result.add(new TestAge ("FAM:MARR:DATE" ,"..:..:HUSB:*:..", TestAge.UNDER  ,minAgeMARR,"minAgeMARR"));
    if (minAgeMARR>0)
      result.add(new TestAge ("FAM:MARR:DATE" ,"..:..:WIFE:*:..", TestAge.UNDER  ,minAgeMARR,"minAgeMARR"));

    // min/max age for father, mother
    if (minAgeMother>0)
      result.add(new TestAge ("FAM:CHIL", "*:..:BIRT:DATE" ,"..:WIFE:*:..", TestAge.UNDER,minAgeMother,"minAgeMother"));
    if (maxAgeMother>0)
      result.add(new TestAge ("FAM:CHIL", "*:..:BIRT:DATE" ,"..:WIFE:*:..", TestAge.OVER ,maxAgeMother,"maxAgeMother"));
    if (minAgeFather>0)
      result.add(new TestAge ("FAM:CHIL", "*:..:BIRT:DATE" ,"..:HUSB:*:..", TestAge.UNDER,minAgeFather,"minAgeFather"));


    // **********************************************************************
    return result;
  }

} //ReportValidate