/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.api.editor.AncestrisEditor;
import ancestris.core.CoreOptions;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.util.Lifecycle;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import org.openide.awt.StatusDisplayer;

@SuppressWarnings(value={"unchecked", "rawtypes"})
final class OptionDataPanel extends javax.swing.JPanel {

    private final OptionDataOptionsPanelController controller;
    String[] encodings = Gedcom.ENCODINGS;
    private AncestrisPreferences gedcomPrefs = null;
    private GedcomOptions gedcomOptions = null;
    private DefaultComboBoxModel comboModel = null;
    private List<AncestrisEditor> editors = null;

    OptionDataPanel(OptionDataOptionsPanelController controller) {
        this.controller = controller;

        gedcomPrefs = Registry.get(genj.gedcom.GedcomOptions.class);
        gedcomOptions = GedcomOptions.getInstance();
        editors = new ArrayList<AncestrisEditor>();
        Indi indi = new Indi();
        for (AncestrisEditor edt : AncestrisEditor.findEditors()) {
            if (edt.canEdit(indi)) {
                editors.add(edt);
            }
        }
        AncestrisEditor[] arrayEditors = editors.toArray(new AncestrisEditor[editors.size()]);
        comboModel = new DefaultComboBoxModel(arrayEditors);
                
        initComponents();
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        namesPanel = new javax.swing.JPanel();
        cbNamesInUppercase = new javax.swing.JCheckBox();
        cbSameSpouseName = new javax.swing.JCheckBox();
        cbGivenName = new javax.swing.JCheckBox();
        jtGivenTag = new javax.swing.JTextField();
        IDPanel = new javax.swing.JPanel();
        cbReuseIDs = new javax.swing.JCheckBox();
        lDefaultIDLength = new javax.swing.JLabel();
        idLength = new javax.swing.JSpinner();
        placePanel = new javax.swing.JPanel();
        cbUseSpace = new javax.swing.JCheckBox();
        cbSplitJuridictions = new javax.swing.JCheckBox();
        encodingPanel = new javax.swing.JPanel();
        lFileEncoding = new javax.swing.JLabel();
        cboxEncoding = new javax.swing.JComboBox(encodings);
        cbSaveEncoding = new javax.swing.JCheckBox();
        editingPanel = new javax.swing.JPanel();
        lDefaultEditor = new javax.swing.JLabel();
        cboxDefaultEditor = new javax.swing.JComboBox();
        cbAutoCommit = new javax.swing.JCheckBox();
        nbCancellations = new javax.swing.JSpinner(new SpinnerNumberModel(10, 10, 300, 5));
        lCancellations = new javax.swing.JLabel();
        cbCreateSpouse = new javax.swing.JCheckBox();
        cbDetectDuplicate = new javax.swing.JCheckBox();
        cbDuplicateEachTime = new javax.swing.JCheckBox();
        cbAddAge = new javax.swing.JCheckBox();

        setRequestFocusEnabled(false);

        mainPanel.setPreferredSize(new java.awt.Dimension(582, 384));

        namesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.namesPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbNamesInUppercase, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbNamesInUppercase.text")); // NOI18N
        cbNamesInUppercase.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbNamesInUppercase.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSameSpouseName, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSameSpouseName.text")); // NOI18N
        cbSameSpouseName.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSameSpouseName.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbGivenName, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbGivenName.text")); // NOI18N
        cbGivenName.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbGivenName.toolTipText")); // NOI18N
        cbGivenName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGivenNameActionPerformed(evt);
            }
        });

        jtGivenTag.setText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.jtGivenTag.text")); // NOI18N
        jtGivenTag.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.jtGivenTag.toolTipText")); // NOI18N
        jtGivenTag.setMinimumSize(new java.awt.Dimension(64, 23));

        javax.swing.GroupLayout namesPanelLayout = new javax.swing.GroupLayout(namesPanel);
        namesPanel.setLayout(namesPanelLayout);
        namesPanelLayout.setHorizontalGroup(
            namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbNamesInUppercase)
                    .addComponent(cbSameSpouseName)
                    .addGroup(namesPanelLayout.createSequentialGroup()
                        .addComponent(cbGivenName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtGivenTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        namesPanelLayout.setVerticalGroup(
            namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namesPanelLayout.createSequentialGroup()
                .addComponent(cbNamesInUppercase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSameSpouseName)
                .addGap(3, 3, 3)
                .addGroup(namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbGivenName)
                    .addComponent(jtGivenTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        IDPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.IDPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbReuseIDs, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbReuseIDs.text")); // NOI18N
        cbReuseIDs.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbReuseIDs.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lDefaultIDLength, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lDefaultIDLength.text")); // NOI18N
        lDefaultIDLength.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lDefaultIDLength.toolTipText")); // NOI18N

        idLength.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        idLength.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.idLength.toolTipText")); // NOI18N

        javax.swing.GroupLayout IDPanelLayout = new javax.swing.GroupLayout(IDPanel);
        IDPanel.setLayout(IDPanelLayout);
        IDPanelLayout.setHorizontalGroup(
            IDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IDPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(IDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(IDPanelLayout.createSequentialGroup()
                        .addComponent(cbReuseIDs)
                        .addGap(0, 39, Short.MAX_VALUE))
                    .addGroup(IDPanelLayout.createSequentialGroup()
                        .addComponent(lDefaultIDLength)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(idLength)))
                .addContainerGap())
        );
        IDPanelLayout.setVerticalGroup(
            IDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IDPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbReuseIDs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(IDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lDefaultIDLength)
                    .addComponent(idLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        placePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.placePanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbUseSpace, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbUseSpace.text")); // NOI18N
        cbUseSpace.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbUseSpace.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSplitJuridictions, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSplitJuridictions.text")); // NOI18N
        cbSplitJuridictions.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSplitJuridictions.toolTipText")); // NOI18N

        javax.swing.GroupLayout placePanelLayout = new javax.swing.GroupLayout(placePanel);
        placePanel.setLayout(placePanelLayout);
        placePanelLayout.setHorizontalGroup(
            placePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(placePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(placePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbUseSpace, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                    .addComponent(cbSplitJuridictions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        placePanelLayout.setVerticalGroup(
            placePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(placePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbUseSpace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSplitJuridictions)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        encodingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.encodingPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lFileEncoding, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lFileEncoding.text")); // NOI18N
        lFileEncoding.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lFileEncoding.toolTipText")); // NOI18N

        cboxEncoding.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cboxEncoding.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSaveEncoding, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSaveEncoding.text")); // NOI18N
        cbSaveEncoding.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbSaveEncoding.toolTipText")); // NOI18N

        javax.swing.GroupLayout encodingPanelLayout = new javax.swing.GroupLayout(encodingPanel);
        encodingPanel.setLayout(encodingPanelLayout);
        encodingPanelLayout.setHorizontalGroup(
            encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encodingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(encodingPanelLayout.createSequentialGroup()
                        .addComponent(lFileEncoding)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxEncoding, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(encodingPanelLayout.createSequentialGroup()
                        .addComponent(cbSaveEncoding)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        encodingPanelLayout.setVerticalGroup(
            encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encodingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lFileEncoding)
                    .addComponent(cboxEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSaveEncoding)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        editingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.editingPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lDefaultEditor, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lDefaultEditor.text")); // NOI18N
        lDefaultEditor.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lDefaultEditor.toolTipText")); // NOI18N

        cboxDefaultEditor.setModel(comboModel);
        cboxDefaultEditor.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cboxDefaultEditor.toolTipText")); // NOI18N
        cboxDefaultEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxDefaultEditorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbAutoCommit, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbAutoCommit.text")); // NOI18N
        cbAutoCommit.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbAutoCommit.toolTipText")); // NOI18N
        cbAutoCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAutoCommitActionPerformed(evt);
            }
        });

        nbCancellations.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.nbCancellations.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lCancellations, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lCancellations.text")); // NOI18N
        lCancellations.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.lCancellations.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCreateSpouse, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbCreateSpouse.text_1")); // NOI18N
        cbCreateSpouse.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbCreateSpouse.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDetectDuplicate, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbDetectDuplicate.text")); // NOI18N
        cbDetectDuplicate.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbDetectDuplicate.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDuplicateEachTime, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbDuplicateEachTime.text")); // NOI18N
        cbDuplicateEachTime.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbDuplicateEachTime.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbAddAge, org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.cbAddAge.text")); // NOI18N

        javax.swing.GroupLayout editingPanelLayout = new javax.swing.GroupLayout(editingPanel);
        editingPanel.setLayout(editingPanelLayout);
        editingPanelLayout.setHorizontalGroup(
            editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editingPanelLayout.createSequentialGroup()
                        .addComponent(lDefaultEditor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxDefaultEditor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(editingPanelLayout.createSequentialGroup()
                        .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbAutoCommit)
                            .addGroup(editingPanelLayout.createSequentialGroup()
                                .addComponent(nbCancellations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lCancellations))
                            .addComponent(cbCreateSpouse)
                            .addComponent(cbDuplicateEachTime)
                            .addComponent(cbDetectDuplicate)
                            .addComponent(cbAddAge))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        editingPanelLayout.setVerticalGroup(
            editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lDefaultEditor)
                    .addComponent(cboxDefaultEditor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAutoCommit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nbCancellations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lCancellations))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCreateSpouse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAddAge)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDuplicateEachTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDetectDuplicate)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(namesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(placePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(encodingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(IDPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(namesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(placePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(IDPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(encodingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(105, 105, 105))
        );

        namesPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.namesPanel.AccessibleContext.accessibleName")); // NOI18N
        IDPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.IDPanel.AccessibleContext.accessibleName")); // NOI18N
        placePanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.placePanel.AccessibleContext.accessibleName")); // NOI18N
        encodingPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionDataPanel.encodingPanel.AccessibleContext.accessibleName")); // NOI18N

        jScrollPane1.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbAutoCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAutoCommitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbAutoCommitActionPerformed

    private void cboxDefaultEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxDefaultEditorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboxDefaultEditorActionPerformed

    private void cbGivenNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGivenNameActionPerformed
        jtGivenTag.setEnabled(cbGivenName.isSelected());
    }//GEN-LAST:event_cbGivenNameActionPerformed

    void load() {
        // Names
        cbNamesInUppercase.setSelected(gedcomOptions.isUpperCaseNames());
        cbGivenName.setSelected(!gedcomOptions.getGivenTag().isEmpty());
        jtGivenTag.setText(gedcomOptions.getGivenTag());
        cbGivenNameActionPerformed(new java.awt.event.ActionEvent(this,0,null));
        setNamesSpouse(gedcomPrefs.get("setWifeLastname", ""));

        // ID Numbers
        setIDFilling(gedcomPrefs.get("isFillGapsInIDs", ""));
        idLength.setValue(gedcomOptions.getEntityIdLength());
        
        // Places
        cbUseSpace.setSelected(gedcomOptions.isUseSpacedPlaces());
        cbSplitJuridictions.setSelected(CoreOptions.getInstance().isSplitJurisdictions());

        // Encoding
        setEncoding(gedcomPrefs.get("defaultEncoding", ""));
        cbSaveEncoding.setSelected(ancestris.app.AppOptions.isWriteBOM());
        cbSaveEncoding.setVisible(false); // 2020-09-05 : this parameter no longer seems to be used anywhere in the code. Hide for now.

        // Editing
        cboxDefaultEditor.setSelectedItem(getEditorFromCanonicalName(gedcomOptions.getDefaultEditor()));
        cbAutoCommit.setSelected(ConfirmChangeWidget.getAutoCommit());
        nbCancellations.setValue(gedcomOptions.getNumberOfUndos());
        cbCreateSpouse.setSelected(gedcomOptions.getCreateSpouse());
        cbAddAge.setSelected(gedcomOptions.isAddAge());
        cbDetectDuplicate.setVisible(false); // do not use for the moment
        cbDetectDuplicate.setSelected(gedcomOptions.getDetectDuplicate());
        cbDuplicateEachTime.setSelected(gedcomOptions.getDuplicateAnyTime());
    }

    void store() {
        // Names
        gedcomOptions.setUpperCaseNames(cbNamesInUppercase.isSelected());
        gedcomOptions.setGivenTag(cbGivenName.isSelected()?jtGivenTag.getText().trim():"");
        gedcomPrefs.put("setWifeLastname", getNamesSpouse());

        // ID Numbers
        gedcomPrefs.put("isFillGapsInIDs", getIdFilling());
        gedcomOptions.setEntityIdLength(Integer.valueOf(idLength.getValue().toString()));

        // Places
        if (cbUseSpace.isSelected() != gedcomOptions.isUseSpacedPlaces()){
            Lifecycle.askForRestart();
            gedcomOptions.setUseSpacedPlaces(cbUseSpace.isSelected());
        }
        CoreOptions.getInstance().setSplitJurisdictions(cbSplitJuridictions.isSelected());

        // Encoding
        gedcomPrefs.put("defaultEncoding", getEncoding());
        ancestris.app.AppOptions.setWriteBOM(cbSaveEncoding.isSelected());

        // Editing
        gedcomOptions.setDefaultEditor(((AncestrisEditor)cboxDefaultEditor.getSelectedItem()).getName(true));
        ConfirmChangeWidget.setAutoCommit(cbAutoCommit.isSelected());
        gedcomOptions.setNumberOfUndos((Integer) (nbCancellations.getValue()));
        gedcomOptions.setCreateSpouse(cbCreateSpouse.isSelected());
        gedcomOptions.setAddAge(cbAddAge.isSelected());
        gedcomOptions.setDetectDuplicate(cbDetectDuplicate.isSelected());
        gedcomOptions.setDuplicateAnyTime(cbDuplicateEachTime.isSelected());
        
        // Display to user
        StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(OptionDataPanel.class, "OptionPanel.saved.statustext"));
    }

    public boolean valid() {
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel IDPanel;
    private javax.swing.JCheckBox cbAddAge;
    private javax.swing.JCheckBox cbAutoCommit;
    private javax.swing.JCheckBox cbCreateSpouse;
    private javax.swing.JCheckBox cbDetectDuplicate;
    private javax.swing.JCheckBox cbDuplicateEachTime;
    private javax.swing.JCheckBox cbGivenName;
    private javax.swing.JCheckBox cbNamesInUppercase;
    private javax.swing.JCheckBox cbReuseIDs;
    private javax.swing.JCheckBox cbSameSpouseName;
    private javax.swing.JCheckBox cbSaveEncoding;
    private javax.swing.JCheckBox cbSplitJuridictions;
    private javax.swing.JCheckBox cbUseSpace;
    private javax.swing.JComboBox cboxDefaultEditor;
    private javax.swing.JComboBox cboxEncoding;
    private javax.swing.JPanel editingPanel;
    private javax.swing.JPanel encodingPanel;
    private javax.swing.JSpinner idLength;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jtGivenTag;
    private javax.swing.JLabel lCancellations;
    private javax.swing.JLabel lDefaultEditor;
    private javax.swing.JLabel lDefaultIDLength;
    private javax.swing.JLabel lFileEncoding;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel namesPanel;
    private javax.swing.JSpinner nbCancellations;
    private javax.swing.JPanel placePanel;
    // End of variables declaration//GEN-END:variables


    void setNamesSpouse(String str) {
        cbSameSpouseName.setSelected(str.equals("true") ? true : false);
    }

    String getNamesSpouse() {
        return cbSameSpouseName.isSelected() ? "true" : "false";
    }

    void setIDFilling(String str) {
        if (str.equals("")) {
            str = "true";
        }
        cbReuseIDs.setSelected(str.equals("true") ? true : false);
    }

    String getIdFilling() {
        return cbReuseIDs.isSelected() ? "true" : "false";
    }

    void setEncoding(String str) {
        if (str.equals("-1")) {
            str = "0";
        }
        Integer i = getIntFromStr(str);
        if (i == -1) {
            i = 0;
        }
        if (i > encodings.length) {
            i = encodings.length;
        }
        cboxEncoding.setSelectedIndex(i);
    }

    String getEncoding() {
        return cboxEncoding.getSelectedIndex() + "";
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

    private AncestrisEditor getEditorFromCanonicalName(String defaultEditor) {
        for (AncestrisEditor edt : editors) {
            if (edt.getName(true).startsWith(defaultEditor)) {
                return edt;
            }
        }
        return editors != null ? editors.get(0) : null;
    }


}
