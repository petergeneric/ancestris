/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package modules.editors.gedcomproperties.utils;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author frederic
 */
public class Utils {
    
    public static void CopyProperty(Property propertyFrom, Property propertyTo) {
        if (propertyFrom == null || propertyTo == null) {
            return;
        }

        List listProp = new LinkedList();
        Property[] propertiesA = propertyFrom.getProperties();
        listProp.addAll(Arrays.asList(propertiesA));
        Property propItemA = null;
        Property propItemB = (Property) propertyTo;
        Property lastpropItemB = null;
        TagPath tagPathA = propertyFrom.getPath();
        int len = tagPathA.length() + 1;

        while (listProp.size() > 0) {
            // manages list
            propItemA = (Property) ((LinkedList) listProp).removeFirst();
            Property[] subProps = propItemA.getProperties();
            listProp.addAll(0, Arrays.asList(subProps));
            // workout if we have changed level or tag
            tagPathA = propItemA.getPath();
            if (tagPathA.length() > len) {
                // we have moved down one level, move B to last property added
                propItemB = lastpropItemB;
                len = tagPathA.length();
            }
            while (tagPathA.length() < len) {
                // Otherwise we have moved up, move B to corresponding parent
                propItemB = propItemB.getParent();
                len--;
            }
            // copy the property
            String tag = propItemA.getTag();
            if (tag.equals("XREF")) {
                continue;
            }
            // Special treatment of NOTE entities which have text in entity tag rather
            // than as a subtag in Gedcom file although Ancestris stores this as a subtag
            if (tagPathA.toString().compareTo("NOTE:NOTE") == 0) {
                propertyTo.setValue(propItemA.getValue());
            } else {
                lastpropItemB = propItemB.addProperty(tag, propItemA.getValue());
            }
        }

    }

}
