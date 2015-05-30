/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties;

import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import java.util.Locale;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

public final class GedcomPropertiesVisualPanel3 extends JPanel implements Constants {

    private final int mode = GedcomPropertiesWizardIterator.getMode();

    private final GedcomPropertiesWizardPanel3 parent;
    

    public GedcomPropertiesVisualPanel3(GedcomPropertiesWizardPanel3 parent) {
        this.parent = parent;
        initComponents();
        updateDisplay();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "STEP_3_name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox(Gedcom.TRANSLATED_LANGUAGES.values().toArray (new String[Gedcom.TRANSLATED_LANGUAGES.values().size()]));
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox(Gedcom.ENCODINGS);
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();

        setBorder(null);
        setAutoscrolls(true);
        setPreferredSize(new java.awt.Dimension(520, 360));

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(520, 360));

        jPanel1.setBorder(null);
        jPanel1.setPreferredSize(new java.awt.Dimension(520, 360));

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, mode == CREATION ? "Panel3.jLabel1.create" : "Panel3.jLabel1.update"));

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, mode == CREATION ? "Panel3.jLabel2.create" : "Panel3.jLabel2.update"));

        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox2ItemStateChanged(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Cantarell", 2, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jLabel5.text"));
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel5.setAutoscrolls(true);

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, mode == CREATION ? "Panel3.jLabel3.create" : "Panel3.jLabel3.update"));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jRadioButton1.Yes"));
        jRadioButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton1StateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jRadioButton2.No"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, mode == CREATION ? "Panel3.jCheckBox1.create" : "Panel3.jCheckBox1.update"));
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jCheckBox1.toolTipText"));

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, mode == CREATION ? "Panel3.jLabel4.create" : "Panel3.jLabel4.update"));
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jLabel4.toolTipText"));

        buttonGroup2.add(jRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton3, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jRadioButton3.Any"));

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton4, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jRadioButton4.Anstfile"));

        buttonGroup2.add(jRadioButton5);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton5, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "Panel3.jRadioButton5.TempleReady"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jRadioButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jRadioButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton5))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jCheckBox1))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel3.class, "GedcomPropertiesVisualPanel3.jPanel1.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton1StateChanged
        updateDisplay();
    }//GEN-LAST:event_jRadioButton1StateChanged

    private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
        updateDisplay();
    }//GEN-LAST:event_jComboBox2ItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public boolean getConversionToBeDone() {
        return jCheckBox1.isSelected();
    }

    public void setLANG(String str) {
        jComboBox1.setSelectedItem(Gedcom.TRANSLATED_LANGUAGES.get(str));
        if (jComboBox1.getSelectedIndex() == -1) {
            jComboBox1.setSelectedItem(Gedcom.TRANSLATED_LANGUAGES.get(Locale.getDefault().getDisplayLanguage(new Locale("en", "EN"))));
        }
    }

    public void setCHAR(String str) {
        jComboBox2.setSelectedItem(str);
        updateDisplay();
    }

    public void setVERS(String str) {
        if (str.equals(Grammar.GRAMMAR551)) {
            jRadioButton1.setSelected(true);
        } else {
            jRadioButton2.setSelected(true);
        }
        updateDisplay();
    }

    public void setDEST(String str) {
        if (str.equals(Gedcom.DEST_ANSTFILE)) {
            jRadioButton4.setSelected(true);
        } else if (str.equals(Gedcom.DEST_TEMPLEREADY)) {
            jRadioButton5.setSelected(true);
        } else {
            jRadioButton3.setSelected(true);
        }
    }

    public String getLANG() {
        String[] tab = Gedcom.TRANSLATED_LANGUAGES.keySet().toArray(new String [Gedcom.TRANSLATED_LANGUAGES.keySet().size()]);
        return tab[jComboBox1.getSelectedIndex()]; 
    }

    public String getCHAR() {
        return jComboBox2.getSelectedItem().toString();
    }

    public String getVERS() {
        return (jRadioButton1.isSelected() ? Grammar.GRAMMAR551 : Grammar.GRAMMAR55);
    }

    public String getDEST() {
        return (jRadioButton4.isSelected() ? Gedcom.DEST_ANSTFILE : jRadioButton5.isSelected() ? Gedcom.DEST_TEMPLEREADY : Gedcom.DEST_ANY);
    }

    private void updateDisplay() {
        jLabel5.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, jComboBox2.getSelectedItem().toString() + "_label"));
        boolean canBeConverted = (mode == UPDATE) && !getVERS().equals(parent.getOriginalVersion());
        jCheckBox1.setVisible(canBeConverted);
        parent.warnVersionChange(canBeConverted);
    }
    
}
