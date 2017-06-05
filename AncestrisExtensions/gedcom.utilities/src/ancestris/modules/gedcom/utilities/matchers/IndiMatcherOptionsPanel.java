package ancestris.modules.gedcom.utilities.matchers;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

public final class IndiMatcherOptionsPanel extends JPanel {

    private IndiMatcherOptions indiMatcherOptions = new IndiMatcherOptions();

    /**
     * Creates new form SearchDuplicatesVisualPanel2
     */
    public IndiMatcherOptionsPanel() {
        initComponents();
        indiMaxDateIntervalSpinner.setModel(new SpinnerNumberModel(indiMatcherOptions.getDateinterval(),0,3650,1));
        indiEmptyValuesValidRadioButton.setSelected(indiMatcherOptions.isEmptyValueValid());
        indiCheckAllNamesRadioButton.setSelected(indiMatcherOptions.isCheckAllNames());
        indiAllFirstNamesRadioButton.setSelected(indiMatcherOptions.isAllFirstNamesEquals());
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        emptyPanel = new javax.swing.JPanel();
        sourcesOptionPanel1 = new javax.swing.JPanel();
        submittersOptionPanel = new javax.swing.JPanel();
        repositoriesOptionPanel = new javax.swing.JPanel();
        indiMaximumDateIntervalLabel = new javax.swing.JLabel();
        indiMaxDateIntervalSpinner = new javax.swing.JSpinner();
        indiEmptyValuesValidRadioButton = new javax.swing.JRadioButton();
        indiAllFirstNamesRadioButton = new javax.swing.JRadioButton();
        indiCheckAllNamesRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );
        emptyPanelLayout.setVerticalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 166, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout sourcesOptionPanel1Layout = new javax.swing.GroupLayout(sourcesOptionPanel1);
        sourcesOptionPanel1.setLayout(sourcesOptionPanel1Layout);
        sourcesOptionPanel1Layout.setHorizontalGroup(
            sourcesOptionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );
        sourcesOptionPanel1Layout.setVerticalGroup(
            sourcesOptionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 166, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout submittersOptionPanelLayout = new javax.swing.GroupLayout(submittersOptionPanel);
        submittersOptionPanel.setLayout(submittersOptionPanelLayout);
        submittersOptionPanelLayout.setHorizontalGroup(
            submittersOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );
        submittersOptionPanelLayout.setVerticalGroup(
            submittersOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 166, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout repositoriesOptionPanelLayout = new javax.swing.GroupLayout(repositoriesOptionPanel);
        repositoriesOptionPanel.setLayout(repositoriesOptionPanelLayout);
        repositoriesOptionPanelLayout.setHorizontalGroup(
            repositoriesOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );
        repositoriesOptionPanelLayout.setVerticalGroup(
            repositoriesOptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 166, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(indiMaximumDateIntervalLabel, org.openide.util.NbBundle.getMessage(IndiMatcherOptionsPanel.class, "IndiMatcherOptionsPanel.indiMaximumDateIntervalLabel.text")); // NOI18N

        indiMaxDateIntervalSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                indiMaxDateIntervalSpinnerStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(indiEmptyValuesValidRadioButton, org.openide.util.NbBundle.getMessage(IndiMatcherOptionsPanel.class, "IndiMatcherOptionsPanel.indiEmptyValuesValidRadioButton.text")); // NOI18N
        indiEmptyValuesValidRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiEmptyValuesValidRadioButtonActionPerformed(evt);
            }
        });

        indiAllFirstNamesRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(indiAllFirstNamesRadioButton, org.openide.util.NbBundle.getMessage(IndiMatcherOptionsPanel.class, "IndiMatcherOptionsPanel.indiAllFirstNamesRadioButton.text")); // NOI18N
        indiAllFirstNamesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiAllFirstNamesRadioButtonActionPerformed(evt);
            }
        });

        indiCheckAllNamesRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(indiCheckAllNamesRadioButton, org.openide.util.NbBundle.getMessage(IndiMatcherOptionsPanel.class, "IndiMatcherOptionsPanel.indiCheckAllNamesRadioButton.text")); // NOI18N
        indiCheckAllNamesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiCheckAllNamesRadioButtonActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(4);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(IndiMatcherOptionsPanel.class, "IndiMatcherOptionsPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(indiEmptyValuesValidRadioButton)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(indiMaximumDateIntervalLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(indiMaxDateIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(indiAllFirstNamesRadioButton)
                    .addComponent(indiCheckAllNamesRadioButton)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(indiMaxDateIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indiMaximumDateIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indiEmptyValuesValidRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indiCheckAllNamesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indiAllFirstNamesRadioButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void indiMaxDateIntervalSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_indiMaxDateIntervalSpinnerStateChanged
        saveOptions();
    }//GEN-LAST:event_indiMaxDateIntervalSpinnerStateChanged

    private void indiEmptyValuesValidRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiEmptyValuesValidRadioButtonActionPerformed
        saveOptions();
    }//GEN-LAST:event_indiEmptyValuesValidRadioButtonActionPerformed

    private void indiCheckAllNamesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiCheckAllNamesRadioButtonActionPerformed
        saveOptions();
    }//GEN-LAST:event_indiCheckAllNamesRadioButtonActionPerformed

    private void indiAllFirstNamesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiAllFirstNamesRadioButtonActionPerformed
        saveOptions();
    }//GEN-LAST:event_indiAllFirstNamesRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JRadioButton indiAllFirstNamesRadioButton;
    private javax.swing.JRadioButton indiCheckAllNamesRadioButton;
    private javax.swing.JRadioButton indiEmptyValuesValidRadioButton;
    private javax.swing.JSpinner indiMaxDateIntervalSpinner;
    private javax.swing.JLabel indiMaximumDateIntervalLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel repositoriesOptionPanel;
    private javax.swing.JPanel sourcesOptionPanel1;
    private javax.swing.JPanel submittersOptionPanel;
    // End of variables declaration//GEN-END:variables

    public void saveOptions() {
        indiMatcherOptions.setDateinterval((Integer) indiMaxDateIntervalSpinner.getValue());
        indiMatcherOptions.setCheckAllNames(indiCheckAllNamesRadioButton.isSelected());
        indiMatcherOptions.setAllFirstNames(indiAllFirstNamesRadioButton.isSelected());
        indiMatcherOptions.setEmptyValueValid(indiEmptyValuesValidRadioButton.isSelected());
    }

    public IndiMatcherOptions getSelectedOptions() {
        return indiMatcherOptions;
    }
    
}
