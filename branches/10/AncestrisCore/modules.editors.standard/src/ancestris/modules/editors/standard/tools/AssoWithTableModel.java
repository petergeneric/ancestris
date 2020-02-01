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

import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.util.swing.ImageIcon;
import java.awt.FontMetrics;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
class AssoWithTableModel extends AbstractTableModel {

    private List<AssoWrapper> assoModelSet = null;

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
        assoModelSet = assoSet;
        
        if (assoSet == null || assoSet.isEmpty()) {
            addRow(0);
            return;
        }
        assoResetData();
    }

    private void assoResetData() {
        data = new Object[assoModelSet.size()][NBCOLUMNS];
        int i = 0;
        for (AssoWrapper asso : assoModelSet) {
            data[i][0] = asso.targetEvent;
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
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void addRow(int row) {
        int rowInserted = row+1;
        if (assoModelSet.isEmpty()) {
            rowInserted = 0;
        }
        assoModelSet.add(rowInserted, new AssoWrapper(""));
        assoResetData();
        fireTableRowsInserted(rowInserted, rowInserted);
    }

    public void removeRow(int row) {
        assoModelSet.remove(row);
        assoResetData();
        fireTableRowsDeleted(row, row);
    }

    public int getMaxWidth(FontMetrics fm, int column) {
        int ret = fm.stringWidth(getColumnName(column)) + 4;
        int rows = getRowCount();
        String str = "";
        for (int i = 0; i < rows; i++) {
            int width = 0;
            Object o = getValueAt(i, column);
            if (o != null) {
                switch (column) {
                    case 0:
                        str = ((EventWrapper) o).eventLabel.getLongLabel() + "MMMMMM";  // add size of an icon plus the drop-down button
                        break;
                    case 1:
                        str = ((String) o);
                        break;
                    case 2:
                        str = ((Indi) o).toString();
                        break;
                    case 3:
                        str = ((String) o);
                        break;
                    case 4:
                        str = ((String) o);
                        break;
                    case 5:
                        str = PropertySex.TXT_UNKNOWN + "MMMMMM"; // size of sex icon and label plus the drop-down button
                        break;
                    case 6:
                        str = ((String) o);
                        break;
                    default:
                        str = o.toString();
                        break;
                }
                width = fm.stringWidth(str) + 4;
                if (width > ret) {
                    ret = width;
                }
            }
        }
        return ret;
    }

    public boolean isChanged(Object data, int row, int column) {
        boolean ret = false;
        AssoWrapper asso = assoModelSet.get(row);
        switch (column) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                ret = !asso.assoLastname.equals((String) data);
                break;
            case 4:
                ret = !asso.assoFirstname.equals((String) data);
                break;
            case 5:
                ImageIcon icon = (ImageIcon) data;
                int newSex = (icon == PropertySex.getImage(PropertySex.MALE) ? PropertySex.MALE : icon == PropertySex.getImage(PropertySex.FEMALE) ? PropertySex.FEMALE : PropertySex.UNKNOWN);
                ret = asso.assoSex != newSex;
                break;
            case 6:
                ret = !asso.assoOccupation.equals((String) data);
                break;
        }
        return ret;
    }

    
    public void updateList(Object data, int row, int column) {
        AssoWrapper asso = assoModelSet.get(row);
        switch (column) {
            case 0:
                asso.targetEvent = ((EventWrapper) data);
                break;
            case 1:
                asso.assoTxt = ((String) data);
                break;
            case 2:
                asso.assoIndi = ((Indi) data);
                break;
            case 3:
                asso.assoLastname = ((String) data);
                break;
            case 4:
                asso.assoFirstname = ((String) data);
                break;
            case 5:
                ImageIcon icon = (ImageIcon) data;
                asso.assoSex = (icon == PropertySex.getImage(PropertySex.MALE) ? PropertySex.MALE : icon == PropertySex.getImage(PropertySex.FEMALE) ? PropertySex.FEMALE : PropertySex.UNKNOWN);
                break;
            case 6:
                asso.assoOccupation = ((String) data);
                break;
        }
    }

    public List<AssoWrapper> getSet() {
        return assoModelSet;
    }

    public void setIndiValues(Indi indi, int row) {
        setValueAt(indi.getLastName(), row, 3);
        setValueAt(indi.getFirstName(), row, 4);
        setValueAt(PropertySex.getImage(indi.getSex()), row, 5);
        setValueAt(assoModelSet.get(0).getOccupation(indi, (EventWrapper) getValueAt(row, 0)), row, 6);
    }


}
