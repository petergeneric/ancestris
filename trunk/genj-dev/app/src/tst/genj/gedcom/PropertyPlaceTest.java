/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Origin;
import junit.framework.TestCase;

/**
 * Testing PropertyPlace
 */
public class PropertyPlaceTest extends TestCase {
  
  private Gedcom gedcom;
  
  /**
   * Prepare a fake indi
   */
  protected void setUp() throws Exception {

    // create gedcom
    gedcom = new Gedcom(Origin.create("file://foo.ged"));

    // done
  }
  
  /**
   * Helper - create an individual
   */
  private Indi createIndi() {
    
    Indi indi = null;
    
    try {
      // create individual
      indi = (Indi)gedcom.createEntity("INDI");
      // .. with default sub-properties
      indi.addDefaultProperties();
    } catch (GedcomException e) {
      fail(e.getMessage());
    }
    
    // done
    return indi;
  }
  
  /**
   * Test place access'n stuff
   */
  public void testJurisdictiosn() {
    
    Indi indi = createIndi();
    Property birt = indi.addProperty("BIRT", "");
    
    test(birt, "Rendsburg, Schleswig Holstein, Deutschland", "Rendsburg", "Rendsburg", "Deutschland");
    test(birt, "Rendsburg", "Rendsburg", "Rendsburg", null);

    test(birt, "Backyard, The Hood, Rendsburg, Schleswig Holstein, Deutschland", "Backyard", "Backyard", "Rendsburg");
    gedcom.setPlaceFormat("backyard,neighbourhood,city,world");
    test(birt, "Backyard, The Hood, Rendsburg, Schleswig Holstein, Deutschland", "Rendsburg", "Backyard", "Rendsburg");
    
  }
  
  /**
   * test
   */
  private void test(Property event, String value, String city, String first, String third) {
    
    PropertyPlace plac = (PropertyPlace)event.addProperty("PLAC", value);
    
    assertEquals(city, plac.getCity());
    assertEquals(first, plac.getFirstAvailableJurisdiction());
    assertEquals(third, plac.getJurisdiction(2));
    
  }
  
} //PropertyDateTest
