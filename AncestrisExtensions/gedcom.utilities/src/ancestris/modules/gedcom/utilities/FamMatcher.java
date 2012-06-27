package ancestris.modules.gedcom.utilities;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;

/**
 *
 * @author lemovice
 */
public class FamMatcher extends EntityMatcher<Fam> {

    @Override
    public int compareEntities(Fam left, Fam right) {
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
}
