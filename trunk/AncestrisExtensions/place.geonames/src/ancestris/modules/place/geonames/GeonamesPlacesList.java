package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlacesList;
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
    public List<Place> findPlace(String place) {

        ToponymSearchResult toponymSearchResult;

        try {
            WebService.setUserName("lemovice");

            ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
            toponymSearchCriteria.setStyle(Style.FULL);
            toponymSearchCriteria.setQ(place);
            toponymSearchResult = WebService.search(toponymSearchCriteria);

            for (Toponym toponym : toponymSearchResult.getToponyms()) {
                PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
                postalCodeSearchCriteria.setStyle(Style.FULL);
                List<PostalCode> postalCodeSearchResult;

                postalCodeSearchCriteria.setPlaceName(toponym.getName());
                postalCodeSearchCriteria.setCountryCode(toponym.getCountryCode());
                postalCodeSearchResult = WebService.postalCodeSearch(postalCodeSearchCriteria);

                logger.log(Level.INFO, "toponym AdminName1 {0} AdminName2 {1} AdminName3 {2} AdminName4 {3} AdminName5 {4}", new Object[]{toponym.getAdminName1(), toponym.getAdminName2(), toponym.getAdminName3(), toponym.getAdminName4(), toponym.getAdminName5()});
                logger.log(Level.INFO, "toponym AdminCode1 {0} AdminCode2 {1} AdminCode3 {2} AdminCode4 {3} AdminCode5 {4}", new Object[]{toponym.getAdminCode1(), toponym.getAdminCode2(), toponym.getAdminCode3(), toponym.getAdminCode4(), toponym.getAdminCode5()});

                for (PostalCode postalCode : postalCodeSearchResult) {
                    logger.log(Level.INFO, "postalCode AdminName1 {0} AdminName2 {1} AdminName3 {2}", new Object[]{postalCode.getAdminName1(), postalCode.getAdminName2(), postalCode.getAdminName3()});
                    logger.log(Level.INFO, "postalCode AdminCode1 {0} AdminCode2 {1} AdminCode3 {2}", new Object[]{postalCode.getAdminCode1(), postalCode.getAdminCode2(), postalCode.getAdminCode3()});

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
