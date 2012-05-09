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
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.UnitOfWork;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Cette classe permet d'insérer un relvé dans une entité d'un fichier GEDCOM.
 * @author Michel
 */
public class MergeDialog extends javax.swing.JDialog {

    private MergeTable table = null;
    protected MergeModel model = null;
//    private Entity entity = null;
//    private Record record = null;

   /**
    * factory de la fenetre
    * @param location
    * @param entity
    * @param record
    */
    public static MergeDialog show(Component parent, final Point location, final Entity entity, final Record record, boolean visible) {

        final MergeDialog dialog = new MergeDialog(SwingUtilities.windowForComponent(parent));
        dialog.setData(entity, record);
        dialog.setLocation(location);
        dialog.setAlwaysOnTop(true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.setVisible(visible);

        return dialog;
    }

    /**
     * Constructeur d'uen fenetre
     */
    protected MergeDialog(java.awt.Window parent) {
        super(parent);
        initComponents();
        //setPreferredSize(new Dimension(getPreferredSize().width, 300));
    }


    /**
     * Initilaise le modele de données du comparateur
     * @param entity
     * @param record
     */
    protected void setData(Entity entity, Record record) {
        if ( entity instanceof Indi) {
            if( record instanceof RecordBirth) {
                model = new BirthModel((Indi) entity, (RecordBirth) record);
            }

        }
        List<Indi> sameIndi = model.findSameIndi();
        if (!sameIndi.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Un événènement existe déjà :").append('\n');
            for (Indi indi : sameIndi) {
                message.append("   ").append(indi.toString()).append('\n');
            }
            jTextAreaMessage.setText(message.toString());
        }
        table = new MergeTable(model);
        //table.setModel(model);
        jScrollPane1.setViewportView(table);
        model.fireTableDataChanged();
        pack();
    }
    
    /**
     *
     */
    class BirthModel extends MergeModel {

        Indi selectedIndi;
        RecordBirth record;

        BirthModel(Indi indi, RecordBirth record) {
            this.selectedIndi = indi;
            this.record = record;

            setData(RowType.IndiLastName,"Nom", record.getIndiLastName().toString(), indi.getLastName());
            setData(RowType.IndiFirstName,"Prénom", record.getIndiFirstName().toString(), indi.getFirstName());
            setData(RowType.IndiSex,"Sexe", record.getIndiSex().toString(), indi.getPropertyValue("SEX"));
            setData(RowType.IndiBirthDate,"Naissance", record.getIndiBirthDate().isEmpty() ? record.getEventDateField().getDisplayValue():  record.getIndiBirthDate().getPropertyDate().getDisplayValue(), indi.getBirthAsString());

            Property birthProperty = indi.getProperty("BIRT");

            // Source de la naissance
            Source[] recordSources = findSources(indi.getGedcom(), record);

            Source[] entitySources;
            if (birthProperty!= null) {
                Property[] sourceProperties;
                // je copie les sources de l'entite
                sourceProperties = birthProperty.getProperties("SOUR",false);
                entitySources = new Source[sourceProperties.length];
                for(int i=0; i <sourceProperties.length; i++) {
                    entitySources[i] = (Source) ((PropertySource)sourceProperties[i]).getTargetEntity();
                }
            } else {
                entitySources = new Source[0];
            }

            // je cherche les sources du releve
            setData(RowType.Source,"Source", recordSources, entitySources);

        }

        /**
         * enregistre le relevé dans l'entité
         */
        @Override
        void saveData() {

          selectedIndi.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) {
                    try {
                        // je copie le nom.
                        if (isChecked(RowType.IndiLastName)) {
                            selectedIndi.setName(selectedIndi.getFirstName(), record.getIndiLastName().toString());
                        }
                        // je copie le prenom.
                        if (isChecked(RowType.IndiFirstName)) {
                            selectedIndi.setName(record.getIndiFirstName().toString(), selectedIndi.getLastName());
                        }

                        // je copie le sexe.
                        if (isChecked(RowType.IndiSex)) {
                            selectedIndi.setSex(record.getIndiSex().getSex());
                        }

                        // je copie la date de naissance
                        Property birthProperty = selectedIndi.getProperty("BIRT");
                        if (isChecked(RowType.IndiBirthDate)) {
                            if (birthProperty == null) {
                                // je cree la propriete de naissance si elle n'existe pas déjà
                                birthProperty = selectedIndi.addProperty("BIRT", "");
                            }
                            PropertyDate propertyDate = (PropertyDate) birthProperty.getProperty("DATE");
                            if (propertyDate == null) {
                                propertyDate = (PropertyDate) birthProperty.addProperty("DATE", "");
                            }
                            propertyDate.setValue(record.getEventDateField().getValue());
                        }

                        // je copie la source
                        if (isChecked(RowType.Source)) {
                            Source recordSource = (Source) model.getRow(RowType.Source).recordValue;
                            if ( gedcom.contains(recordSource)) {
                                // je verifie si la source est déjà associée à la naissance
                                boolean found = false;
                                for(Source entitySource : (Source[]) model.getRow(RowType.Source).entityChoice ) {
                                    if (recordSource.equals(entitySource)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if( found == false ) {
                                    try {
                                        // je relie la source du releve à la propriété de naissance 
                                        PropertyXRef sourcexref = (PropertyXRef) birthProperty.addProperty("SOUR", "@"+recordSource.getId()+"@");
                                        sourcexref.link();
                                    } catch (GedcomException ex) {
                                        throw new Exception(String.format("Link indi=%s with source=%s error=% ", selectedIndi.getName(), recordSource.getTitle(), ex.getMessage()));
                                    }
                                }
                            } else {
                                // je cree une nouvelle source et je la relie à l'entité
                                Source newSource = (Source) gedcom.createEntity(Gedcom.SOUR);
                                newSource.addProperty("TITL", recordSource.getTitle());
                                try {
                                    // je relie la source du releve à l'entité
                                    PropertyXRef sourcexref = (PropertyXRef) birthProperty.addProperty("SOUR", "@"+newSource.getId()+"@");
                                    sourcexref.link();
                                } catch (GedcomException ex) {
                                    throw new Exception(String.format("Link indi=%s with source=%s error=% ", selectedIndi.getName(), recordSource.getTitle(), ex.getMessage()));
                                }

                            }
                        }
                    } catch (Exception ex1) {
                        JOptionPane.showMessageDialog(null, ex1.getMessage(), "Ajout d'un relevé", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
        }

        /**
         * recherche les individus qui ont le même nom et date de naissance
         * et qui sont different de l'individu selectionné
         * @return
         */
        @Override
        protected List<Indi> findSameIndi() {
            List<Indi> sameIndis = new ArrayList<Indi>();

            for (Indi indi : selectedIndi.getGedcom().getIndis()) {
                if(   record.getIndiFirstName().toString().equals(indi.getFirstName())
                   && record.getIndiLastName().toString().equals(indi.getLastName())
                   && selectedIndi.compareTo(indi)!=0
                ) {

                    PropertyDate recordDate;
                    PropertyDate indiDate;
                    if ( record.getIndiBirthDate().getPropertyDate() != null
                         && record.getIndiBirthDate().getPropertyDate().isComparable()) {
                        recordDate = record.getIndiBirthDate().getPropertyDate();
                    } else {
                        if ( record.getEventDateField() != null
                             && record.getEventDateField().isComparable() ) {
                            recordDate = record.getEventDateField();
                        } else {
                            continue;
                        }
                    }

                    if ( indi.getBirthDate() == null && indi.getBirthDate().isComparable() ) {
                        continue;
                    } else {
                        indiDate = indi.getBirthDate();
                    }

                    if ( recordDate.getStart().compareTo(indiDate.getStart()) == 0 ) {
                        sameIndis.add(indi);
                    }
                }
            }
            return sameIndis;
        }
    }


    abstract protected class MergeModel extends AbstractTableModel {

        final int R_IndiFirstName = 0;
        private class MergeRow {

            String label;
            Object entityValue;
            Object recordValue;
            Object[] entityChoice;
            Object[] recordChoice;
            boolean merge;
        }

        HashMap<RowType,MergeRow> data = new HashMap<RowType,MergeRow>();
        List<MergeRow>   dataList = new ArrayList<MergeRow>();

        void setData(RowType rowType, String label, String recordValue, String entityValue) {
            MergeRow mergeRow = new MergeRow();
            mergeRow.label = label;
            mergeRow.entityValue = entityValue;
            mergeRow.recordValue = recordValue;
            mergeRow.merge = true;
            mergeRow.entityChoice = null;
            mergeRow.recordChoice = null;
            data.put(rowType,mergeRow);
            dataList.add(mergeRow);
        }

        void setData(RowType rowType, String label, Object[] recordValues, Object[] entityValues) {
            // je recherche les c
            Object defaultRecordValue = recordValues.length > 0 ? recordValues[0] : null;
            Object defaultEntityValue = entityValues.length > 0 ? entityValues[0] : null;
            // je verifie si une source du releve correspond a une source de l'entité
            for(Object recordValue : recordValues ) {
                for( Object entityValue : entityValues) {
                    if ( recordValue.equals(entityValue)) {
                        defaultEntityValue = entityValue;
                        defaultRecordValue = recordValue;
                    }
                }
            }

            MergeRow mergeRow = new MergeRow();
            data.put(rowType,mergeRow);
            dataList.add(mergeRow);
            mergeRow.label = label;
            mergeRow.entityValue = defaultEntityValue;
            mergeRow.recordValue = defaultRecordValue;
            mergeRow.merge = true;
            mergeRow.entityChoice = entityValues;
            mergeRow.recordChoice = recordValues;

        }

        private MergeRow getRow(RowType rowType) {
            return data.get(rowType);
        }
//        private Object[] getRecordChoice(int rowNum) {
//            return data[rowNum].recordChoice;
//        }
//
//        private Object getEntityValue(int rowNum) {
//            return data[rowNum].entityValue;
//        }
//        private Object[] getEntityChoice(int rowNum) {
//            return data[rowNum].entityChoice;
//        }

        abstract void saveData();
        abstract List<Indi> findSameIndi();
        
        
        private String[] columnNames = {"", "Relevé", "=>", "Individu"};
        private Class[] columnClass = {String.class, Object.class, Boolean.class, Object.class};
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return dataList.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class getColumnClass(int col) {
            return columnClass[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch(col) {
                case 0: return dataList.get(row).label;
                case 1: return dataList.get(row).recordValue;
                case 2: return dataList.get(row).merge;
                case 3: return dataList.get(row).entityValue;
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch( col) {
                case 1:
                    return dataList.get(row).recordChoice != null;
                case 2:
                    return true;
                case 3:
                    return dataList.get(row).entityChoice != null;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            switch(col) {
                case 0:  break;
                case 1:  dataList.get(row).recordValue = value; break;
                case 2:  dataList.get(row).merge = (Boolean) value; break;
                case 3:  dataList.get(row).recordValue = value; break;
                default:  break;
            }
            fireTableCellUpdated(row, col);
        }

        void check(int rowNum, boolean state) {
            dataList.get(rowNum).merge = state;
        }

        boolean isChecked(RowType rowType) {
            return data.get(rowType).merge;
        }


        private Object[] getChoice(int row, int col) {
            switch(col) {
                case 0: return null;
                case 1: return dataList.get(row).recordChoice;
                case 2: return null;
                case 3: return dataList.get(row).entityChoice;
                default: return null;
            }
        }
    }

   private void initData(Indi indi, RecordDeath recordDeath ) {

   }

   private void initData(Indi indi, RecordMarriage recordMarriage ) {

   }

   private void initData(Fam fam, RecordBirth recordBirth ) {

   }

   static enum RowType {
        Source,
        //  indi ///////////////////////////////////////////////////////////////////
        IndiFirstName,
        IndiLastName,
        IndiSex,
        IndiAge,
        IndiBirthDate,
        IndiPlace,
        IndiOccupation,
        IndiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        //indiMarriedSex,
        indiMarriedDead,
        indiMarriedOccupation,
        indiMarriedComment,
        //  indi father ////////////////////////////////////////////////////////////
        indiFatherFirstName,
        indiFatherLastName,
        indiFatherDead,
        indiFatherOccupation,
        indiFatherComment,
        indiMotherFirstName,
        indiMotherLastName,
        indiMotherDead,
        indiMotherOccupation,
        indiMotherComment,
        //  wife ///////////////////////////////////////////////////////////////////
        wifeFirstName,
        wifeLastName,
        wifeSex,
        //wifeDead,
        wifeAge,
        wifeBirthDate,
        wifePlace,
        wifeOccupation,
        wifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        //wifeMarriedSex,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        wifeFatherFirstName,
        wifeFatherLastName,
        wifeFatherDead,
        wifeFatherOccupation,
        wifeFatherComment,
        wifeMotherFirstName,
        wifeMotherLastName,
        wifeMotherDead,
        wifeMotherOccupation,
        wifeMotherComment,
        // wintness ///////////////////////////////////////////////////////////////
        witness1FirstName,
        witness1LastName,
        witness1Occupation,
        witness1Comment,
        witness2FirstName,
        witness2LastName,
        witness2Occupation,
        witness2Comment,
        witness3FirstName,
        witness3LastName,
        witness3Occupation,
        witness3Comment,
        witness4FirstName,
        witness4LastName,
        witness4Occupation,
        witness4Comment
    }

   /**
    * retourne les sources dont le titre correspond à la commune ou au notaire
    *    codecommune nom_commune BMS
    *    codecommune nom_commune Etat civil
    *    codecommune nom_commune Notaire Notaire_nom Notaire_prenom
    * @param gedcom
    * @param record
    * @return
    */
   private Source[] findSources(Gedcom gedcom, Record record)  {
        List<Source> matchedSources = new ArrayList<Source>();
        Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
        
        String cityName = record.getEventPlace().getCityName();
        String cityCode = record.getEventPlace().getCityCode();
        String countyName = record.getEventPlace().getCountyName();
        String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
        Pattern pattern = Pattern.compile(stringPatter);

        for (Entity source : sources) {
            if (pattern.matcher(((Source)source).getTitle()).matches()) {
                matchedSources.add((Source)source);
            }
        }

        if ( matchedSources.isEmpty() ) {
            Source source = new Source("SOUR","");
            source.addSimpleProperty("TITL", String.format("%s %s Etat civil", cityCode, cityName),1);
            matchedSources.add(source);
        }
        return matchedSources.toArray(new Source[matchedSources.size()]);
    }

    private class MergeTable extends JTable {

        private MergeTable(MergeModel model) {
            super(model);
            setDefaultRenderer(Object.class, new MergeTableRenderer());
            setPreferredSize(null);
        }

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            //int modelColumn = convertColumnIndexToModel(column);
            int modelColumn = column;

            if ((modelColumn == 1 || modelColumn == 2 ) &&  model.getChoice(row, modelColumn) != null ) {
                JComboBox comboBox = new JComboBox(model.getChoice(row,modelColumn));
                comboBox.setRenderer(new MergeCellComboRenderer() );
                return new DefaultCellEditor(comboBox);
            } else {
                return super.getCellEditor(row, column);
            }
        }
    }

    private class MergeTableRenderer extends JLabel implements TableCellRenderer {

        public MergeTableRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            //int modelColumn = table.convertColumnIndexToModel(column);
            int modelColumn = column;
            if ( value != null ) {
                setText(value.toString());
            } else {
                setText("");
            }

            switch (modelColumn) {
                case 0:
                    setBackground(Color.lightGray);
                    setForeground(table.getForeground());
                    break;
                case 1:
                case 2:
                case 3:
                    if ( model.dataList.get(row).entityValue == null) {
                        setBackground(table.getBackground());
                    } else if ( model.dataList.get(row).entityValue == null) {
                        setBackground(table.getBackground());
                    } else if ( model.dataList.get(row).entityValue.equals(model.dataList.get(row).recordValue)) {
                        setBackground(Color.GREEN);
                    } else {
                        setBackground(Color.ORANGE);
                    }
                    setForeground(table.getForeground());
                    setOpaque(true);
                    break;
                default:
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                    setOpaque(true);
                    break;
             }
             return this;

        }
    }

    private class MergeCellComboRenderer extends JLabel implements ListCellRenderer {

        public MergeCellComboRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value != null) {
                if (value instanceof Source) {
                    setText(((Source) value).toString());
                } else {
                    setText(value.toString());
                }
            } else {
                setText("");
            }

            return this;
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaMessage = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setPreferredSize(null);
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(null);

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

        jPanel3.setPreferredSize(null);
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setPreferredSize(null);

        jTextAreaMessage.setColumns(20);
        jTextAreaMessage.setRows(2);
        jTextAreaMessage.setPreferredSize(null);
        jScrollPane2.setViewportView(jTextAreaMessage);

        jPanel3.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaMessage;
    // End of variables declaration//GEN-END:variables

}
