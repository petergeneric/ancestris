package ancestris.modules.editors.genealogyeditor.models;

import ancestris.api.place.Place;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GeonamePostalCodeTableModel extends AbstractTableModel {

    String[] placesListColumsTitle = {"City","AdminName1", "AdminCode1", "AdminName2", "AdminCode2", "AdminName3", "AdminCode3", "Postal code", "Country code"};
    String[][] placesListData = null;

    public GeonamePostalCodeTableModel() {
    }

    @Override
    public int getRowCount() {
        return placesListData == null?0:placesListData.length;
    }

    @Override
    public int getColumnCount() {
        return placesListColumsTitle.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return placesListData[row][col];
    }

    @Override
    public String getColumnName(int col) {
        return placesListColumsTitle[col];
    }

    public void update(List<Place> placesList) {
        placesListData = new String[placesList.size()][];
        int row = 0;

        for (Place place : placesList) {
            placesListData[row] = place.getJurisdictions();
            row += 1;
        }

        fireTableDataChanged();
    }
}
