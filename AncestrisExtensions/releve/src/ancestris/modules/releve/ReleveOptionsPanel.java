/*
 * ReleveOptionsPanel.java
 *
 * Created on 1 avr. 2012, 10:25:12
 */

package ancestris.modules.releve;

import ancestris.app.TreeTopComponent;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.dnd.TreeViewDropTarget;
import genj.tree.TreeView;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import org.openide.util.NbBundle;
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
    void load() {
        jCheckBoxCopyCote.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyCoteEnabled", "true")));
        jCheckBoxCopyEventDate.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyEventDateEnabled", "true")));
        jCheckBoxCopyFreeComment.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyFreeCommentEnabled", "true")));
        jCheckBoxCopyNotary.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("CopyNotaryEnabled", "true")));

        jCheckBoxDuplicateRecord.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("DuplicateRecordControlEnabled", "true")));
        jCheckBoxNewValueControl.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("ValueControlEnabled", "true")));
        jCheckBoxGedcomCompletion.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("GedcomCompletionEnabled", "true")));
        jCheckBoxBrowser.setSelected(Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("ImgageBrowserVisible", "false")));

        // je charge la liste des repertoires du browser d'images
        jList1.setModel(ImageDirectoryModel.getModel());
        if (ImageDirectoryModel.getModel().size() > 0 ) {
            jList1.setSelectedIndex(0);
        }

        // je charge les sources
        Collection< ? extends ReleveTopComponent> tcList = AncestrisPlugin.lookupAll(ReleveTopComponent.class);

        if ( tcList.size() > 0 ) {
            ReleveTopComponent currentTc = tcList.iterator().next();
            mergeOptionPanel.initData(null, currentTc.getCurrentFile());
        }

    }

     /**
     * enregistre les valeurs des options
     * appelé à chaque applyChanges  de ReleveOptionsPanelController
     */
    void store() {
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

        NbPreferences.forModule(ReleveTopComponent.class).put("ImgageBrowserVisible", String.valueOf(jCheckBoxBrowser.isSelected()));

        ImageDirectoryModel.getModel().savePreferences();

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

        mergeOptionPanel.saveData();

    }


    /**
     * active le drag and drop avec toutes les vues ouvertes
     */
    public void activateDndWithTreeView() {
       for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
            TreeView view = (TreeView) tc.getView();
            TreeViewDropTarget viewDropTarget = new TreeViewDropTarget();
            viewDropTarget.createDropTarget(view);
        }
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
        jCheckBoxBrowser = new javax.swing.JCheckBox();
        jCheckBoxCopyNotary = new javax.swing.JCheckBox();
        jCheckBoxCopyCote = new javax.swing.JCheckBox();
        jPanelExludeCompletion = new javax.swing.JPanel();
        jButtonFirstNameCompletion = new javax.swing.JButton();
        jButtonLastNameCompletion = new javax.swing.JButton();
        jButtonOccupationCompletion = new javax.swing.JButton();
        jButtonConfigEditor = new javax.swing.JButton();
        jButtonActivateDnd = new javax.swing.JButton();
        jLabelFiller = new javax.swing.JLabel();
        mergeOptionPanel = new ancestris.modules.releve.dnd.MergeOptionPanel();
        jPanelImageBrowser = new javax.swing.JPanel();
        jLabelDirectory = new javax.swing.JLabel();
        jButtonAddDirectory = new javax.swing.JButton();
        jButtonRemoveDirectory = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButtonSwapPreviousDirectory = new javax.swing.JButton();
        jButtonSwapNextDirectory = new javax.swing.JButton();
        fillerPanelVertical = new javax.swing.JPanel();

        setForeground(new java.awt.Color(200, 45, 45));
        setFocusTraversalPolicyProvider(true);
        setLayout(new java.awt.BorderLayout());

        jPanel2.setForeground(new java.awt.Color(200, 45, 45));
        jPanel2.setFocusTraversalPolicyProvider(true);
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

        jCheckBoxBrowser.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jCheckBoxBrowser.text")); // NOI18N
        jCheckBoxBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelEditor.add(jCheckBoxBrowser, gridBagConstraints);

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

        jButtonActivateDnd.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonActivateDnd.text")); // NOI18N
        jButtonActivateDnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActivateDndActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelEditor.add(jButtonActivateDnd, gridBagConstraints);
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
        jPanel2.add(jPanelEditor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(mergeOptionPanel, gridBagConstraints);

        jPanelImageBrowser.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.PanelImageBrowser"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanelImageBrowser.setMaximumSize(new java.awt.Dimension(500, 130));
        jPanelImageBrowser.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelImageBrowser.add(jLabelDirectory, gridBagConstraints);

        jButtonAddDirectory.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonAddDirectory.text")); // NOI18N
        jButtonAddDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelImageBrowser.add(jButtonAddDirectory, gridBagConstraints);

        jButtonRemoveDirectory.setText(org.openide.util.NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.jButtonRemoveDirectory.text")); // NOI18N
        jButtonRemoveDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelImageBrowser.add(jButtonRemoveDirectory, gridBagConstraints);

        jScrollPane2.setMaximumSize(new java.awt.Dimension(800, 130));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(50, 23));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(300, 60));
        jScrollPane2.setRequestFocusEnabled(false);

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3 dddd ddd sdf sdf sdf s f sdf sdfd sdf f sdf rg qr gfqrze qdsfv qsrf", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jList1.setMinimumSize(new java.awt.Dimension(100, 80));
        jList1.setPreferredSize(null);
        jScrollPane2.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelImageBrowser.add(jScrollPane2, gridBagConstraints);

        jButtonSwapPreviousDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowup16.png"))); // NOI18N
        jButtonSwapPreviousDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSwapPreviousDirectoryActionPerformed(evt);
            }
        });
        jPanelImageBrowser.add(jButtonSwapPreviousDirectory, new java.awt.GridBagConstraints());

        jButtonSwapNextDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowdown16.png"))); // NOI18N
        jButtonSwapNextDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSwapNextDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelImageBrowser.add(jButtonSwapNextDirectory, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jPanelImageBrowser, gridBagConstraints);

        fillerPanelVertical.setEnabled(false);
        fillerPanelVertical.setFocusable(false);
        fillerPanelVertical.setRequestFocusEnabled(false);
        fillerPanelVertical.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(fillerPanelVertical, gridBagConstraints);
        fillerPanelVertical.getAccessibleContext().setAccessibleName(""); // NOI18N

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

    /**
     * active drag-and-drop sur les vues du gedcom courant
     * @param evt
     */
    private void jButtonActivateDndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActivateDndActionPerformed
        activateDndWithTreeView();
    }//GEN-LAST:event_jButtonActivateDndActionPerformed

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

    private void jCheckBoxBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBrowserActionPerformed
        // je notifie les editeurs pour rafraichir l'affichage
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            tc.setBrowserVisible(jCheckBoxBrowser.isSelected());
        }

        // j'enregistre immediatement la nouvelle valeur pour qu'elle soit disponible pour les nouveaux editeurs
        NbPreferences.forModule(ReleveTopComponent.class).put("ImgageBrowserVisible", String.valueOf(jCheckBoxBrowser.isSelected()));

    }//GEN-LAST:event_jCheckBoxBrowserActionPerformed

    private void jButtonOccupationCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOccupationCompletionActionPerformed
        ReleveCompletionDialog.showOccupationCompletionPanel();
    }//GEN-LAST:event_jButtonOccupationCompletionActionPerformed

    private void jButtonAddDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddDirectoryActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File defaultDirectory;
        if (jList1.getSelectedValue() != null) {
            defaultDirectory = new File((String)jList1.getSelectedValue());
        } else {
//            String defaultDir = EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from");
//            defaultDirectory = new File(defaultDir);
            FileSystemView fsv = FileSystemView.getFileSystemView();
            defaultDirectory = fsv.getDefaultDirectory();
        }
        if (defaultDirectory != null) {
            fileChooser.setCurrentDirectory(defaultDirectory);
        }
        int fcr = fileChooser.showDialog(this, NbBundle.getMessage(ReleveOptionsPanel.class, "ReleveOptionsPanel.dialogTitle.text"));
        if (fcr == JFileChooser.APPROVE_OPTION) {
            try {
                String directory = fileChooser.getSelectedFile().getCanonicalPath();
                int index = ImageDirectoryModel.getModel().indexOf(directory);
                if ( index == -1 ) {
                    ImageDirectoryModel.getModel().addElement(directory);
                }
                jList1.setSelectedValue(directory, true);
            } catch (IOException ex) {
                return;
            }
        }
        

    }//GEN-LAST:event_jButtonAddDirectoryActionPerformed

    private void jButtonRemoveDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveDirectoryActionPerformed
        // je supprime les elements selectionnés
        for(Object directory : jList1.getSelectedValues()) {
            ImageDirectoryModel.getModel().removeElement(directory);
        }
    }//GEN-LAST:event_jButtonRemoveDirectoryActionPerformed

    private void jButtonSwapPreviousDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwapPreviousDirectoryActionPerformed
        int index = jList1.getSelectedIndex();
        if ( ImageDirectoryModel.getModel().swapPrevious(index) ) {
            jList1.setSelectedIndex(index -1);
        }
    }//GEN-LAST:event_jButtonSwapPreviousDirectoryActionPerformed

    private void jButtonSwapNextDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwapNextDirectoryActionPerformed
         int index = jList1.getSelectedIndex();
        if ( ImageDirectoryModel.getModel().swapNext(index) ) {
            jList1.setSelectedIndex(index +1);
        }
    }//GEN-LAST:event_jButtonSwapNextDirectoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fillerPanelVertical;
    private javax.swing.JButton jButtonActivateDnd;
    private javax.swing.JButton jButtonAddDirectory;
    private javax.swing.JButton jButtonConfigEditor;
    private javax.swing.JButton jButtonFirstNameCompletion;
    private javax.swing.JButton jButtonLastNameCompletion;
    private javax.swing.JButton jButtonOccupationCompletion;
    private javax.swing.JButton jButtonRemoveDirectory;
    private javax.swing.JButton jButtonSwapNextDirectory;
    private javax.swing.JButton jButtonSwapPreviousDirectory;
    private javax.swing.JCheckBox jCheckBoxBrowser;
    private javax.swing.JCheckBox jCheckBoxCopyCote;
    private javax.swing.JCheckBox jCheckBoxCopyEventDate;
    private javax.swing.JCheckBox jCheckBoxCopyFreeComment;
    private javax.swing.JCheckBox jCheckBoxCopyNotary;
    private javax.swing.JCheckBox jCheckBoxDuplicateRecord;
    private javax.swing.JCheckBox jCheckBoxGedcomCompletion;
    private javax.swing.JCheckBox jCheckBoxNewValueControl;
    private javax.swing.JLabel jLabelDirectory;
    private javax.swing.JLabel jLabelFiller;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelEditor;
    private javax.swing.JPanel jPanelExludeCompletion;
    private javax.swing.JPanel jPanelImageBrowser;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private ancestris.modules.releve.dnd.MergeOptionPanel mergeOptionPanel;
    // End of variables declaration//GEN-END:variables


    
    

    // modele
    static public class ImageDirectoryModel extends DefaultListModel {
        final static String ImageBrowserDirectoryPreference = "ImageBrowserDirectories";
        private static ImageDirectoryModel imageDirectoryModel = null;

        public File[] getImageBrowserDirectories() {
            ArrayList<File> directories = new ArrayList<File>();
            for (int i = 0; i < imageDirectoryModel.size(); i++) {
                File directory = new File((String) imageDirectoryModel.get(i));
                if( directory.exists()) {
                    directories.add(directory);
                }
            }

            return directories.toArray(new File[0]);
        }

        static public ImageDirectoryModel getModel() {

            if (imageDirectoryModel == null) {
                imageDirectoryModel = new ImageDirectoryModel();
                imageDirectoryModel.loadPreferences();
            }
            return imageDirectoryModel;
        }
       
        /**
         * charge les repertoires
         */
        private void loadPreferences() {
            this.clear();
            // je recupere la liste des valeurs similaires
            String similarString = NbPreferences.forModule(ImageDirectoryModel.class).get(
                    ImageBrowserDirectoryPreference, "");
            String[] values = similarString.split(";");
            for (int i = 0; i < values.length; i++) {
                if (!values[i].isEmpty()) {
                    this.addElement(values[i]);
                }
            }
        }

        /**
         * enregistre les repertoire
         */
        private void savePreferences() {
            StringBuilder values = new StringBuilder();

            for (int i = 0; i < this.size(); i++) {
                values.append(this.get(i)).append(";");
            }
            NbPreferences.forModule(ImageDirectoryModel.class).put(
                    ImageBrowserDirectoryPreference, values.toString());
        }

         private boolean swapNext(int index ) {
             if ( index < size() -1 && index != -1) {
                 Object directory = remove(index);
                 insertElementAt(directory, index+1);
                 return true;
             } else {
                 Toolkit.getDefaultToolkit().beep();
                 return false;
             }

         }

         private boolean swapPrevious(int index ) {
             if ( index > 0) {
                 Object directory = remove(index);
                 insertElementAt(directory, index-1);
                  return true;
             } else {
                 Toolkit.getDefaultToolkit().beep();
                 return false;
             }

         }
        
    }
}
