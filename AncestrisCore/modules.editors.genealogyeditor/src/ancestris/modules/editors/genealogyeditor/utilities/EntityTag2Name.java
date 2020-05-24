package ancestris.modules.editors.genealogyeditor.utilities;

import genj.gedcom.Gedcom;
import java.util.HashMap;

/**
 *
 * @author lemovice
 */
public class EntityTag2Name {

    private static final HashMap<String, String> ENTITY_TAG_TO_NAME = new HashMap<String, String>() {

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
        if (ENTITY_TAG_TO_NAME.get(entityTag) != null) {
            return ENTITY_TAG_TO_NAME.get(entityTag);
        } else {
            return entityTag;
        }
    }
}
