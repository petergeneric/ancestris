package ancestris.modules.gedcom.matchers;

import genj.gedcom.Media;
import genj.gedcom.Property;

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
        Property fileLeft = left.getProperty("FILE");
        Property fileRight = right.getProperty("FILE");
        if (fileLeft != null && fileRight != null && !fileLeft.getValue().isEmpty() && fileLeft.getValue().equals(fileRight.getValue())) {
            String lT = left.getTitle();
            String rT = right.getTitle();
            if (lT != null && rT != null && !lT.isEmpty() && lT.equals(rT)) {
                return 100;
            } else {
                return 80;
            }
        }
        return 0;
    }

    @Override
    protected String[] getKeys(Media entity) {
        Property prop = entity.getProperty("FILE");
        return new String[]{prop != null ? prop.getValue() : entity.getTitle()};
    }
}
