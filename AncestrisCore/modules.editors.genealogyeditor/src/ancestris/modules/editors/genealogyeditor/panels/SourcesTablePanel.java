package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.models.SourcesTableModel;
import ancestris.modules.editors.genealogyeditor.utilities.AriesFilterPanel;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Source;
import genj.gedcom.UnitOfWork;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class SourcesTablePanel extends javax.swing.JPanel implements AriesFilterPanel {

    private final Gedcom mGedcom;
    private final SourcesTableModel mSourcesTableModel = new SourcesTableModel();
    private Source mSource;
    private final TableRowSorter<TableModel> sourceTableSorter;
    private Registry registry = null;

    /**
     * Creates new form SourcesTablePanel
     */
    public SourcesTablePanel(Gedcom gedcom) {
        this.mGedcom = gedcom;
        initComponents();
        registry = gedcom.getRegistry();
        sourcesTable.setID(SourcesTablePanel.class.getName());
        mSourcesTableModel.addAll((Collection<Source>) gedcom.getEntities(Gedcom.SOUR));
        sourceTableSorter = new TableRowSorter<>(sourcesTable.getModel());
        loadSettings();
        sourcesTable.setRowSorter(sourceTableSorter);
        if (mSourcesTableModel.getRowCount() > 0) {
            editSourceButton.setEnabled(true);
            deleteSourceButton.setEnabled(true);
        } else {
            editSourceButton.setEnabled(false);
            deleteSourceButton.setEnabled(false);
        }
    }
    
    @Override
    public void saveFilterSettings() {
        StringBuilder sb = new StringBuilder();
        List<? extends RowSorter.SortKey> sortKeys = sourceTableSorter.getSortKeys();
        for (int i=0; i < sortKeys.size(); i++) {
            RowSorter.SortKey sk = sortKeys.get(i);
            sb.append(sk.getColumn());
            sb.append(',');
            sb.append(sk.getSortOrder().toString());
            sb.append(';');
        }
        registry.put("Aries.SourceSortOrder", sb.toString());
    }
    
    private void loadSettings() {
        String sortOrder = registry.get("Aries.SourceSortOrder", "");
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
            sourceTableSorter.setSortKeys(sorts);
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
        sourcesToolBar = new javax.swing.JToolBar();
        addSourceButton = new javax.swing.JButton();
        editSourceButton = new javax.swing.JButton();
        deleteSourceButton = new javax.swing.JButton();
        filterToolBar = new ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar(this);
        sourcesTableScrollPane = new javax.swing.JScrollPane();
        sourcesTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        sourcesToolBar.setFloatable(false);
        sourcesToolBar.setRollover(true);

        addSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesTablePanel.addSourceButton.toolTipText"), new Object[] {})); // NOI18N
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
        editSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesTablePanel.editSourceButton.toolTipText"), new Object[] {})); // NOI18N
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
        deleteSourceButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourcesTablePanel.deleteSourceButton.toolTipText"), new Object[] {})); // NOI18N
        deleteSourceButton.setFocusable(false);
        deleteSourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteSourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSourceButtonActionPerformed(evt);
            }
        });
        sourcesToolBar.add(deleteSourceButton);

        jToolBar1.add(sourcesToolBar);
        jToolBar1.add(filterToolBar);

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
            .addComponent(sourcesTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(sourcesTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
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

            SourceEditor sourceEditor = new SourceEditor();
            sourceEditor.setContext(new Context(mSource));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mGedcom);
            atc.getOpenEditors().add(sourceEditor);
            if (sourceEditor.showPanel()) {
                mSourcesTableModel.add(mSource);
                editSourceButton.setEnabled(true);
                deleteSourceButton.setEnabled(true);

            } else {
                while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                    mGedcom.undoUnitOfWork(false);
                }
            }
            atc.getOpenEditors().remove(sourceEditor);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addSourceButtonActionPerformed

    private void editSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSourceButtonActionPerformed
        int selectedRow = sourcesTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = sourcesTable.convertRowIndexToModel(selectedRow);
            int undoNb = mGedcom.getUndoNb();
            Source source = mSourcesTableModel.getValueAt(rowIndex);
            SourceEditor sourceEditor = new SourceEditor();
            sourceEditor.setContext(new Context(source));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mGedcom);
            atc.getOpenEditors().add(sourceEditor);
            if (sourceEditor.showPanel()) {
                mSourcesTableModel.add(source);
            } else {
                while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                    mGedcom.undoUnitOfWork(false);
                }
            }
            atc.getOpenEditors().remove(sourceEditor);
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
                if (mSourcesTableModel.getRowCount() <= 0) {
                    editSourceButton.setEnabled(false);
                    deleteSourceButton.setEnabled(false);
                }
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
                SourceEditor sourceEditor = new SourceEditor();
                Source source = mSourcesTableModel.getValueAt(rowIndex);
                sourceEditor.setContext(new Context(source));
                final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mGedcom);
                atc.getOpenEditors().add(sourceEditor);
                if (!sourceEditor.showPanel()) {
                    while (mGedcom.getUndoNb() > undoNb && mGedcom.canUndo()) {
                        mGedcom.undoUnitOfWork(false);
                    }
                }
                atc.getOpenEditors().remove(sourceEditor);
            }
        }
    }//GEN-LAST:event_sourcesTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceButton;
    private javax.swing.JButton deleteSourceButton;
    private javax.swing.JButton editSourceButton;
    private ancestris.modules.editors.genealogyeditor.utilities.FilterToolBar filterToolBar;
    private javax.swing.JToolBar jToolBar1;
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

    @Override
    public ComboBoxModel<String> getComboBoxModel() {
        return new DefaultComboBoxModel<>(mSourcesTableModel.getColumnsName());
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

        sourceTableSorter.setRowFilter(rf);
    }
    
    
}
