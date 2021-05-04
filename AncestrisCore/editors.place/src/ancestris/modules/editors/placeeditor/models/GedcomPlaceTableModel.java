package ancestris.modules.editors.placeeditor.models;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyPlace;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceTableModel extends AbstractTableModel {

    private Gedcom gedcom = null;
    private Map<String, Set<PropertyPlace>> placesMap = null;
    private String currentPlaceFormat = null;
    private String[] columsTitle;
    private String[][] data = null;
    private int nbColumns = 0;
    private int nbRows = 0;

    public GedcomPlaceTableModel(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public void update() {

        // Update columns from gedcom format and longuest place value
        String[] placeFormat = PropertyPlace.getFormat(gedcom);
        int maxLength = placeFormat.length;
        String newPlaceFormat = PropertyPlace.arrayToString(placeFormat);
        placesMap = getGeoPlacesMap();
        Set<String> places = placesMap.keySet();
        for (String place : places) {
            int l = PropertyPlace.getFormat(place).length - 2;   // remove coordinates length
            if (l > maxLength) {
                maxLength = l;
            }
        }
        // If maxlength longer, complete format
        for (int i = placeFormat.length; i < maxLength; i++) {
            newPlaceFormat += ",?";
        }
        
        if (!newPlaceFormat.equals(currentPlaceFormat)) {
            nbColumns = placeFormat.length + 2;
            columsTitle = new String[nbColumns];
            int index = 0;
            for (; index < placeFormat.length; index++) {
                columsTitle[index] = placeFormat[index];
            }
            columsTitle[index] = "Latitude";
            columsTitle[index + 1] = "Longitude";
            fireTableStructureChanged();
        }
        
        // update values
        nbRows = places.size();
        data = new String[nbRows][nbColumns];
        int row = 0;
        for (String place : places) {
            data[row] = PropertyPlace.getFormat(place);
            row++;
        }
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if ((value instanceof String) && data[row][column].compareTo((String)value) != 0) {
            data[row][column] = (String) value;
            fireTableCellUpdated(row, column);
        }
    }    
    
    @Override
    public int getRowCount() {
        return nbRows;
    }

    @Override
    public int getColumnCount() {
        return nbColumns;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (data != null) {
            if (row >= 0 && row < data.length && column >= 0 && data[row] != null && column < data[row].length) {
                return data[row][column];
            } else {
                return "";
            }
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
        if (placesMap != null) {
            Object[] toArray = placesMap.keySet().toArray();
            return placesMap.get(toArray[row]); // unicity is ensured by the unique "key" added at the end
        } else {
            return null;
        }
    }


    /**
     * Get map of places with set of properties corresponding to them
     * Key is the place value => if value changes, key will need to change as well to regroup all places with same value on display
     *
     * @return
     */
    private Map<String, Set<PropertyPlace>> getGeoPlacesMap() {
        
        Map<String, Set<PropertyPlace>> ret = new TreeMap<String, Set<PropertyPlace>>();
        
        for (String placeStr : gedcom.getReferenceSet("PLAC").getKeys(gedcom.getCollator())) {
            Set<Property> props = gedcom.getReferenceSet("PLAC").getReferences(placeStr);
            for (Property prop : props) {
                String keyString = ((PropertyPlace) prop).getGeoValue();
                Set<PropertyPlace> set = ret.get(keyString);
                if (set == null) {
                    set = new HashSet<PropertyPlace>();
                    ret.put(keyString, set);
                }
                set.add((PropertyPlace) prop);
            }
        }
        return ret;
    }

    public void setGeoPlacesFromModel() {
        
        for (int row = 0;  row < getRowCount() ; row++) {
            
            // Build new value, new Coordinates and remove ending ID
            String[] newValueArray = Arrays.copyOfRange(data[row], 0, getColumnCount()-2);
            String newPlaceValue = PropertyPlace.formatSpaces(PropertyPlace.arrayToString(newValueArray)); 
            String newLatValue = data[row][getColumnCount()-2];
            String newLonValue = data[row][getColumnCount()-1];
            
            // Get places and update them
            Set<PropertyPlace> pPlaces = getValueAt(row);
            for (PropertyPlace pPlace : pPlaces) {
                if (!newPlaceValue.equals(pPlace.getValue())) {
                    pPlace.setValue(newPlaceValue);
                }
                PropertyLatitude pLat = pPlace.getLatitude(false);
                PropertyLongitude pLon = pPlace.getLongitude(false);
                String lat = pLat != null ? pLat.getValue() : "";
                String lon = pLon != null ? pLon.getValue() : "";
                if (!newLatValue.equals(lat) || !newLonValue.equals(lon)) {
                    pPlace.setCoordinates(newLatValue, newLonValue);
                }
            }
        }
        
    }
    
    public void eraseModel() {
        // Map<String, Set<PropertyPlace>> placesMap = null;
        for (Iterator<Map.Entry<String, Set<PropertyPlace>>> it = placesMap.entrySet().iterator(); it.hasNext();) {
            Set<PropertyPlace> set = (Set<PropertyPlace>) it.next().getValue();
            for (Iterator<PropertyPlace> it2 = set.iterator(); it2.hasNext();) {
                it2.next();
                it2.remove();
            }
            it.remove();
        }
        placesMap = null;
    }

   
}
