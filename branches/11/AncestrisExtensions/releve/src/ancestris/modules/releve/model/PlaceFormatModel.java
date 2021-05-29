package ancestris.modules.releve.model;

import ancestris.modules.releve.merge.MergeOptionPanel;
import org.openide.util.NbPreferences;

/**
 *
 * @author michel
 */


public class PlaceFormatModel {
        
        static final  String CITYNAME_JURIDICTION = "RegisterPlaceToJuridiction.cityName";
        static final  String CITYCODE_JURIDICTION = "RegisterPlaceToJuridiction.cityCode";
        static final  String COUNTY_JURIDICTION   = "RegisterPlaceToJuridiction.county";
        static final  String STATE_JURIDICTION    = "RegisterPlaceToJuridiction.state";
        static final  String COUNTRY_JURIDICTION  = "RegisterPlaceToJuridiction.country";
        static final  String JURIDICTION_NUMBER   = "RegisterPlaceToJuridiction.juridictionNumber";
        
        private int cityNameJuridiction = 0;
        private int cityCodeJuridiction = 1;
        private int countyJuridiction = 2;
        private int stateJuridiction = 3;
        private int countryJuridiction = 4;
        private int juridictionNumber = 5;
        
        static public enum RecordJuridiction { CITY_NAME, CITY_CODE, COUNTY, STATE, COUNTRY }
        
        // singleton 
        static private PlaceFormatModel defaultPlaceModel = null;
        
        static public void saveDefaultGedcomName(String gedcomName) {
            NbPreferences.forModule(PlaceFormatModel.class).put( "defaultGedcomName", gedcomName);
            defaultPlaceModel = new PlaceFormatModel(loadDefaultGedcomName());
         }
        
        static public String loadDefaultGedcomName() {
            return NbPreferences.forModule(PlaceFormatModel.class).get( "defaultGedcomName", "default");
        }
        
        static public PlaceFormatModel getCurrentModel() {
            
            if (defaultPlaceModel == null) {
                // Default model is the model of the first currently open gedcom...
                MergeOptionPanel.GedcomFormatModel gedcomFormatModel = new MergeOptionPanel.GedcomFormatModel();
                if (gedcomFormatModel.defaultGedcom > -1) {
                    defaultPlaceModel = gedcomFormatModel.getDefaultGedcomInfo().getPlaceFormatModel();
                } else {
                    // ...or else the one of the last saved preferences gedcom if none is open
                    defaultPlaceModel = new PlaceFormatModel(loadDefaultGedcomName());
                }
            }
            return defaultPlaceModel; 
         }
        
        private String gedcomName = null;
        
        public PlaceFormatModel(String gedcomName) {
            this.gedcomName = gedcomName + ".";
            loadPreferences();
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
            cityNameJuridiction  = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + CITYNAME_JURIDICTION, "0"));
            cityCodeJuridiction  = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + CITYCODE_JURIDICTION, "1"));
            countyJuridiction    = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + COUNTY_JURIDICTION, "2"));
            stateJuridiction     = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + STATE_JURIDICTION, "3"));
            countryJuridiction   = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + COUNTRY_JURIDICTION, "4"));
            juridictionNumber = Integer.parseInt(NbPreferences.forModule(PlaceFormatModel.class).get(gedcomName + JURIDICTION_NUMBER, "5"));
        }
        
        /**
         * enregistre les paires fileName=sourceName
         */
        public void savePreferences(int cityName, int cityCode, int county, int state, int country, int juridictionNb) { 
            cityNameJuridiction = cityName;
            cityCodeJuridiction = cityCode;
            countyJuridiction = county;
            stateJuridiction = state;
            countryJuridiction = country;
            juridictionNumber  = juridictionNb;
            
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + CITYNAME_JURIDICTION, String.valueOf(cityNameJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + CITYCODE_JURIDICTION, String.valueOf(cityCodeJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + COUNTY_JURIDICTION,   String.valueOf(countyJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + STATE_JURIDICTION,    String.valueOf(stateJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + COUNTRY_JURIDICTION,  String.valueOf(countryJuridiction));
            NbPreferences.forModule(PlaceFormatModel.class).put(gedcomName + JURIDICTION_NUMBER,   String.valueOf(juridictionNumber));
        }
        

    }
