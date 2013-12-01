package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.PropertyName;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NamesTableModel extends AbstractTableModel {

    List<PropertyName> namesList = new ArrayList<PropertyName>();
    String[] columnsName = {
        NbBundle.getMessage(NamesTableModel.class, "NamesTableModel.column.FirstName.title"),
        NbBundle.getMessage(NamesTableModel.class, "NamesTableModel.column.LastName.title")};

    public NamesTableModel() {
    }

    @Override
    public int getRowCount() {
        return namesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        PropertyName propertyName = namesList.get(row);
        if (column == 0) {
            return propertyName.getFirstName(false);
        } else {
            return propertyName.getLastName(false);
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(PropertyName name) {
        namesList.add(name);
        fireTableDataChanged();
    }

    public void addAll(List<PropertyName> namesList) {
        this.namesList.addAll(namesList);
        fireTableDataChanged();
    }

    public void update(List<PropertyName> namesList) {
        this.namesList.clear();
        addAll(namesList);
    }

    public PropertyName getValueAt(int row) {
        return namesList.get(row);
    }
}
