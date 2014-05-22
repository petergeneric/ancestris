/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.swing.atable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A customized RowSorter.
 *
 * @param <M> the type of the model, which must be an implementation of
 *            <code>TableModel</code>
 */
public class ATableRowSorter<M extends TableModel> extends TableRowSorter<M> {

    public ATableRowSorter(M model) {
        super(model);
    }

    private void checkColumn(int column) {
        if (column < 0 || column >= getModelWrapper().getColumnCount()) {
            throw new IndexOutOfBoundsException("column beyond range of TableModel");
        }
    }

    /**
     * Cycle the sort order from ascending to descending to none.
     * If
     * the specified column is not sortable, this method has no
     * effect.
     *
     * Copied from {@link DefaultSortOrder} source code.
     *
     * @param column index of the column to make the primary sorted column,
     *               in terms of the underlying model
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see #setSortable(int,boolean)
     * @see #setMaxSortKeys(int)
     */
    @Override
    public void toggleSortOrder(int column) {
    }

    public void toggleSortOrder(int column, boolean clear) {
        checkColumn(column);
        if (isSortable(column)) {
            List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
            SortKey sortKey;
            int sortIndex;
            for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
                if (keys.get(sortIndex).getColumn() == column) {
                    break;
                }
            }
            if (sortIndex == -1) {
                // Key doesn't exist
                sortKey = new SortKey(column, SortOrder.ASCENDING);
            } else {
                // Key is present
                sortKey = toggle(keys.get(sortIndex),keys.size()>1);
            }
            if (clear) {
                keys = new ArrayList<SortKey>(3);
                if (sortKey != null) {
                    keys.add(sortKey);
                }
            } else {
                if (sortKey == null) {
                    keys.remove(sortIndex);
                } else {
                    if (sortIndex == -1) {
                        keys.add(sortKey);
                    } else {
                        keys.set(sortIndex, sortKey);
                    }
                }
            }
            if (keys.size() > getMaxSortKeys()) {
                keys = keys.subList(0, getMaxSortKeys());
            }
            setSortKeys(keys);
        }
    }

    private SortKey toggle(SortKey key,boolean canBeUnsorted) {
        if (key.getSortOrder() == SortOrder.ASCENDING) {
            return new SortKey(key.getColumn(), SortOrder.DESCENDING);
        } else if (key.getSortOrder() == SortOrder.DESCENDING && canBeUnsorted) {
            return null;
        }
        return new SortKey(key.getColumn(), SortOrder.ASCENDING);
    }

}
