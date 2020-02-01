/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frederic Lapeyre(frederic-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util.swing;

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JTextField;

/**
 *
 * @author frederic
 */
public class SelectRelationshipPanel extends javax.swing.JPanel {

    private SelectEntityWidget selectEntityWidget = null;
    

    /**
     * Creates new form 
     */
    public SelectRelationshipPanel(Gedcom gedcom, String entityTag, String label, Entity selectedEntity, String first) {
        selectEntityWidget = new SelectEntityWidget(gedcom, entityTag, first);
        initComponents();
        setLabel(label);
        jPanel1.add(selectEntityWidget);
        setSelection(selectedEntity);
        String id = gedcom.getNextAvailableID(entityTag);
        requestID.setText(id);
        requestID.setColumns(id.length());
        
        selectEntityWidget.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // grab current selection (might be null)
                Entity existing = selectEntityWidget.getSelection();
                // can the user force an id now?
                if (existing != null) {
                    checkID.setSelected(false);
                }
                checkID.setVisible(existing == null);
                requestID.setVisible(existing == null);
                String label = getLabel();
                setLabel(label);
            }
        });
        
        checkID.getModel().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                requestID.setEditable(checkID.isSelected());
                if (checkID.isSelected()) {
                    requestID.requestFocusInWindow();
                }
            }
        });

        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        paramLabel = new javax.swing.JLabel();
        useLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        checkID = new javax.swing.JCheckBox();
        requestID = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(500, 230));

        org.openide.awt.Mnemonics.setLocalizedText(paramLabel, org.openide.util.NbBundle.getMessage(SelectRelationshipPanel.class, "SelectRelationshipPanel.paramLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useLabel, org.openide.util.NbBundle.getMessage(SelectRelationshipPanel.class, "SelectRelationshipPanel.useLabel.text")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkID, org.openide.util.NbBundle.getMessage(SelectRelationshipPanel.class, "SelectRelationshipPanel.checkID.text")); // NOI18N

        requestID.setEditable(false);
        requestID.setText(org.openide.util.NbBundle.getMessage(SelectRelationshipPanel.class, "SelectRelationshipPanel.requestID.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paramLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(useLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(requestID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkID)
                    .addComponent(requestID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(paramLabel)
                .addContainerGap(65, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkID;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JTextField requestID;
    private javax.swing.JLabel useLabel;
    // End of variables declaration//GEN-END:variables

    private void setSelection(Entity selectedEntity) {
        if (selectedEntity != null) {
            selectEntityWidget.setSelection(selectedEntity);
        }
    }

    public Entity getSelection() {
        return selectEntityWidget.getSelection();
    }

    public JTextField getTextIDComponent() {
        return requestID;
    }

    public String getLabel() {
        return null;
    }

    public void setLabel(String label) {
        paramLabel.setText("<html>" + label + "</html>");
    }

    
}
