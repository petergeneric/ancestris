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
package genj.edit;

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic & lemovice
 */
public class SelectEntityToPropagatePanel extends javax.swing.JPanel {

    private SelectEntityWidget selectEntityWidget = null;
    private String label = null;
    

    /**
     * Creates new form SelectEntityPanel
     */
    public SelectEntityToPropagatePanel(Gedcom gedcom, final String entityTag, final String label, Entity selectedEntity, String first) {
        this.label = label;
        selectEntityWidget = new SelectEntityWidget(gedcom, entityTag, first, true);
        initComponents();
        setLabel("");
        jPanel1.add(selectEntityWidget);
        selectEntityWidget.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Entity target = selectEntityWidget.getSelection();
                String string = target == null ? 
                        NbBundle.getMessage(getClass(), "action.propagate.all", new Object[]{label, "" + selectEntityWidget.getEntityCount(), Gedcom.getName(entityTag)})
                        : NbBundle.getMessage(getClass(), "action.propagate.one", new Object[]{label, target.getId(), Gedcom.getName(target.getTag())});
                setLabel(string);
            }
        });

        setSelection(selectedEntity);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 260));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectEntityToPropagatePanel.class, "SelectEntityToPropagatePanel.jLabel1.text")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(SelectEntityToPropagatePanel.class, "action.propagate.value")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SelectEntityToPropagatePanel.class, "SelectEntityToPropagatePanel.jLabel2.text", label));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addContainerGap(134, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    public void setSelection(Entity selectedEntity) {
        if (selectedEntity != null) {
            selectEntityWidget.setSelection(selectedEntity);
        }
    }

    public Entity getSelection() {
        return selectEntityWidget.getSelection();
    }

    private void setLabel(String label) {
        jLabel1.setText("<html>" + label + "</html>");
    }

    public boolean isSelected(){
        return jCheckBox1.isSelected();
    }
}
