package ancestris.modules.releve.model;

import org.openide.util.NbPreferences;

/**
 *
 * @author michel
 */


public class PlaceFormatModel {
        
        static final  String HAMLET_JURIDICTION   = "RegisterPlaceToJuridiction.hamlet";
        static final  String CITYNAME_JURIDICTION = "RegisterPlaceToJuridiction.cityName";
        static final  String CITYCODE_JURIDICTION = "RegisterPlaceToJuridiction.cityCode";
        static final  String COUNTY_JURIDICTION   = "RegisterPlaceToJuridiction.county";
        static final  String STATE_JURIDICTION    = "RegisterPlaceToJuridiction.state";
        static final  String COUNTRY_JURIDICTION  = "RegisterPlaceToJuridiction.country";
        static final  String JURIDICTION_NUMBER   = "RegisterPlaceToJuridiction.juridictionNumber";
        
        private int hamletJuridiction;
        private int cityNameJuridiction;
        private int cityCodeJuridiction;
        private int countyJuridiction;
        private int stateJuridiction;
        private int countryJuridiction;
        private int juridictionNumber;
        
        static public enum RecordJuridiction { HAMLET, CITY_NAME, CITY_CODE, COUNTY, STATE, COUNTRY }
        
        // singleton 
        static private PlaceFormatModel placeModel;
                  
        /** 
         * model factory
         * @return 
         */
        static public PlaceFormatModel getModel() {

            if (placeModel == null) {
                placeModel = new PlaceFormatModel();
                placeModel.loadPreferences();
            }
            return placeModel;
        }
        
        public int getHamletJuridiction() {
            return hamletJuridiction;
        }

        public int getCityNameJuridiction() {
            return cityNameJuridiction;
        }

        public int getCityCodeJuridiction() {
            return cityCodeJuridiction;
        }
        
        public int getCountyJuridiction() {
            return countyJuridiction;
        }
        
        public int getStateJuridiction() {
            return stateJuridiction;
        }
        
        public int getCountryJuridiction() {
            return countryJuridiction;
        }
        
        public int getJuridictionNumber() {
            return juridictionNumber;
        }
        
        public void loadPreferences() {
            hamletJuridiction    = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(HAMLET_JURIDICTION, "0"));
            cityNameJuridiction  = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(CITYNAME_JURIDICTION, "1"));
            cityCodeJuridiction  = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(CITYCODE_JURIDICTION, "2"));
            countyJuridiction    = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(COUNTY_JURIDICTION, "3"));
            stateJuridiction     = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(STATE_JURIDICTION, "4"));
            countryJuridiction   = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(COUNTRY_JURIDICTION, "5"));
            
            juridictionNumber = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(JURIDICTION_NUMBER, "6"));
        }
        
        /**
         * enregistre les paires fileName=sourceName
         */
        public void savePreferences(int hamlet, int cityName, int cityCode, int county, int state, int country, int juridictionNb) { 
            hamletJuridiction = hamlet;
            cityNameJuridiction = cityName;
            cityCodeJuridiction = cityCode;
            countyJuridiction = county;
            stateJuridiction = state;
            countryJuridiction = country;
            juridictionNumber  = juridictionNb;
            
            NbPreferences.forModule(PlaceFormatModel.class).put( HAMLET_JURIDICTION,   String.valueOf(hamletJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( CITYNAME_JURIDICTION, String.valueOf(cityNameJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( CITYCODE_JURIDICTION, String.valueOf(cityCodeJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( COUNTY_JURIDICTION,   String.valueOf(countyJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( STATE_JURIDICTION,    String.valueOf(stateJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( COUNTRY_JURIDICTION,  String.valueOf(countryJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put( JURIDICTION_NUMBER,   String.valueOf(juridictionNumber));
        }
        

    }
