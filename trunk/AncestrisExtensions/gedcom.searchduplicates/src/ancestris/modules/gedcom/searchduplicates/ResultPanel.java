/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.searchduplicates;

import ancestris.modules.gedcom.utilities.matchers.PotentialMatch;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Registry;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ResultPanel extends javax.swing.JPanel {

    private Registry registry = null;
    private HashMap<String, Integer> tagMap = new HashMap<String, Integer>();
    
    private List<PropertyRow> propRows = new ArrayList<PropertyRow>();
        
    /**
     * Creates new form ResultPanel
     */
    public ResultPanel(Gedcom gedcom) {

        registry = gedcom.getRegistry();
        initSortMaps();
        initComponents();
        this.setPreferredSize(new Dimension(registry.get("searchDuplicatesWindowWidth", this.getPreferredSize().width), registry.get("searchDuplicatesWindowHeight", this.getPreferredSize().height)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();

        setMinimumSize(new java.awt.Dimension(50, 30));
        setPreferredSize(new java.awt.Dimension(300, 310));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("searchDuplicatesWindowWidth", evt.getComponent().getWidth());
        registry.put("searchDuplicatesWindowHeight", evt.getComponent().getHeight());

    }//GEN-LAST:event_formComponentResized


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables


    public void setEntities(PotentialMatch<? extends Entity> potentialMatch) {
        Entity leftEntity = potentialMatch.getLeft();
        Entity rightEntity = potentialMatch.getRight();
        propRows.clear();

        // Init IDs
        propRows.add(new PropertyRow(leftEntity.getPropertyName() + " " + NbBundle.getMessage(ResultPanel.class, "ResultPanel.ID"), leftEntity, rightEntity, false));

        // Get all first level tags and for each first level tag, get second level tags and fill in rows
        ArrayList<TagPath> firstLevelTagPaths = getTagPaths(leftEntity, rightEntity);
        Collections.sort(firstLevelTagPaths, new CompareTagPath());
        for (TagPath tagPath : firstLevelTagPaths) {
            Property[] leftProperties = leftEntity.getProperties(tagPath);
            Property[] rightProperties = rightEntity.getProperties(tagPath);
            int nbProperties = Math.max(leftProperties.length, rightProperties.length);
            // loop first level properties
            for (int index = 0; index < nbProperties; index++) {
                Property leftP = (index >= leftProperties.length) ? null : leftProperties[index];
                Property rightP = (index >= rightProperties.length) ? null : rightProperties[index];
                String label = getLabelFromProperty(leftP, rightP);
                // Add first level row with a separator
                propRows.add(new PropertyRow(label, leftP, rightP, true));
                ArrayList<TagPath> secondLevelTagPaths = getTagPaths(leftP, rightP);
                // loop second level properties
                for (TagPath tagPath2 : secondLevelTagPaths) {
                    Property leftP2 = leftEntity.getProperty(tagPath2);
                    Property rightP2 = rightEntity.getProperty(tagPath2);
                    String label2 = leftP2 != null ? leftP2.getPropertyName() : rightP2.getPropertyName();
                    // Add second level row with no separator
                    propRows.add(new PropertyRow(label2, leftP2, rightP2, false));
                }

            }
        }
        

        drawPanelElements();
    }

    private void drawPanelElements() {
        // Draw panel elements
        JPanel panel = new javax.swing.JPanel();
        GroupLayout panelLayout = new GroupLayout(panel);
        panel.setLayout(panelLayout);

        // Horizontal Groups
        ParallelGroup pg = panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        // Add group of 3 columns (label, col A, col B)
        SequentialGroup sgH = panelLayout.createSequentialGroup();
        ParallelGroup pgHH = panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING); // labels
        ParallelGroup pgHHA = panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING); // col A
        ParallelGroup pgHHB = panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING); // col B
        for (PropertyRow row : propRows) {
            pgHH.addComponent(row.label); //, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE);
            pgHHA.addComponent(row.checkPropA, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE);
            pgHHB.addComponent(row.checkPropB, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE);
        }
        sgH.addContainerGap();
        sgH.addGroup(pgHH);
        sgH.addGap(18, 18, 18);
        sgH.addGroup(pgHHA);
        sgH.addGap(18, 18, 18);
        sgH.addGroup(pgHHB);
        sgH.addContainerGap();
        pg.addGroup(sgH);
        // Add separators
        for (PropertyRow row : propRows) {
            if (row.separator) {
                sgH = panelLayout.createSequentialGroup();
                sgH.addContainerGap();
                sgH.addComponent(row.jSeparator);
                sgH.addContainerGap();
                pg.addGroup(sgH);
            }
        }
        panelLayout.setHorizontalGroup(pg);

        // Vertical Groups
        SequentialGroup sgV = panelLayout.createSequentialGroup();
        sgV.addContainerGap();
        for (PropertyRow row : propRows) {
            if (row.separator) {
                sgV.addComponent(row.jSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE);
            }
            sgV.addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(row.label)
                    .addComponent(row.checkPropA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(row.checkPropB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
            sgV.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        }
        sgV.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        panelLayout.setVerticalGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(sgV));

        // Add main panel with the data
        scrollPane.setViewportView(panel);
        return;
    }
    

    public List<Property> getSelectedProperties() {
        List<Property> selectedProperties = new ArrayList<Property>();
        for (PropertyRow row : propRows) {
            if (row.checkPropB.isSelected()) {
                selectedProperties.add(row.propB);
            }
        }
        return selectedProperties;
    }
    

    /**
     * Tools
     */
    
    private ArrayList<TagPath> getTagPaths(Property pLeft, Property pRight) {
        ArrayList<TagPath> tagPaths = new ArrayList<TagPath>();
        if (pLeft != null) {
            for (Property property : pLeft.getProperties()) {
                if (!tagPaths.contains(property.getPath())) {
                    tagPaths.add(property.getPath());
                }
            }
        }
        if (pRight != null) {
            for (Property property : pRight.getProperties()) {
                if (!tagPaths.contains(property.getPath())) {
                    tagPaths.add(property.getPath());
                }
            }
        }
        return tagPaths;
    }
    
    private String getLabelFromProperty(Property leftP, Property rightP) {
        String ret = "";
        Property p = leftP != null ? leftP : rightP;
        if (p.getTag().equals("FAMC")) {  // replace labels which is too long
            ret = NbBundle.getMessage(ResultPanel.class, "ResultPanel.Parents");
        } else if (p.getTag().equals("FAMS")) { // replace labels which is too long
            ret = NbBundle.getMessage(ResultPanel.class, "ResultPanel.Spouse");
        } else if (p.getTag().equals("XREF")) { // replace labels which would show XREF otherwise
            ret = NbBundle.getMessage(ResultPanel.class, "ResultPanel.Reference");
        } else {
            String str = p.getPropertyName();
            int i = str.indexOf(" ");
            ret = str.substring(0, i != -1 ? i : str.length());  // Take only first word
        }
        return ret;
    }

    public String getTextFromProperty(Property p) {
        String ret = "-";
        if (p != null) {
            if (p instanceof Entity) {
                ret = ((Entity) p).getId();
            } else {
                ret = p.getDisplayValue();
            }
        }
        return "<html>" + ret + "</html>";
    }

    /**
     * Specific sort of tag path to have:
     *
     * INDI: - NAME - BIRTH - DEATH - PARENTS - SPOUSE - rest by standard sort function - _tags
     *
     * FAM: - HUSB - WIFE - CHILD - MARR - rest by standard sort function - _tags
     *
     */
    private void initSortMaps() {
        tagMap.put("INDI:NAME", 1);
        tagMap.put("INDI:SEX", 2);
        tagMap.put("INDI:FAMC", 3);
        tagMap.put("INDI:FAMS", 4);
        tagMap.put("INDI:BIRT", 5);
        tagMap.put("INDI:CHR", 6);
        tagMap.put("INDI:DEAT", 7);
        tagMap.put("INDI:BURI", 8);
        tagMap.put("INDI:OCCU", 9);
        
        tagMap.put("FAM:HUSB", 11);
        tagMap.put("FAM:WIFE", 12);
        tagMap.put("FAM:CHIL", 13);
        tagMap.put("FAM:MARR", 14);
        tagMap.put("FAM:MARC", 15);
        
        tagMap.put("SUBM:NAME", 21);
        tagMap.put("SUBM:ADDR", 22);
        tagMap.put("SUBM:PHON", 23);
        tagMap.put("SUBM:EMAIL", 24);
        tagMap.put("SUBM:WWW", 25);
        
        tagMap.put("REPO:NAME", 31);
        tagMap.put("REPO:ADDR", 32);
        tagMap.put("REPO:PHON", 33);
        tagMap.put("REPO:EMAIL", 34);
        tagMap.put("REPO:WWW", 35);
        
        tagMap.put("SOUR:TITL", 41);
        tagMap.put("SOUR:ABBR", 42);
        tagMap.put("SOUR:AUTH", 43);
        tagMap.put("SOUR:TEXT", 44);
        tagMap.put("SOUR:PUBL", 45);
        
        tagMap.put("OBJE:FILE", 51);
        tagMap.put("OBJE:FILE:TITL", 52);
        
        
        
    }

    
    /**
     * Other Classes
     */
    
    
    private class PropertyRow {
        public Property propA = null;
        public Property propB = null;
        public boolean separator = false;
        public JSeparator jSeparator; // separator before
        public JLabel label = new JLabel();
        public JCheckBox checkPropA = new JCheckBox();
        public JCheckBox checkPropB = new JCheckBox();
        private boolean same = false;

        public PropertyRow(String label, Property propA, Property propB, boolean separator) {
            // Remember properties
            this.propA = propA;
            this.propB = propB;
            
            // Separator
            this.separator = separator;
            this.jSeparator = separator ? new JSeparator() : null;

            // Label
            Property prop = propA != null ? propA : propB;
            boolean bold = separator || prop instanceof Entity;
            this.label.setText((bold ? "" : "   ") + label);
            this.label.setIcon(prop.getImage());
            if (bold) {
                this.label.setFont(new Font("Default", Font.BOLD, 12));
            }
            
            // Check boxes
            checkPropA.setText(getTextFromProperty(propA));
            checkPropB.setText(getTextFromProperty(propB));
            same = checkPropA.getText().equals(checkPropB.getText()) || propA instanceof Entity;
            Color color = same ? Color.BLUE : Color.red;
            checkPropA.setSelected(true);
            checkPropB.setSelected(false);
            //checkPropA.setEnabled(!same);
            checkPropB.setEnabled(!same);
            checkPropA.setForeground(color);
            checkPropB.setForeground(color);
            if (!same && prop.getMetaProperty().isSingleton()) {
                checkPropA.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkPropB.setSelected(!checkPropA.isSelected());
                    }
                });
                checkPropB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkPropA.setSelected(!checkPropB.isSelected());
                    }
                });
            } else {
                checkPropA.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkPropA.setSelected(true);
                    }
                });
            }
        }
    }

    private class CompareTagPath implements Comparator {

        /**
         * 0 : equal
         * 1 : o2 before o1
         * -1: o1 before o2
         * 
         */
        
        @Override
        public int compare(Object o1, Object o2) {
            TagPath t1 = (TagPath) o1;
            TagPath t2 = (TagPath) o2;
            String tag1 = t1.getLast();
            String tag2 = t2.getLast();
            
            // _
            if (tag1.startsWith("_") && !tag2.startsWith("_")) {
                return 1;
            }
            if (!tag1.startsWith("_") && tag2.startsWith("_")) {
                return -1;
            }
            if (tag1.startsWith("_") && tag2.startsWith("_")) {
                return t1.compareTo(t2);
            }
            
            // Rest
            Integer i1 = tagMap.get(t1.get(0)+":"+t1.get(1));
            Integer i2 = tagMap.get(t2.get(0)+":"+t2.get(1));
            if (i1 == null && i2 != null) {
                return 1;
            }
            if (i1 != null && i2 == null) {
                return -1;
            }
            if (i1 == null && i2 == null) {
                return t1.compareTo(t2);
            }
            if (i1.compareTo(i2) == 0) {
                return t1.compareTo(t2);
            } else {
                return i1.compareTo(i2);
            }
        }
    }

    
    
}
