package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.NamesTableModel;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Indi;
import genj.gedcom.PropertyName;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NamesListPanel extends javax.swing.JPanel {

    private NamesTableModel namesTableModel = new NamesTableModel();
    private Indi root;

    /**
     * Creates new form NamesListPanel
     */
    public NamesListPanel() {
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

        namesScrollPane = new javax.swing.JScrollPane();
        namesTable = new javax.swing.JTable();
        namesToolBar = new javax.swing.JToolBar();
        addNameButton = new javax.swing.JButton();
        editNameButton = new javax.swing.JButton();
        deleteNameButton = new javax.swing.JButton();

        namesTable.setModel(namesTableModel);
        namesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        namesTable.setShowVerticalLines(false);
        namesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                namesTableMouseClicked(evt);
            }
        });
        namesScrollPane.setViewportView(namesTable);

        namesToolBar.setFloatable(false);
        namesToolBar.setRollover(true);

        addNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesListPanel.addNameButton.toolTipText"), new Object[] {})); // NOI18N
        addNameButton.setFocusable(false);
        addNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNameButtonActionPerformed(evt);
            }
        });
        namesToolBar.add(addNameButton);

        editNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesListPanel.editNameButton.toolTipText"), new Object[] {})); // NOI18N
        editNameButton.setFocusable(false);
        editNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNameButtonActionPerformed(evt);
            }
        });
        namesToolBar.add(editNameButton);

        deleteNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesListPanel.deleteNameButton.toolTipText"), new Object[] {})); // NOI18N
        deleteNameButton.setFocusable(false);
        deleteNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNameButtonActionPerformed(evt);
            }
        });
        namesToolBar.add(deleteNameButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(namesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(namesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(namesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(namesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNameButtonActionPerformed
        NameEditorPanel nameEditorPanel = new NameEditorPanel();
        nameEditorPanel.set(root, null);
        ADialog nameEditorDialog = new ADialog(
                NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.create"),
                nameEditorPanel);
        nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

        if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            nameEditorPanel.commit();
            namesTableModel.add(nameEditorPanel.get());
        }
    }//GEN-LAST:event_addNameButtonActionPerformed

    private void editNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNameButtonActionPerformed
        int selectedRow = namesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = namesTable.convertRowIndexToModel(selectedRow);
            NameEditorPanel nameEditorPanel = new NameEditorPanel();
            nameEditorPanel.set(root, namesTableModel.getValueAt(rowIndex));
            ADialog nameEditorDialog = new ADialog(
                    NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.edit"),
                    nameEditorPanel);
            nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

            if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                nameEditorPanel.commit();
            }
        }
    }//GEN-LAST:event_editNameButtonActionPerformed

    private void deleteNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteNameButtonActionPerformed

    private void namesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_namesTableMouseClicked
        int selectedRow = namesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = namesTable.convertRowIndexToModel(selectedRow);
            NameEditorPanel nameEditorPanel = new NameEditorPanel();
            nameEditorPanel.set(root, namesTableModel.getValueAt(rowIndex));
            ADialog nameEditorDialog = new ADialog(
                    NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.edit"),
                    nameEditorPanel);
            nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

            if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                nameEditorPanel.commit();
            }
        }
    }//GEN-LAST:event_namesTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNameButton;
    private javax.swing.JButton deleteNameButton;
    private javax.swing.JButton editNameButton;
    private javax.swing.JScrollPane namesScrollPane;
    private javax.swing.JTable namesTable;
    private javax.swing.JToolBar namesToolBar;
    // End of variables declaration//GEN-END:variables

    public void setNamesList(Indi root, List<PropertyName> namesList) {
        this.root = root;
        namesTableModel.update(namesList);
    }

    public void commit() {
    }
}
