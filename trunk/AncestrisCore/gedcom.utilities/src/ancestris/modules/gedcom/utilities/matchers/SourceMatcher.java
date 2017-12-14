package ancestris.modules.gedcom.utilities.matchers;

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
            ret += 20;
        }

        Property abbrLeft = left.getProperty("ABBR");
        Property abbrRight = right.getProperty("ABBR");
        if (abbrLeft != null && abbrRight != null && !abbrLeft.getDisplayValue().isEmpty() && !abbrRight.getDisplayValue().isEmpty()
                && abbrRight.getDisplayValue().equals(abbrLeft.getDisplayValue())) {
            ret += 20;
        }

        Property authLeft = left.getProperty("AUTH");
        Property authRight = right.getProperty("AUTH");
        if (authLeft != null && authRight != null && !authLeft.getDisplayValue().isEmpty() && !authRight.getDisplayValue().isEmpty()
                && authRight.getDisplayValue().equals(authLeft.getDisplayValue())) {
            ret += 20;
        }

        Property pLeft = left.getProperty("REPO");
        Property pRight = right.getProperty("REPO");
        Property repoLeft = (pLeft != null && pLeft instanceof PropertyRepository) ? ((PropertyRepository) pLeft).getTargetEntity() : null;
        Property repoRight = (pRight != null && pRight instanceof PropertyRepository) ? ((PropertyRepository) pRight).getTargetEntity() : null;
        if (repoLeft != null && repoLeft.equals(repoRight)) {
            ret += 20;
        }
        
        if (!left.getText().isEmpty() && left.getText().equals(right.getText())) {
            ret += 20;
        }
        
        return ret;
    }

    @Override
    protected String[] getKeys(Source entity) {
        return new String[]{entity.getTitle()};
    }
}
