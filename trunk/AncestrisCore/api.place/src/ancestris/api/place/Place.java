package ancestris.api.place;

public interface Place {

    public final static String JURISDICTION_SEPARATOR = ",";

    public String getName();
    
    public String getCity();

    public Double getLongitude();
    
    public Double getLatitude();

    public Long getPopulation();

   public String getCountryCode();
    
    public String getCountryName();
    
    public String getAdminCode(int level);
    
    public String getAdminName(int level);
    
    public String getTimeZoneId();
    
    public String getTimeZoneGmtOffset();

    public String getInfo();
    
    public String getFirstAvailableJurisdiction();

    public String[] getFormat();

    public String getFormatAsString();

    public String getJurisdiction(int hierarchyLevel);

    public String[] getJurisdictions();

    public String getValueStartingWithCity();

    public void setFormatAsString(boolean global, String format);
    
    public String getPlaceToLocalFormat();
        
    public int compareTo(Place that);

}
