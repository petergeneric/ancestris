/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class MediaPathTableModel extends AbstractTableModel {

    private MediaManagerPanel parent = null;
    private static int NBCOLUMNS = 5; //6;
    private String rootPath = "";

    private String[] columnNames = {
        NbBundle.getMessage(getClass(), "COL_NbMedia"),
        NbBundle.getMessage(getClass(), "COL_Found"),
        NbBundle.getMessage(getClass(), "COL_newPath"),
        NbBundle.getMessage(getClass(), "COL_searchPathButton"),
        NbBundle.getMessage(getClass(), "COL_makeRelative")
    };
    private Object[][] data;

    public MediaPathTableModel(MediaManagerPanel parent, Set<PathData> pathDataList, String rootPath) {
        this.parent = parent;
        this.rootPath = rootPath;
        if (pathDataList == null) {
            data = new Object[0][NBCOLUMNS];
            return;
        }
        resetData(pathDataList);
    }

    public void resetData(Set<PathData> pathDataList) {
        data = new Object[pathDataList.size()][NBCOLUMNS];
        int i = 0;
        for (PathData pathData : pathDataList) {
            data[i][0] = pathData.getNbMedia();
            data[i][1] = pathData.found;
            data[i][2] = pathData.newPath;
            data[i][3] = "...";
            data[i][4] = pathData.relative;
            i++;
        }
    }
    
    public void fireUpdateTable() {
        fireTableDataChanged();
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
        return (col > 2) ? true : false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = aValue;
        if (aValue instanceof Boolean) {
            Boolean rel = (Boolean) aValue;
            String newPath = (String) data[rowIndex][2];
            Path rootbase = Paths.get(rootPath);
            Path newpath = Paths.get(newPath);
            if (rel && !newpath.isAbsolute()) {
                return;
            }
            if (!rel && newpath.isAbsolute()) {
                return;
            }
            if (rel) {
                Path relativePath = rootbase.relativize(newpath);
                data[rowIndex][2] = relativePath.toString() + File.separator;
            } else {
                newpath = Paths.get(rootPath + File.separator + newPath);
                Path absolutePath = newpath.normalize();
                data[rowIndex][2] = absolutePath.toString() + File.separator;
            }
            fireTableCellUpdated(rowIndex, 2);// notify listeners
            parent.updateMap(rowIndex, (String) data[rowIndex][2]);
        }
    }

}
