package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.FieldDate;
import ancestris.modules.releve.model.FieldPicture;
import ancestris.modules.releve.model.Record;
import javax.swing.RowFilter;

/**
 *
 * @author Michel
 */
public class TableModelRecordMarriage extends TableModelRecordAbstract {


    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Husband"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Wife"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class columnType[] = {Integer.class, FieldDate.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordMarriage(DataManager dataManager) {
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
                value = record.getEventDateProperty();
                break;
            case 2:
                value = record.getIndiLastName().toString() + " " + record.getIndiFirstName().toString();
                break;
            case 3:
                value = record.getWifeLastName().toString() + " " + record.getWifeFirstName().toString();
                break;
            case 4:
                value = record.getFreeComment();
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    @Override
    public RowFilter<TableModelRecordAbstract, Integer> getRecordFilter() {
        return new RowFilter<TableModelRecordAbstract, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModelRecordAbstract, ? extends Integer> entry) {
                if (getRecord(entry.getIdentifier()).getType() == DataManager.RecordType.marriage) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    public String getModelName() {
        return "marriage";
    }
}
