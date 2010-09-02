/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.fo;

import java.util.regex.Matcher;

import junit.framework.TestCase;

/**
 * Test Case for testing our formatted Document
 */
public class DocumentTest extends TestCase {

  public void testAttributeRegEx() {

    // build testcase - key/value pairs
    String[] testcase = {
       "foo", "bar",
       "bar", " foo  ",
       "a", "rgb(255,0,128)",
       " b", "#ff00ff",
       "c", "rgb(0,0,128)",
       "bar-tst", " foo  ",
       "x:z", " foo  ",
       "tst", "(A,B,C)"
    };
    
    // assemble into parameter string
    StringBuffer buf = new StringBuffer();
    for (int i=0;i<testcase.length;i+=2) {
      buf.append(testcase[i+0]).append("=").append(testcase[i+1]).append(",");
    }
    
    // parse with Document attribute regular expression
    Matcher m = Document.REGEX_ATTR.matcher(buf.toString());
    for (int i=0;i<testcase.length;i+=2) {
      String key = testcase[i+0].trim();
      String value = testcase[i+1].trim();
      assertTrue("looking for "+key+"="+value, m.find());
      assertEquals(key, m.group(1).trim());
      assertEquals(value, m.group(2).trim());
    }

    // done
  }
  
}
