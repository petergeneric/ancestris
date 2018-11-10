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

import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import javax.swing.JScrollPane;

/**
 *
 * @author Zurga
 */
class OtherFamEntitiesPanel extends EntitiesPanel {

    public OtherFamEntitiesPanel(JScrollPane pane) {
        super(pane);
    }

    @Override
    public Entity[] getEntities(Property rootProperty) {
        if (rootProperty != null && rootProperty instanceof Indi) {
            return ((Indi) rootProperty).getFamiliesWhereSpouse(); // getPartners();  // getPartners gets individuals but null if partner not identified
        }
        return null;
    }
}
