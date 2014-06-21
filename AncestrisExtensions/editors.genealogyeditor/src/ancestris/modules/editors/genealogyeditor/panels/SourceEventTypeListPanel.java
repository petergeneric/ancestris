package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.SourceEventTypesTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import java.util.List;
import org.openide.DialogDescriptor;
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
public class SourceEventTypeListPanel extends javax.swing.JPanel {

    private Property mRoot;
    private Property mRegisteredEvent;
    SourceEventTypesTableModel mSourceEventTypesTableModel = new SourceEventTypesTableModel();

    /**
     * Creates new form SourceEventTypeListPanel
     */
    public SourceEventTypeListPanel() {
        initComponents();
        sourceEventsTable.setID(SourceEventTypeListPanel.class.getName());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceEventsToolBar = new javax.swing.JToolBar();
        addSourceEventButton = new javax.swing.JButton();
        editSourceEventButton = new javax.swing.JButton();
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
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mRegisteredEvent = mRoot.addProperty("EVEN", "");
                }
            }); // end of doUnitOfWork

            RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            recordedEventPanel.set(mRegisteredEvent);

            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.create.title"),
                    recordedEventPanel);
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());

            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                mSourceEventTypesTableModel.add(recordedEventPanel.commit());
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addSourceEventButtonActionPerformed

    private void editSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceEventButtonActionPerformed
        int selectedRow = sourceEventsTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourceEventsTable.convertRowIndexToModel(selectedRow);

            Gedcom gedcom = mRoot.getGedcom();
            int undoNb = gedcom.getUndoNb();

            RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            recordedEventPanel.set(mSourceEventTypesTableModel.getValueAt(rowIndex));

            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.edit.title"),
                    recordedEventPanel);
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());

            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                recordedEventPanel.commit();
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_editSourceEventButtonActionPerformed

    private void sourceEventsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceEventsTableMouseClicked
        int selectedRow = sourceEventsTable.getSelectedRow();
        if (evt.getClickCount() >= 2 && selectedRow != -1) {
            int rowIndex = sourceEventsTable.convertRowIndexToModel(selectedRow);

            Gedcom gedcom = mRoot.getGedcom();
            int undoNb = gedcom.getUndoNb();

            RecordedEventEditorPanel recordedEventPanel = new RecordedEventEditorPanel();
            recordedEventPanel.set(mSourceEventTypesTableModel.getValueAt(rowIndex));

            DialogManager.ADialog recordedEventDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(RecordedEventEditorPanel.class, "RecordedEventEditorPanel.edit.title"),
                    recordedEventPanel);
            recordedEventDialog.setDialogId(RecordedEventEditorPanel.class.getName());

            if (recordedEventDialog.show() == DialogDescriptor.OK_OPTION) {
                recordedEventPanel.commit();
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        }
    }//GEN-LAST:event_sourceEventsTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceEventButton;
    private javax.swing.JButton editSourceEventButton;
    private javax.swing.JScrollPane sourceEventsScrollPane;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable sourceEventsTable;
    private javax.swing.JToolBar sourceEventsToolBar;
    // End of variables declaration//GEN-END:variables

    public void setEventTypesList(Property root, List<Property> eventsTypeList) {
        this.mRoot = root;
        if (eventsTypeList != null) {
            mSourceEventTypesTableModel.addAll(eventsTypeList);
        }
    }
}
