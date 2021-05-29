package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import genj.gedcom.PropertyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class FamilyNamesTableModel extends AbstractTableModel {

    List<PropertyEvent> familyNamesList = new ArrayList<>();
    String[] columnsName = {"Prefix", "name", "suffix"};

    public FamilyNamesTableModel() {
    }

    @Override
    public int getRowCount() {
        return familyNamesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < familyNamesList.size()) {
            PropertyEvent propertyEvent = familyNamesList.get(row);
            if (column == 0) {
                return PropertyTag2Name.getTagName(propertyEvent.getTag());
            } else {
                return propertyEvent.getDate() != null ? propertyEvent.getDate().getDisplayValue() : "";
            }
        } else {
            return "";
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void addAll(List<PropertyEvent> familyNamesList) {
        this.familyNamesList.addAll(familyNamesList);
        fireTableDataChanged();
    }

    public void add(PropertyEvent familyName) {
        this.familyNamesList.add(familyName);
        fireTableDataChanged();
    }

    public PropertyEvent getValueAt(int row) {
        return familyNamesList.get(row);
    }

    public void clear(List<PropertyEvent> familyNamesList) {
        this.familyNamesList.clear();
    }
}
