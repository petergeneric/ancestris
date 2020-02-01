/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class EventTableModel extends AbstractTableModel {

    private static int NBCOLUMNS = 3;
        
    private String[] columnNames = {
        NbBundle.getMessage(getClass(), "COL_event"),
        NbBundle.getMessage(getClass(), "COL_year"),
        NbBundle.getMessage(getClass(), "COL_age")
    };
    private Object[][] data;

    public EventTableModel(List<EventWrapper> eventSet) {
        if (eventSet == null || eventSet.isEmpty()) {
            data = new Object[1][NBCOLUMNS];
            return;
        }
        data = new Object[eventSet.size()][NBCOLUMNS];
        int i = 0;
        for (EventWrapper event : eventSet) {
            data[i][0] = event.eventLabel;
            data[i][1] = event.eventYear;
            data[i][2] = event.eventAge;
            i++;
        }
    }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data != null ? data.length : 0;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data != null ? data[row][col] : null;
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c) != null ? getValueAt(0, c).getClass() : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (data == null) {
                return;
            }
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        public void clear() {
            data = null;
        }

}
