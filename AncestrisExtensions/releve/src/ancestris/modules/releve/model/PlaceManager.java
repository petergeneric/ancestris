package ancestris.modules.releve.model;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface PlaceManager {
    public void addPlaceListener(PlaceListener listener);
    public void removePlaceListener(PlaceListener listener);
    public void setPlace(String value);
    public void setPlace(String cityName, String cityCode, String county, String state, String country);
    public String getCityName();
    public String getCityCode();
    public String getCountyName();
    public String getStateName();
    public String getCountryName();    
}
