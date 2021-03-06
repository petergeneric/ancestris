package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.SourceCitationsTableModel;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
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
public class SourceCitationsTablePanel extends javax.swing.JPanel {

    private Property mRoot;
    private Source mSource;
    private PropertySource mSourceCitation;
    private final SourceCitationsTableModel mSourceCitationsTableModel = new SourceCitationsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(SourceCitationsTablePanel.class);

    /**
     * Creates new form SourceCitationsTablePanel
     */
    public SourceCitationsTablePanel() {
        initComponents();
        sourceCitationsTable.setID(SourceCitationsTablePanel.class.getName());
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
        addSourceCitationButton = new javax.swing.JButton();
        editSourceCitationButton = new javax.swing.JButton();
        deleteSourceCitationButton = new javax.swing.JButton();
        sourceCitationsTableScrollPane = new javax.swing.JScrollPane();
        sourceCitationsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        setMinimumSize(new java.awt.Dimension(453, 57));
        setName(""); // NOI18N

        sourcesToolBar.setFloatable(false);
        sourcesToolBar.setRollover(true);

        addSourceCitationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addSourceCitationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceCitationsTablePanel.addSourceCitationButton.toolTipText"), new Object[] {})); // NOI18N
        addSourceCitationButton.setFocusable(false);
        addSourceCitationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addSourceCitationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addSourceCitationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceCitationButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(addSourceCitationButton);

        editSourceCitationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editSourceCitationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceCitationsTablePanel.editSourceCitationButton.toolTipText"), new Object[] {})); // NOI18N
        editSourceCitationButton.setFocusable(false);
        editSourceCitationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editSourceCitationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editSourceCitationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSourceCitationButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(editSourceCitationButton);

        deleteSourceCitationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteSourceCitationButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceCitationsTablePanel.deleteSourceCitationButton.toolTipText"), new Object[] {})); // NOI18N
        deleteSourceCitationButton.setFocusable(false);
        deleteSourceCitationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteSourceCitationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteSourceCitationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSourceCitationButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(deleteSourceCitationButton);

        sourceCitationsTable.setModel(mSourceCitationsTableModel);
        sourceCitationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceCitationsTableMouseClicked(evt);
            }
        });
        sourceCitationsTableScrollPane.setViewportView(sourceCitationsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourcesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sourceCitationsTableScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourcesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceCitationsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSourceCitationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceCitationButtonActionPerformed

        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        // create a the source link
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mSource = (Source) gedcom.createEntity(Gedcom.SOUR);
                    mSourceCitation = (PropertySource) mRoot.addProperty("SOUR", '@' + mSource.getId() + '@');
                    ((PropertySource) mSourceCitation).link();
                }
            }); // end of doUnitOfWork
            final SourceCitationEditorPanel sourceCitationEditor = new SourceCitationEditorPanel();
            ADialog sourceCitationEditorDialog = new ADialog(
                    NbBundle.getMessage(SourceCitationEditorPanel.class, "SourceCitationEditorPanel.create.title",
                            Gedcom.getName(mRoot.getTag()),
                            mRoot.getEntity()),
                    sourceCitationEditor);

            sourceCitationEditor.set(mRoot, mSourceCitation);

            if ((sourceCitationEditorDialog.show() == DialogDescriptor.OK_OPTION) && (((PropertySource) mSourceCitation).isValid())) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            sourceCitationEditor.commit();
                            if (mSource != mSourceCitation.getTargetEntity()) {
                                gedcom.deleteEntity(mSource);
                            }
                        }
                    });
                    if (mSourceCitationsTableModel.indexOf(mSourceCitation) == -1) {
                        mSourceCitationsTableModel.add(mSourceCitation);
                    }
                    editSourceCitationButton.setEnabled(true);
                    deleteSourceCitationButton.setEnabled(true);
                    changeSupport.fireChange();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addSourceCitationButtonActionPerformed

    private void editSourceCitationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceCitationButtonActionPerformed
        int selectedRow = sourceCitationsTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourceCitationsTable.convertRowIndexToModel(selectedRow);
            Gedcom gedcom = mRoot.getGedcom();
            int undoNb = gedcom.getUndoNb();
            final SourceCitationEditorPanel sourceCitationEditor = new SourceCitationEditorPanel();
            ADialog sourceCitationEditorDialog = new ADialog(
                    NbBundle.getMessage(SourceCitationEditorPanel.class, "SourceCitationEditorPanel.edit.title",
                            Gedcom.getName(mRoot.getTag()),
                            mRoot.getEntity()),
                    sourceCitationEditor);
            sourceCitationEditor.set(mRoot, mSourceCitationsTableModel.getValueAt(rowIndex));

            if (sourceCitationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            sourceCitationEditor.commit();
                        }
                    });
                    changeSupport.fireChange();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_editSourceCitationButtonActionPerformed

    private void deleteSourceCitationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSourceCitationButtonActionPerformed
        final int selectedRow = sourceCitationsTable.getSelectedRow();
        Gedcom gedcom = mRoot.getGedcom();

        if (selectedRow != -1) {
            DialogManager createYesNo = DialogManager.createYesNo(NbBundle.getMessage(SourceCitationsTablePanel.class, "SourceCitationsTableDialog.deleteSourceCitation.title"),
                    NbBundle.getMessage(SourceCitationsTablePanel.class, "SourceCitationsTableDialog.deleteSourceCitation.text",
                            mRoot, mRoot.getEntity()));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRoot.delProperty(mSourceCitationsTableModel.remove(sourceCitationsTable.convertRowIndexToModel(selectedRow)));
                        }
                    }); // end of doUnitOfWork
                    if (mSourceCitationsTableModel.getRowCount() <= 0) {
                        editSourceCitationButton.setEnabled(false);
                        deleteSourceCitationButton.setEnabled(false);
                    }
                    changeSupport.fireChange();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteSourceCitationButtonActionPerformed

    private void sourceCitationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceCitationsTableMouseClicked

        if (evt.getClickCount() >= 2) {
            int selectedRow = sourceCitationsTable.getSelectedRow();
            if (selectedRow != -1) {
                int rowIndex = sourceCitationsTable.convertRowIndexToModel(selectedRow);
                Gedcom gedcom = mRoot.getGedcom();
                int undoNb = gedcom.getUndoNb();

                final SourceCitationEditorPanel sourceCitationEditor = new SourceCitationEditorPanel();
                ADialog sourceCitationEditorDialog = new ADialog(
                        NbBundle.getMessage(SourceCitationEditorPanel.class, "SourceCitationEditorPanel.edit.title",
                                Gedcom.getName(mRoot.getTag()),
                                mRoot.getEntity()),
                        sourceCitationEditor);
                sourceCitationEditor.set(mRoot, mSourceCitationsTableModel.getValueAt(rowIndex));

                if (sourceCitationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        gedcom.doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                sourceCitationEditor.commit();
                            }
                        });

                        changeSupport.fireChange();
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
    }//GEN-LAST:event_sourceCitationsTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceCitationButton;
    private javax.swing.JButton deleteSourceCitationButton;
    private javax.swing.JButton editSourceCitationButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable sourceCitationsTable;
    private javax.swing.JScrollPane sourceCitationsTableScrollPane;
    private javax.swing.JToolBar sourcesToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Property> sourcesList) {
        this.mRoot = root;
        mSourceCitationsTableModel.clear();
        mSourceCitationsTableModel.addAll(sourcesList);
        if (mSourceCitationsTableModel.getRowCount() > 0) {
            editSourceCitationButton.setEnabled(true);
            deleteSourceCitationButton.setEnabled(true);
        } else {
            editSourceCitationButton.setEnabled(false);
            deleteSourceCitationButton.setEnabled(false);
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
