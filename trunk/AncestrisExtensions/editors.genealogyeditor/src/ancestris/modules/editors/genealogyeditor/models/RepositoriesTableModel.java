package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.Repository;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class RepositoriesTableModel extends AbstractTableModel {

    List<Repository> mRepositoriesList = new ArrayList<Repository>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.name.title")
    };

    public RepositoriesTableModel() {
    }

    @Override
    public int getRowCount() {
        return mRepositoriesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < mRepositoriesList.size()) {
            Repository repository = mRepositoriesList.get(row);
            if (column == 0) {
                return repository.getId();
            } else if (column == 1) {
                Property name = repository.getProperty("NAME");
                return name != null ? name.getValue() : "";
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

    public void add(Repository entity) {
        this.mRepositoriesList.add(entity);
        fireTableDataChanged();
    }

    public void addAll(List<Repository> mRepositoriesList) {
        this.mRepositoriesList.addAll(mRepositoriesList);
        fireTableDataChanged();
    }

    public Property remove(int row) {
        Property note = mRepositoriesList.remove(row);
        fireTableDataChanged();
        return note;
    }
    
    public void clear() {
        this.mRepositoriesList.clear();
    }

    public Repository getValueAt(int row) {
        if (row < mRepositoriesList.size()) {
            return mRepositoriesList.get(row);
        } else {
            return null;
        }
    }
}
