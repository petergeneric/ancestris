package ancestris.modules.releve.table;

import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author Michel
 */
/**
     * Class which extends the functionality of DefaultColumnTableModel to
     * also provide capabilities to group columns. This can be used for
     * instance to aid in the layout of groupable table headers.
     */
    public class GroupableTableColumnModel extends DefaultTableColumnModel {

        /**
         * Hold the list of ColumnGroups which define what group each normal
         * column is within, if any.
         */
        protected ArrayList<ColumnGroup> columnGroups = new ArrayList<ColumnGroup> ();

        /**
         * Add a new columngroup.
         * @param columnGroup new ColumnGroup
         */
        public void addColumnGroup(ColumnGroup columnGroup) {
            columnGroups.add(columnGroup);
        }

        /**
         * Provides an Iterator to iterate over the
         * ColumnGroup list.
         * @return Iterator over ColumnGroups
         */
        public Iterator<ColumnGroup> columnGroupIterator() {
            return columnGroups.iterator();
        }

        /**
         * Returns a ColumnGroup specified by an index.
         * @param index index of ColumnGroup
         * @return ColumnGroup
         */
        public ColumnGroup getColumnGroup(int index) {
            if (index >= 0 && index < columnGroups.size()) {
                return columnGroups.get(index);
            }
            return null;
        }

        /**
         * Provides and iterator for accessing the ColumnGroups
         * associated with a column.
         * @param col Column
         * @return ColumnGroup iterator
         */
        public Iterator<TableColumn> getColumnGroups(TableColumn col) {
            if (columnGroups.isEmpty()) {
                return null;
            }
            Iterator<ColumnGroup> iter = columnGroups.iterator();
            while (iter.hasNext()) {
                ColumnGroup cGroup = iter.next();
                ArrayList<TableColumn> v_ret = cGroup.getColumnGroups(col, new ArrayList<TableColumn>());
                if (v_ret != null) {
                    return v_ret.iterator();
                }
            }
            return null;
        }
    }
