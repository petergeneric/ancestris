/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.timeline;

import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import genj.almanac.Almanac;
import genj.util.swing.ListSelectionWidget;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.SpinnerNumberModel;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class AlmanacPanel extends javax.swing.JPanel {

    private ListSelectionWidget<String> almanacsList, almanacCategories;
    private Almanac almanac;

    private SpinnerNumberModel spinmodel;
    public static int MAX_SIG = 9;
    public static int MIN_SIG = 0;
    
    /**
     * Creates new form almanacPanel
     */
    public AlmanacPanel(Almanac almanac, TimelineView view, TimelineViewSettings.Commit commit) {

        this.almanac = almanac;
        
        // List
        almanacsList = new ListSelectionWidget<String>() {
            protected String getText(String choice) {
                return "<html><body>" + choice + "</body></html>";
            }
        };
        almanacsList.setChoices(almanac.getAlmanacs());
        almanacsList.setCheckedChoices(view.getAlmanacList());
        almanacsList.addChangeListener(commit);
        
        // Categories
        almanacCategories = new ListSelectionWidget<String>() {
            protected String getText(String choice) {
                return "<html><body>" + choice + "</body></html>";
            }
        };
        almanacCategories.setChoices(almanac.getCategories());
        almanacCategories.setCheckedChoices(view.getAlmanacCategories());
        almanacCategories.addChangeListener(commit);

        int value = Math.min(MAX_SIG, view.getAlmanacSigLevel());
        spinmodel = new SpinnerNumberModel(value, MIN_SIG, MAX_SIG, 1);
        
        initComponents();
        sigSpinner.addChangeListener(commit);
        listScrollPane.setViewportView(almanacsList);
        catScrollPane.setViewportView(almanacCategories);
        
        
    }
    
    public Set<String> getCheckedAlmanacs() {
        return almanacsList.getCheckedChoices();
    }

    public Set<String> getCheckedCategories() {
        return almanacCategories.getCheckedChoices();
    }

    public int getAlmanacSigLevel() {
        return spinmodel.getNumber().intValue();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new javax.swing.JLabel();
        listScrollPane = new javax.swing.JScrollPane();
        catLabel = new javax.swing.JLabel();
        catScrollPane = new javax.swing.JScrollPane();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        sigLabel = new javax.swing.JLabel();
        sigSpinner = new javax.swing.JSpinner(spinmodel);

        org.openide.awt.Mnemonics.setLocalizedText(listLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.listLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(catLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.catLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sigLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.sigLabel.text")); // NOI18N

        sigSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.sigSpinner.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(listScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(catLabel)
                        .addGap(0, 60, Short.MAX_VALUE))
                    .addComponent(catScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sigLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sigSpinner)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listLabel)
                    .addComponent(catLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton))
                    .addComponent(catScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeButton)
                    .addComponent(sigSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sigLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        File source = new FileChooserBuilder(AlmanacPanel.class.getCanonicalName()+"add")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.add"))
                .setApproveText(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.addButton"))
                .setDefaultExtension(FileChooserBuilder.getAlmanacFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getAlmanacFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .showOpenDialog();
        if (source != null) {
            File dest = new File(almanac.getUserDir() + File.separator + source.getName());
            try {
                FileUtils.copyFile(source, dest);
                almanac.init();
                spinmodel.setStepSize(1); // force commit
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        File file = new FileChooserBuilder(AlmanacPanel.class.getCanonicalName()+"remove")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setDefaultWorkingDirectory(almanac.getUserDir())
                .forceUseOfDefaultWorkingDirectory(true)
                .setTitle(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.remove"))
                .setApproveText(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removeButton"))
                .setDefaultExtension(FileChooserBuilder.getAlmanacFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getAlmanacFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .showOpenDialog();
        if (file != null) {
            Object o = DialogManager.create(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removeconfirm"),
                    NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removemsg", file.getName()))
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptionType(DialogManager.YES_NO_OPTION)
                    .show();
            if (o == DialogManager.YES_OPTION) {
                file.delete();
                almanac.init();
                spinmodel.setStepSize(1); // force commit
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel catLabel;
    private javax.swing.JScrollPane catScrollPane;
    private javax.swing.JLabel listLabel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel sigLabel;
    private javax.swing.JSpinner sigSpinner;
    // End of variables declaration//GEN-END:variables

}
