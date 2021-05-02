package ancestris.modules.gedcom.matchers;

import static ancestris.modules.gedcom.matchers.SourceMatcher.similarity;
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

        int ret = 0;
        String lS = left.getDisplayValue();
        String rS = right.getDisplayValue();
        if (lS != null && rS != null && !lS.isEmpty() && !rS.isEmpty()) {
            Double d = Math.pow(similarity(lS, rS), 4);  // increase curve to reduce similarity
            ret = (int) (d * 100);
        }
        return ret<0 ? 0 : ret;
    }

    @Override
    protected String[] getKeys(Note entity) {
        return new String[]{"NOTE"};
    }
    
    
}
