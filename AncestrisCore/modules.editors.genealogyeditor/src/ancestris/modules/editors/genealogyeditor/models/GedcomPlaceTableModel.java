package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.ReferenceSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceTableModel extends AbstractTableModel {

    ReferenceSet<String, Property> gedcomPlacesMap = new ReferenceSet<String, Property>();
    String[] placeFormat;

    public GedcomPlaceTableModel(String[] placeFormat) {
        this.placeFormat = placeFormat;
    }

    @Override
    public int getRowCount() {
        return gedcomPlacesMap.getKeys().size();
    }

    @Override
    public int getColumnCount() {
        return placeFormat.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
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

    public void update(ReferenceSet<String, Property> gedcomPlacesMap) {
        this.gedcomPlacesMap = gedcomPlacesMap;

        fireTableDataChanged();
    }
    
    public Set<Property> getValueAt(int row) {
        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        return gedcomPlacesMap.getReferences(key);
    }
}
