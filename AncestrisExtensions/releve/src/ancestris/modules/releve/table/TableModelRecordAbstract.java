package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordModelListener;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;



/**
 *
 * @author Michel
 */
public abstract class TableModelRecordAbstract extends AbstractTableModel implements RecordModelListener {

    private DataManager dataManager;


    public TableModelRecordAbstract(DataManager dataManager) {
        this.dataManager = dataManager;
        dataManager.getDataModel().addRecordModelListener(this);
        fireTableDataChanged();
    }

    public void destroy() {
        dataManager.getDataModel().removeRecordModelListener(this);
    }

    public Record getRecord(int index) {
        return dataManager.getDataModel().getRecord(index);
    }

    public int addRecord(Record record) {
        return dataManager.addRecord(record);
    }

//    public String verifyRecord(int index) {
//        return dataManager.verifyRecord(index);
//    }

    public String getPlace() {
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

    ///////////////////////////////////////////////////////////////////////////
    // Implement RecordModelListener methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void recordInserted(int firstIndex, int lastIndex) {
        fireTableRowsInserted(firstIndex, lastIndex);
        //fireTableDataChanged();
    }

    @Override
    public void recordDeleted(int firstIndex, int lastIndex) {
        fireTableRowsDeleted(firstIndex, lastIndex);
    }

    @Override
    public void recordUpdated(int firstIndex, int lastIndex) {
        //TODO : pouquoi fireTableRowsUpdated ne met pas à jour toutes les tables
        fireTableRowsUpdated(firstIndex, lastIndex);
        //fireTableCellUpdated(lastIndex, 0);
        //fireTableDataChanged();
    }

    @Override
    public void allChanged() {
        fireTableDataChanged();
    }



}
