/* 
 * 
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 * GraphInvariantTest.java
 *  $Header$
 */

package genj.gedcom;

import genj.io.GedcomReaderFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 * Graph Invariant Tests
 * 
 * Gedcom information contains links between records - in theory any combination of connectivity can be modelled but for GenJ's 
 * purposes we're not accepting directed circles
 * 
 * This test loads a file graphinvariants.ged which contains a spanning non-circle graph as follows and then performs all kinds
 * of tests on it:
 * <pre>
 *      6-3-7  8-4-9  10-5-11  12-6-13
 *          +      +           +-+    +-+
 *           2-1-3                 4-2-5
 *              +----+     +-----+
 *                       0-0-1
 * </pre>
 */
public class GraphInvariantTest extends TestCase {
  
  private Gedcom gedcom;

  /** setup a clean gedcom before every test */
  protected void setUp() throws IOException {
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // try to read our sample file
    gedcom = GedcomReaderFactory.createReader(getClass().getResourceAsStream("graphinvariants.ged"), null).read();
    
  }
  
  /** lookup individual */
  private Indi indi(int id) {
    return (Indi)gedcom.getEntity(Gedcom.INDI, Integer.toString(id));
  }
  
  /** lookup family */
  private Fam fam(int id) {
    return (Fam)gedcom.getEntity(Gedcom.FAM, Integer.toString(id));
  }

  /** link entity via tag and id to other */
  private void link(Entity entity, String tag, int id) throws GedcomException {
    PropertyXRef xref = (PropertyXRef)entity.addProperty(tag, "@"+id+"@");
    xref.link();
  }

  /**
   * Test for a loop condition introduced through FAMC
   */
  public void testCircleThroughFAMC() {

    // indi 0 shouldn't be able to link back to any family in his ancestor sub-tree through FAMC
    int[] ancestorsToCheck = { 0, 2, 3, 6, 7, 8, 9};
    int famToRevisit = 0;
    for (int i=0; i<ancestorsToCheck.length; i++)
      try {
        link(indi(ancestorsToCheck[i]), "FAMC", famToRevisit);
        fail("didn't recognize circle through FAMC between indi "+ancestorsToCheck[i]+" and family "+famToRevisit);
      } catch (GedcomException e) {
        // good!
      }
    
  }

  /**
   * Test for a loop condition introduced through CHIL
   */
  public void testCircleThroughCHIL() {

    // no family in a direct line under indi 6 shoul be able to link back through FAMC
    int[] famsToCheck = { 0, 1, 3 };
    for (int f=0; f<famsToCheck.length; f++)
      try {
        link(fam(famsToCheck[f]), "CHIL", 6);
        fail("didn't recognize circle through CHIL between family "+famsToCheck[f]+" and indi 6");
      } catch (GedcomException e) {
        // good!
      }

  }
  
  /**
   * Test for a loop condition introduced through HUSB
   */
  public void testCircleThroughHUSB() {
    
    gedcom.deleteEntity(indi(6));
    int fam = 3;
    int[] husbandsToCheck = { 2, 0 };
    for (int i=0; i<husbandsToCheck.length; i++)
      try {
        link(fam(fam), "HUSB", husbandsToCheck[i]);
        fail("didn't recognize circle through HUSB between family "+fam+" and indi "+husbandsToCheck[i]);
      } catch (GedcomException e) {
        // good!
      }
    
  }
  
  /**
   * Test for a loop condition introduced through WIFE
   */
  public void testCircleThroughWIFE() {
    
    gedcom.deleteEntity(indi(13));
    int fam = 6;
    int[] wifesToCheck = { 5, 1 };
    for (int i=0; i<wifesToCheck.length; i++)
      try {
        link(fam(fam), "WIFE", wifesToCheck[i]);
        fail("didn't recognize circle through WIFE between family "+fam+" and indi "+wifesToCheck[i]);
      } catch (GedcomException e) {
        // good!
      }
  }  
  
}