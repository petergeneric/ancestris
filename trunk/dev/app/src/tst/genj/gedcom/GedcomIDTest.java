/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import junit.framework.TestCase;

/**
 * Testing Gedcom ID generation/handling
 */
public class GedcomIDTest extends TestCase {
  
  /**
   * Test IDs
   */
  public void testIDs() throws GedcomException {
    
    Gedcom gedcom = new Gedcom();
    
    // simple first indi
    assertID("I1", gedcom.createEntity(Gedcom.INDI, "I1"));
    
    // next nicely assigned?
    assertID("I2", gedcom.createEntity(Gedcom.INDI));
    
    // jump over next
    assertID("I4", gedcom.createEntity(Gedcom.INDI, "I4"));
    
    // catching up again (not the default behavior)?
    Options.getInstance().isFillGapsInIDs = true;
    assertID("I3", gedcom.createEntity(Gedcom.INDI));
    
    // do a padded one
    assertID("I000005", gedcom.createEntity(Gedcom.INDI, "I000005"));
    
    // knows to skip I5?
    assertID("I6", gedcom.createEntity(Gedcom.INDI));
    
    // 'duplicate' that should work
    assertID("I006", gedcom.createEntity(Gedcom.INDI, "I006"));
    
    // duplicate that should throw exception!
    try {
      gedcom.createEntity(Gedcom.INDI, "I006");
      fail("duplicate ID not caught!");
    } catch (GedcomException e) {
    }
      
    // done
  }
  
  private void assertID(String id, Entity e) {
    assertEquals(id, e.getId());
  }
  
} //GedcomIDTest
