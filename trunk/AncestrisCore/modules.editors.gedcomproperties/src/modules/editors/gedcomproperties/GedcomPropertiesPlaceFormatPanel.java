/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties;

import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.Registry;
import java.awt.Dimension;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import modules.editors.gedcomproperties.utils.PlaceFormatInterface;
import org.openide.util.NbBundle;

public final class GedcomPropertiesPlaceFormatPanel extends JPanel implements Constants {

    private Registry registry = null;
    private final String winWidth = "gedcomProperties4Width";
    private final String winHeight = "gedcomProperties4Height";
    
    public static final int DEFAULT_MODE = UPDATE;

    private final PlaceFormatInterface parent;
    private int mode;
    private Gedcom gedcom;

    private final DefaultListModel<String> listModel = new DefaultListModel<String>();

    private Set<String> incorrectList = null;
    private int nbExpectedPlaces = 0;
    private String EMPTY_PLACE = "";

    /**
     * Creates new form GedcomPropertiesPlaceFormatPanel
     */
    public GedcomPropertiesPlaceFormatPanel(PlaceFormatInterface parent) {
        this.parent = parent;
        mode = parent.getMode();
        gedcom = parent.getGedcom();
        EMPTY_PLACE = NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.emptyPlace");
        initComponents();

        registry = Registry.get(getClass());
        this.setPreferredSize(new Dimension(registry.get(winWidth, this.getPreferredSize().width), registry.get(winHeight, this.getPreferredSize().height)));

        updateDisplay();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "STEP_4_name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(32767, 2));
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton5 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBorder(null);
        setPreferredSize(new java.awt.Dimension(500, 375));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 375));

        jPanel1.setBorder(null);
        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(500, 375));

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jLabel4.text")); // NOI18N

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.jLabel3.update"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.jLabel5.update"));

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/List.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton8, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton8.text")); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox2.text")); // NOI18N
        jCheckBox2.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox2.toolTipText")); // NOI18N

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, mode == CREATION ? "Panel4.jLabel1.create" : "Panel4.jLabel1.update"));

        jPanel2.setPreferredSize(new java.awt.Dimension(493, 210));

        jList1.setModel(listModel);
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jList1.toolTipText")); // NOI18N
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/MoveDown.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton4.text")); // NOI18N
        jButton4.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton4.toolTipText")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/Bank.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton6.text")); // NOI18N
        jButton6.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton6.toolTipText")); // NOI18N
        jButton6.setPreferredSize(new java.awt.Dimension(28, 28));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/Reset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton7, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton7.text")); // NOI18N
        jButton7.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton7.toolTipText")); // NOI18N
        jButton7.setPreferredSize(new java.awt.Dimension(28, 28));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jTextField1.setText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jTextField1.toolTipText")); // NOI18N
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/Add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton1.toolTipText")); // NOI18N
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/Delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton2.toolTipText")); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(28, 28));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/MoveUp.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton3.toolTipText")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 163, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton4)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12))
        );

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, mode == CREATION ? "Panel4.jLabel2.create" : "Panel4.jLabel2.update"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modules/editors/gedcomproperties/ressources/Mapping.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton5, org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton5.text")); // NOI18N
        jButton5.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jButton5.toolTipText")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox2))
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane4)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5))
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jCheckBox2)
                    .addComponent(jButton8))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(filler1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCheckBox1)
                    .addComponent(jButton5)
                    .addComponent(jLabel2))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        String juris = jTextField1.getText();
        jButton1.setEnabled(juris != null && !juris.isEmpty() && !listModel.contains(juris));
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        String defaultFormat = GedcomOptions.getInstance().getPlaceFormat();
        String str = parent.getOriginalPlaceFormat();
        setPLAC(!str.isEmpty() ? str : defaultFormat);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        addNewJurisdiction(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.emptyField"));
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        moveDownSelectedIndex();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        moveUpSelectedIndex();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        removeSelectedIndex();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addNewJurisdiction(jTextField1.getText());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        updateDisplay(jList1.getSelectedIndex());
    }//GEN-LAST:event_jList1ValueChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        jButton5.setEnabled(jCheckBox1.isSelected());
        if (jCheckBox1.isSelected() && parent.getPlaceFormatConverter() == null) {
            parent.setPlaceFormatConverter(new PlaceFormatConverterPanel(parent.getOriginalPlaceFormat(), getPLAC(), null));
            DisplayPlaceFormatConverter(parent.getPlaceFormatConverter());
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (parent.getPlaceFormatConverter() == null) {
            parent.setPlaceFormatConverter(new PlaceFormatConverterPanel(parent.getOriginalPlaceFormat(), getPLAC(), null));
        }
        DisplayPlaceFormatConverter(parent.getPlaceFormatConverter());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put(winWidth, evt.getComponent().getWidth());
        registry.put(winHeight, evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        DisplayIncorrectPlaces(incorrectList);
    }//GEN-LAST:event_jButton8ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    public void setPLAC(String str) {
        if (!str.equals(getPLAC())) {
            parent.setPlaceFormatConverter(null);
        }
        String[] placeFormatList = PropertyPlace.getFormat(str);
        listModel.clear();
        for (String p : placeFormatList) {
            if (p.isEmpty()) {
                listModel.addElement(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.emptyField"));
            } else {
                listModel.addElement(p);
            }
        }
        updateDisplay();
    }

    public String getPLAC() {
        String ret = "";
        String tmpStr = "";
        String addStr = PropertyPlace.JURISDICTION_SEPARATOR;
        for (int i = 0; i < listModel.getSize(); i++) {
            if (i == listModel.getSize()-1) {
                addStr = "";
            } 
            tmpStr = listModel.elementAt(i);
            if (tmpStr.equals(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.emptyField"))) {
                tmpStr = "";
            }
            ret += tmpStr + addStr;
        }
        return ret;
    }

    public boolean getConversionToBeDone() {
        return jCheckBox1.isSelected();
    }

    public boolean getPlacesConversionToBeDone() {
        return jCheckBox2.isSelected();
    }

    private void removeSelectedIndex() {
        int index = jList1.getSelectedIndex();
        listModel.remove(index);
        int size = listModel.getSize();
        if (size != 0 && index == size) { //Select a new index
                //removed item in last position
                index--;
            }
        updateDisplay(index);
    }

    private void addNewJurisdiction(String text) {
        int index = jList1.getSelectedIndex();
        if (index == -1) { //no selection, so insert at beginning
            index = 0;
        } else {           //add after the selected item
            index++;
        }
        listModel.insertElementAt(text, index);
        updateDisplay(index);

        //Reset the text field.
        jTextField1.requestFocusInWindow();
        jTextField1.setText("");
        jButton1.setEnabled(false);
    }

    private void moveUpSelectedIndex() {
        int index = jList1.getSelectedIndex();
        String juris = listModel.remove(index);
        listModel.add(index-1, juris);
        updateDisplay(index-1);
    }

    private void moveDownSelectedIndex() {
        int index = jList1.getSelectedIndex();
        String juris = listModel.remove(index);
        listModel.add(index+1, juris);
        updateDisplay(index+1);
    }

    private void updateDisplay() {
        updateDisplay(listModel.isEmpty() ? -1 : 0);
    }
    
    private void updateDisplay(int index) {
        boolean listIsEmpty = listModel.isEmpty();
        boolean indexIsFirst = (index == 0);
        boolean indexIsLast = (index == listModel.getSize() - 1);
        
        // regular fields
        jButton2.setEnabled(!listIsEmpty);
        jButton3.setEnabled(!listIsEmpty && !indexIsFirst);
        jButton4.setEnabled(!listIsEmpty && !indexIsLast);
        if (!listIsEmpty) {
            jList1.setSelectedIndex(index);
            jList1.ensureIndexIsVisible(index);
        }
        jTextArea1.setText(getPLAC());
        jTextArea2.setText(parent.getOriginalPlaceFormat());
        
        // Fields related to place conversion
        boolean canBeConverted = (mode == UPDATE) && !getPLAC().equals(parent.getOriginalPlaceFormat());   // true if place format has changed
        jCheckBox1.setVisible(canBeConverted);
        jButton5.setVisible(canBeConverted);
        jButton5.setEnabled(jCheckBox1.isSelected());
        jLabel4.setVisible(mode == UPDATE);
        jScrollPane4.setVisible(mode == UPDATE);
        parent.warnVersionChange(canBeConverted);
        if (canBeConverted && parent.getPlaceFormatConverter() != null) {
            parent.getPlaceFormatConverter().setValidatedMap(false, getPLAC());
        }
        
        // Place details
        jLabel3.setVisible(mode == UPDATE);
        jLabel5.setVisible(mode == UPDATE);
        Set<String> keys = getNbOfDifferentPlaces(gedcom);
        String nbOfDifferentFoundPlaces = "" + keys.size();
        jLabel3.setText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.jLabel3.update", nbOfDifferentFoundPlaces));
        nbExpectedPlaces = PropertyPlace.getFormat(parent.getOriginalPlaceFormat()).length;
        incorrectList = getIncorrectPlaces(nbExpectedPlaces, keys);
        if (incorrectList.isEmpty()) {
            jLabel5.setText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.jLabel5.update"));
            jCheckBox2.setVisible(false);
            jButton8.setVisible(false);
        } else {
            jLabel5.setText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "Panel4.jLabel5.update.incorrect", incorrectList.size(), nbExpectedPlaces));
            jCheckBox2.setToolTipText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox2.toolTipText", nbExpectedPlaces));
            jCheckBox2.setVisible(mode == UPDATE);
            jButton8.setVisible(mode == UPDATE);
        }
        
    }

    private void DisplayPlaceFormatConverter(PlaceFormatConverterPanel placeFormatConverter) {
        parent.getPlaceFormatConverter().initToFields(getPLAC());
        Object o = DialogManager.create(NbBundle.getMessage(PlaceFormatConverterPanel.class, "TITL_PlaceFormatConversionSettings"), placeFormatConverter).setMessageType(DialogManager.PLAIN_MESSAGE).show();
        if (o == DialogManager.OK_OPTION) {
            placeFormatConverter.setValidatedMap(true, null);
        } else {
            placeFormatConverter.setValidatedMap(false, null);
        }
    }

    private Set<String> getNbOfDifferentPlaces(Gedcom gedcom) {
        Set<String> ret = new TreeSet<String>();
        for (Property place : gedcom.getPropertiesByClass(PropertyPlace.class)) {
            ret.add(place.getValue());
        }
        return ret;
    }

    private Set<String> getIncorrectPlaces(int nbExpected, Set<String> places) {
        Set<String> ret = new TreeSet<String>();
        for (String place : places) {
            if (place.isEmpty()) {
                ret.add(EMPTY_PLACE);
                continue;
            }
            if (PropertyPlace.getFormat(place).length != nbExpected) {
                ret.add(place);
            }
        }
        return ret;
    }

    private void DisplayIncorrectPlaces(Set<String> list) {
        JScrollPane panel = new JScrollPane(new JList(list.toArray()));
        panel.setPreferredSize(new Dimension(Math.max(panel.getPreferredSize().width, 400), 300));
        DialogManager.create(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "TITL_IncorrectList", list.size(), nbExpectedPlaces), panel)
                .setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
    }


}
