package ancestris.api.place;

public interface Place {

    public final static String JURISDICTION_SEPARATOR = ",";

    int compareTo(Place that);

    /**
     * Accessor - jurisdictions that is the city
     */
    String getCity();

    public Double getLongitude();
    
    public Double getLatitude();

    /**
     * Accessor - first non-empty jurisdiction
     *
     * @return jurisdiction of zero+ length
     */
    String getFirstAvailableJurisdiction();

    /**
     * Accessor - format
     */
    String[] getFormat();

    /**
     * Accessor - the format of this place's value (non localized)
     */
    String getFormatAsString();

    /**
     * Accessor - jurisdiction of given level
     *
     * @return jurisdiction of zero+ length or null if n/a
     */
    String getJurisdiction(int hierarchyLevel);

    /**
     * Accessor - jurisdictions
     */
    String[] getJurisdictions();

    /**
     * Accessor - all jurisdictions starting with city
     */
    String getValueStartingWithCity();

    /**
     * Accessor - the hierarchy of this place's value (non localized)
     */
    void setFormatAsString(boolean global, String format);
}