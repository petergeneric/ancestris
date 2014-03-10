package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.PropertyAssociation;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class AssociationsTableModel extends AbstractTableModel {

    List<PropertyAssociation> propertyAssociationList = new ArrayList<PropertyAssociation>();
    String[] columnsName = {
        NbBundle.getMessage(EventsTableModel.class, "AssociationsTableModel.column.eventType.title"),
        NbBundle.getMessage(EventsTableModel.class, "AssociationsTableModel.column.ID.date")
    };

    public AssociationsTableModel() {
    }

    @Override
    public int getRowCount() {
        return propertyAssociationList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        PropertyAssociation propertyAssociation = propertyAssociationList.get(row);
        if (column == 0) {
            return PropertyTag2Name.getTagName(propertyAssociation.getTag());
        } else {
            return propertyAssociation.getDisplayValue();
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public PropertyAssociation getValueAt(int row) {
        return propertyAssociationList.get(row);
    }
    
    public void add(PropertyAssociation propertyAssociation) {
        propertyAssociationList.add(propertyAssociation);
        fireTableDataChanged();
    }

    public void addAll(List<PropertyAssociation> propertyAssociationList) {
        propertyAssociationList.addAll(propertyAssociationList);
        fireTableDataChanged();
    }
    public PropertyAssociation remove(int row) {
        PropertyAssociation source = propertyAssociationList.remove(row);
        fireTableDataChanged();
        return source;
    }
}
