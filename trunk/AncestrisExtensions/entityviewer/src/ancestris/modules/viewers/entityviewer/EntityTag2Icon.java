package ancestris.modules.viewers.entityviewer;

import java.awt.Image;
import java.util.HashMap;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lemovice
 */
public class EntityTag2Icon {

    private static final HashMap<String, String> entity2Icon = new HashMap<String, String>() {

        {
            put("INDI", "ancestris/modules/viewers/entityviewer/resources/Individual.png");
            put("FAM", "ancestris/modules/viewers/entityviewer/resources/Family.png");
            put("OBJE", "ancestris/modules/viewers/entityviewer/resources/Media.png");
            put("NOTE", "ancestris/modules/viewers/entityviewer/resources/Note.png");
            put("SOUR", "ancestris/modules/viewers/entityviewer/resources/Source.png");
            put("SUBM", "ancestris/modules/viewers/entityviewer/resources/Submitter.png");
            put("REPO", "ancestris/modules/viewers/entityviewer/resources/Repository.png");
            put("Unknown", "ancestris/modules/viewers/entityviewer/resources/Property.png");
        }
    };

    static public String getIconFile(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return entity2Icon.get(entityTag);
        } else {
            return entity2Icon.get("Unknown");
        }
    }

    static public Image getIcon(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return ImageUtilities.loadImage(entity2Icon.get(entityTag));
        } else {
            return ImageUtilities.loadImage(entity2Icon.get("Unknown"));
        }
    }
}
