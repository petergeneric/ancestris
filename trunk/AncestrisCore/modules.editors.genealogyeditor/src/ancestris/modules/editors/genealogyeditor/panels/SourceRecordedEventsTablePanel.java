package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.SourceRecordedEventsTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
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
/*
 * +1 DATA {0:1}
 * +2 EVEN <EVENTS_RECORDED> {0:M}
 * +3 DATE <DATE_PERIOD> {0:1}
 * +3 PLAC <SOURCE_JURISDICTION_PLACE> {0:1}
 * +2 AGNC <RESPONSIBLE_AGENCY> {0:1}
 * +2 <<NOTE_STRUCTURE>> {0:M}
 */
public class SourceRecordedEventsTablePanel extends javax.swing.JPanel {

    private Property mParent;
    private Entity mEntity;
    private Property mRegisteredEvent;
    private final SourceRecordedEventsTableModel mSourceEventTypesTableModel = new SourceRecordedEventsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(FamiliesReferenceTreeTablePanel.class);

    /**
     * Creates new form SourceRecordedEventsTablePanel
     */
    public SourceRecordedEventsTablePanel() {
        initComponents();
        sourceEventsTable.setID(SourceRecordedEventsTablePanel.class.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceEventsToolBar = new javax.swing.JToolBar();
        addSourceEventButton = new javax.swing.JButton();
        editSourceEventButton = new javax.swing.JButton();
        deleteSourceEventButton = new javax.swing.JButton();
        sourceEventsScrollPane = new javax.swing.JScrollPane();
        sourceEventsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        sourceEventsToolBar.setFloatable(false);
        sourceEventsToolBar.setRollover(true);

        addSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addSourceEventButton.setFocusable(false);
        addSourceEventButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addSourceEventButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceEventButtonActionPerformed(evt);
            }
        });
        sourceEventsToolBar.add(addSourceEventButton);

        editSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editSourceEventButton.setFocusable(false);
        editSourceEventButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editSourceEventButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSourceEventButtonActionPerformed(evt);
            }
        });
        sourceEventsToolBar.add(editSourceEventButton);

        deleteSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteSourceEventButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceRecordedEventsTablePanel.deleteSourceEventButton.toolTipText"), new Object[] {})); // NOI18N
        deleteSourceEventButton.setFocusable(false);
        deleteSourceEventButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteSourceEventButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSourceEventButtonActionPerformed(evt);
            }
        });
        sourceEventsToolBar.add(deleteSourceEventButton);

        sourceEventsTable.setModel(mSourceEventTypesTableModel);
        sourceEventsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceEventsTableMouseClicked(evt);
            }
        });
        sourceEventsScrollPane.setViewportView(sourceEventsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceEventsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sourceEventsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(sourceEventsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceEventsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceEventButtonActionPerformed
        Gedcom gedcom = mEntity.getGedcom();
        int undoNb = gedcom.getUndoNb();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    if (mParent == null) {
                        mParent = mEntity.addProperty("DATA", "");
                    }
                    mRegisteredEvent = mParent.addProperty("EVEN", "");
                }
            }); // end of doUnitOfWork

            final RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.create.title"),
                    recordedEventPanel);
            recordedEventPanel.set(mRegisteredEvent);
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());

            recordedEventPanel.addChangeListener(changeListner);
            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mSourceEventTypesTableModel.add(mRegisteredEvent);
                            recordedEventPanel.commit();
                        }
                    });
                    editSourceEventButton.setEnabled(true);
                    deleteSourceEventButton.setEnabled(true);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            recordedEventPanel.removeChangeListener(changeListner);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addSourceEventButtonActionPerformed

    private void editSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceEventButtonActionPerformed
        int selectedRow = sourceEventsTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourceEventsTable.convertRowIndexToModel(selectedRow);

            Gedcom gedcom = mEntity.getGedcom();
            int undoNb = gedcom.getUndoNb();

            final RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.edit.title", mSourceEventTypesTableModel.getValueAt(rowIndex)),
                    recordedEventPanel);
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());
            recordedEventPanel.set(mSourceEventTypesTableModel.getValueAt(rowIndex));

            recordedEventPanel.addChangeListener(changeListner);
            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            recordedEventPanel.commit();
                        }
                    });
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            recordedEventPanel.removeChangeListener(changeListner);
        }
    }//GEN-LAST:event_editSourceEventButtonActionPerformed

    private void sourceEventsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceEventsTableMouseClicked
        int selectedRow = sourceEventsTable.getSelectedRow();
        if (evt.getClickCount() >= 2 && selectedRow != -1) {
            int rowIndex = sourceEventsTable.convertRowIndexToModel(selectedRow);

            Gedcom gedcom = mEntity.getGedcom();
            int undoNb = gedcom.getUndoNb();

            final RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.edit.title", mSourceEventTypesTableModel.getValueAt(rowIndex)),
                    recordedEventPanel);
            recordedEventPanel.set(mSourceEventTypesTableModel.getValueAt(rowIndex));
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());

            recordedEventPanel.addChangeListener(changeListner);
            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            recordedEventPanel.commit();
                        }
                    });
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            recordedEventPanel.removeChangeListener(changeListner);
        }
    }//GEN-LAST:event_sourceEventsTableMouseClicked

    private void deleteSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSourceEventButtonActionPerformed
        int selectedRow = sourceEventsTable.getSelectedRow();
        if (selectedRow != -1) {
            final int rowIndex = sourceEventsTable.convertRowIndexToModel(selectedRow);
            DialogManager createYesNo = DialogManager.createYesNo(NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.deleteSourceEvent.title"),
                NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.deleteSourceEvent.text", mSourceEventTypesTableModel.getValueAt(selectedRow)));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    mParent.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mParent.delProperty(mSourceEventTypesTableModel.remove(rowIndex));
                        }
                    }); // end of doUnitOfWork

                    changeListner.stateChanged(null);
                    if (mSourceEventTypesTableModel.getRowCount() <= 0) {
                        editSourceEventButton.setEnabled(false);
                        deleteSourceEventButton.setEnabled(false);
                    }
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteSourceEventButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceEventButton;
    private javax.swing.JButton deleteSourceEventButton;
    private javax.swing.JButton editSourceEventButton;
    private javax.swing.JScrollPane sourceEventsScrollPane;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable sourceEventsTable;
    private javax.swing.JToolBar sourceEventsToolBar;
    // End of variables declaration//GEN-END:variables

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

    public void setEventTypesList(Entity entity, Property parent, List<Property> eventsTypeList) {
        this.mEntity = entity;
        this.mParent = parent;
        mSourceEventTypesTableModel.clear();
        if (eventsTypeList != null) {
            mSourceEventTypesTableModel.addAll(eventsTypeList);
        }
        if (mSourceEventTypesTableModel.getRowCount() > 0) {
            editSourceEventButton.setEnabled(true);
            deleteSourceEventButton.setEnabled(true);
        } else {
            editSourceEventButton.setEnabled(false);
            deleteSourceEventButton.setEnabled(false);
        }
    }
}
