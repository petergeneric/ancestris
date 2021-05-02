package ancestris.modules.gedcom.matchers;

import genj.gedcom.Property;
import genj.gedcom.PropertyRepository;
import genj.gedcom.Source;

/**
 *
 * @author lemovice
 */
public class SourceMatcher extends EntityMatcher<Source, SourceMatcherOptions> {

    public SourceMatcher() {
        super();
        this.options = new SourceMatcherOptions();
    }

    @Override
    public int compare(Source left, Source right) {
        int ret = 0;

        if (!left.getTitle().isEmpty() && left.getTitle().equals(right.getTitle())) {
            ret += 35;
        }

        Property abbrLeft = left.getProperty("ABBR");
        Property abbrRight = right.getProperty("ABBR");
        if (abbrLeft != null && abbrRight != null && !abbrLeft.getDisplayValue().isEmpty() && !abbrRight.getDisplayValue().isEmpty()
                && abbrRight.getDisplayValue().equals(abbrLeft.getDisplayValue())) {
            ret += 35;
        }

        Property authLeft = left.getProperty("AUTH");
        Property authRight = right.getProperty("AUTH");
        if (authLeft != null && authRight != null && !authLeft.getDisplayValue().isEmpty() && !authRight.getDisplayValue().isEmpty()
                && authRight.getDisplayValue().equals(authLeft.getDisplayValue())) {
            ret += 35;
        }

        Property pLeft = left.getProperty("REPO");
        Property pRight = right.getProperty("REPO");
        Property repoLeft = (pLeft != null && pLeft instanceof PropertyRepository) ? ((PropertyRepository) pLeft).getTargetEntity() : null;
        Property repoRight = (pRight != null && pRight instanceof PropertyRepository) ? ((PropertyRepository) pRight).getTargetEntity() : null;
        if (repoLeft != null && repoLeft.equals(repoRight)) {
            ret += 35;
        }

        String lS = left.getText();
        String rS = right.getText();
        if (lS != null && rS != null && !lS.isEmpty() && !rS.isEmpty()) {
            int s = (int) (similarity(lS, rS) * 40);
            ret += s;
        }

        return ret > 100 ? 100 : ret;
    }

    @Override
    protected String[] getKeys(Source entity) {
        return new String[]{entity.getTitle()};
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

}
