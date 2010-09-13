package report.narrative;

import java.io.IOException;
import java.util.Locale;

/**
 * Test for ReportNarrative in German.
 */
public class ReportNarrativeFrenchTest extends ReportNarrativeTest {

  public void testAncestorsFr() throws IOException {
    testAncestors(Locale.FRANCE);
  }

  public void testDescendantsFr() throws IOException {
    testDescendants(Locale.FRANCE);
  }

}