package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.AssociationsTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.UnitOfWork;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class AssociationsTablePanel extends javax.swing.JPanel {

    private Entity mRootEntity;
    private PropertyAssociation mAssociation;
    private final AssociationsTableModel mAssociationsTableModel = new AssociationsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(AssociationsTablePanel.class);

    /**
     * Creates new form AssociationsTablePanel
     */
    public AssociationsTablePanel() {
        initComponents();
        associationsTable.setID(AssociationsTablePanel.class.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        associationsToolBar = new javax.swing.JToolBar();
        addAssociationButton = new javax.swing.JButton();
        editAssociationButton = new javax.swing.JButton();
        deleteAssociationButton = new javax.swing.JButton();
        associationsTableScrollPane = new javax.swing.JScrollPane();
        associationsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        associationsToolBar.setFloatable(false);
        associationsToolBar.setRollover(true);

        addAssociationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addAssociationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationsTablePanel.addAssociationButton.toolTipText"), new Object[] {})); // NOI18N
        addAssociationButton.setFocusable(false);
        addAssociationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addAssociationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addAssociationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAssociationButtonActionPerformed(evt);
            }
        });
        associationsToolBar.add(addAssociationButton);

        editAssociationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editAssociationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationsTablePanel.editAssociationButton.toolTipText"), new Object[] {})); // NOI18N
        editAssociationButton.setFocusable(false);
        editAssociationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editAssociationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editAssociationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editAssociationButtonActionPerformed(evt);
            }
        });
        associationsToolBar.add(editAssociationButton);

        deleteAssociationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteAssociationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationsTablePanel.deleteAssociationButton.toolTipText"), new Object[] {})); // NOI18N
        deleteAssociationButton.setFocusable(false);
        deleteAssociationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteAssociationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteAssociationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAssociationButtonActionPerformed(evt);
            }
        });
        associationsToolBar.add(deleteAssociationButton);

        associationsTable.setModel(mAssociationsTableModel);
        associationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                associationsTableMouseClicked(evt);
            }
        });
        associationsTableScrollPane.setViewportView(associationsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(associationsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(associationsTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(associationsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(associationsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addAssociationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAssociationButtonActionPerformed
        Gedcom gedcom = mRootEntity.getGedcom();
        int undoNb = gedcom.getUndoNb();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mAssociation = (PropertyAssociation) mRootEntity.addProperty("ASSO", "@@");
                }
            }); // end of doUnitOfWork
            AssociationEditorPanel associationEditorPanel = new AssociationEditorPanel();
            associationEditorPanel.set(mAssociation);

            DialogManager.ADialog associationEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.create.title"),
                    associationEditorPanel);
            associationEditorDialog.setDialogId(AssociationEditorPanel.class.getName());

            if (associationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                mAssociationsTableModel.add(associationEditorPanel.commit());
                deleteAssociationButton.setEnabled(true);
                editAssociationButton.setEnabled(true);
                changeListner.stateChanged(null);
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addAssociationButtonActionPerformed

    private void editAssociationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editAssociationButtonActionPerformed
        int selectedRow = associationsTable.getSelectedRow();
        Gedcom gedcom = mRootEntity.getGedcom();
        if (selectedRow != -1) {
            int rowIndex = associationsTable.convertRowIndexToModel(selectedRow);
            int undoNb = gedcom.getUndoNb();
            final AssociationEditorPanel associationEditorPanel = new AssociationEditorPanel();
            associationEditorPanel.set(mAssociationsTableModel.getValueAt(rowIndex));

            DialogManager.ADialog associationEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.edit.title"),
                    associationEditorPanel);
            associationEditorDialog.setDialogId(AssociationEditorPanel.class.getName());

            if (associationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            associationEditorPanel.commit();
                        }
                    });
                    mAssociationsTableModel.fireTableDataChanged();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_editAssociationButtonActionPerformed

    private void associationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_associationsTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = associationsTable.getSelectedRow();
            Gedcom gedcom = mRootEntity.getGedcom();
            if (selectedRow != -1) {
                int rowIndex = associationsTable.convertRowIndexToModel(selectedRow);
                int undoNb = gedcom.getUndoNb();
                final AssociationEditorPanel associationEditorPanel = new AssociationEditorPanel();
                associationEditorPanel.set(mAssociationsTableModel.getValueAt(rowIndex));

                DialogManager.ADialog associationEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.edit.title"),
                        associationEditorPanel);
                associationEditorDialog.setDialogId(AssociationEditorPanel.class.getName());

                if (associationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        gedcom.doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                associationEditorPanel.commit();
                            }
                        });
                        mAssociationsTableModel.fireTableDataChanged();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_associationsTableMouseClicked

    private void deleteAssociationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAssociationButtonActionPerformed
        int rowIndex = associationsTable.convertRowIndexToModel(associationsTable.getSelectedRow());
        Gedcom gedcom = mRootEntity.getGedcom();

        if (rowIndex != -1) {
            final PropertyAssociation individualRef = mAssociationsTableModel.getValueAt(rowIndex);

            DialogManager createYesNo = DialogManager.createYesNo(
                    NbBundle.getMessage(
                            ChildrenTablePanel.class, "AssociationsTableDialog.deleteAssociation.confirmation.title"),
                    NbBundle.getMessage(
                            ChildrenTablePanel.class, "AssociationsTableDialog.deleteAssociation.confirmation.text",
                            individualRef.getTargetEntity(),
                            mRootEntity));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                mAssociationsTableModel.remove(rowIndex);
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRootEntity.delProperty(individualRef);
                        }
                    }); // end of doUnitOfWork
                    if (mAssociationsTableModel.getRowCount() <= 0) {
                        deleteAssociationButton.setEnabled(false);
                        editAssociationButton.setEnabled(false);
                    }
                    changeListner.stateChanged(null);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteAssociationButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAssociationButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable associationsTable;
    private javax.swing.JScrollPane associationsTableScrollPane;
    private javax.swing.JToolBar associationsToolBar;
    private javax.swing.JButton deleteAssociationButton;
    private javax.swing.JButton editAssociationButton;
    // End of variables declaration//GEN-END:variables

    public void setAssociationsList(Entity rootEntity, List<PropertyAssociation> associationsList) {
        this.mRootEntity = rootEntity;
        mAssociationsTableModel.clear();
        mAssociationsTableModel.addAll(associationsList);
        if (mAssociationsTableModel.getRowCount() > 0) {
            deleteAssociationButton.setEnabled(true);
            editAssociationButton.setEnabled(true);
        } else {
            deleteAssociationButton.setEnabled(false);
            editAssociationButton.setEnabled(false);
        }
    }

    /**
     * Listener
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    private class ChangeListner implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            changeSupport.fireChange();
        }
    }
}
