package ancestris.modules.releve.table;

import ancestris.modules.releve.model.*;
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
        Record record = getRecord(row);
        switch (col) {
            case 0:
                value = row + 1;
                break;
            case 1:
                value = record.getIndi().getLastName().toString() + " " + record.getIndi().getFirstName().toString();
                break;
            case 2:
                value = record.getIndi().getSex();
                break;
            case 3:
                value = record.getEventDateProperty();
                break;
            case 4:
                value = record.getIndi().getAge();
                break;
            case 5:
                value = record.getIndi().getFatherLastName().toString() + " " + record.getIndi().getFatherFirstName().toString();
                break;
            case 6:
                value = record.getIndi().getMotherLastName().toString() + " " + record.getIndi().getMotherFirstName().toString();
                break;
            case 7:
                value = record.getIndi().getBirthPlace().toString();
                break;
            case 8:
                value = record.getCote() + " " + record.getFreeComment();
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
