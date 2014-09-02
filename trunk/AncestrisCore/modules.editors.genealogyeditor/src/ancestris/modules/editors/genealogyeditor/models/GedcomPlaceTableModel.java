package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.ReferenceSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

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
        return placeFormat.length + 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        if (column < placeFormat.length) {
            if (key.split(PropertyPlace.JURISDICTION_SEPARATOR).length > column) {
                return key.split(PropertyPlace.JURISDICTION_SEPARATOR)[column];
            } else {
                return "";
            }
        } else {
            Set<Property> references = gedcomPlacesMap.getReferences(key);
            String MAPTag = "";
            String LATITag = "";
            String LONGTag = "";
            if (((Property) references.toArray()[0]).getGedcom().getGrammar().getVersion().equals("5.5.1") == true) {
                MAPTag = "MAP";
                LATITag = "LATI";
                LONGTag = "LONG";
            } else {
                MAPTag = "_MAP";
                LATITag = "_LATI";
                LONGTag = "_LONG";
            }

            Property placeMap = ((Property) references.toArray()[0]).getProperty(MAPTag);
            if (placeMap != null) {
                if (column < placeFormat.length + 1) {
                    Property latitude = placeMap.getProperty(LATITag);
                    if (latitude != null) {
                        return latitude.getValue();
                    } else {
                        return "";
                    }
                } else {
                    Property longitude = placeMap.getProperty(LONGTag);
                    if (longitude != null) {
                        return longitude.getValue();
                    } else {
                        return "";
                    }
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
        this.gedcomPlacesMap = gedcomPlacesMap;

        fireTableDataChanged();
    }

    public Set<Property> getValueAt(int row) {
        Object[] toArray = gedcomPlacesMap.getKeys().toArray();
        String key = (String) toArray[row];
        return gedcomPlacesMap.getReferences(key);
    }
}
