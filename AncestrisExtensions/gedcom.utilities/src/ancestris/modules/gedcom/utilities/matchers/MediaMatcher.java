package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.Media;
import java.io.File;

/**
 *
 * @author frederic
 */
public class MediaMatcher extends EntityMatcher<Media, MediaMatcherOptions> {

    
    public MediaMatcher() {
        super();
        this.options = new MediaMatcherOptions();
    }

    @Override
    public int compare(Media left, Media right) {
        String lT = left.getTitle();
        String rT = right.getTitle();
        if (lT != null && rT != null) {
            if (lT.equals(rT)) {
                File lF = left.getFile();
                File rF = right.getFile();
                if (lF != null && rF != null) {
                    if (lF.getAbsoluteFile().equals(rF)) {
                        return 100;
                    } else if (lF.getName().equals(rF.getName())) {
                        return 80;
                    }
                } else {
                    return 50;
                }
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Media entity) {
        return new String[]{entity.getValue()};
    }
}
