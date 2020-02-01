/*
 * ReleveStatistic.java
 *
 * Created on 20 juin 2012, 19:59:49
 */

package ancestris.modules.releve;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Michel
 */
public class ReleveStatistic extends javax.swing.JFrame {

    private final TreeMap<Integer, int[]> datas = new TreeMap<Integer, int[]>();
    private final String[] columnNames = {
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.year"),
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.births"),
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.marriages"),
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.deaths"),
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.misc"),
        NbBundle.getMessage(getClass(),"ReleveStatistic.columnTitle.all"),
    };
    private static final String LINE_BREAK = "\n";
    private static final String CELL_BREAK = "\t";


    /** Creates new form ReleveStatistic
     * @param dataManager */
    static public void  showStatistics(DataManager dataManager ) {
        ReleveStatistic statistics = new ReleveStatistic();
        statistics.setVisible(true);
        statistics.setModel(dataManager);
    }


    /** Creates new form ReleveStatistic */
    public ReleveStatistic() {
        initComponents();
        
        // je configure la position de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screen.width - getWidth())/ 2, (screen.height -getHeight()) / 2, getWidth(), getHeight());
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        
        setTitle(NbBundle.getMessage(getClass(), "ReleveTopComponent.menu.statistics"));
    }

    /**
     * Initialise le modele de données de la JTable
     * @param dataManager
     */
   public void setModel(DataManager dataManager ) {
       int[] total = new int[5];
       if(dataManager != null) {
           int nbRecord = dataManager.getDataModel().getRowCount();
           // je compte les releves par année
           for (int i = 0; i < nbRecord; i++) {
               Record record = dataManager.getDataModel().getRecord(i);
               
               int year;
               String value = record.getFieldValue(Record.FieldType.eventDate);
               if ( value.isEmpty() ) {
                   year = 0;
               } else {
                   int index = value.lastIndexOf("/");
                   if (index == -1) {
                       year = Integer.parseInt(value);
                   } else {
                       year = Integer.parseInt(value.substring(index + 1));
                   }
               }
               
               int[] counters = datas.get(year);
               if (counters == null) {
                   counters = new int[5];
                   datas.put(year, counters);

               }
               counters[0] = year;
               if (record instanceof RecordBirth) {
                   counters[1]++;
                   total[1]++;
               } else if (record instanceof RecordMarriage) {
                   counters[2]++;
                   total[2]++;
               } else if (record instanceof RecordDeath) {
                   counters[3]++;
                   total[3]++;
               } else {
                   counters[4]++;
                   total[4]++;
               }
           }
       }

       Object[][] data2 =  new Object[datas.size()+1][6]; //(new int[][])datas.values().toArray(new int[][datas.size]);
       int i = 0;
       for(int[] counters : datas.values()) {
           data2[i][0] = counters[0]; // année
           data2[i][1] = counters[1];
           data2[i][2] = counters[2];
           data2[i][3] = counters[3];
           data2[i][4] = counters[4];
           data2[i][5] = counters[1]+counters[2]+counters[3]+counters[4];
           i++;
       }
       data2[i][0] = "Total";
       data2[i][1] = total[1];
       data2[i][2] = total[2];
       data2[i][3] = total[3];
       data2[i][4] = total[4];
       data2[i][5] = total[1]+total[2]+total[3]+total[4];

       jTable1.setModel( new DefaultTableModel(data2, columnNames));

       
    }

    private void copyToClipboard() {
        Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
        int numCols = jTable1.getColumnCount();
        int numRows = jTable1.getRowCount();

        StringBuilder excelStr = new StringBuilder();

        // je copie les titres des colonnes dans la première ligne
        for (int j = 0; j < numCols; j++) {
            excelStr.append(columnNames[j]);
            if (j < numCols - 1) {
                excelStr.append(CELL_BREAK);
            }
        }
        excelStr.append(LINE_BREAK);

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                excelStr.append(escape(jTable1.getValueAt(i, j)));
                if (j < numCols - 1) {
                    excelStr.append(CELL_BREAK);
                }
            }
            excelStr.append(LINE_BREAK);
        }

        StringSelection sel = new StringSelection(excelStr.toString());
        CLIPBOARD.setContents(sel, sel);
    }

    private String escape(Object cell) {
                return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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
        jTable1.setMaximumSize(null);
        jTable1.setMinimumSize(null);
        jTable1.setPreferredSize(null);
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jButton1.setText(org.openide.util.NbBundle.getMessage(ReleveStatistic.class, "ReleveStatistic.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        copyToClipboard();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables


   

}
