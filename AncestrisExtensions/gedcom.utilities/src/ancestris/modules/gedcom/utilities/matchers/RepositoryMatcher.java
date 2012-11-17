package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.Property;
import genj.gedcom.Repository;

/**
 *
 * @author lemovice
 */
public class RepositoryMatcher extends EntityMatcher<Repository, RepositoryMatcherOptions> {

    public RepositoryMatcher() {
        super();
        this.options = new RepositoryMatcherOptions();
    }

    @Override
    public int compare(Repository left, Repository right) {
        Property leftName = left.getProperty("NAME");
        Property rightName = right.getProperty("NAME");
        if (leftName != null && rightName != null) {
            if (left.equals(rightName)) {
                return 100;
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Repository entity) {
        return new String[]{entity.getValue()};
    }
}
