/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.datulette;

import ancestris.util.Utilities;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.io.FileAssociation;
import genj.util.WordBuffer;
import genj.util.swing.DateWidget;
import java.awt.Dimension;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;

/**
 * Top component which displays something.
 */
public final class DatuTopComponent extends TopComponent {

    private static DatuTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "ancestris/modules/datulette/datulette.png";
    private static final String PREFERRED_ID = "DatuTopComponent";
    private DateWidget dw1 = new DateWidget();
    private boolean updateInProgress = false;
    private Calendar from = PointInTime.GREGORIAN;
    private Calendar to = PointInTime.FRENCHR;
    private int gap = 0; // Valeur du gap en années
    private int incertitude = 0; // Valeur de l'incertitude en mois

    public DatuTopComponent() {
        initComponents();
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        
        jSliderGap.setMajorTickSpacing(50);
        jSliderGap.setMinorTickSpacing(10);
        jSliderGap.setPaintTicks(true);
        jSliderGap.setPaintLabels(true);
        jSliderGap.setValue(gap);
        jTextFieldGap.setText(String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.year"), gap));
        jComboBox1.setSelectedItem(from);
        jComboBox2.setSelectedItem(to);
        ((TitledBorder) jPanel5.getBorder()).setTitle("");

        setName(NbBundle.getMessage(DatuTopComponent.class, "CTL_DatuTopComponent"));
        setToolTipText(NbBundle.getMessage(DatuTopComponent.class, "HINT_DatuTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        dw1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (updateInProgress == false) {
                    updateInProgress = true;
                    updateTooltip();
                    if (dw1.getCalendar() != null) {
                        update();
                    }
                    updateInProgress = false;
                }
            }
        });
    }

    public void update() {
        try {
            incertitude = Integer.parseInt(jTextFieldGap1.getText());
        } catch (NumberFormatException ex) {
            incertitude = 0;
        }

        if (incertitude == 1) {
            jLabel4.setText(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.month"));
        } else if (gap != 0) {
            jLabel4.setText(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.months"));
        }

        if (dw1.getValue() != null) {
            PointInTime Pit = dw1.getValue();
            if (Pit.isComplete()) {
                jLabel1.setText(calcule(Pit));
            }
        }
    }

    private void updateTooltip() {
        Calendar cal = dw1.getCalendar();
        if (cal != null) {
            jComboBox1.setSelectedItem(cal);
            if (cal.equals(PointInTime.GREGORIAN)) {
                jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
            } else if (cal.equals(PointInTime.FRENCHR)) {
                jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.frenchR.toolTipText")); // NOI18N
            } else {
                jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.others.toolTipText")); // NOI18N
            }
            jPanel5.repaint();
        }
    }

    private String calcule(PointInTime pitInitial) {
        String result = null;

        if (incertitude == 0) {
            PointInTime pit = new PointInTime();
            pit.set(pitInitial);

            // Convert to greprian calendar
            try {
                pit.set(PointInTime.GREGORIAN);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            // add Gap
            pit.add(0, 0, gap);

            // Convert to requested calendar
            try {
                pit.set(to);
            } catch (GedcomException ex) {
            }
            result = pit.toString(new WordBuffer(), GedcomOptions.GedcomDateFormat.LONG).toString();
        } else {
            // Pit limits
            PointInTime pitMini = new PointInTime();
            pitMini.set(pitInitial);

            // Convert to greprian calendar
            try {
                pitMini.set(PointInTime.GREGORIAN);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            // add gap years and delta month
            pitMini.add(0, incertitude * -1, gap);


            // Convert to requested calendar
            try {
                pitMini.set(to);
            } catch (GedcomException ex) {
            }
            PointInTime pitMaxi = new PointInTime();
            pitMaxi.set(pitInitial);

            // Convert to greprian calendar
            try {
                pitMaxi.set(PointInTime.GREGORIAN);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            // add gap years and delta month
            pitMaxi.add(0, incertitude, gap);


            // Convert to requested calendar
            try {
                pitMaxi.set(to);
            } catch (GedcomException ex) {
            }
            result = String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.FromTo"), 
                    pitMini.toString(new WordBuffer(), GedcomOptions.GedcomDateFormat.LONG), 
                    pitMaxi.toString(new WordBuffer(), GedcomOptions.GedcomDateFormat.LONG));
        }

        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jSliderGap = new javax.swing.JSlider();
        jTextFieldGap = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldGap1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<Calendar>(PointInTime.CALENDARS);
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<Calendar>(PointInTime.CALENDARS);
        jButton1 = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setPreferredSize(new java.awt.Dimension(300, 430));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 430));
        jScrollPane1.setRequestFocusEnabled(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel1.setMaximumSize(null);
        jPanel1.setOpaque(false);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Bitstream Vera Sans", 1, 12), java.awt.Color.black)); // NOI18N
        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
        jPanel5.setOpaque(false);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add( Box.createHorizontalGlue() );
        jPanel2.add( dw1 );
        jPanel2.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );
        jPanel2.setSize(jPanel5.getPreferredSize().width, dw1.getPreferredSize().height+10);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Bitstream Vera Sans", 1, 12), java.awt.Color.black)); // NOI18N
        jPanel3.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.toolTipText")); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));

        jSliderGap.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        jSliderGap.setMinimum(-100);
        jSliderGap.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jSliderGap.toolTipText")); // NOI18N
        jSliderGap.setValue(0);
        jSliderGap.addChangeListener(formListener);

        jTextFieldGap.setEditable(false);
        jTextFieldGap.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        jTextFieldGap.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldGap.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jSliderGap, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jSliderGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Bitstream Vera Sans", 1, 12), java.awt.Color.black)); // NOI18N
        jPanel7.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel7.toolTipText")); // NOI18N
        jPanel7.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel5.text")); // NOI18N

        jTextFieldGap1.setColumns(2);
        jTextFieldGap1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTextFieldGap1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldGap1.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap1.text")); // NOI18N
        jTextFieldGap1.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap1.toolTipText")); // NOI18N
        jTextFieldGap1.addKeyListener(formListener);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.month")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldGap1, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(0, 0, 0))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldGap1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel4)
                .addComponent(jLabel5))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Bitstream Vera Sans", 1, 12), java.awt.Color.black)); // NOI18N
        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.result.toolTipText")); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(196, 93));

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<Calendar>(PointInTime.CALENDARS));
        jComboBox1.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jComboBox1.toolTipText")); // NOI18N
        jComboBox1.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel3.text")); // NOI18N

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<Calendar>(PointInTime.CALENDARS));
        jComboBox2.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jComboBox2.toolTipText")); // NOI18N
        jComboBox2.addActionListener(formListener);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/datulette/FrenchR.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(formListener);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.KeyListener, javax.swing.event.ChangeListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jComboBox1) {
                DatuTopComponent.this.jComboBox1ActionPerformed(evt);
            }
            else if (evt.getSource() == jComboBox2) {
                DatuTopComponent.this.jComboBox2ActionPerformed(evt);
            }
            else if (evt.getSource() == jButton1) {
                DatuTopComponent.this.jButton1ActionPerformed(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == jTextFieldGap1) {
                DatuTopComponent.this.jTextFieldGap1KeyReleased(evt);
            }
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
        }

        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            if (evt.getSource() == jSliderGap) {
                DatuTopComponent.this.jSliderGapStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderGapStateChanged
        if (!jSliderGap.getValueIsAdjusting()) {
            // Gap  value
            gap = jSliderGap.getValue();
            String texte = "";
            if (gap == 1 || gap == 0) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.year"), gap);
            } else if (gap != 0) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.years"), gap);
            }
            jTextFieldGap.setText(texte);
            update();
        }
    }//GEN-LAST:event_jSliderGapStateChanged

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        to = (Calendar) jComboBox2.getSelectedItem();
        update();
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        PointInTime pit = dw1.getValue();

        if (pit != null) {
            from = (Calendar) jComboBox1.getSelectedItem();
            try {
                pit.set(from);
                //Border PanelBorder = jPanel5.getBorder();
                //((TitledBorder) PanelBorder).setTitle(from.toString());
                if (updateInProgress == false) {
                    dw1.setValue(pit);
                }
            } catch (GedcomException ex) {
                jComboBox1.setSelectedItem(pit.getCalendar());
            }
            update();
        }
        updateTooltip();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextFieldGap1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldGap1KeyReleased
        update();
    }//GEN-LAST:event_jTextFieldGap1KeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String filename = "republicain.jpg";
            File file = Utilities.getResourceAsFile(getClass(), filename, filename);
            FileAssociation.getDefault().execute(file.getAbsolutePath());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<Calendar> jComboBox1;
    private javax.swing.JComboBox<Calendar> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderGap;
    private javax.swing.JTextField jTextFieldGap;
    private javax.swing.JTextField jTextFieldGap1;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DatuTopComponent getDefault() {
        if (instance == null) {
            instance = new DatuTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DatuTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DatuTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DatuTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DatuTopComponent) {
            return (DatuTopComponent) win;
        }
        Logger.getLogger(DatuTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
