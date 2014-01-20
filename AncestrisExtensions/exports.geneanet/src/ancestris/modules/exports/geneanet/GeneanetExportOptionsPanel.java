/*
 *  Copyright (C) 2011 dominique
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

final class GeneanetExportOptionsPanel extends javax.swing.JPanel {

    private final GeneanetExportOptionsPanelController controller;

    GeneanetExportOptionsPanel(GeneanetExportOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelGeneral = new javax.swing.JPanel();
        jCheckBoxExportRestricited = new javax.swing.JCheckBox();
        jFormattedTextFieldDuration = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxExportNotes = new javax.swing.JCheckBox();
        jCheckBoxExportSources = new javax.swing.JCheckBox();
        jCheckBoxLogEnable = new javax.swing.JCheckBox();
        jPanelIndis = new javax.swing.JPanel();
        jCheckBoxExportAlive = new javax.swing.JCheckBox();
        jCheckBoxExportEvents = new javax.swing.JCheckBox();
        jCheckBoxExportRelations = new javax.swing.JCheckBox();
        jPanelFam = new javax.swing.JPanel();
        jCheckBoxExportDivorce = new javax.swing.JCheckBox();
        jCheckBoxExportWeddingDetails = new javax.swing.JCheckBox();

        jPanelGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jPanelGeneral.border.title"))); // NOI18N
        jPanelGeneral.setMinimumSize(new java.awt.Dimension(526, 99));

        jCheckBoxExportRestricited.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportRestricited, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportRestricited.text")); // NOI18N
        jCheckBoxExportRestricited.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxExportRestricitedActionPerformed(evt);
            }
        });

        jFormattedTextFieldDuration.setColumns(4);
        jFormattedTextFieldDuration.setText(org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jFormattedTextFieldDuration.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportNotes, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportNotes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportSources, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportSources.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxLogEnable, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxLogEnable.text")); // NOI18N

        javax.swing.GroupLayout jPanelGeneralLayout = new javax.swing.GroupLayout(jPanelGeneral);
        jPanelGeneral.setLayout(jPanelGeneralLayout);
        jPanelGeneralLayout.setHorizontalGroup(
            jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxLogEnable)
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addComponent(jCheckBoxExportRestricited)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1))
                    .addComponent(jCheckBoxExportNotes)
                    .addComponent(jCheckBoxExportSources))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        jPanelGeneralLayout.setVerticalGroup(
            jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxExportRestricited)
                    .addComponent(jFormattedTextFieldDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportNotes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportSources)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jCheckBoxLogEnable))
        );

        jPanelIndis.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jPanelIndis.border.title"))); // NOI18N
        jPanelIndis.setMinimumSize(new java.awt.Dimension(526, 99));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportAlive, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportAlive.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportEvents, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportEvents.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportRelations, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportRelations.text")); // NOI18N

        javax.swing.GroupLayout jPanelIndisLayout = new javax.swing.GroupLayout(jPanelIndis);
        jPanelIndis.setLayout(jPanelIndisLayout);
        jPanelIndisLayout.setHorizontalGroup(
            jPanelIndisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelIndisLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelIndisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxExportAlive)
                    .addComponent(jCheckBoxExportEvents)
                    .addComponent(jCheckBoxExportRelations))
                .addContainerGap(224, Short.MAX_VALUE))
        );
        jPanelIndisLayout.setVerticalGroup(
            jPanelIndisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelIndisLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxExportAlive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportEvents)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportRelations)
                .addContainerGap())
        );

        jPanelFam.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jPanelFam.border.title"))); // NOI18N
        jPanelFam.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanelFam.setPreferredSize(new java.awt.Dimension(0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportDivorce, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportDivorce.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxExportWeddingDetails, org.openide.util.NbBundle.getMessage(GeneanetExportOptionsPanel.class, "GeneanetExportOptionsPanel.jCheckBoxExportWeddingDetails.text")); // NOI18N

        javax.swing.GroupLayout jPanelFamLayout = new javax.swing.GroupLayout(jPanelFam);
        jPanelFam.setLayout(jPanelFamLayout);
        jPanelFamLayout.setHorizontalGroup(
            jPanelFamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFamLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxExportWeddingDetails)
                    .addComponent(jCheckBoxExportDivorce))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelFamLayout.setVerticalGroup(
            jPanelFamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFamLayout.createSequentialGroup()
                .addComponent(jCheckBoxExportWeddingDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExportDivorce))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelGeneral, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelFam, 0, 526, Short.MAX_VALUE)
                    .addComponent(jPanelIndis, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelIndis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelFam, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxExportRestricitedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportRestricitedActionPerformed
        if (jCheckBoxExportRestricited.isSelected()) {
            jFormattedTextFieldDuration.setEnabled(true);
        } else {
            jFormattedTextFieldDuration.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBoxExportRestricitedActionPerformed

    void load() {
        Preferences modulePreferences = NbPreferences.forModule(GeneanetExport.class);
        jCheckBoxExportRestricited.setSelected(modulePreferences.getBoolean("ExportRestricited", true));
        if (jCheckBoxExportRestricited.isSelected()) {
            jFormattedTextFieldDuration.setValue(modulePreferences.getInt("RestricitionDuration", 100));
        } else {
            jFormattedTextFieldDuration.setValue(100);
        }
        jCheckBoxExportNotes.setSelected(modulePreferences.getBoolean("ExportNotes", true));
        jCheckBoxExportSources.setSelected(modulePreferences.getBoolean("ExportSources", true));
        jCheckBoxLogEnable.setSelected(modulePreferences.getBoolean("LogEnable", false));

        jCheckBoxExportAlive.setSelected(modulePreferences.getBoolean("ExportAlive", true));
        jCheckBoxExportEvents.setSelected(modulePreferences.getBoolean("ExportEvents", true));
        jCheckBoxExportRelations.setSelected(modulePreferences.getBoolean("ExportRelations", false));

        jCheckBoxExportWeddingDetails.setSelected(modulePreferences.getBoolean("ExportWeddingDetails", true));
        jCheckBoxExportDivorce.setSelected(modulePreferences.getBoolean("ExportDivorce", false));
    }

    void store() {
        Preferences modulePreferences = NbPreferences.forModule(GeneanetExport.class);
        modulePreferences.putBoolean("ExportRestricited", jCheckBoxExportRestricited.isSelected());
        modulePreferences.putInt("RestricitionDuration", (Integer) jFormattedTextFieldDuration.getValue());
        modulePreferences.putBoolean("ExportNotes", jCheckBoxExportNotes.isSelected());
        modulePreferences.putBoolean("ExportSources", jCheckBoxExportSources.isSelected());
        modulePreferences.putBoolean("LogEnable", jCheckBoxLogEnable.isSelected());

        modulePreferences.putBoolean("ExportAlive", jCheckBoxExportAlive.isSelected());
        modulePreferences.putBoolean("ExportEvents", jCheckBoxExportEvents.isSelected());
        modulePreferences.putBoolean("ExportRelations", jCheckBoxExportRelations.isSelected());

        modulePreferences.putBoolean("ExportWeddingDetails", jCheckBoxExportWeddingDetails.isSelected());
        modulePreferences.putBoolean("ExportDivorce", jCheckBoxExportDivorce.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxExportAlive;
    private javax.swing.JCheckBox jCheckBoxExportDivorce;
    private javax.swing.JCheckBox jCheckBoxExportEvents;
    private javax.swing.JCheckBox jCheckBoxExportNotes;
    private javax.swing.JCheckBox jCheckBoxExportRelations;
    private javax.swing.JCheckBox jCheckBoxExportRestricited;
    private javax.swing.JCheckBox jCheckBoxExportSources;
    private javax.swing.JCheckBox jCheckBoxExportWeddingDetails;
    private javax.swing.JCheckBox jCheckBoxLogEnable;
    private javax.swing.JFormattedTextField jFormattedTextFieldDuration;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelFam;
    private javax.swing.JPanel jPanelGeneral;
    private javax.swing.JPanel jPanelIndis;
    // End of variables declaration//GEN-END:variables
}
