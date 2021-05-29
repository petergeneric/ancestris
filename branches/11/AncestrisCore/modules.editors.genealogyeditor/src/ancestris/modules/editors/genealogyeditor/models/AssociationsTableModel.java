package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.PropertyXRef;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class AssociationsTableModel extends AbstractTableModel {

    List<PropertyAssociation> mPropertyAssociationList = new ArrayList<>();
    final String[] columnsName = {
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.associatedIndi.title"),
        NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.event.title"),
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
                    PropertyXRef target = propertyAssociation.getTarget();
                    if (target != null) {
                        if (target.getEntity() != null && target.getEntity() instanceof Indi) {
                            return "(" + target.getEntity().getId() + ")" + ((Indi) target.getEntity()).getName();
                        } else {
                            return target.getValue();
                        }
                    }
                }
                case 1: {
                    PropertyRelationship relation = (PropertyRelationship) propertyAssociation.getProperty("RELA");
                    if (relation != null) {
                        String value = relation.getValue();
                        int index = value.lastIndexOf(":");
                        if (index >= 0) {
                            return PropertyTag2Name.getTagName(value.substring(index + 1));
                        } else {
                            return "";
                        }
                    }
                }
                case 2: {
                    PropertyRelationship relation = (PropertyRelationship) propertyAssociation.getProperty("RELA");
                    if (relation != null) {
                        return relation.getDisplayValue();
                    }
                }
                case 3: {
                    if (propertyAssociation.getProperty("SOUR") != null) {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.source.value.yes");
                    } else {
                        return NbBundle.getMessage(AssociationsTableModel.class, "AssociationsTableModel.column.source.value.no");
                    }
                }
                case 4: {
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
    
    public String[] getColumnsName() {
        return columnsName;
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
