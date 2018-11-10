/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.nav;

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import java.util.ArrayList;
import javax.swing.JScrollPane;

/**
 *
 * @author Zurga
 */
class EventsEntitiesPanel extends EntitiesPanel {

    public EventsEntitiesPanel(JScrollPane pane) {
        super(pane);
    }

    @Override
    public Property[] getEntities(Property rootProperty) {
        if (rootProperty != null && rootProperty instanceof Indi) {
            ArrayList<Property> result = new ArrayList<>(5);
            for (Property p : rootProperty.getProperties()) {
                if (p instanceof PropertyEvent) {
                    result.add(p);
                }
            }
            return result.toArray(new Property[]{});
        }
        return null;
    }

}
