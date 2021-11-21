package ancestris.modules.releve.table;

import ancestris.modules.releve.model.*;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record.RecordType;
import javax.swing.RowFilter;

/**
 *
 * @author Michel
 */
public class TableModelRecordDeath extends TableModelRecordAbstract {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Name"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Sex"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Age"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Father"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Mother"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Place") ,
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class<?> columnClass[] = {Integer.class, String.class, FieldSex.class, FieldDate.class, String.class, String.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordDeath(DataManager dataManager) {
        super(dataManager);
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
        return columnClass[column];
    }

   @Override
    public Object getValueAt(int row, int col) {
        Object value;
        ancestris.modules.releve.model.Record record = getRecord(row);
        switch (col) {
            case 0:
                value = row + 1;
                break;
            case 1:
                value = record.getFieldValue(FieldType.indiLastName) + " " + record.getFieldValue(FieldType.indiFirstName);
                break;
            case 2:
                value = record.getFieldValue(FieldType.indiSex);
                break;
            case 3:
                value = record.getField(FieldType.eventDate);
                break;
            case 4:
                value = record.getFieldString(FieldType.indiAge);
                break;
            case 5:
                value = record.getFieldValue(FieldType.indiFatherLastName) + " " + record.getFieldValue(FieldType.indiFatherFirstName);
                break;
            case 6:
                value = record.getFieldValue(FieldType.indiMotherLastName) + " " + record.getFieldValue(FieldType.indiMotherFirstName);
                break;
            case 7:
                value = record.getFieldValue(FieldType.indiBirthPlace);
                break;
            case 8:
                value = record.getFieldValue(FieldType.cote) + " " + record.getFieldValue(FieldType.freeComment);
                break;
            default:
                value = super.getValueAt(row, col);
                break;
        }
        return value;
    }

    

    @Override
    public RowFilter<TableModelRecordAbstract, Integer> getRecordFilter() {
        return new RowFilter<TableModelRecordAbstract, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModelRecordAbstract, ? extends Integer> entry) {
                return getRecord(entry.getIdentifier()).getType() == RecordType.DEATH;
            }
        };
    }

    @Override
    public String getModelName() {
        return "death";
    }

}
