package ancestris.modules.editors.genealogyeditor.panels;

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
public class MultimediaObjectCitationsListPanel extends javax.swing.JPanel {

    private Property mRoot;
    private Property mMultiMediaObject;
    private String mGedcomVersion = "";
    private MultiMediaObjectCitationsTableModel multiMediaObjectCitationsTableModel = new MultiMediaObjectCitationsTableModel();
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(MultimediaObjectCitationsListPanel.class);

    /**
     * Creates new form MultimediaObjectsListPanel
     */
    public MultimediaObjectCitationsListPanel() {
        initComponents();
        multiMediaObjectCitationsTable.setID(MultimediaObjectCitationsListPanel.class.getName());
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
        multiMediaObjectCitationsScrollPane = new javax.swing.JScrollPane();
        multiMediaObjectCitationsTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        setMinimumSize(new java.awt.Dimension(453, 57));

        galleryToolBar.setFloatable(false);
        galleryToolBar.setRollover(true);
        galleryToolBar.setPreferredSize(new java.awt.Dimension(453, 57));

        addMMObjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsListPanel.addMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
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
        linkMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsListPanel.linkMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
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
        editMMObjecButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsListPanel.editMMObjecButton.toolTipText"), new Object[] {})); // NOI18N
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
        deleteMMObjectButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("MultimediaObjectCitationsListPanel.deleteMMObjectButton.toolTipText"), new Object[] {})); // NOI18N
        deleteMMObjectButton.setFocusable(false);
        deleteMMObjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteMMObjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteMMObjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMMObjectButtonActionPerformed(evt);
            }
        });
        galleryToolBar.add(deleteMMObjectButton);

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
                .addComponent(multiMediaObjectCitationsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
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
            if (multiMediaObjectEditor.showPanel()) {
                multiMediaObjectCitationsTableModel.clear();
                multiMediaObjectCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("OBJE")));
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            multiMediaObjectEditor.removeChangeListener(changeListner);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addMMObjectButtonActionPerformed

    private void deleteMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMMObjectButtonActionPerformed
        final int rowIndex = multiMediaObjectCitationsTable.convertRowIndexToModel(multiMediaObjectCitationsTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            final Property multiMediaObjectRef = multiMediaObjectCitationsTableModel.getValueAt(rowIndex);
            Property file = multiMediaObjectRef.getProperty("FILE", true);
            String objectName;
            if (file != null && file instanceof PropertyFile) {
                objectName = ((PropertyFile) file).getFile().getAbsolutePath();
            } else {
                objectName = "";
            }
            DialogManager createYesNo = DialogManager.createYesNo(
                    NbBundle.getMessage(
                            MultimediaObjectCitationsListPanel.class, "MultimediaObjectCitationsListPanel.deleteObjectConfirmation.title",
                            objectName),
                    NbBundle.getMessage(
                            MultimediaObjectCitationsListPanel.class, "MultimediaObjectCitationsListPanel.deleteObjectConfirmation.text",
                            objectName,
                            mRoot));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mRoot.delProperty(multiMediaObjectRef);
                        }
                    }); // end of doUnitOfWork

                    multiMediaObjectCitationsTableModel.remove(rowIndex);
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
            multiMediaObjectEditor.showPanel();
            multiMediaObjectEditor.removeChangeListener(changeListner);
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
                multiMediaObjectEditor.showPanel();
                multiMediaObjectEditor.removeChangeListener(changeListner);
            }
        }
    }//GEN-LAST:event_multiMediaObjectCitationsTableMouseClicked

    private void linkMMObjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkMMObjectButtonActionPerformed
        List<Media> notesList = new ArrayList<Media>((Collection<Media>) mRoot.getGedcom().getEntities(Gedcom.OBJE));

        MultimediaObjectListPanel multimediaObjectListPanel = new MultimediaObjectListPanel();
        multimediaObjectListPanel.set(mRoot, notesList);
        DialogManager.ADialog multimediaObjectListDialog = new DialogManager.ADialog(
                NbBundle.getMessage(MultimediaObjectListPanel.class, "MultimediaObjectListPanel.linkTo.title"),
                multimediaObjectListPanel);
        multimediaObjectListDialog.setDialogId(MultimediaObjectListPanel.class.getName());

        if (multimediaObjectListDialog.show() == DialogDescriptor.OK_OPTION) {
            final Media selectedMultimediaObject = multimediaObjectListPanel.getSelectedMultiMediaObject();
            try {
                mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mRoot.addMedia(selectedMultimediaObject);
                    }
                }); // end of doUnitOfWork

                changeSupport.fireChange();
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            multiMediaObjectCitationsTableModel.clear();
            multiMediaObjectCitationsTableModel.addAll(Arrays.asList(mRoot.getProperties("OBJE")));
        }
    }//GEN-LAST:event_linkMMObjectButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMMObjectButton;
    private javax.swing.JButton deleteMMObjectButton;
    private javax.swing.JButton editMMObjecButton;
    private javax.swing.JToolBar galleryToolBar;
    private javax.swing.JButton linkMMObjectButton;
    private javax.swing.JScrollPane multiMediaObjectCitationsScrollPane;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable multiMediaObjectCitationsTable;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<Property> multiMediasList) {
        mGedcomVersion = root.getGedcom().getGrammar().getVersion();
        this.mRoot = root;
        multiMediaObjectCitationsTableModel.clear();
        multiMediaObjectCitationsTableModel.addAll(multiMediasList);
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
