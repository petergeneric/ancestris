/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import ancestris.gedcom.privacy.PrivacyPolicy;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

public final class WebBookVisualPanel2 extends JPanel {

    private Entity[] indis = null;
    private Gedcom gedcom = null;

    /** Creates new form WebBookVisualPanel2 */
    public WebBookVisualPanel2(Gedcom gedcom) {
        this.gedcom = gedcom;
        indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        initComponents();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WebBookWizardAction.class, "CTL_Step_2");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox(indis);
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBox8 = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jLabel1.text")); // NOI18N

        jComboBox1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_decujus")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jLabel2.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_Unknown")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayFamily")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox2.text")); // NOI18N
        jCheckBox2.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayKids")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox3, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox3.text")); // NOI18N
        jCheckBox3.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplaySiblings")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox4, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox4.text")); // NOI18N
        jCheckBox4.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayRel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox5, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox5.text")); // NOI18N
        jCheckBox5.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayNotes")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox6, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox6.text")); // NOI18N
        jCheckBox6.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayId")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox7, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox7.text")); // NOI18N
        jCheckBox7.setToolTipText(org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "TTT_DisplayEmail")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox8, org.openide.util.NbBundle.getMessage(WebBookVisualPanel2.class, "WebBookVisualPanel2.jCheckBox8.text")); // NOI18N
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox6)
                            .addComponent(jCheckBox5)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox3)
                            .addComponent(jCheckBox2)
                            .addComponent(jCheckBox1)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox8)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox7)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox8)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        PrivacyPolicy.getDefault().openPreferences();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        jButton1.setEnabled(jCheckBox8.isSelected());
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    /**
     * Get root indi using SOSA if available
     * @param gedcom
     * @return
     */
    private Indi getRootIndi(Gedcom gedcom) {
        if (gedcom == null) {
            return null;
        }
        // Get all individuals and stop when sosa 1 is found
        Collection<Indi> entities = (Collection<Indi>) gedcom.getEntities(Gedcom.INDI);
        Property[] props = null;
        String sosaStr = "";
        for (Iterator<Indi> it = entities.iterator(); it.hasNext();) {
            Indi indi = it.next();
            props = indi.getProperties(Indi.TAG_SOSA);
            if (props == null || props.length == 0) {
                props = indi.getProperties(Indi.TAG_SOSADABOVILLE);
                if (props == null) {
                    continue;
                }
            }
            for (int i = 0; i < props.length; i++) {
                Property prop = props[i];
                sosaStr = prop.getDisplayValue();
                if (getNb(sosaStr) == 1) {
                    return indi;
                }
            }
        }

        // If we are here, no sosa was found, take first element
        return entities.iterator().next();
    }

    /**
     * Get nb from string removing left and right letters
     */
    private int getNb(String str) {

        if (str == null || str.isEmpty()) {
            return 0;
        }
        int sosaNb = 0;

        int start = 0, end = str.length()-1;
        while (start <= end && !Character.isDigit(str.charAt(start))) {
            start++;
        }
        end = start;
        while ((end <= str.length() - 1) && Character.isDigit(str.charAt(end))) {
            end++;
        }
        if (end == start) {
            return 0;
        } else {
            try {
                sosaNb = Integer.parseInt(str.substring(start, end));
            } catch (Exception e) {
                sosaNb = 0;
            }
        }
        return sosaNb;
    }



    
    public String getPref01() {
        return jComboBox1.getSelectedItem().toString();
    }

    public void setPref01(String str) {
        if (str.isEmpty()) {
            str = getRootIndi(gedcom).toString();
        }
        if (indis == null) {
            return;
        }
        for (int i = 0; i < indis.length; i++) {
            Indi indi = (Indi)indis[i];
            if (indi.toString().equals(str)) {
                jComboBox1.setSelectedIndex(i);
            }
        }
    }

    public String getPref02() {
        return jTextField1.getText();
    }

    public void setPref02(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultUnknown");
        }
        jTextField1.setText(str);
    }

    public String getPref03() {
        return jCheckBox1.isSelected() ? "1" : "0";
    }

    public void setPref03(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispSpouse");
        }
        jCheckBox1.setSelected(str.equals("1"));
    }

    public String getPref04() {
        return jCheckBox2.isSelected() ? "1" : "0";
    }

    public void setPref04(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispKids");
        }
        jCheckBox2.setSelected(str.equals("1"));
    }

    public String getPref05() {
        return jCheckBox3.isSelected() ? "1" : "0";
    }

    public void setPref05(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispSiblings");
        }
        jCheckBox3.setSelected(str.equals("1"));
    }

    public String getPref06() {
        return jCheckBox4.isSelected() ? "1" : "0";
    }

    public void setPref06(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispRelations");
        }
        jCheckBox4.setSelected(str.equals("1"));
    }

    public String getPref07() {
        return jCheckBox5.isSelected() ? "1" : "0";
    }

    public void setPref07(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispNotes");
        }
        jCheckBox5.setSelected(str.equals("1"));
    }

    public String getPref08() {
        return jCheckBox6.isSelected() ? "1" : "0";
    }

    public void setPref08(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispId");
        }
        jCheckBox6.setSelected(str.equals("1"));
    }

    public String getPref09() {
        return jCheckBox7.isSelected() ? "1" : "0";
    }

    public void setPref09(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultDispEmailButton");
        }
        jCheckBox7.setSelected(str.equals("1"));
    }

    public String getPref10() {
        return jCheckBox8.isSelected() ? "1" : "0";
    }

    public void setPref10(String str) {
        if (str.isEmpty()) {
            str = NbBundle.getMessage(WebBookWizardAction.class, "PREF_defaultHidePrivateData");
        }
        jCheckBox8.setSelected(str.equals("1"));
        jButton1.setEnabled(jCheckBox8.isSelected());
    }
}

