package ancestris.modules.releve.model;

import genj.gedcom.PropertyDate;

/**
 *
 * @author Michel
 */
public class ModelAll extends ModelAbstract {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.EventType"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Participant1"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Participant2"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class columnType[] = {Integer.class, PropertyDate.class, String.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public ModelAll() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement ModelAbstract methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * ajout un nouveau releve dans le modele
     * @return indexRecord
     */
    @Override
    public Record createRecord() {
        return new RecordMisc();
    }

    /**
     * constructeur de releve
     * Cette methode doit etre appelee par le constructeur specifique createRecord()
     * de chaque modele
     * @param record
     */
    @Override
    protected int addRecord(final Record record, boolean updateGui) {
        releveList.add(record);
        int recordIndex = releveList.size()-1;
         if (updateGui) {
            fireTableRowsInserted(recordIndex, recordIndex);
        }
        return recordIndex;
    }

    @Override
     protected void removeRecord(final Record record) {

        int recordIndex = releveList.indexOf(record);
        releveList.remove(record);
        fireTableRowsDeleted(recordIndex, recordIndex);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnName[col];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnType[column];
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Object value;
        Record record = getRecord(row);
        switch (col) {
            case 0:
                value = new Integer(record.recordNo);
                break;
            case 1:
                value = record.getEventDateProperty();
                break;
            case 2:
                {
                    if ( record instanceof RecordBirth) {
                        value = "N";
                    } else if ( record instanceof RecordMarriage) {
                        value = "M";
                    } else if ( record instanceof RecordDeath) {
                        value = "D";
                    } else {
                        if (record.getEventType() != null) {
                            value = record.getEventType().getTag();
                        } else {
                            value = "V";
                        }
                    }
                }
                break;
            case 3:
                value = record.getIndiLastName().toString() + " " + record.getIndiFirstName().toString();
                break;
            case 4:
                if ( record.getWifeLastName() != null) {
                    value = record.getWifeLastName().toString() + " " + record.getWifeFirstName().toString();
                } else {
                    value = "";
                }                
                break;
            case 5:
                value = record.getFreeComment();
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    
    /**
     * retourne la liste des champs affichables
     * @return
     */

    @Override
    public BeanField[] getFieldList( int recordIndex ) {
        
        Record record = getRecord(recordIndex);

        if( record == null)  {
            return new BeanField[0];
        }

         if (record instanceof RecordBirth) {
             return ModelBirth.getFieldList(record);
        } else if (record instanceof RecordMarriage) {
            return ModelMarriage.getFieldList(record);
        } else if (record instanceof RecordDeath) {
            return ModelDeath.getFieldList(record);
        } else {
            return ModelMisc.getFieldList(record);
        }
    }

}
