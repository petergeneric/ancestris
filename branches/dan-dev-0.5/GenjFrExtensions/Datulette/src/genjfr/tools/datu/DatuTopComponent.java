/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.tools.datu;

import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.util.swing.DateWidget;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.logging.Logger;
import javax.swing.Box;
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
    private PointInTime pitMaxi;
    private PointInTime pitMini;
    private DateWidget dw1 = new DateWidget();
    private DateWidget dw2 = new DateWidget();
    private int gap = 0; // Valeur du gap
    private String signe = " + ";
    private static boolean dw1UpdateInProgress = false;
    private static boolean dw2UpdateInProgress = false;

    public DatuTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DatuTopComponent.class, "CTL_DatuTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        dw1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PointInTime pit = dw1.getValue();
                dw1UpdateInProgress = true;
                if (pit != null) {
                    String CelendarName = new String(pit.getCalendar().toString());
                    Border PanelBorder = jPanel5.getBorder();
                    ((TitledBorder) PanelBorder).setTitle(CelendarName);
                    if (pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
                    } else if (pit.getCalendar().equals(PointInTime.FRENCHR)) {
                        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.frenchR.toolTipText")); // NOI18N
                    } else {
                        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.others.toolTipText")); // NOI18N
                    }
                    jPanel5.repaint();
                    UpdateDw1();
                    // Are we call thru dw2.stateChanged ()
                    if (dw2UpdateInProgress == false) {
                        try {
                            if (dw2.getValue() != null) {
                                pit.set(dw2.getValue().getCalendar());
                                dw2.setValue(pit);
                            }
                        } catch (GedcomException ex) {
//                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                dw1UpdateInProgress = false;
            }
        });

        dw1.setValue(PointInTime.getNow());

        dw2.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                dw2UpdateInProgress = true;
                PointInTime pit = dw2.getValue();

                if (pit != null) {
                    String CelendarName = new String(pit.getCalendar().toString());
                    Border PanelBorder = jPanel6.getBorder();
                    ((TitledBorder) PanelBorder).setTitle(CelendarName);
                    if (pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
                    } else if (pit.getCalendar().equals(PointInTime.FRENCHR)) {
                        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.frenchR.toolTipText")); // NOI18N
                    } else {
                        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.others.toolTipText")); // NOI18N
                    }
                    jPanel6.repaint();
                    // Are we call thru dw1.stateChanged ()
                    if (dw1UpdateInProgress == false) {
                        try {
                            if (dw1.getValue() != null) {
                                pit.set(dw1.getValue().getCalendar());
                                dw1.setValue(pit);
                            }
                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        UpdateDw2();
                    }
                }
                dw2UpdateInProgress = false;
            }
        });
        try {
            PointInTime pit = PointInTime.getNow();
            pit.set(PointInTime.JULIAN);
            dw2.setValue(pit);
        } catch (GedcomException ex) {
            dw2.setValue(PointInTime.getNow());
        }


    }

    public void UpdateDw1() {
        if (dw1.getValue() != null) {
            PointInTime Pit = dw1.getValue();
            jDisplay.setText(calcule(Pit).getDisplayValue());
        }
    }

    public void UpdateDw2() {
        if (dw2.getValue() != null) {
            PointInTime Pit = dw2.getValue();
            jDisplay1.setText(calcule(Pit).getDisplayValue());
        }
    }

    private PropertyDate calcule(PointInTime pitInitial) {
        PropertyDate exemple = new PropertyDate();

        if (gap == 0) {
            pitMini = pitInitial;
            pitMaxi = pitInitial;
        } else {
            // Pit limits
            pitMini = new PointInTime(pitInitial.getDay(),
                    pitInitial.getMonth(),
                    pitInitial.getYear(),
                    pitInitial.getCalendar());
            pitMini.add(0, gap * -1, 0);
            pitMaxi = new PointInTime(pitInitial.getDay(),
                    pitInitial.getMonth(),
                    pitInitial.getYear(),
                    pitInitial.getCalendar());
            pitMaxi.add(0, gap, 0);
        }

        if (pitMini.compareTo(pitMaxi) != 0) {
            exemple.setValue(PropertyDate.FROM_TO, pitMini, pitMaxi, "");
        } else {
            exemple.setValue(PropertyDate.DATE, pitMini, null, "");
        }

        return exemple;
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
        jDisplay = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jDisplay1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jTextFieldGap = new javax.swing.JTextField();
        jSliderGap = new javax.swing.JSlider();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel(){
            protected void paintComponent(Graphics g)
            {
                g.drawImage(arvernes_logo_small.getImage(), 0, 0, this.getHeight(), this.getWidth(), null);
                super.paintComponent(g);
            }
        }
        ;
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jPanel1.setPreferredSize(new java.awt.Dimension(196, 569));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel5.setForeground(java.awt.SystemColor.activeCaption);
        jPanel5.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(196, 93));

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add( Box.createHorizontalGlue() );
        jPanel2.add( dw1 );
        jPanel2.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

        jDisplay.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jDisplay.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jDisplay.text_1")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDisplay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel6.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.calendar.gregorian.toolTipText")); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(196, 93));

        jPanel4.setOpaque(false);
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));
        jPanel4.add( Box.createHorizontalGlue() );
        jPanel4.add( dw2 );
        jPanel4.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

        jDisplay1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jDisplay1.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jDisplay1.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDisplay1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDisplay1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel3.setToolTipText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jPanel3.toolTipText")); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(196, 86));

        jTextFieldGap.setColumns(4);
        jTextFieldGap.setEditable(false);
        jTextFieldGap.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldGap.setText(org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jTextFieldGap.text")); // NOI18N
        jTextFieldGap.setBorder(null);
        jTextFieldGap.setOpaque(false);

        jSliderGap.setValue(0);
        jSliderGap.setOpaque(false);
        jSliderGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderGapStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldGap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(jSliderGap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jSliderGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel7.setOpaque(false);
        jPanel7.setPreferredSize(new java.awt.Dimension(115, 115));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 111, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 111, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel7);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24));
        jLabel2.setForeground(new java.awt.Color(51, 51, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderGapStateChanged
        if (!jSliderGap.getValueIsAdjusting()) {
            // Gap absolute value
            gap = jSliderGap.getValue();
            String texte = new String();
            if (gap == 1) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.month"), gap);
            } else if (gap != 0) {
                texte += String.format(NbBundle.getMessage(DatuTopComponent.class, "DatuTopComponent.months"), gap);
            }
            jTextFieldGap.setText(texte);
            UpdateDw1();
            UpdateDw2();
        }
    }//GEN-LAST:event_jSliderGapStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jDisplay;
    private javax.swing.JTextField jDisplay1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderGap;
    private javax.swing.JTextField jTextFieldGap;
    // End of variables declaration//GEN-END:variables
    private javax.swing.ImageIcon arvernes_logo_small = new javax.swing.ImageIcon(getClass().getResource("/genjfr/tools/datu/arvernes_logo_small.png")); // NOI18N

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
