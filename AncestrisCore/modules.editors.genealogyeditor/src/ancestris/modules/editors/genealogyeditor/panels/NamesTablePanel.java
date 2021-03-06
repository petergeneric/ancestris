package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.NamesTableModel;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyName;
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
public class NamesTablePanel extends javax.swing.JPanel {

    private final NamesTableModel mNamesTableModel = new NamesTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(NamesTablePanel.class);
    private Indi root;
    private PropertyName addedName = null;

    /**
     * Creates new form NamesTablePanel
     */
    public NamesTablePanel() {
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

        namesToolBar = new javax.swing.JToolBar();
        addNameButton = new javax.swing.JButton();
        editNameButton = new javax.swing.JButton();
        deleteNameButton = new javax.swing.JButton();
        namesTableScrollPane = new javax.swing.JScrollPane();
        namesTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        namesToolBar.setFloatable(false);
        namesToolBar.setRollover(true);

        addNameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesTablePanel.addNameButton.toolTipText"), new Object[] {})); // NOI18N
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
        editNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesTablePanel.editNameButton.toolTipText"), new Object[] {})); // NOI18N
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
        deleteNameButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NamesTablePanel.deleteNameButton.toolTipText"), new Object[] {})); // NOI18N
        deleteNameButton.setFocusable(false);
        deleteNameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteNameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNameButtonActionPerformed(evt);
            }
        });
        namesToolBar.add(deleteNameButton);

        namesTable.setModel(mNamesTableModel);
        namesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                namesTableMouseClicked(evt);
            }
        });
        namesTableScrollPane.setViewportView(namesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(namesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(namesTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(namesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(namesTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNameButtonActionPerformed
        final NameEditorPanel nameEditorPanel = new NameEditorPanel();
        Gedcom gedcom = root.getGedcom();
        int undoNb = gedcom.getUndoNb();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    addedName = (PropertyName) root.addProperty("NAME", "");
                }
            }); // end of doUnitOfWork
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
        nameEditorPanel.set(root, addedName);
        ADialog nameEditorDialog = new ADialog(
                NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.create"),
                nameEditorPanel);
        nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

        if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            try {
                root.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        nameEditorPanel.commit();
                        mNamesTableModel.add(nameEditorPanel.get());
                    }
                });
                editNameButton.setEnabled(true);
                deleteNameButton.setEnabled(true);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_addNameButtonActionPerformed

    private void editNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNameButtonActionPerformed
        int selectedRow = namesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = namesTable.convertRowIndexToModel(selectedRow);
            final NameEditorPanel nameEditorPanel = new NameEditorPanel();
            nameEditorPanel.set(root, mNamesTableModel.getValueAt(rowIndex));
            ADialog nameEditorDialog = new ADialog(
                    NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.edit"),
                    nameEditorPanel);
            nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

            if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    root.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            nameEditorPanel.commit();
                        }
                    });
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_editNameButtonActionPerformed

    private void deleteNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNameButtonActionPerformed
        int selectedRow = namesTable.getSelectedRow();
        if (selectedRow != -1) {
            final int rowIndex = namesTable.convertRowIndexToModel(selectedRow);
            DialogManager createYesNo = DialogManager.createYesNo(
                    NbBundle.getMessage(
                            NamesTablePanel.class, "NamesTableDialog.deleteName.title"),
                    NbBundle.getMessage(
                            NamesTablePanel.class, "NamesTableDialog.deleteName.text",
                            root));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    root.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            root.delProperty(mNamesTableModel.remove(rowIndex));
                        }
                    }); // end of doUnitOfWork
                    if (mNamesTableModel.getRowCount() <= 0) {
                        editNameButton.setEnabled(false);
                        deleteNameButton.setEnabled(false);
                    }
                    changeListner.stateChanged(null);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteNameButtonActionPerformed

    private void namesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_namesTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = namesTable.getSelectedRow();
            if (selectedRow != -1) {
                int rowIndex = namesTable.convertRowIndexToModel(selectedRow);
                final NameEditorPanel nameEditorPanel = new NameEditorPanel();
                nameEditorPanel.set(root, mNamesTableModel.getValueAt(rowIndex));
                ADialog nameEditorDialog = new ADialog(
                        NbBundle.getMessage(NameEditorPanel.class, "NameEditorPanel.title.edit"),
                        nameEditorPanel);
                nameEditorDialog.setDialogId(NameEditorPanel.class.getName());

                if (nameEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        root.getGedcom().doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                nameEditorPanel.commit();
                            }
                        });
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_namesTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNameButton;
    private javax.swing.JButton deleteNameButton;
    private javax.swing.JButton editNameButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable namesTable;
    private javax.swing.JScrollPane namesTableScrollPane;
    private javax.swing.JToolBar namesToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Indi root, List<PropertyName> namesList) {
        this.root = root;
        mNamesTableModel.clear();
        mNamesTableModel.addAll(namesList);
        if (mNamesTableModel.getRowCount() > 0) {
            editNameButton.setEnabled(true);
            deleteNameButton.setEnabled(true);
        } else {
            editNameButton.setEnabled(false);
            deleteNameButton.setEnabled(false);
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
