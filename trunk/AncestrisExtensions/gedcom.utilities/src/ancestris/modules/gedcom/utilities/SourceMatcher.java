package ancestris.modules.gedcom.utilities;

import genj.gedcom.Source;

/**
 *
 * @author lemovice
 */
public class SourceMatcher extends EntityMatcher<Source> {

    @Override
    public int compare(Source left, Source right) {
        if (left.getTitle().equals(right.getTitle())) {
            if (left.getText().equals(right.getText())) {
                return 100;
            } else {
                return 50;
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Source entity) {
        return new String[] {entity.getTitle()};
    }
}
