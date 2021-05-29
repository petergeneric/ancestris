package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ShelfNumberTableModel extends AbstractTableModel {

    List<Property> calnList = new ArrayList<>();
    String[] columnsName = {
        NbBundle.getMessage(ShelfNumberTableModel.class, "CalnTableModel.column.title.caln"),
        NbBundle.getMessage(ShelfNumberTableModel.class, "CalnTableModel.column.title.medi"),
    };

    public ShelfNumberTableModel() {
    }

    @Override
    public int getRowCount() {
        return calnList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < calnList.size()) {
            final Property propertyCaln = calnList.get(row);
            switch (column) {
                case 0:
                    return propertyCaln.getValue();
                case 1:
                    Property propertyMedi = propertyCaln.getProperty("MEDI");
                    if (propertyMedi != null) {
                        return propertyMedi.getValue();
                    } else {
                        return "";
                    }
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }
    
    public String[] getColumnsName() {
        return columnsName;
    }
  
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    public void addAll(List<Property> calnList) {
        this.calnList.addAll(calnList);
        fireTableDataChanged();
    }

    public void add(Property caln) {
        this.calnList.add(caln);
        fireTableDataChanged();
    }

    public Property getValueAt(int row) {
        return calnList.get(row);
    }

    public Property remove(int row) {
        Property event = calnList.remove(row);
        fireTableDataChanged();
        return event;
    }

    public void clear() {
        calnList.clear();
        fireTableDataChanged();
    }
}
