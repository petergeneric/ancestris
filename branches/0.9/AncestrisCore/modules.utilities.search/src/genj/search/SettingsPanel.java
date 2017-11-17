/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.search;

import genj.util.Registry;

/**
 *
 * @author frederic
 */
public class SettingsPanel extends javax.swing.JPanel {

    private final static int DEFAULT_MAX_HITS = 5000;
    private Registry registry;
    private int max_hits;
    private boolean case_sensitive = false;
    
    /**
     * Creates new form SettingsPanel
     */
    public SettingsPanel(Registry registry) {
        this.registry = registry;
        this.max_hits = getMaxHits();
        this.case_sensitive = getCaseSensitive();
        initComponents();
        maxHitsValue.setText(""+max_hits);
        cbCaseSensitive.setSelected(case_sensitive);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        maxHitsLabel = new javax.swing.JLabel();
        maxHitsValue = new javax.swing.JTextField();
        cbCaseSensitive = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(maxHitsLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.maxHitsLabel.text")); // NOI18N

        maxHitsValue.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxHitsValue.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.maxHitsValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCaseSensitive, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbCaseSensitive.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(maxHitsLabel)
                        .addGap(18, 18, 18)
                        .addComponent(maxHitsValue, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbCaseSensitive)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxHitsLabel)
                    .addComponent(maxHitsValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCaseSensitive)
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    public int getMaxHits() {
        return registry.get("searchMaxHits", DEFAULT_MAX_HITS);
    }

    public boolean getCaseSensitive() {
        return registry.get("searchCaseSensitive", false);
    }
    public void setSettings() {
        max_hits = Integer.valueOf(maxHitsValue.getText());
        registry.put("searchMaxHits", max_hits);
        case_sensitive = cbCaseSensitive.isSelected();
        registry.put("searchCaseSensitive", case_sensitive);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCaseSensitive;
    private javax.swing.JLabel maxHitsLabel;
    private javax.swing.JTextField maxHitsValue;
    // End of variables declaration//GEN-END:variables
}