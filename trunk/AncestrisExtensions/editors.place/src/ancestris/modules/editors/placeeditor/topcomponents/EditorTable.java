package ancestris.modules.editors.placeeditor.topcomponents;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
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

    private Registry registry = null;
    private String mTableId = null;
    private TableRowSorter<TableModel> sorter = null;

    public EditorTable() {
        super();
    }

    public EditorTable(TableModel dm) {
        super(dm);
    }

    public void setID(Gedcom gedcom, String tableId, int selectedColumn) {
        registry = gedcom.getRegistry();
        mTableId = tableId;
        for (int index = 0; index < columnModel.getColumnCount(); index++) {
            int columnSize = registry.get(mTableId + ".column" + index + ".size", 100);
            columnModel.getColumn(index).setPreferredWidth(columnSize);
        }
        getColumnModel().addColumnModelListener(new EditorTableTableColumnModelListener());

        
        // Set sorter 
        // FL: 2016-02-28 : for some unknown reason, default row sorter sorts strings excluding spaces... We also need to sort case insensitive
        final Collator collator = gedcom.getCollator(); // case insensitive and local characters accounted for (but spaces ignored ???)
        Comparator strComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return collator.compare(o1.toString().replace(" ", "!"), o2.toString().replace(" ", "!"));   // fix space comparison ("!" is next to "space" in ASCII table)
            }
        };
        sorter = new TableRowSorter<TableModel>(getModel());
        for (int c = 0; c < getColumnModel().getColumnCount(); c++) {
            sorter.setComparator(c, strComparator);
        }
        setRowSorter(sorter);
   
        // Sort on selectedColumn first, then others starting from the first (otherwise other column do not benefit from the proper sorter/comparator above)
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add( new RowSorter.SortKey(selectedColumn, SortOrder.ASCENDING) );
        for (int c = 0; c < getColumnCount() ; c++) {
            if (c != selectedColumn) {
                sortKeys.add( new RowSorter.SortKey(c, SortOrder.ASCENDING) );
            }
        }
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        
    }
    
    public TableRowSorter<TableModel> getSorter() {
        return sorter;
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
                registry.put(mTableId + ".column" + index + ".size", columnModel.getColumn(index).getPreferredWidth());
            }
        }
        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }

    
    
}
