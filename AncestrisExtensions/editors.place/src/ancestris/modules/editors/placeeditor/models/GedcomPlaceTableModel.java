package ancestris.modules.editors.placeeditor.models;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
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
    private String[] columsTitle;
    private String[][] data = null;
    private int nbColumns = 0;
    private int nbRows = 0;

    public GedcomPlaceTableModel(Gedcom gedcom) {
        this.gedcom = gedcom;
        String[] placeFormat = PropertyPlace.getFormat(gedcom);
        nbColumns = placeFormat.length + 2;
        columsTitle = new String[nbColumns];
        
        int index = 0;
        for (; index < placeFormat.length; index++) {
            columsTitle[index] = placeFormat[index];
        }
        columsTitle[index] = "Latitude";
        columsTitle[index + 1] = "Longitude";
    }

    public void update() {
        placesMap = getGeoPlaces();
        Set<String> places = placesMap.keySet();
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
            return data[row][column];
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
            return placesMap.get(toArray[row]);
        } else {
            return null;
        }
    }


    /**
     * Get map of geo places with set of properties corresponding to them
     * @return 
     */
    private Map<String, Set<PropertyPlace>> getGeoPlaces() {
        Map<String, Set<PropertyPlace>> ret = new TreeMap<String, Set<PropertyPlace>>();
        
        for (String placeStr : gedcom.getReferenceSet("PLAC").getKeys(gedcom.getCollator())) {
            Set<Property> props = gedcom.getReferenceSet("PLAC").getReferences(placeStr);
            for (Property prop : props) {
                String geoPlace = ((PropertyPlace) prop).getGeoValue();
                Set<PropertyPlace> set = ret.get(geoPlace);
                if (set == null) {
                    set = new HashSet<PropertyPlace>();
                    ret.put(geoPlace, set);
                }
                set.add((PropertyPlace) prop);
            }
        }
        return ret;
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
