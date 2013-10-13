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
            String[] splitJurisdictions = place.getJurisdictions();
            String jurisdictions = "";

            // City
            jurisdictions += splitJurisdictions[0] != null ? splitJurisdictions[0] : "";

            //AdminName1
            jurisdictions += splitJurisdictions[1] != null ? ", " + splitJurisdictions[1] : "";
            //AdminCode1
            jurisdictions += splitJurisdictions[2] != null ? " (" + splitJurisdictions[2] + ")" : "";

            //AdminName2
            jurisdictions += splitJurisdictions[3] != null ? ", " + splitJurisdictions[3] : "";
            //AdminCode2
            jurisdictions += splitJurisdictions[4] != null ? " (" + splitJurisdictions[4] + ")" : "";

            //AdminName3
            jurisdictions += splitJurisdictions[5] != null ? ", " + splitJurisdictions[5] : "";
            //AdminCode3
            jurisdictions += splitJurisdictions[6] != null ? " (" + splitJurisdictions[6] + ")" : "";

            //Postal code
            jurisdictions += splitJurisdictions[7] != null ? ", " + splitJurisdictions[7] : "";

            //Country code
            jurisdictions += splitJurisdictions[8] != null ? ", " + splitJurisdictions[8] : "";

            // latitude
            jurisdictions += "- LAT: " + place.getLatitude();
            // longitude
            jurisdictions += " LON: " + place.getLongitude();

            placesListData[row] = jurisdictions;
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
