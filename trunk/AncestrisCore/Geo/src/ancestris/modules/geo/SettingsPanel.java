/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.geo;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author frederic
 */
public class SettingsPanel extends javax.swing.JPanel implements ChangeListener {

    private GeoMapTopComponent gmtc = null;
    private int markersSizeMax = 50;
    private ColorSelectionModel csm = null;

    
    /**
     * Creates new form SettingsPanel
     */
    public SettingsPanel(GeoMapTopComponent gmtc) {
        this.gmtc = gmtc;
        initComponents();
        jColorChooser.setPreviewPanel(new JPanel());
        jColorChooser.setColor(gmtc.getMarkersColor());
        jSizeSpinner.setValue(gmtc.getMarkersSize());
        jResizeWithZoom.setSelected(gmtc.getResizeWithZoom());
        
        csm = jColorChooser.getSelectionModel();
        csm.addChangeListener(this);
        
        rootIndividual.setText(gmtc.getFilerRootIndi());
        
        jAncestorCheckBox.setSelected(gmtc.getFilterAscendants());
        jDescendantCheckBox.setSelected(gmtc.getFilterDescendants());
        jCousinCheckBox.setSelected(gmtc.getFilterCousins());
        jOthersCheckBox.setSelected(gmtc.getFilterAncestors());

        jFromTextField.setText(gmtc.getFilterYearStart());
        jToTextField.setText(gmtc.getFilterYearEnd());
        jBirthCheckBox.setSelected(gmtc.getFilterBirths());
        jWeddingCheckBox.setSelected(gmtc.getFilterMarriages());
        jDeathCheckBox1.setSelected(gmtc.getFilterDeaths());
        jOtherEventCheckBox1.setSelected(gmtc.getFilterEvents());
        
        jSelectedRadioButton.setText(gmtc.getSelectedIndividual());
        
        jMenRadioButton.setSelected(gmtc.getFilterMales());
        jWomenRadioButton.setSelected(gmtc.getFilterFemales());
        jSelectedRadioButton.setSelected(gmtc.getFilterSelectedIndi());
        jSearchedRadioButton.setSelected(gmtc.getFilterSearch());
        jAllRadioButton.setSelected(!gmtc.getFilterMales() && !gmtc.getFilterFemales() && !gmtc.getFilterSelectedIndi() && !gmtc.getFilterSearch());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jRootLabel = new javax.swing.JLabel();
        jRoolIndiLabel = new javax.swing.JLabel();
        rootIndividual = new javax.swing.JLabel();
        jChooseLabel = new javax.swing.JLabel();
        jChooseDecujusButton = new javax.swing.JButton();
        jChooseSelectedButton = new javax.swing.JButton();
        jChooseRootButton = new javax.swing.JButton();
        jAncestorCheckBox = new javax.swing.JCheckBox();
        jDescendantCheckBox = new javax.swing.JCheckBox();
        jCousinCheckBox = new javax.swing.JCheckBox();
        jOthersCheckBox = new javax.swing.JCheckBox();
        jIndividualLabel = new javax.swing.JLabel();
        jAllRadioButton = new javax.swing.JRadioButton();
        jMenRadioButton = new javax.swing.JRadioButton();
        jWomenRadioButton = new javax.swing.JRadioButton();
        jSelectedRadioButton = new javax.swing.JRadioButton();
        jSearchedRadioButton = new javax.swing.JRadioButton();
        jEventsLabel = new javax.swing.JLabel();
        jYearsLabel = new javax.swing.JLabel();
        jFromTextField = new javax.swing.JTextField();
        jAndLabel = new javax.swing.JLabel();
        jToTextField = new javax.swing.JTextField();
        jBirthCheckBox = new javax.swing.JCheckBox();
        jWeddingCheckBox = new javax.swing.JCheckBox();
        jDeathCheckBox1 = new javax.swing.JCheckBox();
        jOtherEventCheckBox1 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jSizeLabel = new javax.swing.JLabel();
        jSizeSpinner = new javax.swing.JSpinner(new SpinnerNumberModel(10, 0, markersSizeMax, 1));
        jResizeWithZoom = new javax.swing.JCheckBox();
        jColorChooser = new javax.swing.JColorChooser();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jScrollPane1.setPreferredSize(new java.awt.Dimension(646, 383));

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(646, 383));

        jPanel2.setPreferredSize(new java.awt.Dimension(646, 383));

        jRootLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jRootLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jRootLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jRoolIndiLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jRoolIndiLabel.text")); // NOI18N

        rootIndividual.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rootIndividual, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.rootIndividual.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jChooseLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jChooseDecujusButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseDecujusButton.text")); // NOI18N
        jChooseDecujusButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseDecujusButton.toolTipText")); // NOI18N
        jChooseDecujusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChooseDecujusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jChooseSelectedButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseSelectedButton.text")); // NOI18N
        jChooseSelectedButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseSelectedButton.toolTipText", gmtc.getSelectedIndividual()));
        jChooseSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChooseSelectedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jChooseRootButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseRootButton.text")); // NOI18N
        jChooseRootButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jChooseRootButton.toolTipText")); // NOI18N
        jChooseRootButton.setMinimumSize(new java.awt.Dimension(61, 27));
        jChooseRootButton.setPreferredSize(new java.awt.Dimension(61, 27));
        jChooseRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChooseRootButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jAncestorCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jAncestorCheckBox.text")); // NOI18N
        jAncestorCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jAncestorCheckBox.toolTipText")); // NOI18N
        jAncestorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAncestorCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jDescendantCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jDescendantCheckBox.text")); // NOI18N
        jDescendantCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDescendantCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCousinCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jCousinCheckBox.text")); // NOI18N
        jCousinCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jCousinCheckBox.toolTipText")); // NOI18N
        jCousinCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCousinCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jOthersCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jOthersCheckBox.text")); // NOI18N
        jOthersCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jOthersCheckBox.toolTipText")); // NOI18N
        jOthersCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOthersCheckBoxActionPerformed(evt);
            }
        });

        jIndividualLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jIndividualLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jIndividualLabel.text")); // NOI18N

        buttonGroup1.add(jAllRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(jAllRadioButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jAllRadioButton.text")); // NOI18N
        jAllRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jAllRadioButton.toolTipText")); // NOI18N
        jAllRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAllRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jMenRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(jMenRadioButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jMenRadioButton.text")); // NOI18N
        jMenRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jMenRadioButton.toolTipText")); // NOI18N
        jMenRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jWomenRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(jWomenRadioButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jWomenRadioButton.text")); // NOI18N
        jWomenRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jWomenRadioButton.toolTipText")); // NOI18N
        jWomenRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jWomenRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jSelectedRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(jSelectedRadioButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSelectedRadioButton.text")); // NOI18N
        jSelectedRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSelectedRadioButton.toolTipText")); // NOI18N
        jSelectedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelectedRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jSearchedRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(jSearchedRadioButton, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSearchedRadioButton.text")); // NOI18N
        jSearchedRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSearchedRadioButton.toolTipText")); // NOI18N
        jSearchedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSearchedRadioButtonActionPerformed(evt);
            }
        });

        jEventsLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jEventsLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jEventsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jYearsLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jYearsLabel.text")); // NOI18N

        jFromTextField.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jFromTextField.text")); // NOI18N
        jFromTextField.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jFromTextField.toolTipText")); // NOI18N
        jFromTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFromTextFieldActionPerformed(evt);
            }
        });

        jAndLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jAndLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jAndLabel.text")); // NOI18N

        jToTextField.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jToTextField.text")); // NOI18N
        jToTextField.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jToTextField.toolTipText")); // NOI18N
        jToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jBirthCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jBirthCheckBox.text")); // NOI18N
        jBirthCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jBirthCheckBox.toolTipText")); // NOI18N
        jBirthCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBirthCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jWeddingCheckBox, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jWeddingCheckBox.text")); // NOI18N
        jWeddingCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jWeddingCheckBox.toolTipText")); // NOI18N
        jWeddingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jWeddingCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jDeathCheckBox1, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jDeathCheckBox1.text")); // NOI18N
        jDeathCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jDeathCheckBox1.toolTipText")); // NOI18N
        jDeathCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeathCheckBox1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jOtherEventCheckBox1, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jOtherEventCheckBox1.text")); // NOI18N
        jOtherEventCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jOtherEventCheckBox1.toolTipText")); // NOI18N
        jOtherEventCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOtherEventCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jRoolIndiLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rootIndividual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jBirthCheckBox)
                                            .addComponent(jWeddingCheckBox)
                                            .addComponent(jDeathCheckBox1)
                                            .addComponent(jOtherEventCheckBox1)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jAncestorCheckBox)
                                                .addGap(18, 18, 18)
                                                .addComponent(jDescendantCheckBox)
                                                .addGap(18, 18, 18)
                                                .addComponent(jCousinCheckBox))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jYearsLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jAndLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jIndividualLabel)
                                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                                        .addGap(2, 2, 2)
                                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jSearchedRadioButton)
                                                            .addComponent(jSelectedRadioButton)
                                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jAllRadioButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jMenRadioButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jWomenRadioButton))))))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jOthersCheckBox))))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jChooseLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jChooseDecujusButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jChooseSelectedButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jChooseRootButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(345, 345, 345))))
                    .addComponent(jEventsLabel)
                    .addComponent(jRootLabel))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRootLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRoolIndiLabel)
                    .addComponent(rootIndividual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAncestorCheckBox)
                    .addComponent(jDescendantCheckBox)
                    .addComponent(jCousinCheckBox)
                    .addComponent(jOthersCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jChooseLabel)
                    .addComponent(jChooseDecujusButton)
                    .addComponent(jChooseSelectedButton)
                    .addComponent(jChooseRootButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jIndividualLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMenRadioButton)
                            .addComponent(jAllRadioButton)
                            .addComponent(jWomenRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSelectedRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSearchedRadioButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jEventsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jYearsLabel)
                            .addComponent(jFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAndLabel)
                            .addComponent(jToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBirthCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jWeddingCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDeathCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOtherEventCheckBox1)))
                .addGap(0, 56, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jSizeLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jSizeLabel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSizeLabel.text")); // NOI18N

        jSizeSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jSizeSpinner.toolTipText")); // NOI18N
        jSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSizeSpinnerStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jResizeWithZoom, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jResizeWithZoom.text")); // NOI18N
        jResizeWithZoom.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jResizeWithZoom.toolTipText")); // NOI18N
        jResizeWithZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResizeWithZoomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jResizeWithZoom))
                    .addComponent(jColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSizeSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(jSizeLabel)
                    .addComponent(jResizeWithZoom))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jScrollPane1.setViewportView(jTabbedPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSizeSpinnerStateChanged
        gmtc.setMarkersSize((int) Integer.valueOf(jSizeSpinner.getValue().toString()));
    }//GEN-LAST:event_jSizeSpinnerStateChanged

    private void jAncestorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAncestorCheckBoxActionPerformed
        gmtc.setFilterAscendants(jAncestorCheckBox.isSelected());
    }//GEN-LAST:event_jAncestorCheckBoxActionPerformed

    private void jCousinCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCousinCheckBoxActionPerformed
        gmtc.setFilterCousins(jCousinCheckBox.isSelected());
    }//GEN-LAST:event_jCousinCheckBoxActionPerformed

    private void jOthersCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOthersCheckBoxActionPerformed
        gmtc.setFilterAncestors(jOthersCheckBox.isSelected());
    }//GEN-LAST:event_jOthersCheckBoxActionPerformed

    private void jChooseRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChooseRootButtonActionPerformed
        rootIndividual.setText(gmtc.setFilterRootIndi());
    }//GEN-LAST:event_jChooseRootButtonActionPerformed

    private void jFromTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFromTextFieldActionPerformed
        gmtc.setFilterYearStart(jFromTextField.getText());
    }//GEN-LAST:event_jFromTextFieldActionPerformed

    private void jToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToTextFieldActionPerformed
        gmtc.setFilterYearEnd(jToTextField.getText());
    }//GEN-LAST:event_jToTextFieldActionPerformed

    private void jBirthCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBirthCheckBoxActionPerformed
        gmtc.setFilterBirths(jBirthCheckBox.isSelected());
    }//GEN-LAST:event_jBirthCheckBoxActionPerformed

    private void jWeddingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jWeddingCheckBoxActionPerformed
        gmtc.setFilterMarriages(jWeddingCheckBox.isSelected());
    }//GEN-LAST:event_jWeddingCheckBoxActionPerformed

    private void jDeathCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeathCheckBox1ActionPerformed
        gmtc.setFilterDeaths(jDeathCheckBox1.isSelected());
    }//GEN-LAST:event_jDeathCheckBox1ActionPerformed

    private void jOtherEventCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOtherEventCheckBox1ActionPerformed
        gmtc.setFilterEvents(jOtherEventCheckBox1.isSelected());
    }//GEN-LAST:event_jOtherEventCheckBox1ActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown

    }//GEN-LAST:event_formComponentShown

    private void jChooseDecujusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChooseDecujusButtonActionPerformed
        rootIndividual.setText(gmtc.setFilterDeCujusIndi());
    }//GEN-LAST:event_jChooseDecujusButtonActionPerformed

    private void jDescendantCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDescendantCheckBoxActionPerformed
        gmtc.setFilterDescendants(jDescendantCheckBox.isSelected());
    }//GEN-LAST:event_jDescendantCheckBoxActionPerformed

    private void jChooseSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChooseSelectedButtonActionPerformed
        rootIndividual.setText(gmtc.setFilterSelectedIndi());
    }//GEN-LAST:event_jChooseSelectedButtonActionPerformed

    private void jResizeWithZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResizeWithZoomActionPerformed
        gmtc.setResizeWithZoom(jResizeWithZoom.isSelected());
        if (!jResizeWithZoom.isSelected()) {
            gmtc.setMarkersSize((int) Integer.valueOf(jSizeSpinner.getValue().toString()));
        }
    }//GEN-LAST:event_jResizeWithZoomActionPerformed

    private void jMenRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenRadioButtonActionPerformed
        setRadioButtons();
    }//GEN-LAST:event_jMenRadioButtonActionPerformed

    private void jWomenRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jWomenRadioButtonActionPerformed
        setRadioButtons();
    }//GEN-LAST:event_jWomenRadioButtonActionPerformed

    private void jSelectedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelectedRadioButtonActionPerformed
        setRadioButtons();
    }//GEN-LAST:event_jSelectedRadioButtonActionPerformed

    private void jSearchedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSearchedRadioButtonActionPerformed
        setRadioButtons();
    }//GEN-LAST:event_jSearchedRadioButtonActionPerformed

    private void jAllRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAllRadioButtonActionPerformed
        setRadioButtons();
    }//GEN-LAST:event_jAllRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton jAllRadioButton;
    private javax.swing.JCheckBox jAncestorCheckBox;
    private javax.swing.JLabel jAndLabel;
    private javax.swing.JCheckBox jBirthCheckBox;
    private javax.swing.JButton jChooseDecujusButton;
    private javax.swing.JLabel jChooseLabel;
    private javax.swing.JButton jChooseRootButton;
    private javax.swing.JButton jChooseSelectedButton;
    private javax.swing.JColorChooser jColorChooser;
    private javax.swing.JCheckBox jCousinCheckBox;
    private javax.swing.JCheckBox jDeathCheckBox1;
    private javax.swing.JCheckBox jDescendantCheckBox;
    private javax.swing.JLabel jEventsLabel;
    private javax.swing.JTextField jFromTextField;
    private javax.swing.JLabel jIndividualLabel;
    private javax.swing.JRadioButton jMenRadioButton;
    private javax.swing.JCheckBox jOtherEventCheckBox1;
    private javax.swing.JCheckBox jOthersCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JCheckBox jResizeWithZoom;
    private javax.swing.JLabel jRoolIndiLabel;
    private javax.swing.JLabel jRootLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jSearchedRadioButton;
    private javax.swing.JRadioButton jSelectedRadioButton;
    private javax.swing.JLabel jSizeLabel;
    private javax.swing.JSpinner jSizeSpinner;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jToTextField;
    private javax.swing.JCheckBox jWeddingCheckBox;
    private javax.swing.JRadioButton jWomenRadioButton;
    private javax.swing.JLabel jYearsLabel;
    private javax.swing.JLabel rootIndividual;
    // End of variables declaration//GEN-END:variables

    public void stateChanged(ChangeEvent e) {
        gmtc.setMarkersColor(jColorChooser.getColor());
    }

    public void saveDates() {
        gmtc.setFilterYearStart(jFromTextField.getText());
        gmtc.setFilterYearEnd(jToTextField.getText());
    }

    private void setRadioButtons() {
        gmtc.setFilterMales(jMenRadioButton.isSelected());
        gmtc.setFilterFemales(jWomenRadioButton.isSelected());
        gmtc.setFilterSelectedIndi(jSelectedRadioButton.isSelected());
        gmtc.setFilterSelectedSearch(jSearchedRadioButton.isSelected());
    }

}
