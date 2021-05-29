package ancestris.modules.editors.geoplace;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ReferencesTableModel extends AbstractTableModel {

    String[] referencesTablecolumnNames = { NbBundle.getMessage(getClass(), "COL_Event"), NbBundle.getMessage(getClass(), "COL_Description") };
    protected ArrayList<PropertyPlace> referencesTableValues;

    public ReferencesTableModel() {
        referencesTableValues = new ArrayList<PropertyPlace>();
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
        if (row < getRowCount()) {
            PropertyPlace place = referencesTableValues.get(row);
            Entity entity = place.getEntity();
            Property parent = place.getParent();
            if (entity != null && parent != null) {
                if (column == 0) {
                    String str = parent.getPropertyName(); 
                    if (str.contains(" ")) {
                        return str.substring(0, str.indexOf(" ")); // only take first word
                    } else {
                        return str;
                    }
                } else {
                    return entity.toString();
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public Entity getValueAt(int row) {
        if (row < getRowCount()) {
            PropertyPlace place = referencesTableValues.get(row);
            return place.getEntity();
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return referencesTablecolumnNames[column];
    }

    public void addRow(PropertyPlace place) {
        referencesTableValues.add(place);
    }
    
    public void clear() {
        referencesTableValues.clear();
    }
    
}
