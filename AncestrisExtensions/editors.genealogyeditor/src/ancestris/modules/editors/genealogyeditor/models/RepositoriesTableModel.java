package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.EntityTag2Name;
import genj.gedcom.Entity;
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

    List<Repository> repositoriesList = new ArrayList<Repository>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.name.title")
    };

    public RepositoriesTableModel() {
    }

    @Override
    public int getRowCount() {
        return repositoriesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < repositoriesList.size()) {
            Repository repository = repositoriesList.get(row);
            if (column == 0) {
                return repository.getId();
            } else if (column == 1) {
                return EntityTag2Name.getTagName(repository.getTag());
            } else {
                Property name = repository.getProperty("NAME");
                return name != null ? name.getValue() : "";
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
        this.repositoriesList.add(entity);
        fireTableDataChanged();
    }

    public void addAll(List<Repository> repositoriesList) {
        this.repositoriesList.addAll(repositoriesList);
        fireTableDataChanged();
    }

    public void update(List<Repository> repositoriesList) {
        this.repositoriesList.clear();
        addAll(repositoriesList);
    }

    public Entity getValueAt(int row) {
        if (row < repositoriesList.size()) {
            return repositoriesList.get(row);
        } else {
            return null;
        }
    }
}
