package ancestris.extensions.reports.flashlist;

import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class FlashListPanel extends javax.swing.JPanel {

    public String legends[] = {
        "FlashListPanel.displayLegendComboBox.legendNone",
        "FlashListPanel.displayLegendComboBox.legendTop",
        "FlashListPanel.displayLegendComboBox.legendBot"
    };
    public String counterIncrements[] = {
        "FlashListPanel.IncrementComboBox.DoNotShow",
        "FlashListPanel.IncrementComboBox.10",
        "FlashListPanel.IncrementComboBox.100",
        "FlashListPanel.IncrementComboBox.1000",
        "FlashListPanel.IncrementComboBox.10000"
    };

    FlashListPanel(FlashListOptionsPanelController controller) {
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

        displayLegendLabel = new javax.swing.JLabel();
        filterKey1Label = new javax.swing.JLabel();
        filterKey2Label = new javax.swing.JLabel();
        filterKey3Label = new javax.swing.JLabel();
        counterIncrementLabel = new javax.swing.JLabel();
        yearSpanLabel = new javax.swing.JLabel();
        nbEventsLabel = new javax.swing.JLabel();
        repeatKeysCheckBox = new javax.swing.JCheckBox();
        repeatHeaderCheckBox = new javax.swing.JCheckBox();
        minSosaLabel = new javax.swing.JLabel();
        addTOCCheckBox = new javax.swing.JCheckBox();
        displayLegendComboBox = new javax.swing.JComboBox(initDisplayLegendComboBox());
        displayZerosCheckBox = new javax.swing.JCheckBox();
        filterKey1TextField = new javax.swing.JTextField();
        filterKey2TextField = new javax.swing.JTextField();
        filterKey3TextField = new javax.swing.JTextField();
        yearSpanFormattedTextField = new javax.swing.JFormattedTextField();
        nbEventsFormattedTextField = new javax.swing.JFormattedTextField();
        minSosaFormattedTextField = new javax.swing.JFormattedTextField();
        counterIncrementComboBox = new javax.swing.JComboBox(initCounterIncrementComboBox());

        org.openide.awt.Mnemonics.setLocalizedText(displayLegendLabel, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.displayLegendLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(filterKey1Label, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.filterKey1Label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(filterKey2Label, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.filterKey2Label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(filterKey3Label, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.filterKey3Label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(counterIncrementLabel, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.counterIncrementLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(yearSpanLabel, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.yearSpanLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nbEventsLabel, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.nbEventsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(repeatKeysCheckBox, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.repeatKeysCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(repeatHeaderCheckBox, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.repeatHeaderCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(minSosaLabel, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.minSosaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addTOCCheckBox, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.addTOCCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(displayZerosCheckBox, org.openide.util.NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.displayZerosCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(repeatHeaderCheckBox)
                    .addComponent(addTOCCheckBox)
                    .addComponent(repeatKeysCheckBox)
                    .addComponent(displayZerosCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(displayLegendLabel)
                            .addComponent(filterKey1Label)
                            .addComponent(filterKey2Label)
                            .addComponent(filterKey3Label)
                            .addComponent(counterIncrementLabel)
                            .addComponent(yearSpanLabel)
                            .addComponent(nbEventsLabel)
                            .addComponent(minSosaLabel))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minSosaFormattedTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(displayLegendComboBox, 0, 114, Short.MAX_VALUE)
                            .addComponent(yearSpanFormattedTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(nbEventsFormattedTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(filterKey3TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(filterKey2TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(filterKey1TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(counterIncrementComboBox, 0, 114, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayLegendLabel)
                    .addComponent(displayLegendComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterKey1Label)
                    .addComponent(filterKey1TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterKey2Label)
                    .addComponent(filterKey2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterKey3Label)
                    .addComponent(filterKey3TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(counterIncrementLabel)
                    .addComponent(counterIncrementComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yearSpanLabel)
                    .addComponent(yearSpanFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nbEventsLabel)
                    .addComponent(nbEventsFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minSosaLabel)
                    .addComponent(minSosaFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(repeatKeysCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(repeatHeaderCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayZerosCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addTOCCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);
        displayLegendComboBox.setSelectedIndex(modulePreferences.getInt("displayLegendComboBox", 0));
        filterKey1TextField.setText(modulePreferences.get("filterKey1TextField", "*"));
        filterKey2TextField.setText(modulePreferences.get("filterKey2TextField", "*"));
        filterKey3TextField.setText(modulePreferences.get("filterKey3TextField", "*"));
        counterIncrementComboBox.setSelectedIndex(modulePreferences.getInt("counterIncrementComboBox", 2));
        yearSpanFormattedTextField.setValue(modulePreferences.get("yearSpanFormattedTextField", "50"));
        nbEventsFormattedTextField.setValue(modulePreferences.get("nbEventsFormattedTextField", "3"));
        minSosaFormattedTextField.setValue(modulePreferences.get("minSosaFormattedTextField", "1"));
        repeatKeysCheckBox.setSelected(modulePreferences.getBoolean("repeatKeysCheckBox", false));
        repeatHeaderCheckBox.setSelected(modulePreferences.getBoolean("repeatHeaderCheckBox", true));
        displayZerosCheckBox.setSelected(modulePreferences.getBoolean("displayZerosCheckBox", false));
        addTOCCheckBox.setSelected(modulePreferences.getBoolean("addTOCCheckBox", false));
    }

    void store() {
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);

        modulePreferences.putInt("displayLegendComboBox", displayLegendComboBox.getSelectedIndex());
        modulePreferences.put("filterKey1TextField", filterKey1TextField.getText());
        modulePreferences.put("filterKey2TextField", filterKey2TextField.getText());
        modulePreferences.put("filterKey3TextField", filterKey3TextField.getText());
        modulePreferences.putInt("counterIncrementComboBox", counterIncrementComboBox.getSelectedIndex());
        modulePreferences.put("yearSpanFormattedTextField", yearSpanFormattedTextField.getText());
        modulePreferences.put("nbEventsFormattedTextField", nbEventsFormattedTextField.getText());
        modulePreferences.put("minSosaFormattedTextField", minSosaFormattedTextField.getText());
        modulePreferences.putBoolean("repeatKeysCheckBox", repeatKeysCheckBox.isSelected());
        modulePreferences.putBoolean("repeatHeaderCheckBox", repeatHeaderCheckBox.isSelected());
        modulePreferences.putBoolean("displayZerosCheckBox", displayZerosCheckBox.isSelected());
        modulePreferences.putBoolean("addTOCCheckBox", addTOCCheckBox.isSelected());
    }

    boolean valid() {
        return true;
    }

    private String[] initDisplayLegendComboBox() {
        ArrayList<String> comboBoxText = new ArrayList<String>(legends.length);
        for (String value : legends) {
            comboBoxText.add(NbBundle.getMessage(ReportFlashList.class, value));
        }
        return comboBoxText.toArray(new String[0]);
    }

    private String[] initCounterIncrementComboBox() {
        ArrayList<String> comboBoxText = new ArrayList<String>(counterIncrements.length);
        for (String value : counterIncrements) {
            comboBoxText.add(NbBundle.getMessage(ReportFlashList.class, value));
        }
        return comboBoxText.toArray(new String[0]);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addTOCCheckBox;
    private javax.swing.JComboBox counterIncrementComboBox;
    private javax.swing.JLabel counterIncrementLabel;
    private javax.swing.JComboBox displayLegendComboBox;
    private javax.swing.JLabel displayLegendLabel;
    private javax.swing.JCheckBox displayZerosCheckBox;
    private javax.swing.JLabel filterKey1Label;
    private javax.swing.JTextField filterKey1TextField;
    private javax.swing.JLabel filterKey2Label;
    private javax.swing.JTextField filterKey2TextField;
    private javax.swing.JLabel filterKey3Label;
    private javax.swing.JTextField filterKey3TextField;
    private javax.swing.JFormattedTextField minSosaFormattedTextField;
    private javax.swing.JLabel minSosaLabel;
    private javax.swing.JFormattedTextField nbEventsFormattedTextField;
    private javax.swing.JLabel nbEventsLabel;
    private javax.swing.JCheckBox repeatHeaderCheckBox;
    private javax.swing.JCheckBox repeatKeysCheckBox;
    private javax.swing.JFormattedTextField yearSpanFormattedTextField;
    private javax.swing.JLabel yearSpanLabel;
    // End of variables declaration//GEN-END:variables
}
