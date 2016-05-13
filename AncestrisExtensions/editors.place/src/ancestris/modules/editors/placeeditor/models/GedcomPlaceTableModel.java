package ancestris.modules.editors.placeeditor.models;

import genj.gedcom.PropertyPlace;
import genj.util.ReferenceSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceTableModel extends AbstractTableModel {

    ReferenceSet gedcomPlacesMap = null;
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

    public void update(ReferenceSet gedcomPlacesMap) {
        this.gedcomPlacesMap = gedcomPlacesMap;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (gedcomPlacesMap == null) {
            return 0;
        }
        return gedcomPlacesMap.getKeys().size();
    }

    @Override
    public int getColumnCount() {
        return columsTitle.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (gedcomPlacesMap == null) {
            return "";
        }
        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        if (key.split(PropertyPlace.JURISDICTION_SEPARATOR).length > column) {
            String str = key.split(PropertyPlace.JURISDICTION_SEPARATOR)[column];
            return str.trim();
        } else {
            return "";
        }
    }

    @Override
    public Class getColumnClass(int column) {
        return String.class;
    }

    @Override
    public String getColumnName(int col) {
        return columsTitle[col];
    }

    public Set<PropertyPlace> getValueAt(int row) {
        if (gedcomPlacesMap == null) {
            return null;
        }

        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        return gedcomPlacesMap.getReferences(key);
    }
    
}
