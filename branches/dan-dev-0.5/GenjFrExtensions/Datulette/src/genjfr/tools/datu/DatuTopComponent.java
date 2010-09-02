/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.tools.datu;

import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.util.swing.DateWidget;
import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.StatusDisplayer;

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

        private PointInTime pitInitial;
        private PointInTime pitComputed;
        private PointInTime pitMini, pitMaxi;
        private DateWidget dw = new DateWidget();
        private PropertyDate exemple = new PropertyDate();

        private int gap = 0; // Valeur du gap
        private String signe = " + ";

        private boolean before = true;

        private int precision = 0; // Valeur absolue
        private int pourcent;
        private boolean percent = true;

    public DatuTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DatuTopComponent.class, "CTL_DatuTopComponent"));
        setToolTipText(NbBundle.getMessage(DatuTopComponent.class, "HINT_DatuTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        dw.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                calcule ();
            }
        });

        pitInitial = PointInTime.getNow();
        dw.setValue(pitInitial);
        calcule ();
    }

    public String getDisplayValue () {
        return exemple.getDisplayValue();
    }

    public PropertyDate getPropertyDate () {
        return exemple;
    }

    private void calcule () {
        // Date de départ
        if (dw.getValue() != null) {
            pitInitial = dw.getValue();
            if (gap == 0) {
                pitComputed = pitInitial;
            } else {
                pitComputed = new PointInTime(  pitInitial.getDay(),
                                                pitInitial.getMonth(),
                                                pitInitial.getYear() + gap,
                                                pitInitial.getCalendar());
            }
        }

        // Pit limits
        if (precision == 0) {
            pitMini = pitComputed;
            pitMaxi = pitComputed;
        } else {
            pitMini = new PointInTime(  pitComputed.getDay(),
                                        pitComputed.getMonth(),
                                        pitComputed.getYear() - precision,
                                        pitComputed.getCalendar());
            pitMaxi = new PointInTime(  pitComputed.getDay(),
                                        pitComputed.getMonth(),
                                        pitComputed.getYear() + precision,
                                        pitComputed.getCalendar());
        }

        affiche ();
    }

    // Traite les champs textes
    private void affiche() {
        String texte = pitInitial.getValue();
        signe = gap >= 0  ? " + " : " - ";
        if (Math.abs(gap) == 1) {
            texte += signe + String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.year"), Math.abs(gap));
        } else if (gap != 0) {
            texte += signe + String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.years"), Math.abs(gap));
        }
        jTextFieldGap.setText(""+gap);
        
        String precis = "";
        if (percent) {
            if (pourcent > 0) {
                precis = String.format("+/- %d %%", pourcent, percent);
            }
        } else {
            if (precision == 1) {
                precis = "+/- " + String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.year"), precision);
            } else if (precision > 1) {
                precis = "+/- " + String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.years"), precision);
            }
        }
        jLabelPrecis.setText(precis);

        texte += " "+precis;

        // Afficher une explication
        StatusDisplayer.getDefault().setStatusText(texte.trim());
        
        if (pitMini.compareTo(pitMaxi) != 0) {
            exemple.setValue(PropertyDate.FROM_TO, pitMini, pitMaxi,texte);
        } else {
            exemple.setValue(PropertyDate.DATE,pitComputed,null,texte);
        }
        jDisplay.setText(exemple.getDisplayValue());
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupGap = new javax.swing.ButtonGroup();
        buttonGroupAccuracy = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jTextFieldGap = new javax.swing.JTextField();
        jSliderGap = new javax.swing.JSlider();
        jRadioEarlier = new javax.swing.JRadioButton();
        jRadioLater = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jDisplay = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jSliderAccuracy = new javax.swing.JSlider();
        jRadioPercent = new javax.swing.JRadioButton();
        jRadioAbs = new javax.swing.JRadioButton();
        jLabelPrecis = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel2.border.title"))); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add( Box.createHorizontalGlue() );
        jPanel2.add( dw );
        jPanel2.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.border.title"))); // NOI18N

        jTextFieldGap.setColumns(4);
        jTextFieldGap.setEditable(false);
        jTextFieldGap.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldGap.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap.text")); // NOI18N
        jTextFieldGap.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap.toolTipText")); // NOI18N

        jSliderGap.setValue(0);
        jSliderGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderGapStateChanged(evt);
            }
        });

        buttonGroupGap.add(jRadioEarlier);
        jRadioEarlier.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioEarlier, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jRadioEarlier.text")); // NOI18N
        jRadioEarlier.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jRadioEarlier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioEarlierActionPerformed(evt);
            }
        });

        buttonGroupGap.add(jRadioLater);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioLater, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jRadioLater.text")); // NOI18N
        jRadioLater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioEarlierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jRadioEarlier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioLater)
                .addGap(31, 31, 31))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jSliderGap, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jSliderGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioEarlier)
                    .addComponent(jTextFieldGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioLater))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel5.setBorder(null);

        jDisplay.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jDisplay.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jDisplay.text_1")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDisplay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel4.border.title"))); // NOI18N

        jSliderAccuracy.setMaximum(99);
        jSliderAccuracy.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jSliderAccuracy.toolTipText")); // NOI18N
        jSliderAccuracy.setValue(0);
        jSliderAccuracy.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderAccuracyStateChanged(evt);
            }
        });

        buttonGroupAccuracy.add(jRadioPercent);
        jRadioPercent.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioPercent, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jRadioPercent.text")); // NOI18N
        jRadioPercent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioPercentActionPerformed(evt);
            }
        });

        buttonGroupAccuracy.add(jRadioAbs);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioAbs, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jRadioAbs.text")); // NOI18N
        jRadioAbs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioPercentActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrecis, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabelPrecis.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioAbs)
                    .addComponent(jRadioPercent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelPrecis, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
            .addComponent(jSliderAccuracy, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabelPrecis)
                        .addGap(14, 14, 14))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jSliderAccuracy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioPercent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioAbs)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderGapStateChanged
        if (! jSliderGap.getValueIsAdjusting()) {
            calculeGap();
        }
    }//GEN-LAST:event_jSliderGapStateChanged
    private void calculeGap() {
            // Gap absolute value
            gap = jSliderGap.getValue();

            // Sens du gap
            if (before) gap *= -1;

            // La précision peut dépendre du gap
            calculePrecision();
    }

    private void jSliderAccuracyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderAccuracyStateChanged
        if (! jSliderAccuracy.getValueIsAdjusting()) {
            calculePrecision();
        }
    }//GEN-LAST:event_jSliderAccuracyStateChanged

    private void calculePrecision() {
        // Calcul de la précision : valeur absolue ou pourcentage
        pourcent = jSliderAccuracy.getValue();
        if (percent) {
            precision = Math.abs(gap) * pourcent / 100;
        } else {
            precision = pourcent;
        }

        calcule();
    }

    // Appelé par earlier et later
    private void jRadioEarlierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioEarlierActionPerformed
        before = jRadioEarlier.isSelected();
        calculeGap();
    }//GEN-LAST:event_jRadioEarlierActionPerformed

    // Appelé par pourcentage et valeur absolue
    private void jRadioPercentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioPercentActionPerformed
        percent = jRadioPercent.isSelected();
        calculePrecision();
    }//GEN-LAST:event_jRadioPercentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupAccuracy;
    private javax.swing.ButtonGroup buttonGroupGap;
    private javax.swing.JTextField jDisplay;
    private javax.swing.JLabel jLabelPrecis;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioAbs;
    private javax.swing.JRadioButton jRadioEarlier;
    private javax.swing.JRadioButton jRadioLater;
    private javax.swing.JRadioButton jRadioPercent;
    private javax.swing.JSlider jSliderAccuracy;
    private javax.swing.JSlider jSliderGap;
    private javax.swing.JTextField jTextFieldGap;
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
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }


}
