/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.tools.datu;

import genj.gedcom.GedcomException;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;
import genj.util.swing.DateWidget;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//genjfr.tools.datu//Datu//EN",
autostore = false)
public final class DatuTopComponent extends TopComponent {

    private static DatuTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "genjfr/tools/datu/Calc.png";
    private static final String PREFERRED_ID = "DatuTopComponent";
    private DateWidget dw1 = new DateWidget();
    private boolean updateInProgress = false;
    private Calendar from = PointInTime.GREGORIAN;
    private Calendar to = PointInTime.FRENCHR;
    private int gap = 0; // Valeur du gap en années
    private int incertitude = 0; // Valeur de l'incertitude en mois

    public DatuTopComponent() {
        initComponents();
        jSliderGap.setMajorTickSpacing(50);
        jSliderGap.setMinorTickSpacing(10);
        jSliderGap.setPaintTicks(true);
        jSliderGap.setPaintLabels(true);
        jComboBox1.setSelectedItem(from);
        jComboBox2.setSelectedItem(to);
        Border PanelBorder = jPanel5.getBorder();
        ((TitledBorder) PanelBorder).setTitle(from.toString());

        setName(NbBundle.getMessage(DatuTopComponent.class, "CTL_DatuTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        dw1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (updateInProgress == false) {
                    updateInProgress = true;
                    PointInTime pit = dw1.getValue();
                    if (pit != null) {
                        from = pit.getCalendar();
                        jComboBox1.setSelectedItem(from);
                        Border PanelBorder = jPanel5.getBorder();
                        ((TitledBorder) PanelBorder).setTitle(from.toString());
                        if (pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                            jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
                        } else if (pit.getCalendar().equals(PointInTime.FRENCHR)) {
                            jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.frenchR.toolTipText")); // NOI18N
                        } else {
                            jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.others.toolTipText")); // NOI18N
                        }
                        jPanel5.repaint();
                        update();
                    }
                    updateInProgress = false;
                }
            }
        });
    }

    public void update() {
        if (dw1.getValue() != null) {
            PointInTime Pit = dw1.getValue();
            if (Pit.isComplete()) {
                jDisplay1.setText(calcule(Pit));
            }
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
            result = pit.toString(new WordBuffer(), PointInTime.FORMAT_LONG).toString();
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
            result = String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.FromTo"), pitMini.toString(new WordBuffer(), PointInTime.FORMAT_LONG), pitMaxi.toString(new WordBuffer(), PointInTime.FORMAT_LONG));
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
        jPanel1 = new javax.swing.JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Image image = arvernes_logo_small.getImage();
                int x = (this.getWidth() - image.getWidth(null)) / 2;
                int y = (this.getHeight() - image.getHeight(null)) / 2;
                g2d.drawImage(image,x, 40, null);
                super.paintComponent(g);
            }
        };
        jToolBar1 = new javax.swing.JToolBar();
        jComboBox1 = new javax.swing.JComboBox(PointInTime.CALENDARS);
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox(PointInTime.CALENDARS);
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
        jDisplay1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel1.setMaximumSize(null);
        jPanel1.setOpaque(false);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(PointInTime.CALENDARS));
        jComboBox1.addActionListener(formListener);
        jToolBar1.add(jComboBox1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel3.text")); // NOI18N
        jToolBar1.add(jLabel3);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(PointInTime.CALENDARS));
        jComboBox2.addActionListener(formListener);
        jToolBar1.add(jComboBox2);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(196, 93));

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add( Box.createHorizontalGlue() );
        jPanel2.add( dw1 );
        jPanel2.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel3.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.toolTipText")); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        jSliderGap.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        jSliderGap.setMinimum(-100);
        jSliderGap.setValue(0);
        jSliderGap.setOpaque(false);
        jSliderGap.addChangeListener(formListener);
        jPanel3.add(jSliderGap);

        jTextFieldGap.setColumns(9);
        jTextFieldGap.setEditable(false);
        jTextFieldGap.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTextFieldGap.setForeground(java.awt.Color.lightGray);
        jTextFieldGap.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldGap.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap.text")); // NOI18N
        jTextFieldGap.setBorder(null);
        jTextFieldGap.setOpaque(false);
        jPanel3.add(jTextFieldGap);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel7.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel7.toolTipText")); // NOI18N
        jPanel7.setOpaque(false);
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel5.text")); // NOI18N
        jPanel7.add(jLabel5);

        jTextFieldGap1.setColumns(2);
        jTextFieldGap1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTextFieldGap1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldGap1.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap1.text")); // NOI18N
        jTextFieldGap1.setBorder(null);
        jTextFieldGap1.setOpaque(false);
        jTextFieldGap1.addKeyListener(formListener);
        jPanel7.add(jTextFieldGap1);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.month")); // NOI18N
        jPanel7.add(jLabel4);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.result.toolTipText")); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(196, 93));

        jDisplay1.setEditable(false);
        jDisplay1.setFont(new java.awt.Font("Dialog", 1, 14));
        jDisplay1.setForeground(java.awt.Color.red);
        jDisplay1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jDisplay1.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jDisplay1.text")); // NOI18N
        jDisplay1.setOpaque(false);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDisplay1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDisplay1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genjfr/tools/datu/information.png"))); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.setBorder(null);
        jButton1.setOpaque(false);
        jButton1.addActionListener(formListener);

        jPanel4.setOpaque(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24));
        jLabel1.setForeground(new java.awt.Color(51, 51, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24));
        jLabel2.setForeground(new java.awt.Color(51, 51, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(jButton1)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addContainerGap())))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
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
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == jTextFieldGap1) {
                DatuTopComponent.this.jTextFieldGap1KeyTyped(evt);
            }
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
            if (gap == 1) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.year"), gap);
            } else if (gap != 0) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.years"), gap);
            }
            jTextFieldGap.setText(texte);
            update();
        }
    }//GEN-LAST:event_jSliderGapStateChanged

    private void jTextFieldGap1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldGap1KeyTyped
        String text = jTextFieldGap1.getText();
        try {
            incertitude = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            jTextFieldGap1.setText("0");
            incertitude = 0;
        }

        if (incertitude == 1) {
            jLabel4.setText(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.month"));
        } else if (gap != 0) {
            jLabel4.setText(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.months"));
        }
        update();
    }//GEN-LAST:event_jTextFieldGap1KeyTyped

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        PointInTime pit = dw1.getValue();

        if (pit != null) {
            from = (Calendar) jComboBox1.getSelectedItem();
            try {
                pit.set(from);
                Border PanelBorder = jPanel5.getBorder();
                ((TitledBorder) PanelBorder).setTitle(from.toString());
                if (updateInProgress == false) {
                    dw1.setValue(pit);
                }
            } catch (GedcomException ex) {
                jComboBox1.setSelectedItem(pit.getCalendar());
            }
            update();
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        to = (Calendar) jComboBox2.getSelectedItem();
        update();
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JOptionPane.showMessageDialog(this, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.authors.text"));
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JTextField jDisplay1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderGap;
    private javax.swing.JTextField jTextFieldGap;
    private javax.swing.JTextField jTextFieldGap1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
    private javax.swing.ImageIcon arvernes_logo_small = new javax.swing.ImageIcon(getClass().getResource("/genjfr/tools/datu/arvernes_logo_small.gif")); // NOI18N

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
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
