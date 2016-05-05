package ancestris.modules.place.geonames;

import ancestris.libs.geonames.GeonamesOptions;
import ancestris.api.place.Place;
import ancestris.api.place.SearchPlace;
import ancestris.util.TimingUtility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geonames.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author frederic & dominique
 */
public class GeonamesPlacesList implements SearchPlace {

    private final static Logger logger = Logger.getLogger(GeonamesPlacesList.class.getName(), null);
    private List<Place> mPlacesList = new ArrayList<Place>();
    private RequestProcessor.Task theTask;

    public RequestProcessor.Task getTask() {
        return theTask;
    }

    @Override
    public void searchPlace(final String searchedPlace, final List<Place> placesList) {
        mPlacesList.clear();
        RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
        Runnable runnable = new Runnable() {

            @Override
            public synchronized void run() {
                if (searchedPlace.isEmpty() == false) {

                    ToponymSearchResult toponymSearchResult;
                    Set<String> tmpListDedup = new HashSet<String>();
                    Place place = null;


                    try {
                        logger.log(Level.FINE, "   ");
                        logger.log(Level.FINE, "Start searching {0} ...", searchedPlace.replaceAll(",", " ").replaceAll(" +", " "));

                        WebService.setUserName(GeonamesOptions.getInstance().getUserName());
                        ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
                        toponymSearchCriteria.setStyle(Style.FULL);
                        toponymSearchCriteria.setLanguage(Locale.getDefault().toString());
                        toponymSearchCriteria.setQ(searchedPlace.replaceAll(",", " ").replaceAll(" +", " "));
                        logger.log(Level.FINE, "Call to WebService.search for toponym - "+ TimingUtility.geInstance().getTime());
                        toponymSearchResult = WebService.search(toponymSearchCriteria);
                        logger.log(Level.FINE, "Answer from WebService.search for toponym- "+ TimingUtility.geInstance().getTime());

                        for (Toponym toponym : toponymSearchResult.getToponyms()) {
                            
                            logger.log(Level.FINE, "toponym: Place Name {0} AdminName1 {1} AdminName2 {2} AdminName3 {3} AdminName4 {4} AdminName5 {5}", new Object[]{toponym.getName(), toponym.getAdminName1(), toponym.getAdminName2(), toponym.getAdminName3(), toponym.getAdminName4(), toponym.getAdminName5()});

                            List<PostalCode> postalCodeSearchResult;
                            PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
                            postalCodeSearchCriteria.setStyle(Style.FULL);
                            postalCodeSearchCriteria.setPlaceName(toponym.getName());
                            String cc = toponym.getCountryCode();
                            if (cc == null || cc.isEmpty()) {
                                continue;
                            }
                            postalCodeSearchCriteria.setCountryCode(cc);
                            logger.log(Level.FINE, "Call to WebService.search for postal code - "+ TimingUtility.geInstance().getTime());
                            postalCodeSearchResult = WebService.postalCodeSearch(postalCodeSearchCriteria);
                            logger.log(Level.FINE, "Answer from WebService.search for postal code- "+ TimingUtility.geInstance().getTime());

                            if (!postalCodeSearchResult.isEmpty()) {
                                for (PostalCode postalCode : postalCodeSearchResult) {
                                    logger.log(Level.FINE, "postalCode AdminName1 {0} AdminName2 {1} AdminName3 {2}", new Object[]{postalCode.getAdminName1(), postalCode.getAdminName2(), postalCode.getAdminName3()});
                                    place = new GeonamesPlace(toponym, postalCode);
                                }
                            } else {
                                place = new GeonamesPlace(toponym, null);
                            }
                            String str = place.toString();
                            if (!tmpListDedup.contains(str)) {
                                mPlacesList.add(place);
                                tmpListDedup.add(str);
                            }
                        }
                        logger.log(Level.FINE, "... search completed.");
                        logger.log(Level.FINE, "   ");
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException("");
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }

                    placesList.addAll(mPlacesList);
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
    
    
    
    public Place searchNearestPlace(double latitude, double longitude) {
        WebService.setUserName(GeonamesOptions.getInstance().getUserName());        
        try {
            List<Toponym> results = WebService.findNearbyPlaceName(latitude, longitude);
            if (!results.isEmpty()) {
                int geonameId = results.get(0).getGeoNameId();
                Toponym toponym = WebService.get(geonameId, null, null);
                return new GeonamesPlace(toponym, null);
            }
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
 }
