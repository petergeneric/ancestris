package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.FamiliesTableModel;
import genj.gedcom.Fam;
import genj.gedcom.Property;
import java.util.List;

/**
 *
 * @author dominique
 */
public class FamiliesListPanel extends javax.swing.JPanel {

    private FamiliesTableModel familiesTableModel = new FamiliesTableModel();
    private Property root;

    /**
     * Creates new form FamiliesListPanel
     */
    public FamiliesListPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        familyNamesToolBar = new javax.swing.JToolBar();
        addFamilyNameButton = new javax.swing.JButton();
        editFamilyNameButton = new javax.swing.JButton();
        deleteFamilyNameButton = new javax.swing.JButton();
        familyNamesScrollPane = new javax.swing.JScrollPane();
        familyNamesTable = new javax.swing.JTable();

        familyNamesToolBar.setFloatable(false);
        familyNamesToolBar.setRollover(true);

        addFamilyNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addFamilyNameButton.setFocusable(false);
        addFamilyNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addFamilyNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addFamilyNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFamilyNameButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(addFamilyNameButton);

        editFamilyNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editFamilyNameButton.setFocusable(false);
        editFamilyNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editFamilyNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editFamilyNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFamilyNameButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(editFamilyNameButton);

        deleteFamilyNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteFamilyNameButton.setFocusable(false);
        deleteFamilyNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteFamilyNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteFamilyNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteFamilyNameButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(deleteFamilyNameButton);

        familyNamesTable.setModel(familiesTableModel);
        familyNamesTable.getColumnModel().getColumn(0).setMaxWidth(100);
        familyNamesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                familyNamesTableMouseClicked(evt);
            }
        });
        familyNamesScrollPane.setViewportView(familyNamesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familyNamesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(familyNamesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(familyNamesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familyNamesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void familyNamesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_familyNamesTableMouseClicked
        if (evt.getClickCount() >= 2) {
            
        }
    }//GEN-LAST:event_familyNamesTableMouseClicked

    private void addFamilyNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFamilyNameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addFamilyNameButtonActionPerformed

    private void editFamilyNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFamilyNameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editFamilyNameButtonActionPerformed

    private void deleteFamilyNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFamilyNameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteFamilyNameButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFamilyNameButton;
    private javax.swing.JButton deleteFamilyNameButton;
    private javax.swing.JButton editFamilyNameButton;
    private javax.swing.JScrollPane familyNamesScrollPane;
    private javax.swing.JTable familyNamesTable;
    private javax.swing.JToolBar familyNamesToolBar;
    // End of variables declaration//GEN-END:variables

    public void setFamiliesList(Property root, List<Fam> familiesList) {
        this.root = root;
        familiesTableModel.update(familiesList);
    }
}
