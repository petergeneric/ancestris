package ancestris.api.place;

import genj.gedcom.PropertyPlace;
import java.util.List;

public interface PlacesList {
  
    /**
     *
     * @param place
     * @return
     */
    public List<Place> findPlace (PropertyPlace place);
}
