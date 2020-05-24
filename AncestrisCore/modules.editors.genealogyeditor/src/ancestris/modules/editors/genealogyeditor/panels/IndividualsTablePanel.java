package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.models.IndividualsTableModel;
import ancestris.modules.editors.genealogyeditor.utilities.AriesFilterPanel;
import genj.gedcom.*;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class IndividualsTablePanel extends javax.swing.JPanel implements AriesFilterPanel {

    private final IndividualsTableModel mIndividualsTableModel = new IndividualsTableModel();
    private Property mRoot;
    Indi mIndividual;
    private final TableRowSorter<TableModel> mPlaceTableSorter;

    /**
     * Creates new form IndividualsTablePanel
     */
    public IndividualsTablePanel() {
        initComponents();
        individualsTable.setID(IndividualsTablePanel.class.getName());
        mPlaceTableSorter = new TableRowSorter<>(individualsTable.getModel());
        individualsTable.setRowSorter(mPlaceTableSorter);
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
        individualsEditToolBar = new javax.swing.JToolBar();
        addIndividualButton = new javax.swing.JButton();
        editIndividualButton = new javax.swing.JButton();
        deleteIndividualButton = new javax.swing.JButton();
        filterToolBar = new ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar(this);
        individualsTableScrollPane = new javax.swing.JScrollPane();
        individualsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        individualsToolBar.setFloatable(false);
        individualsToolBar.setRollover(true);

        individualsEditToolBar.setFloatable(false);
        individualsEditToolBar.setRollover(true);

        addIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addIndividualButton.setFocusable(false);
        addIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addIndividualButtonActionPerformed(evt);
            }
        });
        individualsEditToolBar.add(addIndividualButton);

        editIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editIndividualButton.setFocusable(false);
        editIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editIndividualButtonActionPerformed(evt);
            }
        });
        individualsEditToolBar.add(editIndividualButton);

        deleteIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteIndividualButton.setFocusable(false);
        deleteIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteIndividualButtonActionPerformed(evt);
            }
        });
        individualsEditToolBar.add(deleteIndividualButton);

        individualsToolBar.add(individualsEditToolBar);
        individualsToolBar.add(filterToolBar);

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
            .addComponent(individualsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(individualsTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(individualsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(individualsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
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
            IndividualEditor individualEditor = new IndividualEditor();
            individualEditor.setContext(new Context(mIndividual));
            if (individualEditor.showPanel()) {
                mIndividualsTableModel.add(mIndividual);
                editIndividualButton.setEnabled(true);
                deleteIndividualButton.setEnabled(true);
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

        if (rowIndex != -1) {
            Indi individual = mIndividualsTableModel.getValueAt(rowIndex);
            IndividualEditor individualEditor = new IndividualEditor();
            individualEditor.setContext(new Context(individual));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            atc.getOpenEditors().add(individualEditor);
            individualEditor.showPanel();
            atc.getOpenEditors().remove(individualEditor);
        }
    }//GEN-LAST:event_editIndividualButtonActionPerformed

    private void deleteIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteIndividualButtonActionPerformed
   }//GEN-LAST:event_deleteIndividualButtonActionPerformed

    private void individualsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_individualsTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int rowIndex = individualsTable.convertRowIndexToModel(individualsTable.getSelectedRow());
            Gedcom gedcom = mRoot.getGedcom();

            if (rowIndex != -1) {
                Indi individual = mIndividualsTableModel.getValueAt(rowIndex);
                IndividualEditor individualEditor = new IndividualEditor();
                individualEditor.setContext(new Context(individual));
                final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                atc.getOpenEditors().add(individualEditor);
                individualEditor.showPanel();
                atc.getOpenEditors().remove(individualEditor);
            }
        }
    }//GEN-LAST:event_individualsTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addIndividualButton;
    private javax.swing.JButton deleteIndividualButton;
    private javax.swing.JButton editIndividualButton;
    private ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar filterToolBar;
    private javax.swing.JToolBar individualsEditToolBar;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable individualsTable;
    private javax.swing.JScrollPane individualsTableScrollPane;
    private javax.swing.JToolBar individualsToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Indi> individualsList) {
        this.mRoot = root;
        mIndividualsTableModel.clear(individualsList);
        mIndividualsTableModel.addAll(individualsList);
        if (mIndividualsTableModel.getRowCount() > 0) {
            editIndividualButton.setEnabled(true);
            deleteIndividualButton.setEnabled(true);
        } else {
            editIndividualButton.setEnabled(false);
            deleteIndividualButton.setEnabled(false);
        }
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
        individualsEditToolBar.setVisible(visible);
    }

    @Override
    public ComboBoxModel<String> getComboBoxModel() {
        return new DefaultComboBoxModel<>(mIndividualsTableModel.getColumnsName());
    }

    @Override
    public void filter(int index, String searchFilter) {
        RowFilter<TableModel, Integer> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + searchFilter, index);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        mPlaceTableSorter.setRowFilter(rf);
    }
}
