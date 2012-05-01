/*
 * MergeDialog.java
 *
 * Created on 30 avr. 2012, 18:55:26
 */

package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.UnitOfWork;
import java.awt.Point;
import javax.swing.table.AbstractTableModel;

/**
 * Cette classe permet d'insérer un relvé dans une entité d'un fichier GEDCOM.
 * @author Michel
 */
public class MergeDialog extends javax.swing.JDialog {

    private MergeModel model = null;
    private Entity entity = null;
    private Record record = null;

   /**
    * factory de la fenetre
    * @param location
    * @param entity
    * @param record
    */
   public static void show( final Point location, final Entity entity, final Record record ) {
//       java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
                final MergeDialog dialog = new MergeDialog(new javax.swing.JFrame(), true);
                dialog.setData(entity, record);
                dialog.setLocation(location);
                dialog.setVisible(true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                });
//            }
//        });
    }

    /**
     * Constructeur d'uen fenetre
     */
    private MergeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }


    /**
     * Initilaise le modele de données du comparateur
     * @param entity
     * @param record
     */
    public void setData(Entity entity, Record record) {
        this.entity = entity;
        this.record = record;
        if ( entity instanceof Indi) {
            if( record instanceof RecordBirth) {
                model = new BirthModel((Indi) entity, (RecordBirth) record);
            }

        }
        jTable1.setModel(model);
        model.fireTableDataChanged();
    }

    /**
     *
     */
    class BirthModel extends MergeModel {

        Indi indi;
        RecordBirth record;

        BirthModel(Indi indi, RecordBirth record) {
            this.indi = indi;
            this.record = record;

            data = createData(3);
            data [0][0] = "Nom";
            data [1][0] = "Prénom";
            data [2][0] = "Naissance";

            data [0][1] = indi.getLastName();
            data [1][1] = indi.getFirstName();
            data [2][1] = indi.getBirthAsString();

            data [0][2] = record.getIndiLastName();
            data [1][2] = record.getIndiFirstName();
            data [2][2] = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() :  record.getIndiBirthDate();

        }

        /**
         * enregistre le relevé dans l'entité
         */
        @Override
        void saveData() {

          indi.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) {

                    // je copie le nom et le prénom.
                    if (isChecked(0)) {
                        indi.setName(indi.getFirstName(), record.getIndiLastName().toString());
                    }
                    if (isChecked(1)) {
                        indi.setName(record.getIndiFirstName().toString(), indi.getLastName());
                    }

                    // je copie la date de naissance
                    Property birthProperty = indi.getProperty("BIRT");
                    if (isChecked(2)) {
                        if (birthProperty == null) {
                            birthProperty = indi.addProperty("BIRT", "");
                        }
                        PropertyDate propertyDate = (PropertyDate) birthProperty.getProperty("DATE");
                        if (propertyDate == null) {
                            propertyDate = (PropertyDate) birthProperty.addProperty("DATE", "");
                        }
                        propertyDate.setValue(record.getEventDateField().toString());
                    }
                }
            });
        }
    }

    abstract private class MergeModel extends AbstractTableModel {

        Object [][] data = null;

        Object [][] createData(int nbrows) {
            if( data == null) {
                data = new Object[nbrows][4];
                for(int i = 0 ; i <nbrows; i++ ) {
                    data[i][3] = false;

                }
            }
            return data;
        }

        void check(int rowNum, boolean state) {
            data[rowNum][3] = state;
        }

        boolean isChecked(int rowNum) {
            return ((Boolean)(data[rowNum][3])).booleanValue();
        }
        abstract void saveData();
        
        private String[] columnNames = {"", "Individu", "Relevé", ""};

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
                return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col != 3) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }


    }

   private void initData(Indi indi, RecordDeath recordDeath ) {

   }

   private void initData(Indi indi, RecordMarriage recordMarriage ) {

   }

   private void initData(Fam fam, RecordBirth recordBirth ) {

   }







    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jButtonOK.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonOK.text")); // NOI18N
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonOK);

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.setRolloverEnabled(false);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonCancel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        model.saveData();
        setVisible(false);
        dispose();

    }//GEN-LAST:event_jButtonOKActionPerformed

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                MergeDialog dialog = new MergeDialog(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
