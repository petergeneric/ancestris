package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordInfoPlace;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;



/**
 *
 * @author Michel
 */
public abstract class TableModelRecordAbstract extends AbstractTableModel {

    private final DataManager dataManager;


    public TableModelRecordAbstract(DataManager dataManager) {
        this.dataManager = dataManager;
        fireTableDataChanged();
    }

    public Record getRecord(int index) {
        return dataManager.getDataModel().getRecord(index);
    }

    public int addRecord(Record record) {
        return dataManager.addRecord(record);
    }

    public RecordInfoPlace getPlace() {
        return dataManager.getPlace();
    }
        
    // methodes abstraites
    public abstract RowFilter<TableModelRecordAbstract, Integer> getRecordFilter();
    public abstract String getModelName();


    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getRowCount() {
        return dataManager.getDataModel().getRowCount();
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Object value;
        switch (col) {
            case -1: 
                value = dataManager.getGedcomLink(dataManager.getDataModel().getRecord(row));
                break;                
            default: 
                value = "";
                break;
        }
        return value;
    }
}
