package ancestris.modules.gedcom.utilities;

import genj.gedcom.Submitter;

/**
 *
 * @author lemovice
 */
public class SubmitterMatcher extends EntityMatcher<Submitter> {

    @Override
    public int compareEntities(Submitter left, Submitter right) {
        if (left.getName().equals(right.getName())) {
            if (left.getCountry().equals(right.getCountry())) {
                if (left.getCity().equals(right.getCity())) {
                    return 100;
                }
                return 80;
            }
            return 50;
        }
        return 0;
    }
}
