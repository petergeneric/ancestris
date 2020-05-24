package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.ReferenceSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class GedcomPlaceTableModel extends AbstractTableModel {

    ReferenceSet<String, Property> mGedcomPlacesMap = new ReferenceSet<String, Property>();
    String[] placeFormat;

    public GedcomPlaceTableModel(String[] placeFormat) {
        this.placeFormat = placeFormat;
    }

    @Override
    public int getRowCount() {
        return mGedcomPlacesMap.getKeys().size();
    }

    @Override
    public int getColumnCount() {
        return placeFormat.length + 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object[] toArray = mGedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        if (column < placeFormat.length) {
            if (key.split(PropertyPlace.JURISDICTION_SEPARATOR).length > column) {
                return key.split(PropertyPlace.JURISDICTION_SEPARATOR)[column];
            } else {
                return "";
            }
        } else {
            Set<Property> references = mGedcomPlacesMap.getReferences(key);

            if (((PropertyPlace) references.toArray()[0]).getMap() != null) {
                if (column < placeFormat.length + 1) {
                    Property latitude = ((PropertyPlace) references.toArray()[0]).getLatitude(true);
                    return latitude != null ? latitude.getValue() : "";
                } else {
                    Property longitude = ((PropertyPlace) references.toArray()[0]).getLongitude(true);
                    return longitude != null ? longitude.getValue() : "";
                }
            }
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col < placeFormat.length) {
            return placeFormat[col];
        } else {
            if (col < placeFormat.length + 1) {
                return NbBundle.getMessage(FamiliesTableModel.class, "GedcomPlaceTableModel.column.latitude.title");
            } else {
                return NbBundle.getMessage(FamiliesTableModel.class, "GedcomPlaceTableModel.column.longitude.title");
            }
        }
    }

    public void update(ReferenceSet<String, Property> gedcomPlacesMap) {
        List<String> refSetKeys = gedcomPlacesMap.getKeys();

        for (String refSetKey : refSetKeys) {
            Set<Property> references = gedcomPlacesMap.getReferences(refSetKey);

            for (Property reference : references) {
                String key = refSetKey;

                if (((PropertyPlace)reference).getMap() != null) {
                    Property latitude = ((PropertyPlace)reference).getLatitude(true);
                    if (latitude != null) {
                        key += PropertyPlace.JURISDICTION_SEPARATOR + latitude.getValue();
                    }
                    Property longitude = ((PropertyPlace)reference).getLongitude(true);
                    if (longitude != null) {
                        key += PropertyPlace.JURISDICTION_SEPARATOR + longitude.getValue();
                    }
                }

                mGedcomPlacesMap.add(key, reference);
            }
        }

        fireTableDataChanged();
    }

    public Set<Property> getValueAt(int row) {
        Object[] toArray = mGedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        return mGedcomPlacesMap.getReferences(key);
    }
}
