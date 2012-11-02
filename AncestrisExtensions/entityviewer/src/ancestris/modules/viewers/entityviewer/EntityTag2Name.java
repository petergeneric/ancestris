package ancestris.modules.viewers.entityviewer;

import java.util.HashMap;
import org.openide.util.NbBundle;

/**
 *
 * @author lemovice
 */
public class EntityTag2Name {

    private static final HashMap<String, String> entityTag2Name = new HashMap<String, String>() {

        {
            put("INDI", NbBundle.getMessage(this.getClass(), "entityTag.INDI"));
            put("FAM", NbBundle.getMessage(this.getClass(), "entityTag.FAM"));
            put("OBJE", NbBundle.getMessage(this.getClass(), "entityTag.OBJE"));
            put("NOTE", NbBundle.getMessage(this.getClass(), "entityTag.NOTE"));
            put("SOUR", NbBundle.getMessage(this.getClass(), "entityTag.SOUR"));
            put("SUBM", NbBundle.getMessage(this.getClass(), "entityTag.SUBM"));
            put("REPO", NbBundle.getMessage(this.getClass(), "entityTag.REPO"));
        }
    };

    static public String getTagName(String entityTag) {
        if (entityTag2Name.get(entityTag) != null) {
            return entityTag2Name.get(entityTag);
        } else {
            return entityTag2Name.get(entityTag);
        }
    }
}
