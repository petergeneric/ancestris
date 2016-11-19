/*
 * ReleveOptionsPanel.java
 *
 * Created on 1 avr. 2012, 10:25:12
 */

package ancestris.modules.releve;

import ancestris.core.pluginservice.AncestrisPlugin;
import java.util.Collection;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class ReleveOptionsPanel extends javax.swing.JPanel  {

    /** Creates new form ConfigPanel */
    public ReleveOptionsPanel() {
        initComponents();

    }

    /**
     * charge les valeurs des options
     * appelé à chaque update de ReleveOptionsPanelController
     */
    void loadPreferences() {
        jCheckBoxCopyCote.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyCoteEnabled", "true")));
        jCheckBoxCopyEventDate.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyEventDateEnabled", "true")));
        jCheckBoxCopyFreeComment.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyFreeCommentEnabled", "true")));
        jCheckBoxCopyNotary.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyNotaryEnabled", "true")));

        jCheckBoxDuplicateRecord.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("DuplicateRecordControlEnabled", "true")));
        jCheckBoxNewValueControl.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("ValueControlEnabled", "true")));
        jCheckBoxGedcomCompletion.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("GedcomCompletionEnabled", "true")));

        // je charge les sources
        Collection< ? extends ReleveTopComponent> tcList = AncestrisPlugin.lookupAll(ReleveTopComponent.class);
        
        if ( tcList.size() > 0 ) {
            // je recupere le premier  ReleveTopComponent           
            ReleveTopComponent currentTc = tcList.iterator().next();
            mergeOptionPanel.initData(null, currentTc.getCurrentFile());
        } else {
            mergeOptionPanel.initData(null, null);
        }
        
        browserOptionsPanel.loadPreferences();

    }

     /**
     * enregistre les valeurs des options
     * appelé à chaque applyChanges  de ReleveOptionsPanelController
     */
    void savePreferences() {
        // options de copie des données dans les nouveaux releves
        NbPreferences.forModule(ReleveTopComponent.class).put("CopyCoteEnabled", String.valueOf(jCheckBoxCopyCote.isSelected()));
        NbPreferences.forModule(ReleveTopComponent.class).put("CopyEventDateEnabled", String.valueOf(jCheckBoxCopyEventDate.isSelected()));
        NbPreferences.forModule(ReleveTopComponent.class).put("CopyFreeCommentEnabled", String.valueOf(jCheckBoxCopyFreeComment.isSelected()));
        NbPreferences.forModule(ReleveTopComponent.class).put("CopyNotaryEnabled", String.valueOf(jCheckBoxCopyNotary.isSelected()));
        //options de controle
        NbPreferences.forModule(ReleveTopComponent.class).put("DuplicateRecordControlEnabled", String.valueOf(jCheckBoxDuplicateRecord.isSelected()));
        NbPreferences.forModule(ReleveTopComponent.class).put("ValueControlEnabled", String.valueOf(jCheckBoxNewValueControl.isSelected()));
        // completion avec un Gedcom
        NbPreferences.forModule(ReleveTopComponent.class).put("GedcomCompletionEnabled", String.valueOf(jCheckBoxGedcomCompletion.isSelected()));

        
        // je notifie les composants pour rafraichir les options
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            tc.getDataManager().updateOptions(
                    jCheckBoxCopyCote.isSelected(),
                    jCheckBoxCopyEventDate.isSelected(),
                    jCheckBoxCopyFreeComment.isSelected(),
                    jCheckBoxCopyNotary.isSelected(),
                    jCheckBoxDuplicateRecord.isSelected(),
                    jCheckBoxNewValueControl.isSelected()                   
                    );
        }

        mergeOptionPanel.savePreferences();
        
        // je notifie les composants pour rafraichir l'affichage de la commune
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            tc.getDataManager().refreshPlaceListeners();
        }
        
        
        browserOptionsPanel.savePreferences();

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanelEditor = new javax.swing.JPanel();
        jCheckBoxDuplicateRecord = new javax.swing.JCheckBox();
        jCheckBoxNewValueControl = new javax.swing.JCheckBox();
        jCheckBoxGedcomCompletion = new javax.swing.JCheckBox();
        jCheckBoxCopyFreeComment = new javax.swing.JCheckBox();
        jCheckBoxCopyEventDate = new javax.swing.JCheckBox();
        jCheckBoxCopyNotary = new javax.swing.JCheckBox();
        jCheckBoxCopyCote = new javax.swing.JCheckBox();
        jPanelExludeCompletion = new javax.swing.JPanel();
        jButtonFirstNameCompletion = new javax.swing.JButton();
        jButtonLastNameCompletion = new javax.swing.JButton();
        jButtonOccupationCompletion = new javax.swing.JButton();
        jButtonConfigEditor = new javax.swing.JButton();
        jLabelFiller = new javax.swing.JLabel();
        browserOptionsPanel = new ancestris.modules.releve.imageBrowser.BrowserOptionsPanel();
        mergeOptionPanel = new ancestris.modules.releve.dnd.MergeOptionPanel();
        jLabelFiller1 = new javax.swing.JLabel();

        setForeground(new java.awt.Color(200, 45, 45));
        setFocusTraversalPolicyProvider(true);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(415, 1000));

        jPanel2.setForeground(new java.awt.Color(200, 45, 45));
        jPanel2.setFocusTraversalPolicyProvider(true);
        jPanel2.setMinimumSize(new java.awt.Dimension(400, 1200));
        jPanel2.setPreferredSize(new java.awt.Dimension(413, 990));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanelEditor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jPanelEditor.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanelEditor.setLayout(new java.awt.GridBagLayout());

        jCheckBoxDuplicateRecord.setSelected(true);
        jCheckBoxDuplicateRecord.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxDuplicateRecord.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxDuplicateRecord, gridBagConstraints);

        jCheckBoxNewValueControl.setSelected(true);
        jCheckBoxNewValueControl.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxNewValueControl.text")); // NOI18N
        jCheckBoxNewValueControl.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxNewValueControl, gridBagConstraints);
        jCheckBoxNewValueControl.getAccessibleContext().setAccessibleName(""); // NOI18N

        jCheckBoxGedcomCompletion.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxGedcomCompletion.text")); // NOI18N
        jCheckBoxGedcomCompletion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGedcomCompletionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxGedcomCompletion, gridBagConstraints);

        jCheckBoxCopyFreeComment.setSelected(true);
        jCheckBoxCopyFreeComment.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxCopyFreeComment.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxCopyFreeComment, gridBagConstraints);

        jCheckBoxCopyEventDate.setSelected(true);
        jCheckBoxCopyEventDate.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxCopyEventDate.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxCopyEventDate, gridBagConstraints);

        jCheckBoxCopyNotary.setSelected(true);
        jCheckBoxCopyNotary.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxCopyNotary.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxCopyNotary, gridBagConstraints);

        jCheckBoxCopyCote.setSelected(true);
        jCheckBoxCopyCote.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxCopyCote.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxCopyCote, gridBagConstraints);

        jButtonFirstNameCompletion.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonFirstNameCompletion.text")); // NOI18N
        jButtonFirstNameCompletion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFirstNameCompletionActionPerformed(evt);
            }
        });
        jPanelExludeCompletion.add(jButtonFirstNameCompletion);

        jButtonLastNameCompletion.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonLastNameCompletion.text")); // NOI18N
        jButtonLastNameCompletion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLastNameCompletionActionPerformed(evt);
            }
        });
        jPanelExludeCompletion.add(jButtonLastNameCompletion);

        jButtonOccupationCompletion.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonOccupationCompletion.text")); // NOI18N
        jButtonOccupationCompletion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOccupationCompletionActionPerformed(evt);
            }
        });
        jPanelExludeCompletion.add(jButtonOccupationCompletion);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jPanelExludeCompletion, gridBagConstraints);

        jButtonConfigEditor.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonConfigEditor.text")); // NOI18N
        jButtonConfigEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigEditorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelEditor.add(jButtonConfigEditor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelEditor.add(jLabelFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanelEditor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(browserOptionsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(mergeOptionPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jLabelFiller1, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel2);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * activate DND for all Treeview components
     * @param evt
     */
    private void jCheckBoxGedcomCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGedcomCompletionActionPerformed
        // je notifie les composants pour rafraichir les options
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
                tc.getDataManager().setGedcomCompletion(jCheckBoxGedcomCompletion.isSelected());
        }
    }//GEN-LAST:event_jCheckBoxGedcomCompletionActionPerformed

    private void jButtonLastNameCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLastNameCompletionActionPerformed
        // TODO add your handling code here:
        ReleveCompletionDialog.showLastNameCompletionPanel();

    }//GEN-LAST:event_jButtonLastNameCompletionActionPerformed

    private void jButtonFirstNameCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFirstNameCompletionActionPerformed
        ReleveCompletionDialog.showFirstNameCompletionPanel();
    }//GEN-LAST:event_jButtonFirstNameCompletionActionPerformed

    private void jButtonConfigEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigEditorActionPerformed
        ReleveEditorConfigDialog.showEditorConfigPanel();
    }//GEN-LAST:event_jButtonConfigEditorActionPerformed

    private void jButtonOccupationCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOccupationCompletionActionPerformed
        ReleveCompletionDialog.showOccupationCompletionPanel();
    }//GEN-LAST:event_jButtonOccupationCompletionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.releve.imageBrowser.BrowserOptionsPanel browserOptionsPanel;
    private javax.swing.JButton jButtonConfigEditor;
    private javax.swing.JButton jButtonFirstNameCompletion;
    private javax.swing.JButton jButtonLastNameCompletion;
    private javax.swing.JButton jButtonOccupationCompletion;
    private javax.swing.JCheckBox jCheckBoxCopyCote;
    private javax.swing.JCheckBox jCheckBoxCopyEventDate;
    private javax.swing.JCheckBox jCheckBoxCopyFreeComment;
    private javax.swing.JCheckBox jCheckBoxCopyNotary;
    private javax.swing.JCheckBox jCheckBoxDuplicateRecord;
    private javax.swing.JCheckBox jCheckBoxGedcomCompletion;
    private javax.swing.JCheckBox jCheckBoxNewValueControl;
    private javax.swing.JLabel jLabelFiller;
    private javax.swing.JLabel jLabelFiller1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelEditor;
    private javax.swing.JPanel jPanelExludeCompletion;
    private javax.swing.JScrollPane jScrollPane1;
    private ancestris.modules.releve.dnd.MergeOptionPanel mergeOptionPanel;
    // End of variables declaration//GEN-END:variables


    
    

    
}
