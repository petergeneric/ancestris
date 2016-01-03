/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import genj.gedcom.PropertySex;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
class AssoWithTableModel extends AbstractTableModel {

    private static int NBCOLUMNS = 7;
        
    private String[] columnNames = {
        NbBundle.getMessage(getClass(), "COL_event"),
        NbBundle.getMessage(getClass(), "COL_rela"),
        NbBundle.getMessage(getClass(), "COL_Indi"),
        NbBundle.getMessage(getClass(), "COL_Lastname"),
        NbBundle.getMessage(getClass(), "COL_Firstname"),
        NbBundle.getMessage(getClass(), "COL_Sex"),
        NbBundle.getMessage(getClass(), "COL_Occupation")
    };
    private Object[][] data;

    public AssoWithTableModel(List<AssoWrapper> assoSet) {
        if (assoSet == null || assoSet.isEmpty()) {
            data = new Object[1][NBCOLUMNS];
            return;
        }
        data = new Object[assoSet.size()][NBCOLUMNS];
        int i = 0;
        for (AssoWrapper asso : assoSet) {
            data[i][0] = asso.targetEventDesc;
            data[i][1] = asso.assoTxt;
            data[i][2] = asso.assoIndi;
            data[i][3] = asso.assoLastname;
            data[i][4] = asso.assoFirstname;
            data[i][5] = PropertySex.getImage(asso.assoSex);
            data[i][6] = asso.assoOccupation;
            i++;
        }
    }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
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
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }



}
