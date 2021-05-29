package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.FieldDate;
import ancestris.modules.releve.model.FieldPicture;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.RecordType;
import javax.swing.RowFilter;

/**
 *
 * @author Michel
 */
public class TableModelRecordMisc extends TableModelRecordAbstract {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.EventType"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Participant1"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Participant2"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class<?> columnType[] = {Integer.class, FieldDate.class, String.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordMisc(DataManager dataManager) {
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
        return columnType[column];
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Object value;
        Record record = getRecord(row);
        switch (col) {
            case 0:
                value = row + 1;
                break;
            case 1:
                value = record.getField(FieldType.eventDate);
                break;
            case 2:
                value = record.getFieldValue(FieldType.eventType);
                break;
            case 3:
                value = record.getFieldValue(FieldType.indiLastName) + " " + record.getFieldValue(FieldType.indiFirstName);
                break;
            case 4:
                value = record.getFieldValue(FieldType.wifeLastName) + " " + record.getFieldValue(FieldType.wifeFirstName);
                break;
            case 5:
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
                return getRecord(entry.getIdentifier()).getType() == RecordType.MISC;
            }
        };
    }

    @Override
    public String getModelName() {
        return "misc";
    }
}
