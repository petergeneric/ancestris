package ancestris.api.place;

import java.util.List;

public interface PlacesList {
  
    /**
     *
     * @param place
     * @return
     */
    public List<Place> findPlace (String place);
}
