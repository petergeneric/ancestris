package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.models.IndividualsTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class IndividualsListPanel extends javax.swing.JPanel {

    private IndividualsTableModel mIndividualsTableModel = new IndividualsTableModel();
    private Property mRoot;
    Indi mIndividual;

    /**
     * Creates new form IndividualsListPanel
     */
    public IndividualsListPanel() {
        initComponents();
        individualsTable.setID(IndividualsListPanel.class.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        individualsToolBar = new javax.swing.JToolBar();
        addIndividualButton = new javax.swing.JButton();
        editIndividualButton = new javax.swing.JButton();
        deleteIndividualButton = new javax.swing.JButton();
        individualsTableScrollPane = new javax.swing.JScrollPane();
        individualsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        individualsToolBar.setFloatable(false);
        individualsToolBar.setRollover(true);

        addIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addIndividualButton.setFocusable(false);
        addIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addIndividualButtonActionPerformed(evt);
            }
        });
        individualsToolBar.add(addIndividualButton);

        editIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editIndividualButton.setFocusable(false);
        editIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editIndividualButtonActionPerformed(evt);
            }
        });
        individualsToolBar.add(editIndividualButton);

        deleteIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteIndividualButton.setFocusable(false);
        deleteIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteIndividualButtonActionPerformed(evt);
            }
        });
        individualsToolBar.add(deleteIndividualButton);

        individualsTable.setModel(mIndividualsTableModel);
        individualsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                individualsTableMouseClicked(evt);
            }
        });
        individualsTableScrollPane.setViewportView(individualsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(individualsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(individualsTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(individualsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(individualsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addIndividualButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mIndividual = (Indi) gedcom.createEntity(Gedcom.INDI);
                }
            }); // end of doUnitOfWork
            IndividualEditor individualEditorPanel = new IndividualEditor();
            individualEditorPanel.set(mIndividual);

            DialogManager.ADialog individualEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(IndividualEditor.class, "IndividualEditorPanel.create.title"),
                    individualEditorPanel);
            individualEditorDialog.setDialogId(IndividualEditor.class.getName());

            if (individualEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                individualEditorPanel.commit();
                mIndividualsTableModel.add(mIndividual);
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addIndividualButtonActionPerformed

    private void editIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editIndividualButtonActionPerformed
        int rowIndex = individualsTable.convertRowIndexToModel(individualsTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        if (rowIndex != -1) {
            Indi individual = mIndividualsTableModel.getValueAt(rowIndex);
            IndividualEditor individualEditorPanel = new IndividualEditor();
            individualEditorPanel.set(individual);

            DialogManager.ADialog individualEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(IndividualEditor.class, "IndividualEditorPanel.edit.title", individual),
                    individualEditorPanel);
            individualEditorDialog.setDialogId(IndividualEditor.class.getName());

            if (individualEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    individualEditorPanel.commit();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_editIndividualButtonActionPerformed

    private void deleteIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteIndividualButtonActionPerformed
   }//GEN-LAST:event_deleteIndividualButtonActionPerformed

    private void individualsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_individualsTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int rowIndex = individualsTable.convertRowIndexToModel(individualsTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        if (rowIndex != -1) {
                Indi individual = mIndividualsTableModel.getValueAt(rowIndex);
                IndividualEditor individualEditorPanel = new IndividualEditor();
                individualEditorPanel.set(individual);

                DialogManager.ADialog individualEditorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditor.class, "IndividualEditorPanel.edit.title", individual),
                        individualEditorPanel);
                individualEditorDialog.setDialogId(IndividualEditor.class.getName());

                if (individualEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        individualEditorPanel.commit();
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
    }//GEN-LAST:event_individualsTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addIndividualButton;
    private javax.swing.JButton deleteIndividualButton;
    private javax.swing.JButton editIndividualButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable individualsTable;
    private javax.swing.JScrollPane individualsTableScrollPane;
    private javax.swing.JToolBar individualsToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Indi> individualsList) {
        this.mRoot = root;
        mIndividualsTableModel.clear(individualsList);
        mIndividualsTableModel.addAll(individualsList);
    }

    public Indi getSelectedIndividual() {
        int selectedRow = individualsTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = individualsTable.convertRowIndexToModel(selectedRow);
            return mIndividualsTableModel.getValueAt(rowIndex);
        } else {
            return null;
        }
    }

    public void setToolBarVisible(boolean visible) {
        individualsToolBar.setVisible(visible);
    }
}
