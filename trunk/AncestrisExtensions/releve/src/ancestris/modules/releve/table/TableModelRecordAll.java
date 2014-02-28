package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.FieldPicture;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.PropertyDate;
import javax.swing.RowFilter;

/**
 *
 * @author Michel
 */
public class TableModelRecordAll extends TableModelRecordAbstract {

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
    public TableModelRecordAll(DataManager dataManager) {
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
                value = new Integer(row + 1);
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
                            value = record.getEventType().getName();
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
    
    @Override
    public RowFilter<TableModelRecordAbstract, Integer> getRecordFilter() {
        return new RowFilter<TableModelRecordAbstract, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModelRecordAbstract, ? extends Integer> entry) {
                return true;
            }
        };
    }

    @Override
    public String getModelName() {
        return "all";
    }


}
