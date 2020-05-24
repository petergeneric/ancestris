package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Indi;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class IndividualsTableModel extends AbstractTableModel {

    private List<Indi> individualsList = new ArrayList<Indi>();
    private String[] columnsName = {
        NbBundle.getMessage(IndividualsTableModel.class, "IndividualsTableModel.column.ID.title"),
        NbBundle.getMessage(IndividualsTableModel.class, "IndividualsTableModel.column.Name.title"),
        NbBundle.getMessage(IndividualsTableModel.class, "IndividualsTableModel.column.Sex.title"),
        NbBundle.getMessage(IndividualsTableModel.class, "IndividualsTableModel.column.BirthDate.title"),
        NbBundle.getMessage(IndividualsTableModel.class, "IndividualsTableModel.column.DeathDate.title")
    };
    private String[] sex = {
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.UNKNOWN"),
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.MALE"),
        NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.FEMALE")
    };

    public IndividualsTableModel() {
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
            Indi individual = individualsList.get(row);
            if (column == 0) {
                return individual.getId();
            } else if (column == 1) {
                return individual.getName();
            } else if (column == 2) {
                return sex[individual.getSex()];
            } else if (column == 3) {
                return individual.getBirthAsString();
            } else if (column == 4) {
                return individual.getDeathAsString();
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

    public void add(Indi individual) {
        this.individualsList.add(individual);
        fireTableDataChanged();
    }

    public void addAll(List<Indi> individualsList) {
        this.individualsList.addAll(individualsList);
        fireTableDataChanged();
    }

    public void clear(List<Indi> individualsList) {
        this.individualsList.clear();
    }

    public Indi getValueAt(int row) {
        return individualsList.get(row);
    }
}
