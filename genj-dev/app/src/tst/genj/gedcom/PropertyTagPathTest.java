/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Origin;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test Property Base class with TagPaths
 */
public class PropertyTagPathTest extends TestCase {

  private Gedcom gedcom;
  
  private Indi husband, wife1, wife2;
  
  /**
   * Prepare a fake indi
   */
  protected void setUp() throws Exception {

    // create gedcom
    gedcom = new Gedcom(Origin.create("file://foo.ged"));

    // create individuals
    husband = (Indi)gedcom.createEntity("INDI");
    wife1 = (Indi)gedcom.createEntity("INDI");
    wife2 = (Indi)gedcom.createEntity("INDI");
    
    // .. with default sub-properties
    husband.addDefaultProperties(); 
    wife1.addDefaultProperties(); 
    wife2.addDefaultProperties(); 

    // connect
    addWife(husband, wife1);
    addWife(husband, wife2);
    
    // done
  }
  
  private void addWife(Indi husband, Indi wife) throws GedcomException {
    Fam fam = (Fam)gedcom.createEntity("FAM");
    ((PropertyXRef)fam.addProperty("HUSB", "@"+husband.getId()+"@")).link();
    ((PropertyXRef)fam.addProperty("WIFE", "@"+wife.getId()+"@")).link();
  }
  
  /**
   * Test path backtracking
   */
  public void testPathBacktracking() {
 
    // give husband two births
    // INDI
    //  BIRT
    //   DATE
    //  BIRT
    //   PLAC
    Property birt1 =husband.getProperty("BIRT");
    Property plac1 = birt1.getProperty("PLAC");
    
    Property birt2 = husband.addProperty("BIRT", "");
    Property plac2 = birt2.addProperty("PLAC", "2nd");

    assertProperty(husband, "INDI:BIRT:PLAC", plac1);
    birt1.delProperty(plac1);
    assertProperty(husband, "INDI:BIRT:PLAC", plac2);
    assertNull(husband.getProperty(new TagPath("INDI:BIRT:PLAC"), false));
  }
  
  /**
   * Test path matches that include selectors
   */
  public void testGetPropertyByPath() {
    
    assertProperty(husband, "INDI"                          , husband);
    assertProperty(husband, ".."                               ,null);
    assertProperty(husband, "."                                , husband);
    assertProperty(husband, "INDI:BIRT:DATE:..:..:BIRT:DATE", husband.getProperty(new TagPath("INDI:BIRT:DATE")));

    final Set wifes = new HashSet();
    new TagPath("INDI:FAMS:*:..:WIFE:*:..").iterate(husband, new PropertyVisitor() { 
      protected boolean leaf(Property leaf) {
        wifes.add(leaf);
        return true; // continue
      }
    });
    
    assertEquals("should reach two wifes", wifes.size(), 2);
    
    assertProperty(husband, "INDI:FAMS:*:..:WIFE:*:..", wife1);
    assertProperty(husband, "INDI:FAMS#0:*:..:WIFE:*:..", wife1);
    assertProperty(husband, "INDI:FAMS#1:*:..:WIFE:*:..", wife2);
    
    assertPath(husband, "INDI:FAMS#0");
    assertPath(husband, "INDI:FAMS#1");
    
    
  }
  
  private void assertPath(Property root, String path) {
    TagPath result = root.getProperty(new TagPath(path)).getPath(true);
    assertEquals(result, new TagPath(path));
    assertEquals(result.toString(), path);
  }

  /**
   * helper for checking result of root.getProperty(path)
   */
  private Property assertProperty(Property root, String path, Property prop) {
    Property result = root.getProperty(new TagPath(path));
    assertSame(result, prop);
    return result;
  }
  
} //PropertyTest
