package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
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
import javax.swing.ImageIcon;
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
import org.jdesktop.swingx.decorator.IconHighlighter;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class FamiliesTreeTablePanel extends javax.swing.JPanel {

    private class FamiliesTreeTableTableColumnModelListener implements TableColumnModelListener {

        private final Logger logger = Logger.getLogger(FamiliesTreeTableTableColumnModelListener.class.getName(), null);

        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
            logger.log(Level.FINE, "columnAdded: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
            logger.log(Level.FINE, "columnRemoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMoved(TableColumnModelEvent tcme) {
            logger.log(Level.FINE, "columnMoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            logger.log(Level.FINE, "columnMarginChanged: {0}", ce.toString());
            for (int index = 0; index < familiesTreeTable.getColumnCount(); index++) {
                int preferredWidth = familiesTreeTable.getColumn(index).getPreferredWidth();
                logger.log(Level.FINE, "columnMarginChanged: table id {0} column index {1} size {2}", new Object[]{mTableId, index, preferredWidth});
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
    private final static Logger logger = Logger.getLogger(FamiliesTreeTablePanel.class.getName(), null);
    private int mFamilyEditingType = EDIT_FAMC;
    private Registry mRegistry = Registry.get(FamiliesTreeTablePanel.class);
    private Property mRoot;
    private Fam mCreateFamily = null;
    private String mTableId = FamiliesTreeTablePanel.class.getName();

    /**
     * Creates new form FamiliesTreeTablePanel
     */
    public FamiliesTreeTablePanel() {
        this(EDIT_FAMC);
    }

    public FamiliesTreeTablePanel(int familyEditingType) {
        mFamilyEditingType = familyEditingType;

        initComponents();

        for (int index = 0; index < familiesTreeTable.getColumnModel().getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            familiesTreeTable.getColumnModel().getColumn(index).setPreferredWidth(columnSize);
            logger.log(Level.FINE, "setID: table id {0} column index {1} size {2}", new Object[]{mTableId, index, columnSize});
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
        HighlightPredicate FamilyIconpredicate = new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                int rowIndex = adapter.row;
                TreePath path = familiesTreeTable.getPathForRow(rowIndex);
                Object lastPathComponent = path.getLastPathComponent();
                if (lastPathComponent instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof PropertyXRef) {
                        Entity entity = ((PropertyXRef) node.getUserObject()).getTargetEntity();
                        return entity instanceof Fam;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        };
        familiesTreeTable.addHighlighter(new ColorHighlighter(MyHighlightPredicate, familiesTreeTable.getBackground(), Color.blue));
        familiesTreeTable.addHighlighter(new IconHighlighter(FamilyIconpredicate, new ImageIcon("ancestris/modules/editors/genealogyeditor/resources/indi_add.png")));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        familiesScrollPane = new javax.swing.JScrollPane();
        familiesTreeTable = new org.jdesktop.swingx.JXTreeTable(new FamiliesTreeTableModel(mFamilyEditingType));
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
        addFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.addFamilyButton.toolTipText")); // NOI18N
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
        linkToFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.linkToFamilyButton.toolTipText")); // NOI18N
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
        editButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.editButton.toolTipText")); // NOI18N
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
        deleteFamilyButton.setToolTipText(org.openide.util.NbBundle.getMessage(FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.deleteFamilyButton.toolTipText")); // NOI18N
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
        int undoNb = gedcom.getUndoNb();

        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mCreateFamily = (Fam) gedcom.createEntity(Gedcom.FAM);
                }
            }); // end of doUnitOfWork

            FamilyEditor familyEditor = new FamilyEditor();
            familyEditor.setContext(new Context(mCreateFamily));
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            atc.getOpenEditors().add(familyEditor);
            if (familyEditor.showPanel()) {
                ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).add(mCreateFamily);
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
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
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
            atc.getOpenEditors().remove(familyEditor);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addFamilyButtonActionPerformed

    private void linkToFamilyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToFamilyButtonActionPerformed
        FamiliesTablePanel familiesTablePanel = new FamiliesTablePanel(LIST_FAM);
        familiesTablePanel.set(mRoot, new ArrayList<>(mRoot.getGedcom().getFamilies()));
        DialogManager.ADialog familiesTableDialog = new DialogManager.ADialog(
                NbBundle.getMessage(FamiliesTablePanel.class, "familiesTableDialog.linkto.title"),
                familiesTablePanel);
        familiesTableDialog.setDialogId(FamiliesTablePanel.class.getName());

        if (familiesTableDialog.show() == DialogDescriptor.OK_OPTION) {
            final Fam selectedFamily = familiesTablePanel.getSelectedFamily();
            if (selectedFamily != null) {
                try {
                    ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).add(selectedFamily);
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
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_linkToFamilyButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int rowIndex = familiesTreeTable.convertRowIndexToModel(familiesTreeTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            TreePath path = familiesTreeTable.getPathForRow(rowIndex);
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                Entity entity = (Entity) dataNode.getUserObject();
                if (entity instanceof Fam) {
                    Fam family = (Fam) entity;
                    FamilyEditor familyEditor = new FamilyEditor();
                    familyEditor.setContext(new Context(family));
                    final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                    atc.getOpenEditors().add(familyEditor);
                    familyEditor.showPanel();
                    atc.getOpenEditors().remove(familyEditor);
                } else if (entity instanceof Indi) {
                    Indi child = (Indi) entity;
                    if (!child.equals(mRoot)) {
                        IndividualEditor individualEditor = new IndividualEditor();
                        individualEditor.setContext(new Context(child));
                        final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                        atc.getOpenEditors().add(individualEditor);
                        individualEditor.showPanel();
                        atc.getOpenEditors().remove(individualEditor);
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
                final DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                if (dataNode.getUserObject() instanceof Fam) {
                    final Fam family = (Fam) dataNode.getUserObject();

                    DialogManager createYesNo = DialogManager.createYesNo(
                            NbBundle.getMessage(
                                    FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.deleteFamilyConfirmation.title",
                                    family),
                            NbBundle.getMessage(
                                    FamiliesTreeTablePanel.class, "FamiliesTreeTablePanel.deleteFamilyConfirmation.text",
                                    family,
                                    mRoot));
                    if (createYesNo.show() == DialogManager.YES_OPTION) {
                        try {
                            gedcom.doUnitOfWork(new UnitOfWork() {

                                @Override
                                public void perform(Gedcom gedcom) throws GedcomException {
                                    mRoot.delProperty(family);
                                }
                            }); // end of doUnitOfWork
                            ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).remove(dataNode);
                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else if (dataNode.getUserObject() instanceof PropertyXRef) {
                    if (((PropertyXRef) dataNode.getUserObject()).getTargetEntity() instanceof Indi) {
                        Indi indi = (Indi) ((PropertyXRef) dataNode.getUserObject()).getTargetEntity();
                        TreeNode parent = dataNode.getParent();
                        if (parent instanceof DefaultMutableTreeNode) {
                            if (((DefaultMutableTreeNode) parent).getUserObject() instanceof Fam) {
                                final Fam family = (Fam) ((DefaultMutableTreeNode) parent).getUserObject();

                                DialogManager createYesNo = DialogManager.createYesNo(
                                        NbBundle.getMessage(
                                                FamiliesTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.deleteChildConfirmation.title",
                                                indi),
                                        NbBundle.getMessage(
                                                FamiliesTreeTablePanel.class, "FamiliesReferenceTreeTablePanel.deleteChildConfirmation.text",
                                                indi,
                                                family));
                                if (createYesNo.show() == DialogManager.YES_OPTION) {
                                    try {
                                        gedcom.doUnitOfWork(new UnitOfWork() {

                                            @Override
                                            public void perform(Gedcom gedcom) throws GedcomException {
                                                family.delProperty((PropertyXRef) dataNode.getUserObject());
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
    }//GEN-LAST:event_deleteFamilyButtonActionPerformed

    private void familiesTreeTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_familiesTreeTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int rowIndex = familiesTreeTable.convertRowIndexToModel(familiesTreeTable.getSelectedRow());
            Gedcom gedcom = mRoot.getGedcom();

            if (rowIndex != -1) {
                TreePath path = familiesTreeTable.getPathForRow(rowIndex);
                Object node = path.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;

                    Entity entity = (Entity) dataNode.getUserObject();
                    if (entity instanceof Fam) {
                        Fam family = (Fam) entity;
                        FamilyEditor familyEditor = new FamilyEditor();
                        familyEditor.setContext(new Context(family));
                        final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                        atc.getOpenEditors().add(familyEditor);
                        familyEditor.showPanel();
                        atc.getOpenEditors().remove(familyEditor);
                    } else if (entity instanceof Indi) {
                        Indi child = (Indi) entity;
                        if (!child.equals(mRoot)) {
                            IndividualEditor individualEditor = new IndividualEditor();
                            individualEditor.setContext(new Context(child));
                            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                            atc.getOpenEditors().add(individualEditor);
                            individualEditor.showPanel();
                            atc.getOpenEditors().remove(individualEditor);
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

    public void setFamiliesList(Property root, List<Fam> familiesList) {
        this.mRoot = root;
        ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).clear();
        ((FamiliesTreeTableModel) familiesTreeTable.getTreeTableModel()).addAll(familiesList);
        familiesTreeTable.expandAll();
        familiesTreeTable.getColumnModel().addColumnModelListener(new FamiliesTreeTableTableColumnModelListener());
    }
}
