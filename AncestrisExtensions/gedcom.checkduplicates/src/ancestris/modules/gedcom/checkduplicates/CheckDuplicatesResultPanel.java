package ancestris.modules.gedcom.checkduplicates;

import static ancestris.modules.gedcom.checkduplicates.Bundle.CheckDuplicatesResultPanel_duplicateIndexLabel_text;
import ancestris.modules.gedcom.utilities.matchers.PotentialMatch;
import ancestris.modules.viewers.entityviewer.nodes.EntityChildFactory;
import ancestris.modules.viewers.entityviewer.nodes.EntityNode;
import ancestris.modules.viewers.entityviewer.panels.DisplayEntityPanel;
import genj.gedcom.Entity;
import java.awt.Dialog;
import java.util.LinkedList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lemovice
 */
@Messages("CheckDuplicatesResultPanel.duplicateIndexLabel.text=<html>Duplicate <font color=red>{0}</font> of {1} estimate matching <font color=blue>{2}</font>%<html>")
public class CheckDuplicatesResultPanel extends javax.swing.JPanel {

    private DisplayEntityPanel leftDisplayEntityPanel;
    private DisplayEntityPanel rightDisplayEntityPanel;
    LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList;
    int linkedListIndex;

    /**
     * Creates new form CheckDuplicatesResultPanel
     */
    public CheckDuplicatesResultPanel(LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList) {
        initComponents();
        // Synchronize Horizontal and Vertical scroll bars
        leftDisplayEntityPanel.getVerticalScrollBar().setModel(rightDisplayEntityPanel.getVerticalScrollBar().getModel());
        leftDisplayEntityPanel.getHorizontalScrollBar().setModel(rightDisplayEntityPanel.getHorizontalScrollBar().getModel());
        this.matchesLinkedList = matchesLinkedList;
        this.linkedListIndex = 0;

        if (matchesLinkedList.size() > 0) {
            PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
            Entity left = potentialMatch.getLeft();
            leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

            Entity right = potentialMatch.getRight();
            rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

            duplicateIndexLabel.setText(CheckDuplicatesResultPanel_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), potentialMatch.getCertainty()));
            if (linkedListIndex < matchesLinkedList.size() - 1) {
                nextButton.setEnabled(true);
            }
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

        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        leftEntityPanel = new javax.swing.JPanel();
        rightEntityPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        duplicateIndexLabel = new javax.swing.JLabel();
        expandAllButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        mergeButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(720, 438));
        setOpaque(false);
        setRequestFocusEnabled(false);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(666, 374));
        jSplitPane1.setName("");

        leftEntityPanel.setMinimumSize(new java.awt.Dimension(333, 374));
        leftEntityPanel.setPreferredSize(new java.awt.Dimension(333, 374));
        leftEntityPanel.setRequestFocusEnabled(false);
        leftEntityPanel.setLayout(new java.awt.BorderLayout());

        leftDisplayEntityPanel = new DisplayEntityPanel ();
        leftEntityPanel.add(leftDisplayEntityPanel, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(leftEntityPanel);

        rightEntityPanel.setMinimumSize(new java.awt.Dimension(333, 374));
        rightEntityPanel.setPreferredSize(new java.awt.Dimension(333, 374));
        rightEntityPanel.setLayout(new java.awt.BorderLayout());

        rightDisplayEntityPanel = new DisplayEntityPanel ();
        rightEntityPanel.add(rightDisplayEntityPanel, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(rightEntityPanel);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        duplicateIndexLabel.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesResultPanel.class, "CheckDuplicatesResultPanel.duplicateIndexLabel.text")); // NOI18N

        expandAllButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesResultPanel.class, "CheckDuplicatesResultPanel.expandAllButton.text")); // NOI18N
        expandAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandAllButtonActionPerformed(evt);
            }
        });

        nextButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesResultPanel.class, "CheckDuplicatesResultPanel.nextButton.text")); // NOI18N
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesResultPanel.class, "CheckDuplicatesResultPanel.previousButton.text")); // NOI18N
        previousButton.setEnabled(false);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        mergeButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesResultPanel.class, "CheckDuplicatesResultPanel.mergeButton.text")); // NOI18N
        mergeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(duplicateIndexLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addGap(104, 104, 104)
                .addComponent(previousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(expandAllButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mergeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(duplicateIndexLabel)
                    .addComponent(previousButton)
                    .addComponent(expandAllButton)
                    .addComponent(mergeButton)
                    .addComponent(nextButton)))
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void expandAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandAllButtonActionPerformed
        leftDisplayEntityPanel.getBeanTreeView().expandAll();
        rightDisplayEntityPanel.getBeanTreeView().expandAll();
    }//GEN-LAST:event_expandAllButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        linkedListIndex += 1;
        PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
        Entity left = potentialMatch.getLeft();
        leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

        Entity right = potentialMatch.getRight();
        rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

        duplicateIndexLabel.setText(CheckDuplicatesResultPanel_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), potentialMatch.getCertainty()));

        if (linkedListIndex >= matchesLinkedList.size() - 1) {
            nextButton.setEnabled(false);
        }
        if (linkedListIndex > 0) {
            previousButton.setEnabled(true);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        linkedListIndex -= 1;
        PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
        Entity left = potentialMatch.getLeft();
        leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

        Entity right = potentialMatch.getRight();
        rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

        duplicateIndexLabel.setText(CheckDuplicatesResultPanel_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), potentialMatch.getCertainty()));
        if (linkedListIndex <= 0) {
            previousButton.setEnabled(false);
        }
        if (linkedListIndex < matchesLinkedList.size() - 1) {
            nextButton.setEnabled(true);
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void mergeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeButtonActionPerformed
        PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
        MergeEntityPanel mergeEntityPanel = new MergeEntityPanel(potentialMatch.getLeft(), potentialMatch.getRight());
        DialogDescriptor mergeEntityPanelDescriptor = new DialogDescriptor(
                mergeEntityPanel,
                NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicatePanelDescriptor.title"),
                true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(mergeEntityPanelDescriptor);
        dialog.setVisible(true);
        dialog.setModal(false);
        dialog.toFront();
        if (mergeEntityPanelDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            matchesLinkedList.remove(linkedListIndex);
            if (matchesLinkedList.size() > 0) {
                if (linkedListIndex < matchesLinkedList.size() - 1) {
                    potentialMatch = matchesLinkedList.get(linkedListIndex);
                    Entity left = potentialMatch.getLeft();
                    leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

                    Entity right = potentialMatch.getRight();
                    rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

                    duplicateIndexLabel.setText(CheckDuplicatesResultPanel_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), potentialMatch.getCertainty()));
                } else {
                    linkedListIndex = matchesLinkedList.size() - 1;

                    potentialMatch = matchesLinkedList.get(linkedListIndex);
                    Entity left = potentialMatch.getLeft();
                    leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

                    Entity right = potentialMatch.getRight();
                    rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

                    duplicateIndexLabel.setText(CheckDuplicatesResultPanel_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), potentialMatch.getCertainty()));

                    nextButton.setEnabled(false);

                    if (linkedListIndex <= 0) {
                        previousButton.setEnabled(false);
                    }
                }
            } else {
                leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode());
                rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode());
                duplicateIndexLabel.setText("");

                mergeButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_mergeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel duplicateIndexLabel;
    private javax.swing.JButton expandAllButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel leftEntityPanel;
    private javax.swing.JButton mergeButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JPanel rightEntityPanel;
    // End of variables declaration//GEN-END:variables
}
