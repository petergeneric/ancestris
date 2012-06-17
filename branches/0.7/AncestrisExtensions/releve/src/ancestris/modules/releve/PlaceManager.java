package ancestris.modules.releve;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface PlaceManager {
    public void addPlaceListener(PlaceListener listener);
    public void removePlaceListener(PlaceListener listener);
    public void setPlace(String value);
    public String getPlace();
    public String getCityName();
    public String getCityCode();
    public String getCountyName();
    public String getStateName();
    public String getCountryName();
}
