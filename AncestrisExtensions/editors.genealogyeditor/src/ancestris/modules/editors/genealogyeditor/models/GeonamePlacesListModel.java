package ancestris.modules.editors.genealogyeditor.models;

import ancestris.api.place.Place;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GeonamePlacesListModel extends AbstractListModel {

    ArrayList<Place> placesList = new ArrayList<Place>();
    String[] placesListData = null;

    public GeonamePlacesListModel() {
    }

    public void add(Place place) {
        placesList.add(place);
        update();
    }

    public void addAll(List<Place> placesList) {
        placesList.addAll(placesList);
        update();
    }

    public void update() {
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
    public Object getElementAt(int index) {
        String jurisdictions = "";

        for (String jurisdiction : placesList.get(index).getJurisdictions()) {
            jurisdictions += jurisdiction!=null? jurisdiction + ", ":"";
        }

        return jurisdictions;
    }

    public Place getPlaceAt(int index) {
        return placesList.get(index);
    }
}
