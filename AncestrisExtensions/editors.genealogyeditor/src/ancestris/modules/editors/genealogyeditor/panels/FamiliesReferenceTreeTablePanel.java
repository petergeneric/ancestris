package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.FamiliesTreeTableModel;
import ancestris.modules.editors.genealogyeditor.models.FamilyReferencesTreeTableModel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import genj.util.Registry;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class FamiliesReferenceTreeTablePanel extends javax.swing.JPanel {

    private class FamiliesTreeTableTableColumnModelListener implements TableColumnModelListener {

        private final Logger logger = Logger.getLogger(FamiliesTreeTableTableColumnModelListener.class.getName(), null);

        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnAdded: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnRemoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMoved(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnMoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            logger.log(Level.INFO, "columnMarginChanged: {0}", ce.toString());
            for (int index = 0; index < familiesTreeTable.getColumnCount(); index++) {
                int preferredWidth = familiesTreeTable.getColumn(index).getPreferredWidth();
                logger.log(Level.INFO, "columnMarginChanged: table id {0} column index {1} size {2}", new Object[]{mTableId, index, preferredWidth});
                mRegistry.put(mTableId + ".column" + index + ".size", preferredWidth);
            }
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }
    public static int LIST_FAM = 0;
    public static int EDIT_FAMC = 1;
    public static int EDIT_FAMS = 2;
    private final static Logger logger = Logger.getLogger(FamiliesReferenceTreeTablePanel.class.getName(), null);
    private int mFamilyEditingType = EDIT_FAMC;
    private Registry mRegistry = Registry.get(FamiliesReferenceTreeTablePanel.class);
    private Property mRoot;
    private Fam mCreateFamily = null;
    private String mTableId = FamiliesReferenceTreeTablePanel.class.getName();

    /**
     * Creates new form FamiliesTreeTablePanel
     */
    public FamiliesReferenceTreeTablePanel() {
        this(EDIT_FAMC);
    }

    public FamiliesReferenceTreeTablePanel(int familyEditingType) {
        mFamilyEditingType = familyEditingType;

        initComponents();

        for (int index = 0; index < familiesTreeTable.getColumnModel().getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            familiesTreeTable.getColumnModel().getColumn(index).setPreferredWidth(columnSize);
            logger.log(Level.INFO, "setID: table id {0} column index {1} size {2}", new Object[]{mTableId, index, columnSize});
        }
        HighlightPredicate MyHighlightPredicate = new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                int rowIndex = adapter.row;
                TreePath path = familiesTreeTable.getPathForRow(rowIndex);
                Object lastPathComponent = path.getLastPathComponent();
                if (lastPathComponent instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof PropertyXRef) {
                        Entity entity = ((PropertyXRef) node.getUserObject()).getTargetEntity();
                        return entity.equals(mRoot);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        };
        ColorHighlighter hl = new ColorHighlighter(MyHighlightPredicate, familiesTreeTable.getBackground(), Color.blue);
        familiesTreeTable.addHighlighter(hl);
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

        familiesScrollPane = new javax.swing.JScrollPane();
        familiesTreeTable = new org.jdesktop.swingx.JXTreeTable(new FamilyReferencesTreeTableModel(mFamilyEditingType));
        familyNamesToolBar = new javax.swing.JToolBar();
        addFamilyButton = new javax.swing.JButton();
        linkToFamilyButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteFamilyButton = new javax.swing.JButton();

        familiesTreeTable.setEditable(false);
        familiesTreeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                familiesTreeTableMouseClicked(evt);
            }
        });
        familiesScrollPane.setViewportView(familiesTreeTable);

        familyNamesToolBar.setFloatable(false);
        familyNamesToolBar.setRollover(true);

        addFamilyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesReferenceTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.addFamilyButton.toolTipText")); // NOI18N
        addFamilyButton.setFocusable(false);
        addFamilyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addFamilyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addFamilyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFamilyButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(addFamilyButton);

        linkToFamilyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        linkToFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesReferenceTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.linkToFamilyButton.toolTipText")); // NOI18N
        linkToFamilyButton.setFocusable(false);
        linkToFamilyButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        linkToFamilyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkToFamilyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkToFamilyButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(linkToFamilyButton);

        editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesReferenceTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.editButton.toolTipText")); // NOI18N
        editButton.setFocusable(false);
        editButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(editButton);

        deleteFamilyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesReferenceTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.deleteFamilyButton.toolTipText")); // NOI18N
        deleteFamilyButton.setFocusable(false);
        deleteFamilyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteFamilyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteFamilyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteFamilyButtonActionPerformed(evt);
            }
        });
        familyNamesToolBar.add(deleteFamilyButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(familyNamesToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(familyNamesToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addFamilyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFamilyButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mCreateFamily = (Fam) gedcom.createEntity(Gedcom.FAM);
                    if (mFamilyEditingType == EDIT_FAMC) {
                        mCreateFamily.addChild((Indi) mRoot);
                    } else if (mFamilyEditingType == EDIT_FAMS) {
                        if (((Indi) mRoot).getSex() == PropertySex.MALE) {
                            mCreateFamily.setHusband((Indi) mRoot);
                        } else {
                            mCreateFamily.setWife((Indi) mRoot);
                        }
                    }
                }
            }); // end of doUnitOfWork

            FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
            familyEditorPanel.set(mCreateFamily);

            DialogManager.ADialog familyEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.create.title"),
                    familyEditorPanel);
            familyEditorDialog.setDialogId(FamilyEditorPanel.class.getName());

            if (familyEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).add(familyEditorPanel.commit());
            } else {
                while (gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addFamilyButtonActionPerformed

    private void linkToFamilyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToFamilyButtonActionPerformed
        FamiliesListPanel familiesListPanel = new FamiliesListPanel(LIST_FAM);
        familiesListPanel.setFamiliesList(mRoot, new ArrayList(mRoot.getGedcom().getFamilies()));
        DialogManager.ADialog familiesListDialog = new DialogManager.ADialog(
                NbBundle.getMessage(FamiliesListPanel.class, "FamiliesListPanel.linkto.title"),
                familiesListPanel);
        familiesListDialog.setDialogId(FamiliesListPanel.class.getName());

        if (familiesListDialog.show() == DialogDescriptor.OK_OPTION) {
            final Fam selectedFamily = familiesListPanel.getSelectedFamily();
            if (selectedFamily != null) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            if (mFamilyEditingType == EDIT_FAMC) {
                                selectedFamily.addChild((Indi) mRoot);
                            } else if (mFamilyEditingType == EDIT_FAMS) {
                                if (((Indi) mRoot).getSex() == PropertySex.MALE) {
                                    selectedFamily.setHusband((Indi) mRoot);
                                } else {
                                    selectedFamily.setWife((Indi) mRoot);
                                }
                            }
                        }
                    }); // end of doUnitOfWork
                    ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).add(selectedFamily);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_linkToFamilyButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int rowIndex = familiesTreeTable.convertRowIndexToModel(familiesTreeTable.getSelectedRow());
        if (rowIndex != -1) {
            TreePath path = familiesTreeTable.getPathForRow(rowIndex);
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                Property property = (Property) dataNode.getUserObject();
                if (property instanceof PropertyXRef) {
                    Entity entity = ((PropertyXRef) property).getTargetEntity();
                    if (entity instanceof Fam) {
                        Fam family = (Fam) entity;
                        FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
                        familyEditorPanel.set(family);

                        DialogManager.ADialog familyEditorDialog = new DialogManager.ADialog(
                                NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.edit.title", family),
                                familyEditorPanel);
                        familyEditorDialog.setDialogId(FamilyEditorPanel.class.getName());

                        if (familyEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                            familyEditorPanel.commit();
                        } else {
                            Gedcom gedcom = mRoot.getGedcom();
                            while (gedcom.canUndo()) {
                                gedcom.undoUnitOfWork(false);
                            }
                        }
                    } else if (entity instanceof Indi) {
                        Indi child = (Indi) entity;
                        if (!child.equals(mRoot)) {
                            IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();
                            individualEditorPanel.set(child);

                            DialogManager.ADialog individualEditorDialog = new DialogManager.ADialog(
                                    NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.edit.title", child),
                                    individualEditorPanel);
                            individualEditorDialog.setDialogId(IndividualEditorPanel.class.getName());

                            if (individualEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                                individualEditorPanel.commit();
                            } else {
                                Gedcom gedcom = mRoot.getGedcom();
                                while (gedcom.canUndo()) {
                                    gedcom.undoUnitOfWork(false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void deleteFamilyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFamilyButtonActionPerformed
        int rowIndex = familiesTreeTable.convertRowIndexToModel(familiesTreeTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            TreePath path = familiesTreeTable.getPathForRow(rowIndex);
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                final Property property = (Property) dataNode.getUserObject();
                if (property instanceof PropertyXRef) {
                    Entity entity = ((PropertyXRef) property).getTargetEntity();
                    if (entity instanceof Fam) {
                        Fam family = (Fam) entity;

                        DialogManager createYesNo = DialogManager.createYesNo(
                                NbBundle.getMessage(
                                EventEditorPanel.class, "FamiliesReferenceTreeTablePanel.deleteFamilyConfirmation.title",
                                family),
                                NbBundle.getMessage(
                                EventEditorPanel.class, "FamiliesReferenceTreeTablePanel.deleteFamilyConfirmation.text",
                                family,
                                mRoot));
                        if (createYesNo.show() == DialogManager.YES_OPTION) {
                            try {
                                gedcom.doUnitOfWork(new UnitOfWork() {

                                    @Override
                                    public void perform(Gedcom gedcom) throws GedcomException {
                                        mRoot.delProperty(property);
                                    }
                                }); // end of doUnitOfWork
                                ((FamilyReferencesTreeTableModel) familiesTreeTable.getTreeTableModel()).remove(dataNode);
                            } catch (GedcomException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } else if (entity instanceof Indi) {
                        TreeNode parent = dataNode.getParent();
                        if (parent instanceof DefaultMutableTreeNode) {
                            Property parentProperty = (Property) ((DefaultMutableTreeNode) parent).getUserObject();
                            if (parentProperty instanceof PropertyXRef) {
                                if (((PropertyXRef) parentProperty).getTargetEntity() instanceof Fam) {
                                    final Fam family = (Fam) ((PropertyXRef) parentProperty).getTargetEntity();

                                    DialogManager createYesNo = DialogManager.createYesNo(
                                            NbBundle.getMessage(
                                            EventEditorPanel.class, "FamiliesReferenceTreeTablePanel.deleteChildConfirmation.title",
                                            entity),
                                            NbBundle.getMessage(
                                            EventEditorPanel.class, "FamiliesReferenceTreeTablePanel.deleteChildConfirmation.text",
                                            entity,
                                            family));
                                    if (createYesNo.show() == DialogManager.YES_OPTION) {
                                        try {
                                            gedcom.doUnitOfWork(new UnitOfWork() {

                                                @Override
                                                public void perform(Gedcom gedcom) throws GedcomException {
                                                    family.delProperty(property);
                                                }
                                            }); // end of doUnitOfWork
                                            ((FamilyReferencesTreeTableModel) familiesTreeTable.getTreeTableModel()).remove(dataNode);
                                        } catch (GedcomException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_deleteFamilyButtonActionPerformed

    private void familiesTreeTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_familiesTreeTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int rowIndex = familiesTreeTable.convertRowIndexToModel(familiesTreeTable.getSelectedRow());
            if (rowIndex != -1) {
                TreePath path = familiesTreeTable.getPathForRow(rowIndex);
                Object node = path.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                    Property property = (Property) dataNode.getUserObject();
                    if (property instanceof PropertyXRef) {
                        Entity entity = ((PropertyXRef) property).getTargetEntity();
                        if (entity instanceof Fam) {
                            Fam family = (Fam) entity;
                            FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
                            familyEditorPanel.set(family);

                            DialogManager.ADialog familyEditorDialog = new DialogManager.ADialog(
                                    NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.edit.title", family),
                                    familyEditorPanel);
                            familyEditorDialog.setDialogId(FamilyEditorPanel.class.getName());

                            if (familyEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                                familyEditorPanel.commit();
                            } else {
                                Gedcom gedcom = mRoot.getGedcom();
                                while (gedcom.canUndo()) {
                                    gedcom.undoUnitOfWork(false);
                                }
                            }
                        } else if (entity instanceof Indi) {
                            Indi child = (Indi) entity;
                            if (!child.equals(mRoot)) {
                                IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();
                                individualEditorPanel.set(child);

                                DialogManager.ADialog individualEditorDialog = new DialogManager.ADialog(
                                        NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.edit.title", child),
                                        individualEditorPanel);
                                individualEditorDialog.setDialogId(IndividualEditorPanel.class.getName());

                                if (individualEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                                    individualEditorPanel.commit();
                                } else {
                                    Gedcom gedcom = mRoot.getGedcom();
                                    while (gedcom.canUndo()) {
                                        gedcom.undoUnitOfWork(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_familiesTreeTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFamilyButton;
    private javax.swing.JButton deleteFamilyButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane familiesScrollPane;
    private org.jdesktop.swingx.JXTreeTable familiesTreeTable;
    private javax.swing.JToolBar familyNamesToolBar;
    private javax.swing.JButton linkToFamilyButton;
    // End of variables declaration//GEN-END:variables

    public void setFamiliesList(Property root, List<? extends PropertyXRef> familiesList) {
        this.mRoot = root;
        ((FamilyReferencesTreeTableModel) familiesTreeTable.getTreeTableModel()).addAll(familiesList);
        familiesTreeTable.expandAll();
        familiesTreeTable.getColumnModel().addColumnModelListener(new FamiliesTreeTableTableColumnModelListener());
    }
}
