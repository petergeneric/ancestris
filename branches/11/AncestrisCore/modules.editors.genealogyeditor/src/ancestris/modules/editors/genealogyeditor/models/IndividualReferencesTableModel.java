package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Indi;
import genj.gedcom.PropertyXRef;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class IndividualReferencesTableModel extends AbstractTableModel {

    private final List<PropertyXRef> individualsList = new ArrayList<>();
    private final String[] columnsName = {
        NbBundle.getMessage(IndividualReferencesTableModel.class, "IndividualsTableModel.column.ID.title"),
        NbBundle.getMessage(IndividualReferencesTableModel.class, "IndividualsTableModel.column.Name.title"),
        NbBundle.getMessage(IndividualReferencesTableModel.class, "IndividualsTableModel.column.Sex.title"),
        NbBundle.getMessage(IndividualReferencesTableModel.class, "IndividualsTableModel.column.BirthDate.title"),
        NbBundle.getMessage(IndividualReferencesTableModel.class, "IndividualsTableModel.column.DeathDate.title")
    };
    private final String[] sex = {
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.UNKNOWN"),
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.MALE"),
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.FEMALE")
    };

    public IndividualReferencesTableModel() {
    }

    @Override
    public int getRowCount() {
        return individualsList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < individualsList.size()) {
            PropertyXRef individualReference = individualsList.get(row);
            if (individualReference.getTargetEntity() instanceof Indi) {
                Indi individual = (Indi) individualReference.getTargetEntity();
                switch (column) {
                    case 0:
                        return individual.getId();
                    case 1:
                        return individual.getName();
                    case 2:
                        return sex[individual.getSex()];
                    case 3:
                        return individual.getBirthAsString();
                    case 4:
                        return individual.getDeathAsString();
                    default:
                        return "";
                }
            } else {
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

    public void add(PropertyXRef individual) {
        this.individualsList.add(individual);
        fireTableDataChanged();
    }

    public void addAll(List<? extends PropertyXRef> individualsList) {
        this.individualsList.addAll(individualsList);
        fireTableDataChanged();
    }

    public PropertyXRef remove(int row) {
        PropertyXRef individual = individualsList.remove(row);
        fireTableDataChanged();
        return individual;
    }

    public PropertyXRef getValueAt(int row) {
        return individualsList.get(row);
    }
    
    public void clear() {
        individualsList.clear();
        fireTableDataChanged();
    }
}
