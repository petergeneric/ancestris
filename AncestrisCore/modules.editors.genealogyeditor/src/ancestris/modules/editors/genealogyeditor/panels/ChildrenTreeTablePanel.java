package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.models.ChildrenTreeTableModel;
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
public class ChildrenTreeTablePanel extends javax.swing.JPanel {

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
            for (int index = 0; index < childrenTreeTable.getColumnCount(); index++) {
                int preferredWidth = childrenTreeTable.getColumn(index).getPreferredWidth();
                logger.log(Level.FINE, "columnMarginChanged: table id {0} column index {1} size {2}", new Object[]{mTableId, index, preferredWidth});
                mRegistry.put(mTableId + ".column" + index + ".size", preferredWidth);
            }
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }
    private final static Logger logger = Logger.getLogger(ChildrenTreeTablePanel.class.getName(), null);
    private ChildrenTreeTableModel mChildrenTreeTableModel = new ChildrenTreeTableModel();
    private Registry mRegistry = Registry.get(ChildrenTreeTablePanel.class);
    private Property mRoot;
    private String mTableId = ChildrenTreeTablePanel.class.getName();
    private Indi mIndividual;
    private PropertyChild mAddedChild = null;

    /**
     * Creates new form ChildrenTreeTablePanel
     */
    public ChildrenTreeTablePanel() {
        initComponents();

        for (int index = 0; index < childrenTreeTable.getColumnModel().getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            childrenTreeTable.getColumnModel().getColumn(index).setPreferredWidth(columnSize);
            logger.log(Level.FINE, "setID: table id {0} column index {1} size {2}", new Object[]{mTableId, index, columnSize});
        }
        HighlightPredicate MyHighlightPredicate = new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                int rowIndex = adapter.row;
                TreePath path = childrenTreeTable.getPathForRow(rowIndex);
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
                TreePath path = childrenTreeTable.getPathForRow(rowIndex);
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
        childrenTreeTable.addHighlighter(new ColorHighlighter(MyHighlightPredicate, childrenTreeTable.getBackground(), Color.blue));
        childrenTreeTable.addHighlighter(new IconHighlighter(FamilyIconpredicate, new ImageIcon("ancestris/modules/editors/genealogyeditor/resources/indi_add.png")));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        childrenScrollPane = new javax.swing.JScrollPane();
        childrenTreeTable = new org.jdesktop.swingx.JXTreeTable(mChildrenTreeTableModel);
        childrenToolBar = new javax.swing.JToolBar();
        addChildrenButton = new javax.swing.JButton();
        linkToChildrenButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        childrenTreeTable.setEditable(false);
        childrenTreeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                childrenTreeTableMouseClicked(evt);
            }
        });
        childrenScrollPane.setViewportView(childrenTreeTable);

        childrenToolBar.setFloatable(false);
        childrenToolBar.setRollover(true);

        addChildrenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addChildrenButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("ChildrenTreeTablePanel.addChildrenButton.toolTipText"), new Object[] {})); // NOI18N
        addChildrenButton.setFocusable(false);
        addChildrenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addChildrenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addChildrenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChildrenButtonActionPerformed(evt);
            }
        });
        childrenToolBar.add(addChildrenButton);

        linkToChildrenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        linkToChildrenButton.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("ChildrenTreeTablePanel.linkToChildrenButton.text"), new Object[] {})); // NOI18N
        linkToChildrenButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("ChildrenTreeTablePanel.linkToChildrenButton.toolTipText"), new Object[] {})); // NOI18N
        linkToChildrenButton.setFocusable(false);
        linkToChildrenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkToChildrenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkToChildrenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkToChildrenButtonActionPerformed(evt);
            }
        });
        childrenToolBar.add(linkToChildrenButton);

        editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editButton.setToolTipText(org.openide.util.NbBundle.getMessage(ChildrenTreeTablePanel.class, "ChildrenTreeTablePanel.editButton.toolTipText")); // NOI18N
        editButton.setFocusable(false);
        editButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        childrenToolBar.add(editButton);

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteButton.setToolTipText(org.openide.util.NbBundle.getMessage(ChildrenTreeTablePanel.class, "ChildrenTreeTablePanel.deleteButton.toolTipText")); // NOI18N
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        childrenToolBar.add(deleteButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(childrenScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addComponent(childrenToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(childrenToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int rowIndex = childrenTreeTable.convertRowIndexToModel(childrenTreeTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            TreePath path = childrenTreeTable.getPathForRow(rowIndex);
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;
                TreeNode parent = dataNode.getParent();

                if (parent instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) parent).isRoot()) {

                    Property property = (Property) dataNode.getUserObject();
                    if (property instanceof PropertyChild) {
                        Indi child = ((PropertyChild) property).getChild();
                        if (!child.equals(mRoot)) {
                            IndividualEditor individualEditor = new IndividualEditor();
                            individualEditor.setContext(new Context(child));
                            individualEditor.showPanel();
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void childrenTreeTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_childrenTreeTableMouseClicked
        if (evt.getClickCount() >= 2) {
            int rowIndex = childrenTreeTable.convertRowIndexToModel(childrenTreeTable.getSelectedRow());
            Gedcom gedcom = mRoot.getGedcom();

            if (rowIndex != -1) {
                TreePath path = childrenTreeTable.getPathForRow(rowIndex);
                Object node = path.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;
                    TreeNode parent = dataNode.getParent();

                    if (parent instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) parent).isRoot()) {
                        Property property = (Property) dataNode.getUserObject();
                        if (property instanceof PropertyChild) {
                            Indi child = ((PropertyChild) property).getChild();
                            if (!child.equals(mRoot)) {
                                IndividualEditor individualEditor = new IndividualEditor();
                                individualEditor.setContext(new Context(child));
                                individualEditor.showPanel();
                            }
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_childrenTreeTableMouseClicked

    private void linkToChildrenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToChildrenButtonActionPerformed
        IndividualsListPanel individualsListPanel = new IndividualsListPanel();
        List<Indi> individualsList = new ArrayList<Indi>(mRoot.getGedcom().getIndis());
        individualsListPanel.setToolBarVisible(false);
        individualsListPanel.set(mRoot, individualsList);
        DialogManager.ADialog individualsListDialog = new DialogManager.ADialog(
                NbBundle.getMessage(IndividualsListPanel.class, "IndividualsListPanel.title.select.child"),
                individualsListPanel);
        individualsListDialog.setDialogId(IndividualsListPanel.class.getName());

        if (individualsListDialog.show() == DialogDescriptor.OK_OPTION) {
            final Indi selectedIndividual = individualsListPanel.getSelectedIndividual();
            if (selectedIndividual != null) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            PropertyXRef addChild = ((Fam) mRoot).addChild(selectedIndividual);
                            mChildrenTreeTableModel.add((PropertyChild) addChild);
                            childrenTreeTable.expandAll();
                        }
                    }); // end of doUnitOfWork

                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_linkToChildrenButtonActionPerformed

    private void addChildrenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChildrenButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();

        try {
            mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mIndividual = (Indi) gedcom.createEntity(Gedcom.INDI);
                    mAddedChild = (PropertyChild) ((Fam) mRoot).addChild(mIndividual);
                    String lastName = "";
                    if (((Fam) mRoot).getHusband() != null) {
                        lastName = ((Fam) mRoot).getHusband().getLastName();
                    } else if (((Fam) mRoot).getWife() != null) {
                        lastName = ((Fam) mRoot).getWife().getLastName();
                    }

                    mIndividual.setName("", lastName);
                }
            }); // end of doUnitOfWork

            IndividualEditor individualEditor = new IndividualEditor();
            individualEditor.setContext(new Context(mIndividual));

            individualEditor.showPanel();
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_addChildrenButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int rowIndex = childrenTreeTable.convertRowIndexToModel(childrenTreeTable.getSelectedRow());
        Gedcom gedcom = mRoot.getGedcom();

        if (rowIndex != -1) {
            TreePath path = childrenTreeTable.getPathForRow(rowIndex);
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                final DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) node;
                final TreeNode parent = dataNode.getParent();

                if (dataNode.getUserObject() instanceof PropertyChild) {
                    if (parent instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) parent).isRoot()) {
                        DialogManager createYesNo = DialogManager.createYesNo(
                                NbBundle.getMessage(
                                        ChildrenTreeTablePanel.class, "ChildrenTreeTablePanel.deleteChildConfirmation.title",
                                        ((PropertyChild) dataNode.getUserObject()).getChild()),
                                NbBundle.getMessage(
                                        ChildrenTreeTablePanel.class, "ChildrenTreeTablePanel.deleteChildConfirmation.text",
                                        ((PropertyChild) dataNode.getUserObject()).getChild(),
                                        mRoot));
                        if (createYesNo.show() == DialogManager.YES_OPTION) {
                            try {
                                gedcom.doUnitOfWork(new UnitOfWork() {

                                    @Override
                                    public void perform(Gedcom gedcom) throws GedcomException {
                                        mRoot.delProperty((PropertyXRef) dataNode.getUserObject());
                                    }
                                }); // end of doUnitOfWork
                                ((ChildrenTreeTableModel) childrenTreeTable.getTreeTableModel()).remove(dataNode);
                                childrenTreeTable.expandAll();
                            } catch (GedcomException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChildrenButton;
    private javax.swing.JScrollPane childrenScrollPane;
    private javax.swing.JToolBar childrenToolBar;
    private org.jdesktop.swingx.JXTreeTable childrenTreeTable;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton linkToChildrenButton;
    // End of variables declaration//GEN-END:variables

    public void set(Property root, List<PropertyChild> children) {
        this.mRoot = root;
        ((ChildrenTreeTableModel) childrenTreeTable.getTreeTableModel()).clear();
        ((ChildrenTreeTableModel) childrenTreeTable.getTreeTableModel()).addAll(children);
        childrenTreeTable.expandAll();
        childrenTreeTable.getColumnModel().addColumnModelListener(new FamiliesTreeTableTableColumnModelListener());
    }
}
