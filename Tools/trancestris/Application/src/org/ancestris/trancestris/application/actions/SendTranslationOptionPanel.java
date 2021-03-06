/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class SendTranslationOptionPanel extends javax.swing.JPanel {

    private final SendTranslationOptionsPanelController controller;

    SendTranslationOptionPanel(SendTranslationOptionsPanelController controller) {
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        labelName = new javax.swing.JLabel();
        textFieldName = new javax.swing.JFormattedTextField();
        labelEmailAddress = new javax.swing.JLabel();
        textFieldEmailAddress = new javax.swing.JFormattedTextField();
        labelSMTPHost = new javax.swing.JLabel();
        textFieldSMTPHost = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        textFieldSMTPPort = new javax.swing.JTextField();
        SSLEncryptioncheckBox = new javax.swing.JCheckBox();
        TLSEncryptioncheckBox = new javax.swing.JCheckBox();
        checkBoxAuthenticationRequired = new javax.swing.JCheckBox();
        labelLoginName = new javax.swing.JLabel();
        labelPassord = new javax.swing.JLabel();
        textFieldLoginName = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        noEncryptioncheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();

        org.openide.awt.Mnemonics.setLocalizedText(labelName, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelName.text")); // NOI18N

        textFieldName.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelEmailAddress, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelEmailAddress.text")); // NOI18N

        textFieldEmailAddress.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldEmailAddress.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelSMTPHost, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelSMTPHost.text")); // NOI18N

        textFieldSMTPHost.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldSMTPHost.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.jLabel1.text")); // NOI18N

        textFieldSMTPPort.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldSMTPPort.text")); // NOI18N

        buttonGroup1.add(SSLEncryptioncheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(SSLEncryptioncheckBox, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.SSLEncryptioncheckBox.text")); // NOI18N
        SSLEncryptioncheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SSLEncryptioncheckBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(TLSEncryptioncheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(TLSEncryptioncheckBox, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.TLSEncryptioncheckBox.text")); // NOI18N
        TLSEncryptioncheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TLSEncryptioncheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxAuthenticationRequired, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.checkBoxAuthenticationRequired.text")); // NOI18N
        checkBoxAuthenticationRequired.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        checkBoxAuthenticationRequired.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAuthenticationRequiredActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLoginName, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelLoginName.text")); // NOI18N
        labelLoginName.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(labelPassord, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelPassord.text")); // NOI18N
        labelPassord.setEnabled(false);

        textFieldLoginName.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldLoginName.text")); // NOI18N
        textFieldLoginName.setEnabled(false);

        passwordField.setText(org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.passwordField.text")); // NOI18N
        passwordField.setEnabled(false);

        buttonGroup1.add(noEncryptioncheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(noEncryptioncheckBox, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.noEncryptioncheckBox.text")); // NOI18N
        noEncryptioncheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noEncryptioncheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.jLabel2.text")); // NOI18N

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ancestris/trancestris/application/actions/eye.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jToggleButton1, org.openide.util.NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelLoginName)
                            .addComponent(labelPassord))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldLoginName, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passwordField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(checkBoxAuthenticationRequired)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelEmailAddress)
                            .addComponent(labelName)
                            .addComponent(labelSMTPHost)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldSMTPPort, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(noEncryptioncheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TLSEncryptioncheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SSLEncryptioncheckBox))
                            .addComponent(textFieldSMTPHost, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(textFieldEmailAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(textFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldEmailAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEmailAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldSMTPHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSMTPHost))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(noEncryptioncheckBox)
                    .addComponent(TLSEncryptioncheckBox)
                    .addComponent(SSLEncryptioncheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textFieldSMTPPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxAuthenticationRequired)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLoginName)
                    .addComponent(textFieldLoginName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelPassord)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void checkBoxAuthenticationRequiredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxAuthenticationRequiredActionPerformed
        if (checkBoxAuthenticationRequired.isSelected() == true) {
            labelLoginName.setEnabled(true);
            textFieldLoginName.setEnabled(true);

            labelPassord.setEnabled(true);
            passwordField.setEnabled(true);
        } else {
            labelLoginName.setEnabled(false);
            textFieldLoginName.setEnabled(false);
            textFieldLoginName.setText("");

            labelPassord.setEnabled(false);
            passwordField.setEnabled(false);
            passwordField.setText("");
        }
}//GEN-LAST:event_checkBoxAuthenticationRequiredActionPerformed

    private void noEncryptioncheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noEncryptioncheckBoxActionPerformed
        textFieldSMTPPort.setText("25");
    }//GEN-LAST:event_noEncryptioncheckBoxActionPerformed

    private void TLSEncryptioncheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TLSEncryptioncheckBoxActionPerformed
        textFieldSMTPPort.setText("587");
    }//GEN-LAST:event_TLSEncryptioncheckBoxActionPerformed

    private void SSLEncryptioncheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SSLEncryptioncheckBoxActionPerformed
        textFieldSMTPPort.setText("465");
    }//GEN-LAST:event_SSLEncryptioncheckBoxActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (jToggleButton1.isSelected()) {
            passwordField.setEchoChar((char)0);
        } else {
            passwordField.setEchoChar('*');
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    void load() {
        Preferences modulePreferences = NbPreferences.forModule(SendTranslationOptionPanel.class);
        textFieldName.setText(modulePreferences.get("mail.name", NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldName.text")));
        textFieldEmailAddress.setText(modulePreferences.get("mail.address", NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldEmailAddress.text")));

        textFieldSMTPHost.setText(modulePreferences.get("mail.host", NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.labelSMTPHost.text")));
        textFieldSMTPPort.setText(modulePreferences.get("mail.host.port", NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldSMTPPort.text")));

        noEncryptioncheckBox.setSelected(modulePreferences.getBoolean("mail.host.NoEncryption", true));
        TLSEncryptioncheckBox.setSelected(modulePreferences.getBoolean("mail.host.TLSEncryption", false));
        SSLEncryptioncheckBox.setSelected(modulePreferences.getBoolean("mail.host.SSLEncryption", false));

        checkBoxAuthenticationRequired.setSelected(modulePreferences.getBoolean("mail.host.AuthenticationRequired", false));
        if (checkBoxAuthenticationRequired.isSelected() == true) {
            labelLoginName.setEnabled(true);
            textFieldLoginName.setEnabled(true);
            textFieldLoginName.setText(modulePreferences.get("mail.host.login", NbBundle.getMessage(SendTranslationOptionPanel.class, "SendTranslationOptionPanel.textFieldLoginName.text")));

            labelPassord.setEnabled(true);
            passwordField.setEnabled(true);
            passwordField.setText(modulePreferences.get("mail.host.password", ""));
        } else {
            labelLoginName.setEnabled(false);
            textFieldLoginName.setEnabled(false);
            textFieldLoginName.setText("");

            labelPassord.setEnabled(false);
            passwordField.setEnabled(false);
            passwordField.setText("");
        }
    }

    void store() {
        Preferences modulePreferences = NbPreferences.forModule(SendTranslationOptionPanel.class);
        modulePreferences.put("mail.name", textFieldName.getText());
        modulePreferences.put("mail.address", textFieldEmailAddress.getText());
        modulePreferences.put("mail.host", textFieldSMTPHost.getText());
        modulePreferences.put("mail.host.port", textFieldSMTPPort.getText());
        modulePreferences.putBoolean("mail.host.NoEncryption", noEncryptioncheckBox.isSelected());
        modulePreferences.putBoolean("mail.host.TLSEncryption", TLSEncryptioncheckBox.isSelected());
        modulePreferences.putBoolean("mail.host.SSLEncryption", SSLEncryptioncheckBox.isSelected());
        modulePreferences.putBoolean("mail.host.AuthenticationRequired", checkBoxAuthenticationRequired.isSelected());
        if (checkBoxAuthenticationRequired.isSelected() == true) {
            modulePreferences.put("mail.host.login", textFieldLoginName.getText());
            modulePreferences.put("mail.host.password", String.copyValueOf(passwordField.getPassword()));
        } else {
            modulePreferences.put("mail.host.login", "");
            modulePreferences.put("mail.host.password", "");
        }
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox SSLEncryptioncheckBox;
    private javax.swing.JCheckBox TLSEncryptioncheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkBoxAuthenticationRequired;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel labelEmailAddress;
    private javax.swing.JLabel labelLoginName;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelPassord;
    private javax.swing.JLabel labelSMTPHost;
    private javax.swing.JCheckBox noEncryptioncheckBox;
    private javax.swing.JPasswordField passwordField;
    javax.swing.JFormattedTextField textFieldEmailAddress;
    private javax.swing.JTextField textFieldLoginName;
    javax.swing.JFormattedTextField textFieldName;
    javax.swing.JFormattedTextField textFieldSMTPHost;
    private javax.swing.JTextField textFieldSMTPPort;
    // End of variables declaration//GEN-END:variables
}
