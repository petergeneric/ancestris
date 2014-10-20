package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
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

    List<PropertyAssociation> mPropertyAssociationList = new ArrayList<PropertyAssociation>();
    final String[] columnsName = {
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.associatedIndi.title"),
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.relation.title"),
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.source.title"),
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.note.title")
    };

    public AssociationsTableModel() {
    }

    @Override
    public int getRowCount() {
        return mPropertyAssociationList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        PropertyAssociation propertyAssociation = mPropertyAssociationList.get(row);
        if (propertyAssociation != null) {
            switch (column) {
                case 0: {
                    return propertyAssociation.getDisplayValue();
                }
                case 1: {
                    Property relation = propertyAssociation.getProperty("RELA");
                    if (relation != null) {
                        return relation.getValue();
                    }
                }
                case 2: {
                    if (propertyAssociation.getProperty("SOUR") != null) {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.source.value.yes");
                    } else {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.source.value.no");
                    }
                }
                case 3: {
                    if (propertyAssociation.getProperty("NOTE") != null) {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.note.value.yes");
                    } else {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.note.value.no");
                    }
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

    public PropertyAssociation getValueAt(int row) {
        return mPropertyAssociationList.get(row);
    }

    public void add(PropertyAssociation propertyAssociation) {
        mPropertyAssociationList.add(propertyAssociation);
        fireTableDataChanged();
    }

    public void addAll(List<PropertyAssociation> propertyAssociationList) {
        mPropertyAssociationList.addAll(propertyAssociationList);
        fireTableDataChanged();
    }

    public PropertyAssociation remove(int row) {
        PropertyAssociation source = mPropertyAssociationList.remove(row);
        fireTableDataChanged();
        return source;
    }

    public void clear() {
        mPropertyAssociationList.clear();
    }
}
