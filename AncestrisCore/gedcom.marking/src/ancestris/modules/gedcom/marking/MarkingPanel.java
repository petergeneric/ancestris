/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.marking;

import genj.gedcom.Context;
import genj.util.Registry;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class MarkingPanel extends javax.swing.JPanel {

    private Context context;
    private Registry registry = null;
    private Settings settings = new Settings();    
    private static String prefix = "_";
    
    public final static String 
            SEARCH_INDI = "SEARCH_INDI",
            SEARCH_PARENT_OF = "SEARCH_PARENT_OF",
            SEARCH_CHILD_OF = "SEARCH_CHILD_OF",
            SEARCH_SPOUSE_OF = "SEARCH_SPOUSE_OF",
            SEARCH_ANCESTOR_OF = "SEARCH_ANCESTOR_OF",
            SEARCH_DESCENDANT_OF = "SEARCH_DESCENDANT_OF";

    public final static String[] SEARCH_OPTIONS = { SEARCH_INDI, SEARCH_PARENT_OF, SEARCH_CHILD_OF, SEARCH_SPOUSE_OF, SEARCH_ANCESTOR_OF, SEARCH_DESCENDANT_OF };
    public String[] options = new String[SEARCH_OPTIONS.length];

    
    /**
     * Creates new form MarkingPanel
     */
    public MarkingPanel(Context context) {
        this.context = context;
        registry = context.getGedcom().getRegistry();
        
        for (int i=0 ; i < SEARCH_OPTIONS.length ; i++) {
            options[i] = NbBundle.getMessage(this.getClass(), SEARCH_OPTIONS[i]);
        }
        initComponents();
        
        loadPreferences();
        
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelType = new javax.swing.JLabel();
        jLabelTag = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxTreeTop = new javax.swing.JCheckBox();
        jTextFieldTreeTopTag = new javax.swing.JTextField();
        jTextFieldTreeTopValue = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxTreeBottom = new javax.swing.JCheckBox();
        jTextFieldTreeBottomTag = new javax.swing.JTextField();
        jTextFieldTreeBottomValue = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxImplex = new javax.swing.JCheckBox();
        jTextFieldImplexTag = new javax.swing.JTextField();
        jTextFieldImplexValue = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxMulti = new javax.swing.JCheckBox();
        jTextFieldMultiTag = new javax.swing.JTextField();
        jTextFieldMultiValue = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxSearch = new javax.swing.JCheckBox();
        jTextFieldSearchTag = new javax.swing.JTextField();
        jTextFieldSearchValue = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelActions = new javax.swing.JLabel();
        jCheckBoxErase = new javax.swing.JCheckBox();
        jCheckBoxMark = new javax.swing.JCheckBox();
        jCheckBoxDisplay = new javax.swing.JCheckBox();
        jComboBoxSearchOptions = new javax.swing.JComboBox<>();

        org.openide.awt.Mnemonics.setLocalizedText(jLabelType, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabelType.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelTag, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabelTag.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelValue, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabelValue.text")); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/marking/ico_treetop.png"))); // NOI18N
        jLabel1.setLabelFor(jCheckBoxTreeTop);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabel1.text")); // NOI18N
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxTreeTop, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxTreeTop.text")); // NOI18N
        jCheckBoxTreeTop.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxTreeTop.toolTipText")); // NOI18N

        jTextFieldTreeTopTag.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldTreeTopTag.text")); // NOI18N
        jTextFieldTreeTopTag.setPreferredSize(new java.awt.Dimension(65, 27));

        jTextFieldTreeTopValue.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldTreeTopValue.text")); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/marking/ico_treebottom.png"))); // NOI18N
        jLabel2.setLabelFor(jCheckBoxTreeTop);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabel2.text")); // NOI18N
        jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxTreeBottom, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxTreeBottom.text")); // NOI18N
        jCheckBoxTreeBottom.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxTreeBottom.toolTipText")); // NOI18N

        jTextFieldTreeBottomTag.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldTreeBottomTag.text")); // NOI18N
        jTextFieldTreeBottomTag.setPreferredSize(new java.awt.Dimension(65, 27));

        jTextFieldTreeBottomValue.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldTreeBottomValue.text")); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/marking/ico_implex.png"))); // NOI18N
        jLabel3.setLabelFor(jCheckBoxTreeTop);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabel3.text")); // NOI18N
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxImplex, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxImplex.text")); // NOI18N
        jCheckBoxImplex.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxImplex.toolTipText")); // NOI18N

        jTextFieldImplexTag.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldImplexTag.text")); // NOI18N
        jTextFieldImplexTag.setPreferredSize(new java.awt.Dimension(65, 27));

        jTextFieldImplexValue.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldImplexValue.text")); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/marking/ico_multi.png"))); // NOI18N
        jLabel4.setLabelFor(jCheckBoxTreeTop);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabel4.text")); // NOI18N
        jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxMulti, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxMulti.text")); // NOI18N
        jCheckBoxMulti.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxMulti.toolTipText")); // NOI18N

        jTextFieldMultiTag.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldMultiTag.text")); // NOI18N
        jTextFieldMultiTag.setPreferredSize(new java.awt.Dimension(65, 27));

        jTextFieldMultiValue.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldMultiValue.text")); // NOI18N

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/marking/ico_search.png"))); // NOI18N
        jLabel5.setLabelFor(jCheckBoxTreeTop);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxSearch, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxSearch.text")); // NOI18N
        jCheckBoxSearch.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxSearch.toolTipText")); // NOI18N
        jCheckBoxSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSearchActionPerformed(evt);
            }
        });

        jTextFieldSearchTag.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldSearchTag.text")); // NOI18N
        jTextFieldSearchTag.setPreferredSize(new java.awt.Dimension(65, 27));

        jTextFieldSearchValue.setText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jTextFieldSearchValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelActions, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jLabelActions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxErase, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxErase.text")); // NOI18N
        jCheckBoxErase.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxErase.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxMark, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxMark.text")); // NOI18N
        jCheckBoxMark.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxMark.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplay, org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxDisplay.text")); // NOI18N
        jCheckBoxDisplay.setToolTipText(org.openide.util.NbBundle.getMessage(MarkingPanel.class, "MarkingPanel.jCheckBoxDisplay.toolTipText")); // NOI18N

        jComboBoxSearchOptions.setModel(new javax.swing.DefaultComboBoxModel<>(options));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBoxSearch)
                                    .addComponent(jCheckBoxMulti)
                                    .addComponent(jCheckBoxImplex)
                                    .addComponent(jCheckBoxTreeBottom)
                                    .addComponent(jCheckBoxTreeTop))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldSearchTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldMultiTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldImplexTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldTreeBottomTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldTreeTopTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldSearchValue)
                                    .addComponent(jTextFieldMultiValue)
                                    .addComponent(jTextFieldImplexValue)
                                    .addComponent(jTextFieldTreeBottomValue)
                                    .addComponent(jTextFieldTreeTopValue)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jComboBoxSearchOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jSeparator1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelActions)
                                    .addComponent(jLabelType)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(238, 238, 238)
                                .addComponent(jLabelTag))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(417, 417, 417)
                                .addComponent(jLabelValue)))
                        .addGap(0, 124, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jCheckBoxErase)
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxMark)
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxDisplay)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelType)
                    .addComponent(jLabelTag)
                    .addComponent(jLabelValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxTreeTop)
                        .addComponent(jTextFieldTreeTopTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldTreeTopValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxTreeBottom)
                        .addComponent(jTextFieldTreeBottomTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldTreeBottomValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxImplex)
                        .addComponent(jTextFieldImplexTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldImplexValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxMulti)
                        .addComponent(jTextFieldMultiTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldMultiValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxSearch)
                            .addComponent(jTextFieldSearchTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldSearchValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxSearchOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelActions)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxMark)
                    .addComponent(jCheckBoxDisplay)
                    .addComponent(jCheckBoxErase))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSearchActionPerformed
        jComboBoxSearchOptions.setVisible(jCheckBoxSearch.isSelected());
    }//GEN-LAST:event_jCheckBoxSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxDisplay;
    private javax.swing.JCheckBox jCheckBoxErase;
    private javax.swing.JCheckBox jCheckBoxImplex;
    private javax.swing.JCheckBox jCheckBoxMark;
    private javax.swing.JCheckBox jCheckBoxMulti;
    private javax.swing.JCheckBox jCheckBoxSearch;
    private javax.swing.JCheckBox jCheckBoxTreeBottom;
    private javax.swing.JCheckBox jCheckBoxTreeTop;
    private javax.swing.JComboBox<String> jComboBoxSearchOptions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelActions;
    private javax.swing.JLabel jLabelTag;
    private javax.swing.JLabel jLabelType;
    private javax.swing.JLabel jLabelValue;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextFieldImplexTag;
    private javax.swing.JTextField jTextFieldImplexValue;
    private javax.swing.JTextField jTextFieldMultiTag;
    private javax.swing.JTextField jTextFieldMultiValue;
    private javax.swing.JTextField jTextFieldSearchTag;
    private javax.swing.JTextField jTextFieldSearchValue;
    private javax.swing.JTextField jTextFieldTreeBottomTag;
    private javax.swing.JTextField jTextFieldTreeBottomValue;
    private javax.swing.JTextField jTextFieldTreeTopTag;
    private javax.swing.JTextField jTextFieldTreeTopValue;
    // End of variables declaration//GEN-END:variables


    private void loadPreferences() {
        jCheckBoxTreeTop.setSelected(registry.get("MarkingTreeTop", false));
        jTextFieldTreeTopTag.setText(registry.get("MarkingTreeTopTag", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldTreeTopTag.text")));
        jTextFieldTreeTopValue.setText(registry.get("MarkingTreeTopValue", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldTreeTopValue.text")));
        
        jCheckBoxTreeBottom.setSelected(registry.get("MarkingTreeBottom", false));
        jTextFieldTreeBottomTag.setText(registry.get("MarkingTreeBottomTag", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldTreeBottomTag.text")));
        jTextFieldTreeBottomValue.setText(registry.get("MarkingTreeBottomValue", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldTreeBottomValue.text")));
        
        jCheckBoxImplex.setSelected(registry.get("MarkingImplex", false));
        jTextFieldImplexTag.setText(registry.get("MarkingImplexTag", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldImplexTag.text")));
        jTextFieldImplexValue.setText(registry.get("MarkingImplexValue", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldImplexValue.text")));
        
        jCheckBoxMulti.setSelected(registry.get("MarkingMulti", false));
        jTextFieldMultiTag.setText(registry.get("MarkingMultiTag", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldMultiTag.text")));
        jTextFieldMultiValue.setText(registry.get("MarkingMultiValue", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldMultiValue.text")));
        
        jCheckBoxSearch.setSelected(registry.get("MarkingSearch", false));
        jTextFieldSearchTag.setText(registry.get("MarkingSearchTag", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldSearchTag.text")));
        jTextFieldSearchValue.setText(registry.get("MarkingSearchValue", NbBundle.getMessage(MarkingAction.class, "MarkingPanel.jTextFieldSearchValue.text")));
        jComboBoxSearchOptions.setSelectedIndex(indexOf(registry.get("MarkingSearchOption", SEARCH_INDI)));
        
        jCheckBoxErase.setSelected(registry.get("MarkingErase", false));
        jCheckBoxMark.setSelected(registry.get("MarkingMark", false));
        jCheckBoxDisplay.setSelected(registry.get("MarkingDisplay", false));
        
        jComboBoxSearchOptions.setVisible(jCheckBoxSearch.isSelected());

    }

    public void savePreferences() {
        settings.isTreeTop = jCheckBoxTreeTop.isSelected();
        settings.treeTopTag = jTextFieldTreeTopTag.getText().toUpperCase();
        if (!settings.treeTopTag.startsWith(prefix)) {
            settings.treeTopTag = prefix + settings.treeTopTag;
        }
        settings.treeTopValue = jTextFieldTreeTopValue.getText();

        
        settings.isTreeBottom = jCheckBoxTreeBottom.isSelected();
        settings.treeBottomTag = jTextFieldTreeBottomTag.getText().toUpperCase();
        if (!settings.treeBottomTag.startsWith(prefix)) {
            settings.treeBottomTag = prefix + settings.treeBottomTag;
        }
        settings.treeBottomValue = jTextFieldTreeBottomValue.getText();

        
        settings.isImplex = jCheckBoxImplex.isSelected();
        settings.implexTag = jTextFieldImplexTag.getText().toUpperCase();
        if (!settings.implexTag.startsWith(prefix)) {
            settings.implexTag = prefix + settings.implexTag;
        }
        settings.implexValue = jTextFieldImplexValue.getText();

        
        settings.isMulti = jCheckBoxMulti.isSelected();
        settings.multiTag = jTextFieldMultiTag.getText().toUpperCase();
        if (!settings.multiTag.startsWith(prefix)) {
            settings.multiTag = prefix + settings.multiTag;
        }
        settings.multiValue = jTextFieldMultiValue.getText();

        
        settings.isSearch = jCheckBoxSearch.isSelected();
        settings.searchTag = jTextFieldSearchTag.getText().toUpperCase();
        if (!settings.searchTag.startsWith(prefix)) {
            settings.searchTag = prefix + settings.searchTag;
        }
        settings.searchValue = jTextFieldSearchValue.getText();
        settings.searchOption = SEARCH_OPTIONS[jComboBoxSearchOptions.getSelectedIndex()];

        settings.toBeErased = jCheckBoxErase.isSelected();
        settings.toBeMarked = jCheckBoxMark.isSelected();
        settings.toBeDisplayed = jCheckBoxDisplay.isSelected();

        
        registry.put("MarkingTreeTop", settings.isTreeTop);
        registry.put("MarkingTreeTopTag", settings.treeTopTag);
        registry.put("MarkingTreeTopValue", settings.treeTopValue);
        
        registry.put("MarkingTreeBottom", settings.isTreeBottom);
        registry.put("MarkingTreeBottomTag", settings.treeBottomTag);
        registry.put("MarkingTreeBottomValue", settings.treeBottomValue);
        
        registry.put("MarkingImplex", settings.isImplex);
        registry.put("MarkingImplexTag", settings.implexTag);
        registry.put("MarkingImplexValue", settings.implexValue);
        
        registry.put("MarkingMulti", settings.isMulti);
        registry.put("MarkingMultiTag", settings.multiTag);
        registry.put("MarkingMultiValue", settings.multiValue);
        
        registry.put("MarkingSearch", settings.isSearch);
        registry.put("MarkingSearchTag", settings.searchTag);
        registry.put("MarkingSearchValue", settings.searchValue);
        registry.put("MarkingSearchOption", settings.searchOption);
        
        registry.put("MarkingErase", settings.toBeErased);
        registry.put("MarkingMark", settings.toBeMarked);
        registry.put("MarkingDisplay", settings.toBeDisplayed);
        
        
    }

    public Settings getSettings() {
        return settings;
    }

    private int indexOf(String value) {
        for (int i=0 ; i < SEARCH_OPTIONS.length ; i++) {
            if (SEARCH_OPTIONS[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public static class Settings {
        
        public boolean isTreeTop = true;
        public boolean isTreeBottom = true;
        public boolean isImplex = true;
        public boolean isMulti = true;
        public boolean isSearch = true;

        public String treeTopTag = "";
        public String treeBottomTag = "";
        public String implexTag = "";
        public String multiTag = "";
        public String searchTag = "";

        public String treeTopValue = "";
        public String treeBottomValue = "";
        public String implexValue = "";
        public String multiValue = "";
        public String searchValue = "";

        public String searchOption = "";
        
        public boolean toBeErased = true;
        public boolean toBeMarked = true;
        public boolean toBeDisplayed = true;
        
    }
    


}
