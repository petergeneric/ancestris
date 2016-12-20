package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.NoteCitationsTableModel;
import ancestris.modules.editors.genealogyeditor.renderer.TextPaneTableCellRenderer;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import org.openide.DialogDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NoteCitationsTablePanel extends javax.swing.JPanel {

    private Property mRoot;
    private final NoteCitationsTableModel mNoteCitationsTableModel = new NoteCitationsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(NoteCitationsTablePanel.class);

    /**
     * Creates new form NoteCitationsTablePanel
     */
    public NoteCitationsTablePanel() {
        initComponents();
        noteCitationsTable.setID(NoteCitationsTablePanel.class.getName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        notesToolBar = new javax.swing.JToolBar();
        addNoteButton = new javax.swing.JButton();
        linkToNoteButton = new javax.swing.JButton();
        editNoteButton = new javax.swing.JButton();
        deleteNoteButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        noteCitationsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable() {
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 1) {
                    return new TextPaneTableCellRenderer ();
                }
                return super.getCellRenderer(row, column);
            }
        };

        setMinimumSize(new java.awt.Dimension(453, 57));

        notesToolBar.setFloatable(false);
        notesToolBar.setRollover(true);

        addNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NoteCitationsTablePanel.addNoteButton.toolTipText"), new Object[] {})); // NOI18N
        addNoteButton.setFocusable(false);
        addNoteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addNoteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNoteButtonActionPerformed(evt);
            }
        });
        notesToolBar.add(addNoteButton);

        linkToNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        linkToNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NoteCitationsTablePanel.linkToNoteButton.toolTipText"), new Object[] {})); // NOI18N
        linkToNoteButton.setFocusable(false);
        linkToNoteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkToNoteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkToNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkToNoteButtonActionPerformed(evt);
            }
        });
        notesToolBar.add(linkToNoteButton);

        editNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NoteCitationsTablePanel.editNoteButton.toolTipText"), new Object[] {})); // NOI18N
        editNoteButton.setFocusable(false);
        editNoteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editNoteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNoteButtonActionPerformed(evt);
            }
        });
        notesToolBar.add(editNoteButton);

        deleteNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NoteCitationsTablePanel.deleteNoteButton.toolTipText"), new Object[] {})); // NOI18N
        deleteNoteButton.setFocusable(false);
        deleteNoteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteNoteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNoteButtonActionPerformed(evt);
            }
        });
        notesToolBar.add(deleteNoteButton);

        noteCitationsTable.setModel(mNoteCitationsTableModel);
        noteCitationsTable.setSelectionBackground(new java.awt.Color(89, 142, 195));
        noteCitationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noteCitationsTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(noteCitationsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(notesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(notesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNoteButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        final NoteCitationEditorPanel noteCitationEditorPanel = new NoteCitationEditorPanel();
        noteCitationEditorPanel.set(gedcom, mRoot, null);
        ADialog noteCitationEditorDialog = new ADialog(
                NbBundle.getMessage(NoteCitationEditorPanel.class, "NoteCitationEditorPanel.create.title"),
                noteCitationEditorPanel);
        noteCitationEditorDialog.setDialogId(NoteCitationEditorPanel.class.getName());
        if (noteCitationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        noteCitationEditorPanel.commit();
                        if (noteCitationEditorPanel.get() instanceof Note) {
                            mRoot.addNote((Note) noteCitationEditorPanel.get());
                        }
                    }
                }); // end of doUnitOfWork
                mNoteCitationsTableModel.clear();
                mNoteCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("NOTE")));
                editNoteButton.setEnabled(true);
                deleteNoteButton.setEnabled(true);

                changeListner.stateChanged(null);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_addNoteButtonActionPerformed

    private void editNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNoteButtonActionPerformed
        int selectedRow = noteCitationsTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = noteCitationsTable.convertRowIndexToModel(selectedRow);
            Property note = mNoteCitationsTableModel.getValueAt(rowIndex);
            editNote(note);
        }
    }//GEN-LAST:event_editNoteButtonActionPerformed

    private void deleteNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNoteButtonActionPerformed
        int selectedRow = noteCitationsTable.getSelectedRow();
        if (selectedRow != -1) {
            final int rowIndex = noteCitationsTable.convertRowIndexToModel(selectedRow);
            DialogManager createYesNo = DialogManager.createYesNo(NbBundle.getMessage(NoteCitationsTablePanel.class, "NoteCitationsTableDialog.deleteNote.title"),
                    NbBundle.getMessage(NoteCitationsTablePanel.class, "NoteCitationsTableDialog.deleteNote.text",
                            mRoot));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRoot.delProperty(mNoteCitationsTableModel.remove(rowIndex));
                        }
                    }); // end of doUnitOfWork

                    changeListner.stateChanged(null);
                    if (mNoteCitationsTableModel.getRowCount() <= 0) {
                        editNoteButton.setEnabled(false);
                        deleteNoteButton.setEnabled(false);
                    }
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteNoteButtonActionPerformed

    private void linkToNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToNoteButtonActionPerformed
        List<Note> notesList = new ArrayList<Note>((Collection<Note>) mRoot.getGedcom().getEntities(Gedcom.NOTE));

        NotesTablePanel notesTablePanel = new NotesTablePanel();
        notesTablePanel.set(mRoot, notesList);
        notesTablePanel.setToolBarVisible(false);
        DialogManager.ADialog notesTableDialog = new DialogManager.ADialog(
                NbBundle.getMessage(NotesTablePanel.class, "NotesTableDialog.linkTo.title"),
                notesTablePanel);
        notesTableDialog.setDialogId(NotesTablePanel.class.getName());

        if (notesTableDialog.show() == DialogDescriptor.OK_OPTION) {
            final Note selectedNote = notesTablePanel.getSelectedNote();
            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mRoot.addNote(selectedNote);
                    }
                }); // end of doUnitOfWork

                mNoteCitationsTableModel.clear();
                mNoteCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("NOTE")));
                editNoteButton.setEnabled(true);
                deleteNoteButton.setEnabled(true);
                changeListner.stateChanged(null);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_linkToNoteButtonActionPerformed

    private void noteCitationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noteCitationsTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = noteCitationsTable.getSelectedRow();
            if (selectedRow != -1) {
                int rowIndex = noteCitationsTable.convertRowIndexToModel(selectedRow);
                Property note = mNoteCitationsTableModel.getValueAt(rowIndex);
                editNote(note);
            }
        }
    }//GEN-LAST:event_noteCitationsTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNoteButton;
    private javax.swing.JButton deleteNoteButton;
    private javax.swing.JButton editNoteButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton linkToNoteButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable noteCitationsTable;
    private javax.swing.JToolBar notesToolBar;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Property> notesList) {
        this.mRoot = root;
        mNoteCitationsTableModel.clear();
        mNoteCitationsTableModel.addAll(notesList);
        if (mNoteCitationsTableModel.getRowCount() > 0) {
            editNoteButton.setEnabled(true);
            deleteNoteButton.setEnabled(true);
        } else {
            editNoteButton.setEnabled(false);
            deleteNoteButton.setEnabled(false);
        }
    }

    public void setToolBarVisible(boolean visible) {
        notesToolBar.setVisible(visible);
    }

    private void editNote(Property note) {
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        final NoteCitationEditorPanel noteCitationEditorPanel = new NoteCitationEditorPanel();
        noteCitationEditorPanel.set(gedcom, mRoot, note);

        ADialog noteCitationEditorDialog = new ADialog(
                NbBundle.getMessage(NoteCitationEditorPanel.class, "NoteCitationEditorPanel.edit.title"),
                noteCitationEditorPanel);
        noteCitationEditorDialog.setDialogId(NoteCitationEditorPanel.class.getName());

        if (noteCitationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        noteCitationEditorPanel.commit();
                    }
                });
                changeListner.stateChanged(null);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
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
