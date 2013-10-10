package ancestris.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlacesList;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setLanguage(Locale.getDefault().toString());
        searchCriteria.setStyle(Style.FULL);
        ToponymSearchResult searchResult;

        try {
            searchCriteria.setQ(place.getDisplayValue().replaceAll(",", " "));
            searchResult = WebService.search(searchCriteria);

            for (Toponym toponym : searchResult.getToponyms()) {
                placesList.add(new GeonamesPlace(toponym));
            }
            return placesList;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
