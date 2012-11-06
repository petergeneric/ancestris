package ancestris.modules.gedcom.utilities;

import genj.gedcom.Note;

/**
 *
 * @author lemovice
 */
public class NoteMatcher extends EntityMatcher<Note> {

    @Override
    public int compare(Note left, Note right) {
        if (left.getValue().equals(right.getValue())) {
            return 100;
        } else {
            return 0;
        }
    }

    @Override
    protected String[] getKeys(Note entity) {
        return new String[]{entity.getValue()};
    }
}
