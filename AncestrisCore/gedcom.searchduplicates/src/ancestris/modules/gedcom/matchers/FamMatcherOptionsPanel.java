package ancestris.modules.gedcom.matchers;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

public final class FamMatcherOptionsPanel extends JPanel {

    private FamMatcherOptions famMatcherOptions = new FamMatcherOptions();

    /**
     * Creates new form SearchDuplicatesVisualPanel2
     */
    public FamMatcherOptionsPanel() {
        initComponents();
        famMaxDateIntervalSpinner.setModel(new SpinnerNumberModel(famMatcherOptions.getDateinterval(),0,3650,1));
        famEmptyValuesInvalidRadioButton.setSelected(famMatcherOptions.isEmptyValueInvalid());
        saveOptions();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        famMaxDateIntervalLabel = new javax.swing.JLabel();
        famMaxDateIntervalSpinner = new javax.swing.JSpinner();
        famEmptyValuesInvalidRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(famMaxDateIntervalLabel, org.openide.util.NbBundle.getMessage(FamMatcherOptionsPanel.class, "FamMatcherOptionsPanel.famMaxDateIntervalLabel.text")); // NOI18N

        famMaxDateIntervalSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                famMaxDateIntervalSpinnerStateChanged(evt);
            }
        });

        famEmptyValuesInvalidRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(famEmptyValuesInvalidRadioButton, org.openide.util.NbBundle.getMessage(FamMatcherOptionsPanel.class, "FamMatcherOptionsPanel.famEmptyValuesInvalidRadioButton.text")); // NOI18N
        famEmptyValuesInvalidRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famEmptyValuesInvalidRadioButtonActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(4);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(FamMatcherOptionsPanel.class, "FamMatcherOptionsPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(famEmptyValuesInvalidRadioButton)
                    .addComponent(famMaxDateIntervalLabel)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(famMaxDateIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(famMaxDateIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(famMaxDateIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(famEmptyValuesInvalidRadioButton)
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void famMaxDateIntervalSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_famMaxDateIntervalSpinnerStateChanged
        saveOptions();
    }//GEN-LAST:event_famMaxDateIntervalSpinnerStateChanged

    private void famEmptyValuesInvalidRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famEmptyValuesInvalidRadioButtonActionPerformed
        saveOptions();
    }//GEN-LAST:event_famEmptyValuesInvalidRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton famEmptyValuesInvalidRadioButton;
    private javax.swing.JLabel famMaxDateIntervalLabel;
    private javax.swing.JSpinner famMaxDateIntervalSpinner;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the selected family check options
     */
    public void saveOptions() {
        famMatcherOptions.setDateinterval((Integer) famMaxDateIntervalSpinner.getValue());
        famMatcherOptions.setEmptyValueInvalid(famEmptyValuesInvalidRadioButton.isSelected());
    }
    
    public FamMatcherOptions getSelectedOptions() {
        return famMatcherOptions;
    }
    
}
