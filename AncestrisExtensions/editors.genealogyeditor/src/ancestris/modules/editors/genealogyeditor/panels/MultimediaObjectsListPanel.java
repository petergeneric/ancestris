package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.MultiMediaObjectsTableModel;
import genj.gedcom.Media;
import genj.gedcom.Property;
import java.util.List;

/**
 *
 * @author dominique
 */
public class MultimediaObjectsListPanel extends javax.swing.JPanel {

    private Property root;
    private MultiMediaObjectsTableModel multiMediaObjectsTableModel = new MultiMediaObjectsTableModel();

    /**
     * Creates new form MultimediaObjectsListPanel
     */
    public MultimediaObjectsListPanel() {
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

        galleryToolBar = new javax.swing.JToolBar();
        addMMObjectButton = new javax.swing.JButton();
        editMMObjectButton2 = new javax.swing.JButton();
        deleteMMObjectButton2 = new javax.swing.JButton();
        galleryScrollPane = new javax.swing.JScrollPane();
        GalleryTable = new javax.swing.JTable();

        galleryToolBar.setFloatable(false);
        galleryToolBar.setRollover(true);

        addMMObjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectsListPanel.addMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
        addMMObjectButton.setFocusable(false);
        addMMObjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addMMObjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addMMObjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMMObjectButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(addMMObjectButton);

        editMMObjectButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editMMObjectButton2.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectsListPanel.editMMObjectButton2.toolTipText"), new Object[] {})); // NOI18N
        editMMObjectButton2.setFocusable(false);
        editMMObjectButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editMMObjectButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editMMObjectButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMMObjectButton2ActionPerformed(evt);
            }
        });
        galleryToolBar.add(editMMObjectButton2);

        deleteMMObjectButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteMMObjectButton2.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectsListPanel.deleteMMObjectButton2.toolTipText"), new Object[] {})); // NOI18N
        deleteMMObjectButton2.setFocusable(false);
        deleteMMObjectButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteMMObjectButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteMMObjectButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMMObjectButton2ActionPerformed(evt);
            }
        });
        galleryToolBar.add(deleteMMObjectButton2);

        GalleryTable.setModel(multiMediaObjectsTableModel);
        GalleryTable.getColumnModel().getColumn(0).setMaxWidth(100);
        galleryScrollPane.setViewportView(GalleryTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(galleryToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(galleryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(galleryToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(galleryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMMObjectButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addMMObjectButtonActionPerformed

    private void editMMObjectButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMMObjectButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editMMObjectButton2ActionPerformed

    private void deleteMMObjectButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMMObjectButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteMMObjectButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable GalleryTable;
    private javax.swing.JButton addMMObjectButton;
    private javax.swing.JButton deleteMMObjectButton2;
    private javax.swing.JButton editMMObjectButton2;
    private javax.swing.JScrollPane galleryScrollPane;
    private javax.swing.JToolBar galleryToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Media> namesList) {
        this.root = root;
        multiMediaObjectsTableModel.update(namesList);
    }

    public void commit() {
    }
}
