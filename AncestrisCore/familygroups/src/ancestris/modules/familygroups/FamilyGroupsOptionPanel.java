package ancestris.modules.familygroups;

import org.openide.util.NbPreferences;

final class FamilyGroupsOptionPanel extends javax.swing.JPanel {

    private final FamilyGroupsOptionsPanelController controller;
    
    private int minMin = 1;
    private int maxMin = 100;
    private int minMax = 5;
    private int maxMax = 500;

    FamilyGroupsOptionPanel(FamilyGroupsOptionsPanelController controller) {
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

        minGroupSizeLabel = new javax.swing.JLabel();
        maxGroupSizeLabel = new javax.swing.JLabel();
        minGroupSizeSpinner = new javax.swing.JSpinner();
        maxGroupSizeSpinner = new javax.swing.JSpinner();

        org.openide.awt.Mnemonics.setLocalizedText(minGroupSizeLabel, org.openide.util.NbBundle.getMessage(FamilyGroupsOptionPanel.class, "FamilyGroupsOptionPanel.minGroupSizeLabel.text") + " ("+minMin+"-"+maxMin+")");

        org.openide.awt.Mnemonics.setLocalizedText(maxGroupSizeLabel, org.openide.util.NbBundle.getMessage(FamilyGroupsOptionPanel.class, "FamilyGroupsOptionPanel.maxGroupSizeLabel.text") + " ("+minMax+"-"+maxMax+")");

        minGroupSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, minMin, maxMin, 1));
        minGroupSizeSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyGroupsOptionPanel.class, "FamilyGroupsOptionPanel.minGroupSizeFormattedTextField.toolTipText")); // NOI18N

        maxGroupSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(20, minMax, maxMax, 5));
        maxGroupSizeSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyGroupsOptionPanel.class, "FamilyGroupsOptionPanel.maxGroupSizeFormattedTextField.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxGroupSizeLabel)
                    .addComponent(minGroupSizeLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minGroupSizeLabel)
                    .addComponent(minGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxGroupSizeLabel)
                    .addComponent(maxGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private int getMin(boolean pref) {
        int min;
        try {
            if (pref) {
               min = Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("minGroupSize", "2"));
            } else {
               min = (int) minGroupSizeSpinner.getValue(); 
            }
            if (min > 100) {
                min = 100;
            }
            if (min < 1) {
                min = 1;
            }
        } catch (Exception e) {
            min = 2;
        }
        return min;
    }
    
    private int getMax(boolean pref) {
        int max;
        try {
            if (pref) {
               max = Integer.valueOf(NbPreferences.forModule(OpenFamilyGroupsAction.class).get("maxGroupSize", "20"));
            } else {
               max = (int) maxGroupSizeSpinner.getValue();
            }
            if (max > 500) {
                max = 500;
            }
            if (max < 5) {
                max = 5;
            }
        } catch (Exception e) {
            max = 20;
        }
        return max;
    }
    
    public void load() {
        int min = getMin(true);
        int max = getMax(true);
        if (max < min) {
            maxGroupSizeSpinner.setValue((int) minGroupSizeSpinner.getValue());
        }
        minGroupSizeSpinner.setValue(min);
        maxGroupSizeSpinner.setValue(max);
    }

    public void store() {
        int min = getMin(false);
        int max = getMax(false);
        if (max < min) {
            maxGroupSizeSpinner.setValue(min);
        }
        NbPreferences.forModule(FamilyGroupsOptionPanel.class).put("minGroupSize", String.valueOf(min));
        NbPreferences.forModule(FamilyGroupsOptionPanel.class).put("maxGroupSize", String.valueOf(max));
    }
    
    public boolean valid() {
        return ((int) maxGroupSizeSpinner.getValue()) >= ((int) minGroupSizeSpinner.getValue());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel maxGroupSizeLabel;
    private javax.swing.JSpinner maxGroupSizeSpinner;
    private javax.swing.JLabel minGroupSizeLabel;
    private javax.swing.JSpinner minGroupSizeSpinner;
    // End of variables declaration//GEN-END:variables
}
