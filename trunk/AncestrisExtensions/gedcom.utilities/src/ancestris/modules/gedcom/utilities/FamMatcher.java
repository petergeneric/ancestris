package ancestris.modules.gedcom.utilities;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lemovice
 */
public class FamMatcher extends EntityMatcher<Fam> {

    @Override
    public int compare(Fam left, Fam right) {
        Indi leftHusband = left.getHusband();
        Indi rightHusband = right.getHusband();
        if (leftHusband != null && rightHusband != null) {
            if (leftHusband.compareTo(rightHusband) == 0) {
                Indi leftWife = left.getWife();
                Indi rightWife = right.getWife();
                if (leftWife != null && rightWife != null) {
                    if (leftWife.compareTo(rightWife) == 0) {
                        PropertyDate leftwhen = left.getWhen();
                        PropertyDate rightwhen = right.getWhen();
                        if (leftwhen.isComparable() && rightwhen.isComparable()) {
                            if (leftwhen.compareTo(rightwhen) == 0) {
                                // should we compare chlidrens ?
                                return 100;
                            }
                        }
                        return 50;
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
            husbandNames = new ArrayList<PropertyName> ();
            husbandNames.add(new PropertyName("?", "?"));
        }
        if (entity.getWife() != null) {
                wifeNames = entity.getHusband().getProperties(PropertyName.class);
        } else {
            wifeNames = new ArrayList<PropertyName> ();
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
