package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.models.NotesTableModel;
import ancestris.modules.editors.genealogyeditor.utilities.AriesFilterPanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.DialogDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NotesTablePanel extends javax.swing.JPanel implements AriesFilterPanel {

    private Property mRoot;
    private final NotesTableModel mNotesTableModel = new NotesTableModel();
    private Note mNote;
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(NotesTablePanel.class);
    private final TableRowSorter<TableModel> notesTableSorter;
    private Registry registry = null;

    /**
     * Creates new form NotesTablePanel
     */
    public NotesTablePanel(Property root, List<Note> notesList) {
        initComponents();
        this.mRoot = root;
        registry = root.getGedcom().getRegistry();
        notesTable.setID(NotesTablePanel.class.getName());
        mNotesTableModel.addAll(notesList);
        if (mNotesTableModel.getRowCount() > 0) {
            deleteNoteButton.setEnabled(false);
            editNoteButton.setEnabled(false);
        } else {
            deleteNoteButton.setEnabled(true);
            editNoteButton.setEnabled(true);
        }
        notesTableSorter = new TableRowSorter<>(notesTable.getModel());
        loadSettings();
        notesTable.setRowSorter(notesTableSorter);
    }

    @Override
    public void saveFilterSettings() {
        StringBuilder sb = new StringBuilder();
        List<? extends RowSorter.SortKey> sortKeys = notesTableSorter.getSortKeys();
        for (int i = 0; i < sortKeys.size(); i++) {
            RowSorter.SortKey sk = sortKeys.get(i);
            sb.append(sk.getColumn());
            sb.append(',');
            sb.append(sk.getSortOrder().toString());
            sb.append(';');
        }
        registry.put("Aries.NotesSortOrder", sb.toString());
    }
    
    private void loadSettings() {
        String sortOrder = registry.get("Aries.NotesSortOrder", "");
        if ("".equals(sortOrder)) {
            return;
        }
        List<RowSorter.SortKey> sorts = new ArrayList<>();
        for (String columnInfo : sortOrder.split(";")) {
            String[] column = columnInfo.split(",");
            RowSorter.SortKey sk = new RowSorter.SortKey(Integer.valueOf(column[0]), SortOrder.valueOf(column[1]));
            sorts.add(sk);
        }
        if (sorts.size() > 0) {
            notesTableSorter.setSortKeys(sorts);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        notesToolBar = new javax.swing.JToolBar();
        addNoteButton = new javax.swing.JButton();
        linkToNoteButton = new javax.swing.JButton();
        editNoteButton = new javax.swing.JButton();
        deleteNoteButton = new javax.swing.JButton();
        filterToolBar = new ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar(this);
        notesTableScrollPane = new javax.swing.JScrollPane();
        notesTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        jToolBar1.setBorder(null);
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        notesToolBar.setFloatable(false);
        notesToolBar.setRollover(true);

        addNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NotesTablePanel.addNoteButton.toolTipText"), new Object[] {})); // NOI18N
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
        linkToNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NotesTablePanel.linkToNoteButton.toolTipText"), new Object[] {})); // NOI18N
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
        editNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NotesTablePanel.editNoteButton.toolTipText"), new Object[] {})); // NOI18N
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
        deleteNoteButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("NotesTablePanel.deleteNoteButton.toolTipText"), new Object[] {})); // NOI18N
        deleteNoteButton.setFocusable(false);
        deleteNoteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteNoteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNoteButtonActionPerformed(evt);
            }
        });
        notesToolBar.add(deleteNoteButton);

        jToolBar1.add(notesToolBar);
        jToolBar1.add(filterToolBar);

        notesTable.setModel(mNotesTableModel);
        notesTable.setSelectionBackground(new java.awt.Color(89, 142, 195));
        notesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notesTableMouseClicked(evt);
            }
        });
        notesTableScrollPane.setViewportView(notesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(notesTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(notesTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNoteButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();

        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mNote = (Note) gedcom.createEntity(Gedcom.NOTE);
                }
            }); // end of doUnitOfWork

            NoteEditor noteEditor = new NoteEditor();
            noteEditor.setContext(new Context(mNote));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            atc.getOpenEditors().add(noteEditor);
            if (noteEditor.showPanel()) {
                mNotesTableModel.add(mNote);
                changeListner.stateChanged(null);
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mRoot.addNote(mNote);
                    }
                }); // end of doUnitOfWork
                deleteNoteButton.setEnabled(true);
                editNoteButton.setEnabled(true);
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            atc.getOpenEditors().remove(noteEditor);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addNoteButtonActionPerformed

    private void editNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNoteButtonActionPerformed
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = notesTable.convertRowIndexToModel(selectedRow);
            NoteEditor noteEditor = new NoteEditor();
            noteEditor.setContext(new Context(mNotesTableModel.getValueAt(rowIndex)));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mRoot.getGedcom());
            atc.getOpenEditors().add(noteEditor);
            noteEditor.showPanel();
            atc.getOpenEditors().remove(noteEditor);
        }
    }//GEN-LAST:event_editNoteButtonActionPerformed

    private void deleteNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNoteButtonActionPerformed
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow != -1) {
            final int rowIndex = notesTable.convertRowIndexToModel(selectedRow);
            DialogManager createYesNo = DialogManager.createYesNo(NbBundle.getMessage(NotesTablePanel.class, "NotesTableDialog.deleteNote.title"),
                    NbBundle.getMessage(NotesTablePanel.class, "NotesTableDialog.deleteNote.text",
                            mRoot));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRoot.delProperty(mNotesTableModel.remove(rowIndex));

                        }
                    }); // end of doUnitOfWork
                    changeListner.stateChanged(null);
                    if (mNotesTableModel.getRowCount() == 0) {
                        deleteNoteButton.setEnabled(false);
                        editNoteButton.setEnabled(false);
                    }
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteNoteButtonActionPerformed

    private void linkToNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToNoteButtonActionPerformed
        List<Note> notesList = new ArrayList<>((Collection<Note>) mRoot.getGedcom().getEntities(Gedcom.NOTE));

        NotesTablePanel notesTablePanel = new NotesTablePanel(mRoot, notesList);
        notesTablePanel.setToolBarVisible(false);
        DialogManager.ADialog notesTablePanelDialog = new DialogManager.ADialog(
                NbBundle.getMessage(NoteEditor.class, "NoteEditorPanel.title"),
                notesTablePanel);
        notesTablePanelDialog.setDialogId(NoteEditor.class.getName());

        if (notesTablePanelDialog.show() == DialogDescriptor.OK_OPTION) {
            final Note selectedNote = notesTablePanel.getSelectedNote();
            mNotesTableModel.add(selectedNote);
            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mRoot.addNote(selectedNote);
                    }
                }); // end of doUnitOfWork
                deleteNoteButton.setEnabled(true);
                editNoteButton.setEnabled(true);
                changeListner.stateChanged(null);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        notesTablePanel.saveFilterSettings();
    }//GEN-LAST:event_linkToNoteButtonActionPerformed

    private void notesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_notesTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = notesTable.getSelectedRow();
            if (selectedRow != -1) {
                int rowIndex = notesTable.convertRowIndexToModel(selectedRow);
                NoteEditor noteEditor = new NoteEditor();
                noteEditor.setContext(new Context(mNotesTableModel.getValueAt(rowIndex)));
                noteEditor.addChangeListener(changeListner);
                final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mRoot.getGedcom());
                atc.getOpenEditors().add(noteEditor);
                noteEditor.showPanel();
                noteEditor.removeChangeListener(changeListner);
                atc.getOpenEditors().remove(noteEditor);
            }
        }
    }//GEN-LAST:event_notesTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNoteButton;
    private javax.swing.JButton deleteNoteButton;
    private javax.swing.JButton editNoteButton;
    private ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar filterToolBar;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton linkToNoteButton;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable notesTable;
    private javax.swing.JScrollPane notesTableScrollPane;
    private javax.swing.JToolBar notesToolBar;
    // End of variables declaration//GEN-END:variables

    public void setToolBarVisible(boolean visible) {
        notesToolBar.setVisible(visible);
    }

    public Note getSelectedNote() {
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = notesTable.convertRowIndexToModel(selectedRow);
            return mNotesTableModel.getValueAt(rowIndex);
        } else {
            return null;
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

    @Override
    public ComboBoxModel<String> getComboBoxModel() {
        return new DefaultComboBoxModel<>(mNotesTableModel.getColumnsName());
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

        notesTableSorter.setRowFilter(rf);
    }

    private class ChangeListner implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            changeSupport.fireChange();
        }
    }
}
