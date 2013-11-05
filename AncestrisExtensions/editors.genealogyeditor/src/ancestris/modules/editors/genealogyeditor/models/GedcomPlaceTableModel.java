package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.PropertyPlace;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceTableModel extends AbstractTableModel {

    Map<String, Set<PropertyPlace>> gedcomPlacesMap = new HashMap<String, Set<PropertyPlace>>();
    String[] placeFormat;

    public GedcomPlaceTableModel(String[] placeFormat) {
        this.placeFormat = placeFormat;
    }

    @Override
    public int getRowCount() {
        return gedcomPlacesMap.size();
    }

    @Override
    public int getColumnCount() {
        return placeFormat.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object[] toArray = gedcomPlacesMap.keySet().toArray();
        String key = (String) toArray[row];
        if (key.split(PropertyPlace.JURISDICTION_SEPARATOR).length > column) {
            return key.split(PropertyPlace.JURISDICTION_SEPARATOR)[column];
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return placeFormat[col].toString();
    }

    public void update(Map<String, Set<PropertyPlace>> gedcomPlacesMap) {
        this.gedcomPlacesMap = gedcomPlacesMap;

        fireTableDataChanged();
    }
    
    public Set<PropertyPlace> getValueAt(int row) {
        Object[] toArray = gedcomPlacesMap.keySet().toArray();
        String key = (String) toArray[row];
        return gedcomPlacesMap.get(key);
    }
}
