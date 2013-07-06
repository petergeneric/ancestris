package ancestris.modules.editors.placeeditor.models;

import ancestris.api.place.Place;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GeonamePostalCodeListModel extends AbstractListModel {

    String[] placesListData = null;

    public GeonamePostalCodeListModel() {
    }

    public void update(List<Place> placesList) {
        int row = 0;

        placesListData = new String[placesList.size()];
        
        for (Place place : placesList) {
            String juridictions = "";
            for (String juridiction : place.getJurisdictions())
                juridictions += juridiction + ", ";
            
            placesListData[row] = juridictions;
            row += 1;
        }

        fireContentsChanged(placesListData, 0, row);
    }

    @Override
    public int getSize() {
        return placesListData != null ? placesListData.length : 0;
    }

    @Override
    public Object getElementAt(int i) {
        return placesListData[i];
    }
}
