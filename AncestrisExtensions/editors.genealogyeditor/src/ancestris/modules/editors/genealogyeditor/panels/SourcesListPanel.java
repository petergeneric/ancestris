package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.SourcesTableModel;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Source;
import genj.gedcom.UnitOfWork;
import java.util.Collection;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SourcesListPanel extends javax.swing.JPanel {

    private Gedcom mGedcom;
    private SourcesTableModel mSourcesTableModel = new SourcesTableModel();
    private Source mSource;

    /**
     * Creates new form SourcesListPanel
     */
    public SourcesListPanel(Gedcom gedcom) {
        this.mGedcom = gedcom;
        initComponents();
        sourcesTable.setID(SourcesListPanel.class.getName());
        mSourcesTableModel.update((Collection<Source>) gedcom.getEntities(Gedcom.SOUR));
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
        sourcesTableScrollPane = new javax.swing.JScrollPane();
        sourcesTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

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

        sourcesTable.setModel(mSourcesTableModel);
        sourcesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourcesTableMouseClicked(evt);
            }
        });
        sourcesTableScrollPane.setViewportView(sourcesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourcesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(sourcesTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourcesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcesTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceButtonActionPerformed
        int undoNb = mGedcom.getUndoNb();
        try {
            mGedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mSource = (Source) gedcom.createEntity(Gedcom.SOUR);
                }
            }); // end of doUnitOfWork

            SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
            sourceEditorPanel.set(mSource);

            ADialog sourceEditorDialog = new ADialog(
                    NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.create.title"),
                    sourceEditorPanel);
            sourceEditorDialog.setDialogId(SourceEditorPanel.class.getName());

            if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                mSourcesTableModel.add(sourceEditorPanel.commit());
            } else {
                while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                    mGedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addSourceButtonActionPerformed

    private void editSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceButtonActionPerformed
        int selectedRow = sourcesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
            int undoNb = mGedcom.getUndoNb();
            SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
            sourceEditorPanel.set(mSourcesTableModel.getValueAt(rowIndex));
            ADialog sourceEditorDialog = new ADialog(
                    NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.edit.title"),
                    sourceEditorPanel);
            sourceEditorDialog.setDialogId(SourceEditorPanel.class.getName());

            if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                mSourcesTableModel.add(sourceEditorPanel.commit());
            } else {
                while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                    mGedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_editSourceButtonActionPerformed

    private void deleteSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSourceButtonActionPerformed
        final int selectedRow = sourcesTable.getSelectedRow();

        if (selectedRow != -1) {
            try {
                mGedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
                        mGedcom.deleteEntity(mSourcesTableModel.remove(rowIndex));
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_deleteSourceButtonActionPerformed

    private void sourcesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourcesTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = sourcesTable.getSelectedRow();
            int undoNb = mGedcom.getUndoNb();
            if (selectedRow != -1) {
                int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
                SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
                sourceEditorPanel.set(mSourcesTableModel.getValueAt(rowIndex));
                ADialog sourceEditorDialog = new ADialog(
                        NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.edit.title"),
                        sourceEditorPanel);
                sourceEditorDialog.setDialogId(SourceEditorPanel.class.getName());

                if (sourceEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    mSourcesTableModel.add(sourceEditorPanel.commit());
                } else {
                    while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                        mGedcom.undoUnitOfWork(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_sourcesTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceButton;
    private javax.swing.JButton deleteSourceButton;
    private javax.swing.JButton editSourceButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable sourcesTable;
    private javax.swing.JScrollPane sourcesTableScrollPane;
    private javax.swing.JToolBar sourcesToolBar;
    // End of variables declaration//GEN-END:variables

    public Source getSelectedSource() {
        int selectedRow = sourcesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
            return mSourcesTableModel.getValueAt(rowIndex);
        } else {
            return null;
        }
    }

    public void setToolBarVisible(boolean b) {
        sourcesToolBar.setVisible(b);
    }
}
