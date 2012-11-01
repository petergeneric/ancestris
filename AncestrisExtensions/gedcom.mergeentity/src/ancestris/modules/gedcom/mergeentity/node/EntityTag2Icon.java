package ancestris.modules.gedcom.mergeentity.node;

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
            put("INDI", "ancestris/modules/gedcom/mergeentity/resources/Indi.png");
            put("FAM", "ancestris/modules/gedcom/mergeentity/resources/Fam.png");
            put("OBJE", "ancestris/modules/gedcom/mergeentity/resources/Media.png");
            put("NOTE", "ancestris/modules/gedcom/mergeentity/resources/Note.png");
            put("SOUR", "ancestris/modules/gedcom/mergeentity/resources/Source.png");
            put("SUBM", "ancestris/modules/gedcom/mergeentity/resources/Submitter.png");
            put("REPO", "ancestris/modules/gedcom/mergeentity/resources/Repository.png");
            put("Unknown", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
        }
    };

    static String getIconFile(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return entity2Icon.get(entityTag);
        } else {
            return entity2Icon.get("Unknown");
        }
    }

    static Image getIcon(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return ImageUtilities.loadImage(entity2Icon.get(entityTag));
        } else {
            return ImageUtilities.loadImage(entity2Icon.get("Unknown"));
        }
    }
}
