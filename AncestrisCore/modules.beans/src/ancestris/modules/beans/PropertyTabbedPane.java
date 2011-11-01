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
package ancestris.modules.beans;

import genj.edit.beans.PropertyBean;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author daniel
 */
public class PropertyTabbedPane extends JTabbedPane implements ChangeListener {

    private AListBean notesPanel = new AListBean();
    private AListBean sourcesPanel = new AListBean();
    private PropertyBean propertyPanel;
    private Property property;

    public PropertyTabbedPane(PropertyBean propertyPanel, String title, Icon icon, String tip) {
        super();
        this.propertyPanel = propertyPanel;
        this.propertyPanel.addChangeListener(this);

        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.PAGE_AXIS));
        sourcesPanel.setLayout(new BoxLayout(sourcesPanel, BoxLayout.PAGE_AXIS));


        addTab("<html><b>"+title+"</b></html>", icon, this.propertyPanel, tip);
        addTab(Gedcom.getName("NOTE",true), this.notesPanel);
        addTab(Gedcom.getName("SOUR",true), this.sourcesPanel);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.property = propertyPanel.getProperty();
        clearAll();
        if (this.property == null)
            return;
        notesPanel.add(property.getProperties("NOTE"),null,null);
        notesPanel.revalidate();

        sourcesPanel.add(property.getProperties("SOUR"),null,null);
        sourcesPanel.revalidate();
    }

    private void clearAll(){
        notesPanel.removeAll();
        notesPanel.repaint();

        sourcesPanel.removeAll();
        sourcesPanel.repaint();
    }

}
