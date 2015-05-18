package ancestris.api.place;

import java.util.List;

public interface SearchPlace {

    /**
     *
     * @param place
     * @param placeList
     * @return
     */
    public void searchPlace(String place, List<Place> placeList);
}
