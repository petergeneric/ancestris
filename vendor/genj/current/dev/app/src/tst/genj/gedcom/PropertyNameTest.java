/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import junit.framework.TestCase;

/**
 * Testing PropertyName
 */
public class PropertyNameTest extends TestCase {

  /**
   * Test value encodings 
   */
  public void testValues() {     
    
    PropertyName name = new PropertyName();

    String 
    	first = "Nils",
    	last  = "Meier",
    	suff  = "jr.",
    	value; 

    // "Nils /Meier/ jr."
    name.setName(first, last, suff);
    testName(name, first, last, suff, first+" /"+last+"/ "+suff);
    
    // "Nils"
    name.setName(first, "", "");
    testName(name, first, "", "", first);
    
    // "/Meier/"
    name.setName("", last, "");
    testName(name, "", last, "", "/"+last+"/");
    
    // "// jr."
    name.setName("", "", suff);
    testName(name, "", "", suff, "// "+suff);
    
    // done
  }
  
  private void testName(PropertyName name, String first, String last, String suffix, String value) {
    assertEquals("expected first "+first, first, name.getFirstName());
    assertEquals("expected last "+last, last, name.getLastName());
    assertEquals("expected "+suffix, suffix, name.getSuffix());
    assertEquals("expected "+value, value, name.getValue());
  }
  
} //PropertyNameTest
