package ancestris.api.place;

import java.util.List;
import org.openide.util.TaskListener;

public interface SearchPlace {

    public Place searchMassPlace(String placePieces, String city, Place defaultPlace);
    
    public void searchIndividualPlace(String placePieces, final List<Place> placesList, TaskListener taskListener);
    
    public Place searchNearestPlace(double latitude, double longitude);
}
