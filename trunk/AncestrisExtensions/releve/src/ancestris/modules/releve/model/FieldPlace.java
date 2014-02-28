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
            return cityName+ ","+ cityCode+ ","+ countyName+ ","+stateName+ ","+countryName+","+locality;
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
            if (juridictions.length > 5 ) {
                locality = juridictions[5];
            } else {
                locality = "";
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

    public String getDisplayValue() {
        String placeString ="";
        if (!getLocality().isEmpty()) {
            placeString += getLocality();
        }
        if (!getCityName().isEmpty()) {
            if(!placeString.isEmpty()) {
                placeString += ",";
            }
            placeString += getCityName();
        }
        if (!getCityCode().isEmpty()) {
            if(!placeString.isEmpty()) {
                placeString += ",";
            }
            placeString += getCityCode();
        }
        if (!getCountyName().isEmpty()) {
            if(!placeString.isEmpty()) {
                placeString += ",";
            }
            placeString += getCountyName();
        }
        if (!getStateName().isEmpty()) {
            if(!placeString.isEmpty()) {
                placeString += ",";
            }
            placeString += getStateName();
        }
        if (!getCountryName().isEmpty()) {
            if(!placeString.isEmpty()) {
                placeString += ",";
            }
            placeString += getCountryName();
        }
        return placeString;
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
