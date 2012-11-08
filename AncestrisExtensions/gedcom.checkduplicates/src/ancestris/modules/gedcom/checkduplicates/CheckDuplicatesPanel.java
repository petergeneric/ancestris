package ancestris.modules.gedcom.checkduplicates;

import ancestris.modules.gedcom.utilities.PotentialMatch;
import ancestris.modules.viewers.entityviewer.nodes.EntityChildFactory;
import ancestris.modules.viewers.entityviewer.nodes.EntityNode;
import ancestris.modules.viewers.entityviewer.panels.DisplayEntityPanel;
import genj.gedcom.Entity;
import java.util.LinkedList;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;

/**
 *
 * @author lemovice
 */
public class CheckDuplicatesPanel extends javax.swing.JPanel {

    private DisplayEntityPanel leftDisplayEntityPanel;
    private DisplayEntityPanel rightDisplayEntityPanel;
    LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList;
    int linkedListIndex;
    int linkedListSize;

    /**
     * Creates new form CheckDuplicatesPanel
     */
    public CheckDuplicatesPanel(LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList) {
        initComponents();
        this.matchesLinkedList = matchesLinkedList;
        this.linkedListIndex = 0;
        this.linkedListSize = matchesLinkedList.size() - 1;
        if (linkedListSize > 0) {
            PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
            Entity left = potentialMatch.getLeft();
            leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

            Entity right = potentialMatch.getRight();
            rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

            jLabel3.setText("Estimate Percentage of duplication " + Integer.toString(potentialMatch.getCertainty()) + "%");
            if (linkedListIndex < linkedListSize) {
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        leftEntityPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        rightEntityPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        mergeButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName(org.openide.util.NbBundle.getMessage(CheckDuplicatesPanel.class, "CheckDuplicatesPanel.jSplitPane1.name")); // NOI18N

        jPanel1.setPreferredSize(new java.awt.Dimension(297, 291));

        leftEntityPanel.setLayout(new java.awt.BorderLayout());

        leftDisplayEntityPanel = new DisplayEntityPanel ();
        leftEntityPanel.add(leftDisplayEntityPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(leftEntityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftEntityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        rightEntityPanel.setLayout(new java.awt.BorderLayout());

        rightDisplayEntityPanel = new DisplayEntityPanel ();
        rightEntityPanel.add(rightDisplayEntityPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rightEntityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rightEntityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jLabel3.setText("jLabel3");

        mergeButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesPanel.class, "CheckDuplicatesPanel.mergeButton.text")); // NOI18N
        mergeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeButtonActionPerformed(evt);
            }
        });

        nextButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesPanel.class, "CheckDuplicatesPanel.nextButton.text")); // NOI18N
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText(org.openide.util.NbBundle.getMessage(CheckDuplicatesPanel.class, "CheckDuplicatesPanel.previousButton.text")); // NOI18N
        previousButton.setEnabled(false);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addGap(104, 104, 104)
                .addComponent(previousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mergeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel3)
                .addComponent(mergeButton)
                .addComponent(nextButton)
                .addComponent(previousButton))
        );

        add(jPanel3, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void mergeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeButtonActionPerformed
        leftDisplayEntityPanel.getBeanTreeView().expandAll();
        rightDisplayEntityPanel.getBeanTreeView().expandAll();
    }//GEN-LAST:event_mergeButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        linkedListIndex += 1;
        PotentialMatch<? extends Entity> potentialMatch = matchesLinkedList.get(linkedListIndex);
        Entity left = potentialMatch.getLeft();
        leftDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(left), true), left));

        Entity right = potentialMatch.getRight();
        rightDisplayEntityPanel.getExplorerManager().setRootContext(new EntityNode(Children.create(new EntityChildFactory(right), true), right));

        jLabel3.setText("Estimate Percentage of duplication " + Integer.toString(potentialMatch.getCertainty()) + "%");
        if (linkedListIndex >= linkedListSize) {
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

        jLabel3.setText("Estimate Percentage of duplication " + Integer.toString(potentialMatch.getCertainty()) + "%");
        if (linkedListIndex <= 0) {
            previousButton.setEnabled(false);
        }
        if (linkedListIndex < linkedListSize) {
            nextButton.setEnabled(true);
        }
    }//GEN-LAST:event_previousButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel leftEntityPanel;
    private javax.swing.JButton mergeButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JPanel rightEntityPanel;
    // End of variables declaration//GEN-END:variables
}
