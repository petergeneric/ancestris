package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.SourcesTableModel;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.Source;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SourcesListPanel extends javax.swing.JPanel {

    private Property mRoot;
    private SourcesTableModel sourcesTableModel = new SourcesTableModel();

    /**
     * Creates new form SourcesListPanel
     */
    public SourcesListPanel() {
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

        sourcesToolBar = new javax.swing.JToolBar();
        addSourceButton = new javax.swing.JButton();
        editSourceButton = new javax.swing.JButton();
        deleteSourceButton = new javax.swing.JButton();
        sourcesScrollPane = new javax.swing.JScrollPane();
        sourcesTable = new javax.swing.JTable();

        sourcesToolBar.setFloatable(false);
        sourcesToolBar.setRollover(true);

        addSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesListPanel.addSourceButton.toolTipText"), new Object[] {})); // NOI18N
        addSourceButton.setFocusable(false);
        addSourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addSourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(addSourceButton);

        editSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesListPanel.editSourceButton.toolTipText"), new Object[] {})); // NOI18N
        editSourceButton.setFocusable(false);
        editSourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editSourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSourceButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(editSourceButton);

        deleteSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesListPanel.deleteSourceButton.toolTipText"), new Object[] {})); // NOI18N
        deleteSourceButton.setFocusable(false);
        deleteSourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteSourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSourceButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(deleteSourceButton);

        sourcesTable.setModel(sourcesTableModel);
        sourcesTable.setShowHorizontalLines(false);
        sourcesTable.setShowVerticalLines(false);
        sourcesTable.getColumnModel().getColumn(0).setMaxWidth(100);
        sourcesScrollPane.setViewportView(sourcesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourcesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sourcesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourcesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceButtonActionPerformed
        SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
        sourceEditorPanel.setSource(new Source(Gedcom.SOUR, ""));

        ADialog sourceEditorDialog = new ADialog(
                NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.title"),
                sourceEditorPanel);
        sourceEditorDialog.setDialogId(SourceEditorPanel.class.getName());

        if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            sourcesTableModel.add(sourceEditorPanel.getSource());
        }
    }//GEN-LAST:event_addSourceButtonActionPerformed

    private void editSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceButtonActionPerformed
        int selectedRow = sourcesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
            SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
            sourceEditorPanel.setSource(sourcesTableModel.getValueAt(rowIndex));
            ADialog sourceEditorDialog = new ADialog(
                    NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.title"),
                    sourceEditorPanel);
            sourceEditorDialog.setDialogId(SourceEditorPanel.class.getName());

            if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            }
        }
    }//GEN-LAST:event_editSourceButtonActionPerformed

    private void deleteSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSourceButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteSourceButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceButton;
    private javax.swing.JButton deleteSourceButton;
    private javax.swing.JButton editSourceButton;
    private javax.swing.JScrollPane sourcesScrollPane;
    private javax.swing.JTable sourcesTable;
    private javax.swing.JToolBar sourcesToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Source> sourcesList) {
        this.mRoot = root;
        sourcesTableModel.update(sourcesList);
    }

    public void commit() {
    }
}
