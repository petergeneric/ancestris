 package ancestris.modules.releve;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Record;
import java.awt.Frame;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author michel
 */


public class JuridictionConvertDialog extends javax.swing.JDialog {

    private final DataManager dataManager;
    static private final String juridictionSeparator = ",";
    
    // liste des champs contenant un lieu dans les releves
    static final Field.FieldType[] fieldTypes = {
        FieldType.indiBirthPlace, FieldType.indiResidence, FieldType.indiMarriedResidence, FieldType.indiFatherResidence, FieldType.indiMotherResidence, 
        FieldType.wifePlace,      FieldType.wifeResidence, FieldType.wifeMarriedResidence, FieldType.wifeFatherResidence, FieldType.wifeMotherResidence
    };
    
    /**
     * affiche la fenêtre de changer l'ordre des juridictions dans les liux des relvés
     * @param parent
     * @param dataManager
     */
    public static void show(Frame parent, DataManager dataManager, String sourceTitle) {
        final JuridictionConvertDialog dialog = new JuridictionConvertDialog(parent, dataManager);
        dialog.setVisible(true);
    }
    
    /**
     * Creates new form JuridictionConvertDialog
     */
    public JuridictionConvertDialog(java.awt.Frame parent, DataManager dataManager) {
        super(parent, true);
        this.dataManager = dataManager;
        initComponents();
        
        JuridictionTableModel tableModel = new JuridictionTableModel(dataManager);
        jTableResult.setModel(tableModel);
        jTableResult.getTableHeader().setReorderingAllowed(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelCommand = new javax.swing.JPanel();
        jButtonConvert = new javax.swing.JButton();
        jPanelResult = new javax.swing.JPanel();
        jScrollPaneResult = new javax.swing.JScrollPane();
        jTableResult = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConvert, org.openide.util.NbBundle.getMessage(JuridictionConvertDialog.class, "JuridictionConvertDialog.jButtonConvert.text")); // NOI18N
        jButtonConvert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConvertActionPerformed(evt);
            }
        });
        jPanelCommand.add(jButtonConvert);

        getContentPane().add(jPanelCommand, java.awt.BorderLayout.NORTH);

        jPanelResult.setLayout(new java.awt.BorderLayout());

        jTableResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableResult.getTableHeader().setReorderingAllowed(false);
        jScrollPaneResult.setViewportView(jTableResult);

        jPanelResult.add(jScrollPaneResult, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelResult, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConvertActionPerformed
        
        int nbJuridictions = jTableResult.getColumnCount();
        int[] convert = new int[nbJuridictions];
        
        TableColumnModel columnModel = jTableResult.getTableHeader().getColumnModel();
        for( int c =0 ; c <columnModel.getColumnCount(); c++) {
            convert[c]= columnModel.getColumn(c).getModelIndex(); 
            //System.out.println("c="+c + " index="+ convert[c]);
        }
                
        // je modifie les lieux avec les juridictions dans l'ordre demandé
        for(int i=0; i < dataManager.getDataModel().getRowCount(); i++ ) {
            Record record = dataManager.getDataModel().getRecord(i);
            
            for(Field.FieldType fieldType : fieldTypes ) {
                Field fieldPlace = record.getField(fieldType);
                if( fieldPlace == null ) {
                    continue;
                }
                if( fieldPlace.isEmpty() ) {
                    continue;
                }
                String actualPlace = fieldPlace.getValue().toString();
                String[] juridictions = actualPlace.split(juridictionSeparator);
                String convertedPlace = "";
                for (int j = 0; j < nbJuridictions; j++) {
                    if (juridictions.length -1 >= convert[j] ) {
                        convertedPlace += juridictions[convert[j]].trim();
                    } 
                    if (j < nbJuridictions - 1) {
                        convertedPlace += juridictionSeparator;
                    }
                }
                fieldPlace.setValue(convertedPlace);
                
                // je remplace le lieu par sa nouvelle valeur dans la liste de complétion
                dataManager.getCompletionProvider().updatePlaces(fieldPlace, actualPlace);
            }
        }
        
        dataManager.getDataModel().fireAllChanged();
        
    }//GEN-LAST:event_jButtonConvertActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConvert;
    private javax.swing.JPanel jPanelCommand;
    private javax.swing.JPanel jPanelResult;
    private javax.swing.JScrollPane jScrollPaneResult;
    private javax.swing.JTable jTableResult;
    // End of variables declaration//GEN-END:variables


    private class JuridictionTableModel extends AbstractTableModel {
        private int nbJuridictions;
        private int nbPlaces = 0;
        private final String[][] juridictions;
        
        
        public JuridictionTableModel( DataManager dataManager) {

            // je recherche tous les lieux dans les relevés 
            TreeMap<String,String[]> places = new TreeMap<String,String[]>();
            nbJuridictions = 0;
            for(int i = 0; i < dataManager.getDataModel().getRowCount(); i++) {
                Record record = dataManager.getDataModel().getRecord(i);

                for (Field.FieldType fieldType : fieldTypes) {
                    Field fieldPlace = record.getField(fieldType);
                    if (fieldPlace == null) {
                        continue;
                    }
                    if (fieldPlace.isEmpty()) {
                        continue;
                    }
                    String actualPlace = fieldPlace.getValue().toString();
                    String[] splitPlace = actualPlace.split(juridictionSeparator, -1);
                    if (splitPlace.length > nbJuridictions) {
                        nbJuridictions = splitPlace.length  ;
                    }
                    places.put(actualPlace, splitPlace);
                }
            }
            
            nbPlaces = places.size();
            
            // je copie les juridictions dans le modele de la JTable
            juridictions = new String[places.size()][nbJuridictions];
            int i = 0;
            for( String[] place : places.values() ) {
                int j=0;
                for ( String field : place )  {
                    juridictions[i][j] = field.trim();
                    j++;
                }
                for ( int jj=j ; jj < nbJuridictions; jj++ )  {
                    // j'afiche "---" pour indiquer que la juridiction n'existe pas dans le lieu
                    juridictions[i][j] = "---";
                    j++;
                }
                i++;
            }            
        }
        
        @Override
        public int getColumnCount() {
            return nbJuridictions;
        }

        @Override
        public int getRowCount() {
            return nbPlaces;
        }

        @Override
        public String getColumnName(int col) {
            return Integer.toString(col +1);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        @Override
        public String getValueAt(int row, int col) {
            return juridictions[row][col];         
        }
    }
}
