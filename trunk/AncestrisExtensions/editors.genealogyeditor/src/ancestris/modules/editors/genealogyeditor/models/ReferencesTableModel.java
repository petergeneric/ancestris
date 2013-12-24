package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.EntityTag2Name;
import genj.gedcom.Entity;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ReferencesTableModel extends AbstractTableModel {

    List<Entity> entitiesList = new ArrayList<Entity>();
    private String[] columnsName = {
        NbBundle.getMessage(ReferencesTableModel.class, "ReferencesTableModel.column.ID.title"),
        NbBundle.getMessage(ReferencesTableModel.class, "ReferencesTableModel.column.Type.title"),
        NbBundle.getMessage(ReferencesTableModel.class, "ReferencesTableModel.column.Value.title")
    };

    public ReferencesTableModel() {
    }

    @Override
    public int getRowCount() {
        return entitiesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < entitiesList.size()) {
            Entity entity = entitiesList.get(row);
            if (column == 0) {
                return entity.getId();
            } else if (column == 1) {
                return EntityTag2Name.getTagName(entity.getTag());
            } else {
                return entity.toString(false);
            }
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(Entity entity) {
        this.entitiesList.add(entity);
        fireTableDataChanged();
    }

    public void addAll(List<Entity> entitiesList) {
        this.entitiesList.addAll(entitiesList);
        fireTableDataChanged();
    }

    public void update(List<Entity> entitiesList) {
        this.entitiesList.clear();
        addAll(entitiesList);
    }

    public Entity getValueAt(int row) {
        if (row < entitiesList.size()) {
            return entitiesList.get(row);
        } else {
            return null;
        }
    }
}
