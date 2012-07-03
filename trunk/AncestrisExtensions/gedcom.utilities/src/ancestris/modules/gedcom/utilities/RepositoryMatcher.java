package ancestris.modules.gedcom.utilities;

import genj.gedcom.Property;
import genj.gedcom.Repository;

/**
 *
 * @author lemovice
 */
public class RepositoryMatcher extends EntityMatcher<Repository> {

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
}
