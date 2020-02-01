package ancestris.api.place;

import java.util.List;
import org.openide.util.TaskListener;

public interface SearchPlace {

    /**
     *
     * @param place
     * @param placeList
     * @return
     */
    public void searchPlace(String place, String city, String code, List<Place> placeList, int maxResults, TaskListener tasklistener);
}
