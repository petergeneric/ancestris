package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.SearchPlace;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geonames.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author dominique
 */
public class GeonamesPlacesList implements SearchPlace {

    private final static Logger logger = Logger.getLogger(GeonamesPlacesList.class.getName(), null);

    @Override
    public void searchPlace(final String searchedPlace, final List<Place> placesList) {
        RequestProcessor.Task theTask;
        RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
        Runnable runnable = new Runnable() {

            @Override
            public synchronized void run() {
                if (searchedPlace.isEmpty() == false) {
                    ToponymSearchResult toponymSearchResult;

                    try {
                        WebService.setUserName("lemovice");

                        ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
                        toponymSearchCriteria.setStyle(Style.FULL);
                        toponymSearchCriteria.setQ(searchedPlace);
                        toponymSearchResult = WebService.search(toponymSearchCriteria);

                        for (Toponym toponym : toponymSearchResult.getToponyms()) {
                            PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
                            postalCodeSearchCriteria.setStyle(Style.FULL);
                            postalCodeSearchCriteria.setPlaceName(toponym.getName());
                            postalCodeSearchCriteria.setCountryCode(toponym.getCountryCode());

                            List<PostalCode> postalCodeSearchResult;
                            postalCodeSearchResult = WebService.postalCodeSearch(postalCodeSearchCriteria);

                            logger.log(Level.INFO, "toponym AdminName1 {0} AdminName2 {1} AdminName3 {2} AdminName4 {3} AdminName5 {4}", new Object[]{toponym.getAdminName1(), toponym.getAdminName2(), toponym.getAdminName3(), toponym.getAdminName4(), toponym.getAdminName5()});
                            logger.log(Level.INFO, "toponym AdminCode1 {0} AdminCode2 {1} AdminCode3 {2} AdminCode4 {3} AdminCode5 {4}", new Object[]{toponym.getAdminCode1(), toponym.getAdminCode2(), toponym.getAdminCode3(), toponym.getAdminCode4(), toponym.getAdminCode5()});

                            for (PostalCode postalCode : postalCodeSearchResult) {
                                logger.log(Level.INFO, "postalCode AdminName1 {0} AdminName2 {1} AdminName3 {2}", new Object[]{postalCode.getAdminName1(), postalCode.getAdminName2(), postalCode.getAdminName3()});
                                logger.log(Level.INFO, "postalCode AdminCode1 {0} AdminCode2 {1} AdminCode3 {2}", new Object[]{postalCode.getAdminCode1(), postalCode.getAdminCode2(), postalCode.getAdminCode3()});

                                placesList.add(new GeonamesPlace(toponym, postalCode));
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }
                }
            }
        };

        theTask = RP.create(runnable); //the task is not started yet

        theTask.addTaskListener(new TaskListener() {

            @Override
            public void taskFinished(Task task) {
            }
        });

        theTask.schedule(0); //start the task
    }
}
