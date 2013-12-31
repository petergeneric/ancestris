package ancestris.modules.editors.genealogyeditor.table;

import genj.util.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author dominique
 */
public class EditorTable extends JTable {

    private final static Logger logger = Logger.getLogger(EditorTable.class.getName(), null);
    private Registry mRegistry = null;
    private String mTableId;
    private TableColumnModel mColumnModel;

    public EditorTable() {
        super();
        mColumnModel = getColumnModel();
    }

    public EditorTable(TableModel dm) {
        super(dm);
        mColumnModel = getColumnModel();
    }

    public void setID(String tableId) {
        logger.log(Level.INFO, "setID: {0}", tableId);
        mRegistry = Registry.get(EditorTable.class);
        mTableId = tableId;
        for (int index = 0; index < columnModel.getColumnCount(); index++) {
            int columnSize = mRegistry.get(mTableId + ".column" + index + ".size", 100);
            columnModel.getColumn(index).setPreferredWidth(columnSize);
            logger.log(Level.INFO, "setID: table id {0} column index {1} size {2}", new Object[]{tableId, index, columnSize});
        }
        mColumnModel.addColumnModelListener(new EditorTableTableColumnModelListener());
    }

    private class EditorTableTableColumnModelListener implements TableColumnModelListener {

        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnAdded: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnRemoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMoved(TableColumnModelEvent tcme) {
            logger.log(Level.INFO, "columnMoved: {0}", tcme.getFromIndex());
        }

        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            logger.log(Level.INFO, "columnMarginChanged: {0}", ce.toString());
            for (int index = 0; index < columnModel.getColumnCount(); index++) {
                int preferredWidth = mColumnModel.getColumn(index).getPreferredWidth();
                logger.log(Level.INFO, "columnMarginChanged: table id {0} column index {1} size {2}", new Object[]{mTableId, index, preferredWidth});
                mRegistry.put(mTableId + ".column" + index + ".size", preferredWidth);
            }
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }
}
