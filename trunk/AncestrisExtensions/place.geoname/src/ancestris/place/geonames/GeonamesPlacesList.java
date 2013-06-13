package ancestris.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlacesList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geonames.PostalCode;
import org.geonames.PostalCodeSearchCriteria;
import org.geonames.Style;
import org.geonames.WebService;

/**
 *
 * @author dominique
 */
public class GeonamesPlacesList implements PlacesList {

    private ArrayList<Place> placesList = new ArrayList<Place>();
    private final static Logger logger = Logger.getLogger(GeonamesPlacesList.class.getName(), null);

    @Override
    public List<Place> findPlace(String placeName) {
        PostalCodeSearchCriteria searchCriteria = new PostalCodeSearchCriteria();
        searchCriteria.setPlaceName(placeName);
        searchCriteria.setStyle(Style.FULL);


        try {
            WebService.setUserName("lemovice");
            List<PostalCode> searchResult = WebService.postalCodeSearch(searchCriteria);

            for (PostalCode postalCode : searchResult) {
                placesList.add(new GeonamesPlace(postalCode));
            }
            return placesList;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
