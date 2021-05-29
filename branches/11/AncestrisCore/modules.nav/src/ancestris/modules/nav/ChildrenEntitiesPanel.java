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

import genj.gedcom.Fam;
import genj.gedcom.Property;
import javax.swing.JScrollPane;

/**
 *
 * @author Zurga
 */
class ChildrenEntitiesPanel extends EntitiesPanel {

    public ChildrenEntitiesPanel(JScrollPane pane) {
        super(pane);
    }

    @Override
    public Property[] getEntities(Property rootProperty) {
        if (rootProperty != null && rootProperty instanceof Fam) {
                    return ((Fam) rootProperty).getChildren();
                }
                return null;
    }
    
}
