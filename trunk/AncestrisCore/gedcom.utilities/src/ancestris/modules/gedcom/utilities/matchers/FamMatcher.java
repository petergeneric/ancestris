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
        
        int score = 0;

        // Same husband ?
        Indi leftHusband = left.getHusband();
        Indi rightHusband = right.getHusband();
        if (leftHusband != null && rightHusband != null) {
            int m = new IndiMatcher().compare(leftHusband, rightHusband);
            if (m >= 80) {
                score += 30;
            } else if (m >= 60) {
                score += 20;
            }
        }

        // Same wife ?
        Indi leftWife = left.getWife();
        Indi rightWife = right.getWife();
        if (leftWife != null && rightWife != null) {
            int m = new IndiMatcher().compare(leftWife, rightWife);
            if (m >= 80) {
                score += 30;
            } else if (m >= 60) {
                score += 20;
            }
        }

        // Same date ?
        PropertyDate leftwhen = left.getMarriageDate();
        PropertyDate rightwhen = right.getMarriageDate();
        if (leftwhen != null && leftwhen.isComparable() && rightwhen != null && rightwhen.isComparable()) {
            if (leftwhen.compareTo(rightwhen) <= options.getDateinterval()) {
                score += 20;
            }
        } else if (options.isEmptyValueValid()) {
            score += 20;
        }

        // Same place ?
        if (compareMarriagePlace(left, right)) {
            score += 20;
        }

        return score;
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
    
    private boolean compareMarriagePlace(Fam leftFam, Fam rightFam) {
        Property leftFamMarrDate = leftFam.getProperty("MARR");
        Property rightFamMarrDate = rightFam.getProperty("MARR");

        if (leftFamMarrDate != null && rightFamMarrDate != null) {
            PropertyPlace rightFamPropertyPlace = (PropertyPlace) rightFamMarrDate.getProperty("PLAC");
            PropertyPlace leftFamPropertyPlace = (PropertyPlace) leftFamMarrDate.getProperty("PLAC");
            if (rightFamPropertyPlace != null && leftFamPropertyPlace != null) {
                if (rightFamPropertyPlace.compareTo(leftFamPropertyPlace) == 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    
}
