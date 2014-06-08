package ancestris.modules.editors.placeeditor.models;

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
    String[] columsTitle;

    public GedcomPlaceTableModel(String[] placeFormat) {
        columsTitle = new String[placeFormat.length + 2];
        
        int index = 0;
        for (; index < placeFormat.length; index++) {
            columsTitle[index] = placeFormat[index];
        }
        
        columsTitle[index] = "Latitude";
        columsTitle[index + 1] = "Longitude";
    }

    @Override
    public int getRowCount() {
        return gedcomPlacesMap.size();
    }

    @Override
    public int getColumnCount() {
        return columsTitle.length;
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
        return columsTitle[col];
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
