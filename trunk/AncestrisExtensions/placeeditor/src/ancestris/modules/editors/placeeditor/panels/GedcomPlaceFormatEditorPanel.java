package ancestris.modules.editors.placeeditor.panels;

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;

/**
 *
 * @author dominique
 */
public class GedcomPlaceFormatEditorPanel extends javax.swing.JPanel {

    private Gedcom gedcom;
    private String[] placeFormat;

    /**
     * Creates new form GedcomPlaceFormatEditorPanel
     */
    public GedcomPlaceFormatEditorPanel(Gedcom gedcom) {
        this.gedcom = gedcom;
        placeFormat = PropertyPlace.getFormat(gedcom);

        initComponents();

        if (placeFormat.length > 0) {
            jLabel1.setText(placeFormat[0]);
            jTextField1.setVisible(true);
            jTextField1.setText("1");
        } else {
            jLabel1.setText("");
            jTextField1.setVisible(false);
            jTextField1.setText("1");
        }

        if (placeFormat.length > 1) {
            jLabel2.setText(placeFormat[1]);
            jTextField2.setVisible(true);
        } else {
            jLabel2.setText("");
            jTextField2.setVisible(false);
        }

        if (placeFormat.length > 2) {
            jLabel3.setText(placeFormat[2]);
            jTextField3.setVisible(true);
        } else {
            jLabel3.setText("");
            jTextField3.setVisible(false);
        }

        if (placeFormat.length > 3) {
            jLabel4.setText(placeFormat[3]);
            jTextField4.setVisible(true);
        } else {
            jLabel4.setText("");
            jTextField4.setVisible(false);
        }
        if (placeFormat.length > 4) {
            jLabel5.setText(placeFormat[4]);
            jTextField5.setVisible(true);
        } else {
            jLabel5.setText("");
            jTextField5.setVisible(false);
        }

        if (placeFormat.length > 5) {
            jLabel6.setText(placeFormat[5]);
            jTextField6.setVisible(true);
        } else {
            jLabel6.setText("");
            jTextField6.setVisible(false);
        }

        if (placeFormat.length > 6) {
            jLabel7.setText(placeFormat[6]);
            jTextField7.setVisible(true);
        } else {
            jLabel7.setText("");
            jTextField7.setVisible(false);
        }

        if (placeFormat.length > 7) {
            jLabel8.setText(placeFormat[7]);
            jTextField8.setVisible(true);
        } else {
            jLabel8.setText("");
            jTextField8.setVisible(false);
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jLabel8.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField1.text")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField2.text")); // NOI18N

        jTextField3.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField3.text")); // NOI18N

        jTextField4.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField4.text")); // NOI18N

        jTextField5.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField5.text")); // NOI18N

        jTextField6.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField6.text")); // NOI18N

        jTextField7.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField7.text")); // NOI18N

        jTextField8.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceFormatEditorPanel.class, "GedcomPlaceFormatEditorPanel.jTextField8.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel6)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    // End of variables declaration//GEN-END:variables
}
