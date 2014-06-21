package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class FamiliesTableModel extends AbstractTableModel {

    public static int FAMILY_LIST = 0;
    public static int FAMILY_CHILD = 1;
    public static int FAMILY_SPOUSE = 2;
    private int mFamilyTableType = FAMILY_CHILD;
    private List<Fam> familiesList = new ArrayList<Fam>();
    private static String[] familyListColumnsName = {
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyList.column.ID.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyList.column.husband.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyList.column.wife.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyList.column.weddingDate.title"),
    };
    private static String[] familyChildColumnsName = {
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyChild.column.ID.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyChild.column.husband.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyChild.column.wife.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyChild.column.weddingDate.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familyChild.column.children.title")
    };
    private static String[] familySpouseColumnsName = {
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familySpouse.column.ID.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familySpouse.column.husband.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familySpouse.column.wife.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familySpouse.column.weddingDate.title"),
        NbBundle.getMessage(FamiliesTableModel.class, "FamiliesTableModel.familySpouse.column.children.title")
    };
    private String[] columnsName;

    public FamiliesTableModel(int familyType) {
        mFamilyTableType = familyType;
        if (mFamilyTableType == FAMILY_CHILD) {
            columnsName = familyChildColumnsName;
        } else if (mFamilyTableType == FAMILY_SPOUSE) {
            columnsName = familySpouseColumnsName;
        } else {
            columnsName = familyListColumnsName;
        }
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
            } else if (column == 4) {
                String children = "";
                int index = 0;
                for (Indi child : family.getChildren(true)) {
                    if (index > 0) {
                        children += "\n";
                    }
                    children += child.getName();
                    index++;
                }
                return children;
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

    public void clear() {
        familiesList.clear();
        fireTableDataChanged();
    }

    public Fam getValueAt(int row) {
        return familiesList.get(row);
    }
}
