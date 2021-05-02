package ancestris.modules.gedcom.matchers;

import genj.gedcom.Submitter;

/**
 *
 * @author lemovice
 */
public class SubmitterMatcher extends EntityMatcher<Submitter, SubmitterMatcherOptions> {

    public SubmitterMatcher() {
        super();
        this.options = new SubmitterMatcherOptions();
    }

    @Override
    public int compare(Submitter left, Submitter right) {
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

    @Override
    protected String[] getKeys(Submitter entity) {
        return new String[]{entity.getName()};
    }
}
