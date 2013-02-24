package ancestris.modules.releve;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface PlaceManagerInterface {
    public void addPlaceListener(PlaceListenerInterface listener);
    public void removePlaceListener(PlaceListenerInterface listener);
    public void setPlace(String value);
    public String getPlace();
    public String getCityName();
    public String getCityCode();
    public String getCountyName();
    public String getStateName();
    public String getCountryName();
}
