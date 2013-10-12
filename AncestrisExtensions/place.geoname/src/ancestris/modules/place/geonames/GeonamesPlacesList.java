package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlacesList;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geonames.*;

/**
 *
 * @author dominique
 */
public class GeonamesPlacesList implements PlacesList {

    private ArrayList<Place> placesList = new ArrayList<Place>();
    private final static Logger logger = Logger.getLogger(GeonamesPlacesList.class.getName(), null);

    @Override
    public List<Place> findPlace(PropertyPlace place) {

        ToponymSearchResult toponymSearchResult;
        String[] jurisdictions = place.getJurisdictions();

        try {
            WebService.setUserName("lemovice");
            int index = 0;

            do {
                // parse format
                StringBuilder result = new StringBuilder();
                for (int i = index; i < jurisdictions.length; i++) {
                    result.append(jurisdictions[i].trim());
                    if (i < jurisdictions.length) {
                        result.append(" ");
                    }
                }
                index += 1;
                ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
                toponymSearchCriteria.setQ(result.toString());
                toponymSearchResult = WebService.search(toponymSearchCriteria);

            } while (index < jurisdictions.length && toponymSearchResult.getToponyms().isEmpty());

            for (Toponym toponym : toponymSearchResult.getToponyms()) {
                PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
                postalCodeSearchCriteria.setStyle(Style.FULL);
                List<PostalCode> postalCodeSearchResult;

                postalCodeSearchCriteria.setPlaceName(toponym.getName());
                postalCodeSearchCriteria.setCountryCode(toponym.getCountryCode());
                postalCodeSearchResult = WebService.postalCodeSearch(postalCodeSearchCriteria);

                for (PostalCode postalCode : postalCodeSearchResult) {
                    placesList.add(new GeonamesPlace(toponym, postalCode));
                }
            }

            return placesList;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
