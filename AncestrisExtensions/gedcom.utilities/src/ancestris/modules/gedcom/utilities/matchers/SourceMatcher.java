package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.Property;
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
        if (!left.getTitle().isEmpty() && left.getTitle().equals(right.getTitle())) {
            Property abbrLeft = left.getProperty("ABBR");
            Property abbrRight = right.getProperty("ABBR");
            if (abbrLeft != null && abbrRight != null 
                &&  !abbrLeft.getDisplayValue().isEmpty() 
                &&  !abbrRight.getDisplayValue().isEmpty() 
                &&  abbrRight.getDisplayValue().equals(abbrLeft.getDisplayValue())) {
                Property authLeft = left.getProperty("AUTH");
                Property authRight = right.getProperty("AUTH");
                if (authLeft != null && authRight != null
                        && !authLeft.getDisplayValue().isEmpty()
                        && !authRight.getDisplayValue().isEmpty()
                        && authRight.getDisplayValue().equals(authLeft.getDisplayValue())) {
                    if (!left.getText().isEmpty() && left.getText().equals(right.getText())) {
                        return 100;
                    } else {
                        return 90;
                    }
                } else {
                    return 80;
                }
            } else {
                return 50;
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Source entity) {
        return new String[]{entity.getTitle()};
    }
}
