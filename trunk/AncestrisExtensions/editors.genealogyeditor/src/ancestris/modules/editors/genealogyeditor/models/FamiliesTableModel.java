package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Fam;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class FamiliesTableModel extends AbstractTableModel {

    private List<Fam> familiesList = new ArrayList<Fam>();
    private String[] columnsName = {
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.column.ID.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.column.husband.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.column.wife.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.column.weddingDate.title")
    };

    public FamiliesTableModel() {
    }

    @Override
    public int getRowCount() {
        return familiesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < familiesList.size()) {
            Fam family = familiesList.get(row);
            if (column == 0) {
                return family.getId();
            } else if (column == 1) {
                return family.getHusband() != null ? family.getHusband().getName() : "";
            } else if (column == 2) {
                return family.getWife() != null ? family.getWife().getName() : "";
            } else if (column == 3) {
                return family.getMarriageDate() != null ? family.getMarriageDate().toString() : "";
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

    public void add(Fam family) {
        this.familiesList.add(family);
        fireTableDataChanged();
    }

    public void addAll(List<Fam> familiesList) {
        this.familiesList.addAll(familiesList);
        fireTableDataChanged();
    }

    public Fam remove(int row) {
        Fam fam = familiesList.remove(row);
        fireTableDataChanged();
        return fam;
    }

    public void update(List<Fam> familiesList) {
        this.familiesList.clear();
        addAll(familiesList);
    }

    public Fam getValueAt(int row) {
        return familiesList.get(row);
    }
}
