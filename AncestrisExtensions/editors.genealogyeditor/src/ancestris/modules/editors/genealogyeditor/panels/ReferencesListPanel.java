package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.ReferencesTableModel;
import genj.gedcom.Entity;
import java.util.List;

/**
 *
 * @author dominique
 */
public class ReferencesListPanel extends javax.swing.JPanel {

    private Entity rootEntity;
    private ReferencesTableModel referencesTableModel = new ReferencesTableModel();

    /**
     * Creates new form ReferencesListPanel
     */
    public ReferencesListPanel() {
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

        referencesToolBar = new javax.swing.JToolBar();
        addReferenceButton = new javax.swing.JButton();
        referencesScrollPane = new javax.swing.JScrollPane();
        referencesTable = new javax.swing.JTable();

        referencesToolBar.setFloatable(false);
        referencesToolBar.setRollover(true);

        addReferenceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addReferenceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("ReferencesListPanel.addReferenceButton.toolTipText"), new Object[] {})); // NOI18N
        addReferenceButton.setFocusable(false);
        addReferenceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addReferenceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addReferenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReferenceButtonActionPerformed(evt);
            }
        });
        referencesToolBar.add(addReferenceButton);

        referencesTable.setModel(referencesTableModel);
        referencesTable.setShowHorizontalLines(false);
        referencesTable.setShowVerticalLines(false);
        referencesTable.getColumnModel().getColumn(0).setMaxWidth(100);
        referencesScrollPane.setViewportView(referencesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(referencesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(referencesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(referencesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addReferenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addReferenceButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addReferenceButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addReferenceButton;
    private javax.swing.JScrollPane referencesScrollPane;
    private javax.swing.JTable referencesTable;
    private javax.swing.JToolBar referencesToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Entity rootEntity, List<Entity> referencesList) {
        this.rootEntity = rootEntity;
        referencesTableModel.update(referencesList);
    }

    public void commit() {
    }
}
