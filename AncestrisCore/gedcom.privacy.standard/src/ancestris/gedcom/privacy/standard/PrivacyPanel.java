/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom.privacy.standard;

final class PrivacyPanel extends javax.swing.JPanel {

    private final PrivacyOptionsPanelController controller;

    PrivacyPanel(PrivacyOptionsPanelController controller) {
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        titleReplace = new javax.swing.JLabel();
        jcbLivings = new javax.swing.JCheckBox();
        jcbDead = new javax.swing.JCheckBox();
        jsYears = new javax.swing.JSpinner();
        jsLivingYears = new javax.swing.JSpinner();
        jtPrivTag = new javax.swing.JTextField();
        jtReplace = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel1.text")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel5.text")); // NOI18N

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getStyle() | java.awt.Font.BOLD, jLabel6.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel6.toolTipText")); // NOI18N

        titleReplace.setFont(titleReplace.getFont().deriveFont(titleReplace.getFont().getStyle() | java.awt.Font.BOLD, titleReplace.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(titleReplace, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.titleReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jcbLivings, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jcbLivings.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jcbDead, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jcbDead.text")); // NOI18N

        jsYears.setModel(new javax.swing.SpinnerNumberModel());

        jsLivingYears.setModel(new javax.swing.SpinnerNumberModel());

        jtPrivTag.setText(org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jtPrivTag.text")); // NOI18N

        jtReplace.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtReplace.setText(org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jtReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(PrivacyPanel.class, "PrivacyPanel.jLabel7.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel6)
                            .addComponent(titleReplace))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jcbDead)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jsYears, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addComponent(jtReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jcbLivings)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jsLivingYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtPrivTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jtPrivTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbLivings)
                    .addComponent(jsLivingYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(jsYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel6)
                    .addComponent(jcbDead))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(titleReplace)
                    .addComponent(jtReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        Options privOpt = Options.getInstance();
        jcbLivings.setSelected(privOpt.aliveIsPrivate());
        jcbDead.setSelected(privOpt.deadIsPublic());
        jsLivingYears.setValue(privOpt.getYearsIndiCanBeAlive());

//        SpinnerModel model =new SpinnerNumberModel(privOpt.getPrivateYears(),0,1000,1);
//        jsYears.setModel(model); //setValue(privOpt.getPrivateYears());
        jsYears.setValue(privOpt.getPrivateYears());
        jtPrivTag.setText(privOpt.getPrivateTag());
        jtReplace.setText(privOpt.getPrivateMask());
    }

    void store() {
        Options privOpt = Options.getInstance();

        privOpt.setAlivePrivate(jcbLivings.isSelected());
        privOpt.setDeadIsPublic(jcbDead.isSelected());
        privOpt.setYearsIndiCanBeAlive(Integer.valueOf(jsLivingYears.getValue().toString()));
        privOpt.setPrivateYears(Integer.valueOf(jsYears.getValue().toString()));
        privOpt.setPrivateTag(jtPrivTag.getText());
        privOpt.setPrivateMask(jtReplace.getText());

        controller.changed();
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JCheckBox jcbDead;
    private javax.swing.JCheckBox jcbLivings;
    private javax.swing.JSpinner jsLivingYears;
    private javax.swing.JSpinner jsYears;
    private javax.swing.JTextField jtPrivTag;
    private javax.swing.JTextField jtReplace;
    private javax.swing.JLabel titleReplace;
    // End of variables declaration//GEN-END:variables
}
