/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Base64;
import junit.framework.TestCase;

/**
 * Testing PropertyName
 */
public class PropertyBlobTest extends TestCase {
  
  private PropertyBlob blob = new PropertyBlob();

  /**
   * Test dates
   */
  public void testBlob() {     

    String data = Base64.encode("this is a blob".getBytes());;
    blob.setValue(data);
    
    // blob data is the original data stuffed in
    assertEquals( "blob data got lost", data, Base64.encode(blob.getBlobData()));
    
    // get value is not going to be the whole data (that needs to be accessed via multiline)
    assertTrue( "blob data leaking", !data.equals(blob.getValue()));

    // multiline works fine even if we reset the line iterator value
    MultiLineProperty.Iterator lines = blob.getLineIterator();
    lines.setValue("this is an attempt to overwrite the blob data in a PropertyBlob.iterator");

    // blob data still the same
    assertEquals( "blob data iterator got overwritten", data, lines.getValue());

    
  }
  
  
} //PropertyBlobTest
