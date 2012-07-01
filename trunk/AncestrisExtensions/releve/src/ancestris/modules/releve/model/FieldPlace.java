package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldPlace extends Field {

    private String locality = "";
    private String cityName = "";
    private String cityCode = "";
    private String countyName = "";
    private String stateName = "";
    private String countryName = "";
    
    @Override
    public FieldPlace clone() {
		return (FieldPlace) super.clone();
  	}

    @Override
    public String getValue() {
        if (isEmpty()) {
            return "";
        } else {
            return locality+","+cityName+ ","+ cityCode+ ","+ countyName+ ","+stateName+ ","+countryName;
        }        
    }

    @Override
    public void setValue(Object value) {
            String[] juridictions =  value.toString().split(",");
            if (juridictions.length > 0 ) {
                locality = juridictions[0];
            } else {
                locality = "";
            }
            if (juridictions.length > 1 ) {
                cityName = juridictions[1];
            } else {
                cityName = "";
            }
            if (juridictions.length > 2 ) {
                cityCode = juridictions[2];
            } else {
                cityCode = "";
            }
            if (juridictions.length > 3 ) {
                countyName = juridictions[3];
            } else {
                countyName = "";
            }
            if (juridictions.length > 4 ) {
                stateName = juridictions[4];
            } else {
                stateName = "";
            }
            if (juridictions.length > 5 ) {
                countryName = juridictions[5];
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
        return cityName.isEmpty() && cityCode.isEmpty() && stateName.isEmpty() && countyName.isEmpty() && countryName.isEmpty() && locality.isEmpty();
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

    /**
     * @return the locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * @param locality the countyCode to set
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

}
