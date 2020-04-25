/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties.utils;

import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * Model of table for remote media.
 *
 * @author Zurga
 */
public class RemoteMediaTableModel extends AbstractTableModel {

    private static final int NBCOLUMNS = 3;
    private Object[][] data;
    private RemoteMediaManagerPanel rmmp;

    private String[] columnNames = {
        NbBundle.getMessage(getClass(), "REM_COL_MODIFIIER"),
        NbBundle.getMessage(getClass(), "COL_NbMedia"),
        NbBundle.getMessage(getClass(), "REM_COL_PATH")
    };
    
    public RemoteMediaTableModel() {
        data = new Object[0][NBCOLUMNS];
    }
    
    public RemoteMediaTableModel(Set<PathData> paths, RemoteMediaManagerPanel rMmP) {
        this();
        rmmp = rMmP;
        if (paths == null) {
           return;
        }
        data = new Object[paths.size()][NBCOLUMNS];
        
        int i = 0;
        for (PathData pd : paths) {
            data[i][0] = pd.found;
            data[i][1] = pd.getNbMedia();
            data[i][2] = pd.newPath;
            i++;
        }
        
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }
    
     @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c) != null ? getValueAt(0, c).getClass() : String.class;
    }
    
     @Override
    public boolean isCellEditable(int row, int col) {
        return col == 0;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = aValue;
        if (aValue instanceof Boolean) {
        rmmp.updatePath((String) data[rowIndex][2], (boolean) aValue);
        }
    }

}
