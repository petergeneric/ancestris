/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SendTranslationPanel.java
 *
 * Created on 20 févr. 2012, 21:51:34
 */
package org.ancestris.trancestris.explorers.zipexplorer.actions;

import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class SendTranslationPanel extends javax.swing.JPanel {

//    private javax.swing.ImageIcon ancestris_logo = new javax.swing.ImageIcon(getClass().getResource("/org/ancestris/trancestris/explorer/zipExplorer/actions/ancestris_logo.gif")); // NOI18N
    /** Creates new form SendTranslationPanel */
    public SendTranslationPanel() {
        Preferences modulePreferences = NbPreferences.forModule(SendTranslationPanel.class);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        subjectLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        mailLabel = new javax.swing.JLabel();
        mailToLabel = new javax.swing.JLabel();
        mailToFormattedTextField = new javax.swing.JFormattedTextField();
        emailFormattedTextField = new javax.swing.JFormattedTextField();
        nameFormattedTextField = new javax.swing.JFormattedTextField();
        subjectFormattedTextField = new javax.swing.JFormattedTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();

        subjectLabel.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.subjectLabel.text")); // NOI18N

        messageLabel.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.messageLabel.text")); // NOI18N

        nameLabel.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.nameLabel.text")); // NOI18N

        mailLabel.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.mailLabel.text")); // NOI18N

        mailToLabel.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.mailToLabel.text")); // NOI18N

        emailFormattedTextField.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.emailFormattedTextField.text")); // NOI18N

        nameFormattedTextField.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.nameFormattedTextField.text")); // NOI18N

        subjectFormattedTextField.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.subjectFormattedTextField.text")); // NOI18N

        messageTextArea.setColumns(20);
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(5);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane2.setViewportView(messageTextArea);

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getSize()+4f));
        jLabel7.setForeground(new java.awt.Color(0, 102, 255));
        jLabel7.setText(org.openide.util.NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.jLabel7.text")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(500, 67));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(mailLabel)
                    .addComponent(mailToLabel)
                    .addComponent(subjectLabel)
                    .addComponent(messageLabel))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(nameFormattedTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .addComponent(emailFormattedTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .addComponent(mailToFormattedTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(subjectFormattedTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(102, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mailLabel)
                    .addComponent(emailFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mailToLabel)
                    .addComponent(mailToFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectLabel)
                    .addComponent(subjectFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(253, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JFormattedTextField emailFormattedTextField;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel mailLabel;
    javax.swing.JFormattedTextField mailToFormattedTextField;
    private javax.swing.JLabel mailToLabel;
    private javax.swing.JLabel messageLabel;
    javax.swing.JTextArea messageTextArea;
    javax.swing.JFormattedTextField nameFormattedTextField;
    private javax.swing.JLabel nameLabel;
    javax.swing.JFormattedTextField subjectFormattedTextField;
    private javax.swing.JLabel subjectLabel;
    // End of variables declaration//GEN-END:variables

    public String getEmailFormattedTextField() {
        return emailFormattedTextField.getText();
    }

    public void setEmailFormattedTextField(String email) {
        this.emailFormattedTextField.setText(email);
    }

    public String getMailToFormattedTextField() {
        return mailToFormattedTextField.getText();
    }

    public void setMailToFormattedTextField(String mailTo) {
        this.mailToFormattedTextField.setText(mailTo);
    }

    public String getNameFormattedTextField() {
        return nameFormattedTextField.getText();
    }

    public void setNameFormattedTextField(String nameFormattedTextField) {
        this.nameFormattedTextField.setText(nameFormattedTextField);
    }

    public String getSubjectFormattedTextField() {
        return subjectFormattedTextField.getText();
    }

    public void setSubjectFormattedTextField(String subjectFormattedTextField) {
        this.subjectFormattedTextField.setText(subjectFormattedTextField);
    }

    public String getMessageTextArea() {
        return messageTextArea.getText();
    }
}
