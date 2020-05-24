package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.models.MultiMediaObjectCitationsTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public class MultimediaObjectCitationsTablePanel extends javax.swing.JPanel {

    private Property mRoot;
    private Property mMultiMediaObject;
    private String mGedcomVersion = "";
    private final MultiMediaObjectCitationsTableModel multiMediaObjectCitationsTableModel = new MultiMediaObjectCitationsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(MultimediaObjectCitationsTablePanel.class);

    /**
     * Creates new form MultimediaObjectCitationsTablePanel
     */
    public MultimediaObjectCitationsTablePanel() {
        initComponents();
        multiMediaObjectCitationsTable.setID(MultimediaObjectCitationsTablePanel.class.getName());
//        multiMediaObjectCitationsTable.setRowHeight(36);
        multiMediaObjectCitationsTable.getColumnModel().getColumn(0).setPreferredWidth(36);
        multiMediaObjectCitationsTable.getColumnModel().getColumn(0).setMinWidth(36);
        multiMediaObjectCitationsTable.getColumnModel().getColumn(0).setMaxWidth(36);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        galleryToolBar = new javax.swing.JToolBar();
        addMMObjectButton = new javax.swing.JButton();
        linkMMObjectButton = new javax.swing.JButton();
        editMMObjecButton = new javax.swing.JButton();
        deleteMMObjectButton = new javax.swing.JButton();
        prefMediaEventButton = new javax.swing.JButton();
        multiMediaObjectCitationsScrollPane = new javax.swing.JScrollPane();
        multiMediaObjectCitationsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        setMinimumSize(new java.awt.Dimension(453, 57));

        galleryToolBar.setFloatable(false);
        galleryToolBar.setRollover(true);
        galleryToolBar.setPreferredSize(new java.awt.Dimension(453, 57));

        addMMObjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsTablePanel.addMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
        addMMObjectButton.setFocusable(false);
        addMMObjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addMMObjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addMMObjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMMObjectButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(addMMObjectButton);

        linkMMObjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        linkMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsTablePanel.linkMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
        linkMMObjectButton.setFocusable(false);
        linkMMObjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkMMObjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkMMObjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkMMObjectButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(linkMMObjectButton);

        editMMObjecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editMMObjecButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsTablePanel.editMMObjecButton.toolTipText"), new Object[] {})); // NOI18N
        editMMObjecButton.setFocusable(false);
        editMMObjecButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editMMObjecButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editMMObjecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMMObjecButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(editMMObjecButton);

        deleteMMObjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsTablePanel.deleteMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
        deleteMMObjectButton.setFocusable(false);
        deleteMMObjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteMMObjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteMMObjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMMObjectButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(deleteMMObjectButton);

        prefMediaEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/star.png"))); // NOI18N
        prefMediaEventButton.setText(org.openide.util.NbBundle.getMessage(MultimediaObjectCitationsTablePanel.class, "MultimediaObjectCitationsTablePanel.prefMediaEventButton.text")); // NOI18N
        prefMediaEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultimediaObjectCitationsTablePanel.class, "MultimediaObjectCitationsTablePanel.prefMediaEventButton.toolTipText")); // NOI18N
        prefMediaEventButton.setIconTextGap(0);
        prefMediaEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        prefMediaEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefMediaEventButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(prefMediaEventButton);

        multiMediaObjectCitationsTable.setModel(multiMediaObjectCitationsTableModel);
        multiMediaObjectCitationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                multiMediaObjectCitationsTableMouseClicked(evt);
            }
        });
        multiMediaObjectCitationsScrollPane.setViewportView(multiMediaObjectCitationsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(galleryToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(multiMediaObjectCitationsScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(galleryToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multiMediaObjectCitationsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMMObjectButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();

        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    if (mGedcomVersion.equals("5.5.1")) {
                        mMultiMediaObject = mRoot.getGedcom().createEntity("OBJE");
                        mRoot.addMedia((Media) mMultiMediaObject);
                    } else {
                        mMultiMediaObject = mRoot.addProperty("OBJE", "");
                    }
                }
            }); // end of doUnitOfWork

            MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
            multiMediaObjectEditor.setContext(new Context(mMultiMediaObject));

            multiMediaObjectEditor.addChangeListener(changeListner);
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            atc.getOpenEditors().add(multiMediaObjectEditor);
            if (multiMediaObjectEditor.showPanel()) {
                multiMediaObjectCitationsTableModel.clear();
                multiMediaObjectCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("OBJE")));
                int row = multiMediaObjectCitationsTableModel.getRowOf(mMultiMediaObject);
                multiMediaObjectCitationsTable.getSelectionModel().setSelectionInterval(row, row);
                editMMObjecButton.setEnabled(true);
                deleteMMObjectButton.setEnabled(true);
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            multiMediaObjectEditor.removeChangeListener(changeListner);
            atc.getOpenEditors().remove(multiMediaObjectEditor);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addMMObjectButtonActionPerformed

    private void deleteMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMMObjectButtonActionPerformed
        int selectedRow = multiMediaObjectCitationsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        final int rowIndex = multiMediaObjectCitationsTable.convertRowIndexToModel(selectedRow);
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            final Property multiMediaObjectRef = multiMediaObjectCitationsTableModel.getValueAt(rowIndex);
            Property file = multiMediaObjectRef.getProperty("FILE", true);
            String objectName;
            if (file != null && file instanceof PropertyFile) {
                objectName = ((PropertyFile) file).getInput().get().getLocation();
            } else {
                objectName = "";
            }
            DialogManager createYesNo = DialogManager.createYesNo(NbBundle.getMessage(MultimediaObjectCitationsTablePanel.class, "MultimediaObjectCitationsTableDialog.deleteObjectConfirmation.title",
                    objectName),
                    NbBundle.getMessage(MultimediaObjectCitationsTablePanel.class, "MultimediaObjectCitationsTableDialog.deleteObjectConfirmation.text",
                            objectName,
                            mRoot));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    multiMediaObjectCitationsTableModel.remove(rowIndex);
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRoot.delProperty(multiMediaObjectRef);
                        }
                    }); // end of doUnitOfWork

                    if (multiMediaObjectCitationsTableModel.getRowCount() <= 0) {
                        editMMObjecButton.setEnabled(false);
                        deleteMMObjectButton.setEnabled(false);
                    }
                    changeSupport.fireChange();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteMMObjectButtonActionPerformed

    private void editMMObjecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMMObjecButtonActionPerformed
        int selectedRow = multiMediaObjectCitationsTable.getSelectedRow();

        if (selectedRow != -1) {
            int rowIndex = multiMediaObjectCitationsTable.convertRowIndexToModel(selectedRow);
            Property multiMediaObject = multiMediaObjectCitationsTableModel.getValueAt(rowIndex);
            MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
            multiMediaObjectEditor.setContext(new Context(multiMediaObject));

            multiMediaObjectEditor.addChangeListener(changeListner);
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mRoot.getGedcom());
            atc.getOpenEditors().add(multiMediaObjectEditor);
            if (multiMediaObjectEditor.showPanel()) {
                multiMediaObjectCitationsTable.tableChanged(null);
            }
            multiMediaObjectEditor.removeChangeListener(changeListner);
            atc.getOpenEditors().remove(multiMediaObjectEditor);
        }
    }//GEN-LAST:event_editMMObjecButtonActionPerformed

    private void multiMediaObjectCitationsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multiMediaObjectCitationsTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int selectedRow = multiMediaObjectCitationsTable.getSelectedRow();
            if (selectedRow != -1) {
                int rowIndex = multiMediaObjectCitationsTable.convertRowIndexToModel(selectedRow);
                Property multiMediaObject = multiMediaObjectCitationsTableModel.getValueAt(rowIndex);

                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                multiMediaObjectEditor.setContext(new Context(multiMediaObject));

                multiMediaObjectEditor.addChangeListener(changeListner);
                final AriesTopComponent atc = AriesTopComponent.findEditorWindow(mRoot.getGedcom());
                atc.getOpenEditors().add(multiMediaObjectEditor);
                if (multiMediaObjectEditor.showPanel()) {
                    multiMediaObjectCitationsTable.tableChanged(null);
                }

                multiMediaObjectEditor.removeChangeListener(changeListner);
                atc.getOpenEditors().remove(multiMediaObjectEditor);
            }
        }
    }//GEN-LAST:event_multiMediaObjectCitationsTableMouseClicked

    private void linkMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkMMObjectButtonActionPerformed
        List<Media> notesList = new ArrayList<>((Collection<Media>) mRoot.getGedcom().getEntities(Gedcom.OBJE));

        MultimediaObjectTablePanel multimediaObjectTablePanel = new MultimediaObjectTablePanel();
        multimediaObjectTablePanel.set(mRoot, notesList);
        DialogManager.ADialog multimediaObjectTableDialog = new DialogManager.ADialog(
                NbBundle.getMessage(MultimediaObjectTablePanel.class, "MultimediaObjectTableDialog.linkTo.title"),
                multimediaObjectTablePanel);
        multimediaObjectTableDialog.setDialogId(MultimediaObjectTablePanel.class.getName());

        if (multimediaObjectTableDialog.show() == DialogDescriptor.OK_OPTION) {
            final Media selectedMultimediaObject = multimediaObjectTablePanel.getSelectedMultiMediaObject();
            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mRoot.addMedia(selectedMultimediaObject);
                    }
                }); // end of doUnitOfWork

                multiMediaObjectCitationsTableModel.clear();
                multiMediaObjectCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("OBJE")));
                int row = multiMediaObjectCitationsTableModel.getRowOf(selectedMultimediaObject);
                multiMediaObjectCitationsTable.getSelectionModel().setSelectionInterval(row, row);
                editMMObjecButton.setEnabled(true);
                deleteMMObjectButton.setEnabled(true);
                changeSupport.fireChange();
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_linkMMObjectButtonActionPerformed

    private void prefMediaEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefMediaEventButtonActionPerformed
        int selectedRow = multiMediaObjectCitationsTable.getSelectedRow();

        if (selectedRow != -1) {
            int rowIndex = multiMediaObjectCitationsTable.convertRowIndexToModel(selectedRow);
            final Property selectedMultimediaObject = multiMediaObjectCitationsTableModel.getValueAt(rowIndex);

            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        // Move properrty to first media
                        Property p = selectedMultimediaObject.getParent().getProperty("OBJE");
                        int pos = selectedMultimediaObject.getParent().getPropertyPosition(p);
                        selectedMultimediaObject.getParent().moveProperty(selectedMultimediaObject, pos);
                    }
                }); // end of doUnitOfWork

                // Refresh list of media to display
                multiMediaObjectCitationsTableModel.clear();
                multiMediaObjectCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("OBJE")));
                int row = multiMediaObjectCitationsTableModel.getRowOf(selectedMultimediaObject);
                multiMediaObjectCitationsTable.getSelectionModel().setSelectionInterval(row, row);
                editMMObjecButton.setEnabled(true);
                deleteMMObjectButton.setEnabled(true);
                changeSupport.fireChange();
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }//GEN-LAST:event_prefMediaEventButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMMObjectButton;
    private javax.swing.JButton deleteMMObjectButton;
    private javax.swing.JButton editMMObjecButton;
    private javax.swing.JToolBar galleryToolBar;
    private javax.swing.JButton linkMMObjectButton;
    private javax.swing.JScrollPane multiMediaObjectCitationsScrollPane;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable multiMediaObjectCitationsTable;
    private javax.swing.JButton prefMediaEventButton;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Property> multiMediasList) {
        if (root == null) {
            return;
        }
        mGedcomVersion = root.getGedcom().getGrammar().getVersion();
        this.mRoot = root;
        multiMediaObjectCitationsTableModel.clear();
        multiMediaObjectCitationsTableModel.addAll(multiMediasList);
        if (multiMediaObjectCitationsTableModel.getRowCount() > 0) {
            editMMObjecButton.setEnabled(true);
            deleteMMObjectButton.setEnabled(true);
            multiMediaObjectCitationsTable.getSelectionModel().setSelectionInterval(0, 0);
        } else {
            editMMObjecButton.setEnabled(false);
            deleteMMObjectButton.setEnabled(false);
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
