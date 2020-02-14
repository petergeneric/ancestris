package ancestris.api.place;

import java.util.List;
import org.openide.util.TaskListener;

public interface SearchPlace {

    /**
     * Search a toponym on Internet
     * @param place Place to find
     * @param city City of place to find
     * @param code Code of place to find
     * @param placeList List of places corresponding to the toponym
     * @param maxResults Number of value to return
     * @param tasklistener Hook for callback
     * @return true if no trouble false if Error was triggered during search.
     */
    public boolean searchPlace(String place, String city, String code, List<Place> placeList, int maxResults, TaskListener tasklistener);
}
