package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
import ancestris.api.place.SearchPlace;
import ancestris.libs.geonames.GeonamesOptions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import org.geonames.PostalCode;
import org.geonames.PostalCodeSearchCriteria;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author frederic & dominique
 */
public class GeonamesResearcher implements SearchPlace {

    private final static Toponym DEFAULT_TOPONYM = defaultToponym();

    private static final int MAX_ROWS = 20; // Maximum return by geonames.

    private final static Logger LOG = Logger.getLogger(GeonamesResearcher.class.getName(), null);
    private RequestProcessor.Task theTask;
    private RequestProcessor RP = null;

    private final static String KEYMAP = "geonamesPlaceConversionMap";

    private CountryBias countryBias = null;
    
    private String username = "";

    
    
    
    /**
     * Constructor
     */
    public GeonamesResearcher() {
        if (RP == null) {
            RP = new RequestProcessor("GeonamesResearcher", 1, true);
        }
        username = GeonamesOptions.getInstance().getUserName();
        WebService.setUserName(username);
        countryBias = new CountryBias();
    }

    public RequestProcessor.Task getTask() {
        return theTask;
    }

     /** 
     * @param placePieces
     * @param city
     * @param defaultPlace
     * @return
     */
    @Override
    public Place searchMassPlace(String placePieces, String city, Place defaultPlace) {

        Place retPlace = defaultPlace;

        // Set criteria
        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setStyle(Style.FULL);
        searchCriteria.setLanguage(Locale.getDefault().getLanguage().substring(0, 2));
        searchCriteria.setMaxRows(1);
        searchCriteria.setQ(placePieces);
        String bias = countryBias.getValue();
        if (!bias.isEmpty()) {
            searchCriteria.setCountryBias(bias);
        }

        boolean found = false;
        int cnt = 1;
        while (!found && cnt <= 2) {
            // Run web service search
            ToponymSearchResult toponymSearchResult = null;
            try {
                toponymSearchResult = WebService.search(searchCriteria);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("user does not exist")) {
                    DialogManager.createError(NbBundle.getMessage(GeonamesResearcher.class, "TITL_ErrorUser"), NbBundle.getMessage(GeonamesResearcher.class, "MESS_ErrorUser", username)).show();
                } else if (e.getMessage() != null && e.getMessage().contains("user account not enabled")) {
                    DialogManager.createError(NbBundle.getMessage(GeonamesResearcher.class, "TITL_ErrorEnabled"), NbBundle.getMessage(GeonamesResearcher.class, "MESS_ErrorEnabled", username)).show();
                } else if (e.getMessage() != null && e.getMessage().contains("hourly limit")) {
                    DialogManager.createError(NbBundle.getMessage(GeonamesResearcher.class, "TITL_ErrorLimit"), NbBundle.getMessage(GeonamesResearcher.class, "MESS_ErrorLimit", username)).show();
                } else {
                    DialogManager.createError(NbBundle.getMessage(GeonamesResearcher.class, "TITL_ErrorOther"), NbBundle.getMessage(GeonamesResearcher.class, "MESS_ErrorOther", e.getMessage())).show();
                    LOG.log(Level.SEVERE, "Error during geonames search.", e);
                }
                retPlace = null;
                break;
            }
            
            // Format result
            if (toponymSearchResult != null) {
                for (Toponym iTopo : toponymSearchResult.getToponyms()) {
                    if (iTopo.getCountryCode() == null || iTopo.getCountryCode().trim().isEmpty() || iTopo.getName() == null || iTopo.getName().trim().isEmpty()) {
                        break;
                    }
                    retPlace = new GeonamesPlace(iTopo);
                    countryBias.add(iTopo.getCountryCode());
                    found = true;
                }
            }
            if (!found) {
                searchCriteria.setQ(city);
                cnt++;
            }
        }

        return retPlace;
    }

    /**
     * Look for all places on the internet matching searchPlace and add them to placeList
     *
     * @param placePieces
     * @param placesList : list to which results will be added
     * @param taskListener : search is performed through another thread and task is used when thread is finished
     * @return 
     */
    @Override
    public void searchIndividualPlace(String placePieces, final List<Place> placesList, TaskListener taskListener) {

        String city = getFirstPiece(placePieces);
        
        Runnable runnable = () -> {
            try {
                // First search
                ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
                searchCriteria.setStyle(Style.FULL);
                searchCriteria.setLanguage(Locale.getDefault().getLanguage().substring(0, 2));
                searchCriteria.setMaxRows(MAX_ROWS);
                searchCriteria.setQ(placePieces);
                ToponymSearchResult toponymSearchResult = WebService.search(searchCriteria);
                for (Toponym iTopo : toponymSearchResult.getToponyms()) {
                    if (iTopo.getCountryCode() == null || iTopo.getCountryCode().trim().isEmpty() || iTopo.getName() == null || iTopo.getName().trim().isEmpty()) {
                        continue;
                    }
                    placesList.add(new GeonamesPlace(iTopo));
                }
                
                // Second search with city only
                if (placesList.isEmpty()) {
                    searchCriteria.setQ(city);
                    toponymSearchResult = WebService.search(searchCriteria);
                    for (Toponym iTopo : toponymSearchResult.getToponyms()) {
                        if (iTopo.getCountryCode() == null || iTopo.getCountryCode().trim().isEmpty() || iTopo.getName() == null || iTopo.getName().trim().isEmpty()) {
                            continue;
                        }
                        placesList.add(new GeonamesPlace(iTopo));
                    }
                }
                
                // Fill in postal codes
                if (GeonamesOptions.getInstance().searchPostalCodes()) {
                    for (Place place : placesList) {
                        PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
                        postalCodeSearchCriteria.setStyle(Style.SHORT);
                        postalCodeSearchCriteria.setMaxRows(30);
                        String criteria = place.getName() + " " + place.getCountryCode();  //+ place.getAdminCode(1) + " " 
                        postalCodeSearchCriteria.setPlaceName(criteria);
                        List<PostalCode> postalCodeSearch = WebService.postalCodeSearch(postalCodeSearchCriteria);
                        for (PostalCode pc : postalCodeSearch) {
                            // The issue is that geonames returns lots of places with different names where the place name appears somewhere else in the description,
                            // but in alphabetical order of place names
                            // So we have to pick the right one (close to the center of with the exact name
                            // This should work worldwide
                            if (pc.getPlaceName().equals(place.getName())) {
                                ((GeonamesPlace) place).setPostalCode(pc);
                                break;
                            }
                            if (pc.getLatitude() != Double.NaN && pc.getLongitude() != Double.NaN) {
                                if ((Math.abs(place.getLatitude() - pc.getLatitude()) < 0.1) && (Math.abs(place.getLongitude() - pc.getLongitude()) < 0.1)) {
                                    ((GeonamesPlace) place).setPostalCode(pc);
                                   // keep looking
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("hourly limit")) {
                    DialogManager dm = DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_ErrorLimit"), NbBundle.getMessage(GeonamesResearcher.class, "MESS_ErrorLimit", username));
                    dm.show();
                } else {
                    LOG.log(Level.SEVERE, "Error during geonames search.", e);
                }
            }
            
        };

        theTask = RP.create(runnable); //the task is not started yet
        theTask.addTaskListener(taskListener);
        theTask.schedule(0); //start the task
    }

    @Override
    public Place searchNearestPlace(double latitude, double longitude) {
        WebService.setUserName(GeonamesOptions.getInstance().getUserName());
        try {
            List<Toponym> results = WebService.findNearbyPlaceName(latitude, longitude);
            if (!results.isEmpty()) {
                int geonameId = results.get(0).getGeoNameId();
                Toponym toponym = WebService.get(geonameId, null, null);
                return new GeonamesPlace(toponym);
            }
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return null;
    }

    
    
    
    
    public static String getGeonamesMapString(Gedcom gedcom) {
        return gedcom.getRegistry().get(KEYMAP, "");
    }

    public static String[] getGeonamesMap(Gedcom gedcom) {
        String[] format = null;
        String placeMap = getGeonamesMapString(gedcom);
        PlaceFormatConverterPanel pfc = new PlaceFormatConverterPanel(GeonamesPlace.getPlaceFormat(), gedcom.getPlaceFormat(), placeMap);
        pfc.setTextTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionTitle"));
        pfc.setLeftTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionLeftTitle"));
        pfc.setRightTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionRightTitle"));
        // Display parameter panel asking user to map geonames fields to his/her gedcom fields
        Object o = DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), pfc).setMessageType(DialogManager.PLAIN_MESSAGE).show();
        if (o == DialogManager.OK_OPTION) {
            final String map = pfc.getConversionMapAsString();
            if (!map.replace(PropertyPlace.JURISDICTION_SEPARATOR, "").trim().isEmpty()) {
                gedcom.getRegistry().put(KEYMAP, map);
                format = PropertyPlace.getFormat(map);
            } else {
                if (placeMap.isEmpty()) {
                    DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), NbBundle.getMessage(GeonamesResearcher.class, "ERR_EmptyConversion"))
                            .setMessageType(DialogManager.ERROR_MESSAGE).show();
                } else {
                    DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), NbBundle.getMessage(GeonamesResearcher.class, "ERR_NothingSaved"))
                            .setMessageType(DialogManager.ERROR_MESSAGE).show();
                }
            }
        }
        return format;
    }

    /**
     * Extract first word
     *
     * @param place
     * @return
     */
    private String getFirstPiece(String text) {
        String[] bits = text.split(" ");
        for (String bit : bits) {
            String str = bit.replaceAll("[0-9]", "");
            if (str.length() > 1) {
                return str;
            }
        }
        return text;
    }

    /**
     * Extract first numerical code from string
     *
     * @param place
     * @return
     */
    private String getFirstCode(String text) {
        String[] bits = text.split(" ");
        for (String bit : bits) {
            String str = bit.replaceAll("[^0-9]", "");
            if (str.length() > 1) {
                return str;
            }
        }
        return "";
    }

    /**
     * Defines a default toponym pointing in the middle of the sea
     *
     * @return
     */
    public Place defaultPlace() {
        return new GeonamesPlace(DEFAULT_TOPONYM);
    }

    private static Toponym defaultToponym() {
        Toponym topo = new Toponym();
        topo.setLatitude(PlaceFactory.DEFAULT_LAT);
        topo.setLongitude(PlaceFactory.DEFAULT_LON);
        topo.setPopulation(Long.getLong("0"));
        return topo;
    }




    /**
     * Classes
     */

    private static class CountryBias {

        private HashMap<String, Integer> map = new HashMap<>();

        public void add(String country) {
            Integer count = map.get(country);
            if (count == null) {
                count = 0;
            }
            count++;
            map.put(country, count);
        }

        public String getValue() {
            int max = 0;
            String countryBias = "";
            for (String str : map.keySet()) {
                if (map.get(str) > max) {
                    max = map.get(str);
                    countryBias = str;
                }
            }
            return countryBias;
        }

    }

}
