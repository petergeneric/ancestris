package ancestris.modules.commonAncestor;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

/**
 *
 * @author michel
 */
public class CommonAncestorTreeTest extends TestCase {

  public CommonAncestorTreeTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test of generate method, of class CommonAncestorTree.
   */
  public void testFindCommonAncestors1() {
    System.out.println("testFindCommonAncestors1");
    Indi indi1 = null;
    Indi indi2 = null;
    CommonAncestorTree instance = new CommonAncestorTree();
    Set<Indi> ancestorList = instance.findCommonAncestors(indi1, indi2);
    assertTrue("ancestorList must be not null", ancestorList != null);
    assertEquals("ancestorList is empty", 0, ancestorList.size());
    //fail("The test case is a prototype.");
  }

  /**
   * Test of generate method, of class CommonAncestorTree.
   */
  public void testFindCommonAncestors2() {
        try {
            System.out.println("testFindCommonAncestors2");
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi1 = null;
            Indi indi2 = null;
            indi1 = (Indi) gedcom.getEntity("INDI", "I3");
            indi2 = (Indi) gedcom.getEntity("INDI", "I4");

            CommonAncestorTree instance = new CommonAncestorTree();
            Set<Indi> ancestorList = instance.findCommonAncestors(indi1, indi2);
            assertEquals("ancestorList contains 1 record", 1, ancestorList.size());
        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
  }

  public void testCreateSVG1() {
        try {
            System.out.println("testCreateSVG1");
            
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi1 = null;
            Indi indi2 = null;
            indi1 = (Indi) gedcom.getEntity("INDI", "I3");
            indi2 = (Indi) gedcom.getEntity("INDI", "I4");

            CommonAncestorTree instance = new CommonAncestorTree();
            //Set<Indi> ancestorList = instance.findCommonAncestors(indi1, indi2);
            Indi ancestor = (Indi) gedcom.getEntity("INDI", "I1");
            //File outputFile = new File(System.getProperty("netbeans.user") + File.separator + "testCreateSVG1.svg");
            File outputFile = new File(System.getProperty("user.home") + File.separator + "testCreateSVG1.svg");
            if (outputFile.exists()) {
              outputFile.delete();
            }
            outputFile = new File(System.getProperty("user.home") + File.separator + "testCreateSVG1.svg");
            boolean displayedId = false;
            int husbandWifeFirst = 1;
            boolean displayRecentYear = true;
            String fileTypeName = "svg";
            instance.createCommonTree(indi1, indi2,ancestor, outputFile, displayedId, displayRecentYear, husbandWifeFirst, fileTypeName) ;     
            assertEquals("ancestorList contains 1 record", true, outputFile.exists());
            if (outputFile.exists()) {
              outputFile.delete();
            }
        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
  }
}
