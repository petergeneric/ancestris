package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.Note;

/**
 *
 * @author lemovice
 */
public class NoteMatcher extends EntityMatcher<Note, NoteMatcherOptions> {

    
    public NoteMatcher() {
        super();
        this.options = new NoteMatcherOptions();
    }

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
