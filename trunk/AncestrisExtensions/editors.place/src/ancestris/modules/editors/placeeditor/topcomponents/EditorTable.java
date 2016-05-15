package ancestris.modules.editors.placeeditor.topcomponents;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.util.Comparator;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author dominique, simplified by frederic (performance issues of sort with over 2000 locations)
 */
public class EditorTable extends JTable {

    private Registry mRegistry = null;
    private String mTableId;

    public EditorTable() {
        super();
    }

    public EditorTable(TableModel dm) {
        super(dm);
    }

    public void setID(Gedcom gedcom, String tableId) {
        mRegistry = gedcom.getRegistry();
        mTableId = tableId;
        for (int index = 0; index < columnModel.getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            columnModel.getColumn(index).setPreferredWidth(columnSize);
        }
        getColumnModel().addColumnModelListener(new EditorTableTableColumnModelListener());

        
        // Set sorter 
        // FL: 2016-02-28 : for some unknown reason, default row sorter sorts strings excluding spaces... Using string sorter below solves the issue.
        // Returning getColumnClass as String does not solve the issue (!?!?)
        RowSorter<? extends TableModel> rowSorter = getRowSorter();
        if (getRowSorter() == null) {
            setRowSorter(new TableRowSorter<TableModel>(getModel()));
            rowSorter = getRowSorter();
        }
        Comparator strComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }
        };
        for (int c = 0; c < getColumnModel().getColumnCount(); c++) {
            ((TableRowSorter) rowSorter).setComparator(c, strComparator);
        }
    }
    

    private class EditorTableTableColumnModelListener implements TableColumnModelListener {
        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
        }
        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
        }
        @Override
        public void columnMoved(TableColumnModelEvent tcme) {
        }
        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            for (int index = 0; index < columnModel.getColumnCount(); index++) {
                mRegistry.put(mTableId + ".column" + index + ".size", columnModel.getColumn(index).getPreferredWidth());
            }
        }
        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }

    
    
}
