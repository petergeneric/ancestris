/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import junit.framework.TestCase;

/**
 * Test gedcom multiline handling
 */
public class PropertyMultilineValueTest extends TestCase {

  private final static TagPath MLPATH = new TagPath("INDI:NOTE");
  
  private final static String 
    CONT = "CONT",
    CONC = "CONC";

  /** one used mle */
  private PropertyMultilineValue mle;
  
  /** one iterator used */
  private MultiLineProperty.Iterator it;

  /**
   * setup next testing herein
   */
  protected void setUp() throws Exception {
    
    // setup 40 as value line break (CONC trigger)
    Options.getInstance().setValueLineBreak(40);
    
    // create one mle instance
    mle = new PropertyMultilineValue("TEST");
  }
  
  /**
   * Test display value computation
   */
  public void testDisplayValue() {
    
    // 20050714 changed my mind once again - the display value of a multiline property
    // should actually be the whole string and not something trimmed and modified (...)
    // If the UI needs to trim (e.g. PropertyTreeWidget) then it should do so by itself
    // This is particulary necessary since searching goes through getDisplayValue (can't
    // use getValue()) which made it miss information after ...
    mle.setValue("one\ntwo\nthree");
    assertEquals("wrong display value", ((Property)mle).getDisplayValue(), mle.getValue());
  }
  
  /**
   * Test simple iterator
   */
  public void testSimpleIterator() {

    // a simple case
    iterator("abcde");

    // check result
    assertLine(0, null, "abcde");    
    assertNoNext();
    
    // ok    
  }

  /**
   * Test multiline
   */
  public void testMultiline() {

    // a simple case
    iterator("abcde\nfghij");

    // check result
    assertLine(0, null, "abcde");    
    assertNext();
    assertLine(1, CONT, "fghij");    
    assertNoNext();

    // ok    
  }

  /**
   * Test valueLineBreak
   */
  public void testValueLineBreak() {

    // a simple case
    iterator("0123456789012345678901234567890123456789xyz");

    // check result
    assertLine(0, null, "0123456789012345678901234567890123456789");    
    assertNext();
    assertLine(1, CONC, "xyz");    
    assertNoNext();

    // ok    
  }

  /**
   * Test multiline spaces
   */
  public void testMultilineSpaces() {

    // a simple case
    iterator("\nabcde\n\n  fg  \nhi\n\n");

    // check result
    assertLine(0, null, "");    
    assertNext();
    assertLine(1, CONT, "abcde");    
    assertNext();
    assertLine(1, CONT, "");    
    assertNext();
    assertLine(1, CONT, "  fg  ");    
    assertNext();
    assertLine(1, CONT, "hi");    
    assertNext();
    assertLine(1, CONT, "");    
    assertNoNext();

    // ok    
  }
  
  /**
   * Test space at line breaks
   */
  public void testSpaceAtLineBreak() {

    // simple case where the break falls into space  |<-right here
    iterator("123 567 901 345 789 123 567 901 345 789 xxx");

    // check result
    assertLine(0, null, "123 567 901 345 789 123 567 901 345 78");    
    assertNext();
    assertLine(1, CONC, "9 xxx");    
    assertNoNext();

    // tougher case where the break is huge          |<-up to here
    iterator("1                                       xxx");

    // check result
    assertLine(0, null, "1");    
    assertNext();
    assertLine(1, CONC, "                                       x");
    assertNext();
    assertLine(1, CONC, "xx");    
    assertNoNext();
  }

  /**
   * Init an iterator for give value
   */
  private void iterator(String value) {

    // set mle value
    mle.setValue(value);

    // prepare iterator
    it = mle.getLineIterator();
    
  }

  /**
   * Assert checks on current iterator line
   */  
  private void assertLine(int indent, String tag, String value) {

    assertEquals("wrong indent",  indent, it.getIndent());
    if (tag!=null)
      assertEquals("wrong tag"   ,  tag   , it.getTag   ());
    assertEquals("wrong value" ,  value , it.getValue ());
    
  }
  
  /**
   * Assert there's more in iterator
   */  
  private void assertNext() {

    assertTrue  ("no next"  , it.next());
    
  }
  
  /**
   * Assert there's not more in iterator
   */  
  private void assertNoNext() {

    assertFalse("next"  , it.next());
    
  }
  

} //AnselCharsetTest
