/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.modules.beans.AListBean;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author daniel
 */
public class PropertyTabbedPane extends JTabbedPane {

    private AListBean notesPanel = new AListBean();
    private AListBean sourcesPanel = new AListBean();
    private JPanel propertyPanel;
//    private Property property;

    public PropertyTabbedPane(JPanel propertyPanel, String title, Icon icon, String tip) {
        super();
        this.propertyPanel = propertyPanel;
        addTab("<html><b>"+title+"</b></html>", icon, this.propertyPanel, tip);
        addTab("Notes", this.notesPanel);
        addTab("Sources", this.sourcesPanel);
    }
}
