package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertyRepository;
import genj.gedcom.Repository;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class RepositoryCitationsTableModel extends AbstractTableModel {

    List<PropertyRepository> mRepositoriesList = new ArrayList<PropertyRepository>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "RepositoriesTableModel.column.name.title")
    };

    public RepositoryCitationsTableModel() {
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
            Repository repository = (Repository) mRepositoriesList.get(row).getTargetEntity();
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

    public void add(PropertyRepository entity) {
        this.mRepositoriesList.add(entity);
        fireTableDataChanged();
    }

    public void addAll(List<PropertyRepository> mRepositoriesList) {
        this.mRepositoriesList.addAll(mRepositoriesList);
        fireTableDataChanged();
    }

    public PropertyRepository remove(int row) {
        PropertyRepository repositoryCitation = mRepositoriesList.remove(row);
        fireTableDataChanged();
        return repositoryCitation;
    }
    
    public void clear() {
        this.mRepositoriesList.clear();
    }

    public PropertyRepository getValueAt(int row) {
        if (row < mRepositoriesList.size()) {
            return mRepositoriesList.get(row);
        } else {
            return null;
        }
    }
}
