package ancestris.modules.gedcom.utilities;

import java.awt.Image;
import java.util.HashMap;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lemovice
 */
public class EntityTag2Icon {

    private static final HashMap<String, String> entity2Icon = new HashMap<String, String>() {

        {
            put("INDI", "ancestris/modules/gedcom/utilities/resources/Individual.png");
            put("FAM", "ancestris/modules/gedcom/utilities/resources/Family.png");
            put("OBJE", "ancestris/modules/gedcom/utilities/resources/Media.png");
            put("NOTE", "ancestris/modules/gedcom/utilities/resources/Note.png");
            put("SOUR", "ancestris/modules/gedcom/utilities/resources/Source.png");
            put("SUBM", "ancestris/modules/gedcom/utilities/resources/Submitter.png");
            put("REPO", "ancestris/modules/gedcom/utilities/resources/Repository.png");
            put("Unknown", "ancestris/modules/gedcom/utilities/resources/Property.png");
        }
    };

    static public String getImageFileName(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return entity2Icon.get(entityTag);
        } else {
            return entity2Icon.get("Unknown");
        }
    }

    static public Image getImage(String entityTag) {
        if (entity2Icon.get(entityTag) != null) {
            return ImageUtilities.loadImage(entity2Icon.get(entityTag));
        } else {
            return ImageUtilities.loadImage(entity2Icon.get("Unknown"));
        }
    }

    static public Icon getIcon(String entityTag) {
        return ImageUtilities.image2Icon(getImage(entityTag));
    }
}
