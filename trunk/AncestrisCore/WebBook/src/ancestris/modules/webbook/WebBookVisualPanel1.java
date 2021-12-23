/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

public final class WebBookVisualPanel1 extends JPanel {

    /** Creates new form WebBookVisualPanel1 */
    public WebBookVisualPanel1() {
        initComponents();
        setComponents();
    }

    public void setComponents() {
        jCheckBox1.setSelected(true);
        jCheckBox2.setSelected(true);
        jCheckBox3.setSelected(true);
        jTextArea1.setEnabled(jCheckBox1.isSelected());
        jTextFieldMessageTitle.setEnabled(jCheckBox1.isSelected());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WebBookWizardAction.class, "CTL_Step_1");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new ancestris.swing.UndoTextArea();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jTextFieldMessageTitle = new javax.swing.JTextField();
        jLabelTitleMessage = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(441, 433));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel2.text")); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Message")); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_DisplayMessage")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel3.text")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextField2.text")); // NOI18N
        jTextField2.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Author")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel6.text")); // NOI18N

        jTextField3.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextField3.text")); // NOI18N
        jTextField3.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Address")); // NOI18N

        jTextField4.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextField4.text")); // NOI18N
        jTextField4.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Phone")); // NOI18N

        jTextField5.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextField5.text")); // NOI18N
        jTextField5.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_Email")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jCheckBox2.text")); // NOI18N
        jCheckBox2.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_DisplayStatsAnc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox3, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jCheckBox3.text")); // NOI18N
        jCheckBox3.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "TTT_DisplayStatsLoc")); // NOI18N

        jTextFieldMessageTitle.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextFieldMessageTitle.text")); // NOI18N
        jTextFieldMessageTitle.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jTextFieldMessageTitle.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelTitleMessage, org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabelTitleMessage.text")); // NOI18N
        jLabelTitleMessage.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel1.class, "WebBookVisualPanel1.jLabelTitleMessage.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelTitleMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jCheckBox1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField5)
                                    .addComponent(jTextField4)
                                    .addComponent(jTextField3)
                                    .addComponent(jTextFieldMessageTitle, javax.swing.GroupLayout.Alignment.TRAILING)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox3)
                            .addComponent(jCheckBox2))
                        .addGap(0, 207, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jCheckBox1))
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldMessageTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTitleMessage))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBox2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox3)
                .addContainerGap(76, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        jTextArea1.setEnabled(jCheckBox1.isSelected());
        jTextFieldMessageTitle.setEnabled(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelTitleMessage;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextFieldMessageTitle;
    // End of variables declaration//GEN-END:variables

    public String getPref01() {
        return jTextField1.getText();
    }
    public void setPref01(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultTitle");
        }
        jTextField1.setText(str);
    }

    public String getPref02() {
        return jTextField2.getText();
    }
    public void setPref02(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultAuthor");
        }
        jTextField2.setText(str);
    }

    public String getPref03() {
        return jTextField3.getText();
    }
    public void setPref03(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultAddress");
        }
        jTextField3.setText(str);
    }

    public String getPref04() {
        return jTextField4.getText();
    }
    public void setPref04(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultPhone");
        }
        jTextField4.setText(str);
    }

    public String getPref05() {
        return jTextField5.getText();
    }
    public void setPref05(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultEmail");
        }
        jTextField5.setText(str);
    }

    public String getPref06() {
        return jCheckBox1.isSelected() ? "1" : "0";
    }
    public void setPref06(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispMsg");
        }
        jCheckBox1.setSelected(str.equals("1"));
    }

    public String getPref07() {
        return jCheckBox2.isSelected() ? "1" : "0";
    }
    public void setPref07(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispStatAncestor");
        }
        jCheckBox2.setSelected(str.equals("1"));
    }

    public String getPref08() {
        return jCheckBox3.isSelected() ? "1" : "0";
    }
    public void setPref08(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispStatLoc");
        }
        jCheckBox3.setSelected(str.equals("1"));
    }

    public String getPref09() {
        return jTextArea1.getText();
    }
    public void setPref09(String str) {
        if (" ".equals(str)) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultMessage");
        }
        jTextArea1.setText(str);
    }
    
    public String getPref10() {
        return jTextFieldMessageTitle.getText();
    }
    public void setPref10(String str) {
        if (" ".equals(str)) {
            str = "";
        }
        jTextFieldMessageTitle.setText(str);
    }

}

