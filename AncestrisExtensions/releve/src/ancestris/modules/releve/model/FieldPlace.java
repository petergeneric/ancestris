package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldPlace extends Field {

    private String cityName = "";
    private String cityCode = "";
    private String countyName = "";
    private String stateName = "";
    private String countryName = "";

    @Override
    public String getValue() {
        if (isEmpty()) {
            return "";
        } else {
            return cityName+ ","+ cityCode+ ","+ countyName+ ","+stateName+ ","+countryName;
        }
        
    }

    @Override
    public void setValue(Object value) {
        String[] juridictions =  value.toString().split(",");
        if (juridictions.length > 0 ) {
            cityName = juridictions[0];
        } else {
            cityName = "";
        }
        if (juridictions.length > 1 ) {
            cityCode = juridictions[1];
        } else {
            cityCode = "";
        }
        if (juridictions.length > 2 ) {
            countyName = juridictions[2];
        } else {
            countyName = "";
        }
        if (juridictions.length > 3 ) {
            stateName = juridictions[3];
        } else {
            stateName = "";
        }
        if (juridictions.length > 4 ) {
            countryName = juridictions[4];
        } else {
            countryName = "";
        }
    }

    @Override
    public String toString() {
        return getValue();
    }
   
    @Override
    public boolean isEmpty() {
        return cityName.isEmpty() && cityCode.isEmpty() && stateName.isEmpty() && countyName.isEmpty() && countryName.isEmpty();
    }

    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String value) {
        cityName = value;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String value) {
        cityCode = value;
    }

    /**
     * @return the countyName
     */
    public String getCountyName() {
        return countyName;
    }

    /**
     * @param countyName the countyName to set
     */
    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

     /**
     * @return the cityCode
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * @param cityCode the cityCode to set
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
    
    /**
     * @return the countyCode
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * @param countyCode the countyCode to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }


}
