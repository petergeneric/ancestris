/*
 * MergeDialog.java
 *
 * Created on 30 avr. 2012, 18:55:26
 */

package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.Record;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.tree.TreeView;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 * Cette classe est le point d'entrée du package.
 * Elle permet d'insérer un relevé dans une entité d'un fichier GEDCOM.
 * @author Michel
 */
public class MergeDialog extends javax.swing.JDialog {

    protected MergeModel tableModel = null;
    Component dndSourceComponent = null;

    /**
    * factory de la fenetre
    * @param location
    * @param entity
    * @param record
    */
    public static MergeDialog show(Component parent, final Gedcom gedcom, final Entity entity, final Record record, boolean visible) {

        final MergeDialog dialog = new MergeDialog(parent);
        dialog.setData(record, gedcom, entity);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dialog.setVisible(false);
                dialog.componentClosed();
                dialog.dispose();
            }
        });
        dialog.setVisible(visible);

        return dialog;
    }

    /**
     * Constructeur d'une fenetre
     */
    protected MergeDialog(Component parent) {
        super(SwingUtilities.windowForComponent(parent));
        this.dndSourceComponent = parent;
        setLayout(new java.awt.BorderLayout());
        initComponents();
        setAlwaysOnTop(true);

        // je configure la taille de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String size = NbPreferences.forModule(MergeDialog.class).get("MergeDialogSize", "300,450,0,0");
        String[] dimensions = size.split(",");
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int x = Integer.parseInt(dimensions[2]);
            int y = Integer.parseInt(dimensions[3]);
            if ( width < 100 ) {
                width = 100;
            }
            if ( height < 100 ) {
                height = 100;
            }
            if ( x < 10 || x > screen.width -10) {
                x = (screen.width / 2) - (width / 2);
            }
            if ( y < 10 || y > screen.height -10) {
                y = (screen.height / 2) - (height / 2);
            }
            setBounds(x, y, width, height);
        } else {
            setBounds(screen.width / 2 -100, screen.height / 2- 100, 300, 450);
        }
        pack();

    }

    /**
     * cette methode est applelée à la fermeture de la fenetre
     * Elle enregistre les preference de l'utilsateur.
     */
    protected void componentClosed() {
        // j'enregistre les preferences de la table
        mergeTable.componentClosed();

        // j'enregistre la taille et la position
        String size;
        size = String.valueOf(getWidth()) + ","
                + String.valueOf(getHeight()) + ","
                + String.valueOf(getLocation().x + ","
                + String.valueOf(getLocation().y));

        NbPreferences.forModule(MergeDialog.class).put("MergeDialogSize", size);
    }

    /**
     * Initialise le modele de données du comparateur
     * @param entity
     * @param record
     */
    protected void setData(Record record, Gedcom gedcom, Entity entity ) {

        List<Entity> sameIndi = MergeModel.findSameIndi(record, gedcom,(Indi) entity);
        if( sameIndi.size()>0) {
            mergePanel1.setVisible(true);
            mergePanel1.setIndi(record, gedcom, entity, sameIndi, this);
        } else {
            mergePanel1.setVisible(false);
        }
        // je cree le modele
        createModel(record, gedcom, entity);
    }

    /**
     * cree le modele de donne et l'affiche dans la fenetre
     * Cette methode est appelee par le contructeur de cette fenetre et par
     * le panneau de choix des individus
     * @param entity
     * @param record
     */
    protected void createModel(Record record, Gedcom gedcom, Entity entity) {
        tableModel = MergeModel.createMergeModel(record, gedcom, entity);
        mergeTable.setModel(tableModel);
        tableModel.fireTableDataChanged();
        // je renseigne le titre de la fenetre
        setTitle(tableModel.getTitle());
        // j'affiche l'entité dans l'arbre
        if ( dndSourceComponent instanceof TreeView ) {
            TreeView treeView = (TreeView)dndSourceComponent;
            if (entity != null ) {
                // je centre l'arbre sur l'entité
                treeView.setRoot(entity);
            } else {
                if( tableModel.getRow(MergeModel.RowType.IndiFamily).entityValue!=null) {
                    // je centre l'arbre sur la famille des parents
                    treeView.setRoot((Fam)tableModel.getRow(MergeModel.RowType.IndiFamily).entityValue);
                } 
            }
        }
    }
   /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mergePanel1 = new ancestris.modules.releve.dnd.MergePanel();
        jPanelTable = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mergeTable = new ancestris.modules.releve.dnd.MergeTable();
        jPanelButton = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        mergePanel1.setPreferredSize(new java.awt.Dimension(300, 100));
        getContentPane().add(mergePanel1, java.awt.BorderLayout.NORTH);

        jPanelTable.setPreferredSize(new java.awt.Dimension(400, 300));
        jPanelTable.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setPreferredSize(null);

        mergeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        mergeTable.setPreferredScrollableViewportSize(null);
        jScrollPane2.setViewportView(mergeTable);

        jPanelTable.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelTable, java.awt.BorderLayout.CENTER);

        jButtonOK.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonOK.text")); // NOI18N
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOK);

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.setRolloverEnabled(false);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonCancel);

        getContentPane().add(jPanelButton, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        componentClosed();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        tableModel.copyRecordToEntity();
        componentClosed();
        setVisible(false);
        dispose();

    }//GEN-LAST:event_jButtonOKActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JScrollPane jScrollPane2;
    private ancestris.modules.releve.dnd.MergePanel mergePanel1;
    private ancestris.modules.releve.dnd.MergeTable mergeTable;
    // End of variables declaration//GEN-END:variables

}
