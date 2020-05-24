package ancestris.modules.editors.genealogyeditor.table;

import genj.util.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author dominique
 */
public class EditorTable extends JTable {

    private class EditorTableTableColumnModelListener implements TableColumnModelListener {

        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
            LOGGER.log(Level.FINE, "columnAdded: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
            LOGGER.log(Level.FINE, "columnRemoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMoved(TableColumnModelEvent tcme) {
            LOGGER.log(Level.FINE, "columnMoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            LOGGER.log(Level.FINE, "columnMarginChanged: {0}", ce.toString());
            for (int index = 0; index < columnModel.getColumnCount(); index++) {
                int preferredWidth = columnModel.getColumn(index).getPreferredWidth();
                LOGGER.log(Level.FINE, "columnMarginChanged: table id {0} column index {1} size {2}", new Object[]{mTableId, index, preferredWidth});
                mRegistry.put(mTableId + ".column" + index + ".size", preferredWidth);
            }
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }

    private class EditorTableRowSorterListener implements RowSorterListener {

        @Override
        public void sorterChanged(RowSorterEvent rse) {
            RowSorterEvent.Type type = rse.getType();
            if (type.equals(RowSorterEvent.Type.SORTED)) {
                List<RowSorter.SortKey> sortKeys = rse.getSource().getSortKeys();
                int index = 0;
                for (RowSorter.SortKey key : sortKeys) {
                    mRegistry.put(mTableId + ".column" + key.getColumn() + ".sortIndex", index++);
                    mRegistry.put(mTableId + ".column" + key.getColumn() + ".sortOrder", key.getSortOrder().toString());
                    LOGGER.log(Level.INFO, "sorterChanged SORTED: index {0} order {1}", new Object[]{key.getColumn(), key.getSortOrder().toString()});
                }
            } else if (type.equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                List<RowSorter.SortKey> sortKeys = rse.getSource().getSortKeys();
                int index = 0;
                for (RowSorter.SortKey key : sortKeys) {
                    mRegistry.put(mTableId + ".column" + key.getColumn() + ".sortIndex", index++);
                    mRegistry.put(mTableId + ".column" + key.getColumn() + ".sortOrder", key.getSortOrder().toString());
                    LOGGER.log(Level.INFO, "sorterChanged SORT_ORDER_CHANGED: index {0} order {1}", new Object[]{key.getColumn(), key.getSortOrder().toString()});
                }
            }
        }
    }
    private final static Logger LOGGER = Logger.getLogger(EditorTable.class.getName(), null);
    private Registry mRegistry = null;
    private String mTableId;

    public EditorTable() {
        super();
    }

    public EditorTable(TableModel dm) {
        super(dm);
    }

    public void setID(String tableId) {
        LOGGER.log(Level.FINE, "setID: {0}", tableId);
        mRegistry = Registry.get(EditorTable.class);
        mTableId = tableId;
        for (int index = 0; index < columnModel.getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            columnModel.getColumn(index).setPreferredWidth(columnSize);
            LOGGER.log(Level.FINE, "setID: table id {0} column index {1} size {2}", new Object[]{tableId, index, columnSize});
        }

        RowSorter<? extends TableModel> rowSorter = getRowSorter();
        if (getRowSorter() == null) {
            setRowSorter(new TableRowSorter<>(getModel()));
            rowSorter = getRowSorter();
        }

        rowSorter.addRowSorterListener(new EditorTableRowSorterListener());

        getColumnModel().addColumnModelListener(new EditorTableTableColumnModelListener());
    }
}
