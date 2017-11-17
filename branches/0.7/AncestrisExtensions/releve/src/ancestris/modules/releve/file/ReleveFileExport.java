/*
 * ReleveFileExport.java
 *
 * Created on 29 mars 2012, 18:51:44
 */

package ancestris.modules.releve.file;

/**
 *
 * @author Michel
 */
public class ReleveFileExport extends javax.swing.JDialog {

    /** Creates new form ReleveFileExport */
    public ReleveFileExport(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        panelExport = new javax.swing.JPanel();
        jPanelFormat = new javax.swing.JPanel();
        jRadioButtonAncestris = new javax.swing.JRadioButton();
        jRadioButtonEgmt = new javax.swing.JRadioButton();
        jRadioButtonNimegue = new javax.swing.JRadioButton();
        jPanelModel = new javax.swing.JPanel();
        jRadioButtonAll = new javax.swing.JRadioButton();
        jRadioButtonBirth = new javax.swing.JRadioButton();
        jRadioButtonMarriage = new javax.swing.JRadioButton();
        jRadioButtonDeath = new javax.swing.JRadioButton();
        jRadioButtonMisc = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelExport.setLayout(new java.awt.GridBagLayout());

        jPanelFormat.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jPanelFormat.border.title"))); // NOI18N
        jPanelFormat.setLayout(new java.awt.GridLayout(3, 1, 2, 2));

        jRadioButtonAncestris.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonAncestris.text")); // NOI18N
        jPanelFormat.add(jRadioButtonAncestris);

        jRadioButtonEgmt.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonEgmt.text")); // NOI18N
        jPanelFormat.add(jRadioButtonEgmt);

        jRadioButtonNimegue.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonNimegue.text")); // NOI18N
        jPanelFormat.add(jRadioButtonNimegue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panelExport.add(jPanelFormat, gridBagConstraints);

        jPanelModel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jPanelModel.border.title"))); // NOI18N
        jPanelModel.setLayout(new java.awt.GridLayout(5, 1, 2, 2));

        jRadioButtonAll.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonAll.text")); // NOI18N
        jPanelModel.add(jRadioButtonAll);

        jRadioButtonBirth.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonBirth.text")); // NOI18N
        jPanelModel.add(jRadioButtonBirth);

        jRadioButtonMarriage.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonMarriage.text")); // NOI18N
        jPanelModel.add(jRadioButtonMarriage);

        jRadioButtonDeath.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonDeath.text")); // NOI18N
        jPanelModel.add(jRadioButtonDeath);

        jRadioButtonMisc.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonMisc.text")); // NOI18N
        jPanelModel.add(jRadioButtonMisc);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panelExport.add(jPanelModel, gridBagConstraints);

        getContentPane().add(panelExport, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ReleveFileExport dialog = new ReleveFileExport(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanelFormat;
    private javax.swing.JPanel jPanelModel;
    private javax.swing.JRadioButton jRadioButtonAll;
    private javax.swing.JRadioButton jRadioButtonAncestris;
    private javax.swing.JRadioButton jRadioButtonBirth;
    private javax.swing.JRadioButton jRadioButtonDeath;
    private javax.swing.JRadioButton jRadioButtonEgmt;
    private javax.swing.JRadioButton jRadioButtonMarriage;
    private javax.swing.JRadioButton jRadioButtonMisc;
    private javax.swing.JRadioButton jRadioButtonNimegue;
    private javax.swing.JPanel panelExport;
    // End of variables declaration//GEN-END:variables

}