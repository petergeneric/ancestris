package report.narrative;

import java.io.IOException;
import java.util.Locale;

/**
 * Test for ReportNarrative in German.
 */
public class ReportNarrativeItalianTest extends ReportNarrativeTest {

  public void testDescendantsEn() throws IOException {
    testDescendants(Locale.ITALY);
  }   

  public void testAncestorsEn() throws IOException {
    testAncestors(Locale.ITALY);
    }
}