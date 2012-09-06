/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.core.TextOptions;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import javax.swing.SpinnerNumberModel;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

@SuppressWarnings(value={"unchecked", "rawtypes"})
final class OptionFormatPanel extends javax.swing.JPanel {

    private final OptionFormatOptionsPanelController controller;
    // Values
    String[] indis = new String[]{
        NbBundle.getMessage(OptionFormatPanel.class, "name.format1"),
        NbBundle.getMessage(OptionFormatPanel.class, "name.format2")};

    String[] dates = new String[]{
        NbBundle.getMessage(OptionFormatPanel.class, "date.format.gedcom"),
        NbBundle.getMessage(OptionFormatPanel.class, "date.format.short"),
        NbBundle.getMessage(OptionFormatPanel.class, "date.format.long"),
        NbBundle.getMessage(OptionFormatPanel.class, "date.format.num")};

    OptionFormatPanel(OptionFormatOptionsPanelController controller) {
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

        jTextField3 = new javax.swing.JTextField();
        jSpinner3 = new javax.swing.JSpinner(new SpinnerNumberModel(246, 20, 246, 1));
        jLabel23 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner(new SpinnerNumberModel(128, 128, 16384, 128));
        jLabel11 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox(dates);
        jComboBox4 = new javax.swing.JComboBox(indis);
        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(691, 503));

        jTextField3.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField3.text")); // NOI18N
        jTextField3.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField3.toolTipText")); // NOI18N

        jSpinner3.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jSpinner3.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel23, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel23.text")); // NOI18N

        jSpinner1.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jSpinner1.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel11.text")); // NOI18N

        jTextField5.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField5.text")); // NOI18N
        jTextField5.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField5.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel10.text")); // NOI18N

        jTextField4.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField4.text")); // NOI18N
        jTextField4.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField4.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel14.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel16.text")); // NOI18N

        jTextField9.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField9.text")); // NOI18N
        jTextField9.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField9.toolTipText")); // NOI18N

        jTextField8.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField8.text")); // NOI18N
        jTextField8.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField8.toolTipText")); // NOI18N

        jTextField6.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField6.text")); // NOI18N
        jTextField6.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField6.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel17.text")); // NOI18N

        jTextField10.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField10.text")); // NOI18N
        jTextField10.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField10.toolTipText")); // NOI18N

        jTextField11.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField11.text")); // NOI18N
        jTextField11.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField11.toolTipText")); // NOI18N

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel1.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel4.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel3.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel5.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel7.text")); // NOI18N

        jComboBox3.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jComboBox3.toolTipText")); // NOI18N

        jComboBox4.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jComboBox4.toolTipText")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField1.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jLabel9.text")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField2.text")); // NOI18N
        jTextField2.setToolTipText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionFormatPanel.jTextField2.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel16))
                                .addGap(29, 29, 29)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(47, 47, 47)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel12)
                            .addComponent(jLabel9)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(160, 160, 160)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(160, 160, 160)
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jSpinner1)
                                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(160, 160, 160)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel7)
                            .addComponent(jLabel5))))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel8)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel12)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel13)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel11)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel23)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(161, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        AncestrisPreferences gedcomPrefs = Registry.get(genj.gedcom.GedcomOptions.class);
        TextOptions textPrefs = TextOptions.getInstance();

        setSymbolBirt(textPrefs.getBirthSymbol());
        setSymbolBapm(textPrefs.getBaptismSymbol());
        setSymbolChildOf(textPrefs.getChildOfSymbol());
        setSymbolEngm(textPrefs.getEngagingSymbol());
        setSymbolMarr(textPrefs.getMarriageSymbol());
        setSymbolDivc(textPrefs.getDivorceSymbol());
        setSymbolOccu(textPrefs.getOccuSymbol());
        setSymbolResi(textPrefs.getResiSymbol());
        setSymbolDeat(textPrefs.getDeathSymbol());
        setSymbolBuri(textPrefs.getBurialSymbol());
        jSpinner3.setValue(genj.gedcom.GedcomOptions.getInstance().getValueLineBreak());
        setImageSize(gedcomPrefs.get("maxImageFileSizeKB", ""));
        setDisplayNames(gedcomPrefs.get("nameFormat", ""));
        setDisplayDates(gedcomPrefs.get("dateFormat", ""));
    }

    void store() {
        AncestrisPreferences gedcomPrefs = Registry.get(genj.gedcom.GedcomOptions.class);
        TextOptions reportPrefs = TextOptions.getInstance();

        reportPrefs.setBirthSymbol(getSymbolBirt());
        reportPrefs.setBaptismSymbol(getSymbolBapm());
        reportPrefs.setChildOfSymbol(getSymbolChildOf());
        reportPrefs.setEngagingSymbol(getSymbolEngm());
        reportPrefs.setMarriageSymbol(getSymbolMarr());
        reportPrefs.setDivorceSymbol(getSymbolDivc());
        reportPrefs.setOccuSymbol(getSymbolOccu());
        reportPrefs.setResiSymbol(getSymbolResi());
        reportPrefs.setDeathSymbol(getSymbolDeat());
        reportPrefs.setBurialSymbol(getSymbolBuri());
        genj.gedcom.GedcomOptions.getInstance().setValueLineBreak(Integer.valueOf(jSpinner3.getValue().toString()));
        gedcomPrefs.put("maxImageFileSizeKB", getImageSize());
        gedcomPrefs.put("nameFormat", getDisplayNames());
        gedcomPrefs.put("dateFormat", getDisplayDates());

        StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(OptionFormatPanel.class, "OptionPanel.saved.statustext"));
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables

    void setSymbolBirt(String str) {
        if (str.equals("")) {
            str = "o";
        }
        jTextField1.setText(str);
    }

    String getSymbolBirt() {
        return jTextField1.getText();
    }

    void setSymbolBapm(String str) {
        if (str.equals("")) {
            str = "b.";
        }
        jTextField2.setText(str);
    }

    String getSymbolBapm() {
        return jTextField2.getText();
    }

    void setSymbolChildOf(String str) {
        if (str.equals("")) {
            str = "fs.";
        }
        jTextField3.setText(str);
    }

    String getSymbolChildOf() {
        return jTextField3.getText();
    }

    void setSymbolEngm(String str) {
        if (str.equals("")) {
            str = "(x)";
        }
        jTextField4.setText(str);
    }

    String getSymbolEngm() {
        return jTextField4.getText();
    }

    void setSymbolMarr(String str) {
        if (str.equals("")) {
            str = "x";
        }
        jTextField5.setText(str);
    }

    String getSymbolMarr() {
        return jTextField5.getText();
    }

    void setSymbolDivc(String str) {
        if (str.equals("")) {
            str = ")(";
        }
        jTextField6.setText(str);
    }

    String getSymbolDivc() {
        return jTextField6.getText();
    }

    void setSymbolOccu(String str) {
        if (str.equals("")) {
            str = "=";
        }
        jTextField9.setText(str);
    }

    String getSymbolOccu() {
        return jTextField9.getText();
    }

    void setSymbolResi(String str) {
        if (str.equals("")) {
            str = "^";
        }
        jTextField8.setText(str);
    }

    String getSymbolResi() {
        return jTextField8.getText();
    }

    void setSymbolDeat(String str) {
        if (str.equals("")) {
            str = "+";
        }
        jTextField11.setText(str);
    }

    String getSymbolDeat() {
        return jTextField11.getText();
    }

    void setSymbolBuri(String str) {
        if (str.equals("")) {
            str = "(+)";
        }
        jTextField10.setText(str);
    }

    String getSymbolBuri() {
        return jTextField10.getText();
    }

    void setImageSize(String str) {
        if (str.equals("-1")) {
            str = "128";
        }
        Integer i = getIntFromStr(str);
        if (i == -1) {
            i = 128;
        }
        if (i > 16384) {
            i = 16384;
        }
        jSpinner1.setValue(i);
    }

    String getImageSize() {
        return jSpinner1.getValue().toString();
    }

    void setDisplayNames(String str) {
        if ((str.length() == 0) || str.equals("-1")) {
            str = "1";
        }
        Integer i = getIntFromStr(str);
        if (i == -1) {
            i = 1;
        }
        if (i > 1) {
            i = 1;
        }
        jComboBox4.setSelectedIndex(i);
    }

    String getDisplayNames() {
        return jComboBox4.getSelectedIndex() + "";
    }

    void setDisplayDates(String str) {
        if ((str.length() == 0) || str.equals("-1")) {
            str = "1";
        }
        Integer i = getIntFromStr(str);
        if (i == -1) {
            i = 1;
        }
        if (i > 3) {
            i = 3;
        }
        jComboBox3.setSelectedIndex(i);
    }

    String getDisplayDates() {
        return jComboBox3.getSelectedIndex() + "";
    }

    private Integer getIntFromStr(String str) {

        Integer i = 0;
        try {
            i = Integer.valueOf(str);
        } catch (Exception e) {
            i = -1;
        }
        return i;
    }
}
