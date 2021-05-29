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

import ancestris.modules.beans.AListBean;
import genj.gedcom.Property;
import javax.swing.JScrollPane;

/**
 *
 * @author Zurga
 */
abstract class EntitiesPanel extends AListBean {

    public EntitiesPanel(JScrollPane pane) {
        super();
        init(pane);
    }
    
    private final void init(JScrollPane pane) {
        setBackground(java.awt.Color.white);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
        pane.setViewportView(this);
    }

    public abstract Property[] getEntities(Property rootProperty);

    public void update(Property rootProperty, Property exclude, FamilyPanel fPanel) {
        removeAll();
        // This call should remove all anonymous listeners
        // https://stackoverflow.com/questions/8727752/does-disposing-of-container-remove-all-registered-listeners
        repaint();
        if (rootProperty != null) {
            add(getEntities(rootProperty), exclude, new ABeanHandler(fPanel));
        }
        revalidate();
    }
}
