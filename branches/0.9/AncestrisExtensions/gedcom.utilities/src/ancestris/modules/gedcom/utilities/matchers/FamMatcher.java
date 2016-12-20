package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lemovice
 */
public class FamMatcher extends EntityMatcher<Fam, FamMatcherOptions> {

    public FamMatcher() {
        super();
        this.options = new FamMatcherOptions();
    }

    @Override
    public int compare(Fam left, Fam right) {
        Indi leftHusband = left.getHusband();
        Indi rightHusband = right.getHusband();
        if (leftHusband != null && rightHusband != null) {
            IndiMatcherOptions indiMatcherOptions = new IndiMatcherOptions();
            indiMatcherOptions.setDateinterval(options.getDateinterval());
            if (new IndiMatcher().compare(leftHusband, rightHusband) >= 80) {
                Indi leftWife = left.getWife();
                Indi rightWife = right.getWife();
                if (leftWife != null && rightWife != null) {
                    if (new IndiMatcher().compare(leftWife, rightWife) >= 80) {
                        PropertyDate leftwhen = left.getMarriageDate();
                        PropertyDate rightwhen = right.getMarriageDate();

                        if (leftwhen != null && leftwhen.isComparable() && leftwhen != null && rightwhen.isComparable()) {
                            if (leftwhen.compareTo(rightwhen) <= options.getDateinterval()) {
                                return 100;
                            }
                        } else if (options.isEmptyValueValid()) {
                            return 100;
                        }
                        return 80;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Fam entity) {
        List<String> keys = new ArrayList<String>();
        List<PropertyName> husbandNames;
        List<PropertyName> wifeNames;
        if (entity.getHusband() != null) {
            husbandNames = entity.getHusband().getProperties(PropertyName.class);
        } else {
            husbandNames = new ArrayList<PropertyName>();
            husbandNames.add(new PropertyName("?", "?"));
        }
        if (entity.getWife() != null) {
            wifeNames = entity.getWife().getProperties(PropertyName.class);
        } else {
            wifeNames = new ArrayList<PropertyName>();
            wifeNames.add(new PropertyName("?", "?"));
        }
        for (Property husbandName : husbandNames) {
            for (Property wifeName : wifeNames) {
                keys.add(((PropertyName) husbandName).getFirstName() + ((PropertyName) wifeName).getFirstName());
            }
        }
        return keys.toArray(new String[0]);
    }
}
