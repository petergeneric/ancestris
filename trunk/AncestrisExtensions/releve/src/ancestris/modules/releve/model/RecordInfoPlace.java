package ancestris.modules.releve.model;

import genj.gedcom.GedcomOptions;

/**
 *
 * @author Michel
 */
public class RecordInfoPlace extends Field implements Cloneable {

    private String cityName = "";
    private String cityCode = "";
    private String countyName = "";
    private String stateName = "";
    private String countryName = "";
    
    private static final String juridictionSeparator;
    static {
        if( GedcomOptions.getInstance().isUseSpacedPlaces() == true ) {
            juridictionSeparator = ", ";
        } else {
            juridictionSeparator = ",";
        }
    }
     private PlaceFormatModel placeFormatModel =  PlaceFormatModel.getCurrentModel();

    
    @Override
    public RecordInfoPlace clone() throws CloneNotSupportedException {
        return (RecordInfoPlace) super.clone();
    }

    @Override
    public String getValue() {
        
        placeFormatModel =  PlaceFormatModel.getCurrentModel();
        String[] juridictions= new String[placeFormatModel.getJuridictionNumber()];
        
        if ( placeFormatModel.getCityNameJuridiction()>=0 &&  placeFormatModel.getCityNameJuridiction() < placeFormatModel.getJuridictionNumber() ) {
            juridictions[placeFormatModel.getCityNameJuridiction()] =  cityName;
        }
        if ( placeFormatModel.getCityCodeJuridiction() >=0 &&  placeFormatModel.getCityCodeJuridiction() < placeFormatModel.getJuridictionNumber() ) {
            juridictions[placeFormatModel.getCityCodeJuridiction()] =  cityCode;
        }
        if ( placeFormatModel.getCountyJuridiction() >=0 &&  placeFormatModel.getCountyJuridiction() < placeFormatModel.getJuridictionNumber() ) {
            juridictions[placeFormatModel.getCountyJuridiction()] =  countyName;
        }
        if ( placeFormatModel.getStateJuridiction() >=0 &&  placeFormatModel.getStateJuridiction() < placeFormatModel.getJuridictionNumber() ) {
            juridictions[placeFormatModel.getStateJuridiction()] =  stateName;
        }
        if ( placeFormatModel.getCountryJuridiction() >=0 &&  placeFormatModel.getCountryJuridiction() < placeFormatModel.getJuridictionNumber() ) {
            juridictions[placeFormatModel.getCountryJuridiction()] = countryName;
        }
        
        StringBuilder sb = new StringBuilder();
        for(int i=0 ; i < juridictions.length; i++ ) {
            String juridiction = juridictions[i];
            if (juridiction == null) {
                juridiction = "";
            }
            sb.append(juridiction);
            if(i < juridictions.length-1) {
                sb.append(juridictionSeparator);
            }
        }
        return sb.toString();
    }

    
    @Override
    public void setValue(String value) {
        
        placeFormatModel =  PlaceFormatModel.getCurrentModel();
        String[] juridictions = value.split(juridictionSeparator, -1 );
        
        if (juridictions.length > placeFormatModel.getCityNameJuridiction() && placeFormatModel.getCityNameJuridiction() != -1) {
            cityName = juridictions[placeFormatModel.getCityNameJuridiction()];
        } else {
            cityName = "";
        }
        if (juridictions.length > placeFormatModel.getCityCodeJuridiction()&& placeFormatModel.getCityCodeJuridiction() != -1) {
            cityCode = juridictions[placeFormatModel.getCityCodeJuridiction()];
        } else {
            cityCode = "";
        }
        if (juridictions.length > placeFormatModel.getCountyJuridiction()&& placeFormatModel.getCountyJuridiction() != -1) {
            countyName = juridictions[placeFormatModel.getCountyJuridiction()];
        } else {
            countyName = "";
        }
        if (juridictions.length > placeFormatModel.getStateJuridiction()&& placeFormatModel.getStateJuridiction() != -1) {
            stateName = juridictions[placeFormatModel.getStateJuridiction()];
        } else {
            stateName = "";
        }
        if (juridictions.length > placeFormatModel.getCountryJuridiction() && placeFormatModel.getCountryJuridiction() != -1 ) {
            countryName = juridictions[placeFormatModel.getCountryJuridiction()];
        } else {
            countryName = "";
        }
    }

    
    public void setValue(String cityName, String cityCode, String county, String state, String country) {
        this.cityName = cityName;
        this.cityCode = cityCode;
        this.countyName = county;
        this.stateName = state;
        this.countryName = country;
    }

    public void setValue(RecordInfoPlace recordInfoPlace) {
        this.cityName    = recordInfoPlace.getCityName();
        this.cityCode    = recordInfoPlace.getCityCode();
        this.countyName  = recordInfoPlace.getCountyName();
        this.stateName   = recordInfoPlace.getStateName();
        this.countryName = recordInfoPlace.getCountryName();
    }

    @Override
    public String toString() {
        return getValue();
    }
   
    @Override
    public boolean isEmpty() {
        return cityName.isEmpty() && cityCode.isEmpty() && stateName.isEmpty() && countyName.isEmpty() && countryName.isEmpty();
    }
    
    public String getDisplayValue() {        
        return getValue();
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
