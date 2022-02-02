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
package ancestris.modules.gedcom.sosanumbers;

import genj.common.SelectEntityWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Registry;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SosaPanel extends javax.swing.JPanel implements Constants {

    private int mode = 0;
    private Gedcom gedcom = null;
    private Registry registry = null;
    private Indi selectedIndividual = null;
    private Indi decujusIndividual = null;
    private SelectEntityWidget selectEntityWidget = null;


    /**
     * Creates new form SosaPanel
     */
    public SosaPanel(Context context) {

        // get stored indi if exists
        this.gedcom = context.getGedcom();
        registry = gedcom.getRegistry();
        String decujusID = registry.get(DECUJUSID, "");

        // Identify seleted individual context
        selectedIndividual = null;
        Entity entity = context.getEntity();
        if (entity instanceof Indi) {
            selectedIndividual = (Indi) entity;
        } else if (entity instanceof Fam) {
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            Indi wife = fam.getWife();
            if (husb != null) {
                selectedIndividual = husb;
            } else if (wife != null) {
                selectedIndividual = wife;
            }
        } else if (!decujusID.isEmpty()) {
            Entity ent = gedcom.getEntity(decujusID);
            if (ent != null && ent instanceof Indi) {
                selectedIndividual = (Indi) ent;
            }
        }
        if (selectedIndividual == null) {
            selectedIndividual = (Indi) gedcom.getFirstEntity(Gedcom.INDI);
        }
        
        // Init components including the entity selector
        initComponents();
        selectEntityWidget = new SelectEntityWidget(gedcom, Gedcom.INDI, null, false);
        selectIndiPanel.add(selectEntityWidget);
        
        // Get mode and select action based on mode
        mode = registry.get(ACTION, MODE_GENERATE);
        generateRadioButton.setSelected(mode == MODE_GENERATE);
        eraseRadioButton.setSelected(mode == MODE_ERASE);
        
        // Init buttons selection to memorised values
        int n = registry.get(NUMBERING, NUMBERING_SOSADABOVILLE);
        sosadabovilleRadioButton.setSelected(n == NUMBERING_SOSADABOVILLE);
        sosaRadioButton.setSelected(n == NUMBERING_SOSA);
        dabovilleRadioButton.setSelected(n == NUMBERING_DABOVILLE);
        allNumberingRadioButton.setSelected(n == NUMBERING_ALL);
        allSosaCheckBox.setSelected(registry.get(ALLSOSA, false));
        numberSpouseCheckBox.setSelected(registry.get(NUMBERING_SPOUSE, false));
        int s = registry.get(SELECTION, 1);
        saveCheckBox.setSelected(registry.get(SAVE, true));
        
        // Update selected button labels based on context
        selectedIndividualRadioButton.setVisible(selectedIndividual != null);
        selectedIndividualRadioButton.setText(NbBundle.getMessage(SosaPanel.class, "SosaPanel.selectedIndividualRadioButton.text", (selectedIndividual != null) ? selectedIndividual.toString(true) : ""));
        if (!decujusID.isEmpty()) {
            decujusIndividual = (Indi) gedcom.getEntity(decujusID);
            if (decujusIndividual != null) {
                currentDecujusRadioButton.setText(NbBundle.getMessage(SosaPanel.class, "SosaPanel.currentDecujusRadioButton.text", decujusIndividual.toString(true)));
                currentDecujusRadioButton.setEnabled(true);
                selectEntityWidget.setSelection(decujusIndividual);
            }
            if (s == 1 && selectedIndividual == null) {
                s = 2;
            }
        } else {
            currentDecujusRadioButton.setText(NbBundle.getMessage(SosaPanel.class, "SosaPanel.currentDecujusRadioButton.text", ""));
            currentDecujusRadioButton.setVisible(false);
            if (selectedIndividual != null) {
                selectEntityWidget.setSelection(selectedIndividual); 
                s = 1;
            } else {
                s = 3;
            }
        }
        
        // Preselect choice
        selectedIndividualRadioButton.setSelected(s == 1);
        currentDecujusRadioButton.setSelected(s == 2);
        otherIndividualRadioButton.setSelected(s == 3);
        allIndividualRadioButton.setSelected(s == 4);

        // Update display of panel based on mode
        setDisplay();
        
        
    }
    
    private void setDisplay() {
        numberingLabel.setText(NbBundle.getMessage(SosaPanel.class, mode == MODE_GENERATE ? "SosaPanel.numberingLabel.text" : "SosaPanel.numberingLabel.text2"));
        individualLabel.setText(NbBundle.getMessage(SosaPanel.class, mode == MODE_GENERATE ? "SosaPanel.individualLabel.text" : "SosaPanel.individualLabel.text2"));
        saveCheckBox.setText(NbBundle.getMessage(SosaPanel.class, mode == MODE_GENERATE ? "SosaPanel.saveCheckBox.text" : "SosaPanel.saveCheckBox.text2"));
        allNumberingRadioButton.setVisible(mode == MODE_ERASE);
        allSosaCheckBox.setVisible(mode == MODE_GENERATE);
        numberSpouseCheckBox.setVisible(mode == MODE_GENERATE);
        selectIndiPanel.setVisible(otherIndividualRadioButton.isSelected());
        allIndividualRadioButton.setVisible(mode == MODE_ERASE);
        if (mode == MODE_GENERATE && allNumberingRadioButton.isSelected()) {
            sosadabovilleRadioButton.setSelected(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionButtonGroup = new javax.swing.ButtonGroup();
        numberingButtonGroup = new javax.swing.ButtonGroup();
        individualButtonGroup = new javax.swing.ButtonGroup();
        actionLabel = new javax.swing.JLabel();
        generateRadioButton = new javax.swing.JRadioButton();
        eraseRadioButton = new javax.swing.JRadioButton();
        numberingLabel = new javax.swing.JLabel();
        sosadabovilleRadioButton = new javax.swing.JRadioButton();
        sosaRadioButton = new javax.swing.JRadioButton();
        dabovilleRadioButton = new javax.swing.JRadioButton();
        allNumberingRadioButton = new javax.swing.JRadioButton();
        allSosaCheckBox = new javax.swing.JCheckBox();
        individualLabel = new javax.swing.JLabel();
        selectedIndividualRadioButton = new javax.swing.JRadioButton();
        currentDecujusRadioButton = new javax.swing.JRadioButton();
        otherIndividualRadioButton = new javax.swing.JRadioButton();
        selectIndiPanel = new javax.swing.JPanel();
        allIndividualRadioButton = new javax.swing.JRadioButton();
        saveCheckBox = new javax.swing.JCheckBox();
        numberSpouseCheckBox = new javax.swing.JCheckBox();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(actionLabel, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.actionLabel.text")); // NOI18N

        actionButtonGroup.add(generateRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(generateRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.generateRadioButton.text")); // NOI18N
        generateRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.generateRadioButton.toolTipText")); // NOI18N
        generateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateRadioButtonActionPerformed(evt);
            }
        });

        actionButtonGroup.add(eraseRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(eraseRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.eraseRadioButton.text")); // NOI18N
        eraseRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.eraseRadioButton.toolTipText")); // NOI18N
        eraseRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(numberingLabel, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.numberingLabel.text")); // NOI18N

        numberingButtonGroup.add(sosadabovilleRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(sosadabovilleRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.sosadabovilleRadioButton.text")); // NOI18N
        sosadabovilleRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.sosadabovilleRadioButton.toolTipText")); // NOI18N

        numberingButtonGroup.add(sosaRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(sosaRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.sosaRadioButton.text")); // NOI18N
        sosaRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.sosaRadioButton.toolTipText")); // NOI18N

        numberingButtonGroup.add(dabovilleRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dabovilleRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.dabovilleRadioButton.text")); // NOI18N
        dabovilleRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.dabovilleRadioButton.toolTipText")); // NOI18N

        numberingButtonGroup.add(allNumberingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(allNumberingRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allNumberingRadioButton.text")); // NOI18N
        allNumberingRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allNumberingRadioButton.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(allSosaCheckBox, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allSosaCheckBox.text")); // NOI18N
        allSosaCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allSosaCheckBox.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(individualLabel, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.individualLabel.text")); // NOI18N

        individualButtonGroup.add(selectedIndividualRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(selectedIndividualRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.selectedIndividualRadioButton.text")); // NOI18N
        selectedIndividualRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.selectedIndividualRadioButton.toolTipText")); // NOI18N
        selectedIndividualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedIndividualRadioButtonActionPerformed(evt);
            }
        });

        individualButtonGroup.add(currentDecujusRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(currentDecujusRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.currentDecujusRadioButton.text")); // NOI18N
        currentDecujusRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.currentDecujusRadioButton.toolTipText")); // NOI18N
        currentDecujusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentDecujusRadioButtonActionPerformed(evt);
            }
        });

        individualButtonGroup.add(otherIndividualRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(otherIndividualRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.otherIndividualRadioButton.text")); // NOI18N
        otherIndividualRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.otherIndividualRadioButton.toolTipText")); // NOI18N
        otherIndividualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherIndividualRadioButtonActionPerformed(evt);
            }
        });

        selectIndiPanel.setPreferredSize(new java.awt.Dimension(0, 30));
        selectIndiPanel.setLayout(new java.awt.BorderLayout());

        individualButtonGroup.add(allIndividualRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(allIndividualRadioButton, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allIndividualRadioButton.text")); // NOI18N
        allIndividualRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.allIndividualRadioButton.toolTipText")); // NOI18N
        allIndividualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allIndividualRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveCheckBox, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.saveCheckBox.text")); // NOI18N
        saveCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.saveCheckBox.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(numberSpouseCheckBox, org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.numberSpouseCheckBox.text")); // NOI18N
        numberSpouseCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SosaPanel.class, "SosaPanel.numberSpouseCheckBox.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectIndiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(otherIndividualRadioButton)
                                    .addComponent(selectedIndividualRadioButton)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(generateRadioButton)
                                        .addGap(18, 18, 18)
                                        .addComponent(eraseRadioButton))
                                    .addComponent(currentDecujusRadioButton)
                                    .addComponent(allIndividualRadioButton))
                                .addGap(0, 291, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(numberSpouseCheckBox)
                            .addComponent(allSosaCheckBox)
                            .addComponent(sosadabovilleRadioButton)
                            .addComponent(sosaRadioButton)
                            .addComponent(dabovilleRadioButton)
                            .addComponent(allNumberingRadioButton)
                            .addComponent(actionLabel)
                            .addComponent(numberingLabel)
                            .addComponent(individualLabel)
                            .addComponent(saveCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(actionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateRadioButton)
                    .addComponent(eraseRadioButton))
                .addGap(18, 18, 18)
                .addComponent(numberingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sosadabovilleRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sosaRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dabovilleRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allNumberingRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allSosaCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberSpouseCheckBox)
                .addGap(18, 18, 18)
                .addComponent(individualLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedIndividualRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(currentDecujusRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(otherIndividualRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectIndiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allIndividualRadioButton)
                .addGap(18, 18, 18)
                .addComponent(saveCheckBox)
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    }//GEN-LAST:event_formComponentResized

    private void generateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateRadioButtonActionPerformed
        mode = generateRadioButton.isSelected() ? MODE_GENERATE : MODE_ERASE;
        setDisplay();
    }//GEN-LAST:event_generateRadioButtonActionPerformed

    private void otherIndividualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherIndividualRadioButtonActionPerformed
        selectIndiPanel.setVisible(otherIndividualRadioButton.isSelected());
        if (otherIndividualRadioButton.isSelected()){
            selectEntityWidget.init();
        }
    }//GEN-LAST:event_otherIndividualRadioButtonActionPerformed

    private void eraseRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseRadioButtonActionPerformed
        mode = generateRadioButton.isSelected() ? MODE_GENERATE : MODE_ERASE;
        setDisplay();
    }//GEN-LAST:event_eraseRadioButtonActionPerformed

    private void selectedIndividualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedIndividualRadioButtonActionPerformed
        selectIndiPanel.setVisible(otherIndividualRadioButton.isSelected());
    }//GEN-LAST:event_selectedIndividualRadioButtonActionPerformed

    private void currentDecujusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentDecujusRadioButtonActionPerformed
        selectIndiPanel.setVisible(otherIndividualRadioButton.isSelected());
    }//GEN-LAST:event_currentDecujusRadioButtonActionPerformed

    private void allIndividualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allIndividualRadioButtonActionPerformed
        selectIndiPanel.setVisible(otherIndividualRadioButton.isSelected());
    }//GEN-LAST:event_allIndividualRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup actionButtonGroup;
    private javax.swing.JLabel actionLabel;
    private javax.swing.JRadioButton allIndividualRadioButton;
    private javax.swing.JRadioButton allNumberingRadioButton;
    private javax.swing.JCheckBox allSosaCheckBox;
    private javax.swing.JRadioButton currentDecujusRadioButton;
    private javax.swing.JRadioButton dabovilleRadioButton;
    private javax.swing.JRadioButton eraseRadioButton;
    private javax.swing.JRadioButton generateRadioButton;
    private javax.swing.ButtonGroup individualButtonGroup;
    private javax.swing.JLabel individualLabel;
    private javax.swing.JCheckBox numberSpouseCheckBox;
    private javax.swing.ButtonGroup numberingButtonGroup;
    private javax.swing.JLabel numberingLabel;
    private javax.swing.JRadioButton otherIndividualRadioButton;
    private javax.swing.JCheckBox saveCheckBox;
    private javax.swing.JPanel selectIndiPanel;
    private javax.swing.JRadioButton selectedIndividualRadioButton;
    private javax.swing.JRadioButton sosaRadioButton;
    private javax.swing.JRadioButton sosadabovilleRadioButton;
    // End of variables declaration//GEN-END:variables

    public Indi getSelection() {
        if (selectedIndividualRadioButton.isSelected()) {
            return selectedIndividual;
        } else if (currentDecujusRadioButton.isSelected()) {
            return decujusIndividual;
        } else if (otherIndividualRadioButton.isSelected()) {
            return (Indi) selectEntityWidget.getSelection();
        }
        return null;
    }
    
    public void savePreferences() {
        registry.put(ACTION, mode);
        int n = sosadabovilleRadioButton.isSelected() ? NUMBERING_SOSADABOVILLE : (sosaRadioButton.isSelected() ? NUMBERING_SOSA : (dabovilleRadioButton.isSelected() ? NUMBERING_DABOVILLE : NUMBERING_ALL));
        registry.put(NUMBERING, n);
        int s = selectedIndividualRadioButton.isSelected() ? 1 : (currentDecujusRadioButton.isSelected() ? 2 : (otherIndividualRadioButton.isSelected() ? 3 : 4));
        registry.put(SELECTION, s);
        registry.put(ALLSOSA, allSosaCheckBox.isSelected());
        registry.put(NUMBERING_SPOUSE, numberSpouseCheckBox.isSelected());
        registry.put(SAVE, saveCheckBox.isSelected());

        // Don't remove current DeCujus to allow to use it next time after a remove.
        if (mode == MODE_GENERATE) {
            registry.put(DECUJUSID, getSelection().getId());
        }
    }

    public String getResultMessage() {
        final Indi indi = getSelection();
        final String numbering;
        if (sosadabovilleRadioButton.isSelected()) {
            numbering = sosadabovilleRadioButton.getText();
        } else if (sosaRadioButton.isSelected()) {
            numbering = sosaRadioButton.getText();
        } else if (dabovilleRadioButton.isSelected()) {
            numbering = dabovilleRadioButton.getText();
        } else {
            numbering = allNumberingRadioButton.getText();
        }
        String msg = "";
        if (mode == MODE_GENERATE) {
            if (indi != null) {
                msg = NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.generateDone", numbering, indi.getName());
            }
        } else {
            if (indi != null) {
                msg = NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.eraseDone", numbering, indi.getName()); 
            } else {
                msg = NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.eraseAll", numbering.trim()); 
            }
        }
        return msg;
    }
}
