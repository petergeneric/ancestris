/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.io;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.util.Origin;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;


/**
 * Test property read/write
 */
public class PropertyReadWriteTest extends TestCase {
  
  /** a sample gedcom */
  private Gedcom gedcom;
  
  /**
   * Prepare by setting up a gedcom object
   */
  protected void setUp() throws Exception {

    // create gedcom
    gedcom = new Gedcom(Origin.create("file://foo.ged"));

    // done
  }

  /**
   * Test property read/write
   */
  public void testReadWrite() throws Exception {
    
    // flatten properties
    Indi indi = (Indi)gedcom.createEntity(Gedcom.INDI);
    indi.addProperty("NAME", "Nils /Meier/");
    indi.addProperty("SEX", "M");
    indi.setValue(new TagPath("INDI:BIRT:DATE"), "25 May 1970");
    indi.setValue(new TagPath("INDI:BIRT:PLAC"), "Rendsburg");
    indi.setValue(new TagPath("INDI:BIRT:ADDR"), "one\ntwo");
    indi.setValue(new TagPath("INDI:BIRT:ADDR:CITY"), "Koln");
    indi.addProperty("NOTE", "This is a long note\nwith two lines");
    
    StringWriter out = new StringWriter();
    PropertyWriter writer = new PropertyWriter(out, false);
    for (int i=0;i<indi.getNoOfProperties();i++)
      writer.write(1, indi.getProperty(i));
    String left = out.toString();

    // restore properties
    indi = (Indi)gedcom.createEntity(Gedcom.INDI);
    new PropertyReader(new StringReader(left),null,false).read(indi);
    
    // flatten once more
    out = new StringWriter();
    writer = new PropertyWriter(out, false);
    for (int i=0;i<indi.getNoOfProperties();i++)
      writer.write(1, indi.getProperty(i));
    String right = out.toString();
    
    // compare
    assertEquals(left, right);

    // done
  }
}
