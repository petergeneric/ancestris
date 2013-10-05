package ancestris.modules.editors.placeeditor.models;

import ancestris.modules.gedcom.utilities.EntityTag2Name;
import genj.gedcom.Entity;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class ReferencesTableModel extends AbstractTableModel {

    String[] referencesTablecolumnNames = {"Id",
        "type",
        "description"};
    protected ArrayList<Entity> referencesTableValues;
            
    public ReferencesTableModel() {
        referencesTableValues = new ArrayList<Entity>();
    }

    @Override
    public int getRowCount() {
        return referencesTableValues.size();
    }

    @Override
    public int getColumnCount() {
        return referencesTablecolumnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Entity entity = referencesTableValues.get(row);
        if (column == 0) {
            return entity.getId();
        } else if (column == 1) {
            return EntityTag2Name.getTagName(entity.getTag());
        } else {
            return entity.toString(false);
        }
    }

    @Override
    public String getColumnName(int column) {
        return referencesTablecolumnNames[column];
    }
    
    public void addRow(Entity entity) {
        referencesTableValues.add(entity);
    }
}
