package ancestris.modules.editors.placeeditor;

import ancestris.api.place.Place;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GeonamePostalCodeTableModel extends AbstractTableModel {

    String[] placesListColumsTitle = {"AdminCode1", "AdminName1", "AdminCode2", "AdminName2", "AdminCode3", "AdminName3", "AdminCode4", "AdminName4", "AdminCode5", "AdminName5"};
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
