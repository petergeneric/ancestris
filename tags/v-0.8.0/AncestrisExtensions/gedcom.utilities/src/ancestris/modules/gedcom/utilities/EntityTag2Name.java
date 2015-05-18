package ancestris.modules.gedcom.utilities;

import genj.gedcom.Gedcom;
import java.util.HashMap;

/**
 *
 * @author lemovice
 */
public class EntityTag2Name {

    private static final HashMap<String, String> entityTag2Name = new HashMap<String, String>() {

        {
            put("INDI", Gedcom.getName("INDI"));
            put("FAM", Gedcom.getName("FAM"));
            put("OBJE", Gedcom.getName("OBJE"));
            put("NOTE", Gedcom.getName("NOTE"));
            put("SOUR", Gedcom.getName("SOUR"));
            put("SUBM", Gedcom.getName("SUBM"));
            put("REPO", Gedcom.getName("REPO"));
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
